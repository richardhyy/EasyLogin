package cc.eumc.easylogin.remotemanager;

import cc.eumc.easylogin.downloader.DownloadEntry;

import java.util.List;

public class RemoteChecklist {
    String publisher;
    String lastUpdate;
    String minecraftVersion;
    String releaseVersion;
    List<DownloadEntry> files;
}