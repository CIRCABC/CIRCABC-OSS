package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * the representation of the dashboard of one CIRCABC user it contains the next events, the list of
 * activities in all its Interest group, his memberships and any other information needed ---
 * Description --- \&quot;entries\&quot; &#x3D; all the new content in all the groups the user is
 * member in \&quot;membershipRequests\&quot; &#x3D; all the request a user has submitted and
 * waiting on approval \&quot;incomingEvents\&quot; &#x3D; all the events in the user calendat that
 * are coming soon &gt;&gt; add any useful dashboard elements as a new properties
 */
public class UserDashboard {

  /** Identifier of the user this dashboard belongs to. */
  private String userId = null;

  /** New/relevant content activity entries across all Interest Groups the user is a member of. */
  private List<UserDashboardEntry> entries = new ArrayList<>();

  /** Upcoming calendar events for the user, represented as content nodes. */
  private List<Node> incomingEvents = new ArrayList<>();

  /** Pending membership requests submitted by the user and awaiting approval. */
  private List<Object> membershipRequests = new ArrayList<>();

  /**
   * Returns the identifier of the user this dashboard belongs to.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the identifier of the user this dashboard belongs to.
   *
   * @param userId the user id to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Returns the activity entries collected from all Interest Groups the user is a member of.
   *
   * @return the list of dashboard entries
   */
  public List<UserDashboardEntry> getEntries() {
    return entries;
  }

  /**
   * Sets the activity entries collected from all Interest Groups the user is a member of.
   *
   * @param entries the list of dashboard entries to set
   */
  public void setEntries(List<UserDashboardEntry> entries) {
    this.entries = entries;
  }

  /**
   * Returns the upcoming calendar events for the user.
   *
   * @return the list of incoming event nodes
   */
  public List<Node> getIncomingEvents() {
    return incomingEvents;
  }

  /**
   * Sets the upcoming calendar events for the user.
   *
   * @param incomingEvents the list of incoming event nodes to set
   */
  public void setIncomingEvents(List<Node> incomingEvents) {
    this.incomingEvents = incomingEvents;
  }

  /**
   * Returns the pending membership requests submitted by the user and awaiting approval.
   *
   * @return the list of membership requests
   */
  public List<Object> getMembershipRequests() {
    return membershipRequests;
  }

  /**
   * Sets the pending membership requests submitted by the user and awaiting approval.
   *
   * @param membershipRequests the list of membership requests to set
   */
  public void setMembershipRequests(List<Object> membershipRequests) {
    this.membershipRequests = membershipRequests;
  }

  /**
   * Compares this dashboard with another object for equality. Two dashboards are equal when their
   * user id, entries, incoming events and membership requests are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code UserDashboard}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDashboard userDashboard = (UserDashboard) o;
    return (
      Objects.equals(this.userId, userDashboard.userId) &&
      Objects.equals(this.entries, userDashboard.entries) &&
      Objects.equals(this.incomingEvents, userDashboard.incomingEvents) &&
      Objects.equals(this.membershipRequests, userDashboard.membershipRequests)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived from the user
   * id, entries, incoming events and membership requests.
   *
   * @return the hash code for this dashboard
   */
  @Override
  public int hashCode() {
    return Objects.hash(userId, entries, incomingEvents, membershipRequests);
  }

  /**
   * Returns a human-readable, multi-line string representation of this dashboard for debugging
   * purposes.
   *
   * @return a string representation of this dashboard
   */
  @Override
  public String toString() {
    return (
      "class UserDashboard {\n" +
      "    userId: " +
      toIndentedString(userId) +
      "\n" +
      "    entries: " +
      toIndentedString(entries) +
      "\n" +
      "    incomingEvents: " +
      toIndentedString(incomingEvents) +
      "\n" +
      "    membershipRequests: " +
      toIndentedString(membershipRequests) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
