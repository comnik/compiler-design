package cd.frontend.semantic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cd.Main;
import cd.ToDoException;
import cd.ir.Ast.ClassDecl;
import cd.ir.Symbol;

public class SemanticAnalyzer {
	
	public final Main main;
	
	public SemanticAnalyzer(Main main) {
		this.main = main;
	}
	
	public void check(List<ClassDecl> classDecls) throws SemanticFailure {
        Map<String,Symbol.ClassSymbol> globalSymbols = new HashMap<String,Symbol.ClassSymbol>();

        // Populate symbol table for all top-level classes.
        AstEnricher astEnricher = new AstEnricher();
        classDecls.stream().forEach(classDecl -> {
            Symbol.ClassSymbol clsSymbol = (Symbol.ClassSymbol) astEnricher.visit(classDecl, null);

            if (globalSymbols.containsKey(clsSymbol.name)) {
                String errorFmt = "Found two classes named %s.";
                throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, clsSymbol.name);
            } else {
                globalSymbols.put(clsSymbol.name, clsSymbol);
            }
        });


        // Run type checks.
        AstTypeChecker astTypeChecker = new AstTypeChecker(globalSymbols);
        classDecls.stream().forEach(classDecl -> astTypeChecker.visit(classDecl, null));

        // INVALID_START_POINT
        if (!globalSymbols.containsKey("Main")) {
            String errorFmt = "No Main Class found.";
            throw new SemanticFailure(SemanticFailure.Cause.INVALID_START_POINT, errorFmt);
        } else if (!globalSymbols.get("Main").methods.containsKey("main")) {
            String errorFmt = "No main method found.";
            throw new SemanticFailure(SemanticFailure.Cause.INVALID_START_POINT, errorFmt);
        } else if (!globalSymbols.get("Main").methods.get("main").returnType.toString()
                .equals(Symbol.PrimitiveTypeSymbol.voidType.name)) {
            String errorFmt = "main method should have signature 'void'.";
            throw new SemanticFailure(SemanticFailure.Cause.INVALID_START_POINT, errorFmt);
        } else if (!(globalSymbols.get("Main").methods.get("main").parameters.size() == 0)) {
            String errorFmt = "main method should have no parameters.";
            throw new SemanticFailure(SemanticFailure.Cause.INVALID_START_POINT, errorFmt);
        }

        // Run all other global semantic checks.
        AstSemanticChecker astSemanticChecker = new AstSemanticChecker(globalSymbols);
        classDecls.stream().forEach(classDecl -> astSemanticChecker.visit(classDecl, null));
	}

}
