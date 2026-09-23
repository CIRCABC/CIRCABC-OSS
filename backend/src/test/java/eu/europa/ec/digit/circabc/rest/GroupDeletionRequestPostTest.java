package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupDeletionRequestPostTest {

  private GroupDeletionRequestPost webScript;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private CategoriesApi categoriesApi;
  private AuthenticationService authenticationService;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupDeletionRequestPost();
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    categoriesApi = mock(CategoriesApi.class);
    authenticationService = mock(AuthenticationService.class);

    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("categoriesApi", categoriesApi);
    setField("authenticationService", authenticationService);

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
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenCallsCategoriesApi()
    throws Exception {
    String groupId = "test-group-id";
    String categoryId = "test-category-id";
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      groupId
    );
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      categoryId
    );

    WebScriptRequest req = mockRequest(groupId, "{\"justification\":\"test\"}");
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(currentUserPermissionCheckerService.isGroupAdmin(groupId)).thenReturn(
      true
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(childAssoc);

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(categoriesApi).groupIdDeleteRequestPost(
      any(GroupDeletionRequest.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNullGroupId_thenReturnsEmptyModel() {
    WebScriptRequest req = mockRequest(null, "{}");
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verifyNoInteractions(categoriesApi);
  }

  @Test
  public void testExecuteImpl_whenNotGroupAdmin_thenReturnsForbidden() {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, "{}");
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(currentUserPermissionCheckerService.isGroupAdmin(groupId)).thenReturn(
      false
    );

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String groupId = "invalid-id";
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      groupId
    );

    WebScriptRequest req = mockRequest(groupId, "{\"justification\":\"test\"}");
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(currentUserPermissionCheckerService.isGroupAdmin(groupId)).thenReturn(
      true
    );
    when(nodeService.getPrimaryParent(igRef)).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(igRef)
    );

    Status status = new Status();
    Cache cache = new Cache();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private WebScriptRequest mockRequest(String groupId, String body) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);

    try {
      Content content = mock(Content.class);
      when(content.getContent()).thenReturn(body);
      when(req.getContent()).thenReturn(content);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupDeletionRequestPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
