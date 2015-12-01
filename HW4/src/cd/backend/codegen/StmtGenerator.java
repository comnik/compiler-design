package cd.backend.codegen;

import cd.Config;
import cd.ToDoException;
import cd.backend.codegen.StackManager.Value;
import cd.ir.Ast;
import cd.ir.Ast.*;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.MethodSymbol;
import cd.util.debug.AstOneLine;

import java.util.List;
import java.util.function.BinaryOperator;

import static cd.Config.MAIN;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.BASE_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;

/**
 * Generates code to process statements and declarations.
 */
class StmtGenerator extends AstVisitor<Value, StackManager> {
	protected final AstCodeGenerator cg;

	StmtGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public void gen(Ast ast) {
		visit(ast, null);
	}

    public int labelCounter = 0;

	@Override
	public Value visit(Ast ast, StackManager stackManager) {
        cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));

        try {
			return super.visit(ast, stackManager);
		} finally {
			cg.emit.decreaseIndent();
		}
	}

	@Override
	public Value methodCall(MethodCall ast, StackManager stackManager) {
        return cg.eg.gen(ast.getMethodCallExpr(), stackManager);
	}

	public Value methodCall(MethodSymbol sym, List<Expr> allArguments) {
		throw new RuntimeException("Not required");
	}

	// Emit vtable for arrays of this class:
	@Override
	public Value classDecl(ClassDecl ast, StackManager stackManager) {
        // TODO
        return visitChildren(ast, stackManager);
	}

	@Override
	public Value methodDecl(MethodDecl ast, StackManager willBeIgnored) {
        if (ast.sym.name.equals("main")) {
            // TODO Check if we are in class Main as well.

            // Emit the main() method:
            cg.emit.emitRaw(Config.TEXT_SECTION);
            cg.emit.emitRaw(".globl " + MAIN);
            cg.emit.emitLabel(MAIN);
        } else {
            cg.emit.emitLabel(ast.sym.name);
        }

        // Set parameter offsets.
        ast.sym.parameters.stream().reduce(0, (nextOffset, varSym) -> {
            varSym.offset = nextOffset;
            return nextOffset + varSym.type.getRefSize();
        }, (o1, o2) -> o1);

        // Create virtual registers for all local variables.
        Integer lastOffset = ast.decls().children().stream()
                .map(node -> ((VarDecl) node).sym)
                .reduce(0, (nextOffset, varSym) -> {
                    varSym.offset = nextOffset;
                    return nextOffset - varSym.type.getRefSize();
                }, (o1, o2) -> o1);

        // Get the stack size required to hold locals.
        int stackSize = -lastOffset;

        cg.emit.emit("pushl", BASE_REG);
        cg.emit.emit("movl", STACK_REG, BASE_REG);

		if (Config.systemKind == Config.SystemKind.MACOSX) {
			// Align the stack to 16 bytes.
			cg.emit.emit("andl", constant(-16), STACK_REG);
			stackSize = alignedTo16(stackSize);
		}

        // Make space on the stack, if required.
        if (stackSize > 0) {
            cg.emit.emit("subl", stackSize, STACK_REG);
        }

        // Create a new virtual register manager for this stack frame.
        final StackManager stackManager = new StackManager(lastOffset, cg);

        // Generate code for the body.
        visit(ast.body(), stackManager);

		cg.emitMethodSuffix(true);

		return null;
	}

	@Override
	public Value ifElse(IfElse ast, StackManager stackManager) {
		Value reg = cg.eg.gen(ast.condition(), stackManager);

		String thenLabel = getAndIncrementLabel();
        String otherwiseLabel = getAndIncrementLabel();
        String doneLabel = getAndIncrementLabel();

        if (ast.otherwise() == null) {
            // Just an if statement, no else part.
            cg.emit.emit("cmpl", "$0", reg.toString());
            cg.emit.emit("jle", doneLabel);

            // Then branch.
            visit(ast.then(), stackManager);
            cg.emit.emit("jmp", doneLabel);

            // We're done.
            cg.emit.emitLabel(doneLabel);
            return null;
        } else {
            // There is an else part.
            cg.emit.emit("cmpl", "$0", reg.toString());
            cg.emit.emit("jle", otherwiseLabel);

            // Then branch.
            cg.emit.emitLabel(thenLabel);
            visit(ast.then(), stackManager);
            cg.emit.emit("jmp", doneLabel);

            // Otherwise branch.
            cg.emit.emitLabel(otherwiseLabel);
            visit(ast.otherwise(), stackManager);
            cg.emit.emit("jmp", doneLabel);

            // We're done.
            cg.emit.emitLabel(doneLabel);
            return null;
        }
	}

	@Override
	public Value whileLoop(WhileLoop ast, StackManager stackManager) {
		Value reg = cg.eg.gen(ast.condition(), stackManager);

        String loopLabel = getAndIncrementLabel();
        String doneLabel = getAndIncrementLabel();

        // Check loop condition initially.
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jle", doneLabel);

		// Main while loop.
		cg.emit.emitLabel(loopLabel);
		visit(ast.body(), stackManager);

        // Check if loop condition has changed.
        reg = cg.eg.gen(ast.condition(), stackManager);
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jg", loopLabel);

        // We're done.
		cg.emit.emitLabel(doneLabel);
		return null;
	}

	@Override
	public Value assign(Assign ast, StackManager stackManager) {
        // TODO Support for array access and fields, besides normal vars.
        Var var = (Var) ast.left();
        Value rhsReg = cg.eg.gen(ast.right(), stackManager);

        stackManager.reify(rhsReg);
        cg.emit.emit("movl", rhsReg.toRegister(), AssemblyEmitter.registerOffset(var.sym.offset, BASE_REG));

        // stackManager.releaseRegister(rhsReg);
        return null;
    }

	@Override
	public Value builtInWrite(BuiltInWrite ast, StackManager stackManager) {
        Value printfArg = cg.eg.gen(ast.arg(), stackManager);

        stackManager.emitCallerSave();

        cg.emit.emit("subl", constant(16), STACK_REG);

        stackManager.reify(printfArg);
        cg.emit.emitStore(printfArg.toRegister(), 4, STACK_REG);

        cg.emit.emitStore("$STR_D", 0, STACK_REG);
        cg.emit.emit("call", Config.PRINTF);
        cg.emit.emit("add", constant(16), STACK_REG);

        stackManager.release(printfArg);
        return null;
	}

	@Override
	public Value builtInWriteln(BuiltInWriteln ast, StackManager stackManager) {
        stackManager.emitCallerSave();

        cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emitStore("$STR_NL", 0, STACK_REG);
		cg.emit.emit("call", Config.PRINTF);
		cg.emit.emit("add", constant(16), STACK_REG);

		return null;
	}

	@Override
	public Value returnStmt(ReturnStmt ast, StackManager stackManager) {
        Value result = visit(ast, stackManager);
        stackManager.reify(result);

        Value returnValue = stackManager.getRegister(RegisterManager.RESULT_REG);

        cg.emit.emit("movl", result.toString(), returnValue.toRegister());
        return returnValue;
	}

    // Helper functions.

    /** Returns the next highest multiple y of 16, such that x < y. */
    private int alignedTo16(int x) {
        int y = 16;
        while (x > y) {
            y += 16;
        }
        return y;
    }

    /** Returns a unique label and increments the label counter. */
    private String getAndIncrementLabel() {
        String ret = "L" + Integer.toString(labelCounter);
        labelCounter++;
        return ret;
    }
}
