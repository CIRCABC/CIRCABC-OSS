/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.DuplicateIterationNameException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.service.struct.ManagementService;


/**
 * @author Yanick Pignot
 */
public class RepositoryArchiver implements FileArchiver
{
    private static final Log logger = LogFactory.getLog(RepositoryArchiver.class);

    private static final ThreadLocal<DateFormat> _DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMdd_HmmssSSS");
		}
	};

    private static final String FAIL_IMPORT_SPACE_NAME = "__failed_import_";
    private static final String IMPORT_APPLIED_XML_DESC = "The resulting xml file enhanced by node references. Exists only if the process is successfully terminated.";

    private static final String STACKTRACE_TXT_FILE = "stacktrace.txt";
    private static final String STACKTRACE_TXT_TITLE = "Error stacktrace";
    private static final String STACKTRACE_TXT_DESC = "The stacktrace of the error occured during the importation";

    private static final String IMPORT_APPLIED_XML_FILE = "applied.xml";
    private static final String IMPORT_APPLIED_XML_TITLE = "Applied import file";
    private static final String IMPORT_LOG_XML_DESC = "All logs in a single xml file";

    private static final String IMPORT_LOG_XML_FILE = "log.xml";
    private static final String IMPORT_LOG_XML_TITLE = "Import log result file";

    private static final String IMPORT_SPACE_DESCRIPTION = "Contains the imported migration files on {0}";
    private static final String IMPORT_SPACE_NAME = "import_";

    private static final String IMPORT_TRANSFORMED_XML_DESC = "The import file transformed by the migration pre-processors. This file is really applied.";
    private static final String IMPORT_TRANSFORMED_XML_FILE = "transformed.xml";

    private static final String IMPORT_TRANSFORMED_XML_TITLE = "Transformed import file";

    private static final String ORIGINAL_SPACE_DESCRIPTION = "Contains the uploaded migration file at the start of the iteration.";

    private static final String ORIGINAL_SPACE_NAME = "original";
    private static final String ORIGINAL_XML_DESC = "Contains a not modified import file. It s the starting point of a new iteration process.";

    private static final String ORIGINAL_XML_FILE = "uploaded.xml";
    private static final String ORIGINAL_XML_TITLE = "The current iteration first file.";
    private static final String ROOT_SPACE_DESCRIPTION = "Contains history of migration processes";

    private static final String LOGFILES_PROP_FILE = "iglogs.properties";
    private static final String LOGFILES_PROP_TITLE = "The interest group(s) log files.";
    private static final String LOGFILES_DESCRIPTION = "Contains the IG log files related to the current space importation.";

    private static final String TRANSFORM_INVALID_XML_DESC = "The residual INVALID result of automatic and user traformation operations. If this file is not empty, the import is not terminated.";
    private static final String TRANSFORM_INVALID_XML_FILE = "residual.xml";
    private static final String TRANSFORM_INVALID_XML_TITLE = "Transformed residual file";

    private static final String TRANSFORM_SPACE_DESCRIPTION = "Contains the transformed migration files on {0}";
    private static final String TRANSFORM_SPACE_NAME = "transformed_";
    private static final String TRANSFORM_VALID_XML_DESC = "The valid result of automatic and user traformation operations. This file is ready to be imported.";

    private static final String TRANSFORM_VALID_XML_FILE = "valid.xml";
    private static final String TRANSFORM_VALID_XML_TITLE = "Transformed valid file";

    private static final String VALID_USER_LIST_DESC = "The goal of this file is to make persitant all previous ETL processes to reduce the futur ones.";
    private static final String VALID_USER_LIST_FILE = "previous_valid_users.properties";
    private static final String VALID_USER_LIST_TITLE = "The list of previously persited valid users";

    private static final String USER_CRON_LIST_DESC = "The goal of this file is to make persitant the state of bulk user exports.";
    private static final String USER_CRON_LIST_FILE = "planned_user_export.properties";
    private static final String USER_CRON_LIST_TITLE = "The list of bulk user export stats";

    private static final String STAT_CRON_LIST_DESC = "The goal of this file is to make persitant the state of exportation statistics.";
    private static final String STAT_CRON_LIST_FILE = "planned_export_statistics.properties";
    private static final String STAT_CRON_LIST_TITLE = "The list of exportation statistics export stats";

    private static final String EXPORT_CRON_LIST_DESC = "The goal of this file is to make persitant the state of exports.";
    private static final String EXPORT_CRON_LIST_FILE = "planned_iteration_export.properties";
    private static final String EXPORT_CRON_LIST_TITLE = "The list of iteration export stats";

    private static final String IMPORT_CRON_LIST_DESC = "The goal of this file is to make persitant the state of imports.";
    private static final String IMPORT_CRON_LIST_FILE = "planned_iteration_import.properties";
    private static final String IMPORT_CRON_LIST_TITLE = "The list of iteration import stats";

    
    private ContentService contentService;

    private String dictionaryPath;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private NodeRef rootArchiveSpace;
    private SearchService searchService;
    private String xmlArchivesSpace;

    private Map<QName, String> registredMandatoryNodes = new HashMap<QName, String>();

	private ManagementService managementService;

    public void flagImportationAsFailed(final MigrationIteration iteration, final Date importProcessId, final Throwable error) throws ArchiveException
    {
        try
        {
        	final NodeRef importSpace = getImportSpace(iteration, importProcessId);

            // rename the space
			fileFolderService.rename(importSpace, FAIL_IMPORT_SPACE_NAME + format(new Date()));

            // print the stack trace
            final NodeRef stack = store(importSpace, STACKTRACE_TXT_FILE, null, STACKTRACE_TXT_TITLE, STACKTRACE_TXT_DESC);

			final OutputStream contentOutputStream = getContentOutputStream(stack);

			if(error == null)
			{
				contentOutputStream.write("No trace available".getBytes());
			}
			else
			{
				error.printStackTrace(new PrintStream(contentOutputStream));
			}

			contentOutputStream.flush();
	        contentOutputStream.close();

        }
        catch (final FileExistsException impossible)
        {
            // the timestamp does'nt allow this exception.
        }
        catch (final FileNotFoundException ignore)
        {
            // the import magically failed before its start. Not an error. Nothing to rename.
        }
        catch (IOException e)
		{
			// The application is already on the verge of dying. Don't aggravate its case!
		}
    }

    public synchronized InputStream getContentInputStream(final NodeRef resource)
    {
        final ContentReader contentReader = contentService.getReader(resource, ContentModel.PROP_CONTENT);
        final InputStream contentStream = contentReader.getContentInputStream();
        return contentStream;
    }

    public NodeRef getImportResource(final MigrationIteration iteration) throws ArchiveException
    {
        if(iteration == null)
        {
            throw new NullPointerException("Invalid iteration");
        }
        else if(isIterationReadyForMigration(iteration) == false)
        {
            throw new ArchiveException("The iteration " + iteration.getIdentifier() + " is not ready for an import process. Apply transformation first.");
        }

        // get last tranformation valid file
        return getLastWithChildName(iteration.getTransformationDates(), TRANSFORM_VALID_XML_FILE);
    }

    public MigrationIteration getIterationByName(final String iterationName) throws ArchiveException
    {
        if(iterationName == null || iterationName.length() < 1)
        {
            throw new ArchiveException("A valid iteration name is required");
        }

        MigrationIteration selectedIteration = null;

        for(final MigrationIteration iteration: getIterations())
        {
            if(iteration.getIdentifier().equals(iterationName))
            {
                selectedIteration = iteration;
                break;
            }
        }

        return selectedIteration;
    }

    public List<MigrationIteration> getIterations()  throws ArchiveException
    {
        final ArrayList<MigrationIteration> iterations = new ArrayList<MigrationIteration>();
        final NodeRef migrationRoot = getMigrationRootSpace();
        for(final FileInfo iterationFileInfo: listFileInfos(migrationRoot))
        {
            final MigrationIterationImpl iteration = buildIteration(iterationFileInfo.getNodeRef(), iterationFileInfo.getProperties());

            final NodeRef iterationRef = iterationFileInfo.getNodeRef();
            fillIetartionSequences(iteration, iterationRef);

            iterations.add(iteration);

            if(logger.isDebugEnabled())
            {
                logger.debug("Found: " + iteration.toString());
            }
        }

        return iterations;
    }

    public void registerMandatoryNode(final QName type, final String name)
    {
        registredMandatoryNodes.put(type, name);
    }

    public NodeRef getMigrationRootSpace() throws ArchiveException
    {
        if(rootArchiveSpace == null)
        {
            final List<NodeRef> nodeRefs =
                searchService.selectNodes(
                		managementService.getCompanyHomeNodeRef(),
                        dictionaryPath,
                        null,
                        namespaceService, false);

            if(nodeRefs.size() != 1)
            {
                throw new IllegalArgumentException("Impossible to retreive the data Dictionary. Bad result set size (" + nodeRefs.size() + " for 1 expected) for query (" + dictionaryPath + ")");
            }

            rootArchiveSpace = getOrCreateNode(nodeRefs.get(0), ContentModel.TYPE_FOLDER, xmlArchivesSpace, xmlArchivesSpace, ROOT_SPACE_DESCRIPTION, null, false);

            // create the mandatory files
            getOrCreateNode(rootArchiveSpace, ContentModel.TYPE_CONTENT, USER_CRON_LIST_FILE, USER_CRON_LIST_TITLE, USER_CRON_LIST_DESC, null, false);
            getOrCreateNode(rootArchiveSpace, ContentModel.TYPE_CONTENT, IMPORT_CRON_LIST_FILE, IMPORT_CRON_LIST_TITLE, IMPORT_CRON_LIST_DESC, null, false);
            getOrCreateNode(rootArchiveSpace, ContentModel.TYPE_CONTENT, EXPORT_CRON_LIST_FILE, EXPORT_CRON_LIST_TITLE, EXPORT_CRON_LIST_DESC, null, false);
            getOrCreateNode(rootArchiveSpace, ContentModel.TYPE_CONTENT, STAT_CRON_LIST_FILE, STAT_CRON_LIST_TITLE, STAT_CRON_LIST_DESC, null, false);
            getOrCreateNode(rootArchiveSpace, ContentModel.TYPE_CONTENT, VALID_USER_LIST_FILE, VALID_USER_LIST_TITLE, VALID_USER_LIST_DESC, null, false);

            for(Map.Entry<QName, String> entry: registredMandatoryNodes.entrySet())
            {
                getOrCreateNode(rootArchiveSpace, entry.getKey(), entry.getValue(), entry.getValue(), "", null, false);
            }
        }

        return rootArchiveSpace;
    }

    public MigrationIteration getNodeIteration(final NodeRef nodeRef)  throws ArchiveException
    {
        if(nodeRef == null)
        {
            return null;
        }

        MigrationIterationImpl iteration = null;

        final NodeRef rootRef = getMigrationRootSpace();
        NodeRef iterRef = nodeRef;
        NodeRef parentRef;

        while(iterRef != null)
        {
            parentRef = nodeService.getPrimaryParent(iterRef).getParentRef();

            if(parentRef == null)
            {
                // we are on top
                break;
            }
            else if(parentRef.equals(rootRef))
            {
                // parent ref is the root, then iter ref if the iteration container space
                final Map<QName, Serializable> props = nodeService.getProperties(iterRef);
                iteration = buildIteration(iterRef, props);
                fillIetartionSequences(iteration, iterRef);

                break;
            }
            else
            {
                iterRef = parentRef;
            }
        }
        return iteration;
    }

    public NodeRef getTransformationResource(final MigrationIteration iteration) throws ArchiveException
    {
        if(iteration == null)
        {
            throw new NullPointerException("Invalid iteration");
        }
        else if(isIterationReadyForTransformation(iteration) == false)
        {
            throw new ArchiveException("The iteration " + iteration.getIdentifier() + " is not ready for an import process. Apply importation first.");
        }

        if(iteration.getTransformationDates().size() == 0)
        {
            return iteration.getOriginalFileNodeRef();
        }
        else
        {
            // get last tranformation invalid file
            return getLastWithChildName(iteration.getTransformationDates(), TRANSFORM_INVALID_XML_FILE);
        }
    }

    public boolean isIterationReadyForMigration(final MigrationIteration iteration)
    {
        boolean ready = false;

        final int transSize = iteration.getTransformationDates().size();
        final int exportSize = iteration.getImportedDates().size();
        if(transSize > 0 && transSize > exportSize)
        {
            final int diff = transSize - exportSize;
            if(diff == 1)
            {
                ready = true;
            }
            else if(diff != 0)
            {
                throw new IllegalStateException("The iteration storage structure corrupted for: " + iteration.getIdentifier() + ". ETL Spaces - Import spaces = 0 or 1, NOT " + diff);
            }
        }

        return ready;
    }

    public boolean isIterationReadyForTransformation(final MigrationIteration iteration)
    {
        boolean ready = false;

        final int transSize = iteration.getTransformationDates().size();
        final int exportSize = iteration.getImportedDates().size();
        if(iteration.getOriginalFileNodeRef() != null)
        {
            final int diff = transSize - exportSize;
            if(diff == 0)
            {
                ready = true;
            }
            else if(diff != 1)
            {
                throw new IllegalStateException("The iteration storage structure corrupted for: " + iteration.getIdentifier() + ". ETL Spaces - Import spaces = 0 or 1, NOT " + diff);
            }
        }

        return ready;
    }

    public Properties retreiveProperties(final NodeRef nodeRef) throws ArchiveException
    {
    	 if(nodeRef == null || nodeService.exists(nodeRef) == false || ContentModel.TYPE_CONTENT.equals(nodeService.getType(nodeRef)) == false)
         {
             throw new ArchiveException("A valid content file is required");
         }

    	  try
          {
              if(logger.isDebugEnabled())
              {
                   logger.debug("Start to retreive properties from the content node ...");
              }

              return extractProperties(nodeRef);
          }
    	  catch(final Exception e)
          {
              throw new ArchiveException("Impossible to retreive the persisted property file.", e);
          }
    }

    public Map<Object, Object> retreiveAlreadyTransformedUser() throws ArchiveException
    {
        try
        {
            if(logger.isDebugEnabled())
            {
                 logger.debug("Start to retreive already validated users ...");
            }

            final Properties props = extractProperties(getValidUsersFile());

            return props;
        }
        catch(final ArchiveException e)
        {
            throw e;
        }
        catch(final Exception e)
        {
            throw new ArchiveException("Impossible to retreive the persisted list of valid users.", e);
        }
    }

    public Map<Object, Object> retreivePlannedUserExport() throws ArchiveException
    {
        try
         {
            return retreivePlannedData(USER_CRON_LIST_FILE);
         }
         catch(final ArchiveException e)
         {
             throw e;
         }
        catch(final Exception e)
        {
             throw new ArchiveException("Impossible to retreive the persisted list of planned user exports.", e);
        }
    }

    public Map<Object, Object> retreivePlannedStatisticExports() throws ArchiveException
    {
        try
         {
            return retreivePlannedData(STAT_CRON_LIST_FILE);
         }
         catch(final ArchiveException e)
         {
             throw e;
         }
        catch(final Exception e)
        {
             throw new ArchiveException("Impossible to retreive the persisted list of planned export statistics.", e);
        }
    }

    public Map<Object, Object> retreivePlannedIterationExport() throws ArchiveException
    {
        try
         {
            return retreivePlannedData(EXPORT_CRON_LIST_FILE);
         }
         catch(final ArchiveException e)
         {
             throw e;
         }
        catch(final Exception e)
        {
            throw new ArchiveException("Impossible to retreive the persisted list of planner iteration exports.", e);
        }
    }


    public Map<Object, Object> retreivePlannedIterationImport() throws ArchiveException
    {
        try
         {
            return retreivePlannedData(IMPORT_CRON_LIST_FILE);
         }
         catch(final ArchiveException e)
         {
             throw e;
         }
        catch(final Exception e)
        {
            throw new ArchiveException("Impossible to retreive the persisted list of planner iteration imports.", e);
        }
    }

    public synchronized void addPlannedUserExport(String planificationId, Object data) throws ArchiveException
    {
        try
         {
            storePlannedData(planificationId, data, USER_CRON_LIST_FILE);
         }
        catch(ArchiveException e)
        {
            throw e;
        }
        catch(Exception e)
        {
             throw new ArchiveException("Impossible to store the persisted list of planned user export.", e);
        }
    }

    public synchronized void addPlannedIterationExport(String planificationId, Object data) throws ArchiveException
    {
        try
         {
            storePlannedData(planificationId, data, EXPORT_CRON_LIST_FILE);
         }
        catch(ArchiveException e)
        {
            throw e;
        }
        catch(Exception e)
        {
             throw new ArchiveException("Impossible to store the persisted list of planned iteration exportation.", e);
        }
    }

    public synchronized void addPlannedIterationImport(String planificationId, Object data) throws ArchiveException
    {
        try
         {
            storePlannedData(planificationId, data, IMPORT_CRON_LIST_FILE);
         }
        catch(ArchiveException e)
        {
            throw e;
        }
        catch(Exception e)
        {
             throw new ArchiveException("Impossible to store the persisted list of planned iteration importation.", e);
        }
    }

    public synchronized void addPlannedExportStatistics(String planificationId, Object data) throws ArchiveException
    {
        try
         {
            storePlannedData(planificationId, data, STAT_CRON_LIST_FILE);
         }
        catch(ArchiveException e)
        {
            throw e;
        }
        catch(Exception e)
        {
             throw new ArchiveException("Impossible to store the persisted list of planned exportation statistics.", e);
        }
    }

    public void storeAlreadyTransformedUser(final Map<Object, Object> newValidUsers) throws ArchiveException
    {
        ParameterCheck.mandatory("Valid users ", newValidUsers);

         try
         {
            final NodeRef propFile = getValidUsersFile();
            final Properties props = extractProperties(propFile);

            props.putAll(newValidUsers);

            if(logger.isDebugEnabled())
            {
                logger.debug(newValidUsers.size() + " new validated users added.");
            }

            storeProperties(propFile, props, VALID_USER_LIST_DESC);
         }
        catch(ArchiveException e)
        {
            throw e;
        }
        catch(Exception e)
        {
             throw new ArchiveException("Impossible to store the persisted list of valid users.", e);
        }
    }

    public NodeRef storeImportationLogResult(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException
    {
        return store(getImportSpace(iteration, importProcessId), IMPORT_LOG_XML_FILE, inputStream, IMPORT_LOG_XML_TITLE, IMPORT_LOG_XML_DESC);
    }
    
	public NodeRef storeImportationLogResultDocument(MigrationIteration iteration, Date importProcessId, Document document) throws ArchiveException
	{
		return storeDocument(getImportSpace(iteration, importProcessId), IMPORT_LOG_XML_FILE, document, IMPORT_LOG_XML_TITLE, IMPORT_LOG_XML_DESC);
	}


    

	public NodeRef storeImportedAppliedXML(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException
    {
        return store(getImportSpace(iteration, importProcessId), IMPORT_APPLIED_XML_FILE, inputStream, IMPORT_APPLIED_XML_TITLE, IMPORT_APPLIED_XML_DESC);
    }

    public NodeRef storeImportedTranformedXML(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException
    {
        return store(getImportSpace(iteration, importProcessId), IMPORT_TRANSFORMED_XML_FILE, inputStream, IMPORT_TRANSFORMED_XML_TITLE, IMPORT_TRANSFORMED_XML_DESC);
    }

    public synchronized MigrationIteration startNewIteration(final String shortLabel, final String description) throws ArchiveException, DuplicateIterationNameException
    {
        final String safeName = NameProperty.toValidName(shortLabel);

        if(existLabel(safeName))
        {
            throw new DuplicateIterationNameException(safeName);
        }

        //	create a new iteration
        final MigrationIterationImpl iteration = new MigrationIterationImpl(safeName, description, new Date(), AuthenticationUtil.getFullyAuthenticatedUser());

        final NodeRef rootSpace = getIterationSpace(iteration);

        iteration.setIterationRootSpace(rootSpace);

        if(logger.isDebugEnabled())
        {
            logger.debug("Iteration " + iteration.getIdentifier() + " started.");
        }

        return iteration;
    }

    public Boolean removeIterationIfEmpty(final MigrationIteration iteration)
    {

        if(iteration == null)
        {
            return Boolean.FALSE;
        }
        else
        {
            try
            {
                final NodeRef space = getIterationSpace(iteration);

                if(fileFolderService.list(space).size() > 0)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Iteration " + iteration.getIdentifier() + " seems to be successfully started. It will be not deleted.");
                    }

                    return Boolean.FALSE;
                }
                else
                {
                    nodeService.deleteNode(space);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Iteration " + iteration.getIdentifier() + " deleted since no file created under.");
                    }

                    return Boolean.TRUE;
                }
            }
            catch (Exception ignore)
            {
                return Boolean.FALSE;
            }
        }

    }

    public NodeRef storeOriginalExportFile(final MigrationIteration iteration, final InputStream inputStream) throws ArchiveException
    {
        ParameterCheck.mandatory("An iteration ", iteration);

        return store(getUploadSpace(iteration), ORIGINAL_XML_FILE, inputStream, ORIGINAL_XML_TITLE, ORIGINAL_XML_DESC);
    }

    public NodeRef storeLogsExportFile(final MigrationIteration iteration, final Properties properties) throws ArchiveException
    {
    	ParameterCheck.mandatory("An iteration ", iteration);

    	final NodeRef ref = store(getUploadSpace(iteration), LOGFILES_PROP_FILE, null, LOGFILES_PROP_TITLE, LOGFILES_DESCRIPTION);

    	try
		{
			storeProperties(ref, properties, LOGFILES_DESCRIPTION);
		}
    	catch (IOException e)
		{
			throw new ArchiveException("Impossible to store log files", e);
		}

    	return ref;
    }

    public NodeRef storeExportFile(final MigrationIteration iteration, final String fileName, final InputStream inputStream) throws ArchiveException
    {
        ParameterCheck.mandatory("An iteration ", iteration);
        ParameterCheck.mandatoryString("The file name ", fileName);

        return store(getUploadSpace(iteration), fileName, inputStream, fileName, "");
    }

    public NodeRef storeTransformedResidualFile(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException
    {
        return store(getEtlSpace(iteration, importProcessId), TRANSFORM_INVALID_XML_FILE, inputStream, TRANSFORM_INVALID_XML_TITLE, TRANSFORM_INVALID_XML_DESC);
    }

    public NodeRef storeTransformedValidFile(final MigrationIteration iteration, final Date importProcessId, final InputStream inputStream) throws ArchiveException
    {
        return store(getEtlSpace(iteration, importProcessId), TRANSFORM_VALID_XML_FILE, inputStream, TRANSFORM_VALID_XML_TITLE, TRANSFORM_VALID_XML_DESC);
    }

    /*-----
         Private methods
     -----*/

    private Map<Object, Object> retreivePlannedData(final String name) throws ArchiveException
    {
        final NodeRef ref = getOrCreateFile(getMigrationRootSpace(), name, null, null);

        try
        {
            return extractProperties(ref);
        }
        catch (IOException e)
        {
            throw new ArchiveException("Impossible to read the cron list: " + name, e);
        }
    }

    private void storePlannedData(final String planificationId, final Object data, final String name) throws ArchiveException, InvalidNodeRefException, InvalidTypeException, ContentIOException
    {
        ParameterCheck.mandatoryString("Valid planification Id ", planificationId);
        ParameterCheck.mandatory("Valid data ", data);

        final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                final NodeRef ref = getOrCreateFile(getMigrationRootSpace(), name, "", "");

                try
                {
                    final Properties extractProperties = extractProperties(ref);
                    extractProperties.put(planificationId.trim(), data.toString());
                    storeProperties(ref, extractProperties, "");
                }
                catch (IOException e)
                {
                    throw new ArchiveException("Impossible to write to the cron list: " + name, e);
                }

                return ref;
            }
        }, false, true);
    }

    private MigrationIterationImpl buildIteration(final NodeRef rootSpace, final Map<QName, Serializable> nodeProperties)
    {
        final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<MigrationIterationImpl>()
        {
            public MigrationIterationImpl execute() throws Throwable
            {
                 MigrationIterationImpl iteration = new MigrationIterationImpl(
                            (String) nodeProperties.get(ContentModel.PROP_NAME),
                            (String) nodeProperties.get(ContentModel.PROP_DESCRIPTION),
                            (Date) nodeProperties.get(ContentModel.PROP_CREATED),
                            (String) nodeProperties.get(ContentModel.PROP_CREATOR)
                        );
                 iteration.setIterationRootSpace(rootSpace);

                 return iteration;
            }
        });

    }

    private final boolean existLabel(final String shortLabel) throws ArchiveException
    {
        ParameterCheck.mandatoryString("An iteration label", shortLabel);

        boolean exists = false;
        for(final MigrationIteration iter: getIterations())
        {
            if(iter.getIdentifier().equals(shortLabel))
            {
                exists = true;
                break;
            }
        }
        return exists;
    }

    /**
     * @param iteration
     * @param iterationRef
     */
    private void fillIetartionSequences(final MigrationIterationImpl iteration, final NodeRef iterationRef)
    {
        for(final FileInfo childFileInfo: listFileInfos(iterationRef))
        {
            final String name = childFileInfo.getName();
            final Date createdDate = childFileInfo.getCreatedDate();
            final NodeRef ref = childFileInfo.getNodeRef();

            int index;
            if((index = name.indexOf(FAIL_IMPORT_SPACE_NAME)) > -1)
            {
                iteration.withFailedImportation(ref, getDateFromSpaceName(name, FAIL_IMPORT_SPACE_NAME, index, createdDate));
            }
            else if((index = name.indexOf(TRANSFORM_SPACE_NAME)) > -1)
            {
                iteration.withTransformationDate(ref, getDateFromSpaceName(name, TRANSFORM_SPACE_NAME, index, createdDate));
            }
            else if((index = name.indexOf(IMPORT_SPACE_NAME)) > -1)
            {
                iteration.withImportedDate(ref, getDateFromSpaceName(name, IMPORT_SPACE_NAME, index, createdDate));
            }
            else if(name.equals(ORIGINAL_SPACE_NAME))
            {
                final NodeRef originalRef = nodeService.getChildByName(ref, ContentModel.ASSOC_CONTAINS, ORIGINAL_XML_FILE);
                iteration.setOriginalFileNodeRef(originalRef);
                final NodeRef igLogsRef = nodeService.getChildByName(ref, ContentModel.ASSOC_CONTAINS, LOGFILES_PROP_FILE);
                iteration.setOriginalIgLogsFileNodeRef(igLogsRef);
            }
            else if(logger.isWarnEnabled())
            {
                logger.warn("Unexpected folder name under the migration iteration folder '" + iteration.getIdentifier() + "': " + name );
            }
        }
    }

    /**
     * @param iterationRef
     * @return
     */
    private List<FileInfo> listFileInfos(final NodeRef iterationRef)
    {
        /*final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<FileInfo>>()
        {
            public List<FileInfo> execute() throws Throwable
            {*/
                return fileFolderService.listFolders(iterationRef);
            /*}
        });*/
    }

    private final String format(Date date)
    {
        if(date == null)
        {
            return _DATE_FORMAT.get().format(new Date());
        }
        else
        {
            return _DATE_FORMAT.get().format(date);
        }

    }

    
	public synchronized OutputStream getContentOutputStream(final NodeRef resource)
    {
        final ContentWriter contentWriter= contentService.getWriter(resource, ContentModel.PROP_CONTENT, true);
        final OutputStream outputStream = contentWriter.getContentOutputStream();
        return outputStream;
    }
	
	public ContentWriter getContentWriter(NodeRef resource)
	{
		
		return contentService.getWriter(resource, ContentModel.PROP_CONTENT, true);
	}

    private Date getDateFromSpaceName(final String name, final String prefix, int index, final Date defaultDate)
    {
        final String dateAsString = name.substring(index + prefix.length());
        try
        {
            final Date parsedDate = _DATE_FORMAT.get().parse(dateAsString);
            return parsedDate;
        }
        catch (ParseException e)
        {
            logger.error("The space name '" + name + "' is invalid and not specify the date. The creation date " + defaultDate + " will be used.", e);

            return defaultDate;
        }
    }

    private final NodeRef getEtlSpace(final MigrationIteration iteration, final Date date) throws ArchiveException
    {
        ParameterCheck.mandatory("A process date ", date);
        return getOrCreateDatedSpace(getIterationSpace(iteration), TRANSFORM_SPACE_NAME, TRANSFORM_SPACE_DESCRIPTION, date);
    }

    private final NodeRef getImportSpace(final MigrationIteration iteration, final Date date) throws ArchiveException
    {
        ParameterCheck.mandatory("A process date ", date);
        return getOrCreateDatedSpace(getIterationSpace(iteration), IMPORT_SPACE_NAME, IMPORT_SPACE_DESCRIPTION, date);
    }

    private final NodeRef getIterationSpace(final MigrationIteration iteration) throws ArchiveException
    {
        ParameterCheck.mandatory("An iteration", iteration);
        ParameterCheck.mandatoryString("An iteration identifier", iteration.getIdentifier());
        return getOrCreateSpace(getMigrationRootSpace(), iteration.getIdentifier(), iteration.getDescription());
    }

    private NodeRef getLastWithChildName(final Map<NodeRef, Date> map, final String childName)
    {
        if(map.size() < 1)
        {
            return null;
        }
        else
        {
            Date lastDate = null;
            NodeRef lastRef = null;
            for(final Map.Entry<NodeRef, Date> entry: map.entrySet())
            {
                if(lastDate == null || entry.getValue().after(lastDate))
                {
                    lastDate = entry.getValue();
                    lastRef = entry.getKey();
                }
            }

            return nodeService.getChildByName(lastRef, ContentModel.ASSOC_CONTAINS, childName);

        }
    }

    private final NodeRef getOrCreateDatedSpace(final NodeRef parent, final String name, final String description, final Date date) throws ArchiveException
    {
        final String dateAsString = format(date);
        return getOrCreateSpace(parent, name +  dateAsString, MessageFormat.format(description, dateAsString));
    }

    private final NodeRef getOrCreateFile(final NodeRef parent, final String name, final String title, final String description) throws ArchiveException
    {
        return getOrCreateNode(parent, ContentModel.TYPE_CONTENT, name, title, description);
    }


    private final NodeRef getOrCreateNode(final NodeRef parent, final QName type, final String name, final String title, final String description) throws ArchiveException
    {
        return getOrCreateNode(parent, type, name, title, description, null, false);
    }

    private final NodeRef store(final NodeRef parent, final String name, final InputStream stream, final String title, final String description) throws ArchiveException
    {
        return getOrCreateNode(parent, ContentModel.TYPE_CONTENT, name, title, description, stream, false);
    }

    private NodeRef storeDocument(final NodeRef parent, final String name, final Document document, final String title, final String description) throws ArchiveException
	{
    	return getOrCreateNodeDocument(parent, ContentModel.TYPE_CONTENT, name, title, description, document, false);
	}
    
	private synchronized final NodeRef getOrCreateNode(final NodeRef parent, final QName type, final String name, final String title, final String description, final InputStream defaultContent, boolean newTransaction) throws ArchiveException
    {
        ParameterCheck.mandatory("parent space", parent);
        ParameterCheck.mandatoryString("a node name", name);
        ParameterCheck.mandatory("a node type ", type);

        final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                try
                {
                    final String typeStr = type.getLocalName();

                    NodeRef node = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);

                    if(node == null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("The " + typeStr + " '" + name + "' is not created yet. Try to create it ... ");
                        }

                        final FileInfo fileInfo = fileFolderService.create(parent, name, type);
                        final NodeRef createdNodeRef = fileInfo.getNodeRef();

                        if(title != null)
                        {
                            nodeService.setProperty(createdNodeRef, ContentModel.PROP_TITLE, title);
                        }
                        if(description != null)
                        {
                            nodeService.setProperty(createdNodeRef, ContentModel.PROP_DESCRIPTION, description);
                        }

                        if(logger.isInfoEnabled())
                        {
                            logger.info(typeStr + " '" + name + "' successfully create under the space " + parent);
                        }

                        node = createdNodeRef;

                        if(ContentModel.TYPE_CONTENT.equals(type))
                        {

                        	nodeService.addAspect(createdNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, Collections.singletonMap(ApplicationModel.PROP_EDITINLINE, (Serializable) Boolean.TRUE));

                            storeXmlStream(defaultContent, null, node, name);
                        }

                        return node;
                    }
                    else
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.info(typeStr + " '" + name + "' successfully retrieveid under space " + parent);
                        }

                        return node;
                    }
                }
                catch(ContentIOException e)
                {
                    throw new ArchiveException("Impossible to create a " + type + " with '" + name + "' under " + parent, e);
                }
                catch (Exception e)
                {
                    throw new ArchiveException("Unexpected error occurs during creating a " + type + " with name '" + name + "' under " + parent, e);
                }
            }
        }, false, newTransaction);
    }
	private synchronized final NodeRef getOrCreateNodeDocument(final NodeRef parent, final QName type, final String name, final String title, final String description, final Document document, boolean newTransaction) throws ArchiveException
    {
        ParameterCheck.mandatory("parent space", parent);
        ParameterCheck.mandatoryString("a node name", name);
        ParameterCheck.mandatory("a node type ", type);

        final RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        return helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                try
                {
                    final String typeStr = type.getLocalName();

                    NodeRef node = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);

                    if(node == null)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("The " + typeStr + " '" + name + "' is not created yet. Try to create it ... ");
                        }

                        final FileInfo fileInfo = fileFolderService.create(parent, name, type);
                        final NodeRef createdNodeRef = fileInfo.getNodeRef();

                        if(title != null)
                        {
                            nodeService.setProperty(createdNodeRef, ContentModel.PROP_TITLE, title);
                        }
                        if(description != null)
                        {
                            nodeService.setProperty(createdNodeRef, ContentModel.PROP_DESCRIPTION, description);
                        }

                        if(logger.isInfoEnabled())
                        {
                            logger.info(typeStr + " '" + name + "' successfully create under the space " + parent);
                        }

                        node = createdNodeRef;

                        if(ContentModel.TYPE_CONTENT.equals(type))
                        {

                        	nodeService.addAspect(createdNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, Collections.singletonMap(ApplicationModel.PROP_EDITINLINE, (Serializable) Boolean.TRUE));

                            storeXmlDocument(document, node, name);
                        }

                        return node;
                    }
                    else
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.info(typeStr + " '" + name + "' successfully retrieveid under space " + parent);
                        }

                        return node;
                    }
                }
                catch(ContentIOException e)
                {
                    throw new ArchiveException("Impossible to create a " + type + " with '" + name + "' under " + parent, e);
                }
                catch (Exception e)
                {
                    throw new ArchiveException("Unexpected error occurs during creating a " + type + " with name '" + name + "' under " + parent, e);
                }
            }
        }, false, newTransaction);
    }

    private final NodeRef getOrCreateSpace(final NodeRef parent, final String name, final String description) throws ArchiveException
    {
        return getOrCreateNode(parent, ContentModel.TYPE_FOLDER, name, null, description);
    }

    private final NodeRef getUploadSpace(final MigrationIteration iteration) throws ArchiveException
    {
        return getOrCreateSpace(getIterationSpace(iteration), ORIGINAL_SPACE_NAME, ORIGINAL_SPACE_DESCRIPTION);
    }

    /**
     * @return
     * @throws ArchiveException
     */
    private NodeRef getValidUsersFile() throws ArchiveException
    {
        return getOrCreateFile(getMigrationRootSpace(), VALID_USER_LIST_FILE, VALID_USER_LIST_TITLE, VALID_USER_LIST_DESC);
    }

    private void storeXmlStream(final InputStream stream, final String content, final NodeRef contentNodeRef, final String fileName) throws InvalidNodeRefException, InvalidTypeException, ContentIOException
    {
        // get a writer for the content and put the file
        final ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);

        // set the mimetype and encoding
        if(fileName.endsWith(".xml"))
        {
            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        }
        else
        {
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        }
        writer.setEncoding("UTF-8");

        if(stream == null)
        {
            writer.putContent(content == null ? "" : content);
        }
        else
        {
            writer.putContent(stream);
        }
    }
    
    private void storeXmlDocument(final Document document,  final NodeRef contentNodeRef, final String fileName) throws IOException  
    {
    	File tempFile = TempFileProvider.createTempFile(fileName, "tmp");
		FileWriter fileWriter = new FileWriter(tempFile) ;
		document.write(fileWriter);
        // get a writer for the content and put the file
        final ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);

        // set the mimetype and encoding
        if(fileName.endsWith(".xml"))
        {
            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        }
        else
        {
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        }
        writer.setEncoding("UTF-8");
        writer.putContent(tempFile);
        
        //tempFile.delete();
    }

    /**
     * @param propFile
     * @return
     * @throws IOException
     */
    private synchronized Properties extractProperties(final NodeRef propFile) throws IOException
    {
        final Properties props = new Properties();

        InputStream contentInputStream = null;
        try 
        {
        	contentInputStream = getContentInputStream(propFile);
        	props.load(contentInputStream);
        }
        finally
        {
        	if (contentInputStream != null)
		    {
        		try 
        		{
        			contentInputStream.close();
        		}
        		catch (IOException ex) {
    				logger.warn("Could not close InputStream", ex);
    			}
		    }
        }

        if(logger.isDebugEnabled())
        {
             logger.debug(props.size() + " elements found.");
        }
        return props;
    }

    /**
     * @param propFile
     * @return
     * @throws IOException
     */
    private void storeProperties(final NodeRef propFile, final Properties props, final String comment) throws IOException
    {
        final OutputStream contentOutputStream = getContentOutputStream(propFile);
        props.store(contentOutputStream, comment);
        contentOutputStream.flush();
        contentOutputStream.close();

        if(logger.isDebugEnabled())
        {
             logger.debug("Property file successfully persisted: " + propFile);
        }

    }
    /*-----
         IOC
     -----*/

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }


    /**
     * @param dictionaryPath the dictionaryPath to set
     */
    public final void setDictionaryPath(String dictionaryPath)
    {
        this.dictionaryPath = dictionaryPath;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public final void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param xmlArchivesSpace the xmlArchivesSpace to set
     */
    public final void setXmlArchivesSpace(String xmlArchivesSpace)
    {
        this.xmlArchivesSpace = xmlArchivesSpace;
    }

    public final void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

	public void setManagementService(ManagementService managementService)
	{
		this.managementService = managementService;
	}

	

}
