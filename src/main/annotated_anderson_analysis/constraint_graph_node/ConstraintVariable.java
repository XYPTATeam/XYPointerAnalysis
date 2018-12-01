package annotated_anderson_analysis.constraint_graph_node;

import annotated_anderson_analysis.ConstraintAnnotation;
import soot.Local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstraintVariable extends BasicConstraintGraphNode {
    private Local local;
    private int order;
    private Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> preds;
    private Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> succs;

    public ConstraintVariable(Local local, int order) {
        this.local = local;
        this.order = order;
        preds = new HashMap<>();
        succs = new HashMap<>();
    }

    public Local getLocal() {
        return local;
    }

    public int getOrder() {
        return this.order;
    }

    public Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> getPreds() {
        return this.preds;
    }

    public Map<BasicConstraintGraphNode, Set<ConstraintAnnotation>> getSuccs() {
        return this.succs;
    }

    public void addToPreds(BasicConstraintGraphNode basicNode, ConstraintAnnotation annotation) {
        Set predSet = preds.get(basicNode);
        if (predSet == null) {
            predSet = new HashSet();
            preds.put(basicNode, predSet);
        }

        predSet.add(annotation);
    }

    public void addToSuccs(BasicConstraintGraphNode basicNode, ConstraintAnnotation annotation) {
        Set succSet = succs.get(basicNode);
        if (succSet == null) {
            succSet = new HashSet();
            succs.put(basicNode, succSet);
        }

        succSet.add(annotation);
    }
}
