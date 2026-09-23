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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enumeration of the permission roles that can be granted on the Library service of an Interest
 * Group.
 *
 * <p>Each enum constant is bound to the permission name as declared in Alfresco's {@code
 * permissionDefinitions.xml} configuration file (for example {@code LibAdmin}, {@code LibAccess}).
 * The constants are ordered from the most restrictive ({@link #LIBNOACCESS}) to the most privileged
 * ({@link #LIBADMIN}) through {@link #orderedLibraryPermissions}, which is convenient when
 * comparing or presenting permission levels.
 *
 * <p>Utility methods allow resolving an enum constant from its Alfresco permission string, obtaining
 * the full set of permissions, retrieving the ordered permission names, and getting the minimal set
 * of permissions usable as access defaults.
 *
 * @author Philippe Dubois
 */
public enum LibraryPermissions {
  LIBADMIN("LibAdmin"),
  LIBFULLEDIT("LibFullEdit"),
  LIBEDITONLY("LibEditOnly"),
  LIBMANAGEOWN("LibManageOwn"),
  LIBACCESS("LibAccess"),
  LIBNOACCESS("LibNoAccess");

  /**
   * Permission names ordered from the least privileged ({@code LibNoAccess}) to the most privileged
   * ({@code LibAdmin}). The list is immutable.
   */
  protected static final List<String> orderedLibraryPermissions =
    Collections.unmodifiableList(
      Arrays.asList(
        "LibNoAccess",
        "LibAccess",
        "LibManageOwn",
        "LibEditOnly",
        "LibFullEdit",
        "LibAdmin"
      )
    );
  /** Lazily initialised cache holding all library permission constants. */
  static HashSet<LibraryPermissions> libPermissions = null;

  /** Alfresco permission name associated with this enum constant. */
  protected String libraryPermissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  LibraryPermissions(String value) {
    libraryPermissionString = value;
  }

  /**
   * initialise the list of permissions
   */
  protected static void init() {
    libPermissions = new HashSet<>();
    Collections.addAll(libPermissions, LibraryPermissions.values());
  }

  /**
   * Resolves the enum constant whose Alfresco permission name matches the given string.
   *
   * @param permiString the Alfresco permission name to look up (for example {@code "LibAdmin"}).
   * @return the matching {@link LibraryPermissions} constant.
   * @throws IllegalArgumentException if no constant is associated with the given permission string.
   */
  public static LibraryPermissions withPermissionString(String permiString) {
    LibraryPermissions match = null;

    for (LibraryPermissions permission : getPermissions()) {
      if (permission.libraryPermissionString.equals(permiString)) {
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
   * @return List of LibraryPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<LibraryPermissions> getPermissions() {
    if (libPermissions == null) {
      init();
    }
    return (HashSet<LibraryPermissions>) libPermissions.clone();
  }

  /**
   * return an List representing the permission list.
   *
   * @return List of LibraryPermissions
   */
  public static List<String> getOrderedLibraryPermissions() {
    return orderedLibraryPermissions;
  }

  /**
   * Returns the minimal set of permissions, namely the access and no-access levels.
   *
   * @return an array containing {@link #LIBACCESS} and {@link #LIBNOACCESS}.
   */
  public static LibraryPermissions[] minimalValues() {
    return new LibraryPermissions[] { LIBACCESS, LIBNOACCESS };
  }

  /**
   * Returns the Alfresco permission name associated with this permission.
   *
   * @return the permission string value of this enum constant.
   */
  @Override
  public String toString() {
    if (libPermissions == null) {
      init();
    }
    return libraryPermissionString;
  }
}
