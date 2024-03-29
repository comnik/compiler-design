package cd.flowgraph;

import cd.ir.Ast.Expr;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a basic block of execution.
 */
public class Block {

    public final Set<Block> predecessors = new HashSet<>();
    public final List<Block> successors = new ArrayList<>();

    // All expressions generated inside this block.
    public final Set<String> gen = new HashSet<>();

    // All expressions invalidated inside this block.
    public final Set<String> kill = new HashSet<>();

    public Block(Block... predecessors) {
        Stream.of(predecessors).forEach(pred -> Block.link(pred, this));
    }

    public static void link(Block pred, Block succ) {
        pred.successors.add(succ);
        succ.predecessors.add(pred);
    }

    /** Returns the set of all available expressions before this block executes. */
    public Set<String> input() {
        Set<String> union = new HashSet<>();
        predecessors.stream().forEach(pred -> union.addAll(pred.out()));
        return union;
    }

    /** Returns the set of all expressions that are valid after this block executes. */
    public Set<String> out() {
        Set<String> union = new HashSet<>(input());
        union.removeAll(kill);
        union.addAll(gen);

        return union;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        String indent = "";

        dump(sb, indent);

        return sb.toString();
    }

    public void dump(StringBuilder sb, String indent) {
        sb
                .append(indent)
                .append(this.toString())
                .append("\n");

        this.successors.forEach(succ -> succ.dump(sb, indent + "| "));
    }

    public String toString() {
        return "[Block] " + super.toString();
    }
}
