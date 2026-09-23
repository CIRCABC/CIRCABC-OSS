package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing the dashboard of a single CIRCABC Interest Group.
 *
 * <p>It aggregates the information that is relevant to a user when viewing an Interest Group,
 * such as recent content, upcoming events, pending membership applications and leader
 * communications. The individual pieces of information are exposed through the following
 * properties:
 *
 * <ul>
 *   <li>{@code entries} &ndash; all the new content in the groups the user is a member of;
 *   <li>{@code applicationRequests} &ndash; all the membership applications awaiting approval;
 *   <li>{@code incomingEvents} &ndash; the events in the user's calendar that are coming soon;
 *   <li>{@code importantMessages} &ndash; a communication channel that Interest Group leaders can
 *       use to broadcast messages.
 * </ul>
 *
 * <p>Additional dashboard elements may be added as new properties when needed.
 */
public class GroupDashboard {

  /** Identifier of the Interest Group this dashboard belongs to. */
  private String groupId = null;

  /** The Interest Group this dashboard describes. */
  private InterestGroup group = null;

  /** New content available across the groups the user is a member of. */
  private List<GroupDashboardEntry> entries = new ArrayList<>();

  /** Events in the user's calendar that are coming soon. */
  private List<Node> incomingEvents = new ArrayList<>();

  /** Membership applications awaiting approval. */
  private List<Object> applicationRequests = new ArrayList<>();

  /** Messages broadcast by Interest Group leaders. */
  private List<GroupDashboardImportantMessages> importantMessages =
    new ArrayList<>();

  /**
   * Get groupId
   *
   * @return groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set the identifier of the Interest Group this dashboard belongs to.
   *
   * @param groupId the Interest Group identifier
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Get group
   *
   * @return group
   */
  public InterestGroup getGroup() {
    return group;
  }

  /**
   * Set the Interest Group this dashboard describes.
   *
   * @param group the Interest Group
   */
  public void setGroup(InterestGroup group) {
    this.group = group;
  }

  /**
   * Get entries
   *
   * @return entries
   */
  public List<GroupDashboardEntry> getEntries() {
    return entries;
  }

  /**
   * Set the new content available across the groups the user is a member of.
   *
   * @param entries the dashboard content entries
   */
  public void setEntries(List<GroupDashboardEntry> entries) {
    this.entries = entries;
  }

  /**
   * Get incomingEvents
   *
   * @return incomingEvents
   */
  public List<Node> getIncomingEvents() {
    return incomingEvents;
  }

  /**
   * Set the events in the user's calendar that are coming soon.
   *
   * @param incomingEvents the upcoming events
   */
  public void setIncomingEvents(List<Node> incomingEvents) {
    this.incomingEvents = incomingEvents;
  }

  /**
   * Get applicationRequests
   *
   * @return applicationRequests
   */
  public List<Object> getApplicationRequests() {
    return applicationRequests;
  }

  /**
   * Set the membership applications awaiting approval.
   *
   * @param applicationRequests the pending membership applications
   */
  public void setApplicationRequests(List<Object> applicationRequests) {
    this.applicationRequests = applicationRequests;
  }

  /**
   * Get importantMessages
   *
   * @return importantMessages
   */
  public List<GroupDashboardImportantMessages> getImportantMessages() {
    return importantMessages;
  }

  /**
   * Set the messages broadcast by Interest Group leaders.
   *
   * @param importantMessages the important messages
   */
  public void setImportantMessages(
    List<GroupDashboardImportantMessages> importantMessages
  ) {
    this.importantMessages = importantMessages;
  }

  /**
   * Compares this dashboard to another object for equality. Two dashboards are equal when all
   * their properties are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code GroupDashboard} with equal properties,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupDashboard groupDashboard = (GroupDashboard) o;
    return (
      Objects.equals(this.groupId, groupDashboard.groupId) &&
      Objects.equals(this.group, groupDashboard.group) &&
      Objects.equals(this.entries, groupDashboard.entries) &&
      Objects.equals(this.incomingEvents, groupDashboard.incomingEvents) &&
      Objects.equals(
        this.applicationRequests,
        groupDashboard.applicationRequests
      ) &&
      Objects.equals(this.importantMessages, groupDashboard.importantMessages)
    );
  }

  /**
   * Returns a hash code derived from all the dashboard properties.
   *
   * @return the hash code for this dashboard
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      groupId,
      group,
      entries,
      incomingEvents,
      applicationRequests,
      importantMessages
    );
  }

  /**
   * Returns a human-readable, multi-line representation of this dashboard and its properties.
   *
   * @return a string representation of this dashboard
   */
  @Override
  public String toString() {
    return (
      "class GroupDashboard {\n" +
      "    groupId: " +
      toIndentedString(groupId) +
      "\n" +
      "    group: " +
      toIndentedString(group) +
      "\n" +
      "    entries: " +
      toIndentedString(entries) +
      "\n" +
      "    incomingEvents: " +
      toIndentedString(incomingEvents) +
      "\n" +
      "    applicationRequests: " +
      toIndentedString(applicationRequests) +
      "\n" +
      "    importantMessages: " +
      toIndentedString(importantMessages) +
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
