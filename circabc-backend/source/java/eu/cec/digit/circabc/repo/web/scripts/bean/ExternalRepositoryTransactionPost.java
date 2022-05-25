package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AresBridgeApi;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.model.ExternalRepositoryTransaction;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AresBridgeJsonParser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalRepositoryTransactionPost extends CircabcDeclarativeWebScript {
    private AresBridgeApi aresBridgeApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        if (currentUserPermissionCheckerService.isGuest()
                || currentUserPermissionCheckerService.isExternalUser()) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        }

        try {
            if (id != null) {
                List<ExternalRepositoryTransaction> externalRepositoryTransactions =
                        AresBridgeJsonParser.parseTransaction(req);
                if (id.equalsIgnoreCase(AresBridgeApiImpl.ARES_BRIDGE) && !externalRepositoryTransactions.isEmpty() ) {
                    NodeRef firstNodeRef = Converter.createNodeRefFromId(externalRepositoryTransactions.get(0).getNodeId());
                    String groupId=  apiToolBox.getCurrentInterestGroup(firstNodeRef).getId();
                    for (ExternalRepositoryTransaction item : externalRepositoryTransactions) {
                        
                        this.aresBridgeApi.saveTransaction(
                                groupId, id, item.getTransactionId(), item.getNodeId(), item.getVersionLabel(),item.getName());
                    }
                }
            }
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public AresBridgeApi getAresBridgeApi() {
        return aresBridgeApi;
    }

    public void setAresBridgeApi(AresBridgeApi aresBridgeApi) {
        this.aresBridgeApi = aresBridgeApi;
    }
}
