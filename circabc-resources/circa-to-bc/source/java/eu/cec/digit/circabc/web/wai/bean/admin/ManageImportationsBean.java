/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.service.migration.jobs.IterationImportStatus;
import eu.cec.digit.circabc.service.migration.jobs.MigrationStatusBase.FireStatus;
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.web.WebClientHelper;

/**
 * Bean that manages the importation jobs.
 *
 * @author Pignot Yanick
 */
public class ManageImportationsBean extends ManageMigrationBaseBean
{
	private static final String MSG_ERROR_GET_ITERATIONS = "manage_importations_dialog_errors_get_iterations";
	private static final String MSG_ERROR_NO_ITERATION = "manage_exportations_dialog_error_iteration";

	private static final long serialVersionUID = 8511115643203147770L;
	private static final Log logger = LogFactory.getLog(ManageImportationsBean.class);

	private String newestIteration;
    private String selectedIteration;

    protected void resetState()
    {
    	selectedIteration = null;
        newestIteration = null;
        super.resetState();
    }

	@Override
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
		if(selectedIteration == null || selectedIteration.trim().length() < 1)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_NO_ITERATION));
			error = true;
		}

		if(!error)
		{
			try
			{
				final IterationImportStatus status = getPlannedMigrationService().registerIterationImportation(getFireDate(), recordName, selectedIteration, getNotificationEmailList());

				if(logger.isDebugEnabled())
				{
					logger.debug("Iteration importation job successfully added: " + status);
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

	protected List<IterationImportStatus> getJobs() throws PlannificationException
	{
		return getPlannedMigrationService().getRegistredIterationImportation();
	}

	public DataModel getIterationsDataModel()
    {
        //construct the data model
        final DataModel dataModel = new ListDataModel();
        try
        {
            final List<MigrationIteration> origIterations = getImportService().getIterations(false);
            final List<WebIteration> iterations = new ArrayList<WebIteration>(origIterations.size());
            final List<String> waitingIterations = getWaitingIterations();

            for(final MigrationIteration it : origIterations)
            {
            	if(waitingIterations.contains(it.getIdentifier()) == false)
                {
            		iterations.add(new WebIteration(it));
                }
            }

            dataModel.setWrappedData(iterations);

            if(selectedIteration == null && iterations.size() > 0)
            {
                newestIteration = iterations.get(0).getIdentifier();
            }
        }
        catch(Exception e)
        {
            final String message = translate(MSG_ERROR_GET_ITERATIONS);
			Utils.addErrorMessage(message, e);
            logger.error(message, e);
            dataModel.setWrappedData(Collections.<MigrationIteration>emptyList());
        }
        return dataModel;
    }

	private List<String> getWaitingIterations()
	{
		try
		{
			final List<IterationImportStatus> jobs = getJobs();
			final List<String> waitings = new ArrayList<String>();

			for(final IterationImportStatus status: jobs)
			{
				final FireStatus fireStatus = status.getStatus();
				if(FireStatus.PENDING.equals(fireStatus) || FireStatus.WAITING.equals(fireStatus))
				{
					waitings.add(status.getIterationName());
				}
			}

			return waitings;

		}
		catch (PlannificationException e)
		{
			logger.error("Error when getting waiting iterations",e);
			return Collections.<String>emptyList();
		}

	}

	/**
     * @return the selectedIteration
     */
    public final String getSelectedIteration()
    {
        return selectedIteration != null ? selectedIteration : newestIteration;
    }


    /**
     * @param selectedIteration the selectedIteration to set
     */
    public final void setSelectedIteration(String selectedIteration)
    {
        if(selectedIteration != null)
        {
            this.selectedIteration = selectedIteration;
        }
    }
}