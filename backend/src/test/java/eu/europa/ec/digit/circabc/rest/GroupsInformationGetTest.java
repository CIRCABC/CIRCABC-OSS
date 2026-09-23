package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.model.InformationPage;
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

public class GroupsInformationGetTest {

  private GroupsInformationGet webScript;
  private InformationApi informationApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String IG_ID = "test-ig-id";
  private static final String INF_NODE_ID = "inf-node-id";

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsInformationGet();
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
    templateVars.put("igId", IG_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsInformation()
    throws Exception {
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      INF_NODE_ID
    );
    when(
      nodeService.getChildByName(
        any(NodeRef.class),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq(INF_NODE_ID),
        any()
      )
    ).thenReturn(true);

    InformationPage page = new InformationPage();
    when(informationApi.groupsIdInformationGet(IG_ID)).thenReturn(page);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(page, result.get("information"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      INF_NODE_ID
    );
    when(
      nodeService.getChildByName(
        any(NodeRef.class),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenReturn(infRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfInformationPermission(
        eq(INF_NODE_ID),
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
    when(
      nodeService.getChildByName(
        any(NodeRef.class),
        eq(ContentModel.ASSOC_CONTAINS),
        eq("Information")
      )
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, IG_ID)
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      GroupsInformationGet.class.getSuperclass().getDeclaredMethod(
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
    Field field = GroupsInformationGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
