/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.service.migration.jobs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.Assert;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportStatisticsService;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.jobs.MigrationStatusBase.FireStatus;

/**
 * Job that check if new users are to be imported into circabc
 *
 * @author Yanick Pignot
 */
public class MigrationJobListener implements Job
{
	private static final long DEFAULT_PAUSE_TIME = 500l;

	private static Log logger = LogFactory.getLog(MigrationJobListener.class);

	private static final String ADMIN_USER = "admin";
	private static final String LOCK_ID = MigrationJobListener.class.getSimpleName();

	private CircabcServiceRegistry serviceRegistry;

	private PlannedMigrationService plannedMigrationService;
	private LockService lockService;
	private ExportService exportService;
	private ImportService importService;
	private AidaMigrationService aidaMigrationService;
	private ExportStatisticsService exportStatisticsService;
	private Boolean exportAvailable = null;
	private Boolean aidaExportAvailable = null;
	private Long betweenRunSleepTime = null;
	private boolean shouldMigrationRun ; 

	public void execute(final JobExecutionContext context) throws JobExecutionException
	{
		setShouldMigrationRun();
		
		if (! shouldMigrationRun)
		{
			return;
		}
		
		if(getLockService(context).isLocked(LOCK_ID))
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Another " + LOCK_ID + " job is still running .... waiting for the next fire !");
			}
		}
		else
		{
			try
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start job .... ");
				}

				AuthenticationUtil.setRunAsUser(ADMIN_USER);

				getLockService(context).lock(LOCK_ID);

				try
				{
					if(isAidaExportAvailable(context).booleanValue())
					{
						if(logger.isDebugEnabled())
						{
							logger.debug("Start the User Export.");
						}
						fireUserExport(context);
					}
				}
				catch (final Throwable e)
				{
					logger.error("Can not run job MigrationJobListener", e);
				}
				try
				{
					if(isExportAvailable(context).booleanValue())
					{
						if(logger.isDebugEnabled())
						{
							logger.debug("Start the Iteration Export.");
						}
						fireIterationExport(context);

						if(logger.isDebugEnabled())
						{
							logger.debug("Start the Exportation Statistics.");
						}
						fireExportStatistics(context);
					}
				}
				catch (final Throwable e)
				{
					logger.error("Can not run job MigrationJobListener", e);
				}
				try
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Start the Iteration Import.");
					}
					fireIterationImport(context);
				}
				catch (final Throwable e)
				{
					logger.error("Can not run job MigrationJobListener", e);
				}
			}
			finally
			{
				getLockService(context).unlock(LOCK_ID);
				AuthenticationUtil.clearCurrentSecurityContext();
			}

		}
	}


	/**
	 * @param context
	 * @throws PlannificationException
	 */
	private synchronized void fireUserExport(final JobExecutionContext context) throws PlannificationException
	{
		final List<UserExportStatus> statuses = getPlannedMigrationService(context).getRegistredUserExportation();
		final List<UserExportStatus> toRunStatuses = new ArrayList<UserExportStatus>();

		final Date fireDate = new Date();

		for(final UserExportStatus status:  statuses)
		{
			final FireStatus fireStatus = status.getStatus();
			final Date expectedDate = status.getExpectedFire();

			if(fireDate.after(expectedDate) && FireStatus.WAITING.equals(fireStatus))
			{
				toRunStatuses.add(status);
			}
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(toRunStatuses.size() + " on " + statuses.size() + " are ready to be processed.");
		}

		if(toRunStatuses.size() == 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(" .... process aborted. ");
			}
		}
		else
		{
			for(final UserExportStatus status: toRunStatuses)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to  fire " + status.getIdentifier());
				}

				getPlannedMigrationService(context).fire(status, getAidaMigrationService(context));

				if(FireStatus.SUCCESS.equals(status.getStatus()))
				{
					logger.debug(status.getIdentifier() + " successfully terminated.");
				}
				else if(FireStatus.ERROR.equals(status.getStatus()))
				{
					logger.error(status.getIdentifier() + " failed: " + status.getErrorMessage());
				}
				else
				{
					logger.error(status.getIdentifier() + " is in an invalid state: " + status.getStatus() + ". Please to correct them !");
				}

				sleep(context);
			}
		}
	}


	/**
	 * @param context
	 * @throws PlannificationException
	 */
	private synchronized void fireIterationExport(final JobExecutionContext context) throws PlannificationException
	{
		final List<IterationExportStatus> statuses = getPlannedMigrationService(context).getRegistredIterationExportation();
		final List<IterationExportStatus> toRunStatuses = new ArrayList<IterationExportStatus>();

		final Date fireDate = new Date();

		for(final IterationExportStatus status:  statuses)
		{
			final FireStatus fireStatus = status.getStatus();
			final Date expectedDate = status.getExpectedFire();

			if(fireDate.after(expectedDate) && FireStatus.WAITING.equals(fireStatus))
			{
				toRunStatuses.add(status);
			}
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(toRunStatuses.size() + " on " + statuses.size() + " are ready to be processed.");
		}

		if(toRunStatuses.size() == 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(" .... process aborted. ");
			}
		}
		else
		{
			for(final IterationExportStatus status: toRunStatuses)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to  fire IterationExportStatus:  " + status.getIdentifier());
				}

				getPlannedMigrationService(context).fire(status, getExportService(context), getAidaMigrationService(context));

				if(FireStatus.SUCCESS.equals(status.getStatus()))
				{
					logger.debug(status.getIdentifier() + " successfully terminated.");
				}
				else if(FireStatus.ERROR.equals(status.getStatus()))
				{
					logger.error(status.getIdentifier() + " failed: " + status.getErrorMessage());
				}
				else
				{
					logger.error(status.getIdentifier() + " is in an invalid state: " + status.getStatus() + ". Please to correct them !");
				}

				sleep(context);
			}
		}
	}

	/**
	 * @param context
	 * @throws PlannificationException
	 */
	private synchronized void fireExportStatistics(final JobExecutionContext context) throws PlannificationException
	{
		final List<StatisticExportStatus> statuses = getPlannedMigrationService(context).getRegistredExportationStatistics();
		final List<StatisticExportStatus> toRunStatuses = new ArrayList<StatisticExportStatus>();

		final Date fireDate = new Date();

		for(final StatisticExportStatus status:  statuses)
		{
			final FireStatus fireStatus = status.getStatus();
			final Date expectedDate = status.getExpectedFire();

			if(fireDate.after(expectedDate) && FireStatus.WAITING.equals(fireStatus))
			{
				toRunStatuses.add(status);
			}
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(toRunStatuses.size() + " on " + statuses.size() + " are ready to be processed.");
		}

		if(toRunStatuses.size() == 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(" .... process aborted. ");
			}
		}
		else
		{
			for(final StatisticExportStatus status: toRunStatuses)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to  fire StatisticExportStatus:  " + status.getIdentifier());
				}

				getPlannedMigrationService(context).fire(status, getExportService(context));

				if(FireStatus.SUCCESS.equals(status.getStatus()))
				{
					logger.debug(status.getIdentifier() + " successfully terminated.");
				}
				else if(FireStatus.ERROR.equals(status.getStatus()))
				{
					logger.error(status.getIdentifier() + " failed: " + status.getErrorMessage());
				}
				else
				{
					logger.error(status.getIdentifier() + " is in an invalid state: " + status.getStatus() + ". Please to correct them !");
				}

				sleep(context);
			}
		}
	}

	/**
	 * @param context
	 * @throws PlannificationException
	 */
	private synchronized void fireIterationImport(final JobExecutionContext context) throws PlannificationException
	{
		final List<IterationImportStatus> statuses = getPlannedMigrationService(context).getRegistredIterationImportation();
		final List<IterationImportStatus> toRunStatuses = new ArrayList<IterationImportStatus>();

		final Date fireDate = new Date();

		for(final IterationImportStatus status:  statuses)
		{
			final FireStatus fireStatus = status.getStatus();
			final Date expectedDate = status.getExpectedFire();

			if(fireDate.after(expectedDate) && FireStatus.WAITING.equals(fireStatus))
			{
				toRunStatuses.add(status);
			}
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(toRunStatuses.size() + " on " + statuses.size() + " are ready to be processed.");
		}

		if(toRunStatuses.size() == 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(" .... process aborted. ");
			}
		}
		else
		{
			for(final IterationImportStatus status: toRunStatuses)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to  fire " + status.getIdentifier());
				}

				getPlannedMigrationService(context).fire(status, getImportService(context));

				if(FireStatus.SUCCESS.equals(status.getStatus()))
				{
					logger.debug(status.getIdentifier() + " successfully terminated.");
				}
				else if(FireStatus.ERROR.equals(status.getStatus()))
				{
					logger.error(status.getIdentifier() + " failed: " + status.getErrorMessage());
				}
				else
				{
					logger.error(status.getIdentifier() + " is in an invalid state: " + status.getStatus() + ". Please to correct them !");
				}

				sleep(context);
			}
		}
	}


	/**
	 * @param context
	 */
	private void sleep(final JobExecutionContext context)
	{
		try
		{
			// wait before next run
			Thread.sleep(getBetweenRunSleepTime(context));
		} catch (InterruptedException ignore){}
	}

	private synchronized void initialize(JobExecutionContext context)
	{
		try
		{
			AuthenticationUtil.setRunAsUser(ADMIN_USER);
			JobDataMap jobData = context.getJobDetail().getJobDataMap();

			plannedMigrationService = (PlannedMigrationService) jobData.get("plannedMigrationService");
			lockService = (LockService) jobData.get("lockService");
			serviceRegistry = (CircabcServiceRegistry) jobData.get("serviceRegistry");
			importService = (ImportService)jobData.get("importService");
			exportStatisticsService = (ExportStatisticsService)jobData.get("exportStatisticsService");

			Assert.notNull(plannedMigrationService);
			Assert.notNull(lockService);
			Assert.notNull(serviceRegistry);
			Assert.notNull(importService);
			Assert.notNull(exportStatisticsService);

			String exportServiceName = (String) jobData.get("exportServiceName");
			String aidaMigrationServiceName = (String) jobData.get("aidaMigrationServiceName");
			String betweenRunSleepTimeStr = (String) jobData.get("betweenRunSleepTime");
			if(exportServiceName != null && exportServiceName.length() > 0)
			{
				final QName qname = QName.createQName(exportServiceName);
				if(serviceRegistry.isServiceProvided(qname))
				{
					exportService = (ExportService) serviceRegistry.getService(qname);
				}
			}

			if(aidaMigrationServiceName != null && aidaMigrationServiceName.length() > 0)
			{
				final QName qname = QName.createQName(aidaMigrationServiceName);
				if(serviceRegistry.isServiceProvided(qname))
				{
					aidaMigrationService = (AidaMigrationService) serviceRegistry.getService(qname);
				}
			}

			if(betweenRunSleepTimeStr != null && betweenRunSleepTimeStr.length() > 0)
			{
				try
				{
					betweenRunSleepTime = Long.parseLong(betweenRunSleepTimeStr);
				}
				catch(final NumberFormatException e)
				{
					betweenRunSleepTime = DEFAULT_PAUSE_TIME;
					logger.warn(betweenRunSleepTimeStr + " not a valid long value");
				}
			}

			exportAvailable = exportService != null;
			aidaExportAvailable = aidaMigrationService != null;

		}
		finally
		{
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}


	/**
	 * This method compare migration_host that is configured in circabc-setings.properties  
	 * with host name of server if they are different migration is not run 
	 * in case of error getting local host name or property is not defined job is executing  
	 */
	private void setShouldMigrationRun()
	{
		final String migrationHostName = CircabcConfiguration.getProperty(CircabcConfiguration.MIGRATION_HOST);
		if (migrationHostName == null)
		{
			shouldMigrationRun =true;
			return;
		}
		String hostName =null ;
		try
		{
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e)
		{
			shouldMigrationRun =true;
			return;
		}
		if (hostName.equalsIgnoreCase(migrationHostName))
		{
			shouldMigrationRun =true;
		}
		else
		{
			shouldMigrationRun =false;
		}
		
	}


	/**
	 * @return the plannedMigrationService
	 */
	private final PlannedMigrationService getPlannedMigrationService(final JobExecutionContext context)
	{
		if(plannedMigrationService == null)
		{
			initialize(context);
		}

		return plannedMigrationService;
	}


	/**
	 * @return the lockService
	 */
	private final LockService getLockService(final JobExecutionContext context)
	{
		if(lockService == null)
		{
			initialize(context);
		}

		return lockService;
	}


	/**
	 * @return the exportService
	 */
	private final ExportService getExportService(final JobExecutionContext context)
	{
		if(exportService == null)
		{
			initialize(context);
		}
		return exportService;
	}

	/**
	 * @return the exportService
	 */
	private final ImportService getImportService(final JobExecutionContext context)
	{
		if(importService == null)
		{
			initialize(context);
		}
		return importService;
	}

	/**
	 * @return the exportService
	 */
	private final AidaMigrationService getAidaMigrationService(final JobExecutionContext context)
	{
		if(aidaMigrationService == null)
		{
			initialize(context);
		}
		return aidaMigrationService;
	}

	private final Boolean isExportAvailable(final JobExecutionContext context)
	{
		if(exportAvailable == null)
		{
			initialize(context);
		}
		return exportAvailable;
	}

	private final Boolean isAidaExportAvailable(final JobExecutionContext context)
	{
		if(aidaExportAvailable == null)
		{
			initialize(context);
		}
		return aidaExportAvailable;
	}


	private final Long getBetweenRunSleepTime(final JobExecutionContext context)
	{
		if(betweenRunSleepTime == null)
		{
			initialize(context);
		}
		return betweenRunSleepTime;
	}


	/**
	 * @return the exportStatisticsService
	 */
	public final ExportStatisticsService getExportStatisticsService(final JobExecutionContext context)
	{
		if(exportStatisticsService == null)
		{
			initialize(context);
		}
		return exportStatisticsService;
	}
}
