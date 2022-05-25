package io.swagger.model;

import java.util.Objects;

/**
 * ApplicantAction
 */
public class ApplicantAction {

    private String username = null;

    private String action = null;

    private String message = null;

    /**
     * Get username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get action
     *
     * @return action
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get message
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicantAction applicantAction = (ApplicantAction) o;
        return Objects.equals(this.username, applicantAction.username)
                && Objects.equals(this.action, applicantAction.action)
                && Objects.equals(this.message, applicantAction.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, action, message);
    }

    @Override
    public String toString() {

        return "class ApplicantAction {\n"
                + "    username: "
                + toIndentedString(username)
                + "\n"
                + "    action: "
                + toIndentedString(action)
                + "\n"
                + "    message: "
                + toIndentedString(message)
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
