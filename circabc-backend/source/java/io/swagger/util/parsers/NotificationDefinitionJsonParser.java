package io.swagger.util.parsers;

import io.swagger.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class NotificationDefinitionJsonParser {

    private static final String USER = "user";
    private static final String PROFILE = "profile";
    private static final String NOTIFICATIONS = "notifications";
    private static final String PROFILES = "profiles";
    private static final String USERS = "users";
    private static final String ID = "id";
    private static final String GROUP_NAME = "groupName";
    private static final String NAME = "name";
    private static final String USER_ID = "userId";

    private NotificationDefinitionJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static NotificationDefinition parseJSON(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        NotificationDefinition body = new NotificationDefinition();

        JSONArray profiles = (JSONArray) json.get(PROFILES);
        if (profiles != null) {
            for (Object profile : profiles) {
                if (profile instanceof JSONObject) {
                    JSONObject profileNotification = (JSONObject) profile;
                    NotificationDefinitionProfiles ndp = new NotificationDefinitionProfiles();
                    String notifications = profileNotification.get(NOTIFICATIONS).toString();
                    ndp.setNotifications(notifications);

                    JSONObject profileJson = (JSONObject) profileNotification.get(PROFILE);
                    Profile p = new Profile();
                    String id = profileJson.get(ID).toString();
                    p.setId(id);

                    String groupName = profileJson.get(GROUP_NAME).toString();
                    p.setGroupName(groupName);

                    String name = profileJson.get(NAME).toString();
                    p.setName(name);

                    ndp.setProfile(p);

                    if (id != null && groupName != null) {
                        body.getProfiles().add(ndp);
                    }
                }
            }
        }

        JSONArray users = (JSONArray) json.get(USERS);
        if (users != null) {
            for (Object user : users) {
                if (user instanceof JSONObject) {
                    JSONObject userNotification = (JSONObject) user;
                    NotificationDefinitionUsers ndu = new NotificationDefinitionUsers();
                    String notifications = userNotification.get(NOTIFICATIONS).toString();
                    ndu.setNotifications(notifications);
                    User u = new User();

                    JSONObject userJson = (JSONObject) userNotification.get(USER);
                    String userId = userJson.get(USER_ID).toString();
                    u.setUserId(userId);

                    ndu.setUser(u);

                    if (userId != null) {
                        body.getUsers().add(ndu);
                    }
                }
            }
        }

        return body;
    }
}
