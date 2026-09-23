package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.model.TicketRequestInfo;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AresBridgeJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that issues an authentication ticket for an external
 * repository bridge.
 *
 * <p>As implied by the {@code Post} suffix in the class name, this endpoint is
 * bound to the HTTP {@code POST} method. It expects a path template variable
 * {@code id} identifying the external repository and a JSON request body that is
 * parsed into a {@link io.swagger.model.TicketRequestInfo} (carrying the request
 * date, the HTTP verb and the target path). When the {@code id} matches the ARES
 * Bridge identifier, it delegates to {@link io.swagger.api.AresBridgeApi} to
 * generate a ticket, which is returned in the model under the {@code "ticket"}
 * key.</p>
 *
 * <p>Access is restricted: guest and external users receive an HTTP
 * {@code 403 Forbidden} response, and malformed input results in an HTTP
 * {@code 400 Bad Request} response.</p>
 */
public class ExternalRepositoryTicketPost extends CircabcDeclarativeWebScript {

  /** API used to generate authentication tickets for the ARES Bridge repository. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to determine whether the current user is a guest or an external user. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the ticket request for an external repository.
   *
   * <p>Rejects guest and external users with a {@code 403 Forbidden} status.
   * Otherwise, reads the {@code id} template variable and, when it matches the
   * ARES Bridge identifier, parses the request body and requests a ticket from
   * {@link AresBridgeApi}. Parsing or I/O failures produce a
   * {@code 400 Bad Request} status.</p>
   *
   * @param req    the incoming webscript request, providing the {@code id}
   *               template variable and the JSON request body
   * @param status the response status to populate on error (forbidden or bad request)
   * @param cache  the cache directives for the response
   * @return a model map containing the generated {@code "ticket"}, or {@code null}
   *         when access is denied or the request is malformed
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
        TicketRequestInfo ticketRequestInfo = AresBridgeJsonParser.parse(req);
        if (id.equalsIgnoreCase(AresBridgeApiImpl.ARES_BRIDGE)) {
          model.put(
            "ticket",
            this.aresBridgeApi.getTicket(
              ticketRequestInfo.getRequestDate(),
              ticketRequestInfo.getHttpVerb(),
              ticketRequestInfo.getPath()
            )
          );
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
