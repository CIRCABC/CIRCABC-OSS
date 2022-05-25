/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.util.VersionNumber;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for classes that help the reading of a remote structure for an exportation. Implementations
 * can read data either via HTTP, FTP, FileSystem, circabc services, ...
 *
 * @author Yanick Pignot
 */
public interface RemoteFileReader
{
	/**
	 * Generate a string that represents the path or a resource.
	 *
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 *
	 * ie: for a fileSystem implementation, the return must be file:///c:/circabc/cat/ig/space/document.pdf
	 * 	   for a ftp implementation, the return must be ftp://user:pwd@hostname:21/circabc/cat/ig/space/document.pdf
	 *
	 * @return	the transformation of a path to be inserted in the import root document before an Importation Process
	 */
	public abstract String generateResouceString(final String basePath);

	/**
	 * Return the path separator that will be applied by the implementation
	 *
	 * @return
	 */
	public abstract String getPathSeparator();

	/**
	 * Set the path of a given node.
	 *
	 * @param node
	 */
	public abstract void setNodePath(final XMLNode node);

	/**
	 * Get a binded xml representation of the circabc root
	 *
	 * @return
	 */
	public abstract String getCircabcPath();

	/**
	 * Get a category path for a given name
	 *
	 * @param categoryName
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract String getCategoryPathWithName(final String categoryName)  throws ExportationException;


	/**
	 * Get all found category paths
	 *
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<String> getCategoriesPath() throws ExportationException;

	/**
	 * Get all found interest group paths for a given category path
	 *
	 * @param categoryPath
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<String> getInterestGroupsPath(final String categoryPath) throws ExportationException;

	/**
	 * Get an interest group path for a given category name and a given interest group name.
	 *
	 * @param categoryName
	 * @param igName
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract String getInterestGroupPathWithNames(final String categoryName, final String igName) throws ExportationException;

	/**
	 * Get the category header name for a given category
	 *
	 * @param categoryName
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract String getCategoryHeaderName(final String categoryName) throws ExportationException;


	/**
	 * Get all category header names
	 *
	 * @return
	 * @throws ExportationException
	 */
	public abstract Set<String> getAllCategoryHeaders() throws ExportationException;

	/**
	 * Get the category header name for a given category or return the default one
	 *
	 * @param categoryName
	 * @param defaultCategoryName
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract String getCategoryHeaderName(final String categoryName, final String defaultCategoryName) throws ExportationException;

	/**
	 * Get all childs path of a given parent path
	 *
	 * @param parentPath
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */

	public abstract List<String> listChidrenPath(final String parentPath) throws ExportationException;
	/**
	 * Return true if the path target is a space.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isSpace(final String path) throws ExportationException;

	/**
	 * Return true if the path target is a dossier.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isDossier(final String path) throws ExportationException;

	/**
	 * Return true if the path target is a document.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isDocument(final String path) throws ExportationException;

	/**
	 * Return the size of the file (bytes). A  negative value is returned if the information is not available.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public long getFileSize(final String path) throws ExportationException;

	/**
	 * Return true if the path target is a URL
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isUrl(final String path) throws ExportationException;


	/**
	 * Return true if the path target is a SharedSpaceLink
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isSharedSpaceLink(final String path) throws ExportationException;


	/**
	 * Return true if the path target is a LINK
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract boolean isLink(final String path) throws ExportationException;

	/**
	 * Get all translations of a document or url. Only the last versions will be returned
	 *
	 * @param path
	 * @return										The list of translations of the content
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<Locale> getContentTranslations(final String path) throws ExportationException;

	/**
	 * Get all versions of a given translation of a content.
	 *
	 * @param path
	 * @return										A map with the version number as key and the content path as value
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract Map<VersionNumber, String> getContentVersions(final String path, final Locale locale) throws ExportationException;


	/**
	 * Get all differents contents of a document or an url (All versions for all translations)
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<String> getContents(final String path) throws ExportationException;


	/**
	 * Return all paths associated to a dossier. If the implementation doesn't support Multilingual Dossiers,
	 * return in the list the given path.
	 *
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<String> getDossierTranslations(final String path) throws ExportationException;

	/**
	 * Return all paths associated to an url. If the implementation doesn't support Multilingual URLs,
	 * return in the list the given path.
	 *
	 * @return
	 * @throws ExportationException				If the path doesn't exists or if the conection is not active
	 */
	public abstract List<String> getUrlTranslations(final String path) throws ExportationException;


	/**
	 * Return if the path points to an existing file or folder.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException				If the conection is not active
	 */
	public abstract boolean exists(final String path) throws ExportationException;

}