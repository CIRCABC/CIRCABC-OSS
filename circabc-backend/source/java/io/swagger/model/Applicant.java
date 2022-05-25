package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * an application for membership representation
 */
public class Applicant {

    private DateTime submitted = null;

    private User user = null;

    private String justification = null;

    /**
     * Get submitted
     *
     * @return submitted
     */
    public DateTime getSubmitted() {
        return submitted;
    }

    public void setSubmitted(DateTime submitted) {
        this.submitted = submitted;
    }

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
     * Get justification
     *
     * @return justification
     */
    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Applicant applicant = (Applicant) o;
        return Objects.equals(this.submitted, applicant.submitted)
                && Objects.equals(this.user, applicant.user)
                && Objects.equals(this.justification, applicant.justification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitted, user, justification);
    }

    @Override
    public String toString() {

        return "class Applicant {\n"
                + "    submitted: "
                + toIndentedString(submitted)
                + "\n"
                + "    user: "
                + toIndentedString(user)
                + "\n"
                + "    justification: "
                + toIndentedString(justification)
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
