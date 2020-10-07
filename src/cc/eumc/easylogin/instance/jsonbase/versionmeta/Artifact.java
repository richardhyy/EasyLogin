package cc.eumc.easylogin.instance.jsonbase.versionmeta;

public class Artifact {
    String path;
    String sha1;
    long size;
    String url;

    public String getPath() {
        return path;
    }

    public String getSha1() {
        return sha1;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }
}
