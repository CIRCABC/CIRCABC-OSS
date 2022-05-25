/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration.jobs;

import java.util.Date;
import java.util.List;

import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ImportService;

/**
 * Service in charge to user exportation
 *
 * @author Yanick Pignot
 */
public interface PlannedMigrationService
{

	/**
	 * Register a bulk user export for the next run (conjunction should be as true only with Kleene closure).
	 *
	 * <pre>
	 * 		Equivalent to registerUserExportation(new Date(), uniqueId, emails, conjunction, notificationEmails)
	 * </pre>
	 *
	 * @param uniqueId					The unique id of the process
	 * @param emails					The mails to query
	 * @param conjunction				If query is cunjunction or not
	 * @param negation					If query is negation or not
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public UserExportStatus registerUserExportation(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register a bulk user export at a given date (conjunction should be as true only with Kleene closure).
	 *
	 * @param date						The fire date time
	 * @param uniqueId					The unique id of the process
	 * @param importService				The import service implementation to user to retreive users
	 * @param emails					The mails to query
	 * @param conjunction				If query is cunjunction or not
	 * @param negation					If query is negation or not
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public UserExportStatus registerUserExportation(final Date date, final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation statistic of the given interest groups for the next run .
	 *
	 * <pre>
	 * 		Equivalent to registerStatisticExportation(new Date(), uniqueId, pairs, notificationEmails)
	 * </pre>
	 *
	 * @param uniqueId					The unique id of the process
	 * @param pairs						The interest groups to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException	If the uniqueId is already used or if any error occurs.
	 */
	public StatisticExportStatus registerStatisticExportation(final String uniqueId, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation statistic of the given interest groups at a given date.
	 *
	 * @param date						The fire date time
	 * @param uniqueId					The unique id of the process
	 * @param pairs						The interest groups to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException	If the uniqueId is already used or if any error occurs.
	 */
	public StatisticExportStatus registerStatisticExportation(final Date date, final String uniqueId, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation statistic of <b>ALL</b> interest groups for the next run .
	 *
	 * <pre>
	 * 		Equivalent to registerStatisticExportation(new Date(), uniqueId, notificationEmails)
	 * </pre>
	 *
	 * @param uniqueId					The unique id of the process
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException	If the uniqueId is already used or if any error occurs.
	 */
	public StatisticExportStatus registerStatisticExportation(final String uniqueId, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation statistic of <b>ALL</b> interest groups at a given date.
	 *
	 * @param date						The fire date time
	 * @param uniqueId					The unique id of the process
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException	If the uniqueId is already used or if any error occurs.
	 */
	public StatisticExportStatus registerStatisticExportation(final Date date, final String uniqueId, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation for the next run .
	 *
	 * <pre>
	 * 		Equivalent to registerIterationExportation(new Date(), uniqueId, iterationName, iterationDescription, pairs, notificationEmails)
	 * </pre>
	 *
	 * @param uniqueId					The unique id of the process
	 * @param iterationName				The iteration name to create
	 * @param iterationDescription		Ihe optional iteration desc
	 * @param pairs						The interest groups to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public IterationExportStatus registerIterationExportation(final String uniqueId, final String iterationName, final String iterationDescription, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an exportation  at a given date (conjunction should be as true only with Kleene closure).
	 *
	 * @param date						The fire date time
	 * @param uniqueId					The unique id of the process
	 * @param iterationName				The iteration name to create
	 * @param iterationDescription		Ihe optional iteration desc
	 * @param pairs						The interest groups to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public IterationExportStatus registerIterationExportation(final Date date, final String uniqueId, final String iterationName, final String iterationDescription, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an importation for the next run .
	 *
	 * <pre>
	 * 		Equivalent to registerIterationImportation(new Date(), uniqueId, iterationName, notificationEmails)
	 * </pre>
	 *
	 * @param uniqueId					The unique id of the process
	 * @param iterationName				The iteration to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public IterationImportStatus registerIterationImportation(final String uniqueId, final String iterationName, final List<String> notificationEmails) throws PlannificationException;

	/**
	 * Register an importation  at a given date (conjunction should be as true only with Kleene closure).
	 *
	 * @param date						The fire date time
	 * @param uniqueId					The unique id of the process
	 * @param iterationName				The iteration to import
	 * @param notificationEmails		The emails to send the end report
	 * @return							The generated status
	 *
	 * @throws PlannificationException		If the uniqueId is already used or if any error occurs.
	 */
	public IterationImportStatus registerIterationImportation(final Date date, final String uniqueId, final String iterationName, final List<String> notificationEmails)  throws PlannificationException;


	/**
	 * Get all registred user exportations
	 *
	 * @return the user process reports
	 *
	 * @throws PlannificationException
	 */
	public List<UserExportStatus> getRegistredUserExportation()  throws PlannificationException;


	/**
	 * Get all registred exportations
	 *
	 * @return the exportation process reports
	 *
	 * @throws PlannificationException
	 */
	public List<IterationExportStatus> getRegistredIterationExportation()  throws PlannificationException;

	/**
	 * Get all registred importations
	 *
	 * @return the importation process reports
	 *
	 * @throws PlannificationException
	 */
	public List<IterationImportStatus> getRegistredIterationImportation()  throws PlannificationException;


	/**
	 * Get all registred Exportation Statistics
	 *
	 * @return the Exportation Statistics process reports
	 *
	 * @throws PlannificationException
	 */
	public List<StatisticExportStatus> getRegistredExportationStatistics()  throws PlannificationException;

	/**
	 * Fire an user export with a given exportService implementation
	 *
	 * @param importStatus
	 * @param aidaMigrationService
	 */
	public void fire(final UserExportStatus exportStatus, final AidaMigrationService aidaMigrationService);

	/**
	 * Fire an export with a given exportService implementation
	 *
	 * @param importStatus
	 * @param exportService
	 * @param aidaMigrationService
	 */
	public void fire(final IterationExportStatus exportStatus, final ExportService exportService, final AidaMigrationService aidaMigrationService);

	/**
	 * Fire an import with a given importService implementation
	 *
	 * @param importStatus
	 * @param importService
	 */
	public void fire(final IterationImportStatus importStatus, final ImportService importService);


	/**
	 * Fire an export statistic with a given exportService implementation
	 *
	 * @param importStatus
	 * @param importService
	 */
	public void fire(final StatisticExportStatus importStatus, final ExportService exportService);
}
