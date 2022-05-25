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
 * Enumeration representing the permissions associated to the interest group service Enumeration are
 * used to decribe all the existing permissions in the interste group
 *
 * @author Stephane Clinckart
 */
public enum IgPermissions {
    IGDELETE("IgDelete"),
    IGCREATE("IgCreate");

    static HashSet<IgPermissions> igPermissions = null;

    String igPermissionString;

    IgPermissions(final String permission) {
        igPermissionString = permission;
    }

    protected static void init() {
        igPermissions = new HashSet<>();
        Collections.addAll(igPermissions, IgPermissions.values());
    }

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
                    "No enum const class with permission string " + permiString);
        } else {
            return match;
        }
    }

    /**
     * return an Set representing the permission list.
     *
     * @return Set of IgPermissions
     */
    public static HashSet<IgPermissions> getPermissions() {
        if (igPermissions == null) {
            init();
        }
        return (HashSet<IgPermissions>) igPermissions.clone();
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (igPermissions == null) {
            init();
        }
        return igPermissionString;
    }
}
