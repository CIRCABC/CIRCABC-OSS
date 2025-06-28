package io.swagger.util;

import java.util.regex.Pattern;

/**
 * Utility class to manage Email addresses.
 * @author Alain Morlet
 */
public class EmailUtil {

  private static final String REGEX_PATTERN =
    "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
  private static final Pattern EmailPattern = Pattern.compile(REGEX_PATTERN);

  public static boolean isValidEmailAddress(String emailAddress) {
    return EmailPattern.matcher(emailAddress).matches();
  }
}
