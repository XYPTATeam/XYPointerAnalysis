package annotated_anderson_analysis;

import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LookUpTable {
    private static Map<RefType, Map<String, SootMethod>> classMethodMap = new HashMap<>();

    public static void construct(Chain<SootClass> sClasses) {
        Iterator<SootClass> classIt = sClasses.iterator();
        while (classIt.hasNext()) {
            SootClass sClass = classIt.next();
            List<SootMethod> sMethods = sClass.getMethods();
            Map<String, SootMethod> methodMap = new HashMap<>();
            classMethodMap.put(sClass.getType(), methodMap);
            Iterator<SootMethod> methodIt = sMethods.iterator();
            while (methodIt.hasNext()) {
                SootMethod sMethod;
                sMethod = methodIt.next();
                String methodSig = sMethod.getSubSignature();
                methodMap.put(methodSig, sMethod);
            }
        }
    }

    public static SootMethod search(RefType refType, String subSig) {
        SootMethod retMethod = null;

        Map<String, SootMethod> methodMap = classMethodMap.get(refType);
        if (methodMap != null) {
            retMethod = methodMap.get(subSig);
        }
        return retMethod;
    }
}
