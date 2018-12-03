package annotated_anderson_analysis.constraint_graph_node;

import soot.Local;
import soot.RefType;
import soot.jimple.internal.JimpleLocal;

public class ConstraintObjectConstructor extends ConstraintConstructor {
    private int id;
    private RefType refType;
    private ConstraintVariable objectVariable;

    public ConstraintObjectConstructor(int id, RefType type) {
        this.id = id;
        refType = type;
        Local objectLocal = new JimpleLocal("object_" + id, type);
        objectVariable = new ConstraintVariable(objectLocal, 1);
    }

    public int getId() {
        return id;
    }

    public RefType getRefType() {
        return refType;
    }

    public ConstraintVariable getObjectVariable() {
        return this.objectVariable;
    }

    @Override
    public String toString() {
        return id + ": " + refType;
    }

}
