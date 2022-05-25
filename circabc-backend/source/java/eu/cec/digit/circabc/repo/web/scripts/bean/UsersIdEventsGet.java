package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.EventItem;
import io.swagger.api.EventsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webscript entry to retrieve a list of Events/Meetings given a user id and within a date range
 *
 * @author schwerr
 */
public class UsersIdEventsGet extends DeclarativeWebScript {

    private EventsApi eventsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private AuthenticationService authenticationService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String userId = templateVars.get("userId");
        String startDateString = req.getParameter("startDate");
        String endDateString = req.getParameter("endDate");

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Date startDate;
        Date endDate;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Cannot get the events of somebody else");
            }

            startDate = getDate(startDateString, simpleDateFormat, "The 'startDate' has a wrong format. Must be yyyy-MM-dd");

            endDate = getDate(endDateString, simpleDateFormat, "The 'endDate' has a wrong format. Must be yyyy-MM-dd");

            MLPropertyInterceptor.setMLAware(false);

            List<EventItem> eventItems = this.eventsApi.usersIdEventsGet(userId, startDate, endDate);

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
            throw new IllegalArgumentException(
                    s, e);
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

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
