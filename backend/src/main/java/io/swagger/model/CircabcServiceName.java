/**
 *
 */
package io.swagger.model;

/**
 * Enumeration of the collaboration services offered within a CIRCABC Interest Group.
 *
 * <p>Each constant represents one of the functional services an Interest Group can expose
 * (Information, Library, Newsgroups and Events) and carries the canonical lower-case service
 * name used by the REST API and the underlying Alfresco repository. The associated string value
 * is returned by {@link #getServiceName()} and by {@link #toString()}.
 *
 * @author beaurpi
 */
public enum CircabcServiceName {
  /** The Information service, exposing static informational content of an Interest Group. */
  INFORMATION("information"),
  /** The Library service, providing document and folder management. */
  LIBRARY("library"),
  /** The Newsgroups service, providing discussion forums (topics and posts). */
  NEWSGROUPS("newsgroups"),
  /** The Events service, providing the calendar and events management. */
  EVENTS("events");

  /** The canonical lower-case service name used by the REST API and the repository. */
  private String serviceName;

  /**
   * Returns the canonical lower-case service name associated with this constant.
   *
   * @return the service name (e.g. {@code "library"})
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Creates a service name constant bound to its canonical string representation.
   *
   * @param serviceName the canonical lower-case service name
   */
  CircabcServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Returns the canonical service name, making the enum render as its lower-case string value.
   *
   * @return the service name (e.g. {@code "events"})
   */
  @Override
  public String toString() {
    return this.serviceName;
  }
}
