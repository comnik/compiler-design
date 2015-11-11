package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 */
public class AstSemanticChecker extends AstVisitor<Void, Map<String,Symbol.ClassSymbol>> {

    @Override
    public Void classDecl(Ast.ClassDecl ast, Map<String,Symbol.ClassSymbol> globalSymbolTable) {

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

        // CIRCULAR_INHERITANCE
        if (hasCircularInheritance(ast.name, globalSymbolTable)){
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

        return null;
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

    private Function<Symbol.ClassSymbol,Boolean> checkClass;

    /**
     *  Goes through the inheritance tree and checks if the className
     *  occurs somewhere, thus detecting circular inheritance.
     */
    private Boolean hasCircularInheritance(String className, Map<String,Symbol.ClassSymbol> symTab) {
        Set<String> seen = new HashSet<String>();

        checkClass = (classSym) -> {
            if (seen.contains(classSym.name)) {
                return true;
            } else {
                // We have to fetch the superclass from the symbol table,
                // so that it actually contains further inheritance information.
                Symbol.ClassSymbol superCls = symTab.get(classSym.superClass.name);

                // Check if we have reached "Object".
                if (superCls.name.equals(Symbol.ClassSymbol.objectType.name)) {
                    return false;
                } else {
                    seen.add(classSym.name);
                    return checkClass.apply(superCls);
                }
            }
        };

        return checkClass.apply(symTab.get(className));
    }

}
