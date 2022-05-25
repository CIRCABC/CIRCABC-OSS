package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * InterestGroupFeed
 */
public class InterestGroupFeed {

    private String id = null;

    private String name = null;

    private I18nProperty title = null;

    private List<UserActionLog> feed = new ArrayList<>();

    /**
     * Get id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get title
     *
     * @return title
     */
    public I18nProperty getTitle() {
        return title;
    }

    public void setTitle(I18nProperty title) {
        this.title = title;
    }

    /**
     * Get feed
     *
     * @return feed
     */
    public List<UserActionLog> getFeed() {
        return feed;
    }

    public void setFeed(List<UserActionLog> feed) {
        this.feed = feed;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterestGroupFeed interestGroupFeed = (InterestGroupFeed) o;
        return Objects.equals(this.id, interestGroupFeed.id)
                && Objects.equals(this.name, interestGroupFeed.name)
                && Objects.equals(this.title, interestGroupFeed.title)
                && Objects.equals(this.feed, interestGroupFeed.feed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title, feed);
    }

    @Override
    public String toString() {

        return "class InterestGroupFeed {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    feed: "
                + toIndentedString(feed)
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
