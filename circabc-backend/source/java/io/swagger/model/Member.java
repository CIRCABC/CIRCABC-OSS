package io.swagger.model;

import java.util.Objects;

public class Member {

    private User user = null;

    private Profile profile = null;

    private InterestGroup interestGroup = null;

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
     * Get interestGroup
     *
     * @return interestGroup
     */
    public InterestGroup getInterestGroup() {
        return interestGroup;
    }

    public void setInterestGroup(InterestGroup interestGroup) {
        this.interestGroup = interestGroup;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(this.user, member.user)
                && Objects.equals(this.profile, member.profile)
                && Objects.equals(this.interestGroup, member.interestGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, profile, interestGroup);
    }

    @Override
    public String toString() {

        return "class Member {\n"
                + "    user: "
                + toIndentedString(user)
                + "\n"
                + "    profile: "
                + toIndentedString(profile)
                + "\n"
                + "    interestGroup: "
                + toIndentedString(interestGroup)
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
