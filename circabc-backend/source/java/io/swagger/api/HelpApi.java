/**
 *
 */
package io.swagger.api;

import io.swagger.model.HelpArticle;
import io.swagger.model.HelpCategory;
import io.swagger.model.HelpLink;
import io.swagger.model.HelpSearchResult;

import java.io.File;
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
            List<File> attachementsFiles);
}
