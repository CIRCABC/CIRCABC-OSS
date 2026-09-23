package eu.europa.ec.digit.circabc.rest.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.bulk.BulkService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterService;
import org.junit.Before;
import org.junit.Test;

public class CircabcImporterActionExecuterTest {

  private CircabcImporterActionExecuter executer;
  private NodeService nodeService;
  private ContentService contentService;
  private MailService mailService;
  private PersonService personService;
  private LogService logService;
  private ImporterService importerService;
  private FileFolderService fileFolderService;
  private MimetypeService mimetypeService;
  private BulkService bulkService;
  private BehaviourFilter policyBehaviourFilter;
  private ApiToolBox apiToolBox;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
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

    executer = new CircabcImporterActionExecuter();

    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    mailService = mock(MailService.class);
    personService = mock(PersonService.class);
    logService = mock(LogService.class);
    importerService = mock(ImporterService.class);
    fileFolderService = mock(FileFolderService.class);
    mimetypeService = mock(MimetypeService.class);
    bulkService = mock(BulkService.class);
    policyBehaviourFilter = mock(BehaviourFilter.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("myNodeService", nodeService);
    setField("myContentService", contentService);
    setField("mailService", mailService);
    setField("personService", personService);
    setField("logService", logService);
    setField("myImporterService", importerService);
    setField("myFileFolderService", fileFolderService);
    setField("myMimetypeService", mimetypeService);
    setField("bulkService", bulkService);
    setField("policyBehaviourFilter", policyBehaviourFilter);
    setField("apiToolBox", apiToolBox);

    // Parent class fields
    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("fileFolderService", fileFolderService);
    setField("importerService", importerService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = findField(CircabcImporterActionExecuter.class, fieldName);
    field.setAccessible(true);
    field.set(executer, value);
  }

  private Field findField(Class<?> clazz, String fieldName) {
    while (clazz != null) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new RuntimeException("Field not found: " + fieldName);
  }

  @Test
  public void testExecuteImpl_whenFileTooLarge_thenThrowsIllegalState() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.FALSE);

    // 21 MB file exceeds default 20 MB limit
    ContentData contentData = new ContentData(
      "content://url",
      "application/zip",
      21 * 1024 * 1024L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);

    try {
      executer.executeImpl(action, nodeRef);
      fail("Expected AlfrescoRuntimeException");
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
      assertTrue(e.getCause().getMessage().contains("File is too big"));
    }

    verify(logService).log(any());
  }

  @Test
  public void testExecuteImpl_whenDisableNotification_thenDisablesBehaviour() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.TRUE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_DELETE_FILE)
    ).thenReturn(Boolean.FALSE);

    // Small file
    ContentData contentData = new ContentData(
      "content://url",
      "application/zip",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(
      nodeService.getProperty(destRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(apiToolBox.getCurrentInterestGroup(destRef)).thenReturn(igRef);
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(2L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "test.zip"
    );
    when(nodeService.getPath(destRef)).thenReturn(new Path());
    when(nodeService.exists(nodeRef)).thenReturn(true);

    // Return null reader so import does nothing after validation
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);

    executer.executeImpl(action, nodeRef);

    verify(policyBehaviourFilter).disableBehaviour(
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
  }

  @Test
  public void testExecuteImpl_whenEnableNotification_thenEnablesBehaviour() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_DELETE_FILE)
    ).thenReturn(Boolean.FALSE);

    ContentData contentData = new ContentData(
      "content://url",
      "application/zip",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(
      nodeService.getProperty(destRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(apiToolBox.getCurrentInterestGroup(destRef)).thenReturn(igRef);
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(2L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "test.zip"
    );
    when(nodeService.getPath(destRef)).thenReturn(new Path());
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);

    executer.executeImpl(action, nodeRef);

    verify(policyBehaviourFilter).enableBehaviour(
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
  }

  @Test
  public void testExecuteImpl_whenDeleteFileTrue_thenDeletesNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_DELETE_FILE)
    ).thenReturn(Boolean.TRUE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_NOTIFY_USER)
    ).thenReturn(null);

    ContentData contentData = new ContentData(
      "content://url",
      "application/zip",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(
      nodeService.getProperty(destRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(apiToolBox.getCurrentInterestGroup(destRef)).thenReturn(igRef);
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(2L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "test.zip"
    );
    when(nodeService.getPath(destRef)).thenReturn(new Path());
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);

    executer.executeImpl(action, nodeRef);

    verify(nodeService).deleteNode(nodeRef);
  }

  @Test
  public void testExecuteImpl_whenNotifyUser_thenSendsMail() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_DELETE_FILE)
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_NOTIFY_USER)
    ).thenReturn("someuser");

    ContentData contentData = new ContentData(
      "content://url",
      "application/zip",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(
      nodeService.getProperty(destRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(apiToolBox.getCurrentInterestGroup(destRef)).thenReturn(igRef);
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(2L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "test.zip"
    );
    when(nodeService.getPath(destRef)).thenReturn(new Path());
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);

    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");
    when(personService.getPerson("someuser")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("someuser@test.com");

    executer.executeImpl(action, nodeRef);

    verify(personService).getPerson("someuser");
    verify(mailService).getNoReplyEmailAddress();
  }

  @Test
  public void testExecuteImpl_whenNullContent_thenFileSizeIsZero() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "file-id"
    );
    NodeRef destRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dest-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    Action action = mock(Action.class);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DESTINATION_FOLDER
      )
    ).thenReturn(destRef);
    when(
      action.getParameterValue(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION
      )
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_DELETE_FILE)
    ).thenReturn(Boolean.FALSE);
    when(
      action.getParameterValue(CircabcImporterActionExecuter.PARAM_NOTIFY_USER)
    ).thenReturn(null);

    // Null content property -> size should be 0, passes validation
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);
    when(
      nodeService.getProperty(destRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);
    when(apiToolBox.getCurrentInterestGroup(destRef)).thenReturn(igRef);
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(2L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "test.zip"
    );
    when(nodeService.getPath(destRef)).thenReturn(new Path());
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(null);

    // Should not throw - null content means size 0 which is under limit
    executer.executeImpl(action, nodeRef);

    verify(logService).log(any());
  }
}
