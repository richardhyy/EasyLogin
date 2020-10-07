package cc.eumc.easylogin.authentication;

import cc.eumc.easylogin.account.AccountEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OfflineAuth extends AuthProvider {
    public OfflineAuth(AccountEntry accountEntry) {
        super(accountEntry);
        System.out.println(accountEntry);
    }

    @Override
    public AccountEntry authenticate(String password) throws AuthException {
        getAccountEntry().setUuid(UUID.nameUUIDFromBytes(("OfflinePlayer:" + getAccountEntry().userName).getBytes()).toString());
        getAccountEntry().setDisplayName(getAccountEntry().userName);
        getAccountEntry().setClientToken(UUID.randomUUID().toString());
        getAccountEntry().setAccessToken(getAccountEntry().clientToken);
        getAccountEntry().setAuthType(AuthType.OFFLINE);
        return getAccountEntry();
    }

    @Override
    public AccountEntry refresh() throws AuthException {
        return getAccountEntry();
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean invalidate() {
        return true;
    }

    @Override
    public List<String> fillLaunchArgs(List<String> args) {
        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("${auth_player_name}", getAccountEntry().displayName);
        placeholderMap.put("${auth_uuid}", getAccountEntry().uuid);
        placeholderMap.put("${auth_access_token}", getAccountEntry().accessToken);
        placeholderMap.put("${user_type}", "mojang");

        for (int i=0; i<args.size(); i++) {
            if (placeholderMap.containsKey(args.get(i))) {
                args.set(i, placeholderMap.get(args.get(i)));
            }
        }

        return args;
    }
}
