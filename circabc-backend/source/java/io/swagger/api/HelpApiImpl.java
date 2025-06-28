package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.model.EmailDefinition;
import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of HelpApi interface.
 *
 * @author beaurpi, morleal
 */
public class HelpApiImpl implements HelpApi {

  private static final String FAQS = "faqs";
  private static final String FAQS_LINKS = "faqsLinks";
  private static final String CLOSE_QUERY = " )";
  private static final String OPEN_QUERY = "( ";
  private static final String AND = " AND ";
  private static final String OR = " OR ";
  private static final String PATH = "PATH:";
  private static final String ESCAPE_QUOTES = "\" ";
  private static final String ASPECT_HIGHLIGHTED =
    "ASPECT:ci\\:helpArticleHighlighted";
  private static final String MODIFIED = "modified";
  private static final String TITLE_QUERY = "@title:";
  private static final String DESCRIPTION_QUERY = "@description:";
  private static final String STAR_QUERY = "*";
  private static final String THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE =
    "The target node is not a help article";

  private static final String OTHER = "help.reason.other";
  private static final String UPLOAD_DOWNLOAD =
    "help.reason.upload.or.download";
  private static final String ACCESS_PERMISSION =
    "help.reason.access.or.permission";
  private static final String ADMINISTER_GROUP =
    "help.reason.administer.my.group";

  private static final String INCIDENT_SUFFIX = "/emdig/itsm/incident";
  private static final String ATTACHMENT_SUFFIX =
    "/now/attachment/file?table_name=incident&table_sys_id=";

  //ServiceNow
  private boolean serviceNowEnable;
  private boolean serviceNowPrefix;
  private String serviceNowUrl;
  private String serviceNowUser;
  private String serviceNowPassword;
  private String environmentName;

  //Proxy parameters
  private boolean proxyEnable;
  private String proxyUrl;
  private int proxyPort;
  private String proxyUsername;
  private String proxyPassword;

  private AuthenticationService authenticationService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private PersonService personService;
  private ManagementService managementService;
  private SearchService searchService;
  private ApiToolBox apiToolBox;
  private EmailApi emailApi;
  private LdapUserService ldapUserService;

  private static final Log logger = LogFactory.getLog(HelpApiImpl.class);

  @Override
  public List<HelpCategory> getHelpCategories() {
    NodeRef faqsRef = getFaqsRef();

    List<HelpCategory> result = new ArrayList<>();

    if (faqsRef != null) {
      for (ChildAssociationRef child : nodeService.getChildAssocs(faqsRef)) {
        if (
          nodeService.hasAspect(
            child.getChildRef(),
            CircabcModel.ASPECT_HELP_CATEGORY
          )
        ) {
          result.add(getHelpCategory(child.getChildRef().getId()));
        }
      }
    }

    return result;
  }

  private NodeRef getFaqsRef() {
    NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);
  }

  private NodeRef getFaqLinksRef() {
    NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(
      ddRef,
      ContentModel.ASSOC_CONTAINS,
      FAQS_LINKS
    );
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }

  public AuthenticationService getAuthenticationService() {
    return authenticationService;
  }

  public void setAuthenticationService(
    AuthenticationService authenticationService
  ) {
    this.authenticationService = authenticationService;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  public void setLdapUserService(LdapUserService ldapUserService) {
    this.ldapUserService = ldapUserService;
  }

  //ServiceNow parameters
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  public void setServiceNowEnable(boolean serviceNowEnable) {
    this.serviceNowEnable = serviceNowEnable;
  }

  public void setServiceNowPrefix(boolean serviceNowPrefix) {
    this.serviceNowPrefix = serviceNowPrefix;
  }

  public void setServiceNowUrl(String serviceNowUrl) {
    this.serviceNowUrl = serviceNowUrl;
  }

  public void setServiceNowUser(String serviceNowUser) {
    this.serviceNowUser = serviceNowUser;
  }

  public void setServiceNowPassword(String serviceNowPassword) {
    this.serviceNowPassword = serviceNowPassword;
  }

  //Proxy parameters
  public void setProxyEnable(boolean proxyEnable) {
    this.proxyEnable = proxyEnable;
  }

  public void setProxyUrl(String proxyUrl) {
    this.proxyUrl = proxyUrl;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public void setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  @Override
  public HelpCategory createHelpCategory(HelpCategory helpCategory) {
    NodeRef faqsRef = getFaqsRef();

    if (helpCategory.getTitle() != null) {
      String name = "";

      if (helpCategory.getTitle().containsKey("en")) {
        name = helpCategory.getTitle().get("en");
      } else {
        if (!helpCategory.getTitle().keySet().isEmpty()) {
          String key = (String) helpCategory.getTitle().keySet().toArray()[0];
          name = helpCategory.getTitle().get(key);
        }
      }

      name = getCleanFileName(name);

      ChildAssociationRef categRef = nodeService.createNode(
        faqsRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.ALFRESCO_URI, name),
        ContentModel.TYPE_FOLDER
      );
      nodeService.addAspect(
        categRef.getChildRef(),
        CircabcModel.ASPECT_HELP_CATEGORY,
        null
      );

      nodeService.setProperty(
        categRef.getChildRef(),
        ContentModel.PROP_NAME,
        name
      );
      nodeService.setProperty(
        categRef.getChildRef(),
        ContentModel.PROP_TITLE,
        Converter.toMLText(helpCategory.getTitle())
      );

      helpCategory.setId(categRef.getChildRef().getId());
    }

    return helpCategory;
  }

  private String getCleanFileName(String filename) {
    return filename.replaceAll("[\\\\/:*?\"<>|!]", "_");
  }

  @Override
  public HelpCategory getHelpCategory(String id) {
    NodeRef helpCategoryRef = Converter.createNodeRefFromId(id);

    HelpCategory category = new HelpCategory();
    category.setId(helpCategoryRef.getId());

    Serializable title = nodeService.getProperty(
      helpCategoryRef,
      ContentModel.PROP_TITLE
    );
    if (title instanceof String) {
      category.setTitle(Converter.toI18NProperty((String) title));
    } else if (title instanceof MLText) {
      category.setTitle(Converter.toI18NProperty((MLText) title));
    }

    Integer nbArticles = 0;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        nbArticles++;
      }
    }

    category.setNumberOfArticles(nbArticles);

    return category;
  }

  @Override
  public List<HelpArticle> getCategoryArticles(
    String categoryId,
    Boolean loadContent
  ) {
    NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    List<HelpArticle> result = new ArrayList<>();

    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        result.add(
          getHelpArticleInternal(child.getChildRef().getId(), loadContent)
        );
      }
    }

    return result;
  }

  @Override
  public HelpArticle getHelpArticle(String id) {
    return getHelpArticleInternal(id, true);
  }

  private HelpArticle getHelpArticleInternal(String id, Boolean loadContent) {
    NodeRef helpArticleRef = Converter.createNodeRefFromId(id);

    if (
      nodeService.hasAspect(helpArticleRef, CircabcModel.ASPECT_HELP_ARTICLE)
    ) {
      HelpArticle result = new HelpArticle();

      result.setId(helpArticleRef.getId());

      Serializable title = nodeService.getProperty(
        helpArticleRef,
        ContentModel.PROP_TITLE
      );
      if (title instanceof String) {
        result.setTitle(Converter.toI18NProperty((String) title));
      } else if (title instanceof MLText) {
        result.setTitle(Converter.toI18NProperty((MLText) title));
      }

      if (Boolean.TRUE.equals(loadContent)) {
        Serializable description = nodeService.getProperty(
          helpArticleRef,
          ContentModel.PROP_DESCRIPTION
        );
        if (description instanceof String) {
          result.setContent(Converter.toI18NProperty((String) description));
        } else if (description instanceof MLText) {
          result.setContent(Converter.toI18NProperty((MLText) description));
        }
      }

      NodeRef usernameRef = personService.getPerson(
        nodeService
          .getProperty(helpArticleRef, ContentModel.PROP_MODIFIER)
          .toString()
      );
      String author =
        nodeService
          .getProperty(usernameRef, ContentModel.PROP_FIRSTNAME)
          .toString() +
        " " +
        nodeService
          .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
          .toString()
          .toUpperCase();
      result.setAuthor(author);

      result.setLastUpdate(
        new DateTime(
          nodeService.getProperty(helpArticleRef, ContentModel.PROP_MODIFIED)
        )
      );

      result.setHighlighted(
        nodeService.hasAspect(
          helpArticleRef,
          CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
        )
      );

      result.setParentId(
        nodeService.getPrimaryParent(helpArticleRef).getParentRef().getId()
      );

      return result;
    }

    return null;
  }

  @Override
  public HelpArticle createHelpArticle(String categoryId, HelpArticle article) {
    NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new InvalidArgumentException(
        "The target node is not a help category"
      );
    }

    MLText title = Converter.toMLText(article.getTitle());
    String name = title.getDefaultValue();

    name = getCleanFileName(name);

    ChildAssociationRef articleRef = nodeService.createNode(
      helpCategoryRef,
      ContentModel.ASSOC_CONTAINS,
      QName.createQName(NamespaceService.ALFRESCO_URI, name),
      ContentModel.TYPE_CONTENT
    );
    nodeService.addAspect(
      articleRef.getChildRef(),
      CircabcModel.ASPECT_HELP_ARTICLE,
      null
    );

    nodeService.setProperty(
      articleRef.getChildRef(),
      ContentModel.PROP_NAME,
      name
    );
    nodeService.setProperty(
      articleRef.getChildRef(),
      ContentModel.PROP_TITLE,
      Converter.toMLText(article.getTitle())
    );
    nodeService.setProperty(
      articleRef.getChildRef(),
      ContentModel.PROP_DESCRIPTION,
      Converter.toMLText(article.getContent())
    );

    article.setId(articleRef.getChildRef().getId());

    NodeRef usernameRef = personService.getPerson(
      nodeService
        .getProperty(articleRef.getChildRef(), ContentModel.PROP_CREATOR)
        .toString()
    );
    String author =
      nodeService
        .getProperty(usernameRef, ContentModel.PROP_FIRSTNAME)
        .toString() +
      " " +
      nodeService
        .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
        .toString()
        .toUpperCase();
    article.setAuthor(author);

    article.setLastUpdate(
      new DateTime(
        nodeService.getProperty(
          articleRef.getChildRef(),
          ContentModel.PROP_MODIFIED
        )
      )
    );

    return article;
  }

  public PersonService getPersonService() {
    return personService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  @Override
  public void deleteHelpArticle(String id) {
    NodeRef helpArticleRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.hasAspect(helpArticleRef, CircabcModel.ASPECT_HELP_ARTICLE)
    ) {
      throw new InvalidArgumentException(THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE);
    }

    nodeService.deleteNode(helpArticleRef);
  }

  @Override
  public HelpArticle updateHelpArticle(String id, HelpArticle article) {
    NodeRef articleRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(articleRef)) {
      throw new InvalidArgumentException("The target node is not existing");
    }

    if (!nodeService.hasAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
      throw new InvalidArgumentException(THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE);
    }

    nodeService.setProperty(
      articleRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(article.getTitle())
    );
    nodeService.setProperty(
      articleRef,
      ContentModel.PROP_DESCRIPTION,
      Converter.toMLText(article.getContent())
    );

    NodeRef usernameRef = personService.getPerson(
      nodeService.getProperty(articleRef, ContentModel.PROP_CREATOR).toString()
    );
    String author =
      nodeService
        .getProperty(usernameRef, ContentModel.PROP_FIRSTNAME)
        .toString() +
      " " +
      nodeService
        .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
        .toString()
        .toUpperCase();
    article.setAuthor(author);

    article.setLastUpdate(
      new DateTime(
        nodeService.getProperty(articleRef, ContentModel.PROP_MODIFIED)
      )
    );

    return article;
  }

  @Override
  public void deleteHelpCategory(String id) {
    NodeRef helpCategoryRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new InvalidArgumentException(
        "The target node is not a help category"
      );
    }

    nodeService.deleteNode(helpCategoryRef);
  }

  @Override
  public HelpCategory updateHelpCategory(String id, HelpCategory category) {
    NodeRef categoryRefRef = Converter.createNodeRefFromId(id);

    if (category.getTitle() != null) {
      nodeService.setProperty(
        categoryRefRef,
        ContentModel.PROP_TITLE,
        Converter.toMLText(category.getTitle())
      );
    }

    return category;
  }

  @Override
  public HelpArticle toggleHighlightArticle(String id) {
    NodeRef articleRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(articleRef)) {
      throw new InvalidArgumentException("The target node is not existing");
    }

    if (!nodeService.hasAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
      throw new InvalidArgumentException(THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE);
    }

    if (
      !nodeService.hasAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      )
    ) {
      nodeService.addAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED,
        null
      );
    } else {
      nodeService.removeAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      );
    }

    return getHelpArticle(id);
  }

  @Override
  public List<HelpArticle> getHighlightedArticles() {
    String query = buildHighlightedQuery();

    List<HelpArticle> result = new ArrayList<>();

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(query);
    sp.addStore(Repository.getStoreRef());
    sp.addSort(MODIFIED, true);

    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodeRefs = rs.getNodeRefs();

    for (NodeRef articleRef : nodeRefs) {
      result.add(getHelpArticle(articleRef.getId()));
    }

    return result;
  }

  private String buildHighlightedQuery() {
    NodeRef faqsRef = getFaqsRef();

    return (
      OPEN_QUERY +
      PATH +
      ESCAPE_QUOTES +
      apiToolBox.getPathFromSpaceRef(faqsRef, true) +
      ESCAPE_QUOTES +
      CLOSE_QUERY +
      AND +
      OPEN_QUERY +
      ASPECT_HIGHLIGHTED +
      CLOSE_QUERY
    );
  }

  public SearchService getSearchService() {
    return searchService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public ApiToolBox getApiToolBox() {
    return apiToolBox;
  }

  public void setApiToolBox(ApiToolBox apiToolBox) {
    this.apiToolBox = apiToolBox;
  }

  @Override
  public List<HelpLink> getHelpLinks() {
    List<HelpLink> result = new ArrayList<>();
    NodeRef faqLinksRef = getFaqLinksRef();

    if (faqLinksRef != null) {
      for (ChildAssociationRef child : nodeService.getChildAssocs(
        faqLinksRef
      )) {
        if (
          nodeService.hasAspect(
            child.getChildRef(),
            CircabcModel.ASPECT_HELP_LINK
          )
        ) {
          result.add(getHelpLink(child.getChildRef().getId()));
        }
      }
    }

    return result;
  }

  @Override
  public HelpLink getHelpLink(String id) {
    HelpLink result;

    NodeRef helpLinkRef = Converter.createNodeRefFromId(id);

    result = new HelpLink();
    result.setId(id);

    Object titleObj = nodeService.getProperty(
      helpLinkRef,
      ContentModel.PROP_TITLE
    );
    if (titleObj instanceof String) {
      result.setTitle(Converter.toI18NProperty((String) titleObj));
    } else if (titleObj instanceof MLText) {
      result.setTitle(Converter.toI18NProperty((MLText) titleObj));
    }

    Serializable href = nodeService.getProperty(
      helpLinkRef,
      CircabcModel.PROP_HELP_LINK_HREF
    );
    if (href != null) {
      result.setHref(href.toString());
    }

    return result;
  }

  @Override
  public HelpLink createHelpLink(HelpLink body) {
    NodeRef faqLinksRef = getFaqLinksRef();

    if (body.getTitle() != null) {
      String name = "";

      if (body.getTitle().containsKey("en")) {
        name = body.getTitle().get("en");
      } else {
        if (!body.getTitle().keySet().isEmpty()) {
          String key = (String) body.getTitle().keySet().toArray()[0];
          name = body.getTitle().get(key);
        }
      }

      name = "link--" + getCleanFileName(name);

      ChildAssociationRef linkRef = nodeService.createNode(
        faqLinksRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.ALFRESCO_URI, name),
        ContentModel.TYPE_CONTENT
      );
      nodeService.addAspect(
        linkRef.getChildRef(),
        CircabcModel.ASPECT_HELP_LINK,
        null
      );

      nodeService.setProperty(
        linkRef.getChildRef(),
        ContentModel.PROP_NAME,
        name
      );
      nodeService.setProperty(
        linkRef.getChildRef(),
        ContentModel.PROP_TITLE,
        Converter.toMLText(body.getTitle())
      );
      nodeService.setProperty(
        linkRef.getChildRef(),
        CircabcModel.PROP_HELP_LINK_HREF,
        body.getHref()
      );

      body.setId(linkRef.getChildRef().getId());
    }

    return body;
  }

  @Override
  public HelpLink updateHelpLink(HelpLink body) {
    if (body.getTitle() != null && body.getId() != null) {
      NodeRef linkRef = Converter.createNodeRefFromId(body.getId());

      if (nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)) {
        nodeService.setProperty(
          linkRef,
          ContentModel.PROP_TITLE,
          Converter.toMLText(body.getTitle())
        );
        nodeService.setProperty(
          linkRef,
          CircabcModel.PROP_HELP_LINK_HREF,
          body.getHref()
        );
      }
    }

    return body;
  }

  @Override
  public void deleteHelpLink(String id) {
    NodeRef linkRef = Converter.createNodeRefFromId(id);

    if (nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)) {
      nodeService.deleteNode(linkRef);
    }
  }

  @Override
  public HelpSearchResult searchHelp(String query) {
    HelpSearchResult result = new HelpSearchResult();

    String faqsQuery = buildSearchFaqsQuery(query);

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(faqsQuery);
    sp.addStore(Repository.getStoreRef());
    sp.addSort(MODIFIED, true);

    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodeRefs = rs.getNodeRefs();

    for (NodeRef nodeRef : nodeRefs) {
      if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_HELP_CATEGORY)) {
        result.getCategories().add(getHelpCategory(nodeRef.getId()));
      } else if (
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_HELP_ARTICLE)
      ) {
        result.getArticles().add(getHelpArticle(nodeRef.getId()));
      }
    }

    String faqsLinksQuery = buildSearchFaqsLinksQuery(query);

    final SearchParameters spl = new SearchParameters();
    spl.setLanguage(SearchService.LANGUAGE_LUCENE);
    spl.setQuery(faqsLinksQuery);
    spl.addStore(Repository.getStoreRef());
    spl.addSort(MODIFIED, true);

    ResultSet rsl = searchService.query(spl);
    List<NodeRef> nodeLinksRefs = rsl.getNodeRefs();

    for (NodeRef nodeRef : nodeLinksRefs) {
      if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_HELP_LINK)) {
        result.getLinks().add(getHelpLink(nodeRef.getId()));
      }
    }

    return result;
  }

  private String buildSearchFaqsQuery(String query) {
    NodeRef faqsRef = getFaqsRef();
    final StringBuilder queryBuild = new StringBuilder();
    String[] parts = query.trim().split(" ");

    queryBuild
      .append(OPEN_QUERY)
      .append(PATH)
      .append(ESCAPE_QUOTES)
      .append(apiToolBox.getPathFromSpaceRef(faqsRef, true))
      .append(ESCAPE_QUOTES)
      .append(CLOSE_QUERY)
      .append(AND)
      .append(OPEN_QUERY);

    if ("".equals(query) || "*".equals(query)) {
      queryBuild.append(TITLE_QUERY).append(STAR_QUERY);
    } else {
      for (int i = 0; i < parts.length; i++) {
        if (!Objects.equals(parts[i], "")) {
          queryBuild
            .append(TITLE_QUERY)
            .append(STAR_QUERY)
            .append(parts[i])
            .append(STAR_QUERY);
          queryBuild.append(OR);
          queryBuild
            .append(DESCRIPTION_QUERY)
            .append(STAR_QUERY)
            .append(parts[i])
            .append(STAR_QUERY);

          if (i < parts.length - 1) {
            queryBuild.append(OR);
          }
        }
      }
    }

    queryBuild.append(CLOSE_QUERY);

    return queryBuild.toString();
  }

  private String buildSearchFaqsLinksQuery(String query) {
    NodeRef faqLinksRef = getFaqLinksRef();
    final StringBuilder queryBuild = new StringBuilder();
    String[] parts = query.trim().split(" ");

    queryBuild
      .append(OPEN_QUERY)
      .append(PATH)
      .append(ESCAPE_QUOTES)
      .append(apiToolBox.getPathFromSpaceRef(faqLinksRef, true))
      .append(ESCAPE_QUOTES)
      .append(CLOSE_QUERY)
      .append(AND)
      .append(OPEN_QUERY);

    if ("".equals(query) || "*".equals(query)) {
      queryBuild.append(TITLE_QUERY).append(STAR_QUERY);
    } else {
      for (int i = 0; i < parts.length; i++) {
        if (!Objects.equals(parts[i], "")) {
          queryBuild
            .append(TITLE_QUERY)
            .append(STAR_QUERY)
            .append(parts[i])
            .append(STAR_QUERY);
          if (i < parts.length - 1) {
            queryBuild.append(OR);
          }
        }
      }
    }

    queryBuild.append(CLOSE_QUERY);

    return queryBuild.toString();
  }

  /** @return the emailApi */
  public EmailApi getEmailApi() {
    return emailApi;
  }

  /** @param emailApi the emailApi to set */
  public void setEmailApi(EmailApi emailApi) {
    this.emailApi = emailApi;
  }

  /** Create ServiceNow Incident Ticket. */
  private Ticket createServiceNowTicket(
    String userName,
    String emailFrom,
    String subject,
    String content
  ) throws Exception {
    Ticket ticket = null;

    if (logger.isDebugEnabled()) {
      logger.debug(
        "About to invoke ServiceNow API to create an Incident Ticket with the following parameters: url=" +
        serviceNowUrl +
        ", ServiceNow User=" +
        serviceNowUser +
        ", Contact User: " +
        userName +
        ", email: " +
        emailFrom +
        ", subject:" +
        subject +
        ", content: " +
        content
      );
    }

    // HttpClient - set proxy parameters if needed for the current environment
    DefaultHttpClient client = proxyEnable
      ? getProxyHttpClient()
      : new DefaultHttpClient();

    HttpPost post = new HttpPost(serviceNowUrl + INCIDENT_SUFFIX);
    post.setHeader("Content-Type", "application/json; charset=utf-8");
    post.setHeader(
      "Authorization",
      "Basic " +
      Base64.getEncoder()
        .encodeToString((serviceNowUser + ":" + serviceNowPassword).getBytes())
    );

    // Get info bout the current user
    String currentUser = authenticationService.getCurrentUserName();

    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"incident\":");
    json.append("{");
    json.append("\"assignment_group\":\"DIGIT CIRCABC Support\",");
    json.append("\"business_service\":\"CIRCABC service\",");
    json.append("\"category\":\"incident\",");

    if (currentUserPermissionCheckerService.isGuest()) {
      //if current user is Guest
      json.append("\"caller_id\": \"Guest\",");

      //retrieve firstname and lastname from input parameter userName
      String firstName = null, lastName = null;
      if (userName != null && userName != "") {
        //if there is at least one space character
        if (userName.indexOf(" ") != -1) {
          firstName = userName.substring(0, userName.indexOf(" "));
          lastName = userName.substring(
            userName.indexOf(" ") + 1,
            userName.length()
          );
        } else {
          lastName = userName;
        }
        if (firstName != null) {
          json.append("\"u_external_first_name\": \"" + firstName + "\",");
        }
        if (lastName != null) {
          json.append("\"u_external_last_name\": \"" + lastName + "\",");
        }
      }
      if (emailFrom != null) {
        json.append("\"u_external_email\": \"" + emailFrom + "\",");
      }
    } else if (currentUserPermissionCheckerService.isExternalUser()) {
      //if current user is external
      json.append("\"caller_id\": \"Guest\",");

      //retrieve info about the user
      CircabcUserDataBean user = ldapUserService.getLDAPUserDataByUid(
        currentUser
      );
      String firstName = user.getFirstName();
      String lastName = user.getLastName();
      String email = user.getEmail();
      if (firstName != null) {
        json.append("\"u_external_first_name\": \"" + firstName + "\",");
      }
      if (lastName != null) {
        json.append("\"u_external_last_name\": \"" + lastName + "\",");
      }
      if (email != null) {
        json.append("\"u_external_email\": \"" + email + "\",");
      }
    } else {
      //internal user
      json.append("\"caller_id\": \"" + currentUser.toUpperCase() + "\",");
      //the other fields will be automatically extracted when creating the ticket
    }

    json.append("\"short_description\": \"" + subject + "\",");

    //remove html tags from the description/content of the ticket
    String description = content != null
      ? Converter.convertHtmlToJsonText(content)
      : "";
    json.append("\"description\": \"" + description + "\",");

    json.append("\"service_offering\": \"CIRCABC\"");

    //Do we keep default impact, priority and urgency?
    //json.append("\"impact\": \"3\",");
    //json.append("\"priority\": \"4\",");
    //json.append("\"urgency\": \"3\",");

    json.append("}");
    json.append("}");

    try {
      //Json query should be in UTF-8 encoding
      post.setEntity(
        new StringEntity(json.toString(), StandardCharsets.UTF_8.toString())
      );

      if (logger.isDebugEnabled()) {
        logger.debug("Executing request " + post.getRequestLine());

        Header[] headers = post.getAllHeaders();

        logger.debug("Http Headers: ");
        for (Header header : headers) {
          // do not write password in the logs
          if ("Authorization".contentEquals(header.getName())) {
            logger.debug(
              "Header: " + header.getName() + ", value: BASIC xxxx===="
            );
          } else {
            logger.debug(
              "Header: " + header.getName() + ", value: " + header.getValue()
            );
          }
        }
      }
      HttpResponse response = client.execute(post);
      String result = EntityUtils.toString(response.getEntity());

      if (logger.isDebugEnabled()) {
        logger.debug(response.getProtocolVersion()); // HTTP/1.1
        logger.debug(response.getStatusLine().getStatusCode()); // 201
        logger.debug(response.getStatusLine().getReasonPhrase()); // Created
        logger.debug(response.getStatusLine().toString()); // HTTP/1.1 201 Created
        logger.debug("ServiceNow response: " + result);
      }
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
        // we need to parse the response to find the reference of the incident ticket and
        // table_sys_id if we need to add attachment(s)
        try {
          JSONObject jsonResponse = new JSONObject(result).getJSONObject(
            "result"
          );
          String serviceNowTicket = jsonResponse.getString("displayname");
          String sys_id = jsonResponse.getString("sys_id");
          ticket = new Ticket(serviceNowTicket, sys_id);
        } catch (JSONException e) {
          logger.error(e.getMessage(), e);
        }
      } else {
        // we did not receive HTTP 201 response code
        logger.warn(
          "Error when invoking ServiceNow API to create a ticket for user " +
          userName +
          ": " +
          response.getStatusLine().toString()
        );
        throw new HttpException(response.getStatusLine().toString());
      }
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      throw (e);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw (e);
    } finally {
      client.getConnectionManager().shutdown();
    }

    return ticket;
  }

  /** Add Attachment to an existing ServiceNow Ticket. */
  private void addAttachmentToServiceNowTicket(Ticket ticket, File attachment)
    throws Exception {
    // we need to retrieve the original file name from the temporary file name
    String fileName = Converter.getOriginalFileName(attachment.getName());

    // HttpClient - set proxy parameters if needed for the current environment
    DefaultHttpClient client = proxyEnable
      ? getProxyHttpClient()
      : new DefaultHttpClient();

    String endpointURL =
      serviceNowUrl +
      ATTACHMENT_SUFFIX +
      ticket.getSys_id() +
      "&file_name=" +
      URLEncoder.encode(fileName, "UTF-8");
    if (logger.isDebugEnabled()) {
      logger.debug(
        "About to execture POST method to Rest endpoint URL: " +
        endpointURL +
        " to attach the file " +
        attachment.getName() +
        " with the name " +
        fileName +
        " to the ServiceNow Ticket " +
        ticket.getServiceNowTicket()
      );
    }

    HttpPost post = new HttpPost(endpointURL);

    post.setHeader("Content-Type", "application/octetd-stream");
    post.setHeader(
      "Authorization",
      "Basic " +
      Base64.getEncoder()
        .encodeToString((serviceNowUser + ":" + serviceNowPassword).getBytes())
    );

    // DEBUG the Headers
    Header[] headers = post.getAllHeaders();

    if (logger.isDebugEnabled()) {
      logger.debug("Http Headers: ");
      for (Header header : headers) {
        // do not write password in the logs
        if ("Authorization".contentEquals(header.getName())) {
          logger.debug(
            "Header: " + header.getName() + ", value: BASIC xxxx===="
          );
        } else {
          logger.debug(
            "Header: " + header.getName() + ", value: " + header.getValue()
          );
        }
      }
    }

    // ADD attachment
    FileEntity entity = new FileEntity(attachment, "application/octect-stream");
    post.setEntity(entity);

    try {
      HttpResponse response = client.execute(post);
      String result = EntityUtils.toString(response.getEntity());

      if (logger.isDebugEnabled()) {
        logger.debug(response.getProtocolVersion()); // HTTP/1.1
        logger.debug(response.getStatusLine().getStatusCode()); // 201
        logger.debug(response.getStatusLine().getReasonPhrase()); // Created
        logger.debug(response.getStatusLine().toString()); // HTTP/1.1 201 Created
        logger.debug("ServiceNow response: " + result);
      }
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
        /// we did not receive HTTP 201 response code
        throw new HttpException(response.getStatusLine().toString());
      }
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      throw (e);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw (e);
    } finally {
      client.getConnectionManager().shutdown();
    }
  }

  @Override
  public void contactSupport(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    List<File> attachementsFiles
  ) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug(
        "Method contactSupport has been called. Is ServiceNow API enable? " +
        serviceNowEnable
      );
    }

    //Is the integration with ServiceNow API enable for this environment?
    if (serviceNowEnable) {
      // if reason is not OTHER, the field subject is empty
      // use the reason for the subject
      if (!OTHER.equals(reason)) {
        if (UPLOAD_DOWNLOAD.equals(reason)) {
          subject = "Upload or Download";
        } else if (ACCESS_PERMISSION.equals(reason)) {
          subject = "Access Permission";
        } else if (ADMINISTER_GROUP.equals(reason)) {
          subject = "Interest Group Administration";
        }
      }

      // prefix the subject with the environment name
      if (
        serviceNowPrefix && environmentName != "" && environmentName != null
      ) {
        subject = "[" + environmentName + "] - " + subject;
      }

      Ticket ticket = null;

      // invoking ServiceNow API to create ServiceNow ticket
      try {
        ticket = createServiceNowTicket(name, emailFrom, subject, content);
      } catch (Exception e) {
        //something wrong happend when invoking ServiceNow API to create a ticket
        logger.warn(e.getCause());
      }

      // if we were able to create the ServiceNow Ticket
      if (ticket != null) {
        // Check if we need to add an attachment
        // Currently, the UI let you attach only 1 file but the interface accept a list
        // In the future, if we need to change the implementation and manage several
        // attachments, this is the place!
        if (attachementsFiles != null && !attachementsFiles.isEmpty()) {
          File attachment = attachementsFiles.get(0);
          addAttachmentToServiceNowTicket(ticket, attachment);
        }

        // send confirmation Email
        if (logger.isDebugEnabled()) {
          logger.debug("Send Confirmation email");
        }
        // Add ServiceNow Ticket number to the confirmation email
        EmailDefinition emailConfirmation =
          emailApi.prepareConfirmationForHelpdeskContact(
            reason,
            name,
            emailFrom,
            subject,
            content,
            ticket.getServiceNowTicket()
          );
        emailApi.mailPost(emailConfirmation, false);
      } else {
        //if we do not receive an ServiceNow incident ticket, it means that there was an issue with ServiceNow API
        //We will then send a message to the helpdesk instead
        sendEmailToHelpdesk(
          reason,
          name,
          emailFrom,
          subject,
          content,
          attachementsFiles
        );
      }
    } else {
      sendEmailToHelpdesk(
        reason,
        name,
        emailFrom,
        subject,
        content,
        attachementsFiles
      );
    }
  }

  private void sendEmailToHelpdesk(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    List<File> attachementsFiles
  ) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug(
        "Send email to helpdesk support: reason=" +
        reason +
        ", name=" +
        name +
        ", emailFrom=" +
        emailFrom +
        ", subject=" +
        subject +
        ", content=" +
        content
      );
    }
    EmailDefinition emailDefinition = emailApi.prepareEmailForHelpdeskContact(
      reason,
      name,
      emailFrom,
      subject,
      content
    );
    emailDefinition.setCopyToSender(true);
    emailApi.mailPost(emailDefinition, attachementsFiles, false);

    // send confirmation Email
    if (logger.isDebugEnabled()) {
      logger.debug("Send Confirmation email");
    }
    EmailDefinition emailConfirmation =
      emailApi.prepareConfirmationForHelpdeskContact(
        reason,
        name,
        emailFrom,
        subject,
        content,
        null
      );
    emailApi.mailPost(emailConfirmation, false);
  }

  /**
   * Return an HttpClient with needed Proxy parameter and credentials
   */
  private DefaultHttpClient getProxyHttpClient() {
    DefaultHttpClient client = new DefaultHttpClient();

    client
      .getCredentialsProvider()
      .setCredentials(
        new AuthScope(proxyUrl, proxyPort),
        new UsernamePasswordCredentials(proxyUsername, proxyPassword)
      );
    HttpHost proxy = new HttpHost(proxyUrl, proxyPort);
    client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    return client;
  }

  /**
   * Wrapper class used to store ServiceNowTicket and sys_id.
   */
  class Ticket {

    String serviceNowTicket;
    String sys_id;

    Ticket() {}

    Ticket(String serviceNowTicket, String sys_id) {
      this.serviceNowTicket = serviceNowTicket;
      this.sys_id = sys_id;
    }

    public String getServiceNowTicket() {
      return serviceNowTicket;
    }

    public void setServiceNowTicket(String serviceNowTicket) {
      this.serviceNowTicket = serviceNowTicket;
    }

    public String getSys_id() {
      return sys_id;
    }

    public void setSys_id(String sys_id) {
      this.sys_id = sys_id;
    }
  }
}
