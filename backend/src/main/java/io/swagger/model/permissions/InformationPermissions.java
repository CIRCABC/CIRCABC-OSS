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
 * Enumeration of the permissions available for the Information service in CIRCABC.
 *
 * <p>Each enum constant is bound to the string identifier used by Alfresco to define the permission
 * in the {@code permissionDefinitions.xml} configuration file. The enum provides helper methods to
 * look up a permission from its string value, to obtain the full set of permissions, and to expose
 * the minimal (access / no-access) subset.
 *
 * @author Yanick Pignot
 */
public enum InformationPermissions {
  /** Full administrative rights over the Information service. */
  INFADMIN("InfAdmin"),
  /** Management rights over the Information service (create / edit content). */
  INFMANAGE("InfManage"),
  /** Read access to the Information service. */
  INFACCESS("InfAccess"),
  /** Explicit denial of access to the Information service. */
  INFNOACCESS("InfNoAccess");

  /**
   * Lazily initialised cache holding the full set of permission values. Populated on first access
   * by {@link #init()}.
   */
  @SuppressWarnings("java:S1700")
  static HashSet<InformationPermissions> informationPermissions = null;

  /** The Alfresco permission identifier associated with this enum constant. */
  protected String permissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  InformationPermissions(String value) {
    permissionString = value;
  }

  /**
   * Resolves the enum constant matching the given Alfresco permission identifier.
   *
   * @param permiString the permission string to look up (e.g. {@code "InfAdmin"}).
   * @return the matching {@link InformationPermissions} constant.
   * @throws IllegalArgumentException if no constant matches the supplied permission string.
   */
  public static InformationPermissions withPermissionString(
    String permiString
  ) {
    InformationPermissions match = null;

    for (InformationPermissions permission : getPermissions()) {
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
    informationPermissions = new HashSet<>();
    Collections.addAll(informationPermissions, InformationPermissions.values());
  }

  /**
   * return an Set representing the permission list.
   *
   * @return Set of LibraryPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<InformationPermissions> getPermissions() {
    if (informationPermissions == null) {
      init();
    }
    return (HashSet<InformationPermissions>) informationPermissions.clone();
  }

  /**
   * Returns the minimal set of permissions, limited to the access and no-access values.
   *
   * @return an array containing {@link #INFACCESS} and {@link #INFNOACCESS}.
   */
  public static InformationPermissions[] minimalValues() {
    return new InformationPermissions[] { INFACCESS, INFNOACCESS };
  }

  /**
   * Returns the Alfresco permission identifier associated with this permission.
   *
   * @return the permission string value.
   */
  @Override
  public String toString() {
    if (informationPermissions == null) {
      init();
    }
    return permissionString;
  }
}
