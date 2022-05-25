package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv;
import io.swagger.api.ContentApi;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Email this content zip file link to the user with the given id. If userId is not provided, it
 * emails to the current authenticated user
 *
 * @author schwerr
 */
public class ContentsEmail extends CircabcDeclarativeWebScript {

    MailMeContentBusinessSrv mailMeContentBusinessSrv;
    NodeService nodeService;

    private ContentApi contentApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            MLPropertyInterceptor.setMLAware(false);

            String parentId = req.getParameter("parentId");

            String[] nodeIds = req.getParameterValues("nodeIds");

            if ((nodeIds == null) || (nodeIds.length == 0)) {
                throw new IllegalArgumentException("Empty list of 'nodeIds' supplied.");
            }

            NodeRef contentRef = this.contentApi.createDownloadableZip(nodeIds, parentId);

            String userId = req.getParameter("userId");

            boolean result;

            if ((userId == null) || userId.isEmpty()) {
                result = this.mailMeContentBusinessSrv.send(contentRef, false);
            } else {
                result = this.mailMeContentBusinessSrv.send(contentRef, userId, false);
            }

            model.put("result", result);
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage(ade.getMessage());
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param mailMeContentBusinessSrv the mailMeContentBusinessSrv to set
     */
    public void setMailMeContentBusinessSrv(MailMeContentBusinessSrv mailMeContentBusinessSrv) {
        this.mailMeContentBusinessSrv = mailMeContentBusinessSrv;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param contentApi the contentApi to set
     */
    public void setContentApi(ContentApi contentApi) {
        this.contentApi = contentApi;
    }
}
