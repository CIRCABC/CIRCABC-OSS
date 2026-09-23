package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.DBLogServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import io.swagger.model.Configuration;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class AutoUploadManagementServiceTest {

  private AutoUploadManagementServiceImpl service;

  private AutoUploadConfigurationService autoUploadConfigurationService;
  private RuleService ruleService;
  private ActionService actionService;
  private NodeService nodeService;
  private ContentService contentService;
  private DBLogServiceImpl logService;
  private NotificationService notificationService;
  private TransactionService transactionService;
  private FileFolderService fileFolderService;
  private MimetypeService mimetypeService;
  private LockService circabcLockService;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AutoUploadManagementServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

  @Before
  public void setUp() throws Exception {
    service = new AutoUploadManagementServiceImpl();

    autoUploadConfigurationService = mock(AutoUploadConfigurationService.class);
    ruleService = mock(RuleService.class);
    actionService = mock(ActionService.class);
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    logService = mock(DBLogServiceImpl.class);
    notificationService = mock(NotificationService.class);
    transactionService = mock(TransactionService.class);
    fileFolderService = mock(FileFolderService.class);
    mimetypeService = mock(MimetypeService.class);
    circabcLockService = mock(LockService.class);

    setField("autoUploadConfigurationService", autoUploadConfigurationService);
    setField("ruleService", ruleService);
    setField("actionService", actionService);
    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("logService", logService);
    setField("notificationService", notificationService);
    setField("transactionService", transactionService);
    setField("fileFolderService", fileFolderService);
    setField("mimetypeService", mimetypeService);
    setField("circabcLockService", circabcLockService);
  }

  // --- getConfigurationByNodeRef ---

  @Test
  public void testGetConfigurationByNodeRef_whenFound_thenReturnsConfig()
    throws SQLException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    Configuration expected = new Configuration();
    when(
      autoUploadConfigurationService.getConfigurationByNodeRef(nodeRef)
    ).thenReturn(expected);

    Configuration result = service.getConfigurationByNodeRef(nodeRef);

    assertEquals(expected, result);
  }

  @Test
  public void testGetConfigurationByNodeRef_whenNotFound_thenReturnsNull()
    throws SQLException {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "missing"
    );
    when(
      autoUploadConfigurationService.getConfigurationByNodeRef(nodeRef)
    ).thenReturn(null);

    assertNull(service.getConfigurationByNodeRef(nodeRef));
  }

  // --- listAllConfigurations ---

  @Test
  public void testListAllConfigurations_whenCalled_thenDelegatesToGetAll()
    throws SQLException {
    List<Configuration> expected = new ArrayList<>();
    expected.add(new Configuration());
    when(autoUploadConfigurationService.getAllConfigurations()).thenReturn(
      expected
    );

    List<Configuration> result = service.listAllConfigurations();

    assertEquals(expected, result);
    verify(autoUploadConfigurationService).getAllConfigurations();
  }

  @Test
  public void testListAllConfigurations_whenEmpty_thenReturnsEmptyList()
    throws SQLException {
    when(autoUploadConfigurationService.getAllConfigurations()).thenReturn(
      new ArrayList<>()
    );

    List<Configuration> result = service.listAllConfigurations();

    assertTrue(result.isEmpty());
  }

  // --- logJobResult ---
  // Note: logJobResult uses nodeService.getPath() which returns Path (final class).
  // Path cannot be mocked without mockito-inline. Tested via integration tests instead.

  // --- createContent ---

  @SuppressWarnings("unchecked")
  @Test
  public void testCreateContent_whenFileDoesNotExist_thenCreatesNew() {
    NodeRef fileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "created-id"
    );
    File tmpFile = new File("/tmp/upload.pdf");

    RetryingTransactionHelper helper = mock(RetryingTransactionHelper.class);
    when(transactionService.getRetryingTransactionHelper()).thenReturn(helper);
    when(
      helper.doInTransaction(
        any(RetryingTransactionHelper.RetryingTransactionCallback.class),
        eq(false),
        eq(true)
      )
    ).thenAnswer(invocation -> {
      RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> callback =
        invocation.getArgument(0);
      return callback.execute();
    });

    when(
      nodeService.getChildByName(
        destFolder,
        ContentModel.ASSOC_CONTAINS,
        "upload.pdf"
      )
    ).thenReturn(null);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(createdRef);
    when(
      fileFolderService.create(
        destFolder,
        "upload.pdf",
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(fileInfo);

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(createdRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);
    when(mimetypeService.guessMimetype("upload.pdf")).thenReturn(
      "application/pdf"
    );

    NodeRef result = service.createContent(
      fileRef,
      tmpFile,
      destFolder,
      "upload.pdf"
    );

    assertEquals(createdRef, result);
    verify(nodeService).setProperty(
      createdRef,
      ContentModel.PROP_DESCRIPTION,
      "Document created by auto upload job"
    );
    verify(writer).setMimetype("application/pdf");
    verify(writer).putContent(tmpFile);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCreateContent_whenFileAlreadyExists_thenReturnsExisting() {
    NodeRef fileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef existingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    File tmpFile = new File("/tmp/upload.pdf");

    RetryingTransactionHelper helper = mock(RetryingTransactionHelper.class);
    when(transactionService.getRetryingTransactionHelper()).thenReturn(helper);
    when(
      helper.doInTransaction(
        any(RetryingTransactionHelper.RetryingTransactionCallback.class),
        eq(false),
        eq(true)
      )
    ).thenAnswer(invocation -> {
      RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> callback =
        invocation.getArgument(0);
      return callback.execute();
    });

    when(
      nodeService.getChildByName(
        destFolder,
        ContentModel.ASSOC_CONTAINS,
        "upload.pdf"
      )
    ).thenReturn(existingRef);

    NodeRef result = service.createContent(
      fileRef,
      tmpFile,
      destFolder,
      "upload.pdf"
    );

    assertEquals(existingRef, result);
    verify(fileFolderService, never()).create(
      any(NodeRef.class),
      anyString(),
      any()
    );
  }

  // --- listConfigurations with exception ---

  @Test(expected = RuntimeException.class)
  public void testListConfigurations_whenRuntimeException_thenPropagates()
    throws SQLException {
    when(autoUploadConfigurationService.listConfigurations("bad")).thenThrow(
      new RuntimeException("db error")
    );

    service.listConfigurations("bad");
  }

  // --- unlockJobFile edge case ---

  @Test
  public void testUnlockJobFile_whenExceptionThrown_thenPropagates() {
    when(circabcLockService.isLocked("autoupload9")).thenThrow(
      new RuntimeException("unlock error")
    );

    try {
      service.unlockJobFile(9L);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertEquals("unlock error", e.getMessage());
    }
  }
}
