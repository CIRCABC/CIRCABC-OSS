/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;


import java.util.List;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.journal.etl.ETLReport;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUser;


/**
 * Service in charge of ETL (Extract Transform Load) of migration objects
 *
 * @author Yanick Pignot
 */
public interface ETLService
{

	/**
	 * Purpose an transformation of object
	 *
	 * @param iterationName				The iteration name
	 * @throws ETLException
	 */
	public abstract ETLReport proposeEtl(final String iterationName) throws ETLException;


	/**
	 * Purpose new users with specified uid and/or moniker and/or email
	 *
	 * @param pathologicUser
	 * @param uid
	 * @param moniker
	 * @param email
	 * @return
	 * @throws ETLException
	 */
	public abstract void proposeUsers(final PathologicUser pathologicUser, final String uid, final String moniker, final String email, final String cn) throws ETLException;

	/**
	 * Apply automatic transformation of object
	 *
	 * @param iterationName				The report to apply
	 * @throws ETLException
	 */
	public abstract void applyEtl(final ETLReport report) throws ETLException;


	/**
	 * Get the ETL - import history ready for a transformation process.
	 *
	 * @param sortAscending					sort the list ascending or not
	 * @return								The migration iterations.
	 */
	public abstract List<MigrationIteration> getIterations(final boolean sortAscending) throws ETLException;


}
