package io.swagger.util.parsers;

import io.swagger.model.I18nProperty;
import io.swagger.model.News;
import io.swagger.util.Converter;
import java.io.IOException;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that builds a {@link News} domain object from the JSON payload of a
 * web script request.
 *
 * <p>The parser reads the raw request body, deserialises it as a JSON object and maps the
 * recognised fields ({@code id}, {@code content}, {@code pattern}, {@code layout},
 * {@code size}, {@code date}, {@code title} and {@code url}) onto a {@link News} instance.
 * It also performs validation of the pattern-specific fields (e.g. requiring a title unless
 * the pattern is {@code IFRAME}) and of the supplied URL.
 *
 * <p>This is a stateless helper class exposing only static methods; it is not meant to be
 * instantiated.
 *
 * @author beaurpi
 */
public class NewsJsonParser {

  /** JSON key for the news content/body. */
  private static final String CONTENT = "content";
  /** JSON key for the news identifier. */
  private static final String ID = "id";
  /** JSON key for the news display pattern (see {@link News.PatternEnum}). */
  private static final String PATTERN = "pattern";
  /** JSON key for the news layout (see {@link News.LayoutEnum}). */
  private static final String LAYOUT = "layout";
  /** JSON key for the news size. */
  private static final String SIZE = "size";
  /** JSON key for the date associated with a {@code DATE} pattern. */
  private static final String DATE = "date";
  /** JSON key for the internationalised news title. */
  private static final String TITLE = "title";
  /** JSON key for the news URL. */
  private static final String URL = "url";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class exposes only static members
   */
  private NewsJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into a {@link News} object.
   *
   * <p>Optional fields ({@code id}, {@code content}, {@code pattern}, {@code layout} and
   * {@code url}) are only applied when present in the payload. The {@code size} field is
   * required and the {@code url}, when supplied, is validated.
   *
   * @param req the web script request whose content holds the news JSON payload
   * @return a populated {@link News} instance
   * @throws IOException if the request content cannot be read or the URL is invalid
   * @throws ParseException if the JSON body is malformed or a mandatory field (such as the
   *     title for non-{@code IFRAME} patterns) is missing
   * @throws java.text.ParseException if a date field cannot be parsed
   */
  public static News parse(WebScriptRequest req)
    throws IOException, ParseException, java.text.ParseException {
    News result = new News();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String id = (String) json.get(ID);
    if (id != null) {
      result.setId(id);
    }

    String content = (String) json.get(CONTENT);
    if (content != null) {
      result.setContent(content);
    }

    String pattern = (String) json.get(PATTERN);
    if (pattern != null) {
      parsePattern(result, json, pattern);
    }

    String layout = (String) json.get(LAYOUT);
    if (layout != null) {
      result.setLayout(News.LayoutEnum.fromValue(layout));
    }

    result.setSize(Integer.parseInt(json.get(SIZE).toString()));

    parseUrl(result, (String) json.get(URL));

    return result;
  }

  /**
   * Applies the pattern-specific fields to the {@link News} result.
   *
   * <p>Sets the pattern enum and, depending on its value, reads the associated date (for the
   * {@code DATE} pattern) and the title. For the {@code IFRAME} pattern an empty title is set;
   * otherwise a title must be present in the payload.
   *
   * @param result the news object being populated
   * @param json the parsed JSON payload
   * @param pattern the raw pattern value read from the payload
   * @throws ParseException if the title is required but missing
   * @throws java.text.ParseException if the date field cannot be parsed
   */
  private static void parsePattern(News result, JSONObject json, String pattern)
    throws ParseException, java.text.ParseException {
    result.setPattern(News.PatternEnum.fromValue(pattern));

    if (result.getPattern().equals(News.PatternEnum.DATE)) {
      String date = (String) json.get(DATE);
      if (date != null) {
        result.setDate(
          new LocalDate(Converter.convertStringToSimpleDate(date))
        );
      }
    }

    if (result.getPattern().equals(News.PatternEnum.IFRAME)) {
      result.setTitle(new I18nProperty());
    } else {
      JSONObject titles = (JSONObject) json.get(TITLE);
      if (titles == null) {
        throw new ParseException(0, "Empty title");
      }
      result.setTitle(Converter.toI18NProperty(titles));
    }
  }

  /**
   * Validates and applies the URL to the {@link News} result.
   *
   * <p>A blank or {@code null} URL is ignored. A non-empty URL must be a valid URL according
   * to {@link UrlValidator}.
   *
   * @param result the news object being populated
   * @param url the URL value read from the payload, may be {@code null} or empty
   * @throws IOException if the URL is non-empty but not valid
   */
  private static void parseUrl(News result, String url) throws IOException {
    if (url != null && !url.isEmpty()) {
      UrlValidator urlValidator = new UrlValidator();
      if (!urlValidator.isValid(url)) {
        throw new IOException("Invalid URL: " + url);
      }
      result.setUrl(url);
    }
  }
}
