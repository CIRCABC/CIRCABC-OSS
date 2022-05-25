/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.nntp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.alfresco.repo.cache.SimpleCache;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.processor.common.NNTPResource;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Class that manage all basic nntp tasks for circa newsgroup
 *
 * @author Yanick Pignot
 */
public class NNTPClient
{
	private static final String PENDING_MESSAGES_FOLDER = "moderated";
	private static final int CNT_MAX_TRIES = 3;
	private static final String NNTP_POSTING_DATE = "NNTP-Posting-Date";
	private static final String HEADER_MESSAGE_ID = "Message-ID";
	private static final String HEADER_REFERENCES = "References";
	private static final String ORIGINATORS_UID = "ORIGINATORS UID:";
	private static final String HEADER_COMMENTS = "X-Comments";
	private static final String PATH_SEPARATOR = "/";
	private static final String NNTP_TRANPORT = "nntp://";
	private static final char NNTP_AT = '@';
	private static final char URL_SEPARATOR = ':';
	private static final String NEWSGROUP_PATH_SEPARATOR = ".";
	
	public static final ThreadLocal<DateFormat> POSTING_DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return  new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
		}
	};

	private Store nntpStore = null;

	private SimpleCache<String, List<Folder>> nntpNewsgroupCache;
	private SimpleCache<String, List<Message>> nntpMessagesCache;

	private int connectiontimeout = 0;
	private int timeout = 0;

	private FileClient fileClient;
	private String nntpHost;
	private int nntpPort = NNTPResource.NNTP_DEFAULT_PORT;
	private String nntpUser;
	private String nntpPassword;
	private String circaNNTPRootFolder;


	public void init()
	{

	}

	public String generateResouceString(final String newsgroupName, final String articleId, final int contentNumber) throws ExportationException
	{
		// test if the message is pending one
		final Message message = getMessage(newsgroupName, articleId);
		final StringBuffer buff = new StringBuffer(NNTP_TRANPORT);

		if(isFtpMessage(message))
		{
			try
			{
				buff
					.append(fileClient.generateResouceString(getNewsgroupPendingFolder(newsgroupName)))
					.append(PATH_SEPARATOR)
					.append(message.getFileName())
					.append(PATH_SEPARATOR)
					.append(contentNumber);
			}
			catch (MessagingException e)
			{
				throw new ExportationException("Impossible to build a valide resource url due to " + e.getMessage(),e);
			}
		}
		else
		{
			if(nntpUser != null)
			{
				buff.append(nntpUser);

				if(nntpPassword != null)
				{
					buff
						.append(URL_SEPARATOR)
						.append(nntpPassword);
				}

				buff.append(NNTP_AT);
			}

			buff
				.append(nntpHost)
				.append(URL_SEPARATOR)
				.append(nntpPort);

			buff
				.append(PATH_SEPARATOR)
				.append(newsgroupName)
				.append(PATH_SEPARATOR)
				.append(articleId)
				.append(PATH_SEPARATOR)
				.append(contentNumber);
		}

		return buff.toString();
	}

	public String computeArticleId(final Message message) throws ExportationException
	{
		if(isFtpMessage(message))
		{
			try
			{
				return message.getFileName();
			}
			catch (MessagingException e)
			{
				throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
			}
		}
		else
		{
			return String.valueOf(message.getMessageNumber());
		}
	}

	/**
	 * @param message
	 * @return
	 */
	private boolean isFtpMessage(final Message message)
	{
		return message.getFolder() == null;
	}

	public String buildNewsgroupName(final String category, final String interestGroup)
	{
		final StringBuffer buff = new StringBuffer(circaNNTPRootFolder);
		buff
			.append(NEWSGROUP_PATH_SEPARATOR)
			.append(category)
			.append(NEWSGROUP_PATH_SEPARATOR)
			.append(interestGroup);

		return buff.toString();
	}

	public List<Folder> getNewsgroups(final String category, final String interestGroup) throws ExportationException
	{
		checkConnection();

		final String key = generateKeyForCache(category, interestGroup);

		if(nntpNewsgroupCache.get(key) == null)
		{
			Folder rootFolder;
			try
			{
				rootFolder = getNNTPStore().getDefaultFolder();
				if (rootFolder == null)
				{
					throw new ExportationException("Can't find the NNTP default root folder");
				}

				rootFolder.open(Folder.READ_ONLY);

				final String newsgroupFilter = key + NEWSGROUP_PATH_SEPARATOR;

				final List<Folder> newsgroups = new ArrayList<Folder>();
				final Folder[] allNewsgroups = rootFolder.list();
				for(Folder folder: allNewsgroups)
				{
					if(folder.getFullName().startsWith(newsgroupFilter))
					{
						newsgroups.add(folder);
					}
				}

				rootFolder.close(false);

				nntpNewsgroupCache.put(key, newsgroups);
			}
			catch (MessagingException e)
			{
				throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
			}
		}

		return nntpNewsgroupCache.get(key);
	}

	public Folder getNewsgroup(final String newsgroupName) throws ExportationException
	{
		checkConnection();

		Folder folder = null;
		try
		{
			folder = getNNTPStore().getFolder(newsgroupName);
		}
		catch(final javax.mail.FolderNotFoundException ignore)
		{
			folder = null;
		}
		catch(final MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
		return folder;
	}

	public List<Message> getMessages(final String newsgroupName)throws ExportationException
	{
		checkConnection();

		try
		{
			if(nntpMessagesCache.contains(newsgroupName) == false)
			{
				final Folder folder = getNewsgroup(newsgroupName);
				folder.open(Folder.READ_ONLY);

				final List<Message> messages = new ArrayList<Message>();
				// ensure that the messages are accessible
				for(final Message message: folder.getMessages())
				{
					try
					{
						message.getSubject();
						messages.add(message);
					}
					catch(MessageRemovedException ignore){}
				}

				folder.close(false);

				// check now, the pending messages that are located in the filesystem
				final String folderPath = getNewsgroupPendingFolder(newsgroupName);
				if(fileClient.exists(folderPath))
				{
					final Session session = NNTPResource.getDefaultSession();
					for(final String file: fileClient.list(folderPath, false, true, false))
					{
						final ByteArrayInputStream is = new ByteArrayInputStream(fileClient.downloadAsString(file).getBytes());
						final MimeMessage mimeMessage = new MimeMessage(session, is);
						mimeMessage.setFileName(FilePathUtils.retreiveFileName(file));
						messages.add(mimeMessage);
						try
						{
							is.close();
						} catch (IOException ignore){}
					}
				}

				nntpMessagesCache.put(newsgroupName, messages);
			}

			return nntpMessagesCache.get(newsgroupName);
		}
		catch(final javax.mail.FolderNotFoundException ignore)
		{
			return Collections.<Message>emptyList();
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
	}

	public boolean isModeratedForum(final String newsgroupName)throws ExportationException
	{
		final String folderPath = getNewsgroupPendingFolder(newsgroupName);
		return fileClient.exists(folderPath);
	}

	/**
	 * @param tokens
	 * @return
	 */
	private String getNewsgroupPendingFolder(final String newsgroupName)
	{
		final StringTokenizer tokens = new StringTokenizer(newsgroupName, NEWSGROUP_PATH_SEPARATOR, false);
		//remove root folder
		tokens.nextToken();
		final StringBuffer buff = new StringBuffer(fileClient.getDataRoot());
		buff
			.append(PATH_SEPARATOR)
			.append(tokens.nextToken()) // category
			.append(PATH_SEPARATOR)
			.append(tokens.nextToken()) // ig
			.append(PATH_SEPARATOR)
			.append(fileClient.getNewsDataLocation())
			.append(PATH_SEPARATOR)
			.append(PENDING_MESSAGES_FOLDER)
			.append(PATH_SEPARATOR)
			.append(tokens.nextToken()); // newsgroup short name

		return buff.toString();
	}

	public List<Message> getMessages(final Folder folder)throws ExportationException
	{
		checkConnection();

		return getMessages(folder.getFullName());
	}

	public Message getMessage(final String newsgroupName, final String articleId)throws ExportationException
	{
		return getMessageImpl(newsgroupName, articleId, true);
	}

	private Message getMessageImpl(final String newsgroupName, final String articleId, final boolean failIfNotFound)throws ExportationException
	{
		try
		{
			checkConnection();
			Message match = null;
			for(final Message message: getMessages(newsgroupName))
			{
				// it s FTP
				if(isFtpMessage(message))
				{
					if(articleId.equals(message.getFileName()))
					{
						match = message;
						break;
					}
				}
				// it s nntp
				else
				{
					if(articleId.equals(computeArticleId(message)))
					{
						match = message;
						break;
					}
				}
			}

			if(failIfNotFound && match == null)
			{
				throw new ExportationException("No message found in '" + newsgroupName + "' with index " + articleId);
			}

			return match;
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}

	}

	public Map<Integer, String> getAllParts(final String newsgroupName, final String articleId) throws ExportationException
	{
		checkConnection();

		final Map<Integer, String> attachements = new HashMap<Integer, String>();

		try
		{
			final Message message = getMessage(newsgroupName, articleId);

			final Object content = message.getContent();
			if(content instanceof MimeMultipart)
			{
				final MimeMultipart mimeMultipart = (MimeMultipart) content;
				for(int x = 0; x < mimeMultipart.getCount(); ++x)
				{
					final BodyPart part = mimeMultipart.getBodyPart(x);
					attachements.put(Integer.valueOf(x), part.getFileName());
				}
			}
			else
			{
				attachements.put(Integer.valueOf(0), null);
			}
		}
		catch (final MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
		catch (final IOException e)
		{
			throw new ExportationException("Fail to access to the content of the message: " + e.getMessage(), e);
		}


		return attachements;
	}

	public Object getMessagePart(final String newsgroupName, final String articleId, final int messagePart)throws ExportationException
	{
		checkConnection();

		try
		{
			final Message message = getMessage(newsgroupName, articleId);

			final Object content = message.getContent();
			final Object toReturn;
			if(content instanceof MimeMultipart)
			{
				final MimeMultipart mimeMultipart = (MimeMultipart) content;

				toReturn = mimeMultipart.getBodyPart(messagePart);
			}
			else
			{
				if(messagePart == 0)
				{
					toReturn = content;
				}
				else
				{
					toReturn = null;
				}
			}

			if(toReturn == null)
			{
				throw new ExportationException("Impossible to access to the content (" + messagePart + ") since it doesn't exists");
			}

			return toReturn;
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new ExportationException("Fail to access to the content of the message: " + e.getMessage(), e);
		}
	}


	public Message getReplyOf(final String newsgroupName, final Message message, final List<Message> allMessages) throws ExportationException
	{
		checkConnection();
		Message originator = null;

		try
		{
			final String reference = getHeaderValue(message, HEADER_REFERENCES);
			if(reference != null && reference.trim().length() > 0)
			{
				String ref;
				for(final Message m: allMessages)
				{
					ref = getHeaderValue(m, HEADER_MESSAGE_ID);
					if(reference.equals(ref))
					{
						originator = m;
						break;
					}
				}
			}
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}

		return originator;
	}

	/**
	 * @param message
	 * @throws MessagingException
	 */
	private String getHeaderValue(final Message message, final String headerTitle) throws MessagingException
	{
		String value = null;
		final Enumeration headers = message.getAllHeaders();
		while (headers.hasMoreElements())
		{
		    final Header h = (Header) headers.nextElement();
		    final String headerName = h.getName();
			final String headerValue = h.getValue();
			if(headerName.equals(headerTitle))
			{
				value = headerValue;
				break;
			}
		}

		return value;
	}

	public Date getCreatedDate(final Message message) throws ExportationException
	{
		checkConnection();

		try
		{
			return message.getSentDate();
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
	}

	public String getCreator(final Message message)throws ExportationException
	{
		checkConnection();

		try
		{
			String originator = null;

			final Enumeration headers = message.getAllHeaders();
			while (headers.hasMoreElements())
			{
                final Header h = (Header) headers.nextElement();
                final String headerName = h.getName();
				final String headerValue = h.getValue();

				if(headerName.equals(HEADER_COMMENTS))
				{
					if(headerValue.contains(ORIGINATORS_UID))
					{
						int idx = headerValue.indexOf(ORIGINATORS_UID) + ORIGINATORS_UID.length();
						originator = headerValue.substring(idx).replace("\"", "");
						break;
					}
				}
            }
			return originator;
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
	}

	public Date getModerated(final Message message)throws ExportationException
	{
		checkConnection();

		try
		{
			Date moderated = null;

			final Enumeration headers = message.getAllHeaders();
			while (headers.hasMoreElements())
			{
                final Header h = (Header) headers.nextElement();
                final String headerName = h.getName();
				final String headerValue = h.getValue();

				if(headerName.equals(NNTP_POSTING_DATE))
				{
					moderated = POSTING_DATE_FORMAT.get().parse(headerValue);
				}
            }

			return moderated;
		}
		catch (MessagingException e)
		{
			throw new ExportationException("Fail to access to nntp store: " + e.getMessage(), e);
		}
		catch (ParseException e)
		{
			return null;
		}
	}




	private String generateKeyForCache(final String category, final String interestGroup)
	{
		return circaNNTPRootFolder + NEWSGROUP_PATH_SEPARATOR + category + NEWSGROUP_PATH_SEPARATOR + interestGroup;
	}

	private void checkConnection() throws ExportationException
	{
		getNNTPStore();
	}

	private Store getNNTPStore() throws ExportationException
	{
		return getNNTPStore(0);
	}

	private Store getNNTPStore(int tries) throws ExportationException
	{
		if(nntpStore == null || nntpStore.isConnected() == false)
		{
			try
			{
				nntpStore = NNTPResource.getNNTPStore(connectiontimeout, timeout);
				nntpStore.connect(nntpHost, nntpPort, nntpUser, nntpPassword);
			}
			catch (NoSuchProviderException e)
			{
				throw new ExportationException("NTTP Store not setted. See doc: http://java.sun.com/products/javamail/javadocs/javax/mail/Session.html",e);
			}
			catch (MessagingException e)
			{
				throw new ExportationException("Bad NNTP credentials",e);
			}

			if(nntpStore.isConnected() == false)
			{
				throw new ExportationException("Impossible to establish connection with the nntp store");
			}
		}
		else
		{
			try
			{
				nntpStore.getFolder(circaNNTPRootFolder).getMessages();
			}
			catch (MessagingException e)
			{
				nntpStore = null;

				if(tries == CNT_MAX_TRIES)
				{
					throw new ExportationException("Impossible to establish connection to the nntp store after " + tries + " times. Operation aborted.",e);
				}

				return getNNTPStore(++tries);
			}
		}

		return nntpStore;
	}


	public void setNntpHost(String nntpHost)
	{
		this.nntpHost = nntpHost;
	}

	public void setNntpPassword(String nntpPassword)
	{
		this.nntpPassword = nntpPassword;
	}

	public void setNntpPort(int nntpPort)
	{
		this.nntpPort = nntpPort;
	}

	public void setNntpUser(String nntpUser)
	{
		this.nntpUser = nntpUser;
	}

	public void setConnectiontimeout(int connectiontimeout)
	{
		this.connectiontimeout = connectiontimeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public void setNntpNewsgroupCache(SimpleCache<String, List<Folder>> nntpNewsgroupCache)
	{
		this.nntpNewsgroupCache = nntpNewsgroupCache;
	}

	public void setCircaNNTPRootFolder(String circaNNTPRootFolder)
	{
		this.circaNNTPRootFolder = circaNNTPRootFolder;
	}

	public void setNntpMessagesCache(SimpleCache<String, List<Message>> nntpMessagesCache)
	{
		this.nntpMessagesCache = nntpMessagesCache;
	}

	public void setFileClient(FileClient fileClient)
	{
		this.fileClient = fileClient;
	}
}
