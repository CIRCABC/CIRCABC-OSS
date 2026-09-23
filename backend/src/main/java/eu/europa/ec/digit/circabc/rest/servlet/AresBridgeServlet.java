/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.europa.ec.digit.circabc.rest.servlet;

import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeDaoService;
import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeService;
import io.swagger.api.AresBridgeApi;
import io.swagger.exception.CircabcException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * HTTP servlet that handles the ARES Bridge integration callbacks under the
 * {@code /aresbridge/*} URL space.
 *
 * <p>ARES (the European Commission's document registration system) invokes this
 * servlet to notify CIRCABC about the outcome of document operations. Two HTTP
 * methods are supported:
 *
 * <ul>
 *   <li>{@code POST /aresbridge/{action}} &mdash; receives a JSON callback where
 *       {@code {action}} is one of {@code cancel}, {@code save}, {@code timeout}
 *       or {@code register}. The request must carry an {@code Authorization}
 *       header and a {@code Date} (or {@code X-AB-Date}) header, and its body is
 *       a JSON object containing {@code transactionId}, {@code documentId},
 *       {@code saveNumber} and {@code registrationNumber}. The parsed response is
 *       persisted via {@link AresBridgeDaoService}.</li>
 *   <li>{@code GET /aresbridge/*} &mdash; used both as a health check (returns a
 *       running message when no query string is present) and as a token-secured
 *       redirect callback. When query parameters are supplied they are validated
 *       and stored, the pending ARES bridge work is processed, and the caller is
 *       redirected to the CIRCABC welcome UI.</li>
 * </ul>
 *
 * <p>Collaborating Spring beans ({@link AresBridgeApi}, {@link AresBridgeDaoService}
 * and {@link AresBridgeService}) are looked up lazily from the surrounding
 * {@link WebApplicationContext} on first use.
 */
@WebServlet("/aresbridge/*")
//@SuppressWarnings({ "squid:S112" })
public class AresBridgeServlet extends HttpServlet {

  /** Generic message returned to callers when an unexpected server-side error occurs. */
  private static final String INTERNAL_SERVER_ERROR_OCCURRED =
    "Internal server error occurred";

  /** Logger for this servlet. */
  private static final Log logger = LogFactory.getLog(AresBridgeServlet.class);

  /** DAO service used to persist the responses received from ARES. */
  private transient AresBridgeDaoService aresBridgeDaoService;

  /** API bean providing authorization/token validation for ARES callbacks. */
  private transient AresBridgeApi aresBridgeApi;

  /** Service that processes pending ARES bridge work triggered by GET callbacks. */
  private transient AresBridgeService aresBridgeService;

  /**
   * Initializes the servlet and resolves the required collaborating beans
   * ({@link AresBridgeApi}, {@link AresBridgeDaoService} and
   * {@link AresBridgeService}) from the Spring {@link WebApplicationContext}
   * associated with the servlet context.
   *
   * @param config the servlet configuration provided by the container
   * @throws ServletException if initialization fails
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init();
    WebApplicationContext context =
      WebApplicationContextUtils.getRequiredWebApplicationContext(
        getServletContext()
      );

    aresBridgeApi = (AresBridgeApi) context.getBean("aresBridgeApi");
    aresBridgeDaoService = (AresBridgeDaoService) context.getBean(
      "aresBridgeDaoService"
    );

    aresBridgeService = (AresBridgeService) context.getBean(
      "aresBridgeService"
    );
  }

  /**
   * Handles ARES callback notifications sent as JSON.
   *
   * <p>The last path segment of the request URI selects the action type
   * ({@code cancel}, {@code save}, {@code timeout} or {@code register}). The
   * request is authenticated via its {@code Authorization}/{@code Date} headers,
   * its {@code application/json} body is parsed, and the resulting response is
   * persisted through {@link AresBridgeDaoService}. On success the response
   * status is set to {@code 200 OK}; validation problems yield {@code 400 Bad
   * Request} and unexpected failures yield {@code 500 Internal Server Error}.
   * A duplicate record is treated as success.
   *
   * @param request the incoming callback request
   * @param response the response whose status/body is populated
   */
  @Override
  protected void doPost(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    try {
      if (aresBridgeApi == null) {
        init();
      }

      authenticate(request);

      String path = StringUtils.substringAfterLast(
        request.getRequestURI(),
        "/"
      );
      if (StringUtils.isBlank(path)) {
        throw new CircabcException("Invalid message received");
      }
      if (
        request.getContentType() == null ||
        !request.getContentType().startsWith("application/json")
      ) {
        throw new CircabcException(
          "Invalid message received, content type must be application/json instead of " +
            request.getContentType()
        );
      }

      String requestType = determineRequestType(path);
      String json = readBody(request);
      processJsonPayload(json, requestType);

      response.setStatus(HttpServletResponse.SC_OK);
    } catch (DuplicateKeyException e) {
      if (logger.isInfoEnabled()) {
        logger.info("Record was created in GET", e);
      }
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (CircabcException e) {
      if (logger.isErrorEnabled()) {
        logger.error("CircabcException occurred", e);
      }
      handleError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error in AresBridge callback", e);
      }
      handleError(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_OCCURRED
      );
    }
  }

  private String determineRequestType(String path) throws CircabcException {
    switch (path) {
      case "cancel":
        return "cancel";
      case "save":
        return "save";
      case "timeout":
        return "timeout";
      case "register":
        return "register";
      default:
        throw new CircabcException(
          "Invalid message received, path /" + path + " is not supported"
        );
    }
  }

  private void processJsonPayload(String json, String requestType)
    throws CircabcException {
    JSONParser parser = new JSONParser();
    JSONObject jsonObject;
    try {
      jsonObject = (JSONObject) parser.parse(json);
    } catch (ParseException e) {
      throw new CircabcException(
        "Invalid message received, JSON parsing failed: " + e.getMessage()
      );
    }
    String transactionId = String.valueOf(jsonObject.get("transactionId"));
    String documentId = String.valueOf(jsonObject.get("documentId"));
    String saveNumber = String.valueOf(jsonObject.get("saveNumber"));
    String registrationNumber = String.valueOf(
      jsonObject.get("registrationNumber")
    );
    aresBridgeDaoService.saveResponse(
      transactionId,
      requestType,
      documentId,
      saveNumber,
      registrationNumber
    );
  }

  private String readBody(HttpServletRequest request) throws IOException {
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      buffer.append(line);
    }
    return buffer.toString();
  }

  private void authenticate(HttpServletRequest request)
    throws CircabcException {
    String authorizationHeader = request.getHeader("Authorization");
    if (StringUtils.isBlank(authorizationHeader)) {
      throw new CircabcException("Authorization header is missing");
    }
    String dateHeader = request.getHeader("Date");
    if (StringUtils.isBlank(dateHeader)) {
      dateHeader = request.getHeader("X-AB-Date");
      if (StringUtils.isBlank(dateHeader)) {
        throw new CircabcException("Date header is missing");
      }
    }
    boolean isValid = aresBridgeApi.validateAuthorizationHeader(
      dateHeader,
      authorizationHeader,
      request.getRequestURI()
    );
    if (!isValid) {
      throw new CircabcException("Ticket is not valid");
    }
  }

  private void tryInit(HttpServletResponse response) {
    try {
      init();
    } catch (ServletException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error in AresBridge callback", e);
      }
      handleError(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Failed to initialize service"
      );
    }
  }

  /**
   * Handles ARES bridge GET requests.
   *
   * <p>Ensures the collaborating beans are initialized, stores any token-secured
   * response parameters supplied on the query string, and then processes the
   * request: with no query string it acts as a health check, otherwise it
   * triggers {@link AresBridgeService#process()} and redirects to the CIRCABC
   * welcome UI. Unexpected errors are reported as {@code 500 Internal Server
   * Error}.
   *
   * @param request the incoming GET request
   * @param response the response whose status/body is populated
   */
  @Override
  protected void doGet(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    try {
      if (aresBridgeApi == null) {
        tryInit(response);
      }
      saveResponse(request);
      process(request, response);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Unexpected error in GET request", e);
      }
      handleError(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_OCCURRED
      );
    }
  }

  private void process(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    try {
      if (
        request.getQueryString() == null || request.getQueryString().isEmpty()
      ) {
        response.getWriter().write("AresBridgeCallbackServlet is running");
      } else {
        aresBridgeService.process();
        response.sendRedirect(request.getContextPath() + "/ui/welcome");
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Exception happened", e);
      }
      handleError(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_OCCURRED
      );
    }
  }

  private void saveResponse(HttpServletRequest request) {
    try {
      ResponseParameters params = extractQueryParameters(request);
      validateAndSaveResponse(request, params);
    } catch (DuplicateKeyException e) {
      if (logger.isInfoEnabled()) {
        logger.info("Record was created in POST", e);
      }
    } catch (URISyntaxException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can not parse url", e);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Exception happened", e);
      }
    }
  }

  private ResponseParameters extractQueryParameters(HttpServletRequest request)
    throws URISyntaxException {
    ResponseParameters params = new ResponseParameters();

    @SuppressWarnings("deprecation")
    List<NameValuePair> queryParameters = URLEncodedUtils.parse(
      new URI(getURL(request)),
      StandardCharsets.UTF_8.name()
    );

    for (NameValuePair parameter : queryParameters) {
      populateParameter(params, parameter);
    }

    return params;
  }

  private void populateParameter(
    ResponseParameters params,
    NameValuePair parameter
  ) {
    switch (parameter.getName()) {
      case "token":
        params.token = parameter.getValue();
        break;
      case "date":
        params.date = parameter.getValue();
        break;
      case "transactionId":
        params.transactionId = parameter.getValue();
        break;
      case "apiKey":
        break;
      case "action":
        params.action = parameter.getValue();
        break;
      case "documentId":
        params.documentId = parameter.getValue();
        break;
      case "saveNumber":
        params.saveNumber = parameter.getValue();
        break;
      case "registrationNumber":
        params.registrationNumber = parameter.getValue();
        break;
      default:
        if (logger.isErrorEnabled()) {
          logger.error("Invalid query parameter: " + parameter.getName());
        }
    }
  }

  private void validateAndSaveResponse(
    HttpServletRequest request,
    ResponseParameters params
  ) {
    boolean isValidToken = aresBridgeApi.validateToken(
      params.date,
      params.token,
      request.getRequestURI(),
      "GET"
    );

    if (isValidToken) {
      aresBridgeDaoService.saveResponse(
        params.transactionId,
        params.action,
        params.documentId,
        params.saveNumber,
        params.registrationNumber
      );
    } else {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid token received in GET request");
      }
    }
  }

  // Helper class to store request parameters
  private static class ResponseParameters {

    String token = "";
    String date = "";
    String transactionId = "";
    String action = "";
    String documentId = "";
    String saveNumber = "";
    String registrationNumber = "";
  }

  private void handleError(
    HttpServletResponse response,
    int statusCode,
    String message
  ) {
    try {
      response.setStatus(statusCode);
      response.setContentType("text/plain");
      response.getWriter().write(message);
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to send error response", e);
      }
    }
  }

  /**
   * Reconstructs the full original request URL (scheme, host, optional non-default
   * port, context path, servlet path, path info and query string) from the given
   * request.
   *
   * @param req the request to reconstruct the URL from
   * @return the fully reconstructed request URL as a string
   */
  public static String getURL(HttpServletRequest req) {
    String scheme = req.getScheme(); // http
    String serverName = req.getServerName(); // hostname.com
    int serverPort = req.getServerPort(); // 80
    String contextPath = req.getContextPath(); // /mywebapp
    String servletPath = req.getServletPath(); // /servlet/MyServlet
    String pathInfo = req.getPathInfo(); // /a/b;c=123
    String queryString = req.getQueryString(); // d=789

    // Reconstruct original requesting URL
    StringBuilder url = new StringBuilder();
    url.append(scheme).append("://").append(serverName);

    if (serverPort != 80 && serverPort != 443) {
      url.append(":").append(serverPort);
    }

    url.append(contextPath).append(servletPath);

    if (pathInfo != null) {
      url.append(pathInfo);
    }
    if (queryString != null) {
      url.append("?").append(queryString);
    }
    return url.toString();
  }
}
