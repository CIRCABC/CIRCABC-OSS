package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeDaoService;
import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeService;
import io.swagger.api.AresBridgeApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class AresBridgeServletTest {

  private AresBridgeServlet servlet;
  private AresBridgeDaoService aresBridgeDaoService;
  private AresBridgeApi aresBridgeApi;
  private AresBridgeService aresBridgeService;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter responseWriter;

  @Before
  public void setUp() throws Exception {
    servlet = new AresBridgeServlet();
    aresBridgeDaoService = mock(AresBridgeDaoService.class);
    aresBridgeApi = mock(AresBridgeApi.class);
    aresBridgeService = mock(AresBridgeService.class);

    setField("aresBridgeDaoService", aresBridgeDaoService);
    setField("aresBridgeApi", aresBridgeApi);
    setField("aresBridgeService", aresBridgeService);

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AresBridgeServlet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(servlet, value);
  }

  @Test
  public void testDoPost_whenSave_thenSavesResponse() throws Exception {
    String json =
      "{\"transactionId\":\"tx1\",\"documentId\":\"doc1\",\"saveNumber\":\"s1\",\"registrationNumber\":\"r1\"}";
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn("Mon, 04 May 2026 10:00:00 GMT");
    when(request.getReader()).thenReturn(
      new BufferedReader(new StringReader(json))
    );
    when(
      aresBridgeApi.validateAuthorizationHeader(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(true);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(aresBridgeDaoService).saveResponse(
      "tx1",
      "save",
      "doc1",
      "s1",
      "r1"
    );
  }

  @Test
  public void testDoPost_whenRegister_thenSavesResponse() throws Exception {
    String json =
      "{\"transactionId\":\"tx2\",\"documentId\":\"doc2\",\"saveNumber\":\"s2\",\"registrationNumber\":\"r2\"}";
    when(request.getRequestURI()).thenReturn("/aresbridge/register");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn("Mon, 04 May 2026 10:00:00 GMT");
    when(request.getReader()).thenReturn(
      new BufferedReader(new StringReader(json))
    );
    when(
      aresBridgeApi.validateAuthorizationHeader(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(true);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(aresBridgeDaoService).saveResponse(
      "tx2",
      "register",
      "doc2",
      "s2",
      "r2"
    );
  }

  @Test
  public void testDoPost_whenMissingAuthorizationHeader_thenBadRequest()
    throws Exception {
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn(null);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseWriter.toString().contains("Authorization header"));
  }

  @Test
  public void testDoPost_whenInvalidToken_thenBadRequest() throws Exception {
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
    when(request.getHeader("Date")).thenReturn("Mon, 04 May 2026 10:00:00 GMT");
    when(
      aresBridgeApi.validateAuthorizationHeader(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(false);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseWriter.toString().contains("Ticket is not valid"));
  }

  @Test
  public void testDoPost_whenInvalidPath_thenBadRequest() throws Exception {
    when(request.getRequestURI()).thenReturn("/aresbridge/unknown");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn("Mon, 04 May 2026 10:00:00 GMT");
    when(
      aresBridgeApi.validateAuthorizationHeader(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(true);
    when(request.getReader()).thenReturn(
      new BufferedReader(new StringReader("{}"))
    );

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseWriter.toString().contains("not supported"));
  }

  @Test
  public void testDoPost_whenInvalidContentType_thenBadRequest()
    throws Exception {
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("text/plain");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn("Mon, 04 May 2026 10:00:00 GMT");
    when(
      aresBridgeApi.validateAuthorizationHeader(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(true);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseWriter.toString().contains("application/json"));
  }

  @Test
  public void testDoGet_whenNoQueryString_thenWritesRunningMessage()
    throws Exception {
    when(request.getQueryString()).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/aresbridge/");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("");
    when(request.getServletPath()).thenReturn("/aresbridge");
    when(request.getPathInfo()).thenReturn(null);

    servlet.doGet(request, response);

    verify(response, never()).setStatus(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    );
    assertTrue(
      responseWriter.toString().contains("AresBridgeCallbackServlet is running")
    );
  }

  @Test
  public void testDoGet_whenQueryStringPresent_thenProcessesAndRedirects()
    throws Exception {
    when(request.getQueryString()).thenReturn(
      "token=t1&date=d1&transactionId=tx1&action=save&documentId=doc1&saveNumber=s1&registrationNumber=r1"
    );
    when(request.getRequestURI()).thenReturn("/aresbridge/");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/ctx");
    when(request.getServletPath()).thenReturn("/aresbridge");
    when(request.getPathInfo()).thenReturn(null);
    when(
      aresBridgeApi.validateToken("d1", "t1", "/aresbridge/", "GET")
    ).thenReturn(true);

    servlet.doGet(request, response);

    verify(aresBridgeDaoService).saveResponse(
      "tx1",
      "save",
      "doc1",
      "s1",
      "r1"
    );
    verify(aresBridgeService).process();
    verify(response).sendRedirect("/ctx/ui/welcome");
  }

  @Test
  public void testDoGet_whenInvalidToken_thenDoesNotSave() throws Exception {
    when(request.getQueryString()).thenReturn(
      "token=bad&date=d1&transactionId=tx1&action=save"
    );
    when(request.getRequestURI()).thenReturn("/aresbridge/");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/ctx");
    when(request.getServletPath()).thenReturn("/aresbridge");
    when(request.getPathInfo()).thenReturn(null);
    when(
      aresBridgeApi.validateToken("d1", "bad", "/aresbridge/", "GET")
    ).thenReturn(false);

    servlet.doGet(request, response);

    verify(aresBridgeDaoService, never()).saveResponse(
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testGetURL_whenStandardPort_thenOmitsPort() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("example.com");
    when(request.getServerPort()).thenReturn(80);
    when(request.getContextPath()).thenReturn("/app");
    when(request.getServletPath()).thenReturn("/servlet");
    when(request.getPathInfo()).thenReturn("/info");
    when(request.getQueryString()).thenReturn("key=val");

    String url = AresBridgeServlet.getURL(request);

    assertEquals("http://example.com/app/servlet/info?key=val", url);
  }

  @Test
  public void testGetURL_whenNonStandardPort_thenIncludesPort() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("example.com");
    when(request.getServerPort()).thenReturn(9090);
    when(request.getContextPath()).thenReturn("");
    when(request.getServletPath()).thenReturn("/s");
    when(request.getPathInfo()).thenReturn(null);
    when(request.getQueryString()).thenReturn(null);

    String url = AresBridgeServlet.getURL(request);

    assertEquals("http://example.com:9090/s", url);
  }

  @Test
  public void testDoPost_whenDateHeaderMissing_usesXABDate() throws Exception {
    String json =
      "{\"transactionId\":\"tx1\",\"documentId\":\"doc1\",\"saveNumber\":\"s1\",\"registrationNumber\":\"r1\"}";
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn(null);
    when(request.getHeader("X-AB-Date")).thenReturn(
      "Mon, 04 May 2026 10:00:00 GMT"
    );
    when(request.getReader()).thenReturn(
      new BufferedReader(new StringReader(json))
    );
    when(
      aresBridgeApi.validateAuthorizationHeader(
        eq("Mon, 04 May 2026 10:00:00 GMT"),
        anyString(),
        anyString()
      )
    ).thenReturn(true);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(aresBridgeDaoService).saveResponse(
      "tx1",
      "save",
      "doc1",
      "s1",
      "r1"
    );
  }

  @Test
  public void testDoPost_whenBothDateHeadersMissing_thenBadRequest()
    throws Exception {
    when(request.getRequestURI()).thenReturn("/aresbridge/save");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(request.getHeader("Date")).thenReturn(null);
    when(request.getHeader("X-AB-Date")).thenReturn(null);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    assertTrue(responseWriter.toString().contains("Date header is missing"));
  }
}
