package io.swagger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * MembershipPostDefinition
 */
public class MembershipPostDefinition {

    private Boolean adminNotifications = null;

    private Boolean userNotifications = null;

    private String notifyText = null;

    private Date expirationDate = null;

    private List<UserProfile> memberships = new ArrayList<>();

    public MembershipPostDefinition adminNotifications(Boolean adminNotifications) {
        this.adminNotifications = adminNotifications;
        return this;
    }

    /**
     * @return the expirationDate
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate the expirationDate to set
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Get adminNotifications
     *
     * @return adminNotifications
     */
    public Boolean getAdminNotifications() {
        return adminNotifications;
    }

    public void setAdminNotifications(Boolean adminNotifications) {
        this.adminNotifications = adminNotifications;
    }

    public MembershipPostDefinition userNotifications(Boolean userNotifications) {
        this.userNotifications = userNotifications;
        return this;
    }

    /**
     * Get userNotifications
     *
     * @return userNotifications
     */
    public Boolean getUserNotifications() {
        return userNotifications;
    }

    public void setUserNotifications(Boolean userNotifications) {
        this.userNotifications = userNotifications;
    }

    public MembershipPostDefinition memberships(List<UserProfile> memberships) {
        this.memberships = memberships;
        return this;
    }

    public MembershipPostDefinition addMembershipsItem(UserProfile membershipsItem) {
        this.memberships.add(membershipsItem);
        return this;
    }

    public String getNotifyText() {
        return notifyText;
    }

    public void setNotifyText(String notifyText) {
        this.notifyText = notifyText;
    }

    /**
     * Get memberships
     *
     * @return memberships
     */
    public List<UserProfile> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<UserProfile> memberships) {
        this.memberships = memberships;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MembershipPostDefinition membershipPostDefinition = (MembershipPostDefinition) o;
        return Objects.equals(this.adminNotifications, membershipPostDefinition.adminNotifications)
                && Objects.equals(this.userNotifications, membershipPostDefinition.userNotifications)
                && Objects.equals(this.memberships, membershipPostDefinition.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminNotifications, userNotifications, memberships);
    }

    @Override
    public String toString() {

        return "class MembershipPostDefinition {\n"
                + "    adminNotifications: "
                + toIndentedString(adminNotifications)
                + "\n"
                + "    userNotifications: "
                + toIndentedString(userNotifications)
                + "\n"
                + "    memberships: "
                + toIndentedString(memberships)
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
