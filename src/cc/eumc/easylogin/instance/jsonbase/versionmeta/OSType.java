package cc.eumc.easylogin.instance.jsonbase.versionmeta;

public enum OSType {
    osx,
    windows,
    linux,
    other;

    /**
     * detect the operating system from the os.name System property and return the result
     * @param friendlyOSName String get from os.name System property
     * @return OSType
     */
    public static OSType fromName(String friendlyOSName) {
        String lowerCase = friendlyOSName.toLowerCase();
        if ((lowerCase.contains("osx")) || (lowerCase.contains("mac")) || (lowerCase.contains("darwin"))) {
            return osx;
        } else if (lowerCase.contains("win")) {
            return windows;
        } else if (lowerCase.contains("nix") || lowerCase.contains("nux")) {
            return linux;
        } else {
            return other;
        }
    }
}
