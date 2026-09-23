package eu.europa.ec.digit.circabc.rest.service.customization;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.exception.CustomizationException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NodePreferencesServiceTest {

  private NodePreferencesServiceImpl service;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private ContentService contentService;
  private MimetypeService mimetypeService;
  private MultilingualContentService multilingualContentService;
  private RootPreferencesUpdater rootPreferencesUpdater;
  private SimpleCache<NodeRef, NodeRef> containerCache;
  private SimpleCache<
    NodeRef,
    Map<NodePreferencesServiceImpl.ConfigKey, NodeRef>
  > customizationFolderCache;

  private final NodeRef testNodeRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "test-node"
  );
  private final NodeRef circabcRootRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "circabc-root"
  );
  private final NodeRef circabcDDRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "circabc-dd"
  );
  private final NodeRef containerRef = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "container"
  );

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    service = new NodePreferencesServiceImpl();
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    contentService = mock(ContentService.class);
    mimetypeService = mock(MimetypeService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    rootPreferencesUpdater = mock(RootPreferencesUpdater.class);
    containerCache = mock(SimpleCache.class);
    customizationFolderCache = mock(SimpleCache.class);

    setField("nodeService", nodeService);
    setField("circabcApi", circabcApi);
    setField("contentService", contentService);
    setField("mimetypeService", mimetypeService);
    setField("multilingualContentService", multilingualContentService);
    setField("rootPreferencesUpdater", rootPreferencesUpdater);
    setField("containerCache", containerCache);
    setField("customizationFolderCache", customizationFolderCache);

    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRootRef);
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(circabcDDRef);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodePreferencesServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testIsNodeConfigurable_whenContainerExists_thenReturnsTrue() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(List.of(assocRef));

    assertTrue(service.isNodeConfigurable(testNodeRef));
  }

  @Test
  public void testIsNodeConfigurable_whenNoContainer_thenReturnsFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(Collections.emptyList());

    assertFalse(service.isNodeConfigurable(testNodeRef));
  }

  @Test
  public void testMakeConfigurable_whenNotYetConfigurable_thenCreatesContainer()
    throws Exception {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(Collections.emptyList());

    ChildAssociationRef newAssoc = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );
    when(
      nodeService.createNode(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(newAssoc);

    NodeRef result = service.makeConfigurable(testNodeRef);

    assertEquals(containerRef, result);
    verify(containerCache).clear();
  }

  @Test(expected = CustomizationException.class)
  public void testMakeConfigurable_whenAlreadyConfigurable_thenThrows()
    throws Exception {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    ChildAssociationRef assocRef = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(List.of(assocRef));

    service.makeConfigurable(testNodeRef);
  }

  @Test(expected = CustomizationException.class)
  public void testMakeConfigurable_whenMultilingualContainer_thenThrows()
    throws Exception {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_MULTILINGUAL_CONTAINER
    );

    service.makeConfigurable(testNodeRef);
  }

  @Test
  public void testCustomizationFileExists_whenNoContainer_thenReturnsFalse() {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(Collections.emptyList());

    assertFalse(
      service.customizationFileExists(
        testNodeRef,
        "root",
        "sub",
        "element",
        "file.txt"
      )
    );
  }

  @Test(expected = CustomizationException.class)
  public void testGetCustomization_whenFileNotFound_thenThrows()
    throws Exception {
    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(Collections.emptyList());

    service.getCustomization(testNodeRef, "root", "sub", "element", "file.txt");
  }

  @Test
  public void testGetCustomizationFromNode_whenNull_thenReturnsNull() {
    assertNull(service.getCustomizationFromNode(null));
  }

  @Test
  public void testGetCustomizationFromNode_whenCircabcDD_thenReturnsCircabcRoot() {
    NodeRef result = service.getCustomizationFromNode(circabcDDRef);
    assertEquals(circabcRootRef, result);
  }

  @Test
  public void testGetCustomizationFromNode_whenContainerType_thenReturnsParent() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );
    when(nodeService.getType(containerRef)).thenReturn(
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
    );
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CHILDREN,
      parentRef,
      QName.createQName("test"),
      containerRef
    );
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(parentAssoc);

    NodeRef result = service.getCustomizationFromNode(containerRef);
    assertEquals(parentRef, result);
  }

  @Test(expected = CustomizationException.class)
  public void testRemoveCustomization_whenCircabcRoot_thenThrows()
    throws Exception {
    service.removeCustomization(
      circabcRootRef,
      "root",
      "sub",
      "element",
      "file.txt"
    );
  }

  @Test
  public void testUpdateRootReference_callsUpdater() {
    service.updateRootReference();
    verify(rootPreferencesUpdater).updateSpace(circabcDDRef);
  }
}
