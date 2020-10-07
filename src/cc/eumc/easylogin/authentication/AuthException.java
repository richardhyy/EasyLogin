package cc.eumc.easylogin.authentication;

public class AuthException extends Exception {
    String error;
    String errorMessage;
    String cause;

    public AuthException(String error, String errorMessage) {
        this(error, errorMessage, "");
    }

    public AuthException(String error, String errorMessage, String cause) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.cause = cause==null?"":cause;
    }

    @Override
    public String getMessage() {
        return "Error: " + error + "\n" + errorMessage;
    }
}
