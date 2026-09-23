package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script that handles the HTTP {@code GET} request for
 * retrieving a single Interest Group.
 *
 * <p>The endpoint resolves the group identified by the {@code id} template
 * variable taken from the request URL and returns its details. Before the group
 * is loaded, the current user's Alfresco read permission on the group node is
 * verified; if the user lacks permission an {@code HTTP 403 (Forbidden)}
 * response is produced, and an invalid node reference results in an
 * {@code HTTP 400 (Bad Request)} response.</p>
 *
 * <p>An optional {@code language} request parameter controls the locale used to
 * resolve multilingual content. When it is absent the web script runs in
 * multilingual-aware mode (returning all language variants); when it is present
 * the content and UI locale are set accordingly and multilingual-aware mode is
 * disabled so that values are resolved for that single language.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &ndash; template variable identifying the Interest Group.</li>
 *   <li>{@code language} &ndash; optional request parameter selecting the locale.</li>
 * </ul>
 */
public class GroupGet extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupGet.class);

  /** API providing access to Interest Group business operations. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's permissions on a node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Retrieves the Interest Group identified by the {@code id} template variable
   * and populates the response model.
   *
   * <p>Reads the optional {@code language} request parameter to configure the
   * multilingual behavior, checks that the current user has Alfresco read
   * permission on the group node, then loads the group. The previous
   * multilingual-aware state is always restored before returning.</p>
   *
   * @param req the web script request; supplies the {@code id} template
   *     variable and the optional {@code language} parameter
   * @param status the response status, updated to {@code 403} when access is
   *     denied or {@code 400} when the node reference is invalid
   * @param cache the cache directives for the response
   * @return a model map containing the group {@code id} and the group object
   *     under key {@code g}; {@code null} when an error status (forbidden or
   *     bad request) has been set
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
        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            groupIp
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot access group, not enough permission"
          );
        }

        model.put("id", groupIp);
        model.put("g", this.groupsApi.getInterestGroup(groupIp));
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
