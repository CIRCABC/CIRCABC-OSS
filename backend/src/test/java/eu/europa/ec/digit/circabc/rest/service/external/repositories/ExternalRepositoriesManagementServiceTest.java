package eu.europa.ec.digit.circabc.rest.service.external.repositories;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.RepositoryConfiguration;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class ExternalRepositoriesManagementServiceTest {

  private ExternalRepositoriesManagementServiceImpl service;
  private NodeService nodeService;
  private TransactionService transactionService;
  private RetryingTransactionHelper txnHelper;

  private static final String NODE_ID = "workspace://SpacesStore/test-node-id";
  private static final String PARENT_NODE_ID =
    "workspace://SpacesStore/parent-id";

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

    service = new ExternalRepositoriesManagementServiceImpl();
    nodeService = mock(NodeService.class);
    transactionService = mock(TransactionService.class);
    txnHelper = mock(RetryingTransactionHelper.class);

    setField("nodeService", nodeService);
    setField("transactionService", transactionService);

    when(transactionService.getRetryingTransactionHelper()).thenReturn(
      txnHelper
    );
    when(
      txnHelper.doInTransaction(any(), anyBoolean(), anyBoolean())
    ).thenReturn(null);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field =
      ExternalRepositoriesManagementServiceImpl.class.getDeclaredField(
        fieldName
      );
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testWasPublishedTo_whenNoAspect_thenReturnsFalse() {
    when(
      nodeService.hasAspect(
        new NodeRef(NODE_ID),
        CircabcModel.ASPECT_EXTERNALLY_PUBLISHED
      )
    ).thenReturn(false);

    assertFalse(service.wasPublishedTo("Hermes", NODE_ID));
  }

  @Test
  public void testWasPublishedTo_whenAspectExistsAndRepoNameNull_thenReturnsTrue() {
    when(
      nodeService.hasAspect(
        new NodeRef(NODE_ID),
        CircabcModel.ASPECT_EXTERNALLY_PUBLISHED
      )
    ).thenReturn(true);

    assertTrue(service.wasPublishedTo(null, NODE_ID));
  }

  @Test
  public void testWasPublishedTo_whenAspectExistsAndRepoPresent_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(NODE_ID);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ).thenReturn(true);

    HashMap<String, HashMap<String, String>> repoInfo = new HashMap<>();
    repoInfo.put("Hermes", new HashMap<>());
    when(
      nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO)
    ).thenReturn(repoInfo);

    assertTrue(service.wasPublishedTo("Hermes", NODE_ID));
  }

  @Test
  public void testWasPublishedTo_whenAspectExistsAndRepoAbsent_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(NODE_ID);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ).thenReturn(true);

    HashMap<String, HashMap<String, String>> repoInfo = new HashMap<>();
    when(
      nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO)
    ).thenReturn(repoInfo);

    assertFalse(service.wasPublishedTo("Hermes", NODE_ID));
  }

  @Test
  public void testSaveExternalMetadata_whenNoExistingAspect_thenAddsAspectAndSavesData() {
    NodeRef nodeRef = new NodeRef(NODE_ID);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ).thenReturn(false);

    service.saveExternalMetadata(
      "Hermes",
      NODE_ID,
      "doc1",
      "save1",
      "reg1",
      "request1",
      "txn1"
    );

    verify(nodeService).addAspect(
      eq(nodeRef),
      eq(CircabcModel.ASPECT_EXTERNALLY_PUBLISHED),
      anyMap()
    );
    verify(nodeService).setProperty(
      eq(nodeRef),
      eq(CircabcModel.PROP_REPOSITORIES_INFO),
      any()
    );
  }

  @Test
  public void testSaveExternalMetadata_whenExistingAspect_thenUpdatesData() {
    NodeRef nodeRef = new NodeRef(NODE_ID);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ).thenReturn(true);

    HashMap<String, HashMap<String, String>> existingInfo = new HashMap<>();
    when(
      nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO)
    ).thenReturn(existingInfo);

    service.saveExternalMetadata(
      "Hermes",
      NODE_ID,
      "doc1",
      null,
      null,
      null,
      null
    );

    verify(nodeService, never()).addAspect(any(NodeRef.class), any(), anyMap());
    verify(nodeService).setProperty(
      eq(nodeRef),
      eq(CircabcModel.PROP_REPOSITORIES_INFO),
      any()
    );
  }

  @Test
  public void testGetConfiguredRepositories_whenNoFolder_thenReturnsEmpty() {
    NodeRef parentNodeRef = new NodeRef(PARENT_NODE_ID);
    when(
      nodeService.getChildByName(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "ExternalRepositoryConfigurations"
      )
    ).thenReturn(null);

    Collection<RepositoryConfiguration> result =
      service.getConfiguredRepositories(PARENT_NODE_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetConfiguredRepositories_whenFolderWithChildren_thenReturnsConfigurations() {
    NodeRef parentNodeRef = new NodeRef(PARENT_NODE_ID);
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    when(
      nodeService.getChildByName(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "ExternalRepositoryConfigurations"
      )
    ).thenReturn(folderRef);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(nodeService.getChildAssocs(folderRef)).thenReturn(
      Collections.singletonList(childAssoc)
    );

    when(nodeService.getProperty(childRef, ContentModel.PROP_NAME)).thenReturn(
      "Hermes"
    );
    Date created = new Date();
    when(
      nodeService.getProperty(childRef, ContentModel.PROP_CREATED)
    ).thenReturn(created);

    Collection<RepositoryConfiguration> result =
      service.getConfiguredRepositories(PARENT_NODE_ID);

    assertEquals(1, result.size());
    RepositoryConfiguration config = result.iterator().next();
    assertEquals("Hermes", config.getName());
    assertEquals(created, config.getRegistrationDate());
  }

  @Test
  public void testRemoveRepository_whenNoFolder_thenDoesNothing() {
    NodeRef parentNodeRef = new NodeRef(PARENT_NODE_ID);
    when(
      nodeService.getChildByName(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "ExternalRepositoryConfigurations"
      )
    ).thenReturn(null);

    service.removeRepository(PARENT_NODE_ID, "Hermes");

    verify(transactionService, never()).getRetryingTransactionHelper();
  }

  @Test
  public void testRemoveRepository_whenRepositoryNotFound_thenDoesNothing() {
    NodeRef parentNodeRef = new NodeRef(PARENT_NODE_ID);
    NodeRef folderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );

    when(
      nodeService.getChildByName(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        "ExternalRepositoryConfigurations"
      )
    ).thenReturn(folderRef);
    when(
      nodeService.getChildByName(
        folderRef,
        CircabcModel.ASSOC_CONTAINSCON_FIGURATIONS,
        "Hermes"
      )
    ).thenReturn(null);

    service.removeRepository(PARENT_NODE_ID, "Hermes");

    verify(transactionService, never()).getRetryingTransactionHelper();
  }
}
