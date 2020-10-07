package cc.eumc.easylogin.dialogue;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.animation.UniversalAnimation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.*;

public class ELDialogue {
    //public static Map<String, Dialogue> dialogues = new HashMap<>();
    public static Stack<Dialogue> onDisplayDialogueStack = new Stack<>();

    public static class Dialogue {
        public String uid;
        public AnchorPane anchorPane;

        //private List<Dialogue> coveredDialogues = new ArrayList<>();

        public Dialogue(AnchorPane pane) {
            anchorPane = pane;
            uid = UUID.randomUUID().toString();
            pane.setId(uid + "_PANE");
            //dialogues.put(uid, this);
        }

        public void setHandler(EventHandler<ActionEvent> buttonHandler) {
            setHandler(buttonHandler, anchorPane);
            /*anchorPane.getChildren().forEach(object -> {
                if (object instanceof Button) {
                    object.setId(uuid + "_" + object.getId());
                    ((Button) object).setOnAction(buttonHandler);
                }
            });*/
        }

        public void setHandler(EventHandler<ActionEvent> buttonHandler, Pane pane) {
            pane.getChildren().forEach(object -> {
                if (object instanceof Button) {
                    System.out.println("Handler set: " + object);
                    object.setId(uid + "_" + object.getId());
                    ((Button) object).setOnAction(buttonHandler);
                }
            });
        }

        public void setLabelText(String id, String text) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(() -> setLabelText(id, text));
                return;
            }

            try {
                ((Label) anchorPane.lookup("#" + id)).setText(text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setButtonText(String id, String text) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(() -> setButtonText(id, text));
                return;
            }

            try {
                ((Button) anchorPane.lookup("#" + id)).setText(text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
        public boolean highlightDangerousOperation(String id) {
            try {
                anchorPane.lookup("#" + id).setStyle("-fx-background-color: " + NColor.dangerRedStr);
                return true;
            } catch (Exception e) {
                return false;
            }
        }*/

        public void show() {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(this::show);
                return;
            }

            try {
                onDisplayDialogueStack.peek().hide();
            } catch (Exception ignore) {}

            if (!anchorPane.isVisible()) { // if invoked hide() before
                anchorPane.setVisible(true);
                return;
            }

            if (!EasyLogin.dialogueContainer.isVisible()) {
                UniversalAnimation.playBlurAnimation(EasyLogin.mainPane, 0, 16);
            }
            anchorPane.setId(uid);

            EasyLogin.dialogueContainer.setPrefWidth(EasyLogin.mainPane.getPrefWidth());
            EasyLogin.dialogueContainer.setPrefHeight(EasyLogin.mainPane.getPrefHeight());
            EasyLogin.dialogueContainer.getChildren().add(anchorPane);
            EasyLogin.dialogueContainer.setVisible(true);

            UniversalAnimation.playBlurAnimation(anchorPane, 16, 16);

            EasyLogin.mainPane.setOpacity(0.3);

            /*
            dialogues.forEach((uid, dialogue) -> {
                if (dialogue != this && dialogue.isVisible()) {
                    dialogue.anchorPane.setVisible(false);
                    coveredDialogues.add(dialogue);
                }
            });*/

            onDisplayDialogueStack.push(this);

        }

        public void hide() {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(this::hide);
                return;
            }

            anchorPane.setVisible(false);
            try {
                onDisplayDialogueStack.remove(onDisplayDialogueStack.search(this));
            } catch (Exception ignore) {}

            if (onDisplayDialogueStack.size() == 0) {
                close();
            } else {
                onDisplayDialogueStack.pop().show();
            }

            /*
            coveredDialogues.forEach(covered -> {
                covered.anchorPane.setVisible(true);
            });
            coveredDialogues.clear();*/
        }

        public void close() {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(this::close);
                return;
            }

            try {
                EasyLogin.dialogueContainer.setVisible(false);
                EasyLogin.dialogueContainer.getChildren().clear();
                onDisplayDialogueStack.clear();
                //dialogues.clear();
                UniversalAnimation.playBlurAnimation(EasyLogin.mainPane, 16, 16);
                EasyLogin.mainPane.setOpacity(1.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isVisible() {
            return anchorPane.isVisible();
        }
    }

    public static Dialogue createDialogue(AnchorPane anchorPane) {
        return new Dialogue(anchorPane);
    }

    public static String getButtonAction(Object object) {
        try {
            return ((Button) object).getId().split("_")[1];
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
