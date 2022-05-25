/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import static eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient.PATH_SEPARATOR;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.util.VersionNumber;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaHomePageReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.ldap.CircaLdapClient;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concreate file reader for circa library service
 *
 * @author Yanick Pignot
 */
public class CircaLibFileReaderImpl implements RemoteFileReader
{
	/*Use ldap to get categories and ig (faster and more secure)*/
    private CircaLdapClient ldapClient;
	private FileClient fileClient;
	private String linkPrefix;
	private CircaHomePageReader homePageReader;

	public String generateResouceString(final String basePath)
	{
		return fileClient.generateResouceString(basePath);
	}

	public List<String> getCategoriesPath() throws ExportationException
	{
		try
		{
			final List<String> names= ldapClient.getCategoryNames();
			final List<String> paths = new ArrayList<String>(names.size());
			for(final String name: names)
			{
				paths.add(getCategoryPathWithName(name));
			}
			return paths;
		}
		catch (final Exception e)
		{
			throw new ExportationException("Impossible to retreive the category names: " + e.getMessage(), e);
		}
	}

	public String getCategoryHeaderName(final String categoryName) throws ExportationException
	{
		return getCategoryHeaderName(categoryName, homePageReader.getDefaultHeaderName());
	}

	public Set<String> getAllCategoryHeaders() throws ExportationException
	{
		return homePageReader.getAllCategoryHeaders();
	}

	public String getCategoryHeaderName(final String categoryName, final String defaultCategoryName) throws ExportationException
	{
		final String categoryHeader = homePageReader.getCategoryHeader(categoryName);

		if(categoryHeader == null || categoryHeader.length() < 1)
		{
			return defaultCategoryName;
		}
		else
		{
			return categoryHeader;
		}
	}

	public String getCategoryPathWithName(final String categoryName) throws ExportationException
	{
		return getCircabcPath() + PATH_SEPARATOR + categoryName;
	}

	public String getCircabcPath()
	{
		return fileClient.getDataRoot();
	}

	public List<Locale> getContentTranslations(final String path) throws ExportationException
	{
		final List<String> contents = getContents(path);
		final List<Locale> languages = new ArrayList<Locale>(contents.size());

		Locale locale;
		for(final String content: contents)
		{
			if(FilePathUtils.isDocumentPathFromNoLanguage(content))
			{
				// DIGIT-CIRCABC-1386
				locale = Locale.ENGLISH;
			}
			else
			{
				locale = I18NUtil.parseLocale(FilePathUtils.retreiveLanguageFromPath(content));
			}

			if(!languages.contains(locale))
			{
				languages.add(locale);
			}
		}

		return languages;
	}

	public Map<VersionNumber, String> getContentVersions(final String path, final Locale locale) throws ExportationException
	{
		final String language = (locale == null) ? FilePathUtils.NO_LANGUAGE : locale.getLanguage().toUpperCase();

		final boolean isEnglish = language.equals(FilePathUtils.DEFAULT_LANGUAGE);

		final List<String> contents = getContents(path);
		final Map<VersionNumber, String> contentsByVersion = new HashMap<VersionNumber, String>();

		for(final String content: contents)
		{
			if(isContentLanguage(content, language) ||
					// DIGIT-CIRCABC-1386
				   (isEnglish && FilePathUtils.isDocumentPathFromNoLanguage(content)))
			{
				contentsByVersion.put(new VersionNumber(FilePathUtils.retreiveVersionFromPath(content)), content);
			}
		}

		return contentsByVersion;
	}

	public String getInterestGroupPathWithNames(final String categoryName, final String igName) throws ExportationException
	{
		return getCategoryPathWithName(categoryName) + PATH_SEPARATOR + igName;
	}

	public List<String> getInterestGroupsPath(final String categoryPath) throws ExportationException
	{
		try
		{
			final String catName = FilePathUtils.retreiveFileName(categoryPath);

			final List<String> names= ldapClient.getInterestGroupNames(catName);
			final List<String> paths = new ArrayList<String>(names.size());
			for(final String name: names)
			{
				paths.add(getInterestGroupPathWithNames(catName, name));
			}
			return paths;
		}
		catch (final Exception e)
		{
			throw new ExportationException("Impossible to retreive the category names: " + e.getMessage(), e);
		}
	}

	public String getPathSeparator()
	{
		return PATH_SEPARATOR;
	}

	public List<String> getContents(final String path) throws ExportationException
	{
		String contentFoldePath = null;

		if(fileClient.isFile(path))
		{
			final String parent = path.substring(0, path.lastIndexOf(PATH_SEPARATOR));
			contentFoldePath = parent;
		}
		else
		{
			contentFoldePath = path;
		}

		return fileClient.list(contentFoldePath, false, true, false);
	}

	public boolean isDocument(final String path) throws ExportationException
	{
		return FilePathUtils.isDocument(path, fileClient);
	}

	public long getFileSize(final String path) throws ExportationException
	{
		return fileClient.getFileSize(path);
	}

	public boolean isDossier(final String path) throws ExportationException
	{
		return FilePathUtils.isDossier(path, fileClient);
	}

	public boolean isLink(final String path) throws ExportationException
	{
		return FilePathUtils.isLink(path, fileClient, linkPrefix);
	}


	public boolean isSpace(final String path) throws ExportationException
	{
		return FilePathUtils.isSpace(path, fileClient);
	}

	public boolean isUrl(final String path) throws ExportationException
	{
		return FilePathUtils.isUrl(path, fileClient);
	}

	public boolean isSharedSpaceLink(final String path) throws ExportationException
	{
		// we found two implementation of shared space link in circa
		if(fileClient.isSymbolicLink(path))
		{
			// first it is a symbolic link
			return true;
		}
		else if(path.endsWith(".sec"))
		{
			// second it a a section (.sec file) without any concrete directory associated

			final String parentPath = FilePathUtils.retreiveParentPath(path);
			final String expectedSpaceName = FilePathUtils.removeExtension(path);

			final List<String> siblingSpaces = fileClient.list(parentPath, false, false, true);

			return siblingSpaces.contains(expectedSpaceName) == false;
		}
		else
		{
			return false;
		}
	}

	public List<String> listChidrenPath(final String parentPath) throws ExportationException
	{
		if(isDossier(FilePathUtils.retreiveParentPath(parentPath)))
		{
			final List<String> links = new ArrayList<String>();

			final String content = fileClient.downloadAsString(parentPath);
			final ByteArrayInputStream dossierContent = new ByteArrayInputStream(content.getBytes());
	        final BufferedReader reader = new BufferedReader(new InputStreamReader(dossierContent));
	        try
			{

		        String strLine = null;

				while ((strLine = reader.readLine()) != null)
				{
					if(links.contains(strLine) == false)
					{
						links.add(strLine);
					}
				}

			}
	        catch (IOException e)
			{
				throw new ExportationException(e);
			}
	        finally
	        {
	        	try
				{
					dossierContent.close();
					reader.close();
				}
	        	catch (IOException e)
				{
					throw new ExportationException(e);
				}
	        }

			return links;
		}
		else
		{
			return fileClient.list(parentPath, false);
		}
	}

	public void setNodePath(final XMLNode node)
	{
		String path = null;
		if(node instanceof Circabc || node instanceof CategoryHeader)
		{
			path = getCircabcPath();
		}
		else
		{
			final XMLNode parent = (XMLNode) ElementsHelper.getParent(node);
			final String parentPath = ElementsHelper.getExportationPath(parent);
			String leafName = null;

			if(node instanceof NamedNode)
			{

				leafName = ((NamedNode) node).getName().getValue().toString();
			}
			else
			{
				leafName = getServiceLocation(node.getClass());
			}

			path = parentPath + PATH_SEPARATOR + leafName;
		}

		ElementsHelper.setExportationPath(node, path);
	}

	public List<String> getDossierTranslations(final String path) throws ExportationException
	{
		return getContents(path);
	}

	public List<String> getUrlTranslations(final String path) throws ExportationException
	{
		return getContents(path);
	}

	public boolean exists(final String path) throws ExportationException
	{
		return fileClient.exists(path);
	}

	private static Map<Class, String> serviceLocations = null;

	private String getServiceLocation(final Class clazz)
	{
		if(serviceLocations == null)
		{
			serviceLocations = new HashMap<Class, String>(5);
			serviceLocations.put(Information.class, fileClient.getInformationDataLocation());
			serviceLocations.put(Library.class, fileClient.getLibraryDataLocation());
			serviceLocations.put(Directory.class, fileClient.getDirectoryDataLocation());
			serviceLocations.put(Events.class, fileClient.getMeetingsDataLocation());
			serviceLocations.put(Newsgroups.class, fileClient.getNewsDataLocation());
		}

		return serviceLocations.get(clazz);

	}


	@SuppressWarnings("unused")
	private boolean isContentVersion(final String path, final String version) throws ExportationException
	{
		return FilePathUtils.isDocumentPathFromVersion(path, version) && fileClient.isFile(path);
	}

	private boolean isContentLanguage(final String path, final String lang) throws ExportationException
	{
		return FilePathUtils.isDocumentPathFromLanguage(path, lang) && fileClient.isFile(path);
	}

	public final void setFileClient(final FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	public final void setLinkPrefix(final String linkPrefix)
	{
		this.linkPrefix = linkPrefix;
	}

	/**
	 * @param homePageReader the homePageReader to set
	 */
	public final void setHomePageReader(CircaHomePageReader homePageReader)
	{
		this.homePageReader = homePageReader;
	}


	/**
	 * @return the fileClient
	 */
	protected final FileClient getFileClient()
	{
		return fileClient;
	}

	public final void setLdapClient(CircaLdapClient ldapClient)
	{
		this.ldapClient = ldapClient;
	}
}