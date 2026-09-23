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
 * Enumeration of the permission levels that can be granted on the CIRCABC newsgroup (discussion)
 * service.
 *
 * <p>Each enum constant is paired with the string identifier used to reference the permission in the
 * Alfresco permission model file {@code permissionDefinitions.xml}. The class also provides helpers
 * to look up a constant from its string identifier, to obtain the full set of permissions, and to
 * expose the minimal subset of permissions that can be assigned to end users.
 *
 * @author Clinckart Stephane
 */
public enum NewsGroupPermissions {
  /** Full administrative control over the newsgroup service. */
  NWSADMIN("NwsAdmin"),
  /** Ability to moderate posts (approve, reject or edit contributions). */
  NWSMODERATE("NwsModerate"),
  /** Ability to publish new posts in the newsgroup. */
  NWSPOST("NwsPost"),
  /** Read-only access to the newsgroup content. */
  NWSACCESS("NwsAccess"),
  /** Explicit denial of any access to the newsgroup. */
  NWSNOACCESS("NwsNoAccess");

  /** Lazily initialised cache holding all newsgroup permission values. */
  @SuppressWarnings("java:S1700")
  static HashSet<NewsGroupPermissions> newsgroupPermissions = null;

  /** The string identifier of the permission as declared in {@code permissionDefinitions.xml}. */
  protected String newsgroupPermissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  NewsGroupPermissions(String value) {
    newsgroupPermissionString = value;
  }

  /**
   * Returns the enum constant whose associated string identifier matches the given value.
   *
   * @param permiString the permission string identifier to look up.
   * @return the matching {@link NewsGroupPermissions} constant.
   * @throws IllegalArgumentException if no constant is associated with the given string.
   */
  public static NewsGroupPermissions withPermissionString(String permiString) {
    NewsGroupPermissions match = null;

    for (NewsGroupPermissions permission : getPermissions()) {
      if (permission.newsgroupPermissionString.equals(permiString)) {
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
    newsgroupPermissions = new HashSet<>();
    Collections.addAll(newsgroupPermissions, NewsGroupPermissions.values());
  }

  /**
   * return an List representing the permission list.
   *
   * @return List of LibraryPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<NewsGroupPermissions> getPermissions() {
    if (newsgroupPermissions == null) {
      init();
    }
    return (HashSet<NewsGroupPermissions>) newsgroupPermissions.clone();
  }

  /**
   * Returns the subset of permissions that may be assigned to regular users of the newsgroup,
   * excluding administrative and moderation levels.
   *
   * @return an array containing the minimal assignable permissions.
   */
  public static NewsGroupPermissions[] minimalValues() {
    return new NewsGroupPermissions[] { NWSPOST, NWSACCESS, NWSNOACCESS };
  }

  /**
   * Returns the string value associated to the permission, as used in the Alfresco permission
   * model.
   *
   * @return the permission string identifier.
   */
  @Override
  public String toString() {
    if (newsgroupPermissions == null) {
      init();
    }
    return newsgroupPermissionString;
  }
}
