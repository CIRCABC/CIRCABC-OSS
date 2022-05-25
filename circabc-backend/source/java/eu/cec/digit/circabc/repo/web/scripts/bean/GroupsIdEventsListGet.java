package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import io.swagger.api.EventsApi;
import io.swagger.model.PagedEventItems;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to retrieve a list of Events/Meetings given a filter
 *
 * @author schwerr
 */
public class GroupsIdEventsListGet extends DeclarativeWebScript {

    private EventsApi eventsApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String igId = templateVars.get("igId");
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String filter = req.getParameter("filter");
        String exactDateStr = req.getParameter("exactDate");
        String sort = req.getParameter("sort");

        try {
            NodeRef groupRef = Converter.createNodeRefFromId(igId);
            NodeRef evtNodeRef =
                    this.nodeService.getChildByName(groupRef, ContentModel.ASSOC_CONTAINS, "Events");
            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    evtNodeRef.getId(), EventPermissions.EVEACCESS)) {
                throw new AccessDeniedException(
                        "Cannot get events because user does not have enough permissions");
            }

            if (!("Exact".equals(filter) || "Future".equals(filter) || "Previous".equals(filter))) {
                throw new IllegalArgumentException(
                        "Invalid filter value. Must be " + "'Exact', 'Future' or 'Previous'");
            }

            String pageStr = req.getParameter("page");
            int page = 0;
            page = getPage(pageStr, page);

            String limitStr = req.getParameter("limit");
            int limit = 0;
            if ((limitStr != null) && !limitStr.isEmpty()) {
                limit = getLimit(limitStr, limit);
            }

            if (page <= 0) {
                throw new IllegalArgumentException("Values for 'page' must be > 0");
            }

            if (limit < 0) {
                limit = 0;
            }

            Date exactDate = null;

            if ((exactDateStr != null) && !exactDateStr.isEmpty()) {

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                exactDate = getExactDate(exactDateStr, simpleDateFormat);
            }

            MLPropertyInterceptor.setMLAware(false);

            int startRecord = (page - 1) * limit;

            PagedEventItems pagedEventItems =
                    this.eventsApi.groupsIdEventsListGet(igId, filter, exactDate, startRecord, limit, sort);

            model.put("eventItems", pagedEventItems.getData());
            model.put("total", pagedEventItems.getTotal());
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
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

    private Date getExactDate(String exactDateStr, SimpleDateFormat simpleDateFormat) {
        Date exactDate;
        try {
            exactDate = simpleDateFormat.parse(exactDateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "The 'exactDate' has a wrong format. Must be yyyy-MM-dd", e);
        }
        return exactDate;
    }

    private int getLimit(String limitStr, int limit) {
        try {
            limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'limit': " + limit, e);
        }
        return limit;
    }

    private int getPage(String pageStr, int page) {
        try {
            page = (((pageStr == null) || pageStr.isEmpty()) ? 1 : Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'page': " + page, e);
        }
        return page;
    }

    /**
     * @param eventsApi the eventsApi to set
     */
    public void setEventsApi(EventsApi eventsApi) {
        this.eventsApi = eventsApi;
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
