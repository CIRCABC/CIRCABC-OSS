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
package eu.cec.digit.circabc.service.profile.permissions;

import java.util.Collections;
import java.util.HashSet;

/**
 * Enumeration representing the permissions associated to the Information service Enumeration where
 * used to decribe all the existing permissions in the CircaBC
 *
 * @author Yanick Pignot
 */
public enum InformationPermissions {
  INFADMIN("InfAdmin"),
  INFMANAGE("InfManage"),
  INFACCESS("InfAccess"),
  INFNOACCESS("InfNoAccess");

  static HashSet<InformationPermissions> informationPermissions = null;
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
  public static HashSet<InformationPermissions> getPermissions() {
    if (informationPermissions == null) {
      init();
    }
    return (HashSet<InformationPermissions>) informationPermissions.clone();
  }

  public static InformationPermissions[] minimalValues() {
    return new InformationPermissions[] { INFACCESS, INFNOACCESS };
  }

  /**
   * Return the string value associated to the permission
   */
  public String toString() {
    if (informationPermissions == null) {
      init();
    }
    return permissionString;
  }
}
