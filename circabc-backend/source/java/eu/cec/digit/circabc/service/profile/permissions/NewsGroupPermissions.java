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
 * Enumeration representing the permissions associated to the newsgroup service Enumeration where
 * used to decribe all the existing permissions in the newsgroup
 *
 * @author Clinckart Stephane
 */
public enum NewsGroupPermissions {
    NWSADMIN("NwsAdmin"),
    NWSMODERATE("NwsModerate"),
    NWSPOST("NwsPost"),
    NWSACCESS("NwsAccess"),
    NWSNOACCESS("NwsNoAccess");

    static HashSet<NewsGroupPermissions> newsgroupPermissions = null;
    protected String newsgroupPermissionString;

    /**
     * Constructor initialising the string value of the permission. The String values will be defined
     * in the file permissionDefinitions.xml
     *
     * @param value string value associated to the enumeration value.
     */
    NewsGroupPermissions(String value) {
        newsgroupPermissionString = value;
    }

    public static NewsGroupPermissions withPermissionString(String permiString) {
        NewsGroupPermissions match = null;

        for (NewsGroupPermissions permission : getPermissions()) {
            if (permission.newsgroupPermissionString.equals(permiString)) {
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
     * initialise the list of permissions
     */
    protected static void init() {
        newsgroupPermissions = new HashSet<>();
        Collections.addAll(newsgroupPermissions, NewsGroupPermissions.values());
    }

    /**
     * return an List representing the permission list.
     *
     * @return List of LibraryPermissions
     */
    public static HashSet<NewsGroupPermissions> getPermissions() {
        if (newsgroupPermissions == null) {
            init();
        }
        return (HashSet<NewsGroupPermissions>) newsgroupPermissions.clone();
    }

    public static NewsGroupPermissions[] minimalValues() {
        return new NewsGroupPermissions[]{NWSPOST, NWSACCESS, NWSNOACCESS};
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (newsgroupPermissions == null) {
            init();
        }
        return newsgroupPermissionString;
    }
}
