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
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * lists the members of an Interest Group's directory.
 *
 * <p>Given the Interest Group identifier supplied as the {@code igId} URL
 * template variable, this endpoint returns a paged, optionally filtered and
 * sorted list of user profiles that belong to the group. The caller must hold
 * at least {@link DirectoryPermissions#DIRACCESS} on the group; otherwise the
 * request is rejected with HTTP 403 (Forbidden). An invalid group reference
 * results in HTTP 400 (Bad Request).</p>
 *
 * <p>The following optional request parameters are supported:</p>
 * <ul>
 *   <li>{@code language} - content locale used to render multilingual values;
 *       when omitted the response is returned in a multilingual-aware form.</li>
 *   <li>{@code searchQuery} - free-text filter applied to the members.</li>
 *   <li>{@code profile} - restricts the result to members holding the given
 *       profile.</li>
 *   <li>{@code limit} - page size (defaults to 25 when present but empty).</li>
 *   <li>{@code page} - 1-based page number (defaults to 1 when present but
 *       empty).</li>
 *   <li>{@code order} - sort order for the returned members.</li>
 * </ul>
 *
 * <p>The response model exposes the {@code data} (the list of user profiles)
 * and {@code total} (the overall member count) attributes, which are rendered
 * by the associated FreeMarker template.</p>
 *
 * @see io.swagger.api.GroupsApi#groupsIdMembersGet
 */
public class GroupsMembersGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsMembersGet.class);

  /**
   * API used to retrieve the paged list of members for a given Interest Group.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user holds the directory
   * permission required to read the group's member list.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request, resolving the members of the Interest
   * Group identified by the {@code igId} URL template variable.
   *
   * <p>Optional request parameters ({@code language}, {@code searchQuery},
   * {@code profile}, {@code limit}, {@code page} and {@code order}) are read to
   * filter, paginate and sort the result. Access is only granted when the
   * current user has {@link DirectoryPermissions#DIRACCESS} on the group; the
   * multilingual awareness flag is toggled according to the requested language
   * and always restored before returning.</p>
   *
   * @param req the web script request, providing the {@code igId} template
   *            variable and the optional query parameters
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} on an invalid node reference
   * @param cache the cache directives for the response
   * @return a model map holding the {@code data} (list of member profiles) and
   *         {@code total} (member count) attributes, or {@code null} when the
   *         request fails and a status redirect is issued
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
        searchQuery
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
