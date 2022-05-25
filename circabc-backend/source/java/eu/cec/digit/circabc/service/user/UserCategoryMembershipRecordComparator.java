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

public class UserCategoryMembershipRecordComparator
        implements Comparator<UserCategoryMembershipRecord>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -553338713603165294L;

    private UserCategoryMembershipRecordComparator() {
    }

    public static Comparator<UserCategoryMembershipRecord> getInstance() {
        return UserIGMembershipRecordComparatorHolder.INSTANCE;
    }

    public int compare(UserCategoryMembershipRecord first, UserCategoryMembershipRecord second) {

        return first.getCategory().compareToIgnoreCase(second.getCategory());
    }

    private static class UserIGMembershipRecordComparatorHolder {

        public static final UserCategoryMembershipRecordComparator INSTANCE =
                new UserCategoryMembershipRecordComparator();
    }
}
