/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.jobs.IterationExportStatus;
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.web.WebClientHelper;

/**
 * Bean that manages the exportation jobs.
 *
 * @author Pignot Yanick
 */
public class ManageExportationsBean extends ManageExportationsBaseBean
{
	private static final long serialVersionUID = 8511115643203147770L;
	private static final Log logger = LogFactory.getLog(ManageExportationsBean.class);

	protected static final String MSG_ERROR_NO_IT_NAME = "manage_exportations_dialog_error_iterationname";
	protected static final String MSG_ERROR_BAD_IT_NAME = "manage_exportations_dialog_error_valid_iterationname";
	protected static final String MSG_ERROR_NO_IG = "manage_exportations_dialog_error_no_ig";

	protected void resetState()
	{
		super.resetState();
	}

	@Override
	public void addJobImpl()
	{
		boolean error = false;
		final String iterationName = getIterationName();
		final List<String> selectedInterestGroups = getSelectedInterestGroups();

		if(getFireDate() == null)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_FIREDATE));
			error = true;
		}

		if(iterationName == null || iterationName.trim().length() < 1)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_NO_IT_NAME));
			error = true;
		}
		else if(WebClientHelper.toValidFileName(iterationName).equals(iterationName) == false)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_BAD_IT_NAME, WebClientHelper.toValidFileName(iterationName)));
			error = true;
		}
		if(selectedInterestGroups == null || selectedInterestGroups.size() < 1)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_NO_IG));
			error = true;
		}

		if(!error)
		{
			try
			{
				final List<CategoryInterestGroupPair> pairs = new ArrayList<CategoryInterestGroupPair>(selectedInterestGroups.size());
				for(final String ig: selectedInterestGroups)
				{
					pairs.add(CategoryInterestGroupPair.fromString(ig));
				}

				final IterationExportStatus status = getPlannedMigrationService().registerIterationExportation(getFireDate(), iterationName.trim(), iterationName, getIterationDescription(), pairs, getNotificationEmailList());

				if(logger.isDebugEnabled())
				{
					logger.debug("Iteration Exportation job successfully added: " + status);
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

	protected List<IterationExportStatus> getJobs() throws PlannificationException
	{
		return getPlannedMigrationService().getRegistredIterationExportation();
	}
}