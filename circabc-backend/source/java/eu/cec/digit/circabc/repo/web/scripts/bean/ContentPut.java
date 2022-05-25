package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.ContentApi;
import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class ContentPut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(ContentPut.class);

    private ContentApi contentApi;
    private NodesApi nodesApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NodeService nodeService;


   

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String language = req.getParameter("language");
        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {
            if (!this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBMANAGEOWN, LibraryPermissions.LIBEDITONLY)) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");
            }
            NodeRef nodeRef = Converter.createNodeRefFromId(id);
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) && !this.currentUserPermissionCheckerService.isWorkingCopyOwner(
                    nodeRef.getId())) {
                throw new AccessDeniedException("Cannot update content, not enough permissions");
            }


            Node body = NodeJsonParser.parseContentJSON(req.getContent().getContent());

            this.contentApi.contentIdPut(id, body);

            model.put("node", this.nodesApi.getNodeById(id));

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
        } catch (DuplicateChildNodeNameException dcnne) {
            status.setCode(HttpServletResponse.SC_CONFLICT);
            status.setMessage("Duplicate node name");
            status.setRedirect(true);
            return null;
        } catch (InvalidTypeException ite) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad noderef type");
            status.setRedirect(true);
            return null;
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad body");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
        return model;
    }

    /**
     * @return the contentApi
     */
    public ContentApi getContentApi() {
        return this.contentApi;
    }

    /**
     * @param contentApi the contentApi to set
     */
    public void setContentApi(ContentApi contentApi) {
        this.contentApi = contentApi;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return this.nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
