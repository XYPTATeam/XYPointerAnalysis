package annotated_anderson_analysis;

import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JimpleLocalBox;
import soot.util.Chain;

import java.util.*;

class lookUpItem{
    String sig;
    String method;
    String inClass;
    lookUpItem(String method, String sig, String inclass){
        this.sig = sig;
        this.method = method;
        this.inClass = inclass;
    }
};

class lookUpClass {
    String classname;
    String inheritClass;
    Set<lookUpItem> lookUpList;
    lookUpClass(String classname, String inheritClass){
        this.classname = classname;
        this.inheritClass = inheritClass;
        this.lookUpList = new HashSet<lookUpItem>();
    }
};


public class LoopUpTableConstructor {
    static Map<String, lookUpClass> loopUpTable = new HashMap();
    public void construct() {
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
            protected void internalTransform(String phaseName,
                                             Map options){
                Chain<SootClass> sClasses = Scene.v().getApplicationClasses();
                Iterator classIt = sClasses.iterator();
                while (classIt.hasNext()) {
                    SootClass sClass = (SootClass) classIt.next();
                    String superClass = "";
                    String classname = sClass.getName();
                    if(sClasses.contains(sClass.getSuperclass()))
                        superClass = sClass.getSuperclass().toString();
                    List<SootMethod> sMethods = sClass.getMethods();
                    Iterator methodIt = sMethods.iterator();
                    while (methodIt.hasNext()) {
                        SootMethod sMethod = (SootMethod) methodIt.next();
                        String methodname = sMethod.getName();
                        lookUpClass lookupclass = loopUpTable.get(classname);
                        if(lookupclass == null){
                            lookupclass = new lookUpClass(classname, superClass);
                            loopUpTable.put(classname, lookupclass);
                        }
                        lookupclass.lookUpList.add(new lookUpItem(methodname, sMethod.getSignature(), sClass.getName()));
                    }
                }
                System.out.println("End Building LookUpTable.");
            }
        }));



        String testDir = getClass().getResource("/").getPath();
        String testClass = "Hello";
        String[] sootArgs = new String[5];
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }
}
