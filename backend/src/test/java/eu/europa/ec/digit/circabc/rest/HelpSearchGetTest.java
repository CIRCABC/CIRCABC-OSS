package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpSearchResult;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpSearchGetTest {

  private HelpSearchGet helpSearchGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpSearchGet = new HelpSearchGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenReturnsSearchResult() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("q")).thenReturn("test query");
    HelpSearchResult searchResult = new HelpSearchResult();
    when(helpApi.searchHelp("test query")).thenReturn(searchResult);

    Map<String, Object> result = helpSearchGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(searchResult, result.get("search"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsSearchResult() {
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("q")).thenReturn("aide");
    HelpSearchResult searchResult = new HelpSearchResult();
    when(helpApi.searchHelp("aide")).thenReturn(searchResult);

    Map<String, Object> result = helpSearchGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(searchResult, result.get("search"));
  }

  @Test
  public void testExecuteImpl_whenNullQuery_thenCallsSearchWithNull() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("q")).thenReturn(null);
    HelpSearchResult searchResult = new HelpSearchResult();
    when(helpApi.searchHelp(null)).thenReturn(searchResult);

    Map<String, Object> result = helpSearchGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(searchResult, result.get("search"));
    verify(helpApi).searchHelp(null);
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("q")).thenReturn("fail");
    when(helpApi.searchHelp("fail")).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpSearchGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpSearchGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpSearchGet, value);
  }
}
