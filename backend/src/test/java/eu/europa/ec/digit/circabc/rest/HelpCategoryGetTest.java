package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoryGetTest {

  private HelpCategoryGet helpCategoryGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    helpCategoryGet = new HelpCategoryGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "category-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidId_thenReturnsCategory() {
    when(req.getParameter("language")).thenReturn(null);
    HelpCategory category = new HelpCategory();
    when(helpApi.getHelpCategory("category-123")).thenReturn(category);

    Map<String, Object> result = helpCategoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(category, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocaleAndReturnsCategory() {
    when(req.getParameter("language")).thenReturn("fr");
    HelpCategory category = new HelpCategory();
    when(helpApi.getHelpCategory("category-123")).thenReturn(category);

    Map<String, Object> result = helpCategoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(category, result.get("category"));
  }

  @Test
  public void testExecuteImpl_whenEmptyId_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    templateVars.put("id", "");

    Map<String, Object> result = helpCategoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Help category ID cannot be empty", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpCategory("category-123")).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpCategoryGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoryGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpCategoryGet, value);
  }
}
