package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.DBLogServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import io.swagger.model.Configuration;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class AutoUploadManagementServiceImplTest {

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

  // --- registerConfiguration ---

  @Test
  public void testRegisterConfiguration_whenCalled_thenDelegates()
    throws SQLException {
    Configuration config = new Configuration();
    service.registerConfiguration(config);
    verify(autoUploadConfigurationService).registerConfiguration(config);
  }

  // --- listConfigurations ---

  @Test
  public void testListConfigurations_whenCalled_thenReturnsResult()
    throws SQLException {
    List<Configuration> expected = new ArrayList<>();
    expected.add(new Configuration());
    when(autoUploadConfigurationService.listConfigurations("ig1")).thenReturn(
      expected
    );

    List<Configuration> result = service.listConfigurations("ig1");
    assertEquals(expected, result);
  }

  // --- deleteConfiguration ---

  @Test
  public void testDeleteConfiguration_whenCalled_thenDelegates()
    throws SQLException {
    Configuration config = new Configuration();
    service.deleteConfiguration(config);
    verify(autoUploadConfigurationService).deleteConfiguration(config);
  }

  // --- updateConfiguration ---

  @Test
  public void testUpdateConfiguration_whenCalled_thenDelegates()
    throws SQLException {
    Configuration config = new Configuration();
    service.updateConfiguration(config);
    verify(autoUploadConfigurationService).updateConfiguration(config);
  }

  // --- getConfigurationById ---

  @Test
  public void testGetConfigurationById_whenCalled_thenReturnsResult()
    throws SQLException {
    Configuration expected = new Configuration();
    when(autoUploadConfigurationService.getConfigurationById(42)).thenReturn(
      expected
    );

    Configuration result = service.getConfigurationById(42);
    assertEquals(expected, result);
  }

  // --- buildDefaultExtractRule ---

  @Test
  public void testBuildDefaultExtractRule_whenCalled_thenReturnsConfiguredRule() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    CompositeAction compositeAction = mock(CompositeAction.class);
    when(actionService.createCompositeAction()).thenReturn(compositeAction);

    ActionCondition condition = mock(ActionCondition.class);
    when(actionService.createActionCondition(anyString(), anyMap())).thenReturn(
      condition
    );

    Action action = mock(Action.class);
    when(actionService.createAction(anyString())).thenReturn(action);

    Rule rule = service.buildDefaultExtractRule(spaceRef);

    assertNotNull(rule);
    assertEquals("CIRCABCRuleAutoExtract", rule.getTitle());
    assertFalse(rule.isAppliedToChildren());
    assertTrue(rule.getExecuteAsynchronously());
    assertFalse(rule.getRuleDisabled());

    verify(compositeAction).addActionCondition(condition);
    verify(compositeAction).addAction(action);
  }

  // --- addAutoExtractRuleToSpace ---

  @Test
  public void testAddAutoExtractRuleToSpace_whenCalled_thenSavesRule() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    CompositeAction compositeAction = mock(CompositeAction.class);
    when(actionService.createCompositeAction()).thenReturn(compositeAction);
    when(actionService.createActionCondition(anyString(), anyMap())).thenReturn(
      mock(ActionCondition.class)
    );
    when(actionService.createAction(anyString())).thenReturn(
      mock(Action.class)
    );

    service.addAutoExtractRuleToSpace(spaceRef);

    verify(ruleService).saveRule(eq(spaceRef), any(Rule.class));
  }

  // --- removeAutoExtractRule ---

  @Test
  public void testRemoveAutoExtractRule_whenRuleExists_thenRemovesIt() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    Rule matchingRule = mock(Rule.class);
    when(matchingRule.getTitle()).thenReturn("CIRCABCRuleAutoExtract");

    Rule otherRule = mock(Rule.class);
    when(otherRule.getTitle()).thenReturn("SomeOtherRule");

    List<Rule> rules = new ArrayList<>();
    rules.add(otherRule);
    rules.add(matchingRule);
    when(ruleService.getRules(spaceRef)).thenReturn(rules);

    service.removeAutoExtractRule(spaceRef);

    verify(ruleService).removeRule(spaceRef, matchingRule);
    verify(ruleService, never()).removeRule(spaceRef, otherRule);
  }

  @Test
  public void testRemoveAutoExtractRule_whenNoMatchingRule_thenDoesNothing() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    Rule otherRule = mock(Rule.class);
    when(otherRule.getTitle()).thenReturn("SomeOtherRule");
    when(ruleService.getRules(spaceRef)).thenReturn(
      Collections.singletonList(otherRule)
    );

    service.removeAutoExtractRule(spaceRef);

    verify(ruleService, never()).removeRule(
      any(NodeRef.class),
      any(Rule.class)
    );
  }

  // --- updateContent ---

  @Test
  public void testUpdateContent_whenCalled_thenWritesContent() {
    NodeRef fileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    File file = new File("/tmp/test.txt");

    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(fileRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    service.updateContent(fileRef, file);

    verify(writer).putContent(file);
  }

  // --- lockJobFile ---

  @Test
  public void testLockJobFile_whenNotLocked_thenLocksAndReturns1() {
    when(circabcLockService.isLocked("autoupload5")).thenReturn(false);

    Integer result = service.lockJobFile(5L);

    assertEquals(Integer.valueOf(1), result);
    verify(circabcLockService).lock("autoupload5");
  }

  @Test
  public void testLockJobFile_whenAlreadyLocked_thenReturns0() {
    when(circabcLockService.isLocked("autoupload5")).thenReturn(true);

    Integer result = service.lockJobFile(5L);

    assertEquals(Integer.valueOf(0), result);
    verify(circabcLockService, never()).lock(anyString());
  }

  @Test
  public void testLockJobFile_whenExceptionThrown_thenReturns0() {
    when(circabcLockService.isLocked("autoupload7")).thenThrow(
      new RuntimeException("lock error")
    );

    Integer result = service.lockJobFile(7L);

    assertEquals(Integer.valueOf(0), result);
  }

  // --- unlockJobFile ---

  @Test
  public void testUnlockJobFile_whenLocked_thenUnlocksAndReturns1() {
    when(circabcLockService.isLocked("autoupload5")).thenReturn(true);

    Integer result = service.unlockJobFile(5L);

    assertEquals(Integer.valueOf(1), result);
    verify(circabcLockService).unlock("autoupload5");
  }

  @Test
  public void testUnlockJobFile_whenNotLocked_thenReturns0() {
    when(circabcLockService.isLocked("autoupload5")).thenReturn(false);

    Integer result = service.unlockJobFile(5L);

    assertEquals(Integer.valueOf(0), result);
    verify(circabcLockService, never()).unlock(anyString());
  }

  // --- documentExists ---

  @Test
  public void testDocumentExists_whenExists_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);

    assertTrue(service.documentExists(nodeRef));
  }

  @Test
  public void testDocumentExists_whenNotExists_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);

    assertFalse(service.documentExists(nodeRef));
  }

  // --- sendJobNotification ---

  @Test
  public void testSendJobNotification_whenJobOk_thenSendsSuccessTemplate()
    throws Exception {
    Configuration conf = new Configuration();
    conf.setFileNodeRef("workspace://SpacesStore/file-id");
    conf.setEmails("a@b.com,c@d.com");

    service.sendJobNofitication(conf, AutoUploadJobResult.JOB_OK);

    verify(notificationService).notify(
      eq(new NodeRef("workspace://SpacesStore/file-id")),
      anyList(),
      eq(MailTemplate.AUTO_UPLOAD_SUCCESS)
    );
  }

  @Test
  public void testSendJobNotification_whenJobError_thenSendsErrorTemplate()
    throws Exception {
    Configuration conf = new Configuration();
    conf.setFileNodeRef("workspace://SpacesStore/file-id");
    conf.setEmails("a@b.com");

    service.sendJobNofitication(conf, AutoUploadJobResult.JOB_ERROR);

    verify(notificationService).notify(
      eq(new NodeRef("workspace://SpacesStore/file-id")),
      anyList(),
      eq(MailTemplate.AUTO_UPLOAD_ERROR)
    );
  }

  @Test
  public void testSendJobNotification_whenFtpProblem_thenSendsFtpTemplate()
    throws Exception {
    Configuration conf = new Configuration();
    conf.setFileNodeRef("workspace://SpacesStore/file-id");
    conf.setEmails("a@b.com");

    service.sendJobNofitication(
      conf,
      AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM
    );

    verify(notificationService).notify(
      eq(new NodeRef("workspace://SpacesStore/file-id")),
      anyList(),
      eq(MailTemplate.AUTO_UPLOAD_FTP_PROBLEM)
    );
  }

  @Test
  public void testSendJobNotification_whenNullEmails_thenSendsEmptyList()
    throws Exception {
    Configuration conf = new Configuration();
    conf.setFileNodeRef("workspace://SpacesStore/file-id");
    conf.setEmails(null);

    service.sendJobNofitication(conf, AutoUploadJobResult.JOB_OK);

    verify(notificationService).notify(
      eq(new NodeRef("workspace://SpacesStore/file-id")),
      eq(new ArrayList<>()),
      eq(MailTemplate.AUTO_UPLOAD_SUCCESS)
    );
  }

  @Test
  public void testSendJobNotification_whenExceptionThrown_thenDoesNotPropagate()
    throws Exception {
    Configuration conf = new Configuration();
    conf.setFileNodeRef("workspace://SpacesStore/file-id");
    conf.setEmails("a@b.com");

    doThrow(new RuntimeException("mail error"))
      .when(notificationService)
      .notify(any(NodeRef.class), anyList(), any(MailTemplate.class));

    // Should not throw
    service.sendJobNofitication(conf, AutoUploadJobResult.JOB_OK);
  }

  // --- extractZip ---

  @Test
  public void testExtractZip_whenCalled_thenExecutesAction() {
    NodeRef fileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "zip-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getParentAssocs(fileRef)).thenReturn(
      Collections.singletonList(assocRef)
    );

    Action action = mock(Action.class);
    when(actionService.createAction(anyString(), anyMap())).thenReturn(action);

    service.extractZip(fileRef);

    verify(action).setExecuteAsynchronously(true);
    verify(actionService).executeAction(action, fileRef);
  }
}
