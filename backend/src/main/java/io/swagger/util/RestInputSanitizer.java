package io.swagger.util;

import io.swagger.model.I18nProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Shared REST-layer input sanitizer.
 *
 * <p>Stored rich-text values authored through the REST API (group/space/category descriptions and
 * contact information, forum posts, user signatures, ...) are later rendered as HTML. To prevent
 * stored cross-site scripting, this helper strips any active/unsafe markup (scripts, event handler
 * attributes, {@code javascript:} URLs, embedded frames/objects, ...) while preserving common
 * formatting, using jsoup's {@link Safelist#relaxed()} policy.</p>
 *
 * <p>URL-node values are validated separately: only absolute {@code http}/{@code https} URLs are
 * accepted so unsafe schemes can never be persisted.</p>
 */
public final class RestInputSanitizer {

  /** Validator accepting only absolute {@code http}/{@code https} URLs. */
  private static final UrlValidator HTTP_URL_VALIDATOR = new UrlValidator(
    new String[] { "http", "https" }
  );

  private RestInputSanitizer() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Sanitises a rich-text HTML value, removing any unsafe markup while keeping common formatting.
   *
   * @param value the raw rich-text value; may be {@code null}
   * @return the sanitised HTML, or {@code null} when {@code value} is {@code null}
   */
  public static String sanitizeRichText(String value) {
    if (value == null) {
      return null;
    }

    return Jsoup.clean(value, Safelist.relaxed());
  }

  /**
   * Sanitises every localised value of the given multilingual property.
   *
   * @param property the multilingual rich-text property; may be {@code null}
   * @return a new {@link I18nProperty} with each value sanitised, or an empty property when
   *     {@code property} is {@code null}
   */
  public static I18nProperty sanitizeRichText(I18nProperty property) {
    if (property == null) {
      return new I18nProperty();
    }

    I18nProperty result = new I18nProperty();
    for (Entry<String, String> entry : property.entrySet()) {
      result.put(entry.getKey(), sanitizeRichText(entry.getValue()));
    }
    return result;
  }

  /**
   * Sanitises every localised value of the given map property.
   *
   * @param property the multilingual rich-text property map; may be {@code null}
   * @return a new map with each value sanitised, or an empty map when {@code property} is {@code null}
   */
  public static Map<String, String> sanitizeRichTextMap(
    Map<String, String> property
  ) {
    if (property == null) {
      return Collections.emptyMap();
    }

    return property
      .entrySet()
      .stream()
      .collect(
        java.util.stream.Collectors.toMap(Entry::getKey, e ->
          sanitizeRichText(e.getValue())
        )
      );
  }

  /**
   * Validates that the given value is an absolute {@code http}/{@code https} URL.
   *
   * @param value the URL to validate
   * @return the same {@code value} when it is a valid {@code http}/{@code https} URL
   * @throws IllegalArgumentException when {@code value} is {@code null} or not a valid
   *     {@code http}/{@code https} URL
   */
  public static String requireSafeHttpUrl(String value) {
    if (value == null || !HTTP_URL_VALIDATOR.isValid(value)) {
      throw new IllegalArgumentException("Invalid URL: " + value);
    }

    return value;
  }
}
