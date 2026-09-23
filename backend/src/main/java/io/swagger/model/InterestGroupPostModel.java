package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object describing the payload used to create a new Interest Group in CIRCABC.
 *
 * <p>An instance of this model carries the attributes supplied by the client when requesting the
 * creation of an Interest Group, including its technical name, its internationalised title,
 * description and contact information, the initial list of group leaders, and the optional
 * notification settings applied on creation.
 */
public class InterestGroupPostModel {

  /** Technical (short) name identifying the Interest Group. */
  private String name = null;

  /** Internationalised display title of the Interest Group. */
  private I18nProperty title = new I18nProperty();

  /** Internationalised description of the Interest Group. */
  private I18nProperty description = new I18nProperty();

  /** Internationalised contact information for the Interest Group. */
  private I18nProperty contact = new I18nProperty();

  /** User names of the members granted the leader role in the Interest Group. */
  private List<String> leaders = new ArrayList<>();

  /** Whether a notification should be sent when the Interest Group is created. */
  private Boolean notify = false;

  /** Internationalised text to include in the creation notification. */
  private I18nProperty notifyText = new I18nProperty();

  /**
   * Returns the technical name of the Interest Group.
   *
   * @return the Interest Group name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the technical name of the Interest Group.
   *
   * @param name the Interest Group name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the internationalised title of the Interest Group.
   *
   * @return the title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the internationalised title of the Interest Group.
   *
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Sets the internationalised description and returns this instance for fluent chaining.
   *
   * @param description the description to set
   * @return this {@link InterestGroupPostModel} instance
   */
  public InterestGroupPostModel description(I18nProperty description) {
    this.description = description;
    return this;
  }

  /**
   * Returns the internationalised description of the Interest Group.
   *
   * @return the description
   */
  public I18nProperty getDescription() {
    return description;
  }

  /**
   * Sets the internationalised description of the Interest Group.
   *
   * @param description the description to set
   */
  public void setDescription(I18nProperty description) {
    this.description = description;
  }

  /**
   * Sets the internationalised contact information and returns this instance for fluent chaining.
   *
   * @param contact the contact information to set
   * @return this {@link InterestGroupPostModel} instance
   */
  public InterestGroupPostModel contact(I18nProperty contact) {
    this.contact = contact;
    return this;
  }

  /**
   * Returns the internationalised contact information of the Interest Group.
   *
   * @return the contact information
   */
  public I18nProperty getContact() {
    return contact;
  }

  /**
   * Sets the internationalised contact information of the Interest Group.
   *
   * @param contact the contact information to set
   */
  public void setContact(I18nProperty contact) {
    this.contact = contact;
  }

  /**
   * Sets the list of leaders and returns this instance for fluent chaining.
   *
   * @param leaders the list of leader user names to set
   * @return this {@link InterestGroupPostModel} instance
   */
  public InterestGroupPostModel leaders(List<String> leaders) {
    this.leaders = leaders;
    return this;
  }

  /**
   * Adds a single leader to the list of leaders, initialising the list if necessary.
   *
   * @param leadersItem the leader user name to add
   * @return this {@link InterestGroupPostModel} instance
   */
  public InterestGroupPostModel addLeadersItem(String leadersItem) {
    if (this.leaders == null) {
      this.leaders = new ArrayList<>();
    }
    this.leaders.add(leadersItem);
    return this;
  }

  /**
   * Returns the list of leader user names for the Interest Group.
   *
   * @return the list of leaders
   */
  public List<String> getLeaders() {
    return leaders;
  }

  /**
   * Sets the list of leader user names for the Interest Group.
   *
   * @param leaders the list of leaders to set
   */
  public void setLeaders(List<String> leaders) {
    this.leaders = leaders;
  }

  /**
   * Sets the notification flag and returns this instance for fluent chaining.
   *
   * @param notify {@code true} to send a notification on creation, {@code false} otherwise
   * @return this {@link InterestGroupPostModel} instance
   */
  public InterestGroupPostModel notify(Boolean notify) {
    this.notify = notify;
    return this;
  }

  /**
   * Returns whether a notification should be sent when the Interest Group is created.
   *
   * @return the notification flag
   */
  public Boolean getNotify() {
    return notify;
  }

  /**
   * Sets whether a notification should be sent when the Interest Group is created.
   *
   * @param notify the notification flag to set
   */
  public void setNotify(Boolean notify) {
    this.notify = notify;
  }

  /**
   * Returns the internationalised text used in the creation notification.
   *
   * @return the notification text
   */
  public I18nProperty getNotifyText() {
    return notifyText;
  }

  /**
   * Sets the internationalised text used in the creation notification.
   *
   * @param notifyText the notification text to set
   */
  public void setNotifyText(I18nProperty notifyText) {
    this.notifyText = notifyText;
  }

  /**
   * Compares this model with another object for equality based on all of its attributes.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@link InterestGroupPostModel}, {@code
   *     false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterestGroupPostModel interestGroupPostModel = (InterestGroupPostModel) o;
    return (
      Objects.equals(this.name, interestGroupPostModel.name) &&
      Objects.equals(this.title, interestGroupPostModel.title) &&
      Objects.equals(this.description, interestGroupPostModel.description) &&
      Objects.equals(this.contact, interestGroupPostModel.contact) &&
      Objects.equals(this.leaders, interestGroupPostModel.leaders) &&
      Objects.equals(this.notify, interestGroupPostModel.notify) &&
      Objects.equals(this.notifyText, interestGroupPostModel.notifyText)
    );
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code for this model
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, title, description, contact, leaders, notify);
  }

  /**
   * Returns a human-readable, multi-line string representation of this model.
   *
   * @return a string representation of this model
   */
  @Override
  public String toString() {
    return (
      "class InterestGroupPostModel {\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    description: " +
      toIndentedString(description) +
      "\n" +
      "    contact: " +
      toIndentedString(contact) +
      "\n" +
      "    leaders: " +
      toIndentedString(leaders) +
      "\n" +
      "    notify: " +
      toIndentedString(notify) +
      "\n" +
      "    notifyText: " +
      toIndentedString(notifyText) +
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
