package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.EventsApi;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript that handles the HTTP {@code POST} request used to create a new
 * Event/Meeting (appointment) within the "Events" service of an Interest Group.
 *
 * <p>The target Interest Group is identified by the {@code igId} URL template variable. The
 * endpoint resolves the group node, locates its child "Events" container and verifies that the
 * current user holds the {@link EventPermissions#EVEADMIN} permission on that container before
 * delegating the actual creation to {@link EventsApi#groupsIdEventsPost(String, String)}. The
 * appointment payload is read verbatim from the request body.
 *
 * <p>On success an empty model map is returned. If the user lacks the required permission the
 * response status is set to {@link Status#STATUS_FORBIDDEN}; any other failure results in a
 * {@link Status#STATUS_NOT_ACCEPTABLE} status.
 *
 * @author schwerr
 */
public class GroupsIdEventsPost extends CircabcDeclarativeWebScript {

  /** API providing the business logic used to create the event/appointment. */
  @Autowired
  private EventsApi eventsApi;

  /** Alfresco service used to navigate the repository and locate the "Events" container node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user holds the required event permission. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new event/appointment inside the "Events" service of the Interest Group identified by
   * the {@code igId} URL template variable.
   *
   * <p>The request body is treated as the raw appointment payload. Before delegating to the
   * {@link EventsApi}, the method checks that the current user has {@link EventPermissions#EVEADMIN}
   * permission on the group's "Events" container.
   *
   * @param req the webscript request; supplies the {@code igId} template variable and the
   *     appointment payload in its content body
   * @param status the response status, set to {@link Status#STATUS_FORBIDDEN} when the user lacks
   *     the required permission or {@link Status#STATUS_NOT_ACCEPTABLE} on any other error
   * @param cache the webscript cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error occurs and the status has
   *     been set to indicate the failure
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String igId = templateVars.get("igId");

    try {
      NodeRef groupRef = Converter.createNodeRefFromId(igId);
      NodeRef evtNodeRef = this.nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      );

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
          evtNodeRef.getId(),
          EventPermissions.EVEADMIN
        )
      ) {
        throw new AccessDeniedException(
          "You cannot create an appointment here, you don't have enough permission. " +
            "You need to have EveAdmin permission."
        );
      }

      String appointmentBody = req.getContent().getContent();

      this.eventsApi.groupsIdEventsPost(igId, appointmentBody);
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage(ade.getMessage());
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
