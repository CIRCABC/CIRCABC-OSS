package io.swagger.model;

import java.util.Objects;

public class GroupCreationRequestApproval {

    private long id;

    private int agreement;

    private String argument;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAgreement() {
        return agreement;
    }

    public void setAgreement(int agreement) {
        this.agreement = agreement;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupCreationRequestApproval groupCreationRequestApproval = (GroupCreationRequestApproval) o;
        return Objects.equals(this.id, groupCreationRequestApproval.id)
                && Objects.equals(this.agreement, groupCreationRequestApproval.agreement)
                && Objects.equals(this.argument, groupCreationRequestApproval.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, agreement, argument);
    }

    @Override
    public String toString() {

        return "class GroupCreationRequestApproval {\n"
                + "    id: "
                + toIndentedString(id)
                + "    agreement: "
                + toIndentedString(agreement)
                + "\n"
                + "    argument: "
                + toIndentedString(argument)
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
