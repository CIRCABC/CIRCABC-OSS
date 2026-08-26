package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.GroupLockGuard;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript entry to get the level immediate below of subspaces of the given space id
 *
 * @author schwerr
 */
public class SubspacesIdGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SubspacesIdGet.class);

  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private GroupLockGuard groupLockGuard;

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

    if ((id == null) || id.isEmpty()) {
      throw new IllegalArgumentException(
        "The space 'id' is a mandatory parameter."
      );
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    String sort = req.getParameter("sort");
    String order = req.getParameter("order");
    String skipExpiredParam = req.getParameter("skipExpiredItems");
    boolean skipExpiredItems = (skipExpiredParam == null)
      ? false
      : Boolean.parseBoolean(skipExpiredParam);

    try {
      this.groupLockGuard.checkAccess(id);

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBACCESS
          )
      ) {
        throw new AccessDeniedException(
          "Cannot list the children of this space, not enough permissions"
        );
      }

      if (language == null) {
        MLPropertyInterceptor.setMLAware(true);
      } else {
        Locale locale = new Locale(language);
        I18NUtil.setContentLocale(locale);
        I18NUtil.setLocale(locale);
        MLPropertyInterceptor.setMLAware(false);
      }

      PagedNodes nodes =
        this.spacesApi.spaceGetChildren(
            id,
            0,
            -1,
            sort + "_" + order,
            true,
            false,
            skipExpiredItems
          );

      List<Node> spaces = new ArrayList<>();

      // fish only the spaces
      for (Node node : nodes.getData()) {
        if (ContentModel.TYPE_FOLDER.toString().equals(node.getType())) {
          spaces.add(node);
        }
      }

      model.put("data", spaces);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied", ade);
      }
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * @param spacesApi the spacesApi to set
   */
  public void setSpacesApi(SpacesApi spacesApi) {
    this.spacesApi = spacesApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public void setGroupLockGuard(GroupLockGuard groupLockGuard) {
    this.groupLockGuard = groupLockGuard;
  }
}
