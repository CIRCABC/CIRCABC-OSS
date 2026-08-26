package io.swagger.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaptchaApiImpl implements CaptchaApi {

  private static final Log logger = LogFactory.getLog(CaptchaApiImpl.class);
  private String captchaUrl;
  private String useProxy;
  private String proxyHost;
  private String proxyPort;
  private String proxyUserName;
  private String proxyPassword;

  private static final int CONNECTION_TIMEOUT_10_S = 10000;

  @Override
  public boolean validate(
    String captchaToken,
    String captchaId,
    String answer
  ) {
    boolean result = false;
    HttpClient httpClient = new HttpClient();
    httpClient
      .getParams()
      .setParameter("http.useragent", "CIRCABC EU Captcha client");
    if (useProxy.equals("true")) {
      httpClient
        .getHostConfiguration()
        .setProxy(proxyHost, Integer.valueOf(proxyPort));
      httpClient
        .getState()
        .setProxyCredentials(
          "pslux-proxy-realm",
          proxyHost,
          new UsernamePasswordCredentials(proxyUserName, proxyPassword)
        );
    }
    String url = this.captchaUrl;
    try {
      url =
        url +
        "/api/validateCaptcha/" +
        URLEncoder.encode(captchaId, StandardCharsets.UTF_8.toString()) +
        "?captchaAnswer=" +
        URLEncoder.encode(answer, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can not create url ", e);
      }
      return result;
    }
    PostMethod methodPost = new PostMethod(url);

    methodPost.setRequestHeader("Content-Type", "application/json");
    methodPost.setRequestHeader("x-jwtString", captchaToken);

    try {
      httpClient.getParams().setSoTimeout(CONNECTION_TIMEOUT_10_S);
      int returnCode = httpClient.executeMethod(methodPost);

      if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "The Post method is not implemented by this URI: " + url
          );
        }
      } else {
        String response = methodPost.getResponseBodyAsString();
        if (logger.isInfoEnabled()) {
          logger.info("Request:\n" + url);
          logger.info("Response:\n" + response);
        }
        if (!response.contains("success")) {
          if (logger.isErrorEnabled()) {
            logger.error("Call EU Captcha service :  " + url);
            logger.error("Response is not successful :" + response);
          }
        } else {
          result = true;
        }
      }
    } catch (HttpException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error Accessing EU CAPTCHA: " + url, e);
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error Accessing EU CAPTCHA: " + url, e);
      }
    } finally {
      methodPost.releaseConnection();
    }

    return result;
  }

  public String getCaptchaUrl() {
    return captchaUrl;
  }

  public void setCaptchaUrl(String captchaUrl) {
    this.captchaUrl = captchaUrl;
  }

  public String getUseProxy() {
    return useProxy;
  }

  public void setUseProxy(String useProxy) {
    this.useProxy = useProxy;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(String proxyPort) {
    this.proxyPort = proxyPort;
  }

  public String getProxyUserName() {
    return proxyUserName;
  }

  public void setProxyUserName(String proxyUserName) {
    this.proxyUserName = proxyUserName;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  private String readData(HttpURLConnection conn) throws IOException {
    BufferedReader in = new BufferedReader(
      new InputStreamReader(conn.getInputStream())
    );
    StringBuilder sb = new StringBuilder();
    for (int c; (c = in.read()) >= 0;) {
      sb.append((char) c);
    }
    in.close();
    return sb.toString();
  }
}
