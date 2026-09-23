package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript that handles the HTTP {@code GET} request for
 * retrieving the list of Events/Meetings belonging to a given user, restricted
 * to an optional date range.
 *
 * <p>The endpoint resolves the target user from the {@code userId} URI template
 * variable and reads the {@code startDate} and {@code endDate} request
 * parameters (both expected in {@code yyyy-MM-dd} format). For security reasons
 * a user may only retrieve their own events: if the authenticated user is not
 * the same as {@code userId}, an {@link AccessDeniedException} is raised and the
 * response is set to HTTP {@code 403 Forbidden}. Malformed dates or other
 * failures result in HTTP {@code 406 Not Acceptable}.
 *
 * <p>On success the returned model exposes an {@code eventItems} entry holding
 * the list of {@link EventItem} objects, which the associated FreeMarker
 * template renders as JSON.
 */
public class UsersIdEventsGet extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors for this endpoint. */
  static final Log logger = LogFactory.getLog(UsersIdEventsGet.class);

  /** API used to fetch the events/meetings of a user within a date range. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to verify that the current user matches the requested {@code userId}. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the webscript: validates that the authenticated user is requesting
   * their own events, parses the {@code startDate}/{@code endDate} parameters and
   * queries the {@link EventsApi} for matching events.
   *
   * <p>The {@code userId} is read from the URI template variables, while
   * {@code startDate} and {@code endDate} are read from the request parameters
   * (format {@code yyyy-MM-dd}). ML awareness is temporarily disabled while
   * fetching events and restored afterwards. Errors are translated into the
   * appropriate HTTP status codes and cause a {@code null} model to be returned.
   *
   * @param req the incoming webscript request, providing the {@code userId} URI
   *     template variable and the {@code startDate}/{@code endDate} parameters
   * @param status the response status object, updated to {@code 403} on access
   *     denial and {@code 406} on any other processing error
   * @param cache the cache directives for the response
   * @return a model map containing the {@code eventItems} entry with the list of
   *     {@link EventItem} objects on success, or {@code null} when an error occurs
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
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

      if (
        !(this.currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))
      ) {
        throw new AccessDeniedException(
          "Cannot get the events of somebody else"
        );
      }

      startDate = getDate(
        startDateString,
        simpleDateFormat,
        "The 'startDate' has a wrong format. Must be yyyy-MM-dd"
      );

      endDate = getDate(
        endDateString,
        simpleDateFormat,
        "The 'endDate' has a wrong format. Must be yyyy-MM-dd"
      );

      MLPropertyInterceptor.setMLAware(false);

      List<EventItem> eventItems = this.eventsApi.usersIdEventsGet(
        userId,
        startDate,
        endDate
      );

      model.put("eventItems", eventItems);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when getting events for user: " + userId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error getting events for user: " + userId, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Parses a date string using the supplied formatter.
   *
   * @param startDateString the date string to parse (expected format {@code yyyy-MM-dd})
   * @param simpleDateFormat the formatter used to parse the date string
   * @param s the error message used when the date string cannot be parsed
   * @return the parsed {@link Date}
   * @throws IllegalArgumentException if {@code startDateString} does not match the expected format
   */
  private Date getDate(
    String startDateString,
    SimpleDateFormat simpleDateFormat,
    String s
  ) {
    Date date;
    try {
      date = simpleDateFormat.parse(startDateString);
    } catch (ParseException e) {
      throw new IllegalArgumentException(s, e);
    }
    return date;
  }
}
