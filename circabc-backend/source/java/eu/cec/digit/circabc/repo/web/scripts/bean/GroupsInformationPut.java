package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import io.swagger.api.InformationApi;
import io.swagger.model.InformationPage;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InformationJsonParser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroupsInformationPut extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsInformationPut.class);

    private InformationApi informationApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");
        try {
            NodeRef infRef =
                    this.nodeService.getChildByName(
                            Converter.createNodeRefFromId(id), ContentModel.ASSOC_CONTAINS, "Information");
            if (!this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
                    infRef.getId(), InformationPermissions.INFADMIN)) {
                throw new AccessDeniedException("Not enought permissions on the News node");
            }

            InformationPage body = InformationJsonParser.parse(req);
            this.informationApi.groupsIdInformationPut(id, body);

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request - InvalidNodeRef");
            status.setRedirect(true);
            return null;
        } catch (IOException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request - bad URL");
            status.setRedirect(true);
            return null;
        } catch (ParseException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request - parse error");
            status.setRedirect(true);
            return null;
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

    public NodeService getNodeService() {
        return this.nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
