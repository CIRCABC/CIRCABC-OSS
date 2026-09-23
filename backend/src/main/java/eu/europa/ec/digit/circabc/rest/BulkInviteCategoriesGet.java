package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that lists the categories
 * into which a given user may be bulk invited.
 *
 * <p>The endpoint resolves the mandatory {@code username} request parameter and delegates to
 * {@link UsersApi#getBulkInviteCategories(String)} to retrieve the list of {@link Category}
 * instances the user is eligible to be bulk invited to. The resulting list is exposed to the
 * FreeMarker response template under the {@code categories} key.
 *
 * <p>Error handling maps failures to HTTP status codes: an {@link AccessDeniedException} yields
 * {@code 403 Forbidden}, an {@link InvalidNodeRefException} yields {@code 400 Bad Request}, and any
 * other unexpected exception yields {@code 500 Internal Server Error}.
 *
 * <p>Required request parameters:
 * <ul>
 *   <li>{@code username} &ndash; the identifier of the user for whom eligible bulk-invite
 *       categories are resolved; must not be {@code null} or blank.</li>
 * </ul>
 */
public class BulkInviteCategoriesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(BulkInviteCategoriesGet.class);

  /** API used to resolve the categories a user is eligible to be bulk invited to. */
  @Autowired
  private UsersApi usersApi;

  /**
   * Handles the {@code GET} request: validates the {@code username} parameter and builds the
   * response model containing the categories the user may be bulk invited to.
   *
   * <p>On success the returned model contains the {@code categories} entry. On failure the method
   * sets the appropriate error status on {@code status}, marks it as a redirect and returns
   * {@code null} so the framework renders the corresponding status template.
   *
   * @param req the incoming web script request; must provide a non-blank {@code username}
   *     parameter
   * @param status the response status object, populated with an error code and message when the
   *     request cannot be fulfilled
   * @param cache the response cache directives
   * @return a model map with the {@code categories} entry on success, or {@code null} when an
   *     error status has been set
   * @throws IllegalArgumentException if the {@code username} parameter is missing or blank
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String username = req.getParameter("username");

    if ((username == null) || username.trim().isEmpty()) {
      throw new IllegalArgumentException("'username' cannot be empty.");
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      List<Category> categories = this.usersApi.getBulkInviteCategories(
        username
      );

      model.put("categories", categories);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied for user: " + username, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for user: " + username, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error while bulk invite categories for user: " + username,
        e
      );
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
