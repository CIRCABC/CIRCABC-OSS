package io.swagger.util.parsers;

import io.swagger.model.PreferenceConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class UserJsonParser {

    public static final String LIMIT = "limit";
    private static final Log logger = LogFactory.getLog(UserJsonParser.class);

    private UserJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static PreferenceConfiguration parsePreference(WebScriptRequest req) throws IOException {
        String cBody = req.getContent().getContent();
        return parsePreference(cBody);
    }

    public static PreferenceConfiguration parsePreference(String string) {
        PreferenceConfiguration preference = new PreferenceConfiguration();
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(string);
            JSONObject library = (JSONObject) json.get("library");
            JSONObject column = (JSONObject) library.get("column");
            Boolean name = (Boolean) column.get("name");
            Boolean version = (Boolean) column.get("version");
            Boolean modification = (Boolean) column.get("modification");
            Boolean creation = (Boolean) column.get("creation");
            Boolean size = (Boolean) column.get("size");
            Boolean expiration = (Boolean) column.get("expiration");
            Boolean status = (Boolean) column.get("status");
            Boolean description = (Boolean) column.get("description");
            Boolean author = (Boolean) column.get("author");
            Boolean title = (Boolean) column.get("title");
            Boolean securityRanking = (Boolean) column.get("securityRanking");


            preference.getLibrary().getColumn().setName(name);
            preference.getLibrary().getColumn().setVersion(version);
            preference.getLibrary().getColumn().setModification(modification);
            preference.getLibrary().getColumn().setCreation(creation);
            preference.getLibrary().getColumn().setSize(size);
            preference.getLibrary().getColumn().setExpiration(expiration);
            preference.getLibrary().getColumn().setStatus(status);
            preference.getLibrary().getColumn().setDescription(description);
            preference.getLibrary().getColumn().setAuthor(author);
            preference.getLibrary().getColumn().setTitle(title);
            preference.getLibrary().getColumn().setSecurityRanking(securityRanking);

            JSONObject listing = (JSONObject) library.get("listing");
            if (listing.get("page") instanceof Long) {
                preference
                        .getLibrary()
                        .getListing()
                        .setPage(Integer.parseInt(listing.get("page").toString()));
            }

            if (listing.get(LIMIT) instanceof Long) {
                preference
                        .getLibrary()
                        .getListing()
                        .setLimit(Integer.parseInt(listing.get(LIMIT).toString()));
            }

            String sort = (String) listing.get("sort");
            preference.getLibrary().getListing().setSort(sort);

        } catch (ParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }

        return preference;
    }

    public static JSONObject parsePreferenceAsJson(String string) {
        try {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(string);
        } catch (ParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }

        return null;
    }
}
