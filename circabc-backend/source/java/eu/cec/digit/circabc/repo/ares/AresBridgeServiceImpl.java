package eu.cec.digit.circabc.repo.ares;

import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.util.Converter;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class AresBridgeServiceImpl implements AresBridgeService {

    private static final Log logger = LogFactory.getLog(AresBridgeServiceImpl.class);

    private AresBridgeDaoService aresBridgeDaoService;
    private ExternalRepositoriesManagementService externalRepositoriesManagementService;

    public AresBridgeDaoService getAresBridgeDaoService() {
        return aresBridgeDaoService;
    }

    public void setAresBridgeDaoService(AresBridgeDaoService aresBridgeDaoService) {
        this.aresBridgeDaoService = aresBridgeDaoService;
    }

    @Override
    public void process() {
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            List<AresBridgeDAO> responses = this.aresBridgeDaoService.getResponses();
            for (AresBridgeDAO response : responses) {
                NodeRef nodeRef = Converter.createNodeRefFromId(response.getNodeId());
                if (response.getRequestType().equals("register") || response.getRequestType().equals("save")) {
                    externalRepositoriesManagementService.saveExternalMetadata(AresBridgeApiImpl.ARES_BRIDGE,
                            nodeRef.toString(), response.getDocumentId(), response.getSaveNumber(),
                            response.getRegistrationNumber(), response.getRequestType(), response.getTransactionId());
                }
                aresBridgeDaoService.updateResponse(response.getTransactionId(), response.getRequestType());
            }
        } catch (final Exception e) {
            logger.error("Error when processing ARES Bridge response ", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    public ExternalRepositoriesManagementService getExternalRepositoriesManagementService() {
        return externalRepositoriesManagementService;
    }

    public void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        this.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }
}
