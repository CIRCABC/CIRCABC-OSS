package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.EmailUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class LdapUserGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UsersGet.class);

  private LdapUserService ldapUserService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String query = req.getParameter("query");

    try {
      if (query == null || query.isEmpty()) {
        throw new InvalidArgumentException("Empty Query");
      } else if (currentUserPermissionCheckerService.isGuest()) {
        throw new AccessDeniedException("Method not allowed for guest");
      }

      // check if the query in an email or a userId

      List<String> userIds = null;
      //if we have a valid email address => get the userId of the user
      if (EmailUtil.isValidEmailAddress(query)) {
        userIds = ldapUserService.getLDAPUserIDByMail(query);
      }

      CircabcUserDataBean user;
      if (userIds != null && userIds.size() > 0) {
        user = ldapUserService.getLDAPUserDataNoFilterByUid(userIds.get(0));
      } else {
        user = ldapUserService.getLDAPUserDataNoFilterByUid(query);
      }
      if (user == null) {
        throw new NotFoundException(
          "No user found for the given search parameter!"
        );
      }
      model.put("user", user);
    } catch (InvalidArgumentException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage(inre.getMessage());
      status.setRedirect(true);
      return null;
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (NotFoundException ne) {
      status.setCode(HttpServletResponse.SC_NO_CONTENT);
      status.setMessage(ne.getMessage());
      status.setRedirect(true);
      return null;
    }

    return model;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public void setLdapUserService(LdapUserService ldapUserService) {
    this.ldapUserService = ldapUserService;
  }
}
