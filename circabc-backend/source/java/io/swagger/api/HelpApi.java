/**
 *
 */
package io.swagger.api;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import io.swagger.model.HelpSubcategory;
import io.swagger.model.ImportResult;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/** @author beaurpi */
public interface HelpApi {
  List<HelpCategory> getHelpCategories();

  HelpCategory createHelpCategory(HelpCategory helpCategory);

  HelpCategory getHelpCategory(String id);

  List<HelpArticle> getCategoryArticles(String categoryId, Boolean loadContent);

  HelpArticle getHelpArticle(String id);

  HelpArticle createHelpArticle(String categoryId, HelpArticle article);

  void deleteHelpArticle(String id);

  HelpArticle updateHelpArticle(String id, HelpArticle article);

  void deleteHelpCategory(String id);

  HelpCategory updateHelpCategory(String id, HelpCategory category);

  HelpArticle toggleHighlightArticle(String id);

  List<HelpArticle> getHighlightedArticles();

  List<HelpLink> getHelpLinks();

  HelpLink getHelpLink(String id);

  HelpLink createHelpLink(HelpLink body);

  HelpLink updateHelpLink(HelpLink body);

  void deleteHelpLink(String id);

  HelpSearchResult searchHelp(String query);

  void contactSupport(
    String reason,
    String name,
    String email,
    String subject,
    String content,
    List<File> attachementsFiles
  ) throws Exception;

  void reorderCategoryArticles(String categoryId, List<String> articleIds);

  void reorderCategories(List<String> categoryIds);

  // Subcategories
  List<HelpSubcategory> getCategorySubcategories(String categoryId);

  HelpSubcategory createHelpSubcategory(
    String categoryId,
    HelpSubcategory subcategory
  );

  HelpSubcategory getHelpSubcategory(String id);

  HelpSubcategory updateHelpSubcategory(String id, HelpSubcategory subcategory);

  void deleteHelpSubcategory(String id);

  void reorderCategorySubcategories(
    String categoryId,
    List<String> subcategoryIds
  );

  // Articles within subcategories
  List<HelpArticle> getSubcategoryArticles(
    String subcategoryId,
    Boolean loadContent
  );

  HelpArticle createSubcategoryArticle(
    String subcategoryId,
    HelpArticle article
  );

  void reorderSubcategoryArticles(
    String subcategoryId,
    List<String> articleIds
  );

  /**
   * Exports the complete FAQ structure to JSON format.
   *
   * @return JSON string containing all categories with subcategories and articles
   * @throws Exception if export fails
   */
  String exportFaq() throws Exception;

  /**
   * Imports FAQ structure from a JSON file.
   *
   * @param file MultipartFile containing JSON FAQ structure
   * @return ImportResult with statistics about the import operation
   * @throws Exception if import fails
   */
  ImportResult importFaq(InputStream inputStream, String fileName)
    throws Exception;
}
