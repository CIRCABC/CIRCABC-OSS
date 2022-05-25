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
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.service.migration.jobs.StatisticExportStatus;
import eu.cec.digit.circabc.web.WebClientHelper;

/**
 * Bean that manages the exportation jobs.
 *
 * @author Pignot Yanick
 */
public class ManageExportationStatisticsBean extends ManageExportationsBaseBean
{
	private static final long serialVersionUID = 851199993203147770L;

	private static final Log logger = LogFactory.getLog(ManageExportationStatisticsBean.class);

	private boolean allInterestGroup = false;

	protected void resetState()
	{
		super.resetState();
		allInterestGroup = false;
	}

	@Override
	public void addJobImpl()
	{
		boolean error = false;

		final String recordName = getName() == null ? null : getName().trim();
		final List<String> selectedInterestGroups = getSelectedInterestGroups();

		if (recordName == null || recordName.length() < 1
				|| WebClientHelper.toValidFileName(recordName).equals(recordName) == false)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_JOBNAME));
			error = true;
		}
		if (getFireDate() == null)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_FIREDATE));
			error = true;
		}
		if (allInterestGroup == false && (selectedInterestGroups == null || selectedInterestGroups.size() < 1))
		{
			Utils.addErrorMessage(translate(MSG_ERROR_NO_IG));
			error = true;
		}

		if (!error)
		{
			try
			{
				final StatisticExportStatus status;

				if (allInterestGroup)
				{
					status = getPlannedMigrationService().registerStatisticExportation(getFireDate(), getName(),
							getNotificationEmailList());
				} else
				{
					final List<CategoryInterestGroupPair> pairs = new ArrayList<CategoryInterestGroupPair>(selectedInterestGroups
							.size());
					for (final String ig : selectedInterestGroups)
					{
						pairs.add(CategoryInterestGroupPair.fromString(ig));
					}

					status = getPlannedMigrationService().registerStatisticExportation(getFireDate(), getName(), pairs,
							getNotificationEmailList());
				}

				if (logger.isDebugEnabled())
				{
					logger.debug("Iteration Exportation job successfully added: " + status);
				}

				reset(null);

			} catch (final PlannificationException e)
			{
				if (logger.isErrorEnabled())
				{
					logger.error("Problem adding job definition: " + e.getMessage(), e);
				}

				Utils.addErrorMessage(e.getMessage(), e);
			}
		}
	}

	protected List<StatisticExportStatus> getJobs() throws PlannificationException
	{
		return getPlannedMigrationService().getRegistredExportationStatistics();
	}

	/**
	 * @return the allInterestGroup
	 */
	public final boolean isAllInterestGroup()
	{
		return allInterestGroup;
	}

	/**
	 * @param allInterestGroup
	 *            the allInterestGroup to set
	 */
	public final void setAllInterestGroup(boolean allInterestGroup)
	{
		this.allInterestGroup = allInterestGroup;
	}

}