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

import eu.cec.digit.circabc.migration.journal.MigrationTracer;

/**
 * Perform statistics before exportations
 *
 * @author Yanick Pignot
 */
public interface ExportStatisticsService
{
	/**
	 * Compute statistics on all interest group on all categories
	 *
	 * @param serviceImpl				The export service implementation to use
	 * @param uniqueId					The unique id of the statistics
	 * @param pairs						The interest group to read
	 * @return
	 * @throws ExportationException
	 */
	public abstract MigrationTracer<RootStat> getAllStatistics(final ExportService service, final String uniqueId) throws ExportationException;

	/**
	 * Compute statistics on a list of interest groups
	 *
	 * @param serviceImpl				The export service implementation to use
	 * @param uniqueId					The unique id of the statistics
	 * @param pairs						The list of interest groups to read
	 * @return
	 * @throws ExportationException
	 */
	public abstract MigrationTracer<RootStat> getStatistics(final ExportService service, final String uniqueId, final List<CategoryInterestGroupPair> pairs) throws ExportationException;

	/**
	 * Get the oot folder where are located all the generated statistic files.
	 *
	 * @return							The root folder
	 * @throws ExportationException
	 */
	public abstract NodeRef getExportFileLocation() throws ExportationException;

	/**
	 * Compute statistics on a list of interest groups
	 *
	 * @param serviceImpl				The export service implementation to use
	 * @param uniqueId					The unique id of the statistics
	 * @param pairs						The interest group to read
	 * @return
	 * @throws ExportationException
	 */
	public abstract MigrationTracer<RootStat> getStatistics(final ExportService service, final String uniqueId, final CategoryInterestGroupPair pair) throws ExportationException;

	/**
	 * Asynch compute statistics on all interest group on all categories
	 *
	 * @param serviceImpl				The export service implementation to use
	 * @param uniqueId					The unique id of the statistics
	 * @param pairs						The interest group to read
	 * @param beforeRunJob
	 * @param afterCommit
	 * @return
	 * @throws ExportationException
	 */
    public abstract void asynchGetAllStatistics(final ExportService exportService, final String uniqueId, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<RootStat> afterCommit);

	/**
	 * Asynch compute statistics on a list of interest groups
	 *
	 * @param serviceImpl				The export service implementation to use
	 * @param uniqueId					The unique id of the statistics
	 * @param pairs						The interest group to read
	 * @param beforeRunJob
	 * @param afterCommit
	 * @return
	 * @throws ExportationException
	 */
	public abstract void asynchGetStatistics(final ExportService exportService, final String uniqueId, final List<CategoryInterestGroupPair> pairs, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<RootStat> afterCommit);

	/**
	 * Return all statistic processes at this time <b>on this server</b>.
	 */
	public abstract Set<String> getRunningStatistics();

	/**
	 * Get the current status of a given statistic process name. Null if the process is not running.
	 */
	public abstract MigrationTracer<RootStat> getRunningStatisticJournal(final String processKey);

}