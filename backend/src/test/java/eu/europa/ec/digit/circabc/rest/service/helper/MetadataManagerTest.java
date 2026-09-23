package eu.europa.ec.digit.circabc.rest.service.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class MetadataManagerTest {

  private MetadataManager metadataManager;
  private NodeService nodeService;
  private NodeTypeManager nodeTypeManager;

  @Before
  public void setUp() throws Exception {
    metadataManager = new MetadataManager();
    nodeService = mock(NodeService.class);
    nodeTypeManager = mock(NodeTypeManager.class);

    setField("nodeService", nodeService);
    setField("nodeTypeManager", nodeTypeManager);

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
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = MetadataManager.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(metadataManager, value);
  }

  // --- getValidName tests ---

  @Test
  public void testGetValidName_whenNameContainsInvalidChars_thenReplacedWithUnderscore() {
    String result = metadataManager.getValidName("file\"name*.txt");
    assertEquals("file_name_.txt", result);
  }

  @Test
  public void testGetValidName_whenNameIsClean_thenReturnedUnchanged() {
    String result = metadataManager.getValidName("valid-file.txt");
    assertEquals("valid-file.txt", result);
  }

  @Test
  public void testGetValidName_whenCustomReplacement_thenUsesIt() {
    String result = metadataManager.getValidName("file|name.txt", "-");
    assertEquals("file-name.txt", result);
  }

  @Test
  public void testGetValidName_whenNameHasLeadingTrailingSpaces_thenTrimmed() {
    String result = metadataManager.getValidName("  file.txt  ");
    assertEquals("file.txt", result);
  }

  // --- getValidUniqueName tests ---

  @Test
  public void testGetValidUniqueName_whenNameDoesNotExist_thenReturnCleanName() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file.txt"
      )
    ).thenReturn(null);

    String result = metadataManager.getValidUniqueName(parent, "file.txt");
    assertEquals("file.txt", result);
  }

  @Test
  public void testGetValidUniqueName_whenNameExists_thenAppendsCounter() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef existing = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file.txt"
      )
    ).thenReturn(existing);
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file(0).txt"
      )
    ).thenReturn(null);

    String result = metadataManager.getValidUniqueName(parent, "file.txt");
    assertEquals("file(0).txt", result);
  }

  @Test
  public void testGetValidUniqueName_whenMultipleExist_thenIncrementsCounter() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef existing = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file.txt"
      )
    ).thenReturn(existing);
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file(0).txt"
      )
    ).thenReturn(existing);
    when(
      nodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file(1).txt"
      )
    ).thenReturn(null);

    String result = metadataManager.getValidUniqueName(parent, "file.txt");
    assertEquals("file(1).txt", result);
  }

  // --- isInlineEditable tests ---

  @Test
  public void testIsInlineEditable_whenTextPlain_thenTrue() {
    assertTrue(metadataManager.isInlineEditable("text/plain"));
  }

  @Test
  public void testIsInlineEditable_whenPdf_thenFalse() {
    assertFalse(metadataManager.isInlineEditable("application/pdf"));
  }

  @Test
  public void testIsInlineEditable_whenNull_thenFalse() {
    assertFalse(metadataManager.isInlineEditable(null));
  }

  // --- computeTitle tests ---

  @Test
  public void testComputeTitle_whenTitleExists_thenReturnsTitle() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_TITLE, "My Title");
    props.put(ContentModel.PROP_NAME, "myfile.txt");

    assertEquals("My Title", metadataManager.computeTitle(props));
  }

  @Test
  public void testComputeTitle_whenTitleIsNull_thenReturnsName() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_TITLE, null);
    props.put(ContentModel.PROP_NAME, "myfile.txt");

    assertEquals("myfile.txt", metadataManager.computeTitle(props));
  }

  @Test
  public void testComputeTitle_whenTitleIsBlank_thenReturnsName() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_TITLE, "   ");
    props.put(ContentModel.PROP_NAME, "myfile.txt");

    assertEquals("myfile.txt", metadataManager.computeTitle(props));
  }

  // --- isLockOwner tests ---

  @Test
  public void testIsLockOwner_whenUserIsOwner_thenTrue() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    when(nodeTypeManager.isWorkingCopyDocument(workingCopy)).thenReturn(true);
    when(
      nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER)
    ).thenReturn("testuser");

    assertTrue(metadataManager.isLockOwner(workingCopy));
  }

  @Test
  public void testIsLockOwner_whenUserIsNotOwner_thenFalse() {
    NodeRef workingCopy = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "wc-id"
    );
    when(nodeTypeManager.isWorkingCopyDocument(workingCopy)).thenReturn(true);
    when(
      nodeService.getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER)
    ).thenReturn("otheruser");

    assertFalse(metadataManager.isLockOwner(workingCopy));
  }

  @Test
  public void testIsLockOwner_whenNotWorkingCopy_thenFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(nodeTypeManager.isWorkingCopyDocument(nodeRef)).thenReturn(false);

    assertFalse(metadataManager.isLockOwner(nodeRef));
  }
}
