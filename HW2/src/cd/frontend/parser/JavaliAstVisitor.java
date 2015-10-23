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

    private enum PrimitiveType {
        INT,
        BOOL
    };

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
            declNodes = ctx.varDecl().stream()
                    .map(varCtx -> visit(varCtx)).collect(Collectors.toList());
        }

        // Handle body statements.
        List<Ast> stmtNodes = new ArrayList<Ast>();
        if (ctx.stmt() != null) {
            stmtNodes = ctx.stmt().stream()
                    .map(stmtCtx -> visit(stmtCtx)).collect(Collectors.toList());
        }

        Ast.Seq decls = new Ast.Seq(declNodes);
        Ast.Seq stmts = new Ast.Seq(stmtNodes);

        return new Ast.MethodDecl(type, methodName, typeNames, paramNames, decls, stmts);
    }

    @Override
    public Ast.VarDecl visitVarDecl(JavaliParser.VarDeclContext ctx) {
        String type = ctx.getChild(0).getText();
        String name = ctx.getChild(1).getText();

        return new Ast.VarDecl(type, name);
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
    public Ast.BuiltInRead visitReadExpr(JavaliParser.ReadExprContext ctx) {
        return new Ast.BuiltInRead();
    }

    @Override
    public Ast.MethodCallExpr visitMethodCallExpr(JavaliParser.MethodCallExprContext ctx) {
        Ast.Expr recvr = (Ast.Expr) visit(ctx.identAccess());
        String methodName = ctx.Identifier().getText();

        return new Ast.MethodCallExpr(recvr, methodName, null);
    }

    /**
     * Statements
     */

    @Override
    public Ast.Assign visitAssignmentStmt(JavaliParser.AssignmentStmtContext ctx) {
        Ast.Expr rightHandSide = (Ast.Expr) visit(ctx.getChild(1));
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



}
