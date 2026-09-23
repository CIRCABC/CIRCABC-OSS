package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class RestContentDownloadServletTest {

  private RestContentDownloadServlet servlet;
  private NodeService nodeService;
  private ContentService contentService;
  private PermissionService permissionService;
  private AuthenticationService authenticationService;
  private eu.europa.ec.digit.circabc.rest.service.log.LogService logService;
  private ApiToolBox apiToolBox;
  private HttpServletRequest request;
  private HttpServletResponse response;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  @Before
  public void setUp() throws Exception {
    servlet = new RestContentDownloadServlet();
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    permissionService = mock(PermissionService.class);
    authenticationService = mock(AuthenticationService.class);
    logService = mock(
      eu.europa.ec.digit.circabc.rest.service.log.LogService.class
    );
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("permissionService", permissionService);
    setField("authenticationService", authenticationService);
    setField("logService", logService);
    setField("apiToolBox", apiToolBox);

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
  }

  @Test
  public void testDoGet_whenValidNode_thenStreamsContent() throws Exception {
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.getType(TEST_NODE_REF)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      permissionService.hasPermission(TEST_NODE_REF, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NAME)
    ).thenReturn("test-file.pdf");

    ContentReader reader = mock(ContentReader.class);
    when(
      contentService.getReader(TEST_NODE_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(reader);
    when(reader.getMimetype()).thenReturn("application/pdf");
    byte[] content = "file-content".getBytes(StandardCharsets.UTF_8);
    when(reader.getContentInputStream()).thenReturn(
      new ByteArrayInputStream(content)
    );

    ServletOutputStream outputStream = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(outputStream);

    when(apiToolBox.getCurrentInterestGroup(TEST_NODE_REF)).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).setContentType("application/pdf");
    verify(response).setHeader(
      "Content-Disposition",
      "attachment; filename=\"test-file.pdf\""
    );
    verify(logService).log(any());
  }

  @Test
  public void testDoGet_whenMissingPathInfo_thenBadRequest() throws Exception {
    when(request.getPathInfo()).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_BAD_REQUEST),
      eq("Missing or invalid nodeRef parameter.")
    );
  }

  @Test
  public void testDoGet_whenEmptyPathInfo_thenBadRequest() throws Exception {
    when(request.getPathInfo()).thenReturn("");
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_BAD_REQUEST),
      eq("Missing or invalid nodeRef parameter.")
    );
  }

  @Test
  public void testDoGet_whenNodeNotFound_thenNotFound() throws Exception {
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(any(NodeRef.class))).thenReturn(false);

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_NOT_FOUND),
      eq("Node not found.")
    );
  }

  @Test
  public void testDoGet_whenNoPermission_thenForbidden() throws Exception {
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.getType(TEST_NODE_REF)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      permissionService.hasPermission(TEST_NODE_REF, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_FORBIDDEN),
      eq("You do not have permission to access this file.")
    );
  }

  @Test
  public void testDoGet_whenNoContent_thenNotFound() throws Exception {
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.getType(TEST_NODE_REF)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      permissionService.hasPermission(TEST_NODE_REF, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(
      contentService.getReader(TEST_NODE_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(null);
    when(
      contentService.getReader(TEST_NODE_REF, CircabcModel.PROP_CONTENT)
    ).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_NOT_FOUND),
      eq("Content not found.")
    );
  }

  @Test
  public void testDoGet_whenTicketInAuthHeader_thenValidates()
    throws Exception {
    String ticket = "TICKET_abc123";
    String encoded = Base64.getEncoder().encodeToString(
      ticket.getBytes(StandardCharsets.UTF_8)
    );
    when(request.getHeader("Authorization")).thenReturn("Basic " + encoded);
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.getType(TEST_NODE_REF)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      permissionService.hasPermission(TEST_NODE_REF, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NAME)
    ).thenReturn("doc.txt");

    ContentReader reader = mock(ContentReader.class);
    when(
      contentService.getReader(TEST_NODE_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(reader);
    when(reader.getMimetype()).thenReturn("text/plain");
    when(reader.getContentInputStream()).thenReturn(
      new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8))
    );

    ServletOutputStream outputStream = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(outputStream);
    when(apiToolBox.getCurrentInterestGroup(TEST_NODE_REF)).thenReturn(null);

    servlet.doGet(request, response);

    verify(authenticationService).validate(ticket);
  }

  @Test
  public void testDoGet_whenNodeNotContent_thenBadRequest() throws Exception {
    when(request.getPathInfo()).thenReturn("/" + TEST_ID);
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("ticket")).thenReturn(null);
    when(request.getParameter("alf_ticket")).thenReturn(null);

    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.getType(TEST_NODE_REF)).thenReturn(
      ContentModel.TYPE_FOLDER
    );

    servlet.doGet(request, response);

    verify(response).sendError(
      eq(HttpServletResponse.SC_BAD_REQUEST),
      eq("The node is not of type content.")
    );
  }

  @Test
  public void testDetermineService_whenLibraryAspect_thenReturnsLibrary()
    throws Exception {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_LIBRARY);
    when(nodeService.getAspects(TEST_NODE_REF)).thenReturn(aspects);

    java.lang.reflect.Method method =
      RestContentDownloadServlet.class.getDeclaredMethod(
        "determineService",
        NodeRef.class
      );
    method.setAccessible(true);
    String result = (String) method.invoke(servlet, TEST_NODE_REF);

    assertEquals("Library", result);
  }

  @Test
  public void testDetermineService_whenNewsgroupAspect_thenReturnsNewsgroup()
    throws Exception {
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_NEWSGROUP);
    when(nodeService.getAspects(TEST_NODE_REF)).thenReturn(aspects);

    java.lang.reflect.Method method =
      RestContentDownloadServlet.class.getDeclaredMethod(
        "determineService",
        NodeRef.class
      );
    method.setAccessible(true);
    String result = (String) method.invoke(servlet, TEST_NODE_REF);

    assertEquals("Newsgroup", result);
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
