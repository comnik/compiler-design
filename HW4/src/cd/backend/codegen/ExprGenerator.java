package cd.backend.codegen;

import cd.ToDoException;
import cd.backend.codegen.RegisterManager.Register;
import cd.backend.codegen.StackManager.Value;
import cd.ir.Ast.*;
import cd.ir.ExprVisitor;
import cd.util.debug.AstOneLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	@Override
	public Value binaryOp(BinaryOp ast, StackManager stackManager) {
		int leftRN = cg.rnv.calc(ast.left());
		int rightRN = cg.rnv.calc(ast.right());

		Value leftReg, rightReg;
		if (leftRN > rightRN) {
            leftReg = gen(ast.left(), stackManager);
            rightReg = gen(ast.right(), stackManager);
        } else {
            rightReg = gen(ast.right(), stackManager);
            leftReg = gen(ast.left(), stackManager);
        }

        stackManager.reify(rightReg);
        stackManager.reify(leftReg);

		cg.debug("Binary Op: %s (%s,%s)", ast, leftReg, rightReg);

		switch (ast.operator) {
		case B_TIMES:
			cg.emit.emit("imul", rightReg.toString(), leftReg.toRegister());
			break;
        case B_DIV:
            // TODO
            break;
		case B_PLUS:
			cg.emit.emit("add", rightReg.toString(), leftReg.toRegister());
			break;
		case B_MINUS:
			cg.emit.emit("sub", rightReg.toString(), leftReg.toRegister());
			break;
        case B_MOD:
            // TODO
            break;
        case B_AND:
            cg.emit.emit("and", rightReg.toString(), leftReg.toRegister());
            break;
        case B_OR:
            cg.emit.emit("or", rightReg.toString(), leftReg.toRegister());
            break;
        case B_LESS_THAN:
            // TODO change to similar implementation like the others, if it works.
            cg.emit.emit("subl", leftReg.toString(), rightReg.toRegister());
            cg.emit.emit("movl", rightReg.toString(), leftReg.toRegister());
            break;
        case B_LESS_OR_EQUAL:
            cg.emit.emit("cmpl", leftReg.toString(), rightReg.toRegister()); // Set flags.
            cg.emit.emit("xorl", leftReg.toString(), leftReg.toRegister());   // Set leftReg to 0.
            cg.emit.emit("setle", leftReg.toRegister());           // Set leftReg to 1 if ((SF xor OF) || ZF) == true.
            break;
        case B_GREATER_THAN:
            cg.emit.emit("cmpl", leftReg.toString(), rightReg.toRegister()); // Set flags.
            cg.emit.emit("xorl", leftReg.toString(), leftReg.toRegister());   // Set leftReg to 0.
            cg.emit.emit("setg", leftReg.toRegister());            // Set leftReg to 1 if (!(SF xor OF) && !ZF) == true.
            break;
        case B_GREATER_OR_EQUAL:
            cg.emit.emit("cmpl", leftReg.toString(), rightReg.toRegister()); // Set flags.
            cg.emit.emit("xorl", leftReg.toString(), leftReg.toRegister());   // Set leftReg to 0.
            cg.emit.emit("setge", leftReg.toRegister());           // Set leftReg to 1 if !(SF xor OF) == true.
            break;
        case B_EQUAL:
            cg.emit.emit("cmpl", leftReg.toString(), rightReg.toRegister()); // Set flags.
            cg.emit.emit("xorl", leftReg.toString(), leftReg.toRegister());   // Set leftReg to 0.
            cg.emit.emit("sete", leftReg.toRegister());            // Set leftReg to 1 if ZF == true.
            break;
        case B_NOT_EQUAL:
            cg.emit.emit("cmpl", leftReg.toString(), rightReg.toRegister()); // Set flags.
            cg.emit.emit("xorl", leftReg.toString(), leftReg.toRegister());   // Set leftReg to 0.
            cg.emit.emit("sete", leftReg.toRegister());            // Set leftReg to 1 if ZF == true.
            cg.emit.emit("notl", leftReg.toRegister());            // Invert result.
            break;
        default:
			throw new ToDoException();
		}

		stackManager.release(rightReg);

		return leftReg;
	}

	@Override
	public Value booleanConst(BooleanConst ast, StackManager stackManager) {
        Value v = stackManager.getRegister();
        String booleanValue = (ast.value == true) ? "$0x1" : "$0x0";

        stackManager.reify(v);
        cg.emit.emit("movb", booleanValue, v.toString());
        return v;
    }

	@Override
	public Value builtInRead(BuiltInRead ast, StackManager stackManager) {
		Value v = stackManager.getRegister();

        stackManager.emitCallerSave();
        stackManager.reify(v);

        cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emit("leal", AssemblyEmitter.registerOffset(8, STACK_REG), v.toRegister());
		cg.emit.emitStore(v.toRegister(), 4, STACK_REG);
		cg.emit.emitStore("$STR_D", 0, STACK_REG);
		cg.emit.emit("call", SCANF);
		cg.emit.emitLoad(8, STACK_REG, v.toRegister());
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
		{
			throw new ToDoException();
		}
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
		{
			throw new ToDoException();
		}
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
			throw new ToDoException();
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
            cg.emit.emit("pushl", argValue.toRegister());
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
            cg.emit.emit("negl", argValue.toRegister());
            break;

        case U_BOOL_NOT:
            cg.emit.emit("negl", argValue.toRegister());
            cg.emit.emit("incl", argValue.toRegister());
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
            cg.emit.emitLoad(ast.sym.offset, BASE_REG, reg.toRegister());
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
