package eu.europa.ec.digit.circabc.rest.service.ares;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.model.db.AresBridgeDAO;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;

public class AresBridgeServiceImplTest {

  private AresBridgeServiceImpl service;
  private AresBridgeDaoService aresBridgeDaoService;
  private ExternalRepositoriesManagementService externalRepositoriesManagementService;

  @Before
  public void setUp() throws Exception {
    service = new AresBridgeServiceImpl();
    aresBridgeDaoService = mock(AresBridgeDaoService.class);
    externalRepositoriesManagementService = mock(
      ExternalRepositoriesManagementService.class
    );

    setField("aresBridgeDaoService", aresBridgeDaoService);
    setField(
      "externalRepositoriesManagementService",
      externalRepositoriesManagementService
    );

    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");
  }

  @Test
  public void testProcess_whenRegisterResponse_thenSavesMetadataAndUpdates() {
    AresBridgeDAO response = createDAO(
      "test-node-id",
      "tx-1",
      "register",
      "doc-1",
      "save-1",
      "reg-1"
    );
    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.singletonList(response)
    );

    service.process();

    verify(externalRepositoriesManagementService).saveExternalMetadata(
      "AresBridge",
      "workspace://SpacesStore/test-node-id",
      "doc-1",
      "save-1",
      "reg-1",
      "register",
      "tx-1"
    );
    verify(aresBridgeDaoService).updateResponse("tx-1", "register");
  }

  @Test
  public void testProcess_whenSaveResponse_thenSavesMetadataAndUpdates() {
    AresBridgeDAO response = createDAO(
      "test-node-id",
      "tx-2",
      "save",
      "doc-2",
      "save-2",
      "reg-2"
    );
    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.singletonList(response)
    );

    service.process();

    verify(externalRepositoriesManagementService).saveExternalMetadata(
      "AresBridge",
      "workspace://SpacesStore/test-node-id",
      "doc-2",
      "save-2",
      "reg-2",
      "save",
      "tx-2"
    );
    verify(aresBridgeDaoService).updateResponse("tx-2", "save");
  }

  @Test
  public void testProcess_whenUnknownRequestType_thenOnlyUpdates() {
    AresBridgeDAO response = createDAO(
      "test-node-id",
      "tx-3",
      "unknown",
      "doc-3",
      "save-3",
      "reg-3"
    );
    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.singletonList(response)
    );

    service.process();

    verifyNoInteractions(externalRepositoriesManagementService);
    verify(aresBridgeDaoService).updateResponse("tx-3", "unknown");
  }

  @Test
  public void testProcess_whenEmptyResponses_thenNoInteractions() {
    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.emptyList()
    );

    service.process();

    verifyNoInteractions(externalRepositoriesManagementService);
    verify(aresBridgeDaoService, never()).updateResponse(
      anyString(),
      anyString()
    );
  }

  @Test
  public void testProcess_whenExceptionThrown_thenDoesNotPropagate() {
    when(aresBridgeDaoService.getResponses()).thenThrow(
      new RuntimeException("DB error")
    );

    service.process();

    verifyNoInteractions(externalRepositoriesManagementService);
  }

  @Test
  public void testProcess_whenMultipleResponses_thenProcessesAll() {
    AresBridgeDAO r1 = createDAO("id-1", "tx-1", "register", "d1", "s1", "r1");
    AresBridgeDAO r2 = createDAO("id-2", "tx-2", "save", "d2", "s2", "r2");
    List<AresBridgeDAO> responses = new ArrayList<>();
    responses.add(r1);
    responses.add(r2);
    when(aresBridgeDaoService.getResponses()).thenReturn(responses);

    service.process();

    verify(externalRepositoriesManagementService).saveExternalMetadata(
      "AresBridge",
      "workspace://SpacesStore/id-1",
      "d1",
      "s1",
      "r1",
      "register",
      "tx-1"
    );
    verify(externalRepositoriesManagementService).saveExternalMetadata(
      "AresBridge",
      "workspace://SpacesStore/id-2",
      "d2",
      "s2",
      "r2",
      "save",
      "tx-2"
    );
    verify(aresBridgeDaoService).updateResponse("tx-1", "register");
    verify(aresBridgeDaoService).updateResponse("tx-2", "save");
  }

  private AresBridgeDAO createDAO(
    String nodeId,
    String transactionId,
    String requestType,
    String documentId,
    String saveNumber,
    String registrationNumber
  ) {
    AresBridgeDAO dao = new AresBridgeDAO();
    dao.setNodeId(nodeId);
    dao.setTransactionId(transactionId);
    dao.setRequestType(requestType);
    dao.setDocumentId(documentId);
    dao.setSaveNumber(saveNumber);
    dao.setRegistrationNumber(registrationNumber);
    return dao;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AresBridgeServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
