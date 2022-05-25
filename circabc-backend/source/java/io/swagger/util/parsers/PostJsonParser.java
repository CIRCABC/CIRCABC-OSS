package io.swagger.util.parsers;

import io.swagger.model.Comment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author beaurpi
 */
public class PostJsonParser {

    private static final String TEXT = "text";

    private PostJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static Comment parsePartial(String cBody) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String text = json.get(TEXT).toString();

        Comment result = new Comment();
        result.setText(text);

        return result;
    }
}
