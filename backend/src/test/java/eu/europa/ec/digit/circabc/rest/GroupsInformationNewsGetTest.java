package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.model.PagedNews;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
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

public class GroupsInformationNewsGetTest {

  private GroupsInformationNewsGet webScript;
  private InformationApi informationApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsInformationNewsGet();
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
  public void testExecuteImpl_whenHappyPath_thenReturnsNews() throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("language")).thenReturn(null);
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
        any()
      )
    ).thenReturn(true);
    when(
      informationApi.groupsIdInformationNewsGet("test-ig-id", 25, 0)
    ).thenReturn(new PagedNews());

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertNotNull(result.get("news"));
  }

  @Test
  public void testExecuteImpl_whenPageAndLimitProvided_thenUsesParameters()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(req.getParameter("page")).thenReturn("3");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("language")).thenReturn("fr");
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
        any()
      )
    ).thenReturn(true);
    when(
      informationApi.groupsIdInformationNewsGet("test-ig-id", 10, 2)
    ).thenReturn(new PagedNews());

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    verify(informationApi).groupsIdInformationNewsGet("test-ig-id", 10, 2);
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

    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("language")).thenReturn(null);
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
        any()
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

    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("language")).thenReturn(null);
    when(
      nodeService.getChildByName(
        eq(igRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenThrow(new InvalidNodeRefException(igRef));

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenUsesZeroOffset()
    throws Exception {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-ig-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("language")).thenReturn(null);
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
        any()
      )
    ).thenReturn(true);
    when(
      informationApi.groupsIdInformationNewsGet("test-ig-id", 25, 0)
    ).thenReturn(new PagedNews());

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    verify(informationApi).groupsIdInformationNewsGet("test-ig-id", 25, 0);
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webScript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsInformationNewsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
