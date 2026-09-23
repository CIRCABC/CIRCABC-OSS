package eu.europa.ec.digit.circabc.rest.service.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class TemporaryFileManagerTest {

  private TemporaryFileManager manager;
  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private ContentManager contentManager;
  private CircabcApi circabcApi;

  private static final NodeRef DICT_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "dict-id"
  );
  private static final NodeRef TEMP_ROOT_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "temp-root-id"
  );
  private static final NodeRef FILE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "file-id"
  );

  @Before
  public void setUp() throws Exception {
    manager = new TemporaryFileManager();
    nodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    contentManager = mock(ContentManager.class);
    circabcApi = mock(CircabcApi.class);

    setField("nodeService", nodeService);
    setField("fileFolderService", fileFolderService);
    setField("contentManager", contentManager);
    setField("circabcApi", circabcApi);

    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(DICT_REF);
    when(
      nodeService.getChildByName(DICT_REF, ContentModel.ASSOC_CONTAINS, "temp")
    ).thenReturn(TEMP_ROOT_REF);
  }

  @Test
  public void testCreateTempFile_whenFileProvided_thenCreatesContent() {
    File file = new File("test.txt");
    when(
      contentManager.createContent(
        TEMP_ROOT_REF,
        "test.txt",
        ContentModel.ASSOC_CONTAINS,
        ContentModel.TYPE_CONTENT,
        file,
        true
      )
    ).thenReturn(FILE_REF);

    NodeRef result = manager.createTempFile(file, "test.txt");

    assertEquals(FILE_REF, result);
  }

  @Test
  public void testIsTempFile_whenNodeIsChildOfTempRoot_thenReturnsTrue() {
    when(nodeService.exists(FILE_REF)).thenReturn(true);
    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getParentRef()).thenReturn(TEMP_ROOT_REF);
    when(nodeService.getPrimaryParent(FILE_REF)).thenReturn(assocRef);

    assertTrue(manager.isTempFile(FILE_REF));
  }

  @Test
  public void testIsTempFile_whenNodeIsNull_thenReturnsFalse() {
    assertFalse(manager.isTempFile(null));
  }

  @Test
  public void testIsTempFile_whenNodeDoesNotExist_thenReturnsFalse() {
    when(nodeService.exists(FILE_REF)).thenReturn(false);
    assertFalse(manager.isTempFile(FILE_REF));
  }

  @Test
  public void testRemoveTempFile_whenIsTempFile_thenDeletesNode() {
    when(nodeService.exists(FILE_REF)).thenReturn(true);
    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getParentRef()).thenReturn(TEMP_ROOT_REF);
    when(nodeService.getPrimaryParent(FILE_REF)).thenReturn(assocRef);

    manager.removeTempFile(FILE_REF);

    verify(nodeService).addAspect(
      FILE_REF,
      ContentModel.ASPECT_TEMPORARY,
      null
    );
    verify(nodeService).deleteNode(FILE_REF);
  }

  @Test
  public void testRemoveTempFile_whenNotTempFile_thenDoesNotDelete() {
    when(nodeService.exists(FILE_REF)).thenReturn(true);
    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    NodeRef otherParent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other"
    );
    when(assocRef.getParentRef()).thenReturn(otherParent);
    when(nodeService.getPrimaryParent(FILE_REF)).thenReturn(assocRef);

    manager.removeTempFile(FILE_REF);

    verify(nodeService, never()).deleteNode(FILE_REF);
  }

  @Test
  public void testRemoveTempFiles_whenFileIsOld_thenRemovesIt() {
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(FILE_REF);
    when(nodeService.getChildAssocs(TEMP_ROOT_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );

    // Modified long ago
    Date oldDate = new Date(
      System.currentTimeMillis() - 1000L * 60L * 60L * 48L
    );
    when(
      nodeService.getProperty(FILE_REF, ContentModel.PROP_MODIFIED)
    ).thenReturn(oldDate);

    // isTempFile setup
    when(nodeService.exists(FILE_REF)).thenReturn(true);
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(TEMP_ROOT_REF);
    when(nodeService.getPrimaryParent(FILE_REF)).thenReturn(parentAssoc);

    manager.removeTempFiles();

    verify(nodeService).deleteNode(FILE_REF);
  }

  @Test
  public void testRemoveTempFiles_whenFileIsRecent_thenKeepsIt() {
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(FILE_REF);
    when(nodeService.getChildAssocs(TEMP_ROOT_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );

    // Modified just now
    Date recentDate = new Date();
    when(
      nodeService.getProperty(FILE_REF, ContentModel.PROP_MODIFIED)
    ).thenReturn(recentDate);

    manager.removeTempFiles();

    verify(nodeService, never()).deleteNode(any());
  }

  @Test
  public void testGetTempRoot_whenExists_thenReturnsRef() {
    assertEquals(TEMP_ROOT_REF, manager.getTempRoot());
  }

  @Test
  public void testGetTempRoot_whenMissing_thenCreatesFolder() throws Exception {
    // Reset tempRootRef cache
    setField("tempRootRef", null);
    when(
      nodeService.getChildByName(DICT_REF, ContentModel.ASSOC_CONTAINS, "temp")
    ).thenReturn(null);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(TEMP_ROOT_REF);
    when(
      fileFolderService.create(DICT_REF, "temp", ContentModel.TYPE_FOLDER)
    ).thenReturn(fileInfo);

    NodeRef result = manager.getTempRoot();

    assertEquals(TEMP_ROOT_REF, result);
    verify(fileFolderService).create(
      DICT_REF,
      "temp",
      ContentModel.TYPE_FOLDER
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = TemporaryFileManager.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(manager, value);
  }
}
