package hu.bme.google.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

/**
 * Created by Zoltán on 2015.04.21..
 *
 * Collection of methods to authorize user to Google.
 */
public class GAuthenticator {

    private static final String TOKENS_LOCATION = "C:/Users/Zoltán/IdeaProjects/GoogleDrive/src/tokens.prop";
    private static final String CLIENTSECRET_LOCATION = "C:/Users/Zoltán/IdeaProjects/GoogleDrive/src/client_secret.json";
    private static final String REDIRECT_URI = "http://localhost:8080";

    private static String accessToken = null;
    private static String refreshToken = null;

    private static GoogleAuthorizationCodeFlow flow = null;
    private static HttpTransport httpTransport;
    private static JsonFactory jsonFactory;
    private static GoogleCredential googleCredential;
    private static GoogleClientSecrets clientSecrets;

    static {
        httpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();

        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(CLIENTSECRET_LOCATION));
            clientSecrets = GoogleClientSecrets.load(jsonFactory, in);
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, clientSecrets, Collections.singletonList(DriveScopes.DRIVE))
                    .setAccessType("offline").setApprovalPrompt("force").build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * AccessType: online - web applications, offline - installed applications
     * ApprovalPrompt: auto - web applications, force - installed applications
     *      auto: request auto-approval
     *      force: force the approval UI to show
     *
     *  @return URL to the Google authentication UI
     */
    public static String getAuthenticationURL() {
        return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    }


    /**
     *
     * Returns a Json object for an authorization code, which contains tokens e.g.
     * access and refresh tokens.
     *      access token - access Google API
     *      refresh token - refreshes access token if it expires
     *
     *  @param code returned query parameter by the Google authentication server
     *  @return GoogleTokenResponse object
     */
    public static GoogleTokenResponse getTokens(String code) throws IOException {
        return flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
    }


    /**
     * Make a GoogleCredential object from GoogleTokenResponse object
     *
     * @param tokenResponse object
     */
    public static void makeGoogleCredential(GoogleTokenResponse tokenResponse) {
        googleCredential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(httpTransport).setClientSecrets(clientSecrets).build().setFromTokenResponse(tokenResponse);
    }

    /**
     * Getter for googleCredential object
     */
    public static GoogleCredential getGoogleCredential() {
        return googleCredential;
    }

    /**
     * Get a GoogleCredential object from restored tokens.
     */
    public static void restoreGoogleCredential() {
        googleCredential = new GoogleCredential.Builder().setClientSecrets(clientSecrets)
                .setJsonFactory(jsonFactory).setTransport(httpTransport).build()
                .setRefreshToken(refreshToken).setAccessToken(accessToken);
    }

    /**
     * Store Credential object persistently.
     *
     */
    public static void storeCredentials() {
        Properties googleTokens = new Properties();

        googleTokens.put("access_token", googleCredential.getAccessToken());
        googleTokens.put("refresh_token", googleCredential.getRefreshToken());

        FileWriter writer = null;

        try {
            writer = new FileWriter(TOKENS_LOCATION);
            googleTokens.store(writer, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Retrieve tokens from persistent storage.
     *
     */
    public static void restoreCredentials() {
        Properties googleTokens = new Properties();

        FileReader reader = null;

        try {
            reader = new FileReader(TOKENS_LOCATION);

            googleTokens.load(reader);

            accessToken = googleTokens.getProperty("access_token");
            refreshToken = googleTokens.getProperty("refresh_token");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {

                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Makes a connection to Google Drive.
     *
     * @return Drive object to access user's Drive.
     */
    public static Drive makeDrive() {
        return new Drive.Builder(httpTransport, jsonFactory, googleCredential).setApplicationName("MarkITDown").build();
    }
}
