/**
 * <h1>Compiler</h1>
 *
 * <p>Compile to Jasmin assembly object code.</p>
 *
 * <p>Copyright (c) 2024 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
package backend.compiler;

import intermediate.antlr4.*;
import intermediate.symtab.*;
import intermediate.symtab.Predefined;

/**
 * Compile MonkeyC to Jasmin assembly language.
 */
public class Compiler extends MonkeyCBaseVisitor<Object>
{
    private CodeEmitter         emitter;
    private ProgramGenerator    programGenerator;
    private StatementGenerator  statementGenerator;
    private ExpressionGenerator expressionGenerator;
    
    /**
     * Constructor for the base compiler.
     * @param programEntry the symtab entry for the program name.
     */
    public Compiler(SymtabEntry programEntry)
    {
        String programName = programEntry.getName();
        
        emitter = new CodeEmitter(programName, "j");        
        createGenerators();
    }
    
    /**
     * Create new code generators.
     */
    private void createGenerators()
    {
        programGenerator    = new ProgramGenerator(this);
        statementGenerator  = new StatementGenerator(this);
        expressionGenerator = new ExpressionGenerator(this);
    }

    /**
     * Get the name of the object (Jasmin) file.
     * @return the name.
     */
    public String getObjectFileName() { return emitter.getObjectFileName(); }
    
    /**
     * Get the code emitter.
     * @return the emitter.
     */
    CodeEmitter getCodeEmitter() { return emitter; }
    
    @Override 
    public Object visitProgram(MonkeyCParser.ProgramContext ctx) 
    { 
        programGenerator.emitProgram(ctx);
        return null;
    }

    @Override 
    public Object visitStatement(MonkeyCParser.StatementContext ctx) 
    {
        if (   (ctx.compoundStatement() == null) 
            && (ctx.emptyStatement() == null))
        {
            emitter.emitComment(ctx);
        }
        
        return visitChildren(ctx);
    }

    @Override 
    public Object visitAssignmentStatement(
                                    MonkeyCParser.AssignmentStatementContext ctx) 
    {
        statementGenerator.emitAssignment(ctx);
        return null;
    }

    @Override 
    public Object visitIfStatement(MonkeyCParser.IfStatementContext ctx) 
    {
        statementGenerator.emitIf(ctx);
        return null;
    }

    @Override 
    public Object visitCaseStatement(MonkeyCParser.CaseStatementContext ctx) 
    {
        statementGenerator.emitCase(ctx);
        return null;
    }

    @Override 
    public Object visitRepeatStatement(MonkeyCParser.RepeatStatementContext ctx) 
    {
        statementGenerator.emitRepeat(ctx);
        return null;
    }

    @Override 
    public Object visitWhileStatement(MonkeyCParser.WhileStatementContext ctx) 
    {
        statementGenerator.emitWhile(ctx);
        return null;
    }

    @Override 
    public Object visitForStatement(MonkeyCParser.ForStatementContext ctx) 
    {
        statementGenerator.emitFor(ctx);
        return null;
    }

    @Override 
    public Object visitProcedureCallStatement(
                                MonkeyCParser.ProcedureCallStatementContext ctx) 
    {
        statementGenerator.emitProcedureCall(ctx);
        return null;
    }

    @Override 
    public Object visitExpression(MonkeyCParser.ExpressionContext ctx) 
    {
        expressionGenerator.emitExpression(ctx);
        return null;
    }

    @Override 
    public Object visitVariableFactor(MonkeyCParser.VariableFactorContext ctx) 
    {
        expressionGenerator.emitLoadValue(ctx.variable());
        return null;
    }

    @Override 
    public Object visitVariable(MonkeyCParser.VariableContext ctx) 
    {
        expressionGenerator.emitLoadVariable(ctx);        
        return null;
    }

    @Override 
    public Object visitNumberFactor(MonkeyCParser.NumberFactorContext ctx) 
    {
        if (ctx.type == Predefined.integerType) 
        {
            expressionGenerator.emitLoadIntegerConstant(ctx.number());
        }
        else
        {
            expressionGenerator.emitLoadRealConstant(ctx.number());
        }
        
        return null;
    }

    @Override 
    public Object visitCharacterFactor(MonkeyCParser.CharacterFactorContext ctx) 
    {
        char ch = ctx.getText().charAt(1);
        emitter.emitLoadConstant(ch);

        return null;
    }

    @Override 
    public Object visitStringFactor(MonkeyCParser.StringFactorContext ctx) 
    {
        String jasminString = convertString(ctx.getText());
        emitter.emitLoadConstant(jasminString);
        
        return null;
    }
    
    /**
     * Convert a MonkeyC string to a Java string.
     * @param MonkeyCString the MonkeyC string.
     * @return the Java string.
     */
    String convertString(String MonkeyCString)
    {
        String unquoted = MonkeyCString.substring(1, MonkeyCString.length()-1);
        return unquoted.replace("''", "'").replace("\"", "\\\"");
    }

    @Override 
    public Object visitFunctionCallFactor(
                                    MonkeyCParser.FunctionCallFactorContext ctx) 
    {
        statementGenerator.emitFunctionCall(ctx.functionCall());
        return null;
    }

    @Override 
    public Object visitNotFactor(MonkeyCParser.NotFactorContext ctx) 
    {
        expressionGenerator.emitNotFactor(ctx);
        return null;
    }

    @Override 
    public Object visitParenthesizedFactor(
                                    MonkeyCParser.ParenthesizedFactorContext ctx) 
    {
        return visit(ctx.expression());
    }

    @Override 
    public Object visitWriteStatement(MonkeyCParser.WriteStatementContext ctx) 
    {
        statementGenerator.emitWrite(ctx);
        return null;
    }

    @Override 
    public Object visitWritelnStatement(MonkeyCParser.WritelnStatementContext ctx) 
    {
        statementGenerator.emitWriteln(ctx);
        return null;
    }

    @Override 
    public Object visitReadStatement(MonkeyCParser.ReadStatementContext ctx) 
    {
        statementGenerator.emitRead(ctx);
        return null;
    }

    @Override 
    public Object visitReadlnStatement(MonkeyCParser.ReadlnStatementContext ctx) 
    {
        statementGenerator.emitReadln(ctx);
        return null;
    }
}
