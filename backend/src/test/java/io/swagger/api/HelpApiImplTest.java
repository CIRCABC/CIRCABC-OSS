package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import io.swagger.model.I18nProperty;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class HelpApiImplTest {

  private HelpApiImpl helpApi;

  private NodeService nodeService;
  private PersonService personService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;
  private CircabcApi circabcApi;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );
  private static final NodeRef DD_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "dictionary-id"
  );
  private static final NodeRef FAQS_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "faqs-id"
  );
  private static final NodeRef FAQS_LINKS_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "faqs-links-id"
  );
  private static final NodeRef PERSON_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "person-id"
  );

  @Before
  public void setUp() throws Exception {
    helpApi = new HelpApiImpl();

    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);
    searchService = mock(SearchService.class);
    apiToolBox = mock(ApiToolBox.class);
    circabcApi = mock(CircabcApi.class);

    setField("nodeService", nodeService);
    setField("personService", personService);
    setField("searchService", searchService);
    setField("apiToolBox", apiToolBox);
    setField("circabcApi", circabcApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpApi, value);
  }

  @Test
  public void testGetHelpCategories_whenFaqsHasChildren_thenReturnsList() {
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(DD_REF);
    when(
      nodeService.getChildByName(DD_REF, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(FAQS_REF);

    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    ChildAssociationRef childAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      FAQS_REF,
      ContentModel.ASSOC_CONTAINS,
      childRef
    );
    when(nodeService.getChildAssocs(FAQS_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(
      nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ).thenReturn(true);
    when(nodeService.getProperty(childRef, ContentModel.PROP_TITLE)).thenReturn(
      "Test Category"
    );
    when(nodeService.getChildAssocs(childRef)).thenReturn(new ArrayList<>());

    List<HelpCategory> result = helpApi.getHelpCategories();

    assertEquals(1, result.size());
    assertEquals("cat-id", result.get(0).getId());
  }

  @Test
  public void testGetHelpCategories_whenFaqsIsNull_thenReturnsEmptyList() {
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(DD_REF);
    when(
      nodeService.getChildByName(DD_REF, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(null);

    List<HelpCategory> result = helpApi.getHelpCategories();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetHelpCategory_whenNodeExists_thenReturnsCategory() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_TITLE)
    ).thenReturn("My Category");
    when(nodeService.getChildAssocs(TEST_NODE_REF)).thenReturn(
      new ArrayList<>()
    );

    HelpCategory result = helpApi.getHelpCategory(TEST_ID);

    assertEquals(TEST_ID, result.getId());
    assertEquals(0, (int) result.getNumberOfArticles());
  }

  @Test
  public void testDeleteHelpArticle_whenNodeIsArticle_thenDeletes() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_ARTICLE)
    ).thenReturn(true);

    helpApi.deleteHelpArticle(TEST_ID);

    verify(nodeService).deleteNode(TEST_NODE_REF);
  }

  @Test(expected = InvalidArgumentException.class)
  public void testDeleteHelpArticle_whenNodeIsNotArticle_thenThrows() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_ARTICLE)
    ).thenReturn(false);

    helpApi.deleteHelpArticle(TEST_ID);
  }

  @Test
  public void testDeleteHelpCategory_whenNodeIsCategory_thenDeletes() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_CATEGORY)
    ).thenReturn(true);

    helpApi.deleteHelpCategory(TEST_ID);

    verify(nodeService).deleteNode(TEST_NODE_REF);
  }

  @Test(expected = InvalidArgumentException.class)
  public void testDeleteHelpCategory_whenNodeIsNotCategory_thenThrows() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_CATEGORY)
    ).thenReturn(false);

    helpApi.deleteHelpCategory(TEST_ID);
  }

  @Test
  public void testToggleHighlightArticle_whenNotHighlighted_thenAddsAspect() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_ARTICLE)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(
        TEST_NODE_REF,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      )
    ).thenReturn(false);

    // Mock for getHelpArticle called internally
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Article Title");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("Content");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_MODIFIER)
    ).thenReturn("admin");
    when(personService.getPerson("admin")).thenReturn(PERSON_REF);
    when(
      nodeService.getProperty(PERSON_REF, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(PERSON_REF, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_MODIFIED)
    ).thenReturn(new java.util.Date());
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      FAQS_REF,
      ContentModel.ASSOC_CONTAINS,
      TEST_NODE_REF
    );
    when(nodeService.getPrimaryParent(TEST_NODE_REF)).thenReturn(parentAssoc);

    helpApi.toggleHighlightArticle(TEST_ID);

    verify(nodeService).addAspect(
      TEST_NODE_REF,
      CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED,
      null
    );
  }

  @Test
  public void testToggleHighlightArticle_whenHighlighted_thenRemovesAspect() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_ARTICLE)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(
        TEST_NODE_REF,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      )
    ).thenReturn(true);

    // Mock for getHelpArticle called internally
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Article Title");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("Content");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_MODIFIER)
    ).thenReturn("admin");
    when(personService.getPerson("admin")).thenReturn(PERSON_REF);
    when(
      nodeService.getProperty(PERSON_REF, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(PERSON_REF, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_MODIFIED)
    ).thenReturn(new java.util.Date());
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      FAQS_REF,
      ContentModel.ASSOC_CONTAINS,
      TEST_NODE_REF
    );
    when(nodeService.getPrimaryParent(TEST_NODE_REF)).thenReturn(parentAssoc);

    helpApi.toggleHighlightArticle(TEST_ID);

    verify(nodeService).removeAspect(
      TEST_NODE_REF,
      CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
    );
  }

  @Test(expected = InvalidArgumentException.class)
  public void testToggleHighlightArticle_whenNodeNotExists_thenThrows() {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    helpApi.toggleHighlightArticle(TEST_ID);
  }

  @Test
  public void testGetHelpLinks_whenLinksExist_thenReturnsList() {
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(DD_REF);
    when(
      nodeService.getChildByName(
        DD_REF,
        ContentModel.ASSOC_CONTAINS,
        "faqsLinks"
      )
    ).thenReturn(FAQS_LINKS_REF);

    NodeRef linkRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "link-id"
    );
    ChildAssociationRef childAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      FAQS_LINKS_REF,
      ContentModel.ASSOC_CONTAINS,
      linkRef
    );
    when(nodeService.getChildAssocs(FAQS_LINKS_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(
      nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)
    ).thenReturn(true);
    when(nodeService.getProperty(linkRef, ContentModel.PROP_TITLE)).thenReturn(
      "Link Title"
    );
    when(
      nodeService.getProperty(linkRef, CircabcModel.PROP_HELP_LINK_HREF)
    ).thenReturn("https://example.com");

    List<HelpLink> result = helpApi.getHelpLinks();

    assertEquals(1, result.size());
    assertEquals("link-id", result.get(0).getId());
    assertEquals("https://example.com", result.get(0).getHref());
  }

  @Test
  public void testDeleteHelpLink_whenNodeIsLink_thenDeletes() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_LINK)
    ).thenReturn(true);

    helpApi.deleteHelpLink(TEST_ID);

    verify(nodeService).deleteNode(TEST_NODE_REF);
  }

  @Test
  public void testDeleteHelpLink_whenNodeIsNotLink_thenDoesNotDelete() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_LINK)
    ).thenReturn(false);

    helpApi.deleteHelpLink(TEST_ID);

    verify(nodeService, never()).deleteNode(any(NodeRef.class));
  }

  @Test
  public void testUpdateHelpCategory_whenTitleProvided_thenUpdatesProperty() {
    I18nProperty title = new I18nProperty();
    title.put("en", "Updated Title");

    HelpCategory category = new HelpCategory();
    category.setTitle(title);

    HelpCategory result = helpApi.updateHelpCategory(TEST_ID, category);

    verify(nodeService).setProperty(
      eq(TEST_NODE_REF),
      eq(ContentModel.PROP_TITLE),
      any()
    );
    assertEquals(title, result.getTitle());
  }

  @Test
  public void testSearchHelp_whenQueryMatches_thenReturnsResults() {
    when(circabcApi.getCircabcDictionaryNodeRef()).thenReturn(DD_REF);
    when(
      nodeService.getChildByName(DD_REF, ContentModel.ASSOC_CONTAINS, "faqs")
    ).thenReturn(FAQS_REF);
    when(
      nodeService.getChildByName(
        DD_REF,
        ContentModel.ASSOC_CONTAINS,
        "faqsLinks"
      )
    ).thenReturn(FAQS_LINKS_REF);
    when(apiToolBox.getPathFromSpaceRef(eq(FAQS_REF), eq(true))).thenReturn(
      "/app:company_home/faqs"
    );
    when(
      apiToolBox.getPathFromSpaceRef(eq(FAQS_LINKS_REF), eq(true))
    ).thenReturn("/app:company_home/faqsLinks");

    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(TEST_NODE_REF));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_CATEGORY)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_ARTICLE)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_HELP_LINK)
    ).thenReturn(false);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Test");
    when(nodeService.getChildAssocs(TEST_NODE_REF)).thenReturn(
      new ArrayList<>()
    );

    HelpSearchResult result = helpApi.searchHelp("test");

    assertNotNull(result);
    assertEquals(1, result.getCategories().size());
  }
}
