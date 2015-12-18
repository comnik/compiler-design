package cd.flowgraph;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.VariableSymbol;

import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class KillsVisitor extends AstVisitor<VariableSymbol, Void> {

    private final Map<String, Ast.Assign> assigns =
            new HashMap<String, Ast.Assign>();

    @Override
    public VariableSymbol assign(Ast.Assign ast, Void arg) {
        // Get the symbol of the left expression.
        Symbol.VariableSymbol vSymbol = visit(ast.left(), null);

        // Only do something if the left expr is a var and a local at that.
        if (vSymbol != null && vSymbol.kind.equals(VariableSymbol.Kind.LOCAL)) {
            // Check uf the left var is already assigned to before.
            if (assigns.containsKey(vSymbol.name)) {
                // Update killset of all previous assigns, and the current one.
                assigns.forEach((k,v) -> {
                    if (k.equals(vSymbol.name)) {
                        v.kills.add(ast);
                        ast.kills.add(v);

                        // Debugging
                        System.out.println(ast.toString() + " kills " + v.toString());
                        System.out.println("Likewise, " + v.toString() + " kills " + ast.toString());

                    }
                });
            }
            // Add the assign to the global list.
            assigns.put(vSymbol.name, ast);
        }

        return null;
    }

    public VariableSymbol var(Ast.Var ast, Void arg) {
        return ast.sym;
    }
}
