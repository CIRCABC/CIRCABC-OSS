package io.swagger.util.parsers;

import io.swagger.model.Profile;
import io.swagger.util.Converter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that builds a {@link Profile} domain object from the JSON payload of a
 * web script request.
 *
 * <p>This is a stateless helper class: it exposes only static parsing operations and cannot be
 * instantiated. It reads the raw request body, interprets the well-known profile fields (id,
 * localized title, import/export flags and the per-service permission map) and maps them onto a
 * {@link Profile} instance. Fields that are absent from the payload are simply left unset, which
 * is why the entry point is named {@code parsePartial}.
 *
 * @author beaurpi
 */
public class ProfileJsonParser {

  private static final String IMPORTED = "imported";
  private static final String EXPORTED = "exported";
  private static final String TITLE = "title";
  private static final String PERMISSIONS = "permissions";
  private static final String INFORMATION = "information";
  private static final String LIBRARY = "library";
  private static final String MEMBERS = "members";
  private static final String EVENTS = "events";
  private static final String NEWSGROUPS = "newsgroups";
  private static final String VISIBILITY = "visibility";

  private ProfileJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into a partially populated
   * {@link Profile}.
   *
   * <p>Only the properties present in the payload are set on the returned profile; missing
   * properties are left at their default values. The {@code title} object is mandatory and is
   * converted into an internationalized property, while the optional {@code permissions} object is
   * read entry by entry for the {@code information}, {@code library}, {@code members},
   * {@code events}, {@code newsgroups} and {@code visibility} services.
   *
   * @param req the web script request whose content holds the JSON profile representation
   * @return a {@link Profile} populated with the fields found in the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON or does not contain a {@code title}
   */
  public static Profile parsePartial(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Profile result = new Profile();

    String id = (String) json.get("id");
    if (id != null) {
      result.setId(id);
    }

    JSONObject titles = (JSONObject) json.get(TITLE);
    if (titles == null) {
      throw new ParseException(0, "Empty title");
    }

    result.setTitle(Converter.toI18NProperty(titles));

    Boolean exported = (Boolean) json.get(EXPORTED);
    if (exported != null) {
      result.setExported(exported);
    }

    Boolean imported = (Boolean) json.get(IMPORTED);
    if (imported != null) {
      result.setImported(imported);
    }

    JSONObject permissions = (JSONObject) json.get(PERMISSIONS);
    if (permissions != null) {
      putIfPresent(permissions, result, INFORMATION);
      putIfPresent(permissions, result, LIBRARY);
      putIfPresent(permissions, result, MEMBERS);
      putIfPresent(permissions, result, EVENTS);
      putIfPresent(permissions, result, NEWSGROUPS);
      putIfPresent(permissions, result, VISIBILITY);
    }

    return result;
  }

  private static void putIfPresent(
    JSONObject permissions,
    Profile result,
    String key
  ) {
    String value = (String) permissions.get(key);
    if (value != null) {
      result.getPermissions().put(key, value);
    }
  }
}
