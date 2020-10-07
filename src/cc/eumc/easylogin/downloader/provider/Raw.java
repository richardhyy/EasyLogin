package cc.eumc.easylogin.downloader.provider;

public class Raw extends DownloadProvider {

    @Override
    public String convertURL(String originalURL) {
        return originalURL;
    }
}
