package eu.europa.ec.digit.circabc.rest;

/**
 * Holder for shared string constants used across the CIRCABC REST layer.
 *
 * <p>This is a non-instantiable utility class that centralises literal values (such as common
 * error messages) so they can be reused consistently by the various webscript endpoints and
 * services instead of being duplicated as inline literals.
 */
public final class RestConstants {

  /** Generic error message reused by REST endpoints when reporting an unexpected failure. */
  public static final String ERROR_OCCURRED = "Error occurred";

  /**
   * Private constructor to prevent instantiation of this constants-only utility class.
   */
  private RestConstants() {}
}
