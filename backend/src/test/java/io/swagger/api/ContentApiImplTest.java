package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class ContentApiImplTest {

  private static final String NODE_ID = "test-node-id";
  private static final NodeRef NODE_REF = new NodeRef(
    "workspace://SpacesStore/" + NODE_ID
  );

  private ContentApiImpl contentApi;
  private VersionService versionService;
  private NodeService nodeService;
  private ContentService contentService;
  private MultilingualContentService multilingualContentService;
  private ApiToolBox apiToolBox;
  private NodesApi nodesApi;
  private BehaviourFilter policyBehaviourFilter;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    contentApi = new ContentApiImpl();
    versionService = mock(VersionService.class);
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    apiToolBox = mock(ApiToolBox.class);
    nodesApi = mock(NodesApi.class);
    policyBehaviourFilter = mock(BehaviourFilter.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("versionService", versionService);
    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("multilingualContentService", multilingualContentService);
    setField("apiToolBox", apiToolBox);
    setField("nodesApi", nodesApi);
    setField("policyBehaviourFilter", policyBehaviourFilter);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String name, Object value) throws Exception {
    Field field = ContentApiImpl.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(contentApi, value);
  }

  // --- contentIdVersionsGet ---

  @Test
  public void testContentIdVersionsGet_whenVersioned_thenReturnsVersions() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(true);

    VersionHistory history = mock(VersionHistory.class);
    when(versionService.getVersionHistory(NODE_REF)).thenReturn(history);

    org.alfresco.service.cmr.version.Version alfVersion = mock(
      org.alfresco.service.cmr.version.Version.class
    );
    NodeRef frozenRef = new NodeRef("workspace://SpacesStore/frozen-id");
    when(alfVersion.getVersionLabel()).thenReturn("1.0");
    when(alfVersion.getFrozenStateNodeRef()).thenReturn(frozenRef);
    when(alfVersion.getDescription()).thenReturn("Initial version");
    when(alfVersion.getVersionProperty("modified")).thenReturn(new Date());

    NodeRef versionedNodeRef = new NodeRef(
      "workspace://SpacesStore/versioned-id"
    );
    when(alfVersion.getVersionedNodeRef()).thenReturn(versionedNodeRef);
    NodeRef parentRef = new NodeRef("workspace://SpacesStore/parent-id");
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.PROP_NAME,
      versionedNodeRef
    );
    when(nodeService.getPrimaryParent(versionedNodeRef)).thenReturn(
      parentAssoc
    );

    when(history.getAllVersions()).thenReturn(List.of(alfVersion));

    Node node = new Node();
    node.setProperties(new HashMap<>());
    when(nodesApi.getNode(frozenRef)).thenReturn(node);

    List<Version> result = contentApi.contentIdVersionsGet(NODE_ID, "en");

    assertEquals(1, result.size());
    assertEquals("1.0", result.get(0).getVersionLabel());
    assertEquals("Initial version", result.get(0).getNotes());
  }

  @Test
  public void testContentIdVersionsGet_whenNotVersioned_thenReturnsEmpty() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(false);

    List<Version> result = contentApi.contentIdVersionsGet(NODE_ID, "en");

    assertTrue(result.isEmpty());
  }

  // --- contentIdVersionsVersionIdGet ---

  @Test
  public void testContentIdVersionsVersionIdGet_whenFound_thenReturnsVersion() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(true);

    VersionHistory history = mock(VersionHistory.class);
    when(versionService.getVersionHistory(NODE_REF)).thenReturn(history);

    String versionNodeId = "version-node-id";
    NodeRef frozenRef = new NodeRef("workspace://SpacesStore/" + versionNodeId);
    org.alfresco.service.cmr.version.Version alfVersion = mock(
      org.alfresco.service.cmr.version.Version.class
    );
    when(alfVersion.getVersionLabel()).thenReturn("2.0");
    when(alfVersion.getFrozenStateNodeRef()).thenReturn(frozenRef);
    when(alfVersion.getDescription()).thenReturn("Second version");
    when(history.getAllVersions()).thenReturn(List.of(alfVersion));

    Node node = new Node();
    when(nodesApi.getNode(frozenRef)).thenReturn(node);

    Version result = contentApi.contentIdVersionsVersionIdGet(
      NODE_ID,
      versionNodeId,
      "en"
    );

    assertNotNull(result);
    assertEquals("2.0", result.getVersionLabel());
    assertEquals("Second version", result.getNotes());
  }

  @Test
  public void testContentIdVersionsVersionIdGet_whenNotFound_thenReturnsNull() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(true);

    VersionHistory history = mock(VersionHistory.class);
    when(versionService.getVersionHistory(NODE_REF)).thenReturn(history);

    NodeRef frozenRef = new NodeRef("workspace://SpacesStore/other-id");
    org.alfresco.service.cmr.version.Version alfVersion = mock(
      org.alfresco.service.cmr.version.Version.class
    );
    when(alfVersion.getFrozenStateNodeRef()).thenReturn(frozenRef);
    when(history.getAllVersions()).thenReturn(List.of(alfVersion));

    Version result = contentApi.contentIdVersionsVersionIdGet(
      NODE_ID,
      "non-existent-id",
      "en"
    );

    assertNull(result);
  }

  // --- contentIdDelete ---

  @Test
  public void testContentIdDelete_whenNotMultilingual_thenDeletesNode() {
    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(igRoot);
    when(
      nodeService.hasAspect(NODE_REF, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(false);

    contentApi.contentIdDelete(NODE_ID);

    verify(nodeService).setProperty(
      NODE_REF,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED,
      igRoot.getId()
    );
    verify(nodeService).deleteNode(NODE_REF);
  }

  @Test
  public void testContentIdDelete_whenMultilingualPivot_thenDeletesTranslationsToo() {
    NodeRef igRoot = new NodeRef("workspace://SpacesStore/ig-root");
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(igRoot);
    when(
      nodeService.hasAspect(NODE_REF, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(true);
    when(multilingualContentService.getPivotTranslation(NODE_REF)).thenReturn(
      NODE_REF
    );

    NodeRef translationRef = new NodeRef(
      "workspace://SpacesStore/translation-id"
    );
    Map<Locale, NodeRef> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, NODE_REF);
    translations.put(Locale.FRENCH, translationRef);
    when(multilingualContentService.getTranslations(NODE_REF)).thenReturn(
      translations
    );

    contentApi.contentIdDelete(NODE_ID);

    verify(nodeService).deleteNode(translationRef);
    verify(nodeService).deleteNode(NODE_REF);
  }

  // --- contentIdTranslationsGet ---

  @Test
  public void testContentIdTranslationsGet_thenReturnsTranslations() {
    NodeRef frRef = new NodeRef("workspace://SpacesStore/fr-node");
    Map<Locale, NodeRef> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, NODE_REF);
    translations.put(Locale.FRENCH, frRef);
    when(multilingualContentService.getTranslations(NODE_REF)).thenReturn(
      translations
    );
    when(multilingualContentService.getPivotTranslation(NODE_REF)).thenReturn(
      NODE_REF
    );

    Node enNode = new Node();
    Node frNode = new Node();
    Node pivotNode = new Node();
    when(nodesApi.getNode(NODE_REF)).thenReturn(enNode).thenReturn(pivotNode);
    when(nodesApi.getNode(frRef)).thenReturn(frNode);

    Translations result = contentApi.contentIdTranslationsGet(NODE_ID);

    assertNotNull(result);
    assertEquals(2, result.getTranslations().size());
    assertNotNull(result.getPivot());
  }

  // --- contentIdTopicsGet ---

  @Test
  public void testContentIdTopicsGet_whenNoDiscussion_thenReturnsEmpty() {
    when(nodeService.exists(NODE_REF)).thenReturn(true);
    when(nodeService.getChildAssocs(NODE_REF)).thenReturn(
      Collections.emptyList()
    );

    List<Node> result = contentApi.contentIdTopicsGet(NODE_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testContentIdTopicsGet_whenHasTopics_thenReturnsThem() {
    when(nodeService.exists(NODE_REF)).thenReturn(true);

    NodeRef discussionRef = new NodeRef(
      "workspace://SpacesStore/discussion-id"
    );
    QName discussionQName = QName.createQName(
      NamespaceService.FORUMS_MODEL_1_0_URI,
      "discussion"
    );
    ChildAssociationRef discussionAssoc = new ChildAssociationRef(
      ForumModel.ASSOC_DISCUSSION,
      NODE_REF,
      discussionQName,
      discussionRef
    );
    when(nodeService.getChildAssocs(NODE_REF)).thenReturn(
      List.of(discussionAssoc)
    );

    NodeRef topicRef = new NodeRef("workspace://SpacesStore/topic-id");
    ChildAssociationRef topicAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      discussionRef,
      ContentModel.PROP_NAME,
      topicRef
    );
    when(nodeService.getChildAssocs(discussionRef)).thenReturn(
      List.of(topicAssoc)
    );
    when(nodeService.getType(topicRef)).thenReturn(ForumModel.TYPE_TOPIC);

    Node topicNode = new Node();
    when(nodesApi.getNode(topicRef)).thenReturn(topicNode);

    List<Node> result = contentApi.contentIdTopicsGet(NODE_ID);

    assertEquals(1, result.size());
    assertSame(topicNode, result.get(0));
  }

  // --- contentIdTopicsPost ---

  @Test
  public void testContentIdTopicsPost_whenContentType_thenCreatesTopicNode() {
    when(nodeService.getType(NODE_REF)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getChildAssocs(NODE_REF)).thenReturn(
      Collections.emptyList()
    );

    NodeRef forumRef = new NodeRef("workspace://SpacesStore/forum-id");
    ChildAssociationRef forumAssoc = mock(ChildAssociationRef.class);
    when(forumAssoc.getChildRef()).thenReturn(forumRef);
    when(
      nodeService.createNode(
        eq(NODE_REF),
        eq(ForumModel.ASSOC_DISCUSSION),
        any(QName.class),
        eq(ForumModel.TYPE_FORUM)
      )
    ).thenReturn(forumAssoc);

    NodeRef topicRef = new NodeRef("workspace://SpacesStore/new-topic-id");
    ChildAssociationRef topicAssoc = mock(ChildAssociationRef.class);
    when(topicAssoc.getChildRef()).thenReturn(topicRef);
    when(
      nodeService.createNode(
        eq(forumRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class),
        eq(ForumModel.TYPE_TOPIC),
        anyMap()
      )
    ).thenReturn(topicAssoc);

    Node expectedNode = new Node();
    when(nodesApi.getNode(topicRef)).thenReturn(expectedNode);

    Node body = new Node();
    body.setName("Test Topic");

    Node result = contentApi.contentIdTopicsPost(NODE_ID, body);

    assertSame(expectedNode, result);
  }

  @Test(expected = InvalidTypeException.class)
  public void testContentIdTopicsPost_whenInvalidType_thenThrows() {
    when(nodeService.getType(NODE_REF)).thenReturn(ForumModel.TYPE_TOPIC);

    contentApi.contentIdTopicsPost(NODE_ID, new Node());
  }

  // --- contentIdTranslationsPost ---

  @Test(expected = InvalidAspectException.class)
  public void testContentIdTranslationsPost_whenNotMultilingual_thenThrows() {
    when(
      nodeService.hasAspect(NODE_REF, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(false);

    contentApi.contentIdTranslationsPost(
      NODE_ID,
      "fr",
      new ByteArrayInputStream(new byte[0]),
      "text/plain",
      "file.txt"
    );
  }

  // --- contentIdMultilingualAspectPost ---

  @Test
  public void testContentIdMultilingualAspectPost_thenMakesTranslation() {
    Map<QName, java.io.Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_LOCALE, Locale.ENGLISH);
    when(nodeService.getProperties(NODE_REF)).thenReturn(props);

    NodeRef mlContainer = new NodeRef(
      "workspace://SpacesStore/ml-container-id"
    );
    when(
      multilingualContentService.getTranslationContainer(NODE_REF)
    ).thenReturn(mlContainer);

    MultilingualAspectMetadata body = new MultilingualAspectMetadata();
    body.setPivotLang("en");
    body.setAuthor("Test Author");

    contentApi.contentIdMultilingualAspectPost(NODE_ID, body);

    verify(multilingualContentService).makeTranslation(
      eq(NODE_REF),
      any(Locale.class)
    );
    verify(nodeService).setProperty(
      mlContainer,
      ContentModel.PROP_AUTHOR,
      "Test Author"
    );
  }

  // --- contentIdPut ---

  @Test
  public void testContentIdPut_thenUpdatesProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put("issue_date", "");
    properties.put("expiration_date", "");
    properties.put("reference", "REF-001");
    properties.put("status", "DRAFT");
    properties.put("security", "NORMAL");
    properties.put("author", "Author");
    properties.put("encoding", null);
    properties.put("mimetype", null);

    Node body = new Node();
    body.setName("updated.pdf");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setProperties(properties);

    when(
      nodeService.hasAspect(NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(false);
    ContentData contentData = new ContentData(
      "url",
      "application/pdf",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(NODE_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);

    contentApi.contentIdPut(NODE_ID, body);

    verify(nodeService).setProperty(
      NODE_REF,
      ContentModel.PROP_NAME,
      "updated.pdf"
    );
    verify(nodeService).setProperty(
      NODE_REF,
      DocumentModel.PROP_REFERENCE,
      "REF-001"
    );
    verify(nodeService).setProperty(
      NODE_REF,
      DocumentModel.PROP_STATUS,
      "DRAFT"
    );
    verify(nodeService).setProperty(
      NODE_REF,
      ContentModel.PROP_AUTHOR,
      "Author"
    );
  }

  @Test
  public void testContentIdPut_whenUrlable_thenSetsUrl() {
    Map<String, String> properties = new HashMap<>();
    properties.put("issue_date", "");
    properties.put("expiration_date", "");
    properties.put("reference", null);
    properties.put("status", null);
    properties.put("security", "");
    properties.put("author", null);
    properties.put("url", "https://example.com");

    Node body = new Node();
    body.setName("link");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setProperties(properties);

    when(
      nodeService.hasAspect(NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(true);

    contentApi.contentIdPut(NODE_ID, body);

    verify(nodeService).setProperty(
      NODE_REF,
      DocumentModel.PROP_URL,
      "https://example.com"
    );
  }

  // --- contentIdFirstVersionsGet ---

  @Test
  public void testContentIdFirstVersionsGet_whenVersioned_thenReturnsHeadAndPredecessors() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(true);

    org.alfresco.service.cmr.version.Version headVersion = mock(
      org.alfresco.service.cmr.version.Version.class
    );
    NodeRef headFrozenRef = new NodeRef("workspace://SpacesStore/head-frozen");
    when(headVersion.getVersionLabel()).thenReturn("2.0");
    when(headVersion.getFrozenStateNodeRef()).thenReturn(headFrozenRef);
    when(headVersion.getDescription()).thenReturn("Head version");
    when(headVersion.getVersionProperty("modified")).thenReturn(new Date());
    NodeRef headVersionedRef = new NodeRef(
      "workspace://SpacesStore/head-versioned"
    );
    when(headVersion.getVersionedNodeRef()).thenReturn(headVersionedRef);
    NodeRef parentRef = new NodeRef("workspace://SpacesStore/parent-id");
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      ContentModel.PROP_NAME,
      headVersionedRef
    );
    when(nodeService.getPrimaryParent(headVersionedRef)).thenReturn(
      parentAssoc
    );

    when(versionService.getCurrentVersion(NODE_REF)).thenReturn(headVersion);

    VersionHistory history = mock(VersionHistory.class);
    when(versionService.getVersionHistory(headFrozenRef)).thenReturn(history);

    org.alfresco.service.cmr.version.Version prevVersion = mock(
      org.alfresco.service.cmr.version.Version.class
    );
    NodeRef prevFrozenRef = new NodeRef("workspace://SpacesStore/prev-frozen");
    when(prevVersion.getVersionLabel()).thenReturn("1.0");
    when(prevVersion.getFrozenStateNodeRef()).thenReturn(prevFrozenRef);
    when(prevVersion.getDescription()).thenReturn("Previous");
    when(prevVersion.getVersionProperty("modified")).thenReturn(new Date());
    when(prevVersion.getVersionedNodeRef()).thenReturn(headVersionedRef);
    when(history.getPredecessor(headVersion)).thenReturn(prevVersion);
    when(history.getPredecessor(prevVersion)).thenReturn(null);

    Node headNode = new Node();
    headNode.setProperties(new HashMap<>());
    Node prevNode = new Node();
    prevNode.setProperties(new HashMap<>());
    when(nodesApi.getNode(headFrozenRef)).thenReturn(headNode);
    when(nodesApi.getNode(prevFrozenRef)).thenReturn(prevNode);

    List<Version> result = contentApi.contentIdFirstVersionsGet(NODE_ID);

    assertEquals(2, result.size());
    assertEquals("2.0", result.get(0).getVersionLabel());
    assertEquals("1.0", result.get(1).getVersionLabel());
  }

  @Test
  public void testContentIdFirstVersionsGet_whenNotVersioned_thenReturnsEmpty() {
    when(versionService.isVersioned(NODE_REF)).thenReturn(false);

    List<Version> result = contentApi.contentIdFirstVersionsGet(NODE_ID);

    assertTrue(result.isEmpty());
  }

  // --- contentIdPut with dynamic attributes ---

  @Test
  public void testContentIdPut_whenDynAttrPresent_thenSetsDynAttrProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put("issue_date", "");
    properties.put("expiration_date", "");
    properties.put("reference", null);
    properties.put("status", null);
    properties.put("security", "");
    properties.put("author", null);
    properties.put("dynAttr1", "Value1");
    properties.put("dynAttr2", "Value2");

    Node body = new Node();
    body.setName("doc.pdf");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setProperties(properties);

    when(
      nodeService.hasAspect(NODE_REF, DocumentModel.ASPECT_URLABLE)
    ).thenReturn(false);
    ContentData contentData = new ContentData(
      "url",
      "application/pdf",
      100L,
      "UTF-8"
    );
    when(
      nodeService.getProperty(NODE_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);

    contentApi.contentIdPut(NODE_ID, body);

    verify(nodeService).setProperty(
      NODE_REF,
      QName.createQName(
        DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
        "dynAttr1"
      ),
      "Value1"
    );
    verify(nodeService).setProperty(
      NODE_REF,
      QName.createQName(
        DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
        "dynAttr2"
      ),
      "Value2"
    );
  }
}
