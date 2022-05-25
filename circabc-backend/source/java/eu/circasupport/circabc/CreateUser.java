package eu.circasupport.circabc;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.user.UserService;
import io.swagger.api.UsersApi;
import io.swagger.exception.AlreadyExistsException;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateUser extends DeclarativeWebScript {
    private UserService userService;
    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        User user;
        try {
            String cBody = req.getContent().getContent();
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(cBody);

            Object currentIgIdObject = json.get("currentIgId");
            if (!hasCurrentUserPermissionToCreateUser(currentIgIdObject)) {
                throw new AccessDeniedException("Not enough rights for creating a user");
            }

            String userId = String.valueOf(json.get("userId"));

            String firstName = String.valueOf(json.get("firstname"));
            String lastName = String.valueOf(json.get("lastname"));
            String email = String.valueOf(json.get("email"));
            String phone = String.valueOf(json.get("phone"));
            String title = String.valueOf(json.get("title"));
            String companyId = String.valueOf(json.get("companyId"));
            String fax = String.valueOf(json.get("fax"));
            String urlAddress = String.valueOf(json.get("urlAddress"));
            String postalAddress = String.valueOf(json.get("postalAddress"));
            String description = String.valueOf(json.get("description"));
            String password = String.valueOf(json.get("password"));

            user = new User();
            Map<String, String> properties = new HashMap<>();

            user.setUserId(userId);
            user.setFirstname(firstName);
            user.setLastname(lastName);
            user.setEmail(email);
            user.setPhone(phone);
            properties.put("title", title);
            properties.put("fax", fax);
            properties.put("urlAddress", urlAddress);
            properties.put("postalAddress", postalAddress);
            properties.put("description", description);
            properties.put("companyId", companyId);
            properties.put("password", password);

            user.setProperties(properties);

            usersApi.usersPost(user);
        } catch (AlreadyExistsException ex) {
            status.setCode(HttpServletResponse.SC_CONFLICT);
            status.setMessage(ex.getMessage());
            status.setRedirect(true);
            return null;
        } catch (IOException | ParseException ex) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage("Bad body");
            status.setRedirect(true);
            return null;
        }

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        model.put("user", user);
        return model;
    }

    private boolean hasCurrentUserPermissionToCreateUser(Object currentIgIdObject) {
        if (CircabcConfig.ENT) {
            return currentUserPermissionCheckerService.isCircabcAdmin();
        }
        String currentIgId = convertToNonEmptyString(currentIgIdObject);
        if (currentIgId != null) {
            return currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    currentIgId, DirectoryPermissions.DIRMANAGEMEMBERS);
        } else {
            return currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin();
        }
    }

    private String convertToNonEmptyString(Object jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String str = String.valueOf(jsonObject);
        if (str.isEmpty()) {
            return null;
        }

        return str;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
