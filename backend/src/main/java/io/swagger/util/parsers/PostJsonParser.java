package io.swagger.util.parsers;

import io.swagger.model.Comment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Utility parser that converts the JSON request body of a post/comment operation into a
 * {@link Comment} domain object.
 *
 * <p>This class is not meant to be instantiated; it only exposes static helper methods and
 * therefore enforces non-instantiability through a private constructor.
 *
 * @author beaurpi
 */
public class PostJsonParser {

  /** JSON attribute name that holds the textual content of the comment. */
  private static final String TEXT = "text";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not intended to be instantiated
   */
  private PostJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the given JSON body and extracts the comment text into a partially populated
   * {@link Comment} object.
   *
   * <p>Only the {@code text} attribute is read from the JSON payload; other fields of the
   * returned {@link Comment} are left unset.
   *
   * @param cBody the raw JSON request body expected to contain a {@code text} attribute
   * @return a {@link Comment} populated with the text extracted from the JSON body
   * @throws ParseException if the provided body is not valid JSON
   */
  public static Comment parsePartial(String cBody) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String text = json.get(TEXT).toString();

    Comment result = new Comment();
    result.setText(text);

    return result;
  }
}
