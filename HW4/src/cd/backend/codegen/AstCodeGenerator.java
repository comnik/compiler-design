package cd.backend.codegen;

import java.io.Writer;
import java.util.List;

import cd.Config;
import cd.Main;
import cd.backend.codegen.RegisterManager.Register;
import cd.ir.Ast.ClassDecl;

public class AstCodeGenerator {

	protected RegsNeededVisitor rnv;
	
	protected ExprGenerator eg;
	protected StmtGenerator sg;
	
	protected final Main main;
	
	protected final AssemblyEmitter emit;
	protected final RegisterManager rm = new RegisterManager();

	AstCodeGenerator(Main main, Writer out) {
		{
			initMethodData();
		}
		
		this.emit = new AssemblyEmitter(out);
		this.main = main;
		this.rnv = new RegsNeededVisitor();

		this.eg = new ExprGenerator(this);
		this.sg = new StmtGenerator(this);
	}

	protected void debug(String format, Object... args) {
		this.main.debug(format, args);
	}

	public static AstCodeGenerator createCodeGenerator(Main main, Writer out) {
		return new AstCodeGenerator(main, out);
	}
	
	protected static final String VAR_PREFIX = "var_";
	
	/**
	 * Main method. Causes us to emit x86 assembly corresponding to {@code ast}
	 * into {@code file}. Throws a {@link RuntimeException} should any I/O error
	 * occur.
	 * 
	 * <p>
	 * The generated file will be divided into three sections:
	 * <ol>
	 * <li>Prologue: Generated by {@link}. This contains any
	 * introductory declarations and the like.
	 * <li>Body: Generated by {@link ExprGenerator}. This contains the main
	 * method definitions.
	 * <li>Epilogue: Generated by {@link} This contains any
	 * final declarations required.
	 * </ol>
	 */
	public void go(List<? extends ClassDecl> astRoots) {
        // Emit some useful string constants:
        emit.emitRaw(Config.DATA_STR_SECTION);
        emit.emitLabel("STR_NL");
        emit.emitRaw(Config.DOT_STRING + " \"\\n\"");
        emit.emitLabel("STR_D");
        emit.emitRaw(Config.DOT_STRING + " \"%d\"");

        for (ClassDecl ast : astRoots) {
            sg.gen(ast);
        }

        // Emit the start of execution.
        emit.emitRaw(Config.TEXT_SECTION);
        emit.emitRaw(".globl " + Config.MAIN);
        emit.emitLabel(Config.MAIN);

        emitMethodPrefix();
        emit.emit("call", "__Main_main");
        emitMethodSuffix(true);
    }

    protected void initMethodData() {
        rm.initRegisters();
    }

    protected void emitMethodPrefix() {
        emit.emit("pushl", RegisterManager.BASE_REG);
        emit.emit("movl", RegisterManager.STACK_REG, RegisterManager.BASE_REG);
    }

	protected void emitMethodSuffix(boolean returnNull) {
		if (returnNull)
			emit.emit("movl", "$0", Register.EAX);
		emit.emitRaw("leave");
		emit.emitRaw("ret");
	}
}