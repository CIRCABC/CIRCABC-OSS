package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class RestrictedSpacesIdGetTest {

  private RestrictedSpacesIdGet webscript;
  private SpacesApi spacesApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new RestrictedSpacesIdGet();
    spacesApi = mock(SpacesApi.class);

    Field field = RestrictedSpacesIdGet.class.getDeclaredField("spacesApi");
    field.setAccessible(true);
    field.set(webscript, spacesApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHappyPath_thenReturnsDataAndTotal()
    throws Exception {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);

    Node node = new Node();
    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(List.of(node));
    pagedNodes.setTotal(1L);

    when(
      spacesApi.restrictedSpaceGetChildren(
        "test-node-id",
        1,
        10,
        null,
        false,
        false
      )
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(List.of(node), model.get("data"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNullPageAndLimit_thenUsesMinusOne()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn("name_ASC");
    when(req.getParameter("folderOnly")).thenReturn("true");
    when(req.getParameter("fileOnly")).thenReturn("false");

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(Collections.emptyList());
    pagedNodes.setTotal(0L);

    when(
      spacesApi.restrictedSpaceGetChildren(
        "test-node-id",
        -1,
        -1,
        "name_ASC",
        true,
        false
      )
    ).thenReturn(pagedNodes);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("data"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);

    when(
      spacesApi.restrictedSpaceGetChildren(
        "test-node-id",
        -1,
        -1,
        null,
        false,
        false
      )
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "bad-id"
    );
    when(
      spacesApi.restrictedSpaceGetChildren(
        "test-node-id",
        -1,
        -1,
        null,
        false,
        false
      )
    ).thenThrow(new InvalidNodeRefException(nodeRef));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("folderOnly")).thenReturn(null);
    when(req.getParameter("fileOnly")).thenReturn(null);

    when(
      spacesApi.restrictedSpaceGetChildren(
        "test-node-id",
        -1,
        -1,
        null,
        false,
        false
      )
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
