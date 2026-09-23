package io.swagger.util;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import io.swagger.model.I18nProperty;
import io.swagger.model.InterestGroup;
import io.swagger.model.db.Category;
import io.swagger.model.db.Header;
import io.swagger.model.db.InterestGroupItem;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Stateless utility class that centralises the type/format conversions used across the CIRCABC
 * REST layer.
 *
 * <p>Its responsibilities include:
 *
 * <ul>
 *   <li>Converting between Alfresco {@link MLText} multilingual values and the API's
 *       {@link I18nProperty} representation (and JSON objects thereof).
 *   <li>Formatting and parsing {@link Date} values to/from the UTC string formats exchanged over
 *       the REST API.
 *   <li>Building Alfresco {@link NodeRef} instances from raw node identifiers and extracting node
 *       ids from node reference strings.
 *   <li>Converting internal database/domain entities ({@link InterestGroupItem}, {@link Category},
 *       {@link Header}) into their API model counterparts.
 *   <li>Assorted string helpers (HTML-to-text, QName escaping, quote normalisation, temporary file
 *       name recovery).
 * </ul>
 *
 * <p>All members are {@code static}; the class is not meant to be instantiated.
 */
public class Converter {

  /** Store reference for the Alfresco version store used to build version {@link NodeRef}s. */
  private static final StoreRef STORE_REF_WORKSPACE_VERSION2STORE =
    new StoreRef("workspace", "version2Store");

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static helpers
   */
  private Converter() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Wraps a plain text value into an {@link I18nProperty} keyed by the current UI locale.
   *
   * @param text the text value to store
   * @return an {@link I18nProperty} containing the text under the current locale's language tag
   */
  public static I18nProperty toI18NProperty(String text) {
    I18nProperty result = new I18nProperty();
    result.put(I18NUtil.getLocale().toLanguageTag(), text);
    return result;
  }

  /**
   * Converts an Alfresco {@link MLText} multilingual value into an {@link I18nProperty}.
   *
   * @param text the multilingual text to convert
   * @return an {@link I18nProperty} keyed by language, with {@code null} values replaced by empty
   *     strings
   */
  public static I18nProperty toI18NProperty(MLText text) {
    I18nProperty result = new I18nProperty();
    for (Entry<Locale, String> entry : text.entrySet()) {
      result.put(
        entry.getKey().getLanguage(),
        entry.getValue() == null ? "" : entry.getValue()
      );
    }
    return result;
  }

  /**
   * Formats a date as a minute-precision UTC string ({@code yyyy-MM-dd'T'HH:mm'Z'}).
   *
   * @param d the date to format
   * @return the UTC-formatted date string
   */
  public static String convertDateToString(Date d) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted
    // "Z"
    // to
    // indicate
    // UTC,
    // no
    // timezone
    // offset
    df.setTimeZone(tz);
    return df.format(d);
  }

  /**
   * Formats a date as a millisecond-precision UTC string
   * ({@code yyyy-MM-dd'T'HH:mm:ss.sss'Z'}).
   *
   * @param d the date to format
   * @return the UTC-formatted date string
   */
  public static String convertDateToUTCString(Date d) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'"); // Quoted
    // "Z"
    // to
    // indicate
    // UTC,
    // no
    // timezone
    // offset
    df.setTimeZone(tz);
    return df.format(d);
  }

  /**
   * Formats a date as a date-only string ({@code yyyy-MM-dd}), discarding any time component.
   *
   * @param d the date to format
   * @return the formatted date string
   */
  public static String convertDateWithoutTimeToString(Date d) {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    return df.format(d);
  }

  /**
   * Converts a plain map of language codes to values into an {@link I18nProperty}, normalising each
   * key to its first two characters (the ISO language code) when it is longer than two characters.
   *
   * @param ml the map of language codes to localized values
   * @return the resulting {@link I18nProperty}
   */
  public static I18nProperty convertMlToI18nProperty(Map<String, String> ml) {
    I18nProperty i18n = new I18nProperty();

    for (Entry<String, String> entry : ml.entrySet()) {
      if (entry.getKey() != null && entry.getKey().length() > 2) {
        i18n.put(entry.getKey().substring(0, 2), entry.getValue());
      } else {
        i18n.put(entry.getKey(), entry.getValue());
      }
    }

    return i18n;
  }

  /**
   * Convert an html String in plain text compatible with Json format.
   * We remove all the Html tags but keep the white spaces and line breaks.
   * In case of line break, we use the Json compatible character \\n.
   * We also remove any line breaks at the end of the String
   * @param html the html string to convert
   * @return the converted Json plain text string
   */
  public static String convertHtmlToJsonText(String html) {
    // return text in json compatible format where we use "\\n" instead of "\n" for line breaks
    return convertHtmlToText(html).replace("\n", "\\\\n");
  }

  /**
   * Convert an html String in plain text.
   * We remove all the Html tags but keep the white spaces and line breaks.
   * We also remove any line breaks at the end of the String
   * @param html the html string to convert
   * @return the converted plain text String
   */
  public static String convertHtmlToText(String html) {
    Document doc = Jsoup.parse(html);
    doc
      .select("h1, h2, h3, h4, h5, h6, p, div, blockquote, ul, ol, table")
      .after("\n");
    String text = doc.wholeText().trim();
    //remove lasts "\n" from the end of the String
    while (text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }
    return text; //returns plain text without html tags
  }

  /**
   * Converts an {@link I18nProperty} into an Alfresco {@link MLText} value.
   *
   * @param property the multilingual property to convert
   * @return an {@link MLText}, with {@code null}, {@code "null"} or empty values stored as empty
   *     strings
   */
  public static MLText toMLText(I18nProperty property) {
    MLText result = new MLText();
    for (Entry<String, String> entry : property.entrySet()) {
      if (
        entry.getValue() != null &&
        !"null".equals(entry.getValue()) &&
        !entry.getValue().isEmpty()
      ) {
        result.addValue(Locale.of(entry.getKey()), entry.getValue());
      } else {
        result.addValue(Locale.of(entry.getKey()), "");
      }
    }
    return result;
  }

  /**
   * Converts a JSON object of language codes to values into an Alfresco {@link MLText} value.
   *
   * @param object the JSON object whose keys are language codes and values are localized strings
   * @return the resulting {@link MLText}, with {@code null} values stored as empty strings
   * @throws JSONException if a value cannot be read from the JSON object
   */
  public static MLText toMLText(JSONObject object) throws JSONException {
    MLText result = new MLText();
    final Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      final String key = keys.next();
      String value = (String) object.get(key);
      if (value != null) {
        result.addValue(Locale.of(key), value);
      } else {
        result.addValue(Locale.of(key), "");
      }
    }
    return result;
  }

  /**
   * Builds an {@link MLText} holding the given string under the English locale only.
   *
   * @param str the English value
   * @return an {@link MLText} with the value registered for {@link Locale#ENGLISH}
   */
  public static MLText toMLTextEN(String str) {
    MLText result = new MLText();
    result.addValue(Locale.ENGLISH, str);
    return result;
  }

  /**
   * Converts a JSON object of language codes to values into an {@link I18nProperty}, skipping any
   * JSON {@code null} entries.
   *
   * @param object the JSON object whose keys are language codes and values are localized strings
   * @return the resulting {@link I18nProperty}
   * @throws JSONException if a value cannot be read from the JSON object
   */
  public static I18nProperty toI18NProperty(JSONObject object)
    throws JSONException {
    I18nProperty result = new I18nProperty();
    final Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      final String key = keys.next();
      String value;
      Object valueObject = object.get(key);
      String className = valueObject.getClass().getName();
      if ("org.json.JSONObject$Null".equals(className)) {
        continue;
      }
      value = (String) valueObject;
      result.put(key, value);
    }
    return result;
  }

  /**
   * Get the string value corresponding to the provided MLText property.
   *
   * @param property the property to resolve; if null, an empty string is returned. A plain
   *     {@link String} is returned as-is, while an {@link MLText} is resolved for the requested
   *     language.
   * @param language the language to resolve for an {@link MLText} property; if null, English is
   *     used as default.
   * @return the resolved string value, or an empty string if the property is null or of an
   *     unsupported type
   */
  public static String getStringOrMLTextValue(
    Serializable property,
    String language
  ) {
    if (property == null) {
      return "";
    }

    if (property instanceof String s) {
      return s;
    } else if (property instanceof MLText propertyMLText) {
      Locale locale = (language != null && isValidLanguageLocale(language))
        ? Locale.of(language)
        : Locale.ENGLISH;

      return propertyMLText.getValue(locale);
    }

    return "";
  }

  /**
   * Check if the provided language is valid for a Locale. Ex. "english" would be valid, "nn",
   * wouldn't
   *
   * @param language the language string to validate against the available locales
   * @return {@code true} if the language matches one of the JVM's available locales, {@code false}
   *     otherwise
   */
  private static boolean isValidLanguageLocale(String language) {
    Locale[] locales = Locale.getAvailableLocales();
    for (Locale locale : locales) {
      if (language.equals(locale.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parses a millisecond-precision UTC date string
   * ({@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}) into a {@link Date}.
   *
   * @param string the UTC date string to parse
   * @return the parsed date
   * @throws ParseException if the string does not match the expected format
   */
  public static Date convertStringToDate(String string) throws ParseException {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted
    // "Z"
    // to
    // indicate
    // UTC,
    // no
    // timezone
    // offset
    df.setTimeZone(tz);
    return df.parse(string);
  }

  /**
   * Parses a date-only string ({@code yyyy-MM-dd}) into a {@link Date} at midnight UTC.
   *
   * @param string the date-only string to parse
   * @return the parsed date
   * @throws ParseException if the string does not match the expected format
   */
  public static Date convertStringToSimpleDate(String string)
    throws ParseException {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Quoted "Z" to
    // indicate UTC, no
    // timezone offset
    df.setTimeZone(tz);
    return df.parse(string + "'T'00:00'Z'");
  }

  /**
   * Converts a Simple-JSON object of localized titles into an {@link I18nProperty}, keeping only the
   * supported language codes with non-empty values.
   *
   * @param titles the Simple-JSON object keyed by language code
   * @return the resulting {@link I18nProperty}
   */
  public static I18nProperty toI18NProperty(org.json.simple.JSONObject titles) {
    I18nProperty result = new I18nProperty();

    for (String code : SupportedLanguages.availableLangCodes) {
      if (
        titles.containsKey(code) &&
        (titles.get(code) != null && !titles.get(code).equals(""))
      ) {
        result.put(code, String.valueOf(titles.get(code)));
      }
    }
    return result;
  }

  /**
   * Builds a {@link NodeRef} in the workspace SpacesStore from a bare node id.
   *
   * @param id the node identifier
   * @return the corresponding workspace {@link NodeRef}
   */
  public static NodeRef createNodeRefFromId(String id) {
    return new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, id);
  }

  /**
   * Builds a {@link NodeRef} in the archive SpacesStore from a bare node id.
   *
   * @param archiveNodeId the archived node identifier
   * @return the corresponding archive {@link NodeRef}
   */
  public static NodeRef createArchiveNodeRefFromId(String archiveNodeId) {
    return new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archiveNodeId);
  }

  /**
   * Builds a {@link NodeRef} in the version2 store from a bare node id.
   *
   * @param versionNodeId the version node identifier
   * @return the corresponding version {@link NodeRef}
   */
  public static NodeRef createVersionNodeRefFromId(String versionNodeId) {
    return new NodeRef(STORE_REF_WORKSPACE_VERSION2STORE, versionNodeId);
  }

  /**
   * Extracts the id portion (the substring after the last {@code '/'}) from a node reference string.
   *
   * @param nodeRef the node reference string
   * @return the node id
   * @throws IllegalArgumentException if the string contains no {@code '/'} separator
   */
  public static String extractNodeRefId(String nodeRef) {
    int pos = nodeRef.lastIndexOf('/');
    if (pos != -1) {
      return nodeRef.substring(pos + 1);
    }
    throw new IllegalArgumentException(
      "Node reference " + nodeRef + " is invalid."
    );
  }

  /**
   * Maps a database {@link InterestGroupItem} to the API {@link InterestGroup} model.
   *
   * @param interestGroupItem the source database item
   * @param uiLang the UI language used to key the description
   * @return the populated {@link InterestGroup} API model
   */
  public static InterestGroup toInterestGroup(
    InterestGroupItem interestGroupItem,
    String uiLang
  ) {
    InterestGroup result = new InterestGroup();
    result.setId(interestGroupItem.getId());
    result.setName(interestGroupItem.getName());
    result.setDescription(
      new I18nProperty(uiLang, interestGroupItem.getBestTitle())
    );
    result.setLogoUrl(interestGroupItem.getLogoRef());
    return result;
  }

  /**
   * Maps a database {@link Category} to the API {@link io.swagger.model.Category} model. Falls back
   * to the category name when no title is set, and to English when no UI language is provided.
   *
   * @param category the source database category
   * @param uiLang the UI language used to key the title; defaults to {@code "en"} when null
   * @return the populated {@link io.swagger.model.Category} API model
   */
  public static io.swagger.model.Category toCategory(
    Category category,
    String uiLang
  ) {
    io.swagger.model.Category result = new io.swagger.model.Category();
    result.setId(new NodeRef(category.getNodeRef()).getId());
    result.setName(category.getName());
    String title = category.getTitle();
    if (title == null) {
      title = category.getName();
    }
    if (uiLang == null) {
      uiLang = "en";
    }
    result.setTitle(new I18nProperty(uiLang, title));
    if (category.getLogoRef() != null) {
      result.setLogoRef(category.getLogoRef());
    }
    return result;
  }

  /**
   * Maps a database {@link Header} to the API {@link io.swagger.model.Header} model. Falls back to
   * the header name when no description is set; the description is keyed under English.
   *
   * @param header the source database header
   * @return the populated {@link io.swagger.model.Header} API model
   */
  public static io.swagger.model.Header toHeader(Header header) {
    io.swagger.model.Header result = new io.swagger.model.Header();
    result.setId(new NodeRef(header.getNodeRef()).getId());
    result.setName(header.getName());
    String description = header.getDescription();
    if (description == null) {
      description = header.getName();
    }
    result.setDescription(new I18nProperty("en", description));
    return result;
  }

  /**
   * Reads the value of a multipart form field as a string, transparently handling file fields by
   * reading their input stream with the default charset.
   *
   * @param field the form field to read
   * @return the field value (or file content) as a string
   * @throws IOException if reading a file field's input stream fails
   */
  public static String getValue(FormData.FormField field) throws IOException {
    if (field.getIsFile()) {
      return IOUtils.toString(field.getInputStream(), Charset.defaultCharset());
    } else {
      return field.getValue();
    }
  }

  /**
   * Retrieve original file name of a temporary file.
   * It is a revert of the method TempFileProvider.createTempFile.
   * createTempFile takes a file in attachment and create a temporary file with a new name that follows the following pattern: originalFile + String representation of random Long number + cbctmp suffix
   * example: myFile.jpg => myFile1234567890123456789cbctmp
   * @param tempFileName temporary file name
   * @return original fileName
   */
  public static String getOriginalFileName(String tempFileName) {
    String result = tempFileName;
    if (tempFileName.contains("cbctmp")) {
      // find position of the characters just before the first c of cbctmp
      int index = tempFileName.length() - 6 - 1;
      boolean stop = false;

      // navigate in the String from right to left until finding a non digit character
      while (index >= 0 && !stop) {
        char c = tempFileName.charAt(index);
        if (c >= '0' && c <= '9') {
          --index;
        } else {
          stop = true;
        }
      }
      if (stop) {
        result = tempFileName.substring(0, index + 1);
      }
    }
    return result;
  }

  /**
   * Escapes the special characters ({@code '{'}, {@code '}'}, {@code ':'} and {@code '-'}) of a
   * {@link QName}'s string representation by prefixing each with a backslash.
   *
   * @param qName the qualified name to escape
   * @return the escaped string representation of the qualified name
   */
  public static String escapeQName(QName qName) {
    String string = qName.toString();
    StringBuilder buf = new StringBuilder(string.length() + 4);

    for (int i = 0; i < string.length(); ++i) {
      char c = string.charAt(i);
      if (c == '{' || c == '}' || c == ':' || c == '-') {
        buf.append('\\');
      }

      buf.append(c);
    }

    return buf.toString();
  }

  /**
   * Utility method that will replace enclosing SingleQuotes by double quotes.
   * exemple: 'my String' will be replaced by "my String".
   * 'l'atelier" will be replaced by "l'atelier"
   * @param s String to convert
   * @return converted String
   */
  public static String replaceEnclosingSingleQuotes(String s) {
    String result = s;
    // if there are opening and closing ', replace them by"
    if (
      s != null &&
      s.length() > 2 &&
      s.charAt(0) == '\'' &&
      s.charAt(s.length() - 1) == '\''
    ) {
      StringBuilder str = new StringBuilder();
      str.append("\"");
      str.append(s.substring(1, s.length() - 1));
      str.append("\"");
      result = str.toString();
    }
    return result;
  }
}
