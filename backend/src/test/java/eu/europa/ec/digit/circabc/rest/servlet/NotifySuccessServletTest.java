package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.translation.TranslationDaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class NotifySuccessServletTest {

  private NotifySuccessServlet servlet;
  private TranslationDaoService translationDaoService;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Before
  public void setUp() throws Exception {
    servlet = new NotifySuccessServlet();
    translationDaoService = mock(TranslationDaoService.class);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);

    Field field = AbstractNotifyServlet.class.getDeclaredField(
      "translationDaoService"
    );
    field.setAccessible(true);
    field.set(servlet, translationDaoService);
  }

  @Test
  public void testDoPost_whenValidParameters_thenSavesSuccessResponse()
    throws Exception {
    when(request.getParameter("external-reference")).thenReturn("ext-ref-123");
    when(request.getParameter("requestId")).thenReturn("req-001");
    when(request.getParameter("target-language")).thenReturn("FR");
    when(request.getParameter("translated-text")).thenReturn("Bonjour");

    servlet.doPost(request, response);

    verify(request).setCharacterEncoding("UTF-8");
    verify(translationDaoService).saveSuccessResponse(
      "req-001",
      "FR",
      null,
      "Bonjour"
    );
  }

  @Test
  public void testDoPost_whenRequestIdNull_thenFallsBackToRequestDash()
    throws Exception {
    when(request.getParameter("external-reference")).thenReturn("ext-ref");
    when(request.getParameter("requestId")).thenReturn(null);
    when(request.getParameter("request-id")).thenReturn("req-fallback");
    when(request.getParameter("target-language")).thenReturn("DE");
    when(request.getParameter("translated-text")).thenReturn("Hallo");

    servlet.doPost(request, response);

    verify(translationDaoService).saveSuccessResponse(
      "req-fallback",
      "DE",
      null,
      "Hallo"
    );
  }

  @Test
  public void testDoPost_whenNullParameters_thenSavesWithNulls()
    throws Exception {
    when(request.getParameter("external-reference")).thenReturn(null);
    when(request.getParameter("requestId")).thenReturn(null);
    when(request.getParameter("request-id")).thenReturn(null);
    when(request.getParameter("target-language")).thenReturn(null);
    when(request.getParameter("translated-text")).thenReturn(null);

    servlet.doPost(request, response);

    verify(translationDaoService).saveSuccessResponse(null, null, null, null);
  }

  @Test
  public void testDoPost_whenDaoThrowsException_thenDoesNotPropagate()
    throws Exception {
    when(request.getParameter("external-reference")).thenReturn("ext");
    when(request.getParameter("requestId")).thenReturn("req-err");
    when(request.getParameter("target-language")).thenReturn("EN");
    when(request.getParameter("translated-text")).thenReturn("Hello");
    doThrow(new RuntimeException("DB error"))
      .when(translationDaoService)
      .saveSuccessResponse("req-err", "EN", null, "Hello");

    servlet.doPost(request, response);

    verify(translationDaoService).saveSuccessResponse(
      "req-err",
      "EN",
      null,
      "Hello"
    );
  }

  @Test
  public void testGetLogger_returnsNonNull() {
    assertNotNull(servlet.getLogger());
  }
}
