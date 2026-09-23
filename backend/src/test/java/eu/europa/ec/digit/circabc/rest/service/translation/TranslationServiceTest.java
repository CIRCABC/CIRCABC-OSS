package eu.europa.ec.digit.circabc.rest.service.translation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;

public class TranslationServiceTest {

  private TranslationServiceImpl service;
  private NodeService nodeService;
  private AuthenticationService authenticationService;
  private TranslationDaoService translationDaoService;
  private MachineTranslationService machineTranslationService;
  private CircabcApi circabcApi;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
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

    service = new TranslationServiceImpl();
    nodeService = mock(NodeService.class);
    authenticationService = mock(AuthenticationService.class);
    translationDaoService = mock(TranslationDaoService.class);
    machineTranslationService = mock(MachineTranslationService.class);
    circabcApi = mock(CircabcApi.class);
    circabcConfig = mock(CircabcConfig.class);

    service.setNodeService(nodeService);
    service.setAuthenticationService(authenticationService);
    service.setTranslationDaoService(translationDaoService);
    service.setMachineTranslationService(machineTranslationService);
    service.setFtpUrl("ftp://localhost");
    service.setFileExtensions(Set.of("docx", "pdf", "txt", "xlsx"));
    service.setLanguages(Set.of("EN", "FR", "DE"));
    service.setMaxFileSizeInBytes(10485760L);

    setField("circabcConfig", circabcConfig);
    setField("circabcApi", circabcApi);

    when(circabcConfig.getWebRootUrl()).thenReturn("https://circabc.europa.eu");
    when(circabcConfig.getMTCallbackUrl()).thenReturn(
      "https://callback.europa.eu"
    );
    when(circabcConfig.getMtUsername()).thenReturn("mtuser");
    when(circabcConfig.getMtPassword()).thenReturn("mtpass");
    when(circabcConfig.getMTApplicationName()).thenReturn("CIRCABC");

    service.init();
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TranslationServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testGetAvailableLanguages_returnsConfiguredLanguages() {
    Set<String> result = service.getAvailableLanguages();

    assertEquals(Set.of("EN", "FR", "DE"), result);
  }

  @Test
  public void testGetAvailableFileExtensions_returnsConfiguredExtensions() {
    Set<String> result = service.getAvailableFileExtensions();

    assertEquals(Set.of("docx", "pdf", "txt", "xlsx"), result);
  }

  @Test
  public void testFileMaxSize_returnsConfiguredValue() {
    assertEquals(10485760L, service.fileMaxSize());
  }

  @Test
  public void testCanBeTranslated_whenSupportedExtension_thenReturnsTrue() {
    assertTrue(service.canBeTranslated("document.docx"));
  }

  @Test
  public void testCanBeTranslated_whenUpperCaseExtension_thenReturnsTrue() {
    assertTrue(service.canBeTranslated("document.DOCX"));
  }

  @Test
  public void testCanBeTranslated_whenUnsupportedExtension_thenReturnsFalse() {
    assertFalse(service.canBeTranslated("image.png"));
  }

  @Test
  public void testCanBeTranslated_whenNoExtension_thenReturnsFalse() {
    assertFalse(service.canBeTranslated("noextension"));
  }

  @Test
  public void testCanBeTranslated_whenDotAtStart_thenReturnsFalse() {
    assertFalse(service.canBeTranslated(".hidden"));
  }

  @Test
  public void testGetMTUserDetails_returnsCorrectUserBean() {
    CircabcUserDataBean result = service.getMTUserDetails();

    assertEquals("mtuser", result.getUserName());
    assertEquals("mtpass", result.getPassword());
    assertEquals("Machine translation", result.getFirstName());
    assertEquals("Machine translation", result.getLastName());
    assertEquals("DGT-MT@ec.europa.eu", result.getEmail());
    assertFalse(result.getVisibility());
    assertFalse(result.getGlobalNotification());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyDocumentToBeTranslated_whenNotContent_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);

    service.copyDocumentToBeTranslated(nodeRef);
  }

  @Test
  public void testCleanTempSpace_whenMonthFolderExists_thenDeletesIt() {
    NodeRef mtRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-root"
    );
    NodeRef yearFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "year-folder"
    );
    NodeRef monthFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "month-folder"
    );

    when(circabcApi.getMTNodeRef()).thenReturn(mtRoot);
    when(
      nodeService.getChildByName(mtRoot, ContentModel.ASSOC_CONTAINS, "2024")
    ).thenReturn(yearFolder);
    when(
      nodeService.getChildByName(yearFolder, ContentModel.ASSOC_CONTAINS, "3")
    ).thenReturn(monthFolder);

    service.cleanTempSpace(2024, 3);

    verify(nodeService).addAspect(
      monthFolder,
      ContentModel.ASPECT_TEMPORARY,
      null
    );
    verify(nodeService).deleteNode(monthFolder);
  }

  @Test
  public void testCleanTempSpace_whenYearFolderDoesNotExist_thenDoesNothing() {
    NodeRef mtRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-root"
    );

    when(circabcApi.getMTNodeRef()).thenReturn(mtRoot);
    when(
      nodeService.getChildByName(mtRoot, ContentModel.ASSOC_CONTAINS, "2024")
    ).thenReturn(null);

    service.cleanTempSpace(2024, 3);

    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }

  @Test
  public void testCleanTempSpace_whenMonthFolderDoesNotExist_thenDoesNothing() {
    NodeRef mtRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "mt-root"
    );
    NodeRef yearFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "year-folder"
    );

    when(circabcApi.getMTNodeRef()).thenReturn(mtRoot);
    when(
      nodeService.getChildByName(mtRoot, ContentModel.ASSOC_CONTAINS, "2024")
    ).thenReturn(yearFolder);
    when(
      nodeService.getChildByName(yearFolder, ContentModel.ASSOC_CONTAINS, "3")
    ).thenReturn(null);

    service.cleanTempSpace(2024, 3);

    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }
}
