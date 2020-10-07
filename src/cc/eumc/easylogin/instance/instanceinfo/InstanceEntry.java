package cc.eumc.easylogin.instance.instanceinfo;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.config.MemorySettings;
import cc.eumc.easylogin.config.WindowSettings;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.downloader.DownloadEntry;
import cc.eumc.easylogin.downloader.DownloadException;
import cc.eumc.easylogin.instance.LaunchException;
import cc.eumc.easylogin.instance.jsonbase.versionmeta.MCVersionMeta;
import cc.eumc.easylogin.jvm.JvmManager;
import cc.eumc.easylogin.task.DependencyCompletion;
import cc.eumc.easylogin.util.ArgumentBuilder;
import cc.eumc.easylogin.util.Environment;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InstanceEntry {
    public String displayName;
    public String version;
    public String javaPath;
    public MemorySettings memory;
    public WindowSettings window;
    public ModLoader modLoader;
    public MCServer server;
    public String remoteManagerServer;

    public InstanceEntry(String displayName, String version, String javaPath, MemorySettings memory, WindowSettings window, ModLoader modLoader, MCServer server, String remoteManagerServer) {
        this.displayName = displayName;
        this.version = version;
        this.javaPath = javaPath;
        this.memory = memory;
        this.window = window;
        this.modLoader = modLoader;
        this.server = server;
        this.remoteManagerServer = remoteManagerServer;
    }

    /**
     * Should be run sync
     * @param instanceFolder located to instance's path, e.g. EasyLogin/instances/1.16
     * @throws DownloadException
     */
    private void completeDependency(File instanceFolder) throws DownloadException {
        DependencyCompletion dependencyCompletion = new DependencyCompletion(this, instanceFolder);
        dependencyCompletion.run();
    }

    public void launch(AccountEntry accountEntry, File instanceFolder) throws LaunchException {
        //String playButtonOriginalText = EasyLogin.playButton.getText();
        //EasyLogin.playButton.setText("Preparing...");
        //EasyLogin.playButton.setDisable(true);
        new Thread(() -> {
            try {
                completeDependency(instanceFolder);
                run(accountEntry, instanceFolder); // <- appeared twice!
            } catch (DownloadException e) {
                e.printStackTrace();

                try {
                    ELDialogue.Dialogue confirmDialogue = ELDialogue.createDialogue(FXMLLoader.load(EasyLogin.getInstance().getClass().getResource("DialogueConfirm.fxml")));
                    try {
                        ((Label)confirmDialogue.anchorPane.lookup("#title")).setText("Error Preparing for Instance");
                        ((Label)confirmDialogue.anchorPane.lookup("#content")).setText(e.getReason() + "\nDo you want to continue launching?");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    confirmDialogue.setHandler(event -> {
                        String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                        switch (ac) {
                            case "yes":
                                run(accountEntry, instanceFolder);
                                break;
                            case "no":
                                break;
                        }
                        confirmDialogue.close();
                        System.out.println(ac);
                    });
                    confirmDialogue.show();
                    return;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }).start();
    }

    private void run(AccountEntry accountEntry, File instanceFolder) {
        AccountManager accountManager = EasyLogin.getInstance().getAccountManager();
        AccountEntry account = accountManager.lookupUserByID(accountManager.getStorage().activeID);
        System.out.println(account.accessToken);
        accountManager.refreshUserToken(account);

        List<String> args = getArguments(accountEntry, instanceFolder);

        try {
            ELDialogue.Dialogue dialogue = ELDialogue.createDialogue(FXMLLoader.load(EasyLogin.getInstance().getClass().getResource("DialogueCancellable.fxml")));
            dialogue.setLabelText("title", "Launching " + displayName);
            dialogue.setLabelText("content", "Just a sec");

            dialogue.setHandler(event -> {
                dialogue.close();
                System.out.println("Cancelled");
            });
            dialogue.show();

            Map<String, File> jvmHomeList = Environment.getJvmHomeList();
            jvmHomeList.forEach((ver, path) -> {
                System.out.println("*" + ver + " | " + path);
            });

            JvmManager.createProcess(new File(javaPath), args);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private List<String> getArguments(AccountEntry accountEntry, File instanceFolder) {
        File clientJson = new File(EasyLogin.getInstance().getDataFolder(), "manifestation/" + version + "/" + version + ".json");
        FileReader reader;
        try {
            reader = new FileReader(clientJson);
            MCVersionMeta versionMeta = EasyLogin.getInstance().gson.fromJson(reader, MCVersionMeta.class);
            reader.close();

            if (versionMeta == null ||
                    ((versionMeta.getArguments() == null ||
                    versionMeta.getArguments().getGame() == null ||
                    versionMeta.getArguments().getJvm() == null) && (versionMeta.getMinecraftArguments() == null))) {
                System.out.println(versionMeta.toString());
                showMessage("Sorry", String.format("Error occurred when loading meta for %s, try launching again may solve this problem.", version));
                return null;
            }


            // Building start up arguments
            List<String> args = new ArrayList<>();
            if (versionMeta.getMinecraftArguments() != null) {
                args.addAll(Arrays.asList(versionMeta.getMinecraftArguments().split(" ")));
            }
            else {
                for (Object object : versionMeta.getArguments().getJvm()) {
                    if (object instanceof String) {
                        args.add((String) object);
                    }
                }

                for (Object o : versionMeta.getArguments().getGame()) {
                    if (o instanceof String) {
                        args.add((String) o);
                    }
                }
            }

            args = fillPlaceholder(args, accountEntry, instanceFolder, versionMeta);

            System.out.println(args);

            return args;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    List<String> fillPlaceholder(List<String> args, AccountEntry accountEntry, File instanceFolder, MCVersionMeta versionMeta) {
        /*
        Params:
        --username  --version MultiMC5 --gameDir /Applications/MultiMC.app/Contents/MacOS/instances/1.15.2/.minecraft --assetsDir /Applications/MultiMC.app/Contents/MacOS/assets --assetIndex 1.15 --uuid  --accessToken  --userType  --versionType release

        Window size: 854 x 480

        Java Arguments:
        [-Xdock:icon=icon.png, -Xdock:name="MultiMC: 1.15.2", -XstartOnFirstThread, -Xms512m, -Xmx3000m, -Duser.language=en]
        */
        // [--username, Alan_Richard, --version, ${version_name}, --gameDir, ${game_directory}, --assetsDir, ${assets_root}, --assetIndex, ${assets_index_name}, --uuid, 4bac238b806f4bb7be4f00ec9923387f, --accessToken, ...-, --userType, mojang, --versionType, ${version_type}, -Djava.library.path=${natives_directory}, -Dminecraft.launcher.brand=${launcher_name}, -Dminecraft.launcher.version=${launcher_version}, -cp, ${classpath}]


        args = accountEntry.getAuthProvider().fillLaunchArgs(args); // fill user info

        StringBuilder libraries = new StringBuilder();
        for (DownloadEntry downloadEntry : DependencyCompletion.getLibraryDownloads(versionMeta.getLibraries())) {
            libraries.append(downloadEntry.getTargetFile().getAbsolutePath() + ";");
        }
        libraries.append(new File(EasyLogin.getInstance().getLibraryFolder(), "com/mojang/minecraft/" + version + ".jar").getAbsolutePath());

        HashMap<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("${version_name}", this.version);
        placeholderMap.put("${game_directory}", instanceFolder.getAbsolutePath() + "/.minecraft");
        placeholderMap.put("${assets_root}", EasyLogin.getInstance().getAssetFolder().getAbsolutePath());
        placeholderMap.put("${natives_directory}", libraries.toString());
        placeholderMap.put("${classpath}", versionMeta.getMainClass());
        placeholderMap.put("${version_type}", versionMeta.getType());
        placeholderMap.put("${assets_index_name}", version);
        placeholderMap.put("${launcher_name}", "EasyLogin");
        placeholderMap.put("${launcher_version}", "Deg1");
        args = ArgumentBuilder.fillArguments(args, placeholderMap);
        return args;
    }

    void showMessage(String title, String text) {
        ELDialogue.Dialogue okDialogue;
        try {
            okDialogue = ELDialogue.createDialogue(FXMLLoader.load(EasyLogin.getInstance().getClass().getResource("DialogueSingleButton.fxml")));

            okDialogue.setLabelText("title", title);
            okDialogue.setLabelText("content", text);
            ELDialogue.Dialogue finalOkDialogue = okDialogue;
            okDialogue.setHandler(event -> {
                finalOkDialogue.close();
            });
            okDialogue.show();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

