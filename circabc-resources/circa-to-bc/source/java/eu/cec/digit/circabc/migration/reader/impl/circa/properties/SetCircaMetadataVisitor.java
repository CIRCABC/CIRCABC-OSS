/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.properties;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatorProperty;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.adapter.DateAdapterUtils;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfSpace;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynPropertyType;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.processor.common.NNTPResourceHandler;
import eu.cec.digit.circabc.migration.reader.MetadataUtils;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.CircaClientsRegistry;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.DbClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.DbFileLocations;
import eu.cec.digit.circabc.migration.reader.impl.circa.dao.CircaDaoFactory;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Document;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Global;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.News;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.NewsLinguistic;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Section;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.SectionLinguistic;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaHomePageReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaIGConfigReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.nntp.NNTPClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.util.ParsedPath;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.migration.validation.impl.NodeReferencesValidator;
import eu.cec.digit.circabc.migration.walker.XMLNodesVisitor;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.version.CustomLabelAwareVersionServiceImpl;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Apply the properties to Circa nodes using the node visitor pattern.
 *
 * @author Yanick Pignot
 */
public class SetCircaMetadataVisitor implements XMLNodesVisitor
{
    private static final String DISCUSSION_FOR = "{0} discussion";
    private static final String NG_MODERATOR = "NG Moderator";

    private static final String INFORMATION_NO_LOCALE = "XX";

    private static final Pattern REGEX_ENTRY_EXT = Pattern.compile("(\\/\\.EX\\/[A-Z]{2})(.*)");
    private static final Pattern REGEX_ENTRY_LING = Pattern.compile("(\\/XX\\/[A-Z]{2}\\/)(.*)");

    private static final Log logger = LogFactory.getLog(SetCircaMetadataVisitor.class);

    private static final String LINK_TO_PREFIX = "Link to ";
    private static final String ML_CONTENT_OF_PREFIX = "ML Content of ";

    private final DateFormat DYN_PROP_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private final NNTPResourceHandler nntpResourceHandler = new NNTPResourceHandler();

    private final FileClient fileClient;
    private final CircaDaoFactory daoFactory;
    private final CircaHomePageReader homePageReader;
    private final NNTPClient nntpClient;
    private final String circaDomainPrefix;
    private final CircaIGConfigReader circaIGConfigReader;
    private final UserReader userReader;
    private final DbClient dbClient;

    private static final String IG_CONTACT_INFO_FOLDER = ".contact.html";
    private static final String IG_TITLE_FOLDER = ".title";
    private static final String IG_DESCRIPTION_FOLDER = ".description";

    private static final String BACKUP_EXTENSION = ".backup";
    private static final String NA_JOCKER = "N/A";

    private static final String DOT_HTML = ".html";

    private static final String KEYWORDS_SEPARATOR = ",";

    /**
     * Create an instance with the required depecies
     *
     * @param fileClient
     * @param userReader
     * @param daoFactory
     * @param homePageReader
     * @param circaDomainPrefix
     */
    public SetCircaMetadataVisitor(final String circaDomainPrefix, final CircaClientsRegistry registry)
    {
        this.fileClient = registry.getFileClient();
        this.daoFactory = registry.getDaoFactory();
        this.circaDomainPrefix = circaDomainPrefix;
        this.homePageReader = registry.getHomePageReader();
        this.nntpClient = registry.getNntpClient();
        this.circaIGConfigReader = registry.getConfigReader();
        this.userReader = registry.getUserReader();
        this.dbClient = registry.getDbClient();
    }

    // no property for these nodes in circa
    public void visit(final Circabc circabc) throws Exception {}
    public void visit(final CategoryHeader header) throws Exception { }
    public void visit(final Surveys survey) throws Exception{ }
    public void visit(final Library library) throws Exception{}
    public void visit(final Directory directory) throws Exception{}
    public void visit(final Newsgroups newsgroup) throws Exception{}
    public void visit(final Events eventRoot) throws Exception{}

    // not exists in circa
    public void visit(final InformationContentVersion contentNode) throws Exception{}
    public void visit(final InformationTranslation contentNode) throws Exception{}
    public void visit(final InformationTranslationVersion contentNode) throws Exception{}
    public void visit(final InfMLContent mlcontent) throws Exception{}

    public void visit(final InterestGroup interestGroup) throws Exception
    {
        final String igPath = ElementsHelper.getExportationPath(interestGroup);

        // circa define only three metadata for an interest group
        MetadataUtils.setTitledNodeProperty(interestGroup,
                convertFilesToMLText(igPath + FileClient.PATH_SEPARATOR + IG_TITLE_FOLDER),
                convertFilesToMLText(igPath + FileClient.PATH_SEPARATOR + IG_DESCRIPTION_FOLDER),
                null, logger);

        MetadataUtils.setInterestGroupProperty(interestGroup,
                convertFilesToMLText(igPath + FileClient.PATH_SEPARATOR + IG_CONTACT_INFO_FOLDER), logger);

    }

    public void visit(final Category category) throws Exception
    {
        final String name = category.getName().getValue().toString();
        final String title = homePageReader.getCategoryTitle(name);

        if(logger.isDebugEnabled())
        {
            logger.debug("  ---  with name:        " + name);
            logger.debug("  ---  with title:       " + title);
        }

        category.setTitle(new TypedProperty.TitleProperty(title));
    }

    public void visit(final Content contentNode) throws Exception
    {
        final Document document = retreiveDocument(contentNode);

        if(document != null)
        {
            MetadataUtils.setNodeProperty(contentNode, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(contentNode, computeAlternative(document, contentNode), logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), document.getVersion(), logger);
            MetadataUtils.setContentProperty(contentNode, computeSecurityRanking(document.getAvailability()), computeDate(document.getExpiration()), computeCreator(document.getCreator(), document.getOwner()), computeStatus(document.getStatus()), computeDate(document.getIssued()), document.getReference(), computeKeywords(contentNode, document.getSubject()), computeDynAttr(contentNode, document.getDynAttribute1(), 1) , computeDynAttr(contentNode, document.getDynAttribute2(), 2), computeDynAttr(contentNode, document.getDynAttribute3(), 3), computeDynAttr(contentNode, document.getDynAttribute4(), 4), computeDynAttr(contentNode, document.getDynAttribute5(), 5), logger);
        }
        else
        {
            final String exportationPath = ElementsHelper.getExportationPath(contentNode);
            final String fileName = FilePathUtils.retreiveParentName(exportationPath);
            final String documentName = FilePathUtils.computeArbitraryNameWithExtension(fileName);

            MetadataUtils.setNamedNodeProperty(contentNode, documentName, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, documentName), null, null, logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), computeVersionFromPath(exportationPath), logger);

            logger.warn("Document " + exportationPath + " not found in database. Impossible to set common properties!. The name will be " + documentName);
        }
    }

    public void visit(final InfContent contentNode) throws Exception
    {
        final Global document = retreiveGlobalDocument(contentNode);

        if(document == null)
        {
            // document can be null if the target is not an entry point (no property setted)
            final String filePath = ElementsHelper.getExportationPath(contentNode);
            final String fileName = FilePathUtils.retreiveFileName(filePath);
            MetadataUtils.setNamedNodeProperty(contentNode, fileName, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, fileName), null, null, logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), "1.0", logger);
        }
        else
        {
            final String language = (document.getLanguage().equals(INFORMATION_NO_LOCALE)) ? FilePathUtils.DEFAULT_LANGUAGE : document.getLanguage();
            MetadataUtils.setNodeProperty(contentNode, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(language, document.getTitle()), singleMLText(language, document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(contentNode, computeAlternative(document, contentNode), logger);
            // no version data for information content in circa
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), "1.0", logger);
        }
    }

    public void visit(final InfSpace space) throws Exception
    {
        // no database entry for an information service space
        MetadataUtils.setNamedNodeProperty(space, FilePathUtils.retreiveFileName(ElementsHelper.getExportationPath(space)), logger);
    }

    public void visit(final LibraryContentVersion contentNode) throws Exception
    {
        final Document document = retreiveDocument(contentNode);

        if(document != null)
        {
            MetadataUtils.setNodeProperty(contentNode, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(contentNode, computeAlternative(document, contentNode), logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), document.getVersion(), logger);
            MetadataUtils.setContentProperty(contentNode, computeSecurityRanking(document.getAvailability()), computeDate(document.getExpiration()), computeCreator(document.getCreator(), document.getOwner()), computeStatus(document.getStatus()), computeDate(document.getIssued()), document.getReference(), computeKeywords(contentNode, document.getSubject()), computeDynAttr(contentNode, document.getDynAttribute1(), 1) , computeDynAttr(contentNode, document.getDynAttribute2(), 2), computeDynAttr(contentNode, document.getDynAttribute3(), 3), computeDynAttr(contentNode, document.getDynAttribute4(), 4), computeDynAttr(contentNode, document.getDynAttribute5(), 5), logger);
        }
        else
        {
            final String exportationPath = ElementsHelper.getExportationPath(contentNode);
            final String fileName = FilePathUtils.retreiveParentName(exportationPath);
            final String documentName = FilePathUtils.computeArbitraryNameWithExtension(fileName);

            MetadataUtils.setNamedNodeProperty(contentNode, documentName, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, documentName), null, null, logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), computeVersionFromPath(exportationPath), logger);

            logger.warn("Version " + ElementsHelper.getExportationPath(contentNode) + " not found in database. Impossible to set common properties! The name will be " + documentName);
        }
    }

    public void visit(final LibraryTranslation contentNode) throws Exception
    {
        final Document document = retreiveDocument(contentNode);
        if(document != null)
        {
            MetadataUtils.setNodeProperty(contentNode, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(contentNode, computeAlternative(document, contentNode), logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), document.getVersion(), logger);
            MetadataUtils.setTranslationProperty(contentNode, computeCreator(document.getCreator(), document.getOwner()), computeStatus(document.getStatus()), computeDate(document.getIssued()), document.getReference(), computeKeywords(contentNode, document.getSubject()), computeDynAttr(contentNode, document.getDynAttribute1(), 1) , computeDynAttr(contentNode, document.getDynAttribute2(), 2), computeDynAttr(contentNode, document.getDynAttribute3(), 3), computeDynAttr(contentNode, document.getDynAttribute4(), 4), computeDynAttr(contentNode, document.getDynAttribute5(), 5), computeLang(document.getLanguage()), logger);
        }
        else
        {
            final String exportationPath = ElementsHelper.getExportationPath(contentNode);
            final String fileName = FilePathUtils.retreiveParentName(exportationPath);
            final String documentName = FilePathUtils.computeArbitraryNameWithExtension(fileName);

            MetadataUtils.setNamedNodeProperty(contentNode, documentName, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, documentName), null, null, logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), computeVersionFromPath(exportationPath), logger);
            MetadataUtils.setTranslationProperty(contentNode, null, null, null, null, null, null, null, null, null, null, computeLangFromPath(exportationPath), logger);
            logger.warn("Translation " + exportationPath + " not found in database. Impossible to set common properties! The name will be " + documentName);
        }
    }

    public void visit(final LibraryTranslationVersion contentNode) throws Exception
    {
        final Document document = retreiveDocument(contentNode);

        if(document != null)
        {
            MetadataUtils.setNodeProperty(contentNode, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(contentNode, computeAlternative(document, contentNode), logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), document.getVersion(),logger);
            MetadataUtils.setTranslationProperty(contentNode, computeCreator(document.getCreator(), document.getOwner()), computeStatus(document.getStatus()), computeDate(document.getIssued()), document.getReference(), computeKeywords(contentNode, document.getSubject()), computeDynAttr(contentNode, document.getDynAttribute1(), 1) , computeDynAttr(contentNode, document.getDynAttribute2(), 2), computeDynAttr(contentNode, document.getDynAttribute3(), 3), computeDynAttr(contentNode, document.getDynAttribute4(), 4), computeDynAttr(contentNode, document.getDynAttribute5(), 5), computeLang(document.getLanguage()), logger);
        }
        else
        {
            final String exportationPath = ElementsHelper.getExportationPath(contentNode);
            final String fileName = FilePathUtils.retreiveParentName(exportationPath);
            final String documentName = FilePathUtils.computeArbitraryNameWithExtension(fileName);

            MetadataUtils.setNamedNodeProperty(contentNode, documentName, logger);
            MetadataUtils.setTitledNodeProperty(contentNode, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, documentName), null, null, logger);
            MetadataUtils.setContentNodeProperty(contentNode, computeUri(contentNode), computeVersionFromPath(exportationPath), logger);
            MetadataUtils.setTranslationProperty(contentNode, null, null, null, null, null, null, null, null, null, null, computeLangFromPath(exportationPath), logger);
            logger.warn("Translation " + exportationPath + " not found in database. Impossible to set common properties! The name will be " + documentName);
        }
    }


    public void visit(final MlContent mlContent) throws Exception
    {
        final String nodePath = ElementsHelper.getExportationPath(mlContent);

        Document firstTranslation = null;
        Document iterDocument = null;
        Date firstCreated = null;
        Date iterCreated = null;
        final List<String> childs = fileClient.list(nodePath, false, true, false);
		for(final String translationPath: childs)
        {
            iterDocument = retreiveDocument(translationPath);
            iterCreated = iterDocument == null ? null : computeDate(iterDocument.getCreated());

            if(firstTranslation == null || (iterCreated != null && firstCreated.after(iterCreated)))
            {
            	if(iterDocument != null)
            	{
            		firstTranslation = iterDocument;
                    firstCreated = computeDate(firstTranslation.getCreated());
            	}

            }
        }

        if(firstTranslation != null)
        {
        	 MetadataUtils.setNodeProperty(mlContent, firstCreated, computeCreator(firstTranslation.getCreator(), firstTranslation.getOwner()), computeDate(firstTranslation.getModified()), null, logger);
             MetadataUtils.setTitledNodeProperty(mlContent, singleMLText(firstTranslation.getLanguage(), firstTranslation.getTitle()), singleMLText(firstTranslation.getLanguage(), firstTranslation.getAbstractDesc()), computeCreator(firstTranslation.getOwner(), firstTranslation.getCreator()), logger);
             MetadataUtils.setNamedNodeProperty(mlContent, ML_CONTENT_OF_PREFIX + computeAlternative(firstTranslation, mlContent), logger);
             MetadataUtils.setMLContentProperty(mlContent, computeCreator(firstTranslation.getCreator(), firstTranslation.getOwner()), computeSecurityRanking(firstTranslation.getAvailability()), computeDate(firstTranslation.getExpiration()), computeLang(firstTranslation.getLanguage()), logger);
        }
        else
        {
             final String fileName = FilePathUtils.retreiveParentName(nodePath);
             final String documentName = FilePathUtils.computeArbitraryNameWithExtension(fileName);

             MetadataUtils.setNamedNodeProperty(mlContent, documentName, logger);
             MetadataUtils.setTitledNodeProperty(mlContent, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, documentName), null, null, logger);
             MetadataUtils.setMLContentProperty(mlContent, null, null, null, Locale.ENGLISH, logger);

             logger.warn("ML Container " + nodePath + " not found in database. Impossible to set common properties!. The name will be " + documentName);
        }
    }

    public void visit(final Space space) throws Exception
    {
        final Section section = retreiveSection(space);

        final MLText titles = new MLText();
        final MLText descriptions = new MLText();

        final String spaceName = FilePathUtils.retreiveFileName(ElementsHelper.getExportationPath(space));

        if(section != null)
        {
            Locale locale;
            for(final SectionLinguistic linguistic: section.getSectionLinguistics())
            {
                locale = computeLang(linguistic.getLanguage());
                titles.addValue(locale, linguistic.getTitle());
                descriptions.addValue(locale, linguistic.getAbstractDesc());
            }
            // created date is wrong in circa for example for 27/06/2011 is saved as 20/06/2027
            // as year is lost we will take same year as modified date or previous
            
            Date createdDate = computeDate(section.getCreated());
			Date modifiedDate = computeDate(section.getModified());
			Date calculatedCreatedDay = null;
			
			if (createdDate != null &&  modifiedDate != null )
			{
				int createdDay  = createdDate.getYear() % 100 ;
				int createdMonth  = createdDate.getMonth() ;
				
				int modifiedYear  = modifiedDate.getYear();
				int modifiedMonth  = modifiedDate.getMonth() ;
				int modifiedDay  = modifiedDate.getDate();
				
				if (( createdMonth > modifiedMonth )  ||  ((createdMonth  == modifiedMonth) && (createdDay > modifiedDay )  ))
				{
					modifiedYear = modifiedYear- 1; 
				}
				calculatedCreatedDay = new Date(modifiedYear, createdMonth, createdDay ,modifiedDate.getHours(), modifiedDate.getMinutes(),modifiedDate.getSeconds() );
			}
			
			
			MetadataUtils.setNodeProperty(space, calculatedCreatedDay, computeCreator(section.getCreator(), section.getOwner()), modifiedDate, null, logger);
            MetadataUtils.setTitledNodeProperty(space, titles, descriptions , computeCreator(section.getOwner(), section.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(space, spaceName, logger);
            MetadataUtils.setSpaceProperty(space, computeDate(section.getExpiration()), logger);
        }
        else
        {
            MetadataUtils.setNamedNodeProperty(space, spaceName, logger);
            MetadataUtils.setTitledNodeProperty(space, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, spaceName), null, null, logger);

            logger.warn("Section " + ElementsHelper.getExportationPath(space) + " not found in database. Impossible to set common properties!");
        }

    }


    @SuppressWarnings("unchecked")
    public void visit(final Url url) throws Exception
    {
        final Document document = retreiveDocument(url);

        if(document != null)
        {
            final String urlName = computeUrlName(ElementsHelper.getExportationPath(url), document);
            MetadataUtils.setNodeProperty(url, computeDate(document.getCreated()),computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(url, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(url, urlName, logger);
            MetadataUtils.setUrlProperty(url, fileClient.downloadAsString(ElementsHelper.getExportationPath(url)), logger);
        }
        else
        {
            final XMLElement parent = ElementsHelper.getParent(url);
            final Method getUrls =  parent.getClass().getMethod("getUrls", new Class[]{});
            final List<Url> urls = (List<Url>) getUrls.invoke(parent);
            urls.remove(url);

            logger.warn("Url " + ElementsHelper.getExportationPath(url) + " not found in database. It will be DELETED. ");
        }

    }

    public void visit(final Link link) throws Exception
    {
        final String linkPath = ElementsHelper.getExportationPath(link);

        final CategoryInterestGroupPair pair = FilePathUtils.getInterestGroupFromPath(linkPath, circaDomainPrefix);
        final Document target = daoFactory.getDocumentDao().getDocumentByIdentifier(pair.getCategory(), pair.getInterestGroup(), linkPath);
        final Dossier parentDossier = (Dossier) ElementsHelper.getParent(link);

        if(target == null)
        {
             // target deleted, remove the link
            final List<Link> links = parentDossier.getLinks();
            links.remove(link);

            logger.warn("Target of the link " + ElementsHelper.getExportationPath(link) + " not found in database. It will be DELETED. ");
        }
        else
        {
            final String alternative = computeAlternative(target, link);

            MetadataUtils.setNodeProperty(link, (Date) parentDossier.getCreated().getValue(), (String) parentDossier.getCreator().getValue(), (Date) parentDossier.getModified().getValue(), null, logger);
            MetadataUtils.setTitledNodeProperty(link, singleMLText(target.getLanguage(), target.getTitle()), singleMLText(target.getLanguage(), target.getAbstractDesc()), computeCreator(target.getOwner(), target.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(link, LINK_TO_PREFIX + alternative + ".url", logger);
            MetadataUtils.setLinkProperty(link, computeFileReference(linkPath), logger);
        }
    }

    @SuppressWarnings("unchecked")
    public void visit(final SharedSpacelink link) throws Exception
    {
    	String path = ElementsHelper.getExportationPath(link);
    	if(path.endsWith(FilePathUtils.SECTION_EXTENSION))
		{
			path = FilePathUtils.removeExtension(path);
		}

    	final Section section = retreiveSection(path);

        if(section != null)
        {
            final String originator = section.getOriginator();

            if(originator != null && originator.length() > 0)
            {
                 Locale locale;
                 final MLText titles = new MLText();
                 final MLText descriptions = new MLText();
                 for(final SectionLinguistic linguistic: section.getSectionLinguistics())
                 {
                     locale = computeLang(linguistic.getLanguage());
                     titles.addValue(locale, linguistic.getTitle());
                     descriptions.addValue(locale, linguistic.getAbstractDesc());
                 }

                 final String spaceName = FilePathUtils.retreiveFileName(ElementsHelper.getExportationPath(link));

                 MetadataUtils.setNodeProperty(link, computeDate(section.getCreated()), computeCreator(section.getCreator(), section.getOwner()), computeDate(section.getModified()), null, logger);
                 MetadataUtils.setTitledNodeProperty(link, titles, descriptions , computeCreator(section.getOwner(), section.getCreator()), logger);
                 MetadataUtils.setNamedNodeProperty(link, spaceName, logger);
                 MetadataUtils.setLinkProperty(link, computeSpaceReference(originator), logger);

            }
            else
            {
                throw new ExportationException("Share space link found but no originator (target space) found in the database");
            }
        }
        else
        {
            final XMLElement parent = ElementsHelper.getParent(link);
            final Method getLinks =  parent.getClass().getMethod("getSharedSpacelinks", new Class[]{});
            final List<SharedSpacelink> links = (List<SharedSpacelink>) getLinks.invoke(parent);
            links.remove(link);

            logger.warn("SharedSpaceLink " + ElementsHelper.getExportationPath(link) + " not found in database. It will be DELETED!");
        }

    }

    public void visit(final Dossier dossier) throws Exception
    {
        final String dossierPath = ElementsHelper.getExportationPath(dossier);
        final Document document = retreiveDocument(dossierPath);

        if(document != null)
        {
            MetadataUtils.setNodeProperty(dossier, computeDate(document.getCreated()), computeCreator(document.getCreator(), document.getOwner()), computeDate(document.getModified()), null, logger);
            MetadataUtils.setTitledNodeProperty(dossier, singleMLText(document.getLanguage(), document.getTitle()), singleMLText(document.getLanguage(), document.getAbstractDesc()), computeCreator(document.getOwner(), document.getCreator()), logger);
            MetadataUtils.setNamedNodeProperty(dossier, computeDossierName(dossierPath, document), logger);
        }
        else
        {
            final String dossierName = FilePathUtils.retreiveFileName(ElementsHelper.getExportationPath(dossier));

            MetadataUtils.setNamedNodeProperty(dossier, dossierName, logger);
            MetadataUtils.setTitledNodeProperty(dossier, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, dossierName), null, null, logger);

            logger.warn("Dossier " + ElementsHelper.getExportationPath(dossier) + " not found in database. Impossible to set common properties!");
        }
    }

    public void visit(final Forum forum) throws Exception
    {
        final News news = retreiveForum(forum);
        if(news != null)
        {
            final MLText titles = new MLText();
            final MLText descriptions = new MLText();
            Locale locale;
            String title;
            for(final NewsLinguistic linguistic: news.getNewsLinguistics())
            {
                locale = computeLang(linguistic.getLanguage());
                title = linguistic.getTitle();

                titles.addValue(locale, title.substring(title.lastIndexOf('.') + 1));
                descriptions.addValue(locale, linguistic.getAbstractDesc());
            }

            MetadataUtils.setNodeProperty(forum, computeDate(news.getCreated()), computeCreator(news.getCreator(), null), computeDate(news.getModified()), null, logger);
            MetadataUtils.setNamedNodeProperty(forum, news.getSubject(), true, logger);
            MetadataUtils.setTitledNodeProperty(forum, titles, descriptions , null, logger);
            /* this method is safer ... the value in the database is some time corrupted */
            boolean moderated = nntpClient.isModeratedForum(news.getTitle());
            MetadataUtils.setForumProperty(forum, moderated, logger);
        }
        else
        {
            final String path = ElementsHelper.getExportationPath(forum);
            final String newsName = path.substring(path.lastIndexOf('.') + 1);

            MetadataUtils.setNamedNodeProperty(forum, newsName, logger);
            MetadataUtils.setTitledNodeProperty(forum, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, newsName), null, null, logger);
            MetadataUtils.setForumProperty(forum, false, logger);

            logger.warn("Newsgroup " + ElementsHelper.getExportationPath(forum) + " not found in database. Impossible to set common properties!");
        }
    }
    public void visit(final Topic topic) throws Exception
    {
        javax.mail.Message nntpMessage = getNNTPMessageFromNode(topic);
        final String title = nntpMessage.getSubject();

        final NamedNode forum = (NamedNode) ElementsHelper.getParent(topic);
        final boolean moderated;
        if(forum instanceof Discussions)
        {
            moderated = false;
        }
        else
        {
            moderated = ((Forum)forum).isModerated();
        }

        MetadataUtils.setNodeProperty(topic, nntpClient.getCreatedDate(nntpMessage), computeCreator(nntpClient.getCreator(nntpMessage), null), null, null, logger);
        MetadataUtils.setNamedNodeProperty(topic, title, true, logger);
        MetadataUtils.setTitledNodeProperty(topic, singleMLText(FilePathUtils.DEFAULT_LANGUAGE, title), null , null, logger);

        // in circa, availability and expiration date are setted at the newsgroups level!
        final News news = retreiveForum(forum);
        if(news != null)
        {
            final int valid = Integer.parseInt(news.getValid());
            final Date created = (Date) forum.getCreated().getValue();
            final Date validUntil;
            if(valid > 0)
            {
                final Calendar calendar = new GregorianCalendar();
                calendar.setTime(created);
                calendar.add(Calendar.DAY_OF_YEAR, valid);
                validUntil = calendar.getTime();
            }
            else
            {
                validUntil = null;
            }
            MetadataUtils.setTopicProperty(topic, computeSecurityRanking(news.getAvailability()), validUntil, moderated, logger);
        }
        else
        {
            // should be already logged

            MetadataUtils.setTopicProperty(topic, null, null, moderated, logger);
        }
    }

    public void visit(final Message message) throws Exception
    {
        final Topic topic = ElementsHelper.getElementTopic(message);
        final String newsgroupName =  FilePathUtils.retreiveParentName(ElementsHelper.getExportationPath(message));
        final boolean moderated = topic.isModerated();
        javax.mail.Message nntpMessage = getNNTPMessageFromNode(message);

        MetadataUtils.setNodeProperty(message, nntpClient.getCreatedDate(nntpMessage), computeCreator(nntpClient.getCreator(nntpMessage), null), null, null, logger);
        MetadataUtils.setContentNodeProperty(message, nntpClient.generateResouceString(
                newsgroupName,
                nntpClient.computeArticleId(nntpMessage),
                0), logger);

        if(moderated == true)
        {
            // all news in nntp are accepted.
            boolean accepted = nntpResourceHandler.handle(message.getUri());

            MetadataUtils.setModerationProperty(message, false, accepted, NG_MODERATOR, nntpClient.getModerated(nntpMessage), null, true, logger);
        }
    }

    public void visit(final SimpleContent content) throws Exception
    {
        final Message message = (Message) ElementsHelper.getParent(content);
        final String newsgroupName =  FilePathUtils.retreiveParentName(ElementsHelper.getExportationPath(message));
        final String contentIndex = FilePathUtils.retreiveFileName(ElementsHelper.getExportationPath(content));
        javax.mail.Message nntpMessage = getNNTPMessageFromNode(message);
        final int parseIdx = Integer.parseInt(contentIndex);

        final String messageId = nntpClient.computeArticleId(nntpMessage);
        final Object part = nntpClient.getMessagePart(
                newsgroupName,
                messageId,
                parseIdx);

        if(part instanceof BodyPart)
        {
            final BodyPart bodyPart = (BodyPart) part;

            MetadataUtils.setNamedNodeProperty(content, bodyPart.getFileName(), logger);
            MetadataUtils.setContentNodeProperty(content, nntpClient.generateResouceString(newsgroupName, messageId, parseIdx), "1.0", logger);
        }
        else
        {
            throw new ExportationException("File attachement expected, not the core message.");
        }

    }

    public void visit(final Discussions discussion) throws Exception
    {
        final NamedNode content = (NamedNode) ElementsHelper.getParent(discussion);
        final CreatedProperty created = content.getCreated();
        final CreatorProperty creator = content.getCreator();
        final Serializable contentName = content.getName().getValue();
        final String forumName = MessageFormat.format(DISCUSSION_FOR, contentName);

        MetadataUtils.setNodeProperty(discussion,
                created  == null ?  null : (Date)created.getValue(),
                creator  == null ?  null : (String)creator.getValue(),
                null, null, logger);

        MetadataUtils.setNamedNodeProperty(discussion, forumName, logger);

    }

    public void visit(final Information information) throws Exception
    {
    	final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(information);
    	final Category category = ElementsHelper.getElementCategory(interestGroup);
    	final String interestGroupPath = ElementsHelper.getExportationPath(interestGroup);

    	// check if multiple entry points are setted.
    	final Map<String, String> entryPoints = dbClient.readFile(
    			DbFileLocations.getInformationEntryPoints(fileClient.getDataRoot(), (String) category.getName().getValue(), (String) interestGroup.getName().getValue())
    	);

    	final String indexFile;

    	if(entryPoints == null || entryPoints.size() == 0)
    	{
    		String home = circaIGConfigReader.getInformationHomePage(interestGroupPath);
        	if(home == null)
        	{
        		home = circaIGConfigReader.getInterestGroupHomePage(interestGroupPath);
        	}

        	indexFile = home;
    	}
    	else if(entryPoints.size() == 1)
    	{
    		// go directly to the first entry point !
    		indexFile = entryPoints.values().iterator().next();
    	}
    	else
    	{
    		final InfContent content = new InfContent();

    		content.setName(new TypedProperty.NameProperty("informationSubSites.html"));
    		content.setTitle(new TypedProperty.TitleProperty("Links to available sub-sites"));
    		content.setDescription(new TypedProperty.DescriptionProperty("Html document that hold all sub site links available in " + interestGroup.getName().getValue() + " information service."));
    		content.setVersionLabel(new TypedProperty.VersionLabelProperty("1.0"));
    		final StringBuilder builder = new StringBuilder("");

    		builder.append("<html>").append('\n');
    		builder.append("\t<head>").append('\n');
    		builder.append("\t\t<title>Link summary</title>").append('\n');
    		builder.append("\t</head>").append('\n');
    		builder.append("\t<body>").append('\n');
    		builder.append("\t\t<h1>Information &gt; Sub-sites</h1>").append('\n');
    		builder.append("\t\t\t<ul>").append('\n');

    		for(Map.Entry<String, String> entry: entryPoints.entrySet())
    		{
    			builder
    				.append("\t\t\t\t<li><a href=\"")
    				.append(computeEntryPointPath(entry.getKey()))
    				.append("\" title=\"")
    				.append(entry.getValue())
    				.append("\">")
    				.append(entry.getValue())
    				.append("</a></li>");

    		}

    		builder.append("\t\t\t</ul>").append('\n');
    		builder.append("\t</body>").append('\n');
    		builder.append("</html>").append('\n');

    		content.setContentString(builder.toString());
    		information.getInfContents().add(content);


    		indexFile = (String) content.getName().getValue();
    	}

    	MetadataUtils.setInformationProperty(information, indexFile, true, logger);
    }

    //--------------------------
    //---	  Helpers		 ---
    //--------------------------

    private static final String computeEntryPointPath(final String entryPoint)
    {
    	final Matcher matcherLinguistic = REGEX_ENTRY_LING.matcher(entryPoint);

    	if(matcherLinguistic.matches())
		{
    		// Link contains language information, retrive it: /XX/EN/doc.html --> doc.html
			return matcherLinguistic.group(2);
		}
    	else
    	{
    		final Matcher matcherExternal = REGEX_ENTRY_EXT.matcher(entryPoint);
    		if(matcherExternal.matches())
    		{
        		// Link is an external link, retrive it: /.EX/ENhttp://europa.eu --> http://europa.eu
    			return matcherExternal.group(2);
    		}
    		else
    		{
    			// Link has no additional language info (language setted is equals to the language of the document)
    			return entryPoint;
    		}
    	}
    }

    private javax.mail.Message getNNTPMessageFromNode(final XMLNode message) throws ExportationException, NumberFormatException
    {
        final String exportationPath = ElementsHelper.getExportationPath(message);
        final String messageIndex = FilePathUtils.retreiveFileName(exportationPath);
        final String newsgroupName = FilePathUtils.retreiveParentName(exportationPath);
        final javax.mail.Message nntpMessage = nntpClient.getMessage(newsgroupName, messageIndex);

        return nntpMessage;
    }

    private String computeDossierName(final String dossierPath, final Document document) throws SQLException, IOException, ExportationException
    {
        boolean isMulticontents = isMulticontents(dossierPath, document);

        final String title = document.getTitle();
        final String name = title + (isMulticontents ? "_" + document.getLanguage() + "_" + document.getVersion() : "");

        return name;
    }


    private Locale computeLang(final String lang)
    {
        return I18NUtil.parseLocale(lang.toLowerCase());
    }

    private Locale computeLangFromPath(final String path)
    {
    	if(FilePathUtils.isDocumentPathFromNoLanguage(path))
    	{
    		return Locale.ENGLISH;
    	}
    	else
    	{
    		return computeLang(FilePathUtils.retreiveLanguageFromPath(path));
    	}
    }


    private String computeUrlName(final String urlPath, Document document) throws SQLException, IOException, ExportationException
    {
        boolean isMulticontents = isMulticontents(urlPath, document);
        final String name = computeAlternative(document.getAlternative(), urlPath) + (isMulticontents ? "_" + document.getLanguage() + "_" + document.getVersion() : "") + DOT_HTML;

        return name;
    }

    private boolean isMulticontents(final String path, final Document document) throws SQLException, IOException, ExportationException
    {
        final List<Document> allUrls = retreiveDocumentPool(path, document.getParentUrl(), document.getDocPool());
        return allUrls.size() > 1;
    }

    private String computeSecurityRanking(final String circaAvailability)
    {
        return computeSecurityRanking(Integer.parseInt(circaAvailability));
    }

    private String computeSecurityRanking(final Integer circaAvailability)
    {
        if(circaAvailability == null || circaAvailability == 10)
        {
            return DocumentModel.SECURITY_RANKINGS_PUBLIC;
        }
        else if(circaAvailability == 20)
        {
            return DocumentModel.SECURITY_RANKINGS_INTERNAL;
        }
        else if(circaAvailability == 30)
        {
            return DocumentModel.SECURITY_RANKINGS_LIMITED;
        }
        else
        {
            logger.warn("Security ranking with index " + circaAvailability + " not recognized. A value by default will be used: " + DocumentModel.SECURITY_RANKINGS_PUBLIC);

            return DocumentModel.SECURITY_RANKINGS_PUBLIC;
        }
    }

    private String computeFileReference(final String targetReference) throws ExportationException, SQLException, IOException
    {
        final ParsedPath parsedPath = new ParsedPath(targetReference, fileClient, circaDomainPrefix);
        final StringBuilder builder = new StringBuilder(NodeReferencesValidator.CIRCABC_REFERENCE);

        builder
            .append(FileClient.PATH_SEPARATOR)
            .append(parsedPath.getVirtualCirca())
            .append(FileClient.PATH_SEPARATOR)
            .append(parsedPath.getInterestGroup())
            .append(FileClient.PATH_SEPARATOR)
            .append(Library.class.getSimpleName());

        final String contentFolder = FilePathUtils.retreiveParentPath(parsedPath.getInServicePath());
        final String parentSpace =  FilePathUtils.retreiveParentPath(contentFolder);
        builder.append(parentSpace);
        builder.append(FileClient.PATH_SEPARATOR);

        /* append the name of the target document */
        final Document target = retreiveDocument(parsedPath.toDbReference());
        final String parentFilePath = FilePathUtils.retreiveParentPath(parsedPath.toFilePath());
        if(FilePathUtils.isUrl(parentFilePath, fileClient))
        {
            builder.append(computeUrlName(parsedPath.toFilePath(), target));
        }
        else if(FilePathUtils.isDossier(parentFilePath, fileClient))
        {
            builder.append(computeDossierName(parentFilePath, target));
        }
        else
        {
            builder.append(computeAlternative(target.getAlternative(), targetReference));
        }


        return builder.toString();
    }

    private String computeSpaceReference(final String targetReference) throws ExportationException, SQLException, IOException
    {
        // the path looks like /virtualCirca/ig/space/subspace
        final StringTokenizer tokens = new StringTokenizer(targetReference, "/", false);

        final StringBuilder builder = new StringBuilder(NodeReferencesValidator.CIRCABC_REFERENCE);

        builder
            .append(FileClient.PATH_SEPARATOR)
            .append(tokens.nextToken())
            .append(FileClient.PATH_SEPARATOR)
            .append(tokens.nextToken())
            .append(FileClient.PATH_SEPARATOR)
            .append(Library.class.getSimpleName());

        // get all rest of the line ...
        while(tokens.hasMoreTokens())
        {
            builder
                .append(FileClient.PATH_SEPARATOR)
                .append(tokens.nextToken());
        }


        return builder.toString();
    }

    private List<Integer> computeKeywords(final XMLNode node, final String dcSubject)
    {
        if(dcSubject != null && dcSubject.length() > 0 && dcSubject.equals(NA_JOCKER) == false)
        {
            final StringTokenizer keywords = new StringTokenizer(dcSubject, KEYWORDS_SEPARATOR, false);
            final List<Integer> indexes = new ArrayList<Integer>(keywords.countTokens());

            final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(node);

            KeywordDefinitions definitions = interestGroup.getKeywordDefinitions();
            if(definitions == null)
            {
                interestGroup.setKeywordDefinitions((definitions = new KeywordDefinitions()));
            }

            String token;
            Integer idx;
            while(keywords.hasMoreTokens())
            {
                token = keywords.nextToken();
                idx = getKeywordIndex(token, definitions);

                if(idx.intValue() < 0)
                {
                    idx = firstFreeIndex(definitions);
                    definitions.withDefinitions(new KeywordDefinition().withId(idx).withValue(token));
                }

                indexes.add(idx);
            }

            return indexes;
        }
        else
        {
            return null;
        }

    }

    private Integer getKeywordIndex(final String keyword, final KeywordDefinitions definitions)
    {
        Integer index = null;

        for(KeywordDefinition definition: definitions.getDefinitions())
        {
            if(isKeywordMatch(keyword, definition.getValue(), definition.getI18NValues()))
            {
                index = definition.getId();
                break;
            }
        }

        return (index != null) ? index :  Integer.valueOf(-1);
    }

    private boolean isKeywordMatch(final String keyword, final String value, final List<I18NProperty> values)
    {
        if(value == null && (values == null || values.size() < 1))
        {
            return false;
        }
        else if(keyword.equals(value))
        {
            return true;
        }
        else
        {
            boolean found = false;

            for(final I18NProperty property: values)
            {
                if(keyword.equals(property.getValue()))
                {
                    found = true;
                    break;
                }
            }

            return found;
        }
    }

    private Integer firstFreeIndex(final KeywordDefinitions definitions)
    {
        Integer free = null;
        final List<Integer> reserved = new ArrayList<Integer>();

        for(KeywordDefinition def: definitions.getDefinitions())
        {
            reserved.add(def.getId());
        }

        for(int x = 0; ;++x)
        {
            free = Integer.valueOf(x);
            if(reserved.contains(free) == false)
            {
                break;
            }
        }

        return free;
    }

    private String computeStatus(final String status)
    {
        if(status == null || status.length() < 1 || status.equals(NA_JOCKER))
        {
            return DocumentModel.STATUS_VALUE_DRAFT;
        }
        else if(status.equalsIgnoreCase("Released"))
        {
            return DocumentModel.STATUS_VALUE_RELEASE;
        }
        else if(status.equalsIgnoreCase(DocumentModel.STATUS_VALUE_DRAFT))
        {
            return DocumentModel.STATUS_VALUE_DRAFT;
        }
        else if(status.equalsIgnoreCase(DocumentModel.STATUS_VALUE_FINAL))
        {
            return DocumentModel.STATUS_VALUE_FINAL;
        }
        else
        {
            logger.warn("Status with index '" + status + "' not recognized. A value by default will be used: " + DocumentModel.STATUS_VALUE_DRAFT);

            return DocumentModel.STATUS_VALUE_DRAFT;
        }
    }

    private String computeUri(final XMLNode node)
    {
        return fileClient.generateResouceString(ElementsHelper.getExportationPath(node));
    }


    private Section retreiveSection(final XMLNode node) throws SQLException, IOException, ExportationException
    {
         final String nodePath = ElementsHelper.getExportationPath(node);
         return retreiveSection(nodePath);
    }

	private Section retreiveSection(final String nodePath) throws ExportationException, SQLException, IOException
	{
		final ParsedPath parsedPath = new ParsedPath(nodePath, fileClient, circaDomainPrefix);

         return daoFactory.getSectionDao().getSectionsByIdentifier(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), parsedPath.toDbReference());
	}

    private Document retreiveDocument(final XMLNode node) throws SQLException, IOException, ExportationException
    {
        final String nodePath = ElementsHelper.getExportationPath(node);
        return retreiveDocument(nodePath);
    }

    private List<Document> retreiveDocumentPool(final String nodePath, final String parentUrl, final String docPool) throws SQLException, IOException, ExportationException
    {
        final ParsedPath parsedPath = new ParsedPath(nodePath, fileClient, circaDomainPrefix);

        return daoFactory.getDocumentDao().getDocumentsFromPool(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), parentUrl, docPool);
    }

    private Document retreiveDocument(final String nodePath) throws SQLException, IOException, ExportationException
    {
        final ParsedPath parsedPath = new ParsedPath(nodePath, fileClient, circaDomainPrefix);
        final String reference = parsedPath.toDbReference();
        Document document = daoFactory.getDocumentDao().getDocumentByIdentifier(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), reference);

        if(document == null)
        {
            // sometimes it can occurs ...
            final String parentDocumentPath = reference.substring(0, reference.lastIndexOf('/'));
            final String version = FilePathUtils.retreiveVersionFromPath(nodePath);
            final String query = parentDocumentPath.replace("_", "\\_") + "/\\___\\_" + version + "_";

            final List<Document> documents = daoFactory.getDocumentDao().getDocumentLikeIdentifier(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), query);
            if(documents == null || documents.size() < 1)
            {
                logger.warn("Document " + nodePath + " not found in database. Impossible to set common properties!");
            }
            else if(documents.size() > 1)
            {
                Date lastModified = null;
                Document lastModifiedDoc = null;
                Date iterDate;

                for(final Document doc: documents)
                {
                    iterDate = computeDate(doc.getModified());
                    if(lastModified == null || lastModified.before(iterDate))
                    {
                        lastModifiedDoc = doc;
                        lastModified = iterDate;
                    }
                }

                return lastModifiedDoc;
            }
            else
            {
                document = documents.get(0);
            }

        }
        return document;
    }

    private Global retreiveGlobalDocument(final XMLNode node) throws SQLException, IOException, ExportationException
    {
        final String nodePath = ElementsHelper.getExportationPath(node);
        final ParsedPath parsedPath = new ParsedPath(nodePath, fileClient, circaDomainPrefix);

        return daoFactory.getGlobalDao().getGlobalDocumentByIdentifier(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), parsedPath.toDbReference());
    }

    private News retreiveForum(final XMLNode node) throws SQLException, IOException, ExportationException
    {
        final String nodePath = ElementsHelper.getExportationPath(node);
        final ParsedPath parsedPath = new ParsedPath(nodePath, fileClient, circaDomainPrefix);
        String inServicePath = parsedPath.getInServicePath();
        if(inServicePath.startsWith("/"))
        {
            inServicePath = inServicePath.substring(1);
        }
        return daoFactory.getNewsDao().getNewsByTitle(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), inServicePath);
    }

    private MLText convertFilesToMLText(final String path) throws Exception
    {
        String lang;
        String content;
        final MLText mlText = new MLText();
        for(final String translation: fileClient.list(path, true, true, false))
        {
            if(!translation.endsWith(BACKUP_EXTENSION))
            {
                lang = FilePathUtils.retreiveFileName(translation);
                content = fileClient.downloadAsString(translation);
                mlText.put(computeLang(lang), content);
            }
        }

        return mlText;
    }

    private String computeCreator(final String creator, final String owner)
    {
    	final String user;

    	if(owner == null || owner.length() < 0)
        {
    		user = creator ;
        }
    	else
    	{
    		user = owner;
    	}


    	if(user == null || user.length() == 0)
    	{
    		return null;
    	}
    	else if(LdapHelper.isUserid(user))
    	{
    		return user;
    	}
    	else
    	{
    		String uid = null;

    		try
    		{
    			uid = userReader.getPersonidWithCommonName(user);
    		}
    		catch(final Exception ignore){}

    		return uid == null ? user : uid;
    	}


    }

    private String computeVersionFromPath(final String path)
    {
    	final String version = FilePathUtils.retreiveVersionFromPath(path);
    	if(CustomLabelAwareVersionServiceImpl.isValidVersionLabel(version) == false)
    	{
    		return "1.0";
    	}
    	else
    	{
    		return version;
    	}

    }

    private static final String DATE_REGEX_1 = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
    private static final ThreadLocal<DateFormat> DATE_FORMAT_1 = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
    
    private static final String DATE_REGEX_2 = "[0-9]{2}/[0-9]{2}/[0-9]{4}";
    private static final ThreadLocal<DateFormat> DATE_FORMAT_2 = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy");
		}
	};
    

    private static final String DATE_REGEX_3 = ("[0-9]{4}-[A-Z]{1}[a-z]{2}-[0-9]{2}");
    private static final ThreadLocal<DateFormat> DATE_FORMAT_3 = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MMM-dd", Locale.ENGLISH);
		}
	};

    private static final String LONG_REGEX = "[0-9]+";


    /**
     * Get the most relevant date according a given string.
     *
     * @param date
     * @return
     */
    private Date computeDate(String date) throws ExportationException
    {
        if(date == null || date.trim().length() < 1)
        {
            return null;
        }

        date = date.trim();

        if(date.matches(DATE_REGEX_1))
        {
            try
            {
                return DATE_FORMAT_1.get().parse(date);
            }
            catch (ParseException ignore){}
        }
        else if(date.matches(DATE_REGEX_2))
        {
            try
            {
                return DATE_FORMAT_2.get().parse(date);
            }
            catch (ParseException ignore){}
        }
        else if(date.matches(DATE_REGEX_3))
        {
            try
            {
                return DATE_FORMAT_3.get().parse(date);
            }
            catch (ParseException ignore){}
        }
        else if(date.matches(LONG_REGEX))
        {
            final Long time = Long.valueOf(date);
            return new Date(time * 1000l);
        }

        logger.warn("Unparsable date received from the circa database: " + date);

        throw new ExportationException("Unparsable date received from the circa database: " + date);
    }

    private MLText singleMLText(String lang, String value)
    {
        return new MLText(I18NUtil.parseLocale(lang), value);
    }

    private Serializable computeDynAttr(final XMLNode node, final Serializable value, final int index) throws ExportationException
    {

        if(value == null || value.toString().length() < 1)
        {
            return null;
        }

        boolean isDate = false;
        boolean indexFound = false;

        final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(node);
        final DynamicPropertyDefinitions definitions = interestGroup.getDynamicPropertyDefinitions();

        if(definitions != null)
        {
            for(DynamicPropertyDefinition def : definitions.getDefinitions())
            {
                if(def.getId().equals(index))
                {
                    indexFound = true;

                    isDate = DynPropertyType.DATE_FIELD.equals(def.getType());
                    break;
                }
            }
        }

        if(!indexFound)
        {
            return null;
        }
        else if(!isDate)
        {
            return value;
        }
        else /* index found and defined as a date */
        {
            try
            {
                return DateAdapterUtils.marshalDate(DYN_PROP_DATE_FORMAT.parse(value.toString()));
            }
            catch (final ParseException e)
            {
                throw new ExportationException("Impossible to parse " + value + " to a date for the dynamic attribute " + index + " of the document " + ElementsHelper.getExportationPath(node));
            }
        }
    }

    private String computeAlternative(final Document doc, final XMLNode node)
    {
    	return computeAlternative(doc.getAlternative(), ElementsHelper.getExportationPath(node));
    }

    private String computeAlternative(final Global doc, final XMLNode node)
    {
    	return computeAlternative(doc.getAlternative(), ElementsHelper.getExportationPath(node));
    }

    private String computeAlternative(final String alternative, final String nodepath)
    {
    	if(alternative == null || alternative.trim().length() < 1)
        {
            return FilePathUtils.retreiveFileName(nodepath);
        }
    	else
    	{
    		return alternative;
    	}
    }
}
