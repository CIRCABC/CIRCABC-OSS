package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.attachment.AttachmentService;
import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetails;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetailsService;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Attachement;
import io.swagger.model.Comment;
import io.swagger.model.I18nProperty;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.RestInputSanitizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link TopicsApi} providing the business logic for
 * forum topics and their replies (posts) within the CIRCABC Newsgroup and
 * Library services.
 *
 * <p>This service operates on the underlying Alfresco repository and covers the
 * full lifecycle of topics and posts, including:
 * <ul>
 *   <li>retrieving the replies (posts) of a topic, optionally paginated;</li>
 *   <li>creating new replies under a topic and updating existing posts;</li>
 *   <li>deleting topics and posts;</li>
 *   <li>updating topic metadata (name, title, description, security ranking
 *       and expiration date);</li>
 *   <li>managing post attachments (file uploads, links) including size
 *       validation and streaming of attachment content.</li>
 * </ul>
 *
 * <p>Alfresco collaborators (node, content, file-folder and person services)
 * along with CIRCABC-specific services (moderation, attachments, user details)
 * are injected through Spring. Moderated forums cause updated content to be
 * resubmitted for approval automatically.
 *
 * @author beaurpi
 */
public class TopicsApiImpl implements TopicsApi {

  /** Property key holding the creator user name of a post. */
  private static final String CREATOR = "creator";
  /** Property key holding the HTML message body of a post. */
  private static final String MESSAGE = "message";
  /** Property key holding the avatar node id of the post creator. */
  private static final String AVATAR = "avatar";
  /** Property key flagging whether a post is pending moderation approval. */
  private static final String WAITING_FOR_APPROVAL = "waitingForApproval";

  private final Log logger = LogFactory.getLog(TopicsApiImpl.class);

  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private PersonService personService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private ModerationService moderationService;

  @Autowired
  private AttachmentService attachmentService;

  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Creates a file name for the message being posted
   *
   * @return The file name for the post
   */
  private static String createPostFileName() {
    // add a timestamp
    // add Universal Unique Identifier
    // fix bugs ETWOONE-196 and ETWOONE-203

    // add the HTML file extension
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

    return (
      "posted-" +
      dateFormat.format(new Date()) +
      "-" +
      UUID.randomUUID() +
      ".html"
    );
  }

  /**
   * Returns all replies (posts) of the given topic without pagination.
   *
   * <p>Only children of type {@code fm:post} are returned, and only when the
   * supplied node carries the Newsgroup or Library aspect. If neither aspect is
   * present an empty list is returned.
   *
   * @param id the identifier of the topic node whose replies are requested
   * @return the list of {@link Node} replies, never {@code null}
   * @see io.swagger.api.TopicsApi#getTopicReplies(java.lang.String)
   */
  @Override
  public List<Node> getTopicReplies(String id) {
    NodeRef topicRef = Converter.createNodeRefFromId(id);
    List<Node> result = new ArrayList<>();

    if (
      secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_NEWSGROUP) ||
      secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      List<ChildAssociationRef> children = secureNodeService.getChildAssocs(
        topicRef
      );

      for (ChildAssociationRef item : children) {
        if (
          secureNodeService
            .getType(item.getChildRef())
            .equals(ForumModel.TYPE_POST)
        ) {
          final NodeRef childRef = item.getChildRef();
          Node postNode = getPost(childRef);
          result.add(postNode);
        }
      }
    }

    return result;
  }

  /**
   * Builds the {@link Node} representation of a single post, enriching it with
   * the resolved HTML message body, the creator's avatar, the moderation
   * approval flag and its attachments.
   *
   * <p>Attachments are collected with the multilingual property interceptor
   * disabled so that language-neutral values are returned, and the previous
   * ML-aware state is restored afterwards. Failure to resolve the creator's
   * user details is logged but does not abort the operation.
   *
   * @param childRef the node reference of the post to represent
   * @return the populated post {@link Node}
   */
  private Node getPost(final NodeRef childRef) {
    PostNode postNode = (PostNode) nodesApi.getNode(childRef, new PostNode());
    postNode.getProperties().put(MESSAGE, resolvePostContent(childRef));
    String creator = postNode.getProperties().get(CREATOR);
    try {
      final NodeRef personNodeRef = personService.getPerson(creator);
      final UserDetails userDetails = userDetailsService.getUserDetails(
        personNodeRef
      );
      postNode.getProperties().put(AVATAR, userDetails.getAvatar().getId());
    } catch (Exception e) {
      logger.error("Error while getting user details for :" + creator, e);
    }
    postNode
      .getProperties()
      .put(
        WAITING_FOR_APPROVAL,
        moderationService.isWaitingForApproval(childRef) ? "true" : "false"
      );
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    try {
      MLPropertyInterceptor.setMLAware(false);
      List<Attachement> attachments = getAttachments(childRef.getId());
      postNode.setAttachments(attachments);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return postNode;
  }

  /**
   * Reads the HTML content of a post from the repository content store.
   *
   * @param childRef the node reference of the post
   * @return the post content as a string, or an empty string when the node has
   *         no readable content
   */
  private String resolvePostContent(NodeRef childRef) {
    String result = "";
    if (contentService.getReader(childRef, ContentModel.PROP_CONTENT) != null) {
      result = contentService
        .getReader(childRef, ContentModel.PROP_CONTENT)
        .getContentString();
    }
    return result;
  }

  /**
   * Creates a new reply (post) under the given topic.
   *
   * <p>The call runs as the system user to create the {@code fm:post} node, sets
   * it as inline-editable, writes the supplied comment text as UTF-8 HTML
   * content, and then attaches any provided files and links. The original user
   * context is always restored afterwards.
   *
   * @param id the identifier of the parent topic node
   * @param body the comment whose text becomes the post content
   * @param filesToAdd the file attachments to add to the new post
   * @param linksToAdd the identifiers of nodes to link as attachments
   * @return the created post as a {@link Node}, or {@code null} if creation
   *         yielded no node reference
   * @throws InvalidTopicException if the given id does not reference a topic
   *         node
   */
  @Override
  public Node topicsIdRepliesPost(
    String id,
    Comment body,
    List<FileAttachmentData> filesToAdd,
    List<String> linksToAdd
  ) throws InvalidTopicException {
    NodeRef topicRef = Converter.createNodeRefFromId(id);

    String username = AuthenticationUtil.getRunAsUser();
    AuthenticationUtil.setRunAsUserSystem();

    if (!secureNodeService.getType(topicRef).equals(ForumModel.TYPE_TOPIC)) {
      throw new InvalidTopicException();
    }

    NodeRef postNodeRef;

    try {
      FileInfo postFile = fileFolderService.create(
        topicRef,
        createPostFileName(),
        ForumModel.TYPE_POST
      );
      postNodeRef = postFile.getNodeRef();

      Map<QName, Serializable> editProps = new HashMap<>(1, 1.0f);
      editProps.put(ApplicationModel.PROP_EDITINLINE, true);
      secureNodeService.addAspect(
        postNodeRef,
        ApplicationModel.ASPECT_INLINEEDITABLE,
        editProps
      );

      final ContentWriter writer = contentService.getWriter(
        postNodeRef,
        ContentModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
      writer.setEncoding("UTF-8");
      writer.putContent(RestInputSanitizer.sanitizeRichText(body.getText()));

      // attach files
      for (FileAttachmentData fileToAdd : filesToAdd) {
        addFileAttachment(
          postNodeRef.getId(),
          fileToAdd.getName(),
          fileToAdd.getInputStream()
        );
      }

      // attach links
      for (String linkToAdd : linksToAdd) {
        addLinkAttachment(postNodeRef.getId(), linkToAdd);
      }
    } finally {
      AuthenticationUtil.setRunAsUser(username);
    }

    if (postNodeRef != null) {
      return nodesApi.getNode(postNodeRef);
    } else {
      return null;
    }
  }

  /**
   * Returns the injected file-folder service collaborator.
   *
   * @return the fileFolderService
   */
  public FileFolderService getFileFolderService() {
    return fileFolderService;
  }

  /**
   * Sets the file-folder service collaborator.
   *
   * @param fileFolderService the fileFolderService to set
   */
  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Deletes a post.
   *
   * <p>The node is only removed when it is of type {@code fm:post}; otherwise the
   * call is a no-op.
   *
   * @param id the identifier of the post node to delete
   */
  @Override
  public void postsIdDelete(String id) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    if (secureNodeService.getType(postRef).equals(ForumModel.TYPE_POST)) {
      secureNodeService.deleteNode(postRef);
    }
  }

  /**
   * Updates an existing post: its HTML content and its attachments.
   *
   * <p>When the node is of type {@code fm:post} its content is overwritten with
   * the supplied message (UTF-8 HTML). If the enclosing forum is moderated the
   * post is resubmitted for approval. The requested attachments are then
   * deleted, added (files) and linked (nodes) in that order.
   *
   * @param id the identifier of the post node to update
   * @param body the node whose {@code message} property holds the new content
   * @param filesToAdd the file attachments to add
   * @param linksToAdd the identifiers of nodes to link as attachments
   * @param attachmentsToDelete the identifiers of existing attachments to remove
   * @return the updated post as a {@link Node}
   * @see io.swagger.api.TopicsApi#postsIdPut(java.lang.String,
   *      io.swagger.model.Node,
   *      java.util.List, java.util.List, java.util.List)
   */
  @Override
  public Node postsIdPut(
    String id,
    Node body,
    List<FileAttachmentData> filesToAdd,
    List<String> linksToAdd,
    List<String> attachmentsToDelete
  ) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    if (secureNodeService.getType(postRef).equals(ForumModel.TYPE_POST)) {
      final ContentWriter writer = contentService.getWriter(
        postRef,
        ContentModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
      writer.setEncoding("UTF-8");
      writer.putContent(
        RestInputSanitizer.sanitizeRichText(body.getProperties().get(MESSAGE))
      );
    }

    if (moderationService.isContainerModerated(postRef)) {
      // in case this forum is moderated, when content is updated, resubmit for
      // approval
      moderationService.waitForApproval(postRef);
    }

    // delete attachments
    for (String attachmentToDelete : attachmentsToDelete) {
      removeAttachment(id, attachmentToDelete);
    }

    // attach files
    for (FileAttachmentData fileToAdd : filesToAdd) {
      addFileAttachment(id, fileToAdd.getName(), fileToAdd.getInputStream());
    }

    // attach links
    for (String linkToAdd : linksToAdd) {
      addLinkAttachment(id, linkToAdd);
    }

    return getPost(postRef);
  }

  /**
   * Deletes a topic and, by cascade, all of its replies.
   *
   * @param id the identifier of the topic node to delete
   */
  @Override
  public void topicsIdDelete(String id) {
    NodeRef topicRef = Converter.createNodeRefFromId(id);

    secureNodeService.deleteNode(topicRef);
  }

  /**
   * Returns a paginated page of replies (posts) for the given topic.
   *
   * <p>Replies are only returned when the topic carries the Newsgroup or Library
   * aspect. When {@code nbLimit} is {@code -1} pagination is disabled and all
   * replies are returned; otherwise the requested page is fetched from the
   * file-folder service. Only children of type {@code fm:post} are included, and
   * the total count reflects all children of the topic.
   *
   * @param id the identifier of the topic node
   * @param nbPage the zero-based page index
   * @param nbLimit the page size, or {@code -1} to disable pagination
   * @param sort the requested sort order (currently unused)
   * @return a {@link PagedNodes} holding the page of replies and the total count
   */
  @Override
  public PagedNodes getTopicReplies(
    String id,
    Integer nbPage,
    Integer nbLimit,
    String sort
  ) {
    PagedNodes pagedResult = new PagedNodes();

    NodeRef topicRef = Converter.createNodeRefFromId(id);

    List<Node> result = new ArrayList<>();

    if (
      secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_NEWSGROUP) ||
      secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      PagingRequest pr = new PagingRequest(nbPage * nbLimit, nbLimit);

      if (nbLimit != -1) {
        final PagingResults<FileInfo> list = getFileFolderService().list(
          topicRef,
          true,
          false,
          new HashSet<>(),
          null,
          pr
        );

        for (FileInfo item : list.getPage()) {
          if (
            secureNodeService
              .getType(item.getNodeRef())
              .equals(ForumModel.TYPE_POST)
          ) {
            final NodeRef childRef = item.getNodeRef();
            Node postNode = getPost(childRef);
            result.add(postNode);
          }
        }
      } else {
        result = getTopicReplies(id);
      }

      pagedResult.setTotal((long) getFileFolderService().list(topicRef).size());
      pagedResult.setData(result);
    }

    return pagedResult;
  }

  /**
   * Updates the metadata of a topic.
   *
   * <p>Only the topic name, title, description, security ranking and expiration
   * date are updated; any other properties on the supplied node are ignored.
   * Name update failures are logged without aborting the operation. An empty or
   * missing expiration date clears the property.
   *
   * @param id the identifier of the topic node to update
   * @param topicNode the node carrying the new metadata values
   * @throws IllegalArgumentException if the security ranking is not one of the
   *         allowed values, or if the expiration date is not in
   *         {@code yyyy-MM-dd} format
   * @see io.swagger.api.TopicsApi#updateTopic(java.lang.String,
   *      io.swagger.model.Node)
   */
  @Override
  public void updateTopic(String id, Node topicNode) {
    NodeRef topicRef = getTopicNodeRef(id);

    try {
      if (topicNode.getName() != null) {
        secureNodeService.setProperty(
          topicRef,
          ContentModel.PROP_NAME,
          topicNode.getName()
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Can't update name for topic: " + id, e);
      }
    }

    // for a topic only update these properties and ignore the rest
    I18nProperty title = topicNode.getTitle();
    if (title != null) {
      secureNodeService.setProperty(
        topicRef,
        ContentModel.PROP_TITLE,
        Converter.toMLText(title)
      );
    }

    I18nProperty description = topicNode.getDescription();
    if (description != null) {
      secureNodeService.setProperty(
        topicRef,
        ContentModel.PROP_DESCRIPTION,
        Converter.toMLText(description)
      );
    }
    String securityRanking = topicNode.getProperties().get("security_ranking");

    if (securityRanking != null && !securityRanking.isEmpty()) {
      if (!DocumentModel.SECURITY_RANKINGS.contains(securityRanking)) {
        throw new IllegalArgumentException(
          "The 'security_ranking' is invalid:" + securityRanking
        );
      }

      secureNodeService.setProperty(
        topicRef,
        DocumentModel.PROP_SECURITY_RANKING,
        securityRanking
      );
    }

    String expirationDateString = topicNode
      .getProperties()
      .get("expiration_date");

    if (expirationDateString == null || expirationDateString.isEmpty()) {
      secureNodeService.setProperty(
        topicRef,
        DocumentModel.PROP_EXPIRATION_DATE,
        null
      );
      return;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Date expirationDate;

    try {
      expirationDate = simpleDateFormat.parse(expirationDateString);
      secureNodeService.setProperty(
        topicRef,
        DocumentModel.PROP_EXPIRATION_DATE,
        expirationDate
      );
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "The 'expiration_date' has a wrong format. Must be yyyy-MM-dd",
        e
      );
    }
  }

  /**
   * Resolves and validates the node reference of a topic.
   *
   * @param id the identifier of the topic node
   * @return the resolved {@link NodeRef}
   * @throws IllegalArgumentException if no node exists for the given id
   * @throws InvalidTypeException if the node is not of type {@code fm:topic}
   */
  private NodeRef getTopicNodeRef(String id) {
    NodeRef topicRef = Converter.createNodeRefFromId(id);

    if (!secureNodeService.exists(topicRef)) {
      throw new IllegalArgumentException(
        "The topic with id " + id + " could not be found."
      );
    }

    if (!ForumModel.TYPE_TOPIC.equals(secureNodeService.getType(topicRef))) {
      throw new InvalidTypeException(
        "The given id does not correspond to a node of type topic",
        ForumModel.TYPE_TOPIC
      );
    }

    return topicRef;
  }

  /**
   * Returns the attachments of a post.
   *
   * <p>For hidden-file attachments the size, encoding and mimetype are populated
   * from the underlying content reader.
   *
   * @param id the identifier of the post node
   * @return the list of {@link Attachement} of the post
   * @see io.swagger.api.TopicsApi#getAttachments(java.lang.String)
   */
  @Override
  public List<Attachement> getAttachments(String id) {
    NodeRef postNodeRef = Converter.createNodeRefFromId(id);

    List<Attachement> attachments = attachmentService.getAttachements(
      postNodeRef
    );

    ContentReader reader;

    for (Attachement attachment : attachments) {
      if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
        reader = contentService.getReader(
          attachment.getNodeRef(),
          DocumentModel.PROP_CONTENT
        );
        attachment.setSize(reader.getSize());
        attachment.setEncoding(reader.getEncoding());
        attachment.setMimetype(reader.getMimetype());
      }
    }

    return attachments;
  }

  /**
   * Adds a file attachment to a post from a {@link File}.
   *
   * @param id the identifier of the post node
   * @param name the file name of the attachment
   * @param file the file to attach
   * @see io.swagger.api.TopicsApi#addFileAttachment(java.lang.String,
   *      java.lang.String,
   *      java.io.File)
   */
  @Override
  public void addFileAttachment(String id, String name, File file) {
    NodeRef postNodeRef = Converter.createNodeRefFromId(id);
    attachmentService.addAttachement(postNodeRef, name, file);
  }

  /**
   * Adds a file attachment to a post from an {@link InputStream}.
   *
   * <p>The file name is validated against the ESAPI allowed extensions before
   * the stream is spooled to a temporary file. The combined size of existing
   * and new attachments is validated against the configured per-post limit, and
   * the temporary file and input stream are always released afterwards.
   *
   * @param id the identifier of the post node
   * @param name the file name of the attachment
   * @param inputStream the stream providing the attachment content
   * @throws IllegalArgumentException if the file name / type is invalid or the
   *         total attachment size exceeds the allowed limit
   * @throws IllegalStateException if the temporary file cannot be created
   * @see io.swagger.api.TopicsApi#addFileAttachment(java.lang.String,
   *      java.lang.String,
   *      java.io.InputStream)
   */
  @Override
  public void addFileAttachment(
    String id,
    String name,
    InputStream inputStream
  ) {
    // esapi validation
    final List<String> allowedFileExtensions =
      ESAPI.securityConfiguration().getAllowedFileExtensions();

    try {
      ESAPI.validator().getValidFileName(
        "submitted file",
        name,
        allowedFileExtensions,
        false
      );
    } catch (ValidationException | IntrusionException vex) {
      throw new IllegalArgumentException("Invalid file type: " + name, vex);
    }

    NodeRef postNodeRef = Converter.createNodeRefFromId(id);
    File tempFile = null;

    try {
      tempFile = TempFileProvider.createTempFile("attachment", ".tmp");

      long attachmentTotalSize = Long.parseLong(
        circabcConfig.getPostsAllowedAttachmentSizeinBytes()
      );

      long totalBytesRead = ApiToolBox.inputStreamToFile(
        inputStream,
        tempFile,
        attachmentTotalSize
      );

      validateAttachmentSize(id, totalBytesRead, attachmentTotalSize);

      attachmentService.addAttachement(postNodeRef, name, tempFile);
    } catch (Exception e) {
      logger.error("Could not create temp file.", e);
      throw new IllegalStateException("Could not create temp file.", e);
    } finally {
      cleanupAttachmentResources(tempFile, inputStream);
    }
  }

  /**
   * Validates that adding a new attachment does not exceed the per-post size
   * limit, considering the sizes of existing hidden-file attachments.
   *
   * @param id the identifier of the post node
   * @param totalBytesRead the size in bytes of the attachment being added
   * @param attachmentTotalSize the maximum total attachment size allowed
   * @throws IllegalArgumentException if the combined size exceeds the limit
   */
  private void validateAttachmentSize(
    String id,
    long totalBytesRead,
    long attachmentTotalSize
  ) {
    long totalSize = totalBytesRead;
    for (Attachement attachment : getAttachments(id)) {
      if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
        totalSize += attachment.getSize();
      }
    }
    if (totalSize > attachmentTotalSize) {
      throw new IllegalArgumentException(
        "Size of attachments exceeds the allowed limit for this post: " +
          attachmentTotalSize +
          "bytes, Given: " +
          totalSize
      );
    }
  }

  /**
   * Releases the resources used while adding a streamed attachment by deleting
   * the temporary file and closing the input stream. Any failure is logged and
   * otherwise ignored.
   *
   * @param tempFile the temporary file to delete, may be {@code null}
   * @param inputStream the input stream to close, may be {@code null}
   */
  private void cleanupAttachmentResources(
    File tempFile,
    InputStream inputStream
  ) {
    if (tempFile != null && tempFile.exists()) {
      try {
        java.nio.file.Files.deleteIfExists(tempFile.toPath());
      } catch (java.io.IOException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Can not delete file" + tempFile.toString());
        }
      }
    }
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Can not close input stream", e);
        }
      }
    }
  }

  /**
   * Links an existing node to a post as an attachment.
   *
   * <p>A duplicate child-node-name conflict is logged and swallowed.
   *
   * @param id the identifier of the post node
   * @param destinationId the identifier of the node to link
   * @throws IllegalArgumentException if the node to link does not exist
   * @see io.swagger.api.TopicsApi#addLinkAttachment(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addLinkAttachment(String id, String destinationId) {
    NodeRef postNodeRef = Converter.createNodeRefFromId(id);
    NodeRef destinationNodeRef = Converter.createNodeRefFromId(destinationId);
    if (!secureNodeService.exists(destinationNodeRef)) {
      throw new IllegalArgumentException("Node to be linked does not exist.");
    }
    try {
      attachmentService.addAttachement(postNodeRef, destinationNodeRef);
    } catch (DuplicateChildNodeNameException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Duplicate child  node name ", e);
      }
    }
  }

  /**
   * Removes an attachment from a post.
   *
   * @param id the identifier of the post node
   * @param attachmentId the identifier of the attachment to remove
   * @see io.swagger.api.TopicsApi#removeAttachment(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void removeAttachment(String id, String attachmentId) {
    NodeRef postNodeRef = Converter.createNodeRefFromId(id);
    NodeRef attachmentNodeRef = Converter.createNodeRefFromId(attachmentId);
    attachmentService.removeAttachement(postNodeRef, attachmentNodeRef);
  }

  /**
   * Returns the remaining attachment capacity for a post, in bytes.
   *
   * <p>When the post does not yet exist the full configured limit is returned.
   * The result is never negative; it is clamped to zero once the limit is
   * reached.
   *
   * @param id the identifier of the post node
   * @return the number of bytes still available for attachments
   * @see io.swagger.api.TopicsApi#getAttachmentsRemainingSize(java.lang.String)
   */
  @Override
  public long getAttachmentsRemainingSize(String id) {
    NodeRef postNodeRef = Converter.createNodeRefFromId(id);

    long attachmentTotalSize = Long.parseLong(
      circabcConfig.getPostsAllowedAttachmentSizeinBytes()
    );

    if (!secureNodeService.exists(postNodeRef)) {
      return attachmentTotalSize;
    }

    List<Attachement> attachments = getAttachments(id);

    long totalSize = 0;

    for (Attachement attachment : attachments) {
      if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
        totalSize += attachment.getSize();
      }
    }

    long finalSize = attachmentTotalSize - totalSize;

    return finalSize <= 0 ? 0 : finalSize;
  }

  /**
   * Streams the content of a hidden-file attachment to the given output stream.
   *
   * @param attachmentId the identifier of the attachment node
   * @param outputStream the stream the attachment content is written to
   * @throws IllegalArgumentException if the node is not a hidden attachment
   *         content type
   * @see io.swagger.api.TopicsApi#getAttachment(java.lang.String,
   *      java.io.OutputStream)
   */
  @Override
  public void getAttachment(String attachmentId, OutputStream outputStream) {
    NodeRef nodeRef = Converter.createNodeRefFromId(attachmentId);

    final QName type = secureNodeService.getType(nodeRef);

    ContentReader reader;

    if (type.equals(DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT)) {
      reader = contentService.getReader(nodeRef, DocumentModel.PROP_CONTENT);
    } else {
      throw new IllegalArgumentException(
        "Attachment type is not hidden content."
      );
    }

    reader.getContent(outputStream);
  }
}
