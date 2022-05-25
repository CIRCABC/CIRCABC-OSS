package io.swagger.util.parsers;

import io.swagger.model.InformationPage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class InformationJsonParser {

    private static final String ADAPT = "adapt";
    private static final String DISPLAY_OLD_INFO = "displayOldInformation";

    private InformationJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static InformationPage parse(WebScriptRequest req) throws IOException, ParseException {
        InformationPage body = new InformationPage();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Boolean adapt = (Boolean) json.get(ADAPT);
        if (adapt != null) {
            body.setAdapt(adapt);
        }

        Boolean displayOldInformation = (Boolean) json.get(DISPLAY_OLD_INFO);
        if (displayOldInformation != null) {
            body.setDisplayOldInformation(displayOldInformation);
        }

        return body;
    }
}
