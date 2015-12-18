package cd.flowgraph;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Goes through the AST and creates the flow-graph.
 * Returns the START block. The current block is passed as the second argument.
 */
public class GraphCreatorVisitor extends AstVisitor<Block,Block> {

    /** Wraps the visit function to provide a more meaningful entry-point. */
    public Block createGraph(Ast ast) {
        Block startBlock = new Block();

        // This returns the exit block.
        visit(ast, startBlock);

        return startBlock;
    }

    @Override
    public Block visitChildren(Ast ast, Block block) {
        Block lastValue = block;
        for (Ast child : ast.children())
            lastValue = visit(child, block);
        return lastValue;
    }

    @Override
    public Block methodDecl(Ast.MethodDecl ast, Block block) {
        return visit(ast.body(), block);
    }

    @Override
    public Block assign(Ast.Assign ast, Block block) {
        // Update the blocks kill-set.
        for (Ast.Assign assign : ast.kills) {
            toVar(assign.left()).ifPresent(justVar -> block.kill.add(justVar.name));
        }
        return block;
    }

    private Optional<Symbol.VariableSymbol> toVar(Ast.Expr expr) {
        try {
            return Optional.of(((Ast.Var) expr).sym);
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    // Block perimeters.

    @Override
    public Block ifElse(Ast.IfElse ast, Block prev) {
        // The current block, prev, ends here.
        Block ifElseBlock = new Block();

        if (ast.then() != null) {
            Block thenBlock = visit(ast.then(), new Block(prev));
            Block.link(thenBlock, ifElseBlock);
        }

        if (ast.otherwise() != null) {
            Block otherwiseBlock = visit(ast.otherwise(), new Block(prev));
            Block.link(otherwiseBlock, ifElseBlock);
        }

        return ifElseBlock;
    }

    @Override
    public Block whileLoop(Ast.WhileLoop ast, Block prev) {
        // The current block, prev, ends here.
        Block whileBlock = new Block();

        if (ast.body() != null) {
            Block.link(visit(ast.body(), new Block(prev)), whileBlock);
        }

        return whileBlock;
    }

}
