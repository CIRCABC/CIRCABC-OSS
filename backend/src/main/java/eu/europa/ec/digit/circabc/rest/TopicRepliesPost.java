package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.helper.AspectManager;
import io.swagger.api.FileAttachmentData;
import io.swagger.api.TopicsApi;
import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Comment;
import io.swagger.model.Node;
import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.PostJsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
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
 * Alfresco Declarative Web Script that handles the creation of a reply to an existing
 * topic.
 *
 * <p>This endpoint implements the HTTP {@code POST} operation implied by the class name
 * ({@code TopicRepliesPost}). It posts a new reply (comment), optionally with file
 * attachments and links, under the topic identified by the {@code id} path variable.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} &ndash; path variable identifying the parent topic node.</li>
 *   <li>{@code notify} &ndash; optional request parameter; notifications are sent unless
 *       its value is {@code "false"}.</li>
 *   <li>A {@code multipart/form-data} request body containing one or more of the
 *       following fields: {@code comment} (the reply body as JSON), {@code filesToAdd}
 *       (file attachments) and {@code linksToAdd} (links).</li>
 * </ul>
 *
 * <p>Before creating the reply the caller must hold either the newsgroup post permission
 * or the library access permission on the target node. Depending on whether the parent
 * node belongs to a library or a newsgroup service, the corresponding aspect is applied
 * to the newly created reply, and a content-notify aspect is added when notifications are
 * enabled.
 *
 * <p>The model returned on success contains the created reply node under the key
 * {@code "post"}.
 */
public class TopicRepliesPost extends CircabcDeclarativeWebScript {

  /** Logger used to report errors that occur while posting a topic reply. */
  static final Log logger = LogFactory.getLog(TopicRepliesPost.class);

  /** API providing the business logic for topic-related operations. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify that the current user holds the required permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Helper used to determine the node service context and apply library/newsgroup aspects. */
  @Autowired
  private AspectManager aspectManager;

  /** Alfresco node service used to add the content-notify aspect when notifications are enabled. */
  @Autowired
  private NodeService nodeService;

  /**
   * Handles the web script request to post a reply to a topic.
   *
   * <p>Reads the {@code id} path variable and the optional {@code notify} parameter,
   * validates the caller's permissions and the multipart form, parses the reply body,
   * attachments and links, creates the reply via {@link TopicsApi}, applies the relevant
   * aspects and returns the created node.
   *
   * @param req the web script request; must be a multipart request and must supply the
   *            {@code id} path variable
   * @param status the response status, updated with an error code, message and redirect
   *               flag when the request cannot be processed
   * @param cache the cache directives for the response
   * @return a model map containing the created reply node under the key {@code "post"},
   *         or {@code null} when an error occurred (in which case {@code status} carries
   *         the error details)
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
      ReplyData data = parseFormData(form);

      Node node = topicsApi.topicsIdRepliesPost(
        id,
        data.body,
        data.filesToAdd,
        data.linksToAdd
      );
      applyAspects(node, id, notify);
      model.put("post", node);
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (
      InvalidNodeRefException
      | InvalidTopicException
      | ParseException
      | IOException e
    ) {
      return handleError(status, Status.STATUS_BAD_REQUEST, "Bad request", e);
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    }
    return model;
  }

  /**
   * Verifies that the current user is allowed to post a reply to the given node.
   *
   * @param id the identifier of the parent topic node
   * @throws AccessDeniedException if the user has neither the newsgroup post permission
   *                               nor the library access permission on the node
   */
  private void validatePermission(String id) {
    boolean hasNewsGroupPerm =
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        id,
        NewsGroupPermissions.NWSPOST
      );
    boolean hasLibraryPerm =
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        id,
        LibraryPermissions.LIBACCESS
      );
    if (!hasNewsGroupPerm && !hasLibraryPerm) {
      throw new AccessDeniedException(
        "Cannot post the reply to the topic, not enough permissions"
      );
    }
  }

  /**
   * Extracts and validates the multipart form data from the request.
   *
   * @param req the web script request
   * @return the parsed {@link FormData}
   * @throws IllegalArgumentException if the request is not multipart or contains no fields
   */
  private FormData validateForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null || !form.getIsMultiPart()) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    if (form.getFields().length == 0) {
      throw new IllegalArgumentException("Wrong number of parameters.");
    }
    return form;
  }

  /**
   * Iterates over the fields of the form and builds the reply payload.
   *
   * @param form the validated multipart form data
   * @return a {@link ReplyData} holding the reply body, attachments and links
   * @throws IOException if reading a field's content fails
   * @throws ParseException if the JSON comment field cannot be parsed
   */
  private ReplyData parseFormData(FormData form)
    throws IOException, ParseException {
    ReplyData data = new ReplyData();
    for (FormData.FormField field : form.getFields()) {
      processField(field, data);
    }
    return data;
  }

  /**
   * Processes a single form field and populates the corresponding part of the reply data.
   *
   * <p>Recognised field names are {@code comment} (parsed as the reply body),
   * {@code filesToAdd} (file attachments) and {@code linksToAdd} (links).
   *
   * @param field the form field to process
   * @param data the reply data accumulator to update
   * @throws IOException if reading the field's content fails
   * @throws ParseException if the JSON comment field cannot be parsed
   * @throws IllegalArgumentException if the field name is not one of the expected values
   */
  private void processField(FormData.FormField field, ReplyData data)
    throws IOException, ParseException {
    switch (field.getName()) {
      case "comment":
        data.body = PostJsonParser.parsePartial(Converter.getValue(field));
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
      default:
        throw new IllegalArgumentException("Unexpected parameter.");
    }
  }

  /**
   * Applies the appropriate aspects to a newly created reply node.
   *
   * <p>Depending on whether the parent node belongs to a library or a newsgroup service,
   * the corresponding aspect is added to the reply. When {@code notify} is {@code true} a
   * content-notify aspect is also applied so that subscribers are notified.
   *
   * @param node the newly created reply node
   * @param parentId the identifier of the parent topic node
   * @param notify whether notifications should be triggered for the reply
   */
  private void applyAspects(Node node, String parentId, boolean notify) {
    NodeRef nodeRef = Converter.createNodeRefFromId(node.getId());
    NodeRef parentNodeRef = Converter.createNodeRefFromId(parentId);

    if (aspectManager.isLibraryNode(parentNodeRef)) {
      aspectManager.addLibraryAspect(nodeRef);
    } else if (aspectManager.isNewsgroupNode(parentNodeRef)) {
      aspectManager.addNewsgroupAspect(nodeRef);
    }

    if (notify) {
      nodeService.addAspect(
        nodeRef,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY,
        null
      );
    }
  }

  /**
   * Logs the given error and populates the response status with an error code, message
   * and redirect flag.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set
   * @param message the human-readable error message
   * @param e the exception that caused the error
   * @return {@code null}, signalling to the framework that the response should be
   *         rendered from the error status rather than a model
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    if (logger.isErrorEnabled()) {
      logger.error(message + " when posting topic reply", e);
    }
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }

  /**
   * Simple mutable holder for the data extracted from the multipart form: the reply body,
   * the file attachments to add and the links to add.
   */
  private static class ReplyData {

    /** The parsed reply body/comment; {@code null} until a {@code comment} field is processed. */
    Comment body = null;

    /** The file attachments to add to the reply. */
    List<FileAttachmentData> filesToAdd = new ArrayList<>();

    /** The links to add to the reply. */
    List<String> linksToAdd = new ArrayList<>();
  }
}
