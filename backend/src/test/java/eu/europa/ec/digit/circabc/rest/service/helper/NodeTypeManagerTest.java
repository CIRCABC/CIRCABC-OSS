package eu.europa.ec.digit.circabc.rest.service.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NodeTypeManagerTest {

  private NodeTypeManager nodeTypeManager;
  private NodeService nodeService;
  private DictionaryService dictionaryService;
  private LockService lockService;

  private NodeRef testNodeRef;

  @Before
  public void setUp() throws Exception {
    nodeTypeManager = new NodeTypeManager();
    nodeService = mock(NodeService.class);
    dictionaryService = mock(DictionaryService.class);
    lockService = mock(LockService.class);

    setField("nodeService", nodeService);
    setField("dictionaryService", dictionaryService);
    setField("lockService", lockService);

    testNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodeTypeManager.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodeTypeManager, value);
  }

  // --- isContainer ---

  @Test
  public void testIsContainer_whenTypeIsContainer_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTAINER
    );
    assertTrue(nodeTypeManager.isContainer(testNodeRef));
  }

  @Test
  public void testIsContainer_whenSubtypeOfContainer_thenTrue() {
    QName subType = QName.createQName("http://test", "subContainer");
    when(nodeService.getType(testNodeRef)).thenReturn(subType);
    when(
      dictionaryService.isSubClass(subType, ContentModel.TYPE_CONTAINER)
    ).thenReturn(true);
    assertTrue(nodeTypeManager.isContainer(testNodeRef));
  }

  @Test
  public void testIsContainer_whenNull_thenFalse() {
    assertFalse(nodeTypeManager.isContainer(null));
  }

  // --- isContent ---

  @Test
  public void testIsContent_whenTypeIsContent_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    assertTrue(nodeTypeManager.isContent(testNodeRef));
  }

  @Test
  public void testIsContent_whenDifferentType_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_FOLDER,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(false);
    assertFalse(nodeTypeManager.isContent(testNodeRef));
  }

  // --- isFolder ---

  @Test
  public void testIsFolder_whenTypeIsFolder_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    assertTrue(nodeTypeManager.isFolder(testNodeRef));
  }

  @Test
  public void testIsFolder_whenTypeIsContent_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    assertFalse(nodeTypeManager.isFolder(testNodeRef));
  }

  // --- isDocument ---

  @Test
  public void testIsDocument_whenContentWithoutUrlAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(nodeService.getAspects(testNodeRef)).thenReturn(new HashSet<>());
    assertTrue(nodeTypeManager.isDocument(testNodeRef));
  }

  @Test
  public void testIsDocument_whenContentWithUrlAspect_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    Set<QName> aspects = new HashSet<>();
    aspects.add(DocumentModel.ASPECT_URLABLE);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertFalse(nodeTypeManager.isDocument(testNodeRef));
  }

  // --- isUrl ---

  @Test
  public void testIsUrl_whenContentWithUrlAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    Set<QName> aspects = new HashSet<>();
    aspects.add(DocumentModel.ASPECT_URLABLE);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertTrue(nodeTypeManager.isUrl(testNodeRef));
  }

  @Test
  public void testIsUrl_whenContentWithoutUrlAspect_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(nodeService.getAspects(testNodeRef)).thenReturn(new HashSet<>());
    assertFalse(nodeTypeManager.isUrl(testNodeRef));
  }

  // --- isLockedDocument ---

  @Test
  public void testIsLockedDocument_whenDocumentIsLocked_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    Set<QName> aspects = new HashSet<>();
    aspects.add(ContentModel.ASPECT_LOCKABLE);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    when(lockService.getLockStatus(testNodeRef)).thenReturn(LockStatus.LOCKED);
    assertTrue(nodeTypeManager.isLockedDocument(testNodeRef));
  }

  @Test
  public void testIsLockedDocument_whenDocumentNotLocked_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(nodeService.getAspects(testNodeRef)).thenReturn(new HashSet<>());
    assertFalse(nodeTypeManager.isLockedDocument(testNodeRef));
  }

  // --- isInterestGroup ---

  @Test
  public void testIsInterestGroup_whenFolderWithIgRootAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_IGROOT);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertTrue(nodeTypeManager.isInterestGroup(testNodeRef));
  }

  @Test
  public void testIsInterestGroup_whenFolderWithoutAspect_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.getAspects(testNodeRef)).thenReturn(new HashSet<>());
    assertFalse(nodeTypeManager.isInterestGroup(testNodeRef));
  }

  // --- isCategory ---

  @Test
  public void testIsCategory_whenFolderWithCategoryAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_CATEGORY);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertTrue(nodeTypeManager.isCategory(testNodeRef));
  }

  // --- isPost ---

  @Test
  public void testIsPost_whenTypeIsPost_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ForumModel.TYPE_POST);
    assertTrue(nodeTypeManager.isPost(testNodeRef));
  }

  @Test
  public void testIsPost_whenTypeIsNotPost_thenFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    assertFalse(nodeTypeManager.isPost(testNodeRef));
  }

  // --- isTopic ---

  @Test
  public void testIsTopic_whenTypeIsTopic_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ForumModel.TYPE_TOPIC);
    assertTrue(nodeTypeManager.isTopic(testNodeRef));
  }

  // --- isLibraryRoot ---

  @Test
  public void testIsLibraryRoot_whenFolderWithLibraryRootAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_LIBRARY_ROOT);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertTrue(nodeTypeManager.isLibraryRoot(testNodeRef));
  }

  // --- isInterestGroupService ---

  @Test
  public void testIsInterestGroupService_whenHasServiceRootAspect_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_LIBRARY_ROOT);
    when(nodeService.getAspects(testNodeRef)).thenReturn(aspects);
    assertTrue(nodeTypeManager.isInterestGroupService(testNodeRef));
  }

  @Test
  public void testIsInterestGroupService_whenDirectoryRoot_thenTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      CircabcModel.TYPE_DIRECTORY_SERVICE
    );
    when(nodeService.getAspects(testNodeRef)).thenReturn(new HashSet<>());
    assertTrue(nodeTypeManager.isInterestGroupService(testNodeRef));
  }

  // --- null handling ---

  @Test
  public void testIsFolder_whenNull_thenFalse() {
    assertFalse(nodeTypeManager.isFolder(null));
  }

  @Test
  public void testIsDocument_whenNull_thenFalse() {
    assertFalse(nodeTypeManager.isDocument(null));
  }

  @Test
  public void testIsInterestGroupChild_whenNull_thenFalse() {
    assertFalse(nodeTypeManager.isInterestGroupChild(null));
  }
}
