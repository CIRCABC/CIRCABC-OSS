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
public class PermissionDefinitionJsonParser {

    private static final String USER = "user";
    private static final String PROFILE = "profile";
    private static final String INHERITED = "inherited";
    private static final String PERMISSIONS = "permissions";
    private static final String PERMISSION = "permission";
    private static final String PROFILES = "profiles";
    private static final String USERS = "users";
    private static final String ID = "id";
    private static final String GROUP_NAME = "groupName";
    private static final String NAME = "name";
    private static final String USER_ID = "userId";

    private PermissionDefinitionJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static PermissionDefinition parseJSON(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        PermissionDefinition body = new PermissionDefinition();
        Boolean inherit = (Boolean) json.get(INHERITED);
        if (inherit != null) {
            body.setInherited(inherit);
        } else {
            body.setInherited(true);
        }

        JSONObject perms = (JSONObject) json.get(PERMISSIONS);
        JSONArray profiles = (JSONArray) perms.get(PROFILES);
        if (profiles != null) {
            for (Object profile : profiles) {
                if (profile instanceof JSONObject) {
                    JSONObject profPerm = (JSONObject) profile;
                    PermissionDefinitionPermissionsProfiles permissionsProfiles =
                            new PermissionDefinitionPermissionsProfiles();
                    String permission = profPerm.get(PERMISSION).toString();
                    permissionsProfiles.setPermission(permission);

                    JSONObject profileJson = (JSONObject) profPerm.get(PROFILE);
                    Profile p = new Profile();
                    String id = profileJson.get(ID).toString();
                    p.setId(id);

                    String groupName = profileJson.get(GROUP_NAME).toString();
                    p.setGroupName(groupName);

                    String name = profileJson.get(NAME).toString();
                    p.setName(name);

                    permissionsProfiles.setProfile(p);

                    if (id != null && groupName != null) {
                        body.getPermissions().getProfiles().add(permissionsProfiles);
                    }
                }
            }
        }

        JSONArray users = (JSONArray) perms.get(USERS);
        if (users != null) {
            for (Object user : users) {
                if (user instanceof JSONObject) {
                    JSONObject userPermission = (JSONObject) user;
                    PermissionDefinitionPermissionsUsers permissionsUsers =
                            new PermissionDefinitionPermissionsUsers();
                    String permission = userPermission.get(PERMISSION).toString();
                    permissionsUsers.setPermission(permission);
                    User u = new User();

                    JSONObject userJson = (JSONObject) userPermission.get(USER);
                    String userId = userJson.get(USER_ID).toString();
                    u.setUserId(userId);

                    permissionsUsers.setUser(u);

                    if (userId != null) {
                        body.getPermissions().getUsers().add(permissionsUsers);
                    }
                }
            }
        }
        return body;
    }
}
