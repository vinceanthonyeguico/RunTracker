/**
 * <h1>StatementGenerator</h1>
 *
 * <p>Generate code for executable statements.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package backend.compiler;

import intermediate.antlr4.MonkeyCParser;
import intermediate.symtab.*;
import intermediate.type.*;
import intermediate.type.Typespec.Form;
import static intermediate.type.Typespec.Form.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static backend.compiler.Instruction.*;

public class StatementGenerator
{
    private Compiler compiler;
    private CodeEmitter emitter;
    private String programName;
    
    /**
     * Constructor.
     * @param parent the parent generator.
     * @param compiler the compiler to use.
     */
    public StatementGenerator(Compiler compiler)
    {
        this.compiler = compiler;
        emitter = compiler.getCodeEmitter();
        programName = emitter.getProgramName();
        //localStack = emitter.getLocalStack();
    }

    /**
     * Emit code for an assignment statement.
     * @param ctx the AssignmentStatementContext.
     */
    public void emitAssignment(MonkeyCParser.AssignmentStatementContext ctx)
    {
        MonkeyCParser.VariableContext   varCtx  = ctx.lhs().variable();
        MonkeyCParser.ExpressionContext exprCtx = ctx.rhs().expression();
        SymtabEntry varEntry = varCtx.entry;
        Typespec varType = varCtx.type;
        Typespec exprType = exprCtx.type;

        // The last modifier, if any, is the variable's last subscript or field.
        int modifierCount = varCtx.modifier().size();
        MonkeyCParser.ModifierContext lastModCtx = modifierCount == 0
                            ? null : varCtx.modifier().get(modifierCount - 1);

        // The target variable has subscripts and/or fields.
        if (modifierCount > 0) 
        {
            lastModCtx = varCtx.modifier().get(modifierCount - 1);
            compiler.visit(varCtx);
        }
        
        // Emit code to evaluate the expression.
        compiler.visit(exprCtx);
        
        // float variable := integer constant
        if (   (varType == Predefined.realType)
            && (exprType.baseType() == Predefined.integerType)) emitter.emit(I2D);
        
        // Emit code to store the expression value into the target variable.
        // The target variable has no subscripts or fields.
        if (lastModCtx == null) emitter.emitStoreValue(varEntry, varEntry.getType());

        // The target variable is a field.
        else if (lastModCtx.field() != null)
        {
            emitter.emitStoreValue(lastModCtx.field().entry, 
                                   lastModCtx.field().type);
        }

        // The target variable is an array element.
        else
        {
            emitter.emitStoreValue(null, varType);
        }
    }

    /**
     * Emit code for an IF statement.
     * @param ctx the IfStatementContext.
     */
    public void emitIf(MonkeyCParser.IfStatementContext ctx)
    {
    	Label falseLabel = new Label();
    	Label endLabel = new Label();
    	
        compiler.visit(ctx.expression());
        
        // If expression is false, goto false label if it exists
        // Otherwise goto end label
        emitter.emit(IFEQ, (ctx.falseStatement() != null) ? falseLabel : endLabel);
        compiler.visit(ctx.trueStatement());
        emitter.emit(GOTO, endLabel);
        
        if (ctx.falseStatement() != null) {
            emitter.emitLabel(falseLabel);
            compiler.visit(ctx.falseStatement());	
        }
        
        emitter.emitLabel(endLabel);
    }
    
    /**
     * Emit code for a CASE statement.
     * @param ctx the CaseStatementContext.
     */
    public void emitCase(MonkeyCParser.CaseStatementContext ctx)
    {
    	// Gather mappings between CaseConst:Label and Label:Statement
        HashMap<Integer, Label> constLookup = new HashMap<>();
        HashMap<Label, MonkeyCParser.StatementContext> labelLookup = new HashMap<>();
        createSwitchLookup(ctx.caseBranchList(), constLookup, labelLookup);
        
        // Get the case constants in sorted order
        ArrayList<Integer> sortedCaseConsts = new ArrayList<>();
        for (int caseConst : constLookup.keySet()) {
        	sortedCaseConsts.add(caseConst);
        }
        Collections.sort(sortedCaseConsts);
        
        
        
        // Load value to check in case
        compiler.visit(ctx.expression());
        
        // Emit a value:label pair for each caseConst
        emitter.emit(LOOKUPSWITCH);
        emitter.emitLine();
        for (int caseConst : sortedCaseConsts) {
        	Label branchLabel = constLookup.get(caseConst);
        	emitter.emitCaseConst(Integer.toString(caseConst), branchLabel);
        }
        
        // Emit a default case branch
        Label defaultLabel = new Label();
        emitter.emitCaseConst("default", defaultLabel);
        
        // Emit the labels for each branch
        for (Label branchLabel : labelLookup.keySet()) {
        	emitter.emitLabel(branchLabel);
        	
        	MonkeyCParser.StatementContext branchStmt = labelLookup.get(branchLabel);
        	compiler.visit(branchStmt);
        	emitter.emit(GOTO, defaultLabel);
        }
        
        // Emit default label
        emitter.emitLabel(defaultLabel);
    }
    
    /**
     * Fills in the case constant and label statement mappings for a case statement
     * @param ctx the CaseBranchListContext
     * @param constLookup a pointer to an empty hashmap which will be filled in with constant:label mappings
     * @param labelLookup a pointer to an empty hashmap which will be filled in with label:statement mappings
     */
    public void createSwitchLookup(MonkeyCParser.CaseBranchListContext ctx, HashMap<Integer, Label> constLookup,
    		HashMap<Label, MonkeyCParser.StatementContext> labelLookup) 
    {    	
    	// Loop over each branch
    	for (MonkeyCParser.CaseBranchContext branchCtx : ctx.caseBranch()) {
    		// Check if branch has any constants
    		MonkeyCParser.CaseConstantListContext caseConstListCtx = branchCtx.caseConstantList();
    		if (caseConstListCtx == null) {
    			continue;
    		}
    		
    		// Each constant in the branch gets assigned to the same label
    		Label branchLabel = new Label();
    		    		
    		// Each label gets assigned to one statement
    		MonkeyCParser.StatementContext stmtCtx = branchCtx.statement();
    		labelLookup.put(branchLabel, stmtCtx);
    		
    		// Map each constant to its branch label
    		for (MonkeyCParser.CaseConstantContext caseConstCtx : caseConstListCtx.caseConstant()) {
    			constLookup.put(caseConstCtx.value, branchLabel);
    		}
    	}
    }

    /**
     * Emit code for a REPEAT statement.
     * @param ctx the RepeatStatementContext.
     */
    public void emitRepeat(MonkeyCParser.RepeatStatementContext ctx)
    {
        Label loopTopLabel  = new Label();
        Label loopExitLabel = new Label();

        emitter.emitLabel(loopTopLabel);
        
        compiler.visit(ctx.statementList());
        compiler.visit(ctx.expression());
        emitter.emit(IFNE, loopExitLabel);
        emitter.emit(GOTO, loopTopLabel);
        
        emitter.emitLabel(loopExitLabel);
    }
    
    /**
     * Emit code for a WHILE statement.
     * @param ctx the WhileStatementContext.
     */
    public void emitWhile(MonkeyCParser.WhileStatementContext ctx)
    {
        Label loopTopLabel  = new Label();
        Label loopExitLabel = new Label();
        
        emitter.emitLabel(loopTopLabel);
        
        compiler.visit(ctx.expression());
        emitter.emit(IFEQ, loopExitLabel);
        compiler.visit(ctx.statement());
        emitter.emit(GOTO, loopTopLabel);
        
        emitter.emitLabel(loopExitLabel);
    }
    
    /**
     * Emit code for a FOR statement.
     * @param ctx the ForStatementContext.
     */
    public void emitFor(MonkeyCParser.ForStatementContext ctx)
    {
    	Label loopTopLabel = new Label();
    	Label loopExitLabel = new Label();
    	
    	boolean isIncreasingLoop = ctx.TO() != null;
    	MonkeyCParser.VariableContext iterator = ctx.variable();
    	
    	// Assign iterator to new value
    	compiler.visit(ctx.expression(0));
    	emitter.emitStoreValue(iterator.entry, ctx.variable().type);
    	
    	// Start of loop
    	emitter.emitLabel(loopTopLabel);
    	
    	// Goto end label once iterator is out of bounds
    	emitter.emitLoadValue(iterator.entry);
    	compiler.visit(ctx.expression(1));
    	if (isIncreasingLoop) {
    		emitter.emit(IF_ICMPGT, loopExitLabel);
    	}
    	else {
    		emitter.emit(IF_ICMPLT, loopExitLabel);
    	}
    	
    	// Execute loop body
    	compiler.visit(ctx.statement());
    	
    	// Iterate iterator
    	emitter.emitLoadValue(iterator.entry);
    	emitter.emitLoadConstant(1);
    	if (isIncreasingLoop) {
    		emitter.emit(IADD);
    	}
    	else {
    		emitter.emit(ISUB);;
    	}
    	emitter.emitStoreValue(iterator.entry, iterator.type);
    	emitter.emit(GOTO, loopTopLabel);
    	
    	// End label
    	emitter.emitLabel(loopExitLabel);
    }
    
    /**
     * Emit code for a procedure call statement.
     * @param ctx the ProcedureCallStatementContext.
     */
    public void emitProcedureCall(MonkeyCParser.ProcedureCallStatementContext ctx)
    {
    	// Get the symbol table entry for the procedure
    	SymtabEntry procEntry = ctx.procedureName().entry;    	
    	String procName = programName + "/" + procEntry.getName();

    	// Get the type descriptors for the parameters
    	ArrayList<String> argTypes = new ArrayList<>();
    	ArrayList<SymtabEntry> paramSymtabs = procEntry.getRoutineParameters();
    	for (SymtabEntry symtab : paramSymtabs) {
    		argTypes.add(emitter.typeDescriptor(symtab));
    	}
    	
    	
    	// Put call arguments at top of stack
    	MonkeyCParser.ArgumentListContext argListCtx = ctx.argumentList();
    	if (argListCtx != null) {
            for (int i = 0; i < argListCtx.argument().size(); i++) {
            	MonkeyCParser.ArgumentContext argCtx = ctx.argumentList().argument(i);
            	SymtabEntry paramSymtab = paramSymtabs.get(i);
            		
            	compiler.visit(argCtx.expression());
            		
            	// Cast argument to correct parameter type
            	Typespec paramType = paramSymtab.getType();
            	Typespec argType = argCtx.expression().type;
            	if (argType == Predefined.integerType && paramType == Predefined.realType) {
            		emitter.emit(I2D);
            	}
            }	
    	}
    	
    	// Get the full signature for the procedure call
    	String procCall = procName + "(";
    	for (String argType : argTypes) {
    		procCall += argType;
    	}
    	procCall += ")V";
    	
    	emitter.emit(INVOKESTATIC, procCall);
    }
    
    /**
     * Emit code for a function call statement.
     * @param ctx the FunctionCallContext.
     */
    public void emitFunctionCall(MonkeyCParser.FunctionCallContext ctx)
    {
    	// Get the symbol table entry for the procedure
    	SymtabEntry funcEntry = ctx.functionName().entry;    	
    	String funcName = programName + "/" + funcEntry.getName();

    	// Get the type descriptors for the parameters
    	ArrayList<String> argTypes = new ArrayList<>();
    	ArrayList<SymtabEntry> paramSymtabs = funcEntry.getRoutineParameters();
    	for (SymtabEntry symtab : paramSymtabs) {
    		argTypes.add(emitter.typeDescriptor(symtab));
    	}
    	
    	
    	// Put call arguments at top of stack
    	MonkeyCParser.ArgumentListContext argListCtx = ctx.argumentList();
    	if (argListCtx != null) {
            for (int i = 0; i < argListCtx.argument().size(); i++) {
            	MonkeyCParser.ArgumentContext argCtx = ctx.argumentList().argument(i);
            	SymtabEntry paramSymtab = paramSymtabs.get(i);
            		
            	compiler.visit(argCtx.expression());
            		
            	// Cast argument to correct parameter type
            	Typespec paramType = paramSymtab.getType();
            	Typespec argType = argCtx.expression().type;
            	if (argType == Predefined.integerType && paramType == Predefined.realType) {
            		emitter.emit(I2D);
            	}
            }	
    	}
    	
    	// Get the full signature for the procedure call
    	String funcCall = funcName + "(";
    	for (String argType : argTypes) {
    		funcCall += argType;
    	}
    	funcCall += ")";
    	// Add function return type at the end of signature
    	funcCall += emitter.typeDescriptor(funcEntry.getType());
    	
    	emitter.emit(INVOKESTATIC, funcCall);
    }

    /**
     * Emit code for a WRITE statement.
     * @param ctx the WriteStatementContext.
     */
    public void emitWrite(MonkeyCParser.WriteStatementContext ctx)
    {
        emitWrite(ctx.writeArguments(), false);
    }

    /**
     * Emit code for a WRITELN statement.
     * @param ctx the WritelnStatementContext.
     */
    public void emitWriteln(MonkeyCParser.WritelnStatementContext ctx)
    {
        emitWrite(ctx.writeArguments(), true);
    }

    /**
     * Emit code for a call to WRITE or WRITELN.
     * @param argsCtx the WriteArgumentsContext.
     * @param needLF true if need a line feed.
     */
    private void emitWrite(MonkeyCParser.WriteArgumentsContext argsCtx,
                           boolean needLF)
    {
        emitter.emit(GETSTATIC, "java/lang/System/out", "Ljava/io/PrintStream;");

        // WRITELN with no arguments.
        if (argsCtx == null) 
        {
            emitter.emit(INVOKEVIRTUAL, "java/io/PrintStream.println()V");

            LocalStack localStack = emitter.getLocalStack();
            localStack.decrease(1);
        }
            
        // Generate code for the arguments.
        else
        {
            StringBuffer format = new StringBuffer();
            int exprCount = createWriteFormat(argsCtx, format, needLF);
            LocalStack localStack = emitter.getLocalStack();
            
            // Load the format string.
            emitter.emit(LDC, format.toString());
            
            // Emit the arguments array.
            if (exprCount > 0)
            {
                emitArgumentsArray(argsCtx, exprCount);

                emitter.emit(INVOKEVIRTUAL,
                             "java/io/PrintStream/printf(Ljava/lang/String;" +
                                 "[Ljava/lang/Object;)" +
                             "Ljava/io/PrintStream;");
                localStack.decrease(2);
                emitter.emit(POP);
            }
            else
            {
                emitter.emit(INVOKEVIRTUAL,
                             "java/io/PrintStream/print(Ljava/lang/String;)V");
                localStack.decrease(2);
            }
        }
    }
    
    /**
     * Create the printf format string.
     * @param argsCtx the WriteArgumentsContext.
     * @param format the format string to create.
     * @return the count of expression arguments.
     */
    private int createWriteFormat(MonkeyCParser.WriteArgumentsContext argsCtx,
                                  StringBuffer format, boolean needLF)
    {
        int exprCount = 0;
        format.append("\"");
        
        // Loop over the write arguments.
        for (MonkeyCParser.WriteArgumentContext argCtx : argsCtx.writeArgument())
        {
            Typespec type = argCtx.expression().type;
            String argText = argCtx.getText();
            
            // Append any literal strings.
            if (argText.charAt(0) == '\'') 
            {
                format.append(emitter.convertString(argText));
            }
            
            // For any other expressions, append a field specifier.
            else
            {
                exprCount++;
                format.append("%");
                
                MonkeyCParser.FieldWidthContext fwCtx = argCtx.fieldWidth();              
                if (fwCtx != null)
                {
                    String sign = (   (fwCtx.sign() != null) 
                                   && (fwCtx.sign().getText().equals("-"))) 
                                ? "-" : "";
                    format.append(sign)
                          .append(fwCtx.integerConstant().getText());
                    
                    MonkeyCParser.DecimalPlacesContext dpCtx = 
                                                        fwCtx.decimalPlaces();
                    if (dpCtx != null)
                    {
                        format.append(".")
                              .append(dpCtx.integerConstant().getText());
                    }
                }
                
                String typeFlag = type == Predefined.integerType ? "d" 
                                : type == Predefined.realType    ? "f" 
                                : type == Predefined.booleanType ? "b" 
                                : type == Predefined.charType    ? "c" 
                                :                                  "s";
                format.append(typeFlag);
            }
        }
        
        format.append(needLF ? "\\n\"" : "\"");
 
        return exprCount;
    }
    
    /**
     * Emit the printf arguments array.
     * @param argsCtx
     * @param exprCount
     */
    private void emitArgumentsArray(MonkeyCParser.WriteArgumentsContext argsCtx,
                                    int exprCount)
    {
        // Create the arguments array.
        emitter.emitLoadConstant(exprCount);
        emitter.emit(ANEWARRAY, "java/lang/Object");

        int index = 0;

        // Loop over the write arguments to fill the arguments array.
        for (MonkeyCParser.WriteArgumentContext argCtx : 
                                                    argsCtx.writeArgument())
        {
            String argText = argCtx.getText();
            MonkeyCParser.ExpressionContext exprCtx = argCtx.expression();
            Typespec type = exprCtx.type.baseType();
            
            // Skip string constants, which were made part of
            // the format string.
            if (argText.charAt(0) != '\'') 
            {
                emitter.emit(DUP);
                emitter.emitLoadConstant(index++);

                compiler.visit(exprCtx);

                Form form = type.getForm();
                if (    ((form == SCALAR) || (form == ENUMERATION))
                     && (type != Predefined.stringType))
                {
                    emitter.emit(INVOKESTATIC, emitter.valueOfSignature(type));
                }

                // Store the value into the array.
                emitter.emit(AASTORE);
            }
        }
    }

    /**
     * Emit code for a READ statement.
     * @param ctx the ReadStatementContext.
     */
    public void emitRead(MonkeyCParser.ReadStatementContext ctx)
    {
        emitRead(ctx.readArguments(), false);
    }

    /**
     * Emit code for a READLN statement.
     * @param ctx the ReadlnStatementContext.
     */
    public void emitReadln(MonkeyCParser.ReadlnStatementContext ctx)
    {
        emitRead(ctx.readArguments(), true);
    }

    /**
     * Generate code for a call to READ or READLN.
     * @param argsCtx the ReadArgumentsContext.
     * @param needSkip true if need to skip the rest of the input line.
     */
    private void emitRead(MonkeyCParser.ReadArgumentsContext argsCtx,
                          boolean needSkip)
    {
        int size = argsCtx.variable().size();
        
        // Loop over read arguments.
        for (int i = 0; i < size; i++)
        {
            MonkeyCParser.VariableContext varCtx = argsCtx.variable().get(i);
            Typespec varType = varCtx.type;
            
            if (varType == Predefined.integerType)
            {
                emitter.emit(GETSTATIC, 
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL, "java/util/Scanner/nextInt()I");
                emitter.emitStoreValue(varCtx.entry, null);
            }
            else if (varType == Predefined.realType)
            {
                emitter.emit(GETSTATIC, 
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL, "java/util/Scanner/nextFloat()F");
                emitter.emitStoreValue(varCtx.entry, null);
            }
            else if (varType == Predefined.booleanType)
            {
                emitter.emit(GETSTATIC, 
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL, "java/util/Scanner/nextBoolean()Z");
                emitter.emitStoreValue(varCtx.entry, null);
            }
            else if (varType == Predefined.charType)
            {
                emitter.emit(GETSTATIC, 
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(LDC, "\"\"");
                emitter.emit(INVOKEVIRTUAL, 
                         "java/util/Scanner/useDelimiter(Ljava/lang/String;)" +
                                                        "Ljava/util/Scanner;");
                emitter.emit(POP);                
                emitter.emit(GETSTATIC,
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL, 
                             "java/util/Scanner/next()Ljava/lang/String;");
                emitter.emit(ICONST_0);           
                emitter.emit(INVOKEVIRTUAL, "java/lang/String/charAt(I)C");
                emitter.emitStoreValue(varCtx.entry, null);
                
                emitter.emit(GETSTATIC, 
                              programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL, 
                             "java/util/Scanner/reset()Ljava/util/Scanner;");

            }
            else  // string
            {
                emitter.emit(GETSTATIC, 
                             programName + "/_sysin Ljava/util/Scanner;");
                emitter.emit(INVOKEVIRTUAL,
                             "java/util/Scanner/next()Ljava/lang/String;");
                emitter.emitStoreValue(varCtx.entry, null);
            }
        }

        // READLN: Skip the rest of the input line.
        if (needSkip) 
        {
            emitter.emit(GETSTATIC, programName + "/_sysin Ljava/util/Scanner;");
            emitter.emit(INVOKEVIRTUAL, 
                         "java/util/Scanner/nextLine()Ljava/lang/String;");
            emitter.emit(POP);                 
        }
    }
}