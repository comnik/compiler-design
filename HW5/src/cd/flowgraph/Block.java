package cd.flowgraph;

import cd.ir.Ast.Expr;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a basic block of execution.
 */
public class Block {

    private final Set<Block> predecessors = new HashSet<>();
    private final Set<Block> successors = new HashSet<>();

    // All expressions generated inside this block.
    private final Set<Expr> gen = new HashSet<>();

    // All expressions invalidated inside this block.
    private final Set<Expr> kill = new HashSet<>();

    public Block(Block... predecessors) {
        Stream.of(predecessors).forEach(pred -> Block.link(pred, this));
    }

    public static void link(Block pred, Block succ) {
        pred.successors.add(succ);
        succ.predecessors.add(pred);
    }

    // TODO
    public Set<Expr> input() {
        Set<Expr> union = new HashSet<>(gen);
        union.addAll(gen);
        return union;
    }

    /** Returns the set of all expressions that are valid after this block executes. */
    public Set<Expr> out() {
        // Union of in and gen.
        Set<Expr> union = new HashSet<>(input());
        union.addAll(gen);
        // Remove kill.
        union.removeAll(kill);
        return union;
    }

}
