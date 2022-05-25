package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UserNewsFeed
 */
public class UserNewsFeed {

    private WhenEnum when = null;
    private Long uploads = null;
    private Long updates = null;
    private Long comments = null;
    private List<InterestGroupFeed> groupFeeds = new ArrayList<>();

    /**
     * Get when
     *
     * @return when
     */
    public WhenEnum getWhen() {
        return when;
    }

    public void setWhen(WhenEnum when) {
        this.when = when;
    }

    /**
     * Get uploads
     *
     * @return uploads
     */
    public Long getUploads() {
        return uploads;
    }

    public void setUploads(Long uploads) {
        this.uploads = uploads;
    }

    /**
     * Get updates
     *
     * @return updates
     */
    public Long getUpdates() {
        return updates;
    }

    public void setUpdates(Long updates) {
        this.updates = updates;
    }

    /**
     * Get comments
     *
     * @return comments
     */
    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    /**
     * Get groupFeeds
     *
     * @return groupFeeds
     */
    public List<InterestGroupFeed> getGroupFeeds() {
        return groupFeeds;
    }

    public void setGroupFeeds(List<InterestGroupFeed> groupFeeds) {
        this.groupFeeds = groupFeeds;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserNewsFeed userNewsFeed = (UserNewsFeed) o;
        return Objects.equals(this.when, userNewsFeed.when)
                && Objects.equals(this.uploads, userNewsFeed.uploads)
                && Objects.equals(this.updates, userNewsFeed.updates)
                && Objects.equals(this.comments, userNewsFeed.comments)
                && Objects.equals(this.groupFeeds, userNewsFeed.groupFeeds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(when, uploads, updates, comments, groupFeeds);
    }

    @Override
    public String toString() {

        return "class UserNewsFeed {\n"
                + "    when: "
                + toIndentedString(when)
                + "\n"
                + "    uploads: "
                + toIndentedString(uploads)
                + "\n"
                + "    updates: "
                + toIndentedString(updates)
                + "\n"
                + "    comments: "
                + toIndentedString(comments)
                + "\n"
                + "    groupFeeds: "
                + toIndentedString(groupFeeds)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    /**
     * Gets or Sets when
     */
    public enum WhenEnum {
        TODAY("today"),

        WEEK("week"),

        PREVIOUSWEEK("previousWeek");

        private String value;

        WhenEnum(String value) {
            this.value = value;
        }

        public static WhenEnum fromValue(String text) {
            for (WhenEnum b : WhenEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
