/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.DuplicateIterationNameException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.Statistics;
import eu.cec.digit.circabc.migration.entities.generated.VersionHistory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Discussions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfSpace;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SimpleContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.migration.reader.LogFileReader;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.AsynchJobListeners;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concrete implementation of the Export Service to run against a Circa instance
 *
 * @author Yanick Pignot
 */
public class ExportServiceImpl implements ExportService
{
	private static final String VERSION_DESCRIPTION = "Created from a circa exportation.";
    public static final String STAT_NUMBER_OF_USER = "Number of Users";
    public static final String STAT_NUMBER_OF_CAT = "Number of Categories";
    public static final String STAT_NUMBER_OF_IG = "Number of Interest Groups";
    public static final String STAT_PROCESS_TIME_MINUTE = "Process duration in minute";

    private static final Log logger = LogFactory.getLog(ExportServiceImpl.class);

    private static final String LOG_FOUND_IG = "Interest groups found: ";
    private static final String LOG_FOUND_CAT = "Categories found: ";

    private TransactionService transactionService;
    private TaskExecutor taskExecutor;
    private CircabcServiceRegistry serviceRegistry;

    private RemoteFileReader libFileReader;
    private RemoteFileReader infFileReader;
    private SecurityReader securityReader;
    private MetadataReader metadataReader;
    private UserReader userReader;
    private CalendarReader calendarReader;
    private NewsgroupReader newsgroupReader;
    private LogFileReader logFileReader;

    private FileArchiver fileArchiver;
    private String  implementationName;
    

    private Map<String, MigrationTracer<ImportRoot>> runningJournals = new HashMap<String, MigrationTracer<ImportRoot>>();

    /*
     * Les dernieres connexions des utilisateurs CIRCA sont sauvegardees dans une table nommee � _user_access.db � situee dans le repertoire � www/logs/user_access/_user_access.db �.
     * (db_dump �p _user_access.db > dernlogin.txt)
     **/

    public List<String> getCategoryNames() throws ExportationException
    {
        List<String> names = new ArrayList<String>();
        try
        {
            for(String path : libFileReader.getCategoriesPath())
            {
                names.add(convertPathToName(path, LOG_FOUND_CAT));
            }
            Collections.sort(names);
        }
        catch (Exception e)
        {
            manageException(e);
        }

        return names;
    }

    public Set<CategoryInterestGroupPair> getInterestGroupDepedencies(final CategoryInterestGroupPair pair) throws ExportationException
    {
        // check shared spaces
        final Set<CategoryInterestGroupPair> dependencies = securityReader.getAllSharedLinkTarget(pair);

        // check imported profiles
        dependencies.addAll(securityReader.getAllImportedProfileTarget(pair));

        return dependencies;
    }

    public List<CategoryInterestGroupPair> getInterestGroups(final String categoryName) throws ExportationException
    {
        final List<CategoryInterestGroupPair> pairs;

        try
        {
            final String categoryPath = libFileReader.getCategoryPathWithName(categoryName);
            if(logger.isDebugEnabled())
            {
                logger.debug("Trying to retreive interest group from category: " + categoryName + " ... ");
            }

            String igName;

            pairs = new ArrayList<CategoryInterestGroupPair>();
            final List<String> interestGroupsPaths = libFileReader.getInterestGroupsPath(categoryPath);
            Collections.sort(interestGroupsPaths);
			for(String igPath: interestGroupsPaths)
            {
            	igName = convertPathToName(igPath, LOG_FOUND_IG);
                pairs.add(new CategoryInterestGroupPair(categoryName, igName));
            }
        }
        catch (Exception e)
        {
            manageException(e);
            return null;
        }

        return pairs;
    }

    public MigrationTracer<ImportRoot> runExport(final CategoryInterestGroupPair pair, final String iterationName, final String iterationDescription) throws ExportationException
    {
        Assert.notNull(pair, "The interest group / category pair is mandatory");
        return runExport(Collections.singletonList(pair), iterationName, iterationDescription);
    }

    public void asynchRunExport(final List<CategoryInterestGroupPair> pairs, final String iterationName, final String iterationDescription, final AsynchJobListeners.BeforeRunJob beforeRun, final AsynchJobListeners.AfterRunJob<ImportRoot> afterCommit) throws ExportationException
    {
    	final ExportServiceImpl expService = this;
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
								if(beforeRun != null)
								{
									beforeRun.start(serviceRegistry);
								}

								try
								{
									final MigrationIteration iteration = createIteration(iterationName, iterationDescription);
									final MigrationTracer<ImportRoot> tracer = expService.runExportImpl(pairs, iteration);

									if(afterCommit != null)
									{
										afterCommit.success(serviceRegistry, tracer);
									}
								}
								catch (final ExportationException e)
								{
									if(logger.isErrorEnabled())
									{
										logger.error("Error exporting: " + pairs.toString(), e);
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

    public MigrationTracer<ImportRoot> runExport(final List<CategoryInterestGroupPair> pairs, final String iterationName, final String iterationDescription) throws ExportationException
    {
        Assert.notEmpty(pairs, "No interest group to migrate");

        final MigrationIteration iteration = createIteration(iterationName, iterationDescription);
		return runExportImpl(pairs, iteration);

    }

	public Set<String> getRunningIterations()
	{
		return runningJournals.keySet();
	}

	public MigrationTracer<ImportRoot> getRunningIterationsJournal(final String iterationName)
	{
		return runningJournals.get(iterationName);
	}


	/**
	 * @param iterationName
	 * @param iterationDescription
	 * @return
	 * @throws ExportationException
	 */
	private MigrationIteration createIteration(final String iterationName, final String iterationDescription) throws ExportationException
	{
		MigrationIteration iteration = null;
		try
		{
			 iteration = fileArchiver.startNewIteration(iterationName, iterationDescription);
		}
		catch (ArchiveException e)
		{
			manageException(e);
		}
		catch (DuplicateIterationNameException e)
		{
			manageException(e);
		}
		return iteration;
	}

	/**
	 * @param pairs
	 * @param iteration
	 * @return
	 * @throws ExportationException
	 */
	private MigrationTracer<ImportRoot> runExportImpl(final List<CategoryInterestGroupPair> pairs, final MigrationIteration iteration) throws ExportationException
	{
		final String iterationName = iteration.getIdentifier();
		if(runningJournals.containsKey(iterationName))
		{
			throw new ExportationException("Impossible to launch exportation of the itaration " + iterationName + " since it's already running");
		}

		final MigrationTracer<ImportRoot> tracer = new MigrationTracer<ImportRoot>(getImplementationName(), false);
		tracer.setInterestGroups(pairs);
		tracer.setIteration(iteration);
		try
		{
			runningJournals.put(iterationName, tracer);

			final long startTime = System.currentTimeMillis();

	        final Circabc circabc = new Circabc();
	        final ImportRoot importRoot = new ImportRoot(circabc, new Persons(), new LogFile(), new VersionHistory(), new Statistics());
	        
	        
	        Properties igLogProps = new Properties();
			final NodeRef logsExportFileNodeRef = fileArchiver.storeLogsExportFile(iteration, igLogProps );
	        final OutputStream logsExportOutputStream = fileArchiver.getContentOutputStream(logsExportFileNodeRef);

	        tracer.setUnmarshalledObject(importRoot);
	        tracer.setRunningPhase("Reading root node details");
	        tracer.setReadOnly(true);

	        libFileReader.setNodePath(circabc);
	        metadataReader.setProperties(circabc);
	        securityReader.setPermission(circabc);

	        for(final CategoryInterestGroupPair pair: pairs)
            {
                Assert.hasText(pair.getCategory(), "The category name is mandatory");
                Assert.hasText(pair.getInterestGroup(), "The interest group name is mandatory");

                tracer.setRunningPhase("Reading " + pair.toString() + " node details");

                if(logger.isDebugEnabled())
                {
                    logger.debug("Trying to access interest group: " + pair.toString() + " ... ");
                }

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Start to export: " + pair.toString());
					logger.info("*********************************************************************************");
				}

                final String headerName = libFileReader.getCategoryHeaderName(pair.getCategory());

                if(headerName == null || headerName.length() < 1)
                {
                    throw new ExportationException("No category header found for the category '" + pair.getCategory() + "'. Process aborted" );
                }
                else if(logger.isDebugEnabled())
                {
                    logger.debug("Category header successfully getted: " + headerName + " for the interest group " + pair.toString());
                }

                final CategoryHeader header = BinderUtils.getOrInitNewCategoryHeader(circabc, headerName, libFileReader, logger);
                final Category category = BinderUtils.getOrInitNewCategory(header, pair.getCategory(), libFileReader, logger);
                final InterestGroup igRoot = BinderUtils.getOrInitNewInterestGroup(category, pair.getInterestGroup(), libFileReader, logger);

                long start = 0;
                if(logger.isInfoEnabled())
				{
					start = System.currentTimeMillis();
				}

                BinderUtils.addPersons(importRoot, userReader.getInvitedPersons(category), logger);
                BinderUtils.addPersons(importRoot, userReader.getInvitedPersons(igRoot), logger);

				if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Persons time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

				metadataReader.setProperties(header);
                metadataReader.setProperties(category);
                metadataReader.setProperties(igRoot);

                metadataReader.setProperties(igRoot.getInformation());
                metadataReader.setProperties(igRoot.getLibrary());
                metadataReader.setProperties(igRoot.getEvents());
                metadataReader.setProperties(igRoot.getNewsgroups());
                metadataReader.setProperties(igRoot.getDirectory());

                metadataReader.setDynamicPropertyDefinition(igRoot);
                metadataReader.setKeywordDefinition(igRoot);
                metadataReader.setIconsDefinition(igRoot);

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Root/Category/Ig properties + dyn prop + keywords time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                securityReader.setPermission(category);
                securityReader.setPermission(igRoot);
                securityReader.setProfileDefinition(igRoot);
                securityReader.setApplicants(importRoot, igRoot);

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Root/Category/Ig permissions time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Directory details");

                scanDirectory(igRoot.getDirectory());
                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Directory time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Library details");

                scanLibrary(igRoot.getLibrary());

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Library time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Information details");

                scanInformation(igRoot.getInformation());

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Information Service time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Events details");

                scanEvents(igRoot.getEvents());

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Events time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Newsgroup details");

                scanNewsgroup(igRoot.getNewsgroups());

                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Newsgroups time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");

					start = System.currentTimeMillis();
				}

                tracer.setRunningPhase("Reading " + pair.getInterestGroup() + " Log files");

                logFileReader.addLogEntries(igRoot,logsExportOutputStream);
                
                if(logger.isInfoEnabled())
				{
					logger.info("**********************************************************************************");
					logger.info("Reading Log Files time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
					logger.info("*********************************************************************************");
				}
            }

	        tracer.setRunningPhase("Ending: fill statistics and file version");

	        ElementsHelper.addVersion(importRoot, true, VERSION_DESCRIPTION);

	        ElementsHelper.addStatistics(importRoot, STAT_PROCESS_TIME_MINUTE, "" + (System.currentTimeMillis() - startTime) / 60000);
	        ElementsHelper.addStatistics(importRoot, STAT_NUMBER_OF_IG, "" + pairs.size());
	        ElementsHelper.addStatistics(importRoot, STAT_NUMBER_OF_CAT, "" + getCategories(pairs).size());
	        ElementsHelper.addStatistics(importRoot, STAT_NUMBER_OF_USER, "" + importRoot.getPersons().getPersons().size());

	        tracer.setRunningPhase("Ending: store the generated file");

	        

	        final NodeRef file = fileArchiver.storeOriginalExportFile(iteration, JavaXmlBinder.marshallInStream(importRoot));

        	// very dirty workaround, but JAXB is not scalable enough for the ig log files
        	// TODO change framework implementation !!

        	//fileArchiver.storeLogsExportFile(iteration, igLogProps);
	        logsExportOutputStream.flush();
	        logsExportOutputStream.close();

	        tracer.setMarshalledFile(file);
		}
		catch (Exception e)
		{
			if(logger.isErrorEnabled()) {
        		logger.error("An Error occur", e); 
			}
			manageException(e);
		}
		finally
		{
			runningJournals.remove(iterationName);
			fileArchiver.removeIterationIfEmpty(iteration);
		}

		tracer.setRunningPhase("END: Success");

        return tracer;
	}

    /**
     * Recusive scan of the InterestGroup's library service.
     *
     *  Protected for eventual extension / overridden facilities
     *
     * @param parentNode
     * @throws Exception
     */
    protected void scanLibrary(final XMLNode parentNode) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Library Service: Starting to scan: " + ElementsHelper.getExportationPath(parentNode));
        }

        final List<NamedNode> containers = new ArrayList<NamedNode>();
        for(final String childPath : libFileReader.listChidrenPath(ElementsHelper.getExportationPath(parentNode)))
        {
            if(libFileReader.isSpace(childPath))
            {
                final Space space = new Space();
                containers.add(space);

                if(logger.isDebugEnabled())
                {
                    logger.debug("New space created" );
                }

                setCommonProperties(parentNode, space, childPath, true);
                fillDiscussions(space);
                securityReader.setSharedDefinition(space);
            }
            else if(libFileReader.isDossier(childPath))
            {
                final List<String> dossierTranslations = libFileReader.getDossierTranslations(childPath);

                if(logger.isDebugEnabled())
                {
                    logger.debug("New dossier created with " + ((dossierTranslations.size() > 1) ? dossierTranslations.size() + " transations" : ""));
                }

                if(dossierTranslations.size() > 1 && logger.isWarnEnabled())
                {
                    logger.warn("Circabc doesn't support dossier translations. Several dossiers will be created.");
                }

                for(final String dossierPath: dossierTranslations)
                {
                    final Dossier dossier = new Dossier();
                    containers.add(dossier);

                    setCommonProperties(parentNode, dossier, dossierPath, true);
                    fillDiscussions(dossier);
                }

            }
            else if(libFileReader.isUrl(childPath))
            {
                for(final String urlPath: libFileReader.getUrlTranslations(childPath))
                {
                    final Url url = new Url();

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("New URL created" );
                    }

                    setCommonProperties(parentNode, url, urlPath, true);
                    fillDiscussions(url);
                }
            }
            else if(libFileReader.isDocument(childPath))
            {
                final List<Locale> languages = libFileReader.getContentTranslations(childPath);

                if(logger.isDebugEnabled())
                {
                    logger.debug("New content created" );
                    logger.debug("  ---  With number of translations: " + (languages.size() - 1));
                }

                if(languages.size() > 1)
                {
                    // the document is multiligual
                    final MlContent mlContent = new MlContent();
                    setCommonProperties(parentNode, mlContent, childPath, true);
                    fillDiscussions(mlContent);

                    for(final Locale locale: languages)
                    {
                        LibraryTranslation currentVersion = null;

                        final Map<VersionNumber, String> versions = libFileReader.getContentVersions(childPath, locale);
                        final List<VersionNumber> versionNumbers = new ArrayList<VersionNumber>(versions.size());
                        versionNumbers.addAll(versions.keySet());
                        Collections.sort(versionNumbers);

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("New " + locale.getLanguage() + " translation created" );
                            logger.debug("  ---  With number of versions: " + (versions.size() - 1));
                        }

                        for(int dec = versionNumbers.size() - 1; dec >= 0; --dec)
                        {
                            if(currentVersion == null)
                            {
                                currentVersion = new LibraryTranslation();
                                setCommonProperties(mlContent, currentVersion, versions.get(versionNumbers.get(dec)), false);
                                fillDiscussions(currentVersion);
                                if(versionNumbers.size() > 1)
                                {
                                    currentVersion.withVersions(new LibraryTranslationVersions());
                                }
                            }
                            else
                            {
                                final LibraryTranslationVersion oldVersion = new LibraryTranslationVersion();
                                currentVersion.getVersions().withVersions(oldVersion);
                                setCommonProperties(currentVersion, oldVersion, versions.get(versionNumbers.get(dec)), false);
                            }
                        }

                        mlContent.withTranslations(currentVersion);
                    }
                }
                else
                {
                    Content currentVersion = null;

                    final Map<VersionNumber, String> versions = libFileReader.getContentVersions(childPath, languages.get(0));
                    final List<VersionNumber> versionNumbers = new ArrayList<VersionNumber>(versions.size());
                    versionNumbers.addAll(versions.keySet());


                    Collections.sort(versionNumbers);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("  ---  With number of versions: " + (versions.size() - 1));
                    }

                    for(int dec = versionNumbers.size() - 1; dec >= 0; --dec)
                    {
                        if(currentVersion == null)
                        {
                            currentVersion = new Content();
                            setCommonProperties(parentNode, currentVersion, versions.get(versionNumbers.get(dec)), true);
                            fillDiscussions(currentVersion);
                            if(versionNumbers.size() > 1)
                            {
                                currentVersion.withVersions(new LibraryContentVersions());
                            }
                        }
                        else
                        {
                            final LibraryContentVersion oldVersion = new LibraryContentVersion();
                            currentVersion.getVersions().withVersions(oldVersion);
                            setCommonProperties(currentVersion, oldVersion, versions.get(versionNumbers.get(dec)), false);
                        }
                    }
                }

            }
            else if(libFileReader.isSharedSpaceLink(childPath))
            {
            	final SharedSpacelink link = new SharedSpacelink();

                if(logger.isDebugEnabled())
                {
                    logger.debug("Link to a share space" );
                }

                setCommonProperties(parentNode, link, childPath, true);
            }
            else if(libFileReader.isLink(childPath))
            {
                final Link link = new Link();

                if(logger.isDebugEnabled())
                {
                    logger.debug("New Link" );
                }


                setCommonProperties(parentNode, link, childPath, true);
            }
            else
            {
            	logger.warn("The file " + childPath + " is not recognized.");
            }
        }

        for(final NamedNode container: containers)
        {
            scanLibrary(container);
        }
    }

    private void setCommonProperties(final XMLNode parent, final XMLNode child, final String path, boolean addChild) throws ExportationException
    {
        if(addChild)
        {
            try
            {
                BinderUtils.addChild(parent, child, logger);
            }
            catch (Exception e)
            {
                throw new ExportationException("Impossible to attach " + child.getClass().getSimpleName() + " to its parent " + parent.getClass().getSimpleName(), e);
            }
        }
        ElementsHelper.setParent(parent, child);
        ElementsHelper.setExportationPath(child, path);
        metadataReader.setProperties(child);
        securityReader.setNotification(child);
        securityReader.setPermission(child);
    }

    private void fillDiscussions(final XMLNode node) throws ExportationException
    {
        final String exportationPath = ElementsHelper.getExportationPath(node);
        final String discussionPath = newsgroupReader.getDiscussions(exportationPath);

        if(discussionPath != null)
		{
			final Discussions discussions = new Discussions();
			setCommonProperties(node, discussions, discussionPath, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New discussion forum created: " + ElementsHelper.getExportationPath(discussions));
            }

			for(final String topicPath: newsgroupReader.listDiscussionsTopics(exportationPath))
			{
				final Topic topic = new Topic();
				setCommonProperties(discussions, topic, topicPath, true);

				if(logger.isDebugEnabled())
	            {
	                logger.debug("New discussion topic created: " + ElementsHelper.getExportationPath(topic));
	            }

				fillMessages(topicPath, topic);
			}

			if(node instanceof Content)
			{
				((Content)node).setDiscussions(discussions);
			}
			else if(node instanceof Dossier)
			{
				((Dossier)node).setDiscussions(discussions);
			}
			else if(node instanceof LibraryTranslation)
			{
				((LibraryTranslation)node).setDiscussions(discussions);
			}
			else if(node instanceof MlContent)
			{
				((MlContent)node).setDiscussions(discussions);
			}
			else if(node instanceof Space)
			{
				((Space)node).setDiscussions(discussions);
			}
			else if(node instanceof Url)
			{
				((Url)node).setDiscussions(discussions);
			}
			else if(logger.isWarnEnabled())
			{
				logger.warn("Circabc doesn't support discussions on node of type" + node.getClass());
			}
		}

    }

    /**
     * Recusive scan of the InterestGroup's information service.
     *
     *  Protected for eventual extension / overridden facilities
     *
     * @param parentNode
     * @throws Exception
     */
    protected void scanInformation(final XMLNode parentNode) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Information Service: Starting to scan: " + ElementsHelper.getExportationPath(parentNode));
        }

        final List<NamedNode> containers = new ArrayList<NamedNode>();
        for(final String childPath : infFileReader.listChidrenPath(ElementsHelper.getExportationPath(parentNode)))
        {
            if(infFileReader.isSpace(childPath))
            {
                final InfSpace space = new InfSpace();
                containers.add(space);

                if(logger.isDebugEnabled())
                {
                    logger.debug("New space created" );
                }

                setCommonProperties(parentNode, space, childPath, true);

            }
            else if(infFileReader.isDocument(childPath))
            {
                final List<Locale> translations = infFileReader.getContentTranslations(childPath);

                if(logger.isDebugEnabled())
                {
                    logger.debug("New content created" );
                    logger.debug("  ---  With number of translations: " + (translations.size() - 1));
                }

                if(translations.size() > 1)
                {
                    // the document is multiligual
                    final InfMLContent mlContent = new InfMLContent();
                    setCommonProperties(parentNode, mlContent, childPath, true);

                    for(final Locale locale: translations)
                    {
                        InformationTranslation currentVersion = null;

                        final Map<VersionNumber, String> versions = infFileReader.getContentVersions(childPath, locale);
                        final List<VersionNumber> versionNumbers = new ArrayList<VersionNumber>(versions.size());
                        versionNumbers.addAll(versions.keySet());
                        Collections.sort(versionNumbers);

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("New " + locale.getLanguage() + " translation created" );
                            logger.debug("  ---  With number of versions: " + (versions.size() - 1));
                        }

                        for(int dec = versionNumbers.size() - 1; dec >= 0; --dec)
                        {
                            if(currentVersion == null)
                            {
                                currentVersion = new InformationTranslation();
                                setCommonProperties(mlContent, currentVersion, versions.get(versionNumbers.get(dec)), false);
                                if(versionNumbers.size() > 1)
                                {
                                    currentVersion.withVersions(new InformationTranslationVersions());
                                }
                            }
                            else
                            {
                                final InformationTranslationVersion oldVersion = new InformationTranslationVersion();
                                currentVersion.getVersions().withVersions(oldVersion);
                                setCommonProperties(currentVersion, oldVersion, versions.get(versionNumbers.get(dec)), false);
                            }
                        }

                        mlContent.withTranslations(currentVersion);
                    }
                }
                else
                {
                    InfContent currentVersion = null;

                    final Map<VersionNumber, String> versions = infFileReader.getContentVersions(childPath, translations.get(0));
                    final List<VersionNumber> versionNumbers = new ArrayList<VersionNumber>(versions.size());
                    versionNumbers.addAll(versions.keySet());
                    Collections.sort(versionNumbers);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("  ---  With number of versions: " + (versions.size() - 1));
                    }

                    for(int dec = versionNumbers.size() - 1; dec >= 0; --dec)
                    {
                        if(currentVersion == null)
                        {
                            currentVersion = new InfContent();
                            setCommonProperties(parentNode, currentVersion, versions.get(versionNumbers.get(dec)), true);
                            if(versionNumbers.size() > 1)
                            {
                                currentVersion.withVersions(new InformationContentVersions());
                            }
                        }
                        else
                        {
                            final InformationContentVersion oldVersion = new InformationContentVersion();
                            currentVersion.getVersions().withVersions(oldVersion);
                            setCommonProperties(currentVersion, oldVersion, versions.get(versionNumbers.get(dec)), false);
                        }
                    }
                }

            }
            else
            {
                //logger.warn("The file " + childPath + " is not recognized.");
            }
        }

        for(final NamedNode container: containers)
        {
            scanInformation(container);
        }

    }

    /**
     * Recusive scan of the InterestGroup's directory service.
     *
     *  Protected for eventual extension / overridden facilities
     *
     * @param parentNode
     * @throws Exception
     */
    protected void scanDirectory(final XMLNode parentNode) throws Exception
    {
        // perhaps not usefull

        if(logger.isDebugEnabled())
        {
            logger.debug("Directory Service: Starting to scan: " + ElementsHelper.getExportationPath(parentNode));
        }
    }

    /**
     * Recusive scan of the InterestGroup's newsgroup service.
     *
     *  Protected for eventual extension / overridden facilities
     *
     * @param parentNode
     * @throws Exception
     */
    protected void scanNewsgroup(final XMLNode parentNode) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Newsgroup Service: Starting to scan: " + ElementsHelper.getExportationPath(parentNode));
        }

        final List<String> forumPaths;

        if(parentNode instanceof Newsgroups)
        {
        	 forumPaths = newsgroupReader.listRootForums(ElementsHelper.getExportationPath(parentNode));
        }
        else
        {
        	 forumPaths = newsgroupReader.listSubForums(ElementsHelper.getExportationPath(parentNode));
        }

        for(final String forumPath:  forumPaths)
        {
        	final Forum forum = new Forum();
        	setCommonProperties(parentNode, forum, forumPath, true);

        	if(logger.isDebugEnabled())
            {
                logger.debug("New forum created: " + ElementsHelper.getExportationPath(forum));
            }

        	fillTopics(forumPath, forum);

        	scanNewsgroup(forum);
        }

    }


	private void fillTopics(final String forumPath, final Forum forum) throws ExportationException
	{
		for(final String topicPath: newsgroupReader.listTopics(forumPath))
		{
			final Topic topic = new Topic();
			setCommonProperties(forum, topic, topicPath, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New topic created: " + ElementsHelper.getExportationPath(topic));
            }

			fillMessages(topicPath, topic);
		}
	}

	/**
	 * @param topicPath
	 * @param topic
	 * @throws ExportationException
	 */
	private void fillMessages(final String topicPath, final Topic topic) throws ExportationException
	{
		for(final String messagePath: newsgroupReader.listMessages(topicPath))
		{
			final Message message = new Message();
			setCommonProperties(topic, message, messagePath, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New message created: " + ElementsHelper.getExportationPath(message));
            }

			fillAttachements(messagePath, message);

			fillReplies(messagePath, message);
		}
	}

	/**
	 * @param messagePath
	 * @param message
	 * @throws ExportationException
	 */
	private void fillAttachements(final String messagePath, final Message message) throws ExportationException
	{
		for(final String attachement: newsgroupReader.listAttachements(messagePath))
		{
			final SimpleContent content = new SimpleContent();
			setCommonProperties(message, content, attachement, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New attachement created: " + ElementsHelper.getExportationPath(content));
            }
		}
		for(final String linkPath: newsgroupReader.listLinks(messagePath))
		{
			final Link link = new Link();
			setCommonProperties(message, link, linkPath, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New related link created: " + ElementsHelper.getExportationPath(link));
            }
		}
	}

	/**
	 * @param messagePath
	 * @param message
	 * @throws ExportationException
	 */
	private void fillReplies(final String messagePath, final Message message) throws ExportationException
	{
		for(final String replyPath: newsgroupReader.listReplies(messagePath))
		{
			final Message reply = new Message();
			setCommonProperties(message, reply, replyPath, true);

			if(logger.isDebugEnabled())
            {
                logger.debug("New Reply created: " + ElementsHelper.getExportationPath(reply));
            }

			fillAttachements(replyPath, reply);

			fillReplies(replyPath, reply);
		}
	}

    /**
     * Recusive scan of the InterestGroup's events service.
     *
     *  Protected for eventual extension / overridden facilities
     *
     * @param parentNode
     * @throws Exception
     */
    protected void scanEvents(final XMLNode parentNode) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Events Service: Starting to scan: " + ElementsHelper.getExportationPath(parentNode));
        }

        final Events events = (Events) parentNode;

        final List<Appointment> appointments = calendarReader.getAllAppointments(events);

        if(logger.isDebugEnabled())
        {
            logger.debug(appointments.size() + " events/meetings found.");
        }

        for(final Appointment appointment: appointments)
        {
        	if(appointment instanceof Meeting)
        	{
        		events.withMeetings((Meeting) appointment);
        	}
        	else
        	{
        		events.withEvents((Event) appointment);
        	}
        }

    }

    private Set<String> getCategories(final List<CategoryInterestGroupPair> pairs)
    {
        final Set<String> cats = new HashSet<String>(pairs.size());

        for(final CategoryInterestGroupPair pair: pairs)
        {
            cats.add(pair.getCategory());
        }

        return cats;

    }

    private void manageException(final Exception e) throws ExportationException
    {
        if(e instanceof ExportationException)
        {
            // should be already logged
            throw (ExportationException) e;
        }
        else
        {
            if(logger.isErrorEnabled())
            {
                logger.error("Unexpected error", e);
            }

            throw new ExportationException("Unexpected error during the exportation process", e);
        }
    }

    private String convertPathToName(final String path, final String textToDebug)
    {
        final String separator = libFileReader.getPathSeparator();
        int separatorIndex = path.lastIndexOf(separator);
        final String name;

        if(separatorIndex < 0)
        {
            name = path;
        }
        else
        {
            name = path.substring(separatorIndex + 1);
        }


        if(textToDebug != null && logger.isDebugEnabled())
        {
            logger.debug(textToDebug + ": " + name);
        }

        return name;
    }


    /**
     * @param metadataReader the metadataReader to set
     */
    public final void setMetadataReader(final MetadataReader metadataReader)
    {
        this.metadataReader = metadataReader;
    }

    /**
     * @param securityReader the securityReader to set
     */
    public final void setSecurityReader(final SecurityReader securityReader)
    {
        this.securityReader = securityReader;
    }

    /**
     * @param userReader the userReader to set
     */
    public final void setUserReader(final UserReader userReader)
    {
        this.userReader = userReader;
    }

    /**
     * @param calendarReader the calendarReader to set
     */
    public final void setCalendarReader(CalendarReader calendarReader)
    {
        this.calendarReader = calendarReader;
    }

    /**
     * @param infFileReader the infFileReader to set
     */
    public final void setInfFileReader(RemoteFileReader infFileReader)
    {
        this.infFileReader = infFileReader;
    }

    /**
     * @param libFileReader the libFileReader to set
     */
    public final void setLibFileReader(RemoteFileReader libFileReader)
    {
        this.libFileReader = libFileReader;
    }

    public final void setNewsgroupReader(NewsgroupReader newsgroupReader)
    {
        this.newsgroupReader = newsgroupReader;
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

	public final void setFileArchiver(FileArchiver fileArchiver)
	{
		this.fileArchiver = fileArchiver;
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
	 * @return the calendarReader
	 */
	public final CalendarReader getCalendarReader()
	{
		return calendarReader;
	}

	/**
	 * @return the infFileReader
	 */
	public final RemoteFileReader getInfFileReader()
	{
		return infFileReader;
	}

	/**
	 * @return the libFileReader
	 */
	public final RemoteFileReader getLibFileReader()
	{
		return libFileReader;
	}

	/**
	 * @return the metadataReader
	 */
	public final MetadataReader getMetadataReader()
	{
		return metadataReader;
	}

	/**
	 * @return the newsgroupReader
	 */
	public final NewsgroupReader getNewsgroupReader()
	{
		return newsgroupReader;
	}

	/**
	 * @return the securityReader
	 */
	public final SecurityReader getSecurityReader()
	{
		return securityReader;
	}

	/**
	 * @return the userReader
	 */
	public final UserReader getUserReader()
	{
		return userReader;
	}

	/**
	 * @param logFileReader the logFileReader to set
	 */
	public final void setLogFileReader(LogFileReader logFileReader)
	{
		this.logFileReader = logFileReader;
	}

	
}
