package io.swagger.util;

import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Email validation and sanitization — RFC 5321 / RFC 5322 compliant.
 *
 * @author Alain Morlet
 */
public class EmailUtil {

  private static final Log logger = LogFactory.getLog(EmailUtil.class);

  private static final int MAX_LOCAL_LENGTH = 64; // RFC 5321 §4.5.3.1
  private static final int MAX_DOMAIN_LENGTH = 255;
  private static final int MAX_TOTAL_LENGTH = 254;
  private static final int MAX_LABEL_LENGTH = 63; // RFC 1035 §2.3.4

  // RFC 5322 §3.2.3 atext (dot handled separately)
  private static final String LOCAL_ATEXT = "a-zA-Z0-9!#$%&'*+\\-/=?^_`{|}~";
  private static final String LOCAL_PART_REGEX =
    "[" +
    LOCAL_ATEXT +
    "]" +
    "([" +
    LOCAL_ATEXT +
    ".]*" +
    "[" +
    LOCAL_ATEXT +
    "])?";

  private static final String DNS_LABEL =
    "[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?";
  private static final String HOSTNAME_REGEX =
    DNS_LABEL + "(\\." + DNS_LABEL + ")+";

  // RFC 5321 §4.1.3 address literals
  private static final String IPV4_REGEX =
    "\\[(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)" +
    "(\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)){3}\\]";
  private static final String IPV6_REGEX = "\\[IPv6:[a-fA-F0-9:]+\\]";
  private static final String ADDRESS_LITERAL_REGEX =
    "(" + IPV4_REGEX + "|" + IPV6_REGEX + ")";
  private static final String DOMAIN_REGEX =
    "(" + HOSTNAME_REGEX + "|" + ADDRESS_LITERAL_REGEX + ")";

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^" + LOCAL_PART_REGEX + "@" + DOMAIN_REGEX + "$"
  );

  /**
   * Validates a single email address against RFC 5321/5322.
   *
   * @param emailAddress address to validate (may be {@code null})
   * @return {@code true} if valid
   */
  public static boolean isValidEmailAddress(String emailAddress) {
    if (emailAddress == null || emailAddress.isEmpty()) {
      logger.warn("Email rejected: null or empty");
      return false;
    }

    for (int i = 0; i < emailAddress.length(); i++) {
      final char c = emailAddress.charAt(i);
      if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
        logger.warn("Email rejected (contains whitespace): " + emailAddress);
        return false;
      }
    }

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

    final boolean isAddressLiteral =
      domainPart.charAt(0) == '[' &&
      domainPart.charAt(domainPart.length() - 1) == ']';

    if (!isAddressLiteral) {
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
    }

    if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
      logger.warn("Email rejected (pattern mismatch): " + emailAddress);
      return false;
    }

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
   * Splits input by '{@code ,}' or '{@code ;}', validates each address and
   * returns only the valid ones joined by "{@code , }".
   *
   * @param email raw input (may be {@code null})
   * @return valid addresses comma-separated, empty if none valid, {@code null} if input is {@code null}
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
        if (result.length() > 0) {
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
