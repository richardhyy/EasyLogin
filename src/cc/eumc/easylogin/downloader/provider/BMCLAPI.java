package cc.eumc.easylogin.downloader.provider;

public class BMCLAPI extends DownloadProvider {

    @Override
    public String convertURL(String originalURL) {
        return originalURL
                .replace("//launchermeta.mojang.com/", "//bmclapi2.bangbang93.com/")
                .replace("//launcher.mojang.com/", "//bmclapi2.bangbang93.com/")
                .replace("//resources.download.minecraft.net/", "//bmclapi2.bangbang93.com/assets/")
                .replace("//libraries.minecraft.net/", "//bmclapi2.bangbang93.com/maven/")
                .replace("//files.minecraftforge.net/maven", "//bmclapi2.bangbang93.com/maven")
                .replace("//dl.liteloader.com/versions/", "//bmclapi.bangbang93.com/maven/com/mumfrey/liteloader/")
                .replace("//meta.fabricmc.net/", "//bmclapi2.bangbang93.com/")
                .replace("//maven.fabricmc.net/", "//bmclapi2.bangbang93.com/");
    }
}
