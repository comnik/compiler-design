package cd.flowgraph;

import java.util.*;

/**
 *
 */
public class Block<Type> {

    public Optional<Block> predecessor;
    public Optional<Block> successor1;
    public Optional<Block> successor2;

    public Set<Type> gen;
    public Set<Type> kill;

    // TODO
    public Set<Type> input() {
        Set<Type> union = new HashSet<Type>(gen);
        union.addAll(gen);
        return union;
    }

    public Set<Type> out() {
        // Union of in and gen.
        Set<Type> union = new HashSet<Type>(input());
        union.addAll(gen);
        // Remove kill.
        union.removeAll(kill);
        return union;
    }

    public Block(Set<Type> gen, Set<Type> kill) {
        this.gen = gen;
        this.kill = kill;
    }
}
