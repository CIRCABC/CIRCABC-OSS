package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.*;
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
 * REST webscript endpoint that returns a filtered, paginated list of members of
 * an Interest Group (IG).
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint
 * handles HTTP {@code GET} requests. The target IG is identified by the
 * {@code igId} URL template variable. Before returning any data, the caller must
 * hold {@link DirectoryPermissions#DIRACCESS} on that group; otherwise the
 * request is rejected with an HTTP 403 (Forbidden).
 *
 * <p>The result set can be filtered and shaped via the following request
 * parameters:
 * <ul>
 *   <li>{@code firstName}, {@code lastName}, {@code email} &ndash; member
 *       attribute filters.</li>
 *   <li>{@code profile} &ndash; restricts the results to a single membership
 *       profile.</li>
 *   <li>{@code language} &ndash; content locale used to render multilingual
 *       properties; when absent the webscript stays multilingual-aware.</li>
 *   <li>{@code limit} &ndash; page size (defaults to 25 when the parameter is
 *       present but empty).</li>
 *   <li>{@code page} &ndash; 1-based page index (defaults to 1 when the
 *       parameter is present but empty).</li>
 *   <li>{@code order} &ndash; sort order to apply.</li>
 * </ul>
 *
 * <p>The rendered model exposes the page of matching members under
 * {@code data} and the total number of matches under {@code total}.
 */
public class GroupsMembersFilterGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersFilterGet.class);

  /** API facade providing group membership operations. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's directory permissions on the IG. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request: resolves the target Interest Group from the
   * {@code igId} URL template variable, verifies the caller's directory access,
   * and retrieves the filtered, paginated page of group members.
   *
   * <p>The configured content locale and multilingual awareness are set
   * according to the {@code language} parameter and always restored in a
   * {@code finally} block before returning.
   *
   * @param req the web script request; supplies the {@code igId} template
   *     variable and the {@code firstName}, {@code lastName}, {@code email},
   *     {@code profile}, {@code language}, {@code limit}, {@code page} and
   *     {@code order} query parameters
   * @param status the response status, updated to 403 (Forbidden) on access
   *     denial or 400 (Bad Request) on an invalid node reference
   * @param cache the cache control object for the response
   * @return a model map containing the matching members under {@code data} and
   *     the total match count under {@code total}, or {@code null} when the
   *     request is rejected and a redirect status has been set
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
    String firstName = req.getParameter("firstName");
    String lastName = req.getParameter("lastName");
    String email = req.getParameter("email");

    List<String> searchProfile = null;
    if (req.getParameter("profile") != null) {
      searchProfile = new ArrayList<>();
      searchProfile.add(req.getParameter("profile"));
    }

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    String limitStr = req.getParameter("limit");
    Integer limit = null;
    if (limitStr != null) {
      limit = (limitStr.isEmpty() ? 25 : Integer.parseInt(limitStr));
    }

    String pageStr = req.getParameter("page");
    Integer page = null;
    if (pageStr != null) {
      page = (pageStr.isEmpty() ? 1 : Integer.parseInt(pageStr));
    }

    String order = req.getParameter("order");

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
          "Not enough rights for get the list of users"
        );
      }

      PagedUserProfile result = this.groupsApi.groupsIdMembersGet(
        id,
        searchProfile,
        language,
        limit,
        page,
        order,
        firstName,
        lastName,
        email
      );
      model.put("data", result.getData());
      model.put("total", result.getTotal());
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
