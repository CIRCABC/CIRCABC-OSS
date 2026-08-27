package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.user.UserService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

public class LoginPost extends DeclarativeWebScript {

  private AuthenticationService authenticationService;

  private UserService userService;

  private TransactionService transactionService;

  public void setAuthenticationService(
    AuthenticationService authenticationService
  ) {
    this.authenticationService = authenticationService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  public Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    String username = "";
    String password = "";
    try {
      Content c = req.getContent();
      if (c == null) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Missing POST body."
        );
      }

      JSONObject json;

      json = new JSONObject(c.getContent());
      username = json.getString("username");
      password = json.getString("password");
      if (username == null || username.length() == 0) {
        throw new WebScriptException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Username not specified"
        );
      }

      if (password == null) {
        throw new WebScriptException(
          HttpServletResponse.SC_BAD_REQUEST,
          "Password not specified"
        );
      }
    } catch (JSONException jErr) {
      throw new WebScriptException(
        Status.STATUS_BAD_REQUEST,
        "Unable to parse JSON POST body: " + jErr.getMessage()
      );
    } catch (IOException ioErr) {
      throw new WebScriptException(
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Unable to retrieve POST body: " + ioErr.getMessage()
      );
    }

    Map<String, Object> model = login(username, password);

    if (model == null) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Login failed");
      status.setRedirect(true);
      return null;
    } else {
      return model;
    }
  }

  private Map<String, Object> login(String username, String password) {
    try {
      // get ticket
      authenticationService.authenticate(username, password.toCharArray());

      // add ticket to model for javascript and template access
      Map<String, Object> model = new HashMap<>(7, 1.0f);
      model.put("username", username);
      model.put("ticket", authenticationService.getCurrentTicket());

      return model;
    } catch (AuthenticationException e) {
      return null;
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }
}
