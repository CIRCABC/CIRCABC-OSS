package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the HTTP {@code GET} request for retrieving the
 * content (topics/posts) of a forum node.
 *
 * <p>The forum node reference is supplied as the {@code id} template variable in the URL. Before
 * returning any data the endpoint verifies that the current authority has Alfresco read permission
 * on the requested forum; otherwise access is denied.
 *
 * <p>Supported request parameters:
 *
 * <ul>
 *   <li>{@code language} &ndash; optional ISO language code. When provided, the content and UI
 *       locale are set accordingly and multilingual (ML) awareness is disabled so that content is
 *       returned in the requested language; when omitted, ML awareness is enabled.
 *   <li>{@code page} &ndash; optional 1-based page number for pagination (defaults to the first
 *       page).
 *   <li>{@code limit} &ndash; optional maximum number of results per page (defaults to
 *       {@value #DEFAULT_NUMBER_RESULTS}).
 *   <li>{@code order} &ndash; optional sort order applied to the returned nodes.
 * </ul>
 *
 * <p>On success the response model contains the {@code data} (the paged list of forum nodes) and
 * the {@code total} number of available items.
 *
 * @author beaurpi
 */
public class ForumContentGet extends DeclarativeWebScript {

  /** Logger used to report permission, request and unexpected errors. */
  static final Log logger = LogFactory.getLog(ForumContentGet.class);

  /** Zero-based index of the first page, used as the default when no {@code page} is requested. */
  private static final int START_PAGE = 0;

  /** Default page size applied when the {@code limit} request parameter is not provided. */
  private static final int DEFAULT_NUMBER_RESULTS = 100;

  /** API used to retrieve forum content by node reference. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to check whether the current user holds the required Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script: reads the forum {@code id} template variable and the optional
   * {@code language}, {@code page}, {@code limit} and {@code order} request parameters, verifies
   * that the current authority has read permission on the forum, and populates the response model
   * with the paged forum content and total count.
   *
   * <p>Errors are translated into HTTP status codes rather than propagated: an access permission
   * failure results in {@link Status#STATUS_FORBIDDEN}, an invalid node reference results in
   * {@link Status#STATUS_BAD_REQUEST}, and any other exception results in
   * {@link Status#STATUS_INTERNAL_SERVER_ERROR}; in these cases {@code null} is returned. The
   * previous ML-awareness state is always restored before returning.
   *
   * @param req the web script request carrying the {@code id} template variable and query
   *     parameters
   * @param status the response status, used to signal error conditions
   * @param cache the cache directives for the response
   * @return a model map containing the {@code data} (forum nodes) and {@code total} count on
   *     success, or {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    int nbPage = START_PAGE;
    String page = req.getParameter("page");
    if (page != null) {
      nbPage = ((Integer.parseInt(page) == 0)
        ? 0
        : (Integer.parseInt(page) - 1));
    }

    String limit = req.getParameter("limit");
    int nbLimit = DEFAULT_NUMBER_RESULTS;
    if (limit != null) {
      nbLimit = Integer.parseInt(limit);
    }

    String sort = req.getParameter("order");

    try {
      if (id != null) {
        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            id
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot access the forum, not enough permission"
          );
        }

        PagedNodes nodes = this.forumsApi.getForumById(
          id,
          nbPage,
          nbLimit,
          sort
        );
        model.put("data", nodes.getData());
        model.put("total", nodes.getTotal());
      }
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error(ex.getMessage(), ex);
      }

      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
