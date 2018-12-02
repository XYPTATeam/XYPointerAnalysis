import annotated_anderson_analysis.ConstraintConvertUtility;
import annotated_anderson_analysis.ConstraintGraph;
import annotated_anderson_analysis.LookUpTable;
import soot.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.Chain;
import soot.util.queue.QueueReader;

import java.util.Map;

public class PointerAnalysisTransformer extends SceneTransformer {
    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        Chain<SootClass> classes = Scene.v().getApplicationClasses();
        LookUpTable.construct(classes);

        ReachableMethods rechableMethods = Scene.v().getReachableMethods();
        QueueReader<MethodOrMethodContext> qr = rechableMethods.listener();
        while (qr.hasNext()) {
            SootMethod sm = qr.next().method();
            if (sm.getSubSignature().equals("void main(java.lang.String[])")) {
                ConstraintGraph constraintGraph = new ConstraintGraph();
                ConstraintConvertUtility.analysisMain(sm, constraintGraph);
                System.out.println("Analysis finished.");
            }
        }
    }
}
