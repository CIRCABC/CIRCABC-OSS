package eu.europa.ec.digit.circabc.rest.servlet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.LogRecord;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

public class AbstractRestContentServletTest {

  private AbstractRestContentServlet servlet;
  private NodeService nodeService;
  private MimetypeService mimetypeService;
  private AuthenticationService authenticationService;
  private ApiToolBox apiToolBox;
  private Log logger;

  @Before
  public void setUp() throws Exception {
    logger = mock(Log.class);
    servlet = new AbstractRestContentServlet() {
      @Override
      protected Log getLogger() {
        return logger;
      }
    };

    nodeService = mock(NodeService.class);
    mimetypeService = mock(MimetypeService.class);
    authenticationService = mock(AuthenticationService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("mimetypeService", mimetypeService);
    setField("authenticationService", authenticationService);
    setField("apiToolBox", apiToolBox);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AbstractRestContentServlet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(servlet, value);
  }

  @Test
  public void testExtractUid_whenValidUri4Parts_thenReturnsUid() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    when(req.getRequestURI()).thenReturn("/api/content/some-uid-123");

    String uid = servlet.extractUid(req, resp);

    assertEquals("some-uid-123", uid);
  }

  @Test
  public void testExtractUid_whenValidUri5Parts_thenReturnsUid() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    // split("/") on "/api/content/download/node-id" yields ["", "api", "content", "download", "node-id"] = 5 parts
    when(req.getRequestURI()).thenReturn("/api/content/download/node-id");

    String uid = servlet.extractUid(req, resp);

    assertEquals("node-id", uid);
  }

  @Test
  public void testExtractUid_whenMalformedUri_thenReturnsNull()
    throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    when(req.getRequestURI()).thenReturn("/too/many/parts/in/this/uri");

    String uid = servlet.extractUid(req, resp);

    assertNull(uid);
    verify(resp).sendError(
      HttpServletResponse.SC_BAD_REQUEST,
      "Request URL malformed"
    );
  }

  @Test
  public void testExtractUid_whenEmptyUid_thenReturnsNull() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    when(req.getRequestURI()).thenReturn("/api/content/");

    String uid = servlet.extractUid(req, resp);

    assertNull(uid);
  }

  @Test
  public void testAuthenticateUser_whenNoAuthHeader_thenReturnsTrue() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    when(req.getHeader("Authorization")).thenReturn(null);

    assertTrue(servlet.authenticateUser(req, resp));
  }

  @Test
  public void testAuthenticateUser_whenValidBasicAuth_thenReturnsTrue() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    String encoded = java.util.Base64.getEncoder().encodeToString(
      "user:pass".getBytes()
    );
    when(req.getHeader("Authorization")).thenReturn("BASIC " + encoded);

    assertTrue(servlet.authenticateUser(req, resp));
  }

  @Test
  public void testAuthenticateUser_whenAuthThrows_thenReturnsFalse()
    throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    String encoded = java.util.Base64.getEncoder().encodeToString(
      "ticket123".getBytes()
    );
    when(req.getHeader("Authorization")).thenReturn("BASIC " + encoded);
    doThrow(new RuntimeException("bad ticket"))
      .when(authenticationService)
      .validate("ticket123");
    when(logger.isErrorEnabled()).thenReturn(true);

    assertFalse(servlet.authenticateUser(req, resp));
    verify(resp).sendError(
      HttpServletResponse.SC_UNAUTHORIZED,
      "Authentication failed"
    );
  }

  @Test
  public void testGetErrorCode_whenUnavailable_thenNotFound() {
    assertEquals(
      HttpServletResponse.SC_NOT_FOUND,
      servlet.getErrorCode("resource unavailable")
    );
  }

  @Test
  public void testGetErrorCode_whenDenied_thenForbidden() {
    assertEquals(
      HttpServletResponse.SC_FORBIDDEN,
      servlet.getErrorCode("access denied")
    );
  }

  @Test
  public void testGetErrorCode_whenOther_thenInternalServerError() {
    assertEquals(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      servlet.getErrorCode("something went wrong")
    );
  }

  @Test
  public void testGuessMimetype_whenKnownExtension_thenReturnsMimetype() {
    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("pdf", "application/pdf");
    when(mimetypeService.getMimetypesByExtension()).thenReturn(mimeMap);

    assertEquals("application/pdf", servlet.guessMimetype("document.pdf"));
  }

  @Test
  public void testGuessMimetype_whenUnknownExtension_thenReturnsBinary() {
    when(mimetypeService.getMimetypesByExtension()).thenReturn(new HashMap<>());

    assertEquals(
      MimetypeMap.MIMETYPE_BINARY,
      servlet.guessMimetype("file.xyz")
    );
  }

  @Test
  public void testGuessMimetype_whenNoExtension_thenReturnsBinary() {
    assertEquals(
      MimetypeMap.MIMETYPE_BINARY,
      servlet.guessMimetype("noextension")
    );
  }

  @Test
  public void testDetermineService_whenLibraryAspect_thenReturnsLibrary() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_LIBRARY);
    when(nodeService.getAspects(parentRef)).thenReturn(aspects);

    assertEquals("Library", servlet.determineService(nodeRef));
  }

  @Test
  public void testDetermineService_whenInformationAspect_thenReturnsInformation() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_INFORMATION);
    when(nodeService.getAspects(parentRef)).thenReturn(aspects);

    assertEquals("Information", servlet.determineService(nodeRef));
  }

  @Test
  public void testDetermineService_whenNewsgroupAspect_thenReturnsNewsgroup() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    Set<QName> aspects = new HashSet<>();
    aspects.add(CircabcModel.ASPECT_NEWSGROUP);
    when(nodeService.getAspects(parentRef)).thenReturn(aspects);

    assertEquals("Newsgroup", servlet.determineService(nodeRef));
  }

  @Test
  public void testDetermineService_whenNoMatchingAspect_thenReturnsNull() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(nodeService.getAspects(parentRef)).thenReturn(new HashSet<>());

    assertNull(servlet.determineService(nodeRef));
  }

  @Test
  public void testBuildLogRecord_whenIgIsNull_thenReturnsEmptyRecord() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);

    LogRecord record = servlet.buildLogRecord(
      nodeRef,
      "file.txt",
      "Download",
      "Downloaded: "
    );

    assertNotNull(record);
    assertNull(record.getService());
  }

  @Test
  public void testWriteJsonResponse_whenMultipleFiles_thenWritesJson()
    throws Exception {
    HttpServletResponse resp = mock(HttpServletResponse.class);
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(resp.getWriter()).thenReturn(pw);

    List<Map<String, String>> files = new ArrayList<>();
    Map<String, String> file1 = new HashMap<>();
    file1.put("id", "id-1");
    file1.put("fileName", "doc.pdf");
    files.add(file1);
    Map<String, String> file2 = new HashMap<>();
    file2.put("id", "id-2");
    file2.put("fileName", "img.png");
    files.add(file2);

    servlet.writeJsonResponse(resp, files);
    pw.flush();

    String output = sw.toString();
    assertTrue(
      output.contains("\"nodeRef\": \"workspace://SpacesStore/id-1\"")
    );
    assertTrue(output.contains("\"fileName\": \"doc.pdf\""));
    assertTrue(
      output.contains("\"nodeRef\": \"workspace://SpacesStore/id-2\"")
    );
    assertTrue(output.contains(","));
  }

  @Test
  public void testSendError_whenIOException_thenSetsStatus() throws Exception {
    HttpServletResponse resp = mock(HttpServletResponse.class);
    doThrow(new IOException("broken"))
      .when(resp)
      .sendError(anyInt(), anyString());
    when(logger.isErrorEnabled()).thenReturn(true);

    servlet.sendError(resp, 500, "error");

    verify(resp).setStatus(500);
  }
}
