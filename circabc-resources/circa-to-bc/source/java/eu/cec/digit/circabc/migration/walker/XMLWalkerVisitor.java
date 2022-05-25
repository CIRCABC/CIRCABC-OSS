/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.walker;

import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.LogEntry;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
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

/**
 * Base Interface to walk thought circabc nodes and terminal elements.
 *
 * @author Yanick Pignot
 */
public interface XMLWalkerVisitor extends XMLNodesVisitor
{

	/**
	 * Visit the persons to import.
	 *
	 * @param persons
	 */
	public void visit(final Persons persons) throws Exception;

	/**
	 * Visit a person. (End of recurssions)
	 *
	 * @param person
	 */
	public void visit(final Person person) throws Exception;

	/**
	 * Visit the circabc log files.
	 *
	 * @param persons
	 */
	public void visit(final LogFile logFile) throws Exception;

	/**
	 * Visit a circabc log file line. (End of recurssions)
	 *
	 * @param person
	 */
	public void visit(final LogEntry logEntry) throws Exception;

	/**
	 * Visit the circabc admins of circabc. (End of recurssions)
	 *
	 * @param circabc
	 * @param circabcAdmin
	 */
	public void visit(final Circabc circabc, final CircabcAdmin circabcAdmin) throws Exception;

	/**
	 * Visit the category admins of a category. (End of recurssions)
	 *
	 * @param category
	 * @param categoryAdmin
	 */
	public void visit(final Category category, final CategoryAdmin categoryAdmin) throws Exception;

	/**
	 * Visit the dynamic properties definition of an interest group. (End of recurssions)
	 *
	 * @param interestGroup
	 * @param dynamicPropertyDefinitions
	 */
	public void visit(final InterestGroup interestGroup, final DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception;

	/**
	 * Visit logo definition. (End of recurssions)
	 *
	 * @param interestGroup
	 * @param logoDefinitions
	 */
	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception;

	/**
	 * Visit each applicant of an interest group. (End of recurssions)
	 *
	 * @param interestGroup
	 * @param applicant
	 */
	public void visit(final InterestGroup interestGroup, final Application applicant) throws Exception;

	/**
	 * Visit the access profiles of a directory service. (End of recurssions)
	 *
	 * @param directory
	 * @param name
	 * @param profile
	 */
	public void visit(final Directory directory, final String name, final AccessProfile profile) throws Exception;

	/**
	 * Visit the global access profile (for guest or members) of a directory service. (End of recurssions)
	 *
	 * @param directory
	 * @param directory
	 * @param profile
	 */
	public void visit(final Directory directory, final String name, final GlobalAccessProfile profile) throws Exception;

	/**
	 * Visit the imported access profile of a directory service. (End of recurssions)
	 *
	 * @param directory
	 * @param directory
	 * @param profile
	 */
	public void visit(final Directory directory, final ImportedProfile profile) throws Exception;

	/**
	 * visit an event. (End of recurssions)
	 *
	 * @param eventRoot 	the event root element
	 * @param event 		the node to visit
	 */
	public void visit(final Events eventRoot, final Event event) throws Exception;

	/**
	 * visit a meeting. (End of recurssions)
	 *
	 * @param eventRoot		the event root element
	 * @param meeting 		the node to visit
	 */
	public void visit(final Events eventRoot, final Meeting meeting) throws Exception;


	/**
	 * Visit the keyword definitions of an interest group. (End of recurssions)
	 *
	 * @param interestGroup
	 * @param keywordDefinitions
	 */
	public void visit(final InterestGroup interestGroup, final KeywordDefinitions keywordDefinitions) throws Exception;

	/**
	 * Visit the keywords of a node. (End of recurssions)
	 *
	 * @param node
	 * @param keywords
	 */
	public void visit(final Node node, final KeywordReferences keywords) throws Exception;

	/**
	 * Visit the permissions of a node. (End of recurssions)
	 *
	 * @param node
	 * @param permissions
	 */
	public void visit(final Node node, final LibraryUserRights permissions) throws Exception;

	/**
	 * Visit the permissions of a node. (End of recurssions)
	 *
	 * @param node
	 * @param permissions
	 */
	public void visit(final Node node, final NewsgroupUserRights permissions) throws Exception;

	/**
	 * Visit the permissions of a node. (End of recurssions)
	 *
	 * @param node
	 * @param permissions
	 */
	public void visit(final Node node, final InformationUserRights permissions) throws Exception;

	/**
	 * Visit the notifications of a node. (End of recurssions)
	 *
	 * @param node
	 * @param notifications
	 */
	public void visit(final Node node, final Notifications notifications) throws Exception;

	/**
	 * Visit the extended properties of a node. (End of recurssions)
	 *
	 * @param node
	 * @param property
	 */
	public void visit(final Node node, final ExtendedProperty property) throws Exception;

	/**
	 * Visit the managed properties of a node. (End of recurssions)
	 *
	 * @param node
	 * @param property
	 */
	public void visit(final Node node, final TypedProperty property) throws Exception;

	/**
	 * Visit the user preferences of a person. (End of recurssions)
	 *
	 * @param person
	 * @param property
	 */
	public void visit(final Node node, final TypedPreference preference) throws Exception;

	/**
	 * Visit the shared propertiesof a space. (End of recurssions)
	 *
	 * @param Space
	 * @param sharedProperties
	 */
	public void visit(final Space node, final Shared sharedProperties) throws Exception;


	/**
	 * Visit uri where is located the content of a node. (End of recurssions)
	 *
	 * @param node
	 * @param property
	 */
	public void visitLocation(final Node node, final String uri) throws Exception;

	/**
	 * Visit the path reference of a link target. (End of recurssions)
	 *
	 * @param node
	 * @param reference
	 */
	public void visitLinkTarget(final Link node, final String reference) throws Exception;

	/**
	 * Visit the path reference of a link target. (End of recurssions)
	 *
	 * @param node
	 * @param reference
	 */
	public void visitSharedSpaceLinkTarget(final SharedSpacelink node, final String reference) throws Exception;

	/**
	 * Visit the path reference of a meeting library section. (End of recurssions)
	 *
	 * @param meeting
	 * @param reference
	 */
	public void visitLibrarytSection(final Meeting meeting, final String reference) throws Exception;

}
