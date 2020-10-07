package cc.eumc.easylogin.instance.jsonbase.versionmeta;

// ----------------------------------
public class Library {
    Downloads downloads;
    String name;
    Natives natives;
    Rule[] rules;

    public Downloads getDownloads() {
        return downloads;
    }

    public String getName() {
        return name;
    }

    public Natives getNatives() {
        return natives;
    }

    public Rule[] getRules() {
        return rules;
    }
}
