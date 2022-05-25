package io.swagger.api;

import eu.cec.digit.circabc.repo.keywords.KeywordImpl;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.Converter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beaurpi
 */
public class KeywordsApiImpl implements KeywordsApi {

    private NodeService secureNodeService;
    private KeywordsService keywordsService;

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.KeywordsApi#groupsIdKeywordsGet(java.lang.String)
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
     * @return the secureNodeService
     */
    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    /**
     * @param secureNodeService the secureNodeService to set
     */
    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @return the keywordsService
     */
    public KeywordsService getKeywordsService() {
        return keywordsService;
    }

    /**
     * @param keywordsService the keywordsService to set
     */
    public void setKeywordsService(KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
    }

    @Override
    public void keywordsKeywordIdDelete(String keywordId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(keywordId);
        Keyword keyword = keywordsService.buildKeywordWithId(nodeRef);
        keywordsService.removeKeyword(keyword);
    }

    @Override
    public KeywordDefinition groupsIdKeywordsPost(String id, KeywordDefinition body) {
        NodeRef igNodeRef = Converter.createNodeRefFromId(id);

        KeywordImpl kTmp = new KeywordImpl(Converter.toMLText(body.getTitle()));

        Keyword createdKeyword = keywordsService.createKeyword(igNodeRef, kTmp);

        KeywordDefinition result = new KeywordDefinition();
        result.setId(createdKeyword.getId().getId());
        result.setTitle(Converter.toI18NProperty(createdKeyword.getMLValues()));

        return result;
    }

    @Override
    public KeywordDefinition keywordsKeywordIdPut(String id, KeywordDefinition body) {
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

    @Override
    public List<KeywordDefinition> nodesIdKeywordsGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        List<KeywordDefinition> result = new ArrayList<>();
        if (secureNodeService.exists(nodeRef)) {
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

    @Override
    public void nodesIdKeywordsPost(String id, KeywordDefinition body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        List<Keyword> lKeys = keywordsService.getKeywordsForNode(nodeRef);
        NodeRef nodeRefTmp = Converter.createNodeRefFromId(body.getId());
        lKeys.add(keywordsService.buildKeywordWithId(nodeRefTmp));

        keywordsService.setKeywordsToNode(nodeRef, lKeys);
    }

    @Override
    public KeywordDefinition keywordIdGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        Keyword keyword = keywordsService.buildKeywordWithId(nodeRef);
        KeywordDefinition kwd = new KeywordDefinition();
        kwd.setId(keyword.getId().getId());
        kwd.setTitle(Converter.toI18NProperty(keyword.getMLValues()));
        return kwd;
    }

    @Override
    public Keyword keywordIdOldGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        Keyword keyword = keywordsService.buildKeywordWithId(nodeRef);
        return keyword;
    }
}
