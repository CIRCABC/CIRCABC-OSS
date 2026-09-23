package eu.europa.ec.digit.circabc.rest.service.ares;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.model.db.AresBridgeDAO;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;

public class AresBridgeServiceTest {

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
    AresBridgeDAO response = new AresBridgeDAO();
    response.setNodeId("test-node-id");
    response.setRequestType("register");
    response.setDocumentId("doc-123");
    response.setSaveNumber("save-1");
    response.setRegistrationNumber("reg-456");
    response.setTransactionId("tx-789");

    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.singletonList(response)
    );

    service.process();

    verify(externalRepositoriesManagementService).saveExternalMetadata(
      AresBridgeApiImpl.ARES_BRIDGE,
      "workspace://SpacesStore/test-node-id",
      "doc-123",
      "save-1",
      "reg-456",
      "register",
      "tx-789"
    );
    verify(aresBridgeDaoService).updateResponse("tx-789", "register");
  }

  @Test
  public void testProcess_whenSaveResponse_thenSavesMetadataAndUpdates() {
    AresBridgeDAO response = new AresBridgeDAO();
    response.setNodeId("node-2");
    response.setRequestType("save");
    response.setDocumentId("doc-2");
    response.setSaveNumber("save-2");
    response.setRegistrationNumber("reg-2");
    response.setTransactionId("tx-2");

    when(aresBridgeDaoService.getResponses()).thenReturn(
      Collections.singletonList(response)
    );

    service.process();

    verify(externalRepositoriesManagementService).saveExternalMetadata(
      AresBridgeApiImpl.ARES_BRIDGE,
      "workspace://SpacesStore/node-2",
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
    AresBridgeDAO response = new AresBridgeDAO();
    response.setNodeId("node-3");
    response.setRequestType("unknown");
    response.setTransactionId("tx-3");

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
  public void testProcess_whenMultipleResponses_thenProcessesAll() {
    AresBridgeDAO r1 = new AresBridgeDAO();
    r1.setNodeId("node-a");
    r1.setRequestType("register");
    r1.setDocumentId("doc-a");
    r1.setSaveNumber("s-a");
    r1.setRegistrationNumber("reg-a");
    r1.setTransactionId("tx-a");

    AresBridgeDAO r2 = new AresBridgeDAO();
    r2.setNodeId("node-b");
    r2.setRequestType("save");
    r2.setDocumentId("doc-b");
    r2.setSaveNumber("s-b");
    r2.setRegistrationNumber("reg-b");
    r2.setTransactionId("tx-b");

    when(aresBridgeDaoService.getResponses()).thenReturn(Arrays.asList(r1, r2));

    service.process();

    verify(
      externalRepositoriesManagementService,
      times(2)
    ).saveExternalMetadata(
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyString()
    );
    verify(aresBridgeDaoService).updateResponse("tx-a", "register");
    verify(aresBridgeDaoService).updateResponse("tx-b", "save");
  }

  @Test
  public void testProcess_whenExceptionThrown_thenDoesNotPropagate() {
    when(aresBridgeDaoService.getResponses()).thenThrow(
      new RuntimeException("DB error")
    );

    service.process();

    // Should not throw — exception is caught and logged
    verifyNoInteractions(externalRepositoriesManagementService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AresBridgeServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
