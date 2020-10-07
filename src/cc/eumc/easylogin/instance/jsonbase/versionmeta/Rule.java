package cc.eumc.easylogin.instance.jsonbase.versionmeta;

public class Rule {
    String action;  // allow \ disallow
    OS os;

    public String getAction() {
        return action;
    }

    public OS getOs() {
        return os;
    }
}
