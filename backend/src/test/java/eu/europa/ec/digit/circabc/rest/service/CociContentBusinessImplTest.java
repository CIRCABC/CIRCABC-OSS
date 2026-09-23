package eu.europa.ec.digit.circabc.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CociContentBusinessImplTest {

  private CociContentBusinessImpl cociContentBusiness;
  private CheckOutCheckInService checkOutCheckInService;
  private NodeService nodeService;
  private ContentService contentService;

  @Before
  public void setUp() throws Exception {
    cociContentBusiness = new CociContentBusinessImpl();
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
    field.set(cociContentBusiness, value);
  }

  @Test
  public void testCheckOut_whenCalled_thenDelegatesToService() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    when(checkOutCheckInService.checkout(nodeRef)).thenReturn(workingCopy);

    NodeRef result = cociContentBusiness.checkOut(nodeRef);

    assertEquals(workingCopy, result);
    verify(checkOutCheckInService).checkout(nodeRef);
  }

  @Test
  public void testUpdate_whenMimeTypeProvided_thenSetsItOnWriter() {
    NodeRef doc = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(doc, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);
    InputStream inputStream = new ByteArrayInputStream("content".getBytes());

    cociContentBusiness.update(doc, inputStream, "application/pdf");

    verify(writer).setMimetype("application/pdf");
    verify(writer).putContent(inputStream);
  }

  @Test
  public void testUpdate_whenMimeTypeNull_thenDoesNotSetMimetype() {
    NodeRef doc = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    ContentWriter writer = mock(ContentWriter.class);
    when(
      contentService.getWriter(doc, ContentModel.PROP_CONTENT, true)
    ).thenReturn(writer);
    InputStream inputStream = new ByteArrayInputStream("content".getBytes());

    cociContentBusiness.update(doc, inputStream, null);

    verify(writer, never()).setMimetype(anyString());
    verify(writer).putContent(inputStream);
  }

  @Test
  public void testGetWorkingCopy_whenCalled_thenDelegatesToService() {
    NodeRef original = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "orig-id"
    );
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    when(checkOutCheckInService.getWorkingCopy(original)).thenReturn(
      workingCopy
    );

    NodeRef result = cociContentBusiness.getWorkingCopy(original);

    assertEquals(workingCopy, result);
  }

  @Test
  public void testCancelCheckOut_whenCalled_thenDelegatesToService() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    NodeRef original = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "orig-id"
    );
    when(checkOutCheckInService.cancelCheckout(workingCopy)).thenReturn(
      original
    );

    NodeRef result = cociContentBusiness.cancelCheckOut(workingCopy);

    assertEquals(original, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCheckIn_whenMinorVersion_thenSetsMinorType() {
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

    NodeRef result = cociContentBusiness.checkIn(
      workingCopy,
      true,
      "minor fix",
      false
    );

    assertEquals(checkedIn, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCheckIn_whenContentDataNull_thenPassesNullContentUrl() {
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
        (String) isNull(),
        eq(false)
      )
    ).thenReturn(checkedIn);

    NodeRef result = cociContentBusiness.checkIn(
      workingCopy,
      false,
      null,
      false
    );

    assertEquals(checkedIn, result);
  }
}
