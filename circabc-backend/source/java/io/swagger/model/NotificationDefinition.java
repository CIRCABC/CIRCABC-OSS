package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * the representation of all the notification configuration of one node
 */
public class NotificationDefinition {

    private List<NotificationDefinitionProfiles> profiles = new ArrayList<>();

    private List<NotificationDefinitionUsers> users = new ArrayList<>();

    /**
     * Get profiles
     *
     * @return profiles
     */
    public List<NotificationDefinitionProfiles> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<NotificationDefinitionProfiles> profiles) {
        this.profiles = profiles;
    }

    /**
     * Get users
     *
     * @return users
     */
    public List<NotificationDefinitionUsers> getUsers() {
        return users;
    }

    public void setUsers(List<NotificationDefinitionUsers> users) {
        this.users = users;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationDefinition notificationDefinition = (NotificationDefinition) o;
        return Objects.equals(this.profiles, notificationDefinition.profiles)
                && Objects.equals(this.users, notificationDefinition.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profiles, users);
    }

    @Override
    public String toString() {

        return "class NotificationDefinition {\n"
                + "    profiles: "
                + toIndentedString(profiles)
                + "\n"
                + "    users: "
                + toIndentedString(users)
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
