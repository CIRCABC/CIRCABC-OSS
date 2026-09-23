package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.EventsApi;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco read-only (HTTP {@code GET}) webscript endpoint that retrieves the list of
 * Events/Meetings associated with a given user, filtered by a time period.
 *
 * <p>The endpoint resolves the target user from the {@code userId} URL template variable and
 * applies a period filter provided through the {@code period} request parameter. Accepted values
 * for {@code period} are {@code previous}, {@code exact} and {@code future} (case-insensitive),
 * mapping respectively to {@link EventFilter#Previous}, {@link EventFilter#Exact} and
 * {@link EventFilter#Future}. When {@code exact} is requested, an additional {@code exactDate}
 * request parameter (formatted as {@code yyyy-MM-dd}) is required to pin the reference date.</p>
 *
 * <p>Access is restricted: only the authenticated user themselves or the {@code admin} user may
 * read a user's events; otherwise the endpoint responds with HTTP 403 (Forbidden). Invalid or
 * missing input results in HTTP 406 (Not Acceptable). On success the resolved list of
 * {@link EventItem} instances is exposed in the response model under the {@code eventItems} key
 * for rendering by the associated FreeMarker template.</p>
 *
 * @author schwerr
 */
public class UsersIdEventsPeriodGet extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors for this endpoint. */
  static final Log logger = LogFactory.getLog(UsersIdEventsPeriodGet.class);

  /** API providing the business logic to look up a user's events. */
  @Autowired
  private EventsApi eventsApi;

  /** Alfresco service used to identify the current authenticated user for authorization checks. */
  @Autowired
  private AuthenticationService authenticationService;

  /**
   * Handles the incoming webscript request and builds the response model containing the user's
   * events for the requested period.
   *
   * <p>Reads the {@code userId} template variable together with the {@code period} and optional
   * {@code exactDate} request parameters, enforces that the caller may only access their own
   * events (or is {@code admin}), and delegates the lookup to {@link EventsApi#usersIdEventsGet}.
   * Multilingual property interception is temporarily disabled during the lookup and restored
   * afterwards.</p>
   *
   * @param req the webscript request, providing the {@code userId} template variable and the
   *     {@code period}/{@code exactDate} parameters
   * @param status the response status object, updated to 403 on access denial or 406 on invalid
   *     input / processing errors
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the resolved list of events under the {@code eventItems} key,
   *     or {@code null} when an error status has been set
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
    String exactDateString = req.getParameter("exactDate");
    String period = req.getParameter("period");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Date exactDate = new Date();

    try {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

      if (
        !(this.authenticationService.getCurrentUserName().equals(userId) ||
          this.authenticationService.getCurrentUserName().equals("admin"))
      ) {
        throw new AccessDeniedException(
          "Cannot get the events of somebody else"
        );
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
            "Valid values for 'period' are 'previous', 'exact' and 'future'."
          );
      }

      if (EventFilter.Exact.equals(filter)) {
        exactDate = getDate(exactDateString, simpleDateFormat);
      }

      MLPropertyInterceptor.setMLAware(false);

      List<EventItem> eventItems = this.eventsApi.usersIdEventsGet(
        userId,
        exactDate,
        filter
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
      logger.error(
        "Error getting events for period for user: " +
          userId +
          ", period: " +
          period,
        e
      );
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
   * Parses the supplied date string into a {@link Date} using the given formatter.
   *
   * @param exactDateString the date string to parse, expected in {@code yyyy-MM-dd} format
   * @param simpleDateFormat the formatter used to parse the date string
   * @return the parsed date
   * @throws IllegalArgumentException if the string cannot be parsed with the expected format
   */
  private Date getDate(
    String exactDateString,
    SimpleDateFormat simpleDateFormat
  ) {
    Date exactDate;
    try {
      exactDate = simpleDateFormat.parse(exactDateString);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "The 'exactDate' has a wrong format. Must be yyyy-MM-dd",
        e
      );
    }
    return exactDate;
  }
}
