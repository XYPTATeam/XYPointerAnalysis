package annotated_anderson_analysis;

import annotated_anderson_analysis.constraint_graph_node.BasicConstraintGraphNode;
import annotated_anderson_analysis.constraint_graph_node.ConstraintObjectConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintVariable;
import soot.Local;
import soot.SootFieldRef;
import soot.Value;
import soot.jimple.*;

import java.util.Map;
import java.util.Set;

public class ConstraintConvertUtility {
    public static void convertFromDefinitionStmt(DefinitionStmt stmt, int allocID, ConstraintGraph constraintGraph) {
        if (stmt instanceof AssignStmt) {
            Value leftOp = stmt.getLeftOp();
            Value rightOp = stmt.getRightOp();

            if (leftOp instanceof Local) {
                if (rightOp instanceof NewExpr) {
                    processLocalToNew((Local) leftOp, (NewExpr) rightOp, allocID, constraintGraph);
                } else if (rightOp instanceof Local) {
                    processLocalToLocal((Local) leftOp, (Local) rightOp, constraintGraph);
                } else if (rightOp instanceof InstanceFieldRef) {
                    processLocalToFieldRef((Local) leftOp, (InstanceFieldRef) rightOp, constraintGraph);
                } else if (rightOp instanceof VirtualInvokeExpr) {
                    processLocalToVirtualInvoke((Local) leftOp, (VirtualInvokeExpr) rightOp, constraintGraph);
                }
            } else if (leftOp instanceof InstanceFieldRef) {
                if (rightOp instanceof Local) {
                    processFieldRefToLocal((InstanceFieldRef) leftOp, (Local) rightOp, constraintGraph);
                }
            }
        } else if (stmt instanceof InvokeStmt) {

        }
    }

    private static void processLocalToNew(Local left, NewExpr right, int allocID, ConstraintGraph constraintGraph) {
        ConstraintObjectConstructor constructor = new ConstraintObjectConstructor(allocID, right.getBaseType());
        ConstraintVariable variable = constraintGraph.getFromVariableMap(left);
        constraintGraph.addToGraph(constructor, variable, ConstraintAnnotation.EMPTY);
    }

    private static void processLocalToLocal(Local left, Local right, ConstraintGraph constraintGraph) {
        ConstraintVariable leftVar = constraintGraph.getFromVariableMap(left);
        ConstraintVariable rightVar = constraintGraph.getFromVariableMap(right);
        constraintGraph.addToGraph(leftVar, rightVar, ConstraintAnnotation.EMPTY);
    }

    private static void processLocalToFieldRef(Local left, InstanceFieldRef right, ConstraintGraph constraintGraph) {
        ConstraintVariable leftVar = constraintGraph.getFromVariableMap(left);
        Value rightBaseValue = right.getBase();

        // TODO: multi-level field reference
        if (rightBaseValue instanceof Local) {
            ConstraintVariable rightBaseVar = constraintGraph.getFromVariableMap((Local) rightBaseValue);
            Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> preds = rightBaseVar.getPreds();
            for (BasicConstraintGraphNode pred : preds.keySet()) {
                ConstraintVariable freshVar = null;
                if (pred instanceof ConstraintObjectConstructor) {
                    ConstraintObjectConstructor predObj = (ConstraintObjectConstructor) pred;
                    for (ConstraintAnnotation predAnnotation : preds.get(pred)) {
                        if (freshVar == null)
                            freshVar = new ConstraintVariable(null, 2);
                        if (predAnnotation != ConstraintAnnotation.EMPTY)
                            constraintGraph.addToGraph(predObj.getObjectVariable(), freshVar, predAnnotation.getClone());
                        else
                            constraintGraph.addToGraph(predObj.getObjectVariable(), freshVar, ConstraintAnnotation.EMPTY);
                    }
                    if (freshVar != null) {
                        SootFieldRef fieldRef = right.getFieldRef();
                        ConstraintAnnotation newAnnotation = new ConstraintAnnotation(fieldRef);
                        constraintGraph.addToGraph(freshVar, leftVar, newAnnotation);
                    }
                }
            }
        }
    }

    private static void processLocalToVirtualInvoke(Local left, VirtualInvokeExpr right, ConstraintGraph constraintGraph) {

    }

    private static void processFieldRefToLocal(InstanceFieldRef left, Local right, ConstraintGraph constraintGraph) {
        ConstraintVariable rightVar = constraintGraph.getFromVariableMap(right);
        Value leftBaseValue = left.getBase();

        // TODO: multi-level field reference
        if (leftBaseValue instanceof Local) {
            ConstraintVariable leftBaseVar = constraintGraph.getFromVariableMap((Local) leftBaseValue);
            Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> preds = leftBaseVar.getPreds();
            for (BasicConstraintGraphNode pred : preds.keySet()) {
                ConstraintVariable freshVar = null;
                if (pred instanceof ConstraintObjectConstructor) {
                    ConstraintObjectConstructor predObj = (ConstraintObjectConstructor) pred;
                    for (ConstraintAnnotation predAnnotation : preds.get(pred)) {
                        if (freshVar == null)
                            freshVar = new ConstraintVariable(null, 2);
                        if (predAnnotation != ConstraintAnnotation.EMPTY)
                            constraintGraph.addToGraph(freshVar, predObj.getObjectVariable(), predAnnotation.getClone());
                        else
                            constraintGraph.addToGraph(freshVar, predObj.getObjectVariable(), ConstraintAnnotation.EMPTY);
                    }
                    if (freshVar != null) {
                        SootFieldRef fieldRef = left.getFieldRef();
                        ConstraintAnnotation newAnnotation = new ConstraintAnnotation(fieldRef);
                        constraintGraph.addToGraph(rightVar, freshVar, newAnnotation);
                    }
                }
            }
        }
    }

}
