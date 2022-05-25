/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
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
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CircabcAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.ImportedProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.JournalLine.UpdateOperation;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.model.DossierModel;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * Import the container nodes (category, ig, spaces, forum, topic, ....) using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateSpaces extends MigrateProcessorBase
{
	@Override
	public void visit(final InterestGroup interestGroup) throws Exception
	{
		apply(new MigrateInterestGroupCallback(getJournal(), interestGroup));
		super.visit(interestGroup);
	}

	@Override
	public void visit(final Category category) throws Exception
	{
		apply(new MigrateCategoryCallback(getJournal(), category));
		super.visit(category);
	}

	@Override
	public void visit(final CategoryHeader header) throws Exception
	{
		apply(new MigrateHeaderCallback(getJournal(), header));
		super.visit(header);
	}

	@Override
	public void visit(final Circabc circabc) throws Exception
	{
		if(!ElementsHelper.isNodeCreated(circabc))
		{
			getJournal().getProcessReport().appendSection("Circabc root node must be created before launching the migration tool!");
		}

		super.visit(circabc);
	}

	@Override
	public void visit(final Space space) throws Exception
	{
		apply(new MigrateSpaceCallback(getJournal(), space));
		super.visit(space);
	}

	@Override
	public void visit(final InfSpace space) throws Exception
	{
		apply(new MigrateSpaceCallback(getJournal(), space));
		super.visit(space);
	}

	@Override
	public void visit(final Dossier dossier) throws Exception
	{
		apply(new MigrateDossierCallback(getJournal(), dossier));
		super.visit(dossier);
	}

	@Override
	public void visit(final Directory directory) throws Exception
	{
		apply(new MigrateDirectoryCallback(getJournal(), directory));
		super.visit(directory);
	}

	@Override
	public void visit(final Events eventRoot) throws Exception
	{
		apply(new MigrateEventRootCallback(getJournal(), eventRoot));
		super.visit(eventRoot);
	}

	@Override
	public void visit(final Information information) throws Exception
	{
		apply(new MigrateInformationCallback(getJournal(), information));
		super.visit(information);
	}

	@Override
	public void visit(final Library library) throws Exception
	{
		apply(new MigrateLibraryCallback(getJournal(), library));
		super.visit(library);
	}

	@Override
	public void visit(final Newsgroups newsgroup) throws Exception
	{
		apply(new MigrateNewsgroupCallback(getJournal(), newsgroup));
		super.visit(newsgroup);
	}

	@Override
	public void visit(final Surveys survey) throws Exception
	{
		apply(new MigrateSurveyCallback(getJournal(), survey));
		super.visit(survey);
	}

	@Override
	public void visit(final Forum forum) throws Exception
	{
		apply(new MigrateForumCallback(getJournal(), forum));
		super.visit(forum);
	}

    class MigrateHeaderCallback extends MigrateNodesCallback
    {
    	public MigrateHeaderCallback(final MigrationTracer journal, CategoryHeader space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef rootHeader = getManagementService().getRootCategoryHeader();
			final NodeRef headerRef;

			// Test if the header has been created at this time by an onther async process (Only for category and category header, else process will fail)
			final NodeRef headerByName = getNodeService().getChildByName(rootHeader, ContentModel.ASSOC_SUBCATEGORIES, getName());
			if(headerByName == null)
			{
				final CategoryService categoryService = getRegistry().getAlfrescoServiceRegistry().getCategoryService();
				headerRef = categoryService.createCategory(rootHeader, getName());
			}
			else
			{
				headerRef = headerByName;
			}

    		return headerRef;
    	}
    };

    class MigrateCategoryCallback extends MigrateNodesCallback
    {
    	public MigrateCategoryCallback(final MigrationTracer journal, Category space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef circabcRef = ElementsHelper.getNodeRef(importRoot.getCircabc());
			final NodeRef headerRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef catRef;

			// Test if the category has been created at this time by an onther async process (Only for category and category header, else process will fail)
			final NodeRef catByName = getNodeService().getChildByName(circabcRef, ContentModel.ASSOC_CONTAINS, getName());

			if(catByName == null)
			{
				catRef = getManagementService().createCategory(
						circabcRef,
						getName(),
						headerRef
					);
			}
			else
			{
				catRef = catByName;
			}

			return catRef;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_SPACE_ICON_NAME;
		}
    };

    class MigrateInterestGroupCallback extends MigrateNodesCallback
    {
    	public MigrateInterestGroupCallback(final MigrationTracer journal, InterestGroup space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef categoryRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createIGRoot(
					categoryRef,
					getName(),
					"",
					false
				);



    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_SPACE_ICON_NAME;
		}
    };

    class MigrateLibraryCallback extends MigrateNodesCallback
    {
    	public MigrateLibraryCallback(final MigrationTracer journal, Library space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createLibrary(igRef);
    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_LIBRARY_ICON_NAME;
		}
    };

    class MigrateInformationCallback extends MigrateNodesCallback
    {
    	public MigrateInformationCallback(final MigrationTracer journal, Information space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createInformationService(igRef);
    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_INFORMATION_ICON_NAME;
		}
    };

    class MigrateDirectoryCallback extends MigrateNodesCallback
    {
    	public MigrateDirectoryCallback(final MigrationTracer journal, Directory space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createDirectory(igRef);
    		return ref;
    	}
    };

    class MigrateEventRootCallback extends MigrateNodesCallback
    {
    	public MigrateEventRootCallback(final MigrationTracer journal, Events space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createEventService(igRef);
    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_EVENT_ICON_NAME;
		}
    };

    class MigrateNewsgroupCallback extends MigrateNodesCallback
    {
    	public MigrateNewsgroupCallback(final MigrationTracer journal, Newsgroups space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createNewsGroup(igRef);
    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_NEWSGROUP_ICON_NAME;
		}
    };

    class MigrateSurveyCallback extends MigrateNodesCallback
    {
    	public MigrateSurveyCallback(final MigrationTracer journal, Surveys space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			final NodeRef igRef = ElementsHelper.getParentNodeRef(space);
			final NodeRef ref = getManagementService().createSurvey(igRef);
    		return ref;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_SURVEY_ICON_NAME;
		}
    };


    class MigrateSpaceCallback extends MigrateNodesCallback
    {
    	public MigrateSpaceCallback(final MigrationTracer journal, Space space)
		{
			super(journal, space);
		}
    	public MigrateSpaceCallback(final MigrationTracer journal, InfSpace space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			return executeImplGeneric(space, ContentModel.TYPE_FOLDER);
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_SPACE_ICON_NAME;
		}
    };

    class MigrateDossierCallback extends MigrateNodesCallback
    {
    	public MigrateDossierCallback(final MigrationTracer journal, Dossier space)
		{
			super(journal, space);
		}

		public NodeRef executeImpl(Node space)throws Throwable
    	{
			return executeImplGeneric(space, DossierModel.TYPE_DOSSIER_SPACE);
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_DOSSIER_ICON_NAME;
		}
    };


    class MigrateForumCallback extends MigrateNodesCallback
    {
    	public MigrateForumCallback(final MigrationTracer journal, Forum forum)
		{
			super(journal, forum);
		}

		public NodeRef executeImpl(Node forum)throws Throwable
    	{
			final NodeRef forumRef = executeImplGeneric(forum, ForumModel.TYPE_FORUM);

			if(((Forum)forum).isModerated())
			{
				getRegistry().getModerationService().applyModeration(forumRef, false);

				final JournalLine journalLine = JournalLine.updateNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(forum), UpdateOperation.APPLY_MODERATION, Parameter.IS_MODERATED, Boolean.TRUE);
				super.getJournal().journalize(journalLine);
				getReport().appendSection(ElementsHelper.getQualifiedPath(forum) + " successfully updated being moderated. ");
			}

			return forumRef;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_FORUM_ICON_NAME;
		}
    };

    ///--- Stop recursion for the following nodes

    @Override
	public void visit(final Discussions discussion) throws Exception{}

    @Override
	public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

	@Override
	public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

	@Override
	public void visit(Content content) throws Exception{}

	@Override
	public void visit(Directory directory, ImportedProfile profile) throws Exception{}

	@Override
	public void visit(Directory directory, String name, AccessProfile profile) throws Exception{}

	@Override
	public void visit(Directory directory, String name, GlobalAccessProfile profile) throws Exception{}

	@Override
	public void visit(Directory directory, String name, ImportedProfile profile) throws Exception{}

	@Override
	public void visit(Events eventRoot, Event event) throws Exception{}

	@Override
	public void visit(Events eventRoot, Meeting meeting) throws Exception{}

	@Override
	public void visit(InfContent content) throws Exception{}

	@Override
	public void visit(InfMLContent mlContent) throws Exception{}

	@Override
	public void visit(InformationContentVersion contentVersion) throws Exception{}

	@Override
	public void visit(InformationTranslation translation) throws Exception{}

	@Override
	public void visit(InformationTranslationVersion translationVersion) throws Exception{}

	@Override
	public void visit(InterestGroup interestGroup, Application application) throws Exception{}

	@Override
	public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception{}

	@Override
	public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception{}

	@Override
	public void visit(LibraryContentVersion contentVersion) throws Exception{}

	@Override
	public void visit(LibraryTranslation translation) throws Exception{}

	@Override
	public void visit(LibraryTranslationVersion translationVersion) throws Exception{}

	@Override
	public void visit(Link link) throws Exception{}

	@Override
	public void visit(Message message) throws Exception{}

	@Override
	public void visit(MlContent mlContent) throws Exception{}

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
	public void visit(Person person) throws Exception{}

	@Override
	public void visit(Persons persons) throws Exception{}

	@Override
	public void visit(SharedSpacelink link) throws Exception{}

	@Override
	public void visit(SimpleContent content) throws Exception{}

	@Override
	public void visit(Space node, Shared sharedProperties) throws Exception{}

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

}
