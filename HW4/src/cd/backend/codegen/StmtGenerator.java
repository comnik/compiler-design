package cd.backend.codegen;

import cd.Config;
import cd.backend.codegen.StackManager.Value;
import cd.ir.Ast;
import cd.ir.Ast.*;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.MethodSymbol;
import cd.util.debug.AstOneLine;

import java.util.List;

import static cd.backend.codegen.AssemblyEmitter.constant;
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

    private int labelCounter = 0;
    private Symbol.ClassSymbol currentClassSym;

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
    public Value classDecl(ClassDecl ast, StackManager stackManager) {
        // Set the current class symbol.
        currentClassSym = ast.sym;
        return visitChildren(ast, stackManager);
    }

	@Override
	public Value methodCall(MethodCall ast, StackManager stackManager) {
        return cg.eg.gen(ast.getMethodCallExpr(), stackManager);
	}

	public Value methodCall(MethodSymbol sym, List<Expr> allArguments) {
		throw new RuntimeException("Not required");
	}

	@Override
	public Value methodDecl(MethodDecl ast, StackManager willBeIgnored) {
        cg.emit.emitLabel(getMethodLabel(currentClassSym, ast.sym));

        // Set parameter offsets, leaving 12 bytes for return address and reciever.
        ast.sym.parameters.stream().reduce(12, (nextOffset, varSym) -> {
            varSym.offset = nextOffset;
            return nextOffset + varSym.type.getRefSize();
        }, (o1, o2) -> o1);

        // Create virtual registers for all local variables.
        Integer lastOffset = ast.decls().children().stream()
                .map(node -> ((VarDecl) node).sym)
                .reduce(-4, (nextOffset, varSym) -> {
                    varSym.offset = nextOffset;
                    return nextOffset - varSym.type.getRefSize();
                }, (o1, o2) -> o1);

        // Get the stack size required to hold locals.
        int stackSize = -lastOffset;

        // Create a new virtual register manager for this stack frame.
        final StackManager stackManager = new StackManager(lastOffset, cg);

        // Generate code for the body.
        cg.emitMethodPrefix(stackSize);
        stackManager.emitCalleeSave();
        visit(ast.body(), stackManager);
        stackManager.emitCalleeRestore();

		cg.emitMethodSuffix(ast.sym.returnType == Symbol.PrimitiveTypeSymbol.voidType);

		return null;
	}

	@Override
	public Value ifElse(IfElse ast, StackManager stackManager) {
		Value reg = cg.eg.gen(ast.condition(), stackManager);

		String doneLabel = getAndIncrementLabel();

        if (ast.otherwise() == null) {
            // Just an if statement, no else part.
            cg.emit.emit("cmpl", constant(0), stackManager.reify(reg));
            cg.emit.emit("je", doneLabel);

            // Then branch.
            visit(ast.then(), stackManager);
            cg.emit.emit("jmp", doneLabel);
        } else {
            // There is an else part.
            String thenLabel = getAndIncrementLabel();
            String otherwiseLabel = getAndIncrementLabel();

            cg.emit.emit("cmpl", constant(0), stackManager.reify(reg));
            cg.emit.emit("je", otherwiseLabel);

            // Then branch.
            cg.emit.emitLabel(thenLabel);
            visit(ast.then(), stackManager);
            cg.emit.emit("jmp", doneLabel);

            // Otherwise branch.
            cg.emit.emitLabel(otherwiseLabel);
            visit(ast.otherwise(), stackManager);
            cg.emit.emit("jmp", doneLabel);
        }

        // We're done.
        cg.emit.emitLabel(doneLabel);

        return null;
	}

	@Override
	public Value whileLoop(WhileLoop ast, StackManager stackManager) {
		Value reg = cg.eg.gen(ast.condition(), stackManager);

        String loopLabel = getAndIncrementLabel();
        String doneLabel = getAndIncrementLabel();

        // Check loop condition initially.
		cg.emit.emit("cmpl", constant(0), stackManager.reify(reg));
		cg.emit.emit("jle", doneLabel);

		// Main while loop.
		cg.emit.emitLabel(loopLabel);
		visit(ast.body(), stackManager);

        // Check if loop condition has changed.
        reg = cg.eg.gen(ast.condition(), stackManager);
		cg.emit.emit("cmpl", constant(0), stackManager.reify(reg));
		cg.emit.emit("jg", loopLabel);

        // We're done.
		cg.emit.emitLabel(doneLabel);
		return null;
	}

	@Override
	public Value assign(Assign ast, StackManager stackManager) {
        Value target = cg.eg.gen(ast.left(), stackManager);
        Value value = cg.eg.gen(ast.right(), stackManager);

        cg.emit.emit("movl", stackManager.reify(value), stackManager.toOffset(target));

        stackManager.release(value);
        return null;
    }

	@Override
	public Value builtInWrite(BuiltInWrite ast, StackManager stackManager) {
        Value printfArg = cg.eg.gen(ast.arg(), stackManager);

        stackManager.emitCallerSave();

        cg.emit.emit("subl", constant(16), STACK_REG);

        cg.emit.emitStore(stackManager.reify(printfArg), 4, STACK_REG);

        cg.emit.emitStore("$STR_D", 0, STACK_REG);
        cg.emit.emit("call", Config.PRINTF);
        cg.emit.emit("addl", constant(16), STACK_REG);

        stackManager.emitCallerRestore();

        stackManager.release(printfArg);
        return null;
	}

	@Override
	public Value builtInWriteln(BuiltInWriteln ast, StackManager stackManager) {
        stackManager.emitCallerSave();

        cg.emit.emit("subl", constant(16), STACK_REG);
		cg.emit.emitStore("$STR_NL", 0, STACK_REG);
		cg.emit.emit("call", Config.PRINTF);
		cg.emit.emit("addl", constant(16), STACK_REG);

        stackManager.emitCallerRestore();

		return null;
	}

	@Override
	public Value returnStmt(ReturnStmt ast, StackManager stackManager) {
        Value result = cg.eg.gen(ast.arg(), stackManager);
        Value returnValue = stackManager.getRegister(RegisterManager.RESULT_REG);

        cg.emit.emit("movl", stackManager.reify(result), stackManager.reify(returnValue));
        return returnValue;
	}

    // Helper functions.

    /** Returns a unique label and increments the label counter. */
    private String getAndIncrementLabel() {
        String ret = "L" + Integer.toString(labelCounter);
        labelCounter++;
        return ret;
    }

    /** Returns a unique method label. */
    private String getMethodLabel(Symbol.ClassSymbol clsSym, MethodSymbol methodSym) {
        return "__" + clsSym.name + "_" + methodSym.name;
    }
}
