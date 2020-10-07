package cc.eumc.easylogin.authentication;

import cc.eumc.easylogin.EasyLogin;
import cc.eumc.easylogin.account.AccountEntry;
import cc.eumc.easylogin.util.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MojangYggdrasilAuth extends AuthProvider {
    final String SERVER_URL = "https://authserver.mojang.com/";
    final String AUTHENTICATE = "authenticate";
    final String REFRESH = "refresh";
    final String VALIDATE = "validate";
    final String INVALIDATE = "invalidate";

    final String AUTHENTICATE_REQUEST_JSON = "{\n" +
            "\"agent\": {\n" +
            "\"name\": \"Minecraft\",\n" +
            "\"version\": 1\n" +
            "},\n" +
            "\"username\": \"<username>\",\n" +
            "\"password\": \"<password>\",\n" +
            "\"clientToken\": \"<clientToken>\",\n" +
            "\"requestUser\": true\n" +
            "}";

    final String SIMPLE_REQUEST_JSON = "{\n" +
            "\"accessToken\": \"<accessToken>\",\n" +
            "\"clientToken\": \"<clientToken>\"\n" +
            "}";

    public MojangYggdrasilAuth(AccountEntry accountEntry) {
        super(accountEntry);
    }

    @Override
    public AccountEntry authenticate(String password) throws AuthException {
        getAccountEntry().setAuthType(AuthType.MOJANG_YGGDRASIL);

        String sendData = AUTHENTICATE_REQUEST_JSON.replace("<username>", getAccountEntry().userName)
                                      .replace("<password>", password)
                                      .replace("<clientToken>", getAccountEntry().clientToken);

        try {
            String responseJson = HttpRequest.post(new URL(SERVER_URL + AUTHENTICATE))
                    .header("Content-Type", "application/json")
                    .header("charset", "utf-8")
                    .body(sendData)
                    .execute()
                    .returnContent()
                    .asString("UTF-8").trim();
            System.out.println(responseJson);

            AuthenticateResponse authenticateResponse = EasyLogin.getInstance().gson.fromJson(responseJson, AuthenticateResponse.class);
            if (authenticateResponse == null || authenticateResponse.selectedProfile == null) {
                generateAuthExceptionFromJson(responseJson);
            }
            else {
                System.out.println(authenticateResponse);

                getAccountEntry().setDisplayName(authenticateResponse.selectedProfile.name);
                getAccountEntry().setUuid(authenticateResponse.selectedProfile.id);
                getAccountEntry().setAccessToken(authenticateResponse.accessToken);
                getAccountEntry().setClientToken(authenticateResponse.clientToken);
                return getAccountEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AccountEntry refresh() throws AuthException {
        String sendData = SIMPLE_REQUEST_JSON
                .replace("<accessToken>", getAccountEntry().accessToken)
                .replace("<clientToken>", getAccountEntry().clientToken);

        try {
            String responseJson = HttpRequest.post(new URL(SERVER_URL + REFRESH))
                    .header("Content-Type", "application/json")
                    .header("charset", "utf-8")
                    .body(sendData)
                    .execute()
                    .returnContent()
                    .asString("UTF-8").trim();
            System.out.println(responseJson);

            AuthenticateResponse authenticateResponse = EasyLogin.getInstance().gson.fromJson(responseJson, AuthenticateResponse.class);
            if (authenticateResponse == null || authenticateResponse.accessToken == null) {
                generateAuthExceptionFromJson(responseJson);
            }
            else {
                System.out.println(authenticateResponse);
                getAccountEntry().setAccessToken(authenticateResponse.accessToken);
                getAccountEntry().setClientToken(authenticateResponse.clientToken);
                return getAccountEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean validate() {
        String sendData = SIMPLE_REQUEST_JSON
                .replace("<accessToken>", getAccountEntry().accessToken)
                .replace("<clientToken>", getAccountEntry().clientToken);

        try {
            // Returns an empty payload if successful.
            HttpRequest.post(new URL(SERVER_URL + VALIDATE))
                    .header("Content-Type", "application/json")
                    .header("charset", "utf-8")
                    .body(sendData)
                    .execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean invalidate() {
        String sendData = SIMPLE_REQUEST_JSON
                .replace("<accessToken>", getAccountEntry().accessToken)
                .replace("<clientToken>", getAccountEntry().clientToken);

        try {
            // Returns an empty payload if successful.
            HttpRequest.post(new URL(SERVER_URL + INVALIDATE))
                    .header("Content-Type", "application/json")
                    .header("charset", "utf-8")
                    .body(sendData)
                    .execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

    void generateAuthExceptionFromJson(String json) throws AuthException {
        ErrorResponse errorResponse = EasyLogin.getInstance().gson.fromJson(json, ErrorResponse.class);
        if (errorResponse == null) {
            throw new AuthException("Unexpected Response", "The server responded with the following message: " + json);
        }
        else {
            throw new AuthException(errorResponse.error, errorResponse.errorMessage, errorResponse.cause);
        }
    }

    /**
     * Example
     * {
     *     "accessToken": "random access token",      // hexadecimal or JSON-Web-Token (unconfirmed) [The normal accessToken can be found in the payload of the JWT (second by '.' separated part as Base64 encoded JSON object), in key "yggt"]
     *     "clientToken": "client identifier",        // identical to the one received
     *     "availableProfiles": [                     // only present if the agent field was received
     *         {
     *             "agent": "minecraft",              // Presumably same value as before
     *             "id": "profile identifier",        // hexadecimal
     *             "name": "player name",
     *             "userId": "hex string",
     *             "createdAt": 1325376000000,        // Milliseconds since Jan 1 1970
     *             "legacyProfile": true or false,    // Present even when false
     *             "suspended": true or false,        // probably false
     *             "paid": true or false,             // probably true
     *             "migrated": true or false,         // Seems to be false even for migrated accounts...?  (https://bugs.mojang.com/browse/WEB-1461)
     *             "legacy": true or false            // Only appears in the response if true. Default to false.  Redundant to the newer legacyProfile...
     *         }
     *     ],
     *     "selectedProfile": {                       // only present if the agent field was received
     *         "id": "uuid without dashes",
     *         "name": "player name",
     *         "userId": "hex string",
     *         "createdAt": 1325376000000,
     *         "legacyProfile": true or false,
     *         "suspended": true or false,
     *         "paid": true or false,
     *         "migrated": true or false,
     *         "legacy": true or false
     *     },
     *     "user": {                                  // only present if requestUser was true in the request payload
     *         "id": "user identifier",               // hexadecimal
     *         "email": "user@email.example",         // Hashed(?) value for unmigrated accounts
     *         "username": "user@email.example",      // Regular name for unmigrated accounts, email for migrated ones
     *         "registerIp": "198.51.100.*",          // IP address with the last digit censored
     *         "migratedFrom": "minecraft.net",
     *         "migratedAt": 1420070400000,
     *         "registeredAt": 1325376000000,         // May be a few minutes earlier than createdAt for profile
     *         "passwordChangedAt": 1569888000000,
     *         "dateOfBirth": -2208988800000,
     *         "suspended": false,
     *         "blocked": false,
     *         "secured": true,
     *         "migrated": false,                     // Seems to be false even when migratedAt and migratedFrom are present...
     *         "emailVerified": true,
     *         "legacyUser": false,
     *         "verifiedByParent": false,
     *         "properties": [
     *             {
     *                 "name": "preferredLanguage",   // might not be present for all accounts
     *                 "value": "en"                  // Java locale format (https://docs.oracle.com/javase/8/docs/api/java/util/Locale.html#toString--)
     *             },
     *             {
     *                 "name": "twitch_access_token", // only present if a twitch account is associated (see https://account.mojang.com/me/settings)
     *                 "value": "twitch oauth token"  // OAuth 2.0 Token; alphanumerical; e.g. https://api.twitch.tv/kraken?oauth_token=[...]
     *                                                // the Twitch API is documented here: https://github.com/justintv/Twitch-API
     *             }
     *         ]
     *     }
     * }
     */
    class AuthenticateResponse {
        String accessToken;
        String clientToken;
        //Profile[] availableProfiles;
        Profile selectedProfile;

        /* Example
         *     "selectedProfile": {                       // only present if the agent field was received
         *         "id": "uuid without dashes",
         *         "name": "player name",
         *         "userId": "hex string",
         *         "createdAt": 1325376000000,
         *         "legacyProfile": true or false,
         *         "suspended": true or false,
         *         "paid": true or false,
         *         "migrated": true or false,
         *         "legacy": true or false
         *     },
         */
        class Profile {
            String id;
            String name;
            String userId;
            long createdAt;
            boolean legacyProfile;
            boolean suspended;
            boolean paid;
            boolean migrated;
            boolean legacy;
        }
    }

    /*
    {
    "error": "Short description of the error",
    "errorMessage": "Longer description which can be shown to the user",
    "cause": "Cause of the error" // optional
    }
     */
    class ErrorResponse {
        String error;
        String errorMessage;
        String cause;
    }
}
