package io.swagger.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class FileUtilTest {

  private NodeService nodeService;
  private NodeRef parentRef;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
  }

  @Test
  public void testGenerateUniqueFilename_whenNoConflict_thenReturnsOriginal() {
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "report.pdf"
      )
    ).thenReturn(null);

    String result = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      "report.pdf"
    );

    assertEquals("report.pdf", result);
  }

  @Test
  public void testGenerateUniqueFilename_whenOneConflict_thenAppendsCopyNumber() {
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "report.pdf"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "report(1).pdf"
      )
    ).thenReturn(null);

    String result = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      "report.pdf"
    );

    assertEquals("report(1).pdf", result);
  }

  @Test
  public void testGenerateUniqueFilename_whenMultipleConflicts_thenIncrementsCounter() {
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc.txt"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc(1).txt"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc(2).txt"
      )
    ).thenReturn(null);

    String result = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      "doc.txt"
    );

    assertEquals("doc(2).txt", result);
  }

  @Test
  public void testGenerateUniqueFilename_whenFileAlreadyHasNumber_thenReplacesNumber() {
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc(1).txt"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc(1).txt"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "doc(2).txt"
      )
    ).thenReturn(null);

    String result = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      "doc(1).txt"
    );

    assertEquals("doc(2).txt", result);
  }

  @Test
  public void testGenerateUniqueFilename_whenNoExtension_thenAppendsNumber() {
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "README"
      )
    ).thenReturn(existingNode);
    when(
      nodeService.getChildByName(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        "README(1)"
      )
    ).thenReturn(null);

    String result = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      "README"
    );

    assertEquals("README(1)", result);
  }
}
