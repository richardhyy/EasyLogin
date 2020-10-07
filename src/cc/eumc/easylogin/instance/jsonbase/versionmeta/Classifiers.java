package cc.eumc.easylogin.instance.jsonbase.versionmeta;

import com.google.gson.annotations.SerializedName;

public class Classifiers {
    Artifact javadoc;

    @SerializedName("natives-linux")
    Artifact natives_linux;

    @SerializedName("natives-macos")
    Artifact natives_macos;

    @SerializedName("natives-windows")
    Artifact natives_windows;

    Artifact source;

    public Artifact getJavadoc() {
        return javadoc;
    }

    public Artifact getNatives_linux() {
        return natives_linux;
    }

    public Artifact getNatives_macos() {
        return natives_macos;
    }

    public Artifact getNatives_windows() {
        return natives_windows;
    }

    public Artifact getSource() {
        return source;
    }
}
