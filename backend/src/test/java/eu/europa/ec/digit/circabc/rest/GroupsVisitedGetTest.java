package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.InterestGroup;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsVisitedGetTest {

  private GroupsVisitedGet webScript;
  private GroupsApi groupsApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsVisitedGet();
    groupsApi = mock(GroupsApi.class);

    Field field = GroupsVisitedGet.class.getDeclaredField("groupsApi");
    field.setAccessible(true);
    field.set(webScript, groupsApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenAmountProvided_thenReturnsGroups() {
    when(req.getParameter("amount")).thenReturn("5");
    InterestGroup ig = new InterestGroup();
    when(groupsApi.getVisitedGroups(5)).thenReturn(List.of(ig));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(List.of(ig), model.get("groups"));
    verify(groupsApi).getVisitedGroups(5);
  }

  @Test
  public void testExecuteImpl_whenAmountNull_thenUsesZero() {
    when(req.getParameter("amount")).thenReturn(null);
    when(groupsApi.getVisitedGroups(0)).thenReturn(Collections.emptyList());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).getVisitedGroups(0);
  }

  @Test
  public void testExecuteImpl_whenAmountEmpty_thenUsesZero() {
    when(req.getParameter("amount")).thenReturn("");
    when(groupsApi.getVisitedGroups(0)).thenReturn(Collections.emptyList());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).getVisitedGroups(0);
  }

  @Test
  public void testExecuteImpl_whenAmountNegative_thenUsesZero() {
    when(req.getParameter("amount")).thenReturn("-3");
    when(groupsApi.getVisitedGroups(0)).thenReturn(Collections.emptyList());

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(groupsApi).getVisitedGroups(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenAmountNotNumeric_thenThrows() {
    when(req.getParameter("amount")).thenReturn("abc");

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("amount")).thenReturn("5");
    when(groupsApi.getVisitedGroups(5)).thenThrow(
      new org.alfresco.repo.security.permissions.AccessDeniedException("denied")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("amount")).thenReturn("5");
    when(groupsApi.getVisitedGroups(5)).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "bad",
        new org.alfresco.service.cmr.repository.NodeRef(
          "workspace://SpacesStore/test-id"
        )
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
