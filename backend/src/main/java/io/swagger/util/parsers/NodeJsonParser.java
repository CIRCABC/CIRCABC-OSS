package io.swagger.util.parsers;

import io.swagger.model.Node;
import io.swagger.util.SupportedLanguages;
import java.io.IOException;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class that parses JSON request bodies (either raw JSON strings or the
 * content of an Alfresco {@link WebScriptRequest}) into {@link Node} domain
 * objects.
 *
 * <p>The parser recognises a common set of node attributes such as the name,
 * the internationalised title and description maps, and a fixed set of
 * properties (issue/expiration dates, reference, author, mimetype, encoding,
 * status, security, URL and up to 20 dynamic attributes). Several parsing
 * variants are provided to support different endpoint payloads (full content,
 * post, basic, simple and URL-based bodies).
 *
 * <p>This class is not meant to be instantiated; all functionality is exposed
 * through {@code static} methods.
 *
 * @author beaurpi
 */
public class NodeJsonParser {

  /**
   * Prefix used for the dynamic attribute property keys ({@code dynAttr1} to
   * {@code dynAttr20}) supported by the parser.
   */
  public static final String DYN_ATTR = "dynAttr";
  private static final String URL = "url";
  private static final String MESSAGE = "message";
  private static final String NAME = "name";
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "description";
  private static final String PROPERTIES = "properties";
  private static final String ISSUE_DATE = "issue_date";
  private static final String EXPIRATION_DATE = "expiration_date";
  private static final String REFERENCE = "reference";
  private static final String AUTHOR = "author";
  private static final String MIMETYPE = "mimetype";
  private static final String ENCODING = "encoding";
  private static final String STATUS = "status";
  private static final String SECURITY = "security";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, as this class only exposes static
   *     helpers
   */
  private NodeJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses a full content JSON body into a {@link Node}.
   *
   * <p>Populates the node name, the internationalised title and description
   * maps and the standard content properties (issue date, expiration date,
   * reference, author, mimetype, encoding, status, security and URL) as well as
   * the dynamic attributes {@code dynAttr1} through {@code dynAttr20} when
   * present.
   *
   * @param cBody the raw JSON string to parse
   * @return the populated {@link Node}
   * @throws ParseException if {@code cBody} is not valid JSON
   */
  public static Node parseContentJSON(String cBody) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Node body = new Node();
    body.setName(String.valueOf(json.get(NAME)));

    populateI18nMap(json.get(TITLE), body.getTitle());
    populateI18nMap(json.get(DESCRIPTION), body.getDescription());

    JSONObject properties = (JSONObject) json.get(PROPERTIES);
    body
      .getProperties()
      .put(ISSUE_DATE, String.valueOf(properties.get(ISSUE_DATE)));
    body
      .getProperties()
      .put(EXPIRATION_DATE, String.valueOf(properties.get(EXPIRATION_DATE)));
    body
      .getProperties()
      .put(REFERENCE, String.valueOf(properties.get(REFERENCE)));
    body.getProperties().put(AUTHOR, String.valueOf(properties.get(AUTHOR)));
    putPropertyIfNotNull(body, properties, MIMETYPE);
    putPropertyIfNotNull(body, properties, ENCODING);
    body.getProperties().put(STATUS, String.valueOf(properties.get(STATUS)));
    body
      .getProperties()
      .put(SECURITY, String.valueOf(properties.get(SECURITY)));
    body.getProperties().put(URL, String.valueOf(properties.get(URL)));

    for (int i = 1; i < 21; i++) {
      putPropertyIfNotNull(body, properties, DYN_ATTR + i);
    }

    return body;
  }

  /**
   * Copies the language-keyed entries of a JSON value into the given
   * internationalisation map.
   *
   * <p>If {@code jsonValue} is a {@link JSONObject}, each of the
   * {@link SupportedLanguages#availableLangCodes} language codes present in the
   * object is copied into {@code target}. Non-object values are ignored.
   *
   * @param jsonValue the JSON value expected to hold language-keyed strings
   * @param target the map to populate with language code to value entries
   */
  private static void populateI18nMap(
    Object jsonValue,
    Map<String, String> target
  ) {
    if (jsonValue instanceof JSONObject jsonObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (jsonObj.containsKey(code)) {
          target.put(code, String.valueOf(jsonObj.get(code)));
        }
      }
    }
  }

  /**
   * Copies a single property from the JSON {@code properties} object into the
   * node's properties, but only when the value is present (non-null).
   *
   * @param body the node whose properties are updated
   * @param properties the JSON object holding the source properties
   * @param key the property key to copy
   */
  private static void putPropertyIfNotNull(
    Node body,
    JSONObject properties,
    String key
  ) {
    if (properties.get(key) != null) {
      body.getProperties().put(key, String.valueOf(properties.get(key)));
    }
  }

  /**
   * Parses a post JSON body into a {@link Node}.
   *
   * <p>Delegates to {@link #parseContentJSON(String)} for the common content
   * attributes and additionally copies the {@code message} property when it is
   * present in the payload.
   *
   * @param cBody the raw JSON string to parse
   * @return the populated {@link Node}
   * @throws ParseException if {@code cBody} is not valid JSON
   */
  public static Node parsePostJSON(String cBody) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Node body = parseContentJSON(cBody);

    JSONObject properties = (JSONObject) json.get(PROPERTIES);
    if (properties.containsKey(MESSAGE)) {
      body
        .getProperties()
        .put(MESSAGE, String.valueOf(properties.get(MESSAGE)));
    }

    return body;
  }

  /**
   * Parses the content of a web script request into a minimal {@link Node},
   * populating only the node name.
   *
   * @param req the web script request whose content holds the JSON body
   * @return a {@link Node} with its name set from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static Node parseBasicJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Node body = new Node();
    body.setName(String.valueOf(json.get(NAME)));

    return body;
  }

  /**
   * Parses the content of a web script request into a {@link Node} carrying its
   * name together with the internationalised title and description maps.
   *
   * <p>For both the title and description, only the entries whose keys match a
   * {@link SupportedLanguages#availableLangCodes} language code are copied.
   *
   * @param req the web script request whose content holds the JSON body
   * @return the populated {@link Node}
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static Node parseSimpleJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Node body = new Node();
    body.setName(String.valueOf(json.get(NAME)));

    Object titles = json.get(TITLE);
    if (titles instanceof JSONObject titlesObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (titlesObj.containsKey(code)) {
          body.getTitle().put(code, String.valueOf(titlesObj.get(code)));
        }
      }
    }

    Object descriptions = json.get(DESCRIPTION);
    if (descriptions instanceof JSONObject descriptionsObj) {
      for (String code : SupportedLanguages.availableLangCodes) {
        if (descriptionsObj.containsKey(code)) {
          body
            .getDescription()
            .put(code, String.valueOf(descriptionsObj.get(code)));
        }
      }
    }

    return body;
  }

  /**
   * Parses the content of a web script request into a {@link Node} carrying its
   * name and the {@code url} property taken from the JSON {@code properties}
   * object.
   *
   * @param req the web script request whose content holds the JSON body
   * @return the populated {@link Node}
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static Node parseUrlBasicJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Node body = new Node();
    body.setName(String.valueOf(json.get(NAME)));

    JSONObject properties = (JSONObject) json.get(PROPERTIES);
    body.getProperties().put(URL, String.valueOf(properties.get(URL)));

    return body;
  }
}
