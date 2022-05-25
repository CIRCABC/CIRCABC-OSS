/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalTime;
import org.joda.time.TimeOfDay;
import org.springframework.core.io.Resource;

import com.google.ical.values.DateValue;

import eu.cec.digit.circabc.migration.entities.ElementsConverter;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ContactInfoProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DescriptionProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.TitleProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
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
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CircabcAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.PreProcessor;
import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.migration.walker.TreeWalkerVisitorBase;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.event.AppointmentUtils;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.event.EventService;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * Recurse on each node and prepear it to be created or updated
 *
 * @author Yanick Pignot
 */
public class PrepareNodes extends TreeWalkerVisitorBase implements PreProcessor
{
	private static final Log logger = LogFactory.getLog(PrepareNodes.class);
	
	private ManagementService managementService;
	private MultilingualContentService multilingualContentService;
	private NodeService nodeService;
	private CategoryService categoryService;
	private PersonService personService;
	private VersionService versionService;
	private ContentService contentService;
	private ResourceManager resourceManager;
	private EventService eventService;

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.walker.impl.PrepareObjectGraph#beforeProcess(eu.cec.digit.circabc.migration.entities.generated.ImportRoot)
	 */
	public void beforeProcess(final CircabcServiceRegistry registry, final ImportRoot importRoot, final MigrationTracer journal) throws ImportationException
	{
		ParameterCheck.mandatory("The service registry", registry);
		ParameterCheck.mandatory("The root element", importRoot);
		ParameterCheck.mandatory("An instance of the importation journal", journal);
        ParameterCheck.mandatory("An instance of the report", journal.getProcessReport());

        final ServiceRegistry alfRegistry = registry.getAlfrescoServiceRegistry();

        managementService = registry.getManagementService();
		multilingualContentService = alfRegistry.getMultilingualContentService();
    	nodeService = alfRegistry.getNodeService();
    	categoryService = alfRegistry.getCategoryService();
    	personService = alfRegistry.getPersonService();
    	versionService = alfRegistry.getVersionService();
    	contentService = alfRegistry.getContentService();
    	resourceManager = ResourceManager.getInstance(registry);
    	eventService = registry.getEventService();

		try
    	{
			walk(importRoot);
    	}
    	catch(ImportationException ex)
    	{
    		throw ex;
    	}
    	catch(final Throwable t)
    	{
    		if(logger.isErrorEnabled()) {
				logger.error("Unexpected error when preparing nodes before importation process", t);
    		}
    		throw new ImportationException("Unexpected error when preparing nodes before importation process", t);
    	}

    	onFinish();

	}

	public void onFinish()
	{
		importRoot = null;
		circabcContext = null;
		personsContext = null;
	}


	@Override
	protected void visitNodeChilds(final Node anyNode) throws Exception
	{
		check18NProperties(anyNode);
		super.visitNodeChilds(anyNode);
	}

	/**
	 * Compute the nodeRef of a node.
	 *
	 * identique as computeChildNoderef(parent:child, child);
	 *
	 * @param child
	 */
	private void computeChildNoderef(final Node child)
	{
		final Node parent = ElementsHelper.getNodeParent(child);
		computeChildNoderef(parent, child);
	}


	/**
	 * Compute the noderef of node with a given parent. Usefull when the primary parent of a node is not the same in the schema definition.
	 *
	 * @param parent
	 * @param child
	 */
	private void computeChildNoderef(final Node parent, final Node child)
	{
		if(ElementsHelper.isNodeCreated(parent))
		{
			final String childName = ElementsHelper.getQualifiedName(child);
			final NodeRef parentRef = ElementsHelper.getNodeRef(parent);
			final NodeRef ref = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, childName);
			if(ref != null)
			{
				ElementsHelper.setNodeRef(child, ref);
			}
		}
		// else{} if not exists, child not exists too.
	}

	/**
	 * Compute the noderef of a message with a given topic according the content.
	 *
	 * @param parent
	 * @param child
	 */
	private void computeMessageNoderef(final Topic parent, final Message child)
	{
		if(ElementsHelper.isNodeCreated(parent))
		{
			String messageContent;
			final String uri = child.getUri();
			if(uri != null && uri.length() > 0)
			{
				try
				{
					final Resource resource = resourceManager.adaptRessource(uri);
					messageContent = convertStreamToString(resource.getInputStream());
				}
				catch (Exception e)
				{
					messageContent = null;
				}

			}
			else
			{
				messageContent = child.getContent();
			}

			if(messageContent != null)
			{
				ContentReader childReader;
				String childMessage;
				NodeRef ref;
				for(final ChildAssociationRef association: nodeService.getChildAssocs(parent.getNodeReference()))
				{
					ref = association.getChildRef();
					childReader = contentService.getReader(ref, ContentModel.PROP_CONTENT);
					childMessage = childReader.getContentString();

					if(messageContent.equals(childMessage))
					{
						ElementsHelper.setNodeRef(child, ref);
						break;
					}
				}
			}
		}
		// else{} if not exists, child not exists too.
	}

	private String convertStreamToString(InputStream is) throws IOException
	{
		if (is == null)
		{
			return null;
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			String line;

			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null)
				{
					sb
						.append(line)
						.append("\n");
				}
			}
			finally
			{
				is.close();
			}

			return sb.toString();
		}
	}

	/**
	 * Compute the noderef of a translation where the primary parent is a space in the repository but is a mlContent in the xsd.
	 *
	 * @param translation
	 */
	private void computeTranslationNodeRef(Node translation)
	{
		if(ElementsHelper.isNodeCreated(translation) == false)
		{
			final Node mlContainer = ElementsHelper.getNodeParent(translation);
			final Node space = ElementsHelper.getNodeParent(mlContainer);
			computeChildNoderef(space, translation);
		}
		//else already create by computeMLContainerNodeRef method
	}

	private void computeMLContainerNodeRef(Node mlContainer)
	{
		final Node anyTranslation;

		if(mlContainer instanceof MlContent)
		{
			anyTranslation = ((MlContent)mlContainer).getTranslations().get(0);
		}
		else
		{
			anyTranslation = ((InfMLContent)mlContainer).getTranslations().get(0);
		}

		computeTranslationNodeRef(anyTranslation);

		if(ElementsHelper.isNodeCreated(anyTranslation) && !ElementsHelper.isNodeCreated(mlContainer) )
		{
			final NodeRef translationRef = ElementsHelper.getNodeRef(anyTranslation);
			
			if(multilingualContentService.isTranslation(translationRef))
			{
				final NodeRef containerRef = multilingualContentService.getTranslationContainer(translationRef);
				ElementsHelper.setNodeRef(mlContainer, containerRef);	
			}				
			// else{} the last import failed before making document ml.
		}

	}

	private void computeDiscussionChildNoderef(Discussions discussion)
	{
		final Node content = ElementsHelper.getNodeParent(discussion);
		if(ElementsHelper.isNodeCreated(content))
		{
			final NodeRef contentRef = ElementsHelper.getNodeRef(content);
			NodeRef forumRef = null;
			for(final ChildAssociationRef assoc:nodeService.getChildAssocs(contentRef))
			{
				forumRef = assoc.getChildRef();

				if(ForumModel.TYPE_FORUM.equals(nodeService.getType(forumRef)))
				{
					ElementsHelper.setNodeRef(discussion, forumRef);
					break;
				}
			}
		}
	}

	/**
	 * Set the I18NProperties (aka MLText) in the corresponding property type.
	 *
	 * @param anyNode
	 */
	private void check18NProperties(final Node anyNode)
	{
		if(anyNode instanceof InterestGroup)
		{
			final InterestGroup interestGroup = (InterestGroup) anyNode;
			final List<I18NProperty> contactInfos = interestGroup.getI18NContactInfos();
			final ContactInfoProperty singleContactInfo = interestGroup.getContactInfo();

			if(contactInfos != null && contactInfos.size() > 0)
			{
				interestGroup.setContactInfo(new TypedProperty.ContactInfoProperty(ElementsConverter.adpatI18NProperties(contactInfos)));
			}
			else if(singleContactInfo != null && singleContactInfo.getValue() != null)
			{
				interestGroup.setContactInfo(new TypedProperty.ContactInfoProperty(new MLText(singleContactInfo.getValue().toString())));
			}
		}
		if(anyNode instanceof TitledNode)
		{
			final TitledNode titledNode = (TitledNode) anyNode;
			final List<I18NProperty> titles = titledNode.getI18NTitles();
			final TitleProperty singleTitle = titledNode.getTitle();
			final List<I18NProperty> descriptions = titledNode.getI18NDescriptions();
			final DescriptionProperty singleDescription = titledNode.getDescription();

			if(titles != null && titles.size() > 0)
			{
				titledNode.setTitle(new TypedProperty.TitleProperty(ElementsConverter.adpatI18NProperties(titles)));
			}
			else if(singleTitle != null && singleTitle.getValue() != null)
			{
				titledNode.setTitle(new TypedProperty.TitleProperty(new MLText(singleTitle.getValue().toString())));
			}

			if(descriptions != null && descriptions.size() > 0)
			{
				titledNode.setDescription(new TypedProperty.DescriptionProperty(ElementsConverter.adpatI18NProperties(descriptions)));
			}
			else if(singleDescription != null && singleDescription.getValue() != null)
			{
				titledNode.setDescription(new TypedProperty.DescriptionProperty(new MLText(singleDescription.getValue().toString())));
			}
		}
	}

	/**
	 * Set the I18NProperties (aka MLText) in the corresponding property type.
	 *
	 * @param anyNode
	 */
	private void check18NProperties(final AccessProfile profile)
	{
		final List<I18NProperty> titles = profile.getI18NTitles();
		final TitleProperty singleTitle = profile.getTitle();
		final List<I18NProperty> descriptions = profile.getI18NDescriptions();
		final DescriptionProperty singleDescription = profile.getDescription();

		if(titles != null && titles.size() > 0)
		{
			profile.setTitle(new TypedProperty.TitleProperty(ElementsConverter.adpatI18NProperties(titles)));
		}
		else if(singleTitle != null && singleTitle.getValue() != null)
		{
			profile.setTitle(new TypedProperty.TitleProperty(new MLText(singleTitle.getValue().toString())));
		}

		if(descriptions != null && descriptions.size() > 0)
		{
			profile.setDescription(new TypedProperty.DescriptionProperty(ElementsConverter.adpatI18NProperties(descriptions)));
		}
		else if(singleDescription != null && singleDescription.getValue() != null)
		{
			profile.setDescription(new TypedProperty.DescriptionProperty(new MLText(singleDescription.getValue().toString())));
		}
	}

	@Override
	public void visit(Directory directory, String name, AccessProfile profile) throws Exception
	{
		check18NProperties(profile);
	}

	@Override
	public void visit(Category category) throws Exception
	{
		computeChildNoderef(getCircabc(), category);
		super.visit(category);
	}

	@Override
	public void visit(CategoryHeader header) throws Exception
	{
		// in reporsitory, category headers are not direct children of circabc
		final NodeRef rootHeader = managementService.getRootCategoryHeader();
		final Collection<ChildAssociationRef> headers = categoryService.getChildren(rootHeader, Mode.ALL, Depth.IMMEDIATE);

		final String qualifiedName = ElementsHelper.getQualifiedName(header);

		NodeRef iterNodeRef;
		Serializable iterName;

		for (ChildAssociationRef assoc : headers)
		{
			iterNodeRef = assoc.getChildRef();
			iterName = nodeService.getProperty(iterNodeRef, ContentModel.PROP_NAME);

			if(iterName != null && iterName.equals(qualifiedName))
			{
				ElementsHelper.setNodeRef(header, iterNodeRef);
				break;
			}
		}

		super.visit(header);
	}

	@Override
	public void visit(Circabc circabc) throws Exception
	{
		// circabc is an unique node. Get it directly.
		final NodeRef noderef = managementService.getCircabcNodeRef();
		ElementsHelper.setNodeRef(circabc, noderef);

		super.visit(circabc);
	}

	@Override
	public void visit(Content content) throws Exception
	{
		computeChildNoderef(content);

		final List<LibraryContentVersion> otherVersions = (content.getVersions() == null) ? null : content.getVersions().getVersions();
		if(shouldVersionsMustBeDeleted(content, otherVersions))
		{
			content.setVersions(null);
		}

		super.visit(content);
	}

	@Override
	public void visit(InfContent content) throws Exception
	{
		computeChildNoderef(content);

		final List<InformationContentVersion> otherVersions = (content.getVersions() == null) ? null : content.getVersions().getVersions();
		if(shouldVersionsMustBeDeleted(content, otherVersions))
		{
			content.setVersions(null);
		}

		super.visit(content);
	}

	@Override
	public void visit(LibraryTranslation translation) throws Exception
	{
		computeTranslationNodeRef(translation);

		final List<LibraryTranslationVersion> otherVersions = (translation.getVersions() == null) ? null : translation.getVersions().getVersions();
		if(shouldVersionsMustBeDeleted(translation, otherVersions))
		{
			translation.setVersions(null);
		}

		super.visit(translation);
	}

	@Override
	public void visit(InformationTranslation translation) throws Exception
	{
		computeTranslationNodeRef(translation);

		final List<InformationTranslationVersion> otherVersions = (translation.getVersions() == null) ? null : translation.getVersions().getVersions();
		if(shouldVersionsMustBeDeleted(translation, otherVersions))
		{
			translation.setVersions(null);
		}

		super.visit(translation);
	}

	/**
	 * Since it's impossible to determine if existing version matches exactly with one defined in xml. This rule is applied:
	 *
	 * If the current version is created, and if it defines at least one other version, the XML defined versions will be lost.
	 *
	 *
	 * @param currentVersions
	 * @param otherVersions
	 * @return
	 */
	private boolean shouldVersionsMustBeDeleted(final ContentNode currentVersions, final List<? extends Node> otherVersions)
	{
		boolean should = false;


		if(ElementsHelper.isNodeCreated(currentVersions)
				&& otherVersions != null
				&& otherVersions.size() > 0)
		{
			// the current version is already created in the repository
			// and the content xml definition contains versions
			final NodeRef currentVersionRef = ElementsHelper.getNodeRef(currentVersions);
			final Version version = versionService.getCurrentVersion(currentVersionRef);
			if(version != null)
			{
				// we are sure now that the created node is versionalble
				int numberOfVersions = versionService.getVersionHistory(currentVersionRef).getAllVersions().size();

				if(numberOfVersions > 1)
				{
					should = true;
				}
			}
		}

		return should;
	}

	@Override
	public void visit(Directory directory) throws Exception
	{
		// special case, directory root is not named node
		final Node igRoot = ElementsHelper.getNodeParent(directory);
		if(ElementsHelper.isNodeCreated(igRoot))
		{
			final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(ElementsHelper.getNodeRef(igRoot));

			NodeRef iterRef;
			QName iterQName;
			for (ChildAssociationRef assoc : assocs)
			{
				iterRef = assoc.getChildRef();
				iterQName = nodeService.getType(iterRef);
				if(CircabcModel.TYPE_DIRECTORY_SERVICE.equals(iterQName))
				{
					ElementsHelper.setNodeRef(directory, iterRef);
					break;
				}
			}
		}
		super.visit(directory);
	}

	@Override
	public void visit(Dossier dossier) throws Exception
	{
		computeChildNoderef(dossier);
		super.visit(dossier);
	}

	@Override
	public void visit(final Events eventRoot, final Event event) throws Exception
	{
		super.visit(eventRoot, event);
	}

	@Override
	public void visit(Events eventRoot) throws Exception
	{
		computeChildNoderef(eventRoot);

		if(ElementsHelper.isNodeCreated(eventRoot))
		{
			final NodeRef eventRootRef = ElementsHelper.getNodeRef(eventRoot);
			final List<eu.cec.digit.circabc.service.event.Appointment> repoAppointements = eventService.getAllAppointments(eventRootRef);
			final List<Meeting> xmlMeetings = eventRoot.getMeetings();
			final List<Event> xmlEvents = eventRoot.getEvents();
			final List<Appointment> xmlAppointements = new ArrayList<Appointment>(xmlMeetings.size() + xmlEvents.size());
			xmlAppointements.addAll(xmlMeetings);
			xmlAppointements.addAll(xmlEvents);

			if(repoAppointements != null && repoAppointements.size() > 0 && xmlAppointements.size() > 0)
			{
				final Map<String, Appointment> appByTitle = new HashMap<String, Appointment>(xmlAppointements.size());
				for(Appointment xmlApp: xmlAppointements)
				{
					appByTitle.put(xmlApp.getAppointmentTitle(), xmlApp);
				}

				for(eu.cec.digit.circabc.service.event.Appointment repoApp: repoAppointements)
				{
					final Appointment matchedApp = appByTitle.get(repoApp.getTitle());
					if(matchedApp != null)
					{
						final DateValue xmlDate = AppointmentUtils.convertDateToDateValue(matchedApp.getStartDate());
						final LocalTime xmlTime = AppointmentUtils.convertDateToLocalTime(matchedApp.getStartTime());
						if(xmlDate.equals(repoApp.getStartDate())&& xmlTime.equals(repoApp.getStartTime()))
						{

							// if the appointement is same type (Event or meeting) and if the title is equals and start at the same time,
							// don't create it once.
							if(matchedApp instanceof Meeting && repoApp instanceof eu.cec.digit.circabc.service.event.Meeting)
							{
								eventRoot.getMeetings().remove(matchedApp);
							}
							else if (matchedApp instanceof Event && repoApp instanceof eu.cec.digit.circabc.service.event.Event)
							{
								eventRoot.getEvents().remove(matchedApp);
							}
						}
					}
				}
			}
		}

		super.visit(eventRoot);
	}

	@Override
	public void visit(Forum forum) throws Exception
	{
		computeChildNoderef(forum);
		super.visit(forum);
	}

	@Override
	public void visit(InfMLContent mlContent) throws Exception
	{
		computeMLContainerNodeRef(mlContent);
		super.visit(mlContent);
	}

	@Override
	public void visit(Information information) throws Exception
	{
		computeChildNoderef(information);
		super.visit(information);
	}

	@Override
	public void visit(InfSpace space) throws Exception
	{
		computeChildNoderef(space);
		super.visit(space);
	}

	@Override
	public void visit(InterestGroup interestGroup) throws Exception
	{
		computeChildNoderef(interestGroup);
		super.visit(interestGroup);
	}

	@Override
	public void visit(Library library) throws Exception
	{
		computeChildNoderef(library);
		super.visit(library);
	}

	@Override
	public void visit(Link link) throws Exception
	{
		computeChildNoderef(link);
		super.visit(link);
	}

	@Override
	public void visit(SharedSpacelink link) throws Exception
	{
		computeChildNoderef(link);
		super.visit(link);
	}

	@Override
	public void visit(final Events eventRoot, final Meeting meeting) throws Exception
	{
		super.visit(eventRoot, meeting);
	}

	@Override
	public void visit(Message message) throws Exception
	{
		final Topic topic = (Topic) ElementsHelper.getElementTopic(message);
		computeMessageNoderef(topic, message);

		super.visit(message);
	}

	@Override
	public void visit(MlContent mlContent) throws Exception
	{
		computeMLContainerNodeRef(mlContent);
		super.visit(mlContent);
	}

	@Override
	public void visit(Newsgroups newsgroup) throws Exception
	{
		computeChildNoderef(newsgroup);
		super.visit(newsgroup);
	}

	@Override
	public void visit(Space space) throws Exception
	{
		computeChildNoderef(space);
		super.visit(space);
	}

	@Override
	public void visit(Surveys survey) throws Exception
	{
		computeChildNoderef(survey);
		super.visit(survey);
	}

	@Override
	public void visit(Topic topic) throws Exception
	{
		computeChildNoderef(topic);
		super.visit(topic);
	}

	@Override
	public void visit(Discussions discussion) throws Exception
	{
		computeDiscussionChildNoderef(discussion);
		super.visit(discussion);
	}

	@Override
	public void visit(Url url) throws Exception
	{
		computeChildNoderef(url);
		super.visit(url);
	}

	public void visit(Person person) throws Exception
	{
		if(person != null)
		{
			final String userName = (String) person.getUserId().getValue();
			if(personService.personExists(userName))
			{
				final NodeRef ref = personService.getPerson(userName);
				if(ref != null)
				{
					ElementsHelper.setNodeRef(person, ref);
				}
			}
		}

		super.visit(person);
	}

	@Override
    public void visit(LibraryTranslationVersion contentNode) throws Exception
	{
		super.visit(contentNode);
	}

	@Override
	public void visit(InformationTranslationVersion contentNode) throws Exception
	{
		super.visit(contentNode);
	}

	@Override
	public void visit(LibraryContentVersion contentNode) throws Exception
	{
		super.visit(contentNode);
	}

	@Override
	public void visit(InformationContentVersion contentNode) throws Exception
	{
		super.visit(contentNode);
	}

	public void visit(Node node, ExtendedProperty property) throws Exception
	{
		ElementsHelper.addProperty(node, property.getQname(), property.getValue());
	}

	public void visit(Node node, TypedProperty property) throws Exception
	{
		ElementsHelper.addProperty(node, property.getIdentifier(), property.getValue());
	}

	public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception {}

	public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception {}

	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception {}

	public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

	public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

	public void visit(Directory directory, String name, GlobalAccessProfile profile) throws Exception{}

	public void visit(Node node, KeywordReferences keywords) throws Exception{}

	public void visit(Node node, LibraryUserRights permissions) throws Exception{}

	public void visit(Node node, NewsgroupUserRights permissions) throws Exception{}

	public void visit(Node node, Notifications notifications) throws Exception{}

	public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}

	public void visitLinkTarget(Link node, String reference) throws Exception{}

	public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}

	public void visit(Node node, TypedPreference preference) throws Exception{}

	public void visitLocation(Node node, String uri) throws Exception{}

	public void visit(Space node, Shared sharedProperties) throws Exception{}
}
