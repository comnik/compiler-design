package cd.backend.codegen;

import cd.ToDoException;
import cd.backend.codegen.RegisterManager.Register;
import cd.backend.codegen.VRegManager.VRegister;
import cd.ir.Ast.*;
import cd.ir.ExprVisitor;
import cd.util.debug.AstOneLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cd.Config.SCANF;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.STACK_REG;

/**
 * Generates code to evaluate expressions. After emitting the code, returns a
 * Register where the result can be found.
 */
class ExprGenerator extends ExprVisitor<VRegister, VRegManager> {
	protected final AstCodeGenerator cg;

	ExprGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public VRegister gen(Expr ast, VRegManager vRegManager) {
		return visit(ast, vRegManager);
	}

	@Override
	public VRegister visit(Expr ast, VRegManager vRegManager) {
        cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));

        try {
			return super.visit(ast, vRegManager);
		} finally {
			cg.emit.decreaseIndent();
		}

	}

	@Override
	public VRegister binaryOp(BinaryOp ast, VRegManager vRegManager) {
		int leftRN = cg.rnv.calc(ast.left());
		int rightRN = cg.rnv.calc(ast.right());

		VRegister vLeftReg, vRightReg;
		if (leftRN > rightRN) {
            vLeftReg = gen(ast.left(), vRegManager);
            vRightReg = gen(ast.right(), vRegManager);
        } else {
            vRightReg = gen(ast.right(), vRegManager);
            vLeftReg = gen(ast.left(), vRegManager);
        }
        Register leftReg = vRegManager.toPhysical(vLeftReg);
        Register rightReg = vRegManager.toPhysical(vRightReg);

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
        case B_AND:
            cg.emit.emit("and", rightReg, leftReg);
            break;
        case B_OR:
            cg.emit.emit("or", rightReg, leftReg);
            break;
        case B_LESS_THAN:
            cg.emit.emit("sub", leftReg, rightReg);
            cg.emit.emit("movl", rightReg, leftReg);
            break;
        // TODO Support for remaining binary ops.
        default:
			throw new ToDoException();
		}

		// vRegManager.releaseRegister(vRightReg);

		return vLeftReg;
	}

	@Override
	public VRegister booleanConst(BooleanConst ast, VRegManager vRegManager) {
        VRegister reg = vRegManager.getRegister();
        String booleanValue = (ast.value == true) ? "$0x1" : "$0x0";

        cg.emit.emit("movb", booleanValue, vRegManager.toPhysical(reg));
        return reg;
    }

	@Override
	public VRegister builtInRead(BuiltInRead ast, VRegManager vRegManager) {
		VRegister reg = vRegManager.getRegister();
        Register physicalReg = vRegManager.toPhysical(reg);

		cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emit("leal", AssemblyEmitter.registerOffset(8, STACK_REG), physicalReg);
		cg.emit.emitStore(physicalReg, 4, STACK_REG);
		cg.emit.emitStore("$STR_D", 0, STACK_REG);
		cg.emit.emit("call", SCANF);
		cg.emit.emitLoad(8, STACK_REG, physicalReg);
		cg.emit.emit("add", constant(16), STACK_REG);

        return reg;
	}

	@Override
	public VRegister cast(Cast ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister index(Index ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister intConst(IntConst ast, VRegManager vRegManager) {
        VRegister reg = vRegManager.getRegister();
        cg.emit.emit("movl", "$" + ast.value, vRegManager.toPhysical(reg));
        return reg;
	}

	@Override
	public VRegister field(Field ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister newArray(NewArray ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister newObject(NewObject ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister nullConst(NullConst ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister thisRef(ThisRef ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	@Override
	public VRegister methodCall(MethodCallExpr ast, VRegManager vRegManager) {
        // We have to push arguments in reverse order onto the stack.
        List<Expr> argsWithoutReciever = new ArrayList<Expr>();
        argsWithoutReciever.addAll(ast.argumentsWithoutReceiver());
        Collections.reverse(argsWithoutReciever);

        argsWithoutReciever.forEach(argExpr -> {
            // TODO check if pushl or pushb
            VRegister argValue = visit(argExpr, vRegManager);
            cg.emit.emit("pushl", vRegManager.toPhysical(argValue));
        });

        // Call the function.
        cg.emit.emit("call", 0);

        return vRegManager.RESULT_REG;
	}

	@Override
	public VRegister unaryOp(UnaryOp ast, VRegManager vRegManager) {
        VRegister vArgReg = gen(ast.arg(), vRegManager);
        Register argReg = vRegManager.toPhysical(vArgReg);

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

        return vArgReg;
	}
	
	@Override
	public VRegister var(Var ast, VRegManager vRegManager) {
        VRegister reg;

        switch (ast.sym.kind) {
        case LOCAL:
            reg = ast.sym.vregister; break;
        case PARAM:
            // TODO
            reg = vRegManager.getRegister(); break;
        case FIELD:
            // TODO
            reg = vRegManager.getRegister(); break;
        default:
            throw new RuntimeException("Unknown kind " + ast.sym.kind);
        }

		return reg;
	}

}
