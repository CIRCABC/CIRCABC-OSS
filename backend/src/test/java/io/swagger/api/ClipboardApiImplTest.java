package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationManagerService;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ClipboardApiImplTest {

  private ClipboardApiImpl clipboardApi;

  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private CopyService copyService;
  private DictionaryService dictionaryService;
  private MultilingualContentService multilingualContentService;
  private SearchService searchService;
  private NamespaceService namespaceService;
  private NotificationManagerService notificationManagerService;
  private ApiToolBox apiToolBox;

  private NodeRef sourceNode;
  private NodeRef destNode;
  private NodeRef parentNode;
  private NodeRef igNode;
  private ChildAssociationRef primaryAssoc;

  @Before
  public void setUp() throws Exception {
    clipboardApi = new ClipboardApiImpl();

    nodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    copyService = mock(CopyService.class);
    dictionaryService = mock(DictionaryService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    searchService = mock(SearchService.class);
    namespaceService = mock(NamespaceService.class);
    notificationManagerService = mock(NotificationManagerService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("fileFolderService", fileFolderService);
    setField("copyService", copyService);
    setField("dictionaryService", dictionaryService);
    setField("multilingualContentService", multilingualContentService);
    setField("searchService", searchService);
    setField("namespaceService", namespaceService);
    setField("notificationManagerService", notificationManagerService);
    setField("apiToolBox", apiToolBox);

    sourceNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "source-id"
    );
    destNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dest-id");
    parentNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    igNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    QName assocQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      "source"
    );
    primaryAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentNode,
      assocQName,
      sourceNode,
      true,
      -1
    );

    when(nodeService.getPrimaryParent(sourceNode)).thenReturn(primaryAssoc);
    when(nodeService.getParentAssocs(sourceNode)).thenReturn(
      List.of(primaryAssoc)
    );
    when(nodeService.exists(sourceNode)).thenReturn(true);
    when(
      nodeService.getProperty(sourceNode, ContentModel.PROP_NAME)
    ).thenReturn("testFile.txt");
    when(nodeService.getType(sourceNode)).thenReturn(ContentModel.TYPE_CONTENT);
    when(apiToolBox.getCurrentInterestGroup(sourceNode)).thenReturn(igNode);
    when(
      notificationManagerService.isPasteNotificationEnabled(igNode)
    ).thenReturn(false);
    when(
      notificationManagerService.isPasteAllNotificationEnabled(igNode)
    ).thenReturn(false);
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_CONTENT
      )
    ).thenReturn(true);
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_FOLDER
      )
    ).thenReturn(false);
    when(
      dictionaryService.isSubClass(
        ContentModel.TYPE_CONTENT,
        ContentModel.TYPE_MULTILINGUAL_CONTAINER
      )
    ).thenReturn(false);
    when(fileFolderService.list(destNode)).thenReturn(Collections.emptyList());
  }

  @Test
  public void testPaste_whenCopyContent_thenFileFolderServiceCopyIsCalled()
    throws FileNotFoundException {
    clipboardApi.paste(sourceNode, destNode, ClipboardAction.COPY.getValue());

    verify(fileFolderService).copy(sourceNode, destNode, "testFile.txt");
  }

  @Test
  public void testPaste_whenCopyAndFileExists_thenRetriesWithCopyPrefix()
    throws FileNotFoundException {
    FileInfo existingFile = mock(FileInfo.class);
    when(existingFile.getName()).thenReturn("testFile.txt");
    when(fileFolderService.list(destNode))
      .thenReturn(List.of(existingFile))
      .thenReturn(Collections.emptyList());

    clipboardApi.paste(sourceNode, destNode, ClipboardAction.COPY.getValue());

    verify(fileFolderService).copy(sourceNode, destNode, "Copy_testFile.txt");
  }

  @Test
  public void testPaste_whenMoveToSameParent_thenNoMovePerformed()
    throws FileNotFoundException {
    // destNode is the same as the parent in primaryAssoc
    clipboardApi.paste(sourceNode, parentNode, ClipboardAction.MOVE.getValue());

    verify(fileFolderService, never()).moveFrom(any(), any(), any(), any());
    verify(nodeService, never()).moveNode(any(), any(), any(), any());
  }

  @Test
  public void testPaste_whenMoveToDifferentFolder_thenMoveFromIsCalled()
    throws FileNotFoundException {
    NodeRef otherParent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-parent"
    );
    QName assocQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      "source"
    );
    ChildAssociationRef otherAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      otherParent,
      assocQName,
      sourceNode,
      true,
      -1
    );
    when(nodeService.getPrimaryParent(sourceNode)).thenReturn(otherAssoc);
    when(nodeService.getParentAssocs(sourceNode)).thenReturn(
      List.of(otherAssoc)
    );

    clipboardApi.paste(sourceNode, destNode, ClipboardAction.MOVE.getValue());

    verify(fileFolderService).moveFrom(
      sourceNode,
      otherParent,
      destNode,
      "testFile.txt"
    );
  }

  @Test
  public void testPaste_whenLink_thenCreatesLinkNode()
    throws FileNotFoundException {
    when(
      searchService.selectNodes(
        eq(destNode),
        anyString(),
        any(),
        eq(namespaceService),
        eq(false)
      )
    ).thenReturn(Collections.emptyList());

    when(
      nodeService.createNode(
        eq(destNode),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class),
        any(QName.class),
        anyMap()
      )
    ).thenReturn(
      new ChildAssociationRef(
        ContentModel.ASSOC_CONTAINS,
        destNode,
        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "link"),
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "link-id"),
        true,
        -1
      )
    );

    clipboardApi.paste(sourceNode, destNode, ClipboardAction.LINK.getValue());

    verify(nodeService).createNode(
      eq(destNode),
      eq(ContentModel.ASSOC_CONTAINS),
      any(QName.class),
      any(QName.class),
      anyMap()
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPaste_whenInvalidAction_thenThrowsIllegalArgumentException()
    throws FileNotFoundException {
    clipboardApi.paste(sourceNode, destNode, 99);
  }

  @Test
  public void testPasteMultiple_whenNodeDoesNotExist_thenReturnsEarly()
    throws FileNotFoundException {
    String[] nodeIds = new String[] { "source-id" };
    when(nodeService.exists(sourceNode)).thenReturn(false);

    clipboardApi.paste(nodeIds, destNode, ClipboardAction.COPY.getValue());

    verify(fileFolderService, never()).copy(any(), any(), any());
  }

  @Test
  public void testPasteMultiple_whenNodesExist_thenPastesEachNode()
    throws FileNotFoundException {
    String[] nodeIds = new String[] { "source-id" };

    when(fileFolderService.list(destNode)).thenReturn(Collections.emptyList());

    clipboardApi.paste(nodeIds, destNode, ClipboardAction.COPY.getValue());

    verify(fileFolderService).copy(sourceNode, destNode, "testFile.txt");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ClipboardApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(clipboardApi, value);
  }
}
