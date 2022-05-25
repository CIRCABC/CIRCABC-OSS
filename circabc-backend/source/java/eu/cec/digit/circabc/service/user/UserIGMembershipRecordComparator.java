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
package eu.cec.digit.circabc.service.user;

import java.io.Serializable;
import java.util.Comparator;

public class UserIGMembershipRecordComparator
        implements Comparator<UserIGMembershipRecord>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8330903237674384518L;

    private UserIGMembershipRecordComparator() {
    }

    public static Comparator<UserIGMembershipRecord> getInstance() {
        return UserIGMembershipRecordComparatorHolder.INSTANCE;
    }

    public int compare(UserIGMembershipRecord first, UserIGMembershipRecord second) {
        final String firstString = first.getCategoryTitle() + first.getInterestGroupTitle();
        final String secondString = second.getCategoryTitle() + second.getInterestGroupTitle();
        return firstString.compareToIgnoreCase(secondString);
    }

    private static class UserIGMembershipRecordComparatorHolder {

        public static final UserIGMembershipRecordComparator INSTANCE =
                new UserIGMembershipRecordComparator();
    }
}
