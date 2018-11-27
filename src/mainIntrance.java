import soot.*;
import soot.jimple.spark.SparkTransformer;

import java.io.File;
import java.util.HashMap;
public class mainIntrance {
    public static void main(String[] args){
        String classpath = args[0]  + "rt.jar" + File.pathSeparator
                + args[0] + "jce.jar" + File.pathSeparator
                + args[1];
        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        System.out.println(classpath);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.mypta", new WholeProgramTransformer()));
        soot.Main.main(new String[]{
                "-w",
                "-p", "cg.spark", "enabled:true",
                "-p", "wjtp.mypta", "enabled:true",
                "-soot-class-path", classpath,
                args[2]
        });
    }
}
