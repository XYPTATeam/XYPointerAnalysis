package deprecated;

import soot.Local;

import java.util.*;

public class Anderson {
    private List<AssignConstraint> assignConstraintList = new ArrayList<AssignConstraint>();
    private List<NewConstraint> newConstraintList = new ArrayList<NewConstraint>();
    Map<Local, TreeSet<Integer>> pts = new HashMap<Local, TreeSet<Integer>>();

    void addAssignConstraint(Local from, Local to) {
        assignConstraintList.add(new AssignConstraint(from, to));
    }

    void addNewConstraint(int allocID, Local to) {
        newConstraintList.add(new NewConstraint(allocID, to));
    }

    void run() {
        for (NewConstraint nc : newConstraintList) {
            if (!pts.containsKey(nc.to)) {
                pts.put(nc.to, new TreeSet<Integer>());
            }
            pts.get(nc.to).add(nc.allocID);
        }
        boolean flag = true;
        while (flag == true) {
            flag = false;
            for (AssignConstraint ac : assignConstraintList) {
                if (!pts.containsKey(ac.from)) {
                    continue;
                }
                if (!pts.containsKey(ac.to)) {
                    pts.put(ac.to, new TreeSet<Integer>());
                }
                if (pts.get(ac.to).addAll(pts.get(ac.from))) {
                    flag = true;
                }
            }
        }
    }

    TreeSet<Integer> getPointsToSet(Local local) {
        return pts.get(local);
    }
}

class AssignConstraint {
    Local from, to;

    AssignConstraint(Local from, Local to) {
        this.from = from;
        this.to = to;
    }
}

class NewConstraint {
    Local to;
    int allocID;

    NewConstraint(int allocID, Local to) {
        this.allocID = allocID;
        this.to = to;
    }
}