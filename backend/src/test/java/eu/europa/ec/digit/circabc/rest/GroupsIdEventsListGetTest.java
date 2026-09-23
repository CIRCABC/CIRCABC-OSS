package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.model.PagedEventItems;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.*;

public class GroupsIdEventsListGetTest {

  private GroupsIdEventsListGet webscript;
  private EventsApi eventsApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String IG_ID = "test-ig-id";
  private static final String EVENTS_NODE_ID = "events-node-id";

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

    webscript = new GroupsIdEventsListGet();
    eventsApi = mock(EventsApi.class);
    nodeService = mock(NodeService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("eventsApi", eventsApi);
    setField("nodeService", nodeService);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", IG_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );
    NodeRef eventsNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      EVENTS_NODE_ID
    );
    when(
      nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(eventsNodeRef);

    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_NODE_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdEventsListGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, field.getType().cast(value));
  }

  @Test
  public void testExecuteImpl_whenFutureFilter_thenReturnsEvents() {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn("asc");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    List<EventItem> items = Collections.singletonList(new EventItem());
    PagedEventItems pagedResult = new PagedEventItems(items, 1L);
    when(
      eventsApi.groupsIdEventsListGet(IG_ID, "Future", null, 0, 10, "asc")
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenExactFilterWithDate_thenReturnsEvents() {
    when(req.getParameter("filter")).thenReturn("Exact");
    when(req.getParameter("exactDate")).thenReturn("2026-05-04");
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("5");

    List<EventItem> items = Collections.emptyList();
    PagedEventItems pagedResult = new PagedEventItems(items, 0L);
    when(
      eventsApi.groupsIdEventsListGet(
        eq(IG_ID),
        eq("Exact"),
        any(),
        eq(0),
        eq(5),
        isNull()
      )
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_NODE_ID,
        EventPermissions.EVEACCESS
      )
    ).thenReturn(false);

    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidFilter_thenReturnsError() {
    when(req.getParameter("filter")).thenReturn("InvalidFilter");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenReturnsError() {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidPageFormat_thenReturnsError() {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidExactDate_thenReturnsError() {
    when(req.getParameter("filter")).thenReturn("Exact");
    when(req.getParameter("exactDate")).thenReturn("not-a-date");
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageNotProvided_thenDefaultsToOne() {
    when(req.getParameter("filter")).thenReturn("Previous");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("10");

    List<EventItem> items = Collections.emptyList();
    PagedEventItems pagedResult = new PagedEventItems(items, 0L);
    when(
      eventsApi.groupsIdEventsListGet(IG_ID, "Previous", null, 0, 10, null)
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(items, model.get("eventItems"));
  }

  @Test
  public void testExecuteImpl_whenNegativeLimit_thenTreatedAsZero() {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("sort")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-5");

    List<EventItem> items = Collections.emptyList();
    PagedEventItems pagedResult = new PagedEventItems(items, 0L);
    when(
      eventsApi.groupsIdEventsListGet(IG_ID, "Future", null, 0, 0, null)
    ).thenReturn(pagedResult);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
  }
}
