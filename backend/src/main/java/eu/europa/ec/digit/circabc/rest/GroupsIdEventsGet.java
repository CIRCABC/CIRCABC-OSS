package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles HTTP {@code GET} requests to retrieve the list of
 * Events/Meetings belonging to an Interest Group.
 *
 * <p>The endpoint resolves the Interest Group node from the {@code igId} URI template variable,
 * locates its child "Events" container and verifies that the current user holds the
 * {@link EventPermissions#EVEACCESS} permission on it. When authorised, it delegates to
 * {@link EventsApi#groupsIdEventsGet(String, Date, Date, String)} to fetch the events, optionally
 * restricted to the {@code startDate}/{@code endDate} range and localised via the {@code language}
 * parameter.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code igId} - URI template variable identifying the Interest Group node.</li>
 *   <li>{@code startDate}, {@code endDate} - optional request parameters in {@code yyyy-MM-dd}
 *       format used to bound the returned events.</li>
 *   <li>{@code language} - optional request parameter selecting the locale used to render
 *       multilingual content (defaults to English).</li>
 * </ul>
 *
 * <p>On success the resulting model exposes the retrieved events under the {@code eventItems} key.
 * A permission failure yields an HTTP {@code 403 Forbidden} response, while any other error yields
 * an HTTP {@code 406 Not Acceptable} response.
 *
 * @author schwerr
 */
public class GroupsIdEventsGet extends DeclarativeWebScript {

  /** Logger used to record errors raised while processing the request. */
  static final Log logger = LogFactory.getLog(GroupsIdEventsGet.class);

  /** API providing the business logic for reading events of an Interest Group. */
  @Autowired
  private EventsApi eventsApi;

  /** Service used to verify that the current user holds the required event permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to resolve the "Events" container of the Interest Group. */
  @Autowired
  private NodeService nodeService;

  /**
   * Executes the endpoint logic: resolves the Interest Group's Events container, enforces the
   * required access permission, parses the optional date range and language, and retrieves the
   * matching events.
   *
   * @param req the incoming webscript request; supplies the {@code igId} URI template variable and
   *     the optional {@code startDate}, {@code endDate} and {@code language} parameters
   * @param status the response status object, updated with an error code, message and redirect flag
   *     when the request cannot be fulfilled
   * @param cache the response cache directives (unused)
   * @return a model map containing the retrieved events under the {@code eventItems} key on
   *     success, or {@code null} when an error occurs and the status has been set accordingly
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
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String startDateString = req.getParameter("startDate");
    String endDateString = req.getParameter("endDate");

    Date startDate;
    Date endDate;

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

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

      Locale locale = Locale.of(
        ((language == null) || language.isEmpty()) ? "en" : language
      );
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);

      List<EventItem> eventItems = this.eventsApi.groupsIdEventsGet(
        igId,
        startDate,
        endDate,
        language
      );

      model.put("eventItems", eventItems);
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
   * Parses a date string using the supplied formatter.
   *
   * @param startDateString the date value to parse, expected in the formatter's pattern
   * @param simpleDateFormat the formatter defining the expected date pattern
   * @param s the error message used when the value cannot be parsed
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
