package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupsApi;
import io.swagger.model.ApplicantAction;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.ApplicantActionJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsApplicantsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsApplicantsPost.class);

  private GroupsApi groupsApi;
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
    String id = templateVars.get("igId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(id)
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot post his membership application, not enough permission"
        );
      }
      ApplicantAction body = ApplicantActionJsonParser.parseJSON(req);
      this.groupsApi.groupsIdMembersApplicantsPost(id, body);
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * @return the groupsApi
   */
  public GroupsApi getGroupsApi() {
    return this.groupsApi;
  }

  /**
   * @param groupsApi the groupsApi to set
   */
  public void setGroupsApi(GroupsApi groupsApi) {
    this.groupsApi = groupsApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
