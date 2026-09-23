package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON object that contains all the configuration variables of an Interest Group.
 *
 * <p>This is a plain data-transfer object (DTO) used to serialize/deserialize the
 * configuration of the different services offered by an Interest Group (IG). Each
 * service exposes its settings as a set of key/value pairs:
 *
 * <ul>
 *   <li>{@code information} &ndash; configuration of the Information service</li>
 *   <li>{@code library} &ndash; configuration of the Library service</li>
 *   <li>{@code newsgroups} &ndash; configuration of the Newsgroup service (structured)</li>
 *   <li>{@code events} &ndash; configuration of the Events service</li>
 *   <li>{@code dashboard} &ndash; configuration of the IG dashboard</li>
 * </ul>
 */
public class GroupConfiguration {

  /** Configuration settings of the Information service, keyed by setting name. */
  private Map<String, String> information = null;

  /** Configuration settings of the Library service, keyed by setting name. */
  private Map<String, String> library = null;

  /** Structured configuration of the Newsgroup service. */
  private GroupConfigurationNewsgroups newsgroups = null;

  /** Configuration settings of the Events service, keyed by setting name. */
  private Map<String, String> events = null;

  /** Configuration settings of the IG dashboard, keyed by setting name. */
  private Map<String, String> dashboard = null;

  /**
   * Adds a single entry to the Information service configuration map, creating the
   * map if it does not yet exist.
   *
   * @param key the configuration setting name
   * @param informationItem the value to associate with the given key
   * @return this {@code GroupConfiguration} instance, to allow method chaining
   */
  public GroupConfiguration putInformationItem(
    String key,
    String informationItem
  ) {
    if (this.information == null) {
      this.information = new HashMap<>();
    }
    this.information.put(key, informationItem);
    return this;
  }

  /**
   * Get information
   *
   * @return the Information service configuration map, or {@code null} if none is set
   */
  public Map<String, String> getInformation() {
    return information;
  }

  /**
   * Sets the Information service configuration map.
   *
   * @param information the configuration settings, keyed by setting name
   */
  public void setInformation(Map<String, String> information) {
    this.information = information;
  }

  /**
   * Adds a single entry to the Library service configuration map, creating the
   * map if it does not yet exist.
   *
   * @param key the configuration setting name
   * @param libraryItem the value to associate with the given key
   * @return this {@code GroupConfiguration} instance, to allow method chaining
   */
  public GroupConfiguration putLibraryItem(String key, String libraryItem) {
    if (this.library == null) {
      this.library = new HashMap<>();
    }
    this.library.put(key, libraryItem);
    return this;
  }

  /**
   * Get library
   *
   * @return the Library service configuration map, or {@code null} if none is set
   */
  public Map<String, String> getLibrary() {
    return library;
  }

  /**
   * Sets the Library service configuration map.
   *
   * @param library the configuration settings, keyed by setting name
   */
  public void setLibrary(Map<String, String> library) {
    this.library = library;
  }

  /**
   * Get newsgroups
   *
   * @return the Newsgroup service configuration, or {@code null} if none is set
   */
  public GroupConfigurationNewsgroups getNewsgroups() {
    return newsgroups;
  }

  /**
   * Sets the Newsgroup service configuration.
   *
   * @param newsgroups the structured Newsgroup configuration
   */
  public void setNewsgroups(GroupConfigurationNewsgroups newsgroups) {
    this.newsgroups = newsgroups;
  }

  /**
   * Adds a single entry to the Events service configuration map, creating the
   * map if it does not yet exist.
   *
   * @param key the configuration setting name
   * @param eventsItem the value to associate with the given key
   * @return this {@code GroupConfiguration} instance, to allow method chaining
   */
  public GroupConfiguration putEventsItem(String key, String eventsItem) {
    if (this.events == null) {
      this.events = new HashMap<>();
    }
    this.events.put(key, eventsItem);
    return this;
  }

  /**
   * Get events
   *
   * @return the Events service configuration map, or {@code null} if none is set
   */
  public Map<String, String> getEvents() {
    return events;
  }

  /**
   * Sets the Events service configuration map.
   *
   * @param events the configuration settings, keyed by setting name
   */
  public void setEvents(Map<String, String> events) {
    this.events = events;
  }

  /**
   * Adds a single entry to the dashboard configuration map, creating the
   * map if it does not yet exist.
   *
   * @param key the configuration setting name
   * @param dashboardItem the value to associate with the given key
   * @return this {@code GroupConfiguration} instance, to allow method chaining
   */
  public GroupConfiguration putDashboardItem(String key, String dashboardItem) {
    if (this.dashboard == null) {
      this.dashboard = new HashMap<>();
    }
    this.dashboard.put(key, dashboardItem);
    return this;
  }

  /**
   * Get dashboard
   *
   * @return the dashboard configuration map, or {@code null} if none is set
   */
  public Map<String, String> getDashboard() {
    return dashboard;
  }

  /**
   * Sets the dashboard configuration map.
   *
   * @param dashboard the configuration settings, keyed by setting name
   */
  public void setDashboard(Map<String, String> dashboard) {
    this.dashboard = dashboard;
  }

  /**
   * Compares this configuration with another object for equality. Two
   * {@code GroupConfiguration} instances are equal when all of their service
   * configurations (information, library, newsgroups, events and dashboard) are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code GroupConfiguration}
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupConfiguration groupConfiguration = (GroupConfiguration) o;
    return (
      Objects.equals(this.information, groupConfiguration.information) &&
      Objects.equals(this.library, groupConfiguration.library) &&
      Objects.equals(this.newsgroups, groupConfiguration.newsgroups) &&
      Objects.equals(this.events, groupConfiguration.events) &&
      Objects.equals(this.dashboard, groupConfiguration.dashboard)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from all
   * service configuration fields.
   *
   * @return the hash code for this configuration
   */
  @Override
  public int hashCode() {
    return Objects.hash(information, library, newsgroups, events, dashboard);
  }

  /**
   * Returns a human-readable, multi-line representation of this configuration,
   * intended for debugging and logging.
   *
   * @return a string describing all service configurations
   */
  @Override
  public String toString() {
    return (
      "class GroupConfiguration {\n" +
      "    information: " +
      toIndentedString(information) +
      "\n" +
      "    library: " +
      toIndentedString(library) +
      "\n" +
      "    newsgroups: " +
      toIndentedString(newsgroups) +
      "\n" +
      "    events: " +
      toIndentedString(events) +
      "\n" +
      "    dashboard: " +
      toIndentedString(dashboard) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert; may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Serializes this configuration to a JSON string. Only the {@code newsgroups}
   * section is rendered from its actual content; the remaining service sections
   * are emitted as empty JSON objects.
   *
   * @return a JSON representation of this configuration
   */
  public String toJsonString() {
    return (
      "{\n" +
      "\t\"information\": " +
      "{}," +
      "\n" +
      "\t\"library\": " +
      "{}," +
      "\n" +
      "\t\"newsgroups\": " +
      newsgroups.toJsonString() +
      ",\n" +
      "\t\"events\": " +
      "{}," +
      "\n" +
      "\t\"dashboard\": " +
      "{}" +
      "\n" +
      "}"
    );
  }
}
