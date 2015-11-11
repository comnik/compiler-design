package cd.frontend.semantic;

import cd.ToDoException;
import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

import javax.lang.model.type.PrimitiveType;
import java.lang.reflect.Type;
import java.util.Map;

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
        if (conditionType != Symbol.PrimitiveTypeSymbol.booleanType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }

    @Override
    public Symbol.TypeSymbol whileLoop(Ast.WhileLoop ast, Void arg) {
        Symbol.TypeSymbol conditionType = visit(ast.condition(), null);

        // TYPE_ERROR - while(cond) requires cond to be of type boolean.
        if (conditionType != Symbol.PrimitiveTypeSymbol.booleanType) {
            throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        }

        return null;
    }

    @Override
    public Symbol.TypeSymbol assign(Ast.Assign ast, Void arg) {
        Symbol.TypeSymbol leftType = visit(ast.left(), null);
        Symbol.TypeSymbol rightType = visit(ast.right(), null);

        // TYPE_ERROR - The type of the right-hand side in an assignment
        // must be a subtype of the type of the left-hand side.
    }

    @Override
    public Symbol.TypeSymbol binaryOp(Ast.BinaryOp ast, Void arg) {
        Symbol.TypeSymbol leftType = visit(ast.left(), null);
        Symbol.TypeSymbol rightType = visit(ast.right(), null);

        // if (leftType.name.equals(rightType.name))
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
