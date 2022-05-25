package io.swagger.api;

import eu.cec.digit.circabc.service.keyword.Keyword;
import io.swagger.model.KeywordDefinition;

import java.util.List;

/**
 * @author beaurpi
 */
public interface KeywordsApi {

    /**
     * get the list of keyword definition of an Interest group /groups/{id}/keywords/
     *
     * @return KeywordDefinition
     */
    List<KeywordDefinition> groupsIdKeywordsGet(String id);

    /**
     * deletes a keyword definition in an Interest group /keywords/{keywordId}
     */
    void keywordsKeywordIdDelete(String keywordId);

    /**
     * creates a new keyword in the list of keyword definition of an Interest group
     * /groups/{id}/keywords/
     *
     * @return KeywordDefinition
     */
    KeywordDefinition groupsIdKeywordsPost(String id, KeywordDefinition body);

    /**
     * updates the definition of a keyword
     */
    KeywordDefinition keywordsKeywordIdPut(String id, KeywordDefinition body);

    /**
     * get the keywords / tags related to one node /nodes/{id}/keywords
     */
    List<KeywordDefinition> nodesIdKeywordsGet(String id);

    /**
     * remove the keyword / tag related to one node /nodes/{id}/keywords/{keywordId}
     */
    void nodesIdKeywordsKeywordIdDelete(String id, String keywordId);

    /**
     * add a new keyword to a node /nodes/{id}/keywords
     */
    void nodesIdKeywordsPost(String id, KeywordDefinition body);

    /**
     * get one keyword definition base on noderef id
     *
     * @param id
     * @return
     */
    KeywordDefinition keywordIdGet(String id);

    Keyword keywordIdOldGet(String id);
}
