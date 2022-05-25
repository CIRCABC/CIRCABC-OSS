package io.swagger.util.parsers;

import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import io.swagger.util.Converter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class DynamicPropertyDefinitionJsonParser {

    private static final String STATUS = "status";
    private static final String OLD = "old";
    private static final String NEW = "_new";
    private static final String TITLE = "title";
    private static final String ID = "id";
    private static final String POSSIBLE_VALUES = "possibleValues";
    private static final String UPDATED_VALUES = "updatedValues";
    private static final String TYPE = "propertyType";

    private DynamicPropertyDefinitionJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static DynamicPropertyDefinition parseJsonDynamicPropertyDefinition(WebScriptRequest req)
            throws IOException, ParseException {
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
