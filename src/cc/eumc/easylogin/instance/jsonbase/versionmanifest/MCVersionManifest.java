package cc.eumc.easylogin.instance.jsonbase.versionmanifest;

public class MCVersionManifest {
    MCVersionEntry[] versions;

    public MCVersionEntry getEntry(String versionId) {
        for(MCVersionEntry versionEntry : versions) {
            if (versionEntry.id.equals(versionId)) {
                return versionEntry;
            }
        }
        return null;
    }
}
