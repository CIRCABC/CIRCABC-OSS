package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfNews;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynPropertyType;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.migration.reader.MetadataUtils;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * MetadataReader implementation that reads node properties from the CIRCABC Alfresco repository.
 */
public class AlfrescoMetadataReader implements MetadataReader {

    private static final Log logger = LogFactory.getLog(AlfrescoMetadataReader.class);

    private NodeService nodeService;
    private ContentService contentService;
    private KeywordsService keywordsService;
    private DynamicPropertyService dynamicPropertyService;
    private MultilingualContentService multilingualContentService;

    /** Mapping from keyword NodeRef to sequential export ID, built during setKeywordDefinition */
    private final Map<NodeRef, Integer> keywordNodeRefToId = new HashMap<>();

    /** Base URL of the Alfresco instance, e.g. https://circabc.europa.eu/circabc */
    private String alfrescoBaseUrl = "";

    private String getValidatedBaseUrl() {
        if (alfrescoBaseUrl == null || alfrescoBaseUrl.trim().isEmpty()) {
            throw new IllegalStateException(
                "Property 'circabc.export.alfresco.base.url' is not configured. "
                + "Set it to the full HTTP URL of the Alfresco webapp "
                + "(e.g. http://localhost:8080/circabc) in alfresco-global.properties "
                + "before running an export.");
        }
        return alfrescoBaseUrl;
    }

    @Override
    public void setProperties(final XMLNode node) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(node);
        if (path == null) {
            return;
        }
        try {
            final NodeRef ref = new NodeRef(path);
            if (!nodeService.exists(ref)) {
                return;
            }
            Map<QName, Serializable> props = nodeService.getProperties(ref);
            String name = (String) props.get(ContentModel.PROP_NAME);

            // Set name on NamedNode — use ensureNameUnicity=false since we read
            // the actual name from the repository, no deduplication needed
            if (node instanceof NamedNode && name != null) {
                MetadataUtils.setNamedNodeProperty((NamedNode) node, name, false, logger);
            }

            // Set title/description on TitledNode
            if (node instanceof TitledNode) {
                final Serializable titleProp = props.get(ContentModel.PROP_TITLE);
                final Serializable descProp = props.get(ContentModel.PROP_DESCRIPTION);
                final String owner = (String) props.get(ContentModel.PROP_CREATOR);

                MLText title = null;
                MLText description = null;

                if (titleProp instanceof MLText) {
                    title = (MLText) titleProp;
                } else if (titleProp instanceof String && ((String) titleProp).length() > 0) {
                    title = new MLText(Locale.ENGLISH, (String) titleProp);
                }

                if (descProp instanceof MLText) {
                    description = (MLText) descProp;
                } else if (descProp instanceof String && ((String) descProp).length() > 0) {
                    description = new MLText(Locale.ENGLISH, (String) descProp);
                }

                MetadataUtils.setTitledNodeProperty((TitledNode) node, title, description, owner, logger);
            }

            // Set created/modified/creator/modifier on Node
            if (node instanceof Node) {
                final Date created = (Date) props.get(ContentModel.PROP_CREATED);
                final String creator = (String) props.get(ContentModel.PROP_CREATOR);
                final Date modified = (Date) props.get(ContentModel.PROP_MODIFIED);
                final String modifier = (String) props.get(ContentModel.PROP_MODIFIER);
                MetadataUtils.setNodeProperty((Node) node, created, creator, modified, modifier, logger);
            }

            // Set content URI and version label on ContentNode
            if (node instanceof ContentNode) {
                final ContentData contentData = (ContentData) props.get(ContentModel.PROP_CONTENT);
                if (contentData != null) {
                    final String safeName = (name != null) ? name.replace(" ", "%20") : "content";
                    final String uri = getValidatedBaseUrl() + "/s/api/node/content/"
                            + ref.getStoreRef().getProtocol() + "/"
                            + ref.getStoreRef().getIdentifier() + "/"
                            + ref.getId() + "/"
                            + safeName;
                    final String versionLabel = (String) props.get(ContentModel.PROP_VERSION_LABEL);
                    MetadataUtils.setContentNodeProperty((ContentNode) node, uri,
                            versionLabel != null ? versionLabel : "1.0", logger);
                }
            }

            // Set news properties on InfNews
            if (node instanceof InfNews) {
                final InfNews infNews = (InfNews) node;
                final Serializable newsContent = props.get(CircabcModel.PROP_NEWS_CONTENT);
                if (newsContent != null) { infNews.setNewsContent(newsContent.toString()); }
                final Serializable newsPattern = props.get(CircabcModel.PROP_NEWS_PATTERN);
                if (newsPattern != null) { infNews.setNewsPattern(newsPattern.toString()); }
                final Serializable newsLayout = props.get(CircabcModel.PROP_NEWS_LAYOUT);
                if (newsLayout != null) { infNews.setNewsLayout(newsLayout.toString()); }
                final Serializable newsSize = props.get(CircabcModel.PROP_NEWS_SIZE);
                if (newsSize != null) { infNews.setNewsSize(Integer.valueOf(newsSize.toString())); }
                final Serializable newsDate = props.get(CircabcModel.PROP_NEWS_DATE);
                if (newsDate instanceof Date) { infNews.setNewsDate((Date) newsDate); }
                final Serializable newsUrl = props.get(CircabcModel.PROP_NEWS_URL);
                if (newsUrl != null) { infNews.setNewsUrl(newsUrl.toString()); }
            }

            // Set displayOldInformation on Information
            if (node instanceof Information) {
                final Serializable displayOldInfo = props.get(CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION);
                if (displayOldInfo != null) {
                    ((Information) node).setDisplayOldInformation(
                        new eu.cec.digit.circabc.migration.entities.TypedProperty.DisplayOldInformationProperty(
                            Boolean.valueOf(displayOldInfo.toString())));
                }
            }

            // Set content on Message (forum post body)
            if (node instanceof Message) {
                try {
                    final ContentReader reader = contentService.getReader(ref, ContentModel.PROP_CONTENT);
                    if (reader != null && reader.exists()) {
                        final String messageContent = reader.getContentString();
                        if (messageContent != null && !messageContent.isEmpty()) {
                            ((Message) node).setContent(messageContent);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error reading message content for " + path + ": " + e.getMessage());
                }
            }

            // Set contact information on InterestGroup
            if (node instanceof InterestGroup) {
                final Serializable contactInfo = props.get(CircabcModel.PROP_CONTACT_INFORMATION);
                if (contactInfo != null) {
                    if (contactInfo instanceof MLText) {
                        final MLText mlContact = (MLText) contactInfo;
                        final List<I18NProperty> contactI18n = new ArrayList<>();
                        for (final Map.Entry<Locale, String> entry : mlContact.entrySet()) {
                            contactI18n.add(new I18NProperty(entry.getKey(), entry.getValue()));
                        }
                        ((InterestGroup) node).withI18NContactInfos(contactI18n);
                    } else if (contactInfo instanceof String && !((String)contactInfo).isEmpty()) {
                        ((InterestGroup) node).setContactInfo(
                            new eu.cec.digit.circabc.migration.entities.TypedProperty.ContactInfoProperty(
                                (String) contactInfo));
                    }
                }

                // Set allowApply (canRegisteredApply) - whether the IG is open to new members
                final Serializable canRegisteredApply = props.get(CircabcModel.PROP_CAN_REGISTERED_APPLY);
                if (canRegisteredApply != null) {
                    ((InterestGroup) node).setAllowApply(Boolean.valueOf(canRegisteredApply.toString()));
                }
            }

            // Set pivotLang on MlContent
            if (node instanceof MlContent) {
                Locale pivotLang = null;
                try {
                    final Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(ref);
                    final NodeRef pivotRef = multilingualContentService.getPivotTranslation(ref);
                    if (pivotRef != null) {
                        for (final Map.Entry<Locale, NodeRef> entry : translations.entrySet()) {
                            if (entry.getValue().equals(pivotRef)) {
                                pivotLang = entry.getKey();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error getting pivot language for " + path, e);
                }
                if (pivotLang == null) {
                    final Locale locale = (Locale) props.get(ContentModel.PROP_LOCALE);
                    pivotLang = locale != null ? locale : Locale.ENGLISH;
                }
                MetadataUtils.setMLContentProperty((MlContent) node, 
                        (String) props.get(ContentModel.PROP_CREATOR), null, null, pivotLang, logger);
            }

            // Set pivotLang on InfMLContent (Information service multilingual container)
            if (node instanceof InfMLContent) {
                Locale pivotLang = null;
                try {
                    final Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(ref);
                    final NodeRef pivotRef = multilingualContentService.getPivotTranslation(ref);
                    if (pivotRef != null) {
                        for (final Map.Entry<Locale, NodeRef> entry : translations.entrySet()) {
                            if (entry.getValue().equals(pivotRef)) {
                                pivotLang = entry.getKey();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error getting pivot language for InfMLContent " + path, e);
                }
                if (pivotLang == null) {
                    final Locale locale = (Locale) props.get(ContentModel.PROP_LOCALE);
                    pivotLang = locale != null ? locale : Locale.ENGLISH;
                }
                ((InfMLContent) node).setPivotLang(new TypedProperty.LocaleProperty(pivotLang));
            }

            // Set lang on InformationTranslation
            if (node instanceof InformationTranslation) {
                Locale lang = (Locale) props.get(ContentModel.PROP_LOCALE);
                if (lang == null) {
                    lang = Locale.ENGLISH;
                }
                ((InformationTranslation) node).setLang(new TypedProperty.LocaleProperty(lang));
            }

            // Set lang on InformationTranslationVersion
            if (node instanceof InformationTranslationVersion) {
                Locale lang = (Locale) props.get(ContentModel.PROP_LOCALE);
                if (lang == null) {
                    lang = Locale.ENGLISH;
                }
                ((InformationTranslationVersion) node).setLang(new TypedProperty.LocaleProperty(lang));
            }

            // Set target URL on Url nodes
            if (node instanceof Url) {
                final String target = (String) props.get(DocumentModel.PROP_URL);
                if (target != null && !target.isEmpty()) {
                    ((Url) node).setTarget(new TypedProperty.URLProperty(target));
                }
            }

            // Set expirationDate on Space nodes
            if (node instanceof Space) {
                final Date expirationDate = (Date) props.get(DocumentModel.PROP_EXPIRATION_DATE);
                if (expirationDate != null) {
                    ((Space) node).setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
                }
            }

            // Set lang on LibraryTranslation
            if (node instanceof LibraryTranslation) {
                Locale lang = (Locale) props.get(ContentModel.PROP_LOCALE);
                if (lang == null) {
                    lang = Locale.ENGLISH;
                }
                MetadataUtils.setTranslationProperty((LibraryTranslation) node,
                        (String) props.get(ContentModel.PROP_CREATOR),
                        null, null, null, null, null, null, null, null, null, lang, logger);
            }

            // Set lang on LibraryTranslationVersion
            if (node instanceof LibraryTranslationVersion) {
                Locale lang = (Locale) props.get(ContentModel.PROP_LOCALE);
                if (lang == null) {
                    lang = Locale.ENGLISH;
                }
                MetadataUtils.setTranslationProperty((LibraryTranslationVersion) node,
                        (String) props.get(ContentModel.PROP_CREATOR),
                        null, null, null, null, null, null, null, null, null, lang, logger);
            }

            // Set cProperties (keywords, dynamic properties, security ranking, etc.) on Content nodes
            if (node instanceof Content || node instanceof LibraryContentVersion || node instanceof LibraryTranslation || node instanceof LibraryTranslationVersion) {
                final String securityRanking = (String) props.get(DocumentModel.PROP_SECURITY_RANKING);
                final Date expirationDate = (Date) props.get(DocumentModel.PROP_EXPIRATION_DATE);
                final String author = (String) props.get(ContentModel.PROP_AUTHOR);
                final String status = (String) props.get(DocumentModel.PROP_STATUS);
                final Date issueDate = (Date) props.get(DocumentModel.PROP_ISSUE_DATE);
                final String reference = (String) props.get(DocumentModel.PROP_REFERENCE);

                // Convert keyword NodeRefs to export integer IDs
                List<Integer> keywordIds = null;
                final List<NodeRef> keywordRefs = (List<NodeRef>) DefaultTypeConverter.INSTANCE.getCollection(
                        NodeRef.class, props.get(DocumentModel.PROP_KEYWORD));
                if (keywordRefs != null && !keywordRefs.isEmpty()) {
                    keywordIds = new ArrayList<>();
                    for (final NodeRef kwRef : keywordRefs) {
                        final Integer id = keywordNodeRefToId.get(kwRef);
                        if (id != null) {
                            keywordIds.add(id);
                        }
                    }
                    if (keywordIds.isEmpty()) {
                        keywordIds = null;
                    }
                }

                // Read dynamic property values (1..20)
                final Serializable[] dynProps = new Serializable[20];
                for (int i = 0; i < 20; i++) {
                    dynProps[i] = props.get(DocumentModel.ALL_DYN_PROPS.get(i));
                }

                if (node instanceof Content) {
                    MetadataUtils.setContentProperty((Content) node,
                            securityRanking, expirationDate, author, status, issueDate,
                            reference, keywordIds, dynProps);
                } else if (node instanceof LibraryContentVersion) {
                    MetadataUtils.setContentProperty((LibraryContentVersion) node,
                            securityRanking, expirationDate, author, status, issueDate,
                            reference, keywordIds, dynProps);
                } else if (node instanceof LibraryTranslation) {
                    MetadataUtils.setContentProperty((LibraryTranslation) node,
                            securityRanking, expirationDate, author, status, issueDate,
                            reference, keywordIds, dynProps);
                } else if (node instanceof LibraryTranslationVersion) {
                    MetadataUtils.setContentProperty((LibraryTranslationVersion) node,
                            securityRanking, expirationDate, author, status, issueDate,
                            reference, keywordIds, dynProps);
                }
            }

        } catch (Exception e) {
            throw new ExportationException("Error reading properties for " + path, e);
        }
    }

    @Override
    public void setKeywordDefinition(final InterestGroup interestGroup) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(interestGroup);
        if (path == null) {
            logger.error("setKeywordDefinition: IG exportation path is NULL - keywords will NOT be exported. "
                + "This means the IG NodeRef could not be resolved. Check that the IG name matches in the repository.");
            return;
        }
        try {
            final NodeRef igRef = new NodeRef(path);
            if (!nodeService.exists(igRef)) {
                logger.error("setKeywordDefinition: IG node does not exist: " + path);
                return;
            }
            final List<Keyword> keywords = keywordsService.getKeywords(igRef);
            if (keywords == null || keywords.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("setKeywordDefinition: No keywords found for IG: " + path);
                }
                return;
            }
            logger.info("setKeywordDefinition: Exporting " + keywords.size() + " keywords for IG: " + path);
            KeywordDefinitions definitions = interestGroup.getKeywordDefinitions();
            if (definitions == null) {
                definitions = new KeywordDefinitions();
                interestGroup.withKeywordDefinitions(definitions);
            }
            int keywordIndex = 0;
            for (final Keyword kw : keywords) {
                try {
                    final KeywordDefinition kd = new KeywordDefinition();
                    kd.withId(keywordIndex);
                    keywordNodeRefToId.put(kw.getId(), keywordIndex);
                    keywordIndex++;
                    final MLText mlValues = kw.getMLValues();
                    if (mlValues != null) {
                        for (final Map.Entry<Locale, String> entry : mlValues.entrySet()) {
                            kd.withI18NValues(
                                new eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty()
                                    .withLang(entry.getKey())
                                    .withValue(entry.getValue())
                            );
                        }
                    }
                    definitions.withDefinitions(kd);
                } catch (Exception e) {
                    logger.error("setKeywordDefinition: Error exporting keyword " + kw.getId() + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("setKeywordDefinition: FAILED for IG path=" + path + ": " + e.getMessage(), e);
            throw new ExportationException("Failed to export keywords for IG: " + path, e);
        }
    }

    @Override
    public void setDynamicPropertyDefinition(final InterestGroup interestGroup) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(interestGroup);
        if (path == null) {
            logger.error("setDynamicPropertyDefinition: IG exportation path is NULL - dynamic properties will NOT be exported. "
                + "This means the IG NodeRef could not be resolved. Check that the IG name matches in the repository.");
            return;
        }
        try {
            final NodeRef igRef = new NodeRef(path);
            if (!nodeService.exists(igRef)) {
                logger.error("setDynamicPropertyDefinition: IG node does not exist: " + path);
                return;
            }
            final List<DynamicProperty> dynProps = dynamicPropertyService.getDynamicProperties(igRef);
            if (dynProps == null || dynProps.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("setDynamicPropertyDefinition: No dynamic properties found for IG: " + path);
                }
                return;
            }
            logger.info("setDynamicPropertyDefinition: Exporting " + dynProps.size() + " dynamic properties for IG: " + path);
            DynamicPropertyDefinitions definitions = interestGroup.getDynamicPropertyDefinitions();
            if (definitions == null) {
                definitions = new DynamicPropertyDefinitions();
                interestGroup.withDynamicPropertyDefinitions(definitions);
            }
            for (final DynamicProperty dp : dynProps) {
                try {
                    final DynamicPropertyDefinition dpd = new DynamicPropertyDefinition();
                    dpd.withId(dp.getIndex() != null ? dp.getIndex().intValue() : null);
                    // Map the service DynamicPropertyType to the JAXB DynPropertyType
                    if (dp.getType() != null) {
                        try {
                            dpd.withType(DynPropertyType.fromValue(dp.getType().name()));
                        } catch (IllegalArgumentException e) {
                            logger.warn("setDynamicPropertyDefinition: Unknown type '" + dp.getType().name() + "', defaulting to TEXT_FIELD");
                            dpd.withType(DynPropertyType.TEXT_FIELD);
                        }
                    } else {
                        dpd.withType(DynPropertyType.TEXT_FIELD);
                    }
                    dpd.withValue(dp.getName());
                    // Export selection cases for SELECTION / MULTI_SELECTION types
                    if (dp.isSelectionType() && dp.getListOfValidValues() != null) {
                        dpd.withSelectionCases(dp.getListOfValidValues());
                    }
                    definitions.withDefinitions(dpd);
                } catch (Exception e) {
                    logger.error("setDynamicPropertyDefinition: Error exporting property '" + dp.getName() + "': " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("setDynamicPropertyDefinition: FAILED for IG path=" + path + ": " + e.getMessage(), e);
            throw new ExportationException("Failed to export dynamic properties for IG: " + path, e);
        }
    }

    @Override
    public void setIconsDefinition(final InterestGroup interestGroup) throws ExportationException {
        // Icons are optional customization content - not critical for export
        if (logger.isDebugEnabled()) {
            logger.debug("Icons definition: skipped (optional for CIRCABC export)");
        }
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setKeywordsService(KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
    }

    public void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.dynamicPropertyService = dynamicPropertyService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    public void setAlfrescoBaseUrl(String alfrescoBaseUrl) {
        this.alfrescoBaseUrl = alfrescoBaseUrl;
    }
}
