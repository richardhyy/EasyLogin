package cc.eumc.easylogin.localization;

import sun.util.locale.LanguageTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LanguagePack {
    String displayName;
    String introduction;
    String translator;
    Map<String, String> strMap = new HashMap<>();

    public LanguagePack(File langFile) throws IOException {
        String fileContent = new String(Files.readAllBytes(langFile.toPath()));

        this.displayName = fileContent.substring(fileContent.indexOf("<name>"), fileContent.indexOf("</name>") + 1);
        this.introduction = fileContent.substring(fileContent.indexOf("<introduction>"), fileContent.indexOf("</introduction>") + 1);

        String translations = fileContent.substring(fileContent.indexOf("<translation>"), fileContent.indexOf("</translation>") + 1);
        for (String line : translations.split("\n")) {
            if (line.isEmpty()) continue;

            String[] split = line.split(" = ");
            if (split.length != 2) continue;

            for (int i=0; i<=1; i++) {
                split[i] = split[i]
                        .replace(" \\= ", " = ")
                        .replace("\\n", "\n");
            }

            strMap.put(split[0], split[1]);
        }
    }

    public String tr(String originStr) {
        return strMap.getOrDefault(originStr, originStr);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getTranslator() {
        return translator;
    }
}
