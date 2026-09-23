/**
 *
 */
package io.swagger.api;

import io.swagger.model.Attachement;
import io.swagger.model.Node;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a newsgroup/forum post node.
 *
 * <p>Extends the generic {@link Node} with the list of file attachments associated with the post,
 * allowing a post to carry one or more {@link Attachement} items in addition to the standard node
 * metadata inherited from {@link Node}.
 *
 * @author schwerr
 */
public class PostNode extends Node {

  /** The attachments associated with this post; {@code null} when none have been set. */
  List<Attachement> attachments = null;

  /**
   * Returns the attachments associated with this post.
   *
   * @return the list of attachments, or {@code null} if none have been set
   */
  public List<Attachement> getAttachments() {
    return attachments;
  }

  /**
   * Sets the attachments associated with this post.
   *
   * @param attachments the list of attachments to set
   */
  public void setAttachments(List<Attachement> attachments) {
    this.attachments = attachments;
  }

  /**
   * Indicates whether some other object is "equal to" this post node.
   *
   * <p>Two {@code PostNode} instances are equal when the inherited {@link Node} state is equal and
   * their attachment lists are equal.
   *
   * @param o the reference object with which to compare
   * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PostNode postNode = (PostNode) o;
    return Objects.equals(attachments, postNode.attachments);
  }

  /**
   * Returns a hash code value for this post node, consistent with {@link #equals(Object)}.
   *
   * @return a hash code derived from the inherited {@link Node} state and the attachments
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), attachments);
  }
}
