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
public class UninitializedVarsChecker extends AstVisitor<Block,Block> {

    /** More meaningful wrapper around visit(), takes the ast root and the cfg root. */
    public void check(Ast ast, Block cfg) throws SemanticFailure {
        visit(ast, cfg);
    }

    private Optional<Symbol.VariableSymbol> toVar(Ast.Expr expr) {
        try {
            Ast.Var var = (Ast.Var) expr;
            if (var.sym.kind == Symbol.VariableSymbol.Kind.LOCAL) {
                return Optional.of(var.sym);
            } else {
                return Optional.empty();
            }
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Block visit(Ast ast, Block block) {
        System.out.println("Ast node " + ast + " at block " + block);
        return super.visit(ast, block);
    }

    @Override
    public Block visitChildren(Ast ast, Block block) {
        Block currentBlock = block;
        for (Ast child : ast.children())
            currentBlock = visit(child, currentBlock);
        return currentBlock;
    }

    @Override
    public Block assign(Ast.Assign ast, Block block) {
        // Update this blocks gen set.
        toVar(ast.left()).ifPresent(justVar -> block.gen.add(justVar.name));

        return visit(ast.right(), block);
    }

    @Override
    public Block var(Ast.Var ast, Block block) throws SemanticFailure {
        System.out.println("\tLooking for " + ast.name);
        System.out.println("\tBlock in: " + block.input());
        System.out.println("\tBlock gen: " + block.gen);
        System.out.println("\tBlock kill: " + block.kill);
        System.out.println("\tBlock out: " + block.out());

        if (ast.sym.kind == Symbol.VariableSymbol.Kind.LOCAL && !block.input().contains(ast.name) && !block.gen.contains(ast.name)) {
            throw new SemanticFailure(SemanticFailure.Cause.POSSIBLY_UNINITIALIZED);
        }
        return block;
    }

    // Block perimeters.

    @Override
    public Block ifElse(Ast.IfElse ast, Block prev) {
        // Check the condition.
        visit(ast.condition(), prev);

        // The current block, prev, ends here.
        Block block = prev;
        if (prev.successors.size() > 0) {
            block = visit(ast.then(), prev.successors.get(0));
        }

        if (ast.otherwise() != null && prev.successors.size() > 1) {
            System.out.println(ast.otherwise());
            block = visit(ast.otherwise(), prev.successors.get(1));
        }

        return block;
    }

    @Override
    public Block whileLoop(Ast.WhileLoop ast, Block prev) {
        // Check the condition.
        visit(ast.condition(), prev);

        // The current block, prev, ends here.
        if (ast.body() != null && prev.successors.size() > 0) {
           return visit(ast.body(), prev.successors.get(0));
        } else {
            return prev;
        }
    }
}
