package cd.frontend.parser;

import cd.ToDoException;
import cd.ir.Ast.ClassDecl;

import java.util.ArrayList;
import java.util.List;

import cd.frontend.parser.JavaliParser.ClassDeclContext;
															// should be Ast, not Void
public final class JavaliAstVisitor extends JavaliBaseVisitor<Void> {
	
	public List<ClassDecl> classDecls = new ArrayList<>();
	@Override
	public Void visitClassDecl(ClassDeclContext ctx) {
		{
			// classDecls = ...;
			throw new ToDoException();
		}
		return null;
	}
}
