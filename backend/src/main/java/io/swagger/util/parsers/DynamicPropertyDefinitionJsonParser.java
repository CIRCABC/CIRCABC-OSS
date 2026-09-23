package io.swagger.util.parsers;

import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import io.swagger.util.Converter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that reads the JSON request body of a web script and builds a
 * {@link DynamicPropertyDefinition} domain object from it.
 *
 * <p>The expected JSON payload contains the identifier, a localized title, the
 * property type, an optional list of possible values and an optional list of
 * value updates (each describing the transition from an old value to a new one
 * together with its status). This class only performs parsing/conversion; it is
 * not an Alfresco web script endpoint itself but is meant to be invoked from
 * such endpoints.
 *
 * <p>As a stateless utility, this class exposes only static behavior and cannot
 * be instantiated.
 *
 * @author beaurpi
 */
public class DynamicPropertyDefinitionJsonParser {

  /** JSON key holding the status of an updated value entry. */
  private static final String STATUS = "status";
  /** JSON key holding the previous (old) value of an updated value entry. */
  private static final String OLD = "old";
  /** JSON key holding the replacement (new) value of an updated value entry. */
  private static final String NEW = "_new";
  /** JSON key holding the localized title of the property definition. */
  private static final String TITLE = "title";
  /** JSON key holding the identifier of the property definition. */
  private static final String ID = "id";
  /** JSON key holding the array of allowed/possible values. */
  private static final String POSSIBLE_VALUES = "possibleValues";
  /** JSON key holding the array of value update entries. */
  private static final String UPDATED_VALUES = "updatedValues";
  /** JSON key holding the property type. */
  private static final String TYPE = "propertyType";

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class must not be
   *     instantiated
   */
  private DynamicPropertyDefinitionJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into a
   * {@link DynamicPropertyDefinition}.
   *
   * <p>The identifier, possible values and updated values are optional, while a
   * non-empty title and property type are mandatory. The title is converted
   * into an internationalized property via {@link Converter#toI18NProperty}.
   *
   * @param req the web script request whose content is the JSON payload to
   *     parse; must not be {@code null}
   * @return the {@link DynamicPropertyDefinition} populated from the request
   *     body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the content is not valid JSON, or if the
   *     mandatory title or property type is missing
   */
  public static DynamicPropertyDefinition parseJsonDynamicPropertyDefinition(
    WebScriptRequest req
  ) throws IOException, ParseException {
    DynamicPropertyDefinition body = new DynamicPropertyDefinition();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String id = String.valueOf(json.get(ID));
    if (id != null) {
      body.setId(id);
    }

    JSONObject titles = (JSONObject) json.get(TITLE);
    if (titles == null) {
      throw new ParseException(0, "Empty title");
    }

    body.setTitle(Converter.toI18NProperty(titles));

    String type = String.valueOf(json.get(TYPE));
    if (type == null) {
      throw new ParseException(0, "Empty type");
    }

    body.setPropertyType(type);

    JSONArray values = (JSONArray) json.get(POSSIBLE_VALUES);
    if (values != null) {
      for (Object value : values) {
        body.getPossibleValues().add(value.toString());
      }
    }

    JSONArray updatedValues = (JSONArray) json.get(UPDATED_VALUES);
    if (updatedValues != null) {
      for (Object value : updatedValues) {
        DynamicPropertyDefinitionUpdatedValues update =
          new DynamicPropertyDefinitionUpdatedValues();
        JSONObject item = (JSONObject) value;
        update.setNewValue(String.valueOf(item.get(NEW)));
        update.setOld(String.valueOf(item.get(OLD)));
        update.setStatus(String.valueOf(item.get(STATUS)));
        body.getUpdatedValues().add(update);
      }
    }

    return body;
  }
}
