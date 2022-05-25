/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;

import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.AsynchJobListeners;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportStatisticsService;
import eu.cec.digit.circabc.service.migration.ExportationException;
import eu.cec.digit.circabc.service.migration.RootStat;
import eu.cec.digit.circabc.service.migration.StatisticsWriter;

/**
 * Concrete implementation of the Export Service to run against a Circa instance
 *
 * @author Yanick Pignot
 */
public class ExportStatisticsServiceImpl implements ExportStatisticsService
{
    private static final Log logger = LogFactory.getLog(ExportStatisticsServiceImpl.class);

    private String processName;
    private TransactionService transactionService;
    private TaskExecutor taskExecutor;
    private CircabcServiceRegistry serviceRegistry;

    private static final String ITERATION_NAME = "__export_statistics";
    private static final String ITERATION_DESCRIPTION = "All generated export statistics.";

    private FileArchiver fileArchiver;
    private NodeService nodeService;
    private ContentService contentService;
    private FileFolderService fileFolderService;
    private MimetypeService mimetypeService;
    private List<StatisticsWriter> statWriters;

    private Map<String, MigrationTracer<RootStat>> runningJournals = new HashMap<String, MigrationTracer<RootStat>>();

    public void init()
	{
		fileArchiver.registerMandatoryNode(ContentModel.TYPE_FOLDER, ITERATION_NAME);

		if(statWriters == null || statWriters.size() < 1)
		{
			throw new IllegalStateException("At least one statistic writer is mandatory");
		}
		else
		{
			final List<String> writerExtensions = new ArrayList<String>(statWriters.size());
			for(final StatisticsWriter writer: statWriters)
			{
				final String extension = writer.getExtension();
				if(writerExtensions.contains(extension))
				{
					throw new IllegalStateException("Several writers found with the extension: " + extension);
				}
				else
				{
					writerExtensions.add(extension);
				}
			}
		}

	}


    public void asynchGetStatistics(final ExportService exportService, final String uniqueId, final List<CategoryInterestGroupPair> pairs, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<RootStat> afterCommit)
    {
    	asynchGeStatisticsImpl(exportService, uniqueId, pairs, beforeRunJob, afterCommit);
    }

    public void asynchGetAllStatistics(final ExportService exportService, final String uniqueId, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<RootStat> afterCommit)
    {
    	asynchGeStatisticsImpl(exportService, uniqueId, null, beforeRunJob, afterCommit);
    }

    private void asynchGeStatisticsImpl(final ExportService exportService, final String uniqueId, final List<CategoryInterestGroupPair> pairs, final AsynchJobListeners.BeforeRunJob beforeRunJob, final AsynchJobListeners.AfterRunJob<RootStat> afterCommit)
    {
    	final ExportStatisticsServiceImpl statService = this;
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
								if(beforeRunJob != null)
								{
									beforeRunJob.start(serviceRegistry);
								}

								try
								{
									final MigrationTracer<RootStat> tracer;
									if(pairs == null)
									{
										tracer = statService.getAllStatistics(exportService, uniqueId);
									}
									else
									{
										tracer = statService.getStatistics(exportService, uniqueId, pairs);
									}


									if(afterCommit != null)
									{
										afterCommit.success(serviceRegistry, tracer);
									}
								}
								catch (final ExportationException e)
								{
									if(logger.isErrorEnabled()) {
						        		logger.error("An Error occur", e); 
									}

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


    public MigrationTracer<RootStat> getAllStatistics(final ExportService exportService, final String uniqueId) throws ExportationException
    {
    	ParameterCheck.mandatory("Exportation Service", exportService);

    	final List<CategoryInterestGroupPair> pairs = new ArrayList<CategoryInterestGroupPair>();

    	for(final String categoryName: exportService.getCategoryNames())
    	{
    		pairs.addAll(exportService.getInterestGroups(categoryName));
    	}

    	return getStatisticsImpl(exportService, uniqueId, pairs);

    }

    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, java.util.List)
	 */
    public final MigrationTracer<RootStat> getStatistics(final ExportService exportService, final String uniqueId, final List<CategoryInterestGroupPair> pairs) throws ExportationException
    {
    	ParameterCheck.mandatory("Exportation Service", exportService);
    	ParameterCheck.mandatoryCollection("The list of interest groups", pairs);

    	return getStatisticsImpl(exportService, uniqueId, pairs);
    }

    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair)
	 */
    public final MigrationTracer<RootStat> getStatistics(final ExportService exportService, final String uniqueId, final CategoryInterestGroupPair pair) throws ExportationException
    {
    	ParameterCheck.mandatory("Exportation Service", exportService);
    	ParameterCheck.mandatory("An interest groups", pair);

    	return getStatisticsImpl(exportService, uniqueId, Collections.singletonList(pair));

    }


    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair)
	 */
    public final MigrationTracer<RootStat> getStatisticsImpl(final ExportService exportService, final String uniqueId, final List<CategoryInterestGroupPair> interestGroups) throws ExportationException
    {
    	ParameterCheck.mandatory("Exportation Service", exportService);

    	if(runningJournals.containsKey(uniqueId))
		{
			throw new ExportationException("Impossible to launch importation of the statitics process " + uniqueId + " since it's already running");
		}

    	final NodeRef folder = getExportFileLocation();
		final String cleanId = NameProperty.toValidName(uniqueId);
		if(nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, cleanId) != null)
		{
			throw new ExportationException("Impossible to use id: " + cleanId + " since it is already used.");
		}

		try
		{
			final MigrationTracer<RootStat> tracer = new MigrationTracer<RootStat>(processName + " '" + uniqueId + "'", false);
	    	runningJournals.put(uniqueId, tracer);
	    	tracer.setInterestGroups(interestGroups);
	    	tracer.setReadOnly(true);

	    	final Map<String, Set<String>> igByCategories = new HashMap<String, Set<String>>();
	    	for(final CategoryInterestGroupPair pair: interestGroups)
	    	{
	    		if(igByCategories.containsKey(pair.getCategory()) == false)
	    		{
	    			igByCategories.put(pair.getCategory(), new HashSet<String>());
	    		}

	    		igByCategories.get(pair.getCategory()).add(pair.getInterestGroup());
	    	}

	    	final ImportRoot root = new ImportRoot();
	    	final RootStatImpl rootStat = getRootStatistic(exportService, root);
	    	tracer.setUnmarshalledObject(rootStat);

	    	for(final Map.Entry<String, Set<String>> entry: igByCategories.entrySet())
	    	{
	    		final String catName = entry.getKey();
				final CategoryStatImpl catStat = getCategoryStatistic(exportService, root, catName);
				rootStat.addCategoryStat(catStat);

				for(final String ig: entry.getValue())
				{
					tracer.setRunningPhase("Perform statistics on " + catName + ":" + ig);

					catStat.addInterestGroupStats(getIgStatistic(exportService, root, catName, ig));
				}
	    	}

	    	final MigrationIteration iteration = fileArchiver.getIterationByName(ITERATION_NAME);
	    	final NodeRef space = fileFolderService.create(iteration.getIterationRootSpace(), cleanId, ContentModel.TYPE_FOLDER).getNodeRef();
	    	tracer.setLogFile(space);

	    	for(final StatisticsWriter writer: statWriters)
	    	{
	    		try
	    		{
	    			final List<NodeRef> createdRefs = new ArrayList<NodeRef>();
		    		final OutputStream[] statStreams;
		    		if(writer.isNeedThreeFiles())
		    		{
		    			statStreams = new OutputStream[3];

		    			createdRefs.add(createContent(space, cleanId + "_root" + writer.getExtension(), writer.getReaderDisplayName()));
		    			createdRefs.add(createContent(space, cleanId + "_cat" + writer.getExtension(), writer.getReaderDisplayName()));
		    			createdRefs.add(createContent(space, cleanId + "_ig" + writer.getExtension(), writer.getReaderDisplayName()));
		    		}
		    		else
		    		{
		    			statStreams = new OutputStream[1];
		    			createdRefs.add(createContent(space, cleanId + writer.getExtension(), writer.getReaderDisplayName()));
		    		}

		    		for(int x = 0; x < createdRefs.size(); ++x)
		    		{
		    			final NodeRef nodeRef = createdRefs.get(x);
						final ContentWriter cotentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
						statStreams[x] = cotentWriter.getContentOutputStream();
		    		}

		    		writer.write(rootStat, statStreams);

		    		for(final OutputStream out: statStreams)
		    		{
		    			out.flush();
		    			out.close();
		    		}
	    		}
	    		catch(final Exception e)
	    		{
	    			logger.error("Problem writing file in " + writer.getReaderDisplayName(), e);
	    		}
	    	}

	    	return tracer;
		}
		catch(final Throwable t)
		{
			throw new ExportationException("Unexpected error during process statistics for process: " + uniqueId, t);
		}
		finally
		{
			runningJournals.remove(uniqueId);
		}
    }

    public NodeRef getExportFileLocation() throws ExportationException
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
			throw new ExportationException("Impossible to get iteration: " + ITERATION_NAME);
		}
		else if(iteration.getIterationRootSpace() == null)
		{
			throw new ExportationException("Impossible to get the " + ITERATION_NAME + " root space");
		}

		return iteration.getIterationRootSpace();
	}

	public Set<String> getRunningStatistics()
	{
		return runningJournals.keySet();
	}

	public MigrationTracer<RootStat> getRunningStatisticJournal(final String processKey)
	{
		return runningJournals.get(processKey);
	}

	private NodeRef createContent(final NodeRef parent, final String name, final String title)
	{
		final NodeRef content = fileFolderService.create(parent, name, ContentModel.TYPE_CONTENT).getNodeRef();
        final ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(mimetypeService.guessMimetype(name));
        writer.setEncoding("UTF-8");
        writer.putContent("");

        nodeService.setProperty(content, ContentModel.PROP_TITLE, title);
        nodeService.addAspect(content, ApplicationModel.ASPECT_INLINEEDITABLE, Collections.singletonMap(ApplicationModel.PROP_EDITINLINE, (Serializable) Boolean.TRUE));

		return content;
	}

    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair)
	 */
    private final RootStatImpl getRootStatistic(final ExportService exportService, final ImportRoot root) throws ExportationException
    {
    	final RemoteFileReader libFileReader = exportService.getLibFileReader();

    	final Circabc circabc = new Circabc();
		root.withCircabc(circabc);

		final RootStatImpl stat = new RootStatImpl(exportService.getImplementationName());

		stat.setCategories(exportService.getCategoryNames().size());
		stat.setHeaders(libFileReader.getAllCategoryHeaders().size());

    	return stat;
    }

    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair)
	 */
    private final CategoryStatImpl getCategoryStatistic(final ExportService exportService, final ImportRoot root, final String categoryName) throws ExportationException
    {
    	final RemoteFileReader libFileReader = exportService.getLibFileReader();
    	final SecurityReader securityReader = exportService.getSecurityReader();
    	final UserReader userReader = exportService.getUserReader();

    	String headerName = libFileReader.getCategoryHeaderName(categoryName, null);
    	boolean hidden = false;
    	if(headerName == null)
    	{
    		hidden = true;
    		headerName = libFileReader.getCategoryHeaderName(categoryName);
    	}

    	final CategoryStatImpl stat = new CategoryStatImpl(categoryName, headerName, hidden);

    	final CategoryHeader header = BinderUtils.getOrInitNewCategoryHeader(root.getCircabc(), headerName, libFileReader, logger);
    	final Category category = BinderUtils.getOrInitNewCategory(header, categoryName, libFileReader, logger);

    	securityReader.setPermission(category);
		stat.setCategoryAdmins(userReader.getInvitedPersons(category).size());
		stat.setInterestGroups(exportService.getInterestGroups(categoryName).size());

    	return stat;
    }


    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.ExportStatisticsService#getStatistics(eu.cec.digit.circabc.repo.migration.ExportServiceImpl, eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair)
	 */
    private final InterestGroupStatImpl getIgStatistic(final ExportService exportService, final ImportRoot root, final String catName, final String igName) throws ExportationException
    {
    	final RemoteFileReader libFileReader = exportService.getLibFileReader();
    	final RemoteFileReader infFileReader = exportService.getInfFileReader();
    	final SecurityReader securityReader = exportService.getSecurityReader();
    	final UserReader userReader = exportService.getUserReader();
    	final NewsgroupReader newsReader = exportService.getNewsgroupReader();
    	final CalendarReader calReader = exportService.getCalendarReader();

    	final InterestGroupStatImpl stat = new InterestGroupStatImpl(igName);

    	Category category = null;

    	for(final CategoryHeader header: root.getCircabc().getCategoryHeaders())
    	{
    		for(final Category cat: header.getCategories())
        	{
        		if(cat.getName().getValue().equals(catName))
        		{
        			category = cat;
        			break;
        		}
        	}
    	}

    	final InterestGroup ig = BinderUtils.getOrInitNewInterestGroup(category, igName, libFileReader, logger);

		securityReader.setProfileDefinition(ig);

		final Directory directory = ig.getDirectory();

	    int exported = 0;
		for(final AccessProfile profile: directory.getAccessProfiles())
		{
			exported += (profile.isExported()) ? 1 : 0;
		}

		stat.setIgProfiles(directory.getAccessProfiles().size());
    	stat.setIgUsers(userReader.getInvitedPersons(ig).size());

    	stat.setGuestVisible(directory.getGuest().isVisibility());
    	stat.setIgExportedProfiles(exported);
    	stat.setIgImportedProfiles(directory.getImportedProfiles().size());
    	stat.setRegistredVisible(directory.getRegistredUsers().isVisibility());
    	try
		{
			statLibrary(libFileReader, newsReader, securityReader, stat, ElementsHelper.getExportationPath(ig.getLibrary()), 0);
			statInformation(infFileReader, stat, ElementsHelper.getExportationPath(ig.getInformation()), 0);
			statEvents(calReader, stat, ig.getEvents());
			statNewsgroup(newsReader, stat, ElementsHelper.getExportationPath(ig.getNewsgroups()), true);
		}
    	catch (ExportationException e)
		{
			throw e;
		}
    	catch (Exception e)
		{
			throw new ExportationException(e.getMessage(), e);
		}

    	return stat;
    }


    private void statLibrary(final RemoteFileReader libFileReader, final NewsgroupReader newsReader, final SecurityReader securityReader, final InterestGroupStatImpl stat, final String path, final int level) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Library Service: Starting to scan: " + path);
        }

        final List<String> containers = new ArrayList<String>();
        int childs = 0;

        for(final String childPath : libFileReader.listChidrenPath(path))
        {
        	if(libFileReader.isSpace(childPath))
            {
                containers.add(childPath);
                stat.addLibrarySpace();
                statDiscussions(newsReader, stat, childPath);

                final Space space = new Space();
                ElementsHelper.setExportationPath(space, childPath);
                securityReader.setSharedDefinition(space);

                if(space.getShareds().size() > 0)
                {
                	stat.addIgExportedSpace();
                }

                ++childs;

            }
            else if(libFileReader.isDossier(childPath))
            {
                for(final String dossierPath: libFileReader.getDossierTranslations(childPath))
                {
                    containers.add(dossierPath);
                    stat.addLibraryDossier();
                    statDiscussions(newsReader, stat, dossierPath);

                    ++childs;
                }
            }
            else if(libFileReader.isUrl(childPath))
            {
                for(final String urlPath: libFileReader.getUrlTranslations(childPath))
                {
                	stat.addLibraryUrl();
                	statDiscussions(newsReader, stat, urlPath);

                	++childs;
                }
            }
            else if(libFileReader.isDocument(childPath))
            {
                final List<Locale> languages = libFileReader.getContentTranslations(childPath);

                if(languages.size() > 1)
                {
                	stat.addLibraryMLContent();
                	statDiscussions(newsReader, stat, childPath);
                }

                for(final Locale locale: languages)
                {
                	boolean currentVersion = true;

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
                    	final String versionPath = versions.get(versionNumbers.get(dec));
                    	stat.addDocumentSize(libFileReader.getFileSize(versionPath));

                    	if(currentVersion)
                        {
                        	stat.addLibraryDoc();
							statDiscussions(newsReader, stat, versionPath);
                            currentVersion = false;

                            ++childs;
                        }
                        else
                        {
                        	stat.addLibraryOldVersion();
                        }
                    }
                }
            }
            else if(libFileReader.isSharedSpaceLink(childPath))
            {
               stat.addIgImportedSpace();

               ++childs;
            }
            else if(libFileReader.isLink(childPath))
            {
            	stat.addLibraryLink();

            	++childs;
            }
        }

        stat.addSpaceSize(childs);
        stat.addSpaceDeep(level);

        for(final String container: containers)
        {
            statLibrary(libFileReader, newsReader, securityReader, stat, container, level + 1);
        }
    }

    protected void statInformation(final RemoteFileReader infFileReader, final InterestGroupStatImpl stat, final String path, final int level) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Information Service: Starting to scan: " + path);
        }

        final List<String> containers = new ArrayList<String>();
        int childs = 0;
        for(final String childPath : infFileReader.listChidrenPath(path))
        {
            if(infFileReader.isSpace(childPath))
            {
            	 containers.add(childPath);
                 stat.addInformationSpace();
                 ++childs;
            }
            else if(infFileReader.isDocument(childPath))
            {
            	 final List<Locale> languages = infFileReader.getContentTranslations(childPath);

                 if(languages.size() > 1)
                 {
                 	stat.addInformationMLContent();
                 }

                 for(final Locale locale: languages)
                 {
                	 boolean currentVersion = true;

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
                     	final String versionPath = versions.get(versionNumbers.get(dec));
                     	stat.addDocumentSize(infFileReader.getFileSize(versionPath));

                     	if(currentVersion)
	                    {
                     		stat.addInformationDoc();
                     		currentVersion = false;
                     		++childs;
	                    }
                     	else
                     	{
                     		stat.addInformationOldVersion();
                     	}
                     }
                 }
            }
        }

        stat.addSpaceSize(childs);
        stat.addSpaceDeep(level);

        for(final String container: containers)
        {
            statInformation(infFileReader, stat, container, level + 1);
        }
    }


    private void statEvents(final CalendarReader calendarReader, final InterestGroupStatImpl stat, final Events events) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Events Service: Starting to scan: " + ElementsHelper.getExportationPath(events));
        }

        for(final Appointment appointment : calendarReader.getAllAppointments(events))
        {
        	if(appointment instanceof Meeting)
        	{
        		stat.addCalendarMeeting();
        	}
        	else
        	{
        		stat.addCalendarEvent();
        	}
        }
    }

    protected void statNewsgroup(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String path, final boolean isRoot) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Newsgroup Service: Starting to scan: " + path);
        }

        final List<String> forumPaths;

        if(isRoot)
        {
        	 forumPaths = newsgroupReader.listRootForums(path);
        }
        else
        {
        	 forumPaths = newsgroupReader.listSubForums(path);
        }

        for(final String forumPath:  forumPaths)
        {
        	stat.addNewsgroupForum();

        	if(newsgroupReader.isModeratedForum(forumPath))
        	{
        		stat.addNewsgroupModeratedForum();
        	}

        	statTopics(newsgroupReader, stat, forumPath, false);
        	statNewsgroup(newsgroupReader, stat, forumPath, false);
        }

    }

    private void statTopics(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String forumPath, final boolean isLibrary) throws ExportationException
	{
		for(final String topicPath: newsgroupReader.listTopics(forumPath))
		{
			if(isLibrary)
			{
				stat.addLibraryDiscussionTopic();
			}
			else
			{
				stat.addNewsgroupTopic();
			}

			statMessages(newsgroupReader, stat, topicPath, isLibrary);
		}
	}

    private void statMessages(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String topicPath, final boolean isLibrary) throws ExportationException
	{
		for(final String messagePath: newsgroupReader.listMessages(topicPath))
		{
			if(isLibrary)
			{
				stat.addLibraryDiscussionMessage();
			}
			else
			{
				stat.addNewsgroupMessage();
			}

			statAttachements(newsgroupReader, stat, messagePath, isLibrary);
			statReplies(newsgroupReader, stat, messagePath, isLibrary);
		}
	}


    private void statAttachements(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String messagePath, final boolean isLibrary) throws ExportationException
	{
		final List<String> listAttachements = newsgroupReader.listAttachements(messagePath);

		if(isLibrary)
		{
			stat.setLibraryDiscussionAttachements(listAttachements.size() + stat.getLibraryDiscussionAttachements());
		}
		else
		{
			stat.setNewsgroupMessagesAttachements(listAttachements.size() + stat.getNewsgroupMessagesAttachements());
		}
	}

	private void statReplies(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String messagePath, final boolean isLibrary) throws ExportationException
	{
		for(final String replyPath: newsgroupReader.listReplies(messagePath))
		{
			if(isLibrary)
			{
				stat.addLibraryDiscussionMessage();
			}
			else
			{
				stat.addNewsgroupMessage();
			}

			statAttachements(newsgroupReader, stat, replyPath, isLibrary);
			statReplies(newsgroupReader, stat, replyPath, isLibrary);
		}
	}

	private void statDiscussions(final NewsgroupReader newsgroupReader, final InterestGroupStatImpl stat, final String path) throws ExportationException
    {
        final String discussionPath = newsgroupReader.getDiscussions(path);

        if(discussionPath != null)
		{
        	stat.addLibraryDiscussion();
			for(final String topicPath: newsgroupReader.listDiscussionsTopics(path))
			{
				stat.addLibraryDiscussionTopic();

				statMessages(newsgroupReader, stat, topicPath, true);
			}
		}

    }

	/**
	 * @param serviceRegistry the serviceRegistry to set
	 */
	public final void setServiceRegistry(final CircabcServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * @param taskExecutor the taskExecutor to set
	 */
	public final void setTaskExecutor(final TaskExecutor taskExecutor)
	{
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @param transactionService the transactionService to set
	 */
	public final void setTransactionService(final TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	/**
	 * @param processName the processName to set
	 */
	public final void setProcessName(final String processName)
	{
		this.processName = processName;
	}

	/**
	 * @param fileArchiver the fileArchiver to set
	 */
	public final void setFileArchiver(final FileArchiver fileArchiver)
	{
		this.fileArchiver = fileArchiver;
	}


	/**
	 * @param nodeService the nodeService to set
	 */
	public final void setNodeService(final NodeService nodeService)
	{
		this.nodeService = nodeService;
	}


	/**
	 * @param statWriters the statWriters to set
	 */
	public final void setStatWriters(final List<StatisticsWriter> statWriters)
	{
		this.statWriters = statWriters;
	}

	/**
	 * @param contentService the contentService to set
	 */
	public final void setContentService(final ContentService contentService)
	{
		this.contentService = contentService;
	}


	/**
	 * @param fileFolderService the fileFolderService to set
	 */
	public final void setFileFolderService(final FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}


	/**
	 * @param mimetypeService the mimetypeService to set
	 */
	public final void setMimetypeService(final MimetypeService mimetypeService)
	{
		this.mimetypeService = mimetypeService;
	}
}
