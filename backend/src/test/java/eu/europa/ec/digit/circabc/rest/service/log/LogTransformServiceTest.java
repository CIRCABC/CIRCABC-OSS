package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LogTransformServiceTest {

  private LogTransformServiceImpl service;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private LogDaoService logDaoService;
  private ApiToolBox apiToolBox;

  private static final NodeRef CIRCABC_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "circabc-root"
  );

  @Before
  public void setUp() throws Exception {
    service = new LogTransformServiceImpl();
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    logDaoService = mock(LogDaoService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("circabcApi", circabcApi);
    setField("logDaoService", logDaoService);
    setField("apiToolBox", apiToolBox);

    when(circabcApi.getCircabcNodeRef()).thenReturn(CIRCABC_NODE_REF);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LogTransformServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testTransform_whenValidUUID_thenResolvesNodeRef() {
    String uuid = UUID.randomUUID().toString();
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      uuid
    );
    Date logDate = new Date();

    LogRestDAO input = new LogRestDAO();
    input.setTemplateID(1);
    input.setPathOneValue(uuid);
    input.setUserName("admin");
    input.setLogDate(logDate);
    input.setStatusCode(200);
    input.setInfo("some info");

    when(logDaoService.getActivityID(1L)).thenReturn(100L);
    when(apiToolBox.getNodeRef(uuid)).thenReturn(nodeRef);
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(42L);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn(
      "/Category/IG/Library"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(false);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(
      CIRCABC_NODE_REF
    );
    when(apiToolBox.getDatabaseID(CIRCABC_NODE_REF)).thenReturn(10L);
    when(apiToolBox.getName(CIRCABC_NODE_REF)).thenReturn("TestIG");

    LogRecordDAO result = service.transform(input);

    assertEquals(100, result.getActivityID());
    assertEquals(uuid, result.getUuid());
    assertEquals(42L, result.getDocumentID());
    assertEquals("/Category/IG/Library", result.getPath());
    assertEquals(logDate, result.getDate());
    assertEquals(1, result.getIsOK());
    assertEquals("admin", result.getUser());
    assertEquals(10L, result.getIgID());
    assertEquals("TestIG", result.getIgName());
  }

  @Test
  public void testTransform_whenNullPathOneValue_thenFallsToCircabcNodeRef() {
    LogRestDAO input = new LogRestDAO();
    input.setTemplateID(5);
    input.setPathOneValue(null);
    input.setUserName("user1");
    input.setLogDate(new Date());
    input.setStatusCode(201);
    input.setInfo("plain text");

    when(logDaoService.getActivityID(5L)).thenReturn(200L);
    when(
      logDaoService.getTemplateID(
        "POST",
        "/circabc/repositories/{id}/transaction"
      )
    ).thenReturn(99);
    when(
      nodeService.hasAspect(CIRCABC_NODE_REF, CircabcModel.ASPECT_CIRCABC_ROOT)
    ).thenReturn(true);
    when(apiToolBox.getDatabaseID(CIRCABC_NODE_REF)).thenReturn(1L);
    when(apiToolBox.getName(CIRCABC_NODE_REF)).thenReturn("circabc");
    when(apiToolBox.getCircabcPath(CIRCABC_NODE_REF, true)).thenReturn("/");

    LogRecordDAO result = service.transform(input);

    assertEquals("user1", result.getUser());
    assertEquals(1, result.getIsOK());
    assertEquals(1L, result.getIgID());
  }

  @Test
  public void testTransform_whenStatusCode400_thenIsOKZero() {
    String uuid = UUID.randomUUID().toString();
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      uuid
    );

    LogRestDAO input = new LogRestDAO();
    input.setTemplateID(1);
    input.setPathOneValue(uuid);
    input.setUserName("user3");
    input.setLogDate(new Date());
    input.setStatusCode(400);
    input.setInfo("error");

    when(logDaoService.getActivityID(1L)).thenReturn(100L);
    when(apiToolBox.getNodeRef(uuid)).thenReturn(nodeRef);
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(50L);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/path");
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)
    ).thenReturn(true);
    when(apiToolBox.getName(nodeRef)).thenReturn("node");

    LogRecordDAO result = service.transform(input);

    assertEquals(0, result.getIsOK());
  }

  @Test
  public void testTransform_whenProcessInfoFails_thenReturnsRawInfo() {
    String uuid = UUID.randomUUID().toString();
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      uuid
    );

    LogRestDAO input = new LogRestDAO();
    input.setTemplateID(1);
    input.setPathOneValue(uuid);
    input.setUserName("user4");
    input.setLogDate(new Date());
    input.setStatusCode(200);
    input.setInfo("not json {{{");

    when(logDaoService.getActivityID(1L)).thenReturn(9957105L);
    when(apiToolBox.getNodeRef(uuid)).thenReturn(nodeRef);
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(1L);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/p");
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)
    ).thenReturn(true);
    when(apiToolBox.getName(nodeRef)).thenReturn("n");

    LogRecordDAO result = service.transform(input);

    assertEquals("not json {{{", result.getInfo());
  }
}
