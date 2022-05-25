/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.MigrationLog;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.journal.report.impl.HtmlReportDecorator;
import eu.cec.digit.circabc.service.migration.jobs.MigrationStatusBase;
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;

/**
 * Bean that manages the importation jobs.
 *
 * @author Pignot Yanick
 */
public class RunningImportsBean extends ManageMigrationBaseBean
{
	private static final String HTML_EOL = "<br />";
	private static final String HTML_SEPARATION = "<br /><hr /><br />";
	private static final String HTML_END_TITLE = "</u></b>";
	private static final String HTML_START_TITLE = "<b><u>";
	private static final String HTML_SPACE = "&nbsp;";
	
	private static final ThreadLocal<DateFormat> HTML_DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("'<b>'yyyy MM dd HH:mm:ss'</b>'");
		}
	};

	private static final String MSG_VALIDATION = "view_running_imports_dialog_view_last_validations";
	private static final String MSG_MESSAGES = "view_running_imports_dialog_view_last_messages";
	private static final String MSG_PHASES = "view_running_imports_dialog_view_phases";
	private static final String MSG_REPORT = "view_running_imports_dialog_view_report";
	private static final String MSG_NOT_ACTIVE = "view_running_imports_dialog_view_no_active";
	private static final String MSG_ERROR_REPORT = "view_running_imports_dialog_error_report";
	private static final String MSG_ERROR_INT = "view_running_imports_dialog_error_int";
	private static final String PARAM_ITERATION = "iteration";

	private static final long serialVersionUID = 8511115643299997770L;

	private int logsLines = 20;
	private int validationLines = 5;
	private Boolean printReport = Boolean.FALSE;
	private String selectedIteration;

    protected void resetState()
    {
    	super.resetState();
    	selectedIteration = null;
    }

    public void doNothing(final ActionEvent event)
    {

    }

    public void viewDetails(final ActionEvent event)
    {
    	final UIActionLink link = (UIActionLink) event.getComponent();
    	final Map<String, String> params = link.getParameterMap();
        selectedIteration = params.get(PARAM_ITERATION);
    }

	/**
	 * @param collectionSize
	 * @return
	 */
	private int computeStart(final int collectionSize, final int maxColSzie)
	{
		int idxStart = collectionSize - maxColSzie;

		if(idxStart < 0)
		{
			idxStart = 0;
		}
		else if(idxStart > collectionSize)
		{
			idxStart = collectionSize;
		}
		return idxStart;
	}


    public final List<RunningIteration> getRunningImportations()
	{
		final RetryingTransactionHelper helper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
		return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<RunningIteration>>()
		{
		    public List<RunningIteration> execute() throws Throwable
			{
		    	final List<RunningIteration> wrappers = new ArrayList<RunningIteration>();

		    	addWrappers(wrappers, RunningIteration.IterationType.IMPORT);

		    	if(isExportAvailable())
		    	{
		    		addWrappers(wrappers, RunningIteration.IterationType.EXPORT);
		    		addWrappers(wrappers, RunningIteration.IterationType.USER_EXPORT);
		    		addWrappers(wrappers, RunningIteration.IterationType.STATISTIC);
			    }

				return wrappers;
			}
		});
	}

    private void addWrappers(final List<RunningIteration> wrappers, final RunningIteration.IterationType type)
    {
    	final Set<String> runningIterations;

    	switch (type)
		{
			case EXPORT:
				runningIterations = getExportService().getRunningIterations();
				break;

			case IMPORT:
				runningIterations = getImportService().getRunningIterations();
				break;

			case STATISTIC:
				runningIterations = getExportStatisticsService().getRunningStatistics();
				break;

			case USER_EXPORT:
				runningIterations = getAidaMigrationService().getRunningExportations();
				break;

			default:
				runningIterations = Collections.emptySet();
				break;
		}

    	MigrationTracer<?> journal = null;
    	for(final String iterationName: runningIterations)
    	{
    		switch (type)
    		{
    			case EXPORT:
    				journal = getExportService().getRunningIterationsJournal(iterationName);
    				break;

    			case IMPORT:
    				journal = getImportService().getRunningIterationsJournal(iterationName);
    				break;

    			case STATISTIC:
    				journal = getExportStatisticsService().getRunningStatisticJournal(iterationName);
    				break;

    			case USER_EXPORT:
    				journal = getAidaMigrationService().getRunningExportJournal(iterationName);
    				break;
    		}

    		if(journal != null)
    		{
    			wrappers.add(new RunningIteration(
    					journal,
	    				type
	    				));
    		}
    	}


    }

    public static class RunningIteration implements Serializable
    {
    	/** */
		private static final long serialVersionUID = -4306523035241988508L;

		public enum IterationType
		{
			IMPORT,
			EXPORT,
			USER_EXPORT,
			STATISTIC
		}

		final MigrationTracer<?> runningIterationsJournal;
		final IterationType type;

    	public RunningIteration(final MigrationTracer<?> runningIterationsJournal, final IterationType type)
		{
    		this.runningIterationsJournal = runningIterationsJournal;
    		this.type = type;
		}

    	/**
    	 * @return the iteration
    	 */
    	public final String getIterationName()
    	{
    		final MigrationIteration iteration = runningIterationsJournal.getIteration();
			return iteration == null ? "" : iteration.getIdentifier();
    	}

    	/**
    	 * @return the iteration
    	 */
    	public final String getProcessName()
    	{
    		return runningIterationsJournal.getProcessName();
    	}


    	public final Date getStartDate()
    	{
    		return runningIterationsJournal.getStartDate();
    	}

    	/**
    	 * @return the last runningPhase
    	 */
    	public final String getRunningPhase()
    	{
    		return runningIterationsJournal.getRunningPhase();
    	}

    	/**
    	 * @return the runningPhases
    	 */
    	public final Map<Date, String> getRunningPhases()
    	{
    		return runningIterationsJournal.getRunningPhases();
    	}

		private final MigrationTracer getRunningIterationsJournal()
		{
			return runningIterationsJournal;
		}

		/**
		 * @return the type
		 */
		public final IterationType getType()
		{
			return type;
		}
    }


	public final String getSelectedIteration()
	{
		return selectedIteration;
	}

	public final String getSelectedIterationMessage()
	{
		String selectedIterationMessage = null;
        if(selectedIteration != null && selectedIteration.length() > 0)
        {
        	final List<RunningIteration> runnings = getRunningImportations();
        	for(final RunningIteration it: runnings)
        	{
        		if(it.getIterationName().equals(selectedIteration))
        		{
        			final MigrationTracer<?> journal = it.getRunningIterationsJournal();
        			final StringBuffer buffer = new StringBuffer("");
        			final List<Date> sortedDates = new ArrayList<Date>();

        			final Map<Date, String> runningPhases = journal.getRunningPhases();
					sortedDates.addAll(runningPhases.keySet());
        			Collections.sort(sortedDates);

        			// 1. print the phases

        			buffer
	    				.append(HTML_START_TITLE)
	    				.append(translate(MSG_PHASES))
	    				.append(HTML_END_TITLE)
	    				.append(HTML_EOL)
	    				.append(HTML_EOL);

	    			for(final Date date: sortedDates)
	    			{
	    				buffer
	    					.append(HTML_DATE_FORMAT.get().format(date))
	    					.append(HTML_SPACE)
	    					.append(HTML_SPACE)
	    					.append(runningPhases.get(date))
	    					.append(HTML_EOL);
	    			}

	    			buffer.append(HTML_SEPARATION);

	    			// 2. print the the report (Human Readable)

        			if(printReport.booleanValue())
        			{
        				final Report report = journal.getProcessReport();

        				buffer
		    				.append(HTML_START_TITLE)
		    				.append(translate(MSG_REPORT))
		    				.append(HTML_END_TITLE)
		    				.append(HTML_EOL)
		    				.append(HTML_EOL);

        				if(report != null)
        				{
        					final HtmlReportDecorator decorator = new HtmlReportDecorator(report);
        					final StringWriter writter = new StringWriter();
        					try
							{
								decorator.writeReport(writter);
								buffer.append(writter.getBuffer());
							}
        					catch (final IOException e)
							{
        						buffer.append(translate(MSG_ERROR_REPORT, e.getMessage()));
							}
        				}

        				buffer
        					.append(HTML_SEPARATION);
        			}

        			// 3. print the logs

        			final List<JournalLine> logs = journal.getJournal();
        			final int logSize = logs.size();
        			final int idxLogStart = computeStart(logSize, getLogsLines());

        			final List<JournalLine> sublistLog = logs.subList(idxLogStart, logSize);


	    			buffer
	    				.append(HTML_START_TITLE)
	    				.append(translate(MSG_MESSAGES, getLogsLines(), logSize))
	    				.append(HTML_END_TITLE)
	    				.append(HTML_EOL)
	    				.append(HTML_EOL);

	    			for(final JournalLine line: sublistLog)
	    			{
	    				buffer
	    					.append(line.toString(HTML_DATE_FORMAT.get()))
	    					.append(HTML_EOL);
	    			}

	    			buffer.append(HTML_SEPARATION);

	    			// 4. print the validation

	    			final List<MigrationLog> validations = journal.getValidationMessages();
        			final int valSize = validations.size();
        			final int idxValStart = computeStart(valSize, getValidationLines());

        			final List<MigrationLog> subListValidations = validations.subList(idxValStart, valSize);

        			buffer
        				.append(HTML_START_TITLE)
        				.append(translate(MSG_VALIDATION, getValidationLines(), valSize))
        				.append(HTML_END_TITLE)
        				.append(HTML_EOL)
        				.append(HTML_EOL);

        			for(final MigrationLog line: subListValidations)
        			{
        				buffer
        					.append(line.toString(HTML_DATE_FORMAT.get()))
        					.append(HTML_EOL);
        			}

        			selectedIterationMessage = buffer.toString();
        			break;
        		}
        	}

        	if(selectedIterationMessage == null)
        	{
        		Utils.addErrorMessage(translate(MSG_NOT_ACTIVE, selectedIteration));
        		selectedIteration = null;
        	}
        }
        else
        {
        	selectedIteration = null;
        }

        return selectedIterationMessage;
	}

	public boolean isIterationSelected()
	{
		return selectedIteration != null && selectedIteration.length() > 0;
	}



	@Override
	public void addJobImpl()
	{
		// not used
	}

	@Override
	protected List<? extends MigrationStatusBase> getJobs() throws PlannificationException
	{
		// not used
		return null;
	}

	/**
	 * @return the logsLines
	 */
	public final int getLogsLines()
	{
		return logsLines;
	}

	/**
	 * @param logsLines the logsLines to set
	 */
	public final void setLogsLines(int logsLines)
	{
		this.logsLines = logsLines;
	}

	/**
	 * @return the logsLines
	 */
	public final String getLogsLinesStr()
	{
		return String.valueOf(logsLines);
	}

	/**
	 * @param logsLines the logsLines to set
	 */
	public final void setLogsLinesStr(final String logsLinesStr)
	{
		try
		{
			this.logsLines = Integer.parseInt(logsLinesStr);
		}
		catch(NumberFormatException e)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_INT, logsLinesStr));
		}
	}

	/**
	 * @return the validationLines
	 */
	public final int getValidationLines()
	{
		return validationLines;
	}

	/**
	 * @param validationLines the validationLines to set
	 */
	public final void setValidationLines(int validationLines)
	{
		this.validationLines = validationLines;
	}

	/**
	 * @return the validationLines
	 */
	public final String getValidationLinesStr()
	{
		return String.valueOf(validationLines);
	}

	/**
	 * @param validationLines the validationLines to set
	 */
	public final void setValidationLinesStr(String validationLinesStr)
	{
		try
		{
			this.validationLines = Integer.parseInt(validationLinesStr);
		}
		catch(NumberFormatException e)
		{
			Utils.addErrorMessage(translate(MSG_ERROR_INT, validationLinesStr));
		}
	}

	/**
	 * @return the printReport
	 */
	public final Boolean getPrintReport()
	{
		return printReport;
	}

	/**
	 * @param printReport the printReport to set
	 */
	public final void setPrintReport(Boolean printReport)
	{
		this.printReport = printReport;
	}


}