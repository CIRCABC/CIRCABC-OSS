/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;

/**
 * Service in charge to perform an importation
 *
 * @author Yanick Pignot
 */
public interface ImportService
{
	/**
	 * Store a new migration file in the repository
	 *
	 * @param resource						the import xml file
	 * @param resource						the logs xml file (can be null)
	 * @param shortLabel					an unique identifier
	 * @param description					an optional description
	 * @return								file noderef
	 * @throws ImportationException
	 */
	public abstract NodeRef storeNewImportFile(final InputStream streamApiFile, final InputStream streamLogsFile, final String shortLabel, final String description) throws ImportationException;


	/**
	 * Get the ETL - import history ready for an import porcess.
	 *
	 * @param sortAscending					sort the list ascending or not
	 * @return								The migration iterations.
	 */
	public abstract List<MigrationIteration> getIterations(final boolean sortAscending) throws ImportationException;

	/**
	 * Validate an importation process using a specific iteration
	 *
	 * @param iteration					the iteration to launch
	 * @return							the journal of the action
	 * @throws ImportationException		if an error occurs or if the iteration doesn't exists
	 */
	public abstract MigrationTracer<ImportRoot> validate(final String iterationName) throws ImportationException;

	/**
	 * Validate an importation process using an xml file
	 *
	 * @param resource					the import xml file
	 * @return							the journal of the action
	 * @throws ImportationException		if an error occurs
	 */
	public abstract MigrationTracer<ImportRoot> validate(final NodeRef resource) throws ImportationException;

	/**
	 * Validate an importation process using a specific iteration
	 *
	 * @param iteration					the iteration to launch
	 * @return							the journal of the action
	 * @throws ImportationException		if an error occurs or if the iteration doesn't exists
	 */
	public abstract MigrationTracer<ImportRoot> dryRun(final String iterationName) throws ImportationException;

	/**
	 * Validate an importation process using a specific iteration
	 *
	 * @param iteration					the iteration to launch
	 * @return							the journal of the action
	 * @throws ImportationException		if an error occurs or if the iteration doesn't exists
	 */
	public abstract MigrationTracer<ImportRoot> run(final String iterationName) throws ImportationException;

	/**
	 * Validate an importation process using a specific iteration in a sepate thread
	 *
	 * @param iteration					the iteration to launch
	 * @param afterCommit				what to do after
	 * @return							the journal of the action
	 */
	public abstract void asynchRun(final String iterationName, final AsynchJobListeners.BeforeRunJob beforeRun, final AsynchJobListeners.AfterRunJob<ImportRoot> afterCommit) throws ImportationException;

	/**
	 * Return all iterations that are running at this time <b>on this server</b>.
	 */
	public abstract Set<String> getRunningIterations();

	/**
	 * Get the current status of a given iteration. Null if the iteration is not running.
	 **/
	public abstract MigrationTracer<ImportRoot> getRunningIterationsJournal(final String iterationName);

}
