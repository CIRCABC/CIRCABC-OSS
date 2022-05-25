/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

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
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
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
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.sharespace.ShareSpaceService;


/**
 * Import the shared space properties using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateSharedSpace extends MigrateProcessorBase
{
	@Override
	public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference) throws Exception{}

	@Override
	public void visit(final Space node, final Shared sharedProperties) throws Exception
	{
		apply(new MigrateSharedPropertyCallback(getJournal(), node, sharedProperties));
	}

	class MigrateSharedPropertyCallback extends JournalizedTransactionCallback
    {
		private final Space space;
		private final Shared sharedProperties;
		private final ShareSpaceService shareSpaceService;

        public MigrateSharedPropertyCallback(final MigrationTracer journal, final Space space, final Shared sharedProperties)
        {
        	super(journal);
        	this.space = space;
        	this.sharedProperties = sharedProperties;
        	this.shareSpaceService = getRegistry().getShareSpaceService();
        }

        public String execute() throws Throwable
        {
        	try
			{
        		final NodeRef spaceRef = ElementsHelper.getNodeRef(space);
        		final List<Pair<NodeRef, String>> alreadyInvitedIg = shareSpaceService.getInvitedInterestGroups(spaceRef);

        		final NodeRef targetIgRef = ElementsHelper.getTargetRef(getManagementService(), importRoot.getCircabc(), sharedProperties.getIgReference());
        		final String permission = sharedProperties.getPermission().value();

        		boolean setted = false;

        		if(alreadyInvitedIg != null)
        		{
        			for(Pair<NodeRef, String> igItem : alreadyInvitedIg)
        			{
        				if(igItem.getFirst().equals(targetIgRef))
        				{
        					final String igPermission = igItem.getSecond();
        					setted = permission.equals(igPermission);
        					break;
        				}
        			}
        		}


        		if(!setted)
        		{
        			shareSpaceService.inviteInterestGroup(spaceRef, targetIgRef, permission);

        			getReport().appendSection(ElementsHelper.getXPath(space) + " successfully shared with " + sharedProperties.getIgReference() + " with permission " + permission);

        			final Map<Parameter, Serializable> parameters = new HashMap<Parameter, Serializable>(2);
        			parameters.put(Parameter.TARGET, sharedProperties.getIgReference());
        			parameters.put(Parameter.PERMISSION, sharedProperties.getPermission().value());

        			getJournal().journalize(JournalLine.updateNode(
							Status.SUCCESS,
							ElementsHelper.getQualifiedPath(space),
							spaceRef.toString(),
							JournalLine.UpdateOperation.SHARE_SPACE,
							parameters
						));
        		}
        		else
        		{
        			getReport().appendSection(ElementsHelper.getXPath(space) + " already shared with " + sharedProperties.getIgReference() + " with permission " + permission);
        		}
			}
        	catch(Throwable t)
			{
        		getReport().appendSection("Impossible to share " + ElementsHelper.getXPath(space) + " swith the interest group " + sharedProperties.getIgReference() + " with permission " + sharedProperties.getPermission().value() +  ". " + t.getMessage());

				if(isFailOnError())
				{
					throw t;
				}
				else
				{
					getJournal().journalize(JournalLine.updateNode(
											Status.FAIL,
											ElementsHelper.getQualifiedPath(space),
											JournalLine.UpdateOperation.SHARE_SPACE,
		    								JournalLine.Parameter.XPATH,
		    								ElementsHelper.getXPath(sharedProperties)
						));
				}
			}

			return null;
		}
    }

    //--- Stop recursion for the following nodes

	@Override
	public void visit(Surveys survey) throws Exception{}

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
	public void visit(Link link) throws Exception{}

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
	public void visit(SharedSpacelink link) throws Exception{}

	@Override
	public void visit(Url url) throws Exception{}

	@Override
	public void visitLibrarytSection(Meeting meeting, String reference) throws Exception{}

	@Override
	public void visitLinkTarget(Link node, String reference) throws Exception{}

	@Override
	public void visitLocation(Node node, String uri) throws Exception{}

	@Override
    public void visit(LogFile logFile) throws Exception{}
}