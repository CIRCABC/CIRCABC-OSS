package io.swagger.util.parsers;

import io.swagger.model.*;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that converts the JSON payload of a notification-definition REST request into a
 * {@link NotificationDefinition} domain object.
 *
 * <p>The expected JSON body contains two optional arrays:
 *
 * <ul>
 *   <li>{@code profiles} — per-profile notification settings, each holding a {@code notifications}
 *       value and a nested {@code profile} object ({@code id}, {@code groupName}, {@code name}).
 *   <li>{@code users} — per-user notification settings, each holding a {@code notifications} value
 *       and a nested {@code user} object ({@code userId}).
 * </ul>
 *
 * <p>This class is a stateless utility and cannot be instantiated.
 *
 * @author beaurpi
 */
public class NotificationDefinitionJsonParser {

  /** JSON key for the nested user object inside a user notification entry. */
  private static final String USER = "user";
  /** JSON key for the nested profile object inside a profile notification entry. */
  private static final String PROFILE = "profile";
  /** JSON key holding the notification setting value for a profile or user entry. */
  private static final String NOTIFICATIONS = "notifications";
  /** JSON key for the top-level array of profile notification entries. */
  private static final String PROFILES = "profiles";
  /** JSON key for the top-level array of user notification entries. */
  private static final String USERS = "users";
  /** JSON key for the profile identifier. */
  private static final String ID = "id";
  /** JSON key for the profile group name. */
  private static final String GROUP_NAME = "groupName";
  /** JSON key for the profile display name. */
  private static final String NAME = "name";
  /** JSON key for the user identifier. */
  private static final String USER_ID = "userId";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static methods
   */
  private NotificationDefinitionJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into a {@link NotificationDefinition}.
   *
   * @param req the incoming web script request whose content is the JSON payload to parse
   * @return a {@link NotificationDefinition} populated with the profile and user notification
   *     entries found in the payload
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static NotificationDefinition parseJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    NotificationDefinition body = new NotificationDefinition();
    parseProfileNotifications(body, (JSONArray) json.get(PROFILES));
    parseUserNotifications(body, (JSONArray) json.get(USERS));
    return body;
  }

  private static void parseProfileNotifications(
    NotificationDefinition body,
    JSONArray profiles
  ) {
    if (profiles == null) {
      return;
    }
    for (Object profile : profiles) {
      if (!(profile instanceof JSONObject)) {
        continue;
      }
      JSONObject profileNotification = (JSONObject) profile;
      NotificationDefinitionProfiles ndp = new NotificationDefinitionProfiles();
      ndp.setNotifications(profileNotification.get(NOTIFICATIONS).toString());

      JSONObject profileJson = (JSONObject) profileNotification.get(PROFILE);
      String id = profileJson.get(ID).toString();
      String groupName = profileJson.get(GROUP_NAME).toString();

      Profile p = new Profile();
      p.setId(id);
      p.setGroupName(groupName);
      p.setName(profileJson.get(NAME).toString());
      ndp.setProfile(p);

      if (id != null && groupName != null) {
        body.getProfiles().add(ndp);
      }
    }
  }

  private static void parseUserNotifications(
    NotificationDefinition body,
    JSONArray users
  ) {
    if (users == null) {
      return;
    }
    for (Object user : users) {
      if (!(user instanceof JSONObject)) {
        continue;
      }
      JSONObject userNotification = (JSONObject) user;
      NotificationDefinitionUsers ndu = new NotificationDefinitionUsers();
      ndu.setNotifications(userNotification.get(NOTIFICATIONS).toString());

      JSONObject userJson = (JSONObject) userNotification.get(USER);
      String userId = userJson.get(USER_ID).toString();
      User u = new User();
      u.setUserId(userId);
      ndu.setUser(u);

      if (userId != null) {
        body.getUsers().add(ndu);
      }
    }
  }
}
