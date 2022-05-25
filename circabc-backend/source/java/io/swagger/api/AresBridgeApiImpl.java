package io.swagger.api;

import static io.swagger.util.ares.TokenUtils.generateToken;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.repo.ares.AresBridgeDAO;
import eu.cec.digit.circabc.repo.ares.AresBridgeDaoService;
import eu.cec.digit.circabc.repo.external.repositories.RepositoryConfiguration;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.util.Converter;

public class AresBridgeApiImpl implements AresBridgeApi {

    public static final String ARES_BRIDGE = "AresBridge";
    private String applicationName;
    private String apiKey;
    private String secret;
    private String baseURL;
    private String serviceURL;
    private String uiURL;
    private ExternalRepositoriesManagementService externalRepositoriesManagementService;
    private AresBridgeDaoService aresBridgeDaoService;
    private static final Log logger = LogFactory.getLog(AresBridgeApiImpl.class);

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUiURL() {
        return uiURL;
    }

    public void setUiURL(String uiURL) {
        this.uiURL = uiURL;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getTicket(String requestDate, String httpVerb, String path) {
        return generateToken(this.apiKey, this.secret, requestDate, httpVerb, path);
    }

    @Override
    public Collection<RepositoryConfiguration> getExternalRepositories(String id) {
        String parentNodeId = Converter.createNodeRefFromId(id).toString();
        return this.externalRepositoriesManagementService.getConfiguredRepositories(parentNodeId);
    }

    @Override
    public void addExternalRepositories(String id, String name) {
        String parentNodeId = Converter.createNodeRefFromId(id).toString();
        RepositoryConfiguration configuration = new RepositoryConfiguration();
        configuration.setName(name);
        this.externalRepositoriesManagementService.addRepository(parentNodeId, configuration);
    }

    @Override
    public void deleteExternalRepository(String id, String name) {
        String parentNodeId = Converter.createNodeRefFromId(id).toString();
        this.externalRepositoriesManagementService.removeRepository(parentNodeId, name);
    }

    @Override
    public Collection<String> getAvailableExternalRepositories() {
        Collection<String> result = new ArrayList<>(1);
        result.add(ARES_BRIDGE);
        return result;
    }

    @Override
    public boolean validateAuthorizationHeader(String dateHeader, String authorizationHeader, String path) {
        if (authorizationHeader.contains(ARES_BRIDGE) && authorizationHeader.contains(apiKey)) {
            String ticket = authorizationHeader.replace(ARES_BRIDGE, "").replace(apiKey, "").replace(":", "").trim();
            return io.swagger.util.ares.TokenUtils.validateToken(ticket, apiKey, secret, dateHeader, "POST", path);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Invalid authorization header " + authorizationHeader);
            }
            return false;
        }
    }

    @Override
    public void saveTransaction(String groupId, String repositoryId, String transactionId, String nodeId,
            String versionLabel, String name) {
        this.aresBridgeDaoService.saveRequest(groupId, transactionId, nodeId, versionLabel, name);
    }

    public ExternalRepositoriesManagementService getExternalRepositoriesManagementService() {
        return externalRepositoriesManagementService;
    }

    public void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        this.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }

    public AresBridgeDaoService getAresBridgeDaoService() {
        return aresBridgeDaoService;
    }

    public void setAresBridgeDaoService(AresBridgeDaoService aresBridgeDaoService) {
        this.aresBridgeDaoService = aresBridgeDaoService;
    }

    @Override
    public boolean validateToken(String dateHeader, String token, String path, String method) {

        return io.swagger.util.ares.TokenUtils.validateToken(token, apiKey, secret, dateHeader, method, path);
    }

    @Override
    public Collection<AresBridgeDAO> nodeLog(String nodeId, String name) {
        return aresBridgeDaoService.getResponsesByNodeId(nodeId);
    }

    @Override
    public Collection<AresBridgeDAO> groupLog(String groupId, String name) {
        return aresBridgeDaoService.getResponsesByGroupId(groupId);
    }
}
