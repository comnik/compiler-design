package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

/**
 *
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

        return clsSymbol;
    }

    @Override
    public Symbol.MethodSymbol methodDecl(Ast.MethodDecl ast, Void arg) {
        Symbol.MethodSymbol methodSymbol = new Symbol.MethodSymbol(ast);
        return methodSymbol;
    }

    @Override
    public Symbol.VariableSymbol varDecl(Ast.VarDecl ast, Void arg) {
        Symbol.TypeSymbol typeSymbol = typeFromStr(ast.type);
        return new Symbol.VariableSymbol(ast.name, typeSymbol);
    }

    // Utility methods.

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
