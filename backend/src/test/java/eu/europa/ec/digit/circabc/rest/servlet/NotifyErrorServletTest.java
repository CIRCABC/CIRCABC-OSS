package eu.europa.ec.digit.circabc.rest.servlet;

import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.translation.TranslationDaoService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class NotifyErrorServletTest {

  private NotifyErrorServlet servlet;
  private TranslationDaoService translationDaoService;
  private HttpServletRequest request;

  @Before
  public void setUp() throws Exception {
    servlet = new NotifyErrorServlet();
    translationDaoService = mock(TranslationDaoService.class);
    request = mock(HttpServletRequest.class);

    Field field = AbstractNotifyServlet.class.getDeclaredField(
      "translationDaoService"
    );
    field.setAccessible(true);
    field.set(servlet, translationDaoService);
  }

  @Test
  public void testHandleNotification_whenValidParams_thenSavesErrorResponse() {
    when(request.getParameter("target-languages")).thenReturn("FR");
    when(request.getParameter("error-code")).thenReturn("ERR_001");
    when(request.getParameter("error-message")).thenReturn(
      "Translation failed"
    );

    servlet.handleNotification(request, "req-123", "ext-ref-456");

    verify(translationDaoService).saveErrorResponse(
      "req-123",
      "FR",
      "ERR_001",
      "Translation failed"
    );
  }

  @Test
  public void testHandleNotification_whenNullParams_thenSavesWithNulls() {
    when(request.getParameter("target-languages")).thenReturn(null);
    when(request.getParameter("error-code")).thenReturn(null);
    when(request.getParameter("error-message")).thenReturn(null);

    servlet.handleNotification(request, null, null);

    verify(translationDaoService).saveErrorResponse(null, null, null, null);
  }

  @Test
  public void testHandleNotification_whenDaoThrowsException_thenNoExceptionPropagated() {
    when(request.getParameter("target-languages")).thenReturn("DE");
    when(request.getParameter("error-code")).thenReturn("ERR_500");
    when(request.getParameter("error-message")).thenReturn("Internal error");
    doThrow(new RuntimeException("DB failure"))
      .when(translationDaoService)
      .saveErrorResponse(anyString(), anyString(), anyString(), anyString());

    servlet.handleNotification(request, "req-999", "ext-ref");

    verify(translationDaoService).saveErrorResponse(
      "req-999",
      "DE",
      "ERR_500",
      "Internal error"
    );
  }
}
