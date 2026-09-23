package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.InterestGroup;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script backing the HTTP {@code PUT} endpoint that updates the
 * metadata of an existing Interest Group.
 *
 * <p>The target group is identified by the {@code id} path variable (the group's node
 * reference / identifier). The request body carries a partial JSON representation of an
 * {@link io.swagger.model.InterestGroup}; only the supplied fields are applied.</p>
 *
 * <p>Behavior summary:</p>
 * <ul>
 *   <li>Resolves the {@code id} path variable and the optional {@code language} query
 *       parameter. When {@code language} is provided, the request runs with multilingual
 *       (ML) awareness disabled and the given locale set as the content/UI locale; otherwise
 *       ML awareness is enabled so multilingual property values are handled transparently.</li>
 *   <li>Verifies that the current authority holds Alfresco write permission on the group,
 *       raising an {@link org.alfresco.repo.security.permissions.AccessDeniedException}
 *       otherwise.</li>
 *   <li>Parses the partial JSON body and delegates the update to
 *       {@link io.swagger.api.GroupsApi#groupsIdPut(String, io.swagger.model.InterestGroup)}.</li>
 * </ul>
 *
 * <p>On success the model contains {@code message = "ok"}. Errors are translated to the
 * appropriate HTTP status (403 for access denial, 400 for invalid input, 500 otherwise)
 * and result in a redirect response.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see io.swagger.api.GroupsApi
 */
public class GroupPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupPut.class);

  /** API providing the Interest Group business operations, including the update logic. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to check the current user's Alfresco permissions on the target group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code PUT} request that updates an Interest Group's metadata.
   *
   * <p>Reads the {@code id} path variable to identify the group and the optional
   * {@code language} query parameter to control multilingual handling. After verifying the
   * caller's write permission, it parses the partial JSON body and applies the update via
   * {@link io.swagger.api.GroupsApi#groupsIdPut(String, io.swagger.model.InterestGroup)}.
   * The previous ML-awareness setting is always restored in a {@code finally} block.</p>
   *
   * @param req    the incoming web script request; supplies the {@code id} path variable,
   *               the optional {@code language} parameter and the JSON request body
   * @param status the response status to populate; set to 403, 400 or 500 on failure
   * @param cache  the response cache directives (unused by this endpoint)
   * @return a model map containing {@code message = "ok"} on success, or {@code null} when an
   *         error occurs and a redirect status has been set
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
          !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(
            groupIp
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot edit group metadata, not enough permission"
          );
        }

        InterestGroup body = InterestGroupJsonParser.parsePartialJSON(req);
        this.groupsApi.groupsIdPut(groupIp, body);
        model.put("message", "ok");
      }
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
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
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
