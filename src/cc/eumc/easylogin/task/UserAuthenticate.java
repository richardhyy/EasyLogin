package cc.eumc.easylogin.task;

import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.account.AccountManager;
import cc.eumc.easylogin.authentication.AuthDone;
import cc.eumc.easylogin.authentication.AuthException;
import cc.eumc.easylogin.authentication.AuthProvider;
import cc.eumc.easylogin.authentication.AuthStage;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.CountDownLatch;

public class UserAuthenticate extends Thread {
    AuthProvider authProvider;
    AuthStage authStage;
    String password;
    AuthDone done;

    AccountEntry newAccountEntry;
    boolean result;
    AuthException authException;

    public UserAuthenticate(AuthProvider authProvider, AuthStage authStage) {
        this(authProvider, authStage, null);
    }

    public UserAuthenticate(AuthProvider authProvider, AuthStage authStage, AuthDone authDoneEvent) {
        this.authProvider = authProvider;
        this.authStage = authStage;
        this.done = authDoneEvent;
    }

    public void specifyPassword(String password) {
        this.password = password;
    }

    @Override
    public synchronized void run() {
        System.out.println("Verifying account @ " + authStage.toString() + " ...");

        try {
            switch (authStage) {
                case AUTHENTICATE:
                    newAccountEntry = authProvider.authenticate(password==null? "" : password);
                    break;
                case VALIDATE:
                    result = authProvider.validate();
                    break;
                case REFRESH:
                    newAccountEntry = authProvider.refresh();
                    break;
                case INVALIDATE:
                    result = authProvider.invalidate();
            }
        } catch (AuthException e) {
            e.printStackTrace();
            authException = e;
        }

        if (done != null) {
            UserAuthenticate userAuthenticateInstance = this;
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            final CountDownLatch latch = new CountDownLatch(1);
                            Platform.runLater(() -> {
                                done.done(userAuthenticateInstance);
                            });
                            latch.await();
                            return null;
                        }
                    };
                }
            };
            service.start();
        }

        System.out.println("Verification finished");
    }

    public AccountEntry getNewAccountEntry() {
        return newAccountEntry;
    }

    public boolean getResult() {
        return result;
    }

    public AuthException getAuthException() {
        return authException;
    }
}
