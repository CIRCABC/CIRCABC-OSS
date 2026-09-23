package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.model.MultilingualAspectMetadata;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.MultilingualJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that handles the HTTP {@code POST} request used to
 * set (or update) the multilingual aspect metadata of a content node.
 *
 * <p>The endpoint is bound to a content node identified by the {@code id}
 * template variable in the request URL. Before applying any change it verifies
 * that the current user holds at least one of the required library
 * permissions ({@code LIBMANAGEOWN}, {@code LIBEDITONLY}, {@code LIBFULLEDIT}
 * or {@code LIBADMIN}); otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden}. The multilingual metadata is parsed from the JSON
 * request body and delegated to {@link ContentApi} for persistence.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &ndash; URL template variable identifying the target
 *       content node.</li>
 *   <li>JSON request body &ndash; parsed into a
 *       {@link MultilingualAspectMetadata} instance describing the
 *       multilingual aspect to apply.</li>
 * </ul>
 *
 * @see CircabcDeclarativeWebScript
 */
public class ContentMultilingualAspectPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    ContentMultilingualAspectPost.class
  );

  /** API used to apply the multilingual aspect metadata to the content node. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to check the current user's library permissions on the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request: validates the caller's permissions on the target
   * content node and, if authorised, applies the multilingual aspect metadata
   * provided in the JSON request body.
   *
   * <p>On failure the method sets the appropriate HTTP status on the given
   * {@link Status} object and returns {@code null}:</p>
   * <ul>
   *   <li>{@code 403 Forbidden} when the current user lacks the required
   *       library permissions.</li>
   *   <li>{@code 400 Bad Request} when the node reference is invalid or the
   *       request body cannot be parsed.</li>
   * </ul>
   *
   * @param req the web script request; provides the {@code id} template
   *            variable and the JSON body with the multilingual metadata
   * @param status the response status, updated with error codes and messages
   *               when the request cannot be fulfilled
   * @param cache the cache directives for the response
   * @return the (empty) model map on success, or {@code null} when an error
   *         status and redirect have been set
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
    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBMANAGEOWN,
          LibraryPermissions.LIBEDITONLY,
          LibraryPermissions.LIBFULLEDIT,
          LibraryPermissions.LIBADMIN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
        );
      }
      MultilingualAspectMetadata body =
        MultilingualJsonParser.parseAspectMetadata(req);
      this.contentApi.contentIdMultilingualAspectPost(id, body);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
