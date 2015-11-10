package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

import java.util.Map;

/**
 *
 */
public class AstSemanticChecker extends AstVisitor<Void, Map<String,Symbol.ClassSymbol>> {

    @Override
    public Void classDecl(Ast.ClassDecl ast, Map<String,Symbol.ClassSymbol> globalSymbolTable) {
        // only class types can be extended from
        if (!(globalSymbolTable.containsValue(ast.superClass))) {
            String errorFmt = "Class %s extends non-existent type.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.NO_SUCH_TYPE, errorFmt, ast.sym.name);

        }

        return null;
    }

    @Override
    public Void ifElse(Ast.IfElse ast, Map<String,Symbol.ClassSymbol> globalSymbolTable) {
        if (ast.condition().type.toString() != "boolean" ){
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }

}
