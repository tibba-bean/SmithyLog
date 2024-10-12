import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SmithyLog {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String SPREADSHEET_ID = "1PwfWecbMkKpP1yzV6F_sQqwvYnGk1g9K_A0j1igb8nc";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String APPLICATION_NAME = "SmithyLog";
    private static String DETAILS_SIZE = "";
    private static int i = 1;
    private static String CURRENT_TIME;
    private static String CURRENT_DATE;

    public static void main(String... args) throws
            IOException,
            ParseException,
            GeneralSecurityException,
            InterruptedException
    {
        JsonHelper.getRequestPath();
        File requestJson = new File(JsonHelper.REQUEST_PATH);
        getSheetsService();
        listenForJsonFile(requestJson);
    }

    public static void listenForJsonFile(File requestJson) throws
            IOException,
            ParseException,
            GeneralSecurityException, InterruptedException {


        while (true) {

            if (requestJson.isFile() && requestJson.canRead()) {
                i++;
                getCurrentTime();
                fetchJsonObjectFromRequest();
                pushDataToGoogleSheet();
                deleteRequestFile(requestJson);
            }
            Thread.sleep(2000);
        }
    }

    public static void getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String currentTime = dateFormat.format(date);
        CURRENT_DATE = currentTime.substring(0, 10);
        CURRENT_TIME = currentTime.substring(11, 19);
    }

    public static void fetchJsonObjectFromRequest() throws IOException, ParseException {
        JsonHelper.jsonParse();
        DETAILS_SIZE = JsonHelper.DETAILS_SIZE;
    }

    public static Credential getSheetsService() throws IOException {
        InputStream in = SmithyLog.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                SmithyLog.HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void pushDataToGoogleSheet() throws IOException, GeneralSecurityException {
        String range = String.format("A%s:D%s", i, i);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getSheetsService())
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        ValueRange body = new ValueRange()
                .setMajorDimension("COLUMNS")
                .setValues(Arrays.asList(
                        Collections.singletonList(i - 1),
                        Collections.singletonList(CURRENT_DATE),
                        Collections.singletonList(CURRENT_TIME),
                        Collections.singletonList(DETAILS_SIZE)));
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static void deleteRequestFile(File requestJson) throws IOException {
        try {
            if (requestJson.isFile() && requestJson.canRead()) {
                if (requestJson.delete()) {
                    System.out.println("Deleted file:" + requestJson);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to delete the file. {}" + e.getMessage());
        }
    }
}


