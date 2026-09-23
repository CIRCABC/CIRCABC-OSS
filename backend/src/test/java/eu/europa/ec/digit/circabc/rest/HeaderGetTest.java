package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HeadersApi;
import io.swagger.model.Category;
import io.swagger.model.Header;
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

public class HeaderGetTest {

  private HeaderGet webScript;
  private HeadersApi headerApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new HeaderGet();
    headerApi = mock(HeadersApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("headerApi", headerApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HeaderGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenNoId_thenReturnsAllHeaders() {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("guest")).thenReturn(null);
    List<Header> headers = Collections.singletonList(new Header());
    when(headerApi.getHeaders(null, false)).thenReturn(headers);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(headers, model.get("headers"));
    verify(headerApi).getHeaders(null, false);
  }

  @Test
  public void testExecuteImpl_whenIdAndServicePathEndsWithCategories_thenReturnsCategories() {
    mockTemplateVars("header-id");
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("guest")).thenReturn("true");
    when(req.getServicePath()).thenReturn(
      "/circabc/headers/header-id/categories"
    );
    List<Category> categories = Collections.singletonList(new Category());
    when(headerApi.getCategoriesByHeaderId("header-id", "en", true)).thenReturn(
      categories
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(categories, model.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenIdAndNotCategories_thenReturnsSingleHeader() {
    mockTemplateVars("header-id");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("guest")).thenReturn(null);
    when(req.getServicePath()).thenReturn("/circabc/headers/header-id");
    Header header = new Header();
    when(headerApi.getHeader("header-id")).thenReturn(header);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(header, model.get("header"));
  }

  @Test
  public void testExecuteImpl_whenGuestParamTrue_thenPassesGuestFlag() {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("guest")).thenReturn("true");
    List<Header> headers = Collections.emptyList();
    when(headerApi.getHeaders("fr", true)).thenReturn(headers);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(headerApi).getHeaders("fr", true);
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    mockTemplateVars("bad-id");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("guest")).thenReturn(null);
    when(req.getServicePath()).thenReturn("/circabc/headers/bad-id");
    when(headerApi.getHeader("bad-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("guest")).thenReturn(null);
    when(headerApi.getHeaders(null, false)).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }
}
