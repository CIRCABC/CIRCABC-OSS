package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.exception.ValidationException;
import io.swagger.model.EmailDefinition;
import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import io.swagger.model.HelpSubcategory;
import io.swagger.model.I18nProperty;
import io.swagger.model.ImportResult;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
  private String assignmentGroup;
  private String businessService;
  private String serviceOffering;

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
  private io.swagger.service.FaqExportService faqExportService;
  private io.swagger.service.FaqImportService faqImportService;

  private static final Log logger = LogFactory.getLog(HelpApiImpl.class);

  @Override
  public List<HelpCategory> getHelpCategories() {
    NodeRef faqsRef = getFaqsRef();

    List<HelpCategory> result = new ArrayList<>();
    final Map<String, Date> createdDateMap = new HashMap<>();

    if (faqsRef != null) {
      for (ChildAssociationRef child : nodeService.getChildAssocs(faqsRef)) {
        if (
          nodeService.hasAspect(
            child.getChildRef(),
            CircabcModel.ASPECT_HELP_CATEGORY
          )
        ) {
          result.add(getHelpCategory(child.getChildRef().getId()));

          final Serializable createdProp = nodeService.getProperty(
            child.getChildRef(),
            ContentModel.PROP_CREATED
          );
          if (createdProp instanceof Date) {
            createdDateMap.put(child.getChildRef().getId(), (Date) createdProp);
          }
        }
      }
    }

    result.sort(
      Comparator.comparingInt(HelpCategory::getSortOrder).thenComparing(c ->
        createdDateMap.getOrDefault(c.getId(), new Date(0))
      )
    );

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

  public void setFaqExportService(
    io.swagger.service.FaqExportService faqExportService
  ) {
    this.faqExportService = faqExportService;
  }

  public void setFaqImportService(
    io.swagger.service.FaqImportService faqImportService
  ) {
    this.faqImportService = faqImportService;
  }

  //ServiceNow parameters
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  public void setServiceNowEnable(Boolean serviceNowEnable) {
    this.serviceNowEnable = (serviceNowEnable != null && serviceNowEnable);
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

  public void setAssignmentGroup(String assignmentGroup) {
    this.assignmentGroup = assignmentGroup;
  }

  public void setBusinessService(String businessService) {
    this.businessService = businessService;
  }

  public void setServiceOffering(String serviceOffering) {
    this.serviceOffering = serviceOffering;
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

      if (name == null || name.trim().isEmpty()) {
        throw new InvalidArgumentException(
          "Help category title cannot be empty"
        );
      }

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

    if (!nodeService.exists(helpCategoryRef)) {
      throw new IllegalArgumentException(
        "Help category not found with id: " + id
      );
    }

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new IllegalArgumentException("Node is not a help category: " + id);
    }

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

    final Serializable sortOrderProp = nodeService.getProperty(
      helpCategoryRef,
      CircabcModel.PROP_HELP_CATEGORY_SORT_ORDER
    );
    category.setSortOrder(
      sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
    );

    int nbArticles = 0;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      final NodeRef childRef = child.getChildRef();
      if (nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
        // Legacy article directly under the category
        nbArticles++;
      } else if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_SUBCATEGORY)
      ) {
        // Count articles inside each subcategory
        for (ChildAssociationRef subChild : nodeService.getChildAssocs(
          childRef
        )) {
          if (
            nodeService.hasAspect(
              subChild.getChildRef(),
              CircabcModel.ASPECT_HELP_ARTICLE
            )
          ) {
            nbArticles++;
          }
        }
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

    if (!nodeService.exists(helpCategoryRef)) {
      throw new IllegalArgumentException(
        "Help category not found with id: " + categoryId
      );
    }

    List<HelpArticle> result = new ArrayList<>();
    Map<String, Date> createdDateMap = new HashMap<>();

    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        final String childId = child.getChildRef().getId();
        result.add(getHelpArticleInternal(childId, loadContent));

        final Serializable createdProp = nodeService.getProperty(
          child.getChildRef(),
          ContentModel.PROP_CREATED
        );
        if (createdProp instanceof Date) {
          createdDateMap.put(childId, (Date) createdProp);
        }
      }
    }

    result.sort(
      Comparator.comparingInt(HelpArticle::getSortOrder).thenComparing(a ->
        createdDateMap.getOrDefault(a.getId(), new Date(0))
      )
    );

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

      final Serializable sortOrderProp = nodeService.getProperty(
        helpArticleRef,
        CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER
      );
      result.setSortOrder(
        sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
      );

      return result;
    }

    return null;
  }

  @Override
  public List<HelpArticle> getSubcategoryArticles(
    String subcategoryId,
    Boolean loadContent
  ) {
    NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(subcategoryId);

    List<HelpArticle> result = new ArrayList<>();
    Map<String, Date> createdDateMap = new HashMap<>();

    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpSubcategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        final String childId = child.getChildRef().getId();
        result.add(getHelpArticleInternal(childId, loadContent));

        final Serializable createdProp = nodeService.getProperty(
          child.getChildRef(),
          ContentModel.PROP_CREATED
        );
        if (createdProp instanceof Date) {
          createdDateMap.put(childId, (Date) createdProp);
        }
      }
    }

    result.sort(
      Comparator.comparingInt(HelpArticle::getSortOrder).thenComparing(a ->
        createdDateMap.getOrDefault(a.getId(), new Date(0))
      )
    );

    return result;
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

    if (name == null || name.trim().isEmpty()) {
      throw new InvalidArgumentException("Help article title cannot be empty");
    }

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

    int nextSortOrder = 0;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        child.getChildRef().equals(articleRef.getChildRef()) ||
        !nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        continue;
      }
      final Serializable existingSortOrder = nodeService.getProperty(
        child.getChildRef(),
        CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER
      );
      final int value = existingSortOrder instanceof Integer
        ? (Integer) existingSortOrder
        : 0;
      if (value >= nextSortOrder) {
        nextSortOrder = value + 1;
      }
    }

    nodeService.setProperty(
      articleRef.getChildRef(),
      CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER,
      nextSortOrder
    );
    article.setSortOrder(nextSortOrder);

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

  @Override
  public HelpArticle createSubcategoryArticle(
    String subcategoryId,
    HelpArticle article
  ) {
    final NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(
      subcategoryId
    );

    if (
      !nodeService.hasAspect(
        helpSubcategoryRef,
        CircabcModel.ASPECT_HELP_SUBCATEGORY
      )
    ) {
      throw new InvalidArgumentException(
        "The target node is not a help subcategory"
      );
    }

    MLText title = Converter.toMLText(article.getTitle());
    String name = title.getDefaultValue();

    name = getCleanFileName(name);

    if (name == null || name.trim().isEmpty()) {
      throw new InvalidArgumentException("Help article title cannot be empty");
    }

    ChildAssociationRef articleRef = nodeService.createNode(
      helpSubcategoryRef,
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

    int nextSortOrder = 0;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpSubcategoryRef
    )) {
      if (
        child.getChildRef().equals(articleRef.getChildRef()) ||
        !nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        continue;
      }
      final Serializable existingSortOrder = nodeService.getProperty(
        child.getChildRef(),
        CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER
      );
      final int value = existingSortOrder instanceof Integer
        ? (Integer) existingSortOrder
        : 0;
      if (value >= nextSortOrder) {
        nextSortOrder = value + 1;
      }
    }

    nodeService.setProperty(
      articleRef.getChildRef(),
      CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER,
      nextSortOrder
    );
    article.setSortOrder(nextSortOrder);

    article.setId(articleRef.getChildRef().getId());
    article.setParentId(subcategoryId);

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

  @Override
  public void reorderSubcategoryArticles(
    String subcategoryId,
    List<String> articleIds
  ) {
    final NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(
      subcategoryId
    );

    if (
      !nodeService.hasAspect(
        helpSubcategoryRef,
        CircabcModel.ASPECT_HELP_SUBCATEGORY
      )
    ) {
      throw new InvalidArgumentException("Subcategory not found");
    }

    final Map<String, NodeRef> subcategoryArticleRefs = new HashMap<>();
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpSubcategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        subcategoryArticleRefs.put(
          child.getChildRef().getId(),
          child.getChildRef()
        );
      }
    }

    if (articleIds.size() != subcategoryArticleRefs.size()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Reorder attempt with invalid list for subcategory '" +
          subcategoryId +
          "'. Expected " +
          subcategoryArticleRefs.size() +
          " articles, received " +
          articleIds.size()
        );
      }
      throw new InvalidArgumentException(
        "The list must contain all articles of the subcategory. Expected " +
        subcategoryArticleRefs.size() +
        ", received " +
        articleIds.size() +
        "."
      );
    }

    for (String articleId : articleIds) {
      if (!subcategoryArticleRefs.containsKey(articleId)) {
        if (logger.isWarnEnabled()) {
          logger.warn(
            "Reorder attempt with invalid article ID '" +
            articleId +
            "' for subcategory '" +
            subcategoryId +
            "'"
          );
        }
        throw new InvalidArgumentException(
          "Article with id '" +
          articleId +
          "' does not belong to subcategory '" +
          subcategoryId +
          "'"
        );
      }
    }

    for (int i = 0; i < articleIds.size(); i++) {
      final String articleId = articleIds.get(i);
      final NodeRef articleRef = subcategoryArticleRefs.get(articleId);
      try {
        nodeService.setProperty(
          articleRef,
          CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER,
          i
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Failed to persist sortOrder for article '" +
            articleId +
            "' in subcategory '" +
            subcategoryId +
            "'",
            e
          );
        }
        throw e;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "Successfully reordered " +
        articleIds.size() +
        " articles in subcategory '" +
        subcategoryId +
        "'"
      );
    }
  }

  @Override
  public void reorderCategoryArticles(
    String categoryId,
    List<String> articleIds
  ) {
    final NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new InvalidArgumentException("Category not found");
    }

    final Map<String, NodeRef> categoryArticleRefs = new HashMap<>();
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        categoryArticleRefs.put(
          child.getChildRef().getId(),
          child.getChildRef()
        );
      }
    }

    if (articleIds.size() != categoryArticleRefs.size()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Reorder attempt with invalid list for category '" +
          categoryId +
          "'. Expected " +
          categoryArticleRefs.size() +
          " articles, received " +
          articleIds.size()
        );
      }
      throw new InvalidArgumentException(
        "The list must contain all articles of the category. Expected " +
        categoryArticleRefs.size() +
        ", received " +
        articleIds.size() +
        "."
      );
    }

    for (String articleId : articleIds) {
      if (!categoryArticleRefs.containsKey(articleId)) {
        if (logger.isWarnEnabled()) {
          logger.warn(
            "Reorder attempt with invalid article ID '" +
            articleId +
            "' for category '" +
            categoryId +
            "'"
          );
        }
        throw new InvalidArgumentException(
          "Article with id '" +
          articleId +
          "' does not belong to category '" +
          categoryId +
          "'"
        );
      }
    }

    for (int i = 0; i < articleIds.size(); i++) {
      final String articleId = articleIds.get(i);
      final NodeRef articleRef = categoryArticleRefs.get(articleId);
      try {
        nodeService.setProperty(
          articleRef,
          CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER,
          i
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Failed to persist sortOrder for article '" +
            articleId +
            "' in category '" +
            categoryId +
            "'",
            e
          );
        }
        throw e;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "Successfully reordered " +
        articleIds.size() +
        " articles in category '" +
        categoryId +
        "'"
      );
    }
  }

  @Override
  public void reorderCategories(List<String> categoryIds) {
    final NodeRef faqsRef = getFaqsRef();

    if (faqsRef == null) {
      throw new InvalidArgumentException("FAQs root not found");
    }

    final Map<String, NodeRef> categoryRefs = new HashMap<>();
    for (ChildAssociationRef child : nodeService.getChildAssocs(faqsRef)) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_CATEGORY
        )
      ) {
        categoryRefs.put(child.getChildRef().getId(), child.getChildRef());
      }
    }

    if (categoryIds.size() != categoryRefs.size()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Reorder attempt with invalid list for categories. Expected " +
          categoryRefs.size() +
          " categories, received " +
          categoryIds.size()
        );
      }
      throw new InvalidArgumentException(
        "The list must contain all categories. Expected " +
        categoryRefs.size() +
        ", received " +
        categoryIds.size() +
        "."
      );
    }

    for (String categoryId : categoryIds) {
      if (!categoryRefs.containsKey(categoryId)) {
        if (logger.isWarnEnabled()) {
          logger.warn(
            "Reorder attempt with invalid category ID '" + categoryId + "'"
          );
        }
        throw new InvalidArgumentException(
          "Category with id '" + categoryId + "' does not exist"
        );
      }
    }

    for (int i = 0; i < categoryIds.size(); i++) {
      final String categoryId = categoryIds.get(i);
      final NodeRef categoryRef = categoryRefs.get(categoryId);
      try {
        nodeService.setProperty(
          categoryRef,
          CircabcModel.PROP_HELP_CATEGORY_SORT_ORDER,
          i
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Failed to persist sortOrder for category '" + categoryId + "'",
            e
          );
        }
        throw e;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "Successfully reordered " + categoryIds.size() + " categories"
      );
    }
  }

  @Override
  public List<HelpSubcategory> getCategorySubcategories(String categoryId) {
    final NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    final List<HelpSubcategory> result = new ArrayList<>();
    final Map<String, Date> createdDateMap = new HashMap<>();

    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      final NodeRef childRef = child.getChildRef();

      if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_SUBCATEGORY)
      ) {
        // Skip if the child has the same ID as the parent category (data corruption)
        if (childRef.getId().equals(categoryId)) {
          if (logger.isWarnEnabled()) {
            logger.warn(
              "Category '" +
              categoryId +
              "' has itself as a child with ASPECT_HELP_SUBCATEGORY. Skipping to prevent circular reference."
            );
          }
          continue;
        }

        final HelpSubcategory subcategory = new HelpSubcategory();
        subcategory.setId(childRef.getId());
        subcategory.setParentId(categoryId);

        final Serializable title = nodeService.getProperty(
          childRef,
          ContentModel.PROP_TITLE
        );
        if (title instanceof String) {
          subcategory.setTitle(Converter.toI18NProperty((String) title));
        } else if (title instanceof MLText) {
          subcategory.setTitle(Converter.toI18NProperty((MLText) title));
        }

        final Serializable sortOrderProp = nodeService.getProperty(
          childRef,
          CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER
        );
        subcategory.setSortOrder(
          sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
        );

        int articleCount = 0;
        Date latestArticleUpdate = null;
        for (ChildAssociationRef subcatChild : nodeService.getChildAssocs(
          childRef
        )) {
          if (
            nodeService.hasAspect(
              subcatChild.getChildRef(),
              CircabcModel.ASPECT_HELP_ARTICLE
            )
          ) {
            articleCount++;

            final Serializable modifiedProp = nodeService.getProperty(
              subcatChild.getChildRef(),
              ContentModel.PROP_MODIFIED
            );
            if (modifiedProp instanceof Date) {
              Date articleModified = (Date) modifiedProp;
              if (
                latestArticleUpdate == null ||
                articleModified.after(latestArticleUpdate)
              ) {
                latestArticleUpdate = articleModified;
              }
            }
          }
        }
        subcategory.setNumberOfArticles(articleCount);

        if (latestArticleUpdate != null) {
          subcategory.setLastUpdate(
            new DateTime(latestArticleUpdate).toString()
          );
        }

        result.add(subcategory);

        final Serializable createdProp = nodeService.getProperty(
          childRef,
          ContentModel.PROP_CREATED
        );
        if (createdProp instanceof Date) {
          createdDateMap.put(childRef.getId(), (Date) createdProp);
        }
      }
    }

    result.sort(
      Comparator.comparingInt(HelpSubcategory::getSortOrder).thenComparing(s ->
        createdDateMap.getOrDefault(s.getId(), new Date(0))
      )
    );

    return result;
  }

  @Override
  public HelpSubcategory createHelpSubcategory(
    String categoryId,
    HelpSubcategory subcategory
  ) {
    final NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new InvalidArgumentException(
        "The target node is not a help category"
      );
    }

    final MLText title = Converter.toMLText(subcategory.getTitle());
    String name = title.getDefaultValue();

    if (name == null || name.isEmpty()) {
      if (!subcategory.getTitle().keySet().isEmpty()) {
        final String key = (String) subcategory
          .getTitle()
          .keySet()
          .toArray()[0];
        name = subcategory.getTitle().get(key);
      }
    }

    name = getCleanFileName(name);

    if (name == null || name.trim().isEmpty()) {
      throw new InvalidArgumentException(
        "Help subcategory title cannot be empty"
      );
    }

    final ChildAssociationRef subcatRef = nodeService.createNode(
      helpCategoryRef,
      ContentModel.ASSOC_CONTAINS,
      QName.createQName(NamespaceService.ALFRESCO_URI, name),
      ContentModel.TYPE_FOLDER
    );

    nodeService.addAspect(
      subcatRef.getChildRef(),
      CircabcModel.ASPECT_HELP_SUBCATEGORY,
      null
    );

    nodeService.setProperty(
      subcatRef.getChildRef(),
      ContentModel.PROP_NAME,
      name
    );
    nodeService.setProperty(
      subcatRef.getChildRef(),
      ContentModel.PROP_TITLE,
      Converter.toMLText(subcategory.getTitle())
    );

    int nextSortOrder = 0;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        child.getChildRef().equals(subcatRef.getChildRef()) ||
        !nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_SUBCATEGORY
        )
      ) {
        continue;
      }
      final Serializable existingSortOrder = nodeService.getProperty(
        child.getChildRef(),
        CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER
      );
      final int value = existingSortOrder instanceof Integer
        ? (Integer) existingSortOrder
        : 0;
      if (value >= nextSortOrder) {
        nextSortOrder = value + 1;
      }
    }

    nodeService.setProperty(
      subcatRef.getChildRef(),
      CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER,
      nextSortOrder
    );

    subcategory.setId(subcatRef.getChildRef().getId());
    subcategory.setSortOrder(nextSortOrder);
    subcategory.setParentId(categoryId);
    subcategory.setNumberOfArticles(0);

    if (logger.isInfoEnabled()) {
      logger.info(
        "Created help subcategory '" +
        name +
        "' with sortOrder " +
        nextSortOrder +
        " in category '" +
        categoryId +
        "'"
      );
    }

    return subcategory;
  }

  @Override
  public HelpSubcategory getHelpSubcategory(String id) {
    final NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(id);

    final HelpSubcategory subcategory = new HelpSubcategory();
    subcategory.setId(helpSubcategoryRef.getId());

    final Serializable title = nodeService.getProperty(
      helpSubcategoryRef,
      ContentModel.PROP_TITLE
    );
    if (title instanceof String) {
      subcategory.setTitle(Converter.toI18NProperty((String) title));
    } else if (title instanceof MLText) {
      subcategory.setTitle(Converter.toI18NProperty((MLText) title));
    }

    final Serializable sortOrderProp = nodeService.getProperty(
      helpSubcategoryRef,
      CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER
    );
    subcategory.setSortOrder(
      sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
    );

    final ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(
      helpSubcategoryRef
    );
    if (parentAssoc != null) {
      subcategory.setParentId(parentAssoc.getParentRef().getId());
    }

    int articleCount = 0;
    Date latestArticleUpdate = null;
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpSubcategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        articleCount++;

        final Serializable modifiedProp = nodeService.getProperty(
          child.getChildRef(),
          ContentModel.PROP_MODIFIED
        );
        if (modifiedProp instanceof Date) {
          Date articleModified = (Date) modifiedProp;
          if (
            latestArticleUpdate == null ||
            articleModified.after(latestArticleUpdate)
          ) {
            latestArticleUpdate = articleModified;
          }
        }
      }
    }
    subcategory.setNumberOfArticles(articleCount);

    if (latestArticleUpdate != null) {
      subcategory.setLastUpdate(new DateTime(latestArticleUpdate).toString());
    }

    return subcategory;
  }

  @Override
  public HelpSubcategory updateHelpSubcategory(
    String id,
    HelpSubcategory subcategory
  ) {
    final NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(id);

    if (subcategory.getTitle() != null) {
      nodeService.setProperty(
        helpSubcategoryRef,
        ContentModel.PROP_TITLE,
        Converter.toMLText(subcategory.getTitle())
      );
    }

    return getHelpSubcategory(id);
  }

  @Override
  public void deleteHelpSubcategory(String id) {
    final NodeRef helpSubcategoryRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.hasAspect(
        helpSubcategoryRef,
        CircabcModel.ASPECT_HELP_SUBCATEGORY
      )
    ) {
      throw new InvalidArgumentException(
        "The target node is not a help subcategory"
      );
    }

    // Delete all articles within the subcategory (cascade delete)
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpSubcategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_ARTICLE
        )
      ) {
        nodeService.deleteNode(child.getChildRef());
      }
    }

    // Delete the subcategory itself
    nodeService.deleteNode(helpSubcategoryRef);
  }

  @Override
  public void reorderCategorySubcategories(
    String categoryId,
    List<String> subcategoryIds
  ) {
    final NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

    if (
      !nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)
    ) {
      throw new InvalidArgumentException("Category not found");
    }

    final Map<String, NodeRef> categorySubcategoryRefs = new HashMap<>();
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      if (
        nodeService.hasAspect(
          child.getChildRef(),
          CircabcModel.ASPECT_HELP_SUBCATEGORY
        )
      ) {
        final String subcatId = child.getChildRef().getId();
        categorySubcategoryRefs.put(subcatId, child.getChildRef());

        if (logger.isDebugEnabled()) {
          final Serializable title = nodeService.getProperty(
            child.getChildRef(),
            ContentModel.PROP_TITLE
          );
          logger.debug(
            "Found subcategory in category '" +
            categoryId +
            "': ID=" +
            subcatId +
            ", Title=" +
            title
          );
        }
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
        "Category '" +
        categoryId +
        "' has " +
        categorySubcategoryRefs.size() +
        " subcategories: " +
        categorySubcategoryRefs.keySet()
      );
    }

    if (subcategoryIds.size() != categorySubcategoryRefs.size()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Reorder attempt with invalid list for category '" +
          categoryId +
          "'. Expected " +
          categorySubcategoryRefs.size() +
          " subcategories, received " +
          subcategoryIds.size() +
          ". Expected IDs: " +
          categorySubcategoryRefs.keySet() +
          ", Received IDs: " +
          subcategoryIds
        );
      }
      throw new InvalidArgumentException(
        "The list must contain all subcategories of the category. Expected " +
        categorySubcategoryRefs.size() +
        ", received " +
        subcategoryIds.size() +
        "."
      );
    }

    // Check for duplicates in the received list
    final Set<String> uniqueSubcategoryIds = new HashSet<>(subcategoryIds);
    if (uniqueSubcategoryIds.size() != subcategoryIds.size()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Reorder attempt with duplicate IDs for category '" +
          categoryId +
          "'. Received " +
          subcategoryIds.size() +
          " IDs but only " +
          uniqueSubcategoryIds.size() +
          " unique. IDs: " +
          subcategoryIds
        );
      }
      throw new InvalidArgumentException(
        "The list contains duplicate subcategory IDs. Received " +
        subcategoryIds.size() +
        " IDs but only " +
        uniqueSubcategoryIds.size() +
        " are unique."
      );
    }

    for (String subcategoryId : subcategoryIds) {
      if (!categorySubcategoryRefs.containsKey(subcategoryId)) {
        if (logger.isWarnEnabled()) {
          logger.warn(
            "Reorder attempt with invalid subcategory ID '" +
            subcategoryId +
            "' for category '" +
            categoryId +
            "'"
          );
        }
        throw new InvalidArgumentException(
          "Subcategory with id '" +
          subcategoryId +
          "' does not belong to category '" +
          categoryId +
          "'"
        );
      }
    }

    for (int i = 0; i < subcategoryIds.size(); i++) {
      final String subcategoryId = subcategoryIds.get(i);
      final NodeRef subcategoryRef = categorySubcategoryRefs.get(subcategoryId);
      try {
        nodeService.setProperty(
          subcategoryRef,
          CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER,
          i
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Failed to persist sortOrder for subcategory '" +
            subcategoryId +
            "' in category '" +
            categoryId +
            "'",
            e
          );
        }
        throw e;
      }
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "Successfully reordered " +
        subcategoryIds.size() +
        " subcategories in category '" +
        categoryId +
        "'"
      );
    }
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

    // Delete all subcategories and their articles (cascade delete)
    for (ChildAssociationRef child : nodeService.getChildAssocs(
      helpCategoryRef
    )) {
      NodeRef childRef = child.getChildRef();
      if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_SUBCATEGORY)
      ) {
        // Delete all articles within the subcategory
        for (ChildAssociationRef article : nodeService.getChildAssocs(
          childRef
        )) {
          if (
            nodeService.hasAspect(
              article.getChildRef(),
              CircabcModel.ASPECT_HELP_ARTICLE
            )
          ) {
            nodeService.deleteNode(article.getChildRef());
          }
        }
        // Delete the subcategory
        nodeService.deleteNode(childRef);
      } else if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_ARTICLE)
      ) {
        // Delete articles directly under the category
        nodeService.deleteNode(childRef);
      }
    }

    // Finally, delete the category itself
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

      String cleanName = getCleanFileName(name);

      if (cleanName == null || cleanName.trim().isEmpty()) {
        throw new InvalidArgumentException("Help link title cannot be empty");
      }

      name = "link--" + cleanName;

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
    String serviceOffering,
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
        ", serviceOffering: " +
        serviceOffering +
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
    json.append("\"assignment_group\":\"" + assignmentGroup + "\",");
    json.append("\"business_service\":\"" + businessService + "\",");
    json.append("\"service_offering\":\"" + serviceOffering + "\",");
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
    json.append("\"description\": \"" + description + "\"");

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
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append(
          "Error when invoking ServiceNow API to create a ticket. "
        );
        errorMsg.append("Response Status: ");
        errorMsg.append(response.getStatusLine().toString());
        errorMsg.append(". ServiceNow response: ");
        errorMsg.append(result);
        throw new HttpException(errorMsg.toString());
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
        "About to exectue POST method to Rest endpoint URL: " +
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

    post.setHeader("Content-Type", "application/octet-stream");
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

    // Is the integration with ServiceNow API enabled and URL configured?
    if (serviceNowEnable && serviceNowUrl != null && !serviceNowUrl.isEmpty()) {
      // If reason is not OTHER, we will:
      //    1. change the value of serviceOffering which is "CIRCABC - General request" by default
      //    2. give a value related to reason field for the subject of the email because it is empty
      if (!OTHER.equals(reason)) {
        if (UPLOAD_DOWNLOAD.equals(reason)) {
          subject = "Upload or Download";
          serviceOffering = "CIRCABC - Upload or download";
        } else if (ACCESS_PERMISSION.equals(reason)) {
          subject = "Access Permission";
          serviceOffering = "CIRCABC - Access management";
        } else if (ADMINISTER_GROUP.equals(reason)) {
          subject = "Interest Group Administration";
          serviceOffering = "CIRCABC - Account management";
        }
      }

      // Prefix the subject with the environment name
      if (
        serviceNowPrefix &&
        environmentName != null &&
        !environmentName.isEmpty()
      ) {
        subject = "[" + environmentName + "] - " + subject;
      }

      Ticket ticket = null;

      // Invoking ServiceNow API to create ServiceNow ticket
      try {
        ticket = createServiceNowTicket(
          name,
          emailFrom,
          serviceOffering,
          subject,
          content
        );
      } catch (Exception e) {
        // Something wrong happened when invoking ServiceNow API to create a ticket
        logger.warn(e.getCause());
      }

      // If we were able to create the ServiceNow Ticket
      if (ticket != null) {
        // Attachments
        if (attachementsFiles != null && !attachementsFiles.isEmpty()) {
          File attachment = attachementsFiles.get(0);
          addAttachmentToServiceNowTicket(ticket, attachment);
        }

        // Send confirmation email
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
            ticket.getServiceNowTicket()
          );
        emailApi.mailPost(emailConfirmation, false);
      } else {
        // If ServiceNow failed, fallback to helpdesk email
        sendEmailToHelpdesk(
          reason,
          name,
          emailFrom,
          (subject == null || subject.isEmpty()) ? reason : subject,
          content,
          attachementsFiles
        );
      }
    } else {
      // ServiceNow disabled or not configured
      sendEmailToHelpdesk(
        reason,
        name,
        emailFrom,
        (subject == null || subject.isEmpty()) ? reason : subject,
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

  @Override
  public String exportFaq() throws Exception {
    if (logger.isInfoEnabled()) {
      final String userName = authenticationService.getCurrentUserName();
      logger.info("FAQ export requested by user: " + userName);
    }

    try {
      return faqExportService.exportFaqStructure();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        final String userName = authenticationService.getCurrentUserName();
        logger.error("FAQ export failed for user: " + userName, e);
      }
      throw e;
    }
  }

  @Override
  public ImportResult importFaq(
    final InputStream inputStream,
    final String fileName
  ) throws Exception {
    final String userName = authenticationService.getCurrentUserName();

    if (logger.isInfoEnabled()) {
      logger.info(
        String.format(
          "FAQ import requested by user: %s, file: %s",
          userName,
          fileName
        )
      );
    }

    try {
      // Read file content from InputStream
      final byte[] fileBytes = org.apache.commons.io.IOUtils.toByteArray(
        inputStream
      );

      // Validate file size (max 10MB)
      final long maxFileSize = 10 * 1024 * 1024; // 10MB in bytes
      if (fileBytes.length > maxFileSize) {
        if (logger.isWarnEnabled()) {
          logger.warn(
            String.format(
              "FAQ import rejected - file too large: %d bytes (max: %d bytes), user: %s",
              fileBytes.length,
              maxFileSize,
              userName
            )
          );
        }
        throw new IllegalArgumentException(
          "File size exceeds maximum allowed size of 10MB"
        );
      }

      // Validate file is not empty
      if (fileBytes.length == 0) {
        if (logger.isWarnEnabled()) {
          logger.warn("FAQ import rejected - empty file, user: " + userName);
        }
        throw new IllegalArgumentException("File is empty");
      }

      // Read file content as string
      final String jsonContent = new String(fileBytes, StandardCharsets.UTF_8);

      // Call import service
      final ImportResult result = faqImportService.importFaqStructure(
        jsonContent
      );

      if (logger.isInfoEnabled()) {
        logger.info(
          String.format(
            "FAQ import completed successfully for user: %s, sectioen: %d, subsections: %d, articles: %d",
            userName,
            result.getCategoriesProcessed(),
            result.getSubcategoriesProcessed(),
            result.getArticlesProcessed()
          )
        );
      }

      return result;
    } catch (ValidationException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          String.format(
            "FAQ import validation failed for user: %s, file: %s, errors: %s",
            userName,
            fileName,
            e.getValidationErrors()
          ),
          e
        );
      }
      throw e;
    } catch (IllegalArgumentException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          String.format(
            "FAQ import failed - invalid argument for user: %s, file: %s, message: %s",
            userName,
            fileName,
            e.getMessage()
          ),
          e
        );
      }
      throw e;
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          String.format(
            "FAQ import failed - IO error for user: %s, file: %s",
            userName,
            fileName
          ),
          e
        );
      }
      throw new Exception("Failed to read file content", e);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          String.format(
            "FAQ import failed for user: %s, file: %s",
            userName,
            fileName
          ),
          e
        );
      }
      throw e;
    }
  }
}
