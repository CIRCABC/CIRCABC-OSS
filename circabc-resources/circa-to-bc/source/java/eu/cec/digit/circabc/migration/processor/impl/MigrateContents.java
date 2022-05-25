/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/


package eu.cec.digit.circabc.migration.processor.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.jxpath.JXPathContext;
import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.entities.ElementsConverter;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
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
import eu.cec.digit.circabc.migration.entities.generated.properties.MeetingRequestStatus;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.JournalLine.UpdateOperation;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.version.CustomLabelAwareVersionServiceImpl;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.event.EventItem;
import eu.cec.digit.circabc.service.event.EventService;
import eu.cec.digit.circabc.service.event.UpdateMode;

/**
 * Import the Content nodes (content, url, messages, ....) using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateContents extends MigrateProcessorBase
{
	public MigrateContents()
	{
		super();
	}

	public MigrateContents(final ImportRoot importRoot, final MigrationTracer _journal, final CircabcServiceRegistry registry)
	{
		super(importRoot, _journal, registry);
	}

    @Override
    public void visit(final Events eventRoot, final Event event) throws Exception
    {
        apply(new MigrateAppointementCallback(getJournal(), eventRoot, event));
        super.visit(eventRoot, event);
    }

    @Override
    public void visit(final Events eventRoot, final Meeting meeting) throws Exception
    {
        final NodeRef ref = (NodeRef) apply(new MigrateAppointementCallback(getJournal(), eventRoot, meeting));
        apply(new MigrateMeetingRequestStatusCallback(getJournal(), meeting, ref));

        super.visit(eventRoot, meeting);
    }

    @Override
    public void visit(final Url url) throws Exception
    {
        apply(new MigrateUrlCallback(getJournal(), url));
        super.visit(url);
    }

    @Override
    public void visit(final Content contentNode) throws Exception
    {
    	final LibraryContentVersions versions = contentNode.getVersions();
    	apply(new MigrateContentCallback(getJournal(), contentNode, versions == null ? null : versions.getVersions()));

    	super.visit(contentNode);
    }

    @Override
    public void visit(final InfContent contentNode) throws Exception
    {
    	final InformationContentVersions versions = contentNode.getVersions();

    	apply(new MigrateContentCallback(getJournal(), contentNode, versions == null ? null : versions.getVersions()));
        super.visit(contentNode);
    }

    @Override
    public void visit(final LibraryTranslation contentNode) throws Exception
    {
    	final LibraryTranslationVersions versions = contentNode.getVersions();
        apply(new MigrateContentCallback(getJournal(), contentNode, versions == null ? null : versions.getVersions()));
        super.visit(contentNode);
    }

    @Override
    public void visit(final InformationTranslation contentNode) throws Exception
    {
    	final InformationTranslationVersions versions = contentNode.getVersions();
        apply(new MigrateContentCallback(getJournal(), contentNode, versions == null ? null : versions.getVersions()));

        super.visit(contentNode);
    }

    class MigrateAppointementCallback extends JournalizedTransactionCallback
    {
        final Appointment appointment;
        final Events eventRoot;
        final EventService eventService;

        public MigrateAppointementCallback(final MigrationTracer journal, final Events eventRoot, Appointment appointment)
        {
            super(journal);
            this.appointment = appointment;
            this.eventRoot = eventRoot;
            this.eventService = getRegistry().getEventService();
        }

        public NodeRef execute() throws Throwable
        {
            NodeRef appointementRef = null;
            try
            {
                final NodeRef parent = ElementsHelper.getNodeRef(eventRoot);
                if(appointment instanceof Meeting)
                {
                    final Meeting meeting = (Meeting) appointment;
                    final eu.cec.digit.circabc.service.event.Meeting repoMeeting = ElementsConverter.convertMeeting(importRoot.getCircabc(), meeting);
                    eventService.createMeeting(parent, repoMeeting, false);
                    appointementRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, repoMeeting.getId());

                    getReport().appendSection("Meeting successfully created " + ElementsHelper.getXPath(appointment) + ". ");
                }
                else
                {
                    final Event event = (Event) appointment;
                    final eu.cec.digit.circabc.service.event.Event repoEvent = ElementsConverter.convertEvent(event);
                    eventService.createEvent(parent, repoEvent, false);
                    appointementRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, repoEvent.getId());

                    getReport().appendSection("Event successfully created " + ElementsHelper.getXPath(appointment) + ". ");
                }

                getJournal().journalize(JournalLine.createNode(Status.SUCCESS, ElementsHelper.getXPath(appointment), null));
            }
            catch(Throwable t)
            {
                getReport().appendSection("Impossible to create node " + ElementsHelper.getXPath(appointment) + ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.createNode(Status.FAIL, ElementsHelper.getXPath(appointment), null));
                }
            }
            return appointementRef;
        }
    };

    class MigrateMeetingRequestStatusCallback extends JournalizedTransactionCallback
    {
        final Meeting meeting;
        final NodeRef meetingRef;
        final EventService eventService;

        public MigrateMeetingRequestStatusCallback(final MigrationTracer journal, final Meeting meeting, final NodeRef meetingRef)
        {
            super(journal);
            this.meeting = meeting;
            this.meetingRef = meetingRef;
            this.eventService = getRegistry().getEventService();
        }

        public Object execute() throws Throwable
        {
            try
            {
                if(meeting.getAudienceClosed() != null)
                {
                    final List<EventItem> occurences = eventService.getAllOccurences(meetingRef);

                    for(final EventItem occurence: occurences)
                    {
                        for(final MeetingRequestStatus status: meeting.getMeetingRequestStatuses())
                        {
                            if(isSameDay(status.getDate(), occurence.getDate()))
                            {
                               final eu.cec.digit.circabc.service.event.MeetingRequestStatus repoStatus;
                               final UpdateOperation operation;
                               if(status.isAccepted())
                               {
                            	   repoStatus = eu.cec.digit.circabc.service.event.MeetingRequestStatus.Accepted;
                            	   operation = UpdateOperation.ACCEPT_MEETING;
                               }
                               else
                               {
                            	   repoStatus = eu.cec.digit.circabc.service.event.MeetingRequestStatus.Rejected;
                            	   operation = UpdateOperation.REJECT_MEETING;
                               }

                            	eventService.setMeetingRequestStatus(
                                        occurence.getEventNodeRef(),
                                        status.getInvitedUser(),
                                        repoStatus,
                                        UpdateMode.Single);

                                getJournal().journalize(
                                        JournalLine.updateNode(
                                                Status.SUCCESS,
                                                ElementsHelper.getXPath(meeting),
                                                operation,
                                                Parameter.USER_NAME, status.getInvitedUser())
                                        );
                            }
                        }
                    }

                }
            }
            catch(Throwable t)
            {
                getReport().appendSection("Impossible set meeting request status for  " + ElementsHelper.getXPath(meeting) + ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(
                            JournalLine.updateNode(
                                    Status.FAIL,
                                    ElementsHelper.getXPath(meeting),
                                    UpdateOperation.REJECT_MEETING,
                                    Parameter.USER_NAME, "" + meeting.getMeetingRequestStatuses())
                            );
                }
            }
            return null;
        }


        private boolean isSameDay(Date datea, Date dateb) {

            final Calendar a = GregorianCalendar.getInstance();
            a.setTime(datea);
            final Calendar b = GregorianCalendar.getInstance();
            b.setTime(dateb);

            return (a.get(Calendar.DATE) == b.get(Calendar.DATE) &&
                    a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
                    a.get(Calendar.YEAR) == b.get(Calendar.YEAR));
        }
    };


    class MigrateContentCallback extends MigrateNodesCallback
    {
    	private final List<? extends ContentNode> olderVersions;

    	protected final VersionService versionService;
    	protected final ContentService contentService;
    	protected final MimetypeService mimetypeService;
    	protected final ResourceManager resourceManager;

        public MigrateContentCallback(final MigrationTracer journal, final Node currentVersion, final List<? extends ContentNode> olderVersions)
        {
        	super(journal, currentVersion);
        	this.versionService = getRegistry().getAlfrescoServiceRegistry().getVersionService();
        	this.contentService = getRegistry().getAlfrescoServiceRegistry().getContentService();
        	this.mimetypeService = getRegistry().getAlfrescoServiceRegistry().getMimetypeService();
        	this.resourceManager = ResourceManager.getInstance(getRegistry());

        	if(olderVersions != null)
        	{
        		this.olderVersions = olderVersions;

        		// sort the version to have the oldest in first place
        		Collections.sort(this.olderVersions, ContentNodeVersionComparator.getInstance());
        	}
        	else
        	{
        		this.olderVersions = Collections.<ContentNode>emptyList();
        	}

        }

        public NodeRef executeImpl(final Node content)throws Throwable
        {
            getPolicyBehaviourFilter().disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

            final NodeRef contentNodeRef = executeImplGeneric(/*currentVersion*/content, getType());
            final String name = getName();

            for(final ContentNode version: olderVersions)
            {
            	updateContent(version, contentNodeRef, name);

            	final JXPathContext context = JXPathContext.newContext(node);

            	final PropertyMap properties = new PropertyMap();
            	for(final Object child : context.selectNodes(SELECT_ALL_CHILDS))
            	{
            		if(child != null)
            		{
            			if(child instanceof ExtendedProperty)
                		{
                			final ExtendedProperty prop = (ExtendedProperty) child;
            				properties.put(prop.getQname(), prop.getValue());
                		}
            			else if(child instanceof TypedProperty)
            			{
            				final TypedProperty prop = (TypedProperty) child;
            				properties.put(prop.getIdentifier(), prop.getValue());
            			}
            		}

            	}

            	getMigrateProperties().visit(node, properties, null, false);

            	getJournal().getProcessReport().appendSection(ElementsHelper.getQualifiedPath(version) + " Successfully created with version number: " + version.getVersionLabel());
                getJournal().journalize(JournalLine.updateNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(version), contentNodeRef.toString(), UpdateOperation.ADD_VERSION, Collections.singletonMap(Parameter.VERSION_LABEL, (Serializable) version.getVersionLabel().getValue())));

                ElementsHelper.setNodeRef(version, contentNodeRef);
            }

            updateContent(content, contentNodeRef, name);

            return contentNodeRef;

            
        }

		private void updateContent(final Node content, final NodeRef contentNodeRef, final String name) throws Exception, InvalidNodeRefException, InvalidAspectException, InvalidTypeException, ContentIOException
		{
			final String mimetype = getMimeTypeForFileName(name);
			final boolean onlineEditable = isInlineEditable(mimetype);
			final InputStream inputStream = getInpuStreamFromResource(content);

			// apply the inlineeditable aspect
			if (onlineEditable)
			{
			    final Map<QName, Serializable> editProps = Collections.<QName, Serializable>singletonMap(ApplicationModel.PROP_EDITINLINE, onlineEditable);
			    getNodeService().addAspect(contentNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);
			}

			getNodeService().addAspect(contentNodeRef, ContentModel.ASPECT_AUTHOR, Collections.<QName, Serializable> emptyMap());

	    	final PropertyMap properties = new PropertyMap();
	    	final JXPathContext context = JXPathContext.newContext(content);
	    	for(final Object child : context.selectNodes(SELECT_ALL_CHILDS))
	    	{
	    		if(child != null)
	    		{
	    			if(child instanceof TypedProperty)
					{
						final TypedProperty prop = (TypedProperty) child;
						properties.put(prop.getIdentifier(), prop.getValue());
					}
					else if(child instanceof ExtendedProperty)
					{
						final ExtendedProperty prop = (ExtendedProperty) child;
						properties.put(prop.getQname(), prop.getValue());
					}
	    		}
	    	}

	    	final String versionNote = (String) properties.get(Version2Model.PROP_QNAME_VERSION_DESCRIPTION);
            final String versionlabel = (String) properties.get(Version2Model.PROP_QNAME_VERSION_LABEL);

            properties.remove(Version2Model.PROP_QNAME_VERSION_DESCRIPTION);
            properties.remove(Version2Model.PROP_QNAME_VERSION_LABEL);

            getMigrateProperties().visit(content, properties, null, false);

			final ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
			//	set the mimetype and encoding
			writer.setMimetype(mimetype);

			writer.setEncoding(guessEncoding(inputStream, mimetype));
			if(inputStream != null)
			{
			    writer.putContent(inputStream);
			}
			else
			{
				final String contentString;

				if(content instanceof ContentNode)
				{
					contentString = ((ContentNode)content).getContentString();
				}
				else
				{
					contentString = null;
				}

			    writer.putContent(contentString == null ? "" : contentString);
			}

			final Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(3);
			versionProperties.put(Version2Model.PROP_DESCRIPTION, versionNote);
			versionProperties.put(Version2Model.PROP_VERSION_TYPE, VersionType.MAJOR);
			versionProperties.put(CustomLabelAwareVersionServiceImpl.PROP_CUSTOM_VERSION_LABEL, versionlabel);

			versionService.createVersion(contentNodeRef, versionProperties);
		}

        protected QName getType()
        {
            return ContentModel.TYPE_CONTENT;
        }

        protected String getMimeTypeForFileName(final String filename)
        {
              // fall back to binary mimetype if no match found
              String mimetype = MimetypeMap.MIMETYPE_BINARY;
              int extIndex = filename.lastIndexOf('.');
              if (extIndex != -1)
              {
                 String ext = filename.substring(extIndex + 1).toLowerCase();
                 String mt = mimetypeService.getMimetypesByExtension().get(ext);
                 if (mt != null)
                 {
                    mimetype = mt;
                 }
              }

              return mimetype;
        }

        protected boolean isInlineEditable(String mimetype)
        {
            return
                mimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) ||
                mimetype.equals(MimetypeMap.MIMETYPE_TEXT_CSS) ||
                mimetype.equals(MimetypeMap.MIMETYPE_TEXT_JAVASCRIPT) ||
                mimetype.equals(MimetypeMap.MIMETYPE_TEXT_MEDIAWIKI) ||
                mimetype.equals(MimetypeMap.MIMETYPE_XHTML) ||
                mimetype.equals(MimetypeMap.MIMETYPE_HTML) ||
                mimetype.equals(MimetypeMap.MIMETYPE_XML);

        }

        protected String guessEncoding(InputStream is, String mimetype)
        {
            try
            {
                final ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
                final Charset charset = charsetFinder.getCharset(is, mimetype);
                return charset.name();
            }
            catch(Throwable t)
            {
                return Charset.defaultCharset().name();
            }
        }

        protected InputStream getInpuStreamFromResource(final Node content) throws Exception
        {
            if(content instanceof ContentNode)
            {
            	final ContentNode contentNode = (ContentNode)content;

            	if(contentNode.getUri() == null || contentNode.getUri().length() < 1)
            	{
            		return null;
            	}
            	else
            	{
            		return getInpuStreamFromResource((contentNode).getUri());
            	}


            }
            else
            {
                return null;
            }
        }

        protected InputStream getInpuStreamFromResource(final String uri) throws Exception
        {
            final Resource resource = resourceManager.adaptRessource(uri);
            return resource.getInputStream();
        }
    };

    class MigrateUrlCallback extends MigrateContentCallback
    {
        public MigrateUrlCallback(final MigrationTracer journal, final Url content)
        {
            super(journal, content, null);
        }

        public NodeRef executeImpl(final Node content)throws Throwable
        {
            final NodeRef contentNodeRef = super.executeImpl(content);

            final String urlTarget = (String) ElementsHelper.getProperties(content).get(DocumentModel.ASPECT_URLABLE);
            final Map<QName, Serializable> props = Collections.<QName, Serializable>singletonMap(DocumentModel.ASPECT_URLABLE, urlTarget);
            getNodeService().addAspect(contentNodeRef, DocumentModel.ASPECT_URLABLE, props);

            return contentNodeRef;
        }

        @Override
        protected boolean isInlineEditable(String mimetype)
        {
            return false;
        }
    };

    private MigrateProperties getMigrateProperties()
	{
		final MigrateProperties migrateProperties = new MigrateProperties(importRoot, getJournal(), getRegistry());
		return migrateProperties;
	}

    //--- Stop recursion for the following nodes

    @Override
    public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

    @Override
    public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

    @Override
    public void visit(Directory directory) throws Exception{}

    @Override
    public void visit(Discussions discussion) throws Exception{}

    @Override
    public void visit(Forum forum) throws Exception{}

    @Override
    public void visit(InformationContentVersion contentVersion) throws Exception{}

    @Override
    public void visit(InformationTranslationVersion translationVersion) throws Exception{}

    @Override
    public void visit(InterestGroup interestGroup, Application application) throws Exception{}

    @Override
    public void visit(InterestGroup interestGroup, DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception{}

    @Override
    public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception {}

    @Override
    public void visit(InterestGroup interestGroup, KeywordDefinitions keywordDefinitions) throws Exception{}

    @Override
    public void visit(LibraryContentVersion contentVersion) throws Exception{}

    @Override
    public void visit(LibraryTranslationVersion translationVersion) throws Exception{}

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
    public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}

    @Override
    public void visitLinkTarget(Link node, String reference) throws Exception{}

    @Override
    public void visitLocation(Node node, String uri) throws Exception{}

    @Override
    public void visit(LogFile logFile) throws Exception{}

    @Override
    public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}
}
