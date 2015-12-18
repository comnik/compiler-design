package cd.flowgraph;

import cd.ir.Ast;
import cd.ir.AstVisitor;

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
    public Block methodDecl(Ast.MethodDecl ast, Block block) {
        return visit(ast.body(), block);
    }

    @Override
    public Block assign(Ast.Assign ast, Block block) {
        // Update the blocks kill-set.
        block.kill.addAll(ast.kills.stream().map(Ast.Assign::left).collect(Collectors.toList()));
        return block;
    }

    // Block perimeters.

    @Override
    public Block ifElse(Ast.IfElse ast, Block prev) {
        // The current block, prev, ends here.
        Block ifElseBlock = new Block();

        if (ast.then() != null) {
            Block.link(visit(ast.then(), new Block(prev)), ifElseBlock);
        }

        if (ast.otherwise() != null) {
            Block.link(visit(ast.otherwise(), new Block(prev)), ifElseBlock);
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
