package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.eulogin.EULoginService;
import eu.europa.ec.digit.circabc.rest.service.eulogin.EULoginUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apereo.cas.client.validation.TicketValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

public class EULoginGetTest {

  private EULoginGet euLoginGet;
  private EULoginService euLoginService;
  private TransactionService transactionService;
  private RetryingTransactionHelper txHelper;
  private WebScriptRequest req;
  private WebScriptServletResponse res;
  private HttpServletResponse httpResponse;
  private StringWriter responseWriter;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil for runAsSystem to work
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    Field system = AuthenticationUtil.class.getDeclaredField(
      "defaultAdminUserName"
    );
    system.setAccessible(true);
    system.set(null, "admin");
    AuthenticationUtil.setFullyAuthenticatedUser("admin");

    euLoginGet = new EULoginGet();
    euLoginService = mock(EULoginService.class);
    transactionService = mock(TransactionService.class);
    txHelper = mock(RetryingTransactionHelper.class);

    // Make txHelper execute the callback immediately (simulates transaction)
    when(transactionService.getRetryingTransactionHelper()).thenReturn(
      txHelper
    );
    when(
      txHelper.doInTransaction(any(), anyBoolean(), anyBoolean())
    ).thenAnswer(invocation -> {
      RetryingTransactionCallback<?> callback = invocation.getArgument(0);
      return callback.execute();
    });

    setField("euLoginService", euLoginService);
    setField("transactionService", transactionService);

    req = mock(WebScriptRequest.class);
    res = mock(WebScriptServletResponse.class);
    httpResponse = mock(HttpServletResponse.class);
    responseWriter = new StringWriter();

    when(res.getHttpServletResponse()).thenReturn(httpResponse);
    when(httpResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  // --- Redirect to ECAS tests ---

  @Test
  public void testExecute_whenNoTicket_thenRedirectsToEcas() throws Exception {
    when(req.getParameter("ticket")).thenReturn(null);
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildLoginRedirectUrl(null)).thenReturn(
      "https://ecas.ec.europa.eu/cas/login?service=http%3A%2F%2Flocalhost"
    );

    euLoginGet.execute(req, res);

    verify(httpResponse).sendRedirect(
      "https://ecas.ec.europa.eu/cas/login?service=http%3A%2F%2Flocalhost"
    );
    verify(euLoginService).buildLoginRedirectUrl(null);
  }

  @Test
  public void testExecute_whenEmptyTicket_thenRedirectsToEcas()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("");
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildLoginRedirectUrl(null)).thenReturn(
      "https://ecas.ec.europa.eu/cas/login?service=encoded"
    );

    euLoginGet.execute(req, res);

    verify(httpResponse).sendRedirect(contains("ecas.ec.europa.eu"));
    verify(euLoginService).buildLoginRedirectUrl(null);
  }

  @Test
  public void testExecute_whenNoTicketWithRoute_thenRedirectsToEcasWithRoute()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn(null);
    when(req.getParameter("route")).thenReturn("/ui/group/123");
    when(euLoginService.buildLoginRedirectUrl("/ui/group/123")).thenReturn(
      "https://ecas.ec.europa.eu/cas/login?service=encoded-with-route"
    );

    euLoginGet.execute(req, res);

    verify(euLoginService).buildLoginRedirectUrl("/ui/group/123");
    verify(httpResponse).sendRedirect(
      "https://ecas.ec.europa.eu/cas/login?service=encoded-with-route"
    );
  }

  // --- Successful authentication tests ---

  @Test
  public void testExecute_whenValidTicket_thenValidatesAndRedirects()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("ST-validticket");
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildServiceUrl(null)).thenReturn(
      "http://localhost:8080/service"
    );
    EULoginUserDetails userDetails = new EULoginUserDetails("johndoe");
    when(
      euLoginService.validateCasTicket(
        "ST-validticket",
        "http://localhost:8080/service"
      )
    ).thenReturn(userDetails);
    when(euLoginService.generateAlfrescoTicket("johndoe")).thenReturn(
      "TICKET_abc123"
    );
    when(euLoginService.buildRedirectTarget(null)).thenReturn(
      "http://localhost:4200/ui/welcome"
    );

    euLoginGet.execute(req, res);

    verify(euLoginService).ensureUserExists("johndoe", userDetails);
    verify(euLoginService).generateAlfrescoTicket("johndoe");
    verify(euLoginService).setCookies(
      httpResponse,
      "johndoe",
      "TICKET_abc123",
      null
    );
    verify(httpResponse).sendRedirect("http://localhost:4200/ui/welcome");
  }

  @Test
  public void testExecute_whenValidTicketWithRoute_thenRedirectsToRoute()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("ST-validticket");
    when(req.getParameter("route")).thenReturn("/group/456/library");
    when(euLoginService.buildServiceUrl("/group/456/library")).thenReturn(
      "http://localhost:8080/service?route=..."
    );
    EULoginUserDetails userDetails = new EULoginUserDetails("johndoe");
    when(
      euLoginService.validateCasTicket(
        "ST-validticket",
        "http://localhost:8080/service?route=..."
      )
    ).thenReturn(userDetails);
    when(euLoginService.generateAlfrescoTicket("johndoe")).thenReturn(
      "TICKET_xyz"
    );
    when(euLoginService.buildRedirectTarget("/group/456/library")).thenReturn(
      "http://localhost:4200/group/456/library"
    );

    euLoginGet.execute(req, res);

    verify(euLoginService).setCookies(
      httpResponse,
      "johndoe",
      "TICKET_xyz",
      "/group/456/library"
    );
    verify(httpResponse).sendRedirect(
      "http://localhost:4200/group/456/library"
    );
  }

  // --- Transaction usage tests ---

  @Test
  public void testExecute_whenValidTicket_thenUsesTransactions()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("ST-validticket");
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildServiceUrl(null)).thenReturn("http://service");
    EULoginUserDetails userDetails = new EULoginUserDetails("johndoe");
    when(
      euLoginService.validateCasTicket("ST-validticket", "http://service")
    ).thenReturn(userDetails);
    when(euLoginService.generateAlfrescoTicket("johndoe")).thenReturn(
      "TICKET_abc"
    );
    when(euLoginService.buildRedirectTarget(null)).thenReturn(
      "http://frontend"
    );

    euLoginGet.execute(req, res);

    verify(transactionService).getRetryingTransactionHelper();
    // Two transaction calls: one for ensureUserExists, one for generateAlfrescoTicket
    verify(txHelper, times(2)).doInTransaction(any(), eq(false), eq(true));
  }

  // --- Error handling tests ---

  @Test
  public void testExecute_whenTicketValidationFails_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("ST-badticket");
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildServiceUrl(null)).thenReturn("http://service");
    when(
      euLoginService.validateCasTicket("ST-badticket", "http://service")
    ).thenThrow(new TicketValidationException("Invalid ticket"));

    euLoginGet.execute(req, res);

    verify(httpResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(httpResponse).setContentType("text/plain;charset=UTF-8");
    String body = responseWriter.toString();
    assertTrue(body.contains("CAS ticket validation error"));
  }

  @Test
  public void testExecute_whenUnexpectedError_thenReturnsInternalServerError()
    throws Exception {
    when(req.getParameter("ticket")).thenReturn("ST-validticket");
    when(req.getParameter("route")).thenReturn(null);
    when(euLoginService.buildServiceUrl(null)).thenReturn("http://service");
    EULoginUserDetails userDetails = new EULoginUserDetails("johndoe");
    when(
      euLoginService.validateCasTicket("ST-validticket", "http://service")
    ).thenReturn(userDetails);
    doThrow(new RuntimeException("DB down"))
      .when(euLoginService)
      .ensureUserExists(eq("johndoe"), any(EULoginUserDetails.class));

    euLoginGet.execute(req, res);

    verify(httpResponse).setStatus(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    );
    verify(httpResponse).setContentType("text/plain;charset=UTF-8");
    String body = responseWriter.toString();
    assertTrue(body.contains("internal error"));
  }

  // --- WebScriptResponse type check ---

  @Test
  public void testExecute_whenNotServletResponse_thenThrowsWebScriptException() {
    WebScriptResponse nonServletRes = mock(WebScriptResponse.class);
    when(req.getParameter("ticket")).thenReturn(null);

    try {
      euLoginGet.execute(req, nonServletRes);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("Cannot access HttpServletResponse"));
    }
  }

  // --- Helper ---

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EULoginGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(euLoginGet, value);
  }
}
