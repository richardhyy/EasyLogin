package cc.eumc.easylogin.util;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;

public class FileOperator {
    public static void writeStringToFile(File file, String str) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = new FileWriter(file);
        fw.write(str);
        fw.flush();
    }

    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    public static String calcSHA1(File file) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }
            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }

    public static String calcSHA1(byte[] bytes) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return new HexBinaryAdapter().marshal(sha1.digest(bytes));
    }
}
