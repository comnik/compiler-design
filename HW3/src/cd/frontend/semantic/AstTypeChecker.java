package cd.frontend.semantic;

import cd.ToDoException;
import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.PrimitiveTypeSymbol;
import cd.util.Pair;
import cd.util.TypeUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ensures type safety for an AST, given a global symbol table.
 * Updates the global symbol table with type information.
 */
public class AstTypeChecker extends AstVisitor<Symbol.TypeSymbol,Void> {

    private Map<String,Symbol.ClassSymbol> globalSymbolTable;

    public AstTypeChecker(Map<String,Symbol.ClassSymbol> globalSymTable) {
        this.globalSymbolTable = globalSymTable;
    }

    @Override
    public Symbol.TypeSymbol classDecl(Ast.ClassDecl ast, Void arg) {
        // Check wether we are re-defining the builtin Object type.
        if (ast.sym.name.equals(Symbol.ClassSymbol.objectType.name)) {
            String errorFmt = "Name clash with builtin type 'Object'.";
            throw new SemanticFailure(SemanticFailure.Cause.OBJECT_CLASS_DEFINED, errorFmt);
        }

        // Link with superclass symbol.
        ast.sym.superClass = globalSymbolTable.get(ast.superClass);
        if (ast.sym.superClass == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        // Visit members.
        ast.members().stream().forEach(node -> visit(node, null));

        return ast.sym;
    }

    @Override
    public Symbol.TypeSymbol methodDecl(Ast.MethodDecl ast, Void arg) {
        // Visit body.
        ast.body().rwChildren().stream().forEach(node -> visit(node, null));
        return ast.sym.returnType;
    }

    @Override
    public Symbol.TypeSymbol ifElse(Ast.IfElse ast, Void arg) {
        Symbol.TypeSymbol conditionType = visit(ast.condition(), null);

        // TYPE_ERROR - if(cond) requires cond to be of type boolean.
        if (conditionType != PrimitiveTypeSymbol.booleanType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }

    @Override
    public Symbol.TypeSymbol whileLoop(Ast.WhileLoop ast, Void arg) {
        Symbol.TypeSymbol conditionType = visit(ast.condition(), null);

        // TYPE_ERROR - while(cond) requires cond to be of type boolean.
        if (conditionType != PrimitiveTypeSymbol.booleanType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }

    @Override
    public Symbol.TypeSymbol assign(Ast.Assign ast, Void arg) {
        Symbol.TypeSymbol leftType = visit(ast.left(), null);
        Symbol.TypeSymbol rightType = visit(ast.right(), null);

        if (!rightType.isSubtype(leftType)) {
            // TYPE_ERROR - The type of the right-hand side in an assignment
            // must be a subtype of the type of the left-hand side.
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return leftType;
    }

    @Override
    public Symbol.TypeSymbol binaryOp(Ast.BinaryOp ast, Void arg) {
        Symbol.TypeSymbol leftType = visit(ast.left(), null);
        Symbol.TypeSymbol rightType = visit(ast.right(), null);

        switch (ast.operator) {
            case B_TIMES:
            case B_DIV:
            case B_MOD:
            case B_PLUS:
            case B_MINUS:
                // require operands of type int...
                if (leftType != PrimitiveTypeSymbol.intType || rightType != PrimitiveTypeSymbol.intType) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                // ...and produce result of type int
                return PrimitiveTypeSymbol.intType;

            case B_AND:
            case B_OR:
                // require operands of type boolean...
                if (leftType != PrimitiveTypeSymbol.booleanType || rightType != PrimitiveTypeSymbol.booleanType) {
                    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
                }
                // ...and produce a result of type boolean
                return PrimitiveTypeSymbol.booleanType;

            case B_GREATER_OR_EQUAL:
            case B_GREATER_THAN:
            case B_LESS_OR_EQUAL:
            case B_LESS_THAN:
                // require operands of type int...
                if (leftType != PrimitiveTypeSymbol.intType || rightType != PrimitiveTypeSymbol.intType) {
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
        }

        return null;
    }

    @Override
    public Symbol.TypeSymbol cast(Ast.Cast ast, Void arg) {
        Symbol.TypeSymbol argType = visit(ast.arg(), null);
        Symbol.TypeSymbol castType = TypeUtils.typeFromStr(ast.typeName);

        if (!argType.isSubtype(castType) && !castType.isSubtype(argType)) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return castType;
    }

    @Override
    public Symbol.TypeSymbol index(Ast.Index ast, Void arg) {
        Symbol.TypeSymbol indexType = visit(ast.right(), null);
        Symbol.ArrayTypeSymbol arrayType = (Symbol.ArrayTypeSymbol) visit(ast.left(), null);

        if (arrayType == null || indexType != PrimitiveTypeSymbol.intType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return arrayType.elementType;
    }

    @Override
    public Symbol.TypeSymbol newArray(Ast.NewArray ast, Void arg) {
        Symbol.TypeSymbol capacityType = visit(ast.arg(), null);
        Symbol.TypeSymbol elementSymbol = TypeUtils.typeFromStr(ast.typeName);

        if (capacityType != PrimitiveTypeSymbol.intType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return new Symbol.ArrayTypeSymbol(elementSymbol);
    }

    @Override
    public Symbol.TypeSymbol newObject(Ast.NewObject ast, Void arg) {
        Symbol.ClassSymbol objType = globalSymbolTable.get(ast.typeName);

        if (objType == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_TYPE);
        }

        return objType;
    }

    @Override
    public Symbol.TypeSymbol methodCall(Ast.MethodCall ast, Void arg) {
        Ast.MethodCallExpr methodCallExpr = ast.getMethodCallExpr();

        List<Symbol.TypeSymbol> actualArgTypes = methodCallExpr.argumentsWithoutReceiver().stream()
                .map(expr -> visit(expr, null))
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
    public Symbol.TypeSymbol field(Ast.Field ast, Void arg) {
        Symbol.ClassSymbol targetType = (Symbol.ClassSymbol) visit(ast.arg(), null);
        if (targetType == null) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return ast.sym.type;
    }

    @Override
    public Symbol.TypeSymbol returnStmt(Ast.ReturnStmt ast, Void arg) {
        Symbol.TypeSymbol returnType = visit(ast.arg(), null);
        // TODO Check for correct return type
        return returnType;
    }

    @Override
    public Symbol.TypeSymbol builtInWrite(Ast.BuiltInWrite ast, Void arg) {
        Symbol.TypeSymbol exprType = visit(ast.arg(), null);

        // TYPE_ERROR - write(expr) requires expr to be of type int
        if (exprType != Symbol.PrimitiveTypeSymbol.intType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }


    // Base Cases

    @Override
    public Symbol.TypeSymbol builtInRead(Ast.BuiltInRead ast, Void arg) {
        return Symbol.PrimitiveTypeSymbol.intType;
    }

    @Override
    public Symbol.TypeSymbol booleanConst(Ast.BooleanConst ast, Void arg) {
        return Symbol.PrimitiveTypeSymbol.booleanType;
    }

    @Override
    public Symbol.TypeSymbol intConst(Ast.IntConst ast, Void arg) {
        return Symbol.PrimitiveTypeSymbol.intType;
    }

    @Override
    public Symbol.TypeSymbol thisRef(Ast.ThisRef ast, Void arg) {
        throw new ToDoException("thisRef not implemented yet");
    }

    @Override
    public Symbol.TypeSymbol nullConst(Ast.NullConst ast, Void arg) {
        return Symbol.ClassSymbol.nullType;
    }

}
