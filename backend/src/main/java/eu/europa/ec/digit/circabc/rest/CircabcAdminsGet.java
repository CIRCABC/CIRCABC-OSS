package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CircabcApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
// implemnt Allfresco's WebScript interface to retutn a JSON object

import java.util.HashMap;
import java.util.Locale;
// that return list of Circabc  administrators

import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that returns the list of CIRCABC administrators.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this handles the HTTP
 * {@code GET} request for the {@code circabc/admins} resource and renders the result as JSON.
 *
 * <p>Behavior: the endpoint first verifies that the caller is a CIRCABC administrator or an
 * Alfresco administrator; callers without those privileges receive an HTTP {@code 403 Forbidden}.
 * When authorized, it retrieves the administrators via {@link CircabcApi#getCircabcAdmins()} and
 * exposes them to the response template under the {@code admins} model key.
 *
 * <p>Key inputs (from the request):
 *
 * <ul>
 *   <li>{@code language} (optional) &mdash; an ISO language code. When provided, the content and
 *       UI locale are set accordingly and multilingual (ML) awareness is disabled so that
 *       language-specific values are returned; when omitted, ML awareness is enabled.
 * </ul>
 */
public class CircabcAdminsGet extends DeclarativeWebScript {

  /** Logger used to record errors raised while handling the request. */
  static final Log logger = LogFactory.getLog(CircabcAdminsGet.class);

  /** API facade used to retrieve the CIRCABC administrators. */
  @Autowired
  private CircabcApi circabcApi;

  /** Service used to check whether the current user has administrator privileges. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the GET request and builds the response model containing the CIRCABC administrators.
   *
   * <p>Resolves the optional {@code language} request parameter to configure the locale and
   * multilingual awareness, enforces administrator permissions, and populates the model with the
   * list of administrators under the {@code admins} key. The previous ML-awareness state is always
   * restored before returning.
   *
   * @param req the web script request; may carry an optional {@code language} parameter
   * @param status the response status, updated to {@code 403} on access denial or {@code 400} on an
   *     invalid node reference
   * @param cache the cache directives for the response
   * @return a model map with the {@code admins} entry when authorized, or {@code null} when the
   *     request fails and a redirect status has been set
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
    try {
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isAlfrescoAdmin();
      if (!isAllowed) {
        throw new AccessDeniedException(
          "The user don't have enough permissions"
        );
      }

      model.put("admins", this.circabcApi.getCircabcAdmins());
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
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
