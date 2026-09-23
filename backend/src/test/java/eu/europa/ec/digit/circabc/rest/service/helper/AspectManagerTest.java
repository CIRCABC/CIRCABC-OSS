package eu.europa.ec.digit.circabc.rest.service.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class AspectManagerTest {

  private AspectManager aspectManager;
  private org.alfresco.service.cmr.repository.NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() throws Exception {
    aspectManager = new AspectManager();
    nodeService = mock(org.alfresco.service.cmr.repository.NodeService.class);
    Field field = AspectManager.class.getDeclaredField("nodeService");
    field.setAccessible(true);
    field.set(aspectManager, nodeService);
    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id");
  }

  @Test
  public void testAddLibraryAspect_whenBothMissing_thenReturnsTrue() {
    when(nodeService.hasAspect(eq(nodeRef), any(QName.class))).thenReturn(
      false
    );

    boolean result = aspectManager.addLibraryAspect(nodeRef);

    assertTrue(result);
    verify(nodeService).addAspect(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    verify(nodeService).addAspect(nodeRef, CircabcModel.ASPECT_LIBRARY, null);
  }

  @Test
  public void testAddLibraryAspect_whenBothPresent_thenReturnsFalse() {
    when(nodeService.hasAspect(eq(nodeRef), any(QName.class))).thenReturn(true);

    boolean result = aspectManager.addLibraryAspect(nodeRef);

    assertFalse(result);
    verify(nodeService, never()).addAspect(any(), any(), any());
  }

  @Test
  public void testAddNewsgroupAspect_whenBothMissing_thenReturnsTrue() {
    when(nodeService.hasAspect(eq(nodeRef), any(QName.class))).thenReturn(
      false
    );

    boolean result = aspectManager.addNewsgroupAspect(nodeRef);

    assertTrue(result);
    verify(nodeService).addAspect(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    verify(nodeService).addAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP, null);
  }

  @Test
  public void testAddEventAspect_whenBothMissing_thenReturnsTrue() {
    when(nodeService.hasAspect(eq(nodeRef), any(QName.class))).thenReturn(
      false
    );

    boolean result = aspectManager.addEventAspect(nodeRef);

    assertTrue(result);
    verify(nodeService).addAspect(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    verify(nodeService).addAspect(nodeRef, CircabcModel.ASPECT_EVENT, null);
  }

  @Test
  public void testAddInformationAspect_whenBothMissing_thenReturnsTrue() {
    when(nodeService.hasAspect(eq(nodeRef), any(QName.class))).thenReturn(
      false
    );

    boolean result = aspectManager.addInformationAspect(nodeRef);

    assertTrue(result);
    verify(nodeService).addAspect(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    verify(nodeService).addAspect(
      nodeRef,
      CircabcModel.ASPECT_INFORMATION,
      null
    );
  }

  @Test
  public void testAddLibraryAspect_whenOnlyManagementPresent_thenReturnsTrue() {
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);

    boolean result = aspectManager.addLibraryAspect(nodeRef);

    assertTrue(result);
    verify(nodeService, never()).addAspect(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    verify(nodeService).addAspect(nodeRef, CircabcModel.ASPECT_LIBRARY, null);
  }

  @Test
  public void testIsLibraryNode_whenHasAspect_thenReturnsTrue() {
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);

    assertTrue(aspectManager.isLibraryNode(nodeRef));
  }

  @Test
  public void testIsLibraryNode_whenMissingAspect_thenReturnsFalse() {
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);

    assertFalse(aspectManager.isLibraryNode(nodeRef));
  }

  @Test
  public void testIsNewsgroupNode_whenHasAspect_thenReturnsTrue() {
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    assertTrue(aspectManager.isNewsgroupNode(nodeRef));
  }

  @Test
  public void testIsNewsgroupNode_whenMissingAspect_thenReturnsFalse() {
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);

    assertFalse(aspectManager.isNewsgroupNode(nodeRef));
  }
}
