package annotated_anderson_analysis;

import annotated_anderson_analysis.constraint_graph_node.BasicConstraintGraphNode;
import annotated_anderson_analysis.constraint_graph_node.ConstraintConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintVariable;
import soot.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstraintGraph {
    private Map<Value, ConstraintVariable> variableMap;
    private Map<ConstraintVariable, Map<ConstraintConstructor, Set<ConstraintAnnotation>>> LSMap;

    public ConstraintGraph() {
        LSMap = new HashMap<>();
        variableMap = new HashMap<>();
    }

    public ConstraintVariable getFromVariableMap(Value value) {
        return variableMap.get(value);
    }

    public void addToVaribaleMap(Value value, ConstraintVariable variable) {
        variableMap.put(value, variable);
    }

    public Map<ConstraintConstructor, Set<ConstraintAnnotation>> getLS(ConstraintVariable variable) {
        Map<ConstraintConstructor, Set<ConstraintAnnotation>> LS = LSMap.get(variable);
        if (LS == null) {
            LS = new HashMap<>();
            LSMap.put(variable, LS);
            Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> preds = variable.getPreds();
            for (BasicConstraintGraphNode pred : preds.keySet()) {
                if (pred instanceof ConstraintConstructor) {
                    for (ConstraintAnnotation annotation : preds.get(pred))
                        addToLS((ConstraintConstructor) pred, annotation, LS);
                } else if (pred instanceof ConstraintVariable) {
                    Map<ConstraintConstructor, Set<ConstraintAnnotation>> predLS = getLS((ConstraintVariable) pred);
                    if (predLS != null) {
                        Set<ConstraintAnnotation> predAnnoSet = preds.get(pred);

                        for (ConstraintConstructor predLSConstructor : predLS.keySet()) {
                            Set<ConstraintAnnotation> predLSAnnoSet = predLS.get(predLSConstructor);
                            for (ConstraintAnnotation predLSAnnotation : predLSAnnoSet) {
                                for (ConstraintAnnotation predAnnotation : predAnnoSet) {
                                    ConstraintAnnotation newAnnotation = getMatchedAnnotation(predLSAnnotation, predAnnotation);
                                    if (newAnnotation != null)
                                        addToLS(predLSConstructor, newAnnotation, LS);
                                }
                            }
                        }
                    }
                }
            }
        }

        return LS;
    }

    private void addToLS(ConstraintConstructor constructor, ConstraintAnnotation annotation, Map<ConstraintConstructor, Set<ConstraintAnnotation>> LS) {
        Set<ConstraintAnnotation> annotationSet = LS.get(constructor);
        if (annotationSet == null) {
            annotationSet = new HashSet<>();
            LS.put(constructor, annotationSet);
        }

        annotationSet.add(annotation);
    }

    public void addToGraph(BasicConstraintGraphNode pred, BasicConstraintGraphNode succ, ConstraintAnnotation annotation) {
        if (pred instanceof ConstraintVariable) {
            if (succ instanceof ConstraintVariable) {
                ConstraintVariable varPred = (ConstraintVariable) pred;
                ConstraintVariable varSucc = (ConstraintVariable) succ;
                if (varPred.getOrder() < varSucc.getOrder())
                    varSucc.addToPreds(varPred, annotation);
                else
                    varPred.addToSuccs(varSucc, annotation);
            } else {
                ConstraintVariable variable = (ConstraintVariable) pred;
                variable.addToSuccs(succ, annotation);
            }
        } else if (succ instanceof ConstraintVariable) {
            ConstraintVariable variable = (ConstraintVariable) succ;
            variable.addToSuccs(succ, annotation);
        } else { // non-atomic constraint
            resolve(pred, succ, annotation);
        }
    }

    public void resolve(BasicConstraintGraphNode pred, BasicConstraintGraphNode succ, ConstraintAnnotation annotation) {
        if (pred instanceof ConstraintConstructor && succ instanceof ConstraintConstructor) {
            // TODO: resolve
        }
    }

    public void trans(BasicConstraintGraphNode pred, ConstraintAnnotation annoPred, BasicConstraintGraphNode succ, ConstraintAnnotation annoSucc) {
        ConstraintAnnotation newAnnotation = getMatchedAnnotation(annoPred, annoSucc);
        if (newAnnotation != null) {
            addToGraph(succ, pred, newAnnotation);
        }
    }

    public ConstraintAnnotation getMatchedAnnotation(ConstraintAnnotation annoPred, ConstraintAnnotation annoSucc) {
        ConstraintAnnotation newAnnotation = null;
        if (annoPred == ConstraintAnnotation.EMPTY) {
            if (annoSucc == ConstraintAnnotation.EMPTY)
                newAnnotation = ConstraintAnnotation.EMPTY;
            else
                newAnnotation = annoSucc.getClone();
        } else if (annoSucc == ConstraintAnnotation.EMPTY) {
            newAnnotation = annoPred.getClone();
        } else if (annoPred.equals(annoSucc)) {
            newAnnotation = ConstraintAnnotation.EMPTY;
        }
        return newAnnotation;
    }

}
