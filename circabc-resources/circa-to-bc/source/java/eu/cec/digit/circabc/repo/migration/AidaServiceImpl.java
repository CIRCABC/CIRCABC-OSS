/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.AidaException;
import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.AsynchJobListeners;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;
/**
 * Concrete implementation of the Aida migration Service
 *
 * @author Yanick Pignot
 */
public class AidaServiceImpl implements AidaMigrationService
{
	private static final Log logger = LogFactory.getLog(AidaServiceImpl.class);

	private static final String ITERATION_DESCRIPTION = "All generated bulk user exports.";
	private static final String ITERATION_NAME = "__aida_bulk_exports";

	private final List<UserConverter> userConverters = new ArrayList<UserConverter>();
	private final List<UserVoter> userVoters = new ArrayList<UserVoter>();

	private TransactionService transactionService;
    private TaskExecutor taskExecutor;
    private CircabcServiceRegistry serviceRegistry;
	private FileArchiver fileArchiver;
	private UserReader userReader;
	private NodeService nodeService;
	private FileFolderService fileFolderService;

	private String implementationName;

	private Map<String, MigrationTracer<Users>> runningJournals = new HashMap<String, MigrationTracer<Users>>();

	public void init()
	{
		fileArchiver.registerMandatoryNode(ContentModel.TYPE_FOLDER, ITERATION_NAME);
	}

	public MigrationTracer<Users> exportPersons(final ImportRoot importRoot, final MigrationIteration iteration) throws AidaException
	{
		final List<CategoryInterestGroupPair> pairs = new ArrayList<CategoryInterestGroupPair>();

		for(final CategoryHeader header: importRoot.getCircabc().getCategoryHeaders())
		{
			for(final Category cat: header.getCategories())
			{
				for(final InterestGroup ig: cat.getInterestGroups())
				{
					pairs.add(new CategoryInterestGroupPair((String) cat.getName().getValue(), (String) ig.getName().getValue()));
				}
			}
		}

		final MigrationTracer<Users> tracer = new MigrationTracer<Users>(getImplementationName(), false);
		tracer.setInterestGroups(pairs);
		tracer.setReadOnly(true);
		tracer.setIteration(iteration);

		final Persons personsRoot = importRoot.getPersons();
		final List<Person> persons = (personsRoot == null) ? null : personsRoot.getPersons();

		adaptPersons(tracer, persons, true, true);

		tracer.setRunningPhase("Ending: store the generated file");

		try
		{
			final NodeRef file = fileArchiver.storeExportFile(iteration, getImplementationName() + "_user_import_file.xml", JavaXmlBinder.marshallInStream(tracer.getUnmarshalledObject()));
			tracer.setMarshalledFile(file);
		}
		catch (ArchiveException e)
		{
			throw new AidaException("Error persisting the aida file: " + e.getMessage(), e);
		}
		catch (JAXBException e)
		{
			throw new AidaException("Error marshalling the aida users to a file: " + e.getMessage(), e);
		}

		return tracer;
	}

	public Set<String> getRunningExportations()
	{
		return runningJournals.keySet();
	}

	public MigrationTracer<Users> getRunningExportJournal(final String uniqueId)
	{
		return runningJournals.get(uniqueId);
	}

	/**
	 * @param tracer
	 * @param persons
	 * @throws AidaException
	 */
	private void adaptPersons(final MigrationTracer<Users> tracer, final List<Person> persons, final boolean convert, final boolean vote) throws AidaException
	{
		final List<User> users = new ArrayList<User>();
		tracer.setUnmarshalledObject(new Users(users));

		User user;
		String uid;
		if(persons != null)
		{
			tracer.setRunningPhase("Reading persons details");

			for(final Person person: persons)
			{
				uid = (String) person.getUserId().getValue();
				user = new User(
						LdapHelper.removeDomainFromUid(uid),
						(String) person.getFirstName().getValue(),
						(String) person.getLastName().getValue(),
						(String) person.getEmail().getValue()
						);

				if(convert)
				{
					applyConvertes(user, uid);
				}

				final boolean add;
				if(vote)
				{
					add = applyVoters(user, uid);
				}
				else
				{
					add = true;
				}

				if(add)
				{
					users.add(user);
				}
			}
		}
	}

	/**
	 * @param user
	 * @param uid
	 * @param add
	 * @return
	 * @throws AidaException
	 */
	private boolean applyVoters(User user, String uid) throws AidaException
	{
		boolean add = true;
		for(final UserVoter userVoter : userVoters)
		{
			try
			{
				if(userVoter.addUser(user) == false)
				{
					if(logger.isDebugEnabled())
					{
					logger.debug("User " + uid + " not added due to the result of " + userVoter.getClass().getName() + ". Message: " + userVoter.getRejectMessage() );
					}
					add = false;
					break;
				}
				else
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("User Voter accpetion addition for " + uid + ": " + userVoter.getClass().getName());
					}
				}
			}
			catch(AidaException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new AidaException("Impossible to apply user voter for " + uid + ": " + userVoter.getClass().getName(), e);
			}
		}
		return add;
	}

	/**
	 * @param user
	 * @param uid
	 * @throws AidaException
	 */
	private void applyConvertes(User user, String uid) throws AidaException
	{
		for(final UserConverter userConverter : userConverters)
		{
			try
			{
				userConverter.convert(user);
			}
			catch(AidaException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new AidaException("Impossible to apply user converter for " + uid + ": " + userConverter.getClass().getName(), e);
			}

			if(logger.isDebugEnabled())
			{
				logger.debug("User Convertion performed for " + uid + ": " + userConverter.getClass().getName());
			}
		}
	}

	public MigrationTracer<Users> exportPersons(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation) throws AidaException
    {
		if(runningJournals.containsKey(uniqueId))
		{
			throw new AidaException("Impossible to launch user exportation " + uniqueId + " since it's already running");
		}

		final NodeRef folder = getExportFileLocation();
		final String cleanId = NameProperty.toValidName(uniqueId);
		if(nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, cleanId) != null)
		{
			throw new AidaException("Impossible to use id: " + cleanId + " since it is already used.");
		}

    	try
		{
    		final MigrationTracer<Users> tracer = new MigrationTracer<Users>(getImplementationName(), false);

    		runningJournals.put(uniqueId, tracer);

    		final List<Person> persons = userReader.getPersonsWithEmails(emails, conjunction, negation);

			tracer.setInterestGroups(Collections.<CategoryInterestGroupPair>emptyList());
			tracer.setReadOnly(true);

			adaptPersons(tracer, persons, true, false);
			final Users users = (Users) tracer.getUnmarshalledObject();
			tracer.setRunningPhase("Ending: store the generated file");

			try
			{

				final NodeRef contentNodeRef = JavaXmlBinder.marshallInNode(users, folder, cleanId + ".xml" , fileFolderService);

				tracer.setMarshalledFile(contentNodeRef);
			}
			catch (JAXBException e)
			{
				throw new AidaException("Error marshalling the aida users to a file: " + e.getMessage(), e);
			}

			return tracer;

		}
    	catch (ExportationException e)
		{
			throw new AidaException("Problem export persons: " + e.getMessage(), e);
		}
    	catch (Throwable t)
		{
    		throw new AidaException("Unexpected error when exporting persons: " + t.getMessage(), t);
		}
    	finally
		{
			runningJournals.remove(uniqueId);
		}
    }

	public void asynchExportPersons(final String uniqueId, final List<String> emails, final boolean conjunction, final boolean negation, final AsynchJobListeners.BeforeRunJob beforeRun, final AsynchJobListeners.AfterRunJob<Users> afterCommit)
    {
    	final AidaMigrationService aidaService = this;
    	final String username = AuthenticationUtil.getFullyAuthenticatedUser();

		taskExecutor.execute(new Runnable()
		{
			public void run()
			{
				AuthenticationUtil.runAs(new RunAsWork<Object>()
				{
					public Object doWork() throws Exception
					{
						final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
						return txnHelper.doInTransaction(new RetryingTransactionCallback<Object>()
						{
							public Object execute() throws Throwable
							{
								if(beforeRun != null)
								{
									beforeRun.start(serviceRegistry);
								}

								try
								{
									final MigrationTracer<Users> tracer = aidaService.exportPersons(uniqueId, emails, conjunction, negation);

									if(afterCommit != null)
									{
										afterCommit.success(serviceRegistry, tracer);
									}
								}
								catch (final AidaException e)
								{
									if(logger.isErrorEnabled())
									{
										logger.error("Error exporting: " + uniqueId, e);
									}

									if(afterCommit != null)
									{
										afterCommit.fail(serviceRegistry, e);
									}
								}
								return null;
							}
						});
					}
				}, username);
			}
		});
    }

	public void registerConverter(final UserConverter userConverter)
	{
		ParameterCheck.mandatory("The user converter", userConverter);

		if(logger.isDebugEnabled())
		{
			logger.debug("User Converter registred: " + userConverter.getClass().getName());
		}

		userConverters.add(userConverter);
	}

	public void registerVoter(final UserVoter userVoter)
	{
		ParameterCheck.mandatory("The user voter", userVoter);

		if(logger.isDebugEnabled())
		{
			logger.debug("User Voter registred: " + userVoter.getClass().getName());
		}

		userVoters.add(userVoter);
	}

	public NodeRef getExportFileLocation() throws AidaException
	{
		final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
		final MigrationIteration iteration = helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<MigrationIteration>()
		{
			public MigrationIteration execute() throws Throwable
			{
				MigrationIteration iteration = fileArchiver.getIterationByName(ITERATION_NAME);

				if(iteration == null)
				{
					iteration = fileArchiver.startNewIteration(ITERATION_NAME, ITERATION_DESCRIPTION);
				}
				return iteration;
			}
		});


		if(iteration == null)
		{
			throw new AidaException("Impossible to get iteration: " + ITERATION_NAME);
		}
		else if(iteration.getIterationRootSpace() == null)
		{
			throw new AidaException("Impossible to get the " + ITERATION_NAME + " root space");
		}

		return iteration.getIterationRootSpace();
	}

	/**
	 * @return the implementationName
	 */
	public final String getImplementationName()
	{
		return implementationName;
	}


	/**
	 * @param implementationName the implementationName to set
	 */
	public final void setImplementationName(String implementationName)
	{
		this.implementationName = implementationName;
	}


	/**
	 * @return the fileArchiver
	 */
	public final FileArchiver getFileArchiver()
	{
		return fileArchiver;
	}


	/**
	 * @param fileArchiver the fileArchiver to set
	 */
	public final void setFileArchiver(FileArchiver fileArchiver)
	{
		this.fileArchiver = fileArchiver;
	}

	/**
	 * @param serviceRegistry the serviceRegistry to set
	 */
	public final void setServiceRegistry(CircabcServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * @param taskExecutor the taskExecutor to set
	 */
	public final void setTaskExecutor(TaskExecutor taskExecutor)
	{
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param transactionService the transactionService to set
	 */
	public final void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	/**
	 * @param userReader the userReader to set
	 */
	public final void setUserReader(UserReader userReader)
	{
		this.userReader = userReader;
	}

	/**
	 * @param fileFolderService the fileFolderService to set
	 */
	public final void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
}
