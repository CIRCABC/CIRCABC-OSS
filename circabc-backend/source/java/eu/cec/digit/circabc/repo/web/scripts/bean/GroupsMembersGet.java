package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class GroupsMembersGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsMembersGet.class);

    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String searchQuery = req.getParameter("searchQuery");
        List<String> searchProfile = null;
        if (req.getParameter("profile") != null) {
            searchProfile = new ArrayList<>();
            searchProfile.add(req.getParameter("profile"));
        }

        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        String limitStr = req.getParameter("limit");
        Integer limit = null;
        if (limitStr != null) {
            limit = (limitStr.isEmpty() ? 25 : Integer.parseInt(limitStr));
        }

        String pageStr = req.getParameter("page");
        Integer page = null;
        if (pageStr != null) {
            page = (pageStr.isEmpty() ? 1 : Integer.parseInt(pageStr));
        }

        String order = req.getParameter("order");

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

        try {

            if (!this.currentUserPermissionCheckerService.isAlfrescoAdmin()
                    && this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    id, DirectoryPermissions.DIRNOACCESS)
                    && (this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id,
                    LibraryPermissions.LIBMANAGEOWN,
                    LibraryPermissions.LIBEDITONLY,
                    LibraryPermissions.LIBFULLEDIT,
                    LibraryPermissions.LIBADMIN)
                    || this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id,
                    NewsGroupPermissions.NWSPOST,
                    NewsGroupPermissions.NWSMODERATE,
                    NewsGroupPermissions.NWSADMIN))) {
                throw new AccessDeniedException("Not enough rights for get the list of users");
            }

            PagedUserProfile result =
                    this.groupsApi.groupsIdMembersGet(
                            id, searchProfile, language, limit, page, order, searchQuery);
            model.put("data", result.getData());
            model.put("total", result.getTotal());
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
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
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
}
