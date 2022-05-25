/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.util.List;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for classes that help the reading of a remote newsgroup structure for an exportation.
 *
 * @author Yanick Pignot
 */
public interface NewsgroupReader
{

	/**
	 * list all forums defined under a given path
	 *
	 * @param newsgroupRootPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listRootForums(final String newsgroupRootPath) throws ExportationException;

	/**
	 * list all sub forums defined under a given path
	 *
	 * @param parentForumPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listSubForums(final String parentForumPath) throws ExportationException;

	/**
	 * list all topics defined under a given path (forum path)
	 *
	 * @param parentForumPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listTopics(final String parentForumPath) throws ExportationException;

	/**
	 * list all topics defined under a given document/space path (library path)
	 *
	 * @param parentLibraryContent
	 * @return
	 * @throws ExportationException
	 */
	public abstract String getDiscussions(final String parentLibraryContent) throws ExportationException;

	/**
	 * list all topics defined under a given document/space path (library path)
	 *
	 * @param parentLibraryContent
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listDiscussionsTopics(final String parentLibraryContent) throws ExportationException;

	/**
	 * list all messages defined under a given path (topic path).
	 *
	 * @param parentTopicPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listMessages(final String parentTopicPath) throws ExportationException;

	/**
	 * list all messages defined under a given path (message path).
	 *
	 * @param parentMessagePath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listReplies(final String parentMessagePath) throws ExportationException;


	/**
	 * list all attachements defined under a given path (message path).
	 *
	 * @param parentTopicPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listAttachements(final String parentMessagePath) throws ExportationException;

	/**
	 * list all related content links defined under a given path (message path).
	 *
	 * @param parentTopicPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> listLinks(final String parentMessagePath) throws ExportationException;

	/**
	 * Return if a given newsgroup path is moderated or not
	 *
	 * @param newsgroupPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean isModeratedForum(final String newsgroupPath)throws ExportationException;

	/**
	 * Return if a given newsgroup path is a part of a Library documenty discussion
	 *
	 * @param newsgroupPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean isLibraryDiscussion(final String newsgroupPath);

	/**
	 * Return the in library path of the docuemnt on wich the discussion is related on. Null if not found.
	 *
	 * @param topicPath
	 * @return
	 * @throws ExportationException
	 */
	public abstract String getDiscussionOn(final String topicPath);

}