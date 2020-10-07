package cc.eumc.easylogin.instance.jsonbase.versionmeta;

import java.util.Map;

public class MCVersionMeta {
    Argument arguments;
    String minecraftArguments;
    AssetIndex assetIndex;
    String assets;
    DownloadTypes downloads;
    String id;
    Library[] libraries;
    String mainClass;
    int minimumLauncherVersion;
    String releaseTime;
    String time;
    String type;

    public Argument getArguments() {
        return arguments;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public AssetIndex getAssetIndex() {
        return assetIndex;
    }

    public String getAssets() {
        return assets;
    }

    public DownloadTypes getDownloads() {
        return downloads;
    }

    public String getId() {
        return id;
    }

    public Library[] getLibraries() {
        return libraries;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }
}

/*enum ReleaseType {
    release,
}*/


