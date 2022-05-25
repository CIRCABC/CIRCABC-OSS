/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.util.Collections;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support NewsgroupReader
 *
 * @author Yanick Pignot
 */
public class NewsgroupReaderNOOPImpl implements NewsgroupReader
{

	public String getDiscussionOn(String topicPath)
	{
		return null;
	}

	public String getDiscussions(String parentLibraryContent) throws ExportationException
	{
		return null;
	}

	public boolean isLibraryDiscussion(String newsgroupPath)
	{
		return false;
	}

	public boolean isModeratedForum(String newsgroupPath) throws ExportationException
	{
		return false;
	}

	public List<String> listAttachements(String parentMessagePath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listDiscussionsTopics(String parentLibraryContent) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listLinks(String parentMessagePath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listMessages(String parentTopicPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listReplies(String parentMessagePath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listRootForums(String newsgroupRootPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listSubForums(String parentForumPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> listTopics(String parentForumPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}



}