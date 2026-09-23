package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentIdCheckinTest {

  private ContentIdCheckin contentIdCheckin;
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
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    contentIdCheckin = new ContentIdCheckin();
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
  }

  @Test
  public void testExecuteImpl_whenHappyPath_thenCheckinSucceeds()
    throws Exception {
    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckinPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(req.getParameter("minorChange")).thenReturn("true");
    when(req.getParameter("keepCheckedOut")).thenReturn("false");
    when(req.getParameter("endEditInline")).thenReturn("false");
    when(req.getParameter("comment")).thenReturn("test comment");

    Map<String, Object> result = contentIdCheckin.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(cociContentBusinessSrv).checkIn(
      WORKING_COPY_REF,
      true,
      "test comment",
      false
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenWorkingCopyNotExists_thenThrows() {
    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(false);

    contentIdCheckin.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNotWorkingCopy_thenThrows() {
    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    contentIdCheckin.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenNoCheckinPermission_thenForbidden() {
    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckinPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(false);

    Map<String, Object> result = contentIdCheckin.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEndEditInline_thenRemovesAspect() {
    when(cociContentBusinessSrv.getWorkingCopy(ORIGINAL_REF)).thenReturn(
      WORKING_COPY_REF
    );
    when(nodeService.exists(WORKING_COPY_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(WORKING_COPY_REF, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAlfCheckinPermission(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(
        WORKING_COPY_REF.getId()
      )
    ).thenReturn(true);
    when(req.getParameter("endEditInline")).thenReturn("true");

    Map<String, Object> result = contentIdCheckin.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(nodeService).removeAspect(
      WORKING_COPY_REF,
      ApplicationModel.ASPECT_INLINEEDITABLE
    );
    verify(cociContentBusinessSrv).checkIn(WORKING_COPY_REF, false, "", false);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentIdCheckin.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentIdCheckin, value);
  }
}
