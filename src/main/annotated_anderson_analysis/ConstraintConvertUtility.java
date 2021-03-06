package annotated_anderson_analysis;

import annotated_anderson_analysis.constraint_graph_node.BasicConstraintGraphNode;
import annotated_anderson_analysis.constraint_graph_node.ConstraintConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintObjectConstructor;
import annotated_anderson_analysis.constraint_graph_node.ConstraintVariable;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class ConstraintConvertUtility {
    private static int allocID = 0;
    private static TreeMap<Integer, Local> queries = new TreeMap<>();
    private static Local thisLocal = null;

    public static void analysisMain(SootMethod mainMethod, ConstraintGraph constraintGraph) {
        Body body = mainMethod.getActiveBody();
        List<Value> paramList = new ArrayList<>();
        paramList.add(new JimpleLocal("args", mainMethod.getParameterType(0)));
        for (Unit u : body.getUnits()) {
            convertFromStmt(u, null, paramList, new HashSet<>(), constraintGraph);
        }

        // print result
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("result.txt"));
            for (Integer idx : queries.keySet()) {
                Local testLocal = queries.get(idx);
                ConstraintVariable testVar = constraintGraph.getFromVariableMap(testLocal);
                Map<ConstraintConstructor, Set<ConstraintAnnotation>> resMap = constraintGraph.getLS(testVar);

                // TODO: print result
                ps.print("Query " + idx + " (" + testLocal + ") " + "-- {");
                for (ConstraintConstructor constructor : resMap.keySet()) {
                    Set<ConstraintAnnotation> resAnnoSet = resMap.get(constructor);
                    boolean printFlag = false;
                    for (ConstraintAnnotation annotation : resAnnoSet) {
//                        ps.print(annotation + ", ");
                        if (annotation == ConstraintAnnotation.EMPTY) {
                            printFlag = true;
                            break;
                        }
                    }
                    if (printFlag)
                        ps.print(constructor + ", ");
//                    ps.println("]");
                }
                ps.println("}");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static Set<Local> analysisInFuncInvoke(InvokeExpr invokeExpr, ConstraintGraph constraintGraph) {
        Set<Local> retLocalSet = new HashSet<>();

        List<Value> actualParamList = invokeExpr.getArgs();
        Local oriLocal = thisLocal;
        Set<ConstraintVariable> oriTempVarSet = constraintGraph.getTempVarSet();
        constraintGraph.setTempVarSet(new HashSet<>());

        // get this variable
        if (invokeExpr instanceof InstanceInvokeExpr) {
            Value baseValue = ((InstanceInvokeExpr) invokeExpr).getBase();
            // TODO: recursively function invoking
            if (baseValue instanceof Local)
                thisLocal = ((Local) baseValue);
        }

        // get runtime method
        SootMethod method = invokeExpr.getMethod();
        if (invokeExpr instanceof VirtualInvokeExpr) {
            VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
            String methodSig = virtualInvokeExpr.getMethod().getSubSignature();

            Value baseValue = virtualInvokeExpr.getBase();
            if (baseValue instanceof Local) {
                ConstraintVariable baseVar = constraintGraph.getFromVariableMap((Local) baseValue);
                Map<ConstraintConstructor, Set<ConstraintAnnotation>> LSMap = constraintGraph.getLS(baseVar);
                for (ConstraintConstructor constructor : LSMap.keySet()) {
                    if (constructor instanceof ConstraintObjectConstructor && LSMap.get(constructor).contains(ConstraintAnnotation.EMPTY)) {
                        method = LookUpTable.search(((ConstraintObjectConstructor) constructor).getRefType(), methodSig);
                        if (method != null && method.hasActiveBody()) {
                            Body activeBody = method.getActiveBody();
                            for (Unit u : activeBody.getUnits()) {
                                convertFromStmt(u, invokeExpr, actualParamList, retLocalSet, constraintGraph);
                            }
                        }
                    }
                }
            }
        }
        // process in active body
        else if (method != null && method.hasActiveBody()) {
            Body activeBody = method.getActiveBody();
            for (Unit u : activeBody.getUnits()) {
                convertFromStmt(u, invokeExpr, actualParamList, retLocalSet, constraintGraph);
            }
        }

        // TODO: pop up local variable
        constraintGraph.clearTempVariable();
        constraintGraph.setTempVarSet(oriTempVarSet);
        thisLocal = oriLocal;
        return retLocalSet;
    }

    private static void convertFromStmt(Unit u, InvokeExpr invokeExpr, List<Value> actualParamList, Set<Local> retLocalSet, ConstraintGraph constraintGraph) {
        if (u instanceof InvokeStmt) {
            convertFromInvokeStmt((InvokeStmt) u, constraintGraph);
        } else if (u instanceof IdentityStmt) {
            convertFromIdentityStmt((IdentityStmt) u, actualParamList, constraintGraph);
        } else if (u instanceof AssignStmt) {
            convertFromAssignStmt((AssignStmt) u, constraintGraph);
        } else if (u instanceof ReturnStmt) {
            convertFromReturnStmt((ReturnStmt) u, invokeExpr, retLocalSet, constraintGraph);
        } else if (u instanceof IfStmt) {
            convertFromStmt(((IfStmt) u).getTarget(), invokeExpr, actualParamList, retLocalSet, constraintGraph);
        }
    }

    private static void convertFromInvokeStmt(InvokeStmt stmt, ConstraintGraph constraintGraph) {
        InvokeExpr ie = stmt.getInvokeExpr();
        SootMethod method = ie.getMethod();
        if (method != null) {
            String methodSig = method.toString();
            if (methodSig.equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
                allocID = ((IntConstant) ie.getArgs().get(0)).value;
            } else if (methodSig.equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
                Value v = ie.getArgs().get(1);
                int id = ((IntConstant) ie.getArgs().get(0)).value;
                queries.put(id, (Local) v);
            } else {
                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                // FIXME
//                if (!(invokeExpr instanceof SpecialInvokeExpr))
                analysisInFuncInvoke(invokeExpr, constraintGraph);
            }
        }
    }

    public static void convertFromIdentityStmt(IdentityStmt stmt, List<Value> paramList, ConstraintGraph constraintGraph) {
        Value leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();

        if (leftOp instanceof Local) {
            if (rightOp instanceof ParameterRef) {
                int rightIndex = ((ParameterRef) rightOp).getIndex();
                Value rightVal = paramList.get(rightIndex);
                processAssignToLocal((Local) leftOp, rightVal, constraintGraph);
            } else if (rightOp instanceof ThisRef) {
                processThisIdentity((Local) leftOp, thisLocal, constraintGraph);
            }
        }
    }

    public static void convertFromAssignStmt(AssignStmt stmt, ConstraintGraph constraintGraph) {
        Value leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();

        if (leftOp instanceof Local) {
            processAssignToLocal((Local) leftOp, rightOp, constraintGraph);
        } else if (leftOp instanceof InstanceFieldRef) {
            if (rightOp instanceof Local) {
                processFieldRefToLocal((InstanceFieldRef) leftOp, (Local) rightOp, constraintGraph);
            }
        }
    }

    public static void convertFromReturnStmt(ReturnStmt stmt, InvokeExpr invokeExpr, Set<Local> retLocalSet, ConstraintGraph constraintGraph) {
        Value retVal = stmt.getOp();
        Type retType = invokeExpr.getMethodRef().getReturnType();
        Local retLocal = new JimpleLocal("return_local", retType);
        retLocalSet.add(retLocal);
        processAssignToLocal(retLocal, retVal, constraintGraph);
    }

    private static void processAssignToLocal(Local leftOp, Value rightOp, ConstraintGraph constraintGraph) {
        if (rightOp instanceof NewExpr) {
            processLocalToNew(leftOp, (NewExpr) rightOp, allocID, constraintGraph);
        } else if (rightOp instanceof Local) {
            processLocalToLocal(leftOp, (Local) rightOp, constraintGraph);
        } else if (rightOp instanceof InstanceFieldRef) {
            processLocalToFieldRef(leftOp, (InstanceFieldRef) rightOp, constraintGraph);
        } else if (rightOp instanceof InvokeExpr) {
            processLocalToInvoke(leftOp, (InvokeExpr) rightOp, constraintGraph);
        }
    }

    private static void processThisIdentity(Local left, Local right, ConstraintGraph constraintGraph) {
        ConstraintVariable leftVar = new ConstraintVariable(left, 3);
        constraintGraph.addToVariableMap(left, leftVar);
        ConstraintVariable rightVar = constraintGraph.getFromVariableMap(right);
        constraintGraph.addToGraph(rightVar, leftVar, ConstraintAnnotation.EMPTY);
    }

    private static void processLocalToNew(Local left, NewExpr right, int allocID, ConstraintGraph constraintGraph) {
        ConstraintObjectConstructor constructor = new ConstraintObjectConstructor(allocID, right.getBaseType());
        ConstraintVariable variable = constraintGraph.getFromVariableMap(left);
        constraintGraph.addToGraph(constructor, variable, ConstraintAnnotation.EMPTY);
    }

    private static void processLocalToLocal(Local left, Local right, ConstraintGraph constraintGraph) {
        ConstraintVariable leftVar = constraintGraph.getFromVariableMap(left);
        ConstraintVariable rightVar = constraintGraph.getFromVariableMap(right);
        constraintGraph.addToGraph(rightVar, leftVar, ConstraintAnnotation.EMPTY);
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

    private static void processLocalToInvoke(Local left, InvokeExpr right, ConstraintGraph constraintGraph) {
        Set<Local> retLocalSet = analysisInFuncInvoke(right, constraintGraph);
        for (Local retLocal : retLocalSet)
            processAssignToLocal(left, retLocal, constraintGraph);
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
