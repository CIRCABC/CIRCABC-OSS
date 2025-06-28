package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import eu.cec.digit.circabc.util.DateUtils;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.Node;
import io.swagger.model.PagedSearchNodes;
import io.swagger.model.SearchNode;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
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
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.apache.lucene.queryParser.QueryParser;

/**
 * @author beaurpi
 */
public class SearchApiImpl implements SearchApi {

  private SearchService searchService;
  private NodeService nodeService;
  private DynamicPropertyService dynamicPropertyService;
  private NodesApi nodesApi;
  private ApiToolBox apiToolBox;

  private static final String AND = " AND ";
  private static final String OR = " OR ";

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

    PagedSearchNodes result = new PagedSearchNodes();

    if (limit == null) {
      limit = -1;
    }

    if (page == null) {
      page = 1;
    }

    StringBuilder queryBuilder = new StringBuilder();

    if (nodeId != null) {
      NodeRef targetRef = Converter.createNodeRefFromId(nodeId);
      String path =
        "(PATH:\"" + apiToolBox.getPathFromSpaceRef(targetRef, true) + "\")";
      queryBuilder.append(path);
    }

    // If we need to search in the agenda (SearchIn is set to ALL or Agenda), we
    // need to take into account
    // that the search criteria for an agenda is different: we do not have a name, a
    // content but only a field title
    // in a different namespace EventModel.CIRCABC_EVENT_MODEL_PREFIX!
    // To manage this particular case we will use 2 booleans
    boolean agenda = false;
    boolean all = true;

    // Search in
    // if ALL
    if (
      searchIn == null ||
      "ALL".equalsIgnoreCase(searchIn) ||
      "".equals(searchIn)
    ) {
      queryBuilder
        .append(AND)
        .append("(")
        .append(
          "ASPECT:\"" +
          CircabcModel.CIRCABC_MODEL_PREFIX +
          ":" +
          CircabcModel.ASPECT_INFORMATION_NEWS.getLocalName() +
          "\""
        )
        .append(OR)
        .append(
          "ASPECT:\"" + CircabcModel.CIRCABC_MODEL_PREFIX + ":circaLibrary\""
        )
        .append(OR)
        .append(
          "ASPECT:\"" + CircabcModel.CIRCABC_MODEL_PREFIX + ":circabcEvent\""
        )
        .append(OR)
        .append(
          "ASPECT:\"" + CircabcModel.CIRCABC_MODEL_PREFIX + ":circaNewsGroup\""
        )
        .append(")");
    } else {
      // Not ALL
      all = false;
      if (searchIn.equalsIgnoreCase("INFORMATION")) {
        queryBuilder
          .append(AND)
          .append(
            "(ASPECT:\"" +
            CircabcModel.CIRCABC_MODEL_PREFIX +
            ":" +
            CircabcModel.ASPECT_INFORMATION_NEWS.getLocalName() +
            "\")"
          );
      } else if (searchIn.equalsIgnoreCase("LIBRARY")) {
        queryBuilder
          .append(AND)
          .append(
            "(ASPECT:\"" +
            CircabcModel.CIRCABC_MODEL_PREFIX +
            ":circaLibrary\")"
          );
      } else if (searchIn.equalsIgnoreCase("AGENDA")) {
        queryBuilder
          .append(AND)
          .append(
            "(ASPECT:\"" +
            CircabcModel.CIRCABC_MODEL_PREFIX +
            ":circabcEvent\")"
          );
        agenda = true;
      } else if (searchIn.equalsIgnoreCase("FORUMS")) {
        queryBuilder
          .append(AND)
          .append(
            "(ASPECT:\"" +
            CircabcModel.CIRCABC_MODEL_PREFIX +
            ":circaNewsGroup\")"
          );
      }
    }

    queryBuilder.append(AND);

    //SearchFor: this parameter is used to specify on what value we want to search for a file or folder in the library:
    // ALL, NAME, TITLE or CONTENT.
    //This parameter is only used for Library not for Information, Forum or Agenda
    String search = "ALL";
    if (searchFor != null && !searchFor.equals("")) {
      if (searchFor.equalsIgnoreCase("NAME")) {
        search = "NAME";
      } else if (searchFor.equalsIgnoreCase("TITLE")) {
        search = "TITLE";
      } else if (
        searchFor.equalsIgnoreCase("CONTENT") ||
        searchFor.equalsIgnoreCase("TEXT")
      ) {
        search = "TEXT";
      }
    }

    // We need to manage the special case of Agenda if we search for ALL or in
    // Agenda
    if (all) {
      queryBuilder.append("(");
      queryBuilder.append((buildQueryParameter(q, search)));
      queryBuilder.append(OR);
      queryBuilder.append(buildQueryParameterforAgenda(q));
      queryBuilder.append(")");
    } else if (agenda) {
      queryBuilder.append(buildQueryParameterforAgenda(q));
    } else {
      queryBuilder.append(buildQueryParameter(q, search));
    }

    // Did we provide a language filter?
    if (language != null && !"".equals(language)) {
      Locale locale = new Locale(language);
      queryBuilder
        .append(AND)
        .append("(@sys\\:locale:" + locale.getLanguage() + "_*)");
    }

    // creator
    if (creator != null && creator.length() > 0) {
      queryBuilder.append(AND).append("(@creator:" + creator + ")");
    }

    // creation date
    // Did we provide from and/or to Date boudarie(s)?
    if (creationDateFrom != null || creationDateTo != null) {
      queryBuilder.append(AND).append("(@created:[");
      if (creationDateFrom != null) {
        creationDateFrom = DateUtils.setTimeAtMidnight(creationDateFrom);
        queryBuilder.append(Converter.convertDateToUTCString(creationDateFrom));
      } else {
        queryBuilder.append("MIN");
      }
      queryBuilder.append(" TO ");
      if (creationDateTo != null) {
        creationDateTo = DateUtils.setTimeAt23H59M59S(creationDateTo);
        queryBuilder.append(Converter.convertDateToUTCString(creationDateTo));
      } else {
        queryBuilder.append("MAX");
      }
      queryBuilder.append("])");
    }

    // modification date
    // Did we provide from and/or to Date boudarie(s)?
    if (modifiedDateFrom != null || modifiedDateTo != null) {
      queryBuilder.append(AND).append("(@modified:[");
      if (modifiedDateFrom != null) {
        modifiedDateFrom = DateUtils.setTimeAtMidnight(modifiedDateFrom);
        queryBuilder.append(Converter.convertDateToUTCString(modifiedDateFrom));
      } else {
        queryBuilder.append("MIN");
      }
      queryBuilder.append(" TO ");
      if (modifiedDateTo != null) {
        modifiedDateTo = DateUtils.setTimeAt23H59M59S(modifiedDateTo);
        queryBuilder.append(Converter.convertDateToUTCString(modifiedDateTo));
      } else {
        queryBuilder.append("MAX");
      }
      queryBuilder.append("])");
    }

    // keywords
    if (keywords != null && keywords.length() > 0) {
      // remove the square brackets arround the keywords String and remove the spaces
      keywords = keywords
        .replaceAll("\\[", "")
        .replaceAll("\\]", "")
        .replaceAll("\\s", "");
      String[] keywordParameters = keywords.split(",");
      String prefix = "workspace:\\/\\/SpacesStore\\/";
      int size = keywordParameters.length;

      for (int i = 0; i < size; i++) {
        queryBuilder
          .append(AND)
          .append("(+@")
          .append(
            QueryParser.escape(DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX)
          )
          .append("\\:keyword")
          .append(":\"")
          .append(prefix + QueryParser.escape(keywordParameters[i]))
          .append("\\*\"")
          .append(")");
      }
    }

    // Status
    if (
      status != null && status.length() > 0 && !status.equalsIgnoreCase("ALL")
    ) {
      queryBuilder
        .append(AND)
        .append(
          "(@" + DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX + "\\:status:"
        );
      queryBuilder.append(status.toUpperCase() + ")");
    }

    // SecurityRanking
    if (
      securityRanking != null &&
      securityRanking.length() > 0 &&
      !securityRanking.equalsIgnoreCase("ALL")
    ) {
      queryBuilder
        .append(AND)
        .append(
          "(@" +
          DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX +
          "\\:security_ranking:"
        );
      queryBuilder.append(securityRanking.toUpperCase() + ")");
    }

    // Version Label
    if (version != null && version.length() > 0) {
      queryBuilder.append(AND).append("(@versionLabel:" + version + ")");
    }

    //DynamicProperties
    //Check if we have at least one dynamicProperty provided
    boolean isDynamicPropertyFound = false;
    int i = 0;
    while (i < 20 && !isDynamicPropertyFound) {
      isDynamicPropertyFound = (dynamicProperties[i] != null &&
        dynamicProperties[i] != "");
      ++i;
    }
    if (isDynamicPropertyFound && nodeId != null) {
      NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
      queryBuilder.append(
        buildDynamicPropertiesParameter(nodeRef, dynamicProperties)
      );
    }

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLimit(limit);
    searchParameters.setSkipCount(limit * page);
    searchParameters.setMaxItems(limit);
    searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
    searchParameters.setQuery(queryBuilder.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.addStore(Repository.getStoreRef());

    //add sort parameter
    if (sort != null) {
      if (sort.equalsIgnoreCase("NAME")) {
        searchParameters.addSort("cm:name", order);
      } else if (sort.equalsIgnoreCase("TITLE")) {
        searchParameters.addSort("cm:title", order);
      } else if (sort.equalsIgnoreCase("CREATION_DATE")) {
        searchParameters.addSort("cm:created", order);
      } else if (sort.equalsIgnoreCase("MODIFICATION_DATE")) {
        searchParameters.addSort("cm:modified", order);
      } else if (sort.equalsIgnoreCase("ISSUE_DATE")) {
        searchParameters.addSort("cm:issue_date", order);
      }
    }

    ResultSet rs = searchService.query(searchParameters);

    long numberOfResults = 0;

    for (NodeRef ref : rs.getNodeRefs()) {
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

      if (nodeService.hasAspect(ref, CircabcModel.ASPECT_LIBRARY)) {
        if (nodeService.getType(ref).equals(ForumModel.TYPE_TOPIC)) {
          psn.setResultType("topic");
          psn.setTargetNode(getLibraryParentOfTopic(ref));
        } else if (nodeService.getType(ref).equals(ForumModel.TYPE_POST)) {
          psn.setResultType("post");
          psn.setTargetNode(getLibraryParentOfPost(ref));
        } else if (nodeService.getType(ref).equals(ContentModel.TYPE_FOLDER)) {
          psn.setResultType("folder");
        } else if (nodeService.getType(ref).equals(ContentModel.TYPE_CONTENT)) {
          psn.setResultType("file");
        }
      }

      if (nodeService.hasAspect(ref, CircabcModel.ASPECT_NEWSGROUP)) {
        if (nodeService.getType(ref).equals(ForumModel.TYPE_TOPIC)) {
          psn.setResultType("topic");
          psn.setTargetNode(getTopic(ref));
        } else if (nodeService.getType(ref).equals(ForumModel.TYPE_POST)) {
          psn.setResultType("post");
          psn.setTargetNode(getNewsgroupParentOfPost(ref));
        } else if (nodeService.getType(ref).equals(ForumModel.TYPE_FORUM)) {
          psn.setResultType("forum");
        }
      }

      if (nodeService.hasAspect(ref, CircabcModel.ASPECT_INFORMATION)) {
        if (
          nodeService.getType(ref).equals(CircabcModel.TYPE_INFORMATION_NEWS)
        ) {
          psn.setResultType("information");
          psn.setTargetNode(getTopic(ref));
        }
      }

      if (nodeService.hasAspect(ref, CircabcModel.ASPECT_EVENT)) {
        QName type = nodeService.getType(ref);
        if (type.equals(EventModel.TYPE_EVENT)) {
          psn.setResultType("event");
          psn.setTargetNode(getTopic(ref));
        } else if (type.equals(EventModel.PROP_MEETING_TYPE)) {
          psn.setResultType("meeting");
          psn.setTargetNode(getTopic(ref));
        }
      }

      // if the type is different => it will appear as OTHERS in the frontend
      if (psn.getResultType() == null || "".equals(psn.getResultType())) {
        QName type = nodeService.getType(ref);
        psn.setResultType(type.getLocalName());
        psn.setTargetNode(getTopic(ref));
      }

      // Add the current record to the list of results
      result.getData().add(psn);
      ++numberOfResults;
    }
    result.setTotal(numberOfResults);

    return result;
  }

  private String buildQueryParameter(String q, String searchFor) {
    StringBuilder queryBuilder = new StringBuilder();
    q = Converter.replaceEnclosingSingleQuotes(q.trim());

    if (q.startsWith("\"") && q.endsWith("\"")) {
      queryBuilder.append("(");
      //if All
      if (searchFor.equalsIgnoreCase("ALL")) {
        queryBuilder.append("TEXT:").append(q);
        queryBuilder.append(OR);
        queryBuilder.append("@name:").append(q);
        queryBuilder.append(OR);
        queryBuilder.append("@title:").append(q);
        queryBuilder.append(")");
      } else if (searchFor.equalsIgnoreCase("TEXT")) {
        queryBuilder.append("TEXT:").append(q);
      } else if (searchFor.equalsIgnoreCase("NAME")) {
        queryBuilder.append("@name:").append(q);
      } else if (searchFor.equalsIgnoreCase("TITLE")) {
        queryBuilder.append("@title:").append(q);
        queryBuilder.append(")");
      }
    } else {
      queryBuilder.append("(");
      String[] queryItems = q.split(" ");

      for (int i = 0; i < queryItems.length; i++) {
        if (searchFor.equalsIgnoreCase("ALL")) {
          queryBuilder
            .append("TEXT:")
            .append(QueryParser.escape(queryItems[i]));
          queryBuilder.append(OR);
          queryBuilder
            .append("@name:")
            .append(QueryParser.escape(queryItems[i]));
          queryBuilder.append(OR);
          queryBuilder
            .append("@title:")
            .append(QueryParser.escape(queryItems[i]));
        } else if (searchFor.equalsIgnoreCase("TEXT")) {
          queryBuilder
            .append("TEXT:")
            .append(QueryParser.escape(queryItems[i]));
        } else if (searchFor.equalsIgnoreCase("NAME")) {
          queryBuilder
            .append("@name:")
            .append(QueryParser.escape(queryItems[i]));
        } else if (searchFor.equalsIgnoreCase("TITLE")) {
          queryBuilder
            .append("@title:")
            .append(QueryParser.escape(queryItems[i]));
        }
        if (i + 1 < queryItems.length) {
          queryBuilder.append(OR);
        }
      }
      queryBuilder.append(")");
    }
    return queryBuilder.toString();
  }

  private String buildQueryParameterforAgenda(String q) {
    // For the special case of agenda, we only have the field title from the
    // EventModel {http://www.cc.cec/circabc/model/events/1.0}title with prefix ce

    StringBuilder queryBuilder = new StringBuilder();
    q = Converter.replaceEnclosingSingleQuotes(q.trim());

    if (q.startsWith("\"") && q.endsWith("\"")) {
      queryBuilder
        .append("(@" + EventModel.CIRCABC_EVENT_MODEL_PREFIX + "\\:title:")
        .append(q)
        .append(AND)
        .append("TYPE:" + EventModel.CIRCABC_EVENT_MODEL_PREFIX + "\\:event")
        .append(")");
    } else {
      queryBuilder.append("((");
      String[] queryItems = q.split(" ");

      for (int i = 0; i < queryItems.length; i++) {
        queryBuilder
          .append("@" + EventModel.CIRCABC_EVENT_MODEL_PREFIX + "\\:title:")
          .append(QueryParser.escape(queryItems[i]));
        if (i + 1 < queryItems.length) {
          queryBuilder.append(OR);
        }
      }
      queryBuilder.append(")");
      queryBuilder
        .append(AND)
        .append("TYPE:" + EventModel.CIRCABC_EVENT_MODEL_PREFIX + "\\:event")
        .append(")");
    }
    return queryBuilder.toString();
  }

  private String buildDynamicPropertiesParameter(
    NodeRef node,
    String[] dynamicProperties
  ) {
    StringBuilder queryBuilder = new StringBuilder();
    List<DynamicProperty> interestGroupDynamicProperties = null;
    //Dynamic Properties are only available at the level of an Interest Group
    //If the given node ID is not an InteretsGroup or beloging to an Interest Group (a category for instance),
    //the method dynamicPropertyService.getDynamicProperties(node) will throw an IllegalArgumentException
    //In this case, we will just ignore the dynamic properties paremeters and not include them in the Lucene Query
    try {
      interestGroupDynamicProperties =
        dynamicPropertyService.getDynamicProperties(node);
    } catch (IllegalArgumentException e) {
      //nothing to do, just discrad the dynamic properties parameters from the query.
      return "";
    }
    int nbDynamicProperties = interestGroupDynamicProperties.size();
    final Map<QName, DynamicProperty> map = new HashMap<>(nbDynamicProperties);

    for (DynamicProperty dynamicProperty : interestGroupDynamicProperties) {
      final QName propertyQname = getDynamicPropertyService()
        .getPropertyQname(dynamicProperty);
      map.put(propertyQname, dynamicProperty);
    }

    int i = 1;
    for (QName qName : DocumentModel.ALL_DYN_PROPS) {
      String dynamicProperty = dynamicProperties[i - 1];
      if (dynamicProperty != null && !dynamicProperty.toString().equals("")) {
        String value = dynamicProperty.trim();
        final DynamicProperty property = map.get(qName);
        if (property.getType().equals(DynamicPropertyType.MULTI_SELECTION)) {
          final String validValues = property.getValidValues().trim();
          if (validValues.contains(value)) {
            queryBuilder
              .append(AND)
              .append(buildDynamicPropertyPrefix(qName))
              .append("*" + value + "*)");
          }
        } else if (property.getType().equals(DynamicPropertyType.DATE_FIELD)) {
          Date dateValue = null;
          try {
            dateValue = Converter.convertStringToSimpleDate(value);
            Date dateFrom = DateUtils.setTimeAtMidnight(dateValue);

            queryBuilder
              .append(AND)
              .append(buildDynamicPropertyPrefix(qName))
              .append(
                "\"" + Converter.convertDateToUTCString(dateFrom) + "\")"
              );
          } catch (ParseException e) {
            //If there is an exception, just discard the parameter
          }
        } else {
          queryBuilder
            .append(AND)
            .append(buildDynamicPropertyPrefix(qName))
            .append(dynamicProperty)
            .append(")");
        }
      }
      i++;
    }
    return queryBuilder.toString();
  }

  private String buildDynamicPropertyPrefix(QName qName) {
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
      .append("(+@")
      .append(QueryParser.escape(DocumentModel.CIRCABC_DOCUMENT_MODEL_PREFIX))
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

  /**
   * @return the searchService
   */
  public SearchService getSearchService() {
    return searchService;
  }

  /**
   * @param searchService the searchService to set
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public DynamicPropertyService getDynamicPropertyService() {
    return dynamicPropertyService;
  }

  public void setDynamicPropertyService(
    DynamicPropertyService dynamicPropertyService
  ) {
    this.dynamicPropertyService = dynamicPropertyService;
  }

  public ApiToolBox getApiToolBox() {
    return apiToolBox;
  }

  public void setApiToolBox(ApiToolBox apiToolBox) {
    this.apiToolBox = apiToolBox;
  }

  public NodesApi getNodesApi() {
    return nodesApi;
  }

  public void setNodesApi(NodesApi nodesApi) {
    this.nodesApi = nodesApi;
  }
}
