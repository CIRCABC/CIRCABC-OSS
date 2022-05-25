/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.util.List;
import java.util.Set;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.migration.reader.UserReader;

/**
 * Service in charge to perform any exportation
 *
 * @author Yanick Pignot
 */
public interface ExportService
{
	/**
	 * Return all category names found in root context
	 *
	 * @return
	 * @throws ExportationException
	 */
	public abstract List<String> getCategoryNames() throws ExportationException;

	/**
	 * Return all interest group names found for a given category
	 *
	 * @param category
	 * @return
	 * @throws ImportationException
	 */
	public abstract List<CategoryInterestGroupPair> getInterestGroups(final String category) throws ExportationException;

	/**
	 * Run the import process for a single Interest Group and build an ImportRoot element.
	 *
	 * @param pair
	 * @param iterationName
	 * @param iterationDescription
	 * @return
	 * @throws ExportationException			If the iterationName is already exists or another error occurs
	 */
	public abstract MigrationTracer<ImportRoot> runExport(final CategoryInterestGroupPair pair, final String iterationName, final String iterationDescription) throws ExportationException;

	/**
	 * Run the import process for multiples Interest Groups and build an ImportRoot element.
	 *
	 * @param pairs
	 * @param iterationName
	 * @param iterationDescription
	 * @return
	 * @throws ExportationException			If the iterationName is already exists or another error occurs
	 */
	public abstract MigrationTracer<ImportRoot> runExport(final List<CategoryInterestGroupPair> pairs, final String iterationName, final String iterationDescription) throws ExportationException;

	/**
	 * Run the import process for multiples Interest Groups and build an ImportRoot element.
	 *
	 * @param pairs
	 * @param iterationName
	 * @param iterationDescription
	 * @param afterCommit					What to do after
	 */
	public abstract void asynchRunExport(final List<CategoryInterestGroupPair> pairs, final String iterationName, final String iterationDescription, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<ImportRoot> afterCommit) throws ExportationException;

	/**
	 * Return all interest group that must be migrated before a given one.
	 *
	 * @param
	 * @return
	 * @throws ExportationException
	 */
	public abstract Set<CategoryInterestGroupPair> getInterestGroupDepedencies(final CategoryInterestGroupPair pairs) throws ExportationException;

	/**
	 * Return all iterations exportation that are being running at this time <b>on this server</b>.
	 */
	public abstract Set<String> getRunningIterations();

	/**
	 * Get the current status of a given exportation iteration. Null if the process is not running.
	 */
	public abstract MigrationTracer<ImportRoot> getRunningIterationsJournal(final String iterationName);

	/**
	 * @return the implementationName
	 */
	public abstract String getImplementationName();

	/**
	 * @return the calendarReader
	 */
	public abstract CalendarReader getCalendarReader();

	/**
	 * @return the infFileReader
	 */
	public abstract RemoteFileReader getInfFileReader();

	/**
	 * @return the libFileReader
	 */
	public abstract RemoteFileReader getLibFileReader();
	/**
	 * @return the metadataReader
	 */
	public abstract MetadataReader getMetadataReader();

	/**
	 * @return the newsgroupReader
	 */
	public abstract NewsgroupReader getNewsgroupReader();

	/**
	 * @return the securityReader
	 */
	public abstract SecurityReader getSecurityReader();

	/**
	 * @return the userReader
	 */
	public abstract UserReader getUserReader();
}
