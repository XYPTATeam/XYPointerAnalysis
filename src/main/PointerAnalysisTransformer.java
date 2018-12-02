import annotated_anderson_analysis.ConstraintConvertUtility;
import annotated_anderson_analysis.ConstraintGraph;
import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.Map;
import java.util.TreeMap;

public class PointerAnalysisTransformer extends SceneTransformer {
    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        TreeMap<Integer, Local> queries = new TreeMap<Integer, Local>();
        ReachableMethods rechableMethods = Scene.v().getReachableMethods();
        QueueReader<MethodOrMethodContext> qr = rechableMethods.listener();
        ConstraintGraph constraintGraph = new ConstraintGraph();
        while (qr.hasNext()) {
            SootMethod sm = qr.next().method();
            int allocID = 0;
            if (sm.hasActiveBody()) {
                for (Unit u : sm.getActiveBody().getUnits()) {
                    if (u instanceof InvokeStmt) {
                        InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                        try {
                            if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
                                allocID = ((IntConstant) ie.getArgs().get(0)).value;
                            }
                            if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
                                Value v = ie.getArgs().get(1);
                                int id = ((IntConstant) ie.getArgs().get(0)).value;
                                queries.put(id, (Local) v);
                            }
                        } catch (SootMethodRefImpl.ClassResolutionFailedException e) {
                            continue;
                        }
                    }
                    if (u instanceof DefinitionStmt) {
                        DefinitionStmt du = (DefinitionStmt) u;
//                        ConstraintConvertUtility.convertFromAssignStmt(du, allocID, constraintGraph);
                    }
                }
            }
        }
    }
}
