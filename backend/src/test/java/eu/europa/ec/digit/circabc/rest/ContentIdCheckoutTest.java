package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentIdCheckoutTest {

  private ContentIdCheckout contentIdCheckout;
  private CociContentBusinessSrv cociContentBusinessSrv;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private PermissionService permissionService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );
  private static final NodeRef WORKING_COPY_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "working-copy-id"
  );

  @Before
  public void setUp() throws Exception {
    contentIdCheckout = new ContentIdCheckout();

    cociContentBusinessSrv = mock(CociContentBusinessSrv.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    permissionService = mock(PermissionService.class);

    setField("cociContentBusinessSrv", cociContentBusinessSrv);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("permissionService", permissionService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("editInline")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenHappyPath_thenReturnsWorkingCopyId()
    throws Exception {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckoutPermission(TEST_ID)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(false);
    when(cociContentBusinessSrv.checkOut(TEST_NODE_REF)).thenReturn(
      WORKING_COPY_REF
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals("working-copy-id", result.get("workingCopyId"));
    verify(permissionService).setInheritParentPermissions(
      WORKING_COPY_REF,
      false
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNodeDoesNotExist_thenThrowsException() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    callExecuteImpl();
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckoutPermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUrlNode_thenReturnsNotAcceptable() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckoutPermission(TEST_ID)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(true);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEditInlineTrue_thenAddsInlineEditableAspect() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckoutPermission(TEST_ID)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(false);
    when(cociContentBusinessSrv.checkOut(TEST_NODE_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(req.getParameter("editInline")).thenReturn("true");

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    verify(nodeService).addAspect(eq(WORKING_COPY_REF), any(), anyMap());
  }

  private Map<String, Object> callExecuteImpl() {
    try {
      java.lang.reflect.Method method =
        ContentIdCheckout.class.getDeclaredMethod(
          "executeImpl",
          WebScriptRequest.class,
          Status.class,
          Cache.class
        );
      method.setAccessible(true);
      return (Map<String, Object>) method.invoke(
        contentIdCheckout,
        req,
        status,
        cache
      );
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentIdCheckout.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentIdCheckout, value);
  }
}
