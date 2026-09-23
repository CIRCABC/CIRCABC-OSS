/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package io.swagger.model.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enumeration representing the permissions associated to the Event service Enumeration where used
 * to decribe all the existing permissions in the CircaBC
 *
 * @author Pignot Yanick
 */
public enum EventPermissions {
  /** Full administrative access to the Event service. */
  EVEADMIN("EveAdmin"),
  /** Standard read access to the Event service. */
  EVEACCESS("EveAccess"),
  /** No access to the Event service. */
  EVENOACCESS("EveNoAccess");

  /**
   * Lazily initialised cache holding the set of all {@link EventPermissions} values. Populated by
   * {@link #init()} on first access.
   */
  @SuppressWarnings("java:S1700")
  static HashSet<EventPermissions> eventPermissions = null;

  /**
   * The Alfresco permission identifier associated with this enumeration value, as defined in
   * {@code permissionDefinitions.xml}.
   */
  protected String permissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  EventPermissions(String value) {
    permissionString = value;
  }

  /**
   * Resolves the enumeration value whose Alfresco permission identifier matches the given string.
   *
   * @param permiString the Alfresco permission identifier to look up.
   * @return the matching {@link EventPermissions} value.
   * @throws IllegalArgumentException if no enumeration value matches the given permission string.
   */
  public static EventPermissions withPermissionString(String permiString) {
    EventPermissions match = null;

    for (EventPermissions permission : getPermissions()) {
      if (permission.permissionString.equals(permiString)) {
        match = permission;
        break;
      }
    }
    if (match == null) {
      throw new IllegalArgumentException(
        "No enum const class with permission string " + permiString
      );
    } else {
      return match;
    }
  }

  /**
   * initialise the list of permissions
   */
  protected static void init() {
    eventPermissions = new HashSet<>();
    Collections.addAll(eventPermissions, EventPermissions.values());
  }

  /**
   * return an Set representing the permission list.
   *
   * @return Set of LibraryPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<EventPermissions> getPermissions() {
    if (eventPermissions == null) {
      init();
    }
    return (HashSet<EventPermissions>) eventPermissions.clone();
  }

  /**
   * Returns the subset of permissions considered the minimal set for the Event service.
   *
   * @return an array containing {@link #EVEACCESS} and {@link #EVENOACCESS}.
   */
  public static EventPermissions[] minimalValues() {
    return new EventPermissions[] { EVEACCESS, EVENOACCESS };
  }

  /**
   * Returns the string value associated with the permission.
   *
   * @return the Alfresco permission identifier for this enumeration value.
   */
  @Override
  public String toString() {
    if (eventPermissions == null) {
      init();
    }
    return permissionString;
  }
}
