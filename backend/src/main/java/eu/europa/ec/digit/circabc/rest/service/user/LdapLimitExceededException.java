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
package eu.europa.ec.digit.circabc.rest.service.user;

/**
 * Unchecked exception signalling that an LDAP query returned more results than the configured
 * limit allows (i.e. the LDAP server's size limit was exceeded).
 *
 * <p>It is typically thrown during user lookup/search operations against the LDAP directory so that
 * callers can prompt for a more specific query instead of returning a truncated or oversized result
 * set.
 */
public class LdapLimitExceededException extends RuntimeException {

  /** Serialization version identifier for this exception type. */
  private static final long serialVersionUID = -6026579913500008873L;

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message the detail message describing the limit that was exceeded
   */
  public LdapLimitExceededException(final String message) {
    super(message);
  }

  /** Creates a new exception with no detail message or cause. */
  public LdapLimitExceededException() {
    super();
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message the detail message describing the limit that was exceeded
   * @param cause the underlying cause of this exception
   */
  public LdapLimitExceededException(
    final String message,
    final Throwable cause
  ) {
    super(message, cause);
  }

  /**
   * Creates a new exception wrapping the given underlying cause.
   *
   * @param cause the underlying cause of this exception
   */
  public LdapLimitExceededException(final Throwable cause) {
    super(cause);
  }
}
