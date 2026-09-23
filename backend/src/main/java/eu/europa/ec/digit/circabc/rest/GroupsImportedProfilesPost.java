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
 * Alfresco declarative web script handling the HTTP {@code POST} request that imports a previously
 * exported profile into an Interest Group.
 *
 * <p>The endpoint is bound to an Interest Group identified by the {@code igId} URL template
 * variable and expects the profile definition to be supplied in the request body as JSON. An
 * optional {@code language} request parameter controls the content locale used while reading and
 * writing multilingual properties: when omitted the script operates in multilingual-aware mode,
 * otherwise the given language is applied and multilingual awareness is disabled for the duration
 * of the call.
 *
 * <p>Only callers holding the directory administrator permission
 * ({@link DirectoryPermissions#DIRADMIN}) on the target group are allowed to perform the import;
 * insufficient permissions result in an HTTP {@code 403 Forbidden} response while malformed input
 * or invalid node references yield an HTTP {@code 400 Bad Request} response. On success the created
 * profile is exposed in the model under the {@code profile} key for rendering by the associated
 * FreeMarker template.
 */
public class GroupsImportedProfilesPost extends CircabcDeclarativeWebScript {

  /** Logger used to report access and request-processing errors. */
  static final Log logger = LogFactory.getLog(GroupsImportedProfilesPost.class);

  /** API providing the profile business operations, including importing exported profiles. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Service used to verify that the current user holds the required directory permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Imports an exported profile into the Interest Group referenced by the {@code igId} URL template
   * variable.
   *
   * <p>The method resolves the target group node, applies the requested content locale (or enables
   * multilingual awareness when no {@code language} parameter is given), checks that the current
   * user has directory administrator rights, parses the profile from the request body and delegates
   * the import to {@link ProfilesApi}. The original multilingual awareness state is always restored
   * before returning.
   *
   * @param req the web script request, providing the {@code igId} template variable, the optional
   *     {@code language} parameter and the JSON profile body
   * @param status the response status, set to {@code 403} on access denial or {@code 400} on
   *     invalid input
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the imported profile under the {@code profile} key on success,
   *     or {@code null} when an error status has been set
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
          "Impossible to import an exported profile, not enough permissions"
        );
      }

      Profile body = ProfileJsonParser.parsePartial(req);
      NodeRef groupNodeRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        id
      );
      model.put(
        "profile",
        this.profilesApi.groupsIdImportedProfilesPost(groupNodeRef, body)
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
}
