package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class RestContentUpdateServletTest {

  private RestContentUpdateServlet servlet;
  private NodeService nodeService;
  private PermissionService permissionService;
  private AuthenticationService authenticationService;
  private HttpServletRequest req;
  private HttpServletResponse resp;
  private StringWriter responseWriter;

  @Before
  public void setUp() throws Exception {
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
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    servlet = new RestContentUpdateServlet();

    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    authenticationService = mock(AuthenticationService.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("authenticationService", authenticationService);

    req = mock(HttpServletRequest.class);
    resp = mock(HttpServletResponse.class);
    responseWriter = new StringWriter();
    when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  @Test
  public void testDoPost_whenUidMalformed_thenBadRequest() throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/update");

    invokeDoPost();

    verify(resp).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
  }

  @Test
  public void testDoPost_whenNodeDoesNotExist_thenNotFound() throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/update/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);

    NodeRef targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(targetRef)).thenReturn(false);

    invokeDoPost();

    verify(resp).sendError(
      eq(HttpServletResponse.SC_NOT_FOUND),
      contains("unavailable")
    );
  }

  @Test
  public void testDoPost_whenNoWritePermission_thenForbidden()
    throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/update/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);

    NodeRef targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(targetRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        targetRef,
        PermissionService.WRITE_CONTENT
      )
    ).thenReturn(AccessStatus.DENIED);

    invokeDoPost();

    verify(resp).sendError(
      eq(HttpServletResponse.SC_FORBIDDEN),
      contains("denied")
    );
  }

  @Test
  public void testDoPost_whenNotContentType_thenError() throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/update/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);

    NodeRef targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(targetRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        targetRef,
        PermissionService.WRITE_CONTENT
      )
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(targetRef)).thenReturn(
      QName.createQName("http://www.alfresco.org/model/content/1.0", "folder")
    );

    invokeDoPost();

    verify(resp).sendError(
      eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
      contains("not content type")
    );
  }

  @Test
  public void testDoPost_whenAuthenticationFails_thenUnauthorized()
    throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/update/test-id");
    when(req.getHeader("Authorization")).thenReturn("BASIC invalidtoken");
    doThrow(new RuntimeException("auth failed"))
      .when(authenticationService)
      .validate(anyString());

    invokeDoPost();

    verify(resp).sendError(
      eq(HttpServletResponse.SC_UNAUTHORIZED),
      contains("Authentication")
    );
  }

  private void invokeDoPost() throws Exception {
    Method doPost = RestContentUpdateServlet.class.getDeclaredMethod(
      "doPost",
      HttpServletRequest.class,
      HttpServletResponse.class
    );
    doPost.setAccessible(true);
    doPost.invoke(servlet, req, resp);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = findField(servlet.getClass(), fieldName);
    field.setAccessible(true);
    field.set(servlet, value);
  }

  private Field findField(Class<?> clazz, String fieldName)
    throws NoSuchFieldException {
    while (clazz != null) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }
}
