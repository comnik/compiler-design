package cd.backend.codegen;

import cd.backend.codegen.RegisterManager.Register;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Provides virtual registers, handles spilling.
 */
public class VRegManager {

    public final VRegister RESULT_REG = new VRegister(Register.EAX);

    // A code generator to use.
    // Also holds the physical register manager.
    private AstCodeGenerator codeGen;

    // The stack offset at which space for virtual registers
    // starts. This is usually right below local variables.
    private int offset;

    // All virtual registers that currently are physically backed.
    private Queue<VRegister> backedRegs = new ArrayBlockingQueue<VRegister>(RegisterManager.GPR.length);

    public VRegManager(int stackBase, AstCodeGenerator codeGen) {
        this.codeGen = codeGen;
        this.offset = stackBase;
    }

    /**
     * A virtual register abstracts the spilling of
     * physical registers onto memory if not enough physical registers
     * are available for evaluation of an expression.
     */
    public class VRegister {
        // Physical register backing this virtual register, if any.
        private Register reg = null;

        // Memory offset (relative to the base pointer) of this registers value,
        // in case it was spilled.
        private int offset = -1;

        VRegister() {}

        /** Forces the creation of a physically backed vreg. */
        VRegister(Register reg) {
            this.reg = reg;
        }

        /** Returns true iff this virtual register was spilled onto memory. */
        public boolean isSpilled() {
            return (this.reg == null);
        }

        public String toString() {
            if (!isSpilled()) {
                return reg.toString();
            } else {
                return "Spilled register @ " + offset;
            }
        }
    }

    /** Frees up a physical register, by spilling it onto memory. */
    private void spill() {
        // Get the oldest, physically backed vreg.
        VRegister target = backedRegs.poll();
        if (target == null || target.isSpilled())
            throw new RuntimeException("No physically backed vregs available. This should not happen.");

        // Get an offset for its value.
        target.offset = this.offset;
        this.offset -= RegisterManager.SIZEOF_REG;

        // Spill it's value into memory.
        codeGen.emit.emitStore(target.reg, target.offset, RegisterManager.BASE_REG);

        // Release the register.
        codeGen.rm.releaseRegister(target.reg);
        target.reg = null;
    }

    // API

    /**
     * Returns an available physical register,
     * or spills a physically backed vreg to memory and returns the thereby freed physical reg.
     */
    public VRegister getRegister() {
        // Check whether we need to free a physical register first.
        if (codeGen.rm.availableRegisters() == 0)
            spill();

        VRegister vReg = new VRegister();
        vReg.reg = codeGen.rm.getRegister();
        backedRegs.add(vReg);

        return vReg;
    }

    public void releaseRegister(VRegister vReg) {
        if (!vReg.isSpilled()) {
            if (codeGen.rm.isInUse(vReg.reg))
                codeGen.rm.releaseRegister(vReg.reg);

            backedRegs.remove(vReg);
            vReg.reg = null;
        } else {
            // TODO Free stack space here.
        }
    }

    /**
     * Returns a physical register containing this virtual registers value.
     * If this virtual register was spilled, code will be generated to load it
     * into a physical one first.
     */
    public Register toPhysical(VRegister vReg) {
        if (!vReg.isSpilled()) {
            // There exists a physical backing.
            return vReg.reg;
        } else {
            if (codeGen.rm.availableRegisters() == 0)
                spill();

            vReg.reg = codeGen.rm.getRegister();

            // Load the registers value from memory.
            codeGen.emit.emitLoad(vReg.offset, RegisterManager.BASE_REG, vReg.reg);

            backedRegs.add(vReg);

            return vReg.reg;
        }
    }

}
