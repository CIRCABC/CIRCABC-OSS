package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import io.swagger.api.FileAttachmentData;
import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script backing the {@code PUT} operation on an existing post
 * (a forum/newsgroup topic reply or a library-attached post).
 *
 * <p>The {@code Put} suffix in the class name reflects the HTTP {@code PUT} method: the endpoint
 * updates a post identified by the {@code id} template variable in the request URL. The request
 * body is expected to be a {@code multipart/form-data} form whose fields drive the update:
 *
 * <ul>
 *   <li>{@code post} &ndash; JSON payload describing the new post body/metadata.</li>
 *   <li>{@code filesToAdd} &ndash; one or more file parts to attach to the post.</li>
 *   <li>{@code linksToAdd} &ndash; identifiers/URLs of links to attach to the post.</li>
 *   <li>{@code attachmentsToDelete} &ndash; identifiers of existing attachments to remove.</li>
 * </ul>
 *
 * <p>An optional {@code notify} request parameter controls whether subscribers are notified of the
 * change; any value other than {@code "false"} enables notification. When notifications are
 * enabled the updated node is tagged with the {@link ContentNotifyAspect#ASPECT_CONTENT_NOTIFY}
 * aspect so the notification machinery can pick it up.
 *
 * <p>Before performing the update the caller's permissions are validated: the user must hold a
 * suitable newsgroup permission (moderate, admin or post) or a suitable library permission
 * (manage-own, full-edit or admin). The successfully updated post is returned to the FreeMarker
 * template under the {@code post} model key.
 */
public class PostsPut extends CircabcDeclarativeWebScript {

  /** Reusable status message returned for malformed or invalid requests. */
  private static final String BAD_REQUEST = "Bad request";

  /** Logger used to record errors raised while updating a post. */
  static final Log logger = LogFactory.getLog(PostsPut.class);

  /** API providing the business logic for updating topics/posts. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify that the current user is allowed to edit the target post. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco service used to tag the updated node with the notification aspect. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the incoming {@code PUT} request: validates permissions and the multipart form,
   * applies the requested changes to the post and, when requested, flags it for notification.
   *
   * @param req the web script request; must expose the post {@code id} template variable and a
   *     multipart form body, and may carry a {@code notify} parameter
   * @param status the response status, updated with an appropriate HTTP code and message when an
   *     error occurs
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the updated post under the {@code post} key on success, or
   *     {@code null} when an error has been handled and the corresponding error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String id = req.getServiceMatch().getTemplateVars().get("id");
    boolean notify = !"false".equals(req.getParameter("notify"));

    new DisableNotificationThreadLocal().set(!notify);

    try {
      validatePermission(id);
      FormData form = validateForm(req);
      PostUpdateData data = parseFormData(form);

      Node node = topicsApi.postsIdPut(
        id,
        data.body,
        data.filesToAdd,
        data.linksToAdd,
        data.attachmentsToDelete
      );
      model.put("post", node);

      if (notify) {
        nodeService.addAspect(
          Converter.createNodeRefFromId(node.getId()),
          ContentNotifyAspect.ASPECT_CONTENT_NOTIFY,
          null
        );
      }
    } catch (AccessDeniedException ade) {
      return handleError(
        status,
        Status.STATUS_FORBIDDEN,
        "Access denied",
        ade,
        id
      );
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        BAD_REQUEST,
        inre,
        id
      );
    } catch (ParseException pe) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        BAD_REQUEST,
        pe,
        id
      );
    } catch (IOException ioe) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        BAD_REQUEST,
        ioe,
        id
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e,
        id
      );
    }
    return model;
  }

  /**
   * Ensures the current user has sufficient rights to edit the post.
   *
   * <p>Access is granted if the user holds any newsgroup moderation/admin/post permission or any
   * library manage-own/full-edit/admin permission on the target node.
   *
   * @param id the identifier of the post being edited
   * @throws AccessDeniedException if the user has neither the required newsgroup nor library
   *     permissions
   */
  private void validatePermission(String id) {
    boolean hasNewsGroupPerm =
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        id,
        NewsGroupPermissions.NWSMODERATE,
        NewsGroupPermissions.NWSADMIN,
        NewsGroupPermissions.NWSPOST
      );
    boolean hasLibraryPerm =
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBMANAGEOWN,
        LibraryPermissions.LIBFULLEDIT,
        LibraryPermissions.LIBADMIN
      );
    if (!hasNewsGroupPerm && !hasLibraryPerm) {
      throw new AccessDeniedException(
        "cannot edit the post, not enough permissions"
      );
    }
  }

  /**
   * Parses and validates the multipart form carried by the request.
   *
   * @param req the incoming web script request
   * @return the parsed {@link FormData}
   * @throws IllegalArgumentException if the request is not multipart or contains no fields
   */
  private FormData validateForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    if (form.getFields().length == 0) {
      throw new IllegalArgumentException("Wrong number of parameters.");
    }
    return form;
  }

  /**
   * Aggregates all form fields into a {@link PostUpdateData} value object describing the update.
   *
   * @param form the validated multipart form
   * @return the collected update data
   * @throws IOException if a file part cannot be read
   * @throws ParseException if the {@code post} JSON payload cannot be parsed
   */
  private PostUpdateData parseFormData(FormData form)
    throws IOException, ParseException {
    PostUpdateData data = new PostUpdateData();
    for (FormData.FormField field : form.getFields()) {
      processField(field, data);
    }
    return data;
  }

  /**
   * Processes a single form field, populating the appropriate part of {@code data} according to the
   * field name ({@code post}, {@code filesToAdd}, {@code linksToAdd} or {@code attachmentsToDelete}).
   *
   * @param field the form field to process
   * @param data the accumulator to populate
   * @throws IOException if a file part cannot be read
   * @throws ParseException if the {@code post} JSON payload cannot be parsed
   * @throws IllegalArgumentException if the field name is not recognised
   */
  private void processField(FormData.FormField field, PostUpdateData data)
    throws IOException, ParseException {
    switch (field.getName()) {
      case "post":
        data.body = NodeJsonParser.parsePostJSON(Converter.getValue(field));
        break;
      case "filesToAdd":
        if (field.getIsFile()) {
          data.filesToAdd.add(
            new FileAttachmentData(
              field.getFilename(),
              field.getContent().getSize(),
              field.getInputStream(),
              field.getContent().getMimetype(),
              field.getContent().getEncoding()
            )
          );
        }
        break;
      case "linksToAdd":
        data.linksToAdd.add(field.getContent().getContent());
        break;
      case "attachmentsToDelete":
        data.attachmentsToDelete.add(field.getContent().getContent());
        break;
      default:
        throw new IllegalArgumentException("Unexpected parameter.");
    }
  }

  /**
   * Logs the given error and configures the response status for an error outcome.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set
   * @param message the human-readable status message
   * @param e the exception that triggered the error, logged for diagnostics
   * @param id the identifier of the post being updated, included in the log message
   * @return {@code null}, signalling to {@code executeImpl} that no model should be rendered
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e,
    String id
  ) {
    logger.error(message + " when updating post with id: " + id, e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }

  /**
   * Simple mutable value holder gathering the fields of a post-update request as parsed from the
   * multipart form.
   */
  private static class PostUpdateData {

    /** The new post body/metadata parsed from the {@code post} JSON field. */
    Node body = null;
    /** File attachments to add to the post. */
    List<FileAttachmentData> filesToAdd = new ArrayList<>();
    /** Links to add to the post. */
    List<String> linksToAdd = new ArrayList<>();
    /** Identifiers of existing attachments to delete from the post. */
    List<String> attachmentsToDelete = new ArrayList<>();
  }
}
