/**
 *
 */
package io.swagger.api;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Business-logic contract for the CIRCABC online Help subsystem.
 *
 * <p>This interface defines the operations exposed by the Help REST layer and consumed by the
 * Alfresco webscript endpoints. It covers management and retrieval of the three Help building
 * blocks:
 *
 * <ul>
 *   <li><b>Help categories</b> ({@link HelpCategory}) — top-level groupings of help content.
 *   <li><b>Help articles</b> ({@link HelpArticle}) — individual help entries that belong to a
 *       category and can optionally be highlighted.
 *   <li><b>Help links</b> ({@link HelpLink}) — external or related links surfaced in the Help
 *       area.
 * </ul>
 *
 * <p>In addition it provides full-text search over the help content and a support-contact
 * operation that forwards a user request (optionally with attachments) to the support team.
 *
 * <p>Implementations contain the actual logic; webscript endpoints inject this interface and
 * delegate to it.
 *
 * @author beaurpi
 */
public interface HelpApi {
  /**
   * Returns all available help categories.
   *
   * @return the list of all {@link HelpCategory} entries
   */
  List<HelpCategory> getHelpCategories();

  /**
   * Creates a new help category.
   *
   * @param helpCategory the category to create
   * @return the created {@link HelpCategory}, including any server-generated fields
   */
  HelpCategory createHelpCategory(HelpCategory helpCategory);

  /**
   * Retrieves a single help category by its identifier.
   *
   * @param id the identifier of the category to retrieve
   * @return the matching {@link HelpCategory}
   */
  HelpCategory getHelpCategory(String id);

  /**
   * Returns the articles belonging to the given category.
   *
   * @param categoryId the identifier of the parent category
   * @param loadContent when {@code true}, the full article content is loaded; when {@code false},
   *     only metadata is returned
   * @return the list of {@link HelpArticle} entries in the category
   */
  List<HelpArticle> getCategoryArticles(String categoryId, Boolean loadContent);

  /**
   * Retrieves a single help article by its identifier.
   *
   * @param id the identifier of the article to retrieve
   * @return the matching {@link HelpArticle}
   */
  HelpArticle getHelpArticle(String id);

  /**
   * Creates a new help article within the given category.
   *
   * @param categoryId the identifier of the parent category
   * @param article the article to create
   * @return the created {@link HelpArticle}, including any server-generated fields
   */
  HelpArticle createHelpArticle(String categoryId, HelpArticle article);

  /**
   * Deletes the help article with the given identifier.
   *
   * @param id the identifier of the article to delete
   */
  void deleteHelpArticle(String id);

  /**
   * Updates an existing help article.
   *
   * @param id the identifier of the article to update
   * @param article the new article state to apply
   * @return the updated {@link HelpArticle}
   */
  HelpArticle updateHelpArticle(String id, HelpArticle article);

  /**
   * Deletes the help category with the given identifier.
   *
   * @param id the identifier of the category to delete
   */
  void deleteHelpCategory(String id);

  /**
   * Updates an existing help category.
   *
   * @param id the identifier of the category to update
   * @param category the new category state to apply
   * @return the updated {@link HelpCategory}
   */
  HelpCategory updateHelpCategory(String id, HelpCategory category);

  /**
   * Toggles the highlighted state of the given article.
   *
   * @param id the identifier of the article whose highlight flag should be toggled
   * @return the {@link HelpArticle} with its updated highlight state
   */
  HelpArticle toggleHighlightArticle(String id);

  /**
   * Returns all articles that are currently highlighted.
   *
   * @return the list of highlighted {@link HelpArticle} entries
   */
  List<HelpArticle> getHighlightedArticles();

  /**
   * Returns all help links.
   *
   * @return the list of all {@link HelpLink} entries
   */
  List<HelpLink> getHelpLinks();

  /**
   * Retrieves a single help link by its identifier.
   *
   * @param id the identifier of the link to retrieve
   * @return the matching {@link HelpLink}
   */
  HelpLink getHelpLink(String id);

  /**
   * Creates a new help link.
   *
   * @param body the link to create
   * @return the created {@link HelpLink}, including any server-generated fields
   */
  HelpLink createHelpLink(HelpLink body);

  /**
   * Updates an existing help link.
   *
   * @param body the link state to apply; the target link is identified by the body's identifier
   * @return the updated {@link HelpLink}
   */
  HelpLink updateHelpLink(HelpLink body);

  /**
   * Deletes the help link with the given identifier.
   *
   * @param id the identifier of the link to delete
   */
  void deleteHelpLink(String id);

  /**
   * Performs a full-text search across the help content.
   *
   * @param query the search query text
   * @return a {@link HelpSearchResult} holding the matching help content
   */
  HelpSearchResult searchHelp(String query);

  /**
   * Submits a support-contact request, forwarding it (typically by e-mail) to the support team.
   *
   * @param reason the reason/category of the support request
   * @param name the name of the requester
   * @param email the e-mail address of the requester
   * @param subject the subject of the request
   * @param content the body/message of the request
   * @param attachementsFiles the optional files to attach to the request
   * @throws IOException if the request or its attachments cannot be processed or sent
   */
  void contactSupport(
    String reason,
    String name,
    String email,
    String subject,
    String content,
    List<File> attachementsFiles
  ) throws IOException;
}
