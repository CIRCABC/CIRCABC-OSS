package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by filipsl on 19/04/2017.
 */
public class UserPut extends CircabcDeclarativeWebScript {

    public static final String GLOBAL_NOTIFICATION_ENABLED = "globalNotificationEnabled";
    public static final String TITLE = "title";
    public static final String ORGANISATION = "organisation";
    public static final String POSTAL_ADDRESS = "postalAddress";
    public static final String DESCRIPTION = "description";
    public static final String URL_ADDRESS = "urlAddress";
    public static final String SIGNATURE = "signature";
    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(UserPut.class);
    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private AuthenticationService authenticationService;

    private static User parseUserJSON(WebScriptRequest req) throws IOException, ParseException {

        User body = new User();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        Object userId = json.get("userId");
        if (userId != null) {
            body.setUserId(String.valueOf(userId));
        }
        Object firstname = json.get("firstname");
        if (firstname != null) {
            body.setFirstname(String.valueOf(firstname));
        }

        Object lastname = json.get("lastname");
        if (lastname != null) {
            body.setLastname(String.valueOf(lastname));
        }

        Object email = json.get("email");
        if (email != null) {
            body.setEmail(String.valueOf(email));
        }

        Object phone = json.get("phone");
        if (phone != null) {
            body.setPhone(String.valueOf(phone));
        }

        Object uiLang = json.get("uiLang");
        if (uiLang != null) {
            body.setUiLang(String.valueOf(uiLang));
        }

        Object contentFilterLang = json.get("contentFilterLang");
        if (contentFilterLang != null) {
            body.setContentFilterLang(String.valueOf(contentFilterLang));
        }
        Object visibility = json.get("visibility");
        if (visibility instanceof Boolean) {
            body.setVisibility((Boolean) visibility);
        }

        Object avatar = json.get("avatar");
        if (avatar != null) {
            body.setAvatar(String.valueOf(avatar));
        }

        JSONObject suppliedProperties = (JSONObject) json.get("properties");

        // add user's additional properties
        if (suppliedProperties != null) {

            Map<String, String> properties = new HashMap<>();

            if (suppliedProperties.get(TITLE) != null) {
                properties.put(TITLE, String.valueOf(suppliedProperties.get(TITLE)));
            }
            if (suppliedProperties.get(ORGANISATION) != null) {
                properties.put(ORGANISATION, String.valueOf(suppliedProperties.get(ORGANISATION)));
            }
            if (suppliedProperties.get(POSTAL_ADDRESS) != null) {
                properties.put(POSTAL_ADDRESS, String.valueOf(suppliedProperties.get(POSTAL_ADDRESS)));
            }
            if (suppliedProperties.get(DESCRIPTION) != null) {
                properties.put(DESCRIPTION, String.valueOf(suppliedProperties.get(DESCRIPTION)));
            }
            if (suppliedProperties.get("fax") != null) {
                properties.put("fax", String.valueOf(suppliedProperties.get("fax")));
            }
            if (suppliedProperties.get(URL_ADDRESS) != null) {
                properties.put(URL_ADDRESS, String.valueOf(suppliedProperties.get(URL_ADDRESS)));
            }

            if (suppliedProperties.get(GLOBAL_NOTIFICATION_ENABLED) != null) {
                properties.put(
                        GLOBAL_NOTIFICATION_ENABLED,
                        String.valueOf(suppliedProperties.get(GLOBAL_NOTIFICATION_ENABLED)));
            }
            if (suppliedProperties.get(SIGNATURE) != null) {
                properties.put(SIGNATURE, String.valueOf(suppliedProperties.get(SIGNATURE)));
            }

            body.setProperties(properties);
        }

        return body;
    }

    public UsersApi getUsersApi() {
        return this.usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);
        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        try {
            String userId = templateVars.get("userId");
            String currentUser =
                    org.alfresco.repo.security.authentication.AuthenticationUtil.getRunAsUser();

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Impossible to update the LDAP DB info of somebody else");
            }

            if (userId != null && currentUser.equals(userId)) {
                User body = parseUserJSON(req);
                model.put("user", this.usersApi.usersUserIdPut(userId, body));
            }

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Error");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
