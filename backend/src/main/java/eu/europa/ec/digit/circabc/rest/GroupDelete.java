package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a CIRCABC group (Interest Group).
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention:
 * {@code Group} + {@code Delete} maps this endpoint to the HTTP {@code DELETE}
 * method on a group resource. The group to remove is identified by the
 * {@code id} URL template variable, which is resolved to an Alfresco
 * {@link NodeRef}.</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Resolves the target group node from the {@code id} template variable.</li>
 *   <li>Determines the parent category of the group and verifies that the
 *       current user is an administrator of that category; otherwise an
 *       {@link AccessDeniedException} is raised and the response is set to
 *       {@code 403 Forbidden}.</li>
 *   <li>Delegates the actual deletion to {@link GroupsApi#groupsIdDelete}.</li>
 *   <li>Honors an optional {@code language} request parameter to control the
 *       multilingual (ML) property handling and content/UI locale for the
 *       duration of the call.</li>
 * </ul>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>URL template variable {@code id} — the identifier of the group node.</li>
 *   <li>Request parameter {@code language} — optional locale code; when absent
 *       the interceptor is left ML-aware.</li>
 * </ul>
 */
public class GroupDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupDelete.class);

  /** API façade providing group operations, including deletion. */
  @Autowired
  private GroupsApi groupsApi;

  /** Alfresco node service used to navigate the group's parent hierarchy. */
  @Autowired
  private NodeService nodeService;

  /**
   * Service used to check whether the current user holds the required
   * category-administrator permission before allowing the deletion.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the group deletion request.
   *
   * <p>Reads the {@code id} template variable to locate the group, applies the
   * optional {@code language} parameter to the multilingual/locale context,
   * enforces that the current user is an administrator of the group's parent
   * category, and then deletes the group via {@link GroupsApi#groupsIdDelete}.
   * The original ML-aware state is always restored in a {@code finally} block.</p>
   *
   * @param req    the web script request; provides the {@code id} template
   *               variable and the optional {@code language} parameter
   * @param status the response status, set to {@code 403 Forbidden} on
   *               {@link AccessDeniedException} and {@code 400 Bad Request} on
   *               {@link InvalidNodeRefException}
   * @param cache  the response cache directives (unused)
   * @return an empty model map on success, or {@code null} when an error status
   *         and redirect have been set on the response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupIp = templateVars.get("id");

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
      if (groupIp != null) {
        NodeRef igRef = Converter.createNodeRefFromId(groupIp);
        NodeRef category = nodeService.getPrimaryParent(igRef).getParentRef();

        if (
          !this.currentUserPermissionCheckerService.isCategoryAdmin(
            category.getId()
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot delete the group , not enough permission"
          );
        }

        this.groupsApi.groupsIdDelete(groupIp, true, true);
      }
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
