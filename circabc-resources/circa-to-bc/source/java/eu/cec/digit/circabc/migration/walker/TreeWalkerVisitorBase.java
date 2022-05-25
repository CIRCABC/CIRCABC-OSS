/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/


package eu.cec.digit.circabc.migration.walker;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogEntry;
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
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersions;
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
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;


/**
 * Base tree walker for circabc binding classes graph. All graph will be walked by using the <i>walk()</i> method.
 *
 * @author Yanick Pignot
 */
public abstract class TreeWalkerVisitorBase implements XMLWalkerVisitor
{
	protected static final String SELECT_VERSIONS = "./versions";
	protected static final String SELECT_ALL_CHILDS = "./*";

	protected ImportRoot importRoot;
	protected JXPathContext circabcContext;
	protected JXPathContext personsContext;

	public TreeWalkerVisitorBase(final ImportRoot importRoot)
	{
		this.importRoot = importRoot;

		if(this.importRoot.getCircabc() != null)
		{
			this.circabcContext = JXPathContext.newContext(importRoot.getCircabc());
		}

		if(this.importRoot.getPersons() != null)
		{
			this.personsContext = JXPathContext.newContext(importRoot.getPersons());
		}
	}

	// allow child implementation the responsability to set fileds
	protected TreeWalkerVisitorBase()
	{
		this.importRoot = null;
		this.circabcContext = null;
		this.personsContext = null;
	}


	/**
	 * Start walking thought all the structure
	 *
	 * Same as
	 * 	<code>
	 * 		visit(getCircabc());
	 * 		visit(getPersons());
	 * 		visit(getLogFile());
	 * </code>
	 *
	 */
	public void walk() throws Exception
	{
		if(importRoot.getCircabc() == null && this.importRoot.getPersons() == null && this.importRoot.getLogFile() == null)
		{
			throw new NullPointerException("Either circabc root, persons or logFiles is mandatory.");
		}

		if(this.importRoot.getPersons() != null)
		{
			if(this.personsContext == null)
			{
				this.personsContext = JXPathContext.newContext(importRoot.getPersons());
			}

			visit(this.importRoot.getPersons());
		}

		if(this.importRoot.getCircabc() != null)
		{
			if(this.circabcContext == null)
			{
				this.circabcContext = JXPathContext.newContext(importRoot.getCircabc());
			}

			visit(this.importRoot.getCircabc());
		}

		if(this.importRoot.getLogFile() != null)
		{
			visit(this.importRoot.getLogFile());
		}
	}

	/**
	 * Start walking thought all the structure of a given Circanbc
	 *
	 * Same as <code>visit(getCircabc());</code>
	 *
	 */
	public void walk(final ImportRoot importRoot) throws Exception
	{
		this.importRoot = importRoot;

		walk();
	}

	/**
	 * Generic method to visit any child of a given xml node.
	 *
	 * @see eu.cec.digit.circabc.migration.entities.Node
	 *
	 * @param anyNode 	works any node
	 */
	protected void visitNodeChilds(final Node anyNode) throws Exception
	{
		final JXPathContext context = JXPathContext.newContext(anyNode); ;

		for(final Object child : context.selectNodes(SELECT_ALL_CHILDS))
		{
			if(child != null)
			{
				if(child instanceof Node)
				{
					((Node) child).accept(this);
				}
				else if(child instanceof LibraryContentVersions || child instanceof LibraryTranslationVersions || child instanceof InformationContentVersions || child instanceof InformationTranslationVersions)
				{
					final JXPathContext childContent = JXPathContext.newContext(child);
					for(final Object version : childContent.selectNodes(SELECT_VERSIONS))
					{
						((Node) version).accept(this);
					}
				}
				else if(child instanceof TypedProperty)
				{
					visit(anyNode, (TypedProperty) child);
				}
				else if(child instanceof ExtendedProperty)
				{
					visit(anyNode, (ExtendedProperty) child);
				}
			}
		}
	}

	/**
	 * Walk through a given Persons
	 *
	 * @see eu.cec.digit.circabc.migration.walker.NodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.user.Persons)
	 */
	public void visit(final Persons persons) throws Exception
	{
		if(persons.getPersons() != null)
		{
			for(Person person : persons.getPersons())
			{
				visit(person);
			}
		}
	}

	/**
	 * Walk through a given person
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLWalkerVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.user.Person)
	 */
	public void visit(final Person person) throws Exception
	{
		visitNodeChilds(person);
		visit(person, person.getContentLanguageFilter());
		visit(person, person.getInterfaceLanguage());
	}

	/**
	 * Walk through a given Circabc Root
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc)
	 */
	public void visit(final Circabc circabc) throws Exception
	{
		visit(circabc, circabc.getCircabcAdmin());

		visitNodeChilds(circabc);
	}

	/**
	 * Walk through log filesr
	 *
	 */
	public void visit(final LogFile logFile) throws Exception
	{
		if(logFile != null)
		{
			for(final LogEntry log: logFile.getLogEntries())
			{
				visit(log);
			}
		}
	}

	/**
	 * Walk through a given Category Header
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader)
	 */
	public void visit(final CategoryHeader header) throws Exception
	{
		visitNodeChilds(header);
	}

	/**
	 * Walk through a given Category
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Category)
	 */
	public void visit(final Category category) throws Exception
	{
		visit(category, category.getCategoryAdmin());

		visitNodeChilds(category);
	}



	/** walk through a given Interest Group
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup)
	 */
	public void visit(final InterestGroup interestGroup) throws Exception
	{
		visit(interestGroup, interestGroup.getNotifications());
		visit(interestGroup, interestGroup.getKeywordDefinitions());
		visit(interestGroup, interestGroup.getDynamicPropertyDefinitions());
		visit(interestGroup, interestGroup.getLogoDefinitions());
		if(interestGroup.getApplications() != null && interestGroup.getApplications().getApplications() != null)
		{
			for(Application application: interestGroup.getApplications().getApplications())
			{
				visit(interestGroup, application);
			}
		}


		visitNodeChilds(interestGroup);
	}

	/**
	 * Walk through a given Information IG Service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Information)
	 */
	public void visit(final Information information) throws Exception
	{
		visit(information, information.getNotifications());
		visitNodeChilds(information);
	}

	/**
	 * Walk through a given Library  IG Service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Library)
	 */
	public void visit(final Library library) throws Exception
	{
		visit(library, library.getNotifications());
		visitNodeChilds(library);
	}

	/**
	 * Walk through a given Directory IG Service
	 *
	 * @see eu.cec.digit.cNodemigration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Directory)
	 */
	public void visit(final Directory directory) throws Exception
	{
		if(directory.getGuest() != null)
		{
			visit(directory, CircabcConstant.GUEST_AUTHORITY, directory.getGuest());
		}
		if(directory.getRegistredUsers() != null)
		{
			visit(directory, CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME, directory.getRegistredUsers());
		}

		for(AccessProfile profile : directory.getAccessProfiles())
		{
			visit(directory, profile.getName(), profile);
		}
		for(ImportedProfile profile : directory.getImportedProfiles())
		{
			visit(directory, profile);
		}


		visitNodeChilds(directory);
	}

	/**
	 * Walk through a given Newsgroup IG Service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroup)
	 */
	public void visit(final Newsgroups newsgroup) throws Exception
	{
		visit(newsgroup, newsgroup.getNotifications());
		visitNodeChilds(newsgroup);
	}

	/**
	 * Walk through a given Events IG Service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.EventRoot)
	 */
	public void visit(final Events eventRoot) throws Exception
	{
		visitNodeChilds(eventRoot);
		for(final Meeting appointment : eventRoot.getMeetings())
		{
			visit(eventRoot, appointment);
		}
		for(final Event appointment : eventRoot.getEvents())
		{
			visit(eventRoot, appointment);
		}
	}

	/**
	 * Walk through a given Surveys IG Service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Survey)
	 */
	public void visit(final Surveys survey) throws Exception
	{
		visitNodeChilds(survey);
	}

	/**
	 * Walk through a given Event
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Event)
	 */
	public void visit(final Events eventRoot, final Event event) throws Exception
	{

	}

	/**
	 * Walk through a given meeting
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting)
	 */
	public void visit(final Events eventRoot, final Meeting meeting) throws Exception
	{
		visitLibrarytSection(meeting, meeting.getLibrarySection());
	}

	/**
	 * Walk through a given forum
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Forum)
	 */
	public void visit(final Forum forum) throws Exception
	{
		visit(forum, forum.getNewsgroupUserRights());
		visit(forum, forum.getNotifications());

		visitNodeChilds(forum);
	}

	/**
	 * Walk through a given topic
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Topic)
	 */
	public void visit(final Topic topic) throws Exception
	{
		visit(topic, topic.getNewsgroupUserRights());
		visit(topic, topic.getNotifications());

		visitNodeChilds(topic);
	}

	/**
	 * Walk through a given discussions
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions)
	 */
	public void visit(final Discussions discussion) throws Exception
	{
		visitNodeChilds(discussion);
	}

	/**
	 * Walk through a given message
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Message)
	 */
	public void visit(final Message message) throws Exception
	{
		visit(message, message.getNewsgroupUserRights());
		visit(message, message.getNotifications());

		visitNodeChilds( message);
	}

	/**
	 * Walk through a given content in the library
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Content)
	 */
	public void visit(final Content content) throws Exception
	{
		visitLocation(content, content.getUri());
		visit(content, content.getNotifications());
		visit(content, content.getLibraryUserRights());
		visit(content, content.getKeywords());

		visitNodeChilds( content);
	}

	/**
	 * Walk through a given content in the information service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent)
	 */
	public void visit(final InfContent content) throws Exception
	{
		visitLocation(content, content.getUri());
		visit(content, content.getNotifications());

		visitNodeChilds(content);
	}

	/**
	 * Walk through a given content in the information service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent)
	 */
	public void visit(final SimpleContent content) throws Exception
	{
		visitLocation(content, content.getUri());

		visitNodeChilds(content);
	}

	/**
	 * Walk through a given content version in the library
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion)
	 */
	public void visit(final LibraryContentVersion contentVersion) throws Exception
	{
		visitLocation(contentVersion, contentVersion.getUri());
		visit(contentVersion, contentVersion.getKeywords());

		visitNodeChilds( contentVersion);
	}


	/**
	 * Walk through a given content version in the information service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersion)
	 */
	public void visit(final InformationContentVersion contentVersion) throws Exception
	{
		visitLocation(contentVersion, contentVersion.getUri());
		visitNodeChilds( contentVersion);
	}

	/**
	 * Walk through a given translation in the library
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation)
	 */
	public void visit(final LibraryTranslation translation) throws Exception
	{
		visitLocation(translation, translation.getUri());
		visit(translation, translation.getNotifications());
		visit(translation, translation.getLibraryUserRights());
		visit(translation, translation.getKeywords());

		visitNodeChilds(translation);
	}


	/**
	 * Walk through a given translation in the information service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation)
	 */

	public void visit(final InformationTranslation translation) throws Exception
	{
		visitLocation(translation, translation.getUri());
		visit(translation, translation.getNotifications());

		visitNodeChilds( translation);
	}

	/**
	 * Walk through a given translation version in the library
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion)
	 */
	public void visit(final LibraryTranslationVersion translationVersion) throws Exception
	{
		visitLocation(translationVersion, translationVersion.getUri());
		visit(translationVersion, translationVersion.getKeywords());

		visitNodeChilds( translationVersion);
	}

	/**
	 * Walk through a given translation version in the information service
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion)
	 */

	public void visit(final InformationTranslationVersion translationVersion) throws Exception
	{
		visitLocation(translationVersion, translationVersion.getUri());
		visitNodeChilds( translationVersion);
	}

	/**
	 * Walk through a given Library Multilingual Content
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent)
	 */
	public void visit(final MlContent mlContent) throws Exception
	{
		visitNodeChilds( mlContent);
	}

	/**
	 * Walk through a given Information Multilingual Content
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent)
	 */
	public void visit(final InfMLContent mlContent) throws Exception
	{
		visitNodeChilds( mlContent);
	}

	/**
	 * Walk through a given Library Space
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Space)
	 */
	public void visit(final Space space) throws Exception
	{
		if(space.getShareds() != null)
		{
			for(Shared sharedProperty : space.getShareds())
			{
				visit(space, sharedProperty);
			}
		}
		visit(space, space.getLibraryUserRights());
		visit(space, space.getNotifications());
		visitNodeChilds( space);
	}

	/**
	 * Walk through a given Information Space
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.InfSpace)
	 */
	public void visit(final InfSpace space) throws Exception
	{
		visit(space, space.getInformationUserRights());
		visitNodeChilds( space);
	}

	/**
	 * Walk through a given URL
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Url)
	 */
	public void visit(final Url url) throws Exception
	{
		visit(url, url.getLibraryUserRights());
		visit(url, url.getNotifications());
		visitNodeChilds( url);
	}

	/**
	 * Walk through a given Link
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Link)
	 */
	public void visit(final Link link) throws Exception
	{
		visitLinkTarget(link, link.getReference());
		visit(link, link.getLibraryUserRights());
		visit(link, link.getNotifications());

		visitNodeChilds(link);
	}

	public void visit(SharedSpacelink link) throws Exception
	{
		visitSharedSpaceLinkTarget(link, link.getReference());
		visit(link, link.getLibraryUserRights());
		visit(link, link.getNotifications());

		visitNodeChilds(link);
	}

	/**
	 * Walk through a given Dossier
	 *
	 * @see eu.cec.digit.circabc.migration.walker.XMLNodesVisitor#visit(eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier)
	 */
	public void visit(final Dossier dossier) throws Exception
	{
		visit(dossier, dossier.getLibraryUserRights());
		visit(dossier, dossier.getNotifications());

		visitNodeChilds(dossier);
	}


	/**
	 * @return the circabc
	 */
	public final Circabc getCircabc()
	{
		return importRoot.getCircabc();
	}


	public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}
	public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}
	public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception{}
	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception {}
	public void visit(Directory directory, String name, AccessProfile profile) throws Exception{}
	public void visit(Directory directory, String name, GlobalAccessProfile profile) throws Exception{}
	public void visit(Directory directory, String name, ImportedProfile profile) throws Exception{}
	public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception{}
	public void visit(InterestGroup interestGroup, Application application) throws Exception{}
	public void visit(Node node, KeywordReferences keywords) throws Exception{}
	public void visit(Node node, LibraryUserRights permissions) throws Exception{}
	public void visit(final Node node, final InformationUserRights permissions) throws Exception{}
	public void visit(Node node, NewsgroupUserRights permissions) throws Exception{}
	public void visit(Node node, Notifications notifications) throws Exception{}
	public void visit(Node node, ExtendedProperty property) throws Exception{}
	public void visit(Node node, TypedProperty property) throws Exception{}
	public void visit(Space node, Shared sharedProperties) throws Exception{}
	public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}
	public void visitLinkTarget(Link node, String reference) throws Exception{}
	public void visitLocation(Node node, String uri) throws Exception{}
	public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}
	public void visit(Node node, TypedPreference preference) throws Exception{}
	public void visit(Directory directory, ImportedProfile profile) throws Exception{}
	public void visit(LogEntry logEntry) throws Exception{}

}
