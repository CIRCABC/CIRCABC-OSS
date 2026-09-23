package io.swagger.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that orders {@link SearchResultRecord} instances alphabetically by
 * their last name.
 *
 * <p>The comparison is {@code null}-tolerant: records whose last name is
 * {@code null} are ordered before records that have a last name, and two records
 * with {@code null} last names are considered equal. Non-{@code null} last names
 * are compared using {@link String#compareTo(String)}.
 *
 * <p>This comparator is stateless and is exposed as a lazily initialised,
 * thread-safe singleton via {@link #getInstance()} (using the initialisation-on-demand
 * holder idiom). It implements {@link Serializable} so it can be used with
 * serialisable collections such as {@code TreeMap} or {@code TreeSet}.
 */
@SuppressWarnings("java:S6548") // Stateless comparator — singleton is appropriate
public class SearchResultRecordComparator
  implements Comparator<SearchResultRecord>, Serializable
{

  /**
   * Serialisation version identifier used to verify compatibility during
   * deserialisation.
   */
  private static final long serialVersionUID = 5428412809586963529L;

  /**
   * Private constructor preventing external instantiation; use
   * {@link #getInstance()} to obtain the shared singleton instance.
   */
  private SearchResultRecordComparator() {}

  /**
   * Returns the shared singleton instance of this comparator.
   *
   * @return the singleton {@code SearchResultRecordComparator} instance
   */
  public static SearchResultRecordComparator getInstance() {
    return SearchResultRecordComparatorHolder.INSTANCE;
  }

  /**
   * Compares two search result records by their last name.
   *
   * <p>{@code null} last names are ordered before non-{@code null} last names;
   * two {@code null} last names are treated as equal.
   *
   * @param first the first record to compare
   * @param second the second record to compare
   * @return a negative integer, zero, or a positive integer as the first
   *     record's last name is less than, equal to, or greater than the second
   *     record's last name
   */
  @Override
  public int compare(SearchResultRecord first, SearchResultRecord second) {
    String firstLast = first.getLastName();
    String secondLast = second.getLastName();
    if (firstLast == null) return secondLast == null ? 0 : -1;
    if (secondLast == null) return 1;
    return firstLast.compareTo(secondLast);
  }

  private static class SearchResultRecordComparatorHolder {

    public static final SearchResultRecordComparator INSTANCE =
      new SearchResultRecordComparator();
  }
}
