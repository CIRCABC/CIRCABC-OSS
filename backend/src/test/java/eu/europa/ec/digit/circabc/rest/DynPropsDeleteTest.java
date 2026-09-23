package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DynPropsDeleteTest {

  private static final String DP_ID = "dp-node-id";
  private static final String DP_FOLDER_ID = "dp-folder-id";
  private static final String IG_ID = "ig-node-id";

  private DynPropsDelete dynPropsDelete;
  private DynamicPropertiesApi dynamicPropertiesApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    dynPropsDelete = new DynPropsDelete();
    dynamicPropertiesApi = mock(DynamicPropertiesApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("dynamicPropertiesApi", dynamicPropertiesApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", DP_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

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

    ChildAssociationRef dpParentAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(dpRef)).thenReturn(dpParentAssoc);
    when(dpParentAssoc.getParentRef()).thenReturn(dpFolderRef);

    ChildAssociationRef folderParentAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(dpFolderRef)).thenReturn(
      folderParentAssoc
    );
    when(folderParentAssoc.getParentRef()).thenReturn(igRef);
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenDeletesAndReturnsModel() {
    when(currentUserPermissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(
      true
    );

    Map<String, Object> result = dynPropsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(dynamicPropertiesApi).dynpropsIdDelete(DP_ID);
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden() {
    when(currentUserPermissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(
      false
    );

    Map<String, Object> result = dynPropsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(dynamicPropertiesApi, never()).dynpropsIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    NodeRef dpRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      DP_ID
    );
    when(nodeService.getPrimaryParent(dpRef)).thenThrow(
      new InvalidNodeRefException(dpRef)
    );

    Map<String, Object> result = dynPropsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DynPropsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(dynPropsDelete, value);
  }
}
