/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;

/**
 * Service in charge to perform the migration for aida users
 *
 * @author Yanick Pignot
 */
public interface AidaMigrationService
{

	/**
	 * Export persons defined in the import root in a list of Aida users.
	 *
	 * @param imporRoot
	 * @return
	 */
	public abstract MigrationTracer<Users> exportPersons(final ImportRoot imporRoot, final MigrationIteration iteration) throws AidaException;

	/**
	 * Run an user export
	 *
	 * @param uniqueId
	 * @param emails					The emails (wildcard or not)
	 * @param conjunction				AND or OR
	 * @param negation					NOT or default
	 * @return the queried persons
	 * @throws ExportationException
	 */
	public abstract MigrationTracer<Users> exportPersons(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation) throws AidaException;


	/**
	 * Run an asynchrone user export
	 *
	 * @param uniqueId
	 * @param emails					The emails (wildcard or not)
	 * @param conjunction				AND or OR
	 * @param negation					NOT or default
	 * @return the queried persons
	 * @throws ExportationException
	 */
	public abstract void asynchExportPersons(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final AsynchJobListeners.BeforeRunJob beforeRun, final AsynchJobListeners.AfterRunJob<Users> afterCommit);

	/**
	 * Get the oot folder where are located all the generated aida files.
	 *
	 * @return							The root folder
	 * @throws AidaException
	 */
	public abstract NodeRef getExportFileLocation() throws AidaException;

	/**
	 * Return all user exportations that are running at this time <b>on this server</b>.
	 */
	public abstract Set<String> getRunningExportations();

	/**
	 * Get the current status of a given exportation id. Null if the iteration is not running.
	 **/
	public abstract MigrationTracer<Users> getRunningExportJournal(final String uniqueId);

	/**
	 * Register a user user converter
	 *
	 * @param userConverter
	 */
	public abstract void registerConverter(final UserConverter userConverter);


	/**
	 * Register a user voter
	 *
	 * @param userVoter
	 */
	public abstract void registerVoter(final UserVoter userVoter);


	/**
	 * Base interface for any user voter. At the end of all convertion, the voters will be called.
	 * If one returns false, the user will be not added.
	 *
	 * @author Yanick Pignot
	 */
	public interface UserVoter
	{
		public abstract boolean addUser(User user) throws AidaException;

		public abstract String getRejectMessage();
	}

	/**
	 * Base interface for any user convertion
	 *
	 * @author Yanick Pignot
	 */
	public interface UserConverter
	{
		public abstract void convert(User user) throws AidaException;
	}
}
