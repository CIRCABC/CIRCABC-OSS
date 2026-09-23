package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.PagedNodes;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
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
 * REST webscript endpoint that returns the paginated list of replies of a
 * discussion topic.
 *
 * <p>The class name implies an HTTP {@code GET} operation: it reads the
 * replies belonging to the topic identified by the {@code id} template
 * variable and renders them as JSON.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (URL template variable) &ndash; the node reference /
 *       identifier of the topic whose replies are requested.</li>
 *   <li>{@code language} (request parameter, optional) &ndash; the locale used
 *       to resolve multilingual content; when absent the response stays
 *       multilingual-aware.</li>
 *   <li>{@code page} (request parameter, optional) &ndash; the 1-based page
 *       number to return; defaults to the first page.</li>
 *   <li>{@code limit} (request parameter, optional) &ndash; the maximum number
 *       of replies per page; defaults to {@value #DEFAULT_NUMBER_RESULTS}.</li>
 *   <li>{@code order} (request parameter, optional) &ndash; the sort order
 *       applied to the returned replies.</li>
 * </ul>
 *
 * <p>Access is granted only to callers holding either newsgroup access or
 * library access permission on the target node; otherwise the endpoint
 * responds with HTTP 403. The resulting model exposes the {@code data}
 * (the list of replies) and {@code total} (the total number of replies)
 * entries consumed by the FreeMarker template.</p>
 */
public class TopicRepliesGet extends DeclarativeWebScript {

  /** Default page index (0-based) used when no {@code page} parameter is provided. */
  private static final int START_PAGE = 0;

  /** Default number of replies returned per page when no {@code limit} parameter is provided. */
  private static final int DEFAULT_NUMBER_RESULTS = 25;

  /** Logger used to report errors raised while retrieving topic replies. */
  static final Log logger = LogFactory.getLog(TopicRepliesGet.class);

  /** API providing access to topic-related business operations. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request and builds the model with the paginated
   * topic replies.
   *
   * <p>Reads the topic {@code id} from the URL template variables, applies the
   * requested locale, checks that the caller has the required permissions and
   * then fetches the replies through {@link TopicsApi#getTopicReplies}. The
   * multilingual-awareness flag is always restored before returning.</p>
   *
   * @param req the incoming webscript request, carrying the {@code id}
   *            template variable and the {@code language}, {@code page},
   *            {@code limit} and {@code order} parameters
   * @param status the response status, updated with the appropriate HTTP code
   *               and message when an error occurs
   * @param cache the webscript cache control object
   * @return a model map containing the {@code data} (list of replies) and
   *         {@code total} (total count) entries on success, or {@code null}
   *         when an error is handled and the status is set accordingly
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String id = req.getServiceMatch().getTemplateVars().get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    try {
      validatePermission(id);
      if (id != null) {
        PagedNodes nodes = topicsApi.getTopicReplies(
          id,
          parsePage(req),
          parseLimit(req),
          req.getParameter("order")
        );
        model.put("data", nodes.getData());
        model.put("total", nodes.getTotal());
      }
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad request",
        inre
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Configures the locale used to resolve multilingual content for this
   * request.
   *
   * <p>When no language is supplied the interceptor is left multilingual-aware
   * so that all languages are returned; otherwise the given language is set as
   * both content and UI locale and multilingual awareness is disabled to
   * return only the values for that locale.</p>
   *
   * @param language the requested language code, or {@code null} to keep the
   *                 response multilingual-aware
   */
  private void setupLocale(String language) {
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
  }

  /**
   * Verifies that the current user is allowed to read the replies of the
   * given topic.
   *
   * <p>Access is granted when the caller holds newsgroup access permission or
   * library access permission on the node.</p>
   *
   * @param id the identifier of the topic node to check
   * @throws AccessDeniedException if the current user has neither newsgroup
   *                               nor library access permission
   */
  private void validatePermission(String id) {
    boolean hasNewsGroupPerm =
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        id,
        NewsGroupPermissions.NWSACCESS
      );
    boolean hasLibraryPerm =
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBACCESS
      );
    if (!hasNewsGroupPerm && !hasLibraryPerm) {
      throw new AccessDeniedException(
        "Cannot get the replies of the topic, not enough permissions"
      );
    }
  }

  /**
   * Resolves the 0-based page index from the {@code page} request parameter.
   *
   * <p>The parameter is expected to be 1-based; a value of {@code 0} or a
   * missing parameter both map to the first page.</p>
   *
   * @param req the webscript request carrying the {@code page} parameter
   * @return the 0-based page index to request
   * @throws NumberFormatException if the {@code page} parameter is present but
   *                               not a valid integer
   */
  private int parsePage(WebScriptRequest req) {
    String page = req.getParameter("page");
    if (page == null) return START_PAGE;
    int parsed = Integer.parseInt(page);
    return parsed == 0 ? 0 : parsed - 1;
  }

  /**
   * Resolves the page size from the {@code limit} request parameter.
   *
   * @param req the webscript request carrying the {@code limit} parameter
   * @return the requested limit, or {@value #DEFAULT_NUMBER_RESULTS} when the
   *         parameter is absent
   * @throws NumberFormatException if the {@code limit} parameter is present but
   *                               not a valid integer
   */
  private int parseLimit(WebScriptRequest req) {
    String limit = req.getParameter("limit");
    return limit != null ? Integer.parseInt(limit) : DEFAULT_NUMBER_RESULTS;
  }

  /**
   * Logs the given error and populates the response status so the request
   * ends with the supplied HTTP code.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set
   * @param message the human-readable error message, also used for logging
   * @param e the exception that triggered the error
   * @return always {@code null}, signalling that no model should be rendered
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    if (logger.isErrorEnabled()) {
      logger.error(message + " when getting topic replies", e);
    }
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
