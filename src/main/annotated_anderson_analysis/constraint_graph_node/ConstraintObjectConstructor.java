package annotated_anderson_analysis.constraint_graph_node;

import soot.Local;
import soot.RefType;
import soot.jimple.internal.JimpleLocal;

public class ConstraintObjectConstructor extends ConstraintConstructor {
    private int id;
    private final Local objectLocal;

    public ConstraintObjectConstructor(int id, RefType type) {
        this.id = id;
        objectLocal = new JimpleLocal("Object_" + id, type);
    }

    public int getId() {
        return id;
    }

    public Local getObjectLocal() {
        return objectLocal;
    }
}
