package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ProfilesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code DELETE} endpoint that
 * removes an existing profile from an Interest Group.
 *
 * <p>The profile to delete is identified by its Alfresco node id, which is
 * supplied as the {@code id} template variable in the request URL and resolved
 * against the {@link StoreRef#STORE_REF_WORKSPACE_SPACESSTORE workspace spaces
 * store}. An optional {@code language} request parameter controls the content
 * locale used while the request is processed; when it is omitted the script
 * operates in a multilingual-aware mode.</p>
 *
 * <p>Deletion is only permitted for callers who are group administrators of the
 * Interest Group that owns the profile. When the caller lacks the required
 * permission an {@link AccessDeniedException} is raised and translated into an
 * HTTP {@code 403 Forbidden} response. Invalid node references result in an
 * HTTP {@code 400 Bad Request}, and any other failure results in an HTTP
 * {@code 500 Internal Server Error}. On success the actual removal is delegated
 * to {@link ProfilesApi#profilesIdDelete(NodeRef)}.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see ProfilesApi
 */
public class ProfilesDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ProfilesDelete.class);

  /** API providing the profile business operations, including deletion. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Service used to verify that the current user is a group administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to resolve the profile's owning Interest Group. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the profile deletion request.
   *
   * <p>Resolves the profile node from the {@code id} template variable, applies
   * the requested content locale (or enables multilingual-aware mode when no
   * {@code language} parameter is present), checks that the caller is a group
   * administrator of the owning Interest Group and, if so, records the state of
   * the profile before deletion and delegates the removal to the
   * {@link ProfilesApi}. The multilingual-aware flag is always restored to its
   * previous value before returning.</p>
   *
   * <p>Errors are handled internally and surfaced through the response
   * {@code status} rather than by propagating exceptions: an
   * {@link AccessDeniedException} maps to {@code 403 Forbidden}, an
   * {@link InvalidNodeRefException} maps to {@code 400 Bad Request}, and any
   * other exception maps to {@code 500 Internal Server Error}.</p>
   *
   * @param req    the web script request; provides the {@code id} template
   *               variable and the optional {@code language} parameter
   * @param status the response status, updated with an error code, message and
   *               redirect flag when the deletion cannot be completed
   * @param cache  the response cache control settings
   * @return an empty model map on success, or {@code null} when an error
   *         status has been set on the response
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
      if (id != null) {
        NodeRef profileRef = new NodeRef(
          StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
          id
        );

        if (
          !this.currentUserPermissionCheckerService.isGroupAdmin(
            this.nodeService.getPrimaryParent(profileRef).getParentRef().getId()
          )
        ) {
          throw new AccessDeniedException(
            "Impossible to delete a profile, not enough permissions"
          );
        }
        this.recordBeforeDelete(id);
        this.profilesApi.profilesIdDelete(profileRef);
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when deleting profile with id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when deleting profile with id: " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when deleting profile with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
