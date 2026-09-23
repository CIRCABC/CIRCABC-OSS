package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentIdCancelCheckoutTest {

  private ContentIdCancelCheckout webScript;
  private CociContentBusinessSrv cociContentBusinessSrv;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef ORIGINAL_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );
  private static final NodeRef WORKING_COPY_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "working-copy-id"
  );

  @Before
  public void setUp() throws Exception {
    webScript = new ContentIdCancelCheckout();

    cociContentBusinessSrv = mock(CociContentBusinessSrv.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("cociContentBusinessSrv", cociContentBusinessSrv);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
  }

  @Test
  public void testExecuteImpl_whenSuccessful_thenReturnsOriginalNodeId() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);

    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result-id"
    );
    when(cociContentBusinessSrv.cancelCheckOut(WORKING_COPY_REF)).thenReturn(
      resultRef
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("result-id", model.get("originalNodeId"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenWorkingCopyNotExists_thenThrows() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(false);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNotWorkingCopy_thenThrows() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotOwnerAndNotLibAdmin_thenReturnsForbidden() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        WORKING_COPY_REF.getId(),
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLibAdminButNotOwner_thenSucceeds() {
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        WORKING_COPY_REF.getId(),
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result-id"
    );
    when(cociContentBusinessSrv.cancelCheckOut(WORKING_COPY_REF)).thenReturn(
      resultRef
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("result-id", model.get("originalNodeId"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentIdCancelCheckout.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
