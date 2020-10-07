package cc.eumc.easylogin;

import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.config.ELConfig;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.downloader.provider.BMCLAPI;
import cc.eumc.easylogin.downloader.provider.DownloadProvider;
import cc.eumc.easylogin.downloader.provider.DownloadProviderType;
import cc.eumc.easylogin.downloader.provider.Raw;
import cc.eumc.easylogin.instance.InstanceManager;
import cc.eumc.easylogin.task.CheckStatus;
import cc.eumc.easylogin.task.GrabSkin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class EasyLogin extends Application {
    private static final String CONFIG_FILENAME = "config.json";

    private double xOffset = 0;
    private double yOffset = 0;

    public static Label titleLabel;

    public static Button playButton;

    public static List<Button> buttons = new ArrayList<>();
    public static VBox sideButtonVBox;
    public static Rectangle selectionIndicator;
    public static TabPane tabPane;

    public static VBox instanceVBox;
    public static ScrollPane instanceScrollPane;

    public static VBox userVBox;
    public static ScrollPane userScrollPane;
    public static HBox statusHBox;

    public static AnchorPane mainPane;
    public static AnchorPane dialogueContainer;

    public Scene scene;

    static EasyLogin instance;

    File dataFolder;
    File libraryFolder;
    File assetFolder;
    File manifestFolder;

    ELConfig elConfig;
    AccountManager accountManager;
    InstanceManager instanceManager;

    DownloadProvider downloadProvider;

    public Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Thread.setDefaultUncaughtExceptionHandler(new GeneralUncaughtExceptionHandler());

        dataFolder = new File(System.getProperty("user.dir"), "EasyLogin");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        libraryFolder = new File(dataFolder, "libraries");
        if (!libraryFolder.exists()) {
            libraryFolder.mkdir();
        }

        assetFolder = new File(dataFolder, "assets");
        if (!assetFolder.exists()) {
            assetFolder.mkdir();
        }

        manifestFolder = new File(dataFolder, "manifest");
        if (!manifestFolder.exists()) {
            manifestFolder.mkdir();
        }

        try {
            saveDefaultFile(CONFIG_FILENAME);

            File configFile = new File(getDataFolder(), "config.json");
            FileReader reader = new FileReader(configFile);
            elConfig = gson.fromJson(reader, ELConfig.class);
            //System.out.println(elConfig.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle(getConfig().title);
        scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            mainPane = (AnchorPane)scene.lookup("#mainPane");

            dialogueContainer = (AnchorPane) scene.lookup("#dialogueContainer");

            titleLabel = ((Label)scene.lookup("#titleLabel"));

            playButton = (Button)scene.lookup("#playButton");

            buttons.add((Button)scene.lookup("#instanceBtn"));
            buttons.add((Button)scene.lookup("#userBtn"));
            buttons.add((Button)scene.lookup("#settingBtn"));
            sideButtonVBox = (VBox)scene.lookup("#sideButtonVBox");
            selectionIndicator = (Rectangle)scene.lookup("#selectionIndicator");
            tabPane = (TabPane)scene.lookup("#tabPane");

            instanceVBox = (VBox)scene.lookup("#instanceVBox");
            instanceScrollPane = (ScrollPane)scene.lookup("#instanceScrollPane");

            userVBox = (VBox)scene.lookup("#userVBox");
            userScrollPane = (ScrollPane)scene.lookup("#userScrollPane");
            statusHBox = (HBox)scene.lookup("#statusHBox");
        } catch (Exception e) {
            e.printStackTrace();
        }

        titleLabel.setText(primaryStage.getTitle());

        if (getConfig().downloadProviderType == DownloadProviderType.BMCLAPI) {
            downloadProvider = new BMCLAPI();
        } else {
            downloadProvider = new Raw();
        }

        accountManager = new AccountManager(this);
        instanceManager = new InstanceManager(this);

        new CheckStatus(statusHBox, getClass().getResource("StatusIndicator.fxml")).start();
        new GrabSkin(getAccountManager()).start();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static EasyLogin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getLibraryFolder() {
        return libraryFolder;
    }

    public File getAssetFolder() {
        return assetFolder;
    }

    public File getManifestFolder() {
        return manifestFolder;
    }

    public ELConfig getConfig() {
        return elConfig;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    public void saveDefaultFile(String resourceName) throws IOException {
        saveDefaultFile(resourceName, false);
    }
    public void saveDefaultFile(String resourceName, boolean overwrite) throws IOException {
        File file = new File(getDataFolder(), resourceName);
        if (!file.exists() || overwrite) {
            file.createNewFile();
            InputStream in = getClass().getResource(resourceName).openStream();
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
    }

    public void saveConfig() {
        try (FileWriter fw = new FileWriter(new File(getDataFolder(), CONFIG_FILENAME))) {
            fw.write(instance.gson.toJson(getConfig()));
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GeneralUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
            try {
                ELDialogue.Dialogue confirmDialogue = ELDialogue.createDialogue(FXMLLoader.load(getClass().getResource("DialogueConfirm.fxml")));
                ((Label) confirmDialogue.anchorPane.lookup("#title")).setText("Sorry");
                ((Label) confirmDialogue.anchorPane.lookup("#content")).setText(t.toString() + " throws exception: " + e.getMessage());
                confirmDialogue.setButtonText("yes", "Exit");
                confirmDialogue.setButtonText("no", "Ignore");
                confirmDialogue.setHandler(event -> {
                    String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                    if (ac.equals("yes")) {
                        System.exit(0);
                    }
                    confirmDialogue.close();
                });

                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(confirmDialogue::show);
                        latch.await();
                        return null;
                        }
                    };
                    }
                };
                service.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
