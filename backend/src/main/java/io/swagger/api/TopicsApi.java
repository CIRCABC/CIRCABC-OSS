package io.swagger.api;

import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Attachement;
import io.swagger.model.Comment;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Business API for managing newsgroup/forum topics, their posts (replies) and
 * post attachments within the CIRCABC repository.
 *
 * <p>Implementations of this interface contain the logic backing the REST
 * webscript endpoints exposed under the {@code /topics/*} and {@code /posts/*}
 * URL spaces. A topic is the root discussion node; posts are the replies that
 * belong to a topic, and each post may carry file and link attachments.</p>
 *
 * @author beaurpi
 */
public interface TopicsApi {
  /**
   * Returns all replies (posts) of the given topic.
   *
   * <p>Backs the {@code GET /topics/{id}/replies} endpoint.</p>
   *
   * @param id the identifier of the topic whose replies are requested
   * @return the list of reply nodes belonging to the topic
   */
  List<Node> getTopicReplies(String id);

  /**
   * Returns a paged subset of the replies (posts) of the given topic.
   *
   * <p>Backs the paginated {@code GET /topics/{id}/replies} endpoint.</p>
   *
   * @param id the identifier of the topic whose replies are requested
   * @param nbPage the page number to retrieve (1-based)
   * @param nbLimit the maximum number of replies per page
   * @param sort the sort criterion to apply to the replies
   * @return a paged container holding the requested slice of reply nodes
   */
  PagedNodes getTopicReplies(
    String id,
    Integer nbPage,
    Integer nbLimit,
    String sort
  );

  /**
   * Creates a new reply (post) in the given topic, optionally attaching files
   * and links.
   *
   * <p>Backs the {@code POST /topics/{id}/replies} endpoint.</p>
   *
   * @param id the identifier of the topic to reply to
   * @param body the comment payload holding the reply content
   * @param filesToAdd the file attachments to add to the new reply
   * @param linksToAdd the identifiers of nodes to attach as link attachments
   * @return the created reply node
   * @throws InvalidTopicException if the target topic is invalid or the reply
   *     cannot be created
   */
  Node topicsIdRepliesPost(
    String id,
    Comment body,
    List<FileAttachmentData> filesToAdd,
    List<String> linksToAdd
  ) throws InvalidTopicException;

  /**
   * Deletes a single post.
   *
   * <p>Backs the {@code DELETE /posts/{id}} endpoint.</p>
   *
   * @param id the identifier of the post to remove
   */
  void postsIdDelete(String id);

  /**
   * Updates the content of an existing post, optionally adding new file and
   * link attachments and removing existing attachments.
   *
   * <p>Backs the {@code PUT /posts/{id}} endpoint.</p>
   *
   * @param id the identifier of the post to update
   * @param body the node payload carrying the updated post content
   * @param filesToAdd the file attachments to add to the post
   * @param linksToAdd the identifiers of nodes to attach as link attachments
   * @param attachmentsToDelete the identifiers of existing attachments to remove
   * @return the updated post node
   */
  Node postsIdPut(
    String id,
    Node body,
    List<FileAttachmentData> filesToAdd,
    List<String> linksToAdd,
    List<String> attachmentsToDelete
  );

  /**
   * Returns the list of attachments of the given post.
   *
   * @param id the identifier of the post whose attachments are requested
   * @return the list of attachments belonging to the post
   */
  List<Attachement> getAttachments(String id);

  /**
   * Adds a file attachment to the given post from a {@link File} on disk.
   *
   * @param id the identifier of the post to attach the file to
   * @param name the display name of the attachment
   * @param file the file whose contents become the attachment
   */
  void addFileAttachment(String id, String name, File file);

  /**
   * Adds a file attachment to the given post from the contents of an
   * {@link InputStream}.
   *
   * @param id the identifier of the post to attach the file to
   * @param name the display name of the attachment
   * @param inputStream the stream whose contents become the attachment
   */
  void addFileAttachment(String id, String name, InputStream inputStream);

  /**
   * Adds a link attachment (a reference to another space/node) to the given
   * post.
   *
   * @param id the identifier of the post to attach the link to
   * @param destinationId the identifier of the target node the link points to
   */
  void addLinkAttachment(String id, String destinationId);

  /**
   * Removes the given attachment from the given post.
   *
   * @param id the identifier of the post owning the attachment
   * @param attachmentId the identifier of the attachment to remove
   */
  void removeAttachment(String id, String attachmentId);

  /**
   * Writes the content of the given attachment to the provided output stream.
   *
   * @param attachmentId the identifier of the attachment to read
   * @param outputStream the stream the attachment content is written to
   */
  void getAttachment(String attachmentId, OutputStream outputStream);

  /**
   * Returns the remaining size, in bytes, available for attachments of the
   * given post (the quota not yet consumed by its existing attachments).
   *
   * @param id the identifier of the post
   * @return the remaining attachment size in bytes
   */
  long getAttachmentsRemainingSize(String id);

  /**
   * Deletes a topic together with all of its posts.
   *
   * <p>Backs the {@code DELETE /topics/{id}} endpoint.</p>
   *
   * @param id the identifier of the topic to remove
   */
  void topicsIdDelete(String id);

  /**
   * Updates an existing topic with the provided node data.
   *
   * @param id the identifier of the topic to update
   * @param topicNode the node payload carrying the updated topic data
   */
  void updateTopic(String id, Node topicNode);
}
