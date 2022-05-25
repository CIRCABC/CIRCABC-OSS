package io.swagger.util.parsers;

import io.swagger.model.Node;
import io.swagger.util.SupportedLanguages;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class NodeJsonParser {

    public static final String DYN_ATTR = "dynAttr";
    private static final String URL = "url";
    private static final String MESSAGE = "message";
    private static final String NAME = "name";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String PROPERTIES = "properties";
    private static final String ISSUE_DATE = "issue_date";
    private static final String EXPIRATION_DATE = "expiration_date";
    private static final String REFERENCE = "reference";
    private static final String AUTHOR = "author";
    private static final String MIMETYPE = "mimetype";
    private static final String ENCODING = "encoding";
    private static final String STATUS = "status";
    private static final String SECURITY = "security";

    private NodeJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static Node parseContentJSON(String cBody) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Node body = new Node();
        body.setName(String.valueOf(json.get(NAME)));

        Object titles = json.get(TITLE);

        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Object descriptions = json.get(DESCRIPTION);
        if (descriptions instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) descriptions).containsKey(code)) {
                    body.getDescription().put(code, String.valueOf(((JSONObject) descriptions).get(code)));
                }
            }
        }

        JSONObject properties = (JSONObject) json.get(PROPERTIES);
        body.getProperties().put(ISSUE_DATE, String.valueOf(properties.get(ISSUE_DATE)));
        body.getProperties().put(EXPIRATION_DATE, String.valueOf(properties.get(EXPIRATION_DATE)));
        body.getProperties().put(REFERENCE, String.valueOf(properties.get(REFERENCE)));
        body.getProperties().put(AUTHOR, String.valueOf(properties.get(AUTHOR)));
        body.getProperties().put(MIMETYPE, String.valueOf(properties.get(MIMETYPE)));
        body.getProperties().put(ENCODING, String.valueOf(properties.get(ENCODING)));
        body.getProperties().put(STATUS, String.valueOf(properties.get(STATUS)));
        body.getProperties().put(SECURITY, String.valueOf(properties.get(SECURITY)));
        body.getProperties().put(URL, String.valueOf(properties.get(URL)));

        for (int i = 1; i < 21; i++) {
            if (properties.get(DYN_ATTR + i) != null) {
                body.getProperties().put(DYN_ATTR + i, String.valueOf(properties.get(DYN_ATTR + i)));
            }
        }

        return body;
    }

    public static Node parsePostJSON(String cBody) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Node body = parseContentJSON(cBody);

        JSONObject properties = (JSONObject) json.get(PROPERTIES);
        if (properties.containsKey(MESSAGE)) {
            body.getProperties().put(MESSAGE, String.valueOf(properties.get(MESSAGE)));
        }

        return body;
    }

    public static Node parseBasicJSON(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Node body = new Node();
        body.setName(String.valueOf(json.get(NAME)));

        return body;
    }

    public static Node parseSimpleJSON(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Node body = new Node();
        body.setName(String.valueOf(json.get(NAME)));

        Object titles = json.get(TITLE);
        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Object descriptions = json.get(DESCRIPTION);
        if (descriptions instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) descriptions).containsKey(code)) {
                    body.getDescription().put(code, String.valueOf(((JSONObject) descriptions).get(code)));
                }
            }
        }

        return body;
    }

    public static Node parseUrlBasicJSON(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Node body = new Node();
        body.setName(String.valueOf(json.get(NAME)));

        JSONObject properties = (JSONObject) json.get(PROPERTIES);
        body.getProperties().put(URL, String.valueOf(properties.get(URL)));

        return body;
    }
}
