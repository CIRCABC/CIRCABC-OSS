package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpLink;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpLinksGetTest {

  private HelpLinksGet helpLinksGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    helpLinksGet = new HelpLinksGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenReturnsLinks() {
    when(req.getParameter("language")).thenReturn(null);
    List<HelpLink> links = new ArrayList<>();
    links.add(new HelpLink());
    when(helpApi.getHelpLinks()).thenReturn(links);

    Map<String, Object> result = helpLinksGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(links, result.get("links"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsLinks() {
    when(req.getParameter("language")).thenReturn("fr");
    List<HelpLink> links = Collections.singletonList(new HelpLink());
    when(helpApi.getHelpLinks()).thenReturn(links);

    Map<String, Object> result = helpLinksGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(links, result.get("links"));
  }

  @Test
  public void testExecuteImpl_whenEmptyList_thenReturnsEmptyLinks() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpLinks()).thenReturn(Collections.emptyList());

    Map<String, Object> result = helpLinksGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("links"));
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpLinks()).thenThrow(new RuntimeException("DB error"));

    Map<String, Object> result = helpLinksGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpLinksGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpLinksGet, value);
  }
}
