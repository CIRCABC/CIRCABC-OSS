/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class UserRecoveryOption {

    private Boolean recoverable = false;
    private Profile profile;

    public Boolean getRecoverable() {
        return recoverable;
    }

    public void setRecoverable(Boolean recoverable) {
        this.recoverable = recoverable;
    }

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
        UserRecoveryOption userRecoveryOption = (UserRecoveryOption) o;
        return Objects.equals(this.recoverable, userRecoveryOption.recoverable)
                && Objects.equals(this.profile, userRecoveryOption.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recoverable, profile);
    }

    @Override
    public String toString() {

        return "class News {\n"
                + "    recoverable: "
                + toIndentedString(recoverable)
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
