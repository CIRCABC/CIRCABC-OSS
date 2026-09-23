package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NewsJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that handles the HTTP {@code PUT} request used to update the
 * "News" content of an Interest Group's Information service.
 *
 * <p>The target news item is identified by the {@code id} template variable taken from the
 * request URL. An optional {@code language} request parameter controls how multilingual (ML)
 * content is resolved: when it is omitted the interceptor is set to ML-aware mode, otherwise the
 * corresponding {@link java.util.Locale} is applied for both the content and the UI locale and
 * ML-aware mode is disabled. The JSON request body is parsed into a {@link News} object and
 * delegated to {@link InformationApi#newsIdPut(String, News)}.</p>
 *
 * <p>Before performing the update the endpoint checks that the current user has Alfresco write
 * permission on the target node. The resulting news information is returned to the FreeMarker
 * template under the {@code newsInfo} key.</p>
 */
public class NewsPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NewsPut.class);

  /** API providing access to the Information service business operations, including news updates. */
  @Autowired
  private InformationApi informationApi;

  /** Service used to verify that the current user holds the required Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the news update request.
   *
   * <p>Reads the {@code id} template variable to locate the news item and the optional
   * {@code language} request parameter to configure multilingual handling. After confirming the
   * current user has write permission on the node, the JSON request body is parsed and the update
   * is delegated to the {@link InformationApi}. On failure the appropriate HTTP status code is set
   * on the response and {@code null} is returned so the framework can render the error.</p>
   *
   * @param req the web script request; supplies the {@code id} template variable, the optional
   *     {@code language} parameter and the JSON body describing the news content
   * @param status the response status, updated with an error code and message when the request
   *     cannot be completed (403 for access denied, 400 for bad request, 500 for unexpected errors)
   * @param cache the web script cache directives for the response
   * @return a model map containing the updated news information under the {@code newsInfo} key, or
   *     {@code null} when an error occurs and the status has been set accordingly
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoWritePermission(id)
      ) {
        throw new AccessDeniedException(
          "Not enough permission to update the news"
        );
      }

      News body = NewsJsonParser.parse(req);
      model.put("newsInfo", this.informationApi.newsIdPut(id, body));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      logger.error("Error processing news put request - access denied", ade);
      return null; // NOSONAR
    } catch (
      InvalidNodeRefException
      | java.text.ParseException
      | ParseException
      | IOException inre
    ) {
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      logger.error("Error processing news put request - bad request", inre);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      logger.error("Unexpected error processing news put request", e);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
