package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupLockApi;
import io.swagger.api.GroupLockApiImpl.AlreadyLockedException;
import io.swagger.api.GroupLockApiImpl.LockPersistenceException;
import io.swagger.api.GroupLockApiImpl.NodeNotFoundException;
import io.swagger.model.GroupLockRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript for POST /circabc/groups/{id}/lock — locks an Interest Group.
 */
public class GroupsLockPost extends CircabcDeclarativeWebScript {

  static final Log logger = LogFactory.getLog(GroupsLockPost.class);

  private static final String MESSAGE = "message";
  private static final String READ_ONLY = "readOnly";

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
      GroupLockRequest lockRequest = parseBody(req);
      this.groupLockApi.lockInterestGroup(id, lockRequest);
    } catch (IllegalAccessError e) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied: insufficient permissions");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for lock on IG: " + id, e);
      }
      return null;
    } catch (AlreadyLockedException e) {
      status.setCode(HttpServletResponse.SC_CONFLICT);
      status.setMessage("Interest group is already locked");
      status.setRedirect(true);
      if (logger.isWarnEnabled()) {
        logger.warn("IG already locked: " + id, e);
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
        logger.error("Persistence error for lock on IG: " + id, e);
      }
      return null;
    } catch (IOException | ParseException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Invalid request body");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Bad request body for lock on IG: " + id, e);
      }
      return null;
    }

    return model;
  }

  private GroupLockRequest parseBody(WebScriptRequest req)
    throws IOException, ParseException {
    GroupLockRequest lockRequest = new GroupLockRequest();

    String content = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(content);

    if (json.get(MESSAGE) != null) {
      lockRequest.setMessage(json.get(MESSAGE).toString());
    }

    if (json.get(READ_ONLY) != null) {
      lockRequest.setReadOnly(Boolean.valueOf(json.get(READ_ONLY).toString()));
    } else {
      lockRequest.setReadOnly(false);
    }

    return lockRequest;
  }

  public GroupLockApi getGroupLockApi() {
    return this.groupLockApi;
  }

  public void setGroupLockApi(GroupLockApi groupLockApi) {
    this.groupLockApi = groupLockApi;
  }
}
