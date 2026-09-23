package io.swagger.util;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for validating and sanitizing email addresses in a way that is
 * broadly compliant with RFC 5321 and RFC 5322.
 *
 * <p>Validation is performed in several layers: rejection of null/empty or
 * whitespace-containing input, structural checks on the local and domain parts
 * (length limits, dot placement, domain label rules), a conservative
 * character-class regular expression, and a final check using
 * {@link jakarta.mail.internet.InternetAddress}. The regex intentionally avoids
 * nested repetitions to prevent catastrophic backtracking.
 *
 * <p>This class is stateless and cannot be instantiated; all members are static.
 *
 * @author Alain Morlet
 * @author Fabiano Colling
 */
public class EmailUtil {

  /** Logger used to record why individual addresses are rejected. */
  private static final Log logger = LogFactory.getLog(EmailUtil.class);

  /** Maximum allowed length of the local part (before the {@code @}), per RFC 5321 §4.5.3.1. */
  private static final int MAX_LOCAL_LENGTH = 64; // RFC 5321 §4.5.3.1
  /** Maximum allowed length of the domain part (after the {@code @}). */
  private static final int MAX_DOMAIN_LENGTH = 255;
  /** Maximum allowed length of the entire email address. */
  private static final int MAX_TOTAL_LENGTH = 254;
  /** Maximum allowed length of a single domain label, per RFC 1035 §2.3.4. */
  private static final int MAX_LABEL_LENGTH = 63; // RFC 1035 §2.3.4

  /** Set of characters permitted in the local part (RFC 5322 §3.2.3 atext); the dot is handled separately. */
  // RFC 5322 §3.2.3 atext (dot handled separately)
  private static final String LOCAL_ATEXT = "a-zA-Z0-9!#$%&'*+\\-/=?^_`{|}~";
  /**
   * Character-class-only pattern used as a first-pass structural filter for
   * email addresses. It deliberately contains no nested repetitions, so it is
   * not susceptible to catastrophic backtracking (SonarQube S5998/S5994);
   * finer RFC edge cases are handled by the surrounding structural checks.
   */
  // Simple character-class-only regex — structural validation handles RFC edge cases
  // No nested repetitions, so no backtracking risk (S5998/S5994)
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[" +
      LOCAL_ATEXT +
      "][" +
      LOCAL_ATEXT +
      ".]*@[a-zA-Z0-9][a-zA-Z0-9.\\-]*[a-zA-Z0-9]$"
  );

  private EmailUtil() {
    // Private constructor to hide the implicit public one
  }

  /**
   * Determines whether the supplied string is a valid email address.
   *
   * <p>The address is rejected if it is {@code null} or empty, contains
   * whitespace, fails the structural checks (local/domain parts, length limits,
   * dot and domain-label rules), does not match the character-class pattern, or
   * fails validation by {@link jakarta.mail.internet.InternetAddress}. Every
   * rejection is logged with the reason.
   *
   * @param emailAddress the email address to validate; may be {@code null}
   * @return {@code true} if the address passes all validation layers,
   *     {@code false} otherwise
   */
  public static boolean isValidEmailAddress(String emailAddress) {
    if (emailAddress == null || emailAddress.isEmpty()) {
      logger.warn("Email rejected: null or empty");
      return false;
    }

    if (containsWhitespace(emailAddress)) {
      logger.warn("Email rejected (contains whitespace): " + emailAddress);
      return false;
    }

    if (!hasValidStructure(emailAddress)) {
      return false;
    }

    if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
      logger.warn("Email rejected (pattern mismatch): " + emailAddress);
      return false;
    }

    return validateWithInternetAddress(emailAddress);
  }

  private static boolean containsWhitespace(String emailAddress) {
    for (int i = 0; i < emailAddress.length(); i++) {
      final char c = emailAddress.charAt(i);
      if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
        return true;
      }
    }
    return false;
  }

  private static boolean hasValidStructure(String emailAddress) {
    final String[] parts = emailAddress.split("@", -1);
    if (parts.length != 2) {
      logger.warn("Email rejected (invalid @ format): " + emailAddress);
      return false;
    }
    final String localPart = parts[0];
    final String domainPart = parts[1];

    if (localPart.isEmpty() || domainPart.isEmpty()) {
      logger.warn(
        "Email rejected (empty local or domain part): " + emailAddress
      );
      return false;
    }

    if (
      localPart.length() > MAX_LOCAL_LENGTH ||
      domainPart.length() > MAX_DOMAIN_LENGTH ||
      emailAddress.length() > MAX_TOTAL_LENGTH
    ) {
      logger.warn("Email rejected (exceeds length limits): " + emailAddress);
      return false;
    }

    if (emailAddress.contains("..")) {
      logger.warn("Email rejected (consecutive dots): " + emailAddress);
      return false;
    }

    if (
      localPart.charAt(0) == '.' ||
      localPart.charAt(localPart.length() - 1) == '.'
    ) {
      logger.warn(
        "Email rejected (local part starts or ends with dot): " + emailAddress
      );
      return false;
    }

    return hasValidDomain(domainPart, emailAddress);
  }

  private static boolean hasValidDomain(
    String domainPart,
    String emailAddress
  ) {
    final boolean isAddressLiteral =
      domainPart.charAt(0) == '[' &&
      domainPart.charAt(domainPart.length() - 1) == ']';
    if (isAddressLiteral) {
      return true;
    }

    if (
      domainPart.startsWith(".") ||
      domainPart.endsWith(".") ||
      domainPart.startsWith("-") ||
      domainPart.endsWith("-")
    ) {
      logger.warn("Email rejected (invalid domain format): " + emailAddress);
      return false;
    }
    if (!domainPart.contains(".")) {
      logger.warn("Email rejected (domain missing dot): " + emailAddress);
      return false;
    }
    for (final String label : domainPart.split("\\.")) {
      if (
        label.isEmpty() ||
        label.length() > MAX_LABEL_LENGTH ||
        label.startsWith("-") ||
        label.endsWith("-")
      ) {
        logger.warn("Email rejected (invalid domain label): " + emailAddress);
        return false;
      }
    }
    return true;
  }

  private static boolean validateWithInternetAddress(String emailAddress) {
    try {
      new InternetAddress(emailAddress).validate();
    } catch (final AddressException e) {
      logger.warn(
        "Email rejected (InternetAddress validation failed): " +
          emailAddress +
          " - " +
          e.getMessage()
      );
      return false;
    } catch (final Exception e) {
      logger.error("Unexpected error validating email: " + emailAddress, e);
      return false;
    }
    return true;
  }

  /**
   * Filters a delimited list of email addresses, keeping only the valid ones.
   *
   * <p>The input may contain multiple addresses separated by semicolons or
   * commas. Each address is trimmed and validated via
   * {@link #isValidEmailAddress(String)}; valid addresses are joined into a
   * single comma-and-space separated string, while blank entries are skipped
   * and invalid entries are dropped and logged.
   *
   * @param email the delimited list of email addresses; may be {@code null}
   * @return {@code null} if the input is {@code null}; otherwise a
   *     comma-separated string of the valid addresses (possibly empty)
   */
  public static String sanitizeEmailAddresses(String email) {
    if (email == null) {
      return null;
    }

    final String[] addresses = email.split("[;,]");
    final StringBuilder result = new StringBuilder();

    for (final String address : addresses) {
      final String trimmed = address.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      if (isValidEmailAddress(trimmed)) {
        if (!result.isEmpty()) {
          result.append(", ");
        }
        result.append(trimmed);
      } else {
        logger.warn("Skipping invalid email address: " + trimmed);
      }
    }

    return result.toString();
  }
}
