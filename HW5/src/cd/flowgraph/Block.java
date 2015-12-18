package cd.flowgraph;

import cd.ir.Ast.Expr;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a basic block of execution.
 */
public class Block {

    private final Set<Block> predecessors = new HashSet<>();
    public final List<Block> successors = new ArrayList<>();

    // All expressions generated inside this block.
    public final Set<Expr> gen = new HashSet<>();

    // All expressions invalidated inside this block.
    public final Set<Expr> kill = new HashSet<>();

    public Block(Block... predecessors) {
        Stream.of(predecessors).forEach(pred -> Block.link(pred, this));
    }

    public static void link(Block pred, Block succ) {
        pred.successors.add(succ);
        succ.predecessors.add(pred);
    }

    /** Returns the set of all available expressions before this block executes. */
    public Set<Expr> input() {
        Set<Expr> union = new HashSet<>();
        predecessors.stream().forEach(pred -> union.addAll(pred.out()));
        return union;
    }

    /** Returns the set of all expressions that are valid after this block executes. */
    public Set<Expr> out() {
        Set<Expr> union = new HashSet<>(input());
        union.removeAll(kill);
        union.addAll(gen);

        return union;
    }

}
