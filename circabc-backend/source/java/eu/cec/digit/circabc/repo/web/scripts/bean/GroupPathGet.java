package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CategoriesApi;
import io.swagger.api.GroupsApi;
import io.swagger.api.HeadersApi;
import io.swagger.model.Category;
import io.swagger.model.GroupPath;
import io.swagger.model.Header;
import io.swagger.model.InterestGroup;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GroupPathGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupPathGet.class);

    private GroupsApi groupsApi;
    private HeadersApi headersApi;
    private CategoriesApi categoriesApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String groupId = templateVars.get("id");

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
            if (groupId != null) {

                if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(groupId)) {
                    throw new AccessDeniedException(
                            "Current Authority cannot access group, not enough permission");
                }

                GroupPath result = new GroupPath();

                NodeRef groupRef = Converter.createNodeRefFromId(groupId);
                InterestGroup group = this.groupsApi.getInterestGroupDetails(groupRef, false);
                result.setGroup(group);

                NodeRef categoryRef = nodeService.getPrimaryParent(groupRef).getParentRef();
                Category category = categoriesApi.categoriesIdGet(categoryRef.getId());
                result.setCategory(category);

                Header header = headersApi.getHeaderByCategory(categoryRef.getId());
                result.setHeader(header);

                model.put("groupPath", result);
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
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
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

    /**
     * @param headersApi the headersApi to set
     */
    public void setHeadersApi(HeadersApi headersApi) {
        this.headersApi = headersApi;
    }

    /**
     * @param categoriesApi the categoriesApi to set
     */
    public void setCategoriesApi(CategoriesApi categoriesApi) {
        this.categoriesApi = categoriesApi;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
