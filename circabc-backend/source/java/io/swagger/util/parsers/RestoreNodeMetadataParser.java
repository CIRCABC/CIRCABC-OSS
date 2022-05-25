package io.swagger.util.parsers;

import io.swagger.model.RestoreNodeMetadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;

/**
 * @author beaurpi
 */
public class RestoreNodeMetadataParser {

    private static final String ARCHIVE_NODE_ID = "archiveNodeId";
    private static final String TARGET_FOLDER_ID = "targetFolderId";

    private RestoreNodeMetadataParser() {
        throw new IllegalStateException("Utility class");
    }

    public static RestoreNodeMetadata parseJSon(WebScriptRequest req)
            throws IOException, ParseException {

        String cBody = req.getContent().getContent();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(cBody);

        RestoreNodeMetadata result = new RestoreNodeMetadata();

        String id = (String) json.get(ARCHIVE_NODE_ID);
        if (id != null) {
            result.setArchiveNodeId(id);
        }

        String folderId = (String) json.get(TARGET_FOLDER_ID);
        if (folderId != null) {
            result.setTargetFolderId(folderId);
        }

        return result;
    }
}
