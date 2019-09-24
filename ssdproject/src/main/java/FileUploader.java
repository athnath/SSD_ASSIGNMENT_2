

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class FileUploader {

    private static final String APP_NAME = "File Uploader";
    private static final JsonFactory JS_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_PATH = "tokens";


    /**
     * Global instance of the scopes required by this application.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SC = Collections.singletonList(DriveScopes.DRIVE);
    private static final String FILE_PATH = "/credentials.json";


    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream input = FileUploader.class.getResourceAsStream(FILE_PATH);

        //check whether the input is a null value
        if (input == null) {
            throw new FileNotFoundException("Resource can not be found: " + FILE_PATH);
        }

        GoogleClientSecrets cs = GoogleClientSecrets.load(JS_FACTORY, new InputStreamReader(input));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JS_FACTORY, cs, SC)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }


     public static void main(String[] args) throws IOException, GeneralSecurityException {
         // Build a new authorized API client service.
         Drive dService;

         final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
         dService = new Drive.Builder(HTTP_TRANSPORT, JS_FACTORY, getCredentials(HTTP_TRANSPORT))
                 .setApplicationName(APP_NAME)
                 .build();

         File fm = new File();
         fm.setName("flower.jpg");
         java.io.File fPath = new java.io.File("flower.jpg");
         FileContent mContent = new FileContent("image/jpg", fPath);
         File f = dService.files().create(fm, mContent)
                 .setFields("id")
                 .execute();
         System.out.println("Your File ID is : " + f.getId());


         // Print the id and names for up to 20 files.
         FileList res = dService.files().list()
                 .setPageSize(20)
                 .setFields("nextPageToken, files(id, name)")
                 .execute();

         List<File> fl = res.getFiles();
         if (fl == null || fl.isEmpty()) {
             System.out.println("Doesn't found files.");
         } else {
             System.out.println("Files:");
             for (File fi : fl) {
                 System.out.printf("%s (%s)\n", fi.getName(), fi.getId());
             }
         }

     }
}
