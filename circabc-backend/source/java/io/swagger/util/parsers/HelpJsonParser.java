package io.swagger.util.parsers;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.util.SupportedLanguages;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class HelpJsonParser {

    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String HREF = "href";
    private static final String ID = "id";

    private HelpJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static HelpCategory parseCategory(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        HelpCategory body = new HelpCategory();
        if (json.get(ID) != null) {
            body.setId(String.valueOf(json.get(ID)));
        }

        Object titles = json.get(TITLE);
        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        return body;
    }

    public static HelpArticle parseArticle(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        HelpArticle body = new HelpArticle();
        if (json.get(ID) != null) {
            body.setId(String.valueOf(json.get(ID)));
        }

        Object titles = json.get(TITLE);
        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Object contents = json.get(CONTENT);
        if (contents instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) contents).containsKey(code)) {
                    body.getContent().put(code, String.valueOf(((JSONObject) contents).get(code)));
                }
            }
        }

        return body;
    }

    public static HelpLink parseLink(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        HelpLink body = new HelpLink();
        if (json.get(ID) != null) {
            body.setId(String.valueOf(json.get(ID)));
        }

        Object titles = json.get(TITLE);
        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Object href = json.get(HREF);
        if (href != null) {
            body.setHref((String) href);
        }

        return body;
    }
}
