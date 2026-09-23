package io.swagger.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default {@link CaptchaApi} implementation backed by the European Commission
 * EU CAPTCHA validation service.
 *
 * <p>This bean delegates the actual verification of a CAPTCHA answer to a remote
 * EU CAPTCHA REST endpoint. It issues an HTTP {@code POST} to
 * {@code <captchaUrl>/api/validateCaptcha/<captchaId>?captchaAnswer=<answer>},
 * forwarding the challenge token via the {@code x-jwtString} request header, and
 * interprets the remote response to decide whether the answer is valid.
 *
 * <p>Connection settings (the base service URL and optional outbound proxy) are
 * injected via setters, typically from Spring XML configuration. When proxy use
 * is enabled the client authenticates against the configured proxy using the
 * supplied credentials.
 */
public class CaptchaApiImpl implements CaptchaApi {

  /** Logger for this implementation. */
  private static final Log logger = LogFactory.getLog(CaptchaApiImpl.class);

  /** Base URL of the remote EU CAPTCHA validation service. */
  private String captchaUrl;

  /**
   * Whether outbound requests should go through a proxy; the string
   * {@code "true"} enables proxy usage, any other value disables it.
   */
  private String useProxy;

  /** Host name of the outbound proxy (used only when {@link #useProxy} is enabled). */
  private String proxyHost;

  /** Port of the outbound proxy (used only when {@link #useProxy} is enabled). */
  private String proxyPort;

  /** User name used to authenticate against the outbound proxy. */
  private String proxyUserName;

  /** Password used to authenticate against the outbound proxy. */
  private String proxyPassword;

  /** Socket timeout, in milliseconds, applied to the remote CAPTCHA call. */
  private static final int CONNECTION_TIMEOUT_10_S = 10000;

  /**
   * Validates a CAPTCHA answer by calling the remote EU CAPTCHA service.
   *
   * <p>Builds the validation URL from the configured base URL, the challenge
   * identifier and the URL-encoded answer, sends an HTTP {@code POST} with the
   * challenge token in the {@code x-jwtString} header, and treats the call as
   * successful only when the response body indicates success. Any encoding,
   * transport or unexpected-status error is logged and results in a
   * {@code false} return value rather than a thrown exception.
   *
   * @param captchaToken the token associated with the issued CAPTCHA challenge,
   *     sent to the service in the {@code x-jwtString} header
   * @param captchaId the identifier of the CAPTCHA challenge to validate against
   * @param answer the answer provided by the client for the challenge
   * @return {@code true} if the remote service confirms the answer is correct,
   *     {@code false} otherwise or if the service could not be reached
   */
  @SuppressWarnings("deprecation")
  @Override
  public boolean validate(
    String captchaToken,
    String captchaId,
    String answer
  ) {
    HttpClient httpClient = new HttpClient();
    httpClient
      .getParams()
      .setParameter("http.useragent", "CIRCABC EU Captcha client");
    configureProxy(httpClient);

    String url;
    try {
      url =
        this.captchaUrl +
        "/api/validateCaptcha/" +
        URLEncoder.encode(captchaId, StandardCharsets.UTF_8.toString()) +
        "?captchaAnswer=" +
        URLEncoder.encode(answer, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can not create url ", e);
      }
      return false;
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
        return false;
      }
      return handleCaptchaResponse(methodPost, url);
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error Accessing EU CAPTCHA: " + url, e);
      }
      return false;
    } finally {
      methodPost.releaseConnection();
    }
  }

  @SuppressWarnings("deprecation")
  private void configureProxy(HttpClient httpClient) {
    if ("true".equals(useProxy)) {
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
  }

  private boolean handleCaptchaResponse(PostMethod methodPost, String url)
    throws IOException {
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
      return false;
    }
    return true;
  }

  /**
   * Returns the base URL of the remote EU CAPTCHA validation service.
   *
   * @return the configured CAPTCHA service base URL
   */
  public String getCaptchaUrl() {
    return captchaUrl;
  }

  /**
   * Sets the base URL of the remote EU CAPTCHA validation service.
   *
   * @param captchaUrl the CAPTCHA service base URL to use
   */
  public void setCaptchaUrl(String captchaUrl) {
    this.captchaUrl = captchaUrl;
  }

  /**
   * Returns whether outbound requests are routed through a proxy.
   *
   * @return {@code "true"} when proxy usage is enabled, otherwise a value that
   *     disables it
   */
  public String getUseProxy() {
    return useProxy;
  }

  /**
   * Sets whether outbound requests are routed through a proxy.
   *
   * @param useProxy {@code "true"} to enable proxy usage, any other value to
   *     disable it
   */
  public void setUseProxy(String useProxy) {
    this.useProxy = useProxy;
  }

  /**
   * Returns the outbound proxy host name.
   *
   * @return the proxy host name
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * Sets the outbound proxy host name.
   *
   * @param proxyHost the proxy host name to use
   */
  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  /**
   * Returns the outbound proxy port.
   *
   * @return the proxy port
   */
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * Sets the outbound proxy port.
   *
   * @param proxyPort the proxy port to use
   */
  public void setProxyPort(String proxyPort) {
    this.proxyPort = proxyPort;
  }

  /**
   * Returns the user name used to authenticate against the outbound proxy.
   *
   * @return the proxy user name
   */
  public String getProxyUserName() {
    return proxyUserName;
  }

  /**
   * Sets the user name used to authenticate against the outbound proxy.
   *
   * @param proxyUserName the proxy user name to use
   */
  public void setProxyUserName(String proxyUserName) {
    this.proxyUserName = proxyUserName;
  }

  /**
   * Returns the password used to authenticate against the outbound proxy.
   *
   * @return the proxy password
   */
  public String getProxyPassword() {
    return proxyPassword;
  }

  /**
   * Sets the password used to authenticate against the outbound proxy.
   *
   * @param proxyPassword the proxy password to use
   */
  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }
}
