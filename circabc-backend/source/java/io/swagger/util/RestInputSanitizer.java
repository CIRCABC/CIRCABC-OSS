package io.swagger.util;

import eu.cec.digit.circabc.repo.app.SecurityService;
import io.swagger.model.I18nProperty;
import java.util.Map.Entry;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Shared sanitization helpers for the REST layer. Rich-text fields authored through the REST API
 * are cleaned with the same AntiSamy-backed {@link SecurityService} used by the legacy UI, and
 * URL-node values are restricted to absolute http/https URLs before persistence.
 */
public final class RestInputSanitizer {

  private static final SecurityService SECURITY_SERVICE = new SecurityService();
  private static final UrlValidator HTTP_URL_VALIDATOR = new UrlValidator(
    new String[] { "http", "https" }
  );

  private RestInputSanitizer() {
    throw new IllegalStateException("Utility class");
  }

  public static String sanitizeRichText(String value) {
    if (value == null) {
      return null;
    }

    return SECURITY_SERVICE.getCleanHTML(value, false);
  }

  public static I18nProperty sanitizeRichText(I18nProperty property) {
    if (property == null) {
      return null;
    }

    I18nProperty result = new I18nProperty();
    for (Entry<String, String> entry : property.entrySet()) {
      result.put(entry.getKey(), sanitizeRichText(entry.getValue()));
    }
    return result;
  }

  public static String requireSafeHttpUrl(String value) {
    if (value == null || !HTTP_URL_VALIDATOR.isValid(value)) {
      throw new IllegalArgumentException("Invalid URL: " + value);
    }

    return value;
  }

  /**
   * Returns {@code true} only when the supplied value is an absolute http/https URL.
   * <p>
   * This is intentionally stricter than a {@code contains("http")} substring check: a value such
   * as {@code javascript:alert(1)//http://x} contains the substring "http" but is not a safe URL,
   * and would otherwise be rendered directly into an iframe {@code src} or anchor {@code href}.
   *
   * @param value the candidate URL (may be {@code null})
   * @return {@code true} if the value is a valid absolute http or https URL
   */
  public static boolean isSafeHttpUrl(String value) {
    return value != null && HTTP_URL_VALIDATOR.isValid(value);
  }
}
