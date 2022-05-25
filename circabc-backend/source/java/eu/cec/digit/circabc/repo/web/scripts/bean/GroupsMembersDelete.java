package eu.cec.digit.circabc.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.api.NotificationsApi;
import io.swagger.model.InterestGroup;
import io.swagger.util.CurrentUserPermissionCheckerService;

public class GroupsMembersDelete extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsMembersDelete.class);

    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NotificationsApi notificationsApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");
        String userId = templateVars.get("userId");

        try {

            if (!(this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    id, DirectoryPermissions.DIRMANAGEMEMBERS)
                    || this.currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))) {
                throw new AccessDeniedException("Not enough rights for deleting a user");
            }
            
            //DIGITCIRCABC-5060 If the user subscribed to Notifications from Newsgroup and/or Library, remove the subscription(s)
            //-->
            InterestGroup ig = groupsApi.getInterestGroup(id);
            
            //check if we need to remove Newsgroup notifications for this user
            String newsGroupId = ig.getNewsgroupId();
            if(this.notificationsApi.isUsersubscribedForNotification(newsGroupId,userId)) {
            	this.notificationsApi.removeNotification(newsGroupId, userId);
            }
            //check if we need to remove library notifications for this user
            String libraryId = ig.getLibraryId();
            if(this.notificationsApi.isUsersubscribedForNotification(libraryId,userId)) {
            	this.notificationsApi.removeNotification(libraryId, userId);
            }
            //<--
            
            //remove the user from the Interest Group
            this.groupsApi.groupsIdMembersUserIdDelete(id, userId);
            
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
        }

        return model;
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

    public void setNotificationsApi(NotificationsApi notificationsApi) {
        this.notificationsApi = notificationsApi;
    }
}
