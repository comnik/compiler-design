package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.util.Pair;
import cd.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Fills the symbol table hierarchy with
 * everything that can be inferred without a global symbol information.
 */
public class AstEnricher extends AstVisitor<Symbol,Map<String,Symbol.VariableSymbol>> {

    @Override
    public Symbol.ClassSymbol classDecl(Ast.ClassDecl ast, Map<String,Symbol.VariableSymbol> scope) {
        Symbol.ClassSymbol clsSymbol = new Symbol.ClassSymbol(ast);

        // Create superclass symbol.
        clsSymbol.superClass = (Symbol.ClassSymbol) TypeUtils.typeFromStr(ast.superClass);

        // Add member symbols.
        ast.fields().stream().forEach(fieldNode -> {
            if (clsSymbol.fields.containsKey(fieldNode.name)) {
                String errorFmt = "Class %s contains two fields named %s.";
                throw new SemanticFailure(
                        SemanticFailure.Cause.DOUBLE_DECLARATION,errorFmt, clsSymbol.name, fieldNode.name);
            } else {
                Symbol.VariableSymbol memberSymbol = (Symbol.VariableSymbol) visit(fieldNode, clsSymbol.fields);
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
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) visit(methodNode, clsSymbol.fields);
                clsSymbol.methods.put(methodNode.name, methodSymbol);
            }
        });

        ast.sym = clsSymbol;
        return clsSymbol;
    }

    @Override
    public Symbol.MethodSymbol methodDecl(Ast.MethodDecl ast, Map<String,Symbol.VariableSymbol> scope) {
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
        visit(ast.decls(), ast.sym.locals);

        // Add type information to symbol occurrences.
        visit(ast.body(), ast.sym.locals);

        return ast.sym;
    }

    @Override
    public Symbol seq(Ast.Seq ast, Map<String,Symbol.VariableSymbol> scope) {
        ast.rwChildren().stream().forEach(node -> visit(node, scope));
        return null;
    }

    @Override
    public Symbol.VariableSymbol varDecl(Ast.VarDecl ast, Map<String,Symbol.VariableSymbol> scope) {
        ast.sym = varSymFromString(ast.name, ast.type);

        if (scope.containsKey(ast.name)) {
            String errorFmt = "Two local variables named %s.";
            throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, ast.name);
        } else {
            scope.put(ast.name, ast.sym);
        }

        return ast.sym;
    }

    @Override
    public Symbol.VariableSymbol var(Ast.Var ast, Map<String,Symbol.VariableSymbol> scope) {
        Symbol.VariableSymbol varSym = scope.get(ast.name);
        // NO_SUCH_VARIABLE
        if (varSym != null) {
            ast.setSymbol(varSym);
        } else {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_VARIABLE);
        }
        return varSym;
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
