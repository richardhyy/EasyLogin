package cc.eumc.easylogin.remotemanager;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.downloader.DownloadEntry;
import cc.eumc.easylogin.util.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RemoteManager {
    URL instanceManagerURL;

    public RemoteManager(URL instanceManagerURL) {
        this.instanceManagerURL = instanceManagerURL;
    }

    public List<DownloadEntry> compareRemote() throws IOException {
        List<DownloadEntry> pendingDownloads = new ArrayList<>();
        RemoteChecklist remoteChecklist = grabRemoteChecklist();
        if (remoteChecklist != null) {
            pendingDownloads.addAll(remoteChecklist.files);
        }
        return pendingDownloads;
    }

    public RemoteChecklist grabRemoteChecklist() throws IOException {
        String json = HttpRequest.get(instanceManagerURL)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asString("UTF-8").trim();
        return EasyLogin.getInstance().gson.fromJson(json, RemoteChecklist.class);
    }
}