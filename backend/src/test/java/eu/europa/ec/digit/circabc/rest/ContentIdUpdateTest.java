package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.extensions.webscripts.servlet.FormData;

public class ContentIdUpdateTest {

  private static final String TEST_ID = "test-node-id";

  private ContentIdUpdate webScript;
  private NodeService nodeService;
  private CociContentBusinessSrv cociContentBusinessSrv;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ContentIdUpdate();
    nodeService = mock(NodeService.class);
    cociContentBusinessSrv = mock(CociContentBusinessSrv.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("nodeService", nodeService);
    setField("cociContentBusinessSrv", cociContentBusinessSrv);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

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

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNodeDoesNotExist_thenThrowsException() {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(false);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNodeNotWorkingCopy_thenThrowsException() {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(true);
    when(
      nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenNoLibraryPermission_thenForbidden() {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(true);
    when(
      nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotWorkingCopyOwner_thenForbidden() {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(true);
    when(
      nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenNotAcceptable() {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(true);
    when(
      nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(TEST_ID)
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenValidUpload_thenSuccess() throws Exception {
    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(workingCopyRef)).thenReturn(true);
    when(
      nodeService.hasAspect(workingCopyRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      currentUserPermissionCheckerService.isWorkingCopyOwner(TEST_ID)
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(req.parseContent()).thenReturn(formData);
    when(formData.getIsMultiPart()).thenReturn(true);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getMimetype()).thenReturn("application/pdf");
    when(field.getInputStream()).thenReturn(
      new ByteArrayInputStream("content".getBytes())
    );
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(cociContentBusinessSrv).update(
      eq(workingCopyRef),
      any(),
      eq("application/pdf")
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentIdUpdate.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
