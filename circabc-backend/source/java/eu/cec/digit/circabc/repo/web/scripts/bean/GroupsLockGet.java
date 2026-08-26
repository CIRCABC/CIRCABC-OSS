package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupLockApi;
import io.swagger.api.GroupLockApiImpl.NodeNotFoundException;
import io.swagger.model.GroupLockInfo;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript for GET /circabc/groups/{id}/lock — returns the lock state of an Interest Group.
 */
public class GroupsLockGet extends CircabcDeclarativeWebScript {

  static final Log logger = LogFactory.getLog(GroupsLockGet.class);

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
      GroupLockInfo lockInfo = this.groupLockApi.getGroupLockInfo(id);

      if (lockInfo == null) {
        status.setCode(HttpServletResponse.SC_NOT_FOUND);
        status.setMessage("Interest group not found");
        status.setRedirect(true);
        return null;
      }

      model.put("lockInfo", lockInfo);
    } catch (NodeNotFoundException e) {
      status.setCode(HttpServletResponse.SC_NOT_FOUND);
      status.setMessage("Interest group not found");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("IG not found: " + id, e);
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
