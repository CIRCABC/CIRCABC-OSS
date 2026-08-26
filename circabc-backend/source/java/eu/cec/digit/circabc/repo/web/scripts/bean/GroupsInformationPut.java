package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import io.swagger.api.GroupLockApi;
import io.swagger.api.InformationApi;
import io.swagger.model.InformationPage;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InformationJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsInformationPut extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsInformationPut.class);

  private InformationApi informationApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private GroupLockApi groupLockApi;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    try {
      if (groupLockApi != null && !groupLockApi.canWriteOrAdmin(id)) {
        throw new AccessDeniedException("Interest group is in read-only mode");
      }
      NodeRef infRef =
        this.nodeService.getChildByName(
            Converter.createNodeRefFromId(id),
            ContentModel.ASSOC_CONTAINS,
            "Information"
          );
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
            infRef.getId(),
            InformationPermissions.INFADMIN
          )
      ) {
        throw new AccessDeniedException(
          "Not enought permissions on the News node"
        );
      }

      InformationPage body = InformationJsonParser.parse(req);
      this.informationApi.groupsIdInformationPut(id, body);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied", ade);
      }
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request - InvalidNodeRef");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Bad request - InvalidNodeRef", inre);
      }
      return null;
    } catch (IOException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request - bad URL");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Bad request - bad URL", e);
      }
      return null;
    } catch (ParseException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request - parse error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Bad request - parse error", e);
      }
      return null;
    }

    return model;
  }

  /**
   * @return the informationApi
   */
  public InformationApi getInformationApi() {
    return this.informationApi;
  }

  /**
   * @param informationApi the informationApi to set
   */
  public void setInformationApi(InformationApi informationApi) {
    this.informationApi = informationApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public NodeService getNodeService() {
    return this.nodeService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public GroupLockApi getGroupLockApi() {
    return this.groupLockApi;
  }

  public void setGroupLockApi(GroupLockApi groupLockApi) {
    this.groupLockApi = groupLockApi;
  }
}
