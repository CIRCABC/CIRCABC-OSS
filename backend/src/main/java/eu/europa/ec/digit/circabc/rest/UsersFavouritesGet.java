package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.FavouritesApi;
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
 * Alfresco Web Script endpoint that serves an HTTP {@code GET} request for the
 * favourite nodes of a given user.
 *
 * <p>The endpoint is bound to a URL carrying a {@code userId} template variable
 * (e.g. {@code /users/{userId}/favourites}) and returns the paginated list of
 * nodes that the user has marked as favourites. A user may only retrieve their
 * own favourites: if the authenticated caller does not match the requested
 * {@code userId} the request is rejected with an HTTP 403 (Forbidden).</p>
 *
 * <p>Supported request inputs:</p>
 * <ul>
 *   <li>{@code userId} (URL template variable) - the identifier of the user
 *       whose favourites are requested.</li>
 *   <li>{@code language} (optional query parameter) - locale used to resolve
 *       multilingual (ML) content; when omitted the response stays ML-aware.</li>
 *   <li>{@code page} (optional query parameter) - 1-based page number; defaults
 *       to the first page.</li>
 *   <li>{@code limit} (optional query parameter) - maximum number of results
 *       per page; defaults to {@value #DEFAULT_NUMBER_RESULTS}.</li>
 * </ul>
 *
 * <p>The response model exposes {@code data} (the list of favourite nodes) and
 * {@code total} (the total number of favourites).</p>
 *
 * @author beaurpi
 */
public class UsersFavouritesGet extends DeclarativeWebScript {

  /**
   * Logger for this endpoint.
   */
  static final Log logger = LogFactory.getLog(UsersFavouritesGet.class);

  /** Internal (0-based) index of the first page of results. */
  private static final int START_PAGE = 0;

  /** Default page size used when no {@code limit} parameter is supplied. */
  private static final int DEFAULT_NUMBER_RESULTS = 25;

  /** API used to look up the favourite nodes of a user. */
  @Autowired
  private FavouritesApi favouritesApi;

  /** Service used to verify that the caller is the same user being queried. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request by resolving the target user from the {@code userId}
   * URL template variable, enforcing that the caller may only read their own
   * favourites, and fetching the requested page of favourite nodes.
   *
   * <p>The {@code language} parameter toggles multilingual awareness and the
   * content/UI locale; the original ML-aware state is always restored before
   * returning. On error the method sets the appropriate HTTP status, marks the
   * response as a redirect and returns {@code null}.</p>
   *
   * @param req the web script request; provides the {@code userId} template
   *            variable and the optional {@code language}, {@code page} and
   *            {@code limit} query parameters
   * @param status the response status, updated to 400 or 403 when the request
   *               cannot be fulfilled
   * @param cache the cache directives for the response
   * @return a model map containing {@code data} (the favourite nodes) and
   *         {@code total} (their overall count), or {@code null} if the request
   *         failed and an error status was set
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

    String page = req.getParameter("page");
    int nbPage = START_PAGE;
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

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String userId = templateVars.get("userId");

      if (!(currentUserPermissionCheckerService.isCurrentUserEqualTo(userId))) {
        throw new AccessDeniedException(
          "Cannot get user favourite of somebody else"
        );
      }

      if (userId != null) {
        PagedNodes favs = this.favouritesApi.usersUserIdFavouritesGet(
          userId,
          nbPage,
          nbLimit
        );
        model.put("data", favs.getData());
        model.put("total", favs.getTotal());
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
