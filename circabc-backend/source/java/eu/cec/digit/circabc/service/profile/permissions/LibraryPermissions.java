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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Enumeration representing the permissions associated to the library service Enumeration where used
 * to decribe all the existing permissions in the library
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

    protected static final List<String> orderedLibraryPermissions =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "LibNoAccess",
                            "LibAccess",
                            "LibManageOwn",
                            "LibEditOnly",
                            "LibFullEdit",
                            "LibAdmin"));
    static HashSet<LibraryPermissions> libPermissions = null;
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
                    "No enum const class with permission string " + permiString);
        } else {
            return match;
        }
    }

    /**
     * return an List representing the permission list.
     *
     * @return List of LibraryPermissions
     */
    public static HashSet<LibraryPermissions> getPermissions() {
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

    public static LibraryPermissions[] minimalValues() {
        return new LibraryPermissions[]{LIBACCESS, LIBNOACCESS};
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (libPermissions == null) {
            init();
        }
        return libraryPermissionString;
    }
}
