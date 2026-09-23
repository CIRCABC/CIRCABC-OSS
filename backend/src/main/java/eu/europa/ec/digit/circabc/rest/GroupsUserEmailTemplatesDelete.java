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
 * Alfresco webscript endpoint that deletes one or more email templates from the
 * current user's personal email template space.
 *
 * <p>The class name implies an HTTP {@code DELETE} request. The templates to
 * remove are identified by the mandatory {@code templateIds} request parameter,
 * whose value is a (comma-separated) list of template identifiers. Deletion is
 * delegated to {@link EmailApi#deleteUserMailTemplates(String)}.</p>
 *
 * <p>On success an empty model is returned. If the caller lacks the required
 * permissions the response is set to {@link Status#STATUS_FORBIDDEN}; any other
 * failure results in {@link Status#STATUS_NOT_ACCEPTABLE}.</p>
 *
 * @author schwerr
 */
public class GroupsUserEmailTemplatesDelete extends DeclarativeWebScript {

  /** Logger used to report errors raised while deleting the templates. */
  static final Log logger = LogFactory.getLog(
    GroupsUserEmailTemplatesDelete.class
  );

  /** API used to perform the actual deletion of the user's email templates. */
  @Autowired
  private EmailApi emailApi;

  /**
   * Handles the delete request by removing the email templates identified by the
   * {@code templateIds} request parameter from the current user's template space.
   *
   * @param req the webscript request; must provide a non-empty {@code templateIds}
   *            parameter listing the template identifiers to delete
   * @param status the response status, updated to {@code FORBIDDEN} on access
   *               denial or {@code NOT_ACCEPTABLE} on any other error
   * @param cache the cache directives for the response
   * @return an empty model on success, or {@code null} when an error occurred and
   *         the status has been set for redirection
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      String ids = req.getParameter("templateIds");

      if (ids == null || "".equals(ids)) {
        throw new IllegalArgumentException(
          "'templateIds' cannot be empty. It's mandatory."
        );
      }

      this.emailApi.deleteUserMailTemplates(ids);
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
