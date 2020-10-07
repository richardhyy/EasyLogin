package cc.eumc.easylogin.jvm;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.util.Environment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JvmManager {
    EasyLogin instance;
    Map<String, File> jvmList = new HashMap<>();

    public JvmManager(EasyLogin instance) {
        jvmList = Environment.getJvmHomeList(); // its size's supposed to be at least 1

    }

    public static void createProcess(File jvmHome, List<String> args) throws IOException {
        System.out.println(Environment.getJavaExecutable(jvmHome).getAbsolutePath());
        args.add(0, Environment.getJavaExecutable(jvmHome).getAbsolutePath());
        System.out.println(String.join(" ", args));
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();
    }
}
