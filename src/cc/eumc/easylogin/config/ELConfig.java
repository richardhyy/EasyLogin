package cc.eumc.easylogin.config;

import cc.eumc.easylogin.downloader.provider.DownloadProviderType;

public class ELConfig {
    public String title;
    public String instanceFolder;
    public String activeInstance;
    public Defaults defaults;
    public String language;
    public int downloadThreads;
    public DownloadProviderType downloadProviderType;
    public boolean checkLauncherUpdate;
}

