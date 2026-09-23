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
 * Enumeration representing the permissions associated to the user information Enumeration where
 * used to decribe all the existing permissions in the CircaUserAspect
 *
 * @author Clinckart Stephane
 */
public enum UserPermissions {
  /**
   * Permission granting read access to a user's personal information, backed by the
   * {@code "PersonInfoRead"} permission string defined in {@code permissionDefinitions.xml}.
   */
  PERSONINFOREAD("PersonInfoRead");

  /**
   * Lazily initialised cache holding the full set of {@link UserPermissions} values. Populated by
   * {@link #init()} and remains {@code null} until first accessed.
   */
  static HashSet<UserPermissions> circaUserPermissions = null;

  /**
   * The underlying permission string associated with this enumeration value, as declared in
   * {@code permissionDefinitions.xml}.
   */
  protected String permissionString;

  /**
   * Constructor initialising the string value of the permission. The String values will be defined
   * in the file permissionDefinitions.xml
   *
   * @param value string value associated to the enumeration value.
   */
  UserPermissions(String value) {
    permissionString = value;
  }

  /**
   * initialise the list of permissions
   */
  protected static void init() {
    circaUserPermissions = new HashSet<>();
    Collections.addAll(circaUserPermissions, UserPermissions.values());
  }

  /**
   * return an Set representing the permission list.
   *
   * @return Set of CircabcPermissions
   */
  @SuppressWarnings("unchecked")
  public static Set<UserPermissions> getPermissions() {
    if (circaUserPermissions == null) {
      init();
    }
    return (HashSet<UserPermissions>) circaUserPermissions.clone();
  }

  /**
   * Return the string value associated to the permission
   *
   * @return the permission string associated with this enumeration value.
   */
  @Override
  public String toString() {
    if (circaUserPermissions == null) {
      init();
    }
    return permissionString;
  }
}
