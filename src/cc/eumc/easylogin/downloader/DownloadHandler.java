package cc.eumc.easylogin.downloader;

public interface DownloadHandler {
    void start(DownloadEntry downloadEntry);
    void progress(DownloadEntry downloadEntry, int percent);
    void exception(DownloadEntry downloadEntry, Exception e);
    void done(DownloadEntry downloadEntry);
}
