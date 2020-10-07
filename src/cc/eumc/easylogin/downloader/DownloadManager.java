package cc.eumc.easylogin.downloader;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.animation.NProgressBarAnimation;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.task.Downloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    Map<DownloadEntry, Downloader> pendingDownloads = new HashMap<>();
    DownloadHandler generalDownloadHandler;
    ExecutorService executor;
    boolean cancelled = false;

    public DownloadManager(DownloadHandler generalDownloadHandler) {
        this.generalDownloadHandler = generalDownloadHandler;
    }

    private void setExecutor() {
        int threads = EasyLogin.getInstance().getConfig().downloadThreads;
        this.executor = Executors.newFixedThreadPool(threads<=0? 1 : threads);
    }

    public void setGeneralDownloadHandler(DownloadHandler generalDownloadHandler) {
        this.generalDownloadHandler = generalDownloadHandler;
    }

    public int getPendingNumber() {
        try {
            int pending = 0;
            for (Downloader downloader : pendingDownloads.values()) {
                if (!downloader.isFinished()) {
                    pending++;
                }
            }
            return pending;
        } catch (ConcurrentModificationException e) {
            return -1;
        }
    }

    public int getTotalNumber() {
        return pendingDownloads.size();
    }

    public void removeDownload(DownloadEntry downloadEntry) {
        pendingDownloads.remove(downloadEntry);
    }

    public void addDownload(DownloadEntry downloadEntry, boolean start) {
        addDownload(Collections.singletonList(downloadEntry), start);
    }

    public void addDownload(Collection<DownloadEntry> downloadEntries, boolean start) {
        for (DownloadEntry entry : downloadEntries) {
            // Check for duplication
            Downloader d = pendingDownloads.get(entry);
            if (d != null) {
                if (d.isFinished()) {
                    continue;
                }
            }

            Downloader downloader = new Downloader(entry);
            downloader.setHandler(new DownloadHandler() {
                @Override
                public void start(DownloadEntry downloadEntry) {
                    if (generalDownloadHandler != null) {
                        generalDownloadHandler.start(downloadEntry);
                    }
                }

                @Override
                public void progress(DownloadEntry downloadEntry, int percent) {
                    if (generalDownloadHandler != null) {
                        generalDownloadHandler.progress(downloadEntry, percent);
                    }
                }

                @Override
                public void exception(DownloadEntry downloadEntry, Exception e) {
                    if (generalDownloadHandler != null) {
                        generalDownloadHandler.exception(downloadEntry, e);
                    }
                }

                @Override
                public void done(DownloadEntry downloadEntry) {
                    if (generalDownloadHandler != null) {
                        generalDownloadHandler.done(downloadEntry);
                    }

                }
            });

            pendingDownloads.put(entry, downloader);

        }

        if (start) {
            start();
        }
    }

    /**
     * Finished entries will be ignored
     */
    public void start() {
        if (isCancelled()) {
            System.out.println("Attempt to start download after canceling DownloadManager");
            return;
        }

        setExecutor();

        pendingDownloads.forEach((downloadEntry, downloader) -> {
            if (!downloader.isFinished()) {
                executor.execute(downloader);
            }
        });

        executor.shutdown();
        try {
            executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopAll() {
        setCancelled(true);
        if (executor != null) {
            executor.shutdownNow();
        }
        pendingDownloads.forEach(((downloadEntry, downloader) -> downloader.setCancelled(true)));
    }

    public void stop(DownloadEntry downloadEntry) {
        pendingDownloads.get(downloadEntry).setCancelled(true);
    }

    /**
     * WARN: Created dialogue will not be shown unless you call `show()`
     * Cancel Button will be automatically bounded to stopAll()
     * Progress bar id: progressBar
     * Id of the label located beneath the progress bar: urlLabel
     * @param title title of dialogue
     * @param content subtitle
     * @return dialogue instance
     */
    public ELDialogue.Dialogue createUI(String title, String content) {
        try {
            ELDialogue.Dialogue dialogue = ELDialogue.createDialogue(FXMLLoader.load(EasyLogin.getInstance().getClass().getResource("DialogueDownload.fxml")));
            ((Label) dialogue.anchorPane.lookup("#title")).setText(title);
            ((Label) dialogue.anchorPane.lookup("#content")).setText(content);
            dialogue.setHandler(event -> {
                String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                if (ac.equals("cancelButton")) {
                    stopAll();
                    dialogue.close();
                }
            });
            return dialogue;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void updateProgressBar(ELDialogue.Dialogue dialogue, double progress) {
        System.out.println(progress);
        ProgressBar progressBar = (ProgressBar)dialogue.anchorPane.lookup("#progressBar");
        NProgressBarAnimation.setProgress(progressBar, progress);
    }

    public void updateProgressBar(ELDialogue.Dialogue dialogue) {
        int total = getTotalNumber();
        int finished = getTotalNumber() - getPendingNumber();
        updateProgressBar(dialogue, ((double)finished) / ((double)total));
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
