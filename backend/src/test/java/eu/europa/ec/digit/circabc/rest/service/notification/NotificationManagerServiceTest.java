package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class NotificationManagerServiceTest {

  private NotificationManagerServiceImpl service;
  private NodeService nodeService;
  private NodeRef igNodeRef;

  @Before
  public void setUp() throws Exception {
    service = new NotificationManagerServiceImpl();
    nodeService = mock(NodeService.class);

    Field field = NotificationManagerServiceImpl.class.getDeclaredField(
      "nodeService"
    );
    field.setAccessible(true);
    field.set(service, nodeService);

    igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIsPasteAllNotificationEnabled_whenNotIgRoot_thenThrows() {
    NodeRef nonIgNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "non-ig"
    );
    when(
      nodeService.hasAspect(nonIgNode, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    service.isPasteAllNotificationEnabled(nonIgNode);
  }

  @Test
  public void testIsPasteAllNotificationEnabled_whenHasAspect_thenReturnsTrue() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(true);

    assertTrue(service.isPasteAllNotificationEnabled(igNodeRef));
  }

  @Test
  public void testIsPasteAllNotificationEnabled_whenNoAspect_thenReturnsFalse() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(false);

    assertFalse(service.isPasteAllNotificationEnabled(igNodeRef));
  }

  @Test
  public void testIsPasteNotificationEnabled_whenHasAspect_thenReturnsTrue() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE)
    ).thenReturn(true);

    assertTrue(service.isPasteNotificationEnabled(igNodeRef));
  }

  @Test
  public void testIsPasteNotificationEnabled_whenNoAspect_thenReturnsFalse() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE)
    ).thenReturn(false);

    assertFalse(service.isPasteNotificationEnabled(igNodeRef));
  }

  @Test
  public void testSetPasteAllNotificationEnabled_whenTrueAndNoAspect_thenAddsAspect() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(false);

    service.setPasteAllNotificationEnabled(igNodeRef, true);

    verify(nodeService).addAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL,
      null
    );
  }

  @Test
  public void testSetPasteAllNotificationEnabled_whenTrueAndHasAspect_thenNoOp() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(true);

    service.setPasteAllNotificationEnabled(igNodeRef, true);

    verify(nodeService, never()).addAspect(
      eq(igNodeRef),
      eq(CircabcModel.ASPECT_NOTIFY_PASTE_ALL),
      any()
    );
    verify(nodeService, never()).removeAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL
    );
  }

  @Test
  public void testSetPasteAllNotificationEnabled_whenFalseAndHasAspect_thenRemovesAspect() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(true);

    service.setPasteAllNotificationEnabled(igNodeRef, false);

    verify(nodeService).removeAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL
    );
  }

  @Test
  public void testSetPasteAllNotificationEnabled_whenFalseAndNoAspect_thenNoOp() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL)
    ).thenReturn(false);

    service.setPasteAllNotificationEnabled(igNodeRef, false);

    verify(nodeService, never()).addAspect(
      eq(igNodeRef),
      eq(CircabcModel.ASPECT_NOTIFY_PASTE_ALL),
      any()
    );
    verify(nodeService, never()).removeAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL
    );
  }

  @Test
  public void testSetPasteNotificationEnabled_whenTrueAndNoAspect_thenAddsAspect() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE)
    ).thenReturn(false);

    service.setPasteNotificationEnabled(igNodeRef, true);

    verify(nodeService).addAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE,
      null
    );
  }

  @Test
  public void testSetPasteNotificationEnabled_whenFalseAndHasAspect_thenRemovesAspect() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE)
    ).thenReturn(true);

    service.setPasteNotificationEnabled(igNodeRef, false);

    verify(nodeService).removeAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetPasteNotificationEnabled_whenNotIgRoot_thenThrows() {
    NodeRef nonIgNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "non-ig"
    );
    when(
      nodeService.hasAspect(nonIgNode, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    service.setPasteNotificationEnabled(nonIgNode, true);
  }
}
