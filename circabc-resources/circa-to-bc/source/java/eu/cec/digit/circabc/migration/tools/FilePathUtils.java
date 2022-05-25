/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.tools;

import static eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient.PATH_SEPARATOR;

import java.text.MessageFormat;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Util class that contains methods to perfor operation on file paths
 *
 * @author Yanick Pignot
 */
public abstract class FilePathUtils
{
	private FilePathUtils(){}

	public static final String NO_LANGUAGE = "XX";
	public static final String DEFAULT_LANGUAGE = "EN";
	public static final String SECTION_EXTENSION  = ".sec";

	/**
	 * The content names or path are under the form _XX_1.0_ or _ANY_1.15.2 or ./data/cat/ig/library/mydocpdf/_FR_12.4_
	 **/
	public static final String CONTENT_FILE_NAME_REGEX = "(.)?(.)?(/[-_A-Za-z0-9\\.]*)*_((([A-Z][A-Z])_([0-9]+.)*[0-9]+_)|(ANY_([0-9]+.)*[0-9]+))";

	private static final String TRANSLATION_REGEX = "(.)?(.)?(/[-_A-Za-z0-9\\.]*)*_{0}_([0-9]+.)*[0-9]+_";
	private static final String VERSIONS_REGEX = "(.)?(.)?(/[-_A-Za-z0-9\\.]*)*_[A-Z][A-Z]_{0}_";
	private static final int DEFAULT_EXTENSION_POS_FROM_END = 3;
	private static final String DOSSIER_EXTENSION  = PATH_SEPARATOR + ".is_dosier";
	private static final String URL_EXTENSION  = PATH_SEPARATOR + ".is_url";
	private static final char DOT = '.';

	public static final String retreiveLanguageFromPath(final String path)
	{
		final String name = retreiveFileName(path);
		return name.substring(1, 3);
	}

	public static final String retreiveVersionFromPath(final String path)
	{
		final String name = retreiveFileName(path);
		return name.substring(4, name.length() - 1);
	}

	public static final String retreiveFileName(final String path)
	{
		int index = path.lastIndexOf(PATH_SEPARATOR);

		if(index < 0)
		{
			return path;
		}
		else
		{
			return path.substring(index + 1);
		}
	}

	public static final String retreiveParentPath(final String path)
	{
		int index = path.lastIndexOf(PATH_SEPARATOR);

		if(index < 0)
		{
			return path;
		}
		else
		{
			return path.substring(0, index);
		}
	}

	public static final String retreiveParentName(final String path)
	{
		final String parentPath = retreiveParentPath(path);
		return retreiveFileName(parentPath);
	}

	public static final String removeExtension(final String path)
	{
		final String parentPath;
		final String fileName;
		if(path.contains(PATH_SEPARATOR))
		{
			parentPath = retreiveParentPath(path);
			fileName = retreiveFileName(path);
		}
		else
		{
			parentPath = "";
			fileName = path;
		}

		final int dotIdx = fileName.lastIndexOf(DOT);

		String noExtName;
		if(dotIdx >= 0)
		{
			noExtName = fileName.substring(0, dotIdx);
		}
		else
		{
			noExtName = fileName;
		}

		return parentPath + PATH_SEPARATOR + noExtName;
	}

	public static final String computeArbitraryNameWithExtension(final String name)
	{
		return computeArbitraryNameWithExtension(name, DEFAULT_EXTENSION_POS_FROM_END);
	}

	public static final String computeArbitraryNameWithExtension(final String name, final int dotExpectedPos)
	{
		if(name == null || name.contains(".") || name.length() <= dotExpectedPos)
		{
			return name;
		}
		else
		{
			final int dotIndex = name.length() - dotExpectedPos;
			return name.substring(0, dotIndex) + "." + name.substring(dotIndex);
		}
	}

	public static final boolean isDocumentPath(final String path)
	{
		return path.matches(CONTENT_FILE_NAME_REGEX);
	}

	public static final boolean isDocumentPathFromVersion(final String path, final String version)
	{
		return isDocumentPath(path) && path.matches(MessageFormat.format(VERSIONS_REGEX, version));
	}

	public static final boolean isDocumentPathFromLanguage(final String path, final String language)
	{
		return isDocumentPath(path) && path.matches(MessageFormat.format(TRANSLATION_REGEX, language));
	}

	public static final boolean isDocumentPathFromNoLanguage(final String path)
	{
		return isDocumentPathFromLanguage(path, NO_LANGUAGE);
	}

	public static final String convertNoLocaleToDefault(final String path)
	{
		return path.replaceFirst("/_" + NO_LANGUAGE +  "_", "/_" + DEFAULT_LANGUAGE +  "_");
	}

	public static boolean isContent(final String path, final FileClient fileClient) throws ExportationException
	{
		return FilePathUtils.isDocumentPath(path) && fileClient.isFile(path);
	}

	public static final boolean isDocument(final String path, final FileClient fileClient) throws ExportationException
	{
		if(isDossier(path, fileClient) || isUrl(path, fileClient))
		{
			return false;
		}
		else
		{
			boolean contentFound = false;
			final List<String> childs = fileClient.list(path, false, true, false);

			for (final String child : childs)
			{
				if(isContent(child, fileClient))
				{
					contentFound = true;
					break;
				}
			}

			return contentFound;
		}
	}

	public static final boolean isDossier(final String path, final FileClient fileClient) throws ExportationException
	{
		return fileClient.exists(path + DOSSIER_EXTENSION);
	}

	public static final boolean isLink(final String path, final FileClient fileClient, final String linkPrefix) throws ExportationException
	{
		return path.startsWith(linkPrefix);
	}

	public static final boolean isSpace(final String path, final FileClient fileClient) throws ExportationException
	{
		return fileClient.exists(path + SECTION_EXTENSION) && fileClient.isSymbolicLink(path) == false;
	}

	public static final boolean isUrl(final String path, final FileClient fileClient) throws ExportationException
	{
		return fileClient.exists(path + URL_EXTENSION);
	}

	public static final CategoryInterestGroupPair getInterestGroupFromPath(final String path, final String domainPrefix)
	{
		String pathWithoutRoot;

		final int rootPosition = path.indexOf(domainPrefix);
		if(rootPosition < 0)
		{
			throw new IllegalStateException("Unknow file path data root:" + path);
		}
		else
		{
			int indexStart = rootPosition + domainPrefix.length();
			if(domainPrefix.length() > 1 && domainPrefix.endsWith("/") == false)
			{
				 indexStart++;
			}

			pathWithoutRoot = path.substring(indexStart);
		}

		final String virtualCirca = pathWithoutRoot.substring(0, pathWithoutRoot.indexOf('/'));

		final int endOfVirtualCirca = virtualCirca.length() + 1;

		int indexEndIg = pathWithoutRoot.indexOf('/', endOfVirtualCirca);
		if(indexEndIg < 0)
		{
			indexEndIg = pathWithoutRoot.length();
		}

		final String ig = pathWithoutRoot.substring(endOfVirtualCirca, indexEndIg);

		return new CategoryInterestGroupPair(virtualCirca, ig);
	}

	public static final String getIgServicePath(final String path, final String domainPrefix, final String virtualCirca, final String interestGroup)
	{
		final StringBuffer buff = new StringBuffer(domainPrefix);
		buff
			.append('/')
			.append(virtualCirca)
			.append('/')
			.append(interestGroup)
			.append('/');

		int pos = path.indexOf(buff.toString());

		return path.substring(pos + buff.length());
	}


}
