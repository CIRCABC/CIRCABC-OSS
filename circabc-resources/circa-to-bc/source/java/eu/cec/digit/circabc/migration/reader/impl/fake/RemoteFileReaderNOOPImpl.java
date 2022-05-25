/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.util.VersionNumber;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support RemoteFileReader
 *
 * @author Yanick Pignot
 */
public class RemoteFileReaderNOOPImpl implements RemoteFileReader
{

	public boolean exists(String path) throws ExportationException
	{
		return false;
	}

	public String generateResouceString(String basePath)
	{
		return null;
	}

	public Set<String> getAllCategoryHeaders() throws ExportationException
	{
		return Collections.<String>emptySet();
	}

	public List<String> getCategoriesPath() throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public String getCategoryHeaderName(String categoryName) throws ExportationException
	{
		return null;
	}

	public String getCategoryHeaderName(String categoryName, String defaultCategoryName) throws ExportationException
	{
		return null;
	}

	public String getCategoryPathWithName(String categoryName) throws ExportationException
	{
		return null;
	}

	public String getCircabcPath()
	{
		return null;
	}

	public List<Locale> getContentTranslations(String path) throws ExportationException
	{
		return Collections.<Locale>emptyList();
	}

	public Map<VersionNumber, String> getContentVersions(String path, Locale locale) throws ExportationException
	{
		return Collections.<VersionNumber, String>emptyMap();
	}

	public List<String> getContents(String path) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public List<String> getDossierTranslations(String path) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public long getFileSize(String path) throws ExportationException
	{
		return 0;
	}

	public String getInterestGroupPathWithNames(String categoryName, String igName) throws ExportationException
	{
		return null;
	}

	public List<String> getInterestGroupsPath(String categoryPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public String getPathSeparator()
	{
		return File.pathSeparator;
	}

	public List<String> getUrlTranslations(String path) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public boolean isDocument(String path) throws ExportationException
	{
		return false;
	}

	public boolean isDossier(String path) throws ExportationException
	{
		return false;
	}

	public boolean isLink(String path) throws ExportationException
	{
		return false;
	}

	public boolean isSharedSpaceLink(String path) throws ExportationException
	{
		return false;
	}

	public boolean isSpace(String path) throws ExportationException
	{
		return false;
	}

	public boolean isUrl(String path) throws ExportationException
	{
		return false;
	}

	public List<String> listChidrenPath(String parentPath) throws ExportationException
	{
		return Collections.<String>emptyList();
	}

	public void setNodePath(XMLNode node)
	{
	}


}