package cc.eumc.easylogin.task;

import cc.eumc.easylogin.downloader.DownloadEntry;
import cc.eumc.easylogin.downloader.DownloadHandler;

import java.io.File;

public class Downloader extends Thread {
    DownloadEntry downloadEntry;
    DownloadHandler downloadHandler;
    boolean finished = false;
    private boolean cancelled = false;

    public Downloader(DownloadEntry downloadEntry) {
        this.downloadEntry = downloadEntry;
    }

    public void setHandler(DownloadHandler downloadHandler) {
        this.downloadHandler = downloadHandler;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void run() {
        if (isCancelled()) return;

        if (downloadHandler != null) {
            downloadHandler.start(downloadEntry);
        }

        try {
            downloadEntry.download();
        } catch (Exception exception) {
            exception.printStackTrace();

            if (downloadHandler != null) {
                downloadHandler.exception(downloadEntry, exception);
            }
        }

        finished = true;

        if (downloadHandler != null) {
            downloadHandler.done(downloadEntry);
        }
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void interrupt() {
        super.interrupt();

        setCancelled(true);
    }
}
