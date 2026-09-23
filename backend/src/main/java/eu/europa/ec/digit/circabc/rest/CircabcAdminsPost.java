package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.CircabcApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
 * Alfresco declarative web script that handles the {@code POST} request for granting
 * CIRCABC administrator rights to a set of users.
 *
 * <p>The endpoint expects a JSON request body containing a list of user identifiers
 * (parsed via {@link SimpleIdJsonParser#parseListOfId(WebScriptRequest)}) and promotes
 * those users to CIRCABC administrators through {@link CircabcApi#circabcAdminsPost(List)}.
 * The optional {@code language} request parameter controls the content locale used while
 * processing the request; when it is absent the repository is switched to multilingual
 * (ML aware) mode instead.</p>
 *
 * <p>The operation is restricted to callers who are either a CIRCABC administrator or an
 * Alfresco administrator; any other caller receives an HTTP {@code 403 Forbidden}
 * response. Invalid input or malformed JSON results in an HTTP {@code 400 Bad Request},
 * while unexpected failures yield an HTTP {@code 500 Internal Server Error}.</p>
 */
public class CircabcAdminsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CircabcAdminsPost.class);

  /**
   * Business API used to promote the supplied users to CIRCABC administrators.
   */
  @Autowired
  private CircabcApi circabcApi;

  /**
   * Service used to verify that the current caller has sufficient privileges
   * (CIRCABC administrator or Alfresco administrator) to perform the operation.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the {@code POST} request that grants CIRCABC administrator rights.
   *
   * <p>The method sets up the content locale from the optional {@code language} request
   * parameter, checks that the caller is a CIRCABC or Alfresco administrator, parses the
   * list of user identifiers from the request body and delegates the promotion to
   * {@link CircabcApi#circabcAdminsPost(List)}. On error it sets the appropriate HTTP
   * status on {@code status} and returns {@code null}. The previous ML-aware state is
   * always restored before the method returns.</p>
   *
   * @param req the web script request; provides the optional {@code language} parameter
   *     and the JSON body listing the user identifiers to promote
   * @param status the response status, updated with the relevant HTTP error code when the
   *     request cannot be fulfilled
   * @param cache the cache directives for the response
   * @return an (empty) model map on success, or {@code null} when the request fails and an
   *     error status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

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
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isAlfrescoAdmin();
      if (!isAllowed) {
        throw new AccessDeniedException(
          "The user don't have enough permissions"
        );
      }

      List<String> userIds = SimpleIdJsonParser.parseListOfId(req);
      this.circabcApi.circabcAdminsPost(userIds);
    } catch (AccessDeniedException ade) {
      logger.error(ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
