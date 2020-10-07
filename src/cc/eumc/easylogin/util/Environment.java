package cc.eumc.easylogin.util;

import cc.eumc.easylogin.instance.jsonbase.versionmeta.OS;
import cc.eumc.easylogin.instance.jsonbase.versionmeta.OSType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    public static File getJavaExecutable(File jvmHome) {
        OS currentOS = OS.getCurrentOS();
        String prefix = "";
        String suffix = "";

        if (currentOS.getName() == OSType.osx) {
            prefix = "Contents/Home/bin/";
        } else if (currentOS.getName() == OSType.windows) {
            prefix = "bin/";
            suffix = ".exe";
        }

        return new File(jvmHome, prefix + "java" + suffix);
    }

    /**
     *
     * @return e.g. jdk-12.0.1.jdk | /Library/Java/JavaVirtualMachines/jdk-12.0.1.jdk
     *              jdk1.8.0_221.jdk | /Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk
     */
    public static Map<String, File> getJvmHomeList() {
        OS currentOS = OS.getCurrentOS();
        String jvmHome = System.getProperty("java.home");
        File jvmContainer;

        Map<String, File> jvmHomeList = new HashMap<>();

        switch (currentOS.getName()) {
            case osx:
                // e.g. "/Library/Java/JavaVirtualMachines/jdk-12.0.0.jdk/Contents/Home"
                jvmContainer = new File(jvmHome.replace("/Contents/Home/jre", ""));
                break;

            case linux:
            case windows:
                jvmContainer = new File(jvmHome);
                break;

            default:
                // Unsupported operation system
                jvmHomeList.put(System.getProperty("java.version"), new File(jvmHome));
                return jvmHomeList;
        }

        jvmContainer = jvmContainer.getParentFile();
        String[] jvmVersionArray = jvmContainer.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        for (String versionStr : jvmVersionArray) {
            File javaHome = new File(jvmContainer, versionStr);
            if (getJavaExecutable(javaHome).exists()) {
                jvmHomeList.put(versionStr, javaHome);
            }
        }

        return jvmHomeList;
    }
}
