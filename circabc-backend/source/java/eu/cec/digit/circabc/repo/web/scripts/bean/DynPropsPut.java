package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.DynamicPropertyDefinitionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DynPropsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynPropsPut.class);

  private DynamicPropertiesApi dynamicPropertiesApi;
  private NodeService nodeService;

  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      NodeRef dpRef = Converter.createNodeRefFromId(id);
      NodeRef dpFolderRef =
        this.nodeService.getPrimaryParent(dpRef).getParentRef();
      NodeRef igRef =
        this.nodeService.getPrimaryParent(dpFolderRef).getParentRef();

      if (
        !this.currentUserPermissionCheckerService.isGroupAdmin(igRef.getId())
      ) {
        throw new AccessDeniedException(
          "Cannot delete dynamic property, user is not IG leader"
        );
      }

      DynamicPropertyDefinition ddd =
        DynamicPropertyDefinitionJsonParser.parseJsonDynamicPropertyDefinition(
          req
        );
      model.put("dp", this.dynamicPropertiesApi.dynpropsIdPut(id, ddd));
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    }

    return model;
  }

  /**
   * @return the dynamicPropertiesApi
   */
  public DynamicPropertiesApi getDynamicPropertiesApi() {
    return this.dynamicPropertiesApi;
  }

  /**
   * @param dynamicPropertiesApi the dynamicPropertiesApi to set
   */
  public void setDynamicPropertiesApi(
    DynamicPropertiesApi dynamicPropertiesApi
  ) {
    this.dynamicPropertiesApi = dynamicPropertiesApi;
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
}
