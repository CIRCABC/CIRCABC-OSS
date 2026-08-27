package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupLockApi;
import io.swagger.api.GroupLockApiImpl.LockPersistenceException;
import io.swagger.api.GroupLockApiImpl.NodeNotFoundException;
import io.swagger.api.GroupLockApiImpl.NotLockedException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript for DELETE /circabc/groups/{id}/lock — unlocks an Interest Group.
 */
public class GroupsLockDelete extends CircabcDeclarativeWebScript {

  static final Log logger = LogFactory.getLog(GroupsLockDelete.class);

  private GroupLockApi groupLockApi;

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
      this.groupLockApi.unlockInterestGroup(id);
    } catch (IllegalAccessError e) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied: insufficient permissions");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for unlock on IG: " + id, e);
      }
      return null;
    } catch (NotLockedException e) {
      status.setCode(HttpServletResponse.SC_CONFLICT);
      status.setMessage("Interest group is not locked");
      status.setRedirect(true);
      if (logger.isWarnEnabled()) {
        logger.warn("IG not locked: " + id, e);
      }
      return null;
    } catch (NodeNotFoundException e) {
      status.setCode(HttpServletResponse.SC_NOT_FOUND);
      status.setMessage("Interest group not found");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("IG not found: " + id, e);
      }
      return null;
    } catch (LockPersistenceException e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal error while processing lock operation");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Persistence error for unlock on IG: " + id, e);
      }
      return null;
    }

    return model;
  }

  public GroupLockApi getGroupLockApi() {
    return this.groupLockApi;
  }

  public void setGroupLockApi(GroupLockApi groupLockApi) {
    this.groupLockApi = groupLockApi;
  }
}
