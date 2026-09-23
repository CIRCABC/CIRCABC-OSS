package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordImpl;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.Converter;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the {@link KeywordsApi} business operations.
 *
 * <p>Keywords are taxonomy terms that can be defined at the level of an Interest
 * Group and then associated with individual content nodes. This class handles the
 * full lifecycle of keywords:
 *
 * <ul>
 *   <li>listing, creating, updating and deleting keyword definitions owned by an
 *       Interest Group;</li>
 *   <li>listing the keywords associated with a specific node and adding or
 *       removing those associations.</li>
 * </ul>
 *
 * <p>Node identifiers received as {@code String} values are converted to Alfresco
 * {@link NodeRef} instances via {@link Converter}, and the internal
 * {@link Keyword} domain objects are mapped to {@link KeywordDefinition} DTOs for
 * the REST layer. The actual persistence and repository interactions are delegated
 * to {@link KeywordsService}.
 *
 * @author beaurpi
 */
public class KeywordsApiImpl implements KeywordsApi {

  /** Alfresco service used to verify node existence before reading keywords. */
  @Autowired
  private NodeService nodeService;

  /** Service encapsulating keyword persistence and repository operations. */
  @Autowired
  private KeywordsService keywordsService;

  /**
   * Returns all keyword definitions declared for the given Interest Group.
   *
   * @param id the identifier of the Interest Group node whose keywords are
   *     requested
   * @return the list of {@link KeywordDefinition} objects defined for the group;
   *     an empty list when the group has no keywords
   */
  @Override
  public List<KeywordDefinition> groupsIdKeywordsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    List<KeywordDefinition> result = new ArrayList<>();
    for (Keyword kw : keywordsService.getKeywords(nodeRef)) {
      KeywordDefinition kwd = new KeywordDefinition();
      kwd.setId(kw.getId().getId());
      kwd.setTitle(Converter.toI18NProperty(kw.getMLValues()));
      result.add(kwd);
    }
    return result;
  }

  /**
   * Deletes the keyword definition identified by the given id.
   *
   * @param keywordId the identifier of the keyword node to delete
   */
  @Override
  public void keywordsKeywordIdDelete(String keywordId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(keywordId);
    Keyword keyword = keywordsService.buildKeywordWithId(nodeRef);
    keywordsService.removeKeyword(keyword);
  }

  /**
   * Creates a new keyword definition within the given Interest Group.
   *
   * @param id the identifier of the Interest Group node in which the keyword is
   *     created
   * @param body the keyword definition to create; its multilingual title is used
   *     as the keyword translations
   * @return the created {@link KeywordDefinition}, populated with the generated
   *     identifier and stored translations
   */
  @Override
  public KeywordDefinition groupsIdKeywordsPost(
    String id,
    KeywordDefinition body
  ) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(id);

    KeywordImpl kTmp = new KeywordImpl(Converter.toMLText(body.getTitle()));

    Keyword createdKeyword = keywordsService.createKeyword(igNodeRef, kTmp);

    KeywordDefinition result = new KeywordDefinition();
    result.setId(createdKeyword.getId().getId());
    result.setTitle(Converter.toI18NProperty(createdKeyword.getMLValues()));

    return result;
  }

  /**
   * Updates the multilingual title of an existing keyword definition.
   *
   * @param id the identifier of the keyword node to update
   * @param body the keyword definition carrying the new multilingual title
   * @return the updated {@link KeywordDefinition}, re-read from the repository
   *     after the update
   */
  @Override
  public KeywordDefinition keywordsKeywordIdPut(
    String id,
    KeywordDefinition body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    Keyword k = keywordsService.buildKeywordWithId(nodeRef);
    k.setTranlatations(Converter.toMLText(body.getTitle()));

    keywordsService.updateKeyword(k);

    k = keywordsService.buildKeywordWithId(nodeRef);

    KeywordDefinition result = new KeywordDefinition();
    result.setId(k.getId().getId());
    result.setTitle(Converter.toI18NProperty(k.getMLValues()));

    return result;
  }

  /**
   * Returns the keywords currently associated with the given content node.
   *
   * @param id the identifier of the node whose associated keywords are requested
   * @return the list of {@link KeywordDefinition} objects associated with the
   *     node; an empty list when the node does not exist or has no keywords
   */
  @Override
  public List<KeywordDefinition> nodesIdKeywordsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    List<KeywordDefinition> result = new ArrayList<>();
    if (nodeService.exists(nodeRef)) {
      List<Keyword> lKeys = keywordsService.getKeywordsForNode(nodeRef);
      for (Keyword kw : lKeys) {
        KeywordDefinition kwd = new KeywordDefinition();
        kwd.setId(kw.getId().getId());
        kwd.setTitle(Converter.toI18NProperty(kw.getMLValues()));
        result.add(kwd);
      }
    }

    return result;
  }

  /**
   * Removes the association between a node and a single keyword.
   *
   * <p>The node's remaining keywords are recomputed by excluding the target
   * keyword and then re-applied to the node.
   *
   * @param id the identifier of the node from which the keyword is removed
   * @param keywordId the identifier of the keyword association to remove
   */
  @Override
  public void nodesIdKeywordsKeywordIdDelete(String id, String keywordId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    List<KeywordDefinition> existingKeywords = nodesIdKeywordsGet(id);
    List<Keyword> futureKeywords = new ArrayList<>();
    for (KeywordDefinition kwd : existingKeywords) {
      if (!kwd.getId().equals(keywordId)) {
        NodeRef nodeRefTmp = Converter.createNodeRefFromId(kwd.getId());
        futureKeywords.add(keywordsService.buildKeywordWithId(nodeRefTmp));
      }
    }

    keywordsService.setKeywordsToNode(nodeRef, futureKeywords);
  }

  /**
   * Associates an existing keyword with the given node.
   *
   * <p>The keyword identified by {@code body.getId()} is appended to the node's
   * current set of keywords.
   *
   * @param id the identifier of the node to which the keyword is added
   * @param body the keyword definition whose id identifies the keyword to
   *     associate
   */
  @Override
  public void nodesIdKeywordsPost(String id, KeywordDefinition body) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    List<Keyword> lKeys = keywordsService.getKeywordsForNode(nodeRef);
    NodeRef nodeRefTmp = Converter.createNodeRefFromId(body.getId());
    lKeys.add(keywordsService.buildKeywordWithId(nodeRefTmp));

    keywordsService.setKeywordsToNode(nodeRef, lKeys);
  }

  /**
   * Returns a single keyword definition by its identifier.
   *
   * @param id the identifier of the keyword node to retrieve
   * @return the corresponding {@link KeywordDefinition}
   */
  @Override
  public KeywordDefinition keywordIdGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    Keyword keyword = keywordsService.buildKeywordWithId(nodeRef);
    KeywordDefinition kwd = new KeywordDefinition();
    kwd.setId(keyword.getId().getId());
    kwd.setTitle(Converter.toI18NProperty(keyword.getMLValues()));
    return kwd;
  }

  /**
   * Returns the raw {@link Keyword} domain object for the given identifier.
   *
   * <p>This legacy variant exposes the internal domain model directly rather than
   * the {@link KeywordDefinition} DTO.
   *
   * @param id the identifier of the keyword node to retrieve
   * @return the corresponding {@link Keyword} domain object
   */
  @Override
  public Keyword keywordIdOldGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    return keywordsService.buildKeywordWithId(nodeRef);
  }
}
