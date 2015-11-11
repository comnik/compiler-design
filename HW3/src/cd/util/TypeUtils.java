package cd.util;

import cd.ir.Symbol;

/**
 * Provides some utility functions for dealing with types.
 */
public class TypeUtils {

    /**
     * Translates an AST string representing a type,
     * into the corresponding type symbol.
     */
    public static Symbol.TypeSymbol typeFromStr(String typeStr) {
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
    public static Symbol.PrimitiveTypeSymbol primitiveTypeFromStr(String typeStr) {
        Symbol.PrimitiveTypeSymbol[] primitiveTypes = {Symbol.PrimitiveTypeSymbol.intType, Symbol.PrimitiveTypeSymbol.voidType, Symbol.PrimitiveTypeSymbol.booleanType};

        for (Symbol.PrimitiveTypeSymbol sym : primitiveTypes) {
            if (sym.name.equals(typeStr)) {
                return sym;
            }
        }

        return null;
    }

}
