package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.EventItem;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import io.swagger.api.EventsApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Webscript entry to retrieve a list of Events/Meetings
 *
 * @author schwerr
 */
public class GroupsIdEventsGet extends DeclarativeWebScript {

    private EventsApi eventsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NodeService nodeService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String igId = templateVars.get("igId");
        String language = req.getParameter("language");
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String startDateString = req.getParameter("startDate");
        String endDateString = req.getParameter("endDate");

        Date startDate;
        Date endDate;

        try {
            NodeRef groupRef = Converter.createNodeRefFromId(igId);
            NodeRef evtNodeRef =
                    this.nodeService.getChildByName(groupRef, ContentModel.ASSOC_CONTAINS, "Events");

            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    evtNodeRef.getId(), EventPermissions.EVEACCESS)) {
                throw new AccessDeniedException(
                        "Cannot get events because user does not have enough permissions");
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            startDate =
                    getDate(
                            startDateString,
                            simpleDateFormat,
                            "The 'startDate' has a wrong format. Must be yyyy-MM-dd");

            endDate =
                    getDate(
                            endDateString,
                            simpleDateFormat,
                            "The 'endDate' has a wrong format. Must be yyyy-MM-dd");

            Locale locale = new Locale(((language == null) || language.isEmpty()) ? "en" : language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);

            List<EventItem> eventItems =
                    this.eventsApi.groupsIdEventsGet(igId, startDate, endDate, language);

            model.put("eventItems", eventItems);
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

    private Date getDate(String startDateString, SimpleDateFormat simpleDateFormat, String s) {
        Date date;
        try {
            date = simpleDateFormat.parse(startDateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(s, e);
        }
        return date;
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
