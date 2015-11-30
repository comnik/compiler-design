package cd.backend.codegen;

import cd.Config;
import cd.ToDoException;
import cd.backend.codegen.VRegManager.VRegister;
import cd.ir.Ast;
import cd.ir.Ast.*;
import cd.ir.AstVisitor;
import cd.ir.Symbol.MethodSymbol;
import cd.util.debug.AstOneLine;

import java.util.List;

import static cd.Config.MAIN;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.BASE_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;

/**
 * Generates code to process statements and declarations.
 */
class StmtGenerator extends AstVisitor<VRegister, VRegManager> {
	protected final AstCodeGenerator cg;

	StmtGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public void gen(Ast ast) {
		visit(ast, null);
	}

    public int labelCounter = 0;

	@Override
	public VRegister visit(Ast ast, VRegManager vRegManager) {
        cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));

        try {
			return super.visit(ast, vRegManager);
		} finally {
			cg.emit.decreaseIndent();
		}
	}

	@Override
	public VRegister methodCall(MethodCall ast, VRegManager vRegManager) {
		{
			throw new ToDoException();
		}
	}

	public VRegister methodCall(MethodSymbol sym, List<Expr> allArguments) {
		throw new RuntimeException("Not required");
	}

	// Emit vtable for arrays of this class:
	@Override
	public VRegister classDecl(ClassDecl ast, VRegManager vRegManager) {
        // TODO
        return visitChildren(ast, vRegManager);
	}

	@Override
	public VRegister methodDecl(MethodDecl ast, VRegManager willBeIgnored) {
        if (ast.sym.name.equals("main")) {
            // TODO Check if we are in class Main as well.

            // Emit the main() method:
            cg.emit.emitRaw(Config.TEXT_SECTION);
            cg.emit.emitRaw(".globl " + MAIN);
            cg.emit.emitLabel(MAIN);
        } else {
            cg.emit.emitLabel(ast.sym.name);
        }

        // Create a new virtual register manager for this stack frame
        // and generate code for the body.
        final VRegManager vRegManager = new VRegManager(0, cg);

        // Create virtual registers for all local variables.
        ast.decls().children().stream().forEach(astNode -> {
            VarDecl varDecl = (VarDecl) astNode;

            switch (varDecl.type) {
                case "int":
                    varDecl.sym.vregister = vRegManager.getRegister();
                    break;
                case "boolean":
                    varDecl.sym.vregister = vRegManager.getByteRegister();
                    break;
                default:
                    // Were dealing with a reference here, semantic checking has
                    // already ensured that no unknown types occur.
                    varDecl.sym.vregister = vRegManager.getRegister();
            }
        });

        // Get the stack size required to hold locals.
        int stackSize = vRegManager.getStackSize();

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

        // Generate code for the body.
        visit(ast.body(), vRegManager);

		cg.emitMethodSuffix(true);

		return null;
	}

	@Override
	public VRegister ifElse(IfElse ast, VRegManager vRegManager) {
		VRegister reg = cg.eg.gen(ast.condition(), vRegManager);

		String thenLabel = getAndIncrementLabel();
        String otherwiseLabel = getAndIncrementLabel();
        String doneLabel = getAndIncrementLabel();

        if (ast.otherwise() == null) {
            // Just an if statement, no else part.
            cg.emit.emit("cmpl", "$0", reg.toString());
            cg.emit.emit("jle", doneLabel);

            // Then branch.
            visit(ast.then(), vRegManager);
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
            visit(ast.then(), vRegManager);
            cg.emit.emit("jmp", doneLabel);

            // Otherwise branch.
            cg.emit.emitLabel(otherwiseLabel);
            visit(ast.otherwise(), vRegManager);
            cg.emit.emit("jmp", doneLabel);

            // We're done.
            cg.emit.emitLabel(doneLabel);
            return null;
        }
	}

	@Override
	public VRegister whileLoop(WhileLoop ast, VRegManager vRegManager) {
		VRegister reg = cg.eg.gen(ast.condition(), vRegManager);

        String loopLabel = getAndIncrementLabel();
        String doneLabel = getAndIncrementLabel();

        // Check loop condition initially.
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jle", doneLabel);

		// Main while loop.
		cg.emit.emitLabel(loopLabel);
		visit(ast.body(), vRegManager);

        // Check if loop condition has changed.
        reg = cg.eg.gen(ast.condition(), vRegManager);
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jg", loopLabel);

        // We're done.
		cg.emit.emitLabel(doneLabel);
		return null;
	}

	@Override
	public VRegister assign(Assign ast, VRegManager vRegManager) {
        // TODO Support for array access and fields, besides normal vars.
        Var var = (Var) ast.left();
        VRegister rhsReg = cg.eg.gen(ast.right(), vRegManager);

        cg.emit.emit("movl", vRegManager.toPhysical(rhsReg), vRegManager.toPhysical(var.sym.vregister));

        // vRegManager.releaseRegister(rhsReg);
        return null;
    }

	@Override
	public VRegister builtInWrite(BuiltInWrite ast, VRegManager vRegManager) {
        VRegister printfArg = cg.eg.gen(ast.arg(), vRegManager);

        cg.emit.emit("subl", constant(16), STACK_REG);
        cg.emit.emitStore(vRegManager.toPhysical(printfArg), 4, STACK_REG);
        cg.emit.emitStore("$STR_D", 0, STACK_REG);
        cg.emit.emit("call", Config.PRINTF);
        cg.emit.emit("add", constant(16), STACK_REG);

        // vRegManager.releaseRegister(printfArg);
        return null;
	}

	@Override
	public VRegister builtInWriteln(BuiltInWriteln ast, VRegManager vRegManager) {
		cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emitStore("$STR_NL", 0, STACK_REG);
		cg.emit.emit("call", Config.PRINTF);
		cg.emit.emit("add", constant(16), STACK_REG);
		return null;
	}

	@Override
	public VRegister returnStmt(ReturnStmt ast, VRegManager vRegManager) {
        VRegister result = visit(ast, vRegManager);
        cg.emit.emit("movl", vRegManager.toPhysical(result), vRegManager.toPhysical(vRegManager.RESULT_REG));
        return vRegManager.RESULT_REG;
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
