package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.Share;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative webscript that handles the HTTP {@code POST} request used to add a new
 * share for a given space (folder/node) in the CIRCABC library.
 *
 * <p>The space is identified by the {@code id} template variable extracted from the request URL.
 * The request body is expected to be a JSON object describing the share to create, containing the
 * target interest group ({@code igId}) and the {@code permission} to grant. An optional
 * {@code notifyLeaders} request parameter (with the literal value {@code "true"}) controls whether
 * the leaders of the target interest group are notified about the new share.
 *
 * <p>Before performing the operation the endpoint verifies that the current user holds at least the
 * {@link io.swagger.model.permissions.LibraryPermissions#LIBMANAGEOWN} permission on the space.
 * If the permission check fails an HTTP {@code 403 Forbidden} response is returned; any other error
 * results in an HTTP {@code 406 Not Acceptable} response. On success the model contains a single
 * {@code message} entry set to {@code "ok"}.
 *
 * @author schwerr
 */
public class SpacesIdSharePost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpacesIdSharePost.class);

  /** API providing the business operations on spaces, including share creation. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to check the current user's library permissions on the target space. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Parses the JSON request body into a {@link Share} instance.
   *
   * <p>The expected JSON object contains the {@code igId} (target interest group identifier) and
   * the {@code permission} to be granted; the second constructor argument (share name) is left
   * {@code null}.
   *
   * @param shareBody the raw JSON string sent in the request body
   * @return a {@link Share} populated with the interest group id and permission from the body
   * @throws ParseException if {@code shareBody} is not valid JSON
   */
  private static Share getShare(String shareBody) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(shareBody);

    String igId = (String) json.get("igId");
    String permission = (String) json.get("permission");

    return new Share(igId, null, permission);
  }

  /**
   * Executes the webscript: validates permissions and adds the requested share to the space.
   *
   * <p>Resolves the space id from the URL template variables, checks that the current user has the
   * required library permission, temporarily disables the {@link MLPropertyInterceptor} multilingual
   * awareness, parses the JSON body into a {@link Share} and delegates creation to
   * {@link SpacesApi#addShare(String, Share, boolean)}. The previous ML-awareness state is always
   * restored.
   *
   * @param req the web script request; provides the {@code id} template variable, the optional
   *     {@code notifyLeaders} parameter and the JSON body describing the share
   * @param status the response status, set to {@code 403} on access denial or {@code 406} on any
   *     other error
   * @param cache the cache control object for the response
   * @return a model map containing a {@code message} entry set to {@code "ok"} on success, or
   *     {@code null} when an error occurs and a redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String spaceId = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          spaceId,
          LibraryPermissions.LIBMANAGEOWN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot post the share space, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      boolean notifyLeaders = "true".equals(req.getParameter("notifyLeaders"));

      String shareBody = req.getContent().getContent();

      Share share = getShare(shareBody);

      this.spacesApi.addShare(spaceId, share, notifyLeaders);

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied posting share space with id: " + spaceId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error posting share space with id: " + spaceId, e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
