package io.swagger.model;

import org.joda.time.LocalDate;

import java.util.Objects;

public class GroupDashboardImportantMessages {

    private LocalDate date = null;

    private String message = null;

    /**
     * Get date
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
        GroupDashboardImportantMessages groupDashboardImportantMessages =
                (GroupDashboardImportantMessages) o;
        return Objects.equals(this.date, groupDashboardImportantMessages.date)
                && Objects.equals(this.message, groupDashboardImportantMessages.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, message);
    }

    @Override
    public String toString() {

        return "class GroupDashboardImportantMessages {\n"
                + "    date: "
                + toIndentedString(date)
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
