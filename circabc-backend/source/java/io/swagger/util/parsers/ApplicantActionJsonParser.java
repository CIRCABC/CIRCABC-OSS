package io.swagger.util.parsers;

import io.swagger.model.ApplicantAction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class ApplicantActionJsonParser {

    private static final String MESSAGE = "message";
    private static final String USERNAME = "username";
    private static final String ACTION = "action";

    private ApplicantActionJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static ApplicantAction parseJSON(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        ApplicantAction body = new ApplicantAction();
        body.setUsername(String.valueOf(json.get(USERNAME)));

        body.setAction(String.valueOf(json.get(ACTION)));

        body.setMessage(String.valueOf(json.get(MESSAGE)));

        return body;
    }
}
