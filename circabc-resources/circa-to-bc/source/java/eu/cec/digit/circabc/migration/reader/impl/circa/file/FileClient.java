/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.file;

import java.io.OutputStream;
import java.util.List;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A client for reading circa the file system
 *
 * @author Yanick Pignot
 */
public interface FileClient
{
	public static final String PATH_SEPARATOR = "/";

	/**
	 * From a file path, return it path as a Ressource.
	 *
	 * @param basePath
	 * @return
	 */
	public abstract String generateResouceString(final String basePath);

	/**
	 * Return true if the file or folder exists
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean exists(final String path) throws ExportationException;

	/**
	 * Download the content of a file
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public abstract void download(final String path, final OutputStream outStream) throws ExportationException;

	/**
	 * Download the content of a file and return it in a String
	 *
	 * @param path
	 * @param endOfLine
	 * @return
	 * @throws ExportationException
	 */
	public abstract String downloadAsString(final String path) throws ExportationException;

	/**
	 * List the children of a given folder
	 *
	 * @param path
	 * @param includeHidden
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> list(final String path, final boolean includeHidden) throws ExportationException;

	/**
	 * List the children of a given folder
	 *
	 * @param path
	 * @param includeHidden
	 * @param includeFiles
	 * @param includeFolders
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> list(final String path, final boolean includeHidden, final boolean includeFiles, final boolean includeFolders) throws ExportationException;

	/**
	 * Return true if the target is a file
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean isFile(final String path) throws ExportationException;

	/**
	 * Return the size of the file (bytes). A  negative value is returned if the information is not available.
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public long getFileSize(final String path) throws ExportationException;

	/**
	 * Return the root directory
	 *
	 * @return
	 */
	public abstract String getDataRoot();

	/**
	 * Return the location of the information service location on file system
	 *
	 * @return
	 */
	public abstract String getInformationDataLocation();

	/**
	 * Return the location of the library service location on file system
	 *
	 * @return
	 */
	public abstract String getLibraryDataLocation();

	/**
	 * Return the location of the meeting service location on file system
	 *
	 * @return
	 */
	public abstract String getMeetingsDataLocation();

	/**
	 * Return the location of the newsgroup service location on file system
	 *
	 * @return
	 */
	public abstract String getNewsDataLocation();

	/**
	 * Return the location of the directory service location on file system
	 *
	 * @return
	 */
	public abstract String getDirectoryDataLocation();

	/**
	 * Return true if the taget is a symbolic link
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean isSymbolicLink(final String path) throws ExportationException;

	/**
	 * Return true if the implementation manage files on the same file system that the application
	 *
	 * @return
	 * @throws ExportationException
	 */
	public abstract boolean isSameFileSystem() throws ExportationException;

	/**
	 * Return the filesystem default encoding
	 *
	 * @return
	 */
	public abstract String getSystemEncoding();

	/**
	 * return the icon root location
	 *
	 * @return
	 */
	public abstract String getIconRoot();

}
