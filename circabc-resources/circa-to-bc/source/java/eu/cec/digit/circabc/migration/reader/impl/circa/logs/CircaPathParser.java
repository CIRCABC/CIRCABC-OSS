/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.logs;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Message;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.reader.impl.circa.CircaClientsRegistry;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Calendar;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Document;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.nntp.NNTPClient;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
/**
 * Base interface that helps to parse a Circa log file descrption (info column) to a circabc like path
 *
 * @author Yanick Pignot
 */
public interface CircaPathParser
{
	/**
	 * Get an interest group instance, a circa log line action and a circa info logline and compute a circabc like path for business auditing
	 *
	 * @param interestGroup
	 * @param daoFacory
	 * @param circaAction
	 * @param circaDescription
	 * @return
	 */
	public abstract String parsePath(final InterestGroup interestGroup, final CircaClientsRegistry registry, final String circaAction, final String circaDescription);

	/**
	 * In all cases, return the given interest group path
	 *
	 * @author Yanick Pignot
	 */
	public class InterestGroupPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.computeIGPath(interestGroup).toString();
		}
	}

	/**
	 * Compute the path of a library node
	 *
	 * @author Yanick Pignot
	 */
	public class InformationPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			// Circa path format: /document name.doc

			final StringBuilder builder = LogfilesHelper.computeIGServicePath(interestGroup, Information.class);
			final String path = LogfilesHelper.isolatePath(circaDescription, false);

			if(path.startsWith(FileClient.PATH_SEPARATOR) == false)
			{
				builder.append(FileClient.PATH_SEPARATOR);
			}

			builder.append(path);

			return builder.toString();
		}
	}

	/**
	 * In all cases, return the given interest group information service path
	 *
	 * @author Yanick Pignot
	 */
	public class InformationRootPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.computeIGServicePath(interestGroup, Information.class).toString();
		}
	}

	/**
	 * Compute the path of a library node
	 *
	 * @author Yanick Pignot
	 */
	public class LibraryPathParser implements CircaPathParser
	{
		private static final String LIBRARY_PATH_PREFIX = "/data";

		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			// Circa path format: /data/docpool/_EN_1.15.5_
			final StringBuilder builder = LogfilesHelper.computeIGServicePath(interestGroup, Library.class);
			String path = LogfilesHelper.isolatePath(circaDescription, false);
			appendInLibraryPath(interestGroup, registry, builder, path);
			return builder.toString();
		}

		private void appendInLibraryPath(final InterestGroup interestGroup, final CircaClientsRegistry registry, final StringBuilder builder, String path)
		{
			if(path.startsWith(LIBRARY_PATH_PREFIX))
			{
				path = path.substring(LIBRARY_PATH_PREFIX.length());
			}
			if(path.startsWith(FileClient.PATH_SEPARATOR) == false)
			{
				path = FileClient.PATH_SEPARATOR + path;
			}

			if(path.length() > 1)
			{
				if(path.endsWith(FileClient.PATH_SEPARATOR))
				{
					path = path.substring(0, path.length() - 1);
				}

				if(FilePathUtils.isDocumentPath(path))
				{
					final String docVersionName = FilePathUtils.retreiveFileName(path);
					final String docPoolPath = FilePathUtils.retreiveParentPath(path);
					final String docPoolName = FilePathUtils.retreiveFileName(docPoolPath);
					final String spacePath = FilePathUtils.retreiveParentPath(docPoolPath);

					builder.append(spacePath).append(FileClient.PATH_SEPARATOR);

					final String catName = (String) ElementsHelper.getElementCategory(interestGroup).getName().getValue();
					final String igName = (String) interestGroup.getName().getValue();

					List<Document> documents;
					try
					{
						documents = registry.getDaoFactory().getDocumentDao()
										.getDocumentsFromPool(catName,
												igName,
												getSpaceDblikePath(catName, igName, spacePath),
												docPoolName);
					}
					catch (Exception e)
					{
						documents = null;
					}

					if(documents == null || documents.size() == 0)
					{
						builder.append(FilePathUtils.computeArbitraryNameWithExtension(docPoolName));
					}
					else
					{
						String name = null;
						for(final Document doc: documents)
						{
							if(doc.getIdentifier().endsWith(docVersionName))
							{
								name = doc.getAlternative();
								break;
							}
						}

						if(name == null)
						{
							// search the oldest
							String date = "9999-99-99";
							for(final Document doc: documents)
							{
								try
								{
									final String currentDate = doc.getCreated();
									if(currentDate.compareTo(date) < 0)
									{
										name = doc.getAlternative();
										date = currentDate;
									}
								}
								catch(Exception ex)
								{
									if(name == null)
									{
										name = doc.getAlternative();
									}
								}

							}
						}

						if(name == null || name.trim().length() < 1)
						{
							builder.append(docPoolName);
						}
						else
						{
							builder.append(name);
						}
					}
				}
				else
				{
					builder.append(path);
				}
			}
		}

		private String getSpaceDblikePath(final String catName, final String igName, final String spacePath)
		{
			// transform /space/subSpace in /europa/cat/ig/LIB/SEC/space/subSpace
			final StringBuilder builder = new StringBuilder("/europa/")
				.append(catName)
				.append('/')
				.append(igName)
				.append("/LIB/SEC")
				.append(spacePath.startsWith("/") ? "" : "/")
				.append(spacePath);
			
			final int length = builder.length();
			if(builder.charAt(length -1) == '/')
			{
				builder.delete(length -1, length);
			}
			
    	 	return builder.toString();    	 	
		}
	}

	/**
	 * In all cases, return the given interest group library service path
	 *
	 * @author Yanick Pignot
	 */
	public class LibraryRootPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.computeIGServicePath(interestGroup, Library.class).toString();
		}
	}

	/**
	 * Compute the path of a meeting/event
	 *
	 * @author Yanick Pignot
	 */

	public class MeetingPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			// Circa path format 12471475817602Q2455050
			final StringBuilder builder = LogfilesHelper.computeIGServicePath(interestGroup, Events.class);
			final String path = LogfilesHelper.isolatePath(circaDescription, false);
			final String catName = (String) ElementsHelper.getElementCategory(interestGroup).getName().getValue();
			final String igName = (String) interestGroup.getName().getValue();

			String calTitle;

			try
			{
				final Calendar calendar = registry.getDaoFactory().getCalendarDao().getCalendarById(catName, igName, path);
				if(calendar != null && calendar.getCalendarLinguistics().size() > 0)
				{
					calTitle = calendar.getCalendarLinguistics().get(0).getTitle();
				}
				else
				{
					calTitle = null;
				}
			}
			catch (Exception e)
			{
				calTitle = null;
			}

			if(calTitle != null && calTitle.trim().length() > 0)
			{
				builder.append(FileClient.PATH_SEPARATOR).append(calTitle);
			}
			else
			{
				builder.append(FileClient.PATH_SEPARATOR).append(path);
			}

			return builder.toString();
		}
	}

	/**
	 * In all cases, return the given interest group events service path
	 *
	 * @author Yanick Pignot
	 */
	public class MeetingRootPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.computeIGServicePath(interestGroup, Events.class).toString();
		}
	}

	/**
	 * Compute the path of a newsgroup node
	 *
	 * @author Yanick Pignot
	 */
	public class NewsgroupPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			final String path = LogfilesHelper.isolatePath(circaDescription, true);
			final String forumName = LogfilesHelper.retreiveForumName(path);

			if(forumName == null || forumName.length() < 1)
			{
				return LogfilesHelper.computeIGServicePath(interestGroup, Newsgroups.class).toString();
			}
			else
			{
				final NNTPClient nntpClient = registry.getNntpClient();
				final StringTokenizer tokens = new StringTokenizer(path, ":", false);

				final String newsgroup = tokens.nextToken();
	   		 	final String article   = tokens.hasMoreTokens() ? tokens.nextToken() : null;
	   		 	final String attach    = tokens.hasMoreTokens() ? tokens.nextToken() : null;

	   		 	final StringBuilder builder;
				final boolean isLibraryDiscussion = registry.getNewsgroupReader().isLibraryDiscussion(newsgroup);

				if(isLibraryDiscussion)
				{
					builder = LogfilesHelper.computeIGServicePath(interestGroup, Library.class);
				}
				else
				{
					builder = LogfilesHelper.computeIGServicePath(interestGroup, Newsgroups.class);

					builder
						.append(FileClient.PATH_SEPARATOR)
						.append(forumName);
				}


	   		 	if(article != null)
				{
					try
					{
						final List<Message> messages = nntpClient.getMessages(newsgroup);
						Message message = nntpClient.getMessage(newsgroup, article);
						Message topicMessage;

						do
						{
							 topicMessage = message;
							 message = nntpClient.getReplyOf(newsgroup, topicMessage, messages);
						}
						while(message != null);

						final String subject = topicMessage.getSubject();

						if(isLibraryDiscussion)
						{
							// add document path level /space/document.txt
							final String documentPath = registry.getNewsgroupReader().getDiscussionOn(subject);

							if(documentPath != null)
							{
								new LibraryPathParser().appendInLibraryPath(interestGroup, registry, builder, documentPath + "/_ANY_1.0");

								// add discussion path level /document.txt discussion
								final String docName = builder.substring(builder.lastIndexOf(FileClient.PATH_SEPARATOR) + 1);

								builder
									.append(FileClient.PATH_SEPARATOR)
									.append(NameProperty.toValidName(docName.trim()))
									.append(" discussion");
							}
						}

						builder
							.append(FileClient.PATH_SEPARATOR)
							.append(subject);
					}
					catch (Exception e)
					{
						builder
							.append(FileClient.PATH_SEPARATOR)
							.append(article);
					}
				}

				if(attach != null)
				{
					try
					{
						builder
							.append(FileClient.PATH_SEPARATOR)
							.append(URLDecoder.decode(attach, "UTF-8"));
					}
					catch (UnsupportedEncodingException e)
					{
						builder
							.append(FileClient.PATH_SEPARATOR)
							.append(attach);
					}
				}

				return builder.toString();
			}


		}
	}

	/**
	 * In all cases, return the given interest group newsgroup service path
	 *
	 * @author Yanick Pignot
	 */
	public class NewsgroupRootPathParser implements CircaPathParser
	{
		public String parsePath(final InterestGroup interestGroup,  final CircaClientsRegistry registry, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.computeIGServicePath(interestGroup, Newsgroups.class).toString();
		}
	}
}
