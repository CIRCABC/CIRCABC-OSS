package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsIdEventsGetTest {

  private GroupsIdEventsGet webscript;
  private EventsApi eventsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String IG_ID = "test-ig-id";
  private static final String EVT_ID = "test-evt-id";

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsIdEventsGet();
    eventsApi = mock(EventsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);

    setField("eventsApi", eventsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", IG_ID);
    org.springframework.extensions.webscripts.Match match =
      new org.springframework.extensions.webscripts.Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsEventItems()
    throws Exception {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      EVT_ID
    );

    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("startDate")).thenReturn("2026-01-01");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");
    when(
      nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        EVT_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    List<EventItem> items = List.of(new EventItem());
    when(
      eventsApi.groupsIdEventsGet(
        eq(IG_ID),
        any(Date.class),
        any(Date.class),
        eq("en")
      )
    ).thenReturn(items);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      EVT_ID
    );

    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("startDate")).thenReturn("2026-01-01");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");
    when(
      nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        EVT_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidStartDate_thenReturnsNotAcceptable()
    throws Exception {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      EVT_ID
    );

    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("startDate")).thenReturn("not-a-date");
    when(req.getParameter("endDate")).thenReturn("2026-01-31");
    when(
      nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        EVT_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullLanguage_thenDefaultsToEnglish()
    throws Exception {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );
    NodeRef evtNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      EVT_ID
    );

    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("startDate")).thenReturn("2026-03-01");
    when(req.getParameter("endDate")).thenReturn("2026-03-31");
    when(
      nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(evtNodeRef);
    when(
      currentUserPermissionCheckerService.hasAnyOfEventPermission(
        EVT_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    List<EventItem> items = List.of(new EventItem());
    when(
      eventsApi.groupsIdEventsGet(
        eq(IG_ID),
        any(Date.class),
        any(Date.class),
        isNull()
      )
    ).thenReturn(items);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdEventsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
