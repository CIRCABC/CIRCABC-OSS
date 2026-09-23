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
 * Enumeration representing the permissions associated to the Visibility permission Enumeration are
 * used to decribe all the existing Visibility permissions
 *
 * @author clinckart
 */
public enum VisibilityPermissions {
  /** Grants visibility of the resource. */
  VISIBILITY("Visibility"),
  /** Denies visibility of the resource. */
  NOVISIBILITY("NoVisibility");

  /**
   * Lazily initialized cache holding all the available {@link VisibilityPermissions} values. Used
   * to serve {@link #getPermissions()} without recomputing the set on every call.
   */
  static HashSet<VisibilityPermissions> allPermissions = null;

  /** The Alfresco permission string associated with this enum constant. */
  String visibilityPermissionString;

  /**
   * Creates a visibility permission constant bound to the given Alfresco permission string.
   *
   * @param permission the underlying Alfresco permission string
   */
  VisibilityPermissions(String permission) {
    visibilityPermissionString = permission;
  }

  /**
   * Initializes the internal cache with all the declared {@link VisibilityPermissions} constants.
   */
  protected static void init() {
    allPermissions = new HashSet<>();
    Collections.addAll(allPermissions, VisibilityPermissions.values());
  }

  /**
   * Returns the {@link VisibilityPermissions} constant matching the given Alfresco permission
   * string.
   *
   * @param permiString the Alfresco permission string to look up
   * @return the matching {@link VisibilityPermissions} constant
   * @throws IllegalArgumentException if no constant matches the given permission string
   */
  public static VisibilityPermissions withPermissionString(String permiString) {
    VisibilityPermissions match = null;

    for (VisibilityPermissions permission : getPermissions()) {
      if (permission.visibilityPermissionString.equals(permiString)) {
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
   * return an List representing the permission list.
   *
   * @return List of JoinPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<VisibilityPermissions> getPermissions() {
    if (allPermissions == null) {
      init();
    }
    return (HashSet<VisibilityPermissions>) allPermissions.clone();
  }

  /**
   * Returns the Alfresco permission string associated to this permission.
   *
   * @return the underlying Alfresco permission string
   */
  @Override
  public String toString() {
    if (allPermissions == null) {
      init();
    }
    return visibilityPermissionString;
  }
}
