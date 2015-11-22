package cd.backend.codegen;

import cd.Config;
import cd.ToDoException;
import cd.backend.codegen.RegisterManager.Register;
import cd.ir.Ast;
import cd.ir.Ast.*;
import cd.ir.AstVisitor;
import cd.ir.Symbol.MethodSymbol;
import cd.util.debug.AstOneLine;

import java.util.List;

import static cd.Config.MAIN;
import static cd.backend.codegen.AssemblyEmitter.constant;
import static cd.backend.codegen.RegisterManager.RESULT_REG;
import static cd.backend.codegen.RegisterManager.STACK_REG;
import static cd.backend.codegen.RegisterManager.BASE_REG;

/**
 * Generates code to process statements and declarations.
 */
class StmtGenerator extends AstVisitor<Register, Void> {
	protected final AstCodeGenerator cg;

	StmtGenerator(AstCodeGenerator astCodeGenerator) {
		cg = astCodeGenerator;
	}

	public void gen(Ast ast) {
		visit(ast, null);
	}

	@Override
	public Register visit(Ast ast, Void arg) {
		try {
			cg.emit.increaseIndent("Emitting " + AstOneLine.toString(ast));
			return super.visit(ast, arg);
		} finally {
			cg.emit.decreaseIndent();
		}
	}

	@Override
	public Register methodCall(MethodCall ast, Void dummy) {
		{
			throw new ToDoException();
		}
	}

	public Register methodCall(MethodSymbol sym, List<Expr> allArguments) {
		throw new RuntimeException("Not required");
	}

	// Emit vtable for arrays of this class:
	@Override
	public Register classDecl(ClassDecl ast, Void arg) {
        // TODO
        return visitChildren(ast, arg);
	}

	@Override
	public Register methodDecl(MethodDecl ast, Void arg) {
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
                return accum;
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

        cg.emit.emit("pushl", BASE_REG);
        cg.emit.emit("movl", STACK_REG, BASE_REG);

        // Make space on the stack.
        cg.emit.emit("addl", stackSize, STACK_REG);

        gen(ast.body());
		cg.emitMethodSuffix(true);

		return null;
	}

	@Override
	public Register ifElse(IfElse ast, Void arg) {
		// TODO Support for multiple IfElse statements via name mangling.
		Register reg = cg.eg.gen(ast.condition());

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
	public Register whileLoop(WhileLoop ast, Void arg) {
		// TODO Support for multiple WhileLoop statements via name mangling.
		Register reg = cg.eg.gen(ast.condition());

		// Check condition initially.
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jle", "done");

		// Main while loop.
		cg.emit.emitLabel("loop");
		cg.sg.gen(ast.body());
		reg = cg.eg.gen(ast.condition());
		cg.emit.emit("cmpl", "$0", reg.toString());
		cg.emit.emit("jg", "loop");

		cg.emit.emitLabel("done");
		return null;
	}

	@Override
	public Register assign(Assign ast, Void arg) {
        // TODO Support for array access and fields, besides normal vars.
        Var var = (Var) ast.left();
        Register rhsReg = cg.eg.gen(ast.right());

        cg.emit.emitStore(rhsReg, var.sym.offset, BASE_REG);

        // We need to check if the result register is in use,
        // because for method expressions, RESULT_REG is used without
        // marking it as used in the register manager.
        if (cg.rm.isInUse(rhsReg)) {
            cg.rm.releaseRegister(rhsReg);
        }

        return null;
    }

	@Override
	public Register builtInWrite(BuiltInWrite ast, Void arg) {
        Register printfArg = cg.eg.gen(ast.arg());

        cg.emit.emit("pushl", printfArg);
        cg.emit.emit("pushl", "$STR_D");
        cg.emit.emit("call", Config.PRINTF);

        cg.rm.releaseRegister(printfArg);
        return null;
	}

	@Override
	public Register builtInWriteln(BuiltInWriteln ast, Void arg) {
		cg.emit.emit("sub", constant(16), STACK_REG);
		cg.emit.emitStore("$STR_NL", 0, STACK_REG);
		cg.emit.emit("call", Config.PRINTF);
		cg.emit.emit("add", constant(16), STACK_REG);
		return null;
	}

	@Override
	public Register returnStmt(ReturnStmt ast, Void arg) {
        Register result = visit(ast, arg);
        cg.emit.emit("movl", result, Register.EAX);
        return RESULT_REG;
	}

}