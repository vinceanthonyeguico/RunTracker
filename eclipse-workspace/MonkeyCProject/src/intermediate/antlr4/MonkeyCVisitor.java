// Generated from MonkeyC.g4 by ANTLR 4.13.2

    package intermediate.antlr4;
    import java.util.HashMap;
    import intermediate.symtab.SymtabEntry;
    import intermediate.type.Typespec;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MonkeyCParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MonkeyCVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MonkeyCParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#programHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramHeader(MonkeyCParser.ProgramHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#programParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramParameters(MonkeyCParser.ProgramParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#programIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramIdentifier(MonkeyCParser.ProgramIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(MonkeyCParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#declarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarations(MonkeyCParser.DeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#constantsPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantsPart(MonkeyCParser.ConstantsPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#constantDefinitionsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefinitionsList(MonkeyCParser.ConstantDefinitionsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#constantDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefinition(MonkeyCParser.ConstantDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#constantIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantIdentifier(MonkeyCParser.ConstantIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(MonkeyCParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(MonkeyCParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#typesPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypesPart(MonkeyCParser.TypesPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#typeDefinitionsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinitionsList(MonkeyCParser.TypeDefinitionsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#typeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinition(MonkeyCParser.TypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#typeIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeIdentifier(MonkeyCParser.TypeIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleTypespec}
	 * labeled alternative in {@link MonkeyCParser#typeSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypespec(MonkeyCParser.SimpleTypespecContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayTypespec}
	 * labeled alternative in {@link MonkeyCParser#typeSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayTypespec(MonkeyCParser.ArrayTypespecContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recordTypespec}
	 * labeled alternative in {@link MonkeyCParser#typeSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordTypespec(MonkeyCParser.RecordTypespecContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeIdentifierTypespec}
	 * labeled alternative in {@link MonkeyCParser#simpleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeIdentifierTypespec(MonkeyCParser.TypeIdentifierTypespecContext ctx);
	/**
	 * Visit a parse tree produced by the {@code enumerationTypespec}
	 * labeled alternative in {@link MonkeyCParser#simpleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationTypespec(MonkeyCParser.EnumerationTypespecContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subrangeTypespec}
	 * labeled alternative in {@link MonkeyCParser#simpleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubrangeTypespec(MonkeyCParser.SubrangeTypespecContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#enumerationType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationType(MonkeyCParser.EnumerationTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#enumerationConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationConstant(MonkeyCParser.EnumerationConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#subrangeType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubrangeType(MonkeyCParser.SubrangeTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(MonkeyCParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#arrayDimensionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayDimensionList(MonkeyCParser.ArrayDimensionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#recordType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordType(MonkeyCParser.RecordTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#recordFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordFields(MonkeyCParser.RecordFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variablesPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariablesPart(MonkeyCParser.VariablesPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variableDeclarationsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarationsList(MonkeyCParser.VariableDeclarationsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variableDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarations(MonkeyCParser.VariableDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variableIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableIdentifierList(MonkeyCParser.VariableIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variableIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableIdentifier(MonkeyCParser.VariableIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#routinesPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutinesPart(MonkeyCParser.RoutinesPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#routineDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineDefinition(MonkeyCParser.RoutineDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#procedureHead}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureHead(MonkeyCParser.ProcedureHeadContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#functionHead}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionHead(MonkeyCParser.FunctionHeadContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#routineIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineIdentifier(MonkeyCParser.RoutineIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#parameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameters(MonkeyCParser.ParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#parameterDeclarationsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclarationsList(MonkeyCParser.ParameterDeclarationsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#parameterDeclarations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclarations(MonkeyCParser.ParameterDeclarationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#parameterIdentifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterIdentifierList(MonkeyCParser.ParameterIdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#parameterIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterIdentifier(MonkeyCParser.ParameterIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MonkeyCParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(MonkeyCParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(MonkeyCParser.EmptyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#statementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementList(MonkeyCParser.StatementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(MonkeyCParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#lhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLhs(MonkeyCParser.LhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#rhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRhs(MonkeyCParser.RhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(MonkeyCParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#trueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrueStatement(MonkeyCParser.TrueStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#falseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFalseStatement(MonkeyCParser.FalseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#caseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseStatement(MonkeyCParser.CaseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#caseBranchList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseBranchList(MonkeyCParser.CaseBranchListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#caseBranch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseBranch(MonkeyCParser.CaseBranchContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#caseConstantList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseConstantList(MonkeyCParser.CaseConstantListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#caseConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseConstant(MonkeyCParser.CaseConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#repeatStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatStatement(MonkeyCParser.RepeatStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(MonkeyCParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(MonkeyCParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#procedureCallStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureCallStatement(MonkeyCParser.ProcedureCallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#procedureName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureName(MonkeyCParser.ProcedureNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(MonkeyCParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(MonkeyCParser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#writeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteStatement(MonkeyCParser.WriteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#writelnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWritelnStatement(MonkeyCParser.WritelnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#writeArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteArguments(MonkeyCParser.WriteArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#writeArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteArgument(MonkeyCParser.WriteArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#fieldWidth}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldWidth(MonkeyCParser.FieldWidthContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#decimalPlaces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalPlaces(MonkeyCParser.DecimalPlacesContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#readStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadStatement(MonkeyCParser.ReadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#readlnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadlnStatement(MonkeyCParser.ReadlnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#readArguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadArguments(MonkeyCParser.ReadArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(MonkeyCParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#simpleExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpression(MonkeyCParser.SimpleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(MonkeyCParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variableFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableFactor(MonkeyCParser.VariableFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberFactor(MonkeyCParser.NumberFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code characterFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterFactor(MonkeyCParser.CharacterFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFactor(MonkeyCParser.StringFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallFactor(MonkeyCParser.FunctionCallFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotFactor(MonkeyCParser.NotFactorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesizedFactor}
	 * labeled alternative in {@link MonkeyCParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedFactor(MonkeyCParser.ParenthesizedFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(MonkeyCParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#modifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifier(MonkeyCParser.ModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#indexList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexList(MonkeyCParser.IndexListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(MonkeyCParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(MonkeyCParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(MonkeyCParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(MonkeyCParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(MonkeyCParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#unsignedNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnsignedNumber(MonkeyCParser.UnsignedNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#integerConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerConstant(MonkeyCParser.IntegerConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#realConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealConstant(MonkeyCParser.RealConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#characterConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterConstant(MonkeyCParser.CharacterConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#stringConstant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstant(MonkeyCParser.StringConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#relOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelOp(MonkeyCParser.RelOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#addOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddOp(MonkeyCParser.AddOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link MonkeyCParser#mulOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulOp(MonkeyCParser.MulOpContext ctx);
}