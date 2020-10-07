package cc.eumc.easylogin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgumentBuilder {
    public static List<String> fillArguments(List<String> args, Map<String, String> placeholderFill) {
        for (int i=0; i<args.size(); i++) {
            int indexOfEqualSign = args.get(i).lastIndexOf("=");
            String placeholder = indexOfEqualSign==-1 ? args.get(i) : args.get(i).substring(indexOfEqualSign+1);
            if (placeholderFill.containsKey(placeholder)) {
                args.set(i, args.get(i).replace(placeholder, "") + placeholderFill.get(placeholder));
            }
        }
        return args;
    }
}
