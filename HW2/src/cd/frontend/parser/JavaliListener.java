// Generated from ../src/cd/frontend/parser/Javali.g4 by ANTLR 4.4

	// Java header
	package cd.frontend.parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JavaliParser}.
 */
public interface JavaliListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JavaliParser#newExpr}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(@NotNull JavaliParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#newExpr}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(@NotNull JavaliParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCast(@NotNull JavaliParser.CastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCast(@NotNull JavaliParser.CastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Null}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNull(@NotNull JavaliParser.NullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Null}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNull(@NotNull JavaliParser.NullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Var}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVar(@NotNull JavaliParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Var}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVar(@NotNull JavaliParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#formalParamList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParamList(@NotNull JavaliParser.FormalParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#formalParamList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParamList(@NotNull JavaliParser.FormalParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceType(@NotNull JavaliParser.ReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceType(@NotNull JavaliParser.ReferenceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void enterMethodDecl(@NotNull JavaliParser.MethodDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#methodDecl}.
	 * @param ctx the parse tree
	 */
	void exitMethodDecl(@NotNull JavaliParser.MethodDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(@NotNull JavaliParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(@NotNull JavaliParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodCall}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(@NotNull JavaliParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodCall}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(@NotNull JavaliParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#methodCallExpr}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallExpr(@NotNull JavaliParser.MethodCallExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#methodCallExpr}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallExpr(@NotNull JavaliParser.MethodCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayAccess}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterArrayAccess(@NotNull JavaliParser.ArrayAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayAccess}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitArrayAccess(@NotNull JavaliParser.ArrayAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#memberList}.
	 * @param ctx the parse tree
	 */
	void enterMemberList(@NotNull JavaliParser.MemberListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#memberList}.
	 * @param ctx the parse tree
	 */
	void exitMemberList(@NotNull JavaliParser.MemberListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Identifier}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(@NotNull JavaliParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Identifier}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(@NotNull JavaliParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Brackets}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBrackets(@NotNull JavaliParser.BracketsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Brackets}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBrackets(@NotNull JavaliParser.BracketsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code This}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterThis(@NotNull JavaliParser.ThisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code This}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitThis(@NotNull JavaliParser.ThisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCall}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(@NotNull JavaliParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCall}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(@NotNull JavaliParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(@NotNull JavaliParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(@NotNull JavaliParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(@NotNull JavaliParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(@NotNull JavaliParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#assignmentStmt}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStmt(@NotNull JavaliParser.AssignmentStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#assignmentStmt}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStmt(@NotNull JavaliParser.AssignmentStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#actualParamList}.
	 * @param ctx the parse tree
	 */
	void enterActualParamList(@NotNull JavaliParser.ActualParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#actualParamList}.
	 * @param ctx the parse tree
	 */
	void exitActualParamList(@NotNull JavaliParser.ActualParamListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BinaryOp}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOp(@NotNull JavaliParser.BinaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BinaryOp}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOp(@NotNull JavaliParser.BinaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(@NotNull JavaliParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(@NotNull JavaliParser.ArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(@NotNull JavaliParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#whileStmt}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(@NotNull JavaliParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryOp}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOp(@NotNull JavaliParser.UnaryOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryOp}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOp(@NotNull JavaliParser.UnaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void enterClassDecl(@NotNull JavaliParser.ClassDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void exitClassDecl(@NotNull JavaliParser.ClassDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(@NotNull JavaliParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(@NotNull JavaliParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#readExpr}.
	 * @param ctx the parse tree
	 */
	void enterReadExpr(@NotNull JavaliParser.ReadExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#readExpr}.
	 * @param ctx the parse tree
	 */
	void exitReadExpr(@NotNull JavaliParser.ReadExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#writeStmt}.
	 * @param ctx the parse tree
	 */
	void enterWriteStmt(@NotNull JavaliParser.WriteStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#writeStmt}.
	 * @param ctx the parse tree
	 */
	void exitWriteStmt(@NotNull JavaliParser.WriteStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(@NotNull JavaliParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#returnStmt}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(@NotNull JavaliParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#methodCallStmt}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallStmt(@NotNull JavaliParser.MethodCallStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#methodCallStmt}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallStmt(@NotNull JavaliParser.MethodCallStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Integer}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInteger(@NotNull JavaliParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Integer}
	 * labeled alternative in {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInteger(@NotNull JavaliParser.IntegerContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#stmtBlock}.
	 * @param ctx the parse tree
	 */
	void enterStmtBlock(@NotNull JavaliParser.StmtBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#stmtBlock}.
	 * @param ctx the parse tree
	 */
	void exitStmtBlock(@NotNull JavaliParser.StmtBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#unit}.
	 * @param ctx the parse tree
	 */
	void enterUnit(@NotNull JavaliParser.UnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#unit}.
	 * @param ctx the parse tree
	 */
	void exitUnit(@NotNull JavaliParser.UnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(@NotNull JavaliParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(@NotNull JavaliParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldAccess}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void enterFieldAccess(@NotNull JavaliParser.FieldAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldAccess}
	 * labeled alternative in {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 */
	void exitFieldAccess(@NotNull JavaliParser.FieldAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(@NotNull JavaliParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(@NotNull JavaliParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JavaliParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(@NotNull JavaliParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link JavaliParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(@NotNull JavaliParser.StmtContext ctx);
}