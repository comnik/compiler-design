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

        // NO_SUCH_TYPE
        //
        // only class types can be extended from
        if (ast.superClass == "int" || ast.superClass == "boolean") {
            String errorFmt = "Class %s extends primitive type.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.NO_SUCH_TYPE, errorFmt, ast.name);
        // extended types have to exist
        } else if (!(globalSymbolTable.containsValue(ast.superClass))) {
            String errorFmt = "Class %s extends non-existent type.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.NO_SUCH_TYPE, errorFmt, ast.name);
        }

        // CIRCULAR_INHERITANCE
        if (circularInheritanceChecker(ast, ast.name)){
            String errorFmt = "Class %s should not extend %s.";
            throw new SemanticFailure(
                    SemanticFailure.Cause.CIRCULAR_INHERITANCE, errorFmt, ast.name, ast.superClass);
        }

        return null;
    }

    @Override
    public Void methodDecl(Ast.MethodDecl ast, Map<String,Symbol.ClassSymbol> globalSymbolTable) {
        // MISSING_RETURN
        if (!ast.sym.returnType.name.equals(Symbol.PrimitiveTypeSymbol.voidType.name)) {
            // This method should return something.
        }
    }

    @Override
    public Void ifElse(Ast.IfElse ast, Map<String,Symbol.ClassSymbol> globalSymbolTable) {
        // TYPE_ERROR
        //ast.condition().
        //if (!ast.condition().type.toString().equals(Symbol.PrimitiveTypeSymbol.booleanType.name)) {
        //    throw new SemanticFailure(SemanticFailure.Cause.TYPE_ERROR);
        //}

        return null;
    }


    // Utility methods.

    /**
     *  Goes through the inheritance tree and checks if the className
     *  occurs somewhere, thus detecting circular inheritance.
     */
    private Boolean circularInheritanceChecker(Ast.ClassDecl ast, String className) {
        if (ast.superClass == "Object"){
            return false;
        } else if (ast.superClass == className) {
            return true;
        } else {
            return circularInheritanceChecker(ast.sym.superClass.ast, className);
        }
    }

}
