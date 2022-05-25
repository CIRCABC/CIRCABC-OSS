/**
 *
 */
package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.joda.time.DateTime;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** @author beaurpi */
public class HelpApiImpl implements HelpApi {

    private static final String FAQS = "faqs";
    private static final String FAQS_LINKS = "faqsLinks";
    private static final String CLOSE_QUERY = " )";
    private static final String OPEN_QUERY = "( ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String PATH = "PATH:";
    private static final String ESCAPE_QUOTES = "\" ";
    private static final String ASPECT_HIGHLIGHTED = "ASPECT:ci\\:helpArticleHighlighted";
    private static final String MODIFIED = "modified";
    private static final String TITLE_QUERY = "@title:";
    private static final String DESCRIPTION_QUERY = "@description:";
    private static final String STAR_QUERY = "*";
    private static final String THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE = "The target node is not a help article";
    private NodeService nodeService;
    private PersonService personService;
    private ManagementService managementService;
    private SearchService searchService;
    private ApiToolBox apiToolBox;
    private EmailApi emailApi;

    @Override
    public List<HelpCategory> getHelpCategories() {
        NodeRef faqsRef = getFaqsRef();

        List<HelpCategory> result = new ArrayList<>();

        if (faqsRef != null) {
            for (ChildAssociationRef child : nodeService.getChildAssocs(faqsRef)) {
                if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_HELP_CATEGORY)) {
                    result.add(getHelpCategory(child.getChildRef().getId()));
                }
            }
        }

        return result;
    }

    private NodeRef getFaqsRef() {
        NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
        return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);
    }

    private NodeRef getFaqLinksRef() {
        NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
        return nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS_LINKS);
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

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

            ChildAssociationRef categRef =
                    nodeService.createNode(
                            faqsRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.ALFRESCO_URI, name),
                            ContentModel.TYPE_FOLDER);
            nodeService.addAspect(categRef.getChildRef(), CircabcModel.ASPECT_HELP_CATEGORY, null);

            nodeService.setProperty(categRef.getChildRef(), ContentModel.PROP_NAME, name);
            nodeService.setProperty(
                    categRef.getChildRef(),
                    ContentModel.PROP_TITLE,
                    Converter.toMLText(helpCategory.getTitle()));

            helpCategory.setId(categRef.getChildRef().getId());
        }

        return helpCategory;
    }

    private String getCleanFileName(String filename) {
        return filename.replaceAll("[\\\\/:*?\"<>|!]", "_");
    }

    @Override
    public HelpCategory getHelpCategory(String id) {

        NodeRef helpCategoryRef = Converter.createNodeRefFromId(id);

        HelpCategory category = new HelpCategory();
        category.setId(helpCategoryRef.getId());

        Serializable title = nodeService.getProperty(helpCategoryRef, ContentModel.PROP_TITLE);
        if (title instanceof String) {
            category.setTitle(Converter.toI18NProperty((String) title));
        } else if (title instanceof MLText) {
            category.setTitle(Converter.toI18NProperty((MLText) title));
        }

        Integer nbArticles = 0;
        for (ChildAssociationRef child : nodeService.getChildAssocs(helpCategoryRef)) {
            if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_HELP_ARTICLE)) {
                nbArticles++;
            }
        }

        category.setNumberOfArticles(nbArticles);

        return category;
    }

    @Override
    public List<HelpArticle> getCategoryArticles(String categoryId, Boolean loadContent) {
        NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

        List<HelpArticle> result = new ArrayList<>();

        for (ChildAssociationRef child : nodeService.getChildAssocs(helpCategoryRef)) {
            if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_HELP_ARTICLE)) {
                result.add(getHelpArticleInternal(child.getChildRef().getId(), loadContent));
            }
        }

        return result;
    }

    @Override
    public HelpArticle getHelpArticle(String id) {
        return getHelpArticleInternal(id, true);
    }

    private HelpArticle getHelpArticleInternal(String id, Boolean loadContent) {

        NodeRef helpArticleRef = Converter.createNodeRefFromId(id);

        if (nodeService.hasAspect(helpArticleRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
            HelpArticle result = new HelpArticle();

            result.setId(helpArticleRef.getId());

            Serializable title = nodeService.getProperty(helpArticleRef, ContentModel.PROP_TITLE);
            if (title instanceof String) {
                result.setTitle(Converter.toI18NProperty((String) title));
            } else if (title instanceof MLText) {
                result.setTitle(Converter.toI18NProperty((MLText) title));
            }

            if (Boolean.TRUE.equals(loadContent)) {
                Serializable description =
                        nodeService.getProperty(helpArticleRef, ContentModel.PROP_DESCRIPTION);
                if (description instanceof String) {
                    result.setContent(Converter.toI18NProperty((String) description));
                } else if (description instanceof MLText) {
                    result.setContent(Converter.toI18NProperty((MLText) description));
                }
            }

            NodeRef usernameRef =
                    personService.getPerson(
                            nodeService.getProperty(helpArticleRef, ContentModel.PROP_MODIFIER).toString());
            String author =
                    nodeService.getProperty(usernameRef, ContentModel.PROP_FIRSTNAME).toString()
                            + " "
                            + nodeService
                            .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
                            .toString()
                            .toUpperCase();
            result.setAuthor(author);

            result.setLastUpdate(
                    new DateTime(nodeService.getProperty(helpArticleRef, ContentModel.PROP_MODIFIED)));

            result.setHighlighted(
                    nodeService.hasAspect(helpArticleRef, CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED));

            result.setParentId(nodeService.getPrimaryParent(helpArticleRef).getParentRef().getId());

            return result;
        }

        return null;
    }

    @Override
    public HelpArticle createHelpArticle(String categoryId, HelpArticle article) {
        NodeRef helpCategoryRef = Converter.createNodeRefFromId(categoryId);

        if (!nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)) {
            throw new InvalidArgumentException("The target node is not a help category");
        }

        MLText title = Converter.toMLText(article.getTitle());
        String name = title.getDefaultValue();

        name = getCleanFileName(name);

        ChildAssociationRef articleRef =
                nodeService.createNode(
                        helpCategoryRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.ALFRESCO_URI, name),
                        ContentModel.TYPE_CONTENT);
        nodeService.addAspect(articleRef.getChildRef(), CircabcModel.ASPECT_HELP_ARTICLE, null);

        nodeService.setProperty(articleRef.getChildRef(), ContentModel.PROP_NAME, name);
        nodeService.setProperty(
                articleRef.getChildRef(), ContentModel.PROP_TITLE, Converter.toMLText(article.getTitle()));
        nodeService.setProperty(
                articleRef.getChildRef(),
                ContentModel.PROP_DESCRIPTION,
                Converter.toMLText(article.getContent()));

        article.setId(articleRef.getChildRef().getId());

        NodeRef usernameRef =
                personService.getPerson(
                        nodeService
                                .getProperty(articleRef.getChildRef(), ContentModel.PROP_CREATOR)
                                .toString());
        String author =
                nodeService.getProperty(usernameRef, ContentModel.PROP_FIRSTNAME).toString()
                        + " "
                        + nodeService
                        .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
                        .toString()
                        .toUpperCase();
        article.setAuthor(author);

        article.setLastUpdate(
                new DateTime(
                        nodeService.getProperty(articleRef.getChildRef(), ContentModel.PROP_MODIFIED)));

        return article;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void deleteHelpArticle(String id) {
        NodeRef helpArticleRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(helpArticleRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
            throw new InvalidArgumentException(THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE);
        }

        nodeService.deleteNode(helpArticleRef);
    }

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
                articleRef, ContentModel.PROP_TITLE, Converter.toMLText(article.getTitle()));
        nodeService.setProperty(
                articleRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(article.getContent()));

        NodeRef usernameRef =
                personService.getPerson(
                        nodeService.getProperty(articleRef, ContentModel.PROP_CREATOR).toString());
        String author =
                nodeService.getProperty(usernameRef, ContentModel.PROP_FIRSTNAME).toString()
                        + " "
                        + nodeService
                        .getProperty(usernameRef, ContentModel.PROP_LASTNAME)
                        .toString()
                        .toUpperCase();
        article.setAuthor(author);

        article.setLastUpdate(
                new DateTime(nodeService.getProperty(articleRef, ContentModel.PROP_MODIFIED)));

        return article;
    }

    @Override
    public void deleteHelpCategory(String id) {
        NodeRef helpCategoryRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(helpCategoryRef, CircabcModel.ASPECT_HELP_CATEGORY)) {
            throw new InvalidArgumentException("The target node is not a help category");
        }

        nodeService.deleteNode(helpCategoryRef);
    }

    @Override
    public HelpCategory updateHelpCategory(String id, HelpCategory category) {

        NodeRef categoryRefRef = Converter.createNodeRefFromId(id);

        if (category.getTitle() != null) {

            nodeService.setProperty(
                    categoryRefRef, ContentModel.PROP_TITLE, Converter.toMLText(category.getTitle()));
        }

        return category;
    }

    @Override
    public HelpArticle toggleHighlightArticle(String id) {
        NodeRef articleRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(articleRef)) {
            throw new InvalidArgumentException("The target node is not existing");
        }

        if (!nodeService.hasAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
            throw new InvalidArgumentException(THE_TARGET_NODE_IS_NOT_A_HELP_ARTICLE);
        }

        if (!nodeService.hasAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED)) {
            nodeService.addAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED, null);
        } else {
            nodeService.removeAspect(articleRef, CircabcModel.ASPECT_HELP_ARTICLE_HIGHLIGHTED);
        }

        return getHelpArticle(id);
    }

    @Override
    public List<HelpArticle> getHighlightedArticles() {

        String query = buildHighlightedQuery();

        List<HelpArticle> result = new ArrayList<>();

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(Repository.getStoreRef());
        sp.addSort(MODIFIED, true);

        ResultSet rs = searchService.query(sp);
        List<NodeRef> nodeRefs = rs.getNodeRefs();

        for (NodeRef articleRef : nodeRefs) {
            result.add(getHelpArticle(articleRef.getId()));
        }

        return result;
    }

    private String buildHighlightedQuery() {
        NodeRef faqsRef = getFaqsRef();

        return OPEN_QUERY
                + PATH
                + ESCAPE_QUOTES
                + apiToolBox.getPathFromSpaceRef(faqsRef, true)
                + ESCAPE_QUOTES
                + CLOSE_QUERY
                + AND
                + OPEN_QUERY
                + ASPECT_HIGHLIGHTED
                + CLOSE_QUERY;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    @Override
    public List<HelpLink> getHelpLinks() {

        List<HelpLink> result = new ArrayList<>();
        NodeRef faqLinksRef = getFaqLinksRef();

        if (faqLinksRef != null) {
            for (ChildAssociationRef child : nodeService.getChildAssocs(faqLinksRef)) {
                if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_HELP_LINK)) {
                    result.add(getHelpLink(child.getChildRef().getId()));
                }
            }
        }

        return result;
    }

    @Override
    public HelpLink getHelpLink(String id) {

        HelpLink result;

        NodeRef helpLinkRef = Converter.createNodeRefFromId(id);

        result = new HelpLink();
        result.setId(id);

        Object titleObj = nodeService.getProperty(helpLinkRef, ContentModel.PROP_TITLE);
        if (titleObj instanceof String) {
            result.setTitle(Converter.toI18NProperty((String) titleObj));
        } else if (titleObj instanceof MLText) {
            result.setTitle(Converter.toI18NProperty((MLText) titleObj));
        }

        Serializable href = nodeService.getProperty(helpLinkRef, CircabcModel.PROP_HELP_LINK_HREF);
        if (href != null) {
            result.setHref(href.toString());
        }

        return result;
    }

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

            ChildAssociationRef linkRef =
                    nodeService.createNode(
                            faqLinksRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.ALFRESCO_URI, name),
                            ContentModel.TYPE_CONTENT);
            nodeService.addAspect(linkRef.getChildRef(), CircabcModel.ASPECT_HELP_LINK, null);

            nodeService.setProperty(linkRef.getChildRef(), ContentModel.PROP_NAME, name);
            nodeService.setProperty(
                    linkRef.getChildRef(), ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
            nodeService.setProperty(
                    linkRef.getChildRef(), CircabcModel.PROP_HELP_LINK_HREF, body.getHref());

            body.setId(linkRef.getChildRef().getId());
        }

        return body;
    }

    @Override
    public HelpLink updateHelpLink(HelpLink body) {
        if (body.getTitle() != null && body.getId() != null) {

            NodeRef linkRef = Converter.createNodeRefFromId(body.getId());

            if (nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)) {

                nodeService.setProperty(
                        linkRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
                nodeService.setProperty(linkRef, CircabcModel.PROP_HELP_LINK_HREF, body.getHref());
            }
        }

        return body;
    }

    @Override
    public void deleteHelpLink(String id) {

        NodeRef linkRef = Converter.createNodeRefFromId(id);

        if (nodeService.hasAspect(linkRef, CircabcModel.ASPECT_HELP_LINK)) {
            nodeService.deleteNode(linkRef);
        }
    }

    @Override
    public HelpSearchResult searchHelp(String query) {

        HelpSearchResult result = new HelpSearchResult();

        String faqsQuery = buildSearchFaqsQuery(query);

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(faqsQuery);
        sp.addStore(Repository.getStoreRef());
        sp.addSort(MODIFIED, true);

        ResultSet rs = searchService.query(sp);
        List<NodeRef> nodeRefs = rs.getNodeRefs();

        for (NodeRef nodeRef : nodeRefs) {
            if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_HELP_CATEGORY)) {
                result.getCategories().add(getHelpCategory(nodeRef.getId()));
            } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_HELP_ARTICLE)) {
                result.getArticles().add(getHelpArticle(nodeRef.getId()));
            }
        }

        String faqsLinksQuery = buildSearchFaqsLinksQuery(query);

        final SearchParameters spl = new SearchParameters();
        spl.setLanguage(SearchService.LANGUAGE_LUCENE);
        spl.setQuery(faqsLinksQuery);
        spl.addStore(Repository.getStoreRef());
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
                    queryBuild.append(TITLE_QUERY).append(STAR_QUERY).append(parts[i]).append(STAR_QUERY);
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
                    queryBuild.append(TITLE_QUERY).append(STAR_QUERY).append(parts[i]).append(STAR_QUERY);
                    if (i < parts.length - 1) {
                        queryBuild.append(OR);
                    }
                }
            }
        }

        queryBuild.append(CLOSE_QUERY);

        return queryBuild.toString();
    }

    /** @return the emailApi */
    public EmailApi getEmailApi() {
        return emailApi;
    }

    /** @param emailApi the emailApi to set */
    public void setEmailApi(EmailApi emailApi) {
        this.emailApi = emailApi;
    }

    @Override
    public void contactSupport(
            String reason,
            String name,
            String emailFrom,
            String subject,
            String content,
            List<File> attachementsFiles) {

        EmailDefinition emailDefinition =
                emailApi.prepareEmailForHelpdeskContact(reason, name, emailFrom, subject, content);
        emailDefinition.setCopyToSender(true);
        emailApi.mailPost(emailDefinition, attachementsFiles);

        EmailDefinition emailConfirmation =
                emailApi.prepareConfirmationForHelpdeskContact(reason, name, emailFrom, subject, content);
        emailApi.mailPost(emailConfirmation);
    }
}
