package cc.eumc.easylogin;

import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.animation.UniversalAnimation;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.instance.InstanceManager;
import cc.eumc.easylogin.instance.instanceinfo.InstanceEntry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.util.UUID;

public class Controller {
    public Label helloWorld;
    public Label titleLabel;

    public void newInstance(ActionEvent actionEvent) {

    }

    public void runInstance(ActionEvent actionEvent) throws Exception {
        InstanceManager instanceManager = EasyLogin.getInstance().getInstanceManager();
        AccountManager accountManager = EasyLogin.getInstance().getAccountManager();
        InstanceEntry instance = instanceManager.getStorage().activeInstance;
        AccountEntry account = accountManager.lookupUserByID(accountManager.getStorage().activeID);

        if (account == null) {
            ELDialogue.Dialogue okDialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueSingleButton.fxml")));
            okDialogue.setLabelText("title", "Account Not Selected");
            okDialogue.setLabelText("content", "Click the button below to select or add an account.");
            okDialogue.setHandler(event -> {
                userButton(new ActionEvent(EasyLogin.sideButtonVBox.lookup("#userBtn"), EasyLogin.sideButtonVBox.lookup("#userBtn")));
                okDialogue.close();
            });
            okDialogue.show();
        }
        else if (instance == null) {
            ELDialogue.Dialogue okDialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueSingleButton.fxml")));
            okDialogue.setLabelText("title", "Instance Not Selected");
            okDialogue.setLabelText("content", "Click the button below to select or add an instance.");
            okDialogue.setHandler(event -> {
                instanceButton(new ActionEvent(EasyLogin.sideButtonVBox.lookup("#instanceBtn"), EasyLogin.sideButtonVBox.lookup("#instanceBtn")));
                okDialogue.close();
            });
            okDialogue.show();
        }
        else {
            instanceManager.launchInstance(instance, account);
        }

    }

    public void newUser(ActionEvent actionEvent) {
        EasyLogin.getInstance().getAccountManager().newUserUI();
    }

    public void sayHello(ActionEvent actionEvent) throws Exception {
        throw new Exception("Under construction.");
    }

    public void quit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void instanceButton(ActionEvent actionEvent) {
        EasyLogin.buttons.forEach(btn -> setSelected(btn, false));
        setSelected(actionEvent.getSource(), true);

        EasyLogin.tabPane.getSelectionModel().select(0);
    }

    public void userButton(ActionEvent actionEvent) {
        EasyLogin.buttons.forEach(btn -> setSelected(btn, false));
        setSelected(actionEvent.getSource(), true);
        EasyLogin.tabPane.getSelectionModel().select(1);
    }

    public void settingButton(ActionEvent actionEvent) {
        EasyLogin.buttons.forEach(btn -> setSelected(btn, false));
        setSelected(actionEvent.getSource(), true);
        EasyLogin.tabPane.getSelectionModel().select(2);
    }

    private void setSelected(Object source, boolean selected) {
        Button sourceBtn = (Button)source;

        if (selected) {
            UniversalAnimation.playMovementAnimation(EasyLogin.selectionIndicator, sourceBtn.getLayoutY());
            //Main.selectionIndicator.setY(sourceBtn.getLayoutY());
        }

        for (int i=0; i<sourceBtn.getStyleClass().size(); i++) {
            if (sourceBtn.getStyleClass().get(i).equals("control-btn-" + (!selected?"":"de") + "selected")) {
                sourceBtn.getStyleClass().set(i, "control-btn-" + (selected?"":"de") + "selected");
            }
        }
    }

    public void buttonPressed(MouseEvent mouseEvent) {
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), false);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), false);
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getInnerShadow(14.5));
    }

    public void buttonReleased(MouseEvent mouseEvent) {
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getDropShadow(14.5));
//NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), true);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), true);
    }

    public void buttonPressedShallow(MouseEvent mouseEvent) {
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), false);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), false);
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getInnerShadow(7));
    }

    public void buttonReleasedShallow(MouseEvent mouseEvent) {
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getDropShadow(7));
//NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), true);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), true);
    }

    public void buttonPressedExtraShallow(MouseEvent mouseEvent) {
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), false);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), false);
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getInnerShadow(7));
    }

    public void buttonReleasedExtraShallow(MouseEvent mouseEvent) {
        ((Button)mouseEvent.getSource()).setEffect(NEffect.getDropShadow(7));
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((Button)mouseEvent.getSource()).getEffect(), true);
        //NButtonAnimation.playToggleShadowAnimation((DropShadow)((DropShadow) ((Button)mouseEvent.getSource()).getEffect()).getInput(), true);
    }

    public void login(ActionEvent actionEvent) {

    }

    public void cancel(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getSource();
        button.getId();
    }
}
