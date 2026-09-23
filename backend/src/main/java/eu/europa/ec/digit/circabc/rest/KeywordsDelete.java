package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.KeywordsApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script that handles the HTTP DELETE of a single
 * keyword.
 *
 * <p>The endpoint identifies the keyword to remove from the {@code keywordId}
 * URL template variable. Before deleting, it resolves the keyword's enclosing
 * Interest Group (by walking up the node hierarchy: keyword &rarr; container
 * &rarr; Interest Group) and verifies that the current user is a group
 * administrator of that Interest Group. If the permission check passes, the
 * keyword is deleted through {@link KeywordsApi} and a model containing a
 * {@code "message"} entry set to {@code "ok"} is returned.</p>
 *
 * <p>On failure the appropriate HTTP status is set: {@code 403 Forbidden} when
 * the user lacks permission, {@code 400 Bad Request} when the node reference is
 * invalid, and {@code 500 Internal Server Error} for any other error.</p>
 */
public class KeywordsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(KeywordsDelete.class);

  /** API providing the keyword business operations, including deletion. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Alfresco service used to navigate the node hierarchy around the keyword. */
  @Autowired
  private NodeService nodeService;

  /** Service used to check that the current user is a group administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Deletes the keyword identified by the {@code keywordId} URL template
   * variable, after checking that the current user is a group administrator of
   * the Interest Group that owns the keyword.
   *
   * @param req the web script request; must expose a {@code keywordId} template
   *            variable identifying the keyword node to delete
   * @param status the response status, populated with an error code, message
   *               and redirect flag when the operation fails
   * @param cache the cache control object for the response
   * @return a model map containing a {@code "message"} entry set to
   *         {@code "ok"} on success, or {@code null} when an error occurs (in
   *         which case {@code status} carries the failure details)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("keywordId");

    try {
      NodeRef keywordId = Converter.createNodeRefFromId(id);
      NodeRef containerId = this.nodeService.getPrimaryParent(
        keywordId
      ).getParentRef();
      NodeRef igId = this.nodeService.getPrimaryParent(
        containerId
      ).getParentRef();

      if (
        !this.currentUserPermissionCheckerService.isGroupAdmin(igId.getId())
      ) {
        throw new AccessDeniedException(
          "Cannot remove keyword, not enough permissions"
        );
      }

      this.keywordsApi.keywordsKeywordIdDelete(id);
      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error("Access denied deleting keyword. keywordId: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference deleting keyword. keywordId: " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error deleting keyword. keywordId: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
