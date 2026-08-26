package io.swagger.service;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.exception.ValidationException;
import io.swagger.model.ArticleImportDto;
import io.swagger.model.CategoryImportDto;
import io.swagger.model.ImportResult;
import io.swagger.model.SubcategoryImportDto;
import io.swagger.util.Converter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for importing FAQ structure from JSON format.
 * Creates or updates Alfresco nodes for categories, subcategories, and articles.
 */
public class FaqImportService {

  private static final Log logger = LogFactory.getLog(FaqImportService.class);
  private static final String FAQS = "faqs";

  private final NodeService nodeService;
  private final ManagementService managementService;
  private final FaqValidationService validationService;

  // Map to track old ID -> new NodeRef during import
  private final Map<String, NodeRef> idMapping = new HashMap<>();

  public FaqImportService(
    final NodeService nodeService,
    final ManagementService managementService,
    final FaqValidationService validationService
  ) {
    this.nodeService = nodeService;
    this.managementService = managementService;
    this.validationService = validationService;
  }

  /**
   * Imports FAQ structure from JSON content.
   * Validates the data and creates or updates all nodes.
   *
   * @param jsonContent JSON string containing FAQ structure
   * @return ImportResult with statistics
   * @throws ValidationException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  public ImportResult importFaqStructure(final String jsonContent)
    throws ValidationException {
    if (logger.isInfoEnabled()) {
      logger.info("Starting FAQ import");
    }

    // Clear ID mapping for this import
    idMapping.clear();

    // Validate schema and parse JSON
    final List<CategoryImportDto> categories = validationService.validateSchema(
      jsonContent
    );

    // Validate referential integrity
    validationService.validateReferentialIntegrity(categories);

    // Validate data types
    validationService.validateDataTypes(categories);

    if (logger.isInfoEnabled()) {
      logger.info("Validation successful, proceeding with import");
    }

    // Import all categories, subcategories, and articles
    final ImportResult result = new ImportResult();
    int categoriesProcessed = 0;
    int subcategoriesProcessed = 0;
    int articlesProcessed = 0;

    for (final CategoryImportDto category : categories) {
      createOrUpdateCategory(category);
      categoriesProcessed++;

      // Import articles directly under the category
      if (category.getArticles() != null) {
        for (final ArticleImportDto article : category.getArticles()) {
          createOrUpdateArticle(article);
          articlesProcessed++;
        }
      }

      if (category.getSubcategories() != null) {
        for (final SubcategoryImportDto subcategory : category.getSubcategories()) {
          createOrUpdateSubcategory(subcategory);
          subcategoriesProcessed++;

          if (subcategory.getArticles() != null) {
            for (final ArticleImportDto article : subcategory.getArticles()) {
              createOrUpdateArticle(article);
              articlesProcessed++;
            }
          }
        }
      }
    }

    result.setMessage("FAQ import completed successfully");
    result.setCategoriesProcessed(categoriesProcessed);
    result.setSubcategoriesProcessed(subcategoriesProcessed);
    result.setArticlesProcessed(articlesProcessed);

    if (logger.isInfoEnabled()) {
      logger.info(
        String.format(
          "FAQ import completed: %d sections, %d subsections, %d articles",
          categoriesProcessed,
          subcategoriesProcessed,
          articlesProcessed
        )
      );
    }

    return result;
  }

  /**
   * Creates or updates a category node.
   *
   * @param category CategoryImportDto with all properties
   */
  private void createOrUpdateCategory(final CategoryImportDto category) {
    final NodeRef faqsRef = getFaqsRef();
    NodeRef categoryRef = findNodeById(category.getId());

    if (categoryRef == null) {
      // Create new category
      final String name = getCleanFileName(
        getNameFromI18n(category.getTitle())
      );

      final ChildAssociationRef categRef = nodeService.createNode(
        faqsRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        ContentModel.TYPE_FOLDER
      );

      categoryRef = categRef.getChildRef();

      nodeService.addAspect(
        categoryRef,
        CircabcModel.ASPECT_HELP_CATEGORY,
        null
      );

      if (logger.isDebugEnabled()) {
        logger.debug("Created new category with ID: " + categoryRef.getId());
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Updating existing category: " + category.getId());
      }
    }

    // Map old ID to new/existing NodeRef
    idMapping.put(category.getId(), categoryRef);

    // Set or update properties
    final MLText title = Converter.toMLText(category.getTitle());
    nodeService.setProperty(categoryRef, ContentModel.PROP_TITLE, title);
  }

  /**
   * Creates or updates a subcategory node.
   *
   * @param subcategory SubcategoryImportDto with all properties
   */
  private void createOrUpdateSubcategory(
    final SubcategoryImportDto subcategory
  ) {
    // Try to find parent using ID mapping first, then by direct lookup
    NodeRef parentRef = idMapping.get(subcategory.getParentId());
    if (parentRef == null) {
      parentRef = findNodeById(subcategory.getParentId());
    }

    if (parentRef == null) {
      throw new IllegalStateException(
        "Parent category not found for subcategory: " + subcategory.getId()
      );
    }

    NodeRef subcategoryRef = findNodeById(subcategory.getId());

    if (subcategoryRef == null) {
      // Create new subcategory
      final String name = getCleanFileName(
        getNameFromI18n(subcategory.getTitle())
      );

      final ChildAssociationRef subcatRef = nodeService.createNode(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        ContentModel.TYPE_FOLDER
      );

      subcategoryRef = subcatRef.getChildRef();

      nodeService.addAspect(
        subcategoryRef,
        CircabcModel.ASPECT_HELP_SUBCATEGORY,
        null
      );

      if (logger.isDebugEnabled()) {
        logger.debug(
          "Created new subcategory with ID: " + subcategoryRef.getId()
        );
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Updating existing subcategory: " + subcategory.getId());
      }
    }

    // Map old ID to new/existing NodeRef
    idMapping.put(subcategory.getId(), subcategoryRef);

    // Set or update properties
    final MLText title = Converter.toMLText(subcategory.getTitle());
    nodeService.setProperty(subcategoryRef, ContentModel.PROP_TITLE, title);
    nodeService.setProperty(
      subcategoryRef,
      CircabcModel.PROP_HELP_SUBCATEGORY_SORT_ORDER,
      subcategory.getSortOrder()
    );
  }

  /**
   * Creates or updates an article node.
   *
   * @param article ArticleImportDto with all properties
   */
  private void createOrUpdateArticle(final ArticleImportDto article) {
    // Try to find parent using ID mapping first, then by direct lookup
    NodeRef parentRef = idMapping.get(article.getParentId());
    if (parentRef == null) {
      parentRef = findNodeById(article.getParentId());
    }

    if (parentRef == null) {
      throw new IllegalStateException(
        "Parent subcategory not found for article: " + article.getId()
      );
    }

    NodeRef articleRef = findNodeById(article.getId());

    if (articleRef == null) {
      // Create new article
      final String name = getCleanFileName(getNameFromI18n(article.getTitle()));

      final ChildAssociationRef artRef = nodeService.createNode(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        ContentModel.TYPE_CONTENT
      );

      articleRef = artRef.getChildRef();

      nodeService.addAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE, null);

      if (logger.isDebugEnabled()) {
        logger.debug("Created new article with ID: " + articleRef.getId());
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Updating existing article: " + article.getId());
      }
    }

    // Map old ID to new/existing NodeRef
    idMapping.put(article.getId(), articleRef);

    // Set or update properties
    final MLText title = Converter.toMLText(article.getTitle());
    final MLText content = Converter.toMLText(article.getContent());

    nodeService.setProperty(articleRef, ContentModel.PROP_TITLE, title);
    nodeService.setProperty(articleRef, ContentModel.PROP_DESCRIPTION, content);
    nodeService.setProperty(
      articleRef,
      CircabcModel.PROP_HELP_ARTICLE_SORT_ORDER,
      article.getSortOrder()
    );

    // Handle highlighted aspect
    final boolean isHighlighted = nodeService.hasAspect(
      articleRef,
      CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
    );
    final boolean shouldBeHighlighted =
      article.getHighlighted() != null && article.getHighlighted();

    if (shouldBeHighlighted && !isHighlighted) {
      nodeService.addAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED,
        null
      );
    } else if (!shouldBeHighlighted && isHighlighted) {
      nodeService.removeAspect(
        articleRef,
        CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED
      );
    }
  }

  /**
   * Finds a node by its ID.
   *
   * @param id Node ID
   * @return NodeRef if found, null otherwise
   */
  private NodeRef findNodeById(final String id) {
    if (id == null || id.trim().isEmpty()) {
      return null;
    }

    try {
      final NodeRef nodeRef = Converter.createNodeRefFromId(id);
      if (nodeService.exists(nodeRef)) {
        return nodeRef;
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Node not found for ID: " + id);
      }
    }

    return null;
  }

  /**
   * Extracts a name from i18n property (prefers English, falls back to first available).
   *
   * @param i18nProperty I18n property map
   * @return Name string
   */
  private String getNameFromI18n(final Map<String, String> i18nProperty) {
    if (i18nProperty == null || i18nProperty.isEmpty()) {
      return "untitled";
    }

    if (i18nProperty.containsKey("en")) {
      return i18nProperty.get("en");
    }

    return i18nProperty.values().iterator().next();
  }

  /**
   * Cleans filename by removing invalid characters.
   *
   * @param filename Original filename
   * @return Cleaned filename
   */
  private String getCleanFileName(final String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      return "untitled";
    }
    return filename.replaceAll("[\\\\/:*?\"<>|!]", "_");
  }

  /**
   * Retrieves the FAQs root node reference.
   *
   * @return NodeRef of the FAQs root
   */
  private NodeRef getFaqsRef() {
    final NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
    return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);
  }
}
