package eu.europa.ec.digit.circabc.rest.service.keyword;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.KeywordModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class KeywordsServiceImplTest {

  private KeywordsServiceImpl service;
  private NodeService nodeService;
  private PermissionService permissionService;
  private NamespaceService namespaceService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;

  private NodeRef igNodeRef;
  private NodeRef containerNodeRef;
  private NodeRef keywordNodeRef;
  private NodeRef documentNodeRef;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    namespaceService = mock(NamespaceService.class);
    searchService = mock(SearchService.class);
    apiToolBox = mock(ApiToolBox.class);

    service = new KeywordsServiceImpl();
    service.setNodeService(nodeService);
    service.setPermissionService(permissionService);
    service.setNamespaceService(namespaceService);
    service.setSearchService(searchService);
    service.setApiToolBox(apiToolBox);

    igNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");
    containerNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    keywordNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "keyword-id"
    );
    documentNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
  }

  // --- createKeyword ---

  @Test(expected = NullPointerException.class)
  public void testCreateKeyword_whenNullKeyword_thenThrows() {
    service.createKeyword(igNodeRef, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateKeyword_whenKeywordHasId_thenThrows() {
    Keyword keyword = new KeywordImpl(keywordNodeRef, "value");
    service.createKeyword(igNodeRef, keyword);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateKeyword_whenEmptyValue_thenThrows() {
    Keyword keyword = new KeywordImpl((NodeRef) null, "");
    service.createKeyword(igNodeRef, keyword);
  }

  @Test
  public void testCreateKeyword_whenValidNonTranslated_thenCreatesNode() {
    Keyword keyword = new KeywordImpl((NodeRef) null, "environment");

    // Mock getOrCreateKeywordContainer
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerNodeRef);
    List<ChildAssociationRef> containerAssocs = new ArrayList<>();
    containerAssocs.add(containerAssoc);
    when(
      nodeService.getChildAssocs(eq(igNodeRef), any(QName.class), any())
    ).thenReturn(containerAssocs);

    // Mock createNode for keyword
    ChildAssociationRef kwAssoc = mock(ChildAssociationRef.class);
    when(kwAssoc.getChildRef()).thenReturn(keywordNodeRef);
    when(
      nodeService.createNode(
        eq(containerNodeRef),
        eq(KeywordModel.ASSOC_KEYWORDS),
        eq(KeywordModel.TYPE_KEYWORD),
        eq(KeywordModel.TYPE_KEYWORD),
        any()
      )
    ).thenReturn(kwAssoc);

    Keyword result = service.createKeyword(igNodeRef, keyword);

    assertNotNull(result);
    assertEquals(keywordNodeRef, result.getId());
    assertEquals("environment", result.getValue());
    verify(nodeService).setProperty(
      keywordNodeRef,
      ContentModel.PROP_TITLE,
      "environment"
    );
    verify(nodeService).setProperty(
      keywordNodeRef,
      KeywordModel.PROP_TRANSLATED,
      Boolean.FALSE
    );
  }

  // --- getKeywords ---

  @Test
  public void testGetKeywords_whenNoContainer_thenReturnsEmptyList() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getChildAssocs(eq(igNodeRef), any(QName.class), any())
    ).thenReturn(Collections.emptyList());

    List<Keyword> result = service.getKeywords(igNodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetKeywords_whenKeywordsExist_thenReturnsList() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    // Container exists
    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerNodeRef);
    when(
      nodeService.getChildAssocs(eq(igNodeRef), any(QName.class), any())
    ).thenReturn(List.of(containerAssoc));

    // Keywords in container
    ChildAssociationRef kwAssoc = mock(ChildAssociationRef.class);
    when(kwAssoc.getChildRef()).thenReturn(keywordNodeRef);
    when(
      nodeService.getChildAssocs(
        eq(containerNodeRef),
        eq(KeywordModel.ASSOC_KEYWORDS),
        eq(RegexQNamePattern.MATCH_ALL)
      )
    ).thenReturn(List.of(kwAssoc));
    when(nodeService.getType(keywordNodeRef)).thenReturn(
      KeywordModel.TYPE_KEYWORD
    );

    // Non-multilingual keyword
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(Boolean.FALSE);
    when(
      nodeService.getProperty(keywordNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("climate");

    List<Keyword> result = service.getKeywords(igNodeRef);

    assertEquals(1, result.size());
    assertEquals("climate", result.get(0).getValue());
    assertEquals(keywordNodeRef, result.get(0).getId());
  }

  // --- buildKeywordWithId ---

  @Test(expected = NullPointerException.class)
  public void testBuildKeywordWithId_whenNullNodeRef_thenThrows() {
    service.buildKeywordWithId((NodeRef) null);
  }

  @Test
  public void testBuildKeywordWithId_whenNodeDoesNotExist_thenReturnsNull() {
    when(nodeService.exists(keywordNodeRef)).thenReturn(false);

    Keyword result = service.buildKeywordWithId(keywordNodeRef);

    assertNull(result);
  }

  @Test
  public void testBuildKeywordWithId_whenNonMultilingual_thenReturnsKeyword() {
    when(nodeService.exists(keywordNodeRef)).thenReturn(true);
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(Boolean.FALSE);
    when(
      nodeService.getProperty(keywordNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("energy");

    Keyword result = service.buildKeywordWithId(keywordNodeRef);

    assertNotNull(result);
    assertEquals("energy", result.getValue());
    assertFalse(result.isKeywordTranslated());
  }

  @Test(expected = NullPointerException.class)
  public void testBuildKeywordWithId_whenNullString_thenThrows() {
    service.buildKeywordWithId((String) null);
  }

  // --- exists ---

  @Test
  public void testExists_whenNullKeyword_thenReturnsFalse() {
    assertFalse(service.exists(null));
  }

  @Test
  public void testExists_whenKeywordWithNullId_thenReturnsFalse() {
    Keyword keyword = new KeywordImpl((NodeRef) null, "test");
    assertFalse(service.exists(keyword));
  }

  @Test
  public void testExists_whenNodeExists_thenReturnsTrue() {
    when(nodeService.exists(keywordNodeRef)).thenReturn(true);
    Keyword keyword = new KeywordImpl(keywordNodeRef, "test");

    assertTrue(service.exists(keyword));
  }

  // --- updateKeyword ---

  @Test(expected = NullPointerException.class)
  public void testUpdateKeyword_whenNull_thenThrows() {
    service.updateKeyword(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateKeyword_whenNotTranslated_thenThrows() {
    Keyword keyword = new KeywordImpl(keywordNodeRef, "simple");
    service.updateKeyword(keyword);
  }

  // --- setKeywordsToNode ---

  @Test(expected = NullPointerException.class)
  public void testSetKeywordsToNode_whenNullKeywords_thenThrows() {
    service.setKeywordsToNode(documentNodeRef, null);
  }

  @Test
  public void testSetKeywordsToNode_whenValidKeywords_thenSetsProperty() {
    when(
      nodeService.hasAspect(documentNodeRef, DocumentModel.ASPECT_CPROPERTIES)
    ).thenReturn(true);
    when(
      nodeService.getProperty(documentNodeRef, DocumentModel.PROP_KEYWORD)
    ).thenReturn(null);

    List<Keyword> keywords = List.of(new KeywordImpl(keywordNodeRef, "test"));
    service.setKeywordsToNode(documentNodeRef, keywords);

    verify(nodeService).setProperty(
      eq(documentNodeRef),
      eq(DocumentModel.PROP_KEYWORD),
      any(Serializable.class)
    );
  }

  // --- getKeywordsForNode ---

  @Test
  public void testGetKeywordsForNode_whenNoKeywords_thenReturnsEmptyList() {
    when(
      nodeService.getProperty(documentNodeRef, DocumentModel.PROP_KEYWORD)
    ).thenReturn(null);

    List<Keyword> result = service.getKeywordsForNode(documentNodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetKeywordsForNode_whenKeywordsExist_thenReturnsList() {
    ArrayList<NodeRef> refs = new ArrayList<>();
    refs.add(keywordNodeRef);
    when(
      nodeService.getProperty(documentNodeRef, DocumentModel.PROP_KEYWORD)
    ).thenReturn(refs);
    when(nodeService.exists(keywordNodeRef)).thenReturn(true);
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(Boolean.FALSE);
    when(
      nodeService.getProperty(keywordNodeRef, ContentModel.PROP_TITLE)
    ).thenReturn("transport");

    List<Keyword> result = service.getKeywordsForNode(documentNodeRef);

    assertEquals(1, result.size());
    assertEquals("transport", result.get(0).getValue());
  }

  // --- isKeywordMultilingual ---

  @Test
  public void testIsKeywordMultilingual_whenTranslated_thenReturnsTrue() {
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(Boolean.TRUE);
    Keyword keyword = new KeywordImpl(keywordNodeRef, "test");

    assertTrue(service.isKeywordMultilingual(keyword));
  }

  @Test
  public void testIsKeywordMultilingual_whenNotTranslated_thenReturnsFalse() {
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(Boolean.FALSE);
    Keyword keyword = new KeywordImpl(keywordNodeRef, "test");

    assertFalse(service.isKeywordMultilingual(keyword));
  }

  @Test
  public void testIsKeywordMultilingual_whenNull_thenReturnsFalse() {
    when(
      nodeService.getProperty(keywordNodeRef, KeywordModel.PROP_TRANSLATED)
    ).thenReturn(null);
    Keyword keyword = new KeywordImpl(keywordNodeRef, "test");

    assertFalse(service.isKeywordMultilingual(keyword));
  }

  // --- createKeyword with multilingual ---

  @Test
  public void testCreateKeyword_whenTranslated_thenCreatesMLKeyword() {
    MLText mlValues = new MLText();
    mlValues.put(java.util.Locale.ENGLISH, "environment");
    mlValues.put(java.util.Locale.FRENCH, "environnement");
    Keyword keyword = new KeywordImpl((NodeRef) null, mlValues);

    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerNodeRef);
    when(
      nodeService.getChildAssocs(eq(igNodeRef), any(QName.class), any())
    ).thenReturn(List.of(containerAssoc));

    ChildAssociationRef kwAssoc = mock(ChildAssociationRef.class);
    when(kwAssoc.getChildRef()).thenReturn(keywordNodeRef);
    when(
      nodeService.createNode(
        eq(containerNodeRef),
        eq(KeywordModel.ASSOC_KEYWORDS),
        eq(KeywordModel.TYPE_KEYWORD),
        eq(KeywordModel.TYPE_KEYWORD),
        any()
      )
    ).thenReturn(kwAssoc);

    Keyword result = service.createKeyword(igNodeRef, keyword);

    assertNotNull(result);
    assertEquals(keywordNodeRef, result.getId());
    verify(nodeService).setProperty(
      eq(keywordNodeRef),
      eq(ContentModel.PROP_TITLE),
      eq(mlValues)
    );
    verify(nodeService).setProperty(
      keywordNodeRef,
      KeywordModel.PROP_TRANSLATED,
      Boolean.TRUE
    );
  }

  // --- getOrCreateKeywordContainer ---

  @Test(expected = IllegalArgumentException.class)
  public void testGetOrCreateKeywordContainer_whenNullIg_thenThrows() {
    service.getOrCreateKeywordContainer(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetOrCreateKeywordContainer_whenNotIgRoot_thenThrows() {
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    service.getOrCreateKeywordContainer(igNodeRef, false);
  }

  // --- getIgFromKeyword ---

  @Test
  public void testGetIgFromKeyword_whenKeywordInIg_thenReturnsIg() {
    NodeRef intermediateRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "intermediate-id"
    );
    ChildAssociationRef kwParentAssoc = mock(ChildAssociationRef.class);
    when(kwParentAssoc.getParentRef()).thenReturn(intermediateRef);
    when(nodeService.getPrimaryParent(keywordNodeRef)).thenReturn(
      kwParentAssoc
    );
    when(
      nodeService.hasAspect(keywordNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    ChildAssociationRef intermediateParentAssoc = mock(
      ChildAssociationRef.class
    );
    when(intermediateParentAssoc.getParentRef()).thenReturn(igNodeRef);
    when(nodeService.getPrimaryParent(intermediateRef)).thenReturn(
      intermediateParentAssoc
    );
    when(
      nodeService.hasAspect(intermediateRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    NodeRef result = service.getIgFromKeyword(keywordNodeRef);

    assertEquals(igNodeRef, result);
  }

  // --- addKeywordToNode ---

  @Test
  public void testAddKeywordToNode_whenValidDocument_thenAddsKeyword() {
    Keyword keyword = new KeywordImpl(keywordNodeRef, "test");
    when(
      nodeService.hasAspect(documentNodeRef, DocumentModel.ASPECT_CPROPERTIES)
    ).thenReturn(true);
    when(
      nodeService.getProperty(documentNodeRef, DocumentModel.PROP_KEYWORD)
    ).thenReturn(null);

    service.addKeywordToNode(documentNodeRef, keyword);

    verify(nodeService).setProperty(
      eq(documentNodeRef),
      eq(DocumentModel.PROP_KEYWORD),
      any(java.io.Serializable.class)
    );
  }
}
