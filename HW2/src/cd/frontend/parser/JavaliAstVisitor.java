package cd.frontend.parser;

import cd.frontend.parser.JavaliParser.ClassDeclContext;
import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaliAstVisitor extends JavaliBaseVisitor<Ast> {
	
	public List<ClassDecl> classDecls = new ArrayList<ClassDecl>();

    private static final String TYPE_OBJECT = "Object";
    private static final String WRITE_LN = "writeln";

	@Override
	public ClassDecl visitClassDecl(ClassDeclContext ctx) {
        String className = ctx.getChild(1).getText();
        String superName = TYPE_OBJECT;
        if (ctx.getChildCount() > 5) {
            superName = ctx.getChild(3).getText();
        }

        List<Ast> members = new ArrayList<Ast>();
        for (int i = 0; i < ctx.memberList().getChildCount(); i++) {
            members.add(visit(ctx.memberList().getChild(i)));
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
    public Ast.IntConst visitInteger(JavaliParser.IntegerContext ctx) {
        return new Ast.IntConst(Integer.parseInt(ctx.Integer().getText()));
    }

    @Override
    public Ast.BooleanConst visitBoolean(JavaliParser.BooleanContext ctx) {
        return new Ast.BooleanConst(Boolean.parseBoolean(ctx.Boolean().getText()));
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

    @Override
    public Ast.BinaryOp visitBinaryOp(JavaliParser.BinaryOpContext ctx) {
        Ast.Expr leftOperand = (Ast.Expr) visit(ctx.expr(0));
        Ast.Expr rightOperand = (Ast.Expr) visit(ctx.expr(1));
        Ast.BinaryOp.BOp op = opFromString(ctx.getChild(1).getText());

        return new Ast.BinaryOp(leftOperand, op, rightOperand);
    }

    @Override
    public Ast.Expr visitBrackets(JavaliParser.BracketsContext ctx) {
        return (Ast.Expr) visit(ctx.expr());
    }

    @Override
    public Ast.BuiltInRead visitReadExpr(JavaliParser.ReadExprContext ctx) {
        return new Ast.BuiltInRead();
    }

    @Override
    public Ast.MethodCallExpr visitMethodCallExpr(JavaliParser.MethodCallExprContext ctx) {
        String methodName = ctx.Identifier().getText();

        Ast.Expr qualifier = new Ast.ThisRef();
        if (ctx.identAccess() != null) {
            // Qualified access.
            qualifier = (Ast.Expr) visit(ctx.identAccess());
        }

        // Arguments.
        List<Ast.Expr> args = new ArrayList<Ast.Expr>();
        if (ctx.actualParamList() != null) {
            args = ctx.actualParamList().expr().stream()
                    .map(exprCtx -> (Ast.Expr) visit(exprCtx))
                    .collect(Collectors.toList());
        }

        return new Ast.MethodCallExpr(qualifier, methodName, args);
    }

    @Override
    public Ast.Field visitFieldAccess(JavaliParser.FieldAccessContext ctx) {
        String fieldName = ctx.Identifier().getText();
        Ast.Expr expr = (Ast.Expr) visit(ctx.identAccess());

        return new Ast.Field(expr, fieldName);
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
