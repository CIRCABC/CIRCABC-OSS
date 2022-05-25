/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.List;

import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.service.migration.jobs.UserExportStatus;
import eu.cec.digit.circabc.web.WebClientHelper;

/**
 * Bean that manage bulk user exportation.
 *
 * @author Pignot Yanick
 */
public class ManageBulkUserExport extends ManageMigrationBaseBean
{
	private static final String MSG_ERROR_MAILS = "manage_bulk_user_export_dialog_error_mail";

	private static final long serialVersionUID = 8511115643203147770L;

	private static final Log logger = LogFactory.getLog(ManageBulkUserExport.class);

	private String emailList;
	private Boolean conjunction;
	private Boolean negation;


	public void resetState()
    {
    	emailList = null;
    	conjunction = Boolean.FALSE;
    	negation = Boolean.FALSE;
    	super.resetState();
    }

	protected List<UserExportStatus> getJobs() throws PlannificationException
	{
		return getPlannedMigrationService().getRegistredUserExportation();
	}

	public void addJobImpl()
	{
		boolean error = false;
		final String recordName = getName() == null ? null : getName().trim();

		if(recordName == null || recordName.length() < 1 || WebClientHelper.toValidFileName(recordName).equals(recordName) == false)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_JOBNAME));
			error = true;
		}
		if(getFireDate() == null)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_FIREDATE));
			error = true;
		}
		if(emailList == null || emailList.trim().length() < 1)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_MAILS));
			error = true;
		}

		if(!error)
		{
			final List<String> emails = extractEmailList(emailList);

			try
			{
				final UserExportStatus status = getPlannedMigrationService().registerUserExportation(getFireDate(), recordName, emails, conjunction, negation, getNotificationEmailList());

				if(logger.isDebugEnabled())
				{
					logger.debug("Bulk importation job successfully added: " + status);
				}

				reset(null);

			}
			catch(final PlannificationException e)
			{
				if(logger.isErrorEnabled())
				{
					logger.error("Problem adding job definition: " + e.getMessage(), e);
				}

				Utils.addErrorMessage(e.getMessage(), e);
			}
		}
	}

	/**
	 * @return the negation
	 */
	public final Boolean getNegation()
	{
		return negation;
	}

	/**
	 * @param negation the negation to set
	 */
	public final void setNegation(Boolean negation)
	{
		this.negation = negation;
	}

	/**
	 * @return the conjunction
	 */
	public final Boolean getConjunction()
	{
		return conjunction;
	}

	/**
	 * @param conjunction the conjunction to set
	 */
	public final void setConjunction(Boolean conjunction)
	{
		this.conjunction = conjunction;
	}

	/**
	 * @return the emailList
	 */
	public final String getEmailList()
	{
		return emailList;
	}

	/**
	 * @param emailList the emailList to set
	 */
	public final void setEmailList(String emailList)
	{
		this.emailList = emailList;
	}
}