package cc.eumc.easylogin.account;

import cc.eumc.easylogin.authentication.AuthProvider;
import cc.eumc.easylogin.authentication.AuthType;
import cc.eumc.easylogin.authentication.MojangYggdrasilAuth;
import cc.eumc.easylogin.authentication.OfflineAuth;

public class AccountEntry {
    public String clientToken;
    public String accessToken;
    public String uuid;
    public String userName;
    public String displayName;
    public AuthType authType;

    public AccountEntry(String clientToken, String accessToken, String uuid, String userName, String displayName, AuthType authType) {
        setClientToken(clientToken);
        setAccessToken(accessToken);
        setUuid(uuid);
        setUserName(userName);
        setDisplayName(displayName);
        setAuthType(authType);
    }

    public void update(AccountEntry newAccountEntry) {
        setClientToken(newAccountEntry.clientToken);
        setAccessToken(newAccountEntry.accessToken);
        setUuid(newAccountEntry.uuid);
        setUserName(newAccountEntry.userName);
        setDisplayName(newAccountEntry.displayName);
        setAuthType(newAccountEntry.authType);
    }

    public AuthProvider getAuthProvider() {
        switch (authType) {
            case OFFLINE:
                return new OfflineAuth(this);

            case MOJANG_YGGDRASIL:
                return new MojangYggdrasilAuth(this);

            default:
                return null;
        }
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null? null : uuid.replace("-", "").toLowerCase();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }
}
