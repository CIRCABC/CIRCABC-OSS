package eu.europa.ec.digit.circabc.rest.service.customization;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.exception.CustomizationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

public class NodePreferencesServiceImplTest {

  private NodePreferencesServiceImpl service;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private MultilingualContentService multilingualContentService;
  private ContentService contentService;
  private RootPreferencesUpdater rootPreferencesUpdater;
  private MimetypeService mimetypeService;
  private SimpleCache<NodeRef, NodeRef> containerCache;
  private SimpleCache<
    NodeRef,
    Map<NodePreferencesServiceImpl.ConfigKey, NodeRef>
  > customizationFolderCache;

  private NodeRef testNodeRef;
  private NodeRef circabcNodeRef;
  private NodeRef circabcDDNodeRef;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    service = new NodePreferencesServiceImpl();

    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    multilingualContentService = mock(MultilingualContentService.class);
    contentService = mock(ContentService.class);
    rootPreferencesUpdater = mock(RootPreferencesUpdater.class);
    mimetypeService = mock(MimetypeService.class);
    containerCache = mock(SimpleCache.class);
    customizationFolderCache = mock(SimpleCache.class);

    setField("nodeService", nodeService);
    setField("circabcApi", circabcApi);
    setField("multilingualContentService", multilingualContentService);
    setField("contentService", contentService);
    setField("rootPreferencesUpdater", rootPreferencesUpdater);
    setField("mimetypeService", mimetypeService);
    setField("containerCache", containerCache);
    setField("customizationFolderCache", customizationFolderCache);

    testNodeRef = new NodeRef("workspace://SpacesStore/test-node");
    circabcNodeRef = new NodeRef("workspace://SpacesStore/circabc-root");
    circabcDDNodeRef = new NodeRef("workspace://SpacesStore/circabc-dd");

    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcNodeRef);
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(circabcDDNodeRef);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodePreferencesServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testIsNodeConfigurable_whenContainerExists_thenReturnsTrue() {
    NodeRef containerRef = new NodeRef("workspace://SpacesStore/container");
    ChildAssociationRef assocRef = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );

    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
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
    NodeRef containerRef = new NodeRef("workspace://SpacesStore/new-container");
    ChildAssociationRef newAssoc = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );

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
    NodeRef containerRef = new NodeRef("workspace://SpacesStore/container");
    ChildAssociationRef assocRef = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );

    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
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
  public void testCustomizationFileExists_whenFileExists_thenReturnsTrue() {
    NodeRef containerRef = new NodeRef("workspace://SpacesStore/container");
    NodeRef rootRef = new NodeRef("workspace://SpacesStore/root-custom");
    NodeRef subRef = new NodeRef("workspace://SpacesStore/sub-custom");
    NodeRef elementRef = new NodeRef("workspace://SpacesStore/element-custom");
    NodeRef fileRef = new NodeRef("workspace://SpacesStore/file");

    ChildAssociationRef containerAssoc = new ChildAssociationRef(
      NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
      testNodeRef,
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER,
      containerRef
    );

    when(nodeService.getType(testNodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );
    when(
      nodeService.getChildAssocs(
        testNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(List.of(containerAssoc));

    // Navigate the folder hierarchy using ASSOC_CHILDREN
    when(
      nodeService.getChildAssocs(
        eq(containerRef),
        eq(ContentModel.ASSOC_CHILDREN),
        any(QName.class)
      )
    ).thenReturn(
      List.of(
        new ChildAssociationRef(
          ContentModel.ASSOC_CHILDREN,
          containerRef,
          QName.createQName("test", "root"),
          rootRef
        )
      )
    );
    when(
      nodeService.getChildAssocs(
        eq(rootRef),
        eq(ContentModel.ASSOC_CHILDREN),
        any(QName.class)
      )
    ).thenReturn(
      List.of(
        new ChildAssociationRef(
          ContentModel.ASSOC_CHILDREN,
          rootRef,
          QName.createQName("test", "sub"),
          subRef
        )
      )
    );
    when(
      nodeService.getChildAssocs(
        eq(subRef),
        eq(ContentModel.ASSOC_CHILDREN),
        any(QName.class)
      )
    ).thenReturn(
      List.of(
        new ChildAssociationRef(
          ContentModel.ASSOC_CHILDREN,
          subRef,
          QName.createQName("test", "elem"),
          elementRef
        )
      )
    );
    when(
      nodeService.getChildAssocs(
        eq(elementRef),
        eq(ContentModel.ASSOC_CHILDREN),
        any(QName.class)
      )
    ).thenReturn(
      List.of(
        new ChildAssociationRef(
          ContentModel.ASSOC_CHILDREN,
          elementRef,
          QName.createQName("test", "file"),
          fileRef
        )
      )
    );

    assertTrue(
      service.customizationFileExists(
        testNodeRef,
        "typeRoot",
        "subType",
        "element",
        "test.xml"
      )
    );
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
        "typeRoot",
        "subType",
        "element",
        "test.xml"
      )
    );
  }

  @Test(expected = CustomizationException.class)
  public void testGetCustomization_whenFileDoesNotExist_thenThrows()
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

    service.getCustomization(
      testNodeRef,
      "typeRoot",
      "subType",
      "element",
      "missing.xml"
    );
  }

  @Test(expected = CustomizationException.class)
  public void testRemoveCustomization_whenCircabcRoot_thenThrows()
    throws Exception {
    // Set up so circabcNodeRef is resolved
    setField("circabcNodeRef", circabcNodeRef);

    when(nodeService.getType(circabcNodeRef)).thenReturn(
      ContentModel.TYPE_FOLDER
    );
    when(
      nodeService.getChildAssocs(
        circabcNodeRef,
        NodePreferencesServiceImpl.ASSOC_CUSTOMIZE,
        NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
      )
    ).thenReturn(Collections.emptyList());

    service.removeCustomization(
      circabcNodeRef,
      "typeRoot",
      "subType",
      "element",
      "file.xml"
    );
  }

  @Test
  public void testGetCustomizationFromNode_whenNull_thenReturnsNull() {
    assertNull(service.getCustomizationFromNode(null));
  }

  @Test
  public void testGetCustomizationFromNode_whenDictionaryNode_thenReturnsCircabcRoot()
    throws Exception {
    setField("circabcDictionaryNodeRef", circabcDDNodeRef);
    setField("circabcNodeRef", circabcNodeRef);

    assertEquals(
      circabcNodeRef,
      service.getCustomizationFromNode(circabcDDNodeRef)
    );
  }

  @Test
  public void testGetCustomizationFromNode_whenContainerType_thenReturnsParent()
    throws Exception {
    NodeRef containerRef = new NodeRef("workspace://SpacesStore/container");
    NodeRef parentRef = new NodeRef("workspace://SpacesStore/parent");

    setField("circabcDictionaryNodeRef", circabcDDNodeRef);

    when(nodeService.getType(containerRef)).thenReturn(
      NodePreferencesServiceImpl.TYPE_CUSTOMIZATION_CONTAINER
    );
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(
      new ChildAssociationRef(
        ContentModel.ASSOC_CHILDREN,
        parentRef,
        ContentModel.TYPE_CONTENT,
        containerRef
      )
    );

    assertEquals(parentRef, service.getCustomizationFromNode(containerRef));
  }

  @Test
  public void testAddCustomizationFile_whenNotConfigurable_thenThrows()
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

    InputStream content = new ByteArrayInputStream("test".getBytes());

    try {
      service.addCustomizationFile(
        testNodeRef,
        "typeRoot",
        "subType",
        "element",
        "file.xml",
        content
      );
      fail("Expected CustomizationException");
    } catch (CustomizationException e) {
      assertTrue(e.getMessage().contains("must be setted as configurable"));
    }
  }

  @Test
  public void testUpdateRootReference_delegatesToUpdater() throws Exception {
    setField("circabcDictionaryNodeRef", circabcDDNodeRef);

    service.updateRootReference();

    verify(rootPreferencesUpdater).updateSpace(circabcDDNodeRef);
  }
}
