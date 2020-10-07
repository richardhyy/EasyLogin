package cc.eumc.easylogin.instance.jsonbase.versionmanifest;

public class MCVersionEntry {
        /*
    {"id": "20w20b",
    "type": "snapshot",
    "url": "https://launchermeta.mojang.com/v1/packages/0d92bce6def6ae837cae0b1897ac0f02a5223ed5/20w20b.json",
    "time": "2020-05-14T08:34:20+00:00",
    "releaseTime": "2020-05-14T08:16:26+00:00"}
     */
    String id;
    MCVersionType type;
    String url;
    String time;
    String releaseTime;

    public String getId() {
        return id;
    }

    public MCVersionType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getTime() {
        return time;
    }

    public String getReleaseTime() {
        return releaseTime;
    }
}
