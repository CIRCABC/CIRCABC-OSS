/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;

import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.nntp.NNTPClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.util.ParsedPath;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.repo.migration.ExportServiceImpl;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concrete implementation of NewsgroupReader for circa using nntp.
 *
 * @author Yanick Pignot
 */
public class NewsgroupReaderImpl implements NewsgroupReader
{
    private static final String SUFFIX_DOCUMENT_DISCUSSIONS = ".document-discussions";
    private static final String SPECIFIC_DISCUSSION_REGEX = ".*\\Q (DP:/data{0})\\E";
    private static final String ANY_DISCUSSION_REGEX = ".*\\Q (DP:/data\\E.*\\)";
    private static final Pattern GET_DOCUMENT_PATTERN = Pattern.compile("(.*)(\\(DP\\:)(.*)(\\))");

    private static final Log logger = LogFactory.getLog(ExportServiceImpl.class);

    private NNTPClient nntpClient;
    private String domainPrefix;
    private FileClient fileClient;
    private RemoteFileReader fileReader;

    public List<String> listRootForums(final String newsgroupRootPath) throws ExportationException
    {
        final CategoryInterestGroupPair interestGroup = FilePathUtils.getInterestGroupFromPath(newsgroupRootPath, fileClient.getDataRoot());

        final List<Folder> newsgroups = nntpClient.getNewsgroups(interestGroup.getCategory(), interestGroup.getInterestGroup());

        if(logger.isDebugEnabled())
        {
            logger.debug(newsgroups.size() + " newsgroups found for " + interestGroup.toString());
        }

        final List<String> newsgroupStrings = new ArrayList<String>(newsgroups.size());
        for(final Folder folder: newsgroups)
        {
            if(folder.getFullName().endsWith(SUFFIX_DOCUMENT_DISCUSSIONS) == false)
            {
                newsgroupStrings.add(newsgroupRootPath + "/" + folder.getFullName());
            }
        }

        return newsgroupStrings;
    }

    public List<String> listSubForums(final String parentForumPath) throws ExportationException
    {
        // no sub forums in circa
        return Collections.<String>emptyList();
    }

    public List<String> listMessages(final String parentTopicPath) throws ExportationException
    {
        final String topicMessageId = FilePathUtils.retreiveFileName(parentTopicPath);
        final String newsgroupPath = FilePathUtils.retreiveParentPath(parentTopicPath);
        final String newsgroupName = FilePathUtils.retreiveFileName(newsgroupPath);

        final Message message = nntpClient.getMessage(newsgroupName, topicMessageId);
        final List<String> messagePaths = new ArrayList<String>(1);
        messagePaths.add(newsgroupPath + "/" + nntpClient.computeArticleId(message));

        return messagePaths;
    }

    public List<String> listReplies(final String parentMessagePath) throws ExportationException
    {
    	final String topicMessageId = FilePathUtils.retreiveFileName(parentMessagePath);
        final String newsgroupPath = FilePathUtils.retreiveParentPath(parentMessagePath);
        final String newsgroupName = FilePathUtils.retreiveFileName(newsgroupPath);

        final List<Message> messages = nntpClient.getMessages(newsgroupName);

        final List<String> messagePaths = new ArrayList<String>();
        for(final Message message: messages)
        {
        	Message reply = nntpClient.getReplyOf(newsgroupName, message, messages);
            if((reply != null) && topicMessageId.equals(nntpClient.computeArticleId(reply)))
            {
            	messagePaths.add(newsgroupPath + "/" + nntpClient.computeArticleId(message));
            }
        }

        return messagePaths;
    }

    public String getDiscussions(final String parentLibraryContent) throws ExportationException
    {
        if(listDiscussionsTopics(parentLibraryContent).size() < 1)
        {
            return null;
        }
        else
        {
            // the path of a discussion is the same that its parentone
            return parentLibraryContent;
        }
    }

    private Pair<String, List<String>> lastdiscussionTopics;
    public List<String> listDiscussionsTopics(final String parentLibraryContent) throws ExportationException
    {
    	// since this method is called twice for a threatment, mem the last call.
    	if(lastdiscussionTopics != null && lastdiscussionTopics.getFirst().equals(parentLibraryContent))
    	{
    		return lastdiscussionTopics.getSecond();
    	}

    	final ParsedPath parsedPath = new ParsedPath(parentLibraryContent, fileClient, domainPrefix);
        final String newsgroupName = nntpClient.buildNewsgroupName(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());
        final String discussionNewsgroupName = newsgroupName + SUFFIX_DOCUMENT_DISCUSSIONS;
        final String inServicePath = parsedPath.getInServicePath();
		final String pathWithoutFile;

		if(fileClient.isFile(parentLibraryContent))
		{
	        // remove last element of /documentdoc/_EN_1.0_
			pathWithoutFile = FilePathUtils.retreiveParentPath(inServicePath);

			if(fileReader.getContentTranslations(parentLibraryContent).size() > 1)
			{
				// it's a translation, let the discussions on the mlContent
				return Collections.<String>emptyList();
			}

		}
		else
		{
			pathWithoutFile = inServicePath;
		}

        final String discussionRegex = MessageFormat.format(SPECIFIC_DISCUSSION_REGEX, pathWithoutFile);
        final Folder folder = nntpClient.getNewsgroup(discussionNewsgroupName);

        final List<String> topics = new ArrayList<String>();

        if(folder != null)
        {
            final List<Message> messages = nntpClient.getMessages(discussionNewsgroupName);

            for(final Message message: messages)
            {
                try
                {
                    // keep only the top level messages. The other messages will be responses
                    if(message.getSubject().matches(discussionRegex) && nntpClient.getReplyOf(discussionNewsgroupName, message, messages) == null)
                    {
                        topics.add(parentLibraryContent + "/" + discussionNewsgroupName + "/" + nntpClient.computeArticleId(message));
                    }
                }
                catch(final MessageRemovedException ignore){}
                catch(final MessagingException ex)
                {
                    throw new ExportationException("Fail to access to '" + discussionNewsgroupName + "':" + ex.getMessage(), ex);
                }
            }
        }

        lastdiscussionTopics = new Pair<String, List<String>>(parentLibraryContent, topics);

        return topics;

    }

    public List<String> listTopics(String parentForumPath) throws ExportationException
    {
        final String newsgroupName = FilePathUtils.retreiveFileName(parentForumPath);

        final List<Message> messages = nntpClient.getMessages(newsgroupName);

        final List<String> topics = new ArrayList<String>();
        for(final Message message: messages)
        {
            // keep only the top level messages. The other messages will be responses
            if(nntpClient.getReplyOf(newsgroupName, message, messages) == null)
            {
            	topics.add(parentForumPath + "/" + nntpClient.computeArticleId(message));
            }
        }

        return topics;
    }

    public List<String> listAttachements(final String parentMessagePath) throws ExportationException
    {
        final String topicMessageId = FilePathUtils.retreiveFileName(parentMessagePath);
        final String newsgroupPath = FilePathUtils.retreiveParentPath(parentMessagePath);
        final String newsgroupName = FilePathUtils.retreiveFileName(newsgroupPath);

        final List<String> contents = new ArrayList<String>();

        for(final Map.Entry<Integer, String> attachement: nntpClient.getAllParts(newsgroupName, topicMessageId).entrySet())
        {
            if(attachement.getValue() != null)
            {
                contents.add(parentMessagePath + "/" + attachement.getKey());
            }
            // else if filename is null, it means that it's the main content of the message.
        }

        return contents;
    }

    public boolean isModeratedForum(final String newsgroupPath)throws ExportationException
	{
    	final String newsgroupName = FilePathUtils.retreiveFileName(newsgroupPath);

		return nntpClient.isModeratedForum(newsgroupName);
	}

    public boolean isLibraryDiscussion(final String newsgroupPath)
	{
    	return newsgroupPath != null && newsgroupPath.endsWith(SUFFIX_DOCUMENT_DISCUSSIONS);
	}

    public String getDiscussionOn(final String topicName)
	{
    	if(topicName.matches(ANY_DISCUSSION_REGEX))
    	{
    		final Matcher matcher = GET_DOCUMENT_PATTERN.matcher(topicName);
    		if (matcher.find())
    		{
    			return matcher.group(3);
			}
    		else
    		{
    			return null;
    		}
    	}
    	else
    	{
    		return null;
    	}

	}


    public List<String> listLinks(final String parentMessagePath) throws ExportationException
    {
        //no attachement links in circa
        return Collections.<String>emptyList();
    }

    public final void setNntpClient(NNTPClient nntpClient)
    {
        this.nntpClient = nntpClient;
    }

    public final void setDomainPrefix(String domainPrefix)
    {
        this.domainPrefix = domainPrefix;
    }

    public final void setFileClient(FileClient fileClient)
    {
        this.fileClient = fileClient;
    }

	public final void setFileReader(RemoteFileReader fileReader)
	{
		this.fileReader = fileReader;
	}

}
