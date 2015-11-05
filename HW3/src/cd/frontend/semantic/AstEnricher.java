package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

/**
 *
 */
public class AstEnricher extends AstVisitor<Symbol,Void> {

    @Override
    public Symbol classDecl(Ast.ClassDecl ast, A arg) {
        return new Symbol.ClassSymbol(ast);
    }

}
