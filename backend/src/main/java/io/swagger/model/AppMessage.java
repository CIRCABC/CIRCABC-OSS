package io.swagger.model;

import java.util.Objects;
import org.joda.time.DateTime;

/**
 * Data transfer object representing an application-wide message (banner/notification)
 * displayed to CIRCABC users.
 *
 * <p>An {@code AppMessage} carries the textual content to show, a severity/importance
 * {@code level}, an optional expiry date ({@code dateClosure}), a flag indicating whether
 * the message is currently active ({@code enabled}), and how long it should be displayed
 * ({@code displayTime}). Instances are serialized to and from JSON by the REST layer.</p>
 */
@jakarta.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2018-02-19T14:19:50.800+01:00"
)
public class AppMessage {

  /** Unique identifier of the application message. */
  private Integer id = null;

  /** Textual body of the message shown to users. */
  private String content = null;

  /** Date and time after which the message is no longer displayed. */
  private DateTime dateClosure = null;

  /** Severity or importance level of the message (e.g. info, warning, error). */
  private String level = null;

  /** Whether the message is currently active and should be shown; defaults to {@code false}. */
  private Boolean enabled = false;

  /** Duration, in seconds, for which the message should remain visible. */
  private Integer displayTime = null;

  /**
   * Returns the unique identifier of this message.
   *
   * @return the message id
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this message.
   *
   * @param id the message id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns the textual content of this message.
   *
   * @return the message content
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the textual content of this message.
   *
   * @param content the message content to set
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Returns the date and time after which the message is no longer displayed.
   *
   * @return the closure date, or {@code null} if none is set
   */
  public DateTime getDateClosure() {
    return dateClosure;
  }

  /**
   * Sets the date and time after which the message is no longer displayed.
   *
   * @param dateClosure the closure date to set
   */
  public void setDateClosure(DateTime dateClosure) {
    this.dateClosure = dateClosure;
  }

  /**
   * Returns the severity or importance level of this message.
   *
   * @return the message level
   */
  public String getLevel() {
    return level;
  }

  /**
   * Sets the severity or importance level of this message.
   *
   * @param level the message level to set
   */
  public void setLevel(String level) {
    this.level = level;
  }

  /**
   * Indicates whether this message is currently active and should be shown.
   *
   * @return {@code true} if the message is enabled, {@code false} otherwise
   */
  public Boolean getEnabled() {
    return enabled;
  }

  /**
   * Sets whether this message is currently active and should be shown.
   *
   * @param enabled {@code true} to enable the message, {@code false} to disable it
   */
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the duration, in seconds, for which the message should remain visible.
   *
   * @return the display time
   */
  public Integer getDisplayTime() {
    return displayTime;
  }

  /**
   * Sets the duration, in seconds, for which the message should remain visible.
   *
   * @param displayTime the display time to set
   */
  public void setDisplayTime(Integer displayTime) {
    this.displayTime = displayTime;
  }

  /**
   * Compares this message with another object for equality. Two {@code AppMessage}
   * instances are equal when all of their fields are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code AppMessage}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppMessage appMessage = (AppMessage) o;
    return (
      Objects.equals(this.id, appMessage.id) &&
      Objects.equals(this.content, appMessage.content) &&
      Objects.equals(this.dateClosure, appMessage.dateClosure) &&
      Objects.equals(this.level, appMessage.level) &&
      Objects.equals(this.enabled, appMessage.enabled) &&
      Objects.equals(this.displayTime, appMessage.displayTime)
    );
  }

  /**
   * Returns a hash code derived from all fields of this message.
   *
   * @return the hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, content, dateClosure, level, enabled, displayTime);
  }

  /**
   * Returns a human-readable, multi-line string representation of this message,
   * primarily intended for debugging and logging.
   *
   * @return a string describing this message and its fields
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppMessage {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb
      .append("    dateClosure: ")
      .append(toIndentedString(dateClosure))
      .append("\n");
    sb.append("    level: ").append(toIndentedString(level)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb
      .append("    displayTime: ")
      .append(toIndentedString(displayTime))
      .append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
