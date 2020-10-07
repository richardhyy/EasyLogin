package cc.eumc.easylogin;

import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.dialogue.ELDialogue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BackgroundFill;

public class UserController {
    public void deleteUser(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getSource();
        String id = button.getId().replace("_DELETE", "");
        AccountManager accountManager = EasyLogin.getInstance().getAccountManager();
        AccountEntry accountEntry = accountManager.lookupUserByID(id);
        if (accountEntry == null) {
            System.out.println("User not found: " + id);
            return;
        }

        try {
            ELDialogue.Dialogue confirmDialogue = ELDialogue.createDialogue(FXMLLoader.load(getClass().getResource("DialogueConfirm.fxml")));
            try {
                ((Label) confirmDialogue.anchorPane.lookup("#title")).setText("Are you sure you want to delete this account?");
                ((Label) confirmDialogue.anchorPane.lookup("#content")).setText("“" + accountEntry.displayName + "” will be lost forever! (A long time)");
                //confirmDialogue.highlightDangerousOperation("yes");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            confirmDialogue.setHandler(event -> {
                String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                if (ac.equals("yes")) {
                    accountManager.deleteUser(accountEntry);
                    System.out.println("Deleted User: " + id);
                }
                confirmDialogue.close();
            });
            confirmDialogue.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void activeUser(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getSource();
        String id = button.getId().replace("_ACTIVE", "");

        AccountManager accountManager = EasyLogin.getInstance().getAccountManager();
        AccountEntry accountEntry = accountManager.lookupUserByID(id);
        if (accountEntry == null) {
            System.out.println("User not found: " + id);
            return;
        }
        accountManager.setActive(accountEntry);
        System.out.println("Activated User: " + id);
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
}
