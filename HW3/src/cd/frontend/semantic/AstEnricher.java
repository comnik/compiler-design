package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.util.Pair;
import cd.util.TypeUtils;

/**
 * Fills the symbol table hierarchy with
 * everything that can be inferred without a global symbol information.
 */
public class AstEnricher extends AstVisitor<Symbol,Void> {

    @Override
    public Symbol.ClassSymbol classDecl(Ast.ClassDecl ast, Void arg) {
        Symbol.ClassSymbol clsSymbol = new Symbol.ClassSymbol(ast);

        // Add member symbols.
        ast.fields().stream().forEach(fieldNode -> {
            if (clsSymbol.fields.containsKey(fieldNode.name)) {
                String errorFmt = "Class %s contains two fields named %s.";
                throw new SemanticFailure(
                        SemanticFailure.Cause.DOUBLE_DECLARATION,errorFmt, clsSymbol.name, fieldNode.name);
            } else {
                Symbol.VariableSymbol memberSymbol = (Symbol.VariableSymbol) visit(fieldNode, null);
                clsSymbol.fields.put(fieldNode.name, memberSymbol);
            }
        });

        // Add method symbols.
        ast.methods().stream().forEach(methodNode -> {
            if (clsSymbol.methods.containsKey(methodNode.name)) {
                String errorFmt = "Class %s contains two methods named %s.";
                throw new SemanticFailure(
                        SemanticFailure.Cause.DOUBLE_DECLARATION,errorFmt, clsSymbol.name, methodNode.name);
            } else {
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) visit(methodNode, null);
                clsSymbol.methods.put(methodNode.name, methodSymbol);
            }
        });

        ast.sym = clsSymbol;
        return clsSymbol;
    }

    @Override
    public Symbol.MethodSymbol methodDecl(Ast.MethodDecl ast, Void arg) {
        ast.sym = new Symbol.MethodSymbol(ast);

        // Create symbols for every argument.
        Pair.zip(ast.argumentNames, ast.argumentTypes).stream().forEach(argPair -> {
            if (ast.sym.locals.containsKey(argPair.a)) {
                String errorFmt = "Name clash '%s' in method %s.";
                throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, argPair.a, ast.name);
            } else {
                Symbol.VariableSymbol varSym = varSymFromString(argPair.a, argPair.b);
                ast.sym.locals.put(argPair.a, varSym);
                ast.sym.parameters.add(varSym);
            }
        });

        // Create symbols for local variables.
        ast.decls().rwChildren().stream().forEach(childNode -> {
            String name = childNode.toString();

            if (ast.sym.locals.containsKey(name)) {
                String errorFmt = "Method %s contains two locals named %s.";
                throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, ast.name, name);
            } else {
                ast.sym.locals.put(name, (Symbol.VariableSymbol) visit(childNode, null));
            }
        });

        // Parse the return type.
        ast.sym.returnType = TypeUtils.typeFromStr(ast.returnType);

        return ast.sym;
    }

    @Override
    public Symbol.VariableSymbol varDecl(Ast.VarDecl ast, Void arg) {
        ast.sym = varSymFromString(ast.name, ast.type);
        return ast.sym;
    }


    // Utility methods

    /**
     * Abstracts the creation of a new VariableSymbol from a name and a type string.
     */
    private Symbol.VariableSymbol varSymFromString(String name, String type) {
        Symbol.TypeSymbol typeSymbol = TypeUtils.typeFromStr(type);
        return new Symbol.VariableSymbol(name, typeSymbol);
    }

}
