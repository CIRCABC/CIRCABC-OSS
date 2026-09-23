package io.swagger.util.parsers;

import io.swagger.model.RestoreNodeMetadata;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility parser that reads the JSON body of a "restore node" REST request and maps it into a
 * {@link RestoreNodeMetadata} domain object.
 *
 * <p>The expected JSON payload contains an {@code archiveNodeId} (the identifier of the node
 * currently in the archive/trash store that should be restored) and an optional
 * {@code targetFolderId} (the identifier of the folder into which the node should be restored).
 * Both fields are optional; when a field is absent from the payload the corresponding property on
 * the resulting {@link RestoreNodeMetadata} is left unset.
 *
 * <p>This class only provides static helpers and is not meant to be instantiated.
 *
 * @author beaurpi
 */
public class RestoreNodeMetadataParser {

  /** JSON attribute name holding the identifier of the archived node to restore. */
  private static final String ARCHIVE_NODE_ID = "archiveNodeId";
  /** JSON attribute name holding the identifier of the target folder for the restore. */
  private static final String TARGET_FOLDER_ID = "targetFolderId";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be instantiated
   */
  private RestoreNodeMetadataParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the JSON body of the given web script request into a {@link RestoreNodeMetadata} object.
   *
   * <p>Only the {@code archiveNodeId} and {@code targetFolderId} attributes are read; any other
   * attributes present in the payload are ignored. Missing attributes are left unset on the
   * returned object.
   *
   * @param req the incoming web script request whose content is the JSON payload to parse
   * @return a {@link RestoreNodeMetadata} populated with the values found in the request body
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
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
