package io.swagger.util.parsers;

import io.swagger.model.AppMessage;
import java.io.IOException;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that converts the JSON body of a web script request into an
 * {@link AppMessage} domain object.
 *
 * <p>It is used by the application-message REST endpoints to read the payload
 * sent by clients. The parser is lenient: missing optional fields are filled
 * with sensible defaults ({@code level="info"}, {@code enabled=false} and
 * {@code displayTime=15}) rather than causing a failure.
 *
 * <p>This is a non-instantiable helper class exposing only static methods.
 *
 * @author beaurpi
 */
public class AppMessageJsonParser {

  /** JSON key holding the application message identifier. */
  private static final String ID = "id";
  /** JSON key holding the message body/content. */
  private static final String CONTENT = "content";
  /** JSON key holding the closure date (ISO-8601 date-time). */
  private static final String DATE_CLOSURE = "dateClosure";
  /** JSON key holding the display time, in seconds. */
  private static final String DISPLAY_TIME = "displayTime";
  /** JSON key holding the severity level (e.g. {@code info}, {@code warning}). */
  private static final String LEVEL = "level";
  /** JSON key holding the enabled flag. */
  private static final String ENABLED = "enabled";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be
   *     instantiated
   */
  private AppMessageJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into an
   * {@link AppMessage}.
   *
   * <p>Recognised fields are {@code id}, {@code content}, {@code dateClosure},
   * {@code level}, {@code enabled} and {@code displayTime}. When {@code level},
   * {@code enabled} or {@code displayTime} are absent they default to
   * {@code "info"}, {@code false} and {@code 15} respectively. Empty
   * {@code id} and {@code dateClosure} values are ignored.
   *
   * @param req the web script request whose content is the JSON payload to
   *     parse
   * @return the populated {@link AppMessage} built from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static AppMessage parse(WebScriptRequest req)
    throws IOException, ParseException {
    AppMessage result = new AppMessage();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Object idObj = json.get(ID);
    if (idObj != null) {
      String idStr = idObj.toString();
      if (!"".equals(idStr)) {
        result.setId(Integer.parseInt(idObj.toString()));
      }
    }

    Object contentObj = json.get(CONTENT);
    if (contentObj != null) {
      result.setContent(contentObj.toString());
    }

    Object dateObj = json.get(DATE_CLOSURE);
    if (dateObj != null) {
      String dateStr = dateObj.toString();
      if (!"".equals(dateStr)) {
        result.setDateClosure(DateTime.parse(dateStr));
      }
    }

    Object levelObj = json.get(LEVEL);
    if (levelObj != null) {
      result.setLevel(levelObj.toString());
    } else {
      result.setLevel("info");
    }

    Object enabledObj = json.get(ENABLED);
    if (enabledObj != null) {
      result.setEnabled(Boolean.valueOf(enabledObj.toString()));
    } else {
      result.setEnabled(false);
    }

    Object timeObj = json.get(DISPLAY_TIME);
    if (timeObj != null) {
      result.setDisplayTime(Integer.parseInt(timeObj.toString()));
    } else {
      result.setDisplayTime(15);
    }

    return result;
  }

  /**
   * Reads the raw request body and interprets it as a boolean value.
   *
   * @param req the web script request whose content is the boolean payload
   * @return the parsed {@link Boolean}, or {@code null} if the request body is
   *     absent
   * @throws IOException if the request content cannot be read
   */
  @SuppressWarnings("java:S2447")
  public static Boolean parseBoolean(WebScriptRequest req) throws IOException {
    String cBody = req.getContent().getContent();

    if (cBody != null) {
      return Boolean.parseBoolean(cBody);
    }

    return null;
  }
}
