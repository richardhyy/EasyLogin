package cc.eumc.easylogin.account;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.NEffect;
import cc.eumc.easylogin.authentication.*;
import cc.eumc.easylogin.dialogue.ELDialogue;
import cc.eumc.easylogin.task.GrabSkin;
import cc.eumc.easylogin.task.UserAuthenticate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AccountManager {
    AccountStorage storage;
    File configFile;
    EasyLogin instance;
    final int BUTTON_SHADOW_DEPTH = 7;

    public AccountManager(EasyLogin instance) {
        this.instance = instance;
        try {
            //instance.saveDefaultFile("account.json");
            configFile = new File(instance.getDataFolder(), "account.json");
            FileReader reader = new FileReader(configFile);
            storage = instance.gson.fromJson(reader, AccountStorage.class);
            reader.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (storage == null) {
                storage = new AccountStorage();
            }
            else {
                storage.accounts.forEach(this::addUserToUI);
                setActive(lookupUserByID(storage.activeID));
            }
        }
    }

    public void saveJson() {
        try (FileWriter fw = new FileWriter(configFile)) {
            fw.write(instance.gson.toJson(storage));
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setActive(AccountEntry accountEntry) {
        if (accountEntry == null) {
            return;
        }
        storage.activeID = accountEntry.uuid;
        saveJson();

        storage.accounts.forEach(entry -> {
            Button activeButton = (Button)EasyLogin.userVBox.lookup("#" + entry.uuid + "_ACTIVE");
            if (activeButton != null) {
                if (entry.uuid.equals(accountEntry.uuid)) {
                    activeButton.setText("✓");
                    activeButton.setEffect(NEffect.getInnerShadow(BUTTON_SHADOW_DEPTH));
                }
                else if (!activeButton.getText().isEmpty()) {
                    activeButton.setText("");
                    activeButton.setEffect(NEffect.getDropShadow(BUTTON_SHADOW_DEPTH));
                }
            }
        });
    }

    public void deleteUser(AccountEntry accountEntry) {
        for (Node node : EasyLogin.userVBox.getChildren()) {
            if (node.getId() != null && node.getId().equals(accountEntry.uuid + "_Pane")) {
                EasyLogin.userVBox.setPrefHeight(EasyLogin.userVBox.getPrefHeight() - ((GridPane)node).getPrefHeight() - EasyLogin.userVBox.getSpacing());
                EasyLogin.userVBox.getChildren().remove(node);
                break;
            }
        }

        if (storage.activeID.equals(accountEntry.uuid)) {
            storage.activeID = null;
        }
        storage.accounts.remove(accountEntry);

        saveJson();
    }

    public void loginUser(String username, String password, ELDialogue.Dialogue loginDialogue) {
        AccountEntry accountEntry = new AccountEntry(generateClientToken(), null, null, username, null, null);
        try {
            UserAuthenticate thread = createLoginThread(accountEntry, password, loginDialogue);
            thread.specifyPassword(password);

            authenticationUI(thread, "Verifying", "Just a sec", loginDialogue, 15000);
        }
        catch (AuthException authException) {
            System.out.println(authException);
        }
    }

    /**
     * Refresh authentication
     * @param accountEntry
     */
    public void refreshUserToken(AccountEntry accountEntry) {
        // TODO refresh token & encapsulating "verifying" dialog UI
        try {
            UserAuthenticate thread = new UserAuthenticate(accountEntry.getAuthProvider(), AuthStage.REFRESH, new AuthDone() {
                @Override
                public void done(UserAuthenticate userAuthenticate) {
                    if (userAuthenticate.getNewAccountEntry() != null) {
                        updateUser(accountEntry, userAuthenticate.getNewAccountEntry());
                    }
                }
            });
            authenticationUI(thread, "Validating", "Just a sec", null, 15000);
        } catch (AuthException authException) {
            System.out.println(authException);
        }
    }

    public UserAuthenticate createLoginThread(AccountEntry accountEntry, String password, ELDialogue.Dialogue loginDialogue) throws AuthException {
        AuthType authType = password.isEmpty()? AuthType.OFFLINE : AuthType.MOJANG_YGGDRASIL;
        AuthProvider authProvider;
        switch (authType) {
            case OFFLINE:
                authProvider = new OfflineAuth(accountEntry);
                break;

            case MOJANG_YGGDRASIL:
                authProvider = new MojangYggdrasilAuth(accountEntry);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + authType);
        }

        return new UserAuthenticate(authProvider, AuthStage.AUTHENTICATE, userAuthenticate -> {
            //verifyingDialogue.hide();
            loginDialogue.hide();

            AuthException authException = userAuthenticate.getAuthException();
            if (authException != null) {
                try {
                    ELDialogue.Dialogue dialogueError = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueSingleButton.fxml")));
                    dialogueError.setLabelText("title", "Oops");
                    dialogueError.setLabelText("content", authException.getMessage());
                    dialogueError.setButtonText("button", "OK");
                    dialogueError.setHandler(e -> {
                        dialogueError.hide();
                        loginDialogue.show();
                    });
                    dialogueError.show();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            AccountEntry entry = userAuthenticate.getNewAccountEntry();
            if (entry != null) {
                saveUser(entry);
                addUserToUI(entry);
                loginDialogue.close();
                //verifyingDialogue.close();
            }
        });
    }

    private void authenticationUI(UserAuthenticate authThread, String title, String subtitle, ELDialogue.Dialogue parentDialogue, long timedOut) throws AuthException {
        if (parentDialogue != null) {
            parentDialogue.hide();
        }

        try {
            ELDialogue.Dialogue dialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueCancellable.fxml")));
            dialogue.setLabelText("title", title);
            dialogue.setLabelText("content", subtitle);


            dialogue.setHandler(event -> {
                authThread.stop();
                if (parentDialogue != null) {
                    dialogue.hide();
                    parentDialogue.show();
                }
                else {
                    dialogue.close();
                }
                System.out.println("Cancelled");
            });
            dialogue.show();

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(authThread);

            if (timedOut > 0) {
                try {
                    executorService.shutdown();
                    executorService.awaitTermination(timedOut, TimeUnit.MILLISECONDS);
                    dialogue.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public AccountEntry lookupUserByID(String id) {
        for (AccountEntry accountEntry : storage.accounts) {
            if (accountEntry.uuid.equals(id)) {
                return accountEntry;
            }
        }
        return null;
    }

    /**
     * Update old account entry with a new one, and save account list
     * @param oldAccount
     * @param newAccount
     */
    public void updateUser(AccountEntry oldAccount, AccountEntry newAccount) {
        oldAccount.update(newAccount);
        saveJson();
    }

    public void saveUser(AccountEntry accountEntry) {
        if (storage.accounts == null) {
            storage.accounts = new ArrayList<>();
        }
        storage.accounts.add(accountEntry);
        saveJson();
    }

    public void addUserToUI(AccountEntry accountEntry) {
        try {
            GridPane entryGridPane = FXMLLoader.load(instance.getClass().getResource("UserEntry.fxml"));
            entryGridPane.setId(accountEntry.uuid + "_Pane");

            Button activeButton = (Button) entryGridPane.lookup("#chooseButton");
            activeButton.setId(accountEntry.uuid + "_ACTIVE");
            Button button = (Button) entryGridPane.lookup("#deleteUserButton");
            button.setId(accountEntry.uuid + "_DELETE");
            Label nameLabel = (Label) entryGridPane.lookup("#nameLabel");
            nameLabel.setText(accountEntry.displayName);
            Label emailLabel = (Label) entryGridPane.lookup("#emailLabel");
            emailLabel.setText(accountEntry.authType==AuthType.OFFLINE?"Offline Account":accountEntry.userName);
            ImageView avatarImageView = (ImageView) entryGridPane.lookup("#avatarImageView");
            avatarImageView.setId(accountEntry.uuid + "_AVATAR");

            EasyLogin.userVBox.getChildren().add(entryGridPane);
            EasyLogin.userVBox.setPrefHeight(EasyLogin.userVBox.getPrefHeight() + entryGridPane.getPrefHeight() + EasyLogin.userVBox.getSpacing());
            EasyLogin.userScrollPane.setVvalue(EasyLogin.userScrollPane.getVmax());

            if (storage.accounts.size() == 1) {
                setActive(accountEntry);
            }

            if (accountEntry.userName.equals("Dinnerbone") || accountEntry.userName.equals("Grumm")) {
                entryGridPane.setRotate(180);
            }

            GrabSkin grabSkinThread = new GrabSkin(this);
            grabSkinThread.setFetchList(new ArrayList<>(Collections.singletonList(accountEntry)));
            grabSkinThread.start();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void newUserUI() {
        try {
            ELDialogue.Dialogue dialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueLogin.fxml")));
            dialogue.setHandler(
                e -> {
                    String action;
                    try {
                        action = ((Button) e.getSource()).getId().split("_")[1];
                    } catch (IndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                        return;
                    }

                    switch (action) {
                        case "cancel":
                            dialogue.close();
                            break;
                        case "login":
                            TextField emailField;
                            PasswordField passwordField;
                            try {
                                emailField = (TextField)dialogue.anchorPane.lookup("#emailField");
                                passwordField = (PasswordField)dialogue.anchorPane.lookup("#passwordField");
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                return;
                            }

                            if (emailField.getText().isEmpty()) {
                                dialogue.hide();
                                try {
                                    ELDialogue.Dialogue okDialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueSingleButton.fxml")));
                                    okDialogue.setLabelText("title", "Empty Email");
                                    okDialogue.setLabelText("content", "If you don't have a Minecraft account, input your name.");
                                    okDialogue.setHandler(event -> {
                                        okDialogue.hide();
                                        dialogue.show();
                                    });
                                    okDialogue.show();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                                return;
                            }

                            if (passwordField.getText().isEmpty()) {
                                try {
                                    ELDialogue.Dialogue confirmDialogue = ELDialogue.createDialogue(FXMLLoader.load(instance.getClass().getResource("DialogueConfirm.fxml")));
                                    try {
                                        ((Label)confirmDialogue.anchorPane.lookup("#title")).setText("Empty password");
                                        ((Label)confirmDialogue.anchorPane.lookup("#content")).setText("Add as offline player?");
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                    confirmDialogue.setHandler(event -> {
                                        String ac = ELDialogue.getButtonAction(event.getSource()); // Action
                                        switch (ac) {
                                            case "yes":
                                                loginUser(emailField.getText(), "", dialogue);
                                                break;
                                            case "no":
                                                confirmDialogue.hide();
                                                break;
                                        }

                                        dialogue.show();
                                        confirmDialogue.hide();

                                        System.out.println(ac);
                                    });
                                    dialogue.hide();
                                    confirmDialogue.show();
                                    return;
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                            else {
                                loginUser(emailField.getText(), passwordField.getText(), dialogue);
                            }
                            break;
                    }
                });
            dialogue.show();
            /*
            GridPane entryGridPane = FXMLLoader.load(getClass().getResource("UserEntry.fxml"));
            Button button = (Button)entryGridPane.lookup("#editUserButton");
            button.setId(UUID.randomUUID().toString());
            Main.userVBox.getChildren().add(entryGridPane);
            Main.userVBox.setPrefHeight(Main.userVBox.getPrefHeight() + entryGridPane.getPrefHeight() + Main.userVBox.getSpacing());
            Main.userScrollPane.setVvalue(Main.userScrollPane.getVmax());
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean setAvatar(AccountEntry accountEntry, Image avatar) {
        try {
            ImageView avatarView = (ImageView)EasyLogin.userVBox.lookup("#" + accountEntry.uuid + "_AVATAR");
            avatarView.setImage(avatar);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateClientToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public AccountStorage getStorage() {
        return storage;
    }
}
