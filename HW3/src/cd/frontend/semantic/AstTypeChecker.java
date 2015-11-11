package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

/**
 * Ensures type safety for an AST, given a global symbol table.
 * Updates the global symbol table with type information.
 */
public class AstTypeChecker extends AstVisitor<Void,Void> {

    @Override
    public Void classDecl(Ast.ClassDecl ast, Void arg) {
        // Check wether we are re-defining the builtin Object type.
        if (ast.sym.name.equals(Symbol.ClassSymbol.objectType.name)) {
            String errorFmt = "Name clash with builtin type 'Object'.";
            throw new SemanticFailure(SemanticFailure.Cause.OBJECT_CLASS_DEFINED, errorFmt);
        }

        // Create superclass symbol.
        ast.sym.superClass = new Symbol.ClassSymbol(ast.superClass);

        return null;
    }

}
