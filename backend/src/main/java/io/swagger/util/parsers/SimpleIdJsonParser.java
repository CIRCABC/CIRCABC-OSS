package io.swagger.util.parsers;

import io.swagger.model.SimpleId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for parsing identifier data from the JSON body of a web script request.
 *
 * <p>It provides helpers to read either a single {@link SimpleId} object (a JSON object
 * carrying an {@code "id"} property) or a flat list of identifier strings (a JSON array)
 * from the request payload. This class is stateless and cannot be instantiated.
 *
 * @author beaurpi
 */
public class SimpleIdJsonParser {

  /** JSON property name holding the identifier value in a single-id payload. */
  private static final String ID = "id";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class exposes only static helpers
   */
  private SimpleIdJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses a single identifier from the JSON object contained in the request body.
   *
   * <p>The request content is expected to be a JSON object with an {@code "id"} property,
   * for example {@code {"id": "abc-123"}}.
   *
   * @param req the web script request whose content holds the JSON object to parse
   * @return a {@link SimpleId} populated with the {@code "id"} value from the payload
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static SimpleId parse(WebScriptRequest req)
    throws IOException, ParseException {
    SimpleId result = new SimpleId();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String id = (String) json.get(ID);
    result.setId(id);

    return result;
  }

  /**
   * Parses a list of identifiers from the JSON array contained in the request body.
   *
   * <p>The request content is expected to be a JSON array of values, for example
   * {@code ["abc-123", "def-456"]}. Each non-null element is converted to its string
   * representation and trimmed; {@code null} elements are skipped.
   *
   * @param req the web script request whose content holds the JSON array to parse
   * @return a list of trimmed identifier strings, empty if the array contains no usable values
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static List<String> parseListOfId(WebScriptRequest req)
    throws IOException, ParseException {
    List<String> result = new ArrayList<>();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONArray json = (JSONArray) parser.parse(cBody);

    for (Object aJson : json) {
      if (aJson != null) {
        result.add(aJson.toString().trim());
      }
    }

    return result;
  }
}
