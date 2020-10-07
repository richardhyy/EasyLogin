package cc.eumc.easylogin.downloader;

public class DownloadException extends Exception {
    final String reason;

    public DownloadException(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return super.toString() + " " + getReason();
    }
}
