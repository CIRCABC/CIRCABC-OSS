package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.NodesApi;
import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
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
 * Alfresco declarative web script backing the CIRCABC "create URL in space"
 * REST endpoint.
 *
 * <p>The {@code Post} suffix in the class name reflects the HTTP method the
 * endpoint responds to: an HTTP {@code POST} used to create a new URL (link)
 * content item inside an existing space (folder).
 *
 * <p>Request handling performed by {@link #executeImpl}:
 * <ul>
 *   <li>The target space is identified by the {@code id} URL template
 *       variable.</li>
 *   <li>An optional {@code language} request parameter controls locale-aware
 *       (multilingual) property handling. When absent, the repository is set to
 *       ML-aware mode; when present, the given locale is applied and ML-aware
 *       mode is disabled.</li>
 *   <li>The JSON request body describes the URL to create and is parsed via
 *       {@link NodeJsonParser#parseUrlBasicJSON(WebScriptRequest)}.</li>
 * </ul>
 *
 * <p>Before creating the URL the endpoint verifies that the current user holds
 * the Alfresco "add children" permission on the target space, otherwise the
 * request is rejected. The created {@link Node} is exposed in the response
 * model under the key {@code n}.
 */
public class SpaceUrlPost extends CircabcDeclarativeWebScript {

  /** Request/body key holding the name of the URL item to create. */
  public static final String SPACE_NAME = "name";
  /** Request/body key holding the title of the URL item to create. */
  public static final String SPACE_TITLE = "title";
  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpaceUrlPost.class);

  @Autowired
  private SpacesApi spacesApi;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the POST request that creates a new URL item in a space.
   *
   * <p>Reads the target space id from the {@code id} URL template variable and
   * the URL definition from the JSON request body, applies optional locale
   * handling based on the {@code language} request parameter, checks that the
   * current user may add children to the space and then delegates creation to
   * {@link SpacesApi#spacesIdUrlPost(String, Node)}.
   *
   * <p>On error the method sets an appropriate HTTP status on {@code status},
   * enables redirection and returns {@code null}: {@code 403 Forbidden} when
   * the user lacks permission, {@code 400 Bad Request} for an invalid node
   * reference and {@code 500 Internal Server Error} for parsing/IO or any other
   * unexpected failure. The multilingual awareness flag is always restored to
   * its previous value.
   *
   * @param req the incoming web script request, providing the {@code id}
   *            template variable, the optional {@code language} parameter and
   *            the JSON body
   * @param status the response status, updated with an error code and message
   *               when the operation fails
   * @param cache the response cache directives (unused)
   * @return a model map containing the created {@link Node} under the key
   *         {@code n}, or {@code null} when an error occurred and an error
   *         status/redirect has been set
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

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Cannot create the url, not enough permissions"
        );
      }

      Node body = NodeJsonParser.parseUrlBasicJSON(req);

      model.put("n", this.spacesApi.spacesIdUrlPost(id, body));
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when creating URL in space with id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when creating URL in space with id: " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error("Error creating URL in space with id: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when creating URL in space with id: " + id,
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * @return the spacesApi
   */
  public SpacesApi getSpacesApi() {
    return this.spacesApi;
  }

  /**
   * @param spacesApi the spacesApi to set
   */
  public void setSpacesApi(SpacesApi spacesApi) {
    this.spacesApi = spacesApi;
  }

  /**
   * @return the nodesApi
   */
  public NodesApi getNodesApi() {
    return this.nodesApi;
  }

  /**
   * @param nodesApi the nodesApi to set
   */
  public void setNodesApi(NodesApi nodesApi) {
    this.nodesApi = nodesApi;
  }

  /**
   * Injects the service used to check the current user's Alfresco permissions
   * on the target space.
   *
   * @param currentUserPermissionCheckerService the permission checker service
   *                                             to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
