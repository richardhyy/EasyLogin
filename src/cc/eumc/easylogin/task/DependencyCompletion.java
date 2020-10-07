package cc.eumc.easylogin.task;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.downloader.DownloadEntry;
import cc.eumc.easylogin.downloader.DownloadException;
import cc.eumc.easylogin.downloader.DownloadHandler;
import cc.eumc.easylogin.downloader.DownloadManager;
import cc.eumc.easylogin.instance.InstanceManager;
import cc.eumc.easylogin.instance.MojangAPIConstant;
import cc.eumc.easylogin.instance.instanceinfo.InstanceEntry;
import cc.eumc.easylogin.instance.jsonbase.assetindex.AssetEntry;
import cc.eumc.easylogin.instance.jsonbase.assetindex.MCAssetIndex;
import cc.eumc.easylogin.instance.jsonbase.versionmanifest.MCVersionEntry;
import cc.eumc.easylogin.instance.jsonbase.versionmanifest.MCVersionManifest;
import cc.eumc.easylogin.instance.jsonbase.versionmeta.*;
import cc.eumc.easylogin.util.FileOperator;
import cc.eumc.easylogin.util.HttpRequest;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DependencyCompletion {
    InstanceManager instanceManager;
    InstanceEntry instanceEntry;
    File instanceFolder;

    File libraryFolder;
    File assetFolder;
    File manifestVersionStorage;

    public DependencyCompletion(InstanceEntry instanceEntry, File instanceFolder) {
        this.instanceManager = EasyLogin.getInstance().getInstanceManager();
        this.instanceEntry = instanceEntry;
        this.instanceFolder = instanceFolder;

        this.libraryFolder = EasyLogin.getInstance().getLibraryFolder();

        this.assetFolder = EasyLogin.getInstance().getAssetFolder();

        this.manifestVersionStorage = new File(EasyLogin.getInstance().getManifestFolder(), instanceEntry.version);
        if (!manifestVersionStorage.exists()) {
            manifestVersionStorage.mkdir();
        }
    }

    public void run() throws DownloadException {
        Gson gson = EasyLogin.getInstance().gson;
        DownloadManager downloadManager = new DownloadManager(null);
        ELDialogue.Dialogue dialogue = downloadManager.createUI("Preparing for " + instanceEntry.version, "Downloading dependencies");
        dialogue.show();
        downloadManager.updateProgressBar(dialogue, -1); // indeterminate

        downloadManager.setGeneralDownloadHandler(new DownloadHandler() {
            List<DownloadEntry> reattempted = new ArrayList<>();
            @Override
            public void start(DownloadEntry downloadEntry) {
                dialogue.setLabelText("urlLabel", downloadEntry.getUrl().toString());//, downloadEntry.getTargetFile().getPath()));
                //downloadManager.updateProgressBar(dialogue);
            }

            @Override
            public void progress(DownloadEntry downloadEntry, int percent) {
                //downloadManager.updateProgressBar(dialogue);
            }

            @Override
            public void exception(DownloadEntry downloadEntry, Exception e) {
                if (!reattempted.contains(downloadEntry)) {
                    dialogue.setLabelText("urlLabel", String.format("Error downloading %s, we will have another attempt later", downloadEntry.getUrl()));
                    reattempted.add(downloadEntry);
                    downloadManager.addDownload(downloadEntry, true);
                    //downloadManager.start(); // DownloadManager will ignore finished entries
                }
                else {
                    dialogue.setLabelText("urlLabel", String.format("Error downloading %s", downloadEntry.getUrl()));
                }
            }

            @Override
            public void done(DownloadEntry downloadEntry) {
                dialogue.setLabelText("urlLabel", String.format("Finished downloading %s", downloadEntry.getUrl()));
                downloadManager.updateProgressBar(dialogue);
            }
        });

        try {
            // Minecraft Library & Asset Completion Start ---------------
            if (instanceEntry.version != null) {
                try {
                    dialogue.setLabelText("urlLabel", "Fetching version manifest...");

                    String manifestJson = HttpRequest.get(new URL(MojangAPIConstant.VERSION_MANIFEST))
                            .execute()
                            .expectResponseCode(200)
                            .returnContent()
                            .asString("UTF-8").trim();
                    FileOperator.writeStringToFile(new File(manifestVersionStorage.getParent(), "version_manifest.json"), manifestJson);

                    MCVersionManifest mcVersionManifest = gson.fromJson(manifestJson, MCVersionManifest.class);

                    if (mcVersionManifest == null) {
                        throw new DownloadException("Failed loading version manifest");
                    }

                    MCVersionEntry versionEntry = mcVersionManifest.getEntry(instanceEntry.version);
                    if (versionEntry == null) {
                        throw new DownloadException("Minecraft version " + instanceEntry.version + " not found");
                    }

                    dialogue.setLabelText("urlLabel", "Fetching meta...");

                    String metaJson = HttpRequest.get(new URL(versionEntry.getUrl()))
                            .execute()
                            .expectResponseCode(200)
                            .returnContent()
                            .asString("UTF-8").trim();
                    MCVersionMeta versionMeta = gson.fromJson(metaJson, MCVersionMeta.class);

                    if (versionMeta == null) {
                        throw new DownloadException("Failed loading version meta");
                    }

                    FileOperator.writeStringToFile(new File(manifestVersionStorage, instanceEntry.version + ".json"), metaJson);

                    // Libraries Start ---------------

                    Library[] libraries = versionMeta.getLibraries();
                    List<DownloadEntry> downloadEntries = new ArrayList<>();

                    if (libraries == null) {
                        throw new DownloadException("Failed loading library list");
                    }

                    DownloadItem clientInfo = versionMeta.getDownloads().getClient();
                    if (clientInfo == null) {
                        throw new DownloadException("Failed reading client download info");
                    }
                    downloadEntries.add(new DownloadEntry(clientInfo.getUrl(), new File(libraryFolder, "com/mojang/minecraft/" + instanceEntry.version + ".jar"), clientInfo.getSha1(), clientInfo.getSize()));

                    downloadEntries.addAll(getLibraryDownloads(libraries));

                    downloadManager.addDownload(downloadEntries, true);
                    downloadEntries.clear(); // for reusing
                    // Libraries End ---------------


                    // Assets Start ---------------
                    AssetIndex assetIndexMeta = versionMeta.getAssetIndex();
                    if (assetIndexMeta != null) {
                        dialogue.setLabelText("urlLabel", "Fetching asset index...");
                        String assetIndexJson = HttpRequest.get(new URL(assetIndexMeta.getUrl()))
                                .execute()
                                .expectResponseCode(200)
                                .returnContent()
                                .asString("UTF-8").trim();
                        MCAssetIndex mcAssetIndex = gson.fromJson(assetIndexJson, MCAssetIndex.class);
                        if (mcAssetIndex != null && mcAssetIndex.getObjects() != null) {
                            FileOperator.writeStringToFile(new File(assetFolder, "indexes/" + instanceEntry.version + ".json"), assetIndexJson);
                            Map<String, AssetEntry> objects = mcAssetIndex.getObjects();
                            objects.forEach((location, assetInfo) -> {
                                String saveLoc = assetInfo.getHash().substring(0, 2) + "/" + assetInfo.getHash();
                                downloadEntries.add(
                                        new DownloadEntry(MojangAPIConstant.RESOURCE_BASE + saveLoc,
                                                new File(assetFolder, "objects/" + saveLoc), assetInfo.getHash(), assetInfo.getSize()));
                            });
                            downloadManager.addDownload(downloadEntries, true);
                        } else {
                            System.out.println("Failed loading assets");
                        }
                    }

                    downloadManager.addDownload(downloadEntries, true);
                    downloadEntries.clear(); // for reusing
                    // Assets End ---------------

                } catch (IOException e) {
                    e.printStackTrace();
                    throw new DownloadException("Error downloading necessary files");
                }
            }
            // Minecraft Library & Asset Completion End ---------------
        } catch (DownloadException exception) {
            dialogue.close();
            throw exception;
        }

        dialogue.close();
    }

    public static List<DownloadEntry> getLibraryDownloads(Library[] libraries) {
        List<DownloadEntry> downloadEntries = new ArrayList<>();
        for (Library library : libraries) {
            // Classify & Download
            Artifact artifact = library.getDownloads().getArtifact();
            if (artifact == null) {
                continue;
            }

            Rule[] rules = library.getRules();
            if (rules != null && rules.length > 0) {
                for (Rule rule : rules) {
                    boolean allow = false;
                    boolean disallow = false;
                    if (rule.getOs() != null && rule.getOs().isMatch()) {
                        if (rule.getAction().equals("allow")) {
                            allow = true;
                        } else if (rule.getAction().equals("disallow")) {
                            disallow = true;
                        }
                    }
                    if (!disallow && allow && library.getDownloads().getClassifiers() != null) {
                        OS currentOS = OS.getCurrentOS();
                        Artifact classified = null;
                        switch (currentOS.getName()) {
                            case osx:
                                classified = library.getDownloads().getClassifiers().getNatives_macos();
                                break;
                            case linux:
                                classified = library.getDownloads().getClassifiers().getNatives_linux();
                                break;
                            case windows:
                                classified = library.getDownloads().getClassifiers().getNatives_windows();
                                break;
                            case other:
                                // No match
                                break;
                        }
                        if (classified != null) {
                            downloadEntries.add(new DownloadEntry(classified.getUrl(), new File(EasyLogin.getInstance().getLibraryFolder(), classified.getPath()), classified.getSha1(), classified.getSize()));
                        }
                    }
                }
            } else {
                downloadEntries.add(new DownloadEntry(artifact.getUrl(), new File(EasyLogin.getInstance().getLibraryFolder(), artifact.getPath()), artifact.getSha1(), artifact.getSize()));
            }
        }
        return downloadEntries;
    }

}
