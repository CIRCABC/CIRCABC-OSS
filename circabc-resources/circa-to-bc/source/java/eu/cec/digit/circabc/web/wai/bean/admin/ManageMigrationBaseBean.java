/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportStatisticsService;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.jobs.MigrationStatusBase;
import eu.cec.digit.circabc.service.migration.jobs.PlannedMigrationService;
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.service.namespace.CircabcNameSpaceService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

/**
 * base bean that manage migration jobs
 *
 * @author Pignot Yanick
 */
public abstract class ManageMigrationBaseBean extends BaseWaiDialog
{
	private static final Log logger = LogFactory.getLog(ManageMigrationBaseBean.class);

	/*package*/ static final QName PLANNIFICATION_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "PlannedMigrationService");
	/*package*/ static final QName EXPORTATION_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ExportService");
    /*package*/ static final QName IMPORT_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ImportService");
    /*package*/ static final QName IMPORT_ETL_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "MigrationEtlService");
    /*package*/ static final QName IMPORT_AIDA_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "AidaService");
    /*package*/ static final QName IMPORT_STAT_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ExportStatisticsService");

    protected final String MSG_ERROR_FIREDATE = "manage_migration_jobs_dialog_error_date";
    protected final String MSG_ERROR_JOBNAME = "manage_migration_jobs_dialog_error_name";

	private transient ImportService importService;

	private String name;
	private Date fireDate;
	// make it static to be shared cross dialogs...
	private static String notificationEmails;

	public final void reset(final ActionEvent event)
    {
    	this.init(null);
    }

	protected void resetState()
    {
    	fireDate = new Date();
    	name = null;
    }

	@Override
	protected String finishImpl(final FacesContext context, final String outcome) throws Throwable
	{
		// nothing to do
		return outcome;
	}

	@Override
	public final void init(final Map<String, String> parameters)
	{
		super.init(parameters);
		resetState();

		if(!hasRight())
		{
			logger.warn("Security violation!!! User (" + ((CircabcNavigationBean)getNavigator()).getCurrentUserName() + ") tries to access to a migration page: " + this.getClass().getSimpleName() + " !!");

			throw new IllegalStateException("You have not enough rights to perform this operation. ");
		}
	}

	public final List<? extends MigrationStatusBase> getRegistredJobs()
	{
		final RetryingTransactionHelper helper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
		return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<? extends MigrationStatusBase>>()
		{
		    public List<? extends MigrationStatusBase> execute() throws Throwable
			{
		    	try
				{
					final List<? extends MigrationStatusBase> registredImportation = getJobs();

					if(logger.isDebugEnabled())
					{
						logger.debug(registredImportation.size() + " registred exportation found.");
					}

					return registredImportation;
				}
				catch (PlannificationException e)
				{
					Utils.addErrorMessage("Problem accessing to job definitions: " + e.getMessage(), e);

					if(logger.isErrorEnabled())
					{
						logger.error("Problem accessing to job definitions: " + e.getMessage(), e);
					}

					return Collections.<MigrationStatusBase>emptyList();
				}
			}
		});
	}

	protected abstract List<? extends MigrationStatusBase> getJobs() throws PlannificationException;

	public final void addJob(final ActionEvent event)
	{
		 final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
		 final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
         {
            public String execute() throws Throwable
            {
               addJobImpl();
               return null;
            }
         };

         txnHelper.doInTransaction(callback);
	}

	public abstract void addJobImpl();

	public boolean isExportAvailable()
	{
		return getExportService() != null && getNavigator().getCurrentUser().isAdmin();
	}

	public String getBrowserTitle()
	{
		return "";
	}

	public String getPageIconAltText()
	{
		return "";
	}

	public boolean hasRight()
	{
		final User user = getNavigator().getCurrentUser();
		return user.isAdmin();
	}

    public void exit(ActionEvent event)
    {
    	init(null);
    }

	/**
	 * @return the fireDate
	 */
	public final Date getFireDate()
	{
		return fireDate;
	}

	/**
	 * @param fireDate the fireDate to set
	 */
	public final void setFireDate(Date fireDate)
	{
		this.fireDate = fireDate;
	}

	/**
	 * @return the notificationEmails
	 */
	public final String getNotificationEmails()
	{
		return ManageMigrationBaseBean.notificationEmails;
	}

	protected final List<String> getNotificationEmailList()
	{
		return extractEmailList(getNotificationEmails());
	}

	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param notificationEmailsString
	 * @return
	 */
	protected List<String> extractEmailList(String notificationEmailsString)
	{
		if(notificationEmailsString == null || notificationEmailsString.trim().length() < 1)
		{
			return Collections.<String>emptyList();
		}
		else
		{
			notificationEmailsString = notificationEmailsString.trim().replace("\n", ",").replace(";", ",");
			final StringTokenizer tokens = new StringTokenizer(notificationEmailsString, ",", false);
			final List<String> emails = new ArrayList<String>(tokens.countTokens());

			while(tokens.hasMoreTokens())
			{
				emails.add(tokens.nextToken().trim());
			}

			return emails;
		}
	}

	/**
	 * @param notificationEmails the notificationEmails to set
	 */
	public final void setNotificationEmails(String notificationEmails)
	{
		ManageMigrationBaseBean.notificationEmails = notificationEmails;
	}

	 /**
     * @return the importService
     */
	public final ImportService getImportService()
    {
        if(importService == null)
        {
            importService = (ImportService) Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getService(IMPORT_SERVICE);
        }
        return importService;
    }


    /**
     * @param importService the importService to set
     */
    public final void setImportService(ImportService importService)
    {
        this.importService = importService;
    }

    /**
	 * @return the PlannedMigrationService
	 */
    protected final PlannedMigrationService getPlannedMigrationService()
	{
    	final CircabcServiceRegistry registry = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance());
    	if(registry.isServiceProvided(PLANNIFICATION_SERVICE))
		{
			return (PlannedMigrationService) registry.getService(PLANNIFICATION_SERVICE);
		}
		else
		{
			return null;
		}
	}

	 /**
	 * @return the aidaMigrationService
	 */
	protected final AidaMigrationService getAidaMigrationService()
	{
		final CircabcServiceRegistry registry = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance());
		if(registry.isServiceProvided(IMPORT_AIDA_SERVICE))
		{
			return (AidaMigrationService) registry.getService(IMPORT_AIDA_SERVICE);
		}
		else
		{
			return null;
		}
	}

	protected final ExportService getExportService()
	{
		final CircabcServiceRegistry registry = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance());
		if(registry.isServiceProvided(EXPORTATION_SERVICE))
		{
			return (ExportService) registry.getService(EXPORTATION_SERVICE);
		}
		else
		{
			return null;
		}
	}

	protected final ExportStatisticsService getExportStatisticsService()
	{
		final CircabcServiceRegistry registry = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance());
		if(registry.isServiceProvided(IMPORT_STAT_SERVICE))
		{
			return (ExportStatisticsService) registry.getService(IMPORT_STAT_SERVICE);
		}
		else
		{
			return null;
		}
	}
}