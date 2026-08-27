package io.swagger.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.swagger.exception.ValidationException;
import io.swagger.model.ArticleImportDto;
import io.swagger.model.CategoryImportDto;
import io.swagger.model.I18nProperty;
import io.swagger.model.SubcategoryImportDto;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FaqValidationServiceTest {

  private FaqValidationService validationService;

  @Before
  public void setUp() {
    validationService = new FaqValidationService();
  }

  @Test
  public void shouldValidateValidJsonSchema() throws ValidationException {
    final String validJson =
      "[{\"id\":\"cat1\",\"title\":{\"en\":\"Category 1\"},\"numberOfArticles\":1," +
      "\"subcategories\":[{\"id\":\"sub1\",\"title\":{\"en\":\"Subcategory 1\"}," +
      "\"sortOrder\":1,\"parentId\":\"cat1\",\"numberOfArticles\":1," +
      "\"articles\":[{\"id\":\"art1\",\"parentId\":\"sub1\"," +
      "\"title\":{\"en\":\"Article 1\"},\"content\":{\"en\":\"Content 1\"}," +
      "\"highlighted\":true,\"sortOrder\":1}]}]}]";

    final List<CategoryImportDto> categories = validationService.validateSchema(
      validJson
    );

    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals("cat1", categories.get(0).getId());
  }

  @Test
  public void shouldRejectNullJsonContent() {
    try {
      validationService.validateSchema(null);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(e.getValidationErrors().get(0).contains("empty or null"));
    }
  }

  @Test
  public void shouldRejectEmptyJsonContent() {
    try {
      validationService.validateSchema("");
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(e.getValidationErrors().get(0).contains("empty or null"));
    }
  }

  @Test
  public void shouldRejectInvalidJsonFormat() {
    try {
      validationService.validateSchema("{invalid json");
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e.getValidationErrors().get(0).contains("Invalid JSON format")
      );
    }
  }

  @Test
  public void shouldRejectCategoryMissingId() {
    final String jsonMissingId =
      "[{\"title\":{\"en\":\"Category 1\"},\"numberOfArticles\":0,\"subcategories\":[]}]";

    try {
      validationService.validateSchema(jsonMissingId);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e.getValidationErrors().get(0).contains("missing required field 'id'")
      );
    }
  }

  @Test
  public void shouldRejectCategoryMissingTitle() {
    final String jsonMissingTitle =
      "[{\"id\":\"cat1\",\"numberOfArticles\":0,\"subcategories\":[]}]";

    try {
      validationService.validateSchema(jsonMissingTitle);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e
          .getValidationErrors()
          .get(0)
          .contains("missing required field 'title'")
      );
    }
  }

  @Test
  public void shouldRejectSubcategoryMissingParentId() {
    final String jsonMissingParentId =
      "[{\"id\":\"cat1\",\"title\":{\"en\":\"Category 1\"},\"numberOfArticles\":0," +
      "\"subcategories\":[{\"id\":\"sub1\",\"title\":{\"en\":\"Subcategory 1\"}," +
      "\"sortOrder\":1,\"numberOfArticles\":0,\"articles\":[]}]}]";

    try {
      validationService.validateSchema(jsonMissingParentId);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e
          .getValidationErrors()
          .get(0)
          .contains("missing required field 'parentId'")
      );
    }
  }

  @Test
  public void shouldRejectArticleMissingParentId() {
    final String jsonMissingParentId =
      "[{\"id\":\"cat1\",\"title\":{\"en\":\"Category 1\"},\"numberOfArticles\":1," +
      "\"subcategories\":[{\"id\":\"sub1\",\"title\":{\"en\":\"Subcategory 1\"}," +
      "\"sortOrder\":1,\"parentId\":\"cat1\",\"numberOfArticles\":1," +
      "\"articles\":[{\"id\":\"art1\",\"title\":{\"en\":\"Article 1\"}," +
      "\"content\":{\"en\":\"Content 1\"},\"highlighted\":true,\"sortOrder\":1}]}]}]";

    try {
      validationService.validateSchema(jsonMissingParentId);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e
          .getValidationErrors()
          .get(0)
          .contains("missing required field 'parentId'")
      );
    }
  }

  @Test
  public void shouldValidateReferentialIntegrityForValidData()
    throws ValidationException {
    final List<CategoryImportDto> categories = createValidCategoryList();

    validationService.validateReferentialIntegrity(categories);
    // No exception means success
  }

  @Test
  public void shouldRejectInvalidSubcategoryParentReference() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setParentId("nonexistent-category");

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);

    try {
      validationService.validateReferentialIntegrity(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e
          .getValidationErrors()
          .get(0)
          .contains("references non-existent category")
      );
    }
  }

  @Test
  public void shouldRejectInvalidArticleParentReference() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setParentId("cat1");

    final ArticleImportDto article = new ArticleImportDto();
    article.setId("art1");
    article.setParentId("nonexistent-subcategory");

    final List<ArticleImportDto> articles = new ArrayList<>();
    articles.add(article);
    subcategory.setArticles(articles);

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);

    try {
      validationService.validateReferentialIntegrity(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e
          .getValidationErrors()
          .get(0)
          .contains("references non-existent subcategory")
      );
    }
  }

  @Test
  public void shouldValidateDataTypesForValidData() throws ValidationException {
    final List<CategoryImportDto> categories = createValidCategoryList();

    validationService.validateDataTypes(categories);
    // No exception means success
  }

  @Test
  public void shouldRejectNullSortOrderInSubcategory() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");
    category.setTitle(new I18nProperty("en", "Category 1"));

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setTitle(new I18nProperty("en", "Subcategory 1"));
    subcategory.setSortOrder(null);

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);

    try {
      validationService.validateDataTypes(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(e.getValidationErrors().get(0).contains("null sortOrder"));
    }
  }

  @Test
  public void shouldRejectNullSortOrderInArticle() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");
    category.setTitle(new I18nProperty("en", "Category 1"));

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setTitle(new I18nProperty("en", "Subcategory 1"));
    subcategory.setSortOrder(1);

    final ArticleImportDto article = new ArticleImportDto();
    article.setId("art1");
    article.setTitle(new I18nProperty("en", "Article 1"));
    article.setContent(new I18nProperty("en", "Content 1"));
    article.setHighlighted(true);
    article.setSortOrder(null);

    final List<ArticleImportDto> articles = new ArrayList<>();
    articles.add(article);
    subcategory.setArticles(articles);

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);

    try {
      validationService.validateDataTypes(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(e.getValidationErrors().get(0).contains("null sortOrder"));
    }
  }

  @Test
  public void shouldRejectNullHighlightedFlag() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");
    category.setTitle(new I18nProperty("en", "Category 1"));

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setTitle(new I18nProperty("en", "Subcategory 1"));
    subcategory.setSortOrder(1);

    final ArticleImportDto article = new ArticleImportDto();
    article.setId("art1");
    article.setTitle(new I18nProperty("en", "Article 1"));
    article.setContent(new I18nProperty("en", "Content 1"));
    article.setHighlighted(null);
    article.setSortOrder(1);

    final List<ArticleImportDto> articles = new ArrayList<>();
    articles.add(article);
    subcategory.setArticles(articles);

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);

    try {
      validationService.validateDataTypes(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e.getValidationErrors().get(0).contains("null highlighted flag")
      );
    }
  }

  @Test
  public void shouldRejectEmptyI18nProperty() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");
    category.setTitle(new I18nProperty());

    categories.add(category);

    try {
      validationService.validateDataTypes(categories);
      fail("Expected ValidationException");
    } catch (ValidationException e) {
      assertTrue(
        e.getValidationErrors().get(0).contains("no language translations")
      );
    }
  }

  private List<CategoryImportDto> createValidCategoryList() {
    final List<CategoryImportDto> categories = new ArrayList<>();
    final CategoryImportDto category = new CategoryImportDto();
    category.setId("cat1");
    category.setTitle(new I18nProperty("en", "Category 1"));
    category.setNumberOfArticles(1);

    final SubcategoryImportDto subcategory = new SubcategoryImportDto();
    subcategory.setId("sub1");
    subcategory.setTitle(new I18nProperty("en", "Subcategory 1"));
    subcategory.setSortOrder(1);
    subcategory.setParentId("cat1");
    subcategory.setNumberOfArticles(1);

    final ArticleImportDto article = new ArticleImportDto();
    article.setId("art1");
    article.setParentId("sub1");
    article.setTitle(new I18nProperty("en", "Article 1"));
    article.setContent(new I18nProperty("en", "Content 1"));
    article.setHighlighted(true);
    article.setSortOrder(1);

    final List<ArticleImportDto> articles = new ArrayList<>();
    articles.add(article);
    subcategory.setArticles(articles);

    final List<SubcategoryImportDto> subcategories = new ArrayList<>();
    subcategories.add(subcategory);
    category.setSubcategories(subcategories);

    categories.add(category);
    return categories;
  }
}
