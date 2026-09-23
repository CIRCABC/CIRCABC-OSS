package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordImpl;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import io.swagger.model.I18nProperty;
import io.swagger.model.KeywordDefinition;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class KeywordsApiImplTest {

  private KeywordsApiImpl keywordsApi;
  private NodeService nodeService;
  private KeywordsService keywordsService;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  @Before
  public void setUp() throws Exception {
    keywordsApi = new KeywordsApiImpl();
    nodeService = mock(NodeService.class);
    keywordsService = mock(KeywordsService.class);
    setField("nodeService", nodeService);
    setField("keywordsService", keywordsService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = KeywordsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(keywordsApi, value);
  }

  @Test
  public void testGroupsIdKeywordsGet_whenKeywordsExist_thenReturnsList() {
    MLText mlText = new MLText(Locale.ENGLISH, "keyword1");
    Keyword keyword = mock(Keyword.class);
    when(keyword.getId()).thenReturn(TEST_NODE_REF);
    when(keyword.getMLValues()).thenReturn(mlText);
    when(keywordsService.getKeywords(TEST_NODE_REF)).thenReturn(
      Collections.singletonList(keyword)
    );

    List<KeywordDefinition> result = keywordsApi.groupsIdKeywordsGet(TEST_ID);

    assertEquals(1, result.size());
    assertEquals(TEST_ID, result.get(0).getId());
    assertNotNull(result.get(0).getTitle());
  }

  @Test
  public void testGroupsIdKeywordsGet_whenNoKeywords_thenReturnsEmptyList() {
    when(keywordsService.getKeywords(TEST_NODE_REF)).thenReturn(
      Collections.emptyList()
    );

    List<KeywordDefinition> result = keywordsApi.groupsIdKeywordsGet(TEST_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testKeywordsKeywordIdDelete_whenCalled_thenRemovesKeyword() {
    Keyword keyword = mock(Keyword.class);
    when(keywordsService.buildKeywordWithId(TEST_NODE_REF)).thenReturn(keyword);

    keywordsApi.keywordsKeywordIdDelete(TEST_ID);

    verify(keywordsService).buildKeywordWithId(TEST_NODE_REF);
    verify(keywordsService).removeKeyword(keyword);
  }

  @Test
  public void testGroupsIdKeywordsPost_whenCalled_thenCreatesAndReturnsKeyword() {
    KeywordDefinition body = new KeywordDefinition();
    I18nProperty title = new I18nProperty();
    title.put("en", "new keyword");
    body.setTitle(title);

    Keyword createdKeyword = mock(Keyword.class);
    MLText mlText = new MLText(Locale.ENGLISH, "new keyword");
    when(createdKeyword.getId()).thenReturn(TEST_NODE_REF);
    when(createdKeyword.getMLValues()).thenReturn(mlText);
    when(
      keywordsService.createKeyword(eq(TEST_NODE_REF), any(KeywordImpl.class))
    ).thenReturn(createdKeyword);

    KeywordDefinition result = keywordsApi.groupsIdKeywordsPost(TEST_ID, body);

    assertNotNull(result);
    assertEquals(TEST_ID, result.getId());
    verify(keywordsService).createKeyword(
      eq(TEST_NODE_REF),
      any(KeywordImpl.class)
    );
  }

  @Test
  public void testKeywordsKeywordIdPut_whenCalled_thenUpdatesAndReturnsKeyword() {
    KeywordDefinition body = new KeywordDefinition();
    I18nProperty title = new I18nProperty();
    title.put("en", "updated keyword");
    body.setTitle(title);

    Keyword keyword = mock(Keyword.class);
    MLText mlText = new MLText(Locale.ENGLISH, "updated keyword");
    when(keyword.getId()).thenReturn(TEST_NODE_REF);
    when(keyword.getMLValues()).thenReturn(mlText);
    when(keywordsService.buildKeywordWithId(TEST_NODE_REF)).thenReturn(keyword);

    KeywordDefinition result = keywordsApi.keywordsKeywordIdPut(TEST_ID, body);

    assertNotNull(result);
    assertEquals(TEST_ID, result.getId());
    verify(keywordsService).updateKeyword(keyword);
  }

  @Test
  public void testNodesIdKeywordsGet_whenNodeExists_thenReturnsKeywords() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    Keyword keyword = mock(Keyword.class);
    MLText mlText = new MLText(Locale.ENGLISH, "node keyword");
    when(keyword.getId()).thenReturn(TEST_NODE_REF);
    when(keyword.getMLValues()).thenReturn(mlText);
    when(keywordsService.getKeywordsForNode(TEST_NODE_REF)).thenReturn(
      Collections.singletonList(keyword)
    );

    List<KeywordDefinition> result = keywordsApi.nodesIdKeywordsGet(TEST_ID);

    assertEquals(1, result.size());
    assertEquals(TEST_ID, result.get(0).getId());
  }

  @Test
  public void testNodesIdKeywordsGet_whenNodeDoesNotExist_thenReturnsEmptyList() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    List<KeywordDefinition> result = keywordsApi.nodesIdKeywordsGet(TEST_ID);

    assertTrue(result.isEmpty());
    verify(keywordsService, never()).getKeywordsForNode(any());
  }

  @Test
  public void testNodesIdKeywordsPost_whenCalled_thenAddsKeywordToNode() {
    String keywordId = "keyword-node-id";
    NodeRef keywordNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      keywordId
    );

    KeywordDefinition body = new KeywordDefinition();
    body.setId(keywordId);

    List<Keyword> existingKeywords = new ArrayList<>();
    when(keywordsService.getKeywordsForNode(TEST_NODE_REF)).thenReturn(
      existingKeywords
    );

    Keyword newKeyword = mock(Keyword.class);
    when(keywordsService.buildKeywordWithId(keywordNodeRef)).thenReturn(
      newKeyword
    );

    keywordsApi.nodesIdKeywordsPost(TEST_ID, body);

    verify(keywordsService).setKeywordsToNode(eq(TEST_NODE_REF), anyList());
  }

  @Test
  public void testKeywordIdGet_whenCalled_thenReturnsKeywordDefinition() {
    Keyword keyword = mock(Keyword.class);
    MLText mlText = new MLText(Locale.ENGLISH, "test keyword");
    when(keyword.getId()).thenReturn(TEST_NODE_REF);
    when(keyword.getMLValues()).thenReturn(mlText);
    when(keywordsService.buildKeywordWithId(TEST_NODE_REF)).thenReturn(keyword);

    KeywordDefinition result = keywordsApi.keywordIdGet(TEST_ID);

    assertNotNull(result);
    assertEquals(TEST_ID, result.getId());
  }

  @Test
  public void testKeywordIdOldGet_whenCalled_thenReturnsKeyword() {
    Keyword keyword = mock(Keyword.class);
    when(keywordsService.buildKeywordWithId(TEST_NODE_REF)).thenReturn(keyword);

    Keyword result = keywordsApi.keywordIdOldGet(TEST_ID);

    assertSame(keyword, result);
  }
}
