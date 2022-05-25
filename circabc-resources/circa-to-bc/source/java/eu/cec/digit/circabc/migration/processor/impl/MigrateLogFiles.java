/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.PathNotFoundException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.generated.LogEntry;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.JournalLine.UpdateOperation;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.processor.IGLogFilesUtils;
import eu.cec.digit.circabc.migration.processor.LogsPropertiesProcessor;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Walker that create the users
 *
 * @author Yanick Pignot
 */
public class MigrateLogFiles extends MigrateProcessorBase implements LogsPropertiesProcessor
{
	private static final String EU_CEC_DIGIT_CIRCABC_CACHE_MIGRATION_CIRCA_LOG_PATH_NODE_REF_CACHE = "eu.cec.digit.circabc.cache.migration.circa.logPathNodeRefCache";
	
	private static final Log logger = LogFactory.getLog(MigrateLogFiles.class);
    
	// Changed from ehcache (not used anymore in Alfresco 4.2) to the default non-clustered simple cache, since migration is being run not clustered
	private SimpleCache<String, NodeRef> logPathNodeRefCache = new DefaultSimpleCache<String, NodeRef>(1000, EU_CEC_DIGIT_CIRCABC_CACHE_MIGRATION_CIRCA_LOG_PATH_NODE_REF_CACHE);
	
	public Report process(CircabcServiceRegistry registry, InputStream logInputStream, MigrationTracer importationJournal) throws ImportationException
	{
		ParameterCheck.mandatory("The service registry", registry);
        ParameterCheck.mandatory("An instance of the importation journal", importationJournal);
        ParameterCheck.mandatory("An instance of the report", importationJournal.getProcessReport());
        ParameterCheck.mandatory("An instance of the report report", importationJournal.getJournal());

        setJournal(importationJournal);

        if(logInputStream != null)
		{
			apply(new MigrateLogFilesCallback(importationJournal, logInputStream));
		}

		return getJournal().getProcessReport();
	};

    @Override
    public void visit(final Circabc circabc) throws Exception
    {
    	// don't loose more time ... nothing to use under circabc
    }

    @Override
    public void visit(final Persons persons) throws Exception
    {
    	// don't loose more time ... nothing to use under persons
    }

    class MigrateLogFilesCallback extends JournalizedTransactionCallback
    {
        
        private final InputStream logInputStream;
        private static final long batchSize =1000L ;


        private MigrateLogFilesCallback(final MigrationTracer journal, final InputStream logInputStream)
		{
        	super(journal);
        	this.logInputStream = logInputStream;
		}

		public String execute() throws Throwable
        {
			long counter =0L;
			List<LogRecord>  logRecords = new ArrayList<LogRecord>((int) batchSize); 
        	
		    BufferedReader br = new BufferedReader(new InputStreamReader(logInputStream));
		    String strLine;
		    while ((strLine = br.readLine()) != null)   {
		    try
      		{
      			final LogRecord logRecord = getLogRecord(IGLogFilesUtils.fromString(strLine));
      			if (logRecord != null)
      			{
      				logRecords.add(logRecord);
      				counter++;
      			}
      			if ((counter % batchSize) == 0)
          		{
      				getLogService().logBatch(logRecords);
      				logRecords.clear();
          		}
      		}
      		catch(Throwable ignore){}
		    }
		    //Close the input stream
        	
        	if (! logRecords.isEmpty() )
    		{
    			getLogService().logBatch(logRecords);
    			logRecords.clear();
    		}
			return null;
		}

		/**
		 * @param logEntry
		 * @throws Throwable
		 */
		private LogRecord getLogRecord(final LogEntry logEntry) throws Throwable
		{
			final String logEntryPath = logEntry.getPath();
			try
			{
				final LogRecord record = new LogRecord();
				record.setActivity(logEntry.getActivity());
				record.setInfo(logEntry.getInfo());
				record.setOK(logEntry.isOk());
				record.setPath(logEntryPath);
				record.setService(logEntry.getService());
				record.setUser(logEntry.getUsername());
				record.setDate(logEntry.getDate());

				try
				{
					
					final NodeRef cachedNodeRef = logPathNodeRefCache.get(logEntryPath);
					final NodeRef nodeRef;
					if (cachedNodeRef != null)
					{
						nodeRef = cachedNodeRef;
					}
					else
					{
						final SimplePath simplePath = getManagementService().getNodePath(logEntryPath);
						nodeRef = simplePath.getNodeRef();
						logPathNodeRefCache.put(logEntryPath,nodeRef);
					}
					fillServiceLogRecord(record, nodeRef);

					record.setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
				}
				catch(final PathNotFoundException e)
				{
					record.setDocumentID(null);
					String path = logEntryPath;
					while(path != null && path.length() > 0)
					{
						final String parentPath = FilePathUtils.retreiveParentPath(path);
						if (path.equals(parentPath))
						{
							break;
						}
						path = parentPath;
						try
						{
							final NodeRef cachedNodeRef = logPathNodeRefCache.get(path);
							final NodeRef nodeRef;
							if (cachedNodeRef != null)
							{
								nodeRef = cachedNodeRef;
							}
							else
							{
								final SimplePath simplePath = getManagementService().getNodePath(logEntryPath);
								nodeRef = simplePath.getNodeRef();
								logPathNodeRefCache.put(logEntryPath,nodeRef);
							}
							fillServiceLogRecord(record, nodeRef);
							break;
						}
						catch(final PathNotFoundException ignore){}
					}
				}
				
				if(logger.isInfoEnabled()) {
					logger.info("OK");
				}
				return record;
			}
			catch(Throwable t)
			{
				getReport().appendSection("Impossible to add create log file line " + toString(logEntry) + ". " + t.getMessage());

				if(isFailOnError())
				{
					throw t;
				}
				else
				{
					super.getJournal().journalize(JournalLine.updateNode(Status.FAIL,
							logEntryPath,
							UpdateOperation.ADD_LOG_ENTRY,
							Parameter.NEW_VALUE,
							toString(logEntry)));
				}

				if(logger.isErrorEnabled()) {
					logger.error("NOT OK", t);
				}
			}
			return null;
		}

		private String toString(final LogEntry logEntry)
		{
			return logEntry.getService() + "." + logEntry.getActivity() + ":" + logEntry.getInfo();
		}

        private void fillServiceLogRecord(final LogRecord record, final NodeRef nodeRef)
        {
        	final Set<QName> aspects = getNodeService().getAspects(nodeRef);
			final QName type =  getNodeService().getType(nodeRef);
			final NodeRef igNodeRef;

			if(aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT) ||
					aspects.contains(CircabcModel.ASPECT_CATEGORY) ||
					type.equals(CircabcModel.TYPE_CATEGORY_HEADER))
			{
				igNodeRef = nodeRef;
			}
			else
			{
				igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
			}

			record.setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
			record.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        }
    }

 }
