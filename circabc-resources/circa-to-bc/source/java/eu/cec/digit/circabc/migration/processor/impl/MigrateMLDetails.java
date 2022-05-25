/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CircabcAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;


/**
 * Import the multilingual properties (Make Multilingual and add translations) using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateMLDetails extends MigrateProcessorBase
{
    @Override
    public void visit(final MlContent mlContent) throws Exception
    {
        final Map<Locale, Node> translations = new HashMap<Locale, Node>(mlContent.getTranslations().size());
        for(final LibraryTranslation node : mlContent.getTranslations())
        {
            translations.put((Locale)node.getLang().getValue(), node);
        }

        final Locale pivot = (Locale)mlContent.getPivotLang().getValue();
        apply(new MigrateMLContainerCallback(getJournal(), mlContent, pivot, translations));

        for(final Map.Entry<Locale, Node> translation : translations.entrySet())
        {
        	apply(new MigrateTranlationsCallback(getJournal(), mlContent, pivot, translation.getValue(), translation.getKey()));
        }

    }

    @Override
    public void visit(final InfMLContent mlContent) throws Exception
    {
    	 final Map<Locale, Node> translations = new HashMap<Locale, Node>(mlContent.getTranslations().size());
         for(final InformationTranslation node : mlContent.getTranslations())
         {
             translations.put((Locale)node.getLang().getValue(), node);
         }

         final Locale pivot = (Locale)mlContent.getPivotLang().getValue();
		 apply(new MigrateMLContainerCallback(getJournal(), mlContent, pivot, translations));

        for(final Map.Entry<Locale, Node> translation : translations.entrySet())
        {
            apply(new MigrateTranlationsCallback(getJournal(), mlContent, pivot, translation.getValue(), translation.getKey()));
        }
    }

    class MigrateMLContainerCallback extends JournalizedTransactionCallback
    {
        final Node mlContent;
        final Map<Locale, Node> translations;
        final Locale pivotLanguage;

        public MigrateMLContainerCallback(final MigrationTracer journal, final Node mlContent, final Locale mlContainerLocale, final Map<Locale, Node> translations)
        {
            super(journal);
            this.mlContent = mlContent;
            this.translations = translations;
            this.pivotLanguage= mlContainerLocale;
        }

        public String execute() throws Throwable
        {
            try
            {
            	if(ElementsHelper.isNodeCreated(mlContent))
            	{
            		getReport().appendSection(ElementsHelper.getQualifiedPath(mlContent) + " is already created.");
            	}
            	else
            	{
            		final Node pivot = translations.get(pivotLanguage);
                    final NodeRef pivotNodeRef = ElementsHelper.getNodeRef(pivot);
                    NodeRef mlContainerRef;
                    final MultilingualContentService multilingualContentService = getMultilingualContentService();
                    if(!multilingualContentService.isTranslation(pivotNodeRef))
                    {
                        multilingualContentService.makeTranslation(pivotNodeRef, pivotLanguage);
                        getReport().appendSection(ElementsHelper.getQualifiedPath(pivot) + " successfully setted being multilingual.");

                        getJournal().journalize(JournalLine.updateNode(
                                Status.SUCCESS,
                                ElementsHelper.getQualifiedPath(pivot),
                                pivotNodeRef.toString(),
                                JournalLine.UpdateOperation.MAKE_MULTILINGUAL,
                                null
                            ));
                    }
                    else
                    {
                        getReport().appendSection(ElementsHelper.getQualifiedPath(pivot) + " is already multlingual.");
                    }

                    mlContainerRef = multilingualContentService.getTranslationContainer(pivotNodeRef);
                    ElementsHelper.setNodeRef(mlContent, mlContainerRef);
            	}

            }
            catch(Throwable t)
            {
                getReport().appendSection("Impossible to make multilingual " +  ElementsHelper.getQualifiedPath(mlContent) + ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.updateNode(
                            Status.FAIL,
                            ElementsHelper.getQualifiedPath(mlContent),
                            JournalLine.UpdateOperation.MAKE_MULTILINGUAL,
                            null,
                            null
                        ));
                }
            }

            return null;
        }
    }

    class MigrateTranlationsCallback extends JournalizedTransactionCallback
    {
        final Node mlContent;
        final Node translation;
        final Locale pivotLanguage;
        final Locale translationLocale;
        public MigrateTranlationsCallback(final MigrationTracer journal, final Node mlContent, final Locale mlContainerLocale, final Node translation, final Locale translationLocale)
        {
            super(journal);
            this.mlContent = mlContent;
            this.translation = translation;
            this.pivotLanguage= mlContainerLocale;
            this.translationLocale = translationLocale;
        }

        public NodeRef execute() throws Throwable
        {
            NodeRef translationRef = null;

            try
            {
                final NodeRef mlContainerRef = ElementsHelper.getNodeRef(mlContent);
                translationRef = ElementsHelper.getNodeRef(translation);

                if(!pivotLanguage.equals(translationLocale))
                {
                	final MultilingualContentService multilingualContentService = getMultilingualContentService();

                    if(multilingualContentService.isTranslation(translationRef))
                    {
                        getReport().appendSection(ElementsHelper.getQualifiedPath(translation) + " already a translation of " + ElementsHelper.getQualifiedPath(mlContent));
                    }
                    else
                    {
                        multilingualContentService.addTranslation(translationRef, mlContainerRef, translationLocale);

                        getReport().appendSection(ElementsHelper.getQualifiedPath(translation) + " successfully setted as a translation of " + ElementsHelper.getQualifiedPath(mlContent));

                        getJournal().journalize(JournalLine.updateNode(
                                Status.SUCCESS,
                                ElementsHelper.getQualifiedPath(translation),
                                translationRef.toString(),
                                JournalLine.UpdateOperation.SET_TRANSLATION,
                                null
                            ));

                    }
                }
                else // if(pivotLanguage.equals(locale))
                {
                    // nothing to do, nothing to log. Process done in make multilingual
                }
            }
            catch(Throwable t)
            {
                getReport().appendSection("Impossible to add the traslation " + ElementsHelper.getQualifiedPath(translation) + " to the ml content" + ElementsHelper.getQualifiedPath(mlContent)  + ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.updateNode(
                            Status.FAIL,
                            ElementsHelper.getQualifiedPath(translation),
                            null,
                            JournalLine.UpdateOperation.SET_TRANSLATION,
                            null
                        ));
                }
            }

            return translationRef;
        }
    }

    //--- Stop recursion for the following nodes

    @Override
    public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

    @Override
    public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

    @Override
    public void visit(Content content) throws Exception{}

    @Override
    public void visit(Directory directory) throws Exception{}

    @Override
    public void visit(Discussions discussion) throws Exception{}

    @Override
    public void visit(Dossier dossier) throws Exception{}

    @Override
    public void visit(Events eventRoot) throws Exception{}

    @Override
    public void visit(InfContent content) throws Exception{}

    @Override
    public void visit(InterestGroup interestGroup, Application application) throws Exception{}

    @Override
    public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception{}

    @Override
	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception {}

    @Override
    public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception{}

    @Override
    public void visit(Link link) throws Exception{}

    @Override
    public void visit(Message message) throws Exception{}

    @Override
    public void visit(Newsgroups newsgroup) throws Exception{}

    @Override
    public void visit(Node node, ExtendedProperty property) throws Exception{}

    @Override
    public void visit(Node node, InformationUserRights permissions) throws Exception{}

    @Override
    public void visit(Node node, KeywordReferences keywords) throws Exception{}

    @Override
    public void visit(Node node, LibraryUserRights permissions) throws Exception{}

    @Override
    public void visit(Node node, NewsgroupUserRights permissions) throws Exception{}

    @Override
    public void visit(Node node, Notifications notifications) throws Exception{}

    @Override
    public void visit(Node node, TypedPreference preference) throws Exception{}

    @Override
    public void visit(Node node, TypedProperty property) throws Exception{}

    @Override
    public void visit(Persons persons) throws Exception{}

    @Override
    public void visit(SharedSpacelink link) throws Exception{}

    @Override
    public void visit(SimpleContent content) throws Exception{}

    @Override
    public void visit(Space node, Shared sharedProperties) throws Exception{}

    @Override
    public void visit(Surveys survey) throws Exception{}

    @Override
    public void visit(Topic topic) throws Exception{}

    @Override
    public void visit(Url url) throws Exception{}

    @Override
    public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}

    @Override
    public void visitLinkTarget(Link node, String reference) throws Exception{}

    @Override
    public void visitLocation(Node node, String uri) throws Exception{}

    @Override
    public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}

    @Override
    public void visit(LogFile logFile) throws Exception{}

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public final MultilingualContentService getMultilingualContentService()
    {
        return getRegistry().getAlfrescoServiceRegistry().getMultilingualContentService();
    }

}
