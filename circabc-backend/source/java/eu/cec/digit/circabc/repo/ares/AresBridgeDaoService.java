package eu.cec.digit.circabc.repo.ares;

import java.util.List;

public interface AresBridgeDaoService {
    void updateResponse(String transactionId, String requestType);

    void saveResponse(String transactionId, String requestType, String documentId, String saveNumber,
            String registrationNumber);

    void saveRequest(String groupId, String transactionId, String nodeId, String versionLabel, String name);

    List<AresBridgeDAO> getResponses();

    List<AresBridgeDAO> getResponsesByNodeId(String nodeId);

    List<AresBridgeDAO> getResponsesByGroupId(String groupId);
}
