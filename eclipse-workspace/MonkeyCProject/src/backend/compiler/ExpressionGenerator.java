/**
 * <h1>ExpressionGenerator</h1>
 *
 * <p>Generate code for an expression.</p>
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

import static backend.compiler.Instruction.*;

public class ExpressionGenerator
{
    private Compiler compiler;
    private CodeEmitter emitter;

    /**
     * Constructor.
     * @param the parent executor.
     */
    public ExpressionGenerator(Compiler compiler)
    {
        this.compiler = compiler;
        emitter = compiler.getCodeEmitter();
    }
    
    /**
     * Emit code for an expression.
     * @param ctx the ExpressionContext.
     */
    public void emitExpression(MonkeyCParser.ExpressionContext ctx)
    {
        MonkeyCParser.SimpleExpressionContext simpleCtx1 = 
                                                ctx.simpleExpression().get(0);
        MonkeyCParser.RelOpContext relOpCtx = ctx.relOp();
        Typespec type1 = simpleCtx1.type;
        LocalStack localStack = emitter.getLocalStack();
        
        emitSimpleExpression(simpleCtx1);
        
        // More than one simple expression?
        if (relOpCtx != null)
        {
            String op = relOpCtx.getText();
            MonkeyCParser.SimpleExpressionContext simpleCtx2 = 
                                                ctx.simpleExpression().get(1);
            Typespec type2 = simpleCtx2.type;

            boolean integerMode   = false;
            boolean realMode      = false;
            boolean characterMode = false;

            if (   (type1 == Predefined.integerType)
                && (type2 == Predefined.integerType)) 
            {
                integerMode = true;
            }
            else if (   (type1 == Predefined.realType) 
                     || (type2 == Predefined.realType))
            {
                realMode = true;
            }
            else if (   (type1 == Predefined.charType) 
                     && (type2 == Predefined.charType))
            {
                characterMode = true;
            }

            Label trueLabel = new Label();
            Label exitLabel = new Label();

            if (integerMode || characterMode) 
            {
                emitSimpleExpression(simpleCtx2);
                
                if      (op.equals("=" )) emitter.emit(IF_ICMPEQ, trueLabel);
                else if (op.equals("<>")) emitter.emit(IF_ICMPNE, trueLabel);
                else if (op.equals("<" )) emitter.emit(IF_ICMPLT, trueLabel);
                else if (op.equals("<=")) emitter.emit(IF_ICMPLE, trueLabel);
                else if (op.equals(">" )) emitter.emit(IF_ICMPGT, trueLabel);
                else if (op.equals(">=")) emitter.emit(IF_ICMPGE, trueLabel);
            }
            else if (realMode)
            {
                if (type1 == Predefined.integerType) emitter.emit(I2D);
                emitSimpleExpression(simpleCtx2);
                if (type2 == Predefined.integerType) emitter.emit(I2D);
                
                emitter.emit(DCMPG);

                if      (op.equals("=" )) emitter.emit(IFEQ, trueLabel);
                else if (op.equals("<>")) emitter.emit(IFNE, trueLabel);
                else if (op.equals("<" )) emitter.emit(IFLT, trueLabel);
                else if (op.equals("<=")) emitter.emit(IFLE, trueLabel);
                else if (op.equals(">" )) emitter.emit(IFGT, trueLabel);
                else if (op.equals(">=")) emitter.emit(IFGE, trueLabel);
            }
            else  // stringMode
            {
                emitSimpleExpression(simpleCtx2);
                emitter.emit(INVOKEVIRTUAL,
                             "java/lang/String.compareTo(Ljava/lang/String;)I");
                localStack.decrease(1);
                
                if      (op.equals("=" )) emitter.emit(IFEQ, trueLabel);
                else if (op.equals("<>")) emitter.emit(IFNE, trueLabel);
                else if (op.equals("<" )) emitter.emit(IFLT, trueLabel);
                else if (op.equals("<=")) emitter.emit(IFLE, trueLabel);
                else if (op.equals(">" )) emitter.emit(IFGT, trueLabel);
                else if (op.equals(">=")) emitter.emit(IFGE, trueLabel);
            }

            emitter.emit(ICONST_0); // false
            emitter.emit(GOTO, exitLabel);
            emitter.emitLabel(trueLabel);
            emitter.emit(ICONST_1); // true
            emitter.emitLabel(exitLabel);
            
            localStack.decrease(1);  // only one branch will be taken
        }
    }
    
    /**
     * Emit code for a simple expression.
     * @param ctx the SimpleExpressionContext.
     */
    public void emitSimpleExpression(MonkeyCParser.SimpleExpressionContext ctx)
    {
        int count = ctx.term().size();
        Boolean negate =    (ctx.sign() != null) 
                         && ctx.sign().getText().equals("-");
        
        // First term.
        MonkeyCParser.TermContext termCtx1 = ctx.term().get(0);
        Typespec type1 = termCtx1.type;
        emitTerm(termCtx1);
        
        if (negate) emitter.emit(type1 == Predefined.integerType ? INEG : DNEG);
        
        // Loop over the subsequent terms.
        for (int i = 1; i < count; i++)
        {
            String op = ctx.addOp().get(i-1).getText().toLowerCase();
            MonkeyCParser.TermContext termCtx2 = ctx.term().get(i);
            Typespec type2 = termCtx2.type;
            LocalStack localStack = emitter.getLocalStack();

            boolean integerMode = false;
            boolean realMode    = false;
            boolean booleanMode = false;

            if (   (type1 == Predefined.integerType)
                && (type2 == Predefined.integerType)) 
            {
                integerMode = true;
            }
            else if (   (type1 == Predefined.realType) 
                     || (type2 == Predefined.realType))
            {
                realMode = true;
            }
            else if (   (type1 == Predefined.booleanType) 
                     && (type2 == Predefined.booleanType))
            {
                booleanMode = true;
            }
                            
            if (integerMode)
            {
                emitTerm(termCtx2);
                
                if (op.equals("+")) emitter.emit(IADD);
                else                emitter.emit(ISUB);
            }
            else if (realMode)
            {
                if (type1 == Predefined.integerType) emitter.emit(I2D);
                emitTerm(termCtx2);
                if (type2 == Predefined.integerType) emitter.emit(I2D);
                
                if (op.equals("+")) emitter.emit(DADD);
                else                emitter.emit(DSUB);
            }
            else if (booleanMode)
            {
                emitTerm(termCtx2);
                emitter.emit(IOR);
            }
            else  // stringMode
            {
                emitter.emit(NEW, "java/lang/StringBuilder");
                emitter.emit(DUP_X1);             
                emitter.emit(SWAP);                  
                emitter.emit(INVOKESTATIC, 
                             "java/lang/String/valueOf(Ljava/lang/Object;)" +
                             "Ljava/lang/String;");
                emitter.emit(INVOKESPECIAL, "java/lang/StringBuilder/<init>" +
                                            "(Ljava/lang/String;)V");
                localStack.decrease(1);
                
                emitTerm(termCtx2);
                emitter.emit(INVOKEVIRTUAL, 
                             "java/lang/StringBuilder/append(Ljava/lang/String;)"
                           + "Ljava/lang/StringBuilder;");
                localStack.decrease(1);
                emitter.emit(INVOKEVIRTUAL, 
                             "java/lang/StringBuilder/toString()" +
                             "Ljava/lang/String;");
                localStack.decrease(1);
            }
        }
    }
    
    /**
     * Emit code for a term.
     * @param ctx the TermContext.
     */
    public void emitTerm(MonkeyCParser.TermContext ctx)
    {
        int count = ctx.factor().size();
        
        // First factor.
        MonkeyCParser.FactorContext factorCtx1 = ctx.factor().get(0);
        Typespec type1 = factorCtx1.type;
        compiler.visit(factorCtx1);
        
        // Loop over the subsequent factors.
        for (int i = 1; i < count; i++)
        {
            String op = ctx.mulOp().get(i-1).getText().toLowerCase();
            MonkeyCParser.FactorContext factorCtx2 = ctx.factor().get(i);
            Typespec type2 = factorCtx2.type;

            boolean integerMode = false;
            boolean realMode    = false;

            if (   (type1 == Predefined.integerType)
                && (type2 == Predefined.integerType)) 
            {
                integerMode = true;
            }
            else if (   (type1 == Predefined.realType) 
                     || (type2 == Predefined.realType))
            {
                realMode = true;
            }
                
            if (integerMode)
            {
                compiler.visit(factorCtx2);            

                if      (op.equals("*"))   emitter.emit(IMUL);
                else if (op.equals("/"))   emitter.emit(DDIV);
                else if (op.equals("div")) emitter.emit(IDIV);
                else if (op.equals("mod")) emitter.emit(IREM);
            }
            else if (realMode)
            {
                if (type1 == Predefined.integerType) emitter.emit(I2D);
                compiler.visit(factorCtx2); 
                if (type2 == Predefined.integerType) emitter.emit(I2D);
                
                if      (op.equals("*")) emitter.emit(DMUL);
                else if (op.equals("/")) emitter.emit(DDIV);
            }
            else  // booleanMode
            {
                compiler.visit(factorCtx2);                 
                emitter.emit(IAND);
            }
        }
    }
    
    /**
     * Emit code for NOT.
     * @param ctx the NotFactorContext.
     */
    public void emitNotFactor(MonkeyCParser.NotFactorContext ctx)
    {
        compiler.visit(ctx.factor());
        emitter.emit(ICONST_1);
        emitter.emit(IXOR);
    }

    /**
     * Emit code to load a scalar variable's value 
     * or a structured variable's address.
     * @param ctx the VariableContext.
     */
    public void emitLoadValue(MonkeyCParser.VariableContext varCtx)
    {
        // Load the scalar value or structure address.
        Typespec variableType = emitLoadVariable(varCtx);
        
        // Load an array element's or record field's value.
        int modifierCount = varCtx.modifier().size();
        if (modifierCount > 0)
        {
            MonkeyCParser.ModifierContext lastModCtx =
                                    varCtx.modifier().get(modifierCount - 1);
            
            if (lastModCtx.indexList() != null)
            {
                emitLoadArrayElementValue(variableType);
            }
            else
            {
                emitLoadRecordFieldValue(lastModCtx.field(), variableType);
            }
        }
    }

    /**
     * Emit code to load a scalar variable's value 
     * or a structured variable's address.
     * @param variableNode the variable node.
     * @return the datatype of the variable.
     */
    public Typespec emitLoadVariable(MonkeyCParser.VariableContext varCtx)
    {
        SymtabEntry variableEntry = varCtx.entry;
        Typespec variableType = variableEntry.getType();
        int modifierCount = varCtx.modifier().size();
        
        // Scalar value or structure address.
        emitter.emitLoadValue(variableEntry);

        // Loop over subscript and field modifiers.
        for (int i = 0; i < modifierCount; ++i)
        {
            MonkeyCParser.ModifierContext modCtx = varCtx.modifier().get(i);
            boolean lastModifier = i == modifierCount - 1;

            // Subscript
            if (modCtx.indexList() != null) 
            {
                variableType = emitLoadArrayElementAccess(
                                modCtx.indexList(), variableType, lastModifier);
            }
            
            // Field
            else if (!lastModifier)
            {
                variableType = emitLoadRecordField(modCtx.field(), variableType);
            }
        }

        return variableType;
    }

    /**
     * Emit code to access an array element by loading the array address
     * and the subscript value. This can subsequently be followed by code
     * to load the array element's value or to store into the array element. 
     * @param subscriptsNode the SUBSCRIPTS node.
     * @param elmtType the array element type.
     * @param lastModifier true if this is the variable's last modifier.
     * @return the type of the element.
     */
    private Typespec emitLoadArrayElementAccess(
                                    MonkeyCParser.IndexListContext indexListCtx,
                                    Typespec elmtType, boolean lastModifier)
    {
        int indexCount = indexListCtx.index().size();
        
        // Loop over the subscripts.
        for (int i = 0; i < indexCount; i++)
        {
            MonkeyCParser.IndexContext indexCtx = indexListCtx.index().get(i);
            emitExpression(indexCtx.expression());

            Typespec indexType = elmtType.getArrayIndexType();

            if (indexType.getForm() == SUBRANGE) 
            {
                int min = indexType.getSubrangeMinValue();
                if (min != 0) 
                {
                    emitter.emitLoadConstant(min);
                    emitter.emit(ISUB);
                }
            }

            if (!lastModifier || (i < indexCount - 1)) emitter.emit(AALOAD);
            elmtType = elmtType.getArrayElementType();
        }

        return elmtType;
    }

    /**
     * Emit a load of an array element's value.
     * @param elmtType the element type if character, else null.
     */
    private void emitLoadArrayElementValue(Typespec elmtType)
    {
        Form form = SCALAR;

        if (elmtType != null) 
        {
            elmtType = elmtType.baseType();
            form = elmtType.getForm();
        }

        // Load a character from a string.
        if (elmtType == Predefined.charType) 
        {
            emitter.emit(INVOKEVIRTUAL, "java/lang/StringBuilder.charAt(I)C");
        }

        // Load an array element.
        else 
        {
            emitter.emit(  elmtType == Predefined.integerType ? IALOAD
                         : elmtType == Predefined.realType    ? DALOAD
                         : elmtType == Predefined.booleanType ? BALOAD
                         : elmtType == Predefined.charType    ? CALOAD
                         : form == ENUMERATION                ? IALOAD
                         :                                      AALOAD);
        }
    }
    
    private void emitLoadRecordFieldValue(
                        MonkeyCParser.FieldContext fieldCtx, Typespec recordType)
    {
        emitLoadRecordField(fieldCtx, recordType);
    }

    /**
     * Emit code to load the address or value of a record field.
     * @param fieldCtx the FieldContext.
     * @param last true if this is the variable's last field, else false.
     * @return the type of the field.
     */
    private Typespec emitLoadRecordField(
                        MonkeyCParser.FieldContext fieldCtx, Typespec recordType)
    {
        SymtabEntry fieldEntry = fieldCtx.entry;
        String fieldName = fieldEntry.getName();
        Typespec fieldType = fieldCtx.type;  
        
        String recordTypePath = recordType.getRecordTypePath();
        String fieldPath = recordTypePath + "/" + fieldName;        
        emitter.emit(GETFIELD, fieldPath, emitter.typeDescriptor(fieldType));

        return fieldType;
    }
    
    /**
     * Emit code to load an integer constant.
     * @parm intCtx the IntegerConstantContext.
     */
    public void emitLoadIntegerConstant(MonkeyCParser.NumberContext intCtx)
    {
        int value = Integer.parseInt(intCtx.getText());
        emitter.emitLoadConstant(value);
    }
    
    /**
     * Emit code to load real constant.
     * @parm intCtx the IntegerConstantContext.
     */
    public void emitLoadRealConstant(MonkeyCParser.NumberContext realCtx)
    {
        float value = Float.parseFloat(realCtx.getText());
        emitter.emitLoadConstant(value);
    }
}
