package cc.eumc.easylogin.authentication;

import cc.eumc.easylogin.account.AccountEntry;

import java.util.List;

public abstract class AuthProvider {
    AccountEntry accountEntry;
    public AuthProvider(AccountEntry accountEntry) {
        this.accountEntry = accountEntry;
    }

    public abstract AccountEntry authenticate(String password) throws AuthException;
    public abstract AccountEntry refresh() throws AuthException;
    public abstract boolean validate();
    public abstract boolean invalidate();
    public abstract List<String> fillLaunchArgs(List<String> args);

    public AccountEntry getAccountEntry() {
        return accountEntry;
    }
}
