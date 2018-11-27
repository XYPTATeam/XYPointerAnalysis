import org.jboss.util.Null;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class WholeProgramTransformer extends SceneTransformer {
    protected void internalTransform(String arg0, Map<String, String> arg1){
        TreeMap<Integer, Local> queries = new TreeMap<Integer, Local>();
        Anderson anderson = new Anderson();
        ReachableMethods rechableMethods = Scene.v().getReachableMethods();
        QueueReader<MethodOrMethodContext> qr = rechableMethods.listener();
        while(qr.hasNext()){
            SootMethod sm = qr.next().method();
            int allocID = 0;
            if(sm.hasActiveBody()){
                for(Unit u: sm.getActiveBody().getUnits()){
                    if(u instanceof InvokeStmt){
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
                        }
                        catch(SootMethodRefImpl.ClassResolutionFailedException e){
                            continue;
                        }
                    }
                    if(u instanceof DefinitionStmt){
                        DefinitionStmt du = (DefinitionStmt)u;
                        if(du.getRightOp() instanceof NewExpr){
                            anderson.addNewConstraint(allocID, (Local)du.getLeftOp());
                        }
                        if(du.getLeftOp() instanceof Local && du.getRightOp() instanceof Local){
                            anderson.addAssignConstraint((Local)du.getRightOp(), (Local)du.getLeftOp());
                        }
                    }
                }
            }
        }
        anderson.run();
        String answer = "";
        for(Map.Entry<Integer, Local> q :queries.entrySet()){
            TreeSet<Integer> result = anderson.getPointsToSet(q.getValue());
            answer += q.getKey().toString() + ":";
            if(result != null) {
                for (Integer i : result) {
                    answer += " " + i;
                }
            }

            answer += "\r\n";
        }
        AnswerPrinter.printAnswer(answer);

    }

}
