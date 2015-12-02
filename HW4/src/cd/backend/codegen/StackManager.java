package cd.backend.codegen;

import cd.backend.codegen.RegisterManager.Register;
import cd.backend.codegen.RegisterManager.ByteRegister;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

        // Physical register holding the value at this offset (if any).
        protected Register reg = null;

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

        public String toOffset() {
            if (!onStack())
                throw new RuntimeException("Value not on stack.");

            return AssemblyEmitter.registerOffset(this.offset, RegisterManager.BASE_REG);
        }

        public String toSrc() {
            return isDetached() ? this.toOffset() : reg.toString();
        }

        public Register toReg() {
            if (this.isDetached()) {
                throw new RuntimeException(
                        "This virtual register has no physical register associated with it." +
                                "Have you forgotten to reify it first?");
            }

            return this.reg;
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

    public void release(Value value) {
        if (!value.isDetached())
            detach(value);

        if (value.onStack())
            offsetPool.add(value.offset);
    }

    /**
     * Readies the value to be used in code generation.
     * If it is not represented in a physical register, code will be generated to load it first.
     */
    public void reify(Value v) {
        // If there is no physical register available, cycle one in.
        if (v.isDetached()) {
            attach(v, getPhysical());

            // We only load the value if it was detached from the register.
            if (v.onStack())
                load(v, v.reg);
        }
    }

    /** Emits code to persist all caller saved registers in use. */
    public void emitCallerSave() {
        codeGen.emit.emitComment("Persisting CALLER_SAVE registers...");
        for (Register reg : RegisterManager.CALLER_SAVE) {
            Value value = registerMap.get(reg);
            if (value != null) {
                writeback(value);
                // markDirty(value);
            }
        }
    }


    // Util

    /** Allocates a place on the stack, to store the registers contents. */
    private void allocate(Value v) {
        if (!v.onStack()) {
            Integer reusableOffset = offsetPool.poll();
            if (reusableOffset != null) {
                // We can reuse an existing offset.
                v.offset = reusableOffset;
            } else {
                v.offset = this.stackTop;
                this.stackTop -= RegisterManager.SIZEOF_REG;
            }
        }
    }

    /** Associates a stack offset with a physical register to hold its value. Will not load the value! */
    private void attach(Value value, Register reg) {
        codeGen.emit.emitComment("Attaching " + value + " to " + reg);
        value.reg = reg;
        registerMap.put(value.reg, value);
        regsInUse.add(value.reg);
    }

    /** Disassociates a stack offset from the register holding its value. */
    private void detach(Value value) {
        codeGen.emit.emitComment("Detaching " + value);
        if (codeGen.rm.isInUse(value.reg))
            codeGen.rm.releaseRegister(value.reg);

        registerMap.remove(value.reg);
        regsInUse.remove(value.reg);
        value.reg = null;
    }

    /** Writes a virtual registers value onto the stack. */
    private void writeback(Value value) {
        // If this vreg is not on the stack yet, get a new offset.
        if (!value.onStack())
            allocate(value);

        codeGen.emit.emitComment("Writing back " + value + " onto the stack.");
        codeGen.emit.emitStore(value.reg, value.offset, RegisterManager.BASE_REG);
    }

    /** Frees up a physical register, by spilling it onto memory. */
    private void spill(Register reg) {
        Value target = registerMap.get(reg);
        if (target == null)
            throw new RuntimeException("Tried to spill 'null'.");

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
            codeGen.emit.emitLoad(value.offset, RegisterManager.BASE_REG, reg);
        } else {
            throw new RuntimeException("Can't 'load' a stack offset that is not on the stack.");
        }
    }
}
