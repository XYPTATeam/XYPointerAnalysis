import soot.PackManager;
import soot.Transform;

public class MainEntrance {
    public static void main(String[] args) {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new PointerAnalysisTransformer()));

        String[] sootArgs = new String[5];
        String testDir = args[0];
        String testClass = args[1];
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }
}
