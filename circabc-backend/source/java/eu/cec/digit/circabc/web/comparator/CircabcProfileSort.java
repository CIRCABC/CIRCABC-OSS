/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.comparator;

import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.web.wai.dialog.profile.AccessProfileWrapper;

import java.util.Comparator;

/**
 * @author Clinckart Stephane
 */
public class CircabcProfileSort implements Comparator<AccessProfileWrapper> {

    private CircabcRootProfileManagerService circabcRootProfileManagerService;

    private CircabcProfileSort() {

    }

    public CircabcProfileSort(
            final CircabcRootProfileManagerService circabcRootProfileManagerService) {
        this.circabcRootProfileManagerService = circabcRootProfileManagerService;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(AccessProfileWrapper profile1, AccessProfileWrapper profile2) {
        int pos1 = getPosition(profile1.getName());
        int pos2 = getPosition(profile2.getName());

        if (Integer.MAX_VALUE == pos1) {
            if (Integer.MAX_VALUE == pos2) {
                // Two dynamic profiles
                return profile1.getName().compareTo(profile2.getName());
            }
        }
        if (pos1 < pos2) {
            return -1;
        }
        if (pos1 == pos2) {
            return 0;
        } else
        // pos1 > pos2
        {
            return 1;
        }
    }

    private int getPosition(final String profile) {
        if (IGRootProfileManagerService.Profiles.IGLEADER.equals(profile)) {
            return 1;
        }
        if (IGRootProfileManagerService.Profiles.AUTHOR.equals(profile)) {
            return 2;
        }
        if (IGRootProfileManagerService.Profiles.CONTRIBUTOR.equals(profile)) {
            return 3;
        }
        if (IGRootProfileManagerService.Profiles.REVIEWER.equals(profile)) {
            return 4;
        }
        if (IGRootProfileManagerService.Profiles.SECRETARY.equals(profile)) {
            return 5;
        }
        if (IGRootProfileManagerService.Profiles.ACCESS.equals(profile)) {
            return 6;
        }
        if (circabcRootProfileManagerService.getPrefixedAllCircaUsersGroupName().equals(profile)) {
            return 7;
        }
        if (CircabcConstant.GUEST_AUTHORITY.equals(profile)) {
            return 8;
        }

        return Integer.MAX_VALUE;
    }

}
