/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.keywords;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import io.swagger.util.ApiToolBox;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static eu.cec.digit.circabc.model.KeywordModel.*;

/**
 * Implementation for keywords operations.
 *
 * @author Yanick Pignot
 */
public class KeywordsServiceImpl implements KeywordsService {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(KeywordsServiceImpl.class);

    /**
     * Circabc Keywords association beetween the keyword elements and another keyword elements
     */
    private static final QName ASSOC_IG_KEYWORDCONTAINER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "igKwContainer");

    /**
     * The node service reference
     */
    private NodeService nodeService;
    /**
     * The permission service reference
     */
    private PermissionService permissionService;
    /**
     * The namespace service reference
     */
    private NamespaceService namespaceService;
    /**
     * The search service reference
     */
    private SearchService searchService;

    private ApiToolBox apiToolBox;

    public Keyword createKeyword(final NodeRef igNoderef, final Keyword keyword) {
        if (keyword == null) {
            throw new NullPointerException("Keyword is a mandatory parameter");
        }

        if (keyword.getId() != null) {
            throw new IllegalArgumentException(
                    "The keyword id can't be setted or use the updateKeyword method");
        }

        final boolean translated = keyword.isKeywordTranslated();
        final Serializable valueObject = translated ? keyword.getMLValues() : keyword.getValue();

        if (valueObject == null || valueObject.toString().trim().length() < 1) {
            throw new IllegalArgumentException("Keyword value is a mandatory parameter");
        }

        // Get the container
        final NodeRef container = getOrCreateKeywordContainer(igNoderef, true);

        // Create the keyword
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        container, ASSOC_KEYWORDS, TYPE_KEYWORD, TYPE_KEYWORD, new PropertyMap());

        final NodeRef newKeyword = assocRef.getChildRef();

        // if a locale is specified, set the keyword being a mlText
        if (translated) {
            this.setKeywordTranslationsNoderef(newKeyword, (MLText) valueObject);
        }
        // else, set the keyword normally
        else {
            nodeService.setProperty(newKeyword, ContentModel.PROP_TITLE, valueObject);
            // set the translated property defined by the model
            nodeService.setProperty(newKeyword, PROP_TRANSLATED, Boolean.FALSE);
        }

        //  add the descrition property defined by the titled aspect.
        nodeService.setProperty(newKeyword, ContentModel.PROP_DESCRIPTION, "");

        // done
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Added a keyword: \n"
                            + "   Interest group :  "
                            + igNoderef
                            + "\n"
                            + "   Keyword        : "
                            + keyword);
        }

        Keyword createdKeyword;

        if (valueObject instanceof MLText) {
            createdKeyword = new KeywordImpl(newKeyword, (MLText) valueObject);
        } else {
            createdKeyword = new KeywordImpl(newKeyword, (String) valueObject);
        }

        return createdKeyword;
    }

    public void addKeywordToNode(final NodeRef document, final Keyword keyword) {
        final ArrayList<NodeRef> keywords = new ArrayList<>(5);
        if (document != null) {
            keywords.add(keyword.getId());
        }

        setKeywordsToNodeImpl(document, keywords, false);
    }

    public List<Keyword> getKeywordsForNode(final NodeRef document) {
        final List<NodeRef> keywordsAsNoderef =
                (List<NodeRef>)
                        DefaultTypeConverter.INSTANCE.getCollection(
                                NodeRef.class, nodeService.getProperty(document, DocumentModel.PROP_KEYWORD));

        List<Keyword> keywords = null;

        if (keywordsAsNoderef == null) {
            keywords = Collections.emptyList();
        } else {
            keywords = new ArrayList<>(keywordsAsNoderef.size());
            for (final NodeRef ref : keywordsAsNoderef) {
                if (ref != null) {
                    keywords.add(buildKeywordWithId(ref));
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Keywords found for the node " + document + ": keywords");
        }

        return keywords;
    }

    public void removeKeyword(final Keyword keyword) {
        final List<NodeRef> keywordAsList = new ArrayList<>(1);

        if (keyword == null) {
            throw new IllegalArgumentException("Only a valid keyword noderef is required here");
        }

        keywordAsList.add(keyword.getId());

        final NodeRef ig = getIgFromKeyword(keyword.getId());

        if (!TYPE_KEYWORD.isMatch(nodeService.getType(keyword.getId()))) {
            throw new IllegalArgumentException("Only a valid keyword noderef is required here");
        }

        if (ig == null) {
            logger.warn("The model seems corrupted, no IG found for the keyword " + keyword);

            throw new IllegalArgumentException(
                    "Impossible to get the interest group of the keyword " + keyword);
        }

        final List<NodeRef> referencedDocument = getNodesForKeywordsNode(ig, keywordAsList);

        List<Keyword> keywords = null;

        for (final NodeRef ref : referencedDocument) {
            keywords = getKeywordsForNode(ref);
            keywords.remove(keyword);
            setKeywordsToNode(ref, keywords);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Keyword successfully dropped from the referenced documents: "
                            + "\n  Keyword: "
                            + keyword
                            + "\n  Documents: "
                            + referencedDocument);
        }

        nodeService.deleteNode(keyword.getId());

        if (logger.isDebugEnabled()) {
            logger.debug("Keyword successfully dropped from the ig: " + "\n  Keyword: " + keyword);
        }
    }

    public void setKeywordsToNode(final NodeRef document, final List<Keyword> keywords) {
        if (keywords == null) {
            throw new NullPointerException("The keywords are mandatory parameters");
        }

        final ArrayList<NodeRef> keywordsAsNoderef = new ArrayList<>(keywords.size());
        for (final Keyword k : keywords) {
            keywordsAsNoderef.add(k.getId());
        }

        setKeywordsToNodeImpl(document, keywordsAsNoderef, true);
    }

    public List<NodeRef> getNodesForKeywords(final NodeRef parent, final List<Keyword> keywords) {
        if (parent == null) {
            throw new NullPointerException("The parent node is a madatory parameter");
        }
        if (keywords == null || keywords.size() < 1) {
            throw new IllegalArgumentException("At least one keyword is required");
        }

        final List<NodeRef> keywordNoderef = new ArrayList<>(keywords.size());
        for (final Keyword key : keywords) {
            keywordNoderef.add(key.getId());
        }

        return getNodesForKeywordsNode(parent, keywordNoderef);
    }

    public List<Keyword> getKeywords(final NodeRef ig) {
        final List<NodeRef> keywordsAsNode = getKeywordsNode(ig);
        final List<Keyword> displayKeywords = new ArrayList<>(keywordsAsNode.size());

        for (final NodeRef ref : keywordsAsNode) {
            if (isKeywordNodeMultilingual(ref)) {
                final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

                try {
                    MLPropertyInterceptor.setMLAware(true);
                    final MLText title = (MLText) nodeService.getProperty(ref, ContentModel.PROP_TITLE);
                    displayKeywords.add(new KeywordImpl(ref, title));
                } finally {
                    MLPropertyInterceptor.setMLAware(wasMLAware);
                }
            } else {
                final String title = (String) nodeService.getProperty(ref, ContentModel.PROP_TITLE);
                displayKeywords.add(new KeywordImpl(ref, title));
            }
        }

        return displayKeywords;
    }

    public boolean isKeywordMultilingual(final Keyword keyword) {
        return isKeywordNodeMultilingual(keyword.getId());
    }

    public void updateKeyword(final Keyword keyword) {
        if (keyword == null) {
            throw new NullPointerException("Keyword is a mandatory parameter");
        }
        if (!keyword.isKeywordTranslated()) {
            throw new IllegalArgumentException("We can't update a keyword if it is not translated");
        }

        setKeywordTranslationsNoderef(keyword.getId(), keyword.getMLValues());
    }

    public Keyword buildKeywordWithId(final NodeRef id) throws InvalidNodeRefException {
        if (id == null) {
            throw new NullPointerException("The node id is a manadtory parameter");
        }

        if (!getNodeService().exists(id)) {
            return null;
        }

        Keyword key = null;

        if (isKeywordNodeMultilingual(id)) {
            final boolean wasMLAware = MLPropertyInterceptor.isMLAware();
            try {
                MLPropertyInterceptor.setMLAware(true);
                final MLText translations = (MLText) nodeService.getProperty(id, ContentModel.PROP_TITLE);
                key = new KeywordImpl(id, translations);
            } finally {
                MLPropertyInterceptor.setMLAware(wasMLAware);
            }
        } else {
            key = new KeywordImpl(id, (String) nodeService.getProperty(id, ContentModel.PROP_TITLE));
        }

        return key;
    }

    public Keyword buildKeywordWithId(final String id) throws InvalidNodeRefException {
        if (id == null) {
            throw new NullPointerException("The node id is a manadtory parameter");
        }

        return buildKeywordWithId(new NodeRef(id));
    }

    public boolean exists(final Keyword keyword) {
        if (keyword == null || keyword.getId() == null) {
            return false;
        } else {
            return getNodeService().exists(keyword.getId());
        }
    }

    // -- Helper methods

    protected NodeRef getIgFromKeyword(final NodeRef keyword) {
        NodeRef tempRef = keyword;
        for (; ; ) {
            if (tempRef == null) {
                // we are above Company home. No ig found
                break;
            } else if (nodeService.hasAspect(tempRef, CircabcModel.ASPECT_IGROOT)) {
                // return the ig
                break;
            } else {
                tempRef = nodeService.getPrimaryParent(tempRef).getParentRef();
            }
        }
        return tempRef;
    }

    protected List<NodeRef> getNodesForKeywordsNode(
            final NodeRef parent, final List<NodeRef> keywords) {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        if (keywords == null || keywords.size() < 1) {
            throw new IllegalArgumentException("Please to define at least one keyword for the search");
        }

        List<NodeRef> documents;

        final StringBuilder query = new StringBuilder();

        // search only in the interest group
        query.append(" PATH:\"").append(apiToolBox.getPathFromSpaceRef(parent, true)).append("\" ");
        // search only contents and subtypes of content
        query.append(" AND  TYPE:\"").append(ContentModel.TYPE_CONTENT).append("\" ");
        // search only contents with the Circabc document aspect
        query.append(" AND  ASPECT:\"").append(DocumentModel.ASPECT_CPROPERTIES).append("\" ");
        // search only Circabc document having each keyword in the Keyword property
        for (NodeRef key : keywords) {
            query
                    .append(" AND  +@")
                    .append(QueryParser.escape(DocumentModel.PROP_KEYWORD.toString()))
                    .append(":*")
                    .append(QueryParser.escape(key.toString()))
                    .append("* ");
        }

        // perform the search against the repo
        ResultSet results = null;
        try {
            // Limit search to the first 100 matches
            final SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query.toString());
            sp.addStore(Repository.getStoreRef());

            sp.setLimitBy(LimitBy.UNLIMITED);

            results = searchService.query(sp);
            if (logger.isDebugEnabled()) {
                logger.debug("Search for: " + query);
                logger.debug("Search results returned: " + results.length());
            }

            documents = new ArrayList<>(results.length());

            if (results.length() != 0) {
                NodeRef nodeRef;
                for (final ResultSetRow row : results) {
                    nodeRef = row.getNodeRef();

                    if (nodeService.exists(nodeRef)
                            && permissionService
                            .hasPermission(nodeRef, PermissionService.READ)
                            .equals(AccessStatus.ALLOWED)) {
                        documents.add(nodeRef);
                    }
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Keywords search successfull"
                            + "\n  From: "
                            + parent
                            + "\n  With keywords: "
                            + keywords
                            + "\n  Result: "
                            + documents);

            final long endTime = System.currentTimeMillis();
            logger.debug(
                    "Time to query and build the list of document referenced by the keyword: "
                            + (endTime - startTime)
                            + "ms");
        }

        return documents;
    }

    protected void setKeywordsToNodeImpl(
            final NodeRef document, final ArrayList<NodeRef> newKeywords, final boolean replace) {
        if (document == null) {
            throw new IllegalArgumentException("An noderef group is supplied");
        }

        if (newKeywords == null) {
            throw new IllegalArgumentException("Keywords are supplied");
        }

        if (!nodeService.hasAspect(document, DocumentModel.ASPECT_CPROPERTIES)) {
            throw new IllegalArgumentException(
                    "The "
                            + DocumentModel.ASPECT_CPROPERTIES
                            + " aspect is required to add a keyword to a node");
        }

        Collection<NodeRef> existingKeywords =
                DefaultTypeConverter.INSTANCE.getCollection(
                        NodeRef.class, nodeService.getProperty(document, DocumentModel.PROP_KEYWORD));

        if (existingKeywords == null) {
            existingKeywords = new ArrayList<>();
        }

        if (replace) {
            existingKeywords = newKeywords;
        } else {
            for (final NodeRef kwRef : newKeywords) {
                if (existingKeywords.contains(kwRef)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Keyword " + kwRef + " already setted to the document " + document);
                    }
                } else {
                    existingKeywords.add(kwRef);
                }
            }
        }

        nodeService.setProperty(document, DocumentModel.PROP_KEYWORD, (Serializable) existingKeywords);

        if (logger.isDebugEnabled()) {
            logger.debug("Keywords " + newKeywords + " successfully added to the document " + document);
        }
    }

    protected void setKeywordTranslationsNoderef(final NodeRef keyword, final MLText allValues) {
        if (keyword == null) {
            throw new NullPointerException(
                    "Keyword is a mandatory parameter. Please to create it before with the createKeyword method.");
        }
        if (allValues == null || allValues.size() < 1) {
            throw new IllegalArgumentException("At least one keyword value is required");
        }

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            nodeService.setProperty(keyword, ContentModel.PROP_TITLE, allValues);
            nodeService.setProperty(keyword, PROP_TRANSLATED, Boolean.TRUE);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("The keyword " + keyword + " translations are correctly setted " + allValues);
        }
    }

    protected NodeRef getOrCreateKeywordContainer(final NodeRef ig, final boolean create) {
        if (ig == null) {
            throw new IllegalArgumentException("An interest group must is supplied");
        }

        if (!getNodeService().hasAspect(ig, CircabcModel.ASPECT_IGROOT)) {
            throw new IllegalArgumentException(
                    "Node must have aspect " + CircabcModel.ASPECT_IGROOT + " applied");
        }

        // Now check if a parent mlContainer exists
        NodeRef keywordContainer = null;

        final List<ChildAssociationRef> childAssocRefs =
                nodeService.getChildAssocs(ig, ASSOC_IG_KEYWORDCONTAINER, RegexQNamePattern.MATCH_ALL);

        if (childAssocRefs.size() == 0) {
            if (create) {
                keywordContainer = makeKeywordContainer(ig);
            }
        } else if (childAssocRefs.size() == 1) {
            // Just get it
            final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
            keywordContainer = toKeepAssocRef.getChildRef();

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Keyword container successfully found under the node "
                                + ig
                                + " with the ref "
                                + keywordContainer);
            }

        } else if (childAssocRefs.size() > 1) {
            // This is a problem - destroy all but the first
            if (logger.isWarnEnabled()) {
                logger.warn("Cleaning up multiple keywords containers on the interest group: " + ig);
            }
            final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
            keywordContainer = toKeepAssocRef.getChildRef();
            // Remove all the associations to the container
            boolean first = true;
            for (final ChildAssociationRef assocRef : childAssocRefs) {
                if (first) {
                    first = false;
                    continue;
                }
                nodeService.removeChildAssociation(assocRef);
            }
        }

        // done
        return keywordContainer;
    }

    /**
     * @return Returns a new <b>ci:keywordContainer</b>
     */
    private NodeRef makeKeywordContainer(final NodeRef nodeRef) {
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        nodeRef,
                        ASSOC_IG_KEYWORDCONTAINER,
                        TYPE_KEYWORD_CONTAINER,
                        TYPE_KEYWORD_CONTAINER,
                        new PropertyMap());

        final NodeRef kwContainerNodeRef = assocRef.getChildRef();

        permissionService.setPermission(
                kwContainerNodeRef,
                PermissionService.ALL_AUTHORITIES,
                PermissionService.ALL_PERMISSIONS,
                true);

        permissionService.setPermission(
                kwContainerNodeRef,
                CircabcConstant.GUEST_AUTHORITY,
                PermissionService.ALL_PERMISSIONS,
                true);

        if (logger.isDebugEnabled()) {
            logger.debug("Keyword container successfully created under the node " + nodeRef);
        }

        // Done
        return kwContainerNodeRef;
    }

    public boolean isKeywordNodeMultilingual(final NodeRef keyword) {
        final Boolean isTranslatedObject = (Boolean) nodeService.getProperty(keyword, PROP_TRANSLATED);

        return isTranslatedObject != null && isTranslatedObject;
    }

    protected List<NodeRef> getKeywordsNode(final NodeRef ig) {
        //  Get the container
        final NodeRef container = getOrCreateKeywordContainer(ig, false);

        if (container == null) {
            // not created yet
            return Collections.emptyList();
        }

        // Get all the children
        final List<ChildAssociationRef> keywordsAssoc =
                nodeService.getChildAssocs(container, ASSOC_KEYWORDS, RegexQNamePattern.MATCH_ALL);

        final List<NodeRef> keywords = new ArrayList<>(keywordsAssoc.size());

        NodeRef noderef;

        for (final ChildAssociationRef ref : keywordsAssoc) {
            noderef = ref.getChildRef();

            if (nodeService.getType(noderef).equals(TYPE_KEYWORD)) {
                keywords.add(noderef);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "A non keyword element founds unedr a keyword container: \n"
                                    + "   Interest group :  "
                                    + ig
                                    + "\n"
                                    + "   Container      :  "
                                    + container
                                    + "\n"
                                    + "   type           :  "
                                    + nodeService.getType(noderef)
                                    + "\n"
                                    + "   Node           : "
                                    + noderef);
                }
            }
        }

        // done
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Found keyword: \n"
                            + "   Interest group :  "
                            + ig
                            + "\n"
                            + "   Keyword        : "
                            + keywords);
        }

        return keywords;
    }

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public final void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the namespaceService
     */
    protected final NamespaceService getNamespaceService() {
        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @return the search service
     */
    protected final SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @return the apiToolBox
     */
    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
