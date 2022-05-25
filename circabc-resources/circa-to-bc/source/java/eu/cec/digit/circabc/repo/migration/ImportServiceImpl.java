/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.core.task.TaskExecutor;
import org.xml.sax.SAXException;

import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.LockException;
import eu.cec.digit.circabc.migration.archive.LockHandler;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Operation;
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.MigrationLog;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.journal.report.Report.Section;
import eu.cec.digit.circabc.migration.journal.report.Report.SubSection;
import eu.cec.digit.circabc.migration.processor.LogsPropertiesProcessor;
import eu.cec.digit.circabc.migration.processor.PostProcessor;
import eu.cec.digit.circabc.migration.processor.PreProcessor;
import eu.cec.digit.circabc.migration.processor.Processor;
import eu.cec.digit.circabc.migration.validation.ValidationException;
import eu.cec.digit.circabc.migration.validation.ValidationHandler;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.AsynchJobListeners;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Concrete implementation of the Import Service
 *
 * @author Yanick Pignot
 */
public class ImportServiceImpl implements ImportService
{
	private static final Log logger = LogFactory.getLog(ImportServiceImpl.class);

	private static final String PROCESS_NAME = "importation";

	private TransactionService transactionService;
	private TaskExecutor taskExecutor;
	private CircabcServiceRegistry serviceRegistry;

	private ValidationHandler validationHandler;
	private FileArchiver fileArchiver;
	private LockHandler lockHandler;

	private List<String> preProcessors;
	private List<String> runProcessors;
	private List<String> dryRunProcessors;
	private List<String> postProcessors;

	private boolean failOnError = true;

	private Map<String, MigrationTracer<ImportRoot>> runningJournals = new HashMap<String, MigrationTracer<ImportRoot>>();

	public NodeRef storeNewImportFile(final InputStream streamApiFile, final InputStream streamLogsFile, final String shortLabel, final String description) throws ImportationException
	{
		MigrationIteration iteration = null;

		try
		{
			iteration = fileArchiver.startNewIteration(shortLabel, description);

			final NodeRef apiFileRef = fileArchiver.storeOriginalExportFile(iteration, streamApiFile);

			if(streamLogsFile != null)
			{
				final Properties props = new Properties();
				props.load(streamLogsFile);
				fileArchiver.storeLogsExportFile(iteration, props);
			}

			return apiFileRef;
		}
		catch (final Exception e)
		{
			throw new ImportationException(e.getMessage(), e);
		}
		finally
		{
			fileArchiver.removeIterationIfEmpty(iteration);
		}
	}

	public List<MigrationIteration> getIterations(final boolean sortAscending) throws ImportationException
	{
		try
		{
			final List<MigrationIteration> importAllowedIteration = new ArrayList<MigrationIteration>();
			for(final MigrationIteration iteration: fileArchiver.getIterations())
			{
				if(fileArchiver.isIterationReadyForMigration(iteration) == true
						&& runningJournals.containsKey(iteration.getIdentifier()) == false)
				{
					importAllowedIteration.add(iteration);
				}
			}

			Collections.sort(importAllowedIteration, new IterationComparator(sortAscending));
			return importAllowedIteration;
		}
		catch (final Exception e)
		{
			throw new ImportationException(e.getMessage(), e);
		}

	}

	public MigrationTracer<ImportRoot> dryRun(final String iterationName) throws ImportationException
	{
		final NodeRef xmlDocument = getIterationNodeRef(iterationName);
		return dryRun(xmlDocument);
	}

	private MigrationTracer<ImportRoot> dryRun(final NodeRef resource) throws ImportationException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Start to dry run importation with content node: " + resource);
		}

		final MigrationTracer<ImportRoot> journal = new MigrationTracer<ImportRoot>(PROCESS_NAME, failOnError);

		try
		{
			// 1.	Validate the file
			validateImpl(resource, journal);
			final ImportRoot importRoot = journal.getUnmarshalledObject();

			journal.setReadOnly(true);

			// 2.	Launch pre processors
			for(final String clazz : this.preProcessors)
			{
				PreProcessor preProcessor = buildProcessor(clazz);

				if(logger.isDebugEnabled())
				{
					logger.debug("Launching pre processor: " + preProcessor.getClass());
				}

				preProcessor.beforeProcess(serviceRegistry, importRoot, journal);

			}

			// 3.	Launch pre processors
			for(final String clazz : this.dryRunProcessors)
			{
				Processor processor = buildProcessor(clazz);
				if(logger.isDebugEnabled())
				{
					logger.debug("Launching processor: " + processor.getClass());
				}

				processor.process(serviceRegistry, importRoot, journal);
			}
		}
		catch(final Exception e)
		{
			manageException(resource, e);
		}

		return journal;
	}

	public void asynchRun(final String iterationName, final AsynchJobListeners.BeforeRunJob beforeRun, final AsynchJobListeners.AfterRunJob<ImportRoot> afterCommit) throws ImportationException
	{
		final ImportServiceImpl impService = this;
		final String username = AuthenticationUtil.getFullyAuthenticatedUser();
		taskExecutor.execute(new Runnable()
		{
			public void run()
			{
				final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
				txnHelper.doInTransaction(new RetryingTransactionCallback<Object>()
				{
					public Object execute() throws Throwable
					{
						AuthenticationUtil.runAs(new RunAsWork<Object>()
						{
							public Object doWork() throws Exception
							{
								try
								{
									if(beforeRun != null)
									{
										beforeRun.start(serviceRegistry);
									}

									final MigrationTracer<ImportRoot> tracer = impService.run(iterationName);

									if(afterCommit != null)
									{
										afterCommit.success(serviceRegistry, tracer);
									}
								}
								catch (final ImportationException e)
								{
									if(logger.isErrorEnabled())
									{
										logger.error("Error importing iteration: " + iterationName, e);
									}

									if(afterCommit != null)
									{
										afterCommit.fail(serviceRegistry, e);
									}
								}
								return null;
							}
						}, username);

						return null;
					}
				});
			}
		});
	}

	public Set<String> getRunningIterations()
	{
		return runningJournals.keySet();
	}

	public MigrationTracer<ImportRoot> getRunningIterationsJournal(final String iterationName)
	{
		return runningJournals.get(iterationName);
	}

	public MigrationTracer<ImportRoot> run(final String iterationName) throws ImportationException
	{
		final NodeRef xmlDocument = getIterationNodeRef(iterationName);
		return run(xmlDocument, iterationName);
	}


	/**
	 * @throws ImportationException
	 * @see eu.cec.digit.circabc.service.migration.ImportService#run(org.alfresco.web.framework.resource.Resource)
	 */
	private MigrationTracer<ImportRoot> run(final NodeRef resource, final String iterationName) throws ImportationException
	{
		ParameterCheck.mandatory("The xml noderef ", resource);

		if(runningJournals.containsKey(iterationName))
		{
			throw new ImportationException("Impossible to launch importation of the itaration " + iterationName + " since it's already running");
		}

		final MigrationTracer<ImportRoot> journal = new MigrationTracer<ImportRoot>(PROCESS_NAME, failOnError);

		final MigrationIteration iteration;

		try
		{
			iteration = fileArchiver.getNodeIteration(resource);
		}
		catch (final Exception e)
		{
			throw new ImportationException(e.getMessage(), e);
		}

		if(iteration == null)
		{
			throw new ImportationException("The noderef " + resource + " is not part of a common migration element. " );
		}

		final Date processId = new Date();

		if(logger.isDebugEnabled())
		{
			logger.debug("Start to run importation with reference: " + resource + " and iteration " + iteration.getIdentifier());
		}

		try
		{
			runningJournals.put(iterationName, journal);
			journal.setReadOnly(false);
			journal.setIteration(iteration);

			long start = 0;
			if(logger.isInfoEnabled())
			{
				start = System.currentTimeMillis();
			}

			final boolean  isFirstImport ;
			NodeRef circaLogFile = null;  
			//check if logs are stored in a separate file
			if(isFirstImport(journal))
			{
				// its the first transformation get the ig log files
				circaLogFile = journal.getIteration().getOriginalIgLogsFileNodeRef();
				isFirstImport = true;
			}
			else
			{
				isFirstImport = false;
			}

			// 2.	Validate the file
			journal.setRunningPhase("Validation");
			validateImpl(resource, journal);
			final ImportRoot importRoot = journal.getUnmarshalledObject();

			if(logger.isInfoEnabled())
			{
				logger.info("**********************************************************************************");
				logger.info("Total validation phase time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
				logger.info("*********************************************************************************");
			}


			// 3.	Launch pre processors
			for(final String clazz : this.preProcessors)
			{
				PreProcessor preProcessor = buildProcessor(clazz);

				journal.setRunningPhase("PreProcessor: " + preProcessor.getClass());

				if(logger.isDebugEnabled())
				{
					logger.debug("Launching pre processor: " + preProcessor.getClass());
				}

				if(logger.isInfoEnabled())
				{
					start = System.currentTimeMillis();
				}

				preProcessor.beforeProcess(serviceRegistry, importRoot, journal);

				if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Preprocessor " +  preProcessor.getClass()  + " phase time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");
				}
			}

			// 4.	Store the xml that will be applied to the stucture (after validation and pre processing)
			fileArchiver.storeImportedTranformedXML(iteration, processId, JavaXmlBinder.marshallInStream(importRoot));

			// 5.	Lock the migration process
			lockHandler.lock(fileArchiver.getMigrationRootSpace());
			if(logger.isDebugEnabled())
			{
				logger.debug("Lock successfully acquired.");
			}

			// 6.	Launch processors
			for(final String clazz : this.runProcessors)
			{
				Processor processor = buildProcessor(clazz);

				journal.setRunningPhase("Processor: " + processor.getClass());

				if(logger.isDebugEnabled())
				{
					logger.debug("Launching processor: " + processor.getClass());
				}

				if(logger.isInfoEnabled())
				{
					start = System.currentTimeMillis();
				}

				processor.process(serviceRegistry, importRoot, journal);

				if(isFirstImport && processor instanceof LogsPropertiesProcessor)
				{
					
					InputStream  logInputStream =  fileArchiver.getContentInputStream(circaLogFile);
					
					((LogsPropertiesProcessor) processor).process(serviceRegistry, logInputStream, journal);
					logInputStream.close();
				}

				if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Processor " +  processor.getClass()  + " phase time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");
				}
			}



			// 7.	Launch pre processors
			for(final String clazz : this.postProcessors)
			{
				PostProcessor processor = buildProcessor(clazz);

				journal.setRunningPhase("PostProcessor: " + processor.getClass());

				if(logger.isDebugEnabled())
				{
					logger.debug("Launching post processor: " + processor.getClass());
				}

				if(logger.isInfoEnabled())
				{
					start = System.currentTimeMillis();
				}

				processor.afterProcess(serviceRegistry, importRoot, journal);

				if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Postprocessor " +  processor.getClass()  + " phase time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");
				}
			}

			journal.setRunningPhase("Make Success result persistant");

			// 8.	unlock the space
			lockHandler.unLock(fileArchiver.getMigrationRootSpace());

			// 9.	Store the xml that is applied to the structure (enhanced by node id)
			final NodeRef applied = fileArchiver.storeImportedAppliedXML(iteration, processId, JavaXmlBinder.marshallInStream(importRoot));
			journal.setMarshalledFile(applied);

			// 10.	Store journal
			//final NodeRef logFile = fileArchiver.storeImportationLogResult(iteration, processId, journalAsDom(journal));
			final NodeRef logFile = fileArchiver.storeImportationLogResultDocument(iteration, processId, journalAsDom(journal));
			journal.setLogFile(logFile);

		}
		catch(final Throwable t)
		{
			journal.setRunningPhase("Make Failed result persistant");

			try
			{
				fileArchiver.flagImportationAsFailed(iteration, processId, t);
			}
			catch (ArchiveException ignore){}

			manageException(resource, t);
		}
		finally
		{
			runningJournals.remove(iterationName);

			try
			{
				lockHandler.unLock(fileArchiver.getMigrationRootSpace());

				if(logger.isDebugEnabled())
				{
					logger.debug("Lock successfully released.");
				}
			}
			catch (final Exception e)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Lock NOT successfully released: " + e.getMessage());
				}
			}
		}

		return journal;
	}

	public MigrationTracer<ImportRoot> validate(String iterationName) throws ImportationException
	{
		final NodeRef xmlDocument = getIterationNodeRef(iterationName);
		return validate(xmlDocument);
	}

	/**
	 * @see eu.cec.digit.circabc.service.migration.ImportService#validate(org.alfresco.web.framework.resource.Resource)
	 */
	public MigrationTracer<ImportRoot> validate(final NodeRef resource) throws ImportationException
	{
		final MigrationTracer<ImportRoot> journal = new MigrationTracer<ImportRoot>(PROCESS_NAME, failOnError);

		validateImpl(resource, journal);

		return journal;
	}

	/**
	 * @param resource
	 * @param journal
	 * @throws ImportationException
	 */
	private void validateImpl(final NodeRef resource, final MigrationTracer<ImportRoot> journal) throws ImportationException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Start to validation ref: " + resource);
		}

		Unmarshaller unmarshaller;

		try
		{
			unmarshaller = JavaXmlBinder.createUnmarshaller();

			journal.setRunningPhase("Validation: build validate API xml file" );
			validationHandler.validateXMLFile(unmarshaller, fileArchiver.getContentInputStream(resource), journal);
			journal.setRunningPhase("Validation: java validation for non supported xsd features" );
			validationHandler.validateDataStructure(journal);

			final ImportRoot importRoot = journal.getUnmarshalledObject();

			if(logger.isDebugEnabled())
			{
				logger.debug("Resource successfully validate. Messages: " + journal.getValidationMessages().size());
			}

			journal.setInterestGroups(new ArrayList<CategoryInterestGroupPair>());

			if(importRoot != null && importRoot.getCircabc() != null)
			{
				// fill the interest groups in the journal
				for(final CategoryHeader header: importRoot.getCircabc().getCategoryHeaders())
				{
					for(final Category cat: header.getCategories())
					{
						for(final InterestGroup ig: cat.getInterestGroups())
						{
							journal.getInterestGroups().add(new CategoryInterestGroupPair(
									(String) cat.getName().getValue(),
									(String) ig.getName().getValue()));
						}
					}
				}
			}

		}
		catch(final Exception e)
		{
			e.printStackTrace();
			manageException(resource, e);
		}
	}

	private final NodeRef getIterationNodeRef(String iterationName) throws ImportationException
	{
		MigrationIteration selectedIteration;
		try
		{
			selectedIteration = fileArchiver.getIterationByName(iterationName);
			return fileArchiver.getImportResource(selectedIteration);
		}
		catch (ArchiveException e)
		{
			throw new ImportationException(e.getMessage(), e);
		}

	}

	private Document journalAsDom(final MigrationTracer<ImportRoot> tracer)
	{
		DateFormat df = DateFormat.getDateTimeInstance();

		final List<MigrationLog> validation = tracer.getValidationMessages();
		final Report report = tracer.getProcessReport();
		final List<JournalLine> journal = tracer.getJournal();
		final Map<Date, String> phases = tracer.getRunningPhases();

		final Document document = DocumentHelper.createDocument();
		final String igsString = tracer.getInterestGroups().toString();
		final Element root = document.addElement("full-report");
		root.addAttribute("readOnly", String.valueOf(tracer.isReadOnly()));
		root.addAttribute("failOnError", "" + tracer.isFailOnError());
		root.addAttribute("iteration", tracer.getIteration().getIdentifier());
		root.addAttribute("igs", igsString.substring(1, igsString.length() -1));
		root.addAttribute("name", tracer.getProcessName());
		root.addAttribute("startAt", df.format(tracer.getStartDate()));

		final Element validationsElement = root.addElement("validations");
		final Element reportsElement = root.addElement("reports");
		final Element journalsElement = root.addElement("journals");
		final Element phasesElement = root.addElement("phases");



		if(phases != null)
		{
			for(Map.Entry<Date, String> phase: phases.entrySet())
			{
				final Element phaseElement = phasesElement.addElement("phase");

				phaseElement
					.addElement("start")
					.setText(df.format(phase.getKey()));

				phaseElement
					.addElement("name")
					.setText(phase.getValue());
			}

		}

		if(validation != null)
		{

			for(final MigrationLog message: validation)
			{
				final Element validationElement = validationsElement.addElement("validation");

				validationElement
					.addElement("date")
					.setText(df.format(message.getDate()));
				validationElement
					.addElement("level")
					.setText(message.getMessageLevel().name());
				validationElement
					.addElement("message")
					.setText(message.getMessage());
				validationElement
					.addElement("log")
					.setText(message.toString());
			}
		}

		if(report != null)
		{
			for(final Section section: report.getSections())
			{
				final Element reportElement = reportsElement.addElement("report");

				final Element sectionElem = reportElement.addElement("section");
				sectionElem.addAttribute("name", section.getTitle());

				if(section.getSubSections() != null)
				{
					for(final SubSection subsection: section.getSubSections())
					{
						final Element subsectionElem = sectionElem.addElement("subsection");
						sectionElem.addAttribute("name", subsection.getTitle());

						if(subsection.getLines() != null)
						{
							for(final String line: subsection.getLines())
							{
								subsectionElem.addElement("line").addText(line);
							}
						}
					}
				}
			}
		}

		if(journal != null)
		{
			for(final JournalLine line: journal)
			{
				final Element journalElement = journalsElement.addElement("journal");

				journalElement.addElement("date").setText(df.format(line.getDate()));
				journalElement.addElement("status").setText(line.getStatus().toString());
				journalElement.addElement("operation").setText(line.getOperation().toString());
				journalElement.addElement("type").setText(line.getObjectType().toString());
				journalElement.addElement("path").setText(line.getPath());

				if(line.getId() != null)
				{
					journalElement.addElement("id").setText(line.getId());
				}
				if(line.getOperation().equals(Operation.UPDATE))
		    	{
					journalElement.addElement("updateOperation").setText(line.getUpdateOperation().toString());
		    	}
		    	if(line.getParameters() != null && line.getParameters().size() > 0)
		    	{
		    		Element parameterRoot = journalElement.addElement("parameters");
		    		for(Map.Entry<Parameter, Serializable> entry : line.getParameters().entrySet())
		    		{
		    			Element parameter = parameterRoot.addElement("parameter");
		    			parameter.addElement("key").setText((entry.getKey() != null) ? entry.getKey().toString() : "UNKNOW");
		    			parameter.addElement("value").setText((entry.getValue() != null) ? entry.getValue().toString() : "");
		    		}
		    	}
			}
		}

		//return new ByteArrayInputStream(document.asXML().getBytes());
		return document;
	}

	private void manageException(final NodeRef resource, final Throwable e) throws ImportationException
	{
		if(e instanceof ImportationException)
		{
			// should be already logged
			throw (ImportationException) e;
		}
		if(e instanceof JAXBException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Resource binding failure: Messages: " + e.getMessage());
			}

			throw new ImportationException("Error during the binding of the node " + resource, e);
		}
		else if(e instanceof SAXException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Resource parsing failure: Messages: " + e.getMessage());
			}

			throw new ImportationException("Error during parsing content noderf " + resource, e);
		}
		else if(e instanceof ValidationException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Resource validation failure: Messages: " + ((ValidationException)e).getMessages());
			}

			throw new ImportationException("Validation of content noderef  " + resource + " failure. ", e);
		}
		else if(e instanceof ContentIOException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Impossible to write the resource into the repository: " + e.getMessage());
			}

			throw new ImportationException("Impossible to write resource into the repository node " + resource + " failure", e);
		}
		else if(e instanceof IOException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Impossible to open the resource : " + e.getMessage());
			}

			throw new ImportationException("Opening content noderef  " + resource + " failure", e);
		}
		else if(e instanceof LockException)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("Locking error : " + e.getMessage());
			}

			throw new ImportationException("The locking failure." , e);
		}
		else
		{
			if(logger.isErrorEnabled())
			{
				logger.error("Unexpected error", e);
			}

			throw new ImportationException("Unexpected error during validating node " + resource, e);
		}
	}


	private final <T> T buildProcessor(final String className) throws ImportationException
	{
		try
		{
			return ReflexionUtils.<T>buildValidator(className);
		}
		catch(Exception e)
		{
			throw new ImportationException(e.getMessage());
		}
	}

	private boolean isFirstImport(final MigrationTracer<ImportRoot> journal)
	{
		return journal.getIteration().getTransformationDates().size() == 1;
	}


	/**
	 * @param validationHandler the validationHandler to set
	 */
	public final void setValidationHandler(final ValidationHandler validationHandler)
	{
		this.validationHandler = validationHandler;
	}

	/**
	 * @param fileArchiver the fileArchiver to set
	 */
	public final void setFileArchiver(final FileArchiver fileArchiver)
	{
		this.fileArchiver = fileArchiver;
	}

	/**
	 * @param lockHandler the lockHandler to set
	 */
	public final void setLockHandler(final LockHandler lockHandler)
	{
		this.lockHandler = lockHandler;
	}

	/**
	 * @param postProcessors the postProcessors to set
	 */
	public final void setPostProcessors(final List<String> postProcessors)
	{
		this.postProcessors = postProcessors;
	}

	/**
	 * @param preProcessors the preProcessors to set
	 */
	public final void setPreProcessors(final List<String> preProcessors)
	{
		this.preProcessors = preProcessors;
	}

	/**
	 * @param dryRunProcessors the dryRunProcessors to set
	 */
	public final void setDryRunProcessors(final List<String> dryRunProcessors)
	{
		this.dryRunProcessors = dryRunProcessors;
	}

	/**
	 * @param runProcessors the runProcessors to set
	 */
	public final void setRunProcessors(final List<String> runProcessors)
	{
		this.runProcessors = runProcessors;
	}

	public final void setTaskExecutor(TaskExecutor taskExecutor)
	{
		this.taskExecutor = taskExecutor;
	}

	public final void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public final void setServiceRegistry(CircabcServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * @param failOnError the failOnError to set
	 */
	public final void setFailOnError(boolean failOnError)
	{
		this.failOnError = failOnError;
	}

}
