package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ProfilesApi;
import io.swagger.model.Profile;
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
import org.alfresco.service.cmr.repository.NodeService;
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
 * Alfresco Declarative Web Script backing the HTTP {@code PUT} endpoint used to
 * update an existing CIRCABC profile.
 *
 * <p>The endpoint identifies the target profile from the {@code id} template
 * variable (interpreted as a node identifier in the workspace SpacesStore) and
 * applies a partial update parsed from the JSON request body. Before the update
 * is performed the current user must be a group administrator of the interest
 * group that owns the profile; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden} status.
 *
 * <p>An optional {@code language} request parameter controls localisation
 * handling: when it is absent the node service is left multilingual-aware so
 * that multilingual property values are returned; when it is supplied the
 * corresponding {@link java.util.Locale} is set as the content and UI locale and
 * multilingual awareness is disabled for the duration of the request. The
 * previous multilingual-awareness state is always restored afterwards.
 *
 * <p>On success the updated profile is placed in the returned model under the
 * key {@code profile} for rendering by the associated FreeMarker template.
 * Validation, node reference and parsing problems result in an HTTP
 * {@code 400 Bad Request}, while unexpected failures produce an HTTP
 * {@code 500 Internal Server Error}.
 */
public class ProfilesPut extends CircabcDeclarativeWebScript {

  /** Reusable status message returned for malformed or invalid requests. */
  private static final String BAD_REQUEST = "Bad request";

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ProfilesPut.class);

  /** API providing the profile business operations, including the update logic. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Service used to verify that the current user has group-admin permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to resolve the profile's owning interest group. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the {@code PUT} request that updates an existing profile.
   *
   * <p>Resolves the profile node from the {@code id} template variable, applies
   * the optional {@code language} localisation settings, checks that the current
   * user is a group administrator of the profile's owning interest group, and
   * then performs a partial update using the JSON payload of the request. The
   * multilingual-awareness state of the node service is always restored before
   * returning.
   *
   * @param req the incoming web script request; supplies the {@code id} template
   *     variable, the optional {@code language} parameter and the JSON body
   * @param status the response status object, updated to the appropriate HTTP
   *     code (e.g. {@code 403}, {@code 400} or {@code 500}) when an error occurs
   * @param cache the response cache control object (not modified by this handler)
   * @return a model map containing the updated profile under the {@code profile}
   *     key on success, or {@code null} when an error status has been set
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
            "Impossible to edit a profile, not enough permissions"
          );
        }

        Profile body = ProfileJsonParser.parsePartial(req);
        model.put("profile", this.profilesApi.profilesIdPut(profileRef, body));
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when updating profile with id: " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when updating profile with id: " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(BAD_REQUEST);
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (ParseException pe) {
      logger.error("Parse exception when updating profile with id: " + id, pe);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(BAD_REQUEST);
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException ioe) {
      logger.error("IO exception when updating profile with id: " + id, ioe);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage(BAD_REQUEST);
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when updating profile with id: " + id, e);
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
