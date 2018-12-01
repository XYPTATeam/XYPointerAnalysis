package cfg;

import org.junit.Test;
import soot.*;
import soot.util.Chain;

import java.util.List;
import java.util.Map;

public class cfgTest {
    @Test
    public void testCFG() {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new SceneTransformer() {
                    protected void internalTransform(String phaseName,
                                                     Map options) {
                        System.err.println(Scene.v().getApplicationClasses());
                        Chain<SootClass> classes = Scene.v().getApplicationClasses();
                        for (SootClass sootClass : classes) {
                            System.err.println("Class: " + sootClass.getName());
                            List<SootMethod> methodList = sootClass.getMethods();
                            for (SootMethod method : methodList) {
                                System.err.println("\tMethod: " + method.getName());
                                System.err.println("\t" + method.getSignature());
                                Body body = method.getActiveBody();
                                System.err.println("\t" + body);
                            }
                        }

                    }
                }));

        String[] sootArgs = new String[10];
        String testDir = null;
        String testClass = null;

        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;

        soot.Main.main(sootArgs);
    }
}
