package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EmailApi;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco {@link DeclarativeWebScript} endpoint that persists an email template in the
 * current user's personal email template space.
 *
 * <p>As implied by the {@code Put} suffix in the class name, this endpoint handles the HTTP
 * {@code PUT} method. It reads the template details from the request parameters and delegates
 * the actual save operation to {@link EmailApi#saveUserMailTemplate(String, String, String,
 * boolean)}.
 *
 * <p>Expected request parameters:
 *
 * <ul>
 *   <li>{@code templateName} &mdash; mandatory, the name under which the template is stored.
 *   <li>{@code templateSubject} &mdash; mandatory, the email subject line of the template.
 *   <li>{@code templateText} &mdash; mandatory, the body text of the template.
 *   <li>{@code overwrite} &mdash; whether an existing template with the same name should be
 *       replaced ({@code "true"} to overwrite).
 * </ul>
 *
 * <p>On success the returned model contains the identifier ({@code id}) of the saved template.
 * If the caller lacks permission an HTTP {@code 403 Forbidden} response is produced; any other
 * failure results in an HTTP {@code 406 Not Acceptable} response.
 *
 * @author schwerr
 */
public class GroupsUserEmailTemplatesPut extends DeclarativeWebScript {

  /** Logger used to report errors raised while saving the user's email template. */
  static final Log logger = LogFactory.getLog(
    GroupsUserEmailTemplatesPut.class
  );

  /** Service used to save the email template in the current user's template space. */
  @Autowired
  private EmailApi emailApi;

  /**
   * Handles the {@code PUT} request that saves an email template for the current user.
   *
   * <p>Validates that the {@code templateName}, {@code templateSubject} and {@code templateText}
   * request parameters are present and non-empty, then delegates to
   * {@link EmailApi#saveUserMailTemplate(String, String, String, boolean)} to store the template,
   * honouring the {@code overwrite} flag. Errors are translated into the appropriate HTTP status
   * codes.
   *
   * @param req the web script request carrying the template parameters
   * @param status the response status, updated to {@code 403} on access denial or {@code 406}
   *     on any other error
   * @param cache the cache directives for the response
   * @return a model map containing the {@code id} of the saved template on success, or
   *     {@code null} when an error occurs and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      String name = req.getParameter("templateName");

      if (name == null || "".equals(name)) {
        throw new IllegalArgumentException(
          "'templateName' cannot be empty. It's mandatory."
        );
      }

      String subject = req.getParameter("templateSubject");

      if (subject == null || "".equals(subject)) {
        throw new IllegalArgumentException(
          "'templateSubject' cannot be empty. Templates without subject are not allowed."
        );
      }

      String text = req.getParameter("templateText");

      if (text == null || "".equals(text)) {
        throw new IllegalArgumentException(
          "'templateText' cannot be empty. Templates without text are not allowed."
        );
      }

      boolean overwrite = req.getParameter("overwrite").equals("true");

      String id = this.emailApi.saveUserMailTemplate(
        name,
        subject,
        text,
        overwrite
      );

      model.put("id", id);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
