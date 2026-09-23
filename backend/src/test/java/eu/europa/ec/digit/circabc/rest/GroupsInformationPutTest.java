package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.model.InformationPage;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
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

public class GroupsInformationPutTest {

  private GroupsInformationPut webScript;
  private InformationApi informationApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsInformationPut();
    informationApi = mock(InformationApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("informationApi", informationApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHappyPath_thenReturnsModel()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq("info-id"),
        eq(InformationPermissions.INFADMIN)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"adapt\": true, \"displayOldInformation\": false}"
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    verify(informationApi).groupsIdInformationPut(
      eq("test-ig-id"),
      any(InformationPage.class)
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq("info-id"),
        eq(InformationPermissions.INFADMIN)
      )
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );

    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenThrow(new InvalidNodeRefException("invalid", igRef));

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq("info-id"),
        eq(InformationPermissions.INFADMIN)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenParseException_thenReturnsBadRequest()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq("info-id"),
        eq(InformationPermissions.INFADMIN)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json {{{");

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      GroupsInformationPut.class.getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webScript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsInformationPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
