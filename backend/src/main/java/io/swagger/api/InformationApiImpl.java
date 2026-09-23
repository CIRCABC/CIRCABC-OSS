package io.swagger.api;

import io.swagger.model.InformationPage;
import io.swagger.model.News;
import io.swagger.model.PagedNews;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Default implementation of {@link InformationApi}, providing the business logic for the
 * "Information" service of a CIRCABC Interest Group (IG).
 *
 * <p>This class operates on the Alfresco repository through the injected services. For a given IG
 * node it resolves the child {@code Information} space and exposes operations to:
 *
 * <ul>
 *   <li>read the Information service configuration (index page URL, adapt flag, old-information
 *       visibility and permissions);
 *   <li>list, create, read, update and delete the "News" cards stored under that space.
 * </ul>
 *
 * <p>Node identifiers received as {@code String} are resolved to Alfresco {@link NodeRef}s via
 * {@link Converter#createNodeRefFromId(String)}.
 *
 * @author beaurpi
 */
public class InformationApiImpl implements InformationApi {

  /** Path separator used when walking WebDAV/repository paths. */
  private static final String FILE_SEPARATOR = "/";

  /** Name of the child space that holds the Information service under an Interest Group node. */
  private static final String INFORMATION = "Information";

  /**
   * Node service used to read and write repository nodes with security enforcement (the
   * permission-aware {@code NodeService} bean).
   */
  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  /** Service used to create and list files/folders (e.g. news cards) in the repository. */
  @Autowired
  private FileFolderService fileFolderService;

  /** API used to resolve child content of a space, e.g. the files attached to a news card. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to read the permissions set on Information and news nodes. */
  @Autowired
  private PermissionService permissionService;

  /** Service used to obtain the authorities (groups) of the current user. */
  @Autowired
  private AuthorityService authorityService;

  /** Service used to obtain the identity of the currently authenticated user. */
  @Autowired
  private AuthenticationService authenticationService;

  /** Base web root URL used to build the absolute WebDAV URL of the configured index page. */
  private String webRoolUrl;

  /**
   * Reads the configuration of the Information service of the given Interest Group.
   *
   * <p>Resolves the {@code Information} space under the IG node and builds an {@link
   * InformationPage} containing: the (absolute) URL of the configured index page, the "adapt" flag,
   * the "display old information" flag and the permissions set directly on the Information node.
   * When the configured index page is a repository path (not an external {@code http} URL) and the
   * referenced file exists, an absolute WebDAV URL is computed from the category and IG names.
   *
   * @param id the identifier of the Interest Group node
   * @return the Information service configuration for the group
   * @see io.swagger.api.InformationApi#groupsIdInformationGet(java.lang.String)
   */
  @Override
  public InformationPage groupsIdInformationGet(String id) {
    InformationPage result = new InformationPage();

    NodeRef igRef = Converter.createNodeRefFromId(id);
    NodeRef infRef = secureNodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      INFORMATION
    );

    String indexPage = secureNodeService
      .getProperty(infRef, CircabcModel.PROP_INF_INDEX_PAGE)
      .toString();
    if (!webRoolUrl.endsWith("/")) {
      webRoolUrl = webRoolUrl + "/";
    }

    if (indexFileFound(infRef, indexPage)) {
      if (!indexPage.contains("http")) {
        String igName = secureNodeService
          .getProperty(igRef, ContentModel.PROP_NAME)
          .toString();
        NodeRef categRef = secureNodeService
          .getPrimaryParent(igRef)
          .getParentRef();
        String categName = secureNodeService
          .getProperty(categRef, ContentModel.PROP_NAME)
          .toString();

        String webdavContext = "webdav/CircaBC/";
        String url =
          webRoolUrl +
          webdavContext +
          URLEncoder.encode(categName) +
          "/" +
          URLEncoder.encode(igName) +
          "/Information" +
          (indexPage.startsWith("/") ? "" : "/") +
          indexPage;

        result.setUrl(url);
      } else {
        result.setUrl(indexPage);
      }
    }

    Boolean adapt = Boolean.valueOf(
      secureNodeService
        .getProperty(infRef, CircabcModel.PROP_INF_ADAPT)
        .toString()
    );
    result.setAdapt(adapt);

    Object displayOldInfo = secureNodeService.getProperty(
      infRef,
      CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION
    );
    if (displayOldInfo != null) {
      result.setDisplayOldInformation(
        Boolean.valueOf(displayOldInfo.toString())
      );
    }

    String currentAuthority = "";
    Map<String, String> permissions = new HashMap<>();
    for (org.alfresco.service.cmr.security.AccessPermission ac : permissionService.getPermissions(
      infRef
    )) {
      permissions.put(ac.getPermission(), ac.getAccessStatus().name());
      if (currentAuthority.equals("")) {
        currentAuthority = ac.getAuthority();
      }
    }

    result.setPermissions(permissions);

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
   * @return the webRoolUrl
   */
  public String getWebRoolUrl() {
    return webRoolUrl;
  }

  /**
   * @param webRoolUrl the webRoolUrl to set
   */
  public void setWebRoolUrl(String webRoolUrl) {
    this.webRoolUrl = webRoolUrl;
  }

  /**
   * Checks whether the configured index page actually resolves to an existing node under the given
   * Information space.
   *
   * <p>The index page value may contain an {@code Information/} prefix; the portion after it is
   * treated as a slash-separated relative path that is walked, token by token, from the given
   * parent node.
   *
   * @param parent the Information space node to start resolving from
   * @param indexPage the configured index page path
   * @return {@code true} if the path resolves to an existing node, {@code false} otherwise (also
   *     when {@code indexPage} is {@code null} or blank)
   */
  private boolean indexFileFound(final NodeRef parent, final String indexPage) {
    if (indexPage == null || indexPage.trim().isEmpty()) {
      return false;
    } else {
      NodeRef ref = parent;

      String[] split = indexPage.split("Information/");

      if (split.length == 0) {
        return false;
      }

      String path = "";

      if (split.length == 1) {
        path = split[0];
      } else if (split.length == 2) {
        path = split[1];
      }

      final StringTokenizer tokens = new StringTokenizer(
        path,
        FILE_SEPARATOR,
        false
      );

      boolean found = false;

      while (tokens.hasMoreTokens()) {
        String token = tokens.nextToken();

        ref = secureNodeService.getChildByName(
          ref,
          ContentModel.ASSOC_CONTAINS,
          token
        );
        found = ref != null;
      }

      return found;
    }
  }

  /**
   * Returns a page of "News" cards defined under the Information service of the given group.
   *
   * <p>Results are sorted by creation date (most recent first) and paged. Only children carrying the
   * {@link CircabcModel#ASPECT_INFORMATION_NEWS} aspect are converted to {@link News} items; the
   * total reflects the number of news nodes in the Information space.
   *
   * @param id the identifier of the Interest Group node
   * @param limit the maximum number of news items per page
   * @param page the zero-based page index (the request skips {@code page * limit} items)
   * @return a paged list of news items together with the total count
   */
  @Override
  public PagedNews groupsIdInformationNewsGet(
    String id,
    Integer limit,
    Integer page
  ) {
    NodeRef infoRef = getInformationNodeRef(id);
    PagedNews result = new PagedNews();

    PagingRequest pr = new PagingRequest(page * limit, limit);
    List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

    Pair<QName, Boolean> sortPair = new Pair<>(
      QName.createQName(
        NamespaceService.CONTENT_MODEL_1_0_URI,
        ContentModel.PROP_CREATED.getLocalName()
      ),
      false
    );
    sortProps.add(sortPair);

    Set<QName> ignoredQNames = new HashSet<>();
    ignoredQNames.add(ContentModel.TYPE_FOLDER);
    ignoredQNames.add(ContentModel.TYPE_CONTENT);

    final PagingResults<FileInfo> list = fileFolderService.list(
      infoRef,
      false,
      true,
      ignoredQNames,
      sortProps,
      pr
    );

    for (FileInfo item : list.getPage()) {
      final NodeRef childRef = item.getNodeRef();
      if (
        secureNodeService.hasAspect(
          childRef,
          CircabcModel.ASPECT_INFORMATION_NEWS
        )
      ) {
        News news = newsIdGetInternal(childRef);
        if (news != null) {
          result.getData().add(news);
        }
      }
    }

    Set<QName> infoSet = new HashSet<>();
    infoSet.add(CircabcModel.TYPE_INFORMATION_NEWS);
    result.setTotal(
      (long) secureNodeService.getChildAssocs(infoRef, infoSet).size()
    );

    return result;
  }

  /**
   * Builds a fully populated {@link News} model from the given repository node.
   *
   * <p>Reads the core news properties (content, pattern, layout, title, timestamps, size, creator
   * and modifier), then enriches the result with pattern-specific data, the current user's
   * permissions, the owner and, for iframe news, the URL.
   *
   * @param childRef the node reference of the news card
   * @return the populated news model, or {@code null} if the node does not carry the
   *     {@link CircabcModel#ASPECT_INFORMATION_NEWS} aspect
   */
  private News newsIdGetInternal(NodeRef childRef) {
    if (
      !secureNodeService.hasAspect(
        childRef,
        CircabcModel.ASPECT_INFORMATION_NEWS
      )
    ) {
      return null;
    }

    News news = new News();
    news.setContent(
      secureNodeService
        .getProperty(childRef, CircabcModel.PROP_NEWS_CONTENT)
        .toString()
    );
    news.setId(childRef.getId());

    String pattern = secureNodeService
      .getProperty(childRef, CircabcModel.PROP_NEWS_PATTERN)
      .toString();
    news.setPattern(News.PatternEnum.fromValue(pattern));

    String layout = secureNodeService
      .getProperty(childRef, CircabcModel.PROP_NEWS_LAYOUT)
      .toString();
    news.setLayout(News.LayoutEnum.fromValue(layout));

    Serializable titleObj = secureNodeService.getProperty(
      childRef,
      ContentModel.PROP_TITLE
    );
    if (titleObj instanceof String s) {
      news.setTitle(Converter.toI18NProperty(s));
    } else if (titleObj instanceof MLText mlText) {
      news.setTitle(Converter.toI18NProperty(mlText));
    }

    news.setModified(
      new DateTime(
        secureNodeService.getProperty(childRef, ContentModel.PROP_MODIFIED)
      )
    );
    news.setCreated(
      new DateTime(
        secureNodeService.getProperty(childRef, ContentModel.PROP_CREATED)
      )
    );
    news.setSize(
      Integer.parseInt(
        secureNodeService
          .getProperty(childRef, CircabcModel.PROP_NEWS_SIZE)
          .toString()
      )
    );
    news.setModifier(
      secureNodeService
        .getProperty(childRef, ContentModel.PROP_MODIFIER)
        .toString()
    );
    news.setCreator(
      secureNodeService
        .getProperty(childRef, ContentModel.PROP_CREATOR)
        .toString()
    );

    populateNewsPatternData(news, childRef, pattern);
    news.setPermissions(collectCurrentUserPermissions(childRef));

    Map<String, String> properties = new HashMap<>();
    Serializable ownerObj = secureNodeService.getProperty(
      childRef,
      ContentModel.PROP_OWNER
    );
    if (ownerObj != null) {
      properties.put("owner", ownerObj.toString());
    }
    news.setProperties(properties);

    Serializable url = secureNodeService.getProperty(
      childRef,
      CircabcModel.PROP_NEWS_URL
    );
    if (url != null) {
      news.setUrl(url.toString());
    }

    return news;
  }

  /**
   * Populates the pattern-specific fields of a news model.
   *
   * <p>For {@code DOCUMENT} and {@code IMAGE} patterns the attached files are loaded; for the
   * {@code DATE} pattern the associated date is set.
   *
   * @param news the news model to enrich
   * @param childRef the node reference of the news card
   * @param pattern the news pattern value
   */
  private void populateNewsPatternData(
    News news,
    NodeRef childRef,
    String pattern
  ) {
    News.PatternEnum patternEnum = News.PatternEnum.fromValue(pattern);
    if (
      News.PatternEnum.DOCUMENT.equals(patternEnum) ||
      News.PatternEnum.IMAGE.equals(patternEnum)
    ) {
      news.setFiles(spacesApi.spaceGetChildren(childRef.getId(), false));
    } else if (News.PatternEnum.DATE.equals(patternEnum)) {
      Date newsDate = (Date) secureNodeService.getProperty(
        childRef,
        CircabcModel.PROP_NEWS_DATE
      );
      news.setDate(new LocalDate(newsDate));
    }
  }

  /**
   * Collects the permissions set on the given node that apply to the current user, either directly
   * (user authority) or through one of the groups the user belongs to.
   *
   * @param nodeRef the node whose permissions are inspected
   * @return a map of permission name to access status ({@code ALLOWED}/{@code DENIED}) for the
   *     current user
   */
  private Map<String, String> collectCurrentUserPermissions(NodeRef nodeRef) {
    Map<String, String> permissions = new HashMap<>();
    String userName = AuthenticationUtil.getRunAsUser();
    Set<String> authorities = authorityService.getAuthorities();
    for (org.alfresco.service.cmr.security.AccessPermission ac : permissionService.getAllSetPermissions(
      nodeRef
    )) {
      if (
        (ac.getAuthorityType() == AuthorityType.USER &&
          ac.getAuthority().equals(userName)) ||
        (ac.getAuthorityType() == AuthorityType.GROUP &&
          authorities.contains(ac.getAuthority()))
      ) {
        permissions.put(ac.getPermission(), ac.getAccessStatus().name());
      }
    }
    return permissions;
  }

  /**
   * Resolves the {@code Information} space node located under the given Interest Group node.
   *
   * @param id the identifier of the Interest Group node
   * @return the node reference of the Information space
   */
  private NodeRef getInformationNodeRef(String id) {
    NodeRef groupRef = Converter.createNodeRefFromId(id);
    return secureNodeService.getChildByName(
      groupRef,
      ContentModel.ASSOC_CONTAINS,
      INFORMATION
    );
  }

  /**
   * Creates a new "News" card under the Information service of the given group.
   *
   * <p>A new node of type {@link CircabcModel#TYPE_INFORMATION_NEWS} is created and the news
   * properties (title, content, pattern, size, layout and, depending on the pattern, date or URL)
   * are applied. The current user is set as owner. The returned model is updated with the generated
   * id and the resulting modifier/modified metadata.
   *
   * @param id the identifier of the Interest Group node
   * @param news the news content to create
   * @return the created news, enriched with its generated id and modification metadata
   */
  @Override
  public News groupsIdInformationNewsPost(String id, News news) {
    NodeRef infoRef = getInformationNodeRef(id);

    String uid = UUID.randomUUID().toString();
    FileInfo newCard = fileFolderService.create(
      infoRef,
      uid,
      CircabcModel.TYPE_INFORMATION_NEWS
    );
    NodeRef newCardRef = newCard.getNodeRef();
    secureNodeService.setProperty(
      newCardRef,
      ContentModel.PROP_NAME,
      "news_" + newCardRef.getId()
    );
    secureNodeService.setProperty(
      newCardRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(news.getTitle())
    );
    news.setId(newCardRef.getId());

    Map<QName, Serializable> props = new HashMap<>();
    props.put(CircabcModel.PROP_NEWS_CONTENT, news.getContent());
    props.put(CircabcModel.PROP_NEWS_PATTERN, news.getPattern().toString());
    props.put(CircabcModel.PROP_NEWS_SIZE, news.getSize());
    props.put(CircabcModel.PROP_NEWS_LAYOUT, news.getLayout().toString());

    if (news.getPattern().equals(News.PatternEnum.DATE)) {
      props.put(CircabcModel.PROP_NEWS_DATE, news.getDate().toDate());
    }

    if (news.getPattern().equals(News.PatternEnum.IFRAME)) {
      props.put(CircabcModel.PROP_NEWS_URL, news.getUrl());
    }

    secureNodeService.addAspect(
      newCardRef,
      CircabcModel.ASPECT_INFORMATION_NEWS,
      props
    );

    Map<QName, Serializable> propsOwner = new HashMap<>();
    propsOwner.put(
      ContentModel.PROP_OWNER,
      authenticationService.getCurrentUserName()
    );
    secureNodeService.addAspect(
      newCardRef,
      ContentModel.ASPECT_OWNABLE,
      propsOwner
    );

    news.setModifier(
      secureNodeService
        .getProperty(newCardRef, ContentModel.PROP_MODIFIER)
        .toString()
    );
    news.setModified(
      new DateTime(
        secureNodeService.getProperty(newCardRef, ContentModel.PROP_MODIFIED)
      )
    );

    return news;
  }

  /**
   * Deletes the "News" card identified by the given id.
   *
   * @param id the identifier of the news node to delete
   */
  @Override
  public void newsIdDelete(String id) {
    NodeRef newsRef = Converter.createNodeRefFromId(id);
    secureNodeService.deleteNode(newsRef);
  }

  /**
   * Reads the "News" card identified by the given id.
   *
   * @param id the identifier of the news node
   * @return the populated news model, or {@code null} if the node is not a news card
   */
  @Override
  public News newsIdGet(String id) {
    NodeRef newsRef = Converter.createNodeRefFromId(id);
    return newsIdGetInternal(newsRef);
  }

  /**
   * Updates the "News" card identified by the given id with the supplied content.
   *
   * <p>Updates the title, content, pattern, size and layout, and depending on the pattern the URL
   * ({@code IFRAME}) or the date ({@code DATE}).
   *
   * @param id the identifier of the news node to update
   * @param news the new news content
   * @return the updated news model as read back from the repository
   */
  @Override
  public News newsIdPut(String id, News news) {
    NodeRef newsRef = Converter.createNodeRefFromId(id);

    secureNodeService.setProperty(
      newsRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(news.getTitle())
    );
    secureNodeService.setProperty(
      newsRef,
      CircabcModel.PROP_NEWS_CONTENT,
      news.getContent()
    );
    secureNodeService.setProperty(
      newsRef,
      CircabcModel.PROP_NEWS_PATTERN,
      news.getPattern().toString()
    );
    secureNodeService.setProperty(
      newsRef,
      CircabcModel.PROP_NEWS_SIZE,
      news.getSize()
    );
    secureNodeService.setProperty(
      newsRef,
      CircabcModel.PROP_NEWS_LAYOUT,
      news.getLayout().toString()
    );

    if (news.getPattern().equals(News.PatternEnum.IFRAME)) {
      secureNodeService.setProperty(
        newsRef,
        CircabcModel.PROP_NEWS_URL,
        news.getUrl()
      );
    }

    if (news.getPattern().equals(News.PatternEnum.DATE)) {
      secureNodeService.setProperty(
        newsRef,
        CircabcModel.PROP_NEWS_DATE,
        news.getDate().toDate()
      );
    }

    return newsIdGetInternal(newsRef);
  }

  /**
   * Updates the configuration of the Information service of the given group.
   *
   * <p>Only the non-{@code null} fields of the request are applied: the "adapt" flag and the
   * "display old information" flag.
   *
   * @param id the identifier of the Interest Group node
   * @param body the configuration values to apply
   */
  @Override
  public void groupsIdInformationPut(String id, InformationPage body) {
    NodeRef igRef = Converter.createNodeRefFromId(id);
    NodeRef informationRef = secureNodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      INFORMATION
    );

    if (body.getAdapt() != null) {
      secureNodeService.setProperty(
        informationRef,
        CircabcModel.PROP_INF_ADAPT,
        body.getAdapt()
      );
    }

    if (body.getDisplayOldInformation() != null) {
      secureNodeService.setProperty(
        informationRef,
        CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION,
        body.getDisplayOldInformation()
      );
    }
  }
}
