package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object describing the newsgroup (forum) configuration of an
 * Interest Group.
 *
 * <p>It carries the settings that control the "new" flag behaviour for the
 * Newsgroup service: whether newly created topics and forums are flagged as
 * new, and for how long (in days) such items keep the "new" flag before it
 * expires. Instances of this class are typically serialized to / deserialized
 * from JSON as part of the group configuration REST payload.
 */
public class GroupConfigurationNewsgroups {

  /** Whether newly created topics should be marked with the "new" flag. */
  private Boolean enableFlagNewTopic = null;

  /** Whether newly created forums should be marked with the "new" flag. */
  private Boolean enableFlagNewForum = null;

  /** Number of days a topic keeps the "new" flag before it expires. */
  private Integer ageFlagNewTopic = null;

  /** Number of days a forum keeps the "new" flag before it expires. */
  private Integer ageFlagNewForum = null;

  /**
   * Returns whether the "new" flag is enabled for newly created topics.
   *
   * @return {@code true} if new topics are flagged as new, {@code false}
   *     otherwise, or {@code null} if unset
   */
  public Boolean getEnableFlagNewTopic() {
    return enableFlagNewTopic;
  }

  /**
   * Sets whether the "new" flag is enabled for newly created topics.
   *
   * @param enableFlagNewTopic {@code true} to flag new topics as new
   */
  public void setEnableFlagNewTopic(Boolean enableFlagNewTopic) {
    this.enableFlagNewTopic = enableFlagNewTopic;
  }

  /**
   * Returns whether the "new" flag is enabled for newly created forums.
   *
   * @return {@code true} if new forums are flagged as new, {@code false}
   *     otherwise, or {@code null} if unset
   */
  public Boolean getEnableFlagNewForum() {
    return enableFlagNewForum;
  }

  /**
   * Sets whether the "new" flag is enabled for newly created forums.
   *
   * @param enableFlagNewForum {@code true} to flag new forums as new
   */
  public void setEnableFlagNewForum(Boolean enableFlagNewForum) {
    this.enableFlagNewForum = enableFlagNewForum;
  }

  /**
   * Returns the number of days a topic keeps the "new" flag before it expires.
   *
   * @return the age (in days) of the "new" flag for topics, or {@code null} if
   *     unset
   */
  public Integer getAgeFlagNewTopic() {
    return ageFlagNewTopic;
  }

  /**
   * Sets the number of days a topic keeps the "new" flag before it expires.
   *
   * @param ageFlagNewTopic the age (in days) of the "new" flag for topics
   */
  public void setAgeFlagNewTopic(Integer ageFlagNewTopic) {
    this.ageFlagNewTopic = ageFlagNewTopic;
  }

  /**
   * Returns the number of days a forum keeps the "new" flag before it expires.
   *
   * @return the age (in days) of the "new" flag for forums, or {@code null} if
   *     unset
   */
  public Integer getAgeFlagNewForum() {
    return ageFlagNewForum;
  }

  /**
   * Sets the number of days a forum keeps the "new" flag before it expires.
   *
   * @param ageFlagNewForum the age (in days) of the "new" flag for forums
   */
  public void setAgeFlagNewForum(Integer ageFlagNewForum) {
    this.ageFlagNewForum = ageFlagNewForum;
  }

  /**
   * Compares this configuration with another object for equality. Two
   * instances are equal when all of their flag and age fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a
   *     {@code GroupConfigurationNewsgroups} with equal field values
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupConfigurationNewsgroups groupConfigurationNewsgroups =
      (GroupConfigurationNewsgroups) o;
    return (
      Objects.equals(
        this.enableFlagNewTopic,
        groupConfigurationNewsgroups.enableFlagNewTopic
      ) &&
      Objects.equals(
        this.enableFlagNewForum,
        groupConfigurationNewsgroups.enableFlagNewForum
      ) &&
      Objects.equals(
        this.ageFlagNewTopic,
        groupConfigurationNewsgroups.ageFlagNewTopic
      ) &&
      Objects.equals(
        this.ageFlagNewForum,
        groupConfigurationNewsgroups.ageFlagNewForum
      )
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, computed from
   * all flag and age fields.
   *
   * @return the hash code for this configuration
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      enableFlagNewTopic,
      enableFlagNewForum,
      ageFlagNewTopic,
      ageFlagNewForum
    );
  }

  /**
   * Returns a human-readable, multi-line representation of this configuration,
   * intended for debugging and logging purposes.
   *
   * @return a string representation of this configuration
   */
  @Override
  public String toString() {
    return (
      "class GroupConfigurationNewsgroups {\n" +
      "    enableFlagNewTopic: " +
      toIndentedString(enableFlagNewTopic) +
      "\n" +
      "    enableFlagNewForum: " +
      toIndentedString(enableFlagNewForum) +
      "\n" +
      "    ageFlagNewTopic: " +
      toIndentedString(ageFlagNewTopic) +
      "\n" +
      "    ageFlagNewForum: " +
      toIndentedString(ageFlagNewForum) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render, may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Serializes this configuration into a JSON object string containing the
   * {@code enableFlagNewTopic}, {@code enableFlagNewForum},
   * {@code ageFlagNewTopic} and {@code ageFlagNewForum} fields.
   *
   * @return the JSON representation of this configuration
   */
  public String toJsonString() {
    return (
      "{\n" +
      "\t\"enableFlagNewTopic\": " +
      enableFlagNewTopic +
      ",\n" +
      "\t\"enableFlagNewForum\": " +
      enableFlagNewForum +
      ",\n" +
      "\t\"ageFlagNewTopic\": " +
      ageFlagNewTopic +
      ",\n" +
      "\t\"ageFlagNewForum\": " +
      ageFlagNewForum +
      "\n" +
      "}"
    );
  }
}
