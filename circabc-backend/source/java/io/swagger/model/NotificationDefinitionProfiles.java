package io.swagger.model;

import java.util.Objects;

/**
 * NotificationDefinitionProfiles
 */
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.SpringCodegen",
        date = "2017-04-06T14:37:04.823+02:00")
public class NotificationDefinitionProfiles {

    private Profile profile = null;

    private String notifications = null;

    private Boolean inherited = true;

    /**
     * Get profile
     *
     * @return profile
     */
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Get notification
     *
     * @return notification
     */
    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationDefinitionProfiles notificationDefinitionProfiles =
                (NotificationDefinitionProfiles) o;
        return Objects.equals(this.profile, notificationDefinitionProfiles.profile)
                && Objects.equals(this.notifications, notificationDefinitionProfiles.notifications)
                && Objects.equals(this.inherited, notificationDefinitionProfiles.inherited);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, notifications, inherited);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationDefinitionProfiles {\n");

        sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
        sb.append("    notification: ").append(toIndentedString(notifications)).append("\n");
        sb.append("    inherited: ").append(toIndentedString(inherited)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    /**
     * @return the inherited
     */
    public Boolean getInherited() {
        return inherited;
    }

    /**
     * @param inherited the inherited to set
     */
    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }
}
