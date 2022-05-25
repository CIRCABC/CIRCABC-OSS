/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.util;

import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Parse a file path to a db identifier
 *
 * @author Yanick Pignot
 */
public class ParsedPath
{
	private static final String DB_SECTION_LOCATION = "LIB/SEC";
	private static final String DB_DOCUMENT_LOCATION = "LIB/DOC";
	private static final String DB_INFORMATION_LOCATION = "INF";
	private static final String DB_MEETING_LOCATION = "MEE";
	private static final String DB_NEWS_LOCATION = "NEW";

	final String domainPrefix;
	private final String ig;
	private final String virtualCirca;
	private String inServicePath;
	final FileClient fileClient;
	private Boolean fromLibrary = Boolean.FALSE;
	private Boolean fromMeeting = Boolean.FALSE;
	private Boolean fromInformation = Boolean.FALSE;
	private Boolean fromNewsgroup = Boolean.FALSE;

	private Boolean isSection = Boolean.FALSE;

	public ParsedPath(final String filePath, final FileClient fileClient, final String domainPrefix) throws ExportationException
	{
		this.fileClient = fileClient;
		this.domainPrefix = domainPrefix;

		final boolean isFilePath = filePath.startsWith(fileClient.getDataRoot());
		final boolean isDbPath = filePath.startsWith(domainPrefix);

		if(isDbPath == isFilePath)
		{
			if(isDbPath)
			{
				throw new ExportationException("The domain prefix cant be equals to file system data root !" + domainPrefix);
			}
			else
			{
				throw new ExportationException("The path not supported: " + filePath);
			}
		}

		final String pathPrefix = isFilePath ? fileClient.getDataRoot() : domainPrefix;

		final CategoryInterestGroupPair pair = FilePathUtils.getInterestGroupFromPath(filePath, pathPrefix);

		virtualCirca = pair.getCategory();
		ig = pair.getInterestGroup();

		final String servicePath = FilePathUtils.getIgServicePath(filePath, pathPrefix, virtualCirca, ig);

		String serviceLocation;

		if(servicePath.startsWith((serviceLocation = fileClient.getLibraryDataLocation())))
		{
			fromLibrary = Boolean.TRUE;

			if(fileClient.isFile(filePath) == false)
			{
				isSection = Boolean.TRUE;
			}

		}
		else if(servicePath.startsWith((serviceLocation = fileClient.getInformationDataLocation())))
		{
			fromInformation = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = fileClient.getMeetingsDataLocation())))
		{
			fromMeeting = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = fileClient.getNewsDataLocation())))
		{
			fromNewsgroup = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = DB_DOCUMENT_LOCATION)))
		{
			fromLibrary = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = DB_SECTION_LOCATION)))
		{
			fromLibrary = Boolean.TRUE;
			isSection = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = DB_MEETING_LOCATION)))
		{
			fromMeeting = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = DB_INFORMATION_LOCATION)))
		{
			fromInformation = Boolean.TRUE;
		}
		else if(servicePath.startsWith((serviceLocation = DB_NEWS_LOCATION)))
		{
			fromNewsgroup = Boolean.TRUE;
		}
		else
		{
			// the path is perharps a ig root
			serviceLocation = null;
		}

		if(serviceLocation != null)
		{
			inServicePath = filePath.substring(filePath.indexOf(serviceLocation) + serviceLocation.length());

			if(inServicePath.charAt(0) != '/')
			{
				inServicePath = "/" + inServicePath;
			}
		}

	}

	/**
	 * @return a string representation of the file usable in the database:
	 *
	 * Example:/europa/vc/ig/LIB/SEC/section_subfolder/document
	 */
	public String toDbReference()
	{
		final StringBuffer buffer = new StringBuffer(domainPrefix);
		buffer
			.append(virtualCirca)
			.append('/')
			.append(ig)
			.append('/');

		if(fromLibrary)
		{
			if(isSection)
			{
				buffer.append(DB_SECTION_LOCATION);
			}
			else
			{
				buffer.append(DB_DOCUMENT_LOCATION);
			}
		}
		else if(fromInformation)
		{
			buffer.append(DB_INFORMATION_LOCATION);
		}
		else if(fromMeeting)
		{
			buffer.append(DB_MEETING_LOCATION);
		}
		else if(fromNewsgroup)
		{
			buffer.append(DB_NEWS_LOCATION);
		}

		if(inServicePath != null)
		{
			buffer.append(inServicePath);
		}

		return buffer.toString();
	}

	/**
	 * @return a string representation of the file usable on the file system:
	 *
	 * Example:www/data/vc/ig/library/data/section_subfolder/document/_EN_1.0_
	 */
	public String toFilePath()
	{
		final StringBuffer buffer = new StringBuffer(fileClient.getDataRoot());
		buffer
			.append('/')
			.append(virtualCirca)
			.append('/')
			.append(ig)
			.append('/');

		if(fromLibrary)
		{
			buffer.append(fileClient.getLibraryDataLocation());
		}
		else if(fromInformation)
		{
			buffer.append(fileClient.getInformationDataLocation());
		}
		else if(fromMeeting)
		{
			buffer.append(fileClient.getMeetingsDataLocation());
		}
		else if(fromNewsgroup)
		{
			buffer.append(fileClient.getNewsDataLocation());
		}

		if(inServicePath != null)
		{
			buffer.append(inServicePath);
		}

		return buffer.toString();
	}

	/**
	 * @return a string representation of the file usable in some berkeley db file.
	 *
	 * Example:/vc/ig/section_subfolder
	 */
	public String toShortLibSectionPath()
	{
		final StringBuffer buffer = new StringBuffer("");
		buffer
			.append(virtualCirca)
			.append('/')
			.append(ig);

		if(inServicePath != null)
		{
			buffer
				.append(inServicePath);
		}


		return buffer.toString();
	}


	/**
	 * @return the fromInformation
	 */
	public final Boolean isFromInformation()
	{
		return fromInformation;
	}

	/**
	 * @return the fromLibrary
	 */
	public final Boolean isFromLibrary()
	{
		return fromLibrary;
	}

	/**
	 * @return the fromMeeting
	 */
	public final Boolean isFromMeeting()
	{
		return fromMeeting;
	}

	/**
	 * @return the fromNewsgroup
	 */
	public final Boolean isFromNewsgroup()
	{
		return fromNewsgroup;
	}

	/**
	 * @return the ig
	 */
	public final String getInterestGroup()
	{
		return ig;
	}

	/**
	 * @return the virtualCirca
	 */
	public final String getVirtualCirca()
	{
		return virtualCirca;
	}

	/**
	 * @return the isSection
	 */
	public final Boolean isSection()
	{
		return isSection;
	}

	/**
	 * @return the inServicePath
	 */
	public final String getInServicePath()
	{
		return inServicePath;
	}
}