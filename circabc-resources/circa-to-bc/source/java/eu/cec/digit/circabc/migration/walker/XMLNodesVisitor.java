/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.walker;

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
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Surveys;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;

/**
 * Base Interface to visit circabc nodes.
 *
 * @author Yanick Pignot
 */
public interface XMLNodesVisitor
{

	/**
	 * visit circabc
	 *
	 * @param circabc the node to visit
	 */
	public void visit(Circabc circabc) throws Exception;

	/**
	 * visit a category Header
	 *
	 * @param header the node to visit
	 */
	public void visit(CategoryHeader header) throws Exception;

	/**
	 * visit a category
	 *
	 * @param category the node to visit
	 */
	public void visit(Category category) throws Exception;

	/**
	 * visit an interestgroup
	 *
	 * @param interestGroup the node to visit
	 */
	public void visit(InterestGroup interestGroup) throws Exception;

	/**
	 * visit an information ig services
	 *
	 * @param information the node to visit
	 */
	public void visit(Information information) throws Exception;

	/**
	 * visit a library ig services
	 *
	 * @param library the node to visit
	 */
	public void visit(Library library) throws Exception;

	/**
	 * visit a directory ig services
	 *
	 * @param directory the node to visit
	 */
	public void visit(Directory directory) throws Exception;

	/**
	 * visit a newsgroup ig services
	 *
	 * @param newsgroup the node to visit
	 */
	public void visit(Newsgroups newsgroup) throws Exception;

	/**
	 * visit through event ig services
	 *
	 * @param eventRoot the node to visit
	 */
	public void visit(Events eventRoot) throws Exception;

	/**
	 * visit a survey ig services
	 *
	 * @param survey the node to visit
	 */
	public void visit(Surveys survey) throws Exception;

	/**
	 * visit a forum
	 *
	 * @param forum the node to visit
	 */
	public void visit(Forum forum) throws Exception;

	/**
	 * visit a topic
	 *
	 * @param topic the node to visit
	 */
	public void visit(Topic topic) throws Exception;

	/**
	 * visit a discussion
	 *
	 * @param discussions the node to visit
	 */
	public void visit(Discussions discussion) throws Exception;

	/**
	 * visit a message
	 *
	 * @param message the node to visit
	 */
	public void visit(Message message) throws Exception;

	/**
	 * visit a message
	 *
	 * @param message the node to visit
	 */
	public void visit(SimpleContent simpleContent) throws Exception;

	/**
	 * visit a content node in the library
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(Content contentNode) throws Exception;

	/**
	 * visit a content node in the information servivce
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(InfContent contentNode) throws Exception;

	/**
	 * visit a content version node the library
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(LibraryContentVersion contentNode) throws Exception;

	/**
	 * visit a content version node in the information servivce
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(InformationContentVersion contentNode) throws Exception;

	/**
	 * visit a translation node the library
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(LibraryTranslation contentNode) throws Exception;

	/**
	 * visit a translation node in the information servivce
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(InformationTranslation contentNode) throws Exception;

	/**
	 * visit a translation version node the library
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(LibraryTranslationVersion contentNode) throws Exception;

	/**
	 * visit a translation version node in the information servivce
	 *
	 * @param contentNode the node to visit
	 */
	public void visit(InformationTranslationVersion contentNode) throws Exception;

	/**
	 * visit a mlContent in library service
	 *
	 * @param mlContent the node to visit
	 */
	public void visit(MlContent mlContent) throws Exception;

	/**
	 * visit a ml content in the information service
	 *
	 * @param mlcontent the node to visit
	 */
	public void visit(InfMLContent mlcontent) throws Exception;

	/**
	 * visit a space in library servicex
	 *
	 * @param space the node to visit
	 */
	public void visit(Space space) throws Exception;

	/**
	 * visit a space in information service
	 *
	 * @param space the node to visit
	 */
	public void visit(InfSpace space) throws Exception;

	/**
	 * visit an url
	 *
	 * @param url the node to visit
	 */
	public void visit(Url url) throws Exception;

	/**
	 * visit a link
	 *
	 * @param link the node to visit
	 */
	public void visit(Link link) throws Exception;

	/**
	 * visit a link to a shared space
	 *
	 * @param link the node to visit
	 */
	public void visit(SharedSpacelink link) throws Exception;

	/**
	 * visit a dossier
	 *
	 * @param dossier the node to visit
	 */
	public void visit(Dossier dossier) throws Exception;

}
