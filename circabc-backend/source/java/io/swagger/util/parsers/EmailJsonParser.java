package io.swagger.util.parsers;

import eu.cec.digit.circabc.service.app.message.DistributionEmailDAO;
import io.swagger.model.EmailDefinition;
import io.swagger.model.Profile;
import io.swagger.model.User;
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
public class EmailJsonParser {

    private static final String SUBJECT = "subject";
    private static final String CONTENT = "content";
    private static final String PROFILES = "profiles";
    private static final String ID = "id";
    private static final String GROUP_NAME = "groupName";
    private static final String NAME = "name";
    private static final String USERS = "users";
    private static final String USER_ID = "userId";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String EMAIL = "email";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String ATTACHMENTS = "attachments";

    private EmailJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static EmailDefinition parse(WebScriptRequest req) throws IOException, ParseException {

        EmailDefinition result = new EmailDefinition();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        String subject = (String) json.get(SUBJECT);
        result.setSubject(subject);

        String content = (String) json.get(CONTENT);
        result.setContent(content);

        JSONArray profiles = (JSONArray) json.get(PROFILES);
        if (profiles != null) {
            for (Object profile : profiles) {
                if (profile instanceof JSONObject) {
                    JSONObject prof = (JSONObject) profile;
                    result.getProfiles().add(parseProfile(prof));
                }
            }
        }

        JSONArray users = (JSONArray) json.get(USERS);
        if (users != null) {
            for (Object user : users) {
                if (user instanceof JSONObject) {
                    JSONObject u = (JSONObject) user;
                    result.getUsers().add(parseUser(u));
                }
            }
        }

        JSONArray attachments = (JSONArray) json.get(ATTACHMENTS);
        if (attachments != null) {
            for (Object attachment : attachments) {
                result.getAttachments().add(attachment.toString());
            }
        }

        return result;
    }

    private static User parseUser(JSONObject u) {
        User result = new User();

        String uid = (String) u.get(USER_ID);
        result.setUserId(uid);

        String fname = (String) u.get(FIRSTNAME);
        result.setFirstname(fname);

        String lname = (String) u.get(LASTNAME);
        result.setLastname(lname);

        String email = (String) u.get(EMAIL);
        result.setEmail(email);

        return result;
    }

    public static Profile parseProfile(JSONObject emailProfile) {

        Profile profile = new Profile();
        String id = (String) emailProfile.get(ID);
        profile.setId(id);

        String groupName = (String) emailProfile.get(GROUP_NAME);
        profile.setGroupName(groupName);

        String name = (String) emailProfile.get(NAME);
        profile.setName(name);

        return profile;
    }

    public static List<DistributionEmailDAO> parseDistributionEmails(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(cBody);

        List<DistributionEmailDAO> result = new ArrayList<>();

        if (json != null) {
            for (Object distributionEmail : json) {
                if (distributionEmail instanceof JSONObject) {
                    DistributionEmailDAO item = new DistributionEmailDAO();
                    JSONObject distrib = (JSONObject) distributionEmail;
                    Object idObj = distrib.get(ID);
                    if (idObj != null) {
                        item.setId(Integer.parseInt(idObj.toString()));
                    }
                    Object addrObj = distrib.get(EMAIL_ADDRESS);
                    if (addrObj != null) {
                        item.setEmailAddress(addrObj.toString().toLowerCase());
                    }
                    result.add(item);
                }
            }
        }

        return result;
    }
}
