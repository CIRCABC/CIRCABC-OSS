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

/**
 * Immutable-style data model representing a single user entry returned by a user search operation.
 *
 * <p>Each record carries the identifying and descriptive attributes of a CIRCABC user (login name,
 * first and last name, email address, membership profile and optional moniker). Instances are
 * typically produced by user search services and serialized into the JSON responses of the REST
 * layer.
 *
 * <p>The class overrides {@link #equals(Object)} and {@link #hashCode()} so that records can be
 * safely deduplicated in collections; the {@code moniker} attribute is treated as optional in these
 * comparisons because it may be {@code null} when the record originates from
 * {@code LuceneUserServiceImpl} (see DIGITCIRCABC-5063).
 */
public class SearchResultRecord implements Serializable {

  /**
   * Serialization version identifier for this {@link Serializable} value object.
   */
  private static final long serialVersionUID = -7895552530213168492L;
  /** The unique login name (user id) of the user. */
  private String userName;
  /** The first (given) name of the user. */
  private String firstName;
  /** The last (family) name of the user. */
  private String lastName;
  /** The email address of the user. */
  private String email;
  /** The membership profile associated with the user; defaults to {@code "N/A"} when unknown. */
  private String profile;
  /**
   * An optional display alias for the user. May be {@code null} when the record is produced by
   * {@code LuceneUserServiceImpl} (see DIGITCIRCABC-5063).
   */
  private String moniker;

  /**
   * Creates a fully populated search result record. The {@code profile} is initialized to the
   * placeholder value {@code "N/A"}.
   *
   * @param userName the unique login name (user id) of the user
   * @param moniker the optional display alias of the user; may be {@code null}
   * @param firstName the first (given) name of the user
   * @param lastName the last (family) name of the user
   * @param email the email address of the user
   */
  public SearchResultRecord(
    final String userName,
    final String moniker,
    final String firstName,
    final String lastName,
    final String email
  ) {
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.moniker = moniker;
    this.profile = "N/A";
  }

  /**
   * Creates a search result record without a moniker. The {@code moniker} is set to {@code null}
   * and the {@code profile} is initialized to the placeholder value {@code "N/A"}.
   *
   * @param uid the unique login name (user id) of the user
   * @param firstName the first (given) name of the user
   * @param lastName the last (family) name of the user
   * @param email the email address of the user
   */
  public SearchResultRecord(
    final String uid,
    final String firstName,
    final String lastName,
    final String email
  ) {
    this(uid, null, firstName, lastName, email);
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)} based on the record's attributes.
   *
   * <p>The {@code moniker} contributes to the hash only when it is not {@code null}, since it may be
   * absent for records coming from {@code LuceneUserServiceImpl} (see DIGITCIRCABC-5063).
   *
   * @return the hash code for this record
   */
  @Override
  public int hashCode() {
    //DIGITCIRCABC-5063 - moniker is null if the SearchResult is coming from LuceneUserServiceImpl
    String sb =
      this.email +
      this.firstName +
      this.userName +
      this.lastName +
      (this.moniker != null ? this.moniker : "") +
      this.profile;
    return sb.hashCode();
  }

  /**
   * Compares this record with another object for equality based on user name, first and last name,
   * email and profile.
   *
   * <p>The {@code moniker} is only compared when it is present (non-{@code null}) on both records,
   * because it may be absent for records coming from {@code LuceneUserServiceImpl} (see
   * DIGITCIRCABC-5063).
   *
   * @param obj the object to compare with
   * @return {@code true} if {@code obj} is a {@code SearchResultRecord} with equal attributes,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SearchResultRecord compareTo) {
      if (!isEqual(this.email, compareTo.email)) {
        return false;
      }

      if (!isEqual(this.firstName, compareTo.firstName)) {
        return false;
      }

      if (!isEqual(this.userName, compareTo.userName)) {
        return false;
      }

      if (!isEqual(this.lastName, compareTo.lastName)) {
        return false;
      }

      //DIGITCIRCABC-5063 - moniker is null if the SearchResult is coming from LuceneUserServiceImpl
      if (
        this.moniker != null &&
        compareTo.moniker != null &&
        !isEqual(this.moniker, compareTo.moniker)
      ) {
        return false;
      }

      return isEqual(this.profile, compareTo.profile);
    }
    return false;
  }

  /**
   * Null-safe equality check for two strings.
   *
   * @param a the first string; may be {@code null}
   * @param b the second string; may be {@code null}
   * @return {@code true} if both are {@code null} or equal, {@code false} otherwise
   */
  private boolean isEqual(final String a, final String b) {
    if ((a == null && b != null) || (a != null && b == null)) {
      return false;
    }
    return (a == null) || (a.equals(b));
  }

  /**
   * Sets the unique login name (user id) of the user.
   *
   * @param userName the user name to set
   */
  public void setUserName(final String userName) {
    this.userName = userName;
  }

  /**
   * @return the firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName the firstName to set
   */
  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return the lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param lastName the lastName to set
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /**
   * @return the profile
   */
  public String getProfile() {
    return profile;
  }

  /**
   * @param profile the profile to set
   */
  public void setProfile(final String profile) {
    this.profile = profile;
  }

  /**
   * @return the username
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @return the email
   */
  public final String getEmail() {
    return email;
  }

  /**
   * @param email the email to set
   */
  public final void setEmail(final String email) {
    this.email = email;
  }

  /**
   * Returns a human-readable representation combining the user's full name, optional moniker and
   * email address, e.g. {@code "John Doe - jdoe (john.doe@example.org)"}.
   *
   * @return a display string describing this record
   * @see java.lang.String#toString()
   */
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(firstName).append(' ').append(lastName);

    if (moniker != null) {
      builder.append(" - ").append(moniker);
    }

    builder.append(" (").append(email).append(')');

    return builder.toString();
  }

  /**
   * @return the moniker
   */
  public final String getMoniker() {
    return moniker;
  }

  /**
   * @param moniker the moniker to set
   */
  public final void setMoniker(final String moniker) {
    this.moniker = moniker;
  }
}
