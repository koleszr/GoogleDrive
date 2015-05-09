package hu.bme.google.drive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zoltán on 2015.05.08..
 *
 * Collection of methods to operate on Google Drive files.
 */
public class DriveClient {

    private Drive drive;
    private List<File> driveFiles;

    public DriveClient(Drive drive) {
        this.drive = drive;
    }

    /**
     * Get the list of text files from user's Google Drive.
     */
    public void setDriveFiles() throws IOException {
        FileList files = drive.files().list().execute();
        driveFiles = files.getItems()
                .stream().filter(file -> file.getMimeType().contains("text"))
                .collect(Collectors.toList());
    }

    /**
     * @return list of the files stored on Google Drive
     */
    public List<File> getDriveFiles() {
        return driveFiles;
    }

    /**
     * Opens the given Google Drive file.
     * @param file Drive file object
     */
    public void openFileFromDrive(File file) {

    }

    /**
     * Upload file to Drive.
     */
    public void uploadFileToDrive(File file, java.io.File fileContent) throws IOException {
        file.setTitle(fileContent.getName());

        FileContent mediaContent = new FileContent(file.getMimeType(), fileContent);

        drive.files().insert(file, mediaContent).execute();
    }
}
