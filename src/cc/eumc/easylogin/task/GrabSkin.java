package cc.eumc.easylogin.task;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.authentication.AuthType;
import cc.eumc.easylogin.util.HttpRequest;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GrabSkin extends Thread {
    final String skinAPI = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
    AccountManager accountManager;
    List<AccountEntry> fetchList;
    int sideScale = 32;

    public GrabSkin(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void setFetchList(List<AccountEntry> fetchList) {
        this.fetchList = fetchList;
    }

    public void setSideScale(int sideScale) {
        this.sideScale = sideScale;
    }

    @Override
    public synchronized void run() {
        System.out.println("Grabbing skins...");

        File skinFolder = new File(EasyLogin.getInstance().getDataFolder(), "skins");
        if (!skinFolder.exists()) {
            skinFolder.mkdir();
        }

        for (AccountEntry account : fetchList==null? accountManager.getStorage().accounts : fetchList) {
            if (account.authType == AuthType.MOJANG_YGGDRASIL) {
                byte[] skin = getSkin(account.uuid);

                File skinFile = new File(skinFolder, account.uuid + ".png");
                if (skin == null || skin.length == 0) {
                    try {
                        skin = Files.readAllBytes(skinFile.toPath());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        continue;
                    }
                }
                else {
                    try (FileOutputStream fos = new FileOutputStream(skinFile)) {
                        fos.write(skin);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        continue;
                    }
                }

                Image avatarImage = new Image(new ByteArrayInputStream(skin));
                Image face = getFace(avatarImage);

                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(() -> {
                            accountManager.setAvatar(account, face);
                        });
                        latch.await();
                        return null;
                        }
                    };
                    }
                };
                service.start();

                System.out.println("Finished grabbing skins");
            }
        }
    }

    byte[] getSkin(String uuid) {
        try {
            String json = HttpRequest.get(new URL(String.format(skinAPI, uuid)))
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();

            ProfileResponse profileResponse = EasyLogin.getInstance().gson.fromJson(json, ProfileResponse.class);
            if (profileResponse != null && profileResponse.properties.length > 0) {
                String profileJson = new String(Base64.getDecoder().decode(profileResponse.properties[0].value));
                Profile profile = EasyLogin.getInstance().gson.fromJson(profileJson, Profile.class);
                if (profile == null) return null;
                URL skinURL = new URL(profile.textures.SKIN.url);
                return HttpRequest.get(skinURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asBytes();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    Image getFace(Image skin) {
        if (sideScale%8 != 0) {
            throw new IllegalArgumentException("sideScale must be a multiple of 8");
        }

        PixelReader pixelReader = skin.getPixelReader();
        WritableImage canvas = new WritableImage(sideScale, sideScale);
        WritableImage face = new WritableImage(pixelReader, 8, 8, 8, 8);
        WritableImage outerLayer = new WritableImage(pixelReader, 40, 8, 8, 8);

        for (int x=0; x<8; x++) {
            for (int y=0; y<8; y++) {
                Color color = outerLayer.getPixelReader().getColor(x, y);
                if (color.getOpacity() >= 0.6) {
                    face.getPixelWriter().setColor(x, y, color);
                }
            }
        }

        int step = sideScale/8;
        for (int x=0; x<sideScale; x++) {
            for (int y=0; y<sideScale; y++) {
                canvas.getPixelWriter().setColor(x, y, face.getPixelReader().getColor(x/step, y/step));
            }
        }

        return canvas;
    }
}

/**
 * Example Response:
 * {
 *   "id" : "4566e69fc90748ee8d71d7ba5aa00d20",
 *   "name" : "Thinkofdeath",
 *   "properties" : [ {
 *     "name" : "textures",
 *     "value" : "ewogICJ0aW1lc3RhbXAiIDogMTU4OTUzNDgxMzg1OCwKICAicHJvZmlsZUlkIiA6ICI0NTY2ZTY5ZmM5MDc0OGVlOGQ3MWQ3YmE1YWEwMGQyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGlua29mZGVhdGgiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRkMWUwOGIwYmI3ZTlmNTkwYWYyNzc1ODEyNWJiZWQxNzc4YWM2Y2VmNzI5YWVkZmNiOTYxM2U5OTExYWU3NSIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBjYzA4ODQwNzAwNDQ3MzIyZDk1M2EwMmI5NjVmMWQ2NWExM2E2MDNiZjY0YjE3YzgwM2MyMTQ0NmZlMTYzNSIKICAgIH0KICB9Cn0="
 *   } ]
 * }
 */
class ProfileResponse {
    String id;
    String name;
    Properties[] properties;

    static class Properties {
        String name;
        String value;
        String signature;
    }
}


/**
 {
 "timestamp" : 1589537943296,
 "profileId" : "4566e69fc90748ee8d71d7ba5aa00d20",
 "profileName" : "Thinkofdeath",
 "textures" : {
 "SKIN" : {
 "url" : "http://textures.minecraft.net/texture/74d1e08b0bb7e9f590af27758125bbed1778ac6cef729aedfcb9613e9911ae75"
 },
 "CAPE" : {
 "url" : "http://textures.minecraft.net/texture/b0cc08840700447322d953a02b965f1d65a13a603bf64b17c803c21446fe1635"
 }
 }
 }
 */
class Profile {
    String timestamp;
    String profileId;
    String profileName;
    Textures textures;
}

class Textures {
    Texture SKIN;
    Texture CAPE;
}

class Texture {
    String url;
}