// Generated from ../src/cd/frontend/parser/Javali.g4 by ANTLR 4.4

	// Java header
	package cd.frontend.parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JavaliParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JavaliVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JavaliParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(@NotNull JavaliParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#newExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(@NotNull JavaliParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#writeStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteStmt(@NotNull JavaliParser.WriteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#stmtBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmtBlock(@NotNull JavaliParser.StmtBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#memberList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberList(@NotNull JavaliParser.MemberListContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#varDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(@NotNull JavaliParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(@NotNull JavaliParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#returnStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStmt(@NotNull JavaliParser.ReturnStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(@NotNull JavaliParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#methodCallStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallStmt(@NotNull JavaliParser.MethodCallStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(@NotNull JavaliParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#classDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDecl(@NotNull JavaliParser.ClassDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#whileStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStmt(@NotNull JavaliParser.WhileStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#unit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnit(@NotNull JavaliParser.UnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#referenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceType(@NotNull JavaliParser.ReferenceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(@NotNull JavaliParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#primitiveType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveType(@NotNull JavaliParser.PrimitiveTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#assignmentStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStmt(@NotNull JavaliParser.AssignmentStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#methodDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDecl(@NotNull JavaliParser.MethodDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#formalParamList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParamList(@NotNull JavaliParser.FormalParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#ifStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(@NotNull JavaliParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#identAccess}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentAccess(@NotNull JavaliParser.IdentAccessContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#methodCallExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallExpr(@NotNull JavaliParser.MethodCallExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#readExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadExpr(@NotNull JavaliParser.ReadExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JavaliParser#actualParamList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActualParamList(@NotNull JavaliParser.ActualParamListContext ctx);
}