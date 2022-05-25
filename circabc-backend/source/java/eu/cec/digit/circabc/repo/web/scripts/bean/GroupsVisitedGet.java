package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupsApi;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class GroupsVisitedGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsVisitedGet.class);

    private GroupsApi groupsApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        String amountString = req.getParameter("amount");
        int amount = 0;
        try {
            amount =
                    (((amountString == null) || amountString.isEmpty()) ? 0 : Integer.parseInt(amountString));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'amount': " + amount, e);
        }

        if (amount < 1) {
            amount = 0;
        }

        try {
            MLPropertyInterceptor.setMLAware(false);

            model.put("groups", this.groupsApi.getVisitedGroups(amount));

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
}
