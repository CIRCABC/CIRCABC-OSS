package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object describing an e-mail to be sent through the CIRCABC REST
 * API.
 *
 * <p>It carries the message subject and body together with the intended
 * recipients, expressed either as individual {@link User} entries or as
 * {@link Profile} groups. Optional file {@code attachments} and a flag
 * indicating whether a copy should be delivered back to the sender can also be
 * supplied. Instances are typically deserialized from a JSON request body and
 * consumed by mail-related endpoints and services.</p>
 */
public class EmailDefinition {

  /** Subject line of the e-mail message. */
  private String subject = null;

  /** Body content of the e-mail message. */
  private String content = null;

  /** References (e.g. node identifiers) of files to attach to the e-mail. */
  private List<String> attachments = new ArrayList<>();

  /** Individual users to whom the e-mail is addressed. */
  private List<User> users = new ArrayList<>();

  /** Profiles (recipient groups) to whom the e-mail is addressed. */
  private List<Profile> profiles = new ArrayList<>();

  /** Whether a copy of the e-mail should also be sent to the sender. */
  private Boolean copyToSender = false;

  /**
   * Gets the subject line of the e-mail.
   *
   * @return the e-mail subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject line of the e-mail.
   *
   * @param subject the e-mail subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the body content of the e-mail.
   *
   * @return the e-mail content
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the body content of the e-mail.
   *
   * @param content the e-mail content to set
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Gets the references of the files attached to the e-mail.
   *
   * @return the list of attachment references
   */
  public List<String> getAttachments() {
    return attachments;
  }

  /**
   * Sets the references of the files attached to the e-mail.
   *
   * @param attachments the list of attachment references to set
   */
  public void setAttachments(List<String> attachments) {
    this.attachments = attachments;
  }

  /**
   * Gets the individual user recipients of the e-mail.
   *
   * @return the list of recipient users
   */
  public List<User> getUsers() {
    return users;
  }

  /**
   * Sets the individual user recipients of the e-mail.
   *
   * @param users the list of recipient users to set
   */
  public void setUsers(List<User> users) {
    this.users = users;
  }

  /**
   * Gets the profile (group) recipients of the e-mail.
   *
   * @return the list of recipient profiles
   */
  public List<Profile> getProfiles() {
    return profiles;
  }

  /**
   * Sets the profile (group) recipients of the e-mail.
   *
   * @param profiles the list of recipient profiles to set
   */
  public void setProfiles(List<Profile> profiles) {
    this.profiles = profiles;
  }

  /**
   * Indicates whether a copy of the e-mail should be sent back to the sender.
   *
   * @return {@code true} if the sender receives a copy, {@code false} otherwise
   */
  public Boolean getCopyToSender() {
    return copyToSender;
  }

  /**
   * Sets whether a copy of the e-mail should be sent back to the sender.
   *
   * @param copyToSender {@code true} to send a copy to the sender
   */
  public void setCopyToSender(Boolean copyToSender) {
    this.copyToSender = copyToSender;
  }

  /**
   * Compares this e-mail definition with another for equality based on subject,
   * content, attachments, users and profiles. The {@code copyToSender} flag is
   * not considered.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code EmailDefinition}
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EmailDefinition emailDefinition = (EmailDefinition) o;
    return (
      Objects.equals(this.subject, emailDefinition.subject) &&
      Objects.equals(this.content, emailDefinition.content) &&
      Objects.equals(this.attachments, emailDefinition.attachments) &&
      Objects.equals(this.users, emailDefinition.users) &&
      Objects.equals(this.profiles, emailDefinition.profiles)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, based on
   * subject, content, attachments, users and profiles.
   *
   * @return the hash code for this e-mail definition
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, content, attachments, users, profiles);
  }

  /**
   * Returns a human-readable, multi-line representation of this e-mail
   * definition.
   *
   * @return a string describing this e-mail definition
   */
  @Override
  public String toString() {
    return (
      "class EmailDefinition {\n" +
      "    subject: " +
      toIndentedString(subject) +
      "\n" +
      "    content: " +
      toIndentedString(content) +
      "\n" +
      "    attachments: " +
      toIndentedString(attachments) +
      "\n" +
      "    users: " +
      toIndentedString(users) +
      "\n" +
      "    profiles: " +
      toIndentedString(profiles) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
