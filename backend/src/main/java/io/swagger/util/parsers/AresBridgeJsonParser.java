package io.swagger.util.parsers;

import io.swagger.model.ExternalRepositoryTransaction;
import io.swagger.model.TicketRequestInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Utility class for parsing the JSON payloads exchanged with the ARES bridge.
 *
 * <p>It reads the raw request body from an Alfresco {@link WebScriptRequest}
 * and converts it into the corresponding domain models used by the CIRCABC
 * REST layer. Two payload shapes are supported:
 *
 * <ul>
 *   <li>a ticket request (see {@link #parse(WebScriptRequest)}), and</li>
 *   <li>an external repository transaction with its affected nodes
 *       (see {@link #parseTransaction(WebScriptRequest)}).</li>
 * </ul>
 *
 * <p>This class is not meant to be instantiated; all functionality is exposed
 * through static methods.
 */
public class AresBridgeJsonParser {

  /** JSON key holding the request date of a ticket request. */
  private static final String REQUEST_DATE = "requestDate";
  /** JSON key holding the HTTP verb of a ticket request. */
  private static final String HTTP_VERB = "httpVerb";
  /** JSON key holding the target path of a ticket request. */
  private static final String PATH = "path";

  /** JSON key holding the array of nodes within a transaction payload. */
  private static final String NODES = "nodes";
  /** JSON key holding the transaction identifier. */
  private static final String TRANSACTION_ID = "transactionId";
  /** JSON key holding a node identifier. */
  private static final String ID = "id";
  /** JSON key holding a node name. */
  private static final String NAME = "name";
  /** JSON key holding the properties object of a node. */
  private static final String PROPERTIES = "properties";
  /** JSON key holding the version label within a node's properties. */
  private static final String VERSION_LABEL = "versionLabel";

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class only exposes static
   *     helpers
   */
  private AresBridgeJsonParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses the body of a ticket request into a {@link TicketRequestInfo}.
   *
   * <p>The request date, HTTP verb and path are read from the JSON payload
   * when present; missing fields are simply left unset on the result.
   *
   * @param req the web script request whose JSON body is parsed; its content
   *     is expected to be a JSON object
   * @return a {@link TicketRequestInfo} populated with the fields found in the
   *     payload
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
  public static TicketRequestInfo parse(WebScriptRequest req)
    throws IOException, ParseException {
    TicketRequestInfo result = new TicketRequestInfo();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    Object requestDate = json.get(REQUEST_DATE);
    if (requestDate != null) {
      result.setRequestDate(requestDate.toString());
    }

    Object httpVerb = json.get(HTTP_VERB);
    if (httpVerb != null) {
      result.setHttpVerb(httpVerb.toString());
    }

    Object path = json.get(PATH);
    if (path != null) {
      result.setPath(path.toString());
    }

    return result;
  }

  /**
   * Parses the body of a transaction request into a list of
   * {@link ExternalRepositoryTransaction} instances, one per node.
   *
   * <p>The transaction identifier is read from the top-level payload and
   * applied to every node. For each node, its id, name and version label
   * (nested under {@code properties}) are populated when present.
   *
   * @param req the web script request whose JSON body is parsed; its content
   *     is expected to be a JSON object optionally containing a {@code nodes}
   *     array
   * @return a list of transactions, one for each node in the payload; empty if
   *     no nodes are present
   * @throws IOException if the request content cannot be read
   * @throws ParseException if the request body is not valid JSON
   */
  public static List<ExternalRepositoryTransaction> parseTransaction(
    WebScriptRequest req
  ) throws IOException, ParseException {
    List<ExternalRepositoryTransaction> result = new ArrayList<>();

    String cBody = req.getContent().getContent();
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(cBody);

    String transId = "";
    Object transactionId = json.get(TRANSACTION_ID);
    if (transactionId != null) {
      transId = transactionId.toString();
    }

    JSONArray nodes = (JSONArray) json.get(NODES);
    if (nodes != null) {
      for (Object node : nodes) {
        ExternalRepositoryTransaction transaction =
          new ExternalRepositoryTransaction();
        transaction.setTransactionId(transId);
        JSONObject nodeJson = (JSONObject) node;
        Object nodeId = nodeJson.get(ID);
        if (nodeId != null) {
          transaction.setNodeId(nodeId.toString());
        }
        Object name = nodeJson.get(NAME);
        if (name != null) {
          transaction.setName(name.toString());
        }
        Object properties = nodeJson.get(PROPERTIES);
        JSONObject propertiesJson = (JSONObject) properties;
        Object versionLabel = propertiesJson.get(VERSION_LABEL);
        if (versionLabel != null) {
          transaction.setVersionLabel(versionLabel.toString());
        }
        result.add(transaction);
      }
    }

    return result;
  }
}
