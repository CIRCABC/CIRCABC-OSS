package io.swagger.model;

import java.util.Date;
import java.util.Objects;

/**
 * the representation of the membership of a user inside an Interest group
 */
public class UserProfile {

    private User user = null;

    private Profile profile = null;

    private Date expirationDate = null;

    /**
     * Get user
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserProfile userProfile = (UserProfile) o;
        return Objects.equals(this.user, userProfile.user)
                && Objects.equals(this.profile, userProfile.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, profile);
    }

    @Override
    public String toString() {

        return "class UserProfile {\n"
                + "    user: "
                + toIndentedString(user)
                + "\n"
                + "    profile: "
                + toIndentedString(profile)
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
