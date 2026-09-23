package io.swagger.util.parsers;

import io.swagger.model.MultilingualAspectMetadata;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that extracts multilingual aspect metadata from the JSON body
 * of a web script request.
 *
 * <p>It reads the {@code pivotLang} and {@code author} properties from the
 * request payload and maps them onto a {@link MultilingualAspectMetadata}
 * instance. This class is not meant to be instantiated; all functionality is
 * exposed through static methods.
 *
 * @author beaurpi
 */
public class MultilingualJsonParser {

  /** JSON property name holding the pivot (reference) language of the content. */
  private static final String PIVOT_LANG = "pivotLang";
  /** JSON property name holding the author of the content. */
  private static final String AUTHOR = "author";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static
   *     members
   */
  private MultilingualJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the multilingual aspect metadata from the JSON body of the given web
   * script request.
   *
   * <p>The {@code pivotLang} and {@code author} JSON properties, when present,
   * are copied onto the returned metadata object.
   *
   * @param req the web script request whose content contains the JSON payload
   * @return a {@link MultilingualAspectMetadata} populated with the values found
   *     in the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static MultilingualAspectMetadata parseAspectMetadata(
    WebScriptRequest req
  ) throws IOException, ParseException {
    MultilingualAspectMetadata result = new MultilingualAspectMetadata();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String lang = String.valueOf(json.get(PIVOT_LANG));
    if (lang != null) {
      result.setPivotLang(lang);
    }

    String author = String.valueOf(json.get(AUTHOR));
    if (author != null) {
      result.setAuthor(author);
    }

    return result;
  }
}
