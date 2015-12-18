package cd.frontend.semantic;

import cd.flowgraph.Block;
import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

import java.util.Optional;

/**
 * Warns about potentially uninitialized usage of
 * local variables as operands.
 * Controlled by the -uninit flag.
 */
public class UninitializedVarsChecker extends AstVisitor<Void,Block> {

    /** More meaningful wrapper around visit(), takes the ast root and the cfg root. */
    public void check(Ast ast, Block cfg) throws SemanticFailure {
        visit(ast, cfg);
    }

    private Optional<Symbol.VariableSymbol> toVar(Ast.Expr expr) {
        try {
            return Optional.of(((Ast.Var) expr).sym);
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Void assign(Ast.Assign ast, Block block) {
        // Update this blocks gen set.
        for (Ast.Assign assign : ast.kills) {
            toVar(assign.left()).ifPresent(justVar -> block.gen.remove(justVar.name));
        }
        toVar(ast.left()).ifPresent(justVar -> block.gen.add(justVar.name));

        return visit(ast.right(), block);
    }

    @Override
    public Void var(Ast.Var ast, Block block) throws SemanticFailure {
        if (ast.sym.kind == Symbol.VariableSymbol.Kind.LOCAL && !block.out().contains(ast.name)) {
            System.out.println("Block out: " + block.out());
            System.out.println("Looking for: " + ast.name);
            throw new SemanticFailure(SemanticFailure.Cause.POSSIBLY_UNINITIALIZED);
        }
        return null;
    }

    // Block perimeters.

    @Override
    public Void ifElse(Ast.IfElse ast, Block prev) {
        // The current block, prev, ends here.
        visit(ast.then(), prev.successors.get(0));

        if (ast.otherwise() != null) {
            visit(ast.otherwise(), prev.successors.get(1));
        }

        return null;
    }

    @Override
    public Void whileLoop(Ast.WhileLoop ast, Block prev) {
        // The current block, prev, ends here.
        if (ast.body() != null) {
            visit(ast.body(), prev.successors.get(0));
        }

        return null;
    }
}
