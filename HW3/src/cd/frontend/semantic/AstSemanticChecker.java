package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class AstSemanticChecker extends AstVisitor<Void,Symbol> {

    Map<String, Symbol.ClassSymbol> globalSymbolTable;

    public AstSemanticChecker(Map<String, Symbol.ClassSymbol> globalSymTable) {
        this.globalSymbolTable = globalSymTable;
    }

    @Override
    public Void classDecl(Ast.ClassDecl ast, Symbol parent) {
        // NO_SUCH_TYPE
        if (!ast.sym.superClass.isReferenceType()) {
            // Only reference types can be extended from.
            String errorFmt = "Class %s extends primitive type.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.NO_SUCH_TYPE, errorFmt, ast.name);
        } else if (!(globalSymbolTable.containsKey(ast.superClass))) {
            // Extended types have to exist.
            String errorFmt = "Class %s extends non-existent type.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.NO_SUCH_TYPE, errorFmt, ast.name);
        }

        // Check members.
        ast.members().stream().forEach(node -> visit(node, globalSymbolTable.get(ast.name)));

        return null;
    }

    @Override
    public Void methodDecl(Ast.MethodDecl ast, Symbol parent) {
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) parent;

        // INVALID_OVERRIDE
        Symbol.MethodSymbol superMethod = classSymbol.superClass.getMethod(ast.sym.name);
        if (superMethod != null) {
            if (!sameSignature(ast.sym, superMethod)) {
                String errorFmt = "Method %s overrides super method, but signatures don't match.";
                throw new SemanticFailure(SemanticFailure.Cause.INVALID_OVERRIDE, errorFmt, ast.sym.name);
            }
        }

        // MISSING_RETURN
        if (!ast.sym.returnType.name.equals(Symbol.PrimitiveTypeSymbol.voidType.name)) {
            // This method should return something.
            if (!(hasReturn(ast.body()))) {
                throw new SemanticFailure(SemanticFailure.Cause.MISSING_RETURN);
            }
        }

        // NO_SUCH_TYPE
        if (!ast.sym.parameters.stream()
                .filter(param -> param.type instanceof Symbol.ClassSymbol)
                .allMatch(varSymbol -> globalSymbolTable.containsKey(varSymbol.type.name))) {
            // Some argument type doesn't exist.
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        // Check method body.
        return visit(ast.body(), parent);
    }

    @Override
    public Void seq(Ast.Seq ast, Symbol parent) {
        ast.rwChildren().stream().forEach(node -> visit(node, parent));
        return null;
    }

    @Override
    public Void assign(Ast.Assign ast, Symbol parent){
        // NOT_ASSIGNABLE
        if (ast.left().getClass().getSimpleName().equals("ThisRef")){
            throw new SemanticFailure(SemanticFailure.Cause.NOT_ASSIGNABLE);
        } else if (ast.left().getClass().getSimpleName().equals("MethodCallExpr")){
            throw new SemanticFailure((SemanticFailure.Cause.NOT_ASSIGNABLE));
        }
        return null;
    }


    // Utility methods.

    private boolean hasReturn(Ast ast){
        // Build a list of ast children names.
        List<String> listOfChildren = ast.children().stream()
                .map(ast1 -> ast1.getClass().getSimpleName())
                .collect(Collectors.toList());

        if (listOfChildren.contains("ReturnStmt")) {
            return true;
        } else if (listOfChildren.contains("IfElse")) {
            // Build a list of then and otherwise parts of IfElse
            return ast.children().stream()
                    .filter(ast2 -> ast2.getClass().getSimpleName().equals("IfElse"))
                    .anyMatch( ifElseAst -> { return ((hasReturn(((Ast.IfElse) ifElseAst).then())) &&
                                (hasReturn(((Ast.IfElse) ifElseAst).otherwise())));
                            }
                    );
        }
        return false;
    }

    private boolean sameSignature(Symbol.MethodSymbol m1, Symbol.MethodSymbol m2) {
        if (!m1.returnType.name.equals(m2.returnType.name) || m1.parameters.size() != m2.parameters.size()) {
            return false;
        } else {
            // Compare parameter types.
            return Pair.zip(m1.parameters, m2.parameters).stream()
                    .allMatch(paramPair -> paramPair.a.type.name.equals(paramPair.b.type.name));
        }
    }

}
