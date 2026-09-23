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
package eu.europa.ec.digit.circabc.rest.service.keyword;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.KeywordModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryparser.classic.QueryParserBase;

/**
 * Default implementation of {@link KeywordsService}.
 *
 * <p>Keywords are free-text tags that can be attached to CIRCABC documents. Each Interest Group
 * (IG) owns a single <b>ci:keywordContainer</b> node (created lazily) that holds all of the IG's
 * keyword nodes ({@code ci:keyword}). Individual keywords can be either plain (single language) or
 * multilingual ({@link org.alfresco.service.cmr.repository.MLText}). Documents reference their
 * keywords through the multi-valued {@code DocumentModel#PROP_KEYWORD} property.
 *
 * <p>This service centralises the creation, update, removal, lookup and cross-referencing of
 * keywords, as well as the management of the per-IG keyword container. It relies on the Alfresco
 * {@link NodeService}, {@link PermissionService}, {@link NamespaceService} and {@link SearchService}
 * (Lucene queries) injected via Spring setters, plus the {@link ApiToolBox} helper for path
 * resolution.
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
  private static final QName ASSOC_IG_KEYWORDCONTAINER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "igKwContainer"
  );

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

  /**
   * Helper used to resolve repository paths (e.g. the Lucene {@code PATH:} clause of an IG).
   */
  private ApiToolBox apiToolBox;

  /**
   * Creates a new keyword under the keyword container of the given Interest Group.
   *
   * <p>Depending on {@link Keyword#isKeywordTranslated()} the keyword is stored either as a
   * multilingual {@link org.alfresco.service.cmr.repository.MLText} title or as a plain {@code
   * String} title. The keyword container is created on demand if it does not exist yet.
   *
   * @param igNoderef the Interest Group node that will own the keyword
   * @param keyword the keyword to create; must not be {@code null} and must not already carry an id
   * @return the persisted {@link Keyword} with its newly assigned node id
   * @throws NullPointerException if {@code keyword} is {@code null}
   * @throws IllegalArgumentException if the keyword already has an id or if its value is empty
   */
  public Keyword createKeyword(final NodeRef igNoderef, final Keyword keyword) {
    if (keyword == null) {
      throw new NullPointerException("Keyword is a mandatory parameter");
    }

    if (keyword.getId() != null) {
      throw new IllegalArgumentException(
        "The keyword id can't be setted or use the updateKeyword method"
      );
    }

    final boolean translated = keyword.isKeywordTranslated();
    final Serializable valueObject = translated
      ? keyword.getMLValues()
      : keyword.getValue();

    if (valueObject == null || valueObject.toString().trim().isEmpty()) {
      throw new IllegalArgumentException(
        "Keyword value is a mandatory parameter"
      );
    }

    // Get the container
    final NodeRef container = getOrCreateKeywordContainer(igNoderef, true);

    // Create the keyword
    final ChildAssociationRef assocRef = nodeService.createNode(
      container,
      KeywordModel.ASSOC_KEYWORDS,
      KeywordModel.TYPE_KEYWORD,
      KeywordModel.TYPE_KEYWORD,
      new PropertyMap()
    );

    final NodeRef newKeyword = assocRef.getChildRef();

    // if a locale is specified, set the keyword being a mlText
    if (translated) {
      this.setKeywordTranslationsNoderef(newKeyword, (MLText) valueObject);
    }
    // else, set the keyword normally
    else {
      nodeService.setProperty(newKeyword, ContentModel.PROP_TITLE, valueObject);
      // set the translated property defined by the model
      nodeService.setProperty(
        newKeyword,
        KeywordModel.PROP_TRANSLATED,
        Boolean.FALSE
      );
    }

    //  add the descrition property defined by the titled aspect.
    nodeService.setProperty(newKeyword, ContentModel.PROP_DESCRIPTION, "");

    Keyword createdKeyword;

    if (valueObject instanceof MLText mlText) {
      createdKeyword = new KeywordImpl(newKeyword, mlText);
    } else {
      createdKeyword = new KeywordImpl(newKeyword, (String) valueObject);
    }

    return createdKeyword;
  }

  /**
   * Adds a single keyword to a document without removing the keywords already attached to it.
   *
   * @param document the document node the keyword should be added to
   * @param keyword the keyword to attach
   */
  public void addKeywordToNode(final NodeRef document, final Keyword keyword) {
    final ArrayList<NodeRef> keywords = new ArrayList<>(5);
    if (document != null) {
      keywords.add(keyword.getId());
    }

    setKeywordsToNodeImpl(document, keywords, false);
  }

  /**
   * Returns the keywords currently attached to the given document.
   *
   * @param document the document node to inspect
   * @return the list of {@link Keyword}s referenced by the document, or an empty list if none
   */
  public List<Keyword> getKeywordsForNode(final NodeRef document) {
    final List<NodeRef> keywordsAsNoderef = (List<
      NodeRef
    >) DefaultTypeConverter.INSTANCE.getCollection(
      NodeRef.class,
      nodeService.getProperty(document, DocumentModel.PROP_KEYWORD)
    );

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

    return keywords;
  }

  /**
   * Deletes a keyword from the repository and removes every reference to it.
   *
   * <p>The keyword's owning Interest Group is resolved, all documents in that IG referencing the
   * keyword are updated to drop it, and finally the keyword node itself is deleted.
   *
   * @param keyword the keyword to remove; must reference a valid {@code ci:keyword} node
   * @throws IllegalArgumentException if {@code keyword} is {@code null}, does not point to a valid
   *     keyword node, or if no owning Interest Group can be resolved for it
   */
  public void removeKeyword(final Keyword keyword) {
    final List<NodeRef> keywordAsList = new ArrayList<>(1);

    if (keyword == null) {
      throw new IllegalArgumentException(
        "Only a valid keyword noderef is required here"
      );
    }

    keywordAsList.add(keyword.getId());

    final NodeRef ig = getIgFromKeyword(keyword.getId());

    if (
      !KeywordModel.TYPE_KEYWORD.isMatch(nodeService.getType(keyword.getId()))
    ) {
      throw new IllegalArgumentException(
        "Only a valid keyword noderef is required here"
      );
    }

    if (ig == null) {
      logger.warn(
        "The model seems corrupted, no IG found for the keyword " + keyword
      );

      throw new IllegalArgumentException(
        "Impossible to get the interest group of the keyword " + keyword
      );
    }

    final List<NodeRef> referencedDocument = getNodesForKeywordsNode(
      ig,
      keywordAsList
    );

    List<Keyword> keywords = null;

    for (final NodeRef ref : referencedDocument) {
      keywords = getKeywordsForNode(ref);
      keywords.remove(keyword);
      setKeywordsToNode(ref, keywords);
    }

    nodeService.deleteNode(keyword.getId());
  }

  /**
   * Replaces the full set of keywords attached to a document with the supplied list.
   *
   * @param document the document node whose keywords are being set
   * @param keywords the keywords to associate with the document; must not be {@code null}
   * @throws NullPointerException if {@code keywords} is {@code null}
   */
  public void setKeywordsToNode(
    final NodeRef document,
    final List<Keyword> keywords
  ) {
    if (keywords == null) {
      throw new NullPointerException("The keywords are mandatory parameters");
    }

    final ArrayList<NodeRef> keywordsAsNoderef = new ArrayList<>(
      keywords.size()
    );
    for (final Keyword k : keywords) {
      keywordsAsNoderef.add(k.getId());
    }

    setKeywordsToNodeImpl(document, keywordsAsNoderef, true);
  }

  /**
   * Searches for the documents, within the given parent scope, that are tagged with all of the
   * supplied keywords.
   *
   * @param parent the node defining the search scope (typically an Interest Group)
   * @param keywords the keywords that a matching document must all carry; at least one is required
   * @return the list of matching document nodes the current user is allowed to read
   * @throws NullPointerException if {@code parent} is {@code null}
   * @throws IllegalArgumentException if {@code keywords} is {@code null} or empty
   */
  public List<NodeRef> getNodesForKeywords(
    final NodeRef parent,
    final List<Keyword> keywords
  ) {
    if (parent == null) {
      throw new NullPointerException("The parent node is a madatory parameter");
    }
    if (keywords == null || keywords.isEmpty()) {
      throw new IllegalArgumentException("At least one keyword is required");
    }

    final List<NodeRef> keywordNoderef = new ArrayList<>(keywords.size());
    for (final Keyword key : keywords) {
      keywordNoderef.add(key.getId());
    }

    return getNodesForKeywordsNode(parent, keywordNoderef);
  }

  /**
   * Returns all keywords defined for the given Interest Group, ready for display (both plain and
   * multilingual keywords are resolved with their title).
   *
   * @param ig the Interest Group node whose keywords are requested
   * @return the list of {@link Keyword}s owned by the Interest Group, or an empty list if none
   */
  public List<Keyword> getKeywords(final NodeRef ig) {
    final List<NodeRef> keywordsAsNode = getKeywordsNode(ig);
    final List<Keyword> displayKeywords = new ArrayList<>(
      keywordsAsNode.size()
    );

    for (final NodeRef ref : keywordsAsNode) {
      if (isKeywordNodeMultilingual(ref)) {
        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
          MLPropertyInterceptor.setMLAware(true);
          final MLText title = (MLText) nodeService.getProperty(
            ref,
            ContentModel.PROP_TITLE
          );
          displayKeywords.add(new KeywordImpl(ref, title));
        } finally {
          MLPropertyInterceptor.setMLAware(wasMLAware);
        }
      } else {
        final String title = (String) nodeService.getProperty(
          ref,
          ContentModel.PROP_TITLE
        );
        displayKeywords.add(new KeywordImpl(ref, title));
      }
    }

    return displayKeywords;
  }

  /**
   * Indicates whether the given keyword is multilingual (i.e. holds translated values).
   *
   * @param keyword the keyword to test
   * @return {@code true} if the keyword is multilingual, {@code false} otherwise
   */
  public boolean isKeywordMultilingual(final Keyword keyword) {
    return isKeywordNodeMultilingual(keyword.getId());
  }

  /**
   * Updates the multilingual values of an existing keyword.
   *
   * @param keyword the keyword to update; must be a translated (multilingual) keyword
   * @throws NullPointerException if {@code keyword} is {@code null}
   * @throws IllegalArgumentException if the keyword is not translated
   */
  public void updateKeyword(final Keyword keyword) {
    if (keyword == null) {
      throw new NullPointerException("Keyword is a mandatory parameter");
    }
    if (!keyword.isKeywordTranslated()) {
      throw new IllegalArgumentException(
        "We can't update a keyword if it is not translated"
      );
    }

    setKeywordTranslationsNoderef(keyword.getId(), keyword.getMLValues());
  }

  /**
   * Builds a {@link Keyword} object from a keyword node reference, resolving its plain or
   * multilingual title.
   *
   * @param id the node reference of the keyword; must not be {@code null}
   * @return the corresponding {@link Keyword}, or {@code null} if the node does not exist
   * @throws NullPointerException if {@code id} is {@code null}
   * @throws InvalidNodeRefException if the node reference is invalid
   */
  public Keyword buildKeywordWithId(final NodeRef id)
    throws InvalidNodeRefException {
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
        final MLText translations = (MLText) nodeService.getProperty(
          id,
          ContentModel.PROP_TITLE
        );
        key = new KeywordImpl(id, translations);
      } finally {
        MLPropertyInterceptor.setMLAware(wasMLAware);
      }
    } else {
      key = new KeywordImpl(
        id,
        (String) nodeService.getProperty(id, ContentModel.PROP_TITLE)
      );
    }

    return key;
  }

  /**
   * Builds a {@link Keyword} object from the string form of a keyword node reference.
   *
   * @param id the string representation of the keyword node reference; must not be {@code null}
   * @return the corresponding {@link Keyword}, or {@code null} if the node does not exist
   * @throws NullPointerException if {@code id} is {@code null}
   * @throws InvalidNodeRefException if the string does not represent a valid node reference
   */
  public Keyword buildKeywordWithId(final String id)
    throws InvalidNodeRefException {
    if (id == null) {
      throw new NullPointerException("The node id is a manadtory parameter");
    }

    return buildKeywordWithId(new NodeRef(id));
  }

  /**
   * Tests whether the keyword still exists in the repository.
   *
   * @param keyword the keyword to check
   * @return {@code true} if the keyword and its node reference are non-null and the node exists,
   *     {@code false} otherwise
   */
  public boolean exists(final Keyword keyword) {
    if (keyword == null || keyword.getId() == null) {
      return false;
    } else {
      return getNodeService().exists(keyword.getId());
    }
  }

  // -- Helper methods

  /**
   * Walks up the primary parent chain from a keyword node until it reaches the owning Interest
   * Group (the node carrying the {@code ci:igRoot} aspect).
   *
   * @param keyword the keyword node to start from
   * @return the Interest Group node, or {@code null} if no ancestor carries the IG root aspect
   */
  protected NodeRef getIgFromKeyword(final NodeRef keyword) {
    NodeRef tempRef = keyword;
    while (
      tempRef != null &&
      !nodeService.hasAspect(tempRef, CircabcModel.ASPECT_IGROOT)
    ) {
      tempRef = nodeService.getPrimaryParent(tempRef).getParentRef();
    }
    // At this point tempRef is either null (no IG found) or the IG node itself
    return tempRef;
  }

  /**
   * Runs a Lucene search, scoped to the given parent path, for CIRCABC documents that reference all
   * of the supplied keyword node references, and filters the result to nodes the current user may
   * read.
   *
   * @param parent the node whose repository path defines the search scope
   * @param keywords the keyword node references a document must all carry; at least one is required
   * @return the readable document nodes matching every keyword
   * @throws IllegalArgumentException if {@code keywords} is {@code null} or empty
   */
  protected List<NodeRef> getNodesForKeywordsNode(
    final NodeRef parent,
    final List<NodeRef> keywords
  ) {
    if (keywords == null || keywords.isEmpty()) {
      throw new IllegalArgumentException(
        "Please to define at least one keyword for the search"
      );
    }

    List<NodeRef> documents;

    final StringBuilder query = new StringBuilder();

    // search only in the interest group
    query
      .append(" PATH:\"")
      .append(apiToolBox.getPathFromSpaceRef(parent, true))
      .append("\" ");
    // search only contents and subtypes of content
    query
      .append(" AND  TYPE:\"")
      .append(ContentModel.TYPE_CONTENT)
      .append("\" ");
    // search only contents with the Circabc document aspect
    query
      .append(" AND  ASPECT:\"")
      .append(DocumentModel.ASPECT_CPROPERTIES)
      .append("\" ");
    // search only Circabc document having each keyword in the Keyword property
    for (NodeRef key : keywords) {
      query
        .append(" AND  +@")
        .append(QueryParserBase.escape(DocumentModel.PROP_KEYWORD.toString()))
        .append(":*")
        .append(QueryParserBase.escape(key.toString()))
        .append("* ");
    }

    // perform the search against the repo
    ResultSet results = null;
    try {
      // Limit search to the first 100 matches
      final SearchParameters sp = new SearchParameters();
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery(query.toString());
      sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

      sp.setLimitBy(LimitBy.UNLIMITED);

      results = searchService.query(sp);

      documents = new ArrayList<>(results.length());

      if (results.length() != 0) {
        NodeRef nodeRef;
        for (final ResultSetRow row : results) {
          nodeRef = row.getNodeRef();

          if (
            nodeService.exists(nodeRef) &&
            permissionService
              .hasPermission(nodeRef, PermissionService.READ)
              .equals(AccessStatus.ALLOWED)
          ) {
            documents.add(nodeRef);
          }
        }
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }

    return documents;
  }

  /**
   * Sets or extends the keyword references on a document.
   *
   * <p>The target document must carry the {@code DocumentModel#ASPECT_CPROPERTIES} aspect. When
   * {@code replace} is {@code true} the existing keywords are overwritten; otherwise the new
   * keywords are merged with the existing ones (duplicates are ignored).
   *
   * @param document the document node to update
   * @param newKeywords the keyword node references to set or add
   * @param replace {@code true} to replace all existing keywords, {@code false} to append
   * @throws IllegalArgumentException if {@code document} or {@code newKeywords} is {@code null}, or
   *     if the document lacks the required CIRCABC properties aspect
   */
  protected void setKeywordsToNodeImpl(
    final NodeRef document,
    final ArrayList<NodeRef> newKeywords,
    final boolean replace
  ) {
    if (document == null) {
      throw new IllegalArgumentException("An noderef group is supplied");
    }

    if (newKeywords == null) {
      throw new IllegalArgumentException("Keywords are supplied");
    }

    if (!nodeService.hasAspect(document, DocumentModel.ASPECT_CPROPERTIES)) {
      throw new IllegalArgumentException(
        "The " +
          DocumentModel.ASPECT_CPROPERTIES +
          " aspect is required to add a keyword to a node"
      );
    }

    Collection<NodeRef> existingKeywords =
      DefaultTypeConverter.INSTANCE.getCollection(
        NodeRef.class,
        nodeService.getProperty(document, DocumentModel.PROP_KEYWORD)
      );

    if (existingKeywords == null) {
      existingKeywords = new ArrayList<>();
    }

    if (replace) {
      existingKeywords = newKeywords;
    } else {
      for (final NodeRef kwRef : newKeywords) {
        if (!existingKeywords.contains(kwRef)) {
          existingKeywords.add(kwRef);
        }
      }
    }

    nodeService.setProperty(
      document,
      DocumentModel.PROP_KEYWORD,
      (Serializable) existingKeywords
    );
  }

  /**
   * Stores the multilingual title values on a keyword node and flags it as translated.
   *
   * <p>The property write is performed with the ML property interceptor set to ML-aware so that the
   * full {@link org.alfresco.service.cmr.repository.MLText} is persisted rather than a single
   * locale value.
   *
   * @param keyword the keyword node to update
   * @param allValues the multilingual values to store; at least one entry is required
   * @throws NullPointerException if {@code keyword} is {@code null}
   * @throws IllegalArgumentException if {@code allValues} is {@code null} or empty
   */
  protected void setKeywordTranslationsNoderef(
    final NodeRef keyword,
    final MLText allValues
  ) {
    if (keyword == null) {
      throw new NullPointerException(
        "Keyword is a mandatory parameter. Please to create it before with the createKeyword method."
      );
    }
    if (allValues == null || allValues.size() < 1) {
      throw new IllegalArgumentException(
        "At least one keyword value is required"
      );
    }

    final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

    try {
      MLPropertyInterceptor.setMLAware(true);
      nodeService.setProperty(keyword, ContentModel.PROP_TITLE, allValues);
      nodeService.setProperty(
        keyword,
        KeywordModel.PROP_TRANSLATED,
        Boolean.TRUE
      );
    } finally {
      MLPropertyInterceptor.setMLAware(wasMLAware);
    }
  }

  /**
   * Returns the keyword container node for an Interest Group, optionally creating it.
   *
   * <p>If several containers are found (a corrupted state), all but the first are detached and a
   * warning is logged.
   *
   * @param ig the Interest Group node; must carry the {@code ci:igRoot} aspect
   * @param create {@code true} to create the container when it does not exist yet
   * @return the keyword container node, or {@code null} if none exists and {@code create} is {@code
   *     false}
   * @throws IllegalArgumentException if {@code ig} is {@code null} or does not carry the IG root
   *     aspect
   */
  protected NodeRef getOrCreateKeywordContainer(
    final NodeRef ig,
    final boolean create
  ) {
    if (ig == null) {
      throw new IllegalArgumentException("An interest group must is supplied");
    }

    if (!getNodeService().hasAspect(ig, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException(
        "Node must have aspect " + CircabcModel.ASPECT_IGROOT + " applied"
      );
    }

    // Now check if a parent mlContainer exists
    NodeRef keywordContainer = null;

    final List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
      ig,
      ASSOC_IG_KEYWORDCONTAINER,
      RegexQNamePattern.MATCH_ALL
    );

    if (childAssocRefs.isEmpty()) {
      if (create) {
        keywordContainer = makeKeywordContainer(ig);
      }
    } else if (childAssocRefs.size() == 1) {
      // Just get it
      final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
      keywordContainer = toKeepAssocRef.getChildRef();
    } else if (childAssocRefs.size() > 1) {
      // This is a problem - destroy all but the first
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Cleaning up multiple keywords containers on the interest group: " +
            ig
        );
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
   * Creates a new <b>ci:keywordContainer</b> child of the given node and grants full permissions to
   * all authorities and to the guest user.
   *
   * @param nodeRef the Interest Group node the container is attached to
   * @return the newly created <b>ci:keywordContainer</b> node
   */
  private NodeRef makeKeywordContainer(final NodeRef nodeRef) {
    final ChildAssociationRef assocRef = nodeService.createNode(
      nodeRef,
      ASSOC_IG_KEYWORDCONTAINER,
      KeywordModel.TYPE_KEYWORD_CONTAINER,
      KeywordModel.TYPE_KEYWORD_CONTAINER,
      new PropertyMap()
    );

    final NodeRef kwContainerNodeRef = assocRef.getChildRef();

    permissionService.setPermission(
      kwContainerNodeRef,
      PermissionService.ALL_AUTHORITIES,
      PermissionService.ALL_PERMISSIONS,
      true
    );

    permissionService.setPermission(
      kwContainerNodeRef,
      "guest",
      PermissionService.ALL_PERMISSIONS,
      true
    );

    return kwContainerNodeRef;
  }

  /**
   * Indicates whether a keyword node is multilingual, based on its {@code ci:translated} property.
   *
   * @param keyword the keyword node to test
   * @return {@code true} if the node is flagged as translated, {@code false} otherwise
   */
  public boolean isKeywordNodeMultilingual(final NodeRef keyword) {
    final Boolean isTranslatedObject = (Boolean) nodeService.getProperty(
      keyword,
      KeywordModel.PROP_TRANSLATED
    );

    return isTranslatedObject != null && isTranslatedObject;
  }

  /**
   * Returns the raw keyword node references contained in the Interest Group's keyword container.
   *
   * <p>The container is looked up without being created; non-keyword children found inside it are
   * skipped and logged as a warning.
   *
   * @param ig the Interest Group node whose keyword nodes are requested
   * @return the list of {@code ci:keyword} node references, or an empty list if the container does
   *     not exist yet
   */
  protected List<NodeRef> getKeywordsNode(final NodeRef ig) {
    //  Get the container
    final NodeRef container = getOrCreateKeywordContainer(ig, false);

    if (container == null) {
      // not created yet
      return Collections.emptyList();
    }

    // Get all the children
    final List<ChildAssociationRef> keywordsAssoc = nodeService.getChildAssocs(
      container,
      KeywordModel.ASSOC_KEYWORDS,
      RegexQNamePattern.MATCH_ALL
    );

    final List<NodeRef> keywords = new ArrayList<>(keywordsAssoc.size());

    NodeRef noderef;

    for (final ChildAssociationRef ref : keywordsAssoc) {
      noderef = ref.getChildRef();

      if (nodeService.getType(noderef).equals(KeywordModel.TYPE_KEYWORD)) {
        keywords.add(noderef);
      } else {
        if (logger.isWarnEnabled()) {
          logger.warn(
            "A non keyword element founds unedr a keyword container: \n" +
              "   Interest group :  " +
              ig +
              "\n" +
              "   Container      :  " +
              container +
              "\n" +
              "   type           :  " +
              nodeService.getType(noderef) +
              "\n" +
              "   Node           : " +
              noderef
          );
        }
      }
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
  public final void setPermissionService(
    final PermissionService permissionService
  ) {
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
  public final void setNamespaceService(
    final NamespaceService namespaceService
  ) {
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
