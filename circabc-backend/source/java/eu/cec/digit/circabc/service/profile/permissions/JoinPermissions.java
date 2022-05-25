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
 * Enumeration representing the permissions associated to the join permission Enumeration are used
 * to decribe all the existing join permissions
 *
 * @author Yanick Pignot
 */
public enum JoinPermissions {
    JOIN("Join"),
    NOJOIN("NoJoin");

    static HashSet<JoinPermissions> joinPermissions = null;

    String joinPermissionString;

    JoinPermissions(String permission) {
        joinPermissionString = permission;
    }

    protected static void init() {
        joinPermissions = new HashSet<>();
        Collections.addAll(joinPermissions, JoinPermissions.values());
    }

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
                    "No enum const class with permission string " + permiString);
        } else {
            return match;
        }
    }

    /**
     * return an List representing the permission list.
     *
     * @return List of JoinPermissions
     */
    public static HashSet<JoinPermissions> getPermissions() {
        if (joinPermissions == null) {
            init();
        }
        return (HashSet<JoinPermissions>) joinPermissions.clone();
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (joinPermissions == null) {
            init();
        }
        return joinPermissionString;
    }
}
