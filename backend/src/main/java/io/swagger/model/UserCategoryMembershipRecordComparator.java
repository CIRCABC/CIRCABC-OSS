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
 * Comparator that orders {@link UserCategoryMembershipRecord} instances alphabetically by their
 * category name, ignoring case.
 *
 * <p>The class is stateless and is exposed as a lazily-initialized singleton through the
 * initialization-on-demand holder idiom (see {@link #getInstance()}). It implements {@link
 * Serializable} so that it can be used in contexts where the comparator itself may need to be
 * serialized (for example, as part of a serializable sorted collection).
 */
@SuppressWarnings("java:S6548") // Stateless comparator — singleton is appropriate
public class UserCategoryMembershipRecordComparator
  implements Comparator<UserCategoryMembershipRecord>, Serializable
{

  /** Serialization version identifier for this comparator. */
  private static final long serialVersionUID = -553338713603165294L;

  /** Private constructor to enforce singleton access through {@link #getInstance()}. */
  private UserCategoryMembershipRecordComparator() {}

  /**
   * Returns the shared singleton instance of this comparator.
   *
   * @return the singleton {@link Comparator} for {@link UserCategoryMembershipRecord} instances
   */
  public static Comparator<UserCategoryMembershipRecord> getInstance() {
    return UserIGMembershipRecordComparatorHolder.INSTANCE;
  }

  /**
   * Compares two membership records by their category name, ignoring case.
   *
   * @param first the first record to compare
   * @param second the second record to compare
   * @return a negative integer, zero, or a positive integer as the first record's category is
   *     lexicographically less than, equal to, or greater than the second record's category
   *     (case-insensitive)
   */
  public int compare(
    UserCategoryMembershipRecord first,
    UserCategoryMembershipRecord second
  ) {
    return first.getCategory().compareToIgnoreCase(second.getCategory());
  }

  /**
   * Holder for the lazily-initialized singleton instance, following the
   * initialization-on-demand holder idiom for thread-safe lazy initialization.
   */
  private static class UserIGMembershipRecordComparatorHolder {

    /** The single shared comparator instance. */
    public static final UserCategoryMembershipRecordComparator INSTANCE =
      new UserCategoryMembershipRecordComparator();
  }
}
