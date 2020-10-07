package cc.eumc.easylogin;

import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.instance.instanceinfo.InstanceEntry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class InstanceController {

    public void editInstance(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getSource();
        String name = button.getId().replace("_EDIT", "");
        System.out.println("Edit: " + name);

        InstanceEntry instanceEntry = EasyLogin.getInstance().getInstanceManager().getInstanceEntryByControlIDName(name);

        try {
            ELDialogue.Dialogue editDialogue = ELDialogue.createDialogue(FXMLLoader.load(getClass().getResource("DialogueInstanceEdit.fxml")));
            ((Label) editDialogue.anchorPane.lookup("#title")).setText("Edit Instance");
            ((Label) editDialogue.anchorPane.lookup("#subtitle")).setText(instanceEntry.displayName + " @ ./EasyLogin/" + EasyLogin.getInstance().getConfig().instanceFolder + "/" + EasyLogin.getInstance().getInstanceManager().getStorage().instances.get(instanceEntry));

            editDialogue.setHandler(event -> {
                String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                switch (ac) {
                    case "done":
                        editDialogue.close();
                        break;
                    case "deleteInstanceButton":
                        try {
                            ELDialogue.Dialogue confirmDialogue = ELDialogue.createDialogue(FXMLLoader.load(getClass().getResource("DialogueConfirm.fxml")));

                            ((Label) confirmDialogue.anchorPane.lookup("#title")).setText("Are you sure you want to delete this instance?");
                            ((Label) confirmDialogue.anchorPane.lookup("#content")).setText("“" + instanceEntry.displayName + "” will be lost forever! (A long time)");
                            //confirmDialogue.highlightDangerousOperation("yes");

                            confirmDialogue.setHandler(e -> {
                                String action = ELDialogue.getButtonAction(e.getSource()); // Action
                                System.out.println(action);
                                if (action.equals("yes")) {
                                    EasyLogin.getInstance().getInstanceManager().deleteInstance(instanceEntry);
                                    confirmDialogue.close();
                                    System.out.println("Deleted Instance: " + instanceEntry.displayName);
                                }
                                else {
                                    confirmDialogue.hide();
                                    editDialogue.show();
                                }
                            });
                            editDialogue.hide();
                            confirmDialogue.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                System.out.println(ac);
            });

            editDialogue.show();

            Platform.runLater(() -> { // Avoid NullPointerException caused by ScrollPane
                GridPane instanceEditGridPane = (GridPane)editDialogue.anchorPane.lookup("#instanceEditGridPane");
                editDialogue.setHandler(event -> {
                    String ac = ELDialogue.getButtonAction(event.getSource());
                    System.out.println(ac);
                    /*
                    minecraftChangeButton
                    forgeChangeButton
                    fabricChangeButton
                    liteloaderChangeButton
                    loginServerChangeButton
                    rmsButton
                     */
                    switch (ac) {
                        case "minecraftFolderButton":
                            EasyLogin.getInstance().getInstanceManager().revealInFileManager(instanceEntry);
                            break;
                    }
                }, instanceEditGridPane);
                updateConfigInfo(instanceEntry, instanceEditGridPane, editDialogue);
            });

            } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void selectInstance(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getSource();
        String name = button.getId().replace("_ACTIVE", "");
        //button.setText("✓");
        InstanceEntry instanceEntry = EasyLogin.getInstance().getInstanceManager().getInstanceEntryByControlIDName(name);
        EasyLogin.getInstance().getInstanceManager().setActiveInstance(instanceEntry, true);
        System.out.println("Active: " + name);
    }

    public void buttonPressedShallow(MouseEvent mouseEvent) {
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getInnerShadow(7));
    }

    public void buttonReleasedShallow(MouseEvent mouseEvent) {
        Button button = (Button)mouseEvent.getSource();
        if (!button.getText().equals("✓")) {
            button.setEffect(NEffect.getDropShadow(7));
        }
    }

    private void updateConfigInfo(InstanceEntry instanceEntry, GridPane gridPane, ELDialogue.Dialogue dialogue) {
        setIcon(gridPane, "minecraftIcon", instanceEntry.version==null? NIcon.QUESTION : NIcon.CUBE);
        setIcon(gridPane, "javaIcon", (instanceEntry.javaPath == null && EasyLogin.getInstance().getConfig().defaults.javaPath == null)? NIcon.QUESTION : NIcon.JAVA);
        setIcon(gridPane, "forgeIcon", (instanceEntry.modLoader==null || instanceEntry.modLoader.forgeVersion ==null)? NIcon.QUESTION : NIcon.CHECK);
        setIcon(gridPane, "fabricIcon", (instanceEntry.modLoader==null || instanceEntry.modLoader.fabricVersion ==null)? NIcon.QUESTION : NIcon.CHECK);
        setIcon(gridPane, "liteloaderIcon", (instanceEntry.modLoader==null || instanceEntry.modLoader.liteLoaderVersion ==null)? NIcon.QUESTION : NIcon.CHECK);
        setIcon(gridPane, "loginServerIcon", instanceEntry.server==null? NIcon.QUESTION : NIcon.SERVER);
        setIcon(gridPane, "rmsIcon", instanceEntry.remoteManagerServer==null? NIcon.QUESTION : NIcon.CHECK);
        setIcon(gridPane, "windowIcon", instanceEntry.window==null? NIcon.QUESTION : NIcon.WINDOW);

        dialogue.setLabelText("minecraftVersionLabel", instanceEntry.version==null? "Not Selected" : instanceEntry.version);
        dialogue.setLabelText("javaPathLabel", (instanceEntry.javaPath == null?(EasyLogin.getInstance().getConfig().defaults.javaPath == null? "Not Specified" : "Default: " + EasyLogin.getInstance().getConfig().defaults.javaPath):instanceEntry.javaPath));
        dialogue.setLabelText("forgeVersionLabel", (instanceEntry.modLoader==null || instanceEntry.modLoader.forgeVersion ==null)? "Not Installed" : instanceEntry.modLoader.forgeVersion);
        dialogue.setLabelText("fabricVersionLabel", (instanceEntry.modLoader==null || instanceEntry.modLoader.fabricVersion ==null)? "Not Installed" : instanceEntry.modLoader.fabricVersion);
        dialogue.setLabelText("liteloaderVersionLabel", (instanceEntry.modLoader==null || instanceEntry.modLoader.liteLoaderVersion ==null)? "Not Installed" : instanceEntry.modLoader.liteLoaderVersion);
        dialogue.setLabelText("loginServerLabel", instanceEntry.server==null? "Not Specified" : instanceEntry.server.address + (instanceEntry.server.port==0?"":":" + instanceEntry.server.port));
        dialogue.setLabelText("rmsURLLabel", instanceEntry.remoteManagerServer==null? "Not Specified" : instanceEntry.remoteManagerServer);
        dialogue.setLabelText("windowLabel", instanceEntry.window==null? "Default" : instanceEntry.window.width + "×" + instanceEntry.window.height);

    }

    private void setIcon(Pane pane, String id, String shapeSVG) {
        pane.lookup("#" + id).setStyle("-fx-shape: \"" + shapeSVG + "\"; -fx-background-color: #979797");
    }
}
