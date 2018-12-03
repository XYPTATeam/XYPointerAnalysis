import org.junit.Test;
import soot.PackManager;
import soot.Transform;

public class TestPointerAnalysis {
    @Test
    public void testHello() {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new PointerAnalysisTransformer()));

        String[] sootArgs = new String[5];
        String testDir = getClass().getResource("/").getPath();
        String testClass = "Hello";
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }

    @Test
    public void testFieldSensitivity() {
        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.myTransform", new PointerAnalysisTransformer()));

        String[] sootArgs = new String[5];
        String testDir = getClass().getResource("/").getPath();
        String testClass = "FieldSensitivity";
        sootArgs[0] = "-w";
        sootArgs[1] = "-pp";
        sootArgs[2] = "-cp";
        sootArgs[3] = testDir;
        sootArgs[4] = testClass;
        soot.Main.main(sootArgs);
    }
}
