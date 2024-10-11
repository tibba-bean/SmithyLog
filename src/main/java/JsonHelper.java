import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonHelper extends SmithyLog {


    public static String REQUEST_PATH = "";
    public static String DETAILS_SIZE;

    public static void getRequestPath() {
        String[] pathChunks = { "src", "main", "resources", "request", "request.json" };
        REQUEST_PATH = String.join(File.separator, pathChunks);
    }

    public static void getJsonDetails(JSONObject object) {
        JSONObject DetailsObject = (JSONObject) object.get("details");
        DETAILS_SIZE = (String) DetailsObject.get("size");
    }

    public static void jsonParse() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader(REQUEST_PATH);
        Object obj = jsonParser.parse(reader);
        getJsonDetails((JSONObject) obj);
        reader.close();
    }
}