package annotated_anderson_analysis;

import soot.SootFieldRef;

import java.util.Objects;

public class ConstraintAnnotation {
    public static final ConstraintAnnotation EMPTY = new ConstraintAnnotation();

    private SootFieldRef fieldRef;

    private ConstraintAnnotation() {
        fieldRef = null;
    }

    public ConstraintAnnotation(SootFieldRef fieldRef) {
        this.fieldRef = fieldRef;
    }


    public Object getFieldRef() {
        return this.fieldRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ConstraintAnnotation that = (ConstraintAnnotation) o;
        return Objects.equals(this.fieldRef, that.fieldRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fieldRef);
    }

    public ConstraintAnnotation getClone() {
        ConstraintAnnotation newAnnotation = new ConstraintAnnotation(fieldRef);
        return newAnnotation;
    }

}
