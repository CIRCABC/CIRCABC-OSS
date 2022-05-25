/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.ValidationException;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.AsynchJobListeners;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportStatisticsService;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.service.migration.RootStat;
import eu.cec.digit.circabc.service.migration.jobs.IterationExportStatus;
import eu.cec.digit.circabc.service.migration.jobs.IterationImportStatus;
import eu.cec.digit.circabc.service.migration.jobs.MigrationStatusBase;
import eu.cec.digit.circabc.service.migration.jobs.PlannedMigrationService;
import eu.cec.digit.circabc.service.migration.jobs.PlannificationException;
import eu.cec.digit.circabc.service.migration.jobs.StatisticExportStatus;
import eu.cec.digit.circabc.service.migration.jobs.UserExportStatus;

/**
 * Service in charge to user exportation
 *
 * @author Yanick Pignot
 */
public class PlannedMigrationServiceImpl implements PlannedMigrationService
{
	private static final Log logger = LogFactory.getLog(PlannedMigrationServiceImpl.class);

	private static final CategoryInterestGroupPair ALL_IGS = new CategoryInterestGroupPair("__ALL_CAT___", "__ALL_IG___");

	private enum OperationType
	{
		ITERATION_IMPORT,
		ITERATION_EXPORT,
		USER_EXPORT,
		STATISTIC
	}
	private final static String MAIL_STR_SEPARATOR = ", ";

	private final static List<String> FORBIDDEN_MAIL_CARS = Arrays.asList(new String[]{MAIL_STR_SEPARATOR, "&", "|"});

	private FileArchiver archiver;
	private ExportStatisticsService exportStatisticsService;

	public UserExportStatus registerUserExportation(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final List<String> notificationEmails) throws PlannificationException
	{
		return registerUserExportation(new Date(), uniqueId, emails, conjunction, negation, notificationEmails);
	}

	public UserExportStatus registerUserExportation(final Date date, final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final List<String> notificationEmails)  throws PlannificationException
	{
		ParameterCheck.mandatory("The fire date", date);
		ParameterCheck.mandatoryString("The identifier" , uniqueId);

		if(isIdentifierAlreadyInUse(OperationType.USER_EXPORT, uniqueId))
		{
			throw new PlannificationException("Unique Identifier " + uniqueId + " already in use");
		}

		final UserExportStatus status = new UserExportStatus(uniqueId, date, asString(emails), conjunction, negation, notificationEmails);

		try
		{
			archiver.addPlannedUserExport(uniqueId, status);
		}
		catch (ArchiveException e)
		{
			throw new PlannificationException("Error registring process " + uniqueId + ".");
		}


		return status;
	}

	public IterationExportStatus registerIterationExportation(final String uniqueId, final String iterationName, final String iterationDescription, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException
	{
		return registerIterationExportation(new Date(), uniqueId, iterationName, iterationDescription, pairs, notificationEmails);
	}

	public IterationExportStatus registerIterationExportation(final Date date, final String uniqueId, final String iterationName, final String iterationDescription, final List<CategoryInterestGroupPair> pairs, final List<String> notificationEmails) throws PlannificationException
	{
		ParameterCheck.mandatory("The fire date", date);
		ParameterCheck.mandatoryString("The identifier" , uniqueId);

		if(isIdentifierAlreadyInUse(OperationType.ITERATION_EXPORT, uniqueId))
		{
			throw new PlannificationException("Unique Identifier " + uniqueId + " already in use");
		}

		final IterationExportStatus status = new IterationExportStatus(uniqueId, date, iterationName, iterationDescription, pairs, notificationEmails);

		try
		{
			archiver.addPlannedIterationExport(uniqueId, status);
		}
		catch (ArchiveException e)
		{
			throw new PlannificationException("Error registring process " + uniqueId + ".");
		}


		return status;
	}

	public IterationImportStatus registerIterationImportation(final String uniqueId, final String iterationName, final List<String> notificationEmails) throws PlannificationException
	{
		return registerIterationImportation(new Date(), uniqueId, iterationName, notificationEmails);
	}

	public IterationImportStatus registerIterationImportation(final Date date, final String uniqueId, final String iterationName, final List<String> notificationEmails) throws PlannificationException
	{
		ParameterCheck.mandatory("The fire date", date);
		ParameterCheck.mandatoryString("The identifier" , uniqueId);

		if(isIdentifierAlreadyInUse(OperationType.ITERATION_IMPORT, uniqueId))
		{
			throw new PlannificationException("Unique Identifier " + uniqueId + " already in use");
		}

		final IterationImportStatus status = new IterationImportStatus(uniqueId, date, iterationName, notificationEmails);

		try
		{
			archiver.addPlannedIterationImport(uniqueId, status);
		}
		catch (ArchiveException e)
		{
			throw new PlannificationException("Error registring process " + uniqueId + ".", e);
		}


		return status;
	}

	public StatisticExportStatus registerStatisticExportation(String uniqueId, List<CategoryInterestGroupPair> pairs, List<String> notificationEmails) throws PlannificationException
	{
		return registerStatisticExportation(new Date(), uniqueId, pairs, notificationEmails);
	}

	public StatisticExportStatus registerStatisticExportation(String uniqueId, List<String> notificationEmails) throws PlannificationException
	{
		return registerStatisticExportation(new Date(), uniqueId, notificationEmails);
	}

	public StatisticExportStatus registerStatisticExportation(Date date, String uniqueId, List<String> notificationEmails) throws PlannificationException
	{
		return registerStatisticExportation(date, uniqueId, Collections.singletonList(ALL_IGS), notificationEmails);
	}

	public StatisticExportStatus registerStatisticExportation(Date date, String uniqueId, List<CategoryInterestGroupPair> pairs, List<String> notificationEmails) throws PlannificationException
	{
		ParameterCheck.mandatory("The fire date", date);
		ParameterCheck.mandatoryString("The identifier" , uniqueId);

		if(isIdentifierAlreadyInUse(OperationType.STATISTIC, uniqueId))
		{
			throw new PlannificationException("Unique Identifier " + uniqueId + " already in use");
		}

		final StatisticExportStatus status = new StatisticExportStatus(uniqueId, date, pairs, notificationEmails);

		try
		{
			archiver.addPlannedExportStatistics(uniqueId, status);
		}
		catch (ArchiveException e)
		{
			throw new PlannificationException("Error registring process " + uniqueId + ".");
		}


		return status;
	}

	public List<UserExportStatus> getRegistredUserExportation() throws PlannificationException
	{
		final Map<Object, Object> records = getPlannedOperations(OperationType.USER_EXPORT);

		final List<UserExportStatus> statuses = new ArrayList<UserExportStatus>(records.size());

		try
		{
			for(final Object value: records.values())
			{
				statuses.add(UserExportStatus.buildFromString(value.toString()));
			}
		}
		catch (ParseException e)
		{
			throw new PlannificationException("The persited registration file is invalid.", e);
		}

		return statuses;
	}

	public List<IterationExportStatus> getRegistredIterationExportation() throws PlannificationException
	{
		final Map<Object, Object> records = getPlannedOperations(OperationType.ITERATION_EXPORT);

		final List<IterationExportStatus> statuses = new ArrayList<IterationExportStatus>(records.size());

		try
		{
			for(final Object value: records.values())
			{
				statuses.add(IterationExportStatus.buildFromString(value.toString()));
			}
		}
		catch (ParseException e)
		{
			throw new PlannificationException("The persited registration file is invalid.", e);
		}

		return statuses;
	}

	public List<IterationImportStatus> getRegistredIterationImportation() throws PlannificationException
	{
		final Map<Object, Object> records = getPlannedOperations(OperationType.ITERATION_IMPORT);

		final List<IterationImportStatus> statuses = new ArrayList<IterationImportStatus>(records.size());

		try
		{
			for(final Object value: records.values())
			{
				statuses.add(IterationImportStatus.buildFromString(value.toString()));
			}
		}
		catch (ParseException e)
		{
			throw new PlannificationException("The persited registration file is invalid.", e);
		}

		return statuses;
	}

	public List<StatisticExportStatus> getRegistredExportationStatistics() throws PlannificationException
	{
		final Map<Object, Object> records = getPlannedOperations(OperationType.STATISTIC);

		final List<StatisticExportStatus> statuses = new ArrayList<StatisticExportStatus>(records.size());

		try
		{
			for(final Object value: records.values())
			{
				statuses.add(StatisticExportStatus.buildFromString(value.toString()));
			}
		}
		catch (ParseException e)
		{
			throw new PlannificationException("The persited registration file is invalid.", e);
		}

		return statuses;
	}


	public void fire(final UserExportStatus status, final AidaMigrationService aidaMigrationService)
	{
		ParameterCheck.mandatory("An user export status ", status);
		ParameterCheck.mandatory("The aida service implementation ", aidaMigrationService);

		try
		{
			aidaMigrationService.asynchExportPersons(status.getIdentifier(),
					fromString(status.getQuery()), status.getConjunction(), status.getNegation(),
					new BeforeRun(status, OperationType.USER_EXPORT, archiver),
					new AfterUserExport(status, archiver));

		}
		catch(final Throwable t)
		{
			if(logger.isErrorEnabled()) {
				logger.error("Error during fire User Export '" + status.getIdentifier() + "': " + t.getMessage(), t);
			}
		}
	}

	public void fire(final IterationExportStatus status, final ExportService exportService, final AidaMigrationService aidaMigrationService)
	{
		ParameterCheck.mandatory("An export status ", status);
		ParameterCheck.mandatory("The export service implementation ", exportService);

		try
		{
			exportService.asynchRunExport(status.getInterestGroups(),
					status.getIterationName(), status.getIterationDescription(),
					new BeforeRun(status, OperationType.ITERATION_EXPORT, archiver),
					new AfterIterationExport(status, archiver, aidaMigrationService));
		}
		catch(final Throwable t)
		{
			if(logger.isErrorEnabled()) {
				logger.error("Error during fire Exportation '" + status.getIdentifier() + "': " + t.getMessage(), t);
			}
		}
	}

	public void fire(final IterationImportStatus status, final ImportService importService)
	{
		ParameterCheck.mandatory("An import status ", status);
		ParameterCheck.mandatory("The import service implementation ", importService);

		try
		{
			importService.asynchRun(status.getIterationName(),
					new BeforeRun(status, OperationType.ITERATION_IMPORT, archiver),
					new AfterIterationImport(status, archiver));

		}
		catch(final Throwable t)
		{
			if(logger.isErrorEnabled()) {
				logger.error("Error during fire Importation '" + status.getIdentifier() + "': " + t.getMessage(), t);
			}
		}
	}

	public void fire(final StatisticExportStatus status, final ExportService exportService)
	{
		ParameterCheck.mandatory("An import status ", status);
		ParameterCheck.mandatory("The export service implementation ", exportService);

		try
		{
			final List<CategoryInterestGroupPair> interestGroups = status.getInterestGroups();
			if(interestGroups.size() == 1 && interestGroups.get(0).equals(ALL_IGS))
			{
				exportStatisticsService.asynchGetAllStatistics(exportService,
						status.getIdentifier(),
						new BeforeRun(status, OperationType.STATISTIC, archiver),
						new AfterStatistics(status, archiver));
			}
			else
			{
				exportStatisticsService.asynchGetStatistics(exportService,
						status.getIdentifier(),
						interestGroups,
						new BeforeRun(status, OperationType.STATISTIC, archiver),
						new AfterStatistics(status, archiver));
			}

		}
		catch(final Throwable t)
		{
			if(logger.isErrorEnabled()) {
				logger.error("Error during fire Statistic Export '" + status.getIdentifier() + "': " + t.getMessage(), t);
			}
		}
	}

	//-----
	// helpers

	private boolean isIdentifierAlreadyInUse(final OperationType type, final String uniqueId) throws PlannificationException
	{
		return getPlannedOperations(type).containsKey(uniqueId);
	}

	private Map<Object, Object> getPlannedOperations(final OperationType type) throws PlannificationException
	{
		try
		{
			final Map<Object, Object> objects;
			switch (type)
			{
				case ITERATION_EXPORT:
					objects = archiver.retreivePlannedIterationExport();
					break;
				case ITERATION_IMPORT:
					objects = archiver.retreivePlannedIterationImport();
					break;
				case USER_EXPORT:
					objects =  archiver.retreivePlannedUserExport();
					break;
				case STATISTIC:
					objects =  archiver.retreivePlannedStatisticExports();
					break;
				default:
					objects = null;
			}

			return objects;
		}
		catch (ArchiveException e)
		{
			throw new PlannificationException(e.toString(), e);
		}
	}



	//---
	// IOc


	/**
	 * @param archiver the archiver to set
	 */
	public final void setFileArchiver(final FileArchiver archiver)
	{
		this.archiver = archiver;
	}

	/**
	 * @param exportStatisticsService the exportStatisticsService to set
	 */
	public final void setExportStatisticsService(ExportStatisticsService exportStatisticsService)
	{
		this.exportStatisticsService = exportStatisticsService;
	}

	private String asString(final List<String> mails) throws PlannificationException
	{
		final StringBuilder builder = new StringBuilder("");

		boolean first = true;

		for(final String mail: mails)
		{
			for(final String badCar: FORBIDDEN_MAIL_CARS)
			{
				if(mail.contains(badCar))
				{
					throw new PlannificationException("The email " + mail + " can't contins any of these cars: " + FORBIDDEN_MAIL_CARS);
				}
			}

			if(first == false)
			{
				builder.append(MAIL_STR_SEPARATOR);
			}
			else
			{
				first = false;
			}

			builder.append(mail);
		}

		return builder.toString();
	}

	private List<String> fromString(final String mailsAsString) throws ImportationException
	{
		final StringTokenizer tokens = new StringTokenizer(mailsAsString, MAIL_STR_SEPARATOR, false);

		final List<String> mails = new ArrayList<String>(tokens.countTokens());

		while(tokens.hasMoreTokens())
		{
			mails.add(tokens.nextToken());
		}

		return mails;
	}


	static abstract class AfterProcessBase
	{
		private final List<String> notificationEmails;
		private final MigrationStatusBase status;
		private final FileArchiver archiver;

		private AfterProcessBase(final MigrationStatusBase status, final FileArchiver archiver)
		{
			this.status = status;
			this.archiver = archiver;
			this.notificationEmails = status.getNotificationEmails();

			status.setRealFire(new Date());
			saveStatus();
		}

		protected void recordSuccess(final NodeRef contentNodeRef, final int items)
		{
			status.setNumberOfItems(items);
			status.setXmlFile(contentNodeRef);
			status.setEndDate(new Date());

			saveStatus();
		}

		protected void recordError(final Exception error)
		{
			status.setEndDate(new Date());

			final StringBuffer message = new StringBuffer("");

			message
				.append("Message: ")
				.append(error.getMessage())
				.append(".")
				.append("Cause: ");

			if(error.getCause() != null)
			{
				message
					.append(error.getCause().getCause())
					.append(".");
			}
			else
			{
				message.append("Undefined.");
			}
			status.setErrorMessage(message.toString());

			saveStatus();
		}

		protected void notify(final MailService mailService, final String subject, final Throwable error)
		{
			String body;

	        final Throwable cause = error.getCause();
	        if(cause != null && cause instanceof ValidationException)
	        {
	            body = cause.getMessage();
	        }
	        else
	        {
	            final Writer result = new StringWriter();
	            final PrintWriter printWriter = new PrintWriter(result);

	            error.printStackTrace(printWriter);

	            body = result.toString();
	        }

	        notify(mailService, subject, body);
		}

		protected void notify(final MailService mailService, final String subject, final String body)
		{
			if(notificationEmails != null && notificationEmails.size() > 0)
			{
				 try
                 {
					mailService.send(
							 mailService.getNoReplyEmailAddress(), notificationEmails, null,
							 subject + " " + status.getIdentifier(),
							 "<b><i>Process duration (ms): " +  status.getProcessTimeMillis() + "</i></b><hr />" + body,
							 true, false);
                 }
                 catch (MessagingException e)
                 {
                	 if(logger.isWarnEnabled())
                	 {
                		 logger.warn("Impossible to send emails to " + notificationEmails, e);
                	 }
                 }
			}
		}

		protected void saveStatus()
		{
			try
			{
				saveStatusImpl(archiver, status.getIdentifier(), status);
			}
			catch (ArchiveException e)
			{
				if(logger.isWarnEnabled())
				{
					logger.warn("Impossible to save state of the status " + status.getIdentifier(), e);
				}
			}
		}

		protected abstract void saveStatusImpl(final FileArchiver archiver, final String identifier, final MigrationStatusBase status) throws ArchiveException;

		/**
		 * @return the status
		 */
		protected final MigrationStatusBase getStatus()
		{
			return status;
		}

		protected String getDispalyPath(final CircabcServiceRegistry registry, final NodeRef nodeRef)
		{
			final NodeService nodeService = registry.getAlfrescoServiceRegistry().getNodeService();
			final PermissionService permissionService = registry.getAlfrescoServiceRegistry().getPermissionService();

			return nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService);
		}
	}

	static class AfterUserExport extends AfterProcessBase implements AsynchJobListeners.AfterRunJob<Users>
	{
		private AfterUserExport(final UserExportStatus status, final FileArchiver archiver)
		{
			super(status, archiver);
		}

		public void fail(final CircabcServiceRegistry registry, final Exception exception)
		{
			recordError(exception);
			notify(registry.getMailService(), "[FAILURE User export]", exception);
		}

		public void success(final CircabcServiceRegistry registry, final MigrationTracer<Users> tracer)
		{
			final int users =  tracer.getUnmarshalledObject().getUsers().size();

			recordSuccess(tracer.getMarshalledFile(), users);
			notify(registry.getMailService(),
					"[SUCCESS User export]",
					users + " users has been exported in the file " + getDispalyPath(registry, tracer.getMarshalledFile()));
		}

		@Override
		protected void saveStatusImpl(final FileArchiver archiver, final String identifier, final MigrationStatusBase status) throws ArchiveException
		{
			archiver.addPlannedUserExport(identifier, status);
		}
	}

	static class AfterIterationExport extends AfterProcessBase implements AsynchJobListeners.AfterRunJob<ImportRoot>
	{
		final AidaMigrationService aidaService;
		private AfterIterationExport(final IterationExportStatus status, final FileArchiver archiver, final AidaMigrationService aidaService)
		{
			super(status, archiver);
			this.aidaService = aidaService;
		}

		public void fail(final CircabcServiceRegistry registry, final Exception exception)
		{
			recordError(exception);
			notify(registry.getMailService(), "[Iteration export failure]", exception);
		}

		public void success(final CircabcServiceRegistry registry, final MigrationTracer<ImportRoot> tracer)
		{
			final int igs = ((IterationExportStatus)getStatus()).getInterestGroups().size();

			final NodeService nodeService = registry.getAlfrescoServiceRegistry().getNodeService();
			final NodeRef fileParent = nodeService.getPrimaryParent(tracer.getMarshalledFile()).getParentRef();
			recordSuccess(fileParent, igs);

			notify(registry.getMailService(),
					"[SUCCESS Iteration export]",
					igs + " interest groups exported in the file " + getDispalyPath(registry, tracer.getMarshalledFile()));

			if(this.aidaService != null)
			{
				try
				{
					final MigrationTracer<Users> aidaTracer = aidaService.exportPersons(tracer.getUnmarshalledObject(), tracer.getIteration());

					final Users users = aidaTracer.getUnmarshalledObject();

					notify(registry.getMailService(),
							"[SUCCESS Aida user export]",
							(users == null ? 0 : users.getUsers().size()) + " users exported in the file " + getDispalyPath(registry, aidaTracer.getMarshalledFile()));

				}
				catch(final Throwable t)
				{
					notify(registry.getMailService(), "[Aida user export failure]", t);
				}
			}

		}

		@Override
		protected void saveStatusImpl(final FileArchiver archiver, final String identifier, final MigrationStatusBase status) throws ArchiveException
		{
			archiver.addPlannedIterationExport(identifier, status);
		}

	}

	static class AfterIterationImport extends AfterProcessBase implements AsynchJobListeners.AfterRunJob<ImportRoot>
	{
		private AfterIterationImport(final IterationImportStatus status, final FileArchiver archiver)
		{
			super(status, archiver);
		}

		public void fail(final CircabcServiceRegistry registry, final Exception exception)
		{
			recordError(exception);
			notify(registry.getMailService(), "[FAILURE Iteration import]", exception);
		}

		public void success(final CircabcServiceRegistry registry, final MigrationTracer<ImportRoot> tracer)
		{
			final int count = tracer.getInterestGroups().size();

			recordSuccess(tracer.getLogFile(), count);
			notify(registry.getMailService(),
					"[SUCCESS Iteration import]",
					count + " interest groups imported and the full log files can be found under " + getDispalyPath(registry, tracer.getLogFile()) + getErrorMessage(tracer));
		}

		private List<JournalLine> getErrors(final MigrationTracer<ImportRoot>  tracer)
	    {
	        final List<JournalLine> errors = new ArrayList<JournalLine>();
	        for(JournalLine line : tracer.getJournal())
	        {
	        	if(line.getStatus() == Status.FAIL)
	            {
	        		errors.add(line);
	            }
	        }

	        return errors;
	    }

	    private String getErrorMessage(final MigrationTracer<ImportRoot>  tracer)
	    {
	    	final List<JournalLine> errors = getErrors(tracer);
	    	if(errors.size() < 1)
	    	{
	    		return "<br><hr><b><u>With no error</u></b>";
	    	}
	    	else
	    	{
	    		final StringBuilder builder = new StringBuilder("<br><hr><b><u>With ")
	    			.append(errors.size())
	    			.append(" errors</u></b><br /><br />");

	    		for(JournalLine line: errors)
	    		{
	    			builder.append(line.toString());
	    		}

	    		return builder.toString();
	    	}
	    }

	    @Override
		protected void saveStatusImpl(final FileArchiver archiver, final String identifier, final MigrationStatusBase status) throws ArchiveException
		{
			archiver.addPlannedIterationImport(identifier, status);
		}
	}

	static class AfterStatistics extends AfterProcessBase implements AsynchJobListeners.AfterRunJob<RootStat>
	{
		private AfterStatistics(final StatisticExportStatus status, final FileArchiver archiver)
		{
			super(status, archiver);
		}

		public void fail(final CircabcServiceRegistry registry, final Exception exception)
		{
			recordError(exception);
			notify(registry.getMailService(), "[FAILURE Exportation statistics]", exception);
		}

		public void success(final CircabcServiceRegistry registry, final MigrationTracer<RootStat> tracer)
		{
			final int count = tracer.getInterestGroups().size();

			recordSuccess(tracer.getLogFile(), count);
			notify(registry.getMailService(),
					"[SUCCESS Exportation statistics]",
					"Statistics performed on " +   count + " interest groups. The result files can be found under " + getDispalyPath(registry, tracer.getLogFile()));
		}

	    @Override
		protected void saveStatusImpl(final FileArchiver archiver, final String identifier, final MigrationStatusBase status) throws ArchiveException
		{
			archiver.addPlannedExportStatistics(identifier, status);
		}
	}

	static class BeforeRun implements AsynchJobListeners.BeforeRunJob
	{
		final MigrationStatusBase status;
		final OperationType type;
		final FileArchiver archiver;

		private BeforeRun(final MigrationStatusBase status, final OperationType type, final FileArchiver archiver)
		{
			super();
			this.status = status;
			this.type = type;
			this.archiver = archiver;
		}

		public void start(CircabcServiceRegistry registry)
		{
			if(status.getRealFire() == null)
			{
				status.setRealFire(new Date());
			}

			try
			{
				switch (type)
				{
					case ITERATION_EXPORT:
						archiver.addPlannedIterationExport(status.getIdentifier(), status);
						break;
					case ITERATION_IMPORT:
						archiver.addPlannedIterationImport(status.getIdentifier(), status);
						break;
					case USER_EXPORT:
						archiver.addPlannedUserExport(status.getIdentifier(), status);
						break;
					case STATISTIC:
						archiver.addPlannedExportStatistics(status.getIdentifier(), status);
						break;
				}
			}
			catch (ArchiveException ignore){}
		}
	}
}
