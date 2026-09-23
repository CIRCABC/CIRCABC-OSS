package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpCategoriesGetTest {

  private HelpCategoriesGet helpCategoriesGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpCategoriesGet = new HelpCategoriesGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenReturnsCategories() {
    when(req.getParameter("language")).thenReturn(null);
    List<HelpCategory> categories = List.of(new HelpCategory());
    when(helpApi.getHelpCategories()).thenReturn(categories);

    Map<String, Object> result = helpCategoriesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(categories, result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsCategories() {
    when(req.getParameter("language")).thenReturn("fr");
    List<HelpCategory> categories = List.of(new HelpCategory());
    when(helpApi.getHelpCategories()).thenReturn(categories);

    Map<String, Object> result = helpCategoriesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertSame(categories, result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenEmptyCategories_thenReturnsEmptyList() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpCategories()).thenReturn(Collections.emptyList());

    Map<String, Object> result = helpCategoriesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpCategories()).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpCategoriesGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
    assertTrue(status.getRedirect());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpCategoriesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpCategoriesGet, value);
  }
}
