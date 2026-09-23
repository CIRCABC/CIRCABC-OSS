package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.CategoriesApi;
import io.swagger.api.GroupsApi;
import io.swagger.api.HeadersApi;
import io.swagger.model.Category;
import io.swagger.model.GroupPath;
import io.swagger.model.Header;
import io.swagger.model.InterestGroup;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST endpoint that resolves the full containment path of an Interest Group.
 *
 * <p>This Alfresco Declarative Web Script backs an HTTP {@code GET} request (as
 * implied by the {@code Get} suffix of the class name) and, given an Interest
 * Group node identifier, returns the group together with the Category and Header
 * it belongs to. The three elements are combined into a single {@link GroupPath}
 * model object, which is exposed to the FreeMarker template under the
 * {@code "groupPath"} model key.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} &mdash; the Interest Group node id, supplied as a URL template
 *       variable.</li>
 *   <li>{@code language} &mdash; optional request parameter. When provided, the
 *       content locale is set and multilingual (ML) awareness is disabled so that
 *       values are returned in the requested language; when omitted, ML awareness
 *       is enabled.</li>
 * </ul>
 *
 * <p>Access is guarded by an Alfresco read-permission check on the group node.
 * A lack of permission results in an HTTP {@code 403 Forbidden}, while an invalid
 * node reference results in an HTTP {@code 400 Bad Request}.</p>
 */
public class GroupPathGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupPathGet.class);

  /** API used to load Interest Group details. */
  @Autowired
  private GroupsApi groupsApi;

  /** API used to resolve the Header that owns a given Category. */
  @Autowired
  @Qualifier("HeaderApi") // NOSONAR
  private HeadersApi headersApi;

  /** API used to load Category details. */
  @Autowired
  private CategoriesApi categoriesApi;

  /** Alfresco node service used to navigate the repository parent hierarchy. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user has read access to the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds the group path model for the requested Interest Group.
   *
   * <p>Reads the {@code id} template variable and the optional {@code language}
   * request parameter, configures ML awareness / content locale accordingly,
   * verifies the caller's read permission on the group, and then assembles a
   * {@link GroupPath} containing the group, its parent Category and the owning
   * Header. When no {@code id} is supplied, an empty model is returned. The
   * previous ML-awareness state is always restored before returning.</p>
   *
   * @param req the web script request; provides the {@code id} template variable
   *            and the optional {@code language} parameter
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} on an invalid node reference
   * @param cache the web script response cache directives
   * @return a model map containing the {@code "groupPath"} entry, an empty map
   *         when no {@code id} is provided, or {@code null} when an error status
   *         (forbidden or bad request) has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("id");

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
      if (groupId != null) {
        if (
          !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            groupId
          )
        ) {
          throw new AccessDeniedException(
            "Current Authority cannot access group, not enough permission"
          );
        }

        GroupPath result = new GroupPath();

        NodeRef groupRef = Converter.createNodeRefFromId(groupId);
        InterestGroup group = this.groupsApi.getInterestGroupDetails(
          groupRef,
          false
        );
        result.setGroup(group);

        NodeRef categoryRef = nodeService
          .getPrimaryParent(groupRef)
          .getParentRef();
        Category category = categoriesApi.categoriesIdGet(categoryRef.getId());
        result.setCategory(category);

        Header header = headersApi.getHeaderByCategory(categoryRef.getId());
        result.setHeader(header);

        model.put("groupPath", result);
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
