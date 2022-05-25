package io.swagger.api;

import eu.cec.digit.circabc.business.api.content.Attachement;
import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Comment;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author beaurpi
 */
public interface TopicsApi {

    /**
     * get the replies of a topic /topics/{id}/replies
     */
    List<Node> getTopicReplies(String id);

    PagedNodes getTopicReplies(String id, Integer nbPage, Integer nbLimit, String sort);

    /**
     * create a reply in a topic mapped url /topics/{id}/replies
     */
    Node topicsIdRepliesPost(
            String id, Comment body, List<FileAttachmentData> filesToAdd, List<String> linksToAdd)
            throws InvalidTopicException;

    /**
     * remove one post
     */
    void postsIdDelete(String id);

    /**
     * update the content of a post mapped url /posts/{id}
     */
    Node postsIdPut(
            String id,
            Node body,
            List<FileAttachmentData> filesToAdd,
            List<String> linksToAdd,
            List<String> attachmentsToDelete);

    /**
     * get the list of attachments from the post with the given id
     */
    List<Attachement> getAttachments(String id);

    /**
     * add a file attachment to the post with the given id
     */
    void addFileAttachment(String id, String name, File file);

    /**
     * attach the contents of an inputStream to the post with the given id
     */
    void addFileAttachment(String id, String name, InputStream inputStream);

    /**
     * add a link attachment (link to a space) to the post with the given id
     */
    void addLinkAttachment(String id, String destinationId);

    /**
     * removes the attachment with id attachmentId from the post with the given id
     */
    void removeAttachment(String id, String attachmentId);

    /**
     * gets the attachment content with the given attachmentId into the provided outputStream
     */
    void getAttachment(String attachmentId, OutputStream outputStream);

    /**
     * gets the remaining size in bytes of attachments of a post given its id
     */
    long getAttachmentsRemainingSize(String id);

    /**
     * delete one topic with all its posts
     */
    void topicsIdDelete(String id);

    /**
     * update a topic
     */
    void updateTopic(String id, Node topicNode);
}
