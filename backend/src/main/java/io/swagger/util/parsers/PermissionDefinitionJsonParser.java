package io.swagger.util.parsers;

import io.swagger.model.*;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that converts the raw JSON body of a web script request into a
 * strongly-typed {@link PermissionDefinition} model.
 *
 * <p>The expected JSON payload carries an optional {@code inherited} flag and a
 * {@code permissions} object holding two arrays: {@code profiles} (profile-based
 * permission grants) and {@code users} (user-based permission grants). Each entry
 * associates a {@code permission} string with either a profile or a user
 * descriptor. Malformed or incomplete entries are silently skipped rather than
 * causing the whole parse to fail.
 *
 * <p>This class is a stateless collection of static helpers and is not meant to
 * be instantiated.
 *
 * @author beaurpi
 */
public class PermissionDefinitionJsonParser {

  /** JSON key for a single user descriptor inside a user permission entry. */
  private static final String USER = "user";
  /** JSON key for a single profile descriptor inside a profile permission entry. */
  private static final String PROFILE = "profile";
  /** JSON key for the boolean flag indicating whether permissions are inherited. */
  private static final String INHERITED = "inherited";
  /** JSON key for the object grouping the profile and user permission arrays. */
  private static final String PERMISSIONS = "permissions";
  /** JSON key for the permission name granted to a profile or user. */
  private static final String PERMISSION = "permission";
  /** JSON key for the array of profile permission entries. */
  private static final String PROFILES = "profiles";
  /** JSON key for the array of user permission entries. */
  private static final String USERS = "users";
  /** JSON key for a profile identifier. */
  private static final String ID = "id";
  /** JSON key for a profile group name. */
  private static final String GROUP_NAME = "groupName";
  /** JSON key for a profile display name. */
  private static final String NAME = "name";
  /** JSON key for a user identifier. */
  private static final String USER_ID = "userId";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static
   *     helpers
   */
  private PermissionDefinitionJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Reads the JSON body of the given web script request and builds the
   * corresponding {@link PermissionDefinition}.
   *
   * <p>The {@code inherited} flag defaults to {@code true} when it is absent or
   * {@code null} in the payload. The {@code profiles} and {@code users} arrays
   * under {@code permissions} are parsed into their respective permission
   * collections.
   *
   * @param req the web script request whose content holds the JSON payload
   * @return the populated {@link PermissionDefinition}
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static PermissionDefinition parseJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    PermissionDefinition body = new PermissionDefinition();
    Boolean inherit = (Boolean) json.get(INHERITED);
    body.setInherited(inherit == null || inherit);

    JSONObject perms = (JSONObject) json.get(PERMISSIONS);
    parseProfilePermissions(body, (JSONArray) perms.get(PROFILES));
    parseUserPermissions(body, (JSONArray) perms.get(USERS));
    return body;
  }

  /**
   * Parses the profile permission entries and appends the valid ones to the
   * given permission definition.
   *
   * <p>Non-object array elements are ignored, and an entry is only added when
   * both the profile {@code id} and {@code groupName} are present.
   *
   * @param body the permission definition to populate; must not be {@code null}
   * @param profiles the JSON array of profile permission entries, or
   *     {@code null} if none were provided
   */
  private static void parseProfilePermissions(
    PermissionDefinition body,
    JSONArray profiles
  ) {
    if (profiles == null) {
      return;
    }
    for (Object profile : profiles) {
      if (!(profile instanceof JSONObject)) {
        continue;
      }
      JSONObject profPerm = (JSONObject) profile;
      PermissionDefinitionPermissionsProfiles permissionsProfiles =
        new PermissionDefinitionPermissionsProfiles();
      permissionsProfiles.setPermission(profPerm.get(PERMISSION).toString());

      JSONObject profileJson = (JSONObject) profPerm.get(PROFILE);
      String id = profileJson.get(ID).toString();
      String groupName = profileJson.get(GROUP_NAME).toString();

      Profile p = new Profile();
      p.setId(id);
      p.setGroupName(groupName);
      p.setName(profileJson.get(NAME).toString());
      permissionsProfiles.setProfile(p);

      if (id != null && groupName != null) {
        body.getPermissions().getProfiles().add(permissionsProfiles);
      }
    }
  }

  /**
   * Parses the user permission entries and appends the valid ones to the given
   * permission definition.
   *
   * <p>Non-object array elements are ignored, and an entry is only added when
   * the user {@code userId} is present.
   *
   * @param body the permission definition to populate; must not be {@code null}
   * @param users the JSON array of user permission entries, or {@code null} if
   *     none were provided
   */
  private static void parseUserPermissions(
    PermissionDefinition body,
    JSONArray users
  ) {
    if (users == null) {
      return;
    }
    for (Object user : users) {
      if (!(user instanceof JSONObject)) {
        continue;
      }
      JSONObject userPermission = (JSONObject) user;
      PermissionDefinitionPermissionsUsers permissionsUsers =
        new PermissionDefinitionPermissionsUsers();
      permissionsUsers.setPermission(userPermission.get(PERMISSION).toString());

      JSONObject userJson = (JSONObject) userPermission.get(USER);
      String userId = userJson.get(USER_ID).toString();
      User u = new User();
      u.setUserId(userId);
      permissionsUsers.setUser(u);

      if (userId != null) {
        body.getPermissions().getUsers().add(permissionsUsers);
      }
    }
  }
}
