package io.swagger.service;

import io.swagger.exception.ValidationException;
import io.swagger.model.ArticleImportDto;
import io.swagger.model.CategoryImportDto;
import io.swagger.model.I18nProperty;
import io.swagger.model.SubcategoryImportDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Service for validating FAQ import data.
 * Validates JSON schema, referential integrity, and data types.
 */
public class FaqValidationService {

  private final JSONParser jsonParser;

  public FaqValidationService() {
    this.jsonParser = new JSONParser();
  }

  /**
   * Validates JSON schema matches expected structure.
   *
   * @param jsonContent JSON string to validate
   * @return List of parsed CategoryImportDto objects
   * @throws ValidationException if validation fails
   */
  public List<CategoryImportDto> validateSchema(String jsonContent)
    throws ValidationException {
    final List<String> errors = new ArrayList<>();

    if (jsonContent == null || jsonContent.trim().isEmpty()) {
      errors.add("JSON content is empty or null");
      throw new ValidationException("Schema validation failed", errors);
    }

    JSONArray categoriesArray;
    try {
      final Object parsed = jsonParser.parse(jsonContent);
      if (!(parsed instanceof JSONArray)) {
        errors.add("Root element must be an array of categories");
        throw new ValidationException("Schema validation failed", errors);
      }
      categoriesArray = (JSONArray) parsed;
    } catch (ParseException e) {
      errors.add(
        "Invalid JSON format. Please ensure the file is properly formatted."
      );
      throw new ValidationException("Schema validation failed", errors);
    }

    final List<CategoryImportDto> categories = new ArrayList<>();
    for (int i = 0; i < categoriesArray.size(); i++) {
      final Object categoryObj = categoriesArray.get(i);
      if (!(categoryObj instanceof JSONObject)) {
        errors.add(
          String.format("Category at index %d is not a valid object", i)
        );
        continue;
      }
      final CategoryImportDto category = parseCategory(
        (JSONObject) categoryObj,
        i,
        errors
      );
      if (category != null) {
        categories.add(category);
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Schema validation failed", errors);
    }

    return categories;
  }

  /**
   * Validates referential integrity of parent-child relationships.
   *
   * @param categories List of categories to validate
   * @throws ValidationException if validation fails
   */
  public void validateReferentialIntegrity(List<CategoryImportDto> categories)
    throws ValidationException {
    final List<String> errors = new ArrayList<>();
    final Set<String> categoryIds = new HashSet<>();
    final Set<String> subcategoryIds = new HashSet<>();

    // Collect all category IDs
    for (CategoryImportDto category : categories) {
      if (category.getId() != null) {
        categoryIds.add(category.getId());
      }
    }

    // Validate subcategories and collect their IDs
    for (CategoryImportDto category : categories) {
      if (category.getSubcategories() != null) {
        for (SubcategoryImportDto subcategory : category.getSubcategories()) {
          if (subcategory.getId() != null) {
            subcategoryIds.add(subcategory.getId());
          }

          // Validate subcategory parentId references existing category
          if (subcategory.getParentId() != null) {
            if (!categoryIds.contains(subcategory.getParentId())) {
              errors.add(
                String.format(
                  "Subcategory '%s' references non-existent category '%s'",
                  subcategory.getId(),
                  subcategory.getParentId()
                )
              );
            }
          }
        }
      }
    }

    // Validate articles directly under categories
    for (CategoryImportDto category : categories) {
      if (category.getArticles() != null) {
        for (ArticleImportDto article : category.getArticles()) {
          // Validate article parentId references existing category
          if (article.getParentId() != null) {
            if (!categoryIds.contains(article.getParentId())) {
              errors.add(
                String.format(
                  "Article '%s' references non-existent category '%s'",
                  article.getId(),
                  article.getParentId()
                )
              );
            }
          }
        }
      }
    }

    // Validate articles under subcategories
    for (CategoryImportDto category : categories) {
      if (category.getSubcategories() != null) {
        for (SubcategoryImportDto subcategory : category.getSubcategories()) {
          if (subcategory.getArticles() != null) {
            for (ArticleImportDto article : subcategory.getArticles()) {
              // Validate article parentId references existing subcategory
              if (article.getParentId() != null) {
                if (!subcategoryIds.contains(article.getParentId())) {
                  errors.add(
                    String.format(
                      "Article '%s' references non-existent subcategory '%s'",
                      article.getId(),
                      article.getParentId()
                    )
                  );
                }
              }
            }
          }
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException(
        "Referential integrity validation failed",
        errors
      );
    }
  }

  /**
   * Validates data types and constraints.
   *
   * @param categories List of categories to validate
   * @throws ValidationException if validation fails
   */
  public void validateDataTypes(List<CategoryImportDto> categories)
    throws ValidationException {
    final List<String> errors = new ArrayList<>();

    for (CategoryImportDto category : categories) {
      // Validate category i18n content
      validateI18nProperty(
        category.getTitle(),
        "Category " + category.getId() + " title",
        errors
      );

      // Validate articles directly under category
      if (category.getArticles() != null) {
        for (ArticleImportDto article : category.getArticles()) {
          // Validate article sortOrder
          if (article.getSortOrder() == null) {
            errors.add(
              String.format("Article '%s' has null sortOrder", article.getId())
            );
          }

          // Validate article highlighted flag
          if (article.getHighlighted() == null) {
            errors.add(
              String.format(
                "Article '%s' has null highlighted flag",
                article.getId()
              )
            );
          }

          // Validate article i18n content
          validateI18nProperty(
            article.getTitle(),
            "Article " + article.getId() + " title",
            errors
          );
          validateI18nProperty(
            article.getContent(),
            "Article " + article.getId() + " content",
            errors
          );
        }
      }

      if (category.getSubcategories() != null) {
        for (SubcategoryImportDto subcategory : category.getSubcategories()) {
          // Validate subcategory sortOrder
          if (subcategory.getSortOrder() == null) {
            errors.add(
              String.format(
                "Subcategory '%s' has null sortOrder",
                subcategory.getId()
              )
            );
          }

          // Validate subcategory i18n content
          validateI18nProperty(
            subcategory.getTitle(),
            "Subcategory " + subcategory.getId() + " title",
            errors
          );

          if (subcategory.getArticles() != null) {
            for (ArticleImportDto article : subcategory.getArticles()) {
              // Validate article sortOrder
              if (article.getSortOrder() == null) {
                errors.add(
                  String.format(
                    "Article '%s' has null sortOrder",
                    article.getId()
                  )
                );
              }

              // Validate article highlighted flag
              if (article.getHighlighted() == null) {
                errors.add(
                  String.format(
                    "Article '%s' has null highlighted flag",
                    article.getId()
                  )
                );
              }

              // Validate article i18n content
              validateI18nProperty(
                article.getTitle(),
                "Article " + article.getId() + " title",
                errors
              );
              validateI18nProperty(
                article.getContent(),
                "Article " + article.getId() + " content",
                errors
              );
            }
          }
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Data type validation failed", errors);
    }
  }

  private void validateCategorySchema(
    CategoryImportDto category,
    int index,
    List<String> errors
  ) {
    if (category.getId() == null || category.getId().trim().isEmpty()) {
      errors.add(
        String.format(
          "Category at index %d is missing required field 'id'",
          index
        )
      );
    }

    if (category.getTitle() == null) {
      errors.add(
        String.format(
          "Category at index %d is missing required field 'title'",
          index
        )
      );
    }

    if (category.getSubcategories() != null) {
      for (int j = 0; j < category.getSubcategories().size(); j++) {
        final SubcategoryImportDto subcategory = category
          .getSubcategories()
          .get(j);
        validateSubcategorySchema(subcategory, index, j, errors);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private CategoryImportDto parseCategory(
    final JSONObject json,
    final int index,
    final List<String> errors
  ) {
    final CategoryImportDto category = new CategoryImportDto();

    final String id = (String) json.get("id");
    if (id == null || id.trim().isEmpty()) {
      errors.add(
        String.format(
          "Category at index %d is missing required field 'id'",
          index
        )
      );
      return null;
    }
    category.setId(id);

    final Object titleObj = json.get("title");
    if (titleObj == null) {
      errors.add(
        String.format(
          "Category at index %d is missing required field 'title'",
          index
        )
      );
      return null;
    }
    category.setTitle(parseI18nProperty((Map<String, Object>) titleObj));

    final Object numberOfArticlesObj = json.get("numberOfArticles");
    category.setNumberOfArticles(
      numberOfArticlesObj instanceof Number
        ? ((Number) numberOfArticlesObj).intValue()
        : 0
    );

    // Parse articles directly under category
    final JSONArray articlesArray = (JSONArray) json.get("articles");
    if (articlesArray != null) {
      final List<ArticleImportDto> articles = new ArrayList<>();
      for (int k = 0; k < articlesArray.size(); k++) {
        final Object articleObj = articlesArray.get(k);
        if (articleObj instanceof JSONObject) {
          final ArticleImportDto article = parseCategoryArticle(
            (JSONObject) articleObj,
            index,
            k,
            errors
          );
          if (article != null) {
            articles.add(article);
          }
        }
      }
      category.setArticles(articles);
    }

    final JSONArray subcategoriesArray = (JSONArray) json.get("subcategories");
    if (subcategoriesArray != null) {
      final List<SubcategoryImportDto> subcategories = new ArrayList<>();
      for (int j = 0; j < subcategoriesArray.size(); j++) {
        final Object subcategoryObj = subcategoriesArray.get(j);
        if (subcategoryObj instanceof JSONObject) {
          final SubcategoryImportDto subcategory = parseSubcategory(
            (JSONObject) subcategoryObj,
            index,
            j,
            errors
          );
          if (subcategory != null) {
            subcategories.add(subcategory);
          }
        }
      }
      category.setSubcategories(subcategories);
    }

    return category;
  }

  @SuppressWarnings("unchecked")
  private SubcategoryImportDto parseSubcategory(
    final JSONObject json,
    final int categoryIndex,
    final int subcategoryIndex,
    final List<String> errors
  ) {
    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    final String location = String.format(
      "Subcategory at category[%d].subcategories[%d]",
      categoryIndex,
      subcategoryIndex
    );

    final String id = (String) json.get("id");
    if (id == null || id.trim().isEmpty()) {
      errors.add(String.format("%s is missing required field 'id'", location));
      return null;
    }
    subcategory.setId(id);

    final Object titleObj = json.get("title");
    if (titleObj == null) {
      errors.add(
        String.format("%s is missing required field 'title'", location)
      );
      return null;
    }
    subcategory.setTitle(parseI18nProperty((Map<String, Object>) titleObj));

    final String parentId = (String) json.get("parentId");
    if (parentId == null || parentId.trim().isEmpty()) {
      errors.add(
        String.format("%s is missing required field 'parentId'", location)
      );
      return null;
    }
    subcategory.setParentId(parentId);

    final Object sortOrderObj = json.get("sortOrder");
    subcategory.setSortOrder(
      sortOrderObj instanceof Number ? ((Number) sortOrderObj).intValue() : 0
    );

    final Object numberOfArticlesObj = json.get("numberOfArticles");
    subcategory.setNumberOfArticles(
      numberOfArticlesObj instanceof Number
        ? ((Number) numberOfArticlesObj).intValue()
        : 0
    );

    final JSONArray articlesArray = (JSONArray) json.get("articles");
    if (articlesArray != null) {
      final List<ArticleImportDto> articles = new ArrayList<>();
      for (int k = 0; k < articlesArray.size(); k++) {
        final Object articleObj = articlesArray.get(k);
        if (articleObj instanceof JSONObject) {
          final ArticleImportDto article = parseArticle(
            (JSONObject) articleObj,
            categoryIndex,
            subcategoryIndex,
            k,
            errors
          );
          if (article != null) {
            articles.add(article);
          }
        }
      }
      subcategory.setArticles(articles);
    }

    return subcategory;
  }

  @SuppressWarnings("unchecked")
  private ArticleImportDto parseArticle(
    final JSONObject json,
    final int categoryIndex,
    final int subcategoryIndex,
    final int articleIndex,
    final List<String> errors
  ) {
    final ArticleImportDto article = new ArticleImportDto();
    final String location = String.format(
      "Article at category[%d].subcategories[%d].articles[%d]",
      categoryIndex,
      subcategoryIndex,
      articleIndex
    );

    final String id = (String) json.get("id");
    if (id == null || id.trim().isEmpty()) {
      errors.add(String.format("%s is missing required field 'id'", location));
      return null;
    }
    article.setId(id);

    final String parentId = (String) json.get("parentId");
    if (parentId == null || parentId.trim().isEmpty()) {
      errors.add(
        String.format("%s is missing required field 'parentId'", location)
      );
      return null;
    }
    article.setParentId(parentId);

    final Object titleObj = json.get("title");
    if (titleObj == null) {
      errors.add(
        String.format("%s is missing required field 'title'", location)
      );
      return null;
    }
    article.setTitle(parseI18nProperty((Map<String, Object>) titleObj));

    final Object contentObj = json.get("content");
    if (contentObj == null) {
      errors.add(
        String.format("%s is missing required field 'content'", location)
      );
      return null;
    }
    article.setContent(parseI18nProperty((Map<String, Object>) contentObj));

    final Object highlightedObj = json.get("highlighted");
    article.setHighlighted(
      highlightedObj instanceof Boolean ? (Boolean) highlightedObj : false
    );

    final Object sortOrderObj = json.get("sortOrder");
    article.setSortOrder(
      sortOrderObj instanceof Number ? ((Number) sortOrderObj).intValue() : 0
    );

    return article;
  }

  private I18nProperty parseI18nProperty(final Map<String, Object> map) {
    final I18nProperty i18nProperty = new I18nProperty();
    if (map != null) {
      for (final Map.Entry<String, Object> entry : map.entrySet()) {
        if (entry.getValue() != null) {
          i18nProperty.put(entry.getKey(), entry.getValue().toString());
        }
      }
    }
    return i18nProperty;
  }

  @SuppressWarnings("unchecked")
  private ArticleImportDto parseCategoryArticle(
    final JSONObject json,
    final int categoryIndex,
    final int articleIndex,
    final List<String> errors
  ) {
    final ArticleImportDto article = new ArticleImportDto();
    final String location = String.format(
      "Article at category[%d].articles[%d]",
      categoryIndex,
      articleIndex
    );

    final String id = (String) json.get("id");
    if (id == null || id.trim().isEmpty()) {
      errors.add(String.format("%s is missing required field 'id'", location));
      return null;
    }
    article.setId(id);

    final String parentId = (String) json.get("parentId");
    if (parentId == null || parentId.trim().isEmpty()) {
      errors.add(
        String.format("%s is missing required field 'parentId'", location)
      );
      return null;
    }
    article.setParentId(parentId);

    final Object titleObj = json.get("title");
    if (titleObj == null) {
      errors.add(
        String.format("%s is missing required field 'title'", location)
      );
      return null;
    }
    article.setTitle(parseI18nProperty((Map<String, Object>) titleObj));

    final Object contentObj = json.get("content");
    if (contentObj == null) {
      errors.add(
        String.format("%s is missing required field 'content'", location)
      );
      return null;
    }
    article.setContent(parseI18nProperty((Map<String, Object>) contentObj));

    final Object highlightedObj = json.get("highlighted");
    article.setHighlighted(
      highlightedObj instanceof Boolean ? (Boolean) highlightedObj : false
    );

    final Object sortOrderObj = json.get("sortOrder");
    article.setSortOrder(
      sortOrderObj instanceof Number ? ((Number) sortOrderObj).intValue() : 0
    );

    return article;
  }

  private void validateSubcategorySchema(
    SubcategoryImportDto subcategory,
    int categoryIndex,
    int subcategoryIndex,
    List<String> errors
  ) {
    final String location = String.format(
      "Subcategory at category[%d].subcategories[%d]",
      categoryIndex,
      subcategoryIndex
    );

    if (subcategory.getId() == null || subcategory.getId().trim().isEmpty()) {
      errors.add(String.format("%s is missing required field 'id'", location));
    }

    if (subcategory.getTitle() == null) {
      errors.add(
        String.format("%s is missing required field 'title'", location)
      );
    }

    if (
      subcategory.getParentId() == null ||
      subcategory.getParentId().trim().isEmpty()
    ) {
      errors.add(
        String.format("%s is missing required field 'parentId'", location)
      );
    }

    if (subcategory.getArticles() != null) {
      for (int k = 0; k < subcategory.getArticles().size(); k++) {
        final ArticleImportDto article = subcategory.getArticles().get(k);
        validateArticleSchema(
          article,
          categoryIndex,
          subcategoryIndex,
          k,
          errors
        );
      }
    }
  }

  private void validateArticleSchema(
    ArticleImportDto article,
    int categoryIndex,
    int subcategoryIndex,
    int articleIndex,
    List<String> errors
  ) {
    final String location = String.format(
      "Article at category[%d].subcategories[%d].articles[%d]",
      categoryIndex,
      subcategoryIndex,
      articleIndex
    );

    if (article.getId() == null || article.getId().trim().isEmpty()) {
      errors.add(String.format("%s is missing required field 'id'", location));
    }

    if (
      article.getParentId() == null || article.getParentId().trim().isEmpty()
    ) {
      errors.add(
        String.format("%s is missing required field 'parentId'", location)
      );
    }

    if (article.getTitle() == null) {
      errors.add(
        String.format("%s is missing required field 'title'", location)
      );
    }

    if (article.getContent() == null) {
      errors.add(
        String.format("%s is missing required field 'content'", location)
      );
    }
  }

  private void validateI18nProperty(
    I18nProperty i18nProperty,
    String fieldName,
    List<String> errors
  ) {
    if (i18nProperty == null) {
      errors.add(String.format("%s is null", fieldName));
      return;
    }

    if (i18nProperty.isEmpty()) {
      errors.add(String.format("%s has no language translations", fieldName));
    }
  }
}
