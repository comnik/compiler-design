package cd.backend.codegen;

import cd.Config;
import cd.ToDoException;
import cd.backend.codegen.RegisterManager.Register;
import cd.backend.codegen.VRegManager.VRegister;
import cd.ir.Ast;
import cd.ir.Ast.*;
import cd.ir.AstVisitor;
import cd.ir.Symbol.MethodSymbol;
import cd.util.debug.AstOneLine;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.List;

import static cd.Config.MAIN;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.RESULT_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;
import static cd.backend.codegen.RegisterManager.BASE_REG;

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
	public VRegister methodDecl(MethodDecl ast, VRegManager vRegManager) {
        if (ast.sym.name.equals("main")) {
            // TODO Check if we are in class Main as well.

            // Emit the main() method:
            cg.emit.emitRaw(Config.TEXT_SECTION);
            cg.emit.emitRaw(".globl " + MAIN);
            cg.emit.emitLabel(MAIN);
        } else {
            cg.emit.emitLabel(ast.sym.name);
        }

        // Get the stack size required to hold all local variables.
        Integer stackSize = new AstVisitor<Integer, Integer>() {
            private final static int SIZEOF_INT = 4;
            private final static int SIZEOF_BOOL = 1;

            @Override
            public Integer seq(Ast.Seq ast, Integer base) {
                // This could just be a stream.reduce :: Integer -> Ast -> Integer,
                // but Java won't let me..
                Integer accum = base;
                for (Ast node : ast.rwChildren()) {
                    accum = visit(node, accum);
                }
                return -accum; // Return negative maximum offset so it is a size.
            }

            @Override
            public Integer varDecl(VarDecl ast, Integer offset) {
                // Remember the position on the stack, relative to the base pointer.
                switch (ast.type) {
                    case "int":
                        ast.sym.offset = offset - SIZEOF_INT; break;
                    case "boolean":
                        ast.sym.offset = offset - SIZEOF_BOOL; break;
                    default:
                        // Were dealing with a reference here, semantic checking has
                        // already ensured that no unknown types occur.
                        ast.sym.offset = offset - Config.SIZEOF_PTR;
                }
                return ast.sym.offset;
            }
        }.visit(ast.decls(), 0);

        // Align the stack to 16 bytes.
        cg.emit.emit("andl", constant(-16), STACK_REG);

        if (Config.systemKind == Config.SystemKind.MACOSX) {
            stackSize = alignedTo16(4 + stackSize);
        }

        cg.emit.emit("pushl", BASE_REG);
        cg.emit.emit("movl", STACK_REG, BASE_REG);

        // Make space on the stack.
        cg.emit.emit("subl", stackSize, STACK_REG);

        // Create a new virtual register manager for this stack frame
        // and generate code for the body.
        vRegManager = new VRegManager(stackSize, cg);
        visit(ast.body(), vRegManager);

		cg.emitMethodSuffix(true);

		return null;
	}

	@Override
	public VRegister ifElse(IfElse ast, VRegManager vRegManager) {
		// TODO Support for multiple IfElse statements via name mangling.
		VRegister reg = cg.eg.gen(ast.condition(), vRegManager);

		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jle", "otherwise");

		// then branch
		cg.emit.emitLabel("then");
		cg.sg.gen(ast.then());
		cg.emit.emit("jmp", "done");

		// otherwise branch
		cg.emit.emitLabel("otherwise");
		cg.sg.gen(ast.otherwise());
		cg.emit.emit("jmp", "done");

		cg.emit.emitLabel("done");
		return null;
	}

	@Override
	public VRegister whileLoop(WhileLoop ast, VRegManager vRegManager) {
		// TODO Support for multiple WhileLoop statements via name mangling.
		VRegister reg = cg.eg.gen(ast.condition(), vRegManager);

		// Check condition initially.
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jle", "done");

		// Main while loop.
		cg.emit.emitLabel("loop");
		cg.sg.gen(ast.body());

        reg = cg.eg.gen(ast.condition(), vRegManager);

		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jg", "loop");

		cg.emit.emitLabel("done");
		return null;
	}

	@Override
	public VRegister assign(Assign ast, VRegManager vRegManager) {
        // TODO Support for array access and fields, besides normal vars.
        Var var = (Var) ast.left();
        VRegister rhsReg = cg.eg.gen(ast.right(), vRegManager);

        cg.emit.emitStore(vRegManager.toPhysical(rhsReg), var.sym.offset, BASE_REG);

        vRegManager.releaseRegister(rhsReg);
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

        vRegManager.releaseRegister(printfArg);
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

}
