package eu.cec.digit.circabc.repo.ares;

import org.mybatis.spring.SqlSessionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AresBridgeDaoServiceImpl implements AresBridgeDaoService {

    private SqlSessionTemplate sqlSessionTemplate = null;

    @Override
    public void updateResponse(String transactionId, String requestType) {
        Map<String, Object> params = new HashMap<>();

        params.put("transactionId", transactionId);
        params.put("requestType", requestType);

        sqlSessionTemplate.update("AresBridge.update_response", params);
    }

    @Override
    public void saveResponse(String transactionId, String requestType, String documentId, String saveNumber,
            String registrationNumber) {
        Map<String, Object> params = new HashMap<>();

        params.put("transactionId", transactionId);
        params.put("requestType", requestType);
        params.put("documentId", documentId);
        params.put("saveNumber", saveNumber);
        params.put("registrationNumber", registrationNumber);

        sqlSessionTemplate.insert("AresBridge.insert_response", params);
    }

    @Override
    public void saveRequest(String groupId, String transactionId, String nodeId, String versionLabel, String name) {
        Map<String, Object> params = new HashMap<>();

        params.put("groupId", groupId);
        params.put("transactionId", transactionId);
        params.put("nodeId", nodeId);
        params.put("versionLabel", versionLabel);
        params.put("name", name);

        sqlSessionTemplate.insert("AresBridge.insert_request", params);
    }

    @Override
    public List<AresBridgeDAO> getResponses() {
        return (List<AresBridgeDAO>) sqlSessionTemplate.selectList("AresBridge.select_responses");
    }

    @Override
    public List<AresBridgeDAO> getResponsesByNodeId(String nodeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        return (List<AresBridgeDAO>) sqlSessionTemplate.selectList("AresBridge.select_responses_by_node_id", params);
    }

    @Override
    public List<AresBridgeDAO> getResponsesByGroupId( String groupId) {
        Map<String, Object> params = new HashMap<>();

        params.put("groupId", groupId);
        
        return (List<AresBridgeDAO>) sqlSessionTemplate.selectList("AresBridge.select_responses_by_group_id", params);
    }

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
}
