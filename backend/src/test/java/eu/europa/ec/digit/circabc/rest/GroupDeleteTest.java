package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
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

public class GroupDeleteTest {

  private GroupDelete groupDelete;
  private GroupsApi groupsApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupDelete, value);
  }

  private void setupRequest(Map<String, String> templateVars) {
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Before
  public void setUp() throws Exception {
    groupDelete = new GroupDelete();
    groupsApi = mock(GroupsApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("groupsApi", groupsApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenDeletesGroup() {
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

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    setupRequest(templateVars);
    when(req.getParameter("language")).thenReturn(null);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(childAssoc);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    Map<String, Object> result = groupDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).groupsIdDelete(groupId, true, true);
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenReturnsForbidden() {
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

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    setupRequest(templateVars);
    when(req.getParameter("language")).thenReturn(null);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(childAssoc);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(false);

    Map<String, Object> result = groupDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(groupsApi, never()).groupsIdDelete(
      anyString(),
      anyBoolean(),
      anyBoolean()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String groupId = "invalid-id";

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    setupRequest(templateVars);
    when(req.getParameter("language")).thenReturn(null);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      groupId
    );
    when(nodeService.getPrimaryParent(igRef)).thenThrow(
      new InvalidNodeRefException(igRef)
    );

    Map<String, Object> result = groupDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGroupIdNull_thenReturnsEmptyModel() {
    Map<String, String> templateVars = new HashMap<>();
    setupRequest(templateVars);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = groupDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
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

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    setupRequest(templateVars);
    when(req.getParameter("language")).thenReturn("fr");

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(childAssoc);
    when(childAssoc.getParentRef()).thenReturn(categoryRef);
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(categoryId)
    ).thenReturn(true);

    Map<String, Object> result = groupDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).groupsIdDelete(groupId, true, true);
  }
}
