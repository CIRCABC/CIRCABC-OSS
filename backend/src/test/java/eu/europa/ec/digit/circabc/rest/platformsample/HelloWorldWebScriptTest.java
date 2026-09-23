package eu.europa.ec.digit.circabc.rest.platformsample;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelloWorldWebScriptTest {

  private HelloWorldWebScript webScript;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() {
    webScript = new HelloWorldWebScript();
    req = mock(WebScriptRequest.class);
    status = mock(Status.class);
    cache = mock(Cache.class);
  }

  @Test
  public void testExecuteImpl_returnsModelWithFromJavaKey() {
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("HelloFromJava", model.get("fromJava"));
  }

  @Test
  public void testExecuteImpl_modelContainsExactlyOneEntry() {
    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertEquals(1, model.size());
  }
}
