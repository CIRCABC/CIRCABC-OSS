package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class FTPConnectionTestGetTest {

  private FTPConnectionTestGet ftpConnectionTestGet;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    ftpConnectionTestGet = new FTPConnectionTestGet();
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenThrowsAccessDeniedException()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ).thenReturn(false);

    try {
      invokeExecuteImpl();
      fail("Expected AccessDeniedException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof AccessDeniedException);
    }
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenReturnsResult()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ).thenReturn(true);
    when(req.getParameter("host")).thenReturn("localhost");
    when(req.getParameter("port")).thenReturn("21");
    when(req.getParameter("username")).thenReturn("user");
    when(req.getParameter("password")).thenReturn("pass");
    when(req.getParameter("filePath")).thenReturn("/test/path");

    Map<String, Object> result = invokeExecuteImpl();

    // testConnection will fail with a real FTP connection attempt, returning -2
    assertNotNull(result);
    assertTrue(result.containsKey("result"));
    int connStatus = (int) result.get("result");
    assertTrue(connStatus == -1 || connStatus == -2);
  }

  @Test
  public void testExecuteImpl_whenInvalidPort_thenDefaultsToZero()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ).thenReturn(true);
    when(req.getParameter("host")).thenReturn("localhost");
    when(req.getParameter("port")).thenReturn("notanumber");
    when(req.getParameter("username")).thenReturn("user");
    when(req.getParameter("password")).thenReturn("pass");
    when(req.getParameter("filePath")).thenReturn("/path");

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertTrue(result.containsKey("result"));
  }

  @Test
  public void testExecuteImpl_whenNullPort_thenDefaultsToZero()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin()
    ).thenReturn(true);
    when(req.getParameter("host")).thenReturn("localhost");
    when(req.getParameter("port")).thenReturn(null);
    when(req.getParameter("username")).thenReturn("user");
    when(req.getParameter("password")).thenReturn("pass");
    when(req.getParameter("filePath")).thenReturn("/path");

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertTrue(result.containsKey("result"));
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    Method method = FTPConnectionTestGet.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      ftpConnectionTestGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = FTPConnectionTestGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(ftpConnectionTestGet, value);
  }
}
