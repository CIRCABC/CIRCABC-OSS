/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.archive;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;

/**
 * Archive the the xml files applied
 *
 * @author Yanick Pignot
 */
public interface FileArchiver
{

	/**
	 * Reserve the name of an iteration.
	 *
	 * @param shortLabel						An unique label
	 * @param description						An optional descrition
	 * @return
	 * @throws ArchiveException
	 */
	public abstract MigrationIteration startNewIteration(final String shortLabel, final String description) throws ArchiveException, DuplicateIterationNameException;

	/**
	 * Method that should be used if after startNewIteration. It allows to delete the iteration if no content or space is created under.
	 *
	 * @param iteration						The MigrationIteration
	 * @return								True if the iteration has been deleted
	 */
	public abstract Boolean removeIterationIfEmpty(final MigrationIteration iteration);

	/**
	 * Store the original file of a new Importation process iteration.
	 *
	 * @param resource							The xml file
	 * @param iteration							the iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeOriginalExportFile(final MigrationIteration iteration, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store the original interest group(s) log entries of a new Importation process iteration.
	 *
	 * @param resource							The xml file
	 * @param properties						properties
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeLogsExportFile(final MigrationIteration iteration, final Properties properties) throws ArchiveException;

	/**
	 * Store the original file of a new Importation process iteration.
	 *
	 * @param resource							The xml file
	 * @param iteration							the iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeExportFile(final MigrationIteration iteration, final String fileName, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store a transformed <b>valid</b> xml result
	 *
	 * @param resource						The xml file
	 * @param iteration						The related iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeTransformedValidFile(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store a transformed <b>invalid</b> xml result
	 *
	 * @param resource						The xml file
	 * @param iteration						The related iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeTransformedResidualFile(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store the xml that will be used for migration (after validation and preprocessing)
	 *
	 * @param iteration
	 * @param importProcessId
	 * @param inputStream
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeImportedTranformedXML(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store the applied xml after all processes
	 *
	 * @param iteration
	 * @param importProcessId
	 * @param inputStream
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeImportedAppliedXML(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException;

	/**
	 * Store the result of the migration
	 *
	 * @param iteration
	 * @param importProcessId
	 * @param inputStream
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef storeImportationLogResult(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException;

	/**
	 * Flag an importation process being in error
	 *
	 * @param iteration
	 * @param importProcessId
	 * @param error
	 * @throws ArchiveException
	 */
	public abstract void flagImportationAsFailed(final MigrationIteration iteration, final Date importProcessId, final Throwable error) throws ArchiveException;

	/**
	 * Get the root space where will be stored the import and export files
	 *
	 * @return
	 */
	public abstract NodeRef getMigrationRootSpace() throws ArchiveException;

	/**
	 * Get all iterations
	 *
	 * @return
	 */
	public abstract List<MigrationIteration> getIterations() throws ArchiveException;

	/**
	 * Return the iteration of a given noderef
	 *
	 * @param nodeRef
	 * @return						iteration or null
	 */
	public abstract MigrationIteration getNodeIteration(final NodeRef nodeRef) throws ArchiveException;

	/**
	 * get an iteration with a specific name
	 *
	 * @param iterationName
	 * @param existingIterations
	 * @return
	 */
	public abstract MigrationIteration getIterationByName(final String iterationName)throws ArchiveException;

	/**
	 * return if the next process of the iteration can be an importation
	 * <pre>
	 * An import is possible only
	 *	 a.	  if at least 1 transformation was performed
	 *	 b.   and that transformations list size - import list size = 1
	 * </pre>
	 * @param iteration
	 * @return
	 */
	public abstract boolean isIterationReadyForMigration(final MigrationIteration iteration) throws ArchiveException;

	/**
	 * return if the next process of the iteration can be a transformation
	 * <pre>
	 * A transformation is possible only
	 *	a.	if at least 1 file was upload
	 *	b.   and that transformations list size - import list size = 0
	 * </pre>
	 *
	 * @param iteration
	 * @return
	 */
	public abstract boolean isIterationReadyForTransformation(final MigrationIteration iteration) throws ArchiveException;

	/**
	 * Get the xml noderef that is now ready for an import
	 * <pre>
	 * It means the last <b>valid</b> transformed file of the given iteration
	 * </pre>
	 *
	 * @param iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef getImportResource(final MigrationIteration iteration) throws ArchiveException;

	/**
	 * Get the xml noderef that is now ready for an tranbsformation
	 * <pre>
	 * It means the last <b>invalid</b> transformed file of the given iteration or its original file for the first transformation
	 * </pre>
	 *
	 * @param iteration
	 * @return
	 * @throws ArchiveException
	 */
	public abstract NodeRef getTransformationResource(final MigrationIteration iteration) throws ArchiveException;

	/**
	 * Get the list of valid users during persisted during pervious ETL
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Map<Object, Object> retreiveAlreadyTransformedUser() throws ArchiveException;

	/**
	 * Load a proerties object fro a content
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Properties retreiveProperties(final NodeRef nodeRef) throws ArchiveException;

	/**
	 * Get the list of planned export statistics
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Map<Object, Object> retreivePlannedStatisticExports() throws ArchiveException;

	/**
	 * Get the list of planned iteration export
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Map<Object, Object> retreivePlannedIterationExport() throws ArchiveException;

	/**
	 * Get the list of planned iteration import
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Map<Object, Object> retreivePlannedIterationImport() throws ArchiveException;


	/**
	 * Compute a
	 *
	 * @return
	 * @throws ArchiveException
	 */
	public abstract Map<Object, Object> retreivePlannedUserExport() throws ArchiveException;

	/**
	 * Add a planned user export
	 *
	 * @param planificationId
	 * @param data
	 * @throws ArchiveException
	 */
	public abstract void addPlannedUserExport(String planificationId, Object data) throws ArchiveException;

	/**
	 * Add a planned export
	 *
	 * @param planificationId
	 * @param data
	 * @throws ArchiveException
	 */
	public abstract void addPlannedIterationExport(String planificationId, Object data) throws ArchiveException;

	/**
	 * Add a planned import
	 *
	 * @param planificationId
	 * @param data
	 * @throws ArchiveException
	 */
	public abstract void addPlannedIterationImport(String planificationId, Object data) throws ArchiveException;

	/**
	 * Add a planned exportation statistics
	 *
	 * @param planificationId
	 * @param data
	 * @throws ArchiveException
	 */
	public abstract void addPlannedExportStatistics(String planificationId, Object data) throws ArchiveException;

	/**
	 * Add theses users to the persisted ones
	 *
	 * @param newValidUsers
	 * @throws ArchiveException
	 */
	public abstract void storeAlreadyTransformedUser(final Map<Object, Object> newValidUsers) throws ArchiveException;

	/**
	 * Register a node to create at the startup of the migration.
	 *
	 * @param type				The type of the node
	 * @param name				The name of the node
	 */
	public abstract void registerMandatoryNode(final QName type, final String name);

	/**
	 * get the input stream from a given node
	 *
	 * @param resource
	 * @return
	 */
	public abstract InputStream getContentInputStream(final NodeRef resource) ;

	
	/**
	 * Store the result of the migration
	 * 
	 * @param iteration
	 * @param importProcessId
	 * @param document
	 * @return
	 * @throws ArchiveException 
	 */
	public abstract NodeRef storeImportationLogResultDocument(final MigrationIteration iteration, final Date importProcessId, Document document) throws ArchiveException;

	public abstract OutputStream getContentOutputStream(final NodeRef resource);
	
	public abstract ContentWriter getContentWriter(final NodeRef resource);
}