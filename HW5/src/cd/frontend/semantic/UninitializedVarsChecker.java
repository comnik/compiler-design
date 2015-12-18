package cd.frontend.semantic;

import cd.flowgraph.Block;
import cd.ir.Ast;
import cd.ir.AstVisitor;

import java.util.stream.Collectors;

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

    @Override
    public Void assign(Ast.Assign ast, Block block) {
        // Update this blocks gen set.
        block.gen.removeAll(ast.kills.stream().map(Ast.Assign::left).collect(Collectors.toList()));
        block.gen.add(ast.left());

        return visit(ast.right(), block);
    }

    @Override
    public Void var(Ast.Var ast, Block block) throws SemanticFailure {
        if (!block.out().contains(ast)) {
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
