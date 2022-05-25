package io.swagger.util.parsers;

import io.swagger.model.SimpleId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author beaurpi
 */
public class SimpleIdJsonParser {

    private static final String ID = "id";

    private SimpleIdJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static SimpleId parse(WebScriptRequest req) throws IOException, ParseException {

        SimpleId result = new SimpleId();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String id = (String) json.get(ID);
        result.setId(id);

        return result;
    }

    public static List<String> parseListOfId(WebScriptRequest req)
            throws IOException, ParseException {

        List<String> result = new ArrayList<>();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(cBody);

        for (Object aJson : json) {
            if (aJson != null) {
                result.add(aJson.toString().trim());
            }
        }

        return result;
    }
}
