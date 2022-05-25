package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
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

public class GroupsMembersPut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsMembersPut.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String GROUP_NAME = "groupName";
    private static final String LASTNAME = "lastname";
    private static final String FIRSTNAME = "firstname";
    private static final String EMAIL = "email";
    private static final String USER_ID = "userId";
    private static final String USER = "user";
    private static final String PROFILE = "profile";
    private static final String MEMBERSHIPS = "memberships";
    private static final String ADMIN_NOTIFICATIONS = "adminNotifications";
    private static final String USER_NOTIFICATIONS = "userNotifications";
    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static Profile parseProfileJSON(JSONObject profileObject) {

        Profile result = new Profile();
        result.setId(String.valueOf(profileObject.get(ID)));
        result.setName(String.valueOf(profileObject.get(NAME)));
        result.setGroupName(String.valueOf(profileObject.get(GROUP_NAME)));

        return result;
    }

    private static User parseUserJSON(JSONObject userObject) {
        User result = new User();
        result.setUserId(String.valueOf(userObject.get(USER_ID)));
        result.setEmail(String.valueOf(userObject.get(EMAIL)));
        result.setFirstname(String.valueOf(userObject.get(FIRSTNAME)));
        result.setLastname(String.valueOf(userObject.get(LASTNAME)));

        return result;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

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

        try {
            if (!this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    id, DirectoryPermissions.DIRMANAGEMEMBERS)) {
                throw new AccessDeniedException("Not enough rights for updating a user(s)");
            }
            MembershipPostDefinition body = this.parseBodyJSON(req);
            if (req.getParameter("expirationDate") != null) {
                body.setExpirationDate(Converter.convertStringToDate(req.getParameter("expirationDate")));
            }
            NodeRef groupNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
            this.groupsApi.groupsIdMembersPut(groupNodeRef, body);
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
        } catch (IOException e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage("Bad body");
            status.setRedirect(true);

        } catch (java.text.ParseException | ParseException e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage("Bad body");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    private MembershipPostDefinition parseBodyJSON(WebScriptRequest req)
            throws IOException, ParseException {
        MembershipPostDefinition body = new MembershipPostDefinition();

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);
        body.setAdminNotifications(
                (json.get(GroupsMembersPut.ADMIN_NOTIFICATIONS) == null)
                        ? false
                        : Boolean.valueOf(json.get(ADMIN_NOTIFICATIONS).toString()));
        body.setUserNotifications(
                (json.get(GroupsMembersPut.USER_NOTIFICATIONS) == null)
                        ? false
                        : Boolean.valueOf(json.get(USER_NOTIFICATIONS).toString()));

        JSONArray memberships = (JSONArray) json.get(MEMBERSHIPS);
        for (Object membership1 : memberships) {
            JSONObject membership = (JSONObject) membership1;
            JSONObject user = (JSONObject) membership.get(USER);
            JSONObject profile = (JSONObject) membership.get(PROFILE);

            UserProfile memberTmp = new UserProfile();
            memberTmp.setUser(parseUserJSON(user));
            memberTmp.setProfile(GroupsMembersPut.parseProfileJSON(profile));

            body.getMemberships().add(memberTmp);
        }

        return body;
    }

    /**
     * @return the groupsApi
     */
    public GroupsApi getGroupsApi() {
        return this.groupsApi;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
