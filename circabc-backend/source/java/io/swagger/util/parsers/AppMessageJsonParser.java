package io.swagger.util.parsers;

import io.swagger.model.AppMessage;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class AppMessageJsonParser {

    private static final String ID = "id";
    private static final String CONTENT = "content";
    private static final String DATE_CLOSURE = "dateClosure";
    private static final String DISPLAY_TIME = "displayTime";
    private static final String LEVEL = "level";
    private static final String ENABLED = "enabled";

    private AppMessageJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static AppMessage parse(WebScriptRequest req) throws IOException, ParseException {

        AppMessage result = new AppMessage();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Object idObj = json.get(ID);
        if (idObj != null) {
            String idStr = idObj.toString();
            if (!"".equals(idStr)) {
                result.setId(Integer.parseInt(idObj.toString()));
            }
        }

        Object contentObj = json.get(CONTENT);
        if (contentObj != null) {
            result.setContent(contentObj.toString());
        }

        Object dateObj = json.get(DATE_CLOSURE);
        if (dateObj != null) {
            String dateStr = dateObj.toString();
            if (!"".equals(dateStr)) {
                result.setDateClosure(DateTime.parse(dateStr));
            }
        }

        Object levelObj = json.get(LEVEL);
        if (levelObj != null) {
            result.setLevel(levelObj.toString());
        } else {
            result.setLevel("info");
        }

        Object enabledObj = json.get(ENABLED);
        if (enabledObj != null) {
            result.setEnabled(Boolean.valueOf(enabledObj.toString()));
        } else {
            result.setEnabled(false);
        }

        Object timeObj = json.get(DISPLAY_TIME);
        if (timeObj != null) {
            result.setDisplayTime(Integer.parseInt(timeObj.toString()));
        } else {
            result.setDisplayTime(15);
        }

        return result;
    }

    public static Boolean parseBoolean(WebScriptRequest req) throws IOException {

        String cBody = req.getContent().getContent();

        if (cBody != null) {
            return Boolean.parseBoolean(cBody);
        }

        return null;
    }
}
