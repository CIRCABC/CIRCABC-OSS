package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ExpiredApi;
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
 * Alfresco declarative web script backing the HTTP {@code GET} endpoint that
 * returns the expired documents of a given Interest Group.
 *
 * <p>The endpoint resolves the Interest Group from the {@code igId} template
 * variable in the request URL and, after verifying that the current user is a
 * group administrator for that group, delegates to {@link ExpiredApi} to fetch
 * the paged collection of expired documents.</p>
 *
 * <p>Supported request parameters:</p>
 * <ul>
 *   <li>{@code language} - optional content locale; when supplied the response
 *       is rendered for that specific locale (ML awareness disabled), otherwise
 *       multilingual (ML aware) values are returned.</li>
 *   <li>{@code limit} - optional page size; defaults to {@code 25} when the
 *       parameter is present but empty.</li>
 *   <li>{@code page} - optional 1-based page number; defaults to {@code 1} when
 *       the parameter is present but empty.</li>
 *   <li>{@code order} - optional sort order.</li>
 * </ul>
 *
 * <p>On success the returned model contains the {@code data} (list of expired
 * documents) and {@code total} (overall count) entries. Access errors are
 * translated to HTTP {@code 403 Forbidden} and invalid node references to HTTP
 * {@code 400 Bad Request}.</p>
 */
public class GroupsDocumentsExpiredGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDocumentsExpiredGet.class);

  /** API used to retrieve the expired documents of an Interest Group. */
  @Autowired
  private ExpiredApi expiredApi;

  /** Service used to verify that the current user is a group administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the web script request: validates that the current user is a group
   * administrator of the target Interest Group and builds the model containing
   * the paged list of expired documents.
   *
   * @param req the web script request; provides the {@code igId} template
   *            variable and the {@code language}, {@code limit}, {@code page}
   *            and {@code order} query parameters
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} on an invalid node reference
   * @param cache the response cache directives
   * @return a model map containing the {@code data} and {@code total} entries on
   *         success, or {@code null} when the request is redirected to an error
   *         status
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
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

    String limitStr = req.getParameter("limit");
    Integer limit = null;
    if (limitStr != null) {
      limit = (limitStr.isEmpty() ? 25 : Integer.parseInt(limitStr));
    }

    String pageStr = req.getParameter("page");
    Integer page = null;
    if (pageStr != null) {
      page = (pageStr.isEmpty() ? 1 : Integer.parseInt(pageStr));
    }

    String order = req.getParameter("order");

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "User is not Group admin to get the expired documents"
        );
      }
      PagedNodes result = this.expiredApi.groupsIdDocumentsExpiredGet(
        id,
        limit,
        page,
        order
      );
      model.put("data", result.getData());
      model.put("total", result.getTotal());
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
