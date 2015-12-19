package cd.backend.codegen;

import cd.backend.codegen.RegisterManager.Register;
import cd.backend.codegen.RegisterManager.ByteRegister;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides virtual registers, handles spilling.
 */
public class StackManager {

    private static final int NO_OFFSET = -1;

    // A code generator to use.
    // Also holds the physical register manager.
    private AstCodeGenerator codeGen;

    // The stack offset at which space for virtual registers
    // starts. This is usually right below local variables.
    private int stackTop;

    // Mapping from physical registers to stack offsets.
    private Map<Register,Value> registerMap = new HashMap<>();

    // All physical registers in use.
    private Queue<Register> regsInUse = new ArrayBlockingQueue<>(RegisterManager.GPR.length);

    // A pool of free offsets to reuse.
    private Queue<Integer> offsetPool = new LinkedBlockingQueue<>();

    public StackManager(int stackTop, AstCodeGenerator codeGen) {
        this.codeGen = codeGen;
        this.stackTop = stackTop;
    }

    /**
     * A value represents an evaluated expression, that might reside
     * in a register or somewhere on the stack. It abstracts the spilling of
     * physical registers onto memory if not enough physical registers
     * are available for evaluation of an expression.
     */
    public class Value {
        // Memory offset (relative to the base pointer) of this registers value,
        // in case it was spilled.
        protected int offset = NO_OFFSET;

        protected int src = NO_OFFSET;
        protected Value base = null;
        protected Value idx = null;

        // Physical register holding the value at this offset (if any).
        protected Register reg = null;

        private boolean callerSaved = false;

        /** Returns true iff this virtual register was spilled onto memory. */
        public boolean isDetached() {
            return (this.reg == null);
        }

        /** Returns true iff this virtual register is stored on the stack. */
        public boolean onStack() { return (this.offset != NO_OFFSET); }

        public String toString() {
            if (onStack()) {
                return AssemblyEmitter.registerOffset(this.offset, RegisterManager.BASE_REG);
            } else if (isDetached()) {
                return "[unbound value]";
            } else {
                return reg.toString();
            }
        }
    }


    // API

    /** Returns the size of the stack, as known to the vreg manager. */
    public int getStackSize() {
        System.out.println("Stack size is: " + (-this.stackTop));
        System.out.println("Backed regs: " + this.regsInUse);
        System.out.println("Phys regs available: " + codeGen.rm.availableRegisters());
        return -this.stackTop;
    }

    /** Returns a four-byte value that behaves like a register. */
    public Value getRegister(Register reg) {
        Value v = new Value();

        if (registerMap.containsKey(reg)) {
            // The register is in use.
            spill(reg);
        }

        attach(v, reg);
        return v;
    }

    public Value getRegister() { return getRegister(getPhysical()); }

    public Value getRegisterWithLowByte() {
        Register byteReg = codeGen.rm.findRegisterWithLowByte();
        return getRegister(byteReg);
    }

    public void release(Value... values) {
        Stream.of(values).forEach(value -> {
            // codeGen.emit.emitComment("Releasing " + value.reg);
            if (!value.isDetached()) {
                detach(value);
            }

            if (value.onStack()) {
                // Return the now freed offset back to the offset pool, to be used by the next value to be allocated.
                offsetPool.add(value.offset);
            }
        });
    }

    /**
     * Readies the value to be used in code generation.
     * If it is not represented in a physical register, code will be generated to load it first.
     */
    public Register reify(Value v) {
        // If there is no physical register available, cycle one in.
        if (v.isDetached()) {
            attach(v, getPhysical());
            load(v, v.reg);
        }

        return v.reg;
    }

    public String toOffset(Value v) {
        if (v.base == null) {
            return AssemblyEmitter.registerOffset(v.src, RegisterManager.BASE_REG);
        } else if (v.idx == null) {
            return AssemblyEmitter.registerOffset(v.src, reify(v.base));
        } else {
            return AssemblyEmitter.arrayAddress(reify(v.base), reify(v.idx));
        }
    }

    public int emitCallerSave() {
        // codeGen.emit.emitComment("Regs in use " + registerMap.keySet());
        // codeGen.emit.emitComment("Persisting CALLER_SAVE registers...");
        return saveRegisters(RegisterManager.CALLER_SAVE);
    }

    public void emitCallerRestore(int padding) {
        // codeGen.emit.emitComment("Restoring CALLER_SAVE registers...");
        restoreRegisters(RegisterManager.CALLER_SAVE, padding);
    }

    public int emitCalleeSave() {
        // codeGen.emit.emitComment("Persisting CALLEE_SAVE registers...");
        return saveRegisters(RegisterManager.CALLEE_SAVE);
    }

    public void emitCalleeRestore(int padding) {
        // codeGen.emit.emitComment("Restoring CALLEE_SAVE registers...");
        restoreRegisters(RegisterManager.CALLEE_SAVE, padding);
    }


    // Persisting and restoring registers.

    /**
     * Emits code to persist each register in the given array, provided it is in use.
     * Returns the number of bytes added for alignment, such that the stack can be
     * completely freed up on restore.
     */
    private int saveRegisters(Register[] regsToSave) {
        List<Register> inUse = Stream
                .of(regsToSave)
                .filter(registerMap::containsKey)
                .collect(Collectors.toList());

        int stackSpaceNeeded = inUse.size() * RegisterManager.SIZEOF_REG;
        int stackSpaceWithPadding = alignedTo16(stackSpaceNeeded);
        codeGen.emit.emit("subl", AssemblyEmitter.constant(stackSpaceWithPadding), RegisterManager.STACK_REG);

        inUse.stream().reduce(0, (nextOffset, reg) -> {
            registerMap.get(reg).callerSaved = true;
            codeGen.emit.emit("movl", reg, AssemblyEmitter.registerOffset(nextOffset, RegisterManager.STACK_REG));

            return nextOffset + RegisterManager.SIZEOF_REG;
        }, (o1, o2) -> o1);

        // Calculate the number of alignment bytes.
        return stackSpaceWithPadding - stackSpaceNeeded;
    }

    /** Complement to saveRegisters() */
    private void restoreRegisters(Register[] regsToRestore, int padding) {
        for (int i = regsToRestore.length - 1; i >= 0; i--) {
            Register reg = regsToRestore[i];
            Value v = registerMap.get(reg);
            if (v != null && v.callerSaved) {
                v.callerSaved = false;
                codeGen.emit.emit("popl", reg);
            }
        }

        if (padding > 0) {
            codeGen.emit.emit("addl", AssemblyEmitter.constant(padding), RegisterManager.STACK_REG);
        }
    }


    // Values <-> Registers, Register spilling.

    /** Allocates a place on the stack, to store the registers contents. */
    private void allocate(Value v) {
        if (!v.onStack()) {
            /*Integer reusableOffset = offsetPool.poll();
            if (reusableOffset != null) {
                // We can reuse an existing offset.
                v.offset = reusableOffset;
            } else {*/
            v.offset = this.stackTop;
            this.stackTop -= RegisterManager.SIZEOF_REG;
            //}
        }
    }

    /** Associates a stack offset with a physical register to hold its value. Will not load the value! */
    private void attach(Value value, Register reg) {
        value.reg = reg;

        // Mark the register as used.
        codeGen.rm.markUsed(value.reg);
        registerMap.put(value.reg, value);

        // Update the queue.
        regsInUse.remove(value.reg);
        regsInUse.add(value.reg);
    }

    /** Disassociates a stack offset from the register holding its value. */
    private void detach(Value value) {
        if (codeGen.rm.isInUse(value.reg))
            codeGen.rm.releaseRegister(value.reg);

        registerMap.remove(value.reg);
        regsInUse.remove(value.reg);
        value.reg = null;
    }

    /** Writes a virtual registers value onto the stack. */
    private void writeback(Value value) {
        // If this vreg is not on the stack yet, get a new offset.
        if (!value.onStack()) {
            allocate(value);
        }

        // codeGen.emit.emitComment("Writing back " + value.reg + " onto the stack.");
        codeGen.emit.emitStore(value.reg, value.offset, RegisterManager.BASE_REG);
    }

    /** Frees up a physical register, by spilling it onto memory. */
    private void spill(Register reg) {
        Value target = registerMap.get(reg);
        if (target == null) {
            System.out.println("Tried to spill 'null'.");
            return;
        }

        writeback(target);

        // Release the register.
        release(target);
    }

    /** Helper function for spill() that simply spills the oldest physical register in use. */
    private void spillOldest() { spill(regsInUse.poll()); }

    /** Returns a free physical register, spilling the oldest one if none are available. */
    private Register getPhysical() {
        // Check whether we need to free a physical register first.
        if (codeGen.rm.availableRegisters() == 0)
            spillOldest();

        return codeGen.rm.getRegister();
    }

    /** Emits assembly to load the value of the stack offset into reg. */
    private void load(Value value, Register reg) {
        if (value.onStack()) {
            codeGen.emit.emitComment("Restoring " + reg + " from " + value);
            codeGen.emit.emitLoad(value.offset, RegisterManager.BASE_REG, reg);
        } else {
            codeGen.emit.emitComment("Can't 'load' a stack offset that is not on the stack. Ignoring.");
        }
    }


    // Util

    /** Returns the next highest multiple y of 16, such that x < y. */
    public static int alignedTo16(int x) {
        int y = 16;
        while (x > y) {
            y += 16;
        }
        return y;
    }
}
