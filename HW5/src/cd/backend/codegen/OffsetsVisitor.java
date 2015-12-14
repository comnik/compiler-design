package cd.backend.codegen;

import cd.Config;
import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;

/**
 * Sets offsets and creates vtables.
 */
public class OffsetsVisitor extends AstVisitor<Void,Void> {

    protected final AstCodeGenerator cg;

    OffsetsVisitor(AstCodeGenerator astCodeGenerator) {
        cg = astCodeGenerator;
    }

    @Override
    public Void classDecl(Ast.ClassDecl ast, Void arg) {
        // Emit vtable label.
        cg.emit.emitLabel(ast.sym.getVtableLabel());

        // Emit superclass link.
        cg.emit.emitConstantData(ast.sym.superClass.getVtableLabel());

        // Create vtable entries.
        ast.sym.getVTable().values().stream()
                .reduce(4, (nextOffset, binding) -> {
                    cg.emit.emitConstantData(getMethodLabel(binding.a, binding.b));
                    binding.b.offset = nextOffset;
                    return nextOffset + Config.SIZEOF_PTR;
                }, (o1, o2) -> o1);

        // Set field offsets.
        ast.sym.getFields().values().stream()
                .reduce(4, (nextOffset, varSym) -> {
                    varSym.offset = nextOffset;
                    return nextOffset + varSym.type.getFieldSize();
                }, (o1, o2) -> o1);

        return null;
    }

    /** Returns a unique method label. */
    private String getMethodLabel(Symbol.ClassSymbol clsSym, Symbol.MethodSymbol methodSym) {
        return "__" + clsSym.name + "_" + methodSym.name;
    }
}
