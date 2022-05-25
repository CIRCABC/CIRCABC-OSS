package io.swagger.util.parsers;

import io.swagger.model.MultilingualAspectMetadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class MultilingualJsonParser {

    private static final String PIVOT_LANG = "pivotLang";
    private static final String AUTHOR = "author";

    private MultilingualJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static MultilingualAspectMetadata parseAspectMetadata(WebScriptRequest req)
            throws IOException, ParseException {

        MultilingualAspectMetadata result = new MultilingualAspectMetadata();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String lang = String.valueOf(json.get(PIVOT_LANG));
        if (lang != null) {
            result.setPivotLang(lang);
        }

        String author = String.valueOf(json.get(AUTHOR));
        if (author != null) {
            result.setAuthor(author);
        }

        return result;
    }
}
