package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.Category;
import io.swagger.model.Header;
import io.swagger.model.I18nProperty;
import io.swagger.model.User;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Business-logic implementation of the {@link HeadersApi} contract for CIRCABC "Headers".
 *
 * <p>In the CIRCABC domain model a Header is the top-level classification node under which
 * Categories (and, in turn, Interest Groups) are organised. Headers are stored in the Alfresco
 * repository as classification categories rooted under a well-known category node named
 * {@code "CircaBCHeader"} in the workspace SpacesStore.</p>
 *
 * <p>This class provides CRUD operations over Headers and their child Categories, delegating to
 * Alfresco's {@link CategoryService} and {@link NodeService} for repository access and to
 * CIRCABC-specific services ({@link CircabcService}, {@link CircabcDaoServiceImpl}) for
 * locale-aware data retrieval. Mutating operations use {@link #secureNodeService} so that
 * permission checks are enforced.</p>
 */
public class HeadersApiImpl implements HeadersApi {

  /** Error message used when a supplied node reference does not identify a Header node. */
  public static final String NOT_A_HEADER = "Not a header";

  private final Log logger = LogFactory.getLog(HeadersApiImpl.class);

  /** Unsecured node service used for read access to node properties and associations. */
  private NodeService nodeService;

  /** Permission-aware node service used for mutating (write) operations on Header nodes. */
  private NodeService secureNodeService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private CircabcDaoServiceImpl circabcDaoServiceImpl;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private CategoryService categoryService;

  /**
   * Cached reference to the root {@code "CircaBCHeader"} category node under which all Headers are
   * created. Lazily resolved (and created if absent) via {@link #initCircabcCategoryRoot()}.
   */
  private NodeRef circabcCategoryRoot;

  /**
   * Deletes the Header identified by the given id.
   *
   * <p>The Header is only removed when it has no child members; otherwise the deletion is rejected.</p>
   *
   * @param id the identifier of the Header node to delete
   * @throws IllegalArgumentException if the id does not identify a Header node, or if the Header
   *     still contains child categories/members
   */
  @Override
  public void deleteHeader(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    boolean isHeaderNode = isHeader(nodeRef);
    if (isHeaderNode) {
      if (
        categoryService
          .getChildren(nodeRef, Mode.MEMBERS, Depth.IMMEDIATE)
          .isEmpty()
      ) {
        categoryService.deleteCategory(nodeRef);
      } else {
        throw new IllegalArgumentException("Header is not empty");
      }
    } else {
      throw new IllegalArgumentException(NOT_A_HEADER);
    }
  }

  /**
   * Retrieves the Categories directly contained by the given Header, localised for the current user.
   *
   * <p>Category titles are resolved using the caller's UI language / locale. Any error encountered
   * while loading categories is logged and results in an empty (or partial) list rather than a
   * thrown exception.</p>
   *
   * @param id the identifier of the Header node whose categories are requested
   * @param language optional language hint for the response (currently informational)
   * @param guest optional flag indicating whether the request is made in guest context
   * @return the list of {@link Category} objects belonging to the Header; never {@code null}
   */
  @Override
  public List<Category> getCategoriesByHeaderId(
    String id,
    String language,
    Boolean guest
  ) {
    List<Category> result = new ArrayList<>();

    NodeRef headerRef = Converter.createNodeRefFromId(id);
    Long headerId = (Long) nodeService.getProperty(
      headerRef,
      ContentModel.PROP_NODE_DBID
    );
    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    User user = usersApi.usersUserIdGet(userName);
    Long localeId = circabcService.getUserLocaleID(userName);
    try {
      List<io.swagger.model.db.Category> categories =
        circabcDaoServiceImpl.selectCategoriesByHeaderLocale(
          headerId,
          localeId
        );
      for (io.swagger.model.db.Category category : categories) {
        Category cat = Converter.toCategory(category, user.getUiLang());
        NodeRef categRef = Converter.createNodeRefFromId(cat.getId());
        Map<String, String> title = circabcService.getCategoryTitle(categRef);
        cat.setTitle(Converter.convertMlToI18nProperty(title));
        result.add(cat);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error in getCategoriesByHeaderId", e);
      }
    }

    return result;
  }

  /**
   * Retrieves a single Header, including its name, description and child categories.
   *
   * @param id the identifier of the Header node to retrieve
   * @return the fully populated {@link Header}
   * @throws IllegalArgumentException if the id does not identify a Header node
   */
  @Override
  public Header getHeader(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    boolean isHeaderNode = isHeader(nodeRef);
    if (isHeaderNode) {
      Header header = new Header();
      header.setName(
        (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
      );
      header.setId(nodeRef.getId());
      final Serializable property = nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_DESCRIPTION
      );
      setHaederDescription(header, property);
      header.setCategories(getCategoriesByHeaderId(id, null, null));
      return header;
    } else {
      throw new IllegalArgumentException(NOT_A_HEADER);
    }
  }

  /**
   * Populates the description of a Header from a raw Alfresco property value.
   *
   * <p>Supports both multilingual ({@link MLText}) and plain {@link String} property values,
   * converting them into an {@link I18nProperty}. If the value is neither type the description is
   * left unset.</p>
   *
   * @param header the Header to update
   * @param property the raw {@code cm:description} property value read from the repository
   */
  private void setHaederDescription(Header header, Serializable property) {
    I18nProperty description = null;
    if (property instanceof MLText mlText) {
      description = Converter.toI18NProperty(mlText);
    } else if (property instanceof String str) {
      description = Converter.toI18NProperty(str);
    }
    header.setDescription(description);
  }

  /**
   * Retrieves all Headers defined in the repository.
   *
   * <p>Any error encountered while loading headers is logged and results in an empty (or partial)
   * list rather than a thrown exception.</p>
   *
   * @param language optional language hint for the response (currently informational)
   * @param guest optional flag indicating whether the request is made in guest context
   * @return the list of all {@link Header} objects; never {@code null}
   */
  @Override
  public List<Header> getHeaders(String language, Boolean guest) {
    List<Header> headers = new ArrayList<>();

    try {
      List<io.swagger.model.db.Header> headerList =
        circabcDaoServiceImpl.selectHeaders();
      for (io.swagger.model.db.Header header : headerList) {
        Header hd = Converter.toHeader(header);
        headers.add(hd);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error in getHeaders", e);
      }
    }

    return headers;
  }

  /**
   * Creates a new Category under the given Header.
   *
   * <p>Not yet implemented; currently always returns {@code null}.</p>
   *
   * @param id the identifier of the parent Header node
   * @param body the Category to create
   * @return the created {@link Category}, or {@code null} as this operation is not implemented
   */
  @Override
  public Category headersIdCategoriesPost(String id, Category body) {
    return null;
  }

  /**
   * Creates a new Header under the CIRCABC header root category.
   *
   * <p>The header root is lazily resolved (and created if missing) before the new Header category
   * is created and its description property is set.</p>
   *
   * @param body the Header to create, providing its name and description
   * @return the created {@link Header}, populated with the generated node id
   */
  @Override
  public Header postHeader(Header body) {
    initCircabcCategoryRoot();
    final NodeRef headerNodeRef = categoryService.createCategory(
      circabcCategoryRoot,
      body.getName()
    );
    secureNodeService.setProperty(
      headerNodeRef,
      ContentModel.PROP_DESCRIPTION,
      Converter.toMLText(body.getDescription())
    );
    Header result = new Header();
    result.setId(headerNodeRef.getId());
    result.setName(body.getName());
    result.setDescription(body.getDescription());
    return result;
  }

  /**
   * Updates the name and description of an existing Header.
   *
   * @param id the identifier of the Header node to update
   * @param body the Header carrying the new name and description values
   * @return the updated {@link Header}
   * @throws IllegalArgumentException if the id does not identify a Header node
   */
  @Override
  public Header putHeader(String id, Header body) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    boolean isHeaderNode = isHeader(nodeRef);
    if (isHeaderNode) {
      secureNodeService.setProperty(
        nodeRef,
        ContentModel.PROP_DESCRIPTION,
        Converter.toMLText(body.getDescription())
      );
      secureNodeService.setProperty(
        nodeRef,
        ContentModel.PROP_NAME,
        body.getName()
      );
      Header result = new Header();
      result.setId(id);
      result.setName(body.getName());
      result.setDescription(body.getDescription());
      return result;
    } else {
      throw new IllegalArgumentException(NOT_A_HEADER);
    }
  }

  /**
   * Determines whether the given node is a Header, i.e. whether its primary parent is the CIRCABC
   * header root category.
   *
   * @param nodeRef the node to test
   * @return {@code true} if the node is a direct child of the header root category; {@code false}
   *     otherwise
   */
  private boolean isHeader(NodeRef nodeRef) {
    initCircabcCategoryRoot();
    return nodeService
      .getPrimaryParent(nodeRef)
      .getParentRef()
      .equals(circabcCategoryRoot);
  }

  /**
   * Ensures {@link #circabcCategoryRoot} is resolved, triggering lazy initialisation on first use.
   */
  private void initCircabcCategoryRoot() {
    if (circabcCategoryRoot == null) {
      setCircabcCategoryRoot();
    }
  }

  /**
   * Resolves the {@code "CircaBCHeader"} root category from the workspace SpacesStore and caches it
   * in {@link #circabcCategoryRoot}. If the category does not yet exist it is created under the same
   * parent as the existing root classification categories.
   */
  private void setCircabcCategoryRoot() {
    Collection<ChildAssociationRef> rootCategories =
      categoryService.getCategories(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        ContentModel.ASPECT_GEN_CLASSIFIABLE,
        Depth.IMMEDIATE
      );

    NodeRef parenNodeRef = null;
    for (ChildAssociationRef child : rootCategories) {
      if (parenNodeRef == null) {
        parenNodeRef = child.getParentRef();
      }
      if (
        nodeService
          .getProperty(child.getChildRef(), ContentModel.PROP_NAME)
          .equals("CircaBCHeader")
      ) {
        circabcCategoryRoot = child.getChildRef();
        break;
      }
    }
    if (circabcCategoryRoot == null && parenNodeRef != null) {
      // Create the category "CircaBCHeader"
      circabcCategoryRoot = categoryService.createCategory(
        parenNodeRef,
        "CircaBCHeader"
      );
    }
  }

  /**
   * Injects the unsecured node service used for read operations.
   *
   * @param nodeService the {@link NodeService} to use
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Injects the permission-aware node service used for mutating operations.
   *
   * @param secureNodeService the secure {@link NodeService} to use
   */
  public void setSecureNodeService(NodeService secureNodeService) {
    this.secureNodeService = secureNodeService;
  }

  /**
   * Retrieves the Header that owns the given Category.
   *
   * <p>Resolves the category's classification associations and, when the category is classified
   * under exactly one node, returns the corresponding Header.</p>
   *
   * @param id the identifier of the Category node
   * @return the owning {@link Header}, or {@code null} if none can be unambiguously determined
   * @throws IllegalArgumentException if the resolved classification node is not a Header
   */
  @Override
  public Header getHeaderByCategory(String id) {
    NodeRef categoryRef = Converter.createNodeRefFromId(id);

    Header result = null;

    @SuppressWarnings("unchecked")
    List<NodeRef> categories = (List<NodeRef>) nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_CATEGORIES
    );
    if (categories != null && categories.size() == 1) {
      result = getHeader(categories.get(0).getId());
    }

    return result;
  }
}
