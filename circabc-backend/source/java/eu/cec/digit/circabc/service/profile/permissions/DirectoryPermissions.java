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
 * Enumeration representing the permissions associated to the directory service Enumeration are used
 * to decribe all the existing permissions in the directory
 *
 * @author Philippe Dubois
 */
public enum DirectoryPermissions {
    DIRADMIN("DirAdmin"),
    DIRMANAGEMEMBERS("DirManageMembers"),
    DIRACCESS("DirAccess"),
    DIRNOACCESS("DirNoAccess");

    static HashSet<DirectoryPermissions> dirPermissions = null;

    String dirPermissionString;

    DirectoryPermissions(String permission) {
        dirPermissionString = permission;
    }

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
                    "No enum const class with permission string " + permiString);
        } else {
            return match;
        }
    }

    protected static void init() {
        dirPermissions = new HashSet<>();
        Collections.addAll(dirPermissions, DirectoryPermissions.values());
    }

    /**
     * return an Set representing the permission list.
     *
     * @return Set of LibraryPermissions
     */
    public static HashSet<DirectoryPermissions> getPermissions() {
        if (dirPermissions == null) {
            init();
        }
        return (HashSet<DirectoryPermissions>) dirPermissions.clone();
    }

    public static DirectoryPermissions[] minimalValues() {
        return new DirectoryPermissions[]{DIRACCESS, DIRNOACCESS};
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (dirPermissions == null) {
            init();
        }
        return dirPermissionString;
    }
}
