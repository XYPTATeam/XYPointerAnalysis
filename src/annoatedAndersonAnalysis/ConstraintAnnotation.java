package annoatedAndersonAnalysis;

import java.util.Objects;

public class ConstraintAnnotation {
    public static final ConstraintAnnotation EMPTY = new ConstraintAnnotation();

    // TODO: confirm type of annotation
    private Object annotation;

    private ConstraintAnnotation() {
        annotation = null;
    }

    public ConstraintAnnotation(Object annotation) {
        this.annotation = annotation;
    }


    public Object getAnnotation() {
        return this.annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ConstraintAnnotation that = (ConstraintAnnotation) o;
        return Objects.equals(this.annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.annotation);
    }

    public ConstraintAnnotation getClone() {
        ConstraintAnnotation newAnnotation = new ConstraintAnnotation(annotation);
        return newAnnotation;
    }

}
