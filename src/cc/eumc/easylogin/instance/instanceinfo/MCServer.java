package cc.eumc.easylogin.instance.instanceinfo;

public class MCServer {
    public String address;
    public int port;
    public boolean displayFavicon;
    public String favicon;

    public MCServer(String address, int port) {
        this.address = address;
        this.port = port;
        this.displayFavicon = true;
    }
}
