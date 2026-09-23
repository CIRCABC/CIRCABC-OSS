package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that lists the immediate sub-spaces (child
 * folders) directly below a given space.
 *
 * <p>The class name implies an HTTP {@code GET} operation ({@code SubspacesIdGet}):
 * it resolves the target space from the mandatory {@code id} path variable,
 * verifies that the current user holds library access on that space, retrieves
 * its direct children and returns only those whose type is a folder (i.e. the
 * sub-spaces), exposing them under the {@code data} key of the response model.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (path variable) &ndash; the identifier of the parent space;
 *       mandatory.</li>
 *   <li>{@code language} (query parameter) &ndash; optional locale used to
 *       resolve multilingual content; when absent the response stays
 *       multilingual-aware.</li>
 *   <li>{@code sort} / {@code order} (query parameters) &ndash; sorting field
 *       and direction, combined as {@code sort_order}.</li>
 *   <li>{@code skipExpiredItems} (query parameter) &ndash; when {@code true},
 *       excludes expired items from the listing.</li>
 * </ul>
 *
 * @author schwerr
 */
public class SubspacesIdGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SubspacesIdGet.class);

  /** Service exposing space (folder) operations, including child retrieval. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to verify the current user's library permissions on a space. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the webscript request: lists the immediate sub-spaces (child
   * folders) of the space identified by the {@code id} path variable.
   *
   * <p>The current user must hold {@link LibraryPermissions#LIBACCESS} on the
   * target space. When a {@code language} parameter is supplied the content
   * locale is set accordingly and multilingual awareness is disabled so that
   * localized property values are returned; otherwise multilingual awareness is
   * enabled. Only children of type folder are collected and placed under the
   * {@code data} key of the returned model. Access and processing errors are
   * translated into the appropriate HTTP status codes and result in a
   * {@code null} model.</p>
   *
   * @param req the webscript request; provides the {@code id} template variable
   *            and the {@code language}, {@code sort}, {@code order} and
   *            {@code skipExpiredItems} query parameters
   * @param status the webscript status, updated to {@code 403 Forbidden} on
   *               access denial or {@code 406 Not Acceptable} on other errors
   * @param cache the webscript cache directives
   * @return a model map containing the {@code data} entry with the list of
   *         sub-space {@link Node}s, or {@code null} if an error occurred
   * @throws IllegalArgumentException if the {@code id} path variable is missing
   *                                  or empty
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

    if ((id == null) || id.isEmpty()) {
      throw new IllegalArgumentException(
        "The space 'id' is a mandatory parameter."
      );
    }

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    String sort = req.getParameter("sort");
    String order = req.getParameter("order");
    String skipExpiredParam = req.getParameter("skipExpiredItems");
    boolean skipExpiredItems =
      skipExpiredParam != null && Boolean.parseBoolean(skipExpiredParam);

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot list the children of this space, not enough permissions"
        );
      }

      if (language == null) {
        MLPropertyInterceptor.setMLAware(true);
      } else {
        Locale locale = Locale.of(language);
        I18NUtil.setContentLocale(locale);
        I18NUtil.setLocale(locale);
        MLPropertyInterceptor.setMLAware(false);
      }

      PagedNodes nodes = this.spacesApi.spaceGetChildren(
        id,
        0,
        -1,
        sort + "_" + order,
        true,
        false,
        skipExpiredItems
      );

      List<Node> spaces = new ArrayList<>();

      // fish only the spaces
      for (Node node : nodes.getData()) {
        if (ContentModel.TYPE_FOLDER.toString().equals(node.getType())) {
          spaces.add(node);
        }
      }

      model.put("data", spaces);
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when listing subspaces for space: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error listing subspaces for space: " + id, e);
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
