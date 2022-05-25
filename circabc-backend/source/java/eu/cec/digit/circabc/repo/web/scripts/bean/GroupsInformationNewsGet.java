package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import io.swagger.api.InformationApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
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

public class GroupsInformationNewsGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsInformationNewsGet.class);

    private static final int START_PAGE = 0;
    private static final int DEFAULT_NUMBER_RESULTS = 25;
    private InformationApi informationApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

        String page = req.getParameter("page");
        int nbPage = START_PAGE;
        if (page != null) {
            nbPage = ((Integer.parseInt(page) == 0) ? 0 : (Integer.parseInt(page) - 1));
        }

        String limit = req.getParameter("limit");
        int nbLimit = DEFAULT_NUMBER_RESULTS;
        if (limit != null) {
            nbLimit = Integer.parseInt(limit);
        }

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

            NodeRef infRef =
                    this.nodeService.getChildByName(
                            Converter.createNodeRefFromId(id), ContentModel.ASSOC_CONTAINS, "Information");

            if (!this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
                    infRef.getId(), InformationPermissions.INFACCESS)) {
                throw new AccessDeniedException("Not enought permissions to get the list of News");
            }

            model.put("news", this.informationApi.groupsIdInformationNewsGet(id, nbLimit, nbPage));
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
     * @return the informationApi
     */
    public InformationApi getInformationApi() {
        return this.informationApi;
    }

    /**
     * @param informationApi the informationApi to set
     */
    public void setInformationApi(InformationApi informationApi) {
        this.informationApi = informationApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return this.nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
