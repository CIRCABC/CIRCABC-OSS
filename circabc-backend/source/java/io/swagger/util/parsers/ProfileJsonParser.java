package io.swagger.util.parsers;

import io.swagger.model.Profile;
import io.swagger.util.Converter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class ProfileJsonParser {

    private static final String IMPORTED = "imported";
    private static final String EXPORTED = "exported";
    private static final String TITLE = "title";
    private static final String PERMISSIONS = "permissions";
    private static final String INFORMATION = "information";
    private static final String LIBRARY = "library";
    private static final String MEMBERS = "members";
    private static final String EVENTS = "events";
    private static final String NEWSGROUPS = "newsgroups";
    private static final String VISIBILITY = "visibility";

    private ProfileJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static Profile parsePartial(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Profile result = new Profile();

        String id = (String) json.get("id");
        if (id != null) {
            result.setId(id);
        }

        JSONObject titles = (JSONObject) json.get(TITLE);
        if (titles == null) {
            throw new ParseException(0, "Empty title");
        }

        result.setTitle(Converter.toI18NProperty(titles));

        Boolean exported = (Boolean) json.get(EXPORTED);
        if (exported != null) {
            result.setExported(exported);
        }

        Boolean imported = (Boolean) json.get(IMPORTED);
        if (imported != null) {
            result.setImported(imported);
        }

        JSONObject permissions = (JSONObject) json.get(PERMISSIONS);
        if (permissions != null) {

            String information = (String) permissions.get(INFORMATION);
            if (information != null) {
                result.getPermissions().put(INFORMATION, information);
            }

            String library = (String) permissions.get(LIBRARY);
            if (library != null) {
                result.getPermissions().put(LIBRARY, library);
            }

            String members = (String) permissions.get(MEMBERS);
            if (members != null) {
                result.getPermissions().put(MEMBERS, members);
            }

            String events = (String) permissions.get(EVENTS);
            if (events != null) {
                result.getPermissions().put(EVENTS, events);
            }

            String newsgroups = (String) permissions.get(NEWSGROUPS);
            if (newsgroups != null) {
                result.getPermissions().put(NEWSGROUPS, newsgroups);
            }

            String visibility = (String) permissions.get(VISIBILITY);
            if (visibility != null) {
                result.getPermissions().put(VISIBILITY, visibility);
            }
        }

        return result;
    }
}
