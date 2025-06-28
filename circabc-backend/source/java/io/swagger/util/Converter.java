package io.swagger.util;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import eu.cec.digit.circabc.repo.app.model.Category;
import eu.cec.digit.circabc.repo.app.model.Header;
import eu.cec.digit.circabc.repo.app.model.InterestGroupItem;
import io.swagger.model.I18nProperty;
import io.swagger.model.InterestGroup;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.servlet.FormData;

public class Converter {

  //Date YYYY-MM-DD with TimeStamp containing seconds and milli-seconds. example: 2024-07-05T14:31:11.580Z
  private static final String YYYYMMDD_MM_SS_FORMAT =
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  //Date YYYY-MM-DD with TimeStamp containing only hours and minutes but no seconds. example: 2024-07-05T14:31Z
  private static final String YYYYMMDD_MM_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

  private static final StoreRef STORE_REF_WORKSPACE_VERSION2STORE =
    new StoreRef("workspace", "version2Store");

  private Converter() {
    throw new IllegalStateException("Utility class");
  }

  public static I18nProperty toI18NProperty(String text) {
    I18nProperty result = new I18nProperty();
    result.put(I18NUtil.getLocale().toLanguageTag(), text);
    return result;
  }

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

  public static String convertDateWithoutTimeToString(Date d) {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    return df.format(d);
  }

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
    return convertHtmlToText(html).replaceAll("\n", "\\\\n");
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

  public static MLText toMLText(I18nProperty property) {
    MLText result = new MLText();
    for (Entry<String, String> entry : property.entrySet()) {
      if (
        entry.getValue() != null &&
        !"null".equals(entry.getValue()) &&
        !entry.getValue().isEmpty()
      ) {
        result.addValue(new Locale(entry.getKey()), entry.getValue());
      } else {
        result.addValue(new Locale(entry.getKey()), "");
      }
    }
    return result;
  }

  public static MLText toMLText(JSONObject object) throws JSONException {
    MLText result = new MLText();
    @SuppressWarnings("unchecked")
    final Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      final String key = keys.next();
      String value = (String) object.get(key);
      if (value != null) {
        result.addValue(new Locale(key), value);
      } else {
        result.addValue(new Locale(key), "");
      }
    }
    return result;
  }

  public static MLText toMLTextEN(String str) {
    MLText result = new MLText();
    result.addValue(Locale.ENGLISH, str);
    return result;
  }

  public static I18nProperty toI18NProperty(JSONObject object)
    throws JSONException {
    I18nProperty result = new I18nProperty();
    @SuppressWarnings("unchecked")
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
   * @param property If null, return an empty string.
   * @param language If null, use English as default.
   */
  public static String getStringOrMLTextValue(
    Serializable property,
    String language
  ) {
    if (property == null) {
      return "";
    }

    if (property instanceof String) {
      return (String) property;
    } else if (property instanceof MLText) {
      MLText propertyMLText = (MLText) property;

      Locale locale = (language != null && isValidLanguageLocale(language))
        ? new Locale(language)
        : Locale.ENGLISH;

      return propertyMLText.getValue(locale);
    }

    return "";
  }

  /**
   * Check if the provided language is valid for a Locale. Ex. "english" would be valid, "nn",
   * wouldn't
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

  public static Date convertStringToDate(String date) throws ParseException {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat(YYYYMMDD_MM_SS_FORMAT);
    df.setTimeZone(tz);
    try {
      return df.parse(date);
    } catch (ParseException e) {
      //we need to handle the case where we have only hours and minutes but no seconds
      df = new SimpleDateFormat(YYYYMMDD_MM_FORMAT);
      return df.parse(date);
    }
  }

  public static Date convertStringToSimpleDate(String string)
    throws ParseException {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Quoted "Z" to
    // indicate UTC, no
    // timezone offset
    df.setTimeZone(tz);
    return df.parse(string + "'T'00:00'Z'");
  }

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

  public static NodeRef createNodeRefFromId(String id) {
    return new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, id);
  }

  public static NodeRef createArchiveNodeRefFromId(String archiveNodeId) {
    return new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archiveNodeId);
  }

  public static NodeRef createVersionNodeRefFromId(String versionNodeId) {
    return new NodeRef(STORE_REF_WORKSPACE_VERSION2STORE, versionNodeId);
  }

  public static String extractNodeRefId(String nodeRef) {
    int pos = nodeRef.lastIndexOf('/');
    if (pos != -1) {
      return nodeRef.substring(pos + 1);
    }
    throw new IllegalArgumentException(
      "Node reference " + nodeRef + " is invalid."
    );
  }

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

  public static String getValue(FormData.FormField field) throws IOException {
    if (field.getIsFile()) {
      return IOUtils.toString(field.getInputStream());
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
