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
 * Enumeration representing the permissions associated to the interest group service Enumeration are
 * used to decribe all the existing permissions in the interste group
 *
 * @author Stephane Clinckart
 */
public enum IgPermissions {
  /** Permission granting the ability to delete an interest group. */
  IGDELETE("IgDelete"),
  /** Permission granting the ability to create an interest group. */
  IGCREATE("IgCreate");

  /**
   * Lazily initialized cache holding all interest group permission values, populated by {@link
   * #init()} and returned (as a defensive copy) by {@link #getPermissions()}.
   */
  @SuppressWarnings("java:S1700")
  static HashSet<IgPermissions> igPermissions = null;

  /** The Alfresco/CIRCABC permission identifier associated with this enum constant. */
  String igPermissionString;

  /**
   * Creates an interest group permission constant bound to its permission identifier.
   *
   * @param permission the permission string identifier associated with this constant
   */
  IgPermissions(final String permission) {
    igPermissionString = permission;
  }

  /**
   * Lazily initializes the internal {@link #igPermissions} cache with all declared enum constants.
   */
  protected static void init() {
    igPermissions = new HashSet<>();
    Collections.addAll(igPermissions, IgPermissions.values());
  }

  /**
   * Resolves the enum constant that matches the given permission string.
   *
   * @param permiString the permission string identifier to look up
   * @return the matching {@link IgPermissions} constant
   * @throws IllegalArgumentException if no constant has the given permission string
   */
  public static IgPermissions withPermissionString(String permiString) {
    IgPermissions match = null;

    for (IgPermissions permission : getPermissions()) {
      if (permission.igPermissionString.equals(permiString)) {
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
   * Returns the complete set of interest group permissions, initializing the cache on first access.
   *
   * @return a copy of the {@link Set} of all {@link IgPermissions} constants
   */
  @SuppressWarnings("unchecked")
  public static Set<IgPermissions> getPermissions() {
    if (igPermissions == null) {
      init();
    }
    return (HashSet<IgPermissions>) igPermissions.clone();
  }

  /**
   * Returns the permission string identifier associated with this constant.
   *
   * @return the permission string value of this permission
   */
  @Override
  public String toString() {
    if (igPermissions == null) {
      init();
    }
    return igPermissionString;
  }
}
