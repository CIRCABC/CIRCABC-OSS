package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ProfilesApi;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProfilesDeleteTest {

  private ProfilesDelete profilesDelete;
  private ProfilesApi profilesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private NodeService nodeService;
  private NodeService unsecureNodeService;
  private ApiToolBox apiToolBox;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-profile-id";
  private static final String PARENT_ID = "parent-group-id";

  @Before
  public void setUp() throws Exception {
    profilesDelete = new ProfilesDelete();
    profilesApi = mock(ProfilesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    nodeService = mock(NodeService.class);
    unsecureNodeService = mock(NodeService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField(ProfilesDelete.class, "profilesApi", profilesApi);
    setField(
      ProfilesDelete.class,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(ProfilesDelete.class, "nodeService", nodeService);
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      unsecureNodeService
    );
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenReturnsEmptyModel() {
    setupRequest(null, null);

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(profilesApi);
  }

  @Test
  public void testExecuteImpl_whenValidIdAndAdmin_thenDeletesProfile() {
    setupRequest(TEST_ID, null);
    setupAdminPermission(true);
    setupRecordBeforeDelete();

    NodeRef expectedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(profilesApi).profilesIdDelete(expectedRef);
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    setupRequest(TEST_ID, null);
    setupAdminPermission(false);

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(profilesApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    setupRequest(TEST_ID, "fr");
    setupAdminPermission(true);
    setupRecordBeforeDelete();

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(profilesApi).profilesIdDelete(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    setupRequest(TEST_ID, null);

    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.getPrimaryParent(profileRef)).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        profileRef
      )
    );

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    setupRequest(TEST_ID, null);

    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.getPrimaryParent(profileRef)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = profilesDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setupRequest(String id, String language) {
    Map<String, String> templateVars = new HashMap<>();
    if (id != null) {
      templateVars.put("id", id);
    }
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
  }

  private void setupAdminPermission(boolean isAdmin) {
    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      PARENT_ID
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(profileRef)).thenReturn(childAssoc);
    when(permissionChecker.isGroupAdmin(PARENT_ID)).thenReturn(isAdmin);
  }

  private void setupRecordBeforeDelete() {
    NodeRef deletedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(apiToolBox.getCurrentInterestGroup(deletedRef)).thenReturn(null);
    NodeRef parentForLog = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "log-parent"
    );
    ChildAssociationRef logAssoc = mock(ChildAssociationRef.class);
    when(logAssoc.getParentRef()).thenReturn(parentForLog);
    when(unsecureNodeService.getPrimaryParent(deletedRef)).thenReturn(logAssoc);
    when(apiToolBox.getCircabcPath(deletedRef, true)).thenReturn("/test/path");
    when(apiToolBox.getDatabaseID(deletedRef)).thenReturn(123L);
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(profilesDelete, value);
  }
}
