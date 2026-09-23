package io.swagger.util.parsers;

import io.swagger.model.ApplicantAction;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that builds an {@link ApplicantAction} from the JSON payload of a web script
 * request.
 *
 * <p>The parser reads the request body as a JSON object and maps the {@code username}, {@code
 * action} and {@code message} properties onto a new {@link ApplicantAction} instance. It is used by
 * REST endpoints that process moderation decisions on membership applications (for example
 * accepting or rejecting an applicant).
 *
 * <p>This class is a stateless utility and cannot be instantiated.
 *
 * @author beaurpi
 */
public class ApplicantActionJsonParser {

  /** JSON property name holding the message associated with the action. */
  private static final String MESSAGE = "message";
  /** JSON property name holding the username of the applicant the action targets. */
  private static final String USERNAME = "username";
  /** JSON property name holding the action to perform on the applicant. */
  private static final String ACTION = "action";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class must not be instantiated
   */
  private ApplicantActionJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into an {@link ApplicantAction}.
   *
   * <p>The request content is read as a JSON object and its {@code username}, {@code action} and
   * {@code message} properties are copied onto the returned object.
   *
   * @param req the web script request whose body contains the applicant action JSON payload
   * @return an {@link ApplicantAction} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
  public static ApplicantAction parseJSON(WebScriptRequest req)
    throws IOException, ParseException {
    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    ApplicantAction body = new ApplicantAction();
    body.setUsername(String.valueOf(json.get(USERNAME)));

    body.setAction(String.valueOf(json.get(ACTION)));

    body.setMessage(String.valueOf(json.get(MESSAGE)));

    return body;
  }
}
