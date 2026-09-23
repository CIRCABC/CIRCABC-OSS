package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.KeywordJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint handling the HTTP {@code PUT} request used to update an existing
 * keyword.
 *
 * <p>The endpoint resolves the keyword to update from the {@code keywordId} URL template variable,
 * walks up the node hierarchy (keyword &rarr; container &rarr; interest group) to determine the
 * owning interest group, and verifies that the current user is a group administrator of that
 * interest group before applying any change. The updated keyword definition is read from the JSON
 * request body and delegated to {@link io.swagger.api.KeywordsApi} for persistence.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code keywordId} URL template variable identifying the keyword node to update.</li>
 *   <li>A JSON request body describing the new keyword definition (see
 *       {@link io.swagger.util.parsers.KeywordJsonParser}).</li>
 * </ul>
 *
 * <p>On success the response model exposes the updated keyword under the {@code keyword} key.
 * Failures are mapped to appropriate HTTP status codes: {@code 403 Forbidden} when the caller lacks
 * group-admin permissions, {@code 400 Bad Request} for invalid node references or malformed request
 * bodies, and {@code 500 Internal Server Error} for any other unexpected error.
 */
public class KeywordsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(KeywordsPut.class);

  /** API providing the business operations to read and update keywords. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Alfresco service used to navigate the node hierarchy of the keyword. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user has the required permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the keyword update request.
   *
   * <p>Resolves the target keyword from the {@code keywordId} template variable, determines the
   * owning interest group by traversing the node's primary parents, and ensures the current user is
   * a group administrator before parsing the JSON body and updating the keyword. When an error
   * occurs, the appropriate HTTP status is set on {@code status} and {@code null} is returned.
   *
   * @param req the web script request, providing the {@code keywordId} template variable and the
   *     JSON keyword definition in its body
   * @param status the response status, used to signal success or the relevant error code
   *     (forbidden, bad request or internal error)
   * @param cache the cache directives for the response
   * @return a model map containing the updated keyword under the {@code keyword} key, or
   *     {@code null} if an error occurred
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
          "Cannot update keyword, not enough permissions"
        );
      }

      KeywordDefinition body = KeywordJsonParser.parseJsonFullKeyword(req);
      model.put("keyword", this.keywordsApi.keywordsKeywordIdPut(id, body));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied when updating keyword", ade);
      }
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Error updating keyword", inre);
      }
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error updating keyword", e);
      }
      return null; // NOSONAR
    }

    return model;
  }
}
