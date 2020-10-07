package cc.eumc.easylogin.task;

import cc.eumc.easylogin.NIcon;
import cc.eumc.easylogin.util.HttpRequest;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class CheckStatus extends Thread {
    final static String[][] checkItems = {
            {"\"sessionserver.mojang.com\":\"green\"", "Session"},
            {"\"account.mojang.com\":\"green\"", "Account"},
            {"\"authserver.mojang.com\":\"green\"", "Auth"},
            {"\"api.mojang.com\":\"green\"", "API"},
    };

    final HBox statusHBox;
    final URL gridPaneURL;

    public CheckStatus(HBox container, URL statusEntryURL) {
        statusHBox = container;
        gridPaneURL = statusEntryURL;
    }

    @Override
    public synchronized void run() {
        System.out.println("Checking status...");
        String json = "";
        try {
            json = HttpRequest.get(new URL("https://status.mojang.com/check"))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim().replace(" ", "");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            String finalJson = json;
            System.out.println(finalJson);
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Background work
                            final CountDownLatch latch = new CountDownLatch(1);
                            Platform.runLater(() -> {
                                statusHBox.getChildren().clear();
                                for (String[] checkItem : checkItems) {
                                    addStatusDisplay(finalJson.contains(checkItem[0]) ? NIcon.CHECK : NIcon.EXCLAMATION, checkItem[1]);
                                }
                            });
                            latch.await();
                            return null;
                        }
                    };
                }
            };
            service.start();

        }

        System.out.println("Finished checking status");
    }

    void addStatusDisplay(String icon, String name) {
        try {
            GridPane entry = FXMLLoader.load(gridPaneURL);

            Label statusIcon = (Label)entry.lookup("#statusIcon");
            statusIcon.setStyle("-fx-shape: \"" + icon + "\"; -fx-background-color: #979797");
            statusIcon.setId(UUID.randomUUID().toString());
            Label statusName = (Label)entry.lookup("#statusName");
            statusName.setText(name);
            statusName.setId(UUID.randomUUID().toString());

            statusHBox.getChildren().add(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
