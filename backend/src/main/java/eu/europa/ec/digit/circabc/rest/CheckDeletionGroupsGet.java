package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.LibraryApi;
import io.swagger.model.GroupDeletionReport;
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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Read-only Alfresco webscript endpoint that produces a deletion impact report
 * for an interest group before it is actually removed.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so
 * this handler backs an HTTP {@code GET} request. Given the identifier of a
 * group node, it verifies that the current user is allowed to prepare the
 * group's deletion (either as a category administrator of the parent category
 * or as the directory administrator of the interest group) and then gathers the
 * items that would be affected by the deletion.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} — URL template variable identifying the group node whose
 *       deletion is being checked.</li>
 *   <li>{@code language} — optional request parameter selecting the locale used
 *       for multilingual content; when absent the endpoint operates in
 *       ML-aware mode.</li>
 * </ul>
 *
 * <p>On success the model exposes a {@link io.swagger.model.GroupDeletionReport}
 * under the {@code report} key, listing locked nodes, shared folders and shared
 * profiles associated with the group. Permission failures are reported as
 * {@code 403 Forbidden} and invalid node references as {@code 400 Bad
 * Request}.</p>
 */
public class CheckDeletionGroupsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CheckDeletionGroupsGet.class);

  /** Alfresco node service used to resolve the group's parent category. */
  @Autowired
  private NodeService nodeService;

  /** Library API used to collect the locked nodes, shared folders and shared profiles of the group. */
  @Autowired
  private LibraryApi libraryApi;

  /** Service used to check whether the current user has the required administrative permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds the deletion impact report for the requested interest group.
   *
   * <p>Resolves the group node from the {@code id} template variable, configures
   * the multilingual context based on the optional {@code language} parameter,
   * verifies that the current user is a category administrator or interest group
   * directory administrator, and populates the model with a
   * {@link io.swagger.model.GroupDeletionReport} describing the locked nodes,
   * shared folders and shared profiles that the deletion would affect.</p>
   *
   * @param req the web script request; supplies the {@code id} template variable
   *            and the optional {@code language} parameter
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 400} when the node reference is invalid
   * @param cache the cache directives for the response
   * @return a model map containing the {@code report} entry on success, or
   *         {@code null} when the request is rejected (permission or bad request)
   * @throws org.alfresco.repo.security.permissions.AccessDeniedException never
   *         propagated: caught internally and translated into a {@code 403}
   *         response
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

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      NodeRef groupRef = Converter.createNodeRefFromId(id);
      NodeRef categoryRef = nodeService
        .getPrimaryParent(groupRef)
        .getParentRef();
      String categoryId = categoryRef.getId();
      if (
        !this.currentUserPermissionCheckerService.isCategoryAdmin(categoryId) &&
        !this.currentUserPermissionCheckerService.isInterestGroupDirAdmin(
          groupRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Not enough permission to prepare the deletion of the group"
        );
      }

      GroupDeletionReport report = new GroupDeletionReport();
      report.setLockedNodes(libraryApi.getLockedNodes(id));
      report.setSharedFolders(libraryApi.getSharedNodes(id));
      report.setSharedProfiles(libraryApi.getSharedProfiles(id));

      model.put("report", report);
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
