/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.text.MessageFormat;

import org.alfresco.util.ParameterCheck;

/**
 * Helper classes that define location of db files in a circa hierarchy
 *
 * @author Yanick Pignot
 */
public abstract class DbFileLocations
{
	private static final String PATH_USER_ACCESS_LOGS = "{0}/../logs/user_access/_user_access.db";

	private static final String PATH_ROOT_DOMAINS = "{0}/domains.db";

	private static final String PATH_LINK_SECTIONS = "{0}/{1}/link_sections.db";
	private static final String PATH_SHARED_SECTIONS = "{0}/{1}/shared_sections.db";

	private static final String PATH_APPLICANTS = "{0}/{1}/{2}/applicants.db";

	private static final String PATH_DIRECTORY_ITEMNOTIFICATION = "{0}/{1}/{2}/directory/itemnotification.db";
	private static final String PATH_DIRECTORY_USERNOTIFICATION = "{0}/{1}/{2}/directory/usernotification.db";

	private static final String PATH_INFORMATION_ITEMACLS = "{0}/{1}/{2}/info/itemacls.db";
	private static final String PATH_INFORMATION_USERACLS = "{0}/{1}/{2}/info/useracls.db";
	private static final String PATH_INFORMATION_ENTR_POINTS = "{0}/{1}/{2}/info/entry_points.db";

	private static final String PATH_LIBRARY_ITEMNOTIFICATION = "{0}/{1}/{2}/library/itemnotification.db";
	private static final String PATH_LIBRARY_USERNOTIFICATION = "{0}/{1}/{2}/library/usernotification.db";
	private static final String PATH_LIBRARY_ITEMACLS = "{0}/{1}/{2}/library/itemacls.db";
	private static final String PATH_LIBRARY_USERACLS = "{0}/{1}/{2}/library/useracls.db";

	private static final String PATH_MEETINGS_DATA = "{0}/{1}/{3}/meetings/data/meetings.db";

	private static final String PATH_NEWSGROUPS_ITEMNOTIFICATION = "{0}/{1}/{2}/newsgroups/itemnotification.db";
	private static final String PATH_NEWSGROUPS_USERNOTIFICATION = "{0}/{1}/{2}/newsgroups/usernotification.db";
	private static final String PATH_NEWSGROUPS_ITEMACLS = "{0}/{1}/{2}/newsgroups/itemacls.db";
	private static final String PATH_NEWSGROUPS_USERACLS = "{0}/{1}/{2}/newsgroups/useracls.db";

	public static final String getUserAccessFile(final String rootPath)
	{
		return  getPath(PATH_USER_ACCESS_LOGS, rootPath);
	}

	public static final String getRootDomainsFile(final String rootPath)
	{
		return  getPath(PATH_ROOT_DOMAINS, rootPath);
	}

	public static final String getLinkSectionsFile(final String rootPath, final String categoryName)
	{
		return  getPath(PATH_LINK_SECTIONS, rootPath, categoryName);
	}

	public static final String getSharedSectionsFile(final String rootPath, final String categoryName)
	{
		return  getPath(PATH_SHARED_SECTIONS, rootPath, categoryName);
	}

	public static final String getApplicantsFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_APPLICANTS, rootPath, categoryName, interestGroupName);
	}

	public static final String getDirectoryItemNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_DIRECTORY_ITEMNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getDirectoryUserNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_DIRECTORY_USERNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getInformationItemAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_INFORMATION_ITEMACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getInformationUserAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_INFORMATION_USERACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getInformationEntryPoints(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_INFORMATION_ENTR_POINTS, rootPath, categoryName, interestGroupName);
	}

	public static final String getLibraryItemNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_LIBRARY_ITEMNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getLibraryUserNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_LIBRARY_USERNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getLibraryItemAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_LIBRARY_ITEMACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getLibraryUserAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_LIBRARY_USERACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getNewsgroupItemNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_NEWSGROUPS_ITEMNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getNewsgroupUserNotificationFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_NEWSGROUPS_USERNOTIFICATION, rootPath, categoryName, interestGroupName);
	}

	public static final String getNewsgroupItemAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_NEWSGROUPS_ITEMACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getNewsgroupUserAclFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_NEWSGROUPS_USERACLS, rootPath, categoryName, interestGroupName);
	}

	public static final String getMeetingDataFile(final String rootPath, final String categoryName, final String interestGroupName)
	{
		return  getPath(PATH_MEETINGS_DATA, rootPath, categoryName, interestGroupName);
	}

	private static final String getPath(final String pathPattern, final Object ... params)
	{
		switch (params.length)
		{
			case 3:
				ParameterCheck.mandatoryString("The interest group name", (String) params[2]);
			case 2:
				ParameterCheck.mandatoryString("The category name", (String) params[1]);
			case 1:
				ParameterCheck.mandatoryString("the root location", (String) params[0]);
			break;

			default:
				throw new IllegalStateException("Bad argument number");
		}


		return MessageFormat.format(pathPattern, params);
	}
}
