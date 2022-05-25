/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.journal.report.impl.ReportLog;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;

/**
 * @author Yanick Pignot
 */
public class MigrationTracer<T extends Serializable> implements Serializable
{
	/** */
	private static final long serialVersionUID = -5870461860404669118L;

	/* The unmarshalled root object */
	private T unmarshalledObject = null;

	/* The importation implementtation name */
	private final String processName;

	/* The messages of the validation process */
	private final List<MigrationLog> validationMessages = new ArrayList<MigrationLog>();
	/* The logger of the processors */
	private final Report processReport = new ReportLog();
	/* The journal */
	private final List<JournalLine> journal = new ArrayList<JournalLine>();
	/* if fail on error */
	private final boolean failOnError;

	/* the iteration concerned */
	private MigrationIteration iteration = null;
	/* set to true if the process is in read only */
	private boolean readOnly = true;
	/* The start date */
	private final Date startDate = new Date();
	/* The nunning phase */
	private Map<Date, String> runningPhases = new HashMap<Date, String>();

	/* log file */
	private NodeRef logFile;
	/* The persisted file */
	private NodeRef marshalledFile;

	/* The ig exported */
	private List<CategoryInterestGroupPair> interestGroups;


	/**
	 *
	 */
	public MigrationTracer(final String processName, final boolean failOnError)
	{
		super();
		this.processName = processName;
		this.failOnError = failOnError;
		runningPhases.put(startDate, "initialization");
	}


	/**
	 * @return the processReport
	 */
	public final Report getProcessReport()
	{
		return processReport;
	}

	/**
	 * @return the validationMessages
	 */
	public final List<MigrationLog> getValidationMessages()
	{
		return validationMessages;
	}

	public void addValidationMessage(final MigrationLog log)
	{
		validationMessages.add(log);
	}

	public void journalize(final JournalLine journalLine)
	{
		journal.add(journalLine);
	}

	/**
	 * @return the readOnly
	 */
	public final boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public final void setReadOnly(final boolean readOnly)
	{
		this.readOnly = readOnly;
	}


	/**
	 * @return the journal
	 */
	public final List<JournalLine> getJournal()
	{
		return journal;
	}

	/**
	 * @return the iteration
	 */
	public final MigrationIteration getIteration()
	{
		return iteration;
	}

	/**
	 * @param iteration the iteration to set
	 */
	public final void setIteration(final MigrationIteration iteration)
	{
		if(this.iteration != null)
		{
			throw new IllegalAccessError("The iteration in Read Only");
		}
		this.iteration = iteration;
	}

	public final Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @return the last runningPhase
	 */
	public final String getRunningPhase()
	{
		Date last = null;

		for(final Map.Entry<Date, String> entry: runningPhases.entrySet())
		{
			if(last == null || last.before(entry.getKey()))
			{
				last = entry.getKey();
			}
		}

		return runningPhases.get(last);
	}

	/**
	 * @return the runningPhases
	 */
	public final Map<Date, String> getRunningPhases()
	{
		return runningPhases;
	}


	/**
	 * @param runningPhase the runningPhase to set
	 */
	public final void setRunningPhase(String runningPhase)
	{
		this.runningPhases.put(new Date(), runningPhase);
	}


	/**
	 * @return the logFile
	 */
	public final NodeRef getLogFile()
	{
		return logFile;
	}


	/**
	 * @param logFile the logFile to set
	 */
	public final void setLogFile(NodeRef logFile)
	{
		this.logFile = logFile;
	}


	/**
	 * @return the processName
	 */
	public final String getProcessName()
	{
		return processName;
	}


	/**
	 * @return the interestGroups
	 */
	public final List<CategoryInterestGroupPair> getInterestGroups()
	{
		return interestGroups;
	}


	/**
	 * @param interestGroups the interestGroups to set
	 */
	public final void setInterestGroups(final List<CategoryInterestGroupPair> interestGroups)
	{
		this.interestGroups = interestGroups;
	}


	/**
	 * @return the unmarshalledObject
	 */
	public final T getUnmarshalledObject()
	{
		return unmarshalledObject;
	}


	/**
	 * @param unmarshalledObject the unmarshalledObject to set
	 */
	public final void setUnmarshalledObject(T unmarshalledObject)
	{
		this.unmarshalledObject = unmarshalledObject;
	}


	/**
	 * @return the marshalledFile
	 */
	public final NodeRef getMarshalledFile()
	{
		return marshalledFile;
	}


	/**
	 * @param marshalledFile the marshalledFile to set
	 */
	public final void setMarshalledFile(NodeRef marshalledFile)
	{
		this.marshalledFile = marshalledFile;
	}


	/**
	 * @return the failOnError
	 */
	public final boolean isFailOnError()
	{
		return failOnError;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((iteration == null) ? 0 : iteration.hashCode());
		result = PRIME * result + ((processName == null) ? 0 : processName.hashCode());
		result = PRIME * result + ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MigrationTracer other = (MigrationTracer) obj;
		if (iteration == null)
		{
			if (other.iteration != null)
				return false;
		} else if (!iteration.equals(other.iteration))
			return false;
		if (processName == null)
		{
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		if (startDate == null)
		{
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}
}
