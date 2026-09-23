package io.swagger.util.parsers;

import io.swagger.model.PreferenceConfiguration;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for parsing user-related JSON payloads into domain objects.
 *
 * <p>It reads the JSON body sent by clients (for example when persisting user
 * display preferences) and converts it into a {@link PreferenceConfiguration}
 * instance describing the library column visibility and listing options.
 * Parsing errors are logged and result in a best-effort / empty configuration
 * rather than propagating a checked exception.
 *
 * <p>This class only exposes static helpers and cannot be instantiated.
 *
 * @author beaurpi
 */
public class UserJsonParser {

  /** JSON key holding the maximum number of items to display in a listing page. */
  public static final String LIMIT = "limit";

  /** Logger used to report JSON parsing failures. */
  private static final Log logger = LogFactory.getLog(UserJsonParser.class);

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be
   *     instantiated
   */
  private UserJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Reads the JSON body from the given web script request and parses it into a
   * {@link PreferenceConfiguration}.
   *
   * @param req the incoming web script request whose content holds the JSON
   *     preference payload
   * @return the parsed preference configuration
   * @throws IOException if the request content cannot be read
   */
  public static PreferenceConfiguration parsePreference(WebScriptRequest req)
    throws IOException {
    String cBody = req.getContent().getContent();
    return parsePreference(cBody);
  }

  /**
   * Parses the given JSON string into a {@link PreferenceConfiguration}.
   *
   * <p>The expected JSON structure contains a {@code library} object with a
   * {@code column} object (boolean flags controlling which columns are shown:
   * name, version, modification, creation, size, expiration, status,
   * description, author, title and securityRanking) and a {@code listing}
   * object (page number, limit and sort order). If the string cannot be
   * parsed, the error is logged and a partially populated (or default)
   * configuration is returned.
   *
   * @param string the JSON representation of the user preferences
   * @return the parsed preference configuration; never {@code null}
   */
  public static PreferenceConfiguration parsePreference(String string) {
    PreferenceConfiguration preference = new PreferenceConfiguration();
    try {
      JSONParser parser = new JSONParser();
      JSONObject json = (JSONObject) parser.parse(string);
      JSONObject library = (JSONObject) json.get("library");
      JSONObject column = (JSONObject) library.get("column");
      Boolean name = (Boolean) column.get("name");
      Boolean version = (Boolean) column.get("version");
      Boolean modification = (Boolean) column.get("modification");
      Boolean creation = (Boolean) column.get("creation");
      Boolean size = (Boolean) column.get("size");
      Boolean expiration = (Boolean) column.get("expiration");
      Boolean status = (Boolean) column.get("status");
      Boolean description = (Boolean) column.get("description");
      Boolean author = (Boolean) column.get("author");
      Boolean title = (Boolean) column.get("title");
      Boolean securityRanking = (Boolean) column.get("securityRanking");

      preference.getLibrary().getColumn().setName(name);
      preference.getLibrary().getColumn().setVersion(version);
      preference.getLibrary().getColumn().setModification(modification);
      preference.getLibrary().getColumn().setCreation(creation);
      preference.getLibrary().getColumn().setSize(size);
      preference.getLibrary().getColumn().setExpiration(expiration);
      preference.getLibrary().getColumn().setStatus(status);
      preference.getLibrary().getColumn().setDescription(description);
      preference.getLibrary().getColumn().setAuthor(author);
      preference.getLibrary().getColumn().setTitle(title);
      preference.getLibrary().getColumn().setSecurityRanking(securityRanking);

      JSONObject listing = (JSONObject) library.get("listing");
      if (listing.get("page") instanceof Long) {
        preference
          .getLibrary()
          .getListing()
          .setPage(Integer.parseInt(listing.get("page").toString()));
      }

      if (listing.get(LIMIT) instanceof Long) {
        preference
          .getLibrary()
          .getListing()
          .setLimit(Integer.parseInt(listing.get(LIMIT).toString()));
      }

      String sort = (String) listing.get("sort");
      preference.getLibrary().getListing().setSort(sort);
    } catch (ParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }

    return preference;
  }

  /**
   * Parses the given JSON string into a raw {@link JSONObject}.
   *
   * <p>Unlike {@link #parsePreference(String)}, this method does not interpret
   * the structure of the payload; it simply returns the parsed JSON tree. If
   * parsing fails, the error is logged and an empty {@link JSONObject} is
   * returned.
   *
   * @param string the JSON string to parse
   * @return the parsed JSON object, or an empty object if parsing fails; never
   *     {@code null}
   */
  public static JSONObject parsePreferenceAsJson(String string) {
    try {
      JSONParser parser = new JSONParser();
      return (JSONObject) parser.parse(string);
    } catch (ParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }

    return new JSONObject();
  }
}
