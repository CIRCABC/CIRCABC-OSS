package io.swagger.model;

import java.util.Objects;

/**
 * the representation of the membership of a user with an Interest group
 */
public class InterestGroupProfile {

    private Profile profile = null;

    private InterestGroup interestGroup = null;

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
        InterestGroupProfile interestGroupProfile = (InterestGroupProfile) o;
        return Objects.equals(this.profile, interestGroupProfile.profile)
                && Objects.equals(this.interestGroup, interestGroupProfile.interestGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, interestGroup);
    }

    @Override
    public String toString() {

        return "class InterestGroupProfile {\n"
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
