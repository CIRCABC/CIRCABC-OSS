package io.swagger.util.parsers;

import io.swagger.model.KeywordDefinition;
import io.swagger.util.Converter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class KeywordJsonParser {

    private static final String TITLE = "title";
    private static final String ID = "id";

    private KeywordJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static KeywordDefinition parseJsonFullKeyword(WebScriptRequest req)
            throws IOException, ParseException {
        KeywordDefinition body = new KeywordDefinition();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String id = String.valueOf(json.get(ID));
        if (id != null) {
            body.setId(id);
        } else {
            throw new ParseException(0, "Empty ID");
        }

        JSONObject titles = (JSONObject) json.get(TITLE);
        if (titles == null) {
            throw new ParseException(0, "Empty title");
        }

        body.setTitle(Converter.toI18NProperty(titles));

        return body;
    }

    public static KeywordDefinition parseJsonPartialKeyword(WebScriptRequest req)
            throws IOException, ParseException {
        KeywordDefinition body = new KeywordDefinition();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String id = String.valueOf(json.get(ID));
        if (id != null) {
            body.setId(id);
        } else {
            throw new ParseException(0, "Empty ID");
        }

        JSONObject titles = (JSONObject) json.get(TITLE);
        if (titles == null) {
            throw new ParseException(0, "Empty title");
        }

        body.setTitle(Converter.toI18NProperty(titles));

        return body;
    }
}
