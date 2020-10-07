package cc.eumc.easylogin.downloader;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.util.FileOperator;
import cc.eumc.easylogin.util.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadEntry {
    private URL url;
    private File targetFile;
    private String sha1;
    private long size;

    /**
     *
     * @param url will be converted by DownloadProvider
     * @param targetFile
     * @param sha1 nullable
     * @param size size
     */
    public DownloadEntry(String url, File targetFile, String sha1, long size) {
        try {
            this.url = new URL(EasyLogin.getInstance().getDownloadProvider().convertURL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.targetFile = targetFile;
        this.sha1 = sha1;
        this.size = size;
    }

    public File getTargetFile() {
        return targetFile;
    }
    public void setTargetFile(File file) {
        this.targetFile = file;
    }

    public URL getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public boolean download() throws Exception {
        if (url == null) {
            throw new DownloadException("URL null");
        }

        File parent = targetFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        System.out.println(String.format("Downloading: %s -> %s", url, targetFile.getPath()));

        if (targetFile.exists() && sha1 != null) {
            if ((size > 0 && targetFile.length() == size) // !
                || sha1.equalsIgnoreCase(FileOperator.calcSHA1(targetFile))) {
                System.out.println("Bypass download: " + this.toString());
                return true;
            }
        }

        byte[] bytes = HttpRequest.get(url)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asBytes();

        if (sha1 != null) {
            if (!sha1.equalsIgnoreCase(FileOperator.calcSHA1(bytes))) {
                throw new DownloadException("SHA1 not match");
            }
        }

        FileOutputStream fos = new FileOutputStream(targetFile);
        fos.write(bytes);

        return true;
    }

    @Override
    public String toString() {
        return "DownloadEntry{" +
                "downloadURL='" + url + '\'' +
                ", location='" + targetFile + '\'' +
                ", sha1='" + sha1 + '\'' +
                '}';
    }
}
