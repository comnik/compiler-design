package cd.frontend.parser;

import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;

import java.util.ArrayList;
import java.util.List;

import cd.frontend.parser.JavaliParser.ClassDeclContext;
import org.antlr.v4.runtime.tree.ParseTree;

public final class JavaliAstVisitor extends JavaliBaseVisitor<Ast> {
	
	public List<ClassDecl> classDecls = new ArrayList<>();

	@Override
	public Ast visitClassDecl(ClassDeclContext ctx) {
        String className = ctx.getChild(1).getText();
        String superName = null;
        if (ctx.getChildCount() > 5) {
            superName = ctx.getChild(5).getText();
        }

        List<Ast> members = new ArrayList<Ast>();
        for (ParseTree member : ctx.memberList().children) {
            members.add(visit(member));
        }

        ClassDecl cls = new ClassDecl(className, superName, members);
        classDecls.add(cls);

        return cls;
    }
}
