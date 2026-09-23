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
 * Enumeration describing the possible join permissions that can be granted on a resource.
 *
 * <p>A join permission controls whether a user is allowed to join (become a member of) the
 * associated element. Each enum constant is bound to a canonical permission string used by the
 * underlying Alfresco permission model, allowing conversion between the string representation and
 * the corresponding enum value.
 *
 * @author Yanick Pignot
 */
public enum JoinPermissions {
  /** Permission allowing a user to join the associated element. */
  JOIN("Join"),
  /** Permission denying a user the ability to join the associated element. */
  NOJOIN("NoJoin");

  /**
   * Lazily initialized cache holding all available {@link JoinPermissions} values. Populated by
   * {@link #init()} on first access.
   */
  @SuppressWarnings("java:S1700")
  static HashSet<JoinPermissions> joinPermissions = null;

  /** The canonical permission string associated with this enum constant. */
  String joinPermissionString;

  /**
   * Creates a join permission bound to the given canonical permission string.
   *
   * @param permission the permission string identifying this join permission
   */
  JoinPermissions(String permission) {
    joinPermissionString = permission;
  }

  /**
   * Lazily initializes the internal cache of all {@link JoinPermissions} values.
   */
  protected static void init() {
    joinPermissions = new HashSet<>();
    Collections.addAll(joinPermissions, JoinPermissions.values());
  }

  /**
   * Resolves the {@link JoinPermissions} constant matching the given permission string.
   *
   * @param permiString the permission string to look up
   * @return the matching {@link JoinPermissions} constant
   * @throws IllegalArgumentException if no constant matches the given permission string
   */
  public static JoinPermissions withPermissionString(String permiString) {
    JoinPermissions match = null;

    for (JoinPermissions permission : getPermissions()) {
      if (permission.joinPermissionString.equals(permiString)) {
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
   * Returns a copy of the set containing all available join permissions, initializing the internal
   * cache on first access.
   *
   * @return a {@link Set} of all {@link JoinPermissions} values
   */
  @SuppressWarnings("unchecked")
  public static Set<JoinPermissions> getPermissions() {
    if (joinPermissions == null) {
      init();
    }
    return (HashSet<JoinPermissions>) joinPermissions.clone();
  }

  /**
   * Returns the canonical permission string associated with this join permission.
   *
   * @return the permission string of this constant
   */
  @Override
  public String toString() {
    if (joinPermissions == null) {
      init();
    }
    return joinPermissionString;
  }
}
