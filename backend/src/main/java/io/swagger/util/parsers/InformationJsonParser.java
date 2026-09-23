package io.swagger.util.parsers;

import io.swagger.model.InformationPage;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that builds an {@link InformationPage} from the JSON body of a
 * web script request.
 *
 * <p>It reads the {@code adapt} and {@code displayOldInformation} boolean flags
 * from the incoming JSON payload and maps them onto an {@link InformationPage}
 * instance. Only flags that are present in the payload are applied, leaving the
 * remaining defaults of {@link InformationPage} untouched.
 *
 * <p>This is a stateless utility class and cannot be instantiated.
 *
 * @author beaurpi
 */
public class InformationJsonParser {

  /** JSON property name for the "adapt" boolean flag. */
  private static final String ADAPT = "adapt";
  /** JSON property name for the "displayOldInformation" boolean flag. */
  private static final String DISPLAY_OLD_INFO = "displayOldInformation";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be
   *     instantiated
   */
  private InformationJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into an
   * {@link InformationPage}.
   *
   * <p>The {@code adapt} and {@code displayOldInformation} flags are read from
   * the request payload and applied to the returned object only when present.
   *
   * @param req the web script request whose content holds the JSON payload
   * @return an {@link InformationPage} populated from the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request content is not valid JSON
   */
  public static InformationPage parse(WebScriptRequest req)
    throws IOException, ParseException {
    InformationPage body = new InformationPage();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Boolean adapt = (Boolean) json.get(ADAPT);
    if (adapt != null) {
      body.setAdapt(adapt);
    }

    Boolean displayOldInformation = (Boolean) json.get(DISPLAY_OLD_INFO);
    if (displayOldInformation != null) {
      body.setDisplayOldInformation(displayOldInformation);
    }

    return body;
  }
}
