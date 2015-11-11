package cd.frontend.semantic;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 */
public class AstSemanticChecker extends AstVisitor<Void,Symbol> {

    Map<String, Symbol.ClassSymbol> globalSymbolTable;

    public AstSemanticChecker(Map<String, Symbol.ClassSymbol> globalSymTable) {
        this.globalSymbolTable = globalSymTable;
    }

    @Override
    public Void classDecl(Ast.ClassDecl ast, Symbol parent) {
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

        // Check members.
        ast.members().stream().forEach(node -> visit(node, globalSymbolTable.get(ast.name)));

        return null;
    }

    @Override
    public Void methodDecl(Ast.MethodDecl ast, Symbol parent) {
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) parent;

        // INVALID_OVERRIDE
        Symbol.MethodSymbol superMethod = classSymbol.superClass.getMethod(ast.sym.name);
        if (superMethod != null) {
            if (!sameSignature(ast.sym, superMethod)) {
                String errorFmt = "Method %s overrides super method, but signatures don't match.";
                throw new SemanticFailure(SemanticFailure.Cause.INVALID_OVERRIDE, errorFmt, ast.sym.name);
            }
        }

        // MISSING_RETURN
        if (!ast.sym.returnType.name.equals(Symbol.PrimitiveTypeSymbol.voidType.name)) {
            // This method should return something.
        }

        // Check method body.
        return visit(ast.body(), parent);
    }

    @Override
    public Void seq(Ast.Seq ast, Symbol parent) {
        ast.rwChildren().stream().forEach(node -> visit(node, parent));
        return null;
    }

    @Override
    public Void methodCall(Ast.MethodCall ast, Symbol parent) {
        Ast.MethodCallExpr callExpr = ast.getMethodCallExpr();
        Symbol.MethodSymbol methodSymbol = ((Symbol.ClassSymbol) parent).getMethod(callExpr.methodName);

        // NO_SUCH_METHOD
        if (methodSymbol == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_METHOD);
        }

        // WRONG_NUMBER_OF_ARGUMENTS
        if (callExpr.argumentsWithoutReceiver().size() != methodSymbol.parameters.size()) {
            String errorFmt = "Method %s called with %d parameters, but expects %d.";
            throw new SemanticFailure(SemanticFailure.Cause.WRONG_NUMBER_OF_ARGUMENTS, errorFmt, callExpr.methodName);
        }

        return null;
    }

    @Override
    public Void field(Ast.Field ast, Symbol parent) {
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) parent;
        Symbol.VariableSymbol varSymbol = classSymbol.getField(ast.fieldName);

        // NO_SUCH_FIELD
        if (varSymbol == null) {
            throw new SemanticFailure(SemanticFailure.Cause.NO_SUCH_FIELD);
        }

        return null;
    }

    @Override
    public Void assign(Ast.Assign ast, Symbol parent){
        // NOT_ASSIGNABLE
        if (ast.left().getClass().getName().equals("ThisRef")){
            throw new SemanticFailure(SemanticFailure.Cause.NOT_ASSIGNABLE);
        } else if (ast.left().getClass().getName().equals("MethodCallExpr")){
            throw new SemanticFailure((SemanticFailure.Cause.NOT_ASSIGNABLE));
        }
        return null;
    }


    // Utility methods.

    private Function<Symbol.ClassSymbol,Boolean> checkClass;

    /**
     *  Goes through the inheritance tree and checks whether the className
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
                classSym.superClass = symTab.get(classSym.superClass.name);

                // Check if we have reached "Object".
                if (classSym.superClass.name.equals(Symbol.ClassSymbol.objectType.name)) {
                    return false;
                } else {
                    seen.add(classSym.name);
                    return checkClass.apply(classSym.superClass);
                }
            }
        };

        Symbol.ClassSymbol classSym = symTab.get(className);
        return checkClass.apply(classSym);
    }

    private boolean sameSignature(Symbol.MethodSymbol m1, Symbol.MethodSymbol m2) {
        if (!m1.returnType.name.equals(m2.returnType.name) || m1.parameters.size() != m2.parameters.size()) {
            return false;
        } else {
            // Compare parameter types.
            return Pair.zip(m1.parameters, m2.parameters).stream()
                    .allMatch(paramPair -> paramPair.a.type.name.equals(paramPair.b.type.name));
        }
    }

}
