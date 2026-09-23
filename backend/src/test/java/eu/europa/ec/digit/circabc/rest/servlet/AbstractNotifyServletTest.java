package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.translation.TranslationDaoService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;

public class AbstractNotifyServletTest {

  private TestNotifyServlet servlet;
  private TranslationDaoService translationDaoService;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletConfig servletConfig;
  private ServletContext servletContext;
  private WebApplicationContext webApplicationContext;

  private String capturedRequestId;
  private String capturedExternalReference;

  @Before
  public void setUp() throws Exception {
    translationDaoService = mock(TranslationDaoService.class);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    servletConfig = mock(ServletConfig.class);
    servletContext = mock(ServletContext.class);
    webApplicationContext = mock(WebApplicationContext.class);

    servlet = new TestNotifyServlet();

    capturedRequestId = null;
    capturedExternalReference = null;
  }

  @Test
  public void testDoPost_whenTranslationServiceSet_thenExtractsParameters()
    throws Exception {
    setField("translationDaoService", translationDaoService);
    when(request.getParameter("external-reference")).thenReturn("ext-ref-123");
    when(request.getParameter("requestId")).thenReturn("req-456");

    servlet.doPost(request, response);

    verify(request).setCharacterEncoding("UTF-8");
    assertEquals("req-456", capturedRequestId);
    assertEquals("ext-ref-123", capturedExternalReference);
  }

  @Test
  public void testDoPost_whenRequestIdNull_thenFallsBackToRequestDash()
    throws Exception {
    setField("translationDaoService", translationDaoService);
    when(request.getParameter("external-reference")).thenReturn("ext-ref");
    when(request.getParameter("requestId")).thenReturn(null);
    when(request.getParameter("request-id")).thenReturn("fallback-id");

    servlet.doPost(request, response);

    assertEquals("fallback-id", capturedRequestId);
    assertEquals("ext-ref", capturedExternalReference);
  }

  @Test
  public void testDoPost_whenBothRequestIdParamsNull_thenRequestIdIsNull()
    throws Exception {
    setField("translationDaoService", translationDaoService);
    when(request.getParameter("external-reference")).thenReturn(null);
    when(request.getParameter("requestId")).thenReturn(null);
    when(request.getParameter("request-id")).thenReturn(null);

    servlet.doPost(request, response);

    assertNull(capturedRequestId);
    assertNull(capturedExternalReference);
  }

  @Test
  public void testDoPost_whenTranslationServiceNull_thenCallsInit()
    throws Exception {
    // translationDaoService is null, so doPost should call init(getServletConfig())
    // We need to set up the servlet with a config that provides the context
    when(servletConfig.getServletContext()).thenReturn(servletContext);
    when(
      servletContext.getAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
      )
    ).thenReturn(webApplicationContext);
    when(webApplicationContext.getBean("translationDaoService")).thenReturn(
      translationDaoService
    );

    // Set the servletConfig on the servlet so getServletConfig() returns it
    Field configField = findField(servlet.getClass(), "config");
    configField.setAccessible(true);
    configField.set(servlet, servletConfig);

    when(request.getParameter("external-reference")).thenReturn("ref");
    when(request.getParameter("requestId")).thenReturn("id");

    servlet.doPost(request, response);

    assertEquals("id", capturedRequestId);
    assertEquals("ref", capturedExternalReference);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AbstractNotifyServlet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(servlet, value);
  }

  private Field findField(Class<?> clazz, String fieldName) {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new RuntimeException("Field not found: " + fieldName);
  }

  private class TestNotifyServlet extends AbstractNotifyServlet {

    @Override
    protected Log getLogger() {
      return mock(Log.class);
    }

    @Override
    protected void handleNotification(
      HttpServletRequest request,
      String requestId,
      String externalReference
    ) {
      capturedRequestId = requestId;
      capturedExternalReference = externalReference;
    }
  }
}
