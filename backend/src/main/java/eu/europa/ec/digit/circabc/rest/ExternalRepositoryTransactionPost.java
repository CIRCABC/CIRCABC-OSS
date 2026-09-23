package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.model.ExternalRepositoryTransaction;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AresBridgeJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint handling the HTTP {@code POST} for external
 * repository transactions (as implied by the {@code Post} suffix of the class
 * name).
 *
 * <p>The endpoint records one or more transactions coming from an external
 * repository bridge (currently the ARES Bridge) against the CIRCABC nodes they
 * relate to. The target external repository is identified by the {@code id}
 * template variable taken from the request URL, and the transaction payload is
 * read from the request body as JSON.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Guest and external users are rejected with an HTTP
 *       {@code 403 Forbidden} response.</li>
 *   <li>When the {@code id} matches the ARES Bridge identifier, the request body
 *       is parsed into a list of {@link ExternalRepositoryTransaction} entries.
 *       The interest group is resolved from the first transaction's node, and
 *       every transaction is persisted through {@link AresBridgeApi}.</li>
 *   <li>Malformed request bodies (JSON parsing / IO failures) result in an HTTP
 *       {@code 400 Bad Request} response.</li>
 * </ul>
 */
public class ExternalRepositoryTransactionPost
  extends CircabcDeclarativeWebScript
{

  /** API used to persist external repository (ARES Bridge) transactions. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to determine the current user's access level (guest / external). */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request: validates the caller, parses the incoming
   * external repository transactions and stores them against the resolved
   * interest group.
   *
   * @param req the web script request; provides the {@code id} template
   *        variable identifying the external repository and the JSON body
   *        containing the transactions to save
   * @param status the response status, set to {@code 403} for forbidden callers
   *        or {@code 400} when the request body cannot be parsed
   * @param cache the response cache directives
   * @return an (empty) model map on success, or {@code null} when the request is
   *         rejected (forbidden or bad request) and a redirect status has been set
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

    if (
      currentUserPermissionCheckerService.isGuest() ||
      currentUserPermissionCheckerService.isExternalUser()
    ) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    try {
      if (id != null) {
        List<ExternalRepositoryTransaction> externalRepositoryTransactions =
          AresBridgeJsonParser.parseTransaction(req);
        if (
          id.equalsIgnoreCase(AresBridgeApiImpl.ARES_BRIDGE) &&
          !externalRepositoryTransactions.isEmpty()
        ) {
          NodeRef firstNodeRef = Converter.createNodeRefFromId(
            externalRepositoryTransactions.get(0).getNodeId()
          );
          String groupId = apiToolBox
            .getCurrentInterestGroup(firstNodeRef)
            .getId();
          for (ExternalRepositoryTransaction item : externalRepositoryTransactions) {
            this.aresBridgeApi.saveTransaction(
              groupId,
              id,
              item.getTransactionId(),
              item.getNodeId(),
              item.getVersionLabel(),
              item.getName()
            );
          }
        }
      }
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
