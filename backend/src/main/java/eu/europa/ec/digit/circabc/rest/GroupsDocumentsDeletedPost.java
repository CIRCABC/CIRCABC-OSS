package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ArchiveApi;
import io.swagger.model.RestoreNodeMetadata;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.RestoreNodeMetadataParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST endpoint that restores archived (deleted) documents of an Interest Group (IG).
 *
 * <p>Handles the HTTP {@code POST} request mapped to
 * {@code /circabc/groups/{igId}/documents/deleted}. It restores one or more nodes that were
 * previously moved to the archive (trash) of the given Interest Group back into the repository.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code igId} template variable identifying the target Interest Group.</li>
 *   <li>{@code language} optional request parameter controlling the content locale; when absent the
 *       response is returned in multilingual (ML aware) mode, otherwise the given locale is applied
 *       and ML awareness is disabled.</li>
 *   <li>JSON request body deserialized into a {@link RestoreNodeMetadata} describing the nodes to
 *       restore.</li>
 * </ul>
 *
 * <p>The caller must be a Group administrator of the Interest Group; otherwise the request is
 * rejected with an HTTP 403 (Forbidden). Malformed input results in an HTTP 400 (Bad request).
 *
 * @see CircabcDeclarativeWebScript
 * @see ArchiveApi#groupsIdDocumentsDeletedPost(String, RestoreNodeMetadata)
 */
public class GroupsDocumentsDeletedPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsDocumentsDeletedPost.class);

  /** API providing archive (trash) operations, including restoring deleted IG documents. */
  @Autowired
  private ArchiveApi archiveApi;

  /** Service used to verify that the current user has Group administrator rights on the IG. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Restores the archived / deleted documents of the Interest Group identified by the {@code igId}
   * template variable.
   *
   * <p>Resolves the requested content locale (or enables multilingual mode when no {@code language}
   * parameter is supplied), verifies that the current user is a Group administrator, parses the
   * {@link RestoreNodeMetadata} from the request body and delegates the restore operation to
   * {@link ArchiveApi#groupsIdDocumentsDeletedPost(String, RestoreNodeMetadata)}. The previous
   * multilingual awareness state is always restored before returning.
   *
   * <p>On failure the method sets the appropriate HTTP status and returns {@code null}: HTTP 403
   * when the user is not a Group administrator, and HTTP 400 for an invalid node reference or an
   * unparseable request body.
   *
   * @param req the web script request; provides the {@code igId} template variable, the optional
   *     {@code language} parameter and the JSON body to restore
   * @param status the response status, updated to reflect forbidden or bad-request outcomes
   * @param cache the response cache control settings
   * @return an (empty) model map on success, or {@code null} when the request is rejected and a
   *     redirect status has been set
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

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "User is not Group admin to restore a deleted document"
        );
      }
      RestoreNodeMetadata body = RestoreNodeMetadataParser.parseJSon(req);
      this.archiveApi.groupsIdDocumentsDeletedPost(id, body);
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
