package cd.backend.codegen;

import cd.Config;
import cd.ToDoException;
import cd.backend.codegen.StackManager.Value;
import cd.ir.Ast.*;
import cd.ir.Ast.BinaryOp.BOp;
import cd.ir.ExprVisitor;
import cd.util.debug.AstOneLine;

import java.util.*;

import static cd.Config.SCANF;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.BASE_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;

/**
 * Generates code to evaluate expressions. After emitting the code, returns a
 * Register where the result can be found.
 */
class ExprGenerator extends ExprVisitor<Value, StackManager> {
	protected final AstCodeGenerator cg;

	ExprGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public Value gen(Expr ast, StackManager stackManager) {
		return visit(ast, stackManager);
	}

	@Override
	public Value visit(Expr ast, StackManager stackManager) {
        cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));

        try {
			return super.visit(ast, stackManager);
		} finally {
			cg.emit.decreaseIndent();
		}

	}

    // Small lookup tables to make binaryOp implementation more concise.
    private static final Map<BOp,String> compToCondition;
    static {
        compToCondition = new HashMap<>();
        compToCondition.put(BOp.B_LESS_THAN, "setl");
        compToCondition.put(BOp.B_LESS_OR_EQUAL, "setle");
        compToCondition.put(BOp.B_GREATER_THAN, "setg");
        compToCondition.put(BOp.B_GREATER_OR_EQUAL, "setge");
        compToCondition.put(BOp.B_EQUAL, "sete");
        compToCondition.put(BOp.B_NOT_EQUAL, "sete");
    }

    private static final Map<BOp,String> operatorToOp;
    static {
        operatorToOp = new HashMap<>();
        operatorToOp.put(BOp.B_TIMES, "imul");
        operatorToOp.put(BOp.B_PLUS, "addl");
        operatorToOp.put(BOp.B_MINUS, "subl");
        operatorToOp.put(BOp.B_AND, "andl");
        operatorToOp.put(BOp.B_OR, "orl");
    }

    @Override
	public Value binaryOp(BinaryOp ast, StackManager stackManager) {
		// Check which path to evaluate first.
        int leftRN = cg.rnv.calc(ast.left());
		int rightRN = cg.rnv.calc(ast.right());

		Value left, right;
		if (leftRN > rightRN) {
            left = gen(ast.left(), stackManager);
            right = gen(ast.right(), stackManager);
        } else {
            right = gen(ast.right(), stackManager);
            left = gen(ast.left(), stackManager);
        }

        // Get an output register.
        Value out = left;

        stackManager.reify(right);
        stackManager.reify(left);

		cg.debug("Binary Op: %s (%s,%s)", ast, out, right);

        if (operatorToOp.containsKey(ast.operator)) {
            // B_TIMES, B_PLUS, B_MINUS, B_AND, B_OR
            cg.emit.emit(operatorToOp.get(ast.operator), right.toSrc(), out.toReg());
        } else if (compToCondition.containsKey(ast.operator)) {
            // B_LESS_OR_EQUAL, B_GREATER_THAN, B_GREATER_OR_EQUAL, B_EQUAL

            // Ensure that we have a low byte register available.
            if (!out.toReg().hasLowByteVersion()) {
                out = stackManager.getRegisterWithLowByte();
                stackManager.reify(out);
                cg.emit.emit("movl", left.toSrc(), out.toReg());
                stackManager.release(left);
            }

            cg.emit.emit("cmpl", right.toSrc(), out.toReg()); // Set flags.
            cg.emit.emit(compToCondition.get(ast.operator), out.toReg().lowByteVersion().toString());

            if (ast.operator == BOp.B_NOT_EQUAL) {
                // For not equal we will have to invert the result.
                cg.emit.emit("notl", out.toReg());
            }
        } else {
            switch (ast.operator) {
                case B_DIV:
                    // TODO
                    break;
                case B_MOD:
                    // TODO
                    break;
                default:
                    throw new RuntimeException("Unknown operator " + ast.operator);
            }
        }

		stackManager.release(right);

		return out;
	}

	@Override
	public Value booleanConst(BooleanConst ast, StackManager stackManager) {
        Value v = stackManager.getRegisterWithLowByte();
        stackManager.reify(v);

        String booleanValue = (ast.value == true) ? constant(1) : constant(0);

        cg.emit.emit("movb", booleanValue, v.toReg().lowByteVersion().toString());
        return v;
    }

	@Override
	public Value builtInRead(BuiltInRead ast, StackManager stackManager) {
		Value v = stackManager.getRegister();

        stackManager.emitCallerSave();
        stackManager.reify(v);

        cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emit("leal", AssemblyEmitter.registerOffset(8, STACK_REG), v.toReg());
		cg.emit.emitStore(v.toReg(), 4, STACK_REG);
		cg.emit.emitStore("$STR_D", 0, STACK_REG);
		cg.emit.emit("call", SCANF);
		cg.emit.emitLoad(8, STACK_REG, v.toReg());
		cg.emit.emit("add", constant(16), STACK_REG);

        return v;
	}

	@Override
	public Value cast(Cast ast, StackManager stackManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Value index(Index ast, StackManager stackManager) {
        Value arrayAddr = gen(ast.left(), stackManager);
        Value index = gen(ast.right(), stackManager);

        stackManager.reify(arrayAddr);
        stackManager.reify(index);

        cg.emit.emit("leal", AssemblyEmitter.arrayAddress(arrayAddr.toReg(),index.toReg()), arrayAddr.toReg());

        stackManager.release(index);
        return arrayAddr;
	}

	@Override
	public Value intConst(IntConst ast, StackManager stackManager) {
        Value reg = stackManager.getRegister();
        stackManager.reify(reg);
        cg.emit.emit("movl", constant(ast.value), reg.toString());
        return reg;
	}

	@Override
	public Value field(Field ast, StackManager stackManager) {
        Value objAddr = gen(ast.arg(), stackManager);
        stackManager.reify(objAddr);

        cg.emit.emit("leal", AssemblyEmitter.registerOffset(ast.sym.offset, objAddr.toReg()), objAddr.toReg());

        return objAddr;
	}

	@Override
	public Value newArray(NewArray ast, StackManager stackManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Value newObject(NewObject ast, StackManager stackManager) {
		{
            // TODO: Support for class hierarchy.

            Value ptr = stackManager.getRegister();
            int fieldSize = ast.type.getFieldSize();

            stackManager.emitCallerSave();
            cg.emit.emit("sub", constant(16), STACK_REG);

            // The desired block size in bytes is passed in EDI.
            // May need to be refactored to use StackManager?
            cg.emit.emitStore(constant(fieldSize), 0, RegisterManager.Register.EDI);

            // The resulting pointer is saved in EAX.
            cg.emit.emit("call", Config.CALLOC);
            cg.emit.emitMove(RegisterManager.Register.EAX, ptr.toSrc());
            cg.emit.emit("add", constant(16), STACK_REG);

            return ptr;
		}
	}

	@Override
	public Value nullConst(NullConst ast, StackManager stackManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Value thisRef(ThisRef ast, StackManager stackManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Value methodCall(MethodCallExpr ast, StackManager stackManager) {
        stackManager.emitCallerSave();

        // We have to push arguments in reverse order onto the stack.
        List<Expr> argsWithoutReciever = new ArrayList<Expr>();
        argsWithoutReciever.addAll(ast.argumentsWithoutReceiver());
        Collections.reverse(argsWithoutReciever);

        argsWithoutReciever.forEach(argExpr -> {
            // TODO check if pushl or pushb
            Value argValue = visit(argExpr, stackManager);
            stackManager.reify(argValue);
            cg.emit.emit("pushl", argValue.toReg());
        });

        // Call the function.
        cg.emit.emit("call", 0);

        return stackManager.getRegister(RegisterManager.RESULT_REG);
	}

	@Override
	public Value unaryOp(UnaryOp ast, StackManager stackManager) {
        Value argValue = gen(ast.arg(), stackManager);
        stackManager.reify(argValue);

        switch (ast.operator) {
        case U_PLUS:
            break;

        case U_MINUS:
            cg.emit.emit("negl", argValue.toReg());
            break;

        case U_BOOL_NOT:
            cg.emit.emit("negl", argValue.toReg());
            cg.emit.emit("incl", argValue.toReg());
            break;
        }

        return argValue;
	}
	
	@Override
	public Value var(Var ast, StackManager stackManager) {
        Value reg = stackManager.getRegister();
        stackManager.reify(reg);

        switch (ast.sym.kind) {
        case LOCAL:
        case PARAM:
            cg.emit.emitLoad(ast.sym.offset, BASE_REG, reg.toReg());
            break;
        case FIELD:
            // TODO
            break;
        default:
            throw new RuntimeException("Unknown kind " + ast.sym.kind);
        }

		return reg;
	}

}
