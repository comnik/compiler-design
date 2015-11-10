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

        // Add global built-in symbols.
        globalSymbols.put(Symbol.ClassSymbol.objectType.name, Symbol.ClassSymbol.objectType);

        AstEnricher astEnricher = new AstEnricher();
        AstSemanticChecker astSemanticChecker = new AstSemanticChecker();


        // Run AstEnricher over all top-level classes.
        classDecls.stream().forEach(classDecl -> {
            Symbol.ClassSymbol clsSymbol = (Symbol.ClassSymbol) astEnricher.visit(classDecl, null);

            if (globalSymbols.containsKey(clsSymbol.name)) {
                String errorFmt = "Found two classes named %s.";
                throw new SemanticFailure(SemanticFailure.Cause.DOUBLE_DECLARATION, errorFmt, clsSymbol.name);
            } else {
                globalSymbols.put(clsSymbol.name, clsSymbol);
            }
        });

        // Run global semantic checks.
        classDecls.stream().forEach(classDecl -> {
            astSemanticChecker.visit(classDecl, globalSymbols);
        });
	}

}
