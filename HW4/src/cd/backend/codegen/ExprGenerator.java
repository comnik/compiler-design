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
import static cd.backend.codegen.AssemblyEmitter.labelAddress;
import static cd.backend.codegen.AssemblyEmitter.registerOffset;
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

		cg.debug("Binary Op: %s (%s,%s)", ast, out, right);

        if (operatorToOp.containsKey(ast.operator)) {
            // B_TIMES, B_PLUS, B_MINUS, B_AND, B_OR
            cg.emit.emit(operatorToOp.get(ast.operator), stackManager.reify(right), stackManager.reify(out));
        } else if (compToCondition.containsKey(ast.operator)) {
            // B_LESS_OR_EQUAL, B_GREATER_THAN, B_GREATER_OR_EQUAL, B_EQUAL

            // Ensure that we have a low byte register available.
            if (!stackManager.reify(out).hasLowByteVersion()) {
                out = stackManager.getRegisterWithLowByte();
                cg.emit.emit("movl", stackManager.reify(left), stackManager.reify(out));
                stackManager.release(left);
            }

            cg.emit.emit("cmpl", stackManager.reify(right), stackManager.reify(out)); // Set flags.
            cg.emit.emit(compToCondition.get(ast.operator), stackManager.reify(out).lowByteVersion().toString());

            if (ast.operator == BOp.B_NOT_EQUAL) {
                // For not equal we will have to invert the result.
                cg.emit.emit("notl", stackManager.reify(out));
            }
        } else {
            Value eax = stackManager.getRegister(RegisterManager.Register.EAX);
            Value ebx = stackManager.getRegister(RegisterManager.Register.EBX);
            Value edx = stackManager.getRegister(RegisterManager.Register.EDX);

            switch (ast.operator) {
                case B_DIV:
                    // Operand is stored in eax.
                    cg.emit.emit("movl", stackManager.reify(left), stackManager.reify(eax));

                    // Number to be divided by is stored in ebx.
                    cg.emit.emit("movl", stackManager.reify(right), stackManager.reify(ebx));

                    // Result is stored in eax.
                    cg.emit.emitRaw("cdq");
                    cg.emit.emit("idiv", stackManager.reify(ebx));

                    out = eax;
                    break;
                case B_MOD:
                    // Operand is stored in eax.
                    cg.emit.emit("movl", stackManager.reify(right), stackManager.reify(eax));

                    // Number to be divided by is stored in ebx.
                    cg.emit.emit("movl", stackManager.reify(left), stackManager.reify(ebx));

                    // Remainder is stored in edx.
                    cg.emit.emitRaw("cdq");
                    cg.emit.emit("idiv", stackManager.reify(ebx));

                    out = edx;
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

        String booleanValue = (ast.value == true) ? constant(1) : constant(0);

        cg.emit.emit("movb", booleanValue, stackManager.reify(v).lowByteVersion().toString());
        return v;
    }

	@Override
	public Value builtInRead(BuiltInRead ast, StackManager stackManager) {
		Value v = stackManager.getRegister();

        stackManager.emitCallerSave();

        cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emit("leal", AssemblyEmitter.registerOffset(8, STACK_REG), stackManager.reify(v));
		cg.emit.emitStore(stackManager.reify(v), 4, STACK_REG);
		cg.emit.emitStore("$STR_D", 0, STACK_REG);
		cg.emit.emit("call", SCANF);
		cg.emit.emitLoad(8, STACK_REG, stackManager.reify(v));
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

        String addr = AssemblyEmitter.arrayAddress(stackManager.reify(arrayAddr), stackManager.reify(index));
        cg.emit.emit("leal", addr, stackManager.reify(arrayAddr));

        stackManager.release(index);
        return arrayAddr;
	}

	@Override
	public Value intConst(IntConst ast, StackManager stackManager) {
        Value reg = stackManager.getRegister();
        cg.emit.emit("movl", constant(ast.value), stackManager.reify(reg));
        return reg;
	}

	@Override
	public Value field(Field ast, StackManager stackManager) {
        Value objAddr = gen(ast.arg(), stackManager);

        cg.emit.emitLoad(ast.sym.offset, stackManager.reify(objAddr), stackManager.reify(objAddr));

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
        // Reserve space for the vtable pointer.
        int objSize = Config.SIZEOF_PTR + ast.type.getFieldSize();

        stackManager.emitCallerSave();

        // Pass calloc arguments size and num.
        cg.emit.emit("subl", constant(16), STACK_REG);
        cg.emit.emitStore(constant(objSize), 4, STACK_REG);
        cg.emit.emitStore(constant(1), 0, STACK_REG);
        cg.emit.emit("call", Config.CALLOC);
        cg.emit.emit("addl", constant(16), STACK_REG);

        // The resulting pointer is saved in EAX.
        Value eax = stackManager.getRegister(RegisterManager.Register.EAX);

        // Set the vtable pointer.
        cg.emit.emitStore(labelAddress(ast.type.getVtableLabel()), 0, stackManager.reify(eax));

        return eax;
	}

	@Override
	public Value nullConst(NullConst ast, StackManager stackManager) {
        Value v = stackManager.getRegister();
        cg.emit.emitMove(constant(0), stackManager.reify(v));
        return v;
	}

	@Override
	public Value thisRef(ThisRef ast, StackManager stackManager) {
        Value v = stackManager.getRegister();
        // Reciever is always at 8(%ebp).
        cg.emit.emitLoad(8, BASE_REG, stackManager.reify(v));
        return v;
	}

	@Override
	public Value methodCall(MethodCallExpr ast, StackManager stackManager) {
        stackManager.emitCallerSave();

        Value receiver = gen(ast.receiver(), stackManager);

        // We have to push arguments in reverse order onto the stack.
        List<Expr> argsWithoutReceiver = new ArrayList<Expr>();
        argsWithoutReceiver.addAll(ast.argumentsWithoutReceiver());
        Collections.reverse(argsWithoutReceiver);

        argsWithoutReceiver.forEach(argExpr -> {
            Value argValue = visit(argExpr, stackManager);
            cg.emit.emit("pushl", stackManager.reify(argValue));
            stackManager.release(argValue);
        });

        // The receiver is passed as the first argument.
        cg.emit.emit("pushl", stackManager.reify(receiver));

        // Lookup function address and call.
        Value funcAddr = stackManager.getRegister();
        cg.emit.emitLoad(0, stackManager.reify(receiver), stackManager.reify(funcAddr));
        cg.emit.emitLoad(ast.sym.offset, stackManager.reify(funcAddr), stackManager.reify(funcAddr));
        cg.emit.emit("call", "*"+stackManager.reify(funcAddr));

        stackManager.release(funcAddr);
        stackManager.release(receiver);
        return stackManager.getRegister(RegisterManager.RESULT_REG);
	}

	@Override
	public Value unaryOp(UnaryOp ast, StackManager stackManager) {
        Value argValue = gen(ast.arg(), stackManager);

        switch (ast.operator) {
        case U_PLUS:
            break;

        case U_MINUS:
            cg.emit.emit("negl", stackManager.reify(argValue));
            break;

        case U_BOOL_NOT:
            cg.emit.emit("negl", stackManager.reify(argValue));
            cg.emit.emit("incl", stackManager.reify(argValue));
            break;
        }

        return argValue;
	}
	
	@Override
	public Value var(Var ast, StackManager stackManager) {
        Value reg = stackManager.getRegister();

        switch (ast.sym.kind) {
        case LOCAL:
        case PARAM:
            reg.src = ast.sym.offset;
            cg.emit.emitLoad(ast.sym.offset, BASE_REG, stackManager.reify(reg));
            break;
        default:
            throw new RuntimeException("Unknown kind " + ast.sym.kind);
        }

		return reg;
	}

}
