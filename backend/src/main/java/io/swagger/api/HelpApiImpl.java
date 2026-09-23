package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.EmailDefinition;
import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * Default implementation of the {@link HelpApi} business interface.
 *
 * <p>This service backs the CIRCABC "Help/FAQ" feature and the "Contact support"
 * workflow. It manages the following domain objects, all stored as Alfresco nodes
 * under the CIRCABC dictionary:
 *
 * <ul>
 *   <li>Help categories (folders carrying the {@code ci:helpCategory} aspect),
 *       located under the {@code faqs} space.</li>
 *   <li>Help articles (content nodes carrying the {@code ci:helpArticle} aspect),
 *       optionally flagged as highlighted via {@code ci:helpArticleHighlighted}.</li>
 *   <li>Help links (content nodes carrying the {@code ci:helpLink} aspect),
 *       located under the {@code faqsLinks} space.</li>
 * </ul>
 *
 * <p>It also implements the {@code contactSupport} flow: when the ServiceNow
 * integration is enabled it opens an incident ticket (with optional attachment)
 * through the ServiceNow REST API, and otherwise (or on failure) falls back to
 * sending an e-mail to the helpdesk. All requests are optionally routed through a
 * configured HTTP proxy.
 *
 * <p>Titles and article content are stored as Alfresco {@link MLText} to support
 * multilingual (i18n) values, and searches are performed using Lucene queries
 * built against the FAQ/links spaces.
 *
 * @author beaurpi, morleal
 */
@SuppressWarnings({ "squid:S6541", "squid:S3776", "squid:S112", "squid:S6201" })
public class HelpApiImpl implements HelpApi {

  private static final String FAQS = "faqs";
  private static final String AUTHORIZATION_HEADER = "Authorization";
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
  @Value("${service.now.enable}")
  private boolean serviceNowEnable;

  @Value("${service.now.prefix}")
  private boolean serviceNowPrefix;

  @Value("${service.now.url}")
  private String serviceNowUrl;

  @Value("${service.now.user}")
  private String serviceNowUser;

  @Value("${service.now.password}")
  private String serviceNowPassword;

  @Value("${mail.environment.name}")
  private String environmentName;

  @Value("${assignment_group}")
  private String assignmentGroup;

  @Value("${business_service}")
  private String businessService;

  @Value("${service_offering}")
  private String serviceOffering;

  // Proxy parameters
  @Value("${proxy.enable}")
  private boolean proxyEnable;

  @Value("${proxy.url}")
  private String proxyUrl;

  @Value("${proxy.port:0}")
  private int proxyPort;

  @Value("${proxy.username}")
  private String proxyUsername;

  @Value("${proxy.password}")
  private String proxyPassword;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PersonService personService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private EmailApi emailApi;

  @Qualifier("ldapOrLuceneUserService")
  @Autowired
  private LdapUserService ldapUserService;

  @Autowired
  private CircabcApi circabcApi;

  private static final Log logger = LogFactory.getLog(HelpApiImpl.class);

  /**
   * Returns all help categories defined under the {@code faqs} space.
   *
   * @return the list of {@link HelpCategory} objects (empty if the {@code faqs}
   *     space does not exist or contains no category nodes)
   */
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

  /**
   * Resolves the {@code faqs} space that holds all help categories and articles.
   *
   * @return the {@link NodeRef} of the {@code faqs} folder, or {@code null} if it
   *     does not exist under the CIRCABC dictionary
   */
  private NodeRef getFaqsRef() {
    NodeRef ddRef = circabcApi.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);
  }

  /**
   * Resolves the {@code faqsLinks} space that holds all help links.
   *
   * @return the {@link NodeRef} of the {@code faqsLinks} folder, or {@code null}
   *     if it does not exist under the CIRCABC dictionary
   */
  private NodeRef getFaqLinksRef() {
    NodeRef ddRef = circabcApi.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(
      ddRef,
      ContentModel.ASSOC_CONTAINS,
      FAQS_LINKS
    );
  }

  /**
   * Creates a new help category folder under the {@code faqs} space.
   *
   * <p>The folder name is derived from the English title when present, otherwise
   * from the first available localized title, and is sanitized to remove illegal
   * file-system characters. The {@code ci:helpCategory} aspect and multilingual
   * title are applied to the new node.
   *
   * @param helpCategory the category to create; its {@code title} drives the node
   *     name and title. If the title is {@code null} no node is created.
   * @return the same {@link HelpCategory} instance, updated with the identifier of
   *     the newly created node (when a node was created)
   */
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

  /**
   * Replaces characters that are illegal in Alfresco/file-system names with
   * underscores.
   *
   * @param filename the raw name to sanitize
   * @return a safe name where {@code \ / : * ? " < > | !} have been replaced by
   *     {@code _}
   */
  private String getCleanFileName(String filename) {
    return filename.replaceAll("[\\\\/:*?\"<>|!]", "_");
  }

  /**
   * Retrieves a single help category by its node identifier, including its
   * multilingual title and the number of articles it contains.
   *
   * @param id the node identifier of the help category
   * @return the corresponding {@link HelpCategory} populated with its title and
   *     article count
   */
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

  /**
   * Returns all help articles belonging to the given category.
   *
   * @param categoryId the node identifier of the help category
   * @param loadContent when {@code true}, the (potentially large) article content
   *     is loaded in addition to the metadata; when {@code false} only metadata is
   *     returned
   * @return the list of {@link HelpArticle} objects found under the category
   */
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

  /**
   * Retrieves a single help article by its node identifier, including its content.
   *
   * @param id the node identifier of the help article
   * @return the corresponding {@link HelpArticle}, or {@code null} if the node does
   *     not carry the help-article aspect
   */
  @Override
  public HelpArticle getHelpArticle(String id) {
    return getHelpArticleInternal(id, true);
  }

  /**
   * Builds a {@link HelpArticle} from an Alfresco node, resolving its title,
   * author (modifier's first/last name), last-update date, highlighted flag and
   * parent category.
   *
   * @param id the node identifier of the help article
   * @param loadContent when {@code true}, the article content (stored as the node
   *     description) is loaded as well
   * @return the populated {@link HelpArticle}, or {@code null} if the node does not
   *     carry the {@code ci:helpArticle} aspect
   */
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

  /**
   * Creates a new help article as a content node under the given category.
   *
   * <p>The node name is derived from the default-language title and sanitized. The
   * {@code ci:helpArticle} aspect, multilingual title and content (stored as the
   * node description) are applied, and the returned article is enriched with its
   * new identifier, author and last-update date.
   *
   * @param categoryId the node identifier of the parent help category
   * @param article the article to create, providing the title and content
   * @return the same {@link HelpArticle} instance updated with its generated id,
   *     author and last-update date
   * @throws InvalidArgumentException if {@code categoryId} does not reference a
   *     node carrying the help-category aspect
   */
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

  /**
   * Deletes the help article identified by the given node id.
   *
   * @param id the node identifier of the help article to delete
   * @throws InvalidArgumentException if the node does not carry the help-article
   *     aspect
   */
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

  /**
   * Updates the title and content of an existing help article.
   *
   * @param id the node identifier of the help article to update
   * @param article the article carrying the new title and content
   * @return the same {@link HelpArticle} instance updated with the current author
   *     and last-update date
   * @throws InvalidArgumentException if the node does not exist or does not carry
   *     the help-article aspect
   */
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

  /**
   * Deletes the help category identified by the given node id.
   *
   * @param id the node identifier of the help category to delete
   * @throws InvalidArgumentException if the node does not carry the help-category
   *     aspect
   */
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

  /**
   * Updates the multilingual title of an existing help category.
   *
   * @param id the node identifier of the help category to update
   * @param category the category carrying the new title; if its title is
   *     {@code null} no change is applied
   * @return the same {@link HelpCategory} instance passed in
   */
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

  /**
   * Toggles the "highlighted" state of a help article by adding or removing the
   * {@code ci:helpArticleHighlighted} aspect.
   *
   * @param id the node identifier of the help article
   * @return the refreshed {@link HelpArticle} reflecting the new highlighted state
   * @throws InvalidArgumentException if the node does not exist or does not carry
   *     the help-article aspect
   */
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

  /**
   * Returns all help articles flagged as highlighted, ordered by modification
   * date, by running a Lucene query over the {@code faqs} space.
   *
   * @return the list of highlighted {@link HelpArticle} objects
   */
  @Override
  public List<HelpArticle> getHighlightedArticles() {
    String query = buildHighlightedQuery();

    List<HelpArticle> result = new ArrayList<>();

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(query);
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    sp.addSort(MODIFIED, true);

    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodeRefs = rs.getNodeRefs();

    for (NodeRef articleRef : nodeRefs) {
      result.add(getHelpArticle(articleRef.getId()));
    }

    return result;
  }

  /**
   * Builds the Lucene query selecting highlighted help articles located under the
   * {@code faqs} space.
   *
   * @return the Lucene query string combining the FAQ path with the highlighted
   *     aspect
   */
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

  /**
   * Returns all help links defined under the {@code faqsLinks} space.
   *
   * @return the list of {@link HelpLink} objects (empty if the {@code faqsLinks}
   *     space does not exist or contains no link nodes)
   */
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

  /**
   * Retrieves a single help link by its node identifier, including its
   * multilingual title and target URL.
   *
   * @param id the node identifier of the help link
   * @return the corresponding {@link HelpLink} populated with its title and href
   */
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

  /**
   * Creates a new help link as a content node under the {@code faqsLinks} space.
   *
   * <p>The node name is derived from the English title when present (otherwise the
   * first available localized title), prefixed with {@code link--} and sanitized.
   * The {@code ci:helpLink} aspect, multilingual title and target href are applied.
   *
   * @param body the link to create, providing the title and target href; if its
   *     title is {@code null} no node is created
   * @return the same {@link HelpLink} instance updated with the identifier of the
   *     newly created node (when a node was created)
   */
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

  /**
   * Updates the title and target href of an existing help link.
   *
   * <p>The update is only applied when both the title and id of {@code body} are
   * set and the referenced node carries the {@code ci:helpLink} aspect.
   *
   * @param body the link carrying the id, new title and new href
   * @return the same {@link HelpLink} instance passed in
   */
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

  /**
   * Deletes the help link identified by the given node id, provided it carries the
   * {@code ci:helpLink} aspect.
   *
   * @param id the node identifier of the help link to delete
   */
  @Override
  public void deleteHelpLink(String id) {
    NodeRef linkRef = Converter.createNodeRefFromId(id);

    if (nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)) {
      nodeService.deleteNode(linkRef);
    }
  }

  /**
   * Performs a full-text search across help content.
   *
   * <p>Two Lucene queries are executed: one over the {@code faqs} space (matching
   * categories and articles on title/description) and one over the
   * {@code faqsLinks} space (matching links on title). Matching nodes are grouped
   * by type into the returned result.
   *
   * @param query the search terms; an empty string or {@code "*"} matches all
   *     entries
   * @return a {@link HelpSearchResult} grouping the matching categories, articles
   *     and links
   */
  @Override
  public HelpSearchResult searchHelp(String query) {
    HelpSearchResult result = new HelpSearchResult();

    String faqsQuery = buildSearchFaqsQuery(query);

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(faqsQuery);
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
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
    spl.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
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

  /**
   * Builds the Lucene query used to search help categories and articles under the
   * {@code faqs} space, matching each search term against the title and
   * description fields.
   *
   * @param query the raw search string, split on whitespace into individual terms
   * @return the Lucene query string
   */
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

  /**
   * Builds the Lucene query used to search help links under the {@code faqsLinks}
   * space, matching each search term against the title field.
   *
   * @param query the raw search string, split on whitespace into individual terms
   * @return the Lucene query string
   */
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

  /**
   * Creates a ServiceNow incident ticket through the ServiceNow REST API.
   *
   * <p>The caller details are filled differently depending on the current user:
   * guests use the {@code userName}/{@code emailFrom} arguments, external users are
   * resolved from LDAP, and internal users are identified by their authenticated
   * username. HTML markup in the content is stripped before being sent. The
   * request is routed through the configured proxy when proxy support is enabled.
   *
   * @param userName the free-text name supplied for a guest caller (may contain a
   *     first and last name separated by a space)
   * @param emailFrom the e-mail address supplied for a guest caller
   * @param serviceOffering the ServiceNow service offering to assign to the ticket
   * @param subject the short description of the incident
   * @param content the incident description (HTML is converted to plain text)
   * @return the created {@link Ticket} (reference number and {@code sys_id}), or
   *     {@code null} if the response could not be parsed
   * @throws Exception if the ServiceNow API call fails or returns a non-201 status
   */
  @SuppressWarnings({ "deprecation", "java:S2647" })
  private Ticket createServiceNowTicket(
    String userName,
    String emailFrom,
    String serviceOffering,
    String subject,
    String content
  ) throws Exception {
    Ticket ticket = null;

    // HttpClient - set proxy parameters if needed for the current environment
    DefaultHttpClient client = proxyEnable
      ? getProxyHttpClient()
      : new DefaultHttpClient();

    HttpPost post = new HttpPost(serviceNowUrl + INCIDENT_SUFFIX);
    post.setHeader("Content-Type", "application/json; charset=utf-8");
    post.setHeader(
      AUTHORIZATION_HEADER,
      "Basic " +
        Base64.getEncoder().encodeToString(
          (serviceNowUser + ":" + serviceNowPassword).getBytes()
        )
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
      String firstName = null;
      String lastName = null;
      if (userName != null && !"".equals(userName)) {
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
    String description =
      content != null ? Converter.convertHtmlToJsonText(content) : "";
    json.append("\"description\": \"" + description + "\"");

    json.append("}");
    json.append("}");

    try {
      //Json query should be in UTF-8 encoding
      post.setEntity(
        new StringEntity(json.toString(), StandardCharsets.UTF_8.name())
      );

      HttpResponse response = client.execute(post);
      String result = EntityUtils.toString(response.getEntity());

      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
        // we need to parse the response to find the reference of the incident ticket and
        // table_sys_id if we need to add attachment(s)
        ticket = parseTicket(result);
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
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw (e);
    } finally {
      client.getConnectionManager().shutdown();
    }

    return ticket;
  }

  /**
   * Parses the JSON response returned by the ServiceNow incident-creation call to
   * extract the ticket reference and its {@code sys_id}.
   *
   * @param result the raw JSON response body
   * @return the parsed {@link Ticket}, or {@code null} if the JSON could not be
   *     parsed
   */
  private Ticket parseTicket(String result) {
    try {
      JSONObject jsonResponse = new JSONObject(result).getJSONObject("result");
      String serviceNowTicket = jsonResponse.getString("displayname");
      String sysId = jsonResponse.getString("sys_id");
      return new Ticket(serviceNowTicket, sysId);
    } catch (JSONException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Uploads a file as an attachment to an existing ServiceNow ticket via the
   * ServiceNow attachment API.
   *
   * <p>The original file name is recovered from the temporary upload file name and
   * URL-encoded. The request is routed through the configured proxy when proxy
   * support is enabled.
   *
   * @param ticket the ticket (providing the target {@code sys_id}) to attach the
   *     file to
   * @param attachment the file to upload
   * @throws Exception if the ServiceNow API call fails or returns a non-201 status
   */
  @SuppressWarnings({ "deprecation", "java:S2647" })
  private void addAttachmentToServiceNowTicket(Ticket ticket, File attachment)
    throws Exception {
    // we need to retrieve the original file name from the temporary file name
    String fileName = Converter.getOriginalFileName(attachment.getName());

    // HttpClient - set proxy parameters if needed for the current environment
    DefaultHttpClient client = proxyEnable
      ? getProxyHttpClient()
      : new DefaultHttpClient();

    try {
      String endpointURL =
        serviceNowUrl +
        ATTACHMENT_SUFFIX +
        ticket.getSysId() +
        "&file_name=" +
        URLEncoder.encode(fileName, "UTF-8");

      HttpPost post = new HttpPost(endpointURL);

      post.setHeader("Content-Type", "application/octetd-stream");
      post.setHeader(
        AUTHORIZATION_HEADER,
        "Basic " +
          Base64.getEncoder().encodeToString(
            (serviceNowUser + ":" + serviceNowPassword).getBytes()
          )
      );

      // ADD attachment
      FileEntity entity = new FileEntity(
        attachment,
        "application/octect-stream"
      );
      post.setEntity(entity);

      try {
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
          // we did not receive HTTP 201 response code
          throw new HttpException(response.getStatusLine().toString());
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw (e);
      }
    } finally {
      client.close();
    }
  }

  /**
   * Handles a "contact support" request from the help/FAQ feature.
   *
   * <p>When the ServiceNow integration is enabled and configured, this derives the
   * subject and service offering from the selected reason (unless the reason is
   * "other"), optionally prefixes the subject with the environment name, and opens
   * a ServiceNow incident ticket. The first supplied attachment (if any) is added
   * to the ticket and a confirmation e-mail carrying the ticket number is sent. If
   * the ServiceNow integration is disabled or ticket creation fails, the request
   * is forwarded to the helpdesk by e-mail instead.
   *
   * @param reason the support reason key (e.g. {@code help.reason.other})
   * @param name the free-text name of the requester
   * @param emailFrom the requester's e-mail address
   * @param subject the subject of the request (defaulted from the reason when
   *     empty in the e-mail fallback)
   * @param content the body of the request
   * @param attachementsFiles the list of attached files; currently only the first
   *     one is forwarded to ServiceNow
   * @throws IOException if sending the request fails
   */
  @Override
  public void contactSupport(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    List<File> attachementsFiles
  ) throws IOException {
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

      // prefix the subject with the environment name
      if (
        serviceNowPrefix &&
        !"".equals(environmentName) &&
        environmentName != null
      ) {
        subject = "[" + environmentName + "] - " + subject;
      }

      Ticket ticket = null;

      // invoking ServiceNow API to create ServiceNow ticket
      try {
        ticket = createServiceNowTicket(
          name,
          emailFrom,
          serviceOffering,
          subject,
          content
        );
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
          try {
            addAttachmentToServiceNowTicket(ticket, attachment);
          } catch (Exception e) {
            logger.warn("Failed to attach file to ServiceNow ticket", e);
          }
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
        //if we do not receive a ServiceNow incident ticket, it means that there was an issue with ServiceNow API
        //We will then send a message to the helpdesk instead
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

  /**
   * Sends the support request to the helpdesk by e-mail and sends a confirmation
   * e-mail to the requester. Used as the fallback when ServiceNow is unavailable.
   *
   * @param reason the support reason key
   * @param name the free-text name of the requester
   * @param emailFrom the requester's e-mail address
   * @param subject the subject of the request
   * @param content the body of the request
   * @param attachementsFiles the files to attach to the helpdesk e-mail
   */
  private void sendEmailToHelpdesk(
    String reason,
    String name,
    String emailFrom,
    String subject,
    String content,
    List<File> attachementsFiles
  ) {
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
   * Builds an {@link DefaultHttpClient} pre-configured with the proxy host, port
   * and credentials taken from the application configuration.
   *
   * @return a new HTTP client routing its requests through the configured proxy
   */
  @SuppressWarnings("deprecation")
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

    /** Human-readable ServiceNow ticket reference (display name). */
    String serviceNowTicket;
    /** Internal ServiceNow record identifier, used to attach files. */
    String sysId;

    /** Creates an empty ticket. */
    Ticket() {}

    /**
     * Creates a ticket with the given reference and internal identifier.
     *
     * @param serviceNowTicket the human-readable ServiceNow ticket reference
     * @param sysId the internal ServiceNow record identifier
     */
    Ticket(String serviceNowTicket, String sysId) {
      this.serviceNowTicket = serviceNowTicket;
      this.sysId = sysId;
    }

    /**
     * @return the human-readable ServiceNow ticket reference
     */
    public String getServiceNowTicket() {
      return serviceNowTicket;
    }

    /**
     * @param serviceNowTicket the human-readable ServiceNow ticket reference to set
     */
    public void setServiceNowTicket(String serviceNowTicket) {
      this.serviceNowTicket = serviceNowTicket;
    }

    /**
     * @return the internal ServiceNow record identifier
     */
    public String getSysId() {
      return sysId;
    }

    /**
     * @param sysId the internal ServiceNow record identifier to set
     */
    public void setSysId(String sysId) {
      this.sysId = sysId;
    }
  }
}
