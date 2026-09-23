package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpLink;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the HTTP {@code POST} request for creating a new
 * help link.
 *
 * <p>This endpoint creates a help link from the JSON payload sent in the request body. An optional
 * {@code language} request parameter controls the locale used when persisting the multilingual
 * content: when it is absent the underlying node service is left multilingual-aware, otherwise the
 * given language is applied as the content and interface locale and multilingual awareness is
 * disabled so the value is stored for that specific locale.
 *
 * <p>Only an Alfresco administrator or a CIRCABC administrator is allowed to create a help link.
 * Requests from other users are rejected with an HTTP {@code 403 Forbidden} response, while a
 * malformed or unreadable request body results in an HTTP {@code 400 Bad Request} response.
 *
 * @author beaurpi
 */
public class HelpLinksPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(HelpLinksPost.class);

  /** API used to perform the help link business operations, such as creating a help link. */
  @Autowired
  private HelpApi helpApi;

  /** Service used to verify whether the current user has the required administrator permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Creates a new help link from the request payload.
   *
   * <p>Reads the optional {@code language} request parameter to configure the multilingual context,
   * verifies that the current user is an Alfresco or CIRCABC administrator, parses the JSON body
   * into a {@link HelpLink} and delegates its creation to the {@link HelpApi}. The multilingual
   * awareness state is always restored to its original value before returning.
   *
   * @param req the web script request, providing the optional {@code language} parameter and the
   *     JSON body describing the help link to create
   * @param status the response status, set to {@link Status#STATUS_FORBIDDEN} when the user lacks
   *     the required permissions or to {@link Status#STATUS_BAD_REQUEST} when the body cannot be
   *     parsed
   * @param cache the cache directives for the response
   * @return a model map containing the created help link under the {@code "link"} key, or
   *     {@code null} when the request is rejected (access denied or bad request)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
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
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot create help link, not enough permission"
        );
      }

      HelpLink body = HelpJsonParser.parseLink(req);

      model.put("link", helpApi.createHelpLink(body));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Internal error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
