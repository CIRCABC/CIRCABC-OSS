package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import io.swagger.model.KeywordDefinition;
import java.util.List;

/**
 * Business API for managing keywords (also referred to as tags) within CIRCABC.
 *
 * <p>Keywords exist at two levels:
 *
 * <ul>
 *   <li><b>Interest Group level</b> &mdash; a group maintains a list of {@link KeywordDefinition}s
 *       that can be created, updated, listed and deleted.
 *   <li><b>Node level</b> &mdash; individual content nodes can be tagged with keywords, which can be
 *       listed, added and removed.
 * </ul>
 *
 * <p>Implementations of this interface contain the logic invoked by the corresponding Alfresco
 * webscript endpoints; the method names mirror the REST URL patterns and HTTP verbs they serve.
 *
 * @author beaurpi
 */
public interface KeywordsApi {
  /**
   * Returns the list of keyword definitions declared for an Interest Group.
   *
   * <p>Serves {@code GET /groups/{id}/keywords/}.
   *
   * @param id the identifier of the Interest Group whose keyword definitions are requested
   * @return the list of {@link KeywordDefinition}s defined for the group
   */
  List<KeywordDefinition> groupsIdKeywordsGet(String id);

  /**
   * Deletes a keyword definition from an Interest Group.
   *
   * <p>Serves {@code DELETE /keywords/{keywordId}}.
   *
   * @param keywordId the identifier of the keyword definition to delete
   */
  void keywordsKeywordIdDelete(String keywordId);

  /**
   * Creates a new keyword in the list of keyword definitions of an Interest Group.
   *
   * <p>Serves {@code POST /groups/{id}/keywords/}.
   *
   * @param id the identifier of the Interest Group to which the keyword is added
   * @param body the keyword definition to create
   * @return the created {@link KeywordDefinition}
   */
  KeywordDefinition groupsIdKeywordsPost(String id, KeywordDefinition body);

  /**
   * Updates the definition of an existing keyword.
   *
   * <p>Serves {@code PUT /keywords/{keywordId}}.
   *
   * @param id the identifier of the keyword definition to update
   * @param body the new keyword definition values
   * @return the updated {@link KeywordDefinition}
   */
  KeywordDefinition keywordsKeywordIdPut(String id, KeywordDefinition body);

  /**
   * Returns the keywords (tags) associated with a single content node.
   *
   * <p>Serves {@code GET /nodes/{id}/keywords}.
   *
   * @param id the identifier of the node whose keywords are requested
   * @return the list of {@link KeywordDefinition}s associated with the node
   */
  List<KeywordDefinition> nodesIdKeywordsGet(String id);

  /**
   * Removes a keyword (tag) from a single content node.
   *
   * <p>Serves {@code DELETE /nodes/{id}/keywords/{keywordId}}.
   *
   * @param id the identifier of the node from which the keyword is removed
   * @param keywordId the identifier of the keyword to remove from the node
   */
  void nodesIdKeywordsKeywordIdDelete(String id, String keywordId);

  /**
   * Adds a new keyword (tag) to a content node.
   *
   * <p>Serves {@code POST /nodes/{id}/keywords}.
   *
   * @param id the identifier of the node to tag
   * @param body the keyword to add to the node
   */
  void nodesIdKeywordsPost(String id, KeywordDefinition body);

  /**
   * Returns a single keyword definition identified by its node reference id.
   *
   * @param id the node reference identifier of the keyword definition
   * @return the matching {@link KeywordDefinition}
   */
  KeywordDefinition keywordIdGet(String id);

  /**
   * Returns a single keyword in the legacy {@link Keyword} representation, identified by its node
   * reference id.
   *
   * @param id the node reference identifier of the keyword
   * @return the matching {@link Keyword}
   */
  Keyword keywordIdOldGet(String id);
}
