package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
 * Alfresco declarative web script backing the HTTP {@code GET} endpoint that retrieves the direct
 * subforums of a given forum.
 *
 * <p>The forum is identified by the {@code id} template variable taken from the request URL. Before
 * any data is returned, the current user's read permission on the forum node is verified; if the
 * user lacks read access an {@link AccessDeniedException} is raised and the response is set to
 * {@code 403 Forbidden}. When the referenced node does not exist or is otherwise invalid the
 * response is set to {@code 400 Bad Request}.
 *
 * <p>Supported request parameters:
 *
 * <ul>
 *   <li>{@code language} &mdash; optional locale code. When supplied, the content and UI locale are
 *       set to that language and multilingual (ML) awareness is disabled so that values are returned
 *       in the requested language; when omitted, ML awareness is enabled.
 *   <li>{@code sort} and {@code order} &mdash; optional sorting hints. When both are non-empty they
 *       are combined into a single {@code sort_order} directive passed to the underlying API.
 * </ul>
 *
 * <p>The resulting model exposes the forum {@code id} and the list of subforum {@code nodes} to the
 * associated FreeMarker template.
 *
 * @author beaurpi
 */
public class ForumSubforumsGet extends DeclarativeWebScript {

  /** Logger used to report permission and node-resolution errors. */
  static final Log logger = LogFactory.getLog(ForumSubforumsGet.class);

  /** API providing forum-related business operations, including subforum retrieval. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to verify the current user's Alfresco read permission on the forum node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming web script request and builds the response model containing the forum's
   * subforums.
   *
   * <p>Resolves the forum {@code id} from the URL template variables, applies the optional
   * {@code language} locale, checks read permission for the current user and, when both
   * {@code sort} and {@code order} parameters are present, applies the combined sort directive. The
   * original ML-awareness flag is always restored before the method returns.
   *
   * @param req the web script request, carrying the {@code id} template variable and the optional
   *     {@code language}, {@code sort} and {@code order} parameters
   * @param status the web script response status, updated to {@code 403} or {@code 400} on error
   * @param cache the web script cache control object for the response
   * @return a model map containing the forum {@code id} and the list of subforum {@code nodes}, or
   *     {@code null} when the request is rejected due to denied access or an invalid node reference
   * @throws AccessDeniedException if the current authority does not have read permission on the
   *     forum (caught internally and translated to a {@code 403 Forbidden} response)
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

    String sort = req.getParameter("sort");
    String order = req.getParameter("order");

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
    try {
      if (id != null) {
        model.put("id", id);

        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            id
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot access the forum, not enough permission"
          );
        }

        if (!Objects.equals(sort, "") && !Objects.equals(order, "")) {
          model.put(
            "nodes",
            this.forumsApi.forumsIdSubforumsGet(id, sort + "_" + order)
          );
        } else {
          model.put("nodes", this.forumsApi.forumsIdSubforumsGet(id));
        }
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
