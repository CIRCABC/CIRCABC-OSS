package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.KeywordJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesKeywordsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesKeywordsPost.class);

  private KeywordsApi keywordsApi;
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
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
            id,
            LibraryPermissions.LIBEDITONLY
          )
      ) {
        throw new AccessDeniedException(
          "Impossible to add the keyword of the document, not enough permissions"
        );
      }

      KeywordDefinition body = KeywordJsonParser.parseJsonFullKeyword(req);
      this.keywordsApi.nodesIdKeywordsPost(id, body);
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
   * @return the keywordsApi
   */
  public KeywordsApi getKeywordsApi() {
    return this.keywordsApi;
  }

  /**
   * @param keywordsApi the keywordsApi to set
   */
  public void setKeywordsApi(KeywordsApi keywordsApi) {
    this.keywordsApi = keywordsApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
