package io.swagger.util.parsers;

import io.swagger.model.KeywordDefinition;
import io.swagger.util.Converter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for parsing keyword definitions from the JSON body of a web script request.
 *
 * <p>It reads the raw JSON payload of an incoming {@link WebScriptRequest} and converts it into a
 * {@link KeywordDefinition} domain object, extracting the keyword identifier and its
 * internationalized title. This class is not meant to be instantiated.
 *
 * @author beaurpi
 */
public class KeywordJsonParser {

  /** JSON property name holding the keyword's internationalized title object. */
  private static final String TITLE = "title";

  /** JSON property name holding the keyword's identifier. */
  private static final String ID = "id";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since this class only exposes static members
   */
  private KeywordJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses a full keyword definition from the JSON body of the given web script request.
   *
   * <p>The request body is expected to be a JSON object containing an {@code id} property and a
   * {@code title} property, where the title is itself a JSON object mapping locales to their
   * translated values.
   *
   * @param req the web script request whose content holds the keyword JSON payload
   * @return a {@link KeywordDefinition} populated with the parsed identifier and internationalized
   *     title
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the body is not valid JSON, or if the {@code id} or {@code title}
   *     properties are missing
   */
  public static KeywordDefinition parseJsonFullKeyword(WebScriptRequest req)
    throws IOException, ParseException {
    KeywordDefinition body = new KeywordDefinition();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String id = String.valueOf(json.get(ID));
    if (id != null) {
      body.setId(id);
    } else {
      throw new ParseException(0, "Empty ID");
    }

    JSONObject titles = (JSONObject) json.get(TITLE);
    if (titles == null) {
      throw new ParseException(0, "Empty title");
    }

    body.setTitle(Converter.toI18NProperty(titles));

    return body;
  }
}
