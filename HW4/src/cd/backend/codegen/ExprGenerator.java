package cd.backend.codegen;

import static cd.Config.SCANF;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.BASE_REG;
import static cd.backend.codegen.RegisterManager.RESULT_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;
import cd.ToDoException;
import cd.backend.codegen.RegisterManager.Register;
import cd.ir.Ast.BinaryOp;
import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.BuiltInRead;
import cd.ir.Ast.Cast;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Field;
import cd.ir.Ast.Index;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.NewArray;
import cd.ir.Ast.NewObject;
import cd.ir.Ast.NullConst;
import cd.ir.Ast.ThisRef;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.Var;
import cd.ir.ExprVisitor;
import cd.util.debug.AstOneLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates code to evaluate expressions. After emitting the code, returns a
 * Register where the result can be found.
 */
class ExprGenerator extends ExprVisitor<Register, Void> {
	protected final AstCodeGenerator cg;

	ExprGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public Register gen(Expr ast) {
		return visit(ast, null);
	}

	@Override
	public Register visit(Expr ast, Void arg) {
		try {
			cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));
			return super.visit(ast, null);
		} finally {
			cg.emit.decreaseIndent();
		}

	}

	@Override
	public Register binaryOp(BinaryOp ast, Void arg) {
		// Simplistic HW1 implementation that does
		// not care if it runs out of registers, and
		// supports only a limited range of operations:

		int leftRN = cg.rnv.calc(ast.left());
		int rightRN = cg.rnv.calc(ast.right());

		Register leftReg, rightReg;
		if (leftRN > rightRN) {
			leftReg = gen(ast.left());
			rightReg = gen(ast.right());
		} else {
			rightReg = gen(ast.right());
			leftReg = gen(ast.left());
		}

		cg.debug("Binary Op: %s (%s,%s)", ast, leftReg, rightReg);

		switch (ast.operator) {
		case B_TIMES:
			cg.emit.emit("imul", rightReg, leftReg);
			break;
		case B_PLUS:
			cg.emit.emit("add", rightReg, leftReg);
			break;
		case B_MINUS:
			cg.emit.emit("sub", rightReg, leftReg);
			break;
		default:
			throw new ToDoException();
		}

		cg.rm.releaseRegister(rightReg);

		return leftReg;
	}

	@Override
	public Register booleanConst(BooleanConst ast, Void arg) {
        Register reg = cg.rm.getRegister();
        String booleanValue = (ast.value == true) ? "$0x1" : "$0x0";

        cg.emit.emit("movb", booleanValue, reg);
        return reg;
    }

	@Override
	public Register builtInRead(BuiltInRead ast, Void arg) {
		Register reg = cg.rm.getRegister();
		cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emit("leal", AssemblyEmitter.registerOffset(8, STACK_REG), reg);
		cg.emit.emitStore(reg, 4, STACK_REG);
		cg.emit.emitStore("$STR_D", 0, STACK_REG);
		cg.emit.emit("call", SCANF);
		cg.emit.emitLoad(8, STACK_REG, reg);
		cg.emit.emit("add", constant(16), STACK_REG);
		return reg;
	}

	@Override
	public Register cast(Cast ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register index(Index ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register intConst(IntConst ast, Void arg) {
        Register reg = cg.rm.getRegister();
        cg.emit.emit("movl", "$" + ast.value, reg);
        return reg;
	}

	@Override
	public Register field(Field ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register newArray(NewArray ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register newObject(NewObject ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register nullConst(NullConst ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register thisRef(ThisRef ast, Void arg) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public Register methodCall(MethodCallExpr ast, Void arg) {
        // We have to push arguments in reverse order onto the stack.
        List<Expr> argsWithoutReciever = new ArrayList<Expr>();
        argsWithoutReciever.addAll(ast.argumentsWithoutReceiver());
        Collections.reverse(argsWithoutReciever);

        argsWithoutReciever.forEach(argExpr -> {
            // TODO check if pushl or pushb
            Register argValue = visit(argExpr, null);
            cg.emit.emit("pushl", argValue);
        });

        // Call the function.
        cg.emit.emit("call", 0);

        return RESULT_REG;
	}

	@Override
	public Register unaryOp(UnaryOp ast, Void arg) {
        Register argReg = gen(ast.arg());
        switch (ast.operator) {
        case U_PLUS:
            break;

        case U_MINUS:
            cg.emit.emit("negl", argReg);
            break;

        case U_BOOL_NOT:
            cg.emit.emit("negl", argReg);
            cg.emit.emit("incl", argReg);
            break;
        }

        return argReg;
	}
	
	@Override
	public Register var(Var ast, Void arg) {
        Register reg = cg.rm.getRegister();
        switch (ast.sym.kind) {
            case LOCAL:
                cg.emit.emitLoad(ast.sym.offset, BASE_REG, reg);
                break;
            case PARAM:
                // TODO
                break;
            case FIELD:
                // TODO
                break;
        }
		return reg;
	}

}