package eu.cec.digit.circabc.repo.web.scripts.bean;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import io.swagger.api.PingInfoApi;
import io.swagger.model.PingInfo;

public class PingGet extends DeclarativeWebScript {

  PingInfoApi pingInfoApi;

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(PingGet.class);

  @Override
  protected Map<String, Object> executeImpl(
      WebScriptRequest req,
      Status status,
      Cache cache) {

    Map<String, Object> model = new HashMap<>(7, 1.0f);
   
    try {
      PingInfo info = pingInfoApi.getPingInfo();
      model.put("info", info);
    } catch (UnknownHostException e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Unable to get local hist address");
      status.setRedirect(true);
      return null;
    } 
    return model;
  }

  public void setPingInfoApi(PingInfoApi pingInfoApi) {
    this.pingInfoApi = pingInfoApi;
  }

}
