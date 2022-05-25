/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
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
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.service.sharespace.ShareSpaceService;
import eu.cec.digit.circabc.service.struct.ManagementService;


/**
 * Import the links (Links and shared links) using the tree walking
 *
 * Must be called once target nodes are created and shared spaces are setted correctly.
 *
 * @author Yanick Pignot
 */
public class MigrateLinks extends MigrateProcessorBase
{

    @Override
    public void visit(Link link) throws Exception
    {
        apply(new MigrateLinkCallback(getJournal(), link));
    }

    @Override
    public void visit(SharedSpacelink link) throws Exception
    {
        apply(new MigrateSharedSpacelinkCallback(getJournal(), link));
    }

    class MigrateSharedSpacelinkCallback extends MigrateNodesCallback
    {
        public MigrateSharedSpacelinkCallback(final MigrationTracer journal, final SharedSpacelink sharedSpacelink)
        {
            super(journal, sharedSpacelink);
        }

        @Override
        public NodeRef executeImpl(final Node node) throws Throwable
        {
            final SharedSpacelink sharedSpacelink = (SharedSpacelink) node;

            final NodeRef parentRef = ElementsHelper.getParentNodeRef(sharedSpacelink);
            final String reference = sharedSpacelink.getReference();
			final NodeRef targetRef = ElementsHelper.getTargetRef(getManagementService(), importRoot.getCircabc(), reference);

            if(targetRef == null)
            {
            	throw new NullPointerException("Impossible to create shared space link " + ElementsHelper.getQualifiedPath(node)  + " to " + reference + ". The target node doesn't exist.");
            }

            final ShareSpaceService shareSpaceService = getRegistry().getShareSpaceService();
            final NodeRef created = shareSpaceService.linkSharedSpace(parentRef, targetRef, getName());
            
            final Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
            titledProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
            getNodeService().addAspect(created, ApplicationModel.ASPECT_UIFACETS, titledProps);           
            
            return created;
        }

        @Override
        protected void reportError(Throwable t) throws Throwable
        {
        	if(t instanceof NullPointerException)
        	{
        		 getReport().appendSection(t.getMessage());

                 if(isFailOnError())
                 {
                     throw t;
                 }
                 else
                 {
                     final JournalLine journalLineNode = JournalLine.createNode(Status.FAIL, ElementsHelper.getQualifiedPath(node), null);
                     super.getJournal().journalize(journalLineNode);
                 }
        	}
        	else
        	{
        		super.reportError(t);
        	}
        }

    }

    class MigrateLinkCallback extends MigrateNodesCallback
    {
        private boolean isSpace = false;
        String reference = null;
        NodeRef targetRef = null;

        public MigrateLinkCallback(final MigrationTracer journal, final Link link)
        {
            super(journal, link);
        }

        @Override
        public NodeRef executeImpl(final Node node) throws Throwable
        {
            final Link link = (Link) node;

            boolean isPostAttachment = ElementsHelper.getParent(link) instanceof Message;
            final NodeRef parentRef = ElementsHelper.getParentNodeRef(link);
            reference = link.getReference();
            targetRef = ElementsHelper.getTargetRef(getManagementService(), importRoot.getCircabc(), reference);

            if(targetRef == null)
            {
            	throw new NullPointerException("Impossible to create link " + ElementsHelper.getQualifiedPath(node)  + " to " + reference + ". The target node doesn't exist.");
            }

            if(isPostAttachment)
            {
            	final AttachementService attachementService = getRegistry().getAttachementService();
                if(attachementService.getAttachements(parentRef).contains(targetRef))
                {
                    getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " already attached to the post.");

                    return targetRef;
                }
                else
                {
                    return attachementService.attach(parentRef, targetRef);
                }
            }
            else
            {
            	final DictionaryService dictionaryService = getRegistry().getAlfrescoServiceRegistry().getDictionaryService();
                final QName targetType = getNodeService().getType(targetRef);

                final ChildAssociationRef assocRef = getNodeService().getPrimaryParent(targetRef);

                final Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
                props.put(ContentModel.PROP_NAME, getName());
                props.put(ContentModel.PROP_LINK_DESTINATION, (Serializable) targetRef);

                ChildAssociationRef childRef;

                if (dictionaryService.isSubClass(targetType, ContentModel.TYPE_CONTENT))
                {
                   // create File Link node
                   childRef = getNodeService().createNode(
                         parentRef,
                         ContentModel.ASSOC_CONTAINS,
                         assocRef.getQName(),
                         ApplicationModel.TYPE_FILELINK,
                         props);
                }
                else
                {
                	
                	// create Folder link node
                   childRef = getNodeService().createNode(
                         parentRef,
                         ContentModel.ASSOC_CONTAINS,
                         assocRef.getQName(),
                         ApplicationModel.TYPE_FOLDERLINK,
                         props);
                   
                   final Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
                   titledProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
                   getNodeService().addAspect(childRef.getChildRef(), ApplicationModel.ASPECT_UIFACETS, titledProps);
                }
                
                return childRef.getChildRef();
            }
        }

        @Override
        protected void reportError(Throwable t) throws Throwable
        {
        	if(t instanceof NullPointerException)
        	{
        		 getReport().appendSection(t.getMessage());

                 if(isFailOnError())
                 {
                     throw t;
                 }
                 else
                 {
                     final JournalLine journalLineNode = JournalLine.createNode(Status.FAIL, ElementsHelper.getQualifiedPath(node), null);
                     super.getJournal().journalize(journalLineNode);
                 }
        	}
        	else
        	{
        		super.reportError(t);
        	}
        }

        @Override
        public String getIcon() throws Throwable
        {
            return (this.isSpace) ? ManagementService.DEFAULT_SPACE_LINK_ICON_NAME : null;
        }
    }

       //--- Stop recursion for the following nodes

    @Override
    public void visit(Content content) throws Exception{}

    @Override
    public void visit(Category category, CategoryAdmin categoryAdmin) throws Exception{}

    @Override
    public void visit(Circabc circabc, CircabcAdmin circabcAdmin) throws Exception{}

    @Override
    public void visit(Directory directory) throws Exception{}

    @Override
    public void visit(Discussions discussion) throws Exception{}

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
    public void visit(Message message) throws Exception{}

    @Override
    public void visit(MlContent mlContent) throws Exception{}

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
    public void visit(SimpleContent content) throws Exception{}

    @Override
    public void visit(Space node, Shared sharedProperties) throws Exception{}

    @Override
    public void visit(Surveys survey) throws Exception{}

    @Override
    public void visit(Url url) throws Exception{}

    @Override
    public void visitLinkTarget(Link node, String reference) throws Exception{}

    @Override
    public void visitLocation(Node node, String uri) throws Exception{}

    @Override
    public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}

    @Override
    public void visit(LogFile logFile) throws Exception{}
}
