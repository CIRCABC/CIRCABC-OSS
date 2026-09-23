package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ProfilesApi;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
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
 * Alfresco {@link DeclarativeWebScript} endpoint that handles the HTTP {@code GET} request for
 * retrieving the profiles defined within an Interest Group.
 *
 * <p>The endpoint resolves the Interest Group from the {@code igId} path template variable and,
 * after verifying that the current user holds directory access rights on it, returns the list of
 * profiles under the {@code "profiles"} key of the response model. Rendering of the JSON response
 * is delegated to the associated FreeMarker template.
 *
 * <p>Supported request parameters:
 * <ul>
 *   <li>{@code language} &ndash; optional locale code; when supplied the content and UI locale are
 *       set to this value and multilingual (ML) awareness is disabled so that a single localized
 *       value is returned. When omitted, ML awareness is enabled and multilingual values are
 *       returned.</li>
 *   <li>{@code searchQuery} &ndash; optional filter applied to the profiles.</li>
 *   <li>{@code nonEmptyProfiles} &ndash; when equal to {@code "true"}, restricts the result to
 *       profiles that contain at least one member.</li>
 * </ul>
 *
 * <p>On failure the endpoint sets an appropriate HTTP status: {@code 403 Forbidden} when the user
 * lacks the required directory permission, and {@code 400 Bad Request} when the Interest Group node
 * reference is invalid.
 */
public class GroupsProfilesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsProfilesGet.class);

  /** API used to look up the profiles belonging to an Interest Group. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Service used to verify that the current user holds the required directory permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request and builds the response model containing the Interest Group's
   * profiles.
   *
   * <p>The Interest Group identifier is read from the {@code igId} path template variable. Locale
   * and multilingual awareness are configured based on the {@code language} request parameter, the
   * current user's directory permissions are checked, and the matching profiles are placed under
   * the {@code "profiles"} model key. The original ML-awareness state is always restored before the
   * method returns.
   *
   * @param req the web script request providing the {@code igId} template variable and the
   *     {@code language}, {@code searchQuery} and {@code nonEmptyProfiles} parameters
   * @param status the response status, updated to {@code 403} or {@code 400} on error
   * @param cache the response cache control settings
   * @return a model map with the {@code "profiles"} entry on success, or {@code null} when an error
   *     occurred and a redirect status has been set
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
    String searchQuery = req.getParameter("searchQuery");

    boolean nonEmptyProfiles = "true".equals(
      req.getParameter("nonEmptyProfiles")
    );

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
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for retrieving profiles"
        );
      }
      model.put(
        "profiles",
        this.profilesApi.groupsIdProfilesGet(id, searchQuery, nonEmptyProfiles)
      );
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
