package eu.europa.ec.digit.circabc.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CociContentBusinessSrvTest {

  private CociContentBusinessImpl service;
  private CheckOutCheckInService checkOutCheckInService;
  private NodeService nodeService;
  private ContentService contentService;

  @Before
  public void setUp() throws Exception {
    service = new CociContentBusinessImpl();
    checkOutCheckInService = mock(CheckOutCheckInService.class);
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);

    setField("checkOutCheckInService", checkOutCheckInService);
    setField("nodeService", nodeService);
    setField("contentService", contentService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CociContentBusinessImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testCheckOut_whenValidNodeRef_thenReturnsWorkingCopy() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "original-id"
    );
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "working-copy-id"
    );
    when(checkOutCheckInService.checkout(nodeRef)).thenReturn(workingCopy);

    NodeRef result = service.checkOut(nodeRef);

    assertEquals(workingCopy, result);
    verify(checkOutCheckInService).checkout(nodeRef);
  }

  @Test
  public void testUpdate_whenMimeTypeProvided_thenSetsMimeTypeAndWritesContent() {
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    InputStream inputStream = new ByteArrayInputStream("content".getBytes());
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(docRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    service.update(docRef, inputStream, "application/pdf");

    verify(writer).setMimetype("application/pdf");
    verify(writer).putContent(inputStream);
  }

  @Test
  public void testUpdate_whenMimeTypeNull_thenSkipsMimeType() {
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    InputStream inputStream = new ByteArrayInputStream("content".getBytes());
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(docRef, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);

    service.update(docRef, inputStream, null);

    verify(writer, never()).setMimetype(anyString());
    verify(writer).putContent(inputStream);
  }

  @Test
  public void testGetWorkingCopy_whenCalled_thenDelegatesToService() {
    NodeRef originalRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "original-id"
    );
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    when(checkOutCheckInService.getWorkingCopy(originalRef)).thenReturn(
      workingCopy
    );

    NodeRef result = service.getWorkingCopy(originalRef);

    assertEquals(workingCopy, result);
  }

  @Test
  public void testCancelCheckOut_whenCalled_thenReturnsOriginalNode() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    NodeRef original = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "original-id"
    );
    when(checkOutCheckInService.cancelCheckout(workingCopy)).thenReturn(
      original
    );

    NodeRef result = service.cancelCheckOut(workingCopy);

    assertEquals(original, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCheckIn_whenMinorVersion_thenSetsMinorVersionType() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    NodeRef checkedIn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "checked-in-id"
    );
    ContentData contentData = mock(ContentData.class);
    when(contentData.getContentUrl()).thenReturn("store://content-url");
    when(
      nodeService.getProperty(workingCopy, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(
      checkOutCheckInService.checkin(
        eq(workingCopy),
        any(Map.class),
        eq("store://content-url"),
        eq(false)
      )
    ).thenReturn(checkedIn);

    NodeRef result = service.checkIn(workingCopy, true, "minor fix", false);

    assertEquals(checkedIn, result);
    ArgumentCaptor<Map<String, Serializable>> propsCaptor =
      ArgumentCaptor.forClass(Map.class);
    verify(checkOutCheckInService).checkin(
      eq(workingCopy),
      propsCaptor.capture(),
      eq("store://content-url"),
      eq(false)
    );
    Map<String, Serializable> props = propsCaptor.getValue();
    assertEquals("minor fix", props.get("description"));
    assertEquals(
      VersionType.MINOR,
      props.get(VersionBaseModel.PROP_VERSION_TYPE)
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCheckIn_whenMajorVersionAndNullNote_thenSetsMajorAndEmptyDescription() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    NodeRef checkedIn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "checked-in-id"
    );
    when(
      nodeService.getProperty(workingCopy, ContentModel.PROP_CONTENT)
    ).thenReturn(null);
    when(
      checkOutCheckInService.checkin(
        eq(workingCopy),
        any(Map.class),
        isNull(),
        eq(true)
      )
    ).thenReturn(checkedIn);

    NodeRef result = service.checkIn(workingCopy, false, null, true);

    assertEquals(checkedIn, result);
    ArgumentCaptor<Map<String, Serializable>> propsCaptor =
      ArgumentCaptor.forClass(Map.class);
    verify(checkOutCheckInService).checkin(
      eq(workingCopy),
      propsCaptor.capture(),
      isNull(),
      eq(true)
    );
    Map<String, Serializable> props = propsCaptor.getValue();
    assertEquals("", props.get("description"));
    assertEquals(
      VersionType.MAJOR,
      props.get(VersionBaseModel.PROP_VERSION_TYPE)
    );
  }
}
