package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyType;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.Node;
import io.swagger.model.PagedSearchNodes;
import io.swagger.model.SearchNode;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.EventModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.DateUtil;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the {@link SearchApi} contract that performs full-text and
 * metadata searches over the CIRCABC content repository.
 *
 * <p>This class translates the high-level search parameters exposed by the REST
 * layer into an Alfresco Lucene query, executes it through the
 * {@link SearchService}, and maps the matching nodes into {@link SearchNode}
 * result objects wrapped in a {@link PagedSearchNodes} response.</p>
 *
 * <p>The generated query supports scoping the search to a specific area
 * (Library, Newsgroup, Information, Agenda/Events, or all of them via CIRCABC
 * aspects), restricting the searched fields (name, title, content, or all),
 * and applying additional filters such as language, creator, creation and
 * modification date ranges, keywords, status, security ranking, version and
 * interest-group dynamic properties. Results can optionally be sorted and
 * paginated.</p>
 *
 * @author beaurpi
 */
public class SearchApiImpl implements SearchApi {

  /** Lucene field prefix used to match the {@code title} property. */
  private static final String AMPERSAND_TITLE = "@title:";

  /** Lucene field prefix used to match the {@code name} property. */
  private static final String NAME = "@name:";

  /** Lucene field prefix used to match the indexed full-text content. */
  private static final String TEXT = "TEXT:";

  /** Token identifying a title-based search / sort selection. */
  private static final String TITLE = "TITLE";

  /** Lucene fragment opening an aspect filter clause. */
  private static final String OPEN_PARANTHESIS_ASPECT = "(ASPECT:\"";

  /** Lucene keyword used to filter results by aspect. */
  private static final String ASPECT = "ASPECT:\"";

  @Autowired
  private SearchService searchService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private DynamicPropertyService dynamicPropertyService;

  /** Lucene logical AND operator (surrounded by spaces) used to join clauses. */
  private static final String AND = " AND ";

  /** Lucene logical OR operator (surrounded by spaces) used to join clauses. */
  private static final String OR = " OR ";

  /**
   * Immutable holder grouping together all the criteria that influence the
   * construction of the Lucene search query, used to keep
   * {@link #buildSearchQuery(SearchQueryParams)} readable and to avoid long
   * parameter lists.
   *
   * @param q the free-text query string
   * @param nodeId identifier of the node scoping the search (used to resolve the search path and dynamic properties)
   * @param language ISO language code used to restrict results by locale
   * @param searchFor field selector (NAME, TITLE, CONTENT/TEXT or ALL)
   * @param searchIn area selector (INFORMATION, LIBRARY, AGENDA, NEWSGROUP or ALL)
   * @param creator user name of the content creator to filter by
   * @param creationDateFrom lower bound of the creation date range
   * @param creationDateTo upper bound of the creation date range
   * @param modifiedDateFrom lower bound of the modification date range
   * @param modifiedDateTo upper bound of the modification date range
   * @param keywords comma-separated list of keyword node identifiers to filter by
   * @param status document status filter
   * @param securityRanking document security ranking filter
   * @param version version label filter
   * @param dynamicProperties interest-group specific dynamic property values
   */
  private record SearchQueryParams(
    String q,
    String nodeId,
    String language,
    String searchFor,
    String searchIn,
    String creator,
    Date creationDateFrom,
    Date creationDateTo,
    Date modifiedDateFrom,
    Date modifiedDateTo,
    String keywords,
    String status,
    String securityRanking,
    String version,
    String[] dynamicProperties
  ) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (
        !(o instanceof
            SearchQueryParams(
              String oQ,
              String oNodeId,
              String oLanguage,
              String oSearchFor,
              String oSearchIn,
              String oCreator,
              Date oCreationDateFrom,
              Date oCreationDateTo,
              Date oModifiedDateFrom,
              Date oModifiedDateTo,
              String oKeywords,
              String oStatus,
              String oSecurityRanking,
              String oVersion,
              String[] oDynamicProperties
            ))
      ) return false;
      return (
        java.util.Objects.equals(q, oQ) &&
        java.util.Objects.equals(nodeId, oNodeId) &&
        java.util.Objects.equals(language, oLanguage) &&
        java.util.Objects.equals(searchFor, oSearchFor) &&
        java.util.Objects.equals(searchIn, oSearchIn) &&
        java.util.Objects.equals(creator, oCreator) &&
        java.util.Objects.equals(creationDateFrom, oCreationDateFrom) &&
        java.util.Objects.equals(creationDateTo, oCreationDateTo) &&
        java.util.Objects.equals(modifiedDateFrom, oModifiedDateFrom) &&
        java.util.Objects.equals(modifiedDateTo, oModifiedDateTo) &&
        java.util.Objects.equals(keywords, oKeywords) &&
        java.util.Objects.equals(status, oStatus) &&
        java.util.Objects.equals(securityRanking, oSecurityRanking) &&
        java.util.Objects.equals(version, oVersion) &&
        java.util.Arrays.equals(dynamicProperties, oDynamicProperties)
      );
    }

    @Override
    public int hashCode() {
      int result = java.util.Objects.hash(
        q,
        nodeId,
        language,
        searchFor,
        searchIn,
        creator,
        creationDateFrom,
        creationDateTo,
        modifiedDateFrom,
        modifiedDateTo,
        keywords,
        status,
        securityRanking,
        version
      );
      result = 31 * result + java.util.Arrays.hashCode(dynamicProperties);
      return result;
    }

    @Override
    public String toString() {
      return (
        "SearchQueryParams[q=" +
        q +
        ", nodeId=" +
        nodeId +
        ", dynamicProperties=" +
        java.util.Arrays.toString(dynamicProperties) +
        "]"
      );
    }
  }

  /**
   * Executes a paginated search over the repository based on the supplied
   * criteria and returns the matching nodes.
   *
   * <p>The method validates the query string, builds a Lucene query from the
   * provided filters, runs it through the {@link SearchService} and converts
   * the hits into a {@link PagedSearchNodes} response.</p>
   *
   * @param q the free-text query string; must not be {@code null} or empty
   * @param nodeId identifier of the node scoping the search (path and dynamic properties)
   * @param language ISO language code used to restrict results by locale
   * @param page the 1-based page index to return; defaults to {@code 1} when {@code null}
   * @param limit the maximum number of items per page; defaults to {@code -1} (unbounded) when {@code null}
   * @param searchFor field selector (NAME, TITLE, CONTENT/TEXT or ALL)
   * @param searchIn area selector (INFORMATION, LIBRARY, AGENDA, NEWSGROUP or ALL)
   * @param creator user name of the content creator to filter by
   * @param creationDateFrom lower bound of the creation date range
   * @param creationDateTo upper bound of the creation date range
   * @param modifiedDateFrom lower bound of the modification date range
   * @param modifiedDateTo upper bound of the modification date range
   * @param keywords comma-separated list of keyword node identifiers to filter by
   * @param status document status filter
   * @param securityRanking document security ranking filter
   * @param version version label filter
   * @param dynamicProperties interest-group specific dynamic property values
   * @param sort the field to sort by (NAME, TITLE, CREATION_DATE, MODIFICATION_DATE or ISSUE_DATE)
   * @param order the sort direction; {@code true} for ascending, {@code false} for descending
   * @return a {@link PagedSearchNodes} holding the matching nodes and their total count
   * @throws EmptyQueryStringException if {@code q} is {@code null} or empty
   */
  public PagedSearchNodes searchGet(
    String q,
    String nodeId,
    String language,
    Integer page,
    Integer limit,
    String searchFor,
    String searchIn,
    String creator,
    Date creationDateFrom,
    Date creationDateTo,
    Date modifiedDateFrom,
    Date modifiedDateTo,
    String keywords,
    String status,
    String securityRanking,
    String version,
    String[] dynamicProperties,
    String sort,
    boolean order
  ) throws EmptyQueryStringException {
    if ("".equals(q) || q == null) {
      throw new EmptyQueryStringException("The query text cannot be empty");
    }

    if (limit == null) {
      limit = -1;
    }
    if (page == null) {
      page = 1;
    }

    String query = buildSearchQuery(
      new SearchQueryParams(
        q,
        nodeId,
        language,
        searchFor,
        searchIn,
        creator,
        creationDateFrom,
        creationDateTo,
        modifiedDateFrom,
        modifiedDateTo,
        keywords,
        status,
        securityRanking,
        version,
        dynamicProperties
      )
    );

    SearchParameters searchParameters = buildSearchParameters(
      query,
      limit,
      page,
      sort,
      order
    );
    ResultSet rs = searchService.query(searchParameters);

    return buildSearchResults(rs);
  }

  private String buildSearchQuery(SearchQueryParams p) {
    StringBuilder qb = new StringBuilder();

    if (p.nodeId() != null) {
      NodeRef targetRef = Converter.createNodeRefFromId(p.nodeId());
      qb
        .append("(PATH:\"")
        .append(apiToolBox.getPathFromSpaceRef(targetRef, true))
        .append("\")");
    }

    boolean agenda = false;
    boolean all =
      p.searchIn() == null ||
      "ALL".equalsIgnoreCase(p.searchIn()) ||
      "".equals(p.searchIn());

    if (all) {
      appendAllAspectsFilter(qb);
    } else {
      agenda = appendSingleAspectFilter(qb, p.searchIn());
    }

    qb.append(AND);
    String search = resolveSearchFor(p.searchFor());
    appendTextQuery(qb, p.q(), search, all, agenda);

    appendLanguageFilter(qb, p.language());
    appendCreatorFilter(qb, p.creator());
    appendDateRangeFilter(
      qb,
      "@created",
      p.creationDateFrom(),
      p.creationDateTo()
    );
    appendDateRangeFilter(
      qb,
      "@modified",
      p.modifiedDateFrom(),
      p.modifiedDateTo()
    );
    appendKeywordsFilter(qb, p.keywords());
    appendFieldFilter(qb, p.status(), "status");
    appendFieldFilter(qb, p.securityRanking(), "security_ranking");
    if (p.version() != null && !p.version().isEmpty()) {
      qb.append(AND).append("(@versionLabel:").append(p.version()).append(")");
    }
    appendDynamicPropertiesFilter(qb, p.dynamicProperties(), p.nodeId());

    return qb.toString();
  }

  private void appendAllAspectsFilter(StringBuilder qb) {
    qb
      .append(AND)
      .append("(")
      .append(ASPECT)
      .append(CircabcModel.CIRCABC_MODEL_PREFIX)
      .append(":")
      .append(CircabcModel.ASPECT_INFORMATION_NEWS.getLocalName())
      .append("\"")
      .append(OR)
      .append(ASPECT)
      .append(CircabcModel.CIRCABC_MODEL_PREFIX)
      .append(":circaLibrary\"")
      .append(OR)
      .append(ASPECT)
      .append(CircabcModel.CIRCABC_MODEL_PREFIX)
      .append(":circabcEvent\"")
      .append(OR)
      .append(ASPECT)
      .append(CircabcModel.CIRCABC_MODEL_PREFIX)
      .append(":circaNewsGroup\"")
      .append(")");
  }

  private boolean appendSingleAspectFilter(StringBuilder qb, String searchIn) {
    String aspectSuffix;
    boolean agenda = false;
    if (searchIn.equalsIgnoreCase("INFORMATION")) {
      aspectSuffix =
        CircabcModel.CIRCABC_MODEL_PREFIX +
        ":" +
        CircabcModel.ASPECT_INFORMATION_NEWS.getLocalName();
    } else if (searchIn.equalsIgnoreCase("LIBRARY")) {
      aspectSuffix = CircabcModel.CIRCABC_MODEL_PREFIX + ":circaLibrary";
    } else if (searchIn.equalsIgnoreCase("AGENDA")) {
      aspectSuffix = CircabcModel.CIRCABC_MODEL_PREFIX + ":circabcEvent";
      agenda = true;
    } else {
      aspectSuffix = CircabcModel.CIRCABC_MODEL_PREFIX + ":circaNewsGroup";
    }
    qb
      .append(AND)
      .append(OPEN_PARANTHESIS_ASPECT)
      .append(aspectSuffix)
      .append("\")");
    return agenda;
  }

  private String resolveSearchFor(String searchFor) {
    if (searchFor == null || searchFor.isEmpty()) {
      return "ALL";
    }
    if (searchFor.equalsIgnoreCase("NAME")) {
      return "NAME";
    }
    if (searchFor.equalsIgnoreCase(TITLE)) {
      return TITLE;
    }
    if (
      searchFor.equalsIgnoreCase("CONTENT") ||
      searchFor.equalsIgnoreCase("TEXT")
    ) {
      return "TEXT";
    }
    return "ALL";
  }

  private void appendTextQuery(
    StringBuilder qb,
    String q,
    String search,
    boolean all,
    boolean agenda
  ) {
    if (all) {
      qb
        .append("(")
        .append(buildQueryParameter(q, search))
        .append(OR)
        .append(buildQueryParameterforAgenda(q))
        .append(")");
    } else if (agenda) {
      qb.append(buildQueryParameterforAgenda(q));
    } else {
      qb.append(buildQueryParameter(q, search));
    }
  }

  private void appendLanguageFilter(StringBuilder qb, String language) {
    if (language != null && !"".equals(language)) {
      Locale locale = Locale.of(language);
      qb
        .append(AND)
        .append("(@sys\\:locale:")
        .append(locale.getLanguage())
        .append("_*)");
    }
  }

  private void appendCreatorFilter(StringBuilder qb, String creator) {
    if (creator != null && !creator.isEmpty()) {
      qb.append(AND).append("(@creator:").append(creator).append(")");
    }
  }

  private void appendDateRangeFilter(
    StringBuilder qb,
    String field,
    Date from,
    Date to
  ) {
    if (from == null && to == null) {
      return;
    }
    qb.append(AND).append("(").append(field).append(":[");
    if (from != null) {
      qb.append(
        Converter.convertDateToUTCString(DateUtil.setTimeAtMidnight(from))
      );
    } else {
      qb.append("MIN");
    }
    qb.append(" TO ");
    if (to != null) {
      qb.append(
        Converter.convertDateToUTCString(DateUtil.setTimeAt23H59M59S(to))
      );
    } else {
      qb.append("MAX");
    }
    qb.append("])");
  }

  private void appendKeywordsFilter(StringBuilder qb, String keywords) {
    if (keywords == null || keywords.isEmpty()) {
      return;
    }
    keywords = keywords
      .replace("\\[", "")
      .replace("\\]", "")
      .replaceAll("\\s", "");
    String prefix = "workspace:\\/\\/SpacesStore\\/";
    for (String kw : keywords.split(",")) {
      qb
        .append(AND)
        .append("(+@")
        .append(
          QueryParserBase.escape(DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX)
        )
        .append("\\:keyword")
        .append(":\"")
        .append(prefix)
        .append(QueryParserBase.escape(kw))
        .append("\\*\"")
        .append(")");
    }
  }

  private void appendFieldFilter(
    StringBuilder qb,
    String value,
    String fieldName
  ) {
    if (value == null || value.isEmpty() || value.equalsIgnoreCase("ALL")) {
      return;
    }
    qb
      .append(AND)
      .append("(@")
      .append(DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX)
      .append("\\:")
      .append(fieldName)
      .append(":")
      .append(value.toUpperCase())
      .append(")");
  }

  private void appendDynamicPropertiesFilter(
    StringBuilder qb,
    String[] dynamicProperties,
    String nodeId
  ) {
    boolean found = false;
    for (int i = 0; i < 20 && !found; i++) {
      found = dynamicProperties[i] != null && !"".equals(dynamicProperties[i]);
    }
    if (found && nodeId != null) {
      qb.append(
        buildDynamicPropertiesParameter(
          Converter.createNodeRefFromId(nodeId),
          dynamicProperties
        )
      );
    }
  }

  private SearchParameters buildSearchParameters(
    String query,
    int limit,
    int page,
    String sort,
    boolean order
  ) {
    SearchParameters sp = new SearchParameters();
    sp.setLimit(limit);
    sp.setSkipCount(limit * page);
    sp.setMaxItems(limit);
    sp.setLimitBy(LimitBy.FINAL_SIZE);
    sp.setQuery(query);
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    addSortParameter(sp, sort, order);
    return sp;
  }

  private void addSortParameter(
    SearchParameters sp,
    String sort,
    boolean order
  ) {
    if (sort == null) {
      return;
    }
    Map<String, String> sortMap = Map.of(
      "NAME",
      "cm:name",
      TITLE,
      "cm:title",
      "CREATION_DATE",
      "cm:created",
      "MODIFICATION_DATE",
      "cm:modified",
      "ISSUE_DATE",
      "cm:issue_date"
    );
    String field = sortMap.get(sort.toUpperCase());
    if (field != null) {
      sp.addSort(field, order);
    }
  }

  private PagedSearchNodes buildSearchResults(ResultSet rs) {
    PagedSearchNodes result = new PagedSearchNodes();
    long numberOfResults = 0;
    for (NodeRef ref : rs.getNodeRefs()) {
      SearchNode psn = buildSearchNode(ref);
      result.getData().add(psn);
      ++numberOfResults;
    }
    result.setTotal(numberOfResults);
    return result;
  }

  private SearchNode buildSearchNode(NodeRef ref) {
    SearchNode psn = new SearchNode();
    Node n = nodesApi.getNode(ref);
    psn.setId(n.getId());
    psn.setDescription(n.getDescription());
    psn.setName(n.getName());
    psn.setParentId(n.getParentId());
    psn.setType(n.getType());
    psn.setPermissions(n.getPermissions());
    psn.setProperties(n.getProperties());
    psn.setTitle(n.getTitle());
    psn.setDescription(n.getDescription());
    psn.setService(n.getService());

    setResultTypeAndTarget(psn, ref);
    return psn;
  }

  private void setResultTypeAndTarget(SearchNode psn, NodeRef ref) {
    QName type = nodeService.getType(ref);

    if (nodeService.hasAspect(ref, CircabcModel.ASPECT_LIBRARY)) {
      setLibraryResultType(psn, ref, type);
    } else if (nodeService.hasAspect(ref, CircabcModel.ASPECT_NEWSGROUP)) {
      setNewsgroupResultType(psn, ref, type);
    } else if (
      nodeService.hasAspect(ref, CircabcModel.ASPECT_INFORMATION) &&
      type.equals(CircabcModel.TYPE_INFORMATION_NEWS)
    ) {
      psn.setResultType("information");
      psn.setTargetNode(getTopic(ref));
    } else if (nodeService.hasAspect(ref, CircabcModel.ASPECT_EVENT)) {
      setEventResultType(psn, ref, type);
    }

    if (psn.getResultType() == null || "".equals(psn.getResultType())) {
      psn.setResultType(type.getLocalName());
      psn.setTargetNode(getTopic(ref));
    }
  }

  private void setLibraryResultType(SearchNode psn, NodeRef ref, QName type) {
    if (type.equals(ForumModel.TYPE_TOPIC)) {
      psn.setResultType("topic");
      psn.setTargetNode(getLibraryParentOfTopic(ref));
    } else if (type.equals(ForumModel.TYPE_POST)) {
      psn.setResultType("post");
      psn.setTargetNode(getLibraryParentOfPost(ref));
    } else if (type.equals(ContentModel.TYPE_FOLDER)) {
      psn.setResultType("folder");
    } else if (type.equals(ContentModel.TYPE_CONTENT)) {
      psn.setResultType("file");
    }
  }

  private void setNewsgroupResultType(SearchNode psn, NodeRef ref, QName type) {
    if (type.equals(ForumModel.TYPE_TOPIC)) {
      psn.setResultType("topic");
      psn.setTargetNode(getTopic(ref));
    } else if (type.equals(ForumModel.TYPE_POST)) {
      psn.setResultType("post");
      psn.setTargetNode(getNewsgroupParentOfPost(ref));
    } else if (type.equals(ForumModel.TYPE_FORUM)) {
      psn.setResultType("forum");
    }
  }

  private void setEventResultType(SearchNode psn, NodeRef ref, QName type) {
    if (type.equals(EventModel.TYPE_EVENT)) {
      psn.setResultType("event");
      psn.setTargetNode(getTopic(ref));
    } else if (type.equals(EventModel.PROP_MEETING_TYPE)) {
      psn.setResultType("meeting");
      psn.setTargetNode(getTopic(ref));
    }
  }

  private String buildQueryParameter(String q, String searchFor) {
    StringBuilder queryBuilder = new StringBuilder();
    q = Converter.replaceEnclosingSingleQuotes(q.trim());

    queryBuilder.append("(");
    if (q.startsWith("\"") && q.endsWith("\"")) {
      appendSearchFields(queryBuilder, searchFor, q);
    } else {
      String[] queryItems = q.split("\\s+");
      for (int i = 0; i < queryItems.length; i++) {
        appendSearchFields(
          queryBuilder,
          searchFor,
          QueryParserBase.escape(queryItems[i])
        );
        if (i + 1 < queryItems.length) {
          queryBuilder.append(OR);
        }
      }
    }
    queryBuilder.append(")");
    return queryBuilder.toString();
  }

  private void appendSearchFields(
    StringBuilder queryBuilder,
    String searchFor,
    String value
  ) {
    if (searchFor.equalsIgnoreCase("ALL")) {
      queryBuilder.append(TEXT).append(value);
      queryBuilder.append(OR);
      queryBuilder.append(NAME).append(value);
      queryBuilder.append(OR);
      queryBuilder.append(AMPERSAND_TITLE).append(value);
    } else if (searchFor.equalsIgnoreCase("TEXT")) {
      queryBuilder.append(TEXT).append(value);
    } else if (searchFor.equalsIgnoreCase("NAME")) {
      queryBuilder.append(NAME).append(value);
    } else if (searchFor.equalsIgnoreCase(TITLE)) {
      queryBuilder.append(AMPERSAND_TITLE).append(value);
    }
  }

  private String buildQueryParameterforAgenda(String q) {
    // For the special case of agenda, we only have the field title from the
    // EventModel {http://www.cc.cec/circabc/model/events/1.0}title with prefix ce

    StringBuilder queryBuilder = new StringBuilder();
    q = Converter.replaceEnclosingSingleQuotes(q.trim());

    if (q.startsWith("\"") && q.endsWith("\"")) {
      queryBuilder
        .append("(@")
        .append(EventModel.CIRCABC_EVENT_MODEL_PREFIX)
        .append("\\:title:")
        .append(q)
        .append(AND)
        .append("TYPE:")
        .append(EventModel.CIRCABC_EVENT_MODEL_PREFIX)
        .append("\\:event")
        .append(")");
    } else {
      queryBuilder.append("((");
      String[] queryItems = q.split("\\s+");

      for (int i = 0; i < queryItems.length; i++) {
        queryBuilder
          .append("@")
          .append(EventModel.CIRCABC_EVENT_MODEL_PREFIX)
          .append("\\:title:")
          .append(QueryParserBase.escape(queryItems[i]));
        if (i + 1 < queryItems.length) {
          queryBuilder.append(OR);
        }
      }
      queryBuilder.append(")");
      queryBuilder
        .append(AND)
        .append("TYPE:")
        .append(EventModel.CIRCABC_EVENT_MODEL_PREFIX)
        .append("\\:event")
        .append(")");
    }
    return queryBuilder.toString();
  }

  private String buildDynamicPropertiesParameter(
    NodeRef node,
    String[] dynamicProperties
  ) {
    StringBuilder queryBuilder = new StringBuilder();
    List<DynamicProperty> interestGroupDynamicProperties;
    try {
      interestGroupDynamicProperties =
        dynamicPropertyService.getDynamicProperties(node);
    } catch (IllegalArgumentException e) {
      return "";
    }

    final Map<QName, DynamicProperty> map = HashMap.newHashMap(
      interestGroupDynamicProperties.size()
    );
    for (DynamicProperty dp : interestGroupDynamicProperties) {
      map.put(dynamicPropertyService.getPropertyQname(dp), dp);
    }

    int i = 1;
    for (QName qName : DocumentModel.ALL_DYN_PROPS) {
      String dynamicProperty = dynamicProperties[i - 1];
      if (dynamicProperty != null && !dynamicProperty.isEmpty()) {
        appendDynamicPropertyQuery(
          queryBuilder,
          qName,
          map.get(qName),
          dynamicProperty.trim()
        );
      }
      i++;
    }
    return queryBuilder.toString();
  }

  private void appendDynamicPropertyQuery(
    StringBuilder queryBuilder,
    QName qName,
    DynamicProperty property,
    String value
  ) {
    if (property.getType().equals(DynamicPropertyType.MULTI_SELECTION)) {
      if (property.getValidValues().trim().contains(value)) {
        queryBuilder
          .append(AND)
          .append(buildDynamicPropertyPrefix(qName))
          .append("*" + value + "*)");
      }
    } else if (property.getType().equals(DynamicPropertyType.DATE_FIELD)) {
      try {
        Date dateValue = Converter.convertStringToSimpleDate(value);
        Date dateFrom = DateUtil.setTimeAtMidnight(dateValue);
        queryBuilder
          .append(AND)
          .append(buildDynamicPropertyPrefix(qName))
          .append("\"" + Converter.convertDateToUTCString(dateFrom) + "\")");
      } catch (ParseException e) {
        // If there is an exception, just discard the parameter
      }
    } else {
      queryBuilder
        .append(AND)
        .append(buildDynamicPropertyPrefix(qName))
        .append(value)
        .append(")");
    }
  }

  private String buildDynamicPropertyPrefix(QName qName) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
      .append("(+@")
      .append(
        QueryParserBase.escape(DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX)
      )
      .append("\\:")
      .append(qName.getLocalName())
      .append(":");
    return queryBuilder.toString();
  }

  /**
   * return the topic ref in case the node is in the newsgroups
   */
  private String getNewsgroupParentOfPost(NodeRef ref) {
    return nodeService.getPrimaryParent(ref).getParentRef().getId();
  }

  /**
   * return the forum ref in case the node is in the newsgroups
   */
  private String getTopic(NodeRef ref) {
    return ref.getId();
  }

  /**
   * return the document ref in case the node is a thread of comment
   */
  private String getLibraryParentOfTopic(NodeRef ref) {
    NodeRef discussionRef = nodeService.getPrimaryParent(ref).getParentRef();
    return nodeService.getPrimaryParent(discussionRef).getParentRef().getId();
  }

  /**
   * return the document ref in case the node is a post in a document
   */
  private String getLibraryParentOfPost(NodeRef ref) {
    NodeRef discussionRef = nodeService.getPrimaryParent(ref).getParentRef();
    NodeRef topicRef = nodeService
      .getPrimaryParent(discussionRef)
      .getParentRef();
    return nodeService.getPrimaryParent(topicRef).getParentRef().getId();
  }
}
