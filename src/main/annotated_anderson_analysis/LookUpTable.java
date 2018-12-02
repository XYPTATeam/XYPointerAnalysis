package annotated_anderson_analysis;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

import java.util.*;

class lookUpItem {
    String sig;
    String method;
    String inClass;

    lookUpItem(String method, String sig, String inclass) {
        this.sig = sig;
        this.method = method;
        this.inClass = inclass;
    }
};

class lookUpClass {
    String classname;
    String inheritClass;
    Set<lookUpItem> lookUpList;

    lookUpClass(String classname, String inheritClass) {
        this.classname = classname;
        this.inheritClass = inheritClass;
        this.lookUpList = new HashSet<>();
    }
};


public class LookUpTable {
    private static Map<String, lookUpClass> classMap = new HashMap();

    public static void construct(Chain<SootClass> sClasses) {
        Iterator classIt = sClasses.iterator();
        while (classIt.hasNext()) {
            SootClass sClass = (SootClass) classIt.next();
            String superClass = "";
            String classname = sClass.getName();
            if (sClasses.contains(sClass.getSuperclass()))
                superClass = sClass.getSuperclass().toString();
            List<SootMethod> sMethods = sClass.getMethods();
            Iterator methodIt = sMethods.iterator();
            while (methodIt.hasNext()) {
                SootMethod sMethod = (SootMethod) methodIt.next();
                String methodname = sMethod.getName();
                lookUpClass lookupclass = classMap.get(classname);
                if (lookupclass == null) {
                    lookupclass = new lookUpClass(classname, superClass);
                    classMap.put(classname, lookupclass);
                }
                lookupclass.lookUpList.add(new lookUpItem(methodname, sMethod.getSignature(), sClass.getName()));
            }
        }
//        System.out.println("End Building LookUpTable.");
    }

    public static lookUpItem search(VirtualInvokeExpr invokeExpr) {
        String sig = invokeExpr.getMethod().toString();
        lookUpClass lc = classMap.get(invokeExpr.getClass());
        for (lookUpItem li : lc.lookUpList) {
            if (li.sig == sig) {
                return li;
            }
        }
        String superclass = lc.inheritClass;
        lc = classMap.get(superclass);
        for (lookUpItem li : lc.lookUpList) {
            if (li.sig == sig) {
                return li;
            }
        }
        return null;
    }
}
