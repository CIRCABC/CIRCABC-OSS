package io.swagger.service;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.model.ArticleExportDto;
import io.swagger.model.CategoryExportDto;
import io.swagger.model.I18nProperty;
import io.swagger.model.SubcategoryExportDto;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Service for exporting FAQ structure to JSON format.
 * Traverses the Alfresco node hierarchy and serializes categories, subcategories, and articles.
 */
public class FaqExportService {

  private static final Log logger = LogFactory.getLog(FaqExportService.class);
  private static final String FAQS = "faqs";

  private final NodeService nodeService;
  private final ManagementService managementService;

  public FaqExportService(
    final NodeService nodeService,
    final ManagementService managementService
  ) {
    this.nodeService = nodeService;
    this.managementService = managementService;
  }

  /**
   * Exports the complete FAQ structure to JSON string.
   *
   * @return JSON string containing all categories with their subcategories and articles
   */
  public String exportFaqStructure() {
    if (logger.isInfoEnabled()) {
      logger.info("Starting FAQ export");
    }

    final List<CategoryExportDto> categories = getAllCategories();

    if (logger.isInfoEnabled()) {
      logger.info("Exported " + categories.size() + " categories");
    }

    return toJsonString(categories);
  }

  /**
   * Retrieves all FAQ categories with their complete hierarchy.
   *
   * @return List of category DTOs with subcategories and articles
   */
  private List<CategoryExportDto> getAllCategories() {
    final List<CategoryExportDto> categories = new ArrayList<>();
    final NodeRef faqsRef = getFaqsRef();

    if (faqsRef == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("FAQs root node not found");
      }
      return categories;
    }

    for (final ChildAssociationRef child : nodeService.getChildAssocs(
      faqsRef
    )) {
      final NodeRef childRef = child.getChildRef();
      if (nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_CATEGORY)) {
        final CategoryExportDto category = mapCategory(childRef);
        categories.add(category);
      }
    }

    return categories;
  }

  /**
   * Maps an Alfresco category node to a CategoryExportDto.
   *
   * @param categoryRef NodeRef of the category
   * @return CategoryExportDto with all properties and subcategories
   */
  private CategoryExportDto mapCategory(final NodeRef categoryRef) {
    final CategoryExportDto category = new CategoryExportDto();
    category.setId(categoryRef.getId());

    final Serializable title = nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_TITLE
    );
    category.setTitle(convertToI18nProperty(title));

    // Get articles directly under the category
    final List<ArticleExportDto> categoryArticles = getArticles(categoryRef);
    category.setArticles(categoryArticles);

    final List<SubcategoryExportDto> subcategories = getSubcategories(
      categoryRef
    );
    category.setSubcategories(subcategories);

    int totalArticles = categoryArticles.size();
    for (final SubcategoryExportDto subcategory : subcategories) {
      totalArticles += subcategory.getNumberOfArticles();
    }
    category.setNumberOfArticles(totalArticles);

    return category;
  }

  /**
   * Retrieves all subcategories for a given category.
   *
   * @param categoryRef NodeRef of the parent category
   * @return List of subcategory DTOs with articles
   */
  private List<SubcategoryExportDto> getSubcategories(
    final NodeRef categoryRef
  ) {
    final List<SubcategoryExportDto> subcategories = new ArrayList<>();

    for (final ChildAssociationRef child : nodeService.getChildAssocs(
      categoryRef
    )) {
      final NodeRef childRef = child.getChildRef();

      if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_SUBCATEGORY)
      ) {
        final SubcategoryExportDto subcategory = mapSubcategory(
          childRef,
          categoryRef.getId()
        );
        subcategories.add(subcategory);
      }
    }

    return subcategories;
  }

  /**
   * Maps an Alfresco subcategory node to a SubcategoryExportDto.
   *
   * @param subcategoryRef NodeRef of the subcategory
   * @param parentId ID of the parent category
   * @return SubcategoryExportDto with all properties and articles
   */
  private SubcategoryExportDto mapSubcategory(
    final NodeRef subcategoryRef,
    final String parentId
  ) {
    final SubcategoryExportDto subcategory = new SubcategoryExportDto();
    subcategory.setId(subcategoryRef.getId());
    subcategory.setParentId(parentId);

    final Serializable title = nodeService.getProperty(
      subcategoryRef,
      ContentModel.PROP_TITLE
    );
    subcategory.setTitle(convertToI18nProperty(title));

    final Serializable sortOrderProp = nodeService.getProperty(
      subcategoryRef,
      CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER
    );
    subcategory.setSortOrder(
      sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
    );

    final List<ArticleExportDto> articles = getArticles(subcategoryRef);
    subcategory.setArticles(articles);
    subcategory.setNumberOfArticles(articles.size());

    return subcategory;
  }

  /**
   * Retrieves all articles for a given subcategory.
   *
   * @param subcategoryRef NodeRef of the parent subcategory
   * @return List of article DTOs
   */
  private List<ArticleExportDto> getArticles(final NodeRef subcategoryRef) {
    final List<ArticleExportDto> articles = new ArrayList<>();

    for (final ChildAssociationRef child : nodeService.getChildAssocs(
      subcategoryRef
    )) {
      final NodeRef childRef = child.getChildRef();
      if (nodeService.hasAspect(childRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
        final ArticleExportDto article = mapArticle(
          childRef,
          subcategoryRef.getId()
        );
        articles.add(article);
      }
    }

    return articles;
  }

  /**
   * Maps an Alfresco article node to an ArticleExportDto.
   *
   * @param articleRef NodeRef of the article
   * @param parentId ID of the parent subcategory
   * @return ArticleExportDto with all properties
   */
  private ArticleExportDto mapArticle(
    final NodeRef articleRef,
    final String parentId
  ) {
    final ArticleExportDto article = new ArticleExportDto();
    article.setId(articleRef.getId());
    article.setParentId(parentId);

    final Serializable title = nodeService.getProperty(
      articleRef,
      ContentModel.PROP_TITLE
    );
    article.setTitle(convertToI18nProperty(title));

    final Serializable description = nodeService.getProperty(
      articleRef,
      ContentModel.PROP_DESCRIPTION
    );
    article.setContent(convertToI18nProperty(description));

    article.setHighlighted(
      nodeService.hasAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      )
    );

    final Serializable sortOrderProp = nodeService.getProperty(
      articleRef,
      CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER
    );
    article.setSortOrder(
      sortOrderProp instanceof Integer ? (Integer) sortOrderProp : 0
    );

    return article;
  }

  /**
   * Converts Alfresco property value to I18nProperty.
   *
   * @param property Serializable property value (String or MLText)
   * @return I18nProperty with language mappings
   */
  private I18nProperty convertToI18nProperty(final Serializable property) {
    if (property instanceof String) {
      return Converter.toI18NProperty((String) property);
    } else if (property instanceof MLText) {
      return Converter.toI18NProperty((MLText) property);
    }
    return new I18nProperty();
  }

  /**
   * Retrieves the FAQs root node reference.
   *
   * @return NodeRef of the FAQs root, or null if not found
   */
  private NodeRef getFaqsRef() {
    final NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);
  }

  /**
   * Converts list of CategoryExportDto to JSON string.
   *
   * @param categories List of categories to serialize
   * @return JSON string representation
   */
  @SuppressWarnings("unchecked")
  private String toJsonString(final List<CategoryExportDto> categories) {
    final JSONArray jsonArray = new JSONArray();
    for (final CategoryExportDto category : categories) {
      jsonArray.add(categoryToJson(category));
    }
    return jsonArray.toJSONString();
  }

  @SuppressWarnings("unchecked")
  private JSONObject categoryToJson(final CategoryExportDto category) {
    final JSONObject json = new JSONObject();
    json.put("id", category.getId());
    json.put("title", i18nPropertyToJson(category.getTitle()));
    json.put("numberOfArticles", category.getNumberOfArticles());

    final JSONArray articlesArray = new JSONArray();
    if (category.getArticles() != null) {
      for (final ArticleExportDto article : category.getArticles()) {
        articlesArray.add(articleToJson(article));
      }
    }
    json.put("articles", articlesArray);

    final JSONArray subcategoriesArray = new JSONArray();
    for (final SubcategoryExportDto subcategory : category.getSubcategories()) {
      subcategoriesArray.add(subcategoryToJson(subcategory));
    }
    json.put("subcategories", subcategoriesArray);

    return json;
  }

  @SuppressWarnings("unchecked")
  private JSONObject subcategoryToJson(final SubcategoryExportDto subcategory) {
    final JSONObject json = new JSONObject();
    json.put("id", subcategory.getId());
    json.put("title", i18nPropertyToJson(subcategory.getTitle()));
    json.put("sortOrder", subcategory.getSortOrder());
    json.put("parentId", subcategory.getParentId());
    json.put("numberOfArticles", subcategory.getNumberOfArticles());

    final JSONArray articlesArray = new JSONArray();
    for (final ArticleExportDto article : subcategory.getArticles()) {
      articlesArray.add(articleToJson(article));
    }
    json.put("articles", articlesArray);

    return json;
  }

  @SuppressWarnings("unchecked")
  private JSONObject articleToJson(final ArticleExportDto article) {
    final JSONObject json = new JSONObject();
    json.put("id", article.getId());
    json.put("parentId", article.getParentId());
    json.put("title", i18nPropertyToJson(article.getTitle()));
    json.put("content", i18nPropertyToJson(article.getContent()));
    json.put("highlighted", article.getHighlighted());
    json.put("sortOrder", article.getSortOrder());
    return json;
  }

  @SuppressWarnings("unchecked")
  private JSONObject i18nPropertyToJson(final I18nProperty i18nProperty) {
    final JSONObject json = new JSONObject();
    if (i18nProperty != null) {
      json.putAll(i18nProperty);
    }
    return json;
  }
}
