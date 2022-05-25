/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/


package eu.cec.digit.circabc.migration.processor.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
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
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.JournalLine.UpdateOperation;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.impl.MigrateContents.MigrateContentCallback;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * Import the Content discussion nodes (discussions, topics, messages) using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateDiscussions extends MigrateProcessorBase
{

	@Override
	public void visit(final Topic topic) throws Exception
	{
		apply(new MigrateTopicCallback(getJournal(), topic));
		super.visit(topic);
	}

	@Override
	public void visit(final Message message) throws Exception
    {
		apply(new MigrateMessageCallback(getMigrateContents(), getJournal(), message));
    	super.visit(message);
    }

	@Override
	public void visit(final Discussions discussion) throws Exception
	{
		apply(new MigrateDiscussionCallback(getJournal(), discussion));
		super.visit(discussion);
	}

	@Override
	public void visit(final SimpleContent content) throws Exception
	{
		apply(new MigrateAttachmentCallback(getMigrateContents(), getJournal(), content));
		super.visit(content);
	}

    class MigrateDiscussionCallback extends MigrateNodesCallback
    {
    	public MigrateDiscussionCallback(final MigrationTracer journal, final Discussions discussion)
		{
			super(journal, discussion);
		}

		public NodeRef executeImpl(final Node discussion)throws Throwable
    	{
			final NodeRef contentRef = ElementsHelper.getParentNodeRef(discussion);

			NodeRef forumRef = null;

			if (getNodeService().hasAspect(contentRef, ForumModel.ASPECT_DISCUSSABLE))
			{
				forumRef = getDiscussionNodeRef(contentRef);
			}

			if(forumRef == null)
			{
				//add the discussable aspect
				getNodeService().addAspect(contentRef, ForumModel.ASPECT_DISCUSSABLE, null);

				// alfresco version 3.4.6 changed when adding aspect we do not need to create node
				List<ChildAssociationRef> destChildren = getNodeService().getChildAssocs(
						contentRef,
		              ForumModel.ASSOC_DISCUSSION,
		              RegexQNamePattern.MATCH_ALL);
		        if (destChildren.size() == 0)
		        {
		           throw new CircabcRuntimeException("The discussable aspect behaviour is not creating a topic");
		        }
		        else
		        {
		           ChildAssociationRef discussionAssoc = destChildren.get(0);
		           forumRef = discussionAssoc.getChildRef();
		        }
			}

			return forumRef;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_FORUM_ICON_NAME;
		}

		private NodeRef getDiscussionNodeRef(final NodeRef contentRef)
		{
			NodeRef forumRef = null;
			for(final ChildAssociationRef assoc: getNodeService().getChildAssocs(contentRef))
			{
				if(ForumModel.TYPE_FORUM.equals(getNodeService().getType(assoc.getChildRef())))
				{
					forumRef = assoc.getChildRef();
					break;
				}
			}
			return forumRef;
		}

    };

    class MigrateTopicCallback extends MigrateNodesCallback
    {
    	private final ModerationService moderationService;

    	public MigrateTopicCallback(final MigrationTracer journal, Topic topic)
		{
			super(journal, topic);
			this.moderationService = getRegistry().getModerationService();
		}

		public NodeRef executeImpl(Node topic)throws Throwable
    	{
			final NodeRef topicRef = executeImplGeneric(topic, ForumModel.TYPE_TOPIC);

			if(((Topic)topic).isModerated())
			{
				moderationService.applyModeration(topicRef, false);

				final JournalLine journalLine = JournalLine.updateNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(topic), UpdateOperation.APPLY_MODERATION, Parameter.IS_MODERATED, Boolean.TRUE);
				super.getJournal().journalize(journalLine);
				getReport().appendSubSection(ElementsHelper.getQualifiedPath(topic) + " successfully updated being moderated. ");
			}

			return topicRef;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_TOPIC_ICON_NAME;
		}
    };

    class MigrateMessageCallback extends MigrateContentCallback
    {
    	private final ModerationService moderationService;

		public MigrateMessageCallback(MigrateContents migrateContents, MigrationTracer journal, Node currentVersion)
		{
			migrateContents.super(journal, currentVersion, null);
			this.moderationService = getRegistry().getModerationService();
		}

		public NodeRef executeImpl(final Node message)throws Throwable
    	{
			final NodeRef messageNodeRef = super.executeImpl(message);

			final Node parent = (Node) ElementsHelper.getParent(message);
			final Topic topic;

			if(parent instanceof Topic)
			{
				topic = (Topic) parent;
			}
			else
			{
				topic = ElementsHelper.getElementTopic(parent);
				applyReply(message, messageNodeRef, parent);
			}

			applyModerationStatus((Message) message, messageNodeRef, topic);

		    return messageNodeRef;
    	}

		/**
		 * @param message
		 * @param messageNodeRef
		 * @param parent
		 * @throws InvalidNodeRefException
		 * @throws InvalidAspectException
		 * @throws AssociationExistsException
		 */
		private void applyReply(final Node message, final NodeRef messageNodeRef, final Node parent) throws InvalidNodeRefException, InvalidAspectException, AssociationExistsException
		{
			final NodeRef parentNodeRef = ElementsHelper.getNodeRef(parent);

			getNodeService().addAspect(messageNodeRef, ContentModel.ASPECT_REFERENCING, null);
			getNodeService().createAssociation(messageNodeRef, parentNodeRef, ContentModel.ASSOC_REFERENCES);

			getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " successfully referencing the source post: " + parentNodeRef);

			final JournalLine journalLine = JournalLine.updateNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(message), UpdateOperation.POST_REPLY_OF, Parameter.TARGET, parentNodeRef);
			super.getJournal().journalize(journalLine);
		}

		/**
		 * @param messageNode
		 * @param messageNodeRef
		 * @param topic
		 * @throws InvalidNodeRefException
		 */
		private void applyModerationStatus(final Message message, final NodeRef messageNodeRef, final Topic topic) throws InvalidNodeRefException
		{
			if(topic.isModerated())
			{
				final Parameter journalParam;

				if(message.getAccepted() != null)
				{
					journalParam = JournalLine.Parameter.MODERATION_STATUS_ACCEPTED;

					final String moderator = message.getAccepted().getModerator();
					final Date date = message.getAccepted().getDate();

					moderationService.accept(messageNodeRef);

					getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " successfully accepted.");

					if(moderator != null && moderator.length() > 0)
					{
						getNodeService().setProperty(messageNodeRef, ModerationModel.PROP_APPROVED_BY, moderator);

						getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " approved by " + moderator);
					}
					if(date != null)
					{
						getNodeService().setProperty(messageNodeRef, ModerationModel.PROP_APPROVED_ON, date);

						getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " approved on " + moderator);
					}


				}
				else if(message.getRefused() != null)
				{
					journalParam = JournalLine.Parameter.MODERATION_STATUS_REJECTED;

					final String moderator = message.getRefused().getModerator();
					final Date date = message.getRefused().getDate();
					final String reason = message.getRefused().getReason();

					moderationService.reject(messageNodeRef, reason);

					getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " successfully rejected with reason: " + reason);

					if(moderator != null && moderator.length() > 0)
					{
						getNodeService().setProperty(messageNodeRef, ModerationModel.PROP_REJECT_BY, moderator);

						getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " rejected by " + moderator);
					}
					if(date != null)
					{
						getNodeService().setProperty(messageNodeRef, ModerationModel.PROP_REJECT_ON, date);

						getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " rejected on " + date);
					}
				}
				else
				{
					journalParam = JournalLine.Parameter.MODERATION_STATUS_PENDING;

					moderationService.waitForApproval(messageNodeRef);

					getReport().appendSubSection(ElementsHelper.getQualifiedPath(message) + " waiting for approval.");
				}

				final JournalLine journalLine = JournalLine.updateNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(message), UpdateOperation.APPLY_MODERATION, journalParam, Boolean.TRUE);
				super.getJournal().journalize(journalLine);

			}
		}

		@Override
		protected QName getType()
		{
			return ForumModel.TYPE_POST;
		}

		@Override
		protected String getName()
        {
			final StringBuilder name = new StringBuilder("posted-");

			// add a timestamp
	    	final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
	    	name
	    		.append(dateFormat.format(getMoreRelevantDate()))
	    		.append("-")
	    		.append(UUID.randomUUID())
	    		.append(".html");

	    	return name.toString();
	    }

		private Date getMoreRelevantDate()
		{
			final Message message = (Message) this.node;

			if(message.getCreated() != null && message.getCreated().getValue() != null)
			{
				return (Date) message.getCreated().getValue();
			}
			else if(message.getModified() != null && message.getModified().getValue() != null)
			{
				return (Date) message.getModified().getValue();
			}
			else
			{
				return new Date();
			}
		}

		@Override
		protected InputStream getInpuStreamFromResource(final Node content) throws Exception
	    {
	    	if(content instanceof Message)
	    	{
	    		final Message message = (Message) content;

	    		final String uri = message.getUri();
				if(uri != null && uri.length() > 0)
	    		{
	    			final Resource resource = resourceManager.adaptRessource(uri);
		    		return resource.getInputStream();
	    		}
				else
				{
					final String contentString = message.getContent();
					final byte[] messageAsBytes = (contentString == null) ? new byte[]{} : contentString.getBytes();
					return  new ByteArrayInputStream(messageAsBytes);
				}

	    	}
	    	else
	    	{
	    		return super.getInpuStreamFromResource(content);
	    	}
	    }
    };

    class MigrateAttachmentCallback extends MigrateContentCallback
    {
    	private final AttachementService attachementService;

		public MigrateAttachmentCallback(MigrateContents migrateContents, MigrationTracer journal, SimpleContent simpleContent)
		{
			migrateContents.super(journal, simpleContent, null);
			this.attachementService = getRegistry().getAttachementService();
		}

		public NodeRef executeImpl(Node simpleContent)throws Throwable
    	{
			final NodeRef referer = ElementsHelper.getParentNodeRef(simpleContent);
			final String name = getName();
			NodeRef refered = null;

			for(final NodeRef ref: attachementService.getAttachements(referer))
			{
				if(attachementService.isHiddenAttachement(ref) && getNodeService().getProperty(ref, ContentModel.PROP_NAME).equals(name))
				{
					refered = ref;
					break;
				}
			}

			if(refered == null)
			{
				final InputStream is = getInpuStreamFromResource(simpleContent);

				final String mimetype = getMimeTypeForFileName(getName());
				final String encoding = guessEncoding(is, mimetype);

				refered = attachementService.attach(referer, is, name, encoding, mimetype);
			}
			else
			{
				getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " already attached to the post.");
			}
			return refered;
    	}

		public String getIcon()throws Throwable
		{
			return ManagementService.DEFAULT_TOPIC_ICON_NAME;
		}
    };

    //--- Stop recursion for the following nodes

    @Override
	public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

	@Override
	public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

	@Override
	public void visit(Directory directory) throws Exception{}

	@Override
	public void visit(Events eventRoot) throws Exception{}

	@Override
	public void visit(Information information) throws Exception{}

	@Override
	public void visit(InterestGroup interestGroup, Application application) throws Exception{}

	@Override
	public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception{}

	@Override
	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception {}

	@Override
	public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception{}

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
	public void visit(Space node, Shared sharedProperties) throws Exception{}

	@Override
	public void visit(Surveys survey) throws Exception{}

	@Override
	public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}

	@Override
	public void visitLinkTarget(Link node, String reference) throws Exception{}

	@Override
	public void visitLocation(Node node, String uri) throws Exception{}

	@Override
	public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}

	@Override
	public void visit(LibraryContentVersion contentVersion) throws Exception{}

	@Override
	public void visit(LibraryTranslationVersion translationVersion) throws Exception{}

	@Override
    public void visit(LogFile logFile) throws Exception{}

	private MigrateContents getMigrateContents()
	{
		final MigrateContents migrateContents = new MigrateContents(importRoot, getJournal(), getRegistry());
		return migrateContents;
	}

}
