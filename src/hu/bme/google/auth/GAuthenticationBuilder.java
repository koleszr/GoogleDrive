package hu.bme.google.auth;

import com.google.api.services.drive.Drive;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Zoltán on 2015.05.08..
 *
 * Makes the authentication code flow to Google.
 */
public class GAuthenticationBuilder {


    /**
     * Makes the authentication process.
     */
    public void authenticate() {
        GAuthenticator.restoreCredentials();

        if (GAuthenticator.getRefreshToken() != null && !GAuthenticator.getRefreshToken().equals("")) {
            GAuthenticator.restoreGoogleCredential();
        } else {
            try {
                oauthFlow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Make an OAuth 2.0 authentication flow.
     */
    private void oauthFlow() throws IOException {
        final ServerTask serverTask = new ServerTask();
        startServer(serverTask);

        // Open the default web browser of the system and redirect user to authentication page.
        Desktop.getDesktop().browse(URI.create(GAuthenticator.getAuthenticationURL()));

        listenForCode();
        GAuthenticator.makeGoogleCredential(GAuthenticator.getTokens(ServerTask.getCode()));

        stopServer(serverTask);

        store();
    }

    /**
     * Starts a Jetty web server on a separate thread, which listens to authentication code.
     *
     * @param serverTask object
     */
    private void startServer(ServerTask serverTask) {
        Runnable server = () -> {
            try {
                serverTask.createServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread t = new Thread(server);
        t.start();
    }

    /**
     * Stops the Jetty web server.
     *
     * @param serverTask object
     */
    private void stopServer(ServerTask serverTask) {
        try {
            serverTask.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check every 100 ms if the ServerTask gets the authorization code from Google servers.
     */
    private void listenForCode() {
        while (ServerTask.getCode() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void store() {
        GAuthenticator.storeCredentials();
    }

    /**
     * @return Drive object which can be used to access Google Drive files.
     */
    public Drive getDrive() {
        return GAuthenticator.makeDrive();
    }
}
