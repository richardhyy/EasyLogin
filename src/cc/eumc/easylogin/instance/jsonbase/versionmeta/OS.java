package cc.eumc.easylogin.instance.jsonbase.versionmeta;

public class OS {
    private static OS currentOS;

    OSType name;
    String version;
    String arch;

    public OS(OSType type, String version, String arch) {
        this.name = type;
        this.version = version;
        this.arch = arch;
    }

    /**
     * Check if current os matches this one
     * @return true if match
     */
    public boolean isMatch() {
        OS currentOS = getCurrentOS();
        int match = 1;

        if (this.name == currentOS.name) {
            if (arch != null) {
                if (!arch.contains(currentOS.arch)) {
                    match--;
                }
            }
            if (this.version != null) {
                if (name == OSType.windows && version.contains("10") && !System.getProperty("os.name").contains("10")) {
                    match--;
                } else {
                    match -= currentOS.version.equals(this.version)? 0 : 1;
                }
            }
        } else {
            match--;
        }

        return match == 1;
    }

    public static OS getCurrentOS() {
        if (currentOS == null) {
            currentOS = new OS(OSType.fromName(System.getProperty("os.name")), System.getProperty("os.version"), System.getProperty("os.arch"));
        }
        return currentOS;
    }

    public OSType getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getArch() {
        return arch;
    }
}
