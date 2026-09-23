package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EmailApi;
import io.swagger.model.MailTemplateDefinition;
import java.util.HashMap;
import java.util.List;
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
 * Alfresco webscript endpoint that retrieves the list of mail templates created by the currently
 * authenticated user.
 *
 * <p>This is a read-only {@code GET} endpoint (as implied by the {@code Get} suffix in the class
 * name). It takes no request parameters and returns the user's personal mail templates. The
 * resulting model exposes the templates under the {@code "templates"} key for the associated
 * FreeMarker template to render as JSON.
 *
 * <p>Error handling: an {@link AccessDeniedException} results in an HTTP {@code 403 Forbidden}
 * response, while any other failure results in an HTTP {@code 406 Not Acceptable} response.
 *
 * @author schwerr
 */
public class GroupsUserEmailTemplatesGet extends DeclarativeWebScript {

  /** Logger used to report errors that occur while resolving the user's mail templates. */
  static final Log logger = LogFactory.getLog(
    GroupsUserEmailTemplatesGet.class
  );

  /** API used to look up the mail templates owned by the current user. */
  @Autowired
  private EmailApi emailApi;

  /**
   * Handles the webscript request by fetching the mail templates belonging to the current user.
   *
   * @param req the incoming webscript request; no request parameters are read
   * @param status the response status, updated to {@code 403} on access denial or {@code 406} on
   *     any other error
   * @param cache the cache control for the response
   * @return a model map containing the user's mail templates under the {@code "templates"} key, or
   *     {@code null} when an error occurs and the status has been set for redirection
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      List<MailTemplateDefinition> templates =
        this.emailApi.getUserMailTemplates();

      model.put("templates", templates);
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
