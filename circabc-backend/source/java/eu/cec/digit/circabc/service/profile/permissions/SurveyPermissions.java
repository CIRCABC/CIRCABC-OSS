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
 * Enumeration representing the permissions associated to the survey service Enumeration where used
 * to decribe all the existing permissions in the survey
 *
 * @author Yanick Pignot
 */
public enum SurveyPermissions {
    SURADMIN("SurAdmin"),
    SURENCODE("SurEncode"),
    SURACCESS("SurAccess"),
    SURNOACCESS("SurNoAccess");

    static HashSet<SurveyPermissions> surveyPermissions = null;
    protected String surveyPermissionString;

    /**
     * Constructor initialising the string value of the permission. The String values will be defined
     * in the file permissionDefinitions.xml
     *
     * @param value string value associated to the enumeration value.
     */
    SurveyPermissions(String value) {
        surveyPermissionString = value;
    }

    /**
     * initialise the list of permissions
     */
    protected static void init() {
        surveyPermissions = new HashSet<>();
        Collections.addAll(surveyPermissions, SurveyPermissions.values());
    }

    public static SurveyPermissions withPermissionString(String permiString) {
        SurveyPermissions match = null;

        for (SurveyPermissions permission : getPermissions()) {
            if (permission.surveyPermissionString.equals(permiString)) {
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
    public static HashSet<SurveyPermissions> getPermissions() {
        if (surveyPermissions == null) {
            init();
        }
        return (HashSet<SurveyPermissions>) surveyPermissions.clone();
    }

    public static SurveyPermissions[] minimalValues() {
        return new SurveyPermissions[]{SURACCESS, SURNOACCESS};
    }

    /**
     * Return the string value associated to the permission
     */
    public String toString() {
        if (surveyPermissions == null) {
            init();
        }
        return surveyPermissionString;
    }
}
