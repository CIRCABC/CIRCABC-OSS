package eu.europa.ec.digit.circabc.rest.exception;

/**
 * Runtime exception raised when an operation against the LDAP directory fails.
 *
 * <p>This unchecked exception signals problems encountered while accessing or
 * querying the LDAP backend (for example connection failures, lookup errors or
 * unexpected responses from the directory). Being a {@link RuntimeException}, it
 * can be thrown from within the REST/service layer without being declared in
 * method signatures and is typically translated into an appropriate HTTP error
 * response by the surrounding webscript infrastructure.
 */
public class LdapAccessException extends RuntimeException {

  /**
   * Creates a new exception with the given detail message.
   *
   * @param message a human-readable description of the LDAP access failure
   */
  public LdapAccessException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the LDAP access failure
   * @param cause the underlying exception that triggered this failure
   */
  public LdapAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
