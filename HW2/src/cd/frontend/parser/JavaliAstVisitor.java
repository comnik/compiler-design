package cd.frontend.parser;

import cd.frontend.parser.JavaliParser.ClassDeclContext;
import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaliAstVisitor extends JavaliBaseVisitor<Ast> {
	
	public List<ClassDecl> classDecls = new ArrayList<ClassDecl>();

    private static final String TYPE_OBJECT = "Object";
    private static final String WRITE_LN = "writeln";
    private static final String NULL = "null";

    private static final int MAX_INT = 2147483647;
    private static final int MIN_INT = -MAX_INT;

	@Override
	public ClassDecl visitClassDecl(ClassDeclContext ctx) {
        String className = ctx.getChild(1).getText();
        String superName = TYPE_OBJECT;
        if (ctx.getChildCount() > 5) {
            superName = ctx.getChild(3).getText();
        }

        List<Ast> members = new ArrayList<Ast>();
        for (int i = 0; i < ctx.memberList().getChildCount(); i++) {
            Ast member = visit(ctx.memberList().getChild(i));

            // Handle Seq special case.
            if (member instanceof Ast.Seq) {
                members.addAll(((Ast.Seq) member).rwChildren());
            } else {
                members.add(member);
            }
        }

        ClassDecl cls = new ClassDecl(className, superName, members);
        classDecls.add(cls);

        return cls;
    }

    @Override
    public Ast.MethodDecl visitMethodDecl(JavaliParser.MethodDeclContext ctx) {
        String type = ctx.getChild(0).getText();
        String methodName = ctx.Identifier().getText();

        // Handle parameter list.
        List<String> paramNames = new ArrayList<String>();
        List<String> typeNames = new ArrayList<String>();

        if (ctx.formalParamList() != null) {
            paramNames = ctx.formalParamList().Identifier().stream()
                    .map(TerminalNode::getText)
                    .collect(Collectors.toList());

            typeNames = ctx.formalParamList().type().stream()
                    .map(JavaliParser.TypeContext::getText)
                    .collect(Collectors.toList());
        }

        // Handle body declarations.
        List<Ast> declNodes = new ArrayList<Ast>();
        if (ctx.varDecl() != null) {
            ctx.varDecl().stream().forEach(varCtx -> {
                Ast varDecl = visit(varCtx);
                if (varDecl instanceof Ast.Seq) {
                    declNodes.addAll(((Ast.Seq) varDecl).rwChildren());
                } else {
                    declNodes.add(varDecl);
                }
            });
        }

        // Handle body statements.
        List<Ast> stmtNodes = new ArrayList<Ast>();
        if (ctx.stmt() != null) {
            stmtNodes = ctx.stmt().stream()
                    .map(stmtCtx -> visit(stmtCtx))
                    .collect(Collectors.toList());
        }

        Ast.Seq decls = new Ast.Seq(declNodes);
        Ast.Seq stmts = new Ast.Seq(stmtNodes);

        return new Ast.MethodDecl(type, methodName, typeNames, paramNames, decls, stmts);
    }

    @Override
    public Ast visitVarDecl(JavaliParser.VarDeclContext ctx) {
        String type = ctx.type().getText();
        List<Ast> varDecls = ctx.Identifier().stream()
                .map(tNode -> new Ast.VarDecl(type, tNode.getText()))
                .collect(Collectors.toList());

        if (varDecls.size() == 1) {
            return varDecls.get(0);
        } else {
            return new Ast.Seq(varDecls);
        }
    }

    /**
     * Expressions
     */

    @Override
    public Ast visitNewExpr(JavaliParser.NewExprContext ctx) {
        String typeName;
        Ast.Expr capacity;

        if (ctx.Identifier() != null) {
            typeName = ctx.Identifier().getText();
        } else {
            // Primitive array.
            typeName = ctx.primitiveType().getText();
        }

        if (ctx.expr() != null) {
            // New Array.
            capacity = (Ast.Expr) visit(ctx.expr());
            return new Ast.NewArray(typeName + "[]", capacity);
        } else {
            return new Ast.NewObject(typeName);
        }
    }

    @Override
    public Ast.IntConst visitInteger(JavaliParser.IntegerContext ctx) {
        try {
            int parsedInt = Integer.parseInt(ctx.Integer().getText());
            if (parsedInt > MAX_INT || parsedInt < MIN_INT) {
                throw new NumberFormatException();
            }

            return new Ast.IntConst(parsedInt);
        } catch (NumberFormatException ex) {
            throw new ParseFailure(ctx.getSourceInterval().a, "Integer " + ctx.Integer().getText() + " exceeds int bounds.");
        }
    }

    @Override
    public Ast.BooleanConst visitBoolean(JavaliParser.BooleanContext ctx) {
        return new Ast.BooleanConst(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public Ast.NullConst visitNull(JavaliParser.NullContext ctx) {
        return new Ast.NullConst();
    }

    @Override
    public Ast.UnaryOp visitUnaryOp(JavaliParser.UnaryOpContext ctx) {
        Ast.Expr operand = (Ast.Expr) visit(ctx.expr());
        Ast.UnaryOp.UOp op = uOpFromString(ctx.getChild(0).getText());

        return new Ast.UnaryOp(op, operand);
    }

    @Override
    public Ast.Cast visitCast(JavaliParser.CastContext ctx) {
        String typeName = ctx.referenceType().getText();
        Ast.Expr expr = (Ast.Expr) visit(ctx.expr());

        return new Ast.Cast(expr, typeName);
    }

    /**
     * Generic binary op handling.
     */
    private Ast.BinaryOp genericBinaryOp(
            JavaliParser.ExprContext leftExprCtx, JavaliParser.ExprContext rightExprCtx, String opRepr) {
        Ast.Expr leftOperand = (Ast.Expr) visit(leftExprCtx);
        Ast.Expr rightOperand = (Ast.Expr) visit(rightExprCtx);
        Ast.BinaryOp.BOp op = opFromString(opRepr);

        return new Ast.BinaryOp(leftOperand, op, rightOperand);
    }

    @Override
    public Ast.BinaryOp visitBinaryAdd(JavaliParser.BinaryAddContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.BinaryOp visitBinaryMul(JavaliParser.BinaryMulContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.BinaryOp visitCompare(JavaliParser.CompareContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.BinaryOp visitEquality(JavaliParser.EqualityContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.BinaryOp visitLogicalAnd(JavaliParser.LogicalAndContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.BinaryOp visitLogicalOr(JavaliParser.LogicalOrContext ctx) {
        return genericBinaryOp(ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public Ast.Expr visitBrackets(JavaliParser.BracketsContext ctx) {
        return (Ast.Expr) visit(ctx.expr());
    }

    @Override
    public Ast.BuiltInRead visitReadExpr(JavaliParser.ReadExprContext ctx) {
        return new Ast.BuiltInRead();
    }

    /**
     * Abstracts the MethodCallExpr handling, so it can be re-used
     * in identAccess without changes to the RuleContext hierarchy.
     *
     * @param identifier Identifies the method.
     * @param qualifier Optional object on which the method should be called.
     */
    private Ast.MethodCallExpr genericMethodCallExpr(
            TerminalNode identifier, JavaliParser.IdentAccessContext qualifier, JavaliParser.ActualParamListContext params) {
        String methodName = identifier.getText();

        Ast.Expr qualifierExpr = new Ast.ThisRef();
        if (qualifier != null) {
            // Qualified access.
            qualifierExpr = (Ast.Expr) visit(qualifier);
        }

        // Arguments.
        List<Ast.Expr> args = new ArrayList<Ast.Expr>();
        if (params != null) {
            args = params.expr().stream()
                    .map(exprCtx -> (Ast.Expr) visit(exprCtx))
                    .collect(Collectors.toList());
        }

        return new Ast.MethodCallExpr(qualifierExpr, methodName, args);
    }

    @Override
    public Ast.MethodCallExpr visitMethodCallExpr(JavaliParser.MethodCallExprContext ctx) {
        return genericMethodCallExpr(ctx.Identifier(), ctx.identAccess(), ctx.actualParamList());
    }

    @Override
    public Ast.MethodCallExpr visitQualified(JavaliParser.QualifiedContext ctx) {
        return genericMethodCallExpr(ctx.Identifier(), ctx.identAccess(), ctx.actualParamList());
    }

    @Override
    public Ast.MethodCallExpr visitUnqualified(JavaliParser.UnqualifiedContext ctx) {
        return genericMethodCallExpr(ctx.Identifier(), null, ctx.actualParamList());
    }

    @Override
    public Ast.Field visitFieldAccess(JavaliParser.FieldAccessContext ctx) {
        String fieldName = ctx.Identifier().getText();
        Ast.Expr expr = (Ast.Expr) visit(ctx.identAccess());

        return new Ast.Field(expr, fieldName);
    }

    @Override
    public Ast.Index visitArrayAccess(JavaliParser.ArrayAccessContext ctx) {
        Ast.Expr array = (Ast.Expr) visit(ctx.identAccess());
        Ast.Expr index = (Ast.Expr) visit(ctx.expr());

        return new Ast.Index(array, index);
    }

    @Override
    public Ast.Var visitIdentifier(JavaliParser.IdentifierContext ctx) {
        return new Ast.Var(ctx.getText());
    }

    @Override
    public Ast.ThisRef visitThis(JavaliParser.ThisContext ctx) {
        return new Ast.ThisRef();
    }

    /**
     * Statements
     */

    @Override
    public Ast.MethodCall visitMethodCallStmt(JavaliParser.MethodCallStmtContext ctx) {
        Ast.MethodCallExpr methodCallExpr = visitMethodCallExpr(ctx.methodCallExpr());
        return new Ast.MethodCall(methodCallExpr);
    }

    @Override
    public Ast.Assign visitAssignmentStmt(JavaliParser.AssignmentStmtContext ctx) {
        Ast.Expr rightHandSide = (Ast.Expr) visit(ctx.getChild(2));
        Ast.Expr leftHandSide = (Ast.Expr) visit(ctx.identAccess());

        return new Ast.Assign(leftHandSide, rightHandSide);
    }

    @Override
    public Ast visitWriteStmt(JavaliParser.WriteStmtContext ctx) {
        if (ctx.getChild(0).getText().equals(WRITE_LN)) {
            return new Ast.BuiltInWriteln();
        } else {
            Ast.Expr expr = (Ast.Expr) visit(ctx.expr());
            return new Ast.BuiltInWrite(expr);
        }
    }

    @Override
    public Ast.IfElse visitIfStmt(JavaliParser.IfStmtContext ctx) {
        Ast.Expr cond = (Ast.Expr) visit(ctx.expr());
        Ast.Stmt thenBlock = (Ast.Stmt) visitStmtBlock(ctx.stmtBlock(0));
        Ast.Stmt elseBlock = null;

        if (ctx.stmtBlock(1) != null) {
            elseBlock = (Ast.Stmt) visitStmtBlock(ctx.stmtBlock(1));
        }

        return new Ast.IfElse(cond, thenBlock, elseBlock);
    }

    @Override
    public Ast.ReturnStmt visitReturnStmt(JavaliParser.ReturnStmtContext ctx) {
        Ast.Expr arg = (Ast.Expr) visit(ctx.expr());
        return new Ast.ReturnStmt(arg);
    }

    /**
     * Helper Methods
     */
    private Ast.BinaryOp.BOp opFromString(String opStr) {
        for (Ast.BinaryOp.BOp op : Ast.BinaryOp.BOp.values()) {
            if (opStr.equals(op.repr))
                return op;
        }

        return null;
    }

    private Ast.UnaryOp.UOp uOpFromString(String opStr) {
        for (Ast.UnaryOp.UOp op : Ast.UnaryOp.UOp.values()) {
            if (opStr.equals(op.repr))
                return op;
        }

        return null;
    }

}
