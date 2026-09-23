package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EventsApi;
import io.swagger.model.PagedEventItems;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that retrieves a paginated list of Events/Meetings
 * belonging to an Interest Group, filtered and sorted according to the request
 * parameters.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, implying an
 * HTTP {@code GET} operation on the events collection of a group. The endpoint
 * resolves the group's "Events" container node, verifies that the current user
 * holds the {@link EventPermissions#EVEACCESS} permission on it, and then
 * delegates the actual query to {@link EventsApi#groupsIdEventsListGet}.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code igId} — path template variable identifying the Interest Group.</li>
 *   <li>{@code filter} — request parameter, one of {@code "Exact"},
 *       {@code "Future"} or {@code "Previous"} (mandatory).</li>
 *   <li>{@code exactDate} — request parameter, a date in {@code yyyy-MM-dd}
 *       format (used together with the {@code "Exact"} filter).</li>
 *   <li>{@code page} — 1-based page number request parameter (must be &gt; 0).</li>
 *   <li>{@code limit} — page size request parameter (0 means no limit).</li>
 *   <li>{@code sort} — optional sort specification request parameter.</li>
 * </ul>
 *
 * <p>On success the returned model contains the {@code eventItems} list and the
 * {@code total} number of matching events. Access failures produce an HTTP 403
 * (Forbidden) response, while other errors produce an HTTP 406 (Not Acceptable)
 * response.
 *
 * @author schwerr
 */
public class GroupsIdEventsListGet extends DeclarativeWebScript {

  /** Logger used to report errors raised while handling the request. */
  static final Log logger = LogFactory.getLog(GroupsIdEventsListGet.class);

  /** Business API used to query the paged list of events for a group. */
  @Autowired
  private EventsApi eventsApi;

  /** Alfresco node service used to resolve the group's "Events" container node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to check that the current user holds the required event permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request: validates the request parameters, checks the
   * current user's permissions on the group's "Events" container and returns the
   * matching paginated list of events.
   *
   * @param req the webscript request; supplies the {@code igId} template variable
   *     and the {@code filter}, {@code exactDate}, {@code sort}, {@code page} and
   *     {@code limit} parameters
   * @param status the response status, set to {@code 403} on access denial or
   *     {@code 406} on any other failure
   * @param cache the response cache directives (unused)
   * @return a model map containing {@code eventItems} (the list of events) and
   *     {@code total} (the total number of matching events), or {@code null} when
   *     an error occurs and the response is redirected to the status template
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String igId = templateVars.get("igId");
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String filter = req.getParameter("filter");
    String exactDateStr = req.getParameter("exactDate");
    String sort = req.getParameter("sort");

    try {
      NodeRef groupRef = Converter.createNodeRefFromId(igId);
      NodeRef evtNodeRef = this.nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      );
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
          evtNodeRef.getId(),
          EventPermissions.EVEACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot get events because user does not have enough permissions"
        );
      }

      if (
        !("Exact".equals(filter) ||
          "Future".equals(filter) ||
          "Previous".equals(filter))
      ) {
        throw new IllegalArgumentException(
          "Invalid filter value. Must be " + "'Exact', 'Future' or 'Previous'"
        );
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

      PagedEventItems pagedEventItems = this.eventsApi.groupsIdEventsListGet(
        igId,
        filter,
        exactDate,
        startRecord,
        limit,
        sort
      );

      model.put("eventItems", pagedEventItems.getData());
      model.put("total", pagedEventItems.getTotal());
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
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
   * Parses the {@code exactDate} request parameter into a {@link Date}.
   *
   * @param exactDateStr the raw date string, expected in {@code yyyy-MM-dd} format
   * @param simpleDateFormat the formatter used to parse the string
   * @return the parsed date
   * @throws IllegalArgumentException if the string cannot be parsed with the
   *     expected {@code yyyy-MM-dd} format
   */
  private Date getExactDate(
    String exactDateStr,
    SimpleDateFormat simpleDateFormat
  ) {
    Date exactDate;
    try {
      exactDate = simpleDateFormat.parse(exactDateStr);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "The 'exactDate' has a wrong format. Must be yyyy-MM-dd",
        e
      );
    }
    return exactDate;
  }

  /**
   * Parses the {@code limit} request parameter into an integer page size.
   *
   * @param limitStr the raw limit string to parse
   * @param limit the current limit value, used only for the error message
   * @return the parsed limit value
   * @throws IllegalArgumentException if the string is not a valid integer
   */
  private int getLimit(String limitStr, int limit) {
    try {
      limit = Integer.parseInt(limitStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'limit': " + limit,
        e
      );
    }
    return limit;
  }

  /**
   * Parses the {@code page} request parameter into a 1-based page number,
   * defaulting to {@code 1} when the parameter is absent or empty.
   *
   * @param pageStr the raw page string to parse, may be {@code null} or empty
   * @param page the current page value, used only for the error message
   * @return the parsed page number, or {@code 1} if none was provided
   * @throws IllegalArgumentException if the string is not a valid integer
   */
  private int getPage(String pageStr, int page) {
    try {
      page = (((pageStr == null) || pageStr.isEmpty())
        ? 1
        : Integer.parseInt(pageStr));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'page': " + page,
        e
      );
    }
    return page;
  }
}
