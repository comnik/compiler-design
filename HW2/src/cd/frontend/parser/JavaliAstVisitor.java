package cd.frontend.parser;

import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;

import java.util.ArrayList;
import java.util.List;

import cd.frontend.parser.JavaliParser.ClassDeclContext;
import cd.util.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public final class JavaliAstVisitor extends JavaliBaseVisitor<Ast> {
	
	public List<ClassDecl> classDecls = new ArrayList<>();

    private static final String CLS_OBJECT = "Object";

	@Override
	public Ast visitClassDecl(ClassDeclContext ctx) {
        String className = ctx.getChild(1).getText();
        String superName = CLS_OBJECT;
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
    public Ast visitMethodDecl(JavaliParser.MethodDeclContext ctx) {
        String type = ctx.getChild(0).getText();
        String methodName = ctx.getChild(1).getText();

        List<String> paramNames = new ArrayList<String>();
        List<String> typeNames = new ArrayList<String>();

        if (ctx.formalParamList() != null) {
            for (TerminalNode tNode : ctx.formalParamList().Identifier()) {
                paramNames.add(tNode.getText());
            }

            for (JavaliParser.TypeContext tCtx : ctx.formalParamList().type()) {
                typeNames.add(tCtx.getText());
            }
        }

        return new Ast.MethodDecl(type, methodName, typeNames, paramNames, null, null);
    }

    @Override
    public Ast visitVarDecl(JavaliParser.VarDeclContext ctx) {
        String type = ctx.getChild(0).getText();
        String name = ctx.getChild(1).getText();

        return new Ast.VarDecl(type, name);
    }
}
