package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EventsApi;
import io.swagger.model.AppointmentType;
import io.swagger.model.EventItem;
import io.swagger.model.PagedEventItems;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GroupsIdEventsExportGetTest {

  private GroupsIdEventsExportGet webscript;
  private EventsApi eventsApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ApiToolBox apiToolBox;
  private WebScriptRequest req;
  private WebScriptResponse res;

  private static final String IG_ID = "test-ig-id";
  private static final NodeRef GROUP_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    IG_ID
  );
  private static final NodeRef EVENTS_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "events-id"
  );

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

    webscript = new GroupsIdEventsExportGet();
    eventsApi = mock(EventsApi.class);
    nodeService = mock(NodeService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("eventsApi", eventsApi);
    setField("nodeService", nodeService);
    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("apiToolBox", apiToolBox);

    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", IG_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    when(
      nodeService.getChildByName(
        GROUP_REF,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(EVENTS_REF);
  }

  @Test
  public void testExecute_whenValidCsvExport_thenWritesCsv() throws Exception {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("exactDate")).thenReturn(null);
    when(req.getParameter("format")).thenReturn("csv");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_REF.getId(),
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    PagedEventItems pagedItems = new PagedEventItems(
      Collections.emptyList(),
      0
    );
    when(
      eventsApi.groupsIdEventsListGet(
        eq(IG_ID),
        eq("Future"),
        (Date) isNull(),
        eq(1),
        eq(0),
        isNull()
      )
    ).thenReturn(pagedItems);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);

    webscript.execute(req, res);

    verify(res).setContentType("text/csv;charset=UTF-8");
    verify(res).setHeader(
      eq("Content-Disposition"),
      eq("attachment;filename=Events.csv")
    );
    String output = baos.toString("UTF-8");
    assertTrue(output.contains("contact,interest group"));
  }

  @Test
  public void testExecute_whenAccessDenied_thenSetsForbiddenStatus()
    throws Exception {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("format")).thenReturn("csv");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_REF.getId(),
        EventPermissions.EVEACCESS
      )
    ).thenReturn(false);

    webscript.execute(req, res);

    verify(res).setStatus(403);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenInvalidFilter_thenThrowsIOException()
    throws Exception {
    when(req.getParameter("filter")).thenReturn("InvalidFilter");
    when(req.getParameter("format")).thenReturn("csv");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_REF.getId(),
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    webscript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenInvalidFormat_thenThrowsIOException()
    throws Exception {
    when(req.getParameter("filter")).thenReturn("Future");
    when(req.getParameter("format")).thenReturn("pdf");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_REF.getId(),
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    webscript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenInvalidExactDate_thenThrowsIOException()
    throws Exception {
    when(req.getParameter("filter")).thenReturn("Exact");
    when(req.getParameter("exactDate")).thenReturn("not-a-date");
    when(req.getParameter("format")).thenReturn("csv");
    when(
      permissionChecker.hasAnyOfEventPermission(
        EVENTS_REF.getId(),
        EventPermissions.EVEACCESS
      )
    ).thenReturn(true);

    webscript.execute(req, res);
  }

  @Test
  public void testExport_whenXlsWithEvents_thenWritesXls() throws Exception {
    EventItem item = createEventItem();
    NodeRef eventNodeRef = item.getEventNodeRef();
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    NodeRef definitionRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "definition-id"
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(containerRef);
    when(nodeService.getParentAssocs(eventNodeRef)).thenReturn(
      List.of(childAssoc)
    );

    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getParentRef()).thenReturn(definitionRef);
    when(nodeService.getParentAssocs(containerRef)).thenReturn(
      List.of(containerAssoc)
    );

    when(nodeService.getProperties(definitionRef)).thenReturn(new HashMap<>());
    when(apiToolBox.getOccurenceAsString(null)).thenReturn("");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);

    webscript.export(List.of(item), "xls", res);

    verify(res).setContentType("application/vnd.ms-excel;charset=UTF-8");
    assertTrue(baos.size() > 0);
  }

  private EventItem createEventItem() {
    EventItem item = new EventItem();
    item.setEventNodeRef(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "event-node-id")
    );
    item.setContact("John");
    item.setInterestGroup("TestIG");
    item.setInterestGroupTitle("Test Interest Group");
    item.setTitle("Test Event");
    item.setDate(new Date());
    item.setStartTime(new LocalTime(10, 0));
    item.setEndTime(new LocalTime(11, 0));
    item.setEventType(AppointmentType.Meeting);
    return item;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdEventsExportGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
