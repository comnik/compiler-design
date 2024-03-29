package cd.backend.codegen;

import cd.Config;
import cd.backend.ExitCode;
import cd.backend.codegen.RegisterManager.Register;

import java.io.IOException;
import java.io.Writer;

import static cd.backend.codegen.RegisterManager.STACK_REG;

public class AssemblyEmitter {
	public Writer out;
	public StringBuilder indent = new StringBuilder();
	public int counter = 0;

	public AssemblyEmitter(Writer out) {
		this.out = out;
	}

	/** Creates an constant operand. */
	static String constant(int i) {
		return "$" + i;
	}

	/** Creates an constant operand with the address of a label. */
	static String labelAddress(String lbl) {
		return "$" + lbl;
	}

	/** Creates an operand relative to another operand. */
	static String registerOffset(int offset, Register reg) {
		return String.format("%d(%s)", offset, reg);
	}

	/** Creates an operand addressing an item in an array */
	static String arrayAddress(Register arrReg, Register idxReg) {
		// final int offset = Config.SIZEOF_PTR * 2; // one word each in front for vptr and length
		final int mul = Config.SIZEOF_PTR; // assume all arrays of 4-byte elem
		return String.format("(%s,%s,%d)", arrReg, idxReg, mul);
	}

	void increaseIndent(String comment) {
		indent.append("  ");
		if (comment != null)
			emitComment(comment);
	}

	void decreaseIndent() {
		indent.setLength(indent.length() - 2);
	}

	void emitCommentSection(String name) {
		int indentLen = indent.length();
		int breakLen = 68 - indentLen - name.length();
		StringBuffer sb = new StringBuffer();
		sb.append(Config.COMMENT_SEP).append(" ");
		for (int i = 0; i < indentLen; i++)
			sb.append("_");
		sb.append(name);
		for (int i = 0; i < breakLen; i++)
			sb.append("_");

		try {
			out.write(sb.toString());
			out.write("\n");
		} catch (IOException e) {
		}
	}

	void emitComment(String comment) {
		emitRaw(Config.COMMENT_SEP + " " + comment);
	}

	void emit(String op, Register src, String dest) {
		emit(op, src.repr, dest);
	}

	void emit(String op, String src, Register dest) {
		emit(op, src, dest.repr);
	}

	void emit(String op, Register src, Register dest) {
		emit(op, src.repr, dest.repr);
	}

	void emit(String op, String src, String dest) {
		emitRaw(String.format("%s %s, %s", op, src, dest));
	}

	void emit(String op, int src, Register dest) {
		emit(op, constant(src), dest);
	}

	void emit(String op, String dest) {
		emitRaw(op + " " + dest);
	}

	void emit(String op, Register reg) {
		emit(op, reg.repr);
	}

	void emit(String op, int dest) {
		emit(op, constant(dest));
	}

	void emitMove(Register src, String dest) {
		emitMove(src.repr, dest);
	}

	void emitMove(Register src, Register dest) {
		emitMove(src.repr, dest.repr);
	}

	void emitMove(String src, Register dest) {
		emitMove(src, dest.repr);
	}

	void emitMove(String src, String dest) {
		if (!src.equals(dest))
			emit("movl", src, dest);
	}

	void emitLoad(int srcOffset, Register src, Register dest) {
		emitMove(registerOffset(srcOffset, src), dest.repr);
	}

	void emitStore(Register src, int destOffset, Register dest) {
		emitStore(src.repr, destOffset, dest);
	}

	void emitStore(String src, int destOffset, Register dest) {
		emitMove(src, registerOffset(destOffset, dest));
	}

	void emitConstantData(String data) {
		emitRaw(String.format("%s %s", Config.DOT_INT, data));
	}

	String uniqueLabel() {
		String labelName = "label" + counter++;
		return labelName;
	}

	void emitLabel(String label) {
		try {
			out.write(label + ":" + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void emitRaw(String op) {
		try {
			out.write(indent.toString());
			out.write(op);
			out.write("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    /** Emits code for the specified exit code. */
    void emitExit(ExitCode eCode) {
        emit("sub", constant(16), STACK_REG);
        emitStore(constant(eCode.value), 0, STACK_REG);
        emit("call", Config.EXIT);
        emit("add", constant(16), STACK_REG);
    }

    /** Emits code that checks for a null pointer. */
    void emitCheckNull(Register reg) {
        String continueLabel = uniqueLabel();
        emit("cmpl", constant(0), reg);
        emit("jne", continueLabel);
        emitExit(ExitCode.NULL_POINTER);
        emitLabel(continueLabel);
    }

    void emitCheckBounds(StackManager stackManager, StackManager.Value arrayAddr, StackManager.Value index) {
        String continueLabel1 = uniqueLabel();
        String continueLabel2 = uniqueLabel();

        // Load array size.
        StackManager.Value arraySize = stackManager.getRegister();
        emitLoad(-4, stackManager.reify(arrayAddr), stackManager.reify(arraySize));

        // If index < 0, then exit.
        emit("cmpl", constant(0), stackManager.reify(index));
        emit("jge", continueLabel1);
        emitExit(ExitCode.INVALID_ARRAY_BOUNDS);
        emitLabel(continueLabel1);

        // If index > array size, then exit.
        emit("decl", stackManager.reify(arraySize));
        emit("cmpl", stackManager.reify(arraySize), stackManager.reify(index));
        emit("jle", continueLabel2);
        emitExit(ExitCode.INVALID_ARRAY_BOUNDS);
        emitLabel(continueLabel2);
    }
}