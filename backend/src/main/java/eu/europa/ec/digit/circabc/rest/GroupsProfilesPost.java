package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ProfilesApi;
import io.swagger.model.Profile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.ProfileJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code POST} endpoint used to
 * create a new profile within an Interest Group's directory.
 *
 * <p>The endpoint is addressed by the group (Interest Group) identifier supplied
 * as the {@code igId} URL template variable and expects the profile definition to
 * be provided in the JSON request body. An optional {@code language} request
 * parameter controls the content locale: when omitted the script operates in a
 * multilingual-aware mode, otherwise the given language is applied as the content
 * and UI locale.</p>
 *
 * <p>Before creating the profile the caller is checked for directory
 * administrator rights ({@link DirectoryPermissions#DIRADMIN}) on the target
 * group. On success the created {@link Profile} is placed in the returned model
 * under the {@code profile} key. Insufficient rights result in an HTTP 403
 * (Forbidden) response, while an invalid group reference or malformed request
 * body result in an HTTP 400 (Bad Request) response.</p>
 */
public class GroupsProfilesPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsProfilesPost.class);

  /**
   * API used to perform profile-related business operations, such as creating a
   * new profile for a group.
   */
  @Autowired
  private ProfilesApi profilesApi;

  /**
   * Service used to verify that the current user holds the permissions required
   * to create a profile on the target group's directory.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code POST} request that creates a new profile for the Interest
   * Group identified by the {@code igId} URL template variable.
   *
   * <p>The profile definition is read from the JSON request body. The optional
   * {@code language} request parameter selects the content locale; when absent the
   * script runs in multilingual-aware mode. The current user must hold directory
   * administrator rights on the target group, otherwise access is denied.</p>
   *
   * @param req the web script request, providing the {@code igId} template
   *     variable, the optional {@code language} parameter and the JSON body
   *     describing the profile to create
   * @param status the response status, set to 403 when the user lacks the
   *     required rights and to 400 when the group reference or request body is
   *     invalid
   * @param cache the cache directives for the response
   * @return a model map containing the created {@link Profile} under the
   *     {@code profile} key, or {@code null} when the request fails and an error
   *     status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
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
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
          id,
          DirectoryPermissions.DIRADMIN
        )
      ) {
        throw new AccessDeniedException(
          "Not enough rights for creating a new profile"
        );
      }
      Profile body = ProfileJsonParser.parsePartial(req);
      NodeRef groupNodeRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        id
      );
      model.put(
        "profile",
        this.profilesApi.groupsIdProfilesPost(groupNodeRef, body)
      );
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
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

  /**
   * @return the profilesApi
   */
  public ProfilesApi getProfilesApi() {
    return this.profilesApi;
  }

  /**
   * @param profilesApi the profilesApi to set
   */
  public void setProfilesApi(ProfilesApi profilesApi) {
    this.profilesApi = profilesApi;
  }

  /**
   * Sets the service used to check the current user's directory permissions.
   *
   * @param currentUserPermissionCheckerService the permission checker service to
   *     set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
