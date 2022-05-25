package io.swagger.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GroupCreationRequest
 */
public class GroupCreationRequest {

    private Integer id = null;

    private User from = null;

    private String proposedName = null;

    private I18nProperty proposedTitle = new I18nProperty();

    private I18nProperty proposedDescription = new I18nProperty();

    private String justification = null;

    private User reviewer = null;

    private Integer agreement = 0;

    private DateTime requestDate = null;

    private String categoryRef = null;

    private List<User> leaders = new ArrayList<>();

    private String argument;

    private DateTime agreementDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get from
     *
     * @return from
     */
    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    /**
     * Get proposedName
     *
     * @return proposedName
     */
    public String getProposedName() {
        return proposedName;
    }

    public void setProposedName(String proposedName) {
        this.proposedName = proposedName;
    }

    /**
     * Get proposedTitle
     *
     * @return proposedTitle
     */
    public I18nProperty getProposedTitle() {
        return proposedTitle;
    }

    public void setProposedTitle(I18nProperty proposedTitle) {
        this.proposedTitle = proposedTitle;
    }

    /**
     * Get proposedDescription
     *
     * @return proposedDescription
     */
    public I18nProperty getProposedDescription() {
        return proposedDescription;
    }

    public void setProposedDescription(I18nProperty proposedDescription) {
        this.proposedDescription = proposedDescription;
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

    /**
     * Get requestDate
     *
     * @return requestDate
     */
    public DateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(DateTime date) {
        this.requestDate = date;
    }

    /**
     * Get categoryRef
     *
     * @return categoryRef
     */
    public String getCategoryRef() {
        return categoryRef;
    }

    public void setCategoryRef(String categoryRef) {
        this.categoryRef = categoryRef;
    }

    public GroupCreationRequest addLeadersItem(User leadersItem) {
        if (this.leaders == null) {
            this.leaders = new ArrayList<>();
        }
        this.leaders.add(leadersItem);
        return this;
    }

    /**
     * Get leaders
     *
     * @return leaders
     */
    public List<User> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<User> leaders) {
        this.leaders = leaders;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public Integer getAgreement() {
        return agreement;
    }

    public void setAgreement(Integer agreement) {
        this.agreement = agreement;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupCreationRequest groupCreationRequest = (GroupCreationRequest) o;
        return Objects.equals(this.id, groupCreationRequest.id)
                && Objects.equals(this.from, groupCreationRequest.from)
                && Objects.equals(this.proposedName, groupCreationRequest.proposedName)
                && Objects.equals(this.proposedTitle, groupCreationRequest.proposedTitle)
                && Objects.equals(this.proposedDescription, groupCreationRequest.proposedDescription)
                && Objects.equals(this.justification, groupCreationRequest.justification)
                && Objects.equals(this.requestDate, groupCreationRequest.requestDate)
                && Objects.equals(this.categoryRef, groupCreationRequest.categoryRef)
                && Objects.equals(this.leaders, groupCreationRequest.leaders)
                && Objects.equals(this.agreement, groupCreationRequest.agreement)
                && Objects.equals(this.reviewer, groupCreationRequest.reviewer)
                && Objects.equals(this.argument, groupCreationRequest.argument)
                && Objects.equals(this.agreementDate, groupCreationRequest.agreementDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                from,
                proposedName,
                proposedTitle,
                proposedDescription,
                justification,
                requestDate,
                categoryRef,
                leaders,
                reviewer,
                agreement,
                argument,
                agreementDate);
    }

    @Override
    public String toString() {

        return "class GroupCreationRequest {\n"
                + "    id: "
                + toIndentedString(id)
                + "    from: "
                + toIndentedString(from)
                + "\n"
                + "    proposedName: "
                + toIndentedString(proposedName)
                + "\n"
                + "    proposedTitle: "
                + toIndentedString(proposedTitle)
                + "\n"
                + "    proposedDescription: "
                + toIndentedString(proposedDescription)
                + "\n"
                + "    justification: "
                + toIndentedString(justification)
                + "\n"
                + "    requestDate: "
                + toIndentedString(requestDate)
                + "\n"
                + "    categoryRef: "
                + toIndentedString(categoryRef)
                + "\n"
                + "    leaders: "
                + toIndentedString(leaders)
                + "\n"
                + "    agreement: "
                + toIndentedString(agreement)
                + "\n"
                + "    reviewer: "
                + toIndentedString(reviewer)
                + "\n"
                + "    argument: "
                + toIndentedString(argument)
                + "\n"
                + "    agreementDate: "
                + toIndentedString(agreementDate)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public DateTime getAgreementDate() {
        return agreementDate;
    }

    public void setAgreementDate(DateTime agreementDate) {
        this.agreementDate = agreementDate;
    }
}
