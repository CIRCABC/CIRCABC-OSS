package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.NodeUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodeGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodeGet.class);

  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

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
      Locale locale = new Locale(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Not enough permission to delete the news"
        );
      }

      Node n = this.nodesApi.getNodeById(id);
      if (NodeUtil.isContent(n) && NodeUtil.isExpired(n)) {
        //If the node is a file and if it is expired we display it only if the current user has group admin rights:
        if (!currentUserPermissionCheckerService.isGroupAdmin(id)) {
          throw new AccessDeniedException(
            "User is not Group admin and cannot expired documents"
          );
        }
      }
      model.put("n", n);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException inre) {
      // BUG DIGITCIRCABC-4899
      String detailMessage = inre.getMessage();
      if (
        detailMessage != null && detailMessage.startsWith("Node does not exist")
      ) {
        status.setCode(HttpServletResponse.SC_NOT_FOUND);
        status.setMessage("Node with given ID does not exist!");
      } else {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage("Bad request");
      }
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * @return the nodesApi
   */
  public NodesApi getNodesApi() {
    return this.nodesApi;
  }

  /**
   * @param nodesApi the nodesApi to set
   */
  public void setNodesApi(NodesApi nodesApi) {
    this.nodesApi = nodesApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
