package cd.flowgraph;

import cd.ir.Ast;
import cd.ir.AstVisitor;
import cd.ir.Symbol;
import cd.ir.Symbol.VariableSymbol;

import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class KillsVisitor extends AstVisitor<VariableSymbol, Void> {

    private final Map<String, List<Ast.Assign>> assigns = new HashMap<>();

    @Override
    public VariableSymbol assign(Ast.Assign ast, Void arg) {
        // Get the symbol of the left expression.
        Symbol.VariableSymbol vSymbol = visit(ast.left(), null);

        // Only do something if the left expr is a var and a local at that.
        if (vSymbol != null && vSymbol.kind.equals(VariableSymbol.Kind.LOCAL)) {
            List<Ast.Assign> assignList;

            // Check if the left var is already assigned to before.
            if (assigns.containsKey(vSymbol.name)) {
                // Update killset of all previous assigns, and the current one.
                assignList = assigns.get(vSymbol.name);
                assignList.forEach(assignment -> assignment.kills.add(ast));
                ast.kills.addAll(assignList);

                // Debugging
                System.out.println(ast + " kills " + assignList);
                System.out.println("Likewise, " + assignList + " kills " + ast);
            } else {
                // Initialize the assign list.
                assignList = new ArrayList<>();
            }

            assignList.add(ast);
            assigns.put(vSymbol.name, assignList);
        }

        return null;
    }

    public VariableSymbol var(Ast.Var ast, Void arg) {
        return ast.sym;
    }
}
