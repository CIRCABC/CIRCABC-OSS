package io.swagger.util.parsers;

import io.swagger.model.AdminContactRequest;
import io.swagger.model.Category;
import io.swagger.util.SupportedLanguages;
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
public class CategoryJsonParser {

    private static final String CONTENT = "content";
    private static final String ID = "ID";
    private static final String NAME = "name";
    private static final String SEND_COPY = "sendCopy";
    private static final String TITLE = "title";
    private static final String USE_SINGLE_CONTACT = "useSingleContact";
    private static final String CONTACT_EMAILS = "contactEmails";

    private CategoryJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static Category parseSimpleJSON(WebScriptRequest req) throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Category body = new Category();
        body.setName(String.valueOf(json.get(NAME)));
        body.setId(String.valueOf(json.get(ID)));

        Object titles = json.get(TITLE);
        if (titles instanceof JSONObject) {
            for (String code : SupportedLanguages.availableLangCodes) {
                if (((JSONObject) titles).containsKey(code)) {
                    body.getTitle().put(code, String.valueOf(((JSONObject) titles).get(code)));
                }
            }
        }

        Object useSingleContact = json.get(USE_SINGLE_CONTACT);
        if (useSingleContact != null) {
            body.setUseSingleContact(Boolean.parseBoolean(useSingleContact.toString()));
        }

        Object contactEmails = json.get(CONTACT_EMAILS);
        if (contactEmails instanceof JSONArray) {
            JSONArray emails = (JSONArray) contactEmails;
            List<String> emailList = new ArrayList<>();
            for (Object email : emails) {
                if (!"".equals(email)) {
                    emailList.add(email.toString());
                }
            }
            body.setContactEmails(emailList);
        }

        return body;
    }

    public static AdminContactRequest parseAdminContactRequest(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        AdminContactRequest result = new AdminContactRequest();
        Object content = json.get(CONTENT);
        if (content != null) {
            result.setContent(content.toString());
        }

        Object sendCopy = json.get(SEND_COPY);
        if (sendCopy != null) {
            result.setSendCopy(Boolean.parseBoolean(sendCopy.toString()));
        }

        return result;
    }
}
