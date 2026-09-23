package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.eulogin.EULoginService;
import eu.europa.ec.digit.circabc.rest.service.eulogin.EULoginUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.cas.client.validation.TicketValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Web script that handles EU Login (ECAS) CAS authentication flow.
 *
 * <p>This is a thin controller that delegates all business logic to {@link EULoginService}.
 * Flow:
 * <ol>
 *   <li>Frontend redirects user to this endpoint</li>
 *   <li>If no CAS ticket parameter, redirects to ECAS login page</li>
 *   <li>ECAS redirects back with a service ticket</li>
 *   <li>This endpoint validates the ticket via ECAS laxValidate</li>
 *   <li>Ensures the user exists in Alfresco (creates from LDAP if needed)</li>
 *   <li>Generates an Alfresco authentication ticket</li>
 *   <li>Sets cookies (username, ticket, route) and redirects to frontend</li>
 *   <li>On CAS validation failure or other errors, responds with plain-text status messages.</li>
 * </ol>
 */
public class EULoginGet extends AbstractWebScript {

  /** Commons Logging logger used to trace the EU Login authentication flow. */
  private static final Log logger = LogFactory.getLog(EULoginGet.class);

  /** Shared content type used by plain-text error responses. */
  private static final String TEXT_PLAIN_UTF8 = "text/plain;charset=UTF-8";

  /**
   * Service encapsulating all EU Login (ECAS) business logic: building redirect URLs,
   * validating CAS tickets, provisioning users, generating Alfresco tickets and setting cookies.
   */
  @Autowired
  private EULoginService euLoginService;

  /**
   * Alfresco transaction service used to obtain a {@link RetryingTransactionHelper} so user
   * provisioning and Alfresco ticket generation can run inside their own transactions.
   */
  @Autowired
  private TransactionService transactionService;

  /**
   * Handles an HTTP GET request for the EU Login (ECAS) CAS authentication flow.
   *
   * <p>Behavior depends on whether a CAS service {@code ticket} request parameter is present:
   * <ul>
   *   <li>If no ticket is present, the caller is redirected to the ECAS login page.</li>
   *   <li>If a ticket is present, it is validated against ECAS; on success the user is
   *   provisioned in Alfresco (as system, within a transaction), an Alfresco authentication
   *   ticket is generated, authentication cookies are set, and the caller is redirected to
   *   the configured frontend target.</li>
   * </ul>
   *
   * <p>On a CAS validation failure the response status is set to {@code 403 Forbidden};
   * on any other unexpected error it is set to {@code 500 Internal Server Error}.
   *
   * @param req the web script request; reads the {@code ticket} and {@code route} parameters
   * @param res the web script response; must be backed by a servlet response so that HTTP
   *            redirects and cookies can be written
   * @throws IOException if writing the redirect, status or error body to the response fails
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    HttpServletResponse httpResponse = getHttpServletResponse(res);
    String casTicket = req.getParameter("ticket");
    String route = req.getParameter("route");

    if (casTicket == null || casTicket.isEmpty()) {
      String redirectUrl = euLoginService.buildLoginRedirectUrl(route);

      if (logger.isDebugEnabled()) {
        logger.debug(
          "No CAS ticket present, redirecting to ECAS: " + redirectUrl
        );
      }

      httpResponse.sendRedirect(redirectUrl);
      return;
    }

    try {
      String serviceUrl = euLoginService.buildServiceUrl(route);
      EULoginUserDetails userDetails = euLoginService.validateCasTicket(
        casTicket,
        serviceUrl
      );
      String username = userDetails.getUsername();

      if (logger.isDebugEnabled()) {
        logger.debug("CAS ticket validated successfully for user: " + username);
      }

      // Run user provisioning and ticket generation as system in a transaction.
      // The webscript uses <transaction>none</transaction> to keep servlet response
      // access for redirects, so we create our own transaction here.
      RetryingTransactionHelper txHelper =
        transactionService.getRetryingTransactionHelper();

      AuthenticationUtil.runAsSystem(() -> {
        txHelper.doInTransaction(
          () -> {
            euLoginService.ensureUserExists(username, userDetails);
            return null;
          },
          false,
          true
        );
        return null;
      });

      String alfrescoTicket = AuthenticationUtil.runAsSystem(() ->
        txHelper.doInTransaction(
          () -> euLoginService.generateAlfrescoTicket(username),
          false,
          true
        )
      );

      euLoginService.setCookies(httpResponse, username, alfrescoTicket, route);

      String redirectTarget = euLoginService.buildRedirectTarget(route);

      if (logger.isDebugEnabled()) {
        logger.debug(
          "Authentication successful for " +
            username +
            ", redirecting to: " +
            redirectTarget
        );
      }

      httpResponse.sendRedirect(redirectTarget);
    } catch (TicketValidationException e) {
      logger.error("CAS ticket validation failed", e);
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      httpResponse.setContentType(TEXT_PLAIN_UTF8);
      httpResponse
        .getWriter()
        .write("Authentication failed: CAS ticket validation error");
    } catch (AuthenticationException e) {
      logger.error("EU Login denied: account is disabled", e);
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      httpResponse.setContentType(TEXT_PLAIN_UTF8);
      httpResponse.getWriter().write("Authentication failed: account disabled");
    } catch (Exception e) {
      logger.error("Unexpected error during EU Login authentication", e);
      httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      httpResponse.setContentType(TEXT_PLAIN_UTF8);
      httpResponse.getWriter().write("Authentication failed: internal error");
    }
  }

  /**
   * Extracts the underlying {@link HttpServletResponse} from the web script response so that
   * redirects and cookies can be written directly.
   *
   * @param res the web script response to unwrap
   * @return the underlying servlet HTTP response
   * @throws WebScriptException with status {@code 500 Internal Server Error} if the response is
   *                            not backed by a servlet container (i.e. not a
   *                            {@link WebScriptServletResponse})
   */
  private HttpServletResponse getHttpServletResponse(WebScriptResponse res) {
    if (res instanceof WebScriptServletResponse servletResponse) {
      return servletResponse.getHttpServletResponse();
    }
    throw new WebScriptException(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "Cannot access HttpServletResponse — this web script requires servlet container"
    );
  }
}
