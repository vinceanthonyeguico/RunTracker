/**
 * <h1>Semantics</h1>
 *
 * <p>Perform the semantic pass.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package frontend;

import java.util.ArrayList;
import java.util.HashSet;

import static frontend.SemanticErrorHandler.Code.*;

import intermediate.antlr4.*;
import intermediate.symtab.*;
import intermediate.symtab.SymtabEntry.Kind;
import intermediate.type.*;
import intermediate.type.Typespec.*;
import intermediate.util.*;
import static intermediate.symtab.SymtabEntry.Kind.*;
import static intermediate.symtab.SymtabEntry.Routine.*;
import static intermediate.type.Typespec.Form.*;

import backend.BackendMode;
import static backend.BackendMode.*;

/**
 * Semantic operations.
 * Perform type checking and create symbol tables.
 */
public class Semantics extends MonkeyCBaseVisitor<Object>
{
    private BackendMode mode;
    private SymtabStack symtabStack;
    private SymtabEntry programEntry;
    private SemanticErrorHandler error;
    
    public Semantics(BackendMode mode)
    {
        // Create and initialize the symbol table stack.
        this.symtabStack = new SymtabStack();
        Predefined.initialize(symtabStack);
        
        this.mode = mode;
        this.error = new SemanticErrorHandler();
    }
    
    public SymtabEntry getProgramEntry() { return programEntry; }
    public int getErrorCount() { return error.getCount(); };
    
    /**
     * Return the default value for a data type.
     * @param type the data type.
     * @return the default value.
     */
    public static Object defaultValue(Typespec type)
    {
        type = type.baseType();

        if      (type == Predefined.integerType) return Integer.valueOf(0);
        else if (type == Predefined.realType)    return Double.valueOf(0.0f);
        else if (type == Predefined.booleanType) return Boolean.valueOf(false);
        else if (type == Predefined.charType)    return Character.valueOf('#');
        else /* string */                        return String.valueOf("#");
    }

    @Override 
    public Object visitProgram(MonkeyCParser.ProgramContext ctx) 
    { 
        visit(ctx.programHeader());
        visit(ctx.block().declarations());
        visit(ctx.block().compoundStatement());
        
        // Print the cross-reference table.
        CrossReferencer crossReferencer = new CrossReferencer();
        crossReferencer.print(symtabStack);

        return null;
    }
    
    @Override 
    public Object visitProgramHeader(MonkeyCParser.ProgramHeaderContext ctx) 
    { 
        MonkeyCParser.ProgramIdentifierContext idCtx = ctx.programIdentifier();
        String programName = idCtx.IDENTIFIER().getText();  // don't shift case
        
        programEntry = symtabStack.enterLocal(programName, PROGRAM);
        programEntry.setRoutineSymtab(symtabStack.push());
        
        symtabStack.setProgramEntry(programEntry);
        symtabStack.getLocalSymtab().setOwner(programEntry);
        
        idCtx.entry = programEntry;
        return null;
    }

    @Override 
    public Object visitConstantDefinition(
                                MonkeyCParser.ConstantDefinitionContext ctx) 
    { 
        MonkeyCParser.ConstantIdentifierContext idCtx = ctx.constantIdentifier();
        String constantName = idCtx.IDENTIFIER().getText().toLowerCase();
        SymtabEntry constantEntry = symtabStack.lookupLocal(constantName);
        
        if (constantEntry == null)
        {
            MonkeyCParser.ConstantContext constCtx = ctx.constant();
            Object constValue = visit(constCtx);
            
            constantEntry = symtabStack.enterLocal(constantName, CONSTANT);
            constantEntry.setValue(constValue);
            constantEntry.setType(constCtx.type);
            
            idCtx.entry = constantEntry;
            idCtx.type  = constCtx.type;
        }
        else
        {
            error.flag(REDECLARED_IDENTIFIER, ctx);
            
            idCtx.entry = constantEntry;
            idCtx.type  = Predefined.integerType;
        }

        constantEntry.appendLineNumber(ctx.getStart().getLine());        
        return null;
    }

    @Override 
    public Object visitConstant(MonkeyCParser.ConstantContext ctx) 
    {
        if (ctx.IDENTIFIER() != null)
        {
            String constantName = ctx.IDENTIFIER().getText().toLowerCase();
            SymtabEntry constantEntry = symtabStack.lookup(constantName);
            
            if (constantEntry != null)
            {
                Kind kind = constantEntry.getKind();
                if ((kind != CONSTANT) && (kind != ENUMERATION_CONSTANT))
                {
                    error.flag(INVALID_CONSTANT, ctx);
                }
                
                ctx.type  = constantEntry.getType();
                ctx.value = constantEntry.getValue();
                
                constantEntry.appendLineNumber(ctx.getStart().getLine());
            }
            else
            {
                error.flag(UNDECLARED_IDENTIFIER, ctx);
                
                ctx.type = Predefined.integerType;
                ctx.value = 0;
            }
        }
        else if (ctx.characterConstant() != null)
        {
            ctx.type  = Predefined.charType;
            ctx.value = (char) ctx.getText().charAt(1);
        }
        else if (ctx.stringConstant() != null)
        {
            String MonkeyCString = ctx.stringConstant().STRING().getText();
            String unquoted = MonkeyCString.substring(1, MonkeyCString.length()-1);
            ctx.type  = Predefined.stringType;            
            ctx.value = unquoted.replace("''", "'").replace("\"", "\\\"");
        }
        else  // number
        {
            if (ctx.unsignedNumber().integerConstant() != null)
            {
                ctx.type  = Predefined.integerType;
                ctx.value = Integer.parseInt(ctx.getText());
            }
            else
            {
                ctx.type  = Predefined.realType;
                ctx.value = Double.parseDouble(ctx.getText());
            }
        }
        
        return ctx.value;
    }

    @Override 
    public Object visitTypeDefinition(MonkeyCParser.TypeDefinitionContext ctx) 
    { 
        MonkeyCParser.TypeIdentifierContext idCtx = ctx.typeIdentifier();
        String typeName = idCtx.IDENTIFIER().getText().toLowerCase();
        SymtabEntry typeEntry = symtabStack.lookupLocal(typeName);
        
        MonkeyCParser.TypeSpecificationContext typespecCtx = 
                                                        ctx.typeSpecification();
        
        // If it's a record type, create a named record type.
        if (typespecCtx instanceof MonkeyCParser.RecordTypespecContext)
        {
            typeEntry = createRecordType(
                    (MonkeyCParser.RecordTypespecContext) typespecCtx, typeName);            
        }

        // Enter the type name of any other type into the symbol table.
        else if (typeEntry == null)
        {
            visit(typespecCtx);
            
            typeEntry = symtabStack.enterLocal(typeName, TYPE);
            typeEntry.setType(typespecCtx.type);
            typespecCtx.type.setIdentifier(typeEntry);
        }
        
        // Redeclared identifier.
        else 
        {
            error.flag(REDECLARED_IDENTIFIER, ctx);
        }
        
        idCtx.entry = typeEntry;
        idCtx.type  = typespecCtx.type;

        typeEntry.appendLineNumber(ctx.getStart().getLine());        
        return null;
    }

    @Override 
    public Object visitRecordTypespec(MonkeyCParser.RecordTypespecContext ctx) 
    { 
        // Create an unnamed record type.
        String recordTypeName = Symtab.generateUnnamedName();
        createRecordType(ctx, recordTypeName);
        
        return null;
    }
    
    /**
     * Create a new record type.
     * @param recordTypeSpecCtx the RecordTypespecContext.
     * @param recordTypeName the name of the record type.
     * @return the symbol table entry of the record type identifier.
     */
    private SymtabEntry createRecordType(
                        MonkeyCParser.RecordTypespecContext recordTypeSpecCtx, 
                        String recordTypeName)
    {
        MonkeyCParser.RecordTypeContext recordTypeCtx = 
                                                recordTypeSpecCtx.recordType();
        Typespec recordType = new Typespec(RECORD);
        
        SymtabEntry recordTypeEntry = 
                                symtabStack.enterLocal(recordTypeName, TYPE);
        recordTypeEntry.setType(recordType);
        recordType.setIdentifier(recordTypeEntry);
        
        String recordTypePath = createRecordTypePath(recordType);
        recordType.setRecordTypePath(recordTypePath);

        // Enter the record fields into the record type's symbol table.
        Symtab recordSymtab = createRecordSymtab(recordTypeCtx.recordFields(),
                                                 recordTypeEntry);
        recordType.setRecordSymtab(recordSymtab);

        recordTypeCtx.entry    = recordTypeEntry;
        recordTypeSpecCtx.type = recordType;
        
        return recordTypeEntry;
    }

    /**
     * Create the fully qualified type pathname of a record type.
     * @param recordType the record type.
     * @return the pathname.
     */
    private String createRecordTypePath(Typespec recordType)
    {
        SymtabEntry recordEntry = recordType.getIdentifier();
        SymtabEntry parentEntry = recordEntry.getSymtab().getOwner();
        String path = recordEntry.getName();
        
        while (   (parentEntry.getKind() == TYPE) 
               && (parentEntry.getType().getForm() == RECORD))
        {
            path = parentEntry.getName() + "$" + path;
            parentEntry = parentEntry.getSymtab().getOwner();
        }
        
        path = parentEntry.getName() + "$" + path;
        return path;
    }

    /**
     * Create the symbol table for a record type.
     * @param ctx the RecordFieldsContext,
     * @param ownerEntry the symbol table entry of the owner's identifier.
     * @return the symbol table.
     */
    private Symtab createRecordSymtab(MonkeyCParser.RecordFieldsContext ctx,
                                      SymtabEntry ownerEntry) 
    { 
        Symtab recordSymtab = symtabStack.push();

        recordSymtab.setOwner(ownerEntry);
        visit(ctx.variableDeclarationsList());
        recordSymtab.resetVariables(RECORD_FIELD);
        symtabStack.pop();
        
        return recordSymtab;
    }
    
    @Override 
    public Object visitSimpleTypespec(MonkeyCParser.SimpleTypespecContext ctx) 
    { 
        visit(ctx.simpleType());
        ctx.type = ctx.simpleType().type;
        
        return null;
    }

    @Override 
    public Object visitTypeIdentifierTypespec(
                                MonkeyCParser.TypeIdentifierTypespecContext ctx) 
    { 
        visit(ctx.typeIdentifier());
        ctx.type = ctx.typeIdentifier().type;
        
        return null;
    }

    @Override 
    public Object visitTypeIdentifier(MonkeyCParser.TypeIdentifierContext ctx) 
    { 
        String typeName = ctx.IDENTIFIER().getText().toLowerCase();
        SymtabEntry typeEntry = symtabStack.lookup(typeName);
        
        if (typeEntry != null)
        {
            if (typeEntry.getKind() != TYPE)
            {
                error.flag(INVALID_TYPE, ctx);
                ctx.type = Predefined.integerType;
            }
            else
            {
                ctx.type = typeEntry.getType();
            }
            
            typeEntry.appendLineNumber(ctx.start.getLine());
        }
        else
        {
            error.flag(UNDECLARED_IDENTIFIER, ctx);
            ctx.type = Predefined.integerType;
        }
        
        ctx.entry = typeEntry;
        return null;
    }

    @Override 
    public Object visitEnumerationTypespec(
                                    MonkeyCParser.EnumerationTypespecContext ctx) 
    { 
        Typespec enumType = new Typespec(ENUMERATION);
        ArrayList<SymtabEntry> constants = new ArrayList<>();
        int value = -1;

        // Loop over the enumeration constants.
        for (MonkeyCParser.EnumerationConstantContext constCtx : 
                                    ctx.enumerationType().enumerationConstant())
        {
            MonkeyCParser.ConstantIdentifierContext constIdCtx = 
                                                constCtx.constantIdentifier();
            String constantName = constIdCtx.IDENTIFIER().getText()
                                                         .toLowerCase();
            SymtabEntry constantEntry = symtabStack.lookupLocal(constantName);
            
            if (constantEntry == null)
            {
                constantEntry = symtabStack.enterLocal(constantName, 
                                                    ENUMERATION_CONSTANT);
                constantEntry.setType(enumType);
                constantEntry.setValue(++value);
                
                constants.add(constantEntry);
            }
            else
            {
                error.flag(REDECLARED_IDENTIFIER, constCtx);
            }
            
            constIdCtx.entry = constantEntry;
            constIdCtx.type  = enumType;
            
            constantEntry.appendLineNumber(ctx.getStart().getLine());        
        }

        enumType.setEnumerationConstants(constants);
        ctx.type = enumType;
       
        return null;
    }

    @Override 
    public Object visitSubrangeTypespec(
                                    MonkeyCParser.SubrangeTypespecContext ctx) 
    { 
        Typespec type = new Typespec(SUBRANGE);
        MonkeyCParser.SubrangeTypeContext subCtx = ctx.subrangeType();
        MonkeyCParser.ConstantContext minCtx = subCtx.constant().get(0);
        MonkeyCParser.ConstantContext maxCtx = subCtx.constant().get(1);
        
        Object minObj = visit(minCtx);
        Object maxObj = visit(maxCtx);
        
        Typespec minType = minCtx.type;
        Typespec maxType = maxCtx.type;
        
        if (   (   (minType.getForm() != SCALAR)
                && (minType.getForm() != ENUMERATION))
            || (minType == Predefined.realType)
            || (minType == Predefined.stringType))
        {
            error.flag(INVALID_CONSTANT, minCtx);
            minType = Predefined.integerType;
            minObj = 0;
        }
        
        int minValue;
        int maxValue;

        if (minType == Predefined.integerType)
        {
            minValue = (Integer) minObj;
            maxValue = (Integer) maxObj;
        }
        else if (minType == Predefined.charType)
        {
            minValue = (Character) minObj;
            maxValue = (Character) maxObj;
        }
        else  // enumeration constants
        {
            minValue = (Integer) minCtx.value;
            maxValue = (Integer) maxCtx.value;
        }

        if ((maxType != minType) || (minValue > maxValue))
        {
            error.flag(INVALID_CONSTANT, maxCtx);
            maxType = minType;
            maxObj  = minObj;
        }
        
        type.setSubrangeBaseType(minType);
        type.setSubrangeMinValue((Integer) minValue);
        type.setSubrangeMaxValue((Integer) maxValue);

        ctx.type = type;
        return null;
    }

    @Override 
    public Object visitArrayTypespec(MonkeyCParser.ArrayTypespecContext ctx) 
    { 
        Typespec arrayType = new Typespec(ARRAY);
        MonkeyCParser.ArrayTypeContext arrayCtx = ctx.arrayType();
        MonkeyCParser.ArrayDimensionListContext listCtx = 
                                                arrayCtx.arrayDimensionList();
        
        ctx.type = arrayType;
        
        // Loop over the array dimensions.
        int count = listCtx.simpleType().size();
        for (int i = 0; i < count; i++)
        {
            MonkeyCParser.SimpleTypeContext simpleCtx = 
                                                    listCtx.simpleType().get(i);
            visit(simpleCtx);
            arrayType.setArrayIndexType(simpleCtx.type);
            arrayType.setArrayElementCount(typeCount(simpleCtx.type));
            
            if (i < count-1) 
            {
                Typespec elmtType = new Typespec(ARRAY);
                arrayType.setArrayElementType(elmtType);
                arrayType = elmtType;
            }
        }
        
        visit(arrayCtx.typeSpecification());
        Typespec elmtType = arrayCtx.typeSpecification().type;
        arrayType.setArrayElementType(elmtType);
        
        return null;
    }
    
    /**
     * Return the number of values in a datatype.
     * @param type the datatype.
     * @return the number of values.
     */
    private int typeCount(Typespec type)
    {
        int count = 0;
        
        if (type.getForm() == ENUMERATION)
        {
            ArrayList<SymtabEntry> constants = type.getEnumerationConstants();
            count = constants.size();
        }
        else  // subrange
        {
            int minValue = type.getSubrangeMinValue();
            int maxValue = type.getSubrangeMaxValue();
            count = maxValue - minValue + 1;
        }
        
        return count;
    }

    @Override 
    public Object visitVariableDeclarations(
                                MonkeyCParser.VariableDeclarationsContext ctx) 
    { 
        MonkeyCParser.TypeSpecificationContext typeCtx = ctx.typeSpecification();
        visit(typeCtx);
        
        MonkeyCParser.VariableIdentifierListContext listCtx = 
                                                ctx.variableIdentifierList();
        
        // Loop over the variables being declared.
        for (MonkeyCParser.VariableIdentifierContext idCtx : 
                                                listCtx.variableIdentifier())
        {
            int lineNumber = idCtx.getStart().getLine();        
            String variableName = idCtx.IDENTIFIER().getText().toLowerCase();
            SymtabEntry variableEntry = symtabStack.lookupLocal(variableName);
            
            if (variableEntry == null)
            {
                variableEntry = symtabStack.enterLocal(variableName, VARIABLE);
                variableEntry.setType(typeCtx.type);
                
                // Assign slot numbers to local variables.
                Symtab symtab = variableEntry.getSymtab();
                if (symtab.getNestingLevel() > 1)
                {
                    variableEntry.setSlotNumber(symtab.nextSlotNumber());
                    
                    if (variableEntry.getType() == Predefined.realType)
                    {
                        symtab.nextSlotNumber();
                    }
                }
                
                idCtx.entry = variableEntry;
            }
            else
            {
                error.flag(REDECLARED_IDENTIFIER, ctx);
            }
            
            variableEntry.appendLineNumber(lineNumber);        
        }
        
        return null;
    }

    @Override 
    @SuppressWarnings("unchecked")
    public Object visitRoutineDefinition(
                                    MonkeyCParser.RoutineDefinitionContext ctx) 
    {
        MonkeyCParser.FunctionHeadContext  funcCtx = ctx.functionHead();
        MonkeyCParser.ProcedureHeadContext procCtx = ctx.procedureHead();
        MonkeyCParser.RoutineIdentifierContext idCtx = null;
        MonkeyCParser.ParametersContext parameters = null;
        boolean functionDefinition = funcCtx != null;
        Typespec returnType = null;
        String routineName;
        
        if (functionDefinition)
        {
            idCtx = funcCtx.routineIdentifier();
            parameters = funcCtx.parameters();
        }
        else
        {
            idCtx = procCtx.routineIdentifier();
            parameters = procCtx.parameters();
        }
        
        routineName = idCtx.IDENTIFIER().getText().toLowerCase();
        SymtabEntry routineEntry = symtabStack.lookupLocal(routineName);
        
        if (routineEntry != null)
        {
            error.flag(REDECLARED_IDENTIFIER, 
                       ctx.getStart().getLine(), routineName);
            return null;
        }

        routineEntry = symtabStack.enterLocal(
                        routineName, functionDefinition ? FUNCTION : PROCEDURE);
        routineEntry.setRoutineCode(DECLARED);
        idCtx.entry = routineEntry;
        
        // Append to the parent routine's list of subroutines.
        SymtabEntry parentEntry = symtabStack.getLocalSymtab().getOwner();
        parentEntry.appendSubroutine(routineEntry);
        
        routineEntry.setRoutineSymtab(symtabStack.push());
        idCtx.entry = routineEntry;
        
        Symtab symtab = symtabStack.getLocalSymtab();
        symtab.setOwner(routineEntry);
        
        if (parameters != null)
        {
            ArrayList<SymtabEntry> parameterEntries = (ArrayList<SymtabEntry>) 
                                visit(parameters.parameterDeclarationsList());
            routineEntry.setRoutineParameters(parameterEntries);
            
            for (SymtabEntry parmEntry : parameterEntries)
            {
                parmEntry.setSlotNumber(symtab.nextSlotNumber());
                
                if (parmEntry.getType() == Predefined.realType)
                {
                    symtab.nextSlotNumber();
                }
            }
        }
        
        if (functionDefinition)
        {
            MonkeyCParser.TypeIdentifierContext typeIdCtx = 
                                                    funcCtx.typeIdentifier();
            visit(typeIdCtx);
            returnType = typeIdCtx.type;
            
            if (returnType.getForm() != SCALAR)
            {
                error.flag(INVALID_RETURN_TYPE, typeIdCtx);
                returnType = Predefined.integerType;
            }
            
            routineEntry.setType(returnType);
            idCtx.type = returnType;
        }
        else
        {
            idCtx.type = null;
        }
        
        visit(ctx.block().declarations());     
        
        // Enter the function's associated variable into its symbol table.
        if (functionDefinition)
        {
            SymtabEntry assocVarEntry = 
                                symtabStack.enterLocal(routineName, VARIABLE);
            assocVarEntry.setSlotNumber(symtab.nextSlotNumber());
            assocVarEntry.setType(returnType);
        }
        
        visit(ctx.block().compoundStatement());
        routineEntry.setExecutable(ctx.block().compoundStatement());
        
        symtabStack.pop();
        return null;
    }

    @Override 
    @SuppressWarnings("unchecked")
    public Object visitParameterDeclarationsList(
                            MonkeyCParser.ParameterDeclarationsListContext ctx)
    {
        ArrayList<SymtabEntry> parameterList = new ArrayList<>();
        
        // Loop over the parameter declarations.
        for (MonkeyCParser.ParameterDeclarationsContext dclCtx : 
                                                    ctx.parameterDeclarations())
        {
            ArrayList<SymtabEntry> parameterSublist = 
                                        (ArrayList<SymtabEntry>) visit(dclCtx);
            parameterList.addAll(parameterSublist);
        }
        
        return parameterList;
    }

    @Override 
    public Object visitParameterDeclarations(
                                MonkeyCParser.ParameterDeclarationsContext ctx) 
    {
        Kind kind = ctx.VAR() != null ? REFERENCE_PARAMETER : VALUE_PARAMETER; 
        MonkeyCParser.TypeIdentifierContext typeCtx = ctx.typeIdentifier();
        
        visit(typeCtx);
        Typespec parmType = typeCtx.type;
        
        ArrayList<SymtabEntry> parameterSublist = new ArrayList<>();
        
        // Loop over the parameter identifiers.
        MonkeyCParser.ParameterIdentifierListContext parmListCtx = 
                                                ctx.parameterIdentifierList();
        for (MonkeyCParser.ParameterIdentifierContext parmIdCtx : 
                                            parmListCtx.parameterIdentifier())
        {
            int lineNumber = parmIdCtx.getStart().getLine();   
            String parmName = parmIdCtx.IDENTIFIER().getText().toLowerCase();
            SymtabEntry parmEntry = symtabStack.lookupLocal(parmName);
            
            if (parmEntry == null)
            {
                parmEntry = symtabStack.enterLocal(parmName, kind);
                parmEntry.setType(parmType);
                
                if (   (kind == REFERENCE_PARAMETER) 
                    && (mode != EXECUTOR)
                    && (parmType.getForm() == SCALAR))
                {
                    error.flag(INVALID_REFERENCE_PARAMETER, parmIdCtx);
                }
            }
            else
            {
                error.flag(REDECLARED_IDENTIFIER, parmIdCtx);
            }
            
            parmIdCtx.entry = parmEntry;
            parmIdCtx.type  = parmType;
            
            parameterSublist.add(parmEntry);
            parmEntry.appendLineNumber(lineNumber);    
        }
        
        return parameterSublist;
    }
    
    @Override 
    public Object visitAssignmentStatement(
                                    MonkeyCParser.AssignmentStatementContext ctx) 
    {
        MonkeyCParser.LhsContext lhsCtx = ctx.lhs();
        MonkeyCParser.RhsContext rhsCtx = ctx.rhs();
        
        visitChildren(ctx);
        
        Typespec lhsType = lhsCtx.type;
        Typespec rhsType = rhsCtx.expression().type;
        
        if (!TypeChecker.areAssignmentCompatible(lhsType, rhsType))
        {
            error.flag(INCOMPATIBLE_ASSIGNMENT, rhsCtx);
        }
        
        return null;
    }

    @Override 
    public Object visitLhs(MonkeyCParser.LhsContext ctx) 
    {
        MonkeyCParser.VariableContext varCtx = ctx.variable();
        visit(varCtx);
        ctx.type = varCtx.type;
        
        return null;
    }

    @Override 
    public Object visitIfStatement(MonkeyCParser.IfStatementContext ctx) 
    {
        MonkeyCParser.ExpressionContext     exprCtx  = ctx.expression();
        MonkeyCParser.TrueStatementContext  trueCtx  = ctx.trueStatement();
        MonkeyCParser.FalseStatementContext falseCtx = ctx.falseStatement();
        
        visit(exprCtx);
        Typespec exprType = exprCtx.type;
        
        if (!TypeChecker.isBoolean(exprType))
        {
            error.flag(TYPE_MUST_BE_BOOLEAN, exprCtx);
        }
        
        visit(trueCtx);
        if (falseCtx != null) visit(falseCtx);
        
        return null;
    }

    @Override 
    public Object visitCaseStatement(MonkeyCParser.CaseStatementContext ctx) 
    {
        MonkeyCParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        Typespec exprType = exprCtx.type;
        Form exprTypeForm = exprType.getForm();
        
        if (   (   (exprTypeForm != SCALAR) 
                && (exprTypeForm != ENUMERATION) 
                && (exprTypeForm != SUBRANGE))
            || (exprType == Predefined.realType)
            || (exprType == Predefined.stringType))
        {
            error.flag(TYPE_MISMATCH, exprCtx);
            exprType = Predefined.integerType;
        }
        
        HashSet<Integer> constants = new HashSet<>();
        MonkeyCParser.CaseBranchListContext branchListCtx = ctx.caseBranchList();
        
        // Loop over the CASE branches.
        for (MonkeyCParser.CaseBranchContext branchCtx : 
                                                    branchListCtx.caseBranch())
        {
            MonkeyCParser.CaseConstantListContext constListCtx = 
                                                    branchCtx.caseConstantList();
            MonkeyCParser.StatementContext stmtCtx = branchCtx.statement();
            
            if (constListCtx != null)
            {
                // Loop over the CASE constants in each branch.
                for (MonkeyCParser.CaseConstantContext caseConstCtx : 
                                                    constListCtx.caseConstant())
                {
                    MonkeyCParser.ConstantContext constCtx = 
                                                        caseConstCtx.constant();
                    Object constValue = visit(constCtx);
                    
                    caseConstCtx.type  = constCtx.type;
                    caseConstCtx.value = 0;
                    
                    if (constCtx.type != exprType)
                    {
                        error.flag(TYPE_MISMATCH, constCtx);
                    }
                    else if (   (constCtx.type == Predefined.integerType)
                             || (constCtx.type.getForm() == ENUMERATION))
                    {
                        caseConstCtx.value = (Integer) constValue;
                    }
                    else if (constCtx.type == Predefined.charType)
                    {
                        caseConstCtx.value = (Character) constValue;
                    }
                    
                    if (constants.contains(caseConstCtx.value))
                    {
                        error.flag(DUPLICATE_CASE_CONSTANT, constCtx);
                    }
                    else
                    {
                        constants.add(caseConstCtx.value);
                    }
                }
            }
            
            if (stmtCtx != null) visit(stmtCtx);
        }
        
        return null;
    }

    @Override 
    public Object visitRepeatStatement(MonkeyCParser.RepeatStatementContext ctx) 
    {
        MonkeyCParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        Typespec exprType = exprCtx.type;
        
        if (!TypeChecker.isBoolean(exprType))
        {
            error.flag(TYPE_MUST_BE_BOOLEAN, exprCtx);
        }
        
        visit(ctx.statementList());
        return null;
    }

    @Override 
    public Object visitWhileStatement(MonkeyCParser.WhileStatementContext ctx) 
    {
        MonkeyCParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        Typespec exprType = exprCtx.type;
        
        if (!TypeChecker.isBoolean(exprType))
        {
            error.flag(TYPE_MUST_BE_BOOLEAN, exprCtx);
        }
        
        visit(ctx.statement());
        return null;
    }

    @Override 
    public Object visitForStatement(MonkeyCParser.ForStatementContext ctx) 
    {
        MonkeyCParser.VariableContext varCtx = ctx.variable();
        visit(varCtx);
        
        String controlName = varCtx.variableIdentifier().getText().toLowerCase();
        Typespec controlType = Predefined.integerType;
        
        if (varCtx.entry != null)
        {
            controlType = varCtx.type;
            
            if (   (controlType.getForm() != SCALAR )
                || (controlType == Predefined.realType)
                || (controlType == Predefined.stringType)
                || (varCtx.modifier().size() != 0))
            {
                error.flag(INVALID_CONTROL_VARIABLE, varCtx);
            }
        }
        else
        {
            error.flag(UNDECLARED_IDENTIFIER, ctx.getStart().getLine(), 
                       controlName);
        }
        
        MonkeyCParser.ExpressionContext startCtx = ctx.expression().get(0);
        MonkeyCParser.ExpressionContext endCtx   = ctx.expression().get(1);
        
        visit(startCtx);
        visit(endCtx);
        
        if (startCtx.type != controlType) error.flag(TYPE_MISMATCH, startCtx);
        if (startCtx.type != endCtx.type) error.flag(TYPE_MISMATCH, endCtx);
        
        visit(ctx.statement());
        return null;
    }

    @Override 
    public Object visitProcedureCallStatement(
                                MonkeyCParser.ProcedureCallStatementContext ctx) 
    {
        MonkeyCParser.ProcedureNameContext nameCtx = ctx.procedureName();
        MonkeyCParser.ArgumentListContext listCtx = ctx.argumentList();
        String name = ctx.procedureName().getText().toLowerCase();
        SymtabEntry procedureEntry = symtabStack.lookup(name);
        boolean badName = false;
        
        if (procedureEntry == null)
        {
            error.flag(UNDECLARED_IDENTIFIER, nameCtx);
            badName = true;
        }
        else if (procedureEntry.getKind() != PROCEDURE)
        {
            error.flag(NAME_MUST_BE_PROCEDURE, nameCtx);
            badName = true;
        }
        
        // Bad procedure name. Do a simple arguments check and then leave.
        if (badName)
        {
            for (MonkeyCParser.ArgumentContext exprCtx : listCtx.argument())
            {
                visit(exprCtx);
            }
        }
        
        // Good procedure name.
        else
        {
            ArrayList<SymtabEntry> parms = procedureEntry.getRoutineParameters();
            checkCallArguments(listCtx, parms);
        }
        
        nameCtx.entry = procedureEntry;
        return null;
    }

    @Override 
    public Object visitFunctionCallFactor(
                                    MonkeyCParser.FunctionCallFactorContext ctx) 
    {
        MonkeyCParser.FunctionCallContext callCtx = ctx.functionCall();
        MonkeyCParser.FunctionNameContext nameCtx = callCtx.functionName();
        MonkeyCParser.ArgumentListContext listCtx = callCtx.argumentList();
        String name = callCtx.functionName().getText().toLowerCase();
        SymtabEntry functionEntry = symtabStack.lookup(name);
        boolean badName = false;
        
        ctx.type = Predefined.integerType;

        if (functionEntry == null)
        {
            error.flag(UNDECLARED_IDENTIFIER, nameCtx);
            badName = true;
        }
        else if (functionEntry.getKind() != FUNCTION)
        {
            error.flag(NAME_MUST_BE_FUNCTION, nameCtx);
            badName = true;
        }
        
        // Bad function name. Do a simple arguments check and then leave.
        if (badName)
        {
            for (MonkeyCParser.ArgumentContext exprCtx : listCtx.argument())
            {
                visit(exprCtx);
            }
        }
        
        // Good function name.
        else
        {
            ArrayList<SymtabEntry> parameters = 
                                        functionEntry.getRoutineParameters();
            checkCallArguments(listCtx, parameters);
            ctx.type = functionEntry.getType();
        }
        
        nameCtx.entry = functionEntry;
        nameCtx.type  = ctx.type;

        return null;
    }
    
    /**
     * Perform semantic operations on procedure and function call arguments.
     * @param listCtx the ArgumentListContext.
     * @param parameters the arraylist of parameters to fill.
     */
    private void checkCallArguments(MonkeyCParser.ArgumentListContext listCtx,
                                    ArrayList<SymtabEntry> parameters)
    {
        int parmsCount = parameters.size();
        int argsCount = listCtx != null ? listCtx.argument().size() : 0;
        
        if (parmsCount != argsCount)
        {
            error.flag(ARGUMENT_COUNT_MISMATCH, listCtx);
            return;
        }
        
        // Check each argument against the corresponding parameter.
        for (int i = 0; i < parmsCount; i++)
        {
            MonkeyCParser.ArgumentContext argCtx = listCtx.argument().get(i);
            MonkeyCParser.ExpressionContext exprCtx = argCtx.expression();
            visit(exprCtx);
            
            SymtabEntry parmEntry = parameters.get(i);
            Typespec parmType = parmEntry.getType();
            Typespec argType  = exprCtx.type;
            
            // For a VAR parameter, the argument must be a variable
            // with the same datatype.
            if (parmEntry.getKind() == REFERENCE_PARAMETER)
            {
                if (expressionIsVariable(exprCtx))
                {
                    if (parmType != argType)
                    {
                        error.flag(TYPE_MISMATCH, exprCtx);
                    }
                }
                else
                {
                    error.flag(ARGUMENT_MUST_BE_VARIABLE, exprCtx);
                }
            }
            
            // For a value parameter, the argument type must be
            // assignment compatible with the parameter type.
            else if (!TypeChecker.areAssignmentCompatible(parmType, argType))
            {
                error.flag(TYPE_MISMATCH, exprCtx);
            }
        }
    }

    /**
     * Determine whether or not an expression is a variable only.
     * @param exprCtx the ExpressionContext.
     * @return true if it's an expression only, else false.
     */
    private boolean expressionIsVariable(MonkeyCParser.ExpressionContext exprCtx)
    {
        // Only a single simple expression?
        if (exprCtx.simpleExpression().size() == 1)
        {
            MonkeyCParser.SimpleExpressionContext simpleCtx = 
                                              exprCtx.simpleExpression().get(0);
            // Only a single term?
            if (simpleCtx.term().size() == 1)
            {
                MonkeyCParser.TermContext termCtx = simpleCtx.term().get(0);
                
                // Only a single factor?
                if (termCtx.factor().size() == 1)
                {
                    return termCtx.factor().get(0) instanceof 
                                            MonkeyCParser.VariableFactorContext;
                }
            }
        }
        
        return false;
    }

    @Override 
    public Object visitExpression(MonkeyCParser.ExpressionContext ctx) 
    {
        MonkeyCParser.SimpleExpressionContext simpleCtx1 =
                                                ctx.simpleExpression().get(0);

        // First simple expression.
        visit(simpleCtx1);
        
        Typespec simpleType1 = simpleCtx1.type;
        ctx.type = simpleType1;
        
        MonkeyCParser.RelOpContext relOpCtx = ctx.relOp();
        
        // Second simple expression?
        if (relOpCtx != null)
        {
            MonkeyCParser.SimpleExpressionContext simpleCtx2 = 
                                                ctx.simpleExpression().get(1);
            visit(simpleCtx2);
            
            Typespec simpleType2 = simpleCtx2.type;
            if (!TypeChecker.areComparisonCompatible(simpleType1, simpleType2))
            {
                error.flag(INCOMPATIBLE_COMPARISON, ctx);
            }
            
            ctx.type = Predefined.booleanType;
        }
        
        return null;
    }

    @Override 
    public Object visitSimpleExpression(MonkeyCParser.SimpleExpressionContext ctx) 
    {
        int count = ctx.term().size();
        MonkeyCParser.SignContext signCtx = ctx.sign();
        Boolean hasSign = signCtx != null;
        MonkeyCParser.TermContext termCtx1 = ctx.term().get(0);
        
        if (hasSign)
        {
            String sign = signCtx.getText();
            if (sign.equals("+") && sign.equals("-"))
            {
                error.flag(INVALID_SIGN, signCtx);
            }
        }
        
        // First term.
        visit(termCtx1);
        Typespec termType1 = termCtx1.type;        
        
        // Loop over any subsequent terms.
        for (int i = 1; i < count; i++)
        {
            String op = ctx.addOp().get(i-1).getText().toLowerCase();
            MonkeyCParser.TermContext termCtx2 = ctx.term().get(i);
            visit(termCtx2);
            Typespec termType2 = termCtx2.type;
            
            // Both operands boolean ==> boolean result. Else type mismatch.
            if (op.equals("or"))
            {
                if (!TypeChecker.isBoolean(termType1)) 
                {
                    error.flag(TYPE_MUST_BE_BOOLEAN, termCtx1);
                }
                if (!TypeChecker.isBoolean(termType2)) 
                {
                    error.flag(TYPE_MUST_BE_BOOLEAN, termCtx2);
                }
                if (hasSign)
                {
                    error.flag(INVALID_SIGN, signCtx);
                }
                
                termType2 = Predefined.booleanType;
            }
            else if (op.equals("+"))
            {
                // Both operands integer ==> integer result
                if (TypeChecker.areBothInteger(termType1, termType2)) 
                {
                    termType2 = Predefined.integerType;
                }

                // Both real operands ==> real result 
                // One real and one integer operand ==> real result
                else if (TypeChecker.isAtLeastOneReal(termType1, termType2)) 
                {
                    termType2 = Predefined.realType;
                }
                
                // Both operands string ==> string result
                else if (TypeChecker.areBothString(termType1, termType2))
                {
                    if (hasSign) error.flag(INVALID_SIGN, signCtx);                    
                    termType2 = Predefined.stringType;
                }

                // Type mismatch.
                else
                {
                    if (!TypeChecker.isIntegerOrReal(termType1))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, termCtx1);
                        termType2 = Predefined.integerType;
                    }
                    if (!TypeChecker.isIntegerOrReal(termType2))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, termCtx2);
                        termType2 = Predefined.integerType;
                    }
                }
            }
            else  // -
            {
                // Both operands integer ==> integer result
                if (TypeChecker.areBothInteger(termType1, termType2)) 
                {
                    termType2 = Predefined.integerType;
                }

                // Both real operands ==> real result 
                // One real and one integer operand ==> real result
                else if (TypeChecker.isAtLeastOneReal(termType1, termType2)) 
                {
                    termType2 = Predefined.realType;
                }
                
                // Type mismatch.
                else
                {
                    if (!TypeChecker.isIntegerOrReal(termType1))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, termCtx1);
                        termType2 = Predefined.integerType;
                    }
                    if (!TypeChecker.isIntegerOrReal(termType2))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, termCtx2);
                        termType2 = Predefined.integerType;
                    }
                }
            }
            
            termType1 = termType2;
        }
        
        ctx.type = termType1;
        return null;
    }

    @Override 
    public Object visitTerm(MonkeyCParser.TermContext ctx) 
    {
        int count = ctx.factor().size();
        MonkeyCParser.FactorContext factorCtx1 = ctx.factor().get(0);
        
        // First factor.
        visit(factorCtx1);
        Typespec factorType1 = factorCtx1.type; 
        
        // Loop over any subsequent factors.
        for (int i = 1; i < count; i++)
        {
            String op = ctx.mulOp().get(i-1).getText().toLowerCase();
            MonkeyCParser.FactorContext factorCtx2 = ctx.factor().get(i);
            visit(factorCtx2);
            Typespec factorType2 = factorCtx2.type;
            
            if (op.equals("*"))
            {
                // Both operands integer  ==> integer result
                if (TypeChecker.areBothInteger(factorType1, factorType2)) 
                {
                    factorType2 = Predefined.integerType;
                }

                // Both real operands ==> real result 
                // One real and one integer operand ==> real result
                else if (TypeChecker.isAtLeastOneReal(factorType1, factorType2)) 
                {
                    factorType2 = Predefined.realType;
                }
                
                // Type mismatch.
                else
                {
                    if (!TypeChecker.isIntegerOrReal(factorType1))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, factorCtx1);
                        factorType2 = Predefined.integerType;
                    }
                    if (!TypeChecker.isIntegerOrReal(factorType2))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, factorCtx2);
                        factorType2 = Predefined.integerType;
                    }
                }
            }
            else if (op.equals("/"))
            {
                // All integer and real operand combinations ==> real result
                if (   TypeChecker.areBothInteger(factorType1, factorType2)
                    || TypeChecker.isAtLeastOneReal(factorType1, factorType2))
                {
                    factorType2 = Predefined.realType;
                }
                
                // Type mismatch.
                else 
                {
                    if (!TypeChecker.isIntegerOrReal(factorType1))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, factorCtx1);
                        factorType2 = Predefined.integerType;
                    }
                    if (!TypeChecker.isIntegerOrReal(factorType2))
                    {
                        error.flag(TYPE_MUST_BE_NUMERIC, factorCtx2);
                        factorType2 = Predefined.integerType;
                    }
                }
            }
            else if (op.equals("div") || op.equals("mod"))
            {
                // Both operands integer ==> integer result. Else type mismatch.
                if (!TypeChecker.isInteger(factorType1))
                {
                    error.flag(TYPE_MUST_BE_INTEGER, factorCtx1);
                    factorType2 = Predefined.integerType;
                }
                if (!TypeChecker.isInteger(factorType2))
                {
                    error.flag(TYPE_MUST_BE_INTEGER, factorCtx2);
                    factorType2 = Predefined.integerType;
                }
            }
            else if (op.equals("and"))
            {
                // Both operands boolean ==> boolean result. Else type mismatch.
                if (!TypeChecker.isBoolean(factorType1))
                {
                    error.flag(TYPE_MUST_BE_BOOLEAN, factorCtx1);
                    factorType2 = Predefined.booleanType;
                }
                if (!TypeChecker.isBoolean(factorType2))
                {
                    error.flag(TYPE_MUST_BE_BOOLEAN, factorCtx2);
                    factorType2 = Predefined.booleanType;
                }
            }
            
            factorType1 = factorType2;
        }

        ctx.type = factorType1;
        return null;
    }

    @Override 
    public Object visitVariableFactor(MonkeyCParser.VariableFactorContext ctx) 
    {
        MonkeyCParser.VariableContext varCtx = ctx.variable();
        visit(varCtx);        
        ctx.type  = varCtx.type;
        
        return null;
    }

    @Override 
    public Object visitVariable(MonkeyCParser.VariableContext ctx) 
    {
        MonkeyCParser.VariableIdentifierContext varIdCtx = 
                                                    ctx.variableIdentifier();
        
        visit(varIdCtx);
        ctx.entry = varIdCtx.entry;
        ctx.type  = variableDatatype(ctx, varIdCtx.type);

        return null;
    }

    @Override 
    public Object visitVariableIdentifier(
                                    MonkeyCParser.VariableIdentifierContext ctx) 
    {
        String variableName = ctx.IDENTIFIER().getText().toLowerCase();
        SymtabEntry variableEntry = symtabStack.lookup(variableName);
        
        if (variableEntry != null)
        {
            int lineNumber = ctx.getStart().getLine();
            ctx.type = variableEntry.getType();
            ctx.entry = variableEntry;
            variableEntry.appendLineNumber(lineNumber);
            
            Kind kind = variableEntry.getKind();
            switch (kind)
            {
                case TYPE:
                case PROGRAM:
                case PROGRAM_PARAMETER:
                case PROCEDURE:
                case UNDEFINED:
                    error.flag(INVALID_VARIABLE, ctx);
                    break;
                    
                default: break;
            }
        }
        else
        {
            error.flag(UNDECLARED_IDENTIFIER, ctx);
            ctx.type = Predefined.integerType;
        }

        return null;
    }

    /**
     * Determine the datatype of a variable that can have modifiers.
     * @param varCtx the VariableContext.
     * @param varType the variable's datatype without the modifiers.
     * @return the datatype with any modifiers.
     */
    private Typespec variableDatatype(
                        MonkeyCParser.VariableContext varCtx, Typespec varType)
    {
        Typespec type = varType;
        
        // Loop over the modifiers.
        for (MonkeyCParser.ModifierContext modCtx : varCtx.modifier())
        {
            // Subscripts.
            if (modCtx.indexList() != null)
            {
                MonkeyCParser.IndexListContext indexListCtx = modCtx.indexList();
                
                // Loop over the subscripts.
                for (MonkeyCParser.IndexContext indexCtx : indexListCtx.index())
                {
                    if (type.getForm() == ARRAY)
                    {
                        Typespec indexType = type.getArrayIndexType();
                        MonkeyCParser.ExpressionContext exprCtx = 
                                                        indexCtx.expression();
                        visit(exprCtx);
                        
                        if (indexType.baseType() != exprCtx.type.baseType())
                        {
                            error.flag(TYPE_MISMATCH, exprCtx);
                        }
                        
                        // Datatype of the next dimension.
                        type = type.getArrayElementType();
                    }
                    else
                    {
                        error.flag(TOO_MANY_SUBSCRIPTS, indexCtx);
                    }
                }
            }
            else  // Record field.
            {
                if (type.getForm() == RECORD)
                {
                    Symtab symtab = type.getRecordSymtab();
                    MonkeyCParser.FieldContext fieldCtx = modCtx.field();
                    String fieldName = 
                                fieldCtx.IDENTIFIER().getText().toLowerCase();
                    SymtabEntry fieldEntry = symtab.lookup(fieldName);

                    // Field of the record type?
                    if (fieldEntry != null) 
                    {
                        type = fieldEntry.getType();
                        fieldCtx.entry = fieldEntry;
                        fieldCtx.type = type;
                        fieldEntry.appendLineNumber(modCtx.getStart().getLine());
                    }
                    else 
                    {
                        error.flag(INVALID_FIELD, modCtx);
                    }
                }
                
                // Not a record variable.
                else 
                {
                    error.flag(INVALID_FIELD, modCtx);
                }
            }
        }
        
        return type;
    }
    
    @Override 
    public Object visitNumberFactor(MonkeyCParser.NumberFactorContext ctx) 
    {
        MonkeyCParser.NumberContext          numberCtx   = ctx.number();
        MonkeyCParser.UnsignedNumberContext  unsignedCtx = 
                                                    numberCtx.unsignedNumber();
        MonkeyCParser.IntegerConstantContext integerCtx  = 
                                                unsignedCtx.integerConstant();

        ctx.type = (integerCtx != null) ? Predefined.integerType
                                        : Predefined.realType;
        
        return null;
    }

    @Override 
    public Object visitCharacterFactor(
                                    MonkeyCParser.CharacterFactorContext ctx) 
    {
        ctx.type = Predefined.charType;
        return null;
    }

    @Override 
    public Object visitStringFactor(MonkeyCParser.StringFactorContext ctx) 
    {
        ctx.type = Predefined.stringType;
        return null;
    }

    @Override 
    public Object visitNotFactor(MonkeyCParser.NotFactorContext ctx) 
    {
        MonkeyCParser.FactorContext factorCtx = ctx.factor();
        visit(factorCtx);
        
        if (factorCtx.type != Predefined.booleanType)
        {
            error.flag(TYPE_MUST_BE_BOOLEAN, factorCtx);
        }
        
        ctx.type = Predefined.booleanType;
        return null;
    }

    @Override 
    public Object visitParenthesizedFactor(
                                    MonkeyCParser.ParenthesizedFactorContext ctx) 
    {
        MonkeyCParser.ExpressionContext exprCtx = ctx.expression();
        visit(exprCtx);
        ctx.type = exprCtx.type;

        return null;
    }
}
