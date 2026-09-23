package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ProfilesApi;
import io.swagger.model.Profile;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProfilesPutTest {

  private ProfilesPut profilesPut;
  private ProfilesApi profilesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    profilesPut = new ProfilesPut();
    profilesApi = mock(ProfilesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);

    setField("profilesApi", profilesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);

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
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsProfile()
    throws Exception {
    String profileId = "profile-id-123";
    String parentId = "parent-id-456";
    mockTemplateVars(profileId);
    when(req.getParameter("language")).thenReturn(null);
    mockNodeServiceParent(profileId, parentId);
    when(currentUserPermissionCheckerService.isGroupAdmin(parentId)).thenReturn(
      true
    );

    String json = "{\"id\":\"profile-id-123\",\"title\":{\"en\":\"Test\"}}";
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);

    Profile expectedProfile = new Profile();
    expectedProfile.setId(profileId);
    when(
      profilesApi.profilesIdPut(any(NodeRef.class), any(Profile.class))
    ).thenReturn(expectedProfile);

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedProfile, result.get("profile"));
    verify(profilesApi).profilesIdPut(any(NodeRef.class), any(Profile.class));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String profileId = "profile-id-123";
    String parentId = "parent-id-456";
    mockTemplateVars(profileId);
    when(req.getParameter("language")).thenReturn(null);
    mockNodeServiceParent(profileId, parentId);
    when(currentUserPermissionCheckerService.isGroupAdmin(parentId)).thenReturn(
      false
    );

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdNull_thenReturnsEmptyModel()
    throws Exception {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(profilesApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    String profileId = "profile-id-123";
    String parentId = "parent-id-456";
    mockTemplateVars(profileId);
    when(req.getParameter("language")).thenReturn("fr");
    mockNodeServiceParent(profileId, parentId);
    when(currentUserPermissionCheckerService.isGroupAdmin(parentId)).thenReturn(
      true
    );

    String json = "{\"id\":\"profile-id-123\",\"title\":{\"fr\":\"Test FR\"}}";
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);

    Profile expectedProfile = new Profile();
    when(
      profilesApi.profilesIdPut(any(NodeRef.class), any(Profile.class))
    ).thenReturn(expectedProfile);

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(expectedProfile, result.get("profile"));
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    String profileId = "profile-id-123";
    String parentId = "parent-id-456";
    mockTemplateVars(profileId);
    when(req.getParameter("language")).thenReturn(null);
    mockNodeServiceParent(profileId, parentId);
    when(currentUserPermissionCheckerService.isGroupAdmin(parentId)).thenReturn(
      true
    );

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenParseException_thenReturnsBadRequest()
    throws Exception {
    String profileId = "profile-id-123";
    String parentId = "parent-id-456";
    mockTemplateVars(profileId);
    when(req.getParameter("language")).thenReturn(null);
    mockNodeServiceParent(profileId, parentId);
    when(currentUserPermissionCheckerService.isGroupAdmin(parentId)).thenReturn(
      true
    );

    String json = "{\"id\":\"profile-id-123\"}"; // missing title -> ParseException
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);

    Map<String, Object> result = profilesPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private void mockNodeServiceParent(String profileId, String parentId) {
    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      profileId
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      parentId
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(profileRef)).thenReturn(childAssoc);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ProfilesPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(profilesPut, value);
  }
}
