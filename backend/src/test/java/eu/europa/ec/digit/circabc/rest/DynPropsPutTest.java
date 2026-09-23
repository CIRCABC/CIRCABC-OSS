package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
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

public class DynPropsPutTest {

  private DynPropsPut dynPropsPut;
  private DynamicPropertiesApi dynamicPropertiesApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String DP_ID = "dp-id-123";
  private static final String DP_FOLDER_ID = "dp-folder-id";
  private static final String IG_ID = "ig-id-456";

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

    dynPropsPut = new DynPropsPut();
    dynamicPropertiesApi = mock(DynamicPropertiesApi.class);
    nodeService = mock(NodeService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("dynamicPropertiesApi", dynamicPropertiesApi);
    setField("nodeService", nodeService);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", DP_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);

    NodeRef dpRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      DP_ID
    );
    NodeRef dpFolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      DP_FOLDER_ID
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );

    ChildAssociationRef dpParent = mock(ChildAssociationRef.class);
    when(dpParent.getParentRef()).thenReturn(dpFolderRef);
    when(nodeService.getPrimaryParent(dpRef)).thenReturn(dpParent);

    ChildAssociationRef folderParent = mock(ChildAssociationRef.class);
    when(folderParent.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(dpFolderRef)).thenReturn(folderParent);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DynPropsPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(dynPropsPut, value);
  }

  @Test
  public void testExecuteImpl_whenGroupAdmin_thenSuccess() throws Exception {
    when(permissionChecker.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"id\":\"dp-id-123\",\"title\":{\"en\":\"Test\"},\"propertyType\":\"TEXT\"}"
    );

    DynamicPropertyDefinition result = new DynamicPropertyDefinition();
    result.setId(DP_ID);
    when(
      dynamicPropertiesApi.dynpropsIdPut(
        eq(DP_ID),
        any(DynamicPropertyDefinition.class)
      )
    ).thenReturn(result);

    Map<String, Object> model = dynPropsPut.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(result, model.get("dp"));
    verify(dynamicPropertiesApi).dynpropsIdPut(
      eq(DP_ID),
      any(DynamicPropertyDefinition.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNotGroupAdmin_thenForbidden()
    throws Exception {
    when(permissionChecker.isGroupAdmin(IG_ID)).thenReturn(false);

    Map<String, Object> model = dynPropsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(dynamicPropertiesApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    when(permissionChecker.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json");

    Map<String, Object> model = dynPropsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(permissionChecker.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> model = dynPropsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenMissingTitle_thenBadRequest()
    throws Exception {
    when(permissionChecker.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"id\":\"dp-id-123\",\"propertyType\":\"TEXT\"}"
    );

    Map<String, Object> model = dynPropsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
