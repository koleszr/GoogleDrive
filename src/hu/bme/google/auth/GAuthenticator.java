package hu.bme.google.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Zoltán on 2015.04.21..
 */
public class GAuthenticator {

    private static final String CLIENT_ID = "540686391407-s2f23gr815e7pbhi33meg8fsf5em2b6m.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "0Qx2tNu-s3QwZZvmeF6caZlt";
    private static final String REDIRECT_URI = "http://localhost:8080";

    private static GoogleAuthorizationCodeFlow flow = null;
    private static HttpTransport httpTransport;
    private static JsonFactory jsonFactory;
    private static Credential credential;


    /**
     * AccessType: online - web alkalmazások esetén, offline - telepített alkalmazások esetén
     * ApprovalPrompt: auto - web alkalmazások esetén, force - telepített alkalmazások esetén
     *      auto: automatikus jóváhagyás kérése
     *      force: jóváhagyási UI mutatása mindenképp
     *
     *  @return: URL a Google autentikációs felület eléréséhez
     */
    public static String getAuthenticationURL() {

        httpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();

        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("offline").setApprovalPrompt("force").build();

        return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    }


    /**
     * JSON formátumú objektumot ad vissza, egy authorizációs kódért cserébe,
     * ami tokeneket - accessToken, refreshToken - tartalmaz.
     *      access token - google API elérése
     *      refresh token - access token frissítése, ha lejár az érvényessége
     *
     *  @param: code - a böngésző által visszaadott query paraméter
     *  @return: GoogleTokenResponse objektum
     */
    public static GoogleTokenResponse getTokens(String code) throws IOException {

        return flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
    }

    /**
     * credential mező beállítása
     *
     *  @param: GoogleCredential objektum
     *  @return: GoogleCredential objektum
     */
    public static void setCredential(Credential credential) {

        GAuthenticator.credential = credential;
    }

    /**
     * GoogleCredentials objektum készítése és tárolása.
     *
     * @param: GoogleTokenResponse objektum
     * @return: OAuth 2.0 credentials
     * @throws: IOException
     */
    public static Credential storeCredential(GoogleTokenResponse tokenResponse) throws IOException {
        return flow.createAndStoreCredential(tokenResponse, null);
    }

    /**
     * Kapcsolat létrehozása a Drive-hoz.
     *
     * @return: Drive objektum a felhasználó tárhelyének eléréséhez
     */
    public static Drive connectToGoogleDrive() {

        return new Drive.Builder(httpTransport, jsonFactory, credential).build();
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getClientSecret() {
        return CLIENT_SECRET;
    }

    public static String getRedirectUri() {
        return REDIRECT_URI;
    }
}
