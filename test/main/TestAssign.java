import org.junit.Test;
import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import java.util.Map;

public class TestAssign {
    @Test
    public void testAssign() {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new SceneTransformer() {
                    protected void internalTransform(String phaseName,
                                                     Map options) {
                        ReachableMethods rechableMethods = Scene.v().getReachableMethods();
                        QueueReader<MethodOrMethodContext> qr = rechableMethods.listener();
                        while (qr.hasNext()) {
                            SootMethod sm = qr.next().method();
                            if (sm.toString().startsWith("<java") || sm.toString().startsWith("<sun")) {
                                qr.remove();
                            }
                        }

                        qr = rechableMethods.listener();
                        while (qr.hasNext()) {
                            MethodOrMethodContext mc = qr.next();
                            SootMethod sm = mc.method();
                            int allocID = 0;
                            if (sm.hasActiveBody()) {
                                for (Unit u : sm.getActiveBody().getUnits()) {
                                    if (u instanceof DefinitionStmt) {
                                        DefinitionStmt du = (DefinitionStmt) u;
                                        System.out.println(du);
                                    }
                                }
                            }
                        }
                    }
                }));

        String[] sootArgs = new String[5];
        String testDir = getClass().getResource("/").getPath();
        String testClass = "Assign";
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }

    @Test
    public void testAnalysis() {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new PointerAnalysisTransformer()));

        String[] sootArgs = new String[5];
        String testDir = getClass().getResource("/").getPath();
        String testClass = "Assign";
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }
}
