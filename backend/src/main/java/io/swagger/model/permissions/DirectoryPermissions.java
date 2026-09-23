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
 * Enumeration of the permission levels that can be granted on the directory service.
 *
 * <p>Each enum constant maps to the Alfresco permission string used internally by CIRCABC to
 * describe the access rights a member (or profile) may hold on a directory. The constants are
 * ordered from the most privileged ({@link #DIRADMIN}) to the least privileged ({@link
 * #DIRNOACCESS}).
 *
 * @author Philippe Dubois
 */
public enum DirectoryPermissions {
  /** Full administrative rights over the directory. */
  DIRADMIN("DirAdmin"),
  /** Rights to manage the members of the directory. */
  DIRMANAGEMEMBERS("DirManageMembers"),
  /** Read access to the directory. */
  DIRACCESS("DirAccess"),
  /** No access to the directory. */
  DIRNOACCESS("DirNoAccess");

  /** Lazily initialised cache holding all enum values, used by {@link #getPermissions()}. */
  static HashSet<DirectoryPermissions> dirPermissions = null;

  /** The Alfresco permission string associated with this permission level. */
  String dirPermissionString;

  /**
   * Creates a permission constant bound to its Alfresco permission string.
   *
   * @param permission the Alfresco permission string represented by this constant
   */
  DirectoryPermissions(String permission) {
    dirPermissionString = permission;
  }

  /**
   * Resolves the enum constant matching the given Alfresco permission string.
   *
   * @param permiString the Alfresco permission string to look up
   * @return the {@link DirectoryPermissions} constant whose permission string equals {@code
   *     permiString}
   * @throws IllegalArgumentException if no constant matches the given permission string
   */
  public static DirectoryPermissions withPermissionString(String permiString) {
    DirectoryPermissions match = null;

    for (DirectoryPermissions permission : getPermissions()) {
      if (permission.dirPermissionString.equals(permiString)) {
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

  /** Populates the internal cache of permissions with all enum values. */
  protected static void init() {
    dirPermissions = new HashSet<>();
    Collections.addAll(dirPermissions, DirectoryPermissions.values());
  }

  /**
   * Returns a copy of the set of all directory permissions.
   *
   * @return a {@link Set} containing every {@link DirectoryPermissions} constant
   */
  @SuppressWarnings("unchecked")
  public static Set<DirectoryPermissions> getPermissions() {
    if (dirPermissions == null) {
      init();
    }
    return (HashSet<DirectoryPermissions>) dirPermissions.clone();
  }

  /**
   * Returns the minimal set of permissions, i.e. those expressing plain access or the absence of
   * access.
   *
   * @return an array containing {@link #DIRACCESS} and {@link #DIRNOACCESS}
   */
  public static DirectoryPermissions[] minimalValues() {
    return new DirectoryPermissions[] { DIRACCESS, DIRNOACCESS };
  }

  /**
   * Returns the Alfresco permission string associated with this permission level.
   *
   * @return the permission string bound to this constant
   */
  @Override
  public String toString() {
    if (dirPermissions == null) {
      init();
    }
    return dirPermissionString;
  }
}
