package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.util.Pair;

/**
 *
 */
public class AstEnricher extends AstVisitor<Symbol,Void> {

    @Override
    public Symbol.ClassSymbol classDecl(Ast.ClassDecl ast, Void arg) {
        Symbol.ClassSymbol clsSymbol = new Symbol.ClassSymbol(ast);

        // create superclass symbol
        clsSymbol.superClass = new Symbol.ClassSymbol(ast.superClass);

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
        ast.body().rwChildren().stream().forEach(childNode -> {
            String name = childNode.toString();

            if (ast.sym.locals.containsKey(name)) {
                String errorFmt = "Method %s contains two locals named %s.";
                throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, ast.name, name);
            } else {
                ast.sym.locals.put(name, (Symbol.VariableSymbol) visit(childNode, null));
            }
        });

        // Parse the return type.
        ast.sym.returnType = typeFromStr(ast.returnType);

        return ast.sym;
    }

    @Override
    public Symbol.VariableSymbol varDecl(Ast.VarDecl ast, Void arg) {
        ast.sym = varSymFromString(ast.name, ast.type);
        return ast.sym;
    }


    // Utility methods.

    /**
     * Abstracts the creation of a new VariableSymbol from a name and a type string.
     */
    private Symbol.VariableSymbol varSymFromString(String name, String type) {
        Symbol.TypeSymbol typeSymbol = typeFromStr(type);
        return new Symbol.VariableSymbol(name, typeSymbol);
    }

    /**
     * Translates an AST string representing a type,
     * into the corresponding type symbol.
     */
    private Symbol.TypeSymbol typeFromStr(String typeStr) {
        // Check for array type.
        if (typeStr.contains("[]")) {
            // int[] -> new ArrayType("int"), etc..
            return new Symbol.ArrayTypeSymbol(typeFromStr(typeStr.replace("[]", "")));
        } else {
            Symbol.PrimitiveTypeSymbol primitiveTypeSym = primitiveTypeFromStr(typeStr);

            if (primitiveTypeSym != null) {
                // This is a primitive type.
                return primitiveTypeSym;
            } else {
                // This is a reference type.
                return new Symbol.ClassSymbol(typeStr);
            }
        }
    }

    /**
     * Translates an AST string representing one of the primitive
     * types like int, bool and void, into a type symbol.
     */
    private Symbol.PrimitiveTypeSymbol primitiveTypeFromStr(String typeStr) {
        Symbol.PrimitiveTypeSymbol[] primitiveTypes = {Symbol.PrimitiveTypeSymbol.intType, Symbol.PrimitiveTypeSymbol.voidType, Symbol.PrimitiveTypeSymbol.booleanType};

        for (Symbol.PrimitiveTypeSymbol sym : primitiveTypes) {
            if (sym.name.equals(typeStr)) {
                return sym;
            }
        }

        return null;
    }

}
