package io.swagger.api;

import io.swagger.model.AbuseReport;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import java.util.List;

/**
 * Business operations for the Newsgroup/Forum service of an Interest Group.
 *
 * <p>This interface defines the forum-related operations exposed by the CIRCABC REST layer. It
 * covers browsing forum content, managing sub-forums and topics, moderating posts, and handling
 * abuse reports. Implementations translate these high-level operations into Alfresco repository
 * actions and are invoked by the corresponding webscript endpoint classes.
 *
 * <p>Throughout this API a forum, sub-forum, topic or post is identified by the string {@code id}
 * of its underlying Alfresco node.
 *
 * @author beaurpi
 */
public interface ForumsApi {
  /**
   * Gets the content of a forum, i.e. the list of sub-forums, topics or any other child of the
   * given node.
   *
   * <p>Mapped URL: {@code /forums/{id}/content}.
   *
   * @param id the identifier of the forum node whose content is requested
   * @return the list of child {@link Node}s contained in the forum
   */
  List<Node> getForumById(String id);

  /**
   * Creates a new sub-forum within an existing forum.
   *
   * <p>Mapped URL: {@code /forums/{id}/subforums}.
   *
   * @param id the identifier of the parent forum node
   * @param body the {@link Node} describing the sub-forum to create
   * @return the newly created sub-forum {@link Node}
   */
  Node forumsIdSubforumsPost(String id, Node body);

  /**
   * Gets the list of sub-forums of a given forum.
   *
   * @param id the identifier of the parent forum node
   * @return the list of sub-forum {@link Node}s
   */
  List<Node> forumsIdSubforumsGet(String id);

  /**
   * Gets the list of sub-forums of a given forum, applying the requested sort order.
   *
   * @param id the identifier of the parent forum node
   * @param sorting the sort criteria to apply to the returned sub-forums
   * @return the sorted list of sub-forum {@link Node}s
   */
  List<Node> forumsIdSubforumsGet(String id, String sorting);

  /**
   * Creates a new empty topic within a forum. This does not create any post inside the topic.
   *
   * <p>Mapped URL: {@code /forums/{id}/content}.
   *
   * @param id the identifier of the forum node in which the topic is created
   * @param body the {@link Node} describing the topic to create
   * @return the newly created topic {@link Node}
   */
  Node forumsIdContentPost(String id, Node body);

  /**
   * Deletes a forum together with all of its topics and posts.
   *
   * @param id the identifier of the forum node to delete
   */
  void forumsIdDelete(String id);

  /**
   * Updates the metadata of an existing forum.
   *
   * @param id the identifier of the forum node to update
   * @param forumNode the {@link Node} carrying the updated forum data
   */
  void updateForum(String id, Node forumNode);

  /**
   * Toggles moderation on a forum.
   *
   * <p>The {@code acceptAll} flag is only considered when moderation is being disabled: it
   * determines whether the posts still pending verification should all be accepted or all be
   * rejected.
   *
   * @param id the identifier of the forum node
   * @param enable {@code true} to enable moderation, {@code false} to disable it
   * @param acceptAll when disabling moderation, {@code true} to accept all pending posts,
   *     {@code false} to reject them
   */
  void toggleModeration(String id, boolean enable, boolean acceptAll);

  /**
   * Verifies a moderated post, accepting or rejecting it.
   *
   * @param id the identifier of the post node to verify
   * @param approve {@code true} to accept the post, {@code false} to reject it
   * @param rejectReason an optional reason provided when the post is rejected
   */
  void verifyPost(String id, boolean approve, String rejectReason);

  /**
   * Gets the abuse reports that have been signaled for a given node.
   *
   * @param id the identifier of the node whose signaled abuses are requested
   * @return the list of {@link AbuseReport}s associated with the node
   */
  List<AbuseReport> getSignaledAbuses(String id);

  /**
   * Signals an abuse on a given node.
   *
   * @param id the identifier of the node being reported
   * @param abuseText the text describing the reported abuse
   */
  void signalAbuse(String id, String abuseText);

  /**
   * Removes all signaled abuses from a given node.
   *
   * @param id the identifier of the node whose signaled abuses are cleared
   */
  void removeAbuses(String id);

  /**
   * Gets the content of a forum as a paged result.
   *
   * @param id the identifier of the forum node whose content is requested
   * @param nbPage the page number to retrieve (page index)
   * @param nbLimit the maximum number of nodes to return per page
   * @param sort the sort criteria to apply to the returned content
   * @return a {@link PagedNodes} holding the requested page of child nodes and paging metadata
   */
  PagedNodes getForumById(String id, int nbPage, int nbLimit, String sort);
}
