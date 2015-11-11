package cd.frontend.semantic;

import cd.ToDoException;
import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.PrimitiveTypeSymbol;
import cd.util.Pair;
import cd.util.TypeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ensures type safety for an AST, given a global symbol table.
 * Updates the global symbol table with type information.
 */
public class AstTypeChecker extends AstVisitor<Symbol.TypeSymbol,Symbol.TypeSymbol> {

    private Map<String,Symbol.ClassSymbol> globalSymbolTable;

    public AstTypeChecker(Map<String,Symbol.ClassSymbol> globalSymTable) {
        this.globalSymbolTable = globalSymTable;

        // Add global built-in symbols.
        globalSymbolTable.put(Symbol.ClassSymbol.objectType.name, Symbol.ClassSymbol.objectType);
    }

    @Override
    public Symbol.TypeSymbol classDecl(Ast.ClassDecl ast, Symbol.TypeSymbol enclosingType) {
        // Check wether we are re-defining the builtin Object type.
        if (ast.sym.name.equals(Symbol.ClassSymbol.objectType.name)) {
            throw new SemanticFailure(SemanticFailure.Cause.OBJECT_CLASS_DEFINED,
                    "Name clash with builtin type 'Object'.");
        }

        // Fix-up with global symbol table.
        ast.sym = globalSymbolTable.get(ast.sym.name);
        ast.sym.superClass = globalSymbolTable.get(ast.superClass);

        // Ensure superclass exists.
        if (ast.sym.superClass == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        // CIRCULAR_INHERITANCE
        if (hasCircularInheritance(ast.sym)){
            String errorFmt = "Class %s should not extend %s.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.CIRCULAR_INHERITANCE, errorFmt, ast.name, ast.superClass);
        }

        // Visit members.
        ast.members().stream().forEach(node -> visit(node, ast.sym));

        return ast.sym;
    }

    @Override
    public Symbol.TypeSymbol methodDecl(Ast.MethodDecl ast, Symbol.TypeSymbol enclosingType) {
        // Parse the return type.
        ast.sym.returnType = typeFromStr(ast.returnType);

        // Visit body.
        visit(ast.body(), ast.sym.returnType);

        return ast.sym.returnType;
    }

    @Override
    public Symbol.TypeSymbol seq(Ast.Seq ast, Symbol.TypeSymbol enclosingType) {
        ast.rwChildren().stream().forEach(node -> visit(node, enclosingType));
        return null;
    }

    @Override
    public Symbol.TypeSymbol ifElse(Ast.IfElse ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol conditionType = visit(ast.condition(), enclosingType);

        // TYPE_ERROR
        if (!conditionType.equals(PrimitiveTypeSymbol.booleanType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR,
                    "if(cond) requires cond to be of type boolean");
        }

        visit(ast.then(), enclosingType);
        visit(ast.otherwise(), enclosingType);

        return null;
    }

    @Override
    public Symbol.TypeSymbol whileLoop(Ast.WhileLoop ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol conditionType = visit(ast.condition(), enclosingType);

        // TYPE_ERROR
        if (!conditionType.equals(PrimitiveTypeSymbol.booleanType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR,
                    "while(cond) requires cond to be of type boolean");
        }

        visit(ast.body(), enclosingType);

        return null;
    }

    @Override
    public Symbol.TypeSymbol assign(Ast.Assign ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol leftType = visit(ast.left(), enclosingType);
        Symbol.TypeSymbol rightType = visit(ast.right(), enclosingType);

        if (!rightType.isSubtype(leftType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR,
                    "Assignment operands must be of compatible types.");
        }

        return leftType;
    }

    @Override
    public Symbol.TypeSymbol unaryOp(Ast.UnaryOp ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol argType = visit(ast.arg(), enclosingType);

        switch (ast.operator) {
            case U_PLUS:
            case U_MINUS:
                if (!argType.equals(PrimitiveTypeSymbol.intType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR,
                            "Unary arithmetic operators require an operand of type int.");
                }
                return PrimitiveTypeSymbol.intType;

            case U_BOOL_NOT:
                if (!argType.equals(PrimitiveTypeSymbol.booleanType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR,
                            "Unary boolean operators require an operand of type boolean.");
                }
                return PrimitiveTypeSymbol.booleanType;

            default:
                throw new RuntimeException("Unknown operator " + ast.operator.repr);
        }
    }

    @Override
    public Symbol.TypeSymbol binaryOp(Ast.BinaryOp ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol leftType = visit(ast.left(), enclosingType);
        Symbol.TypeSymbol rightType = visit(ast.right(), enclosingType);

        switch (ast.operator) {
            case B_TIMES:
            case B_DIV:
            case B_MOD:
            case B_PLUS:
            case B_MINUS:
                // require operands of type int...
                if (!leftType.equals(PrimitiveTypeSymbol.intType) || !rightType.equals(PrimitiveTypeSymbol.intType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                // ...and produce result of type int
                return PrimitiveTypeSymbol.intType;

            case B_AND:
            case B_OR:
                // require operands of type boolean...
                if (!leftType.equals(PrimitiveTypeSymbol.booleanType) ||
                        !rightType.equals(PrimitiveTypeSymbol.booleanType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                // ...and produce a result of type boolean
                return PrimitiveTypeSymbol.booleanType;

            case B_GREATER_OR_EQUAL:
            case B_GREATER_THAN:
            case B_LESS_OR_EQUAL:
            case B_LESS_THAN:
                // require operands of type int...
                if (!leftType.equals(PrimitiveTypeSymbol.intType) ||
                        !rightType.equals(PrimitiveTypeSymbol.intType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                // ...and produce a result of type boolean
                return PrimitiveTypeSymbol.booleanType;

            case B_EQUAL:
            case B_NOT_EQUAL:
                // require operands of type L and R, where one is the subtype of the other
                if (!leftType.isSubtype(rightType) && !rightType.isSubtype(leftType)) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                return PrimitiveTypeSymbol.booleanType;

            default:
                throw new RuntimeException("Unsupported operand " + ast.operator.repr);
        }
    }

    @Override
    public Symbol.TypeSymbol cast(Ast.Cast ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol argType = visit(ast.arg(), enclosingType);
        Symbol.TypeSymbol castType = typeFromStr(ast.typeName);

        if (!argType.isSubtype(castType) && !castType.isSubtype(argType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return castType;
    }

    @Override
    public Symbol.TypeSymbol index(Ast.Index ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol indexType = visit(ast.right(), enclosingType);
        Symbol.ArrayTypeSymbol arrayType = (Symbol.ArrayTypeSymbol) visit(ast.left(), enclosingType);

        if (arrayType == null || !indexType.equals(PrimitiveTypeSymbol.intType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return arrayType.elementType;
    }

    @Override
    public Symbol.TypeSymbol newArray(Ast.NewArray ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol capacityType = visit(ast.arg(), enclosingType);
        Symbol.TypeSymbol elementSymbol = typeFromStr(ast.typeName);

        if (!capacityType.equals(PrimitiveTypeSymbol.intType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return new Symbol.ArrayTypeSymbol(elementSymbol);
    }

    @Override
    public Symbol.TypeSymbol newObject(Ast.NewObject ast, Symbol.TypeSymbol enclosingType) {
        Symbol.ClassSymbol objType = globalSymbolTable.get(ast.typeName);

        if (objType == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        return objType;
    }

    @Override
    public Symbol.TypeSymbol methodCall(Ast.MethodCall ast, Symbol.TypeSymbol enclosingType) {
        Ast.MethodCallExpr methodCallExpr = ast.getMethodCallExpr();

        List<Symbol.TypeSymbol> actualArgTypes = methodCallExpr.argumentsWithoutReceiver().stream()
                .map(expr -> visit(expr, enclosingType))
                .collect(Collectors.toList());

        List<Symbol.TypeSymbol> formalArgTypes = methodCallExpr.sym.parameters.stream()
                .map(varSym -> varSym.type)
                .collect(Collectors.toList());

        boolean argTypesCorrect = Pair.zip(actualArgTypes, formalArgTypes).stream()
                .allMatch(typePair -> typePair.a.isSubtype(typePair.b));

        if (!argTypesCorrect) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return methodCallExpr.sym.returnType;
    }

    @Override
    public Symbol.TypeSymbol field(Ast.Field ast, Symbol.TypeSymbol enclosingType) {
        Symbol.ClassSymbol targetType = (Symbol.ClassSymbol) visit(ast.arg(), enclosingType);
        if (targetType == null) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return ast.sym.type;
    }

    @Override
    public Symbol.TypeSymbol returnStmt(Ast.ReturnStmt ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol returnType = visit(ast.arg(), enclosingType);

        if (!returnType.isSubtype(enclosingType)) {
            // TYPE_ERROR - In a method return statment, the expression type must be a subtype
            // of the corresponding formal return type.
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return returnType;
    }

    @Override
    public Symbol.TypeSymbol builtInWrite(Ast.BuiltInWrite ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol exprType = visit(ast.arg(), enclosingType);

        // TYPE_ERROR - write(expr) requires expr to be of type int
        if (!exprType.equals(Symbol.PrimitiveTypeSymbol.intType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }


    // Base Cases

    @Override
    public Symbol.TypeSymbol var(Ast.Var ast, Symbol.TypeSymbol enclosingType) {
        Symbol.TypeSymbol varType = ast.type;
        Symbol.ClassSymbol cls = (Symbol.ClassSymbol) ast.type;

        if (cls != null) {
            // Fix-up with global symbol table.
            varType = globalSymbolTable.get(cls.name);
        }

        if (varType == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        ast.setSymbol(new Symbol.VariableSymbol(ast.name, cls));
        return ast.sym.type;
    }

    @Override
    public Symbol.TypeSymbol builtInRead(Ast.BuiltInRead ast, Symbol.TypeSymbol enclosingType) {
        return Symbol.PrimitiveTypeSymbol.intType;
    }

    @Override
    public Symbol.TypeSymbol booleanConst(Ast.BooleanConst ast, Symbol.TypeSymbol enclosingType) {
        return Symbol.PrimitiveTypeSymbol.booleanType;
    }

    @Override
    public Symbol.TypeSymbol intConst(Ast.IntConst ast, Symbol.TypeSymbol enclosingType) {
        return Symbol.PrimitiveTypeSymbol.intType;
    }

    @Override
    public Symbol.TypeSymbol thisRef(Ast.ThisRef ast, Symbol.TypeSymbol enclosingType) {
        return enclosingType;
    }

    @Override
    public Symbol.TypeSymbol nullConst(Ast.NullConst ast, Symbol.TypeSymbol enclosingType) {
        return Symbol.ClassSymbol.nullType;
    }


    // Utility Methods

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
            Symbol.PrimitiveTypeSymbol primitiveTypeSym = TypeUtils.primitiveTypeFromStr(typeStr);

            if (primitiveTypeSym != null) {
                // This is a primitive type.
                return primitiveTypeSym;
            } else {
                // This is a reference type.
                Symbol.ClassSymbol type = globalSymbolTable.get(typeStr);
                if (type == null) {
                    throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
                }

                return type;
            }
        }
    }

    private Function<Symbol.ClassSymbol,Boolean> checkClass;

    /**
     *  Goes through the inheritance tree and checks whether the className
     *  occurs somewhere, thus detecting circular inheritance.
     */
    private Boolean hasCircularInheritance(Symbol.ClassSymbol cls) {
        Set<String> seen = new HashSet<String>();

        checkClass = (classSym) -> {
            if (seen.contains(classSym.name)) {
                return true;
            } else {
                // Check if we have reached "Object".
                if (classSym.superClass.name.equals(Symbol.ClassSymbol.objectType.name)) {
                    return false;
                } else {
                    seen.add(classSym.name);
                    return checkClass.apply(classSym.superClass);
                }
            }
        };

        return checkClass.apply(cls);
    }

}
