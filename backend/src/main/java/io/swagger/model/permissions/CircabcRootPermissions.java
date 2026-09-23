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
 * Enumeration of the root-level (application-wide) permissions available in CIRCABC.
 *
 * <p>Each constant maps to a permission name as declared in the Alfresco permission model file
 * {@code permissionDefinitions.xml}. This enum is used throughout the application to describe and
 * resolve the top-level CIRCABC permissions and to translate between the enum values and their
 * corresponding permission string representations.
 *
 * @author Clinckart Stephane
 */
public enum CircabcRootPermissions {
  /** Grants full CIRCABC administration rights. */
  CIRCABCADMIN("CircaBCAdmin"),
  /** Grants the ability to manage CIRCABC members. */
  CIRCABCMANAGEMEMBERS("CircaBCManageMembers"),
  /** Grants standard access to CIRCABC. */
  CIRCABCACCESS("CircaBCAccess"),
  /** Explicitly denies access to CIRCABC. */
  CIRCABCNOACCESS("CircaBCNoAccess");

  /** Lazily initialised cache holding the complete set of root permissions. */
  static HashSet<CircabcRootPermissions> circaBCPermissions = null;

  /** The permission name as defined in {@code permissionDefinitions.xml}. */
  protected String permissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  CircabcRootPermissions(String value) {
    permissionString = value;
  }

  /**
   * Resolves the enum constant whose permission string matches the supplied value.
   *
   * @param permiString the permission string to look up (as defined in {@code
   *     permissionDefinitions.xml}).
   * @return the matching {@link CircabcRootPermissions} constant.
   * @throws IllegalArgumentException if no constant has the given permission string.
   */
  public static CircabcRootPermissions withPermissionString(
    String permiString
  ) {
    CircabcRootPermissions match = null;

    for (CircabcRootPermissions permission : getPermissions()) {
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
    circaBCPermissions = new HashSet<>();
    Collections.addAll(circaBCPermissions, CircabcRootPermissions.values());
  }

  /**
   * return an Set representing the permission list.
   *
   * @return Set of CircabcPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<CircabcRootPermissions> getPermissions() {
    if (circaBCPermissions == null) {
      init();
    }
    return (HashSet<CircabcRootPermissions>) circaBCPermissions.clone();
  }

  /**
   * Returns the string value associated to the permission.
   *
   * @return the permission string as defined in {@code permissionDefinitions.xml}.
   */
  @Override
  public String toString() {
    if (circaBCPermissions == null) {
      init();
    }
    return permissionString;
  }
}
