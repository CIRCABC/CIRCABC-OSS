/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.file;

import static eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient.PATH_SEPARATOR;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Class that helps the reading of interst group configuration
 *
 *  The category titles and category headers are found in the circa home page and not in a server.
 *
 * @author Yanick Pignot
 */
public class CircaIGConfigReader
{
	private FileClient fileClient;
	private String igConfigFile;
	private String iconFolderName;
	private String iconPropertyName;
	private String igHomePropertyName;
	private String infHomePropertyName;

	public List<String> getInterestgroupIconsPath(final String interestGroupPath) throws ExportationException
	{
		final String folderPath = computeIconFolderPath(interestGroupPath);
		return fileClient.list(folderPath, false, true, false);
	}

	public String getInterestgroupDefaultIconPath(final String interestGroupPath) throws ExportationException
	{
		final String iconName = getIGConfig(interestGroupPath, iconPropertyName);
		if(iconName == null || iconName.trim().length() < 1)
		{
			return null;
		}
		else
		{
			return computeIconFolderPath(interestGroupPath) + PATH_SEPARATOR + iconName;
		}
	}

	public String getInterestGroupHomePage(final String interestGroupPath) throws ExportationException
	{
		return getHomePage(interestGroupPath, igHomePropertyName);
	}

	public String getInformationHomePage(final String interestGroupPath) throws ExportationException
	{
		return getHomePage(interestGroupPath, infHomePropertyName);
	}

	private String getHomePage(final String interestGroupPath, final String propertyname) throws ExportationException
	{
		final String homePage = getIGConfig(interestGroupPath, propertyname);
		if(homePage == null || homePage.trim().length() < 1)
		{
			return null;
		}
		else
		{
			return homePage;
		}
	}



	private String getIGConfig(final String interestGroupPath, final String propertyName) throws ExportationException
	{
		final String configFilePath = interestGroupPath + PATH_SEPARATOR + igConfigFile;
		if(fileClient.exists(configFilePath))
		{
			final Properties props = new Properties();
			final ByteArrayInputStream is = new ByteArrayInputStream(fileClient.downloadAsString(configFilePath).getBytes());
			try
			{
				props.load(is);
				return props.getProperty(propertyName);
			}
			catch (IOException e)
			{
				throw new ExportationException("Impossible to read Interest group configuration file not found at " + configFilePath);
			}
			finally
			{
				try
				{
					is.close();
				}
				catch(final Exception ignore){}
			}
		}
		else
		{
			throw new ExportationException("Interest group configuration file not found at " + configFilePath);
		}
	}

	/**
	 * @param interestGroupPath
	 * @return
	 */
	private String computeIconFolderPath(final String interestGroupPath)
	{
		final String igName = FilePathUtils.retreiveFileName(interestGroupPath);
		final String category = FilePathUtils.retreiveParentName(interestGroupPath);

		final StringBuffer buffer = new StringBuffer(fileClient.getIconRoot());
		buffer
			.append(PATH_SEPARATOR)
			.append(category)
			.append(PATH_SEPARATOR)
			.append(igName)
			.append(PATH_SEPARATOR)
			.append(iconFolderName);

		final String folderPath = buffer.toString();
		return folderPath;
	}

	public final void setFileClient(final FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	/**
	 * @param iconFolderName the iconFolderName to set
	 */
	public final void setIconFolderName(String iconFolderName)
	{
		this.iconFolderName = iconFolderName;
	}


	/**
	 * @param iconPropertyName the iconPropertyName to set
	 */
	public final void setIconPropertyName(String iconPropertyName)
	{
		this.iconPropertyName = iconPropertyName;
	}


	/**
	 * @param igConfigFile the igConfigFile to set
	 */
	public final void setIgConfigFile(String igConfigFile)
	{
		this.igConfigFile = igConfigFile;
	}

	/**
	 * @param igHomePropertyName the igHomePropertyName to set
	 */
	public final void setIgHomePropertyName(String igHomePropertyName)
	{
		this.igHomePropertyName = igHomePropertyName;
	}

	/**
	 * @param infHomePropertyName the infHomePropertyName to set
	 */
	public final void setInfHomePropertyName(String infHomePropertyName)
	{
		this.infHomePropertyName = infHomePropertyName;
	}


}
