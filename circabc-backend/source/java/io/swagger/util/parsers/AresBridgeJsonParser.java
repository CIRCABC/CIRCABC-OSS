package io.swagger.util.parsers;

import io.swagger.model.ExternalRepositoryTransaction;
import io.swagger.model.TicketRequestInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AresBridgeJsonParser {

    private static final String REQUEST_DATE = "requestDate";
    private static final String HTTP_VERB = "httpVerb";
    private static final String PATH = "path";

    private static final String NODES = "nodes";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PROPERTIES = "properties";
    private static final String VERSION_LABEL = "versionLabel";

    private AresBridgeJsonParser() {
        throw new IllegalStateException("Utility class");
    }

    public static TicketRequestInfo parse(WebScriptRequest req) throws IOException, ParseException {

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

    public static List<ExternalRepositoryTransaction> parseTransaction(WebScriptRequest req)
            throws IOException, ParseException {

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
                ExternalRepositoryTransaction transaction = new ExternalRepositoryTransaction();
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
