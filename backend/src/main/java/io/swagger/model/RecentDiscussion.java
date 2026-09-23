/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Domain model representing a recent discussion in a Newsgroup service.
 *
 * <p>A recent discussion pairs a discussion {@link Node topic} with the most recent {@link Node
 * post} belonging to that topic. Instances are typically serialized as part of REST responses that
 * surface recently active discussions to the frontend.
 *
 * @author beaurpi
 */
public class RecentDiscussion {

  /** The most recent post (message) belonging to the associated topic. */
  private Node post;
  /** The discussion topic that the recent post belongs to. */
  private Node topic;

  /**
   * Returns the discussion topic.
   *
   * @return the topic node, or {@code null} if not set
   */
  public Node getTopic() {
    return topic;
  }

  /**
   * Sets the discussion topic.
   *
   * @param topic the topic node to associate with this recent discussion
   */
  public void setTopic(Node topic) {
    this.topic = topic;
  }

  /**
   * Returns the most recent post of the discussion.
   *
   * @return the post node, or {@code null} if not set
   */
  public Node getPost() {
    return post;
  }

  /**
   * Sets the most recent post of the discussion.
   *
   * @param post the post node to associate with this recent discussion
   */
  public void setPost(Node post) {
    this.post = post;
  }

  /**
   * Compares this recent discussion to another object for equality.
   *
   * <p>Two instances are considered equal when both their {@code post} and {@code topic} fields are
   * equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code RecentDiscussion}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecentDiscussion recentDiscussion = (RecentDiscussion) o;
    return (
      Objects.equals(this.post, recentDiscussion.post) &&
      Objects.equals(this.topic, recentDiscussion.topic)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the {@code post} and
   * {@code topic} fields.
   *
   * @return the hash code for this recent discussion
   */
  @Override
  public int hashCode() {
    return Objects.hash(post, topic);
  }

  /**
   * Returns a human-readable string representation of this recent discussion, including its post
   * and topic.
   *
   * @return a string describing this recent discussion
   */
  @Override
  public String toString() {
    return (
      "class RecentDiscussion {\n" +
      "    post: " +
      toIndentedString(post) +
      "\n" +
      "    topic: " +
      toIndentedString(topic) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
