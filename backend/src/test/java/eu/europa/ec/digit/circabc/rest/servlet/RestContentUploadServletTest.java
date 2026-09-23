package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
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

public class RestContentUploadServletTest {

  private RestContentUploadServlet servlet;
  private NodeService nodeService;
  private PermissionService permissionService;
  private AuthenticationService authenticationService;
  private HttpServletRequest req;
  private HttpServletResponse resp;
  private StringWriter responseWriter;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
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

    servlet = new RestContentUploadServlet();

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
  public void testDoPost_whenMalformedUri_thenBadRequest() throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/upload");
    when(req.getHeader("Authorization")).thenReturn(null);

    invokeDoPost();

    verify(resp).sendError(
      HttpServletResponse.SC_BAD_REQUEST,
      "Request URL malformed"
    );
  }

  @Test
  public void testDoPost_whenNodeDoesNotExist_thenNotFound() throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    when(req.getRequestURI()).thenReturn("/rest/upload/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);
    when(nodeService.exists(parentRef)).thenReturn(false);

    invokeDoPost();

    verify(resp).sendError(
      HttpServletResponse.SC_NOT_FOUND,
      "Content unavailable"
    );
  }

  @Test
  public void testDoPost_whenAccessDenied_thenForbidden() throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    when(req.getRequestURI()).thenReturn("/rest/upload/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);
    when(nodeService.exists(parentRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        parentRef,
        PermissionService.CREATE_CHILDREN
      )
    ).thenReturn(AccessStatus.DENIED);

    invokeDoPost();

    verify(resp).sendError(
      HttpServletResponse.SC_FORBIDDEN,
      "Access denied - cannot create file"
    );
  }

  @Test
  public void testDoPost_whenNotFolderType_thenError() throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    when(req.getRequestURI()).thenReturn("/rest/upload/test-id");
    when(req.getHeader("Authorization")).thenReturn(null);
    when(nodeService.exists(parentRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        parentRef,
        PermissionService.CREATE_CHILDREN
      )
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_CONTENT);

    invokeDoPost();

    verify(resp).sendError(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "Destination is not folder type"
    );
  }

  @Test
  public void testDoPost_whenAuthenticationFails_thenUnauthorized()
    throws Exception {
    when(req.getRequestURI()).thenReturn("/rest/upload/test-id");
    when(req.getHeader("Authorization")).thenReturn("BASIC aW52YWxpZA==");
    doThrow(new RuntimeException("bad credentials"))
      .when(authenticationService)
      .validate(anyString());

    invokeDoPost();

    verify(resp).sendError(
      HttpServletResponse.SC_UNAUTHORIZED,
      "Authentication failed"
    );
  }

  @Test
  public void testValidateParent_whenFolderType_thenNoError() throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );

    when(nodeService.exists(parentRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        parentRef,
        PermissionService.CREATE_CHILDREN
      )
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);

    String result = invokeValidateParent(parentRef);

    assertNull(result);
  }

  @Test
  public void testValidateParent_whenInfoNewsType_thenNoError()
    throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );

    when(nodeService.exists(parentRef)).thenReturn(true);
    when(
      permissionService.hasPermission(
        parentRef,
        PermissionService.CREATE_CHILDREN
      )
    ).thenReturn(AccessStatus.ALLOWED);
    when(nodeService.getType(parentRef)).thenReturn(
      CircabcModel.TYPE_INFORMATION_NEWS
    );

    String result = invokeValidateParent(parentRef);

    assertNull(result);
  }

  private void invokeDoPost() throws Exception {
    java.lang.reflect.Method doPost =
      RestContentUploadServlet.class.getDeclaredMethod(
        "doPost",
        HttpServletRequest.class,
        HttpServletResponse.class
      );
    doPost.setAccessible(true);
    doPost.invoke(servlet, req, resp);
  }

  private String invokeValidateParent(NodeRef parentRef) throws Exception {
    java.lang.reflect.Method method =
      RestContentUploadServlet.class.getDeclaredMethod(
        "validateParent",
        NodeRef.class
      );
    method.setAccessible(true);
    return (String) method.invoke(servlet, parentRef);
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
