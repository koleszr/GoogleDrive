package hu.bme.google.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Zoltán on 2015.04.21..
 */
public class Executor {

    public void execute() {

        //GAuthenticator authenticator = new GAuthenticator();

        ServerThread serverThread = new ServerThread();
        Thread t = new Thread(serverThread);
        t.start();

        String url = GAuthenticator.getAuthenticationURL();

        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (ServerThread.getCode() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String code = ServerThread.getCode();

        GoogleTokenResponse response = null;

        try {
            response = GAuthenticator.getTokens(code);
            System.out.println("Access token: " + response.getAccessToken());
            System.out.println("Refresh token: " + response.getRefreshToken());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Credential credential = null;
        try {
            credential = GAuthenticator.storeCredential(response);
            GAuthenticator.setCredential(credential);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Drive drive = GAuthenticator.connectToGoogleDrive();

        try {
            FileList fileList = drive.files().list().execute();
            java.util.List<File> list = fileList.getItems();

            for (File file : list) {
                System.out.print(file.getOriginalFilename());
                System.out.println("\t" + file.getOwnerNames());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverThread.getServer().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
