package eu.europa.ec.digit.circabc.rest.service.ares;

import eu.europa.ec.digit.circabc.rest.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.model.db.AresBridgeDAO;
import io.swagger.util.Converter;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link AresBridgeService} that processes responses received from the
 * Ares system (the European Commission's document registration and management platform).
 *
 * <p>This service polls the persisted Ares bridge responses via {@link AresBridgeDaoService} and,
 * for each response, updates the corresponding CIRCABC node's external metadata (for
 * {@code register} and {@code save} request types) through the
 * {@link ExternalRepositoriesManagementService}. Each processed response is then marked as handled.
 * The whole workflow runs under the system user context and is typically invoked by a scheduled job.
 */
public class AresBridgeServiceImpl implements AresBridgeService {

  /** Logger used to report errors that occur while processing Ares bridge responses. */
  private static final Log logger = LogFactory.getLog(
    AresBridgeServiceImpl.class
  );

  /** DAO service providing access to pending Ares bridge responses and their status updates. */
  @Autowired
  private AresBridgeDaoService aresBridgeDaoService;

  /** Service used to persist the external (Ares) metadata onto the target CIRCABC nodes. */
  @Autowired
  private ExternalRepositoriesManagementService externalRepositoriesManagementService;

  /**
   * Processes all pending Ares bridge responses.
   *
   * <p>Runs as the system user and iterates over every response returned by
   * {@link AresBridgeDaoService#getResponses()}. For responses whose request type is
   * {@code register} or {@code save}, the associated node's external metadata (document id, save
   * number, registration number, request type and transaction id) is saved via the
   * {@link ExternalRepositoriesManagementService}. Every response is subsequently updated to mark
   * it as processed. Any exception raised during processing is caught and logged so that a single
   * failing response does not abort the run; the security context is always cleared afterwards.
   */
  @Override
  public void process() {
    try {
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
      List<AresBridgeDAO> responses = this.aresBridgeDaoService.getResponses();
      for (AresBridgeDAO response : responses) {
        NodeRef nodeRef = Converter.createNodeRefFromId(response.getNodeId());
        if (
          response.getRequestType().equals("register") ||
          response.getRequestType().equals("save")
        ) {
          externalRepositoriesManagementService.saveExternalMetadata(
            AresBridgeApiImpl.ARES_BRIDGE,
            nodeRef.toString(),
            response.getDocumentId(),
            response.getSaveNumber(),
            response.getRegistrationNumber(),
            response.getRequestType(),
            response.getTransactionId()
          );
        }
        aresBridgeDaoService.updateResponse(
          response.getTransactionId(),
          response.getRequestType()
        );
      }
    } catch (final Exception e) {
      logger.error("Error when processing ARES Bridge response ", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }
}
