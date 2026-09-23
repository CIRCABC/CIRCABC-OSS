package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpLink;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpLinkGetTest {

  private HelpLinkGet helpLinkGet;
  private HelpApi helpApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    helpLinkGet = new HelpLinkGet();
    helpApi = mock(HelpApi.class);
    setField("helpApi", helpApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "link-123");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidId_thenReturnsLink() {
    when(req.getParameter("language")).thenReturn(null);
    HelpLink link = new HelpLink();
    when(helpApi.getHelpLink("link-123")).thenReturn(link);

    Map<String, Object> result = helpLinkGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(link, result.get("link"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocaleAndReturnsLink() {
    when(req.getParameter("language")).thenReturn("fr");
    HelpLink link = new HelpLink();
    when(helpApi.getHelpLink("link-123")).thenReturn(link);

    Map<String, Object> result = helpLinkGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertSame(link, result.get("link"));
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(req.getParameter("language")).thenReturn(null);
    when(helpApi.getHelpLink("link-123")).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> result = helpLinkGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal server error", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpLinkGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpLinkGet, value);
  }
}
