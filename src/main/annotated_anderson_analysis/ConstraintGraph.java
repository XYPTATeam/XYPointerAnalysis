package annotated_anderson_analysis;

import annotated_anderson_analysis.constraint_graph_node.BasicConstraintGraphNode;
import annotated_anderson_analysis.constraint_graph_node.ConstraintConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintVariable;
import soot.Local;

import java.util.*;

public class ConstraintGraph {
    private Map<Local, ConstraintVariable> variableMap;
    private Map<ConstraintVariable, Map<ConstraintConstructor, Set<ConstraintAnnotation>>> LSMap;
    private Set<ConstraintVariable> tempVarSet;

    public ConstraintGraph() {
        LSMap = new HashMap<>();
        variableMap = new HashMap<>();
        tempVarSet = new HashSet<>();
    }

    public void addToVariableMap(Local value, ConstraintVariable variable) {
        variableMap.put(value, variable);
        tempVarSet.add(variable);
    }

    public ConstraintVariable getFromVariableMap(Local value) {
        ConstraintVariable retVariable = variableMap.get(value);
        if (retVariable == null) {
            retVariable = new ConstraintVariable(value);
            tempVarSet.add(retVariable);
            variableMap.put(value, retVariable);
        }

        return retVariable;
    }

    public Set<ConstraintVariable> getTempVarSet() {
        return tempVarSet;
    }

    public void setTempVarSet(Set<ConstraintVariable> tempVarSet) {
        this.tempVarSet = tempVarSet;
    }

    public void clearTempVariable() {
        for (ConstraintVariable variable : tempVarSet)
            clearTempVariable(variable);
    }

    private void clearTempVariable(ConstraintVariable clearedVar) {
        Set<Local> keySet = variableMap.keySet();
        Iterator<Local> ite = keySet.iterator();
        while (ite.hasNext()) {
            Local local = ite.next();
            if (variableMap.get(local) == clearedVar)
                ite.remove();
        }

        for (ConstraintVariable variable : variableMap.values()) {
            Set<BasicConstraintGraphNode> predKeySet = variable.getPreds().keySet();
            if (predKeySet.contains(clearedVar))
                predKeySet.remove(clearedVar);
            Set<BasicConstraintGraphNode> succKeySet = variable.getSuccs().keySet();
            if (succKeySet.contains(clearedVar))
                succKeySet.remove(clearedVar);
        }
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
        // clear LS result after changing the constraint graph
        if (!LSMap.keySet().isEmpty())
            LSMap.clear();

        if (pred instanceof ConstraintVariable) {
            ConstraintVariable predVar = (ConstraintVariable) pred;
            if (succ instanceof ConstraintVariable) {
                ConstraintVariable succVar = (ConstraintVariable) succ;
//                if (predVar.getOrder() < succVar.getOrder()) {
//                    succVar.addToPreds(predVar, annotation);
//                    checkTransInSuccs(succVar, predVar, annotation);
//                } else {
//                    predVar.addToSuccs(succVar, annotation);
//                    checkTransInPreds(predVar, succVar, annotation);
//                }
                succVar.addToPreds(predVar, annotation);
                predVar.addToSuccs(succVar, annotation);
                checkTransInSuccs(succVar, predVar, annotation);
                checkTransInPreds(predVar, succVar, annotation);
            } else {
                {
                    predVar.addToSuccs(succ, annotation);
                    checkTransInPreds(predVar, succ, annotation);
                }
            }
        } else if (succ instanceof ConstraintVariable) {
            ConstraintVariable succVar = (ConstraintVariable) succ;
            succVar.addToPreds(pred, annotation);
            checkTransInSuccs(succVar, pred, annotation);
        } else { // non-atomic constraint
            resolve(pred, succ, annotation);
        }
    }

    private void resolve(BasicConstraintGraphNode pred, BasicConstraintGraphNode succ, ConstraintAnnotation annotation) {
        if (pred instanceof ConstraintConstructor && succ instanceof ConstraintConstructor) {
            // TODO: resolve
        }
    }

    private void trans(BasicConstraintGraphNode pred, ConstraintAnnotation predAnnotation, BasicConstraintGraphNode succ, ConstraintAnnotation succAnnotation) {
        ConstraintAnnotation newAnnotation = getMatchedAnnotation(predAnnotation, succAnnotation);
        if (newAnnotation != null) {
            addToGraph(pred, succ, newAnnotation);
        }
    }

    private void checkTransInPreds(ConstraintVariable variable, BasicConstraintGraphNode succ, ConstraintAnnotation succAnnotation) {
        Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> preds = variable.getPreds();
        for (BasicConstraintGraphNode pred : preds.keySet()) {
            for (ConstraintAnnotation predAnnotation : preds.get(pred)) {
                trans(pred, predAnnotation, succ, succAnnotation);
            }
        }
    }

    private void checkTransInSuccs(ConstraintVariable variable, BasicConstraintGraphNode pred, ConstraintAnnotation predAnnotation) {
        Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> succs = variable.getSuccs();
        for (BasicConstraintGraphNode succ : succs.keySet()) {
            Set<ConstraintAnnotation> succAnnotationSet = succs.get(succ);
            for (ConstraintAnnotation succAnnotation : succAnnotationSet) {
                trans(pred, predAnnotation, succ, succAnnotation);
            }
        }
    }

    private ConstraintAnnotation getMatchedAnnotation(ConstraintAnnotation predAnnotation, ConstraintAnnotation succAnnotation) {
        ConstraintAnnotation newAnnotation = null;
        if (predAnnotation == ConstraintAnnotation.EMPTY) {
            if (succAnnotation == ConstraintAnnotation.EMPTY)
                newAnnotation = ConstraintAnnotation.EMPTY;
            else
                newAnnotation = succAnnotation.getClone();
        } else if (succAnnotation == ConstraintAnnotation.EMPTY) {
            newAnnotation = predAnnotation.getClone();
        } else if (predAnnotation.equals(succAnnotation)) {
            newAnnotation = ConstraintAnnotation.EMPTY;
        }
        return newAnnotation;
    }
}
