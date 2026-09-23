package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupCreationRequest;
import io.swagger.util.parsers.InterestGroupJsonParser;
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
 * Alfresco webscript endpoint handling the HTTP {@code POST} request that
 * submits a request to create a new Interest Group under a given Category.
 *
 * <p>The category is identified by the {@code id} URL template variable. The
 * request body is parsed into a {@link GroupCreationRequest} and delegated to
 * {@link CategoriesApi#categoriesIdGroupRequestPost(String, GroupCreationRequest)},
 * which performs the underlying business logic. No special permission check is
 * performed at the webscript level.</p>
 *
 * <p>An optional {@code language} request parameter controls locale handling:
 * when provided, the content and UI locale are set accordingly and multilingual
 * awareness is disabled; when absent, multilingual awareness is enabled. The
 * previous multilingual-awareness state is always restored once processing
 * completes.</p>
 *
 * @see CircabcDeclarativeWebScript
 * @see CategoriesApi
 */
public class CategoryRequestGroupPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryRequestGroupPost.class);

  /**
   * API providing the category-related business operations, including the
   * submission of Interest Group creation requests. Injected by Spring.
   */
  @Autowired
  private CategoriesApi categoriesApi;

  /**
   * Processes the POST request that submits an Interest Group creation request
   * for the category identified by the {@code id} URL template variable.
   *
   * <p>Reads the optional {@code language} request parameter to configure the
   * locale and multilingual awareness, parses the request body into a
   * {@link GroupCreationRequest}, and forwards it to the {@link CategoriesApi}.
   * The original multilingual-awareness state is restored before returning.</p>
   *
   * <p>On failure the response status is set and {@code null} is returned:
   * {@link Status#STATUS_FORBIDDEN} when access is denied, or
   * {@link Status#STATUS_BAD_REQUEST} when the request is invalid or cannot be
   * parsed.</p>
   *
   * @param req    the web script request, providing the {@code id} template
   *               variable, the optional {@code language} parameter and the
   *               JSON request body
   * @param status the response status to be populated on error
   * @param cache  the cache directives for the response
   * @return an (empty) model map on success, or {@code null} when the request
   *         failed and an error status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String categoryId = templateVars.get("id");

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
      // no special permission check required here.
      GroupCreationRequest body =
        InterestGroupJsonParser.parseGroupCreationRequest(req);
      this.categoriesApi.categoriesIdGroupRequestPost(categoryId, body);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error("Bad request", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
