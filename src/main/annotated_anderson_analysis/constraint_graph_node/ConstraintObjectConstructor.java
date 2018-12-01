package annotated_anderson_analysis.constraint_graph_node;

import soot.Local;
import soot.RefType;
import soot.jimple.internal.JimpleLocal;

public class ConstraintObjectConstructor extends ConstraintConstructor {
    private int id;

    private final ConstraintVariable objectVariable;

    public ConstraintObjectConstructor(int id, RefType type) {
        this.id = id;
        Local objectLocal = new JimpleLocal("object_" + id, type);
        objectVariable = new ConstraintVariable(objectLocal, 1);
    }

    public int getId() {
        return id;
    }

    public ConstraintVariable getObjectVariable() {
        return this.objectVariable;
    }

}
