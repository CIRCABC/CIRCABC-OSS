package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.event.EventFilter;
import eu.cec.digit.circabc.service.event.EventItem;
import io.swagger.api.EventsApi;
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
 * Webscript entry to retrieve a list of Events/Meetings given a user id and a certain period
 * (previous, exact, future)
 *
 * @author schwerr
 */
public class UsersIdEventsPeriodGet extends DeclarativeWebScript {

    private EventsApi eventsApi;
    private AuthenticationService authenticationService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String userId = templateVars.get("userId");
        String exactDateString = req.getParameter("exactDate");
        String period = req.getParameter("period");

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Date exactDate = new Date();

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if (!(this.authenticationService.getCurrentUserName().equals(userId)
                    || this.authenticationService.getCurrentUserName().equals("admin"))) {
                throw new AccessDeniedException("Cannot get the events of somebody else");
            }

            if (period == null || period.isEmpty()) {
                throw new IllegalArgumentException("The 'period' must not be missing.");
            }

            EventFilter filter;
            switch (period.toLowerCase()) {
                case "future":
                    filter = EventFilter.Future;
                    break;
                case "previous":
                    filter = EventFilter.Previous;
                    break;
                case "exact":
                    filter = EventFilter.Exact;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Valid values for 'period' are 'previous', 'exact' and 'future'.");
            }

            if (EventFilter.Exact.equals(filter)) {
                exactDate = getDate(exactDateString, simpleDateFormat);
            }

            MLPropertyInterceptor.setMLAware(false);

            List<EventItem> eventItems = this.eventsApi.usersIdEventsGet(userId, exactDate, filter);

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

    private Date getDate(String exactDateString, SimpleDateFormat simpleDateFormat) {
        Date exactDate;
        try {
            exactDate = simpleDateFormat.parse(exactDateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "The 'exactDate' has a wrong format. Must be yyyy-MM-dd", e);
        }
        return exactDate;
    }

    /**
     * @param eventsApi the eventsApi to set
     */
    public void setEventsApi(EventsApi eventsApi) {
        this.eventsApi = eventsApi;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
