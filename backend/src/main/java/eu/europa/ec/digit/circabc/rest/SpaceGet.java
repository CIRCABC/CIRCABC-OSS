package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to list the children
 * of a space (folder) in the CIRCABC repository.
 *
 * <p>The target space is identified by the {@code id} template variable in the request URL.
 * Before returning any content, the endpoint verifies that the current user has Alfresco read
 * permission on the requested space; if not, an HTTP {@code 403 Forbidden} response is produced.
 *
 * <p>The results can be paginated and filtered through the following optional request parameters:
 * <ul>
 *   <li>{@code language} - locale used to resolve multilingual (ML) content properties
 *       (defaults to {@code en}).</li>
 *   <li>{@code page} - 1-based page number to retrieve; when omitted (together with
 *       {@code limit}) all children are returned unpaginated.</li>
 *   <li>{@code limit} - maximum number of results per page (defaults to
 *       {@value #DEFAULT_NUMBER_RESULTS}).</li>
 *   <li>{@code order} - sort expression applied to the results.</li>
 *   <li>{@code folderOnly} - when {@code "true"}, restrict results to folders.</li>
 *   <li>{@code fileOnly} - when {@code "true"}, restrict results to files.</li>
 *   <li>{@code skipExpiredItems} - when {@code "true"}, exclude expired items.</li>
 * </ul>
 *
 * <p>On success the returned model contains the list of child nodes under {@code data} and the
 * total number of matching children under {@code total}. Error conditions are mapped to the
 * appropriate HTTP status codes ({@code 403}, {@code 400} or {@code 500}).
 */
public class SpaceGet extends DeclarativeWebScript {

  /** Logger used to report permission and processing errors for this endpoint. */
  static final Log logger = LogFactory.getLog(SpaceGet.class);

  /** Default (0-based) page index used when no explicit page is requested. */
  private static final int START_PAGE = 0;

  /** Default page size applied when no {@code limit} parameter is provided. */
  private static final int DEFAULT_NUMBER_RESULTS = 25;

  /** API used to fetch the children of a space from the repository. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to check whether the current user may read the requested space. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: resolves the requested space, checks read permission, fetches
   * the (optionally paginated and filtered) children and builds the response model.
   *
   * @param req the web script request; supplies the {@code id} template variable and the optional
   *            {@code language}, {@code page}, {@code limit}, {@code order}, {@code folderOnly},
   *            {@code fileOnly} and {@code skipExpiredItems} parameters
   * @param status the response status, updated with the relevant HTTP code and message when an
   *               error occurs
   * @param cache the cache control object for the response
   * @return a model map containing the child nodes under {@code data} and the total count under
   *         {@code total} on success, or {@code null} when an error is handled and the status has
   *         been set accordingly
   */
  @Override
  @SuppressWarnings({ "squid:S1168" })
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    String id = req.getServiceMatch().getTemplateVars().get("id");

    try {
      validatePermission(id);
      PagedNodes nodes = fetchChildren(req, id);
      model.put("data", nodes.getData());
      model.put("total", nodes.getTotal());
    } catch (AccessDeniedException ade) {
      return handleError(
        status,
        Status.STATUS_FORBIDDEN,
        "Access denied",
        ade,
        id
      );
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad request",
        inre,
        id
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e,
        id
      );
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * Configures the thread-local content and UI locale from the requested language and disables
   * ML awareness so that language-specific property values are returned.
   *
   * @param language the requested language code, or {@code null} to fall back to {@code en}
   */
  private void setupLocale(String language) {
    Locale locale = Locale.of(language != null ? language : "en");
    I18NUtil.setContentLocale(locale);
    I18NUtil.setLocale(locale);
    MLPropertyInterceptor.setMLAware(false);
  }

  /**
   * Ensures the current user has Alfresco read permission on the requested space.
   *
   * @param id the node reference identifier of the space
   * @throws AccessDeniedException if the current user cannot read the space
   */
  private void validatePermission(String id) {
    if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)) {
      throw new AccessDeniedException(
        "Cannot read the space, not enough permissions"
      );
    }
  }

  /**
   * Retrieves the children of the given space, applying pagination and filtering based on the
   * request parameters. When neither {@code page} nor {@code limit} is provided, all children are
   * fetched unpaginated (using {@code -1} for both bounds).
   *
   * @param req the web script request providing the pagination and filtering parameters
   * @param id the node reference identifier of the space whose children are fetched
   * @return the paged collection of child nodes matching the request
   */
  private PagedNodes fetchChildren(WebScriptRequest req, String id) {
    String page = req.getParameter("page");
    String limit = req.getParameter("limit");
    String sort =
      req.getParameter("order") != null ? req.getParameter("order") : "";
    boolean folderOnly = "true".equals(req.getParameter("folderOnly"));
    boolean fileOnly = "true".equals(req.getParameter("fileOnly"));
    boolean skipExpiredItems = Boolean.parseBoolean(
      req.getParameter("skipExpiredItems")
    );

    if (isUnpaginated(page, limit)) {
      return spacesApi.spaceGetChildren(
        id,
        -1,
        -1,
        sort,
        folderOnly,
        fileOnly,
        skipExpiredItems
      );
    }

    int nbPage = parsePage(page);
    int nbLimit = parseLimit(limit);
    return spacesApi.spaceGetChildren(
      id,
      nbPage,
      nbLimit,
      sort,
      folderOnly,
      fileOnly,
      skipExpiredItems
    );
  }

  /**
   * Determines whether the request should be treated as unpaginated, i.e. both the page and the
   * limit are absent, empty or negative.
   *
   * @param page the raw {@code page} parameter value
   * @param limit the raw {@code limit} parameter value
   * @return {@code true} if all children should be returned without pagination
   */
  private boolean isUnpaginated(String page, String limit) {
    return isEmptyOrNegative(page) && isEmptyOrNegative(limit);
  }

  /**
   * Checks whether the given value is absent, empty or represents {@code -1}.
   *
   * @param value the raw parameter value to test
   * @return {@code true} if the value is {@code null}, empty or {@code "-1"}
   */
  private boolean isEmptyOrNegative(String value) {
    return value == null || "-1".equals(value) || value.isEmpty();
  }

  /**
   * Parses the requested page into a 0-based page index. A {@code null} value falls back to
   * {@link #START_PAGE}; otherwise the 1-based input is converted to 0-based (with {@code 0}
   * left unchanged).
   *
   * @param page the raw {@code page} parameter value
   * @return the 0-based page index
   * @throws NumberFormatException if {@code page} is not a valid integer
   */
  private int parsePage(String page) {
    if (page == null) return START_PAGE;
    int parsed = Integer.parseInt(page);
    return parsed == 0 ? 0 : parsed - 1;
  }

  /**
   * Parses the requested page size, falling back to {@link #DEFAULT_NUMBER_RESULTS} when the
   * {@code limit} parameter is absent.
   *
   * @param limit the raw {@code limit} parameter value
   * @return the number of results per page
   * @throws NumberFormatException if {@code limit} is not a valid integer
   */
  private int parseLimit(String limit) {
    return limit != null ? Integer.parseInt(limit) : DEFAULT_NUMBER_RESULTS;
  }

  /**
   * Logs the given error, sets the response status accordingly and requests a redirect to the
   * status template so that the error is rendered as an HTTP error response.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set
   * @param message the human-readable status message to set and log
   * @param e the exception that caused the error
   * @param id the identifier of the space being processed, included in the log message
   * @return always {@code null}, signalling that no model was produced
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e,
    String id
  ) {
    logger.error(message + " for space with ID: " + id, e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
