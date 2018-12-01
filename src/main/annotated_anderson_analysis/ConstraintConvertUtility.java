package annotated_anderson_analysis;

import annotated_anderson_analysis.constraint_graph_node.ConstraintObjectConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintVariable;
import soot.Local;
import soot.Value;
import soot.jimple.*;

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
                } else if (rightOp instanceof FieldRef) {
                    processLocalToFieldRef((Local) leftOp, (FieldRef) rightOp, constraintGraph);
                } else if (rightOp instanceof VirtualInvokeExpr) {
                    processLocalToVirtualInvoke((Local) leftOp, (VirtualInvokeExpr) rightOp, constraintGraph);
                }
            } else if (leftOp instanceof FieldRef) {
                if (rightOp instanceof Local) {
                    processFieldRefToLocal((FieldRef) leftOp, (Local) rightOp, constraintGraph);
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

    private static void processLocalToFieldRef(Local left, FieldRef right, ConstraintGraph constraintGraph) {

    }

    private static void processLocalToVirtualInvoke(Local left, VirtualInvokeExpr right, ConstraintGraph constraintGraph) {

    }

    private static void processFieldRefToLocal(FieldRef left, Local right, ConstraintGraph constraintGraph) {

    }

}
