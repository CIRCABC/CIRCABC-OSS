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
package io.swagger.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * {@link Comparator} for {@link UserIGMembershipRecord} instances.
 *
 * <p>Records are ordered case-insensitively by the concatenation of their category title and
 * interest group title, so that a user's memberships can be listed grouped by category and then by
 * interest group.
 *
 * <p>The comparator is stateless and is exposed as a lazily-initialised, thread-safe singleton via
 * {@link #getInstance()} (using the initialization-on-demand holder idiom). It implements {@link
 * Serializable} so it can be used with serializable collections such as sorted sets and maps.
 */
@SuppressWarnings("java:S6548") // Stateless comparator — singleton is appropriate
public class UserIGMembershipRecordComparator
  implements Comparator<UserIGMembershipRecord>, Serializable
{

  /** Serialization version identifier for this {@link Serializable} comparator. */
  private static final long serialVersionUID = -8330903237674384518L;

  /** Private constructor to enforce singleton access through {@link #getInstance()}. */
  private UserIGMembershipRecordComparator() {}

  /**
   * Returns the shared, thread-safe singleton instance of this comparator.
   *
   * @return the singleton {@link Comparator} for {@link UserIGMembershipRecord} instances
   */
  public static Comparator<UserIGMembershipRecord> getInstance() {
    return UserIGMembershipRecordComparatorHolder.INSTANCE;
  }

  /**
   * Compares two membership records case-insensitively by their category title followed by their
   * interest group title.
   *
   * @param first the first membership record to compare
   * @param second the second membership record to compare
   * @return a negative integer, zero, or a positive integer as the first record is ordered before,
   *     equal to, or after the second record
   */
  public int compare(
    UserIGMembershipRecord first,
    UserIGMembershipRecord second
  ) {
    final String firstString =
      first.getCategoryTitle() + first.getInterestGroupTitle();
    final String secondString =
      second.getCategoryTitle() + second.getInterestGroupTitle();
    return firstString.compareToIgnoreCase(secondString);
  }

  private static class UserIGMembershipRecordComparatorHolder {

    public static final UserIGMembershipRecordComparator INSTANCE =
      new UserIGMembershipRecordComparator();
  }
}
