package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
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

public class NewsDeleteTest {

  private NewsDelete newsDelete;
  private InformationApi informationApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ApiToolBox apiToolBox;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    newsDelete = new NewsDelete();
    informationApi = mock(InformationApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    apiToolBox = mock(ApiToolBox.class);
    nodeService = mock(NodeService.class);

    setField(NewsDelete.class, "informationApi", informationApi);
    setField(
      NewsDelete.class,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      nodeService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenDeletesNews()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-node-id")
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(123L);

    Map<String, Object> result = newsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(informationApi).newsIdDelete("test-node-id");
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-node-id")
    ).thenReturn(false);

    Map<String, Object> result = newsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(informationApi, never()).newsIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-node-id")
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(1L);

    doThrow(new InvalidNodeRefException(nodeRef))
      .when(informationApi)
      .newsIdDelete("test-node-id");

    Map<String, Object> result = newsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-node-id")
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(1L);

    doThrow(new RuntimeException("unexpected"))
      .when(informationApi)
      .newsIdDelete("test-node-id");

    Map<String, Object> result = newsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      permissionChecker.hasAlfrescoDeletePermission("test-node-id")
    ).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(nodeRef, true)).thenReturn("/path");
    when(apiToolBox.getDatabaseID(nodeRef)).thenReturn(1L);

    Map<String, Object> result = newsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(informationApi).newsIdDelete("test-node-id");
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(newsDelete, value);
  }
}
