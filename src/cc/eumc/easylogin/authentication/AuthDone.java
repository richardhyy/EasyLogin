package cc.eumc.easylogin.authentication;

import cc.eumc.easylogin.task.UserAuthenticate;

public interface AuthDone {
    void done(UserAuthenticate userAuthenticate);
}
