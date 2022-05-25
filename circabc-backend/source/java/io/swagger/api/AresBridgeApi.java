package io.swagger.api;

import java.util.Collection;

import eu.cec.digit.circabc.repo.ares.AresBridgeDAO;
import eu.cec.digit.circabc.repo.external.repositories.RepositoryConfiguration;

public interface AresBridgeApi {

    String getTicket(String requestDate, String httpVerb, String path);

    Collection<RepositoryConfiguration> getExternalRepositories(String id);

    void addExternalRepositories(String id, String name);

    void deleteExternalRepository(String id, String name);

    Collection<String> getAvailableExternalRepositories();

    boolean validateAuthorizationHeader(String dateHeader, String authorizationHeader, String path);

    boolean validateToken(String dateHeader, String token, String path, String method);

    void saveTransaction(String groupId, String repositoryId, String transactionId, String nodeId, String versionLabel,
            String name);

    Collection<AresBridgeDAO> nodeLog(String nodeId, String name);

    Collection<AresBridgeDAO> groupLog(String groupId, String name);
}
