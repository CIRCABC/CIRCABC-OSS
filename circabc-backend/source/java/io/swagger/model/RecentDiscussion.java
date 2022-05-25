/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class RecentDiscussion {

    private Node post;
    private Node topic;

    public Node getTopic() {
        return topic;
    }

    public void setTopic(Node topic) {
        this.topic = topic;
    }

    public Node getPost() {
        return post;
    }

    public void setPost(Node post) {
        this.post = post;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecentDiscussion recentDiscussion = (RecentDiscussion) o;
        return Objects.equals(this.post, recentDiscussion.post)
                && Objects.equals(this.topic, recentDiscussion.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, topic);
    }

    @Override
    public String toString() {

        return "class RecentDiscussion {\n"
                + "    post: "
                + toIndentedString(post)
                + "\n"
                + "    topic: "
                + toIndentedString(topic)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
