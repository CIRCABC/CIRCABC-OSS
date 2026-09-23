package io.swagger.model;

/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/

import java.io.Serializable;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Light-weight, serializable value object that represents a single displayable
 * notification subscription entry for the UI.
 *
 * <p>Each instance associates an Alfresco authority (a user or a group) with its
 * {@link NotificationStatus} for a given repository node, together with metadata
 * such as the authority type, the display name and whether the notification
 * setting is inherited from a parent node. It is a plain data holder used to
 * render notification information; it contains no business logic.
 *
 * <p>Equality and hashing are based on the authority, node and status so that
 * wrappers describing the same subscription are treated as equal.
 *
 * @author Yanick Pignot
 */
public class NotificationWrapper implements Serializable {

  /** Serialization version identifier for this value object. */
  private static final long serialVersionUID = -8112745596456613819L;

  /** Login name of the user associated with this notification entry. */
  private String username;
  /** Notification status (e.g. enabled/disabled) for the authority on the node. */
  private NotificationStatus status;
  /** Type of the authority (user or group). */
  private AuthorityType type;
  /** Alfresco authority identifier (user name or group name). */
  private String authority;
  /** Identifier of the repository node the notification setting applies to. */
  private String nodeId;
  /** Whether the notification setting is inherited from a parent node. */
  private boolean isInherited;
  /** Optional localized title associated with the notification entry. */
  private I18nProperty title;

  /**
   * Creates a notification wrapper without a title.
   *
   * @param type the authority type (user or group)
   * @param username the login name of the associated user
   * @param status the notification status for the authority on the node
   * @param authority the Alfresco authority identifier
   * @param nodeId the identifier of the repository node
   * @param isInherited {@code true} if the setting is inherited from a parent node
   */
  public NotificationWrapper(
    final AuthorityType type,
    final String username,
    final NotificationStatus status,
    final String authority,
    final String nodeId,
    final boolean isInherited
  ) {
    super();
    this.type = type;
    this.username = username;
    this.status = status;
    this.authority = authority;
    this.nodeId = nodeId;
    this.isInherited = isInherited;
  }

  /**
   * Creates a notification wrapper including a localized title.
   *
   * @param type the authority type (user or group)
   * @param username the login name of the associated user
   * @param status the notification status for the authority on the node
   * @param authority the Alfresco authority identifier
   * @param nodeId the identifier of the repository node
   * @param isInherited {@code true} if the setting is inherited from a parent node
   * @param title the localized title associated with the notification entry
   */
  public NotificationWrapper(
    final AuthorityType type,
    final String username,
    final NotificationStatus status,
    final String authority,
    final String nodeId,
    final boolean isInherited,
    final I18nProperty title
  ) {
    super();
    this.type = type;
    this.username = username;
    this.status = status;
    this.authority = authority;
    this.nodeId = nodeId;
    this.isInherited = isInherited;
    this.title = title;
  }

  /**
   * @return the title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.authority + " ( " + this.username + " ) " + this.status;
  }

  /**
   * Compares this wrapper with another for equality based on the authority and
   * the notification status.
   *
   * @param o the object to compare with
   * @return {@code true} if the other object is a {@code NotificationWrapper}
   *     with the same authority and status
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NotificationWrapper)) {
      return false;
    }
    final NotificationWrapper other = (NotificationWrapper) o;
    return (
      this.getAuthority().equals(other.getAuthority()) &&
      this.status.equals(other.status)
    );
  }

  /**
   * Returns a hash code derived from the authority, node identifier and status.
   *
   * @return the hash code for this wrapper
   */
  @Override
  public int hashCode() {
    return authority.hashCode() + nodeId.hashCode() + status.hashCode();
  }

  /**
   * @return the authority identifier
   */
  public final String getAuthority() {
    return authority;
  }

  /**
   * @return the notification status name (the enum constant name)
   */
  public String getStatusName() {
    return status.name();
  }

  /**
   * @return the authority type name (the enum constant name)
   */
  public String gettypeName() {
    return type.name();
  }

  /**
   * @return the username
   */
  public final String getUsername() {
    return username;
  }

  /**
   * @return the nodeId
   */
  public final String getNodeId() {
    return nodeId;
  }

  /**
   * @return the status
   */
  public final NotificationStatus getStatusValue() {
    return status;
  }

  /**
   * @return the type
   */
  public final AuthorityType getTypeValue() {
    return type;
  }

  /**
   * @return the status as an object string
   */
  public final String getStatusValueToString() {
    return status.toString();
  }

  /**
   * @return the type as an object string
   */
  public final String getTypeValueToString() {
    return type.toString();
  }

  /**
   * @return {@code true} if the notification setting is inherited from a parent node
   */
  public boolean getInherited() {
    return isInherited;
  }

  /**
   * @param isInherited {@code true} if the setting is inherited from a parent node
   */
  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  /**
   * @return the inherited flag rendered as a string
   */
  public String getInheritedString() {
    return String.valueOf(isInherited);
  }

  /**
   * Sets the inherited flag from its string representation.
   *
   * @param isInheritedString the string to parse into a boolean flag
   */
  public void setInheritedString(String isInheritedString) {
    this.isInherited = Boolean.valueOf(isInheritedString);
  }
}
