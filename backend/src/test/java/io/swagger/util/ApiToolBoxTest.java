package io.swagger.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ApiToolBoxTest {

  private ApiToolBox apiToolBox;
  private NodeService nodeService;
  private NamespaceService namespaceService;
  private AuthorityService authorityService;

  @Before
  public void setUp() throws Exception {
    apiToolBox = new ApiToolBox();
    nodeService = mock(NodeService.class);
    namespaceService = mock(NamespaceService.class);
    authorityService = mock(AuthorityService.class);

    setField("nodeService", nodeService);
    setField("namespaceService", namespaceService);
    setField("authorityService", authorityService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ApiToolBox.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(apiToolBox, value);
  }

  // --- inputStreamToFile tests ---

  @Test
  public void testInputStreamToFile_whenValidInput_thenWritesCorrectly()
    throws Exception {
    byte[] data = "hello world".getBytes();
    InputStream is = new ByteArrayInputStream(data);
    File tempFile = File.createTempFile("test", ".tmp");
    tempFile.deleteOnExit();

    long bytesWritten = ApiToolBox.inputStreamToFile(is, tempFile, 1024);

    assertEquals(data.length, bytesWritten);
    assertEquals(
      "hello world",
      new String(java.nio.file.Files.readAllBytes(tempFile.toPath()))
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputStreamToFile_whenExceedsLimit_thenThrows()
    throws Exception {
    byte[] data = "this is more than five bytes".getBytes();
    InputStream is = new ByteArrayInputStream(data);
    File tempFile = File.createTempFile("test", ".tmp");
    tempFile.deleteOnExit();

    ApiToolBox.inputStreamToFile(is, tempFile, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputStreamToFile_whenNullInputStream_thenThrows()
    throws Exception {
    File tempFile = File.createTempFile("test", ".tmp");
    tempFile.deleteOnExit();
    ApiToolBox.inputStreamToFile(null, tempFile, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputStreamToFile_whenNullFile_thenThrows() throws Exception {
    ApiToolBox.inputStreamToFile(
      new ByteArrayInputStream(new byte[0]),
      null,
      100
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputStreamToFile_whenZeroLimit_thenThrows()
    throws Exception {
    File tempFile = File.createTempFile("test", ".tmp");
    tempFile.deleteOnExit();
    ApiToolBox.inputStreamToFile(
      new ByteArrayInputStream(new byte[1]),
      tempFile,
      0
    );
  }

  // --- getNodeRef tests ---

  @Test
  public void testGetNodeRef_whenWorkspaceNodeExists_thenReturnsIt() {
    NodeRef expected = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(expected)).thenReturn(true);

    NodeRef result = apiToolBox.getNodeRef("test-id");

    assertEquals(expected, result);
  }

  @Test
  public void testGetNodeRef_whenArchiveNodeExists_thenReturnsIt() {
    NodeRef workspaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef archiveRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(workspaceRef)).thenReturn(false);
    when(nodeService.exists(archiveRef)).thenReturn(true);

    NodeRef result = apiToolBox.getNodeRef("test-id");

    assertEquals(archiveRef, result);
  }

  @Test
  public void testGetNodeRef_whenNeitherExists_thenReturnsNull() {
    NodeRef workspaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef archiveRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "test-id"
    );
    when(nodeService.exists(workspaceRef)).thenReturn(false);
    when(nodeService.exists(archiveRef)).thenReturn(false);

    NodeRef result = apiToolBox.getNodeRef("test-id");

    assertNull(result);
  }

  // --- getUsersFromGroup tests ---

  @Test
  public void testGetUsersFromGroup_whenGroupHasUsers_thenReturnsThem() {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(authorityService.getAuthorityNodeRef("GROUP_TEST")).thenReturn(
      groupRef
    );

    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(userRef);
    when(nodeService.getChildAssocs(groupRef)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(nodeService.getType(userRef)).thenReturn(ContentModel.TYPE_PERSON);
    when(
      nodeService.getProperty(userRef, ContentModel.PROP_USERNAME)
    ).thenReturn("john.doe");

    List<String> users = apiToolBox.getUsersFromGroup("GROUP_TEST");

    assertEquals(1, users.size());
    assertEquals("john.doe", users.get(0));
  }

  @Test
  public void testGetUsersFromGroup_whenGroupNotFound_thenReturnsEmptyList() {
    when(authorityService.getAuthorityNodeRef("GROUP_EVERYONE")).thenReturn(
      null
    );

    List<String> users = apiToolBox.getUsersFromGroup("GROUP_EVERYONE");

    assertTrue(users.isEmpty());
  }

  // --- getOccurenceAsString tests ---

  @Test
  public void testGetOccurenceAsString_whenNull_thenReturnsNotApplicable() {
    String result = apiToolBox.getOccurenceAsString(null);
    assertEquals("Not Applicable", result);
  }

  @Test
  public void testGetOccurenceAsString_whenEmpty_thenReturnsNotApplicable() {
    String result = apiToolBox.getOccurenceAsString("");
    assertEquals("Not Applicable", result);
  }

  @Test
  public void testGetOccurenceAsString_whenOnlyOnce_thenReturnsOnlyOnce() {
    String result = apiToolBox.getOccurenceAsString("OnlyOnce|null|null|-1|-1");
    assertEquals("Only Once", result);
  }

  // --- getCurrentCategory tests ---

  @Test(expected = NullPointerException.class)
  public void testGetCurrentCategory_whenNull_thenThrows() {
    apiToolBox.getCurrentCategory(null);
  }

  @Test
  public void testGetCurrentCategory_whenNodeHasAspect_thenReturnsIt() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(true);

    NodeRef result = apiToolBox.getCurrentCategory(nodeRef);

    assertEquals(nodeRef, result);
  }
}
