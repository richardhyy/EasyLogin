package cc.eumc.easylogin.instance;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.NEffect;
import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.downloader.DownloadException;
import cc.eumc.easylogin.instance.instanceinfo.InstanceEntry;
import cc.eumc.easylogin.util.FileOperator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

public class InstanceManager {
    private static final String INSTANCE_CONFIG_FILENAME = "instance.json";
    InstanceStorage storage;
    EasyLogin instance;
    File instanceFolder;
    final int BUTTON_SHADOW_DEPTH = 7;

    public InstanceManager(EasyLogin instance) {
        this.instance = instance;
        storage = new InstanceStorage();

        try {
            instanceFolder = new File(instance.getDataFolder(), instance.getConfig().instanceFolder);
            if (!instanceFolder.exists()) {
                instanceFolder.mkdir();
            }
            else {
                String[] instanceDirs = instanceFolder.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                });
                System.out.println(Arrays.toString(instanceDirs));

                if (instanceDirs != null) {
                    for (String folder : instanceDirs) {
                        InstanceEntry instanceEntry;
                        File instanceConfig = new File(instanceFolder, folder + "/" + INSTANCE_CONFIG_FILENAME);
                        if (!instanceConfig.exists()) {
                            instanceEntry = new InstanceEntry(folder, null, null, null, null, null, null, null);
                            saveInstanceInfo(folder, instanceEntry);
                        }
                        else {
                            instanceEntry = readInstanceInfo(instanceConfig);
                            if (instanceEntry == null) {
                                continue;
                            }
                        }

                        storage.instances.put(instanceEntry, folder);
                        addInstanceToUI(instanceEntry);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (instance.getConfig().activeInstance != null) {
                setActiveInstance(getInstanceEntryByFolderName(instance.getConfig().activeInstance), false);
            }
        }
    }

    public void launchInstance(InstanceEntry instanceEntry, AccountEntry accountEntry) throws LaunchException {
        try {
            instanceEntry.launch(accountEntry, new File(getInstanceFolder(), storage.instances.get(instanceEntry)));

        } catch (LaunchException e) {
            e.printStackTrace();
        }
    }

    public void deleteInstance(InstanceEntry instanceEntry) {
        if (storage.activeInstance == instanceEntry) {
            storage.activeInstance = null;
            instance.getConfig().activeInstance = null;
            instance.saveConfig();
        }

        File instanceDir = new File(getInstanceFolder(), storage.instances.get(instanceEntry));
        FileOperator.deleteDir(instanceDir);

        EasyLogin.instanceVBox.getChildren().remove(EasyLogin.instanceVBox.lookup("#" + toControlID(storage.instances.get(instanceEntry)) + "_PANE"));

        System.out.println("Instance deleted: " + instanceEntry);
        storage.instances.remove(instanceEntry);
    }

    public void setActiveInstance(InstanceEntry instanceEntry, boolean saveConfig) {
        storage.activeInstance = instanceEntry;
        if (instanceEntry == null) {
            System.out.println("setActiveInstance by null");
            return;
        }

        storage.instances.forEach((entry, name) -> {
            Button activeButton = (Button)EasyLogin.instanceVBox.lookup("#" + toControlID(name) + "_ACTIVE");
            //if (activeButton != null) {
                if (entry == instanceEntry) {
                    activeButton.setText("✓");
                    activeButton.setEffect(NEffect.getInnerShadow(BUTTON_SHADOW_DEPTH));
                } else if (!activeButton.getText().isEmpty()) {
                    activeButton.setText("");
                    activeButton.setEffect(NEffect.getDropShadow(BUTTON_SHADOW_DEPTH));
                }
            //}
        });

        if (saveConfig) {
            instance.getConfig().activeInstance = storage.instances.get(instanceEntry);
            instance.saveConfig();
        }
    }

    void saveInstanceInfo(String folder, InstanceEntry instanceEntry) {
        File file = new File(instanceFolder, folder + "/" + INSTANCE_CONFIG_FILENAME);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(instance.gson.toJson(instanceEntry));
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    InstanceEntry readInstanceInfo(File configFile) {
        try {
            FileReader reader = new FileReader(configFile);
            return instance.gson.fromJson(reader, InstanceEntry.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InstanceEntry getInstanceEntryByControlIDName(String controlID) {
        for (InstanceEntry instanceEntry : storage.instances.keySet()) {
            if (toControlID(storage.instances.get(instanceEntry)).equals(controlID)) {
                return instanceEntry;
            }
        }
        return null;
    }

    public InstanceEntry getInstanceEntryByFolderName(String folder) {
        for (InstanceEntry instanceEntry : storage.instances.keySet()) {
            if (storage.instances.get(instanceEntry).equals(folder)) {
                return instanceEntry;
            }
        }
        return null;
    }

    public void addInstanceToUI(InstanceEntry instanceEntry) {
        try {
            String folderName = storage.instances.get(instanceEntry);

            GridPane entryGridPane = FXMLLoader.load(instance.getClass().getResource("InstanceEntry.fxml"));
            entryGridPane.setId(toControlID(folderName) + "_PANE");
            Button activeButton = (Button)entryGridPane.lookup("#activeButton");
            activeButton.setId(toControlID(folderName) + "_ACTIVE");
            Button button = (Button)entryGridPane.lookup("#editInstanceButton");
            button.setId(toControlID(folderName) + "_EDIT");
            Label folderLabel = (Label)entryGridPane.lookup("#folderLabel");
            folderLabel.setId(toControlID(folderName) + "_FOLDER");
            folderLabel.setText(folderName);
            ImageView imageView = (ImageView)entryGridPane.lookup("#iconImageView");
            imageView.setId(toControlID(folderName) + "_ICON");
            Image icon = getInstanceIcon(instanceEntry);
            if (icon != null) {
                imageView.setImage(icon);
            }
            TextField textField = (TextField)entryGridPane.lookup("#displayNameTextField");
            textField.setId(toControlID(folderName) + "_NAME");
            textField.setText(instanceEntry.displayName);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                String oldText;
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                    {
                        oldText = textField.getText();
                        // on focus
                    }
                    else
                    {
                        // out focus
                        if (!oldText.equals(textField.getText())) {
                            instanceEntry.displayName = textField.getText();
                            saveInstanceInfo(storage.instances.get(instanceEntry), instanceEntry);
                        }
                    }
                }
            });
            EasyLogin.instanceVBox.getChildren().add(entryGridPane);
            EasyLogin.instanceVBox.setPrefHeight(EasyLogin.instanceVBox.getPrefHeight() + entryGridPane.getPrefHeight() + EasyLogin.instanceVBox.getSpacing());
            EasyLogin.instanceScrollPane.setVvalue(EasyLogin.instanceScrollPane.getVmax());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Image getInstanceIcon(InstanceEntry instanceEntry) {
        File image = new File(instanceFolder, storage.instances.get(instanceEntry) + "/.minecraft/icon.png");
        if (!image.exists()) {
            return null;
        }

        try {
            return new Image(new ByteArrayInputStream(Files.readAllBytes(image.toPath())));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void revealInFileManager(InstanceEntry instanceEntry) {
        try {
            File dir = new File(EasyLogin.getInstance().getInstanceManager().getInstanceFolder() + "/" + EasyLogin.getInstance().getInstanceManager().getStorage().instances.get(instanceEntry));
            File mcDir = new File(dir, ".minecraft");
            if (mcDir.isDirectory()) {
                Desktop.getDesktop().open(mcDir);
            }
            else {
                Desktop.getDesktop().open(dir);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public File getInstanceFolder() {
        return instanceFolder;
    }

    public InstanceStorage getStorage() {
        return storage;
    }

    public String toControlID(String folderName) {
        return UUID.nameUUIDFromBytes(folderName.getBytes()).toString();
    }
}
