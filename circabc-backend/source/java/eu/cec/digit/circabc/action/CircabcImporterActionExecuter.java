/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.action;

import eu.cec.digit.circabc.action.config.ImportConfig;
import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.service.bulk.BulkService;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CircabcImporterActionExecuter extends ImporterActionExecuter {

    public static final String NAME = "circabc-import";
    public static final String PARAM_DELETE_FILE = "delete-file";
    public static final String PARAM_NOTIFY_USER = "notify-user";
    public static final String PARAM_UPDATE_CONTENT = "update-content";
    public static final String PARAM_DISABLE_FILE_NOTIFICATION = "disable-file-notification";

    public static final String PARAM_ENCODING = "encoding";
    public static final String PARAM_DESTINATION_FOLDER = "destination";

    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String TEMP_FILE_SUFFIX_ACP = ".acp";
    private static final String TEMP_FILE_SUFFIX_ZIP = ".zip";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(CircabcImporterActionExecuter.class);
    private NodeService myNodeService;
    private ContentService myContentService;
    private MailService mailService;
    private PersonService personService;
    private LogService logService;
    private ImportConfig config;
    private ManagementService managementService;
    private ImporterService myImporterService;
    private FileFolderService myFileFolderService;
    private MimetypeService myMimetypeService;
    private BulkService bulkService;
    private BehaviourFilter policyBehaviourFilter;

    @Override
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {

        LogRecord logRecord = new LogRecord();
        try {
            Boolean deleteFile = (Boolean) ruleAction.getParameterValue(PARAM_DELETE_FILE);
            Boolean disableFileNotification = (Boolean) ruleAction
                    .getParameterValue(PARAM_DISABLE_FILE_NOTIFICATION);
            if (disableFileNotification) {
                policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            } else {
                policyBehaviourFilter.enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }
            String userToNotify = (String) ruleAction.getParameterValue(PARAM_NOTIFY_USER);

            NodeRef importDest = (NodeRef) ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
            long fileSize = getFileSize(actionedUponNodeRef);
            final long maxSizeInBytes = this.config.getMaxSizeInMegaBytes() * 1024L * 1024L;

            setLogRecord(logRecord, actionedUponNodeRef, importDest, fileSize);

            if (fileSize > maxSizeInBytes) {
                throw new IllegalStateException(
                        "File is too big to be imported maximum size in bytes : " + String
                                .valueOf(maxSizeInBytes) + " current size in bytes " + String.valueOf(fileSize));
            }

            Boolean update = false;

            if (ruleAction.getParameterValue(PARAM_UPDATE_CONTENT) != null) {
                update = (Boolean) ruleAction.getParameterValue(PARAM_UPDATE_CONTENT);
            }

            if (isZipFileWithIndexInside(actionedUponNodeRef)) {
                bulkImport(actionedUponNodeRef, importDest);

            } else {
                if (update) {
                    executeImplContentOverridable(ruleAction, actionedUponNodeRef);
                } else {
                    super.executeImpl(ruleAction, actionedUponNodeRef);
                }
            }
            if (userToNotify != null && userToNotify.length() != 0) {
                notifyUser(userToNotify);
            }
            if (deleteFile) {
                myNodeService.deleteNode(actionedUponNodeRef);
            }
        } catch (Exception e) {
            logRecord.setOK(false);
            if (logger.isErrorEnabled()) {
                logger.error("Error during import job ", e);
            }
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } finally {
            getLogService().log(logRecord);

        }

    }


    protected void bulkImport(NodeRef actionedUponNodeRef, NodeRef importDest) throws IOException {
        if (myNodeService.exists(actionedUponNodeRef) == true) {
            // The node being passed in should be an Alfresco content package
            ContentReader reader = myContentService
                    .getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                if (MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())) {
                    File tempFile = null;
                    try {
                        tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ZIP);
                        reader.getContent(tempFile);
                        List<ValidationMessage> messages = new ArrayList<>();
                        List<IndexRecord> indexRecords = getBulkService().getIndexRecords(tempFile, messages);
                        bulkService.upload(importDest, tempFile, indexRecords, messages);
                    } catch (IOException ioErr) {
                        throw new AlfrescoRuntimeException("Failed to import ZIP file.", ioErr);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (tempFile != null) {
                            boolean isDeleted = tempFile.delete();
                            if (!isDeleted && logger.isWarnEnabled()) {
                                logger.warn("Unable to delete file : " + tempFile.getCanonicalPath());
                            }

                        }
                    }
                }
            }
        }
    }


    private boolean isZipFileWithIndexInside(
            NodeRef actionedUponNodeRef) {
        boolean result = false;
        if (myNodeService.exists(actionedUponNodeRef) == true) {
            // The node being passed in should be an Alfresco content package
            ContentReader reader = myContentService
                    .getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                if (MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())) {
                    // perform an import of a standard ZIP file
                    ZipFile zipFile = null;
                    File tempFile = null;
                    try {
                        tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ZIP);
                        reader.getContent(tempFile);
                        // NOTE: This encoding allows us to workaround bug:
                        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
                        // We also try to use the extra encoding information if present
                        // ALF-2016
                        zipFile = new ZipFile(tempFile, "UTF-8", true);
                        result = (zipFile.getEntry("index.txt") != null);

                    } catch (IOException ioErr) {
                        throw new AlfrescoRuntimeException("Failed to import ZIP file.", ioErr);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (tempFile != null) {
                            boolean isDeleted = tempFile.delete();
                            if (!isDeleted && logger.isWarnEnabled()) {
                                try {
                                    logger.warn("Unable to delete file : " + tempFile.getCanonicalPath());
                                } catch (IOException e) {
                                    logger.warn("Unable to get getCanonicalPath for : " + tempFile.getPath(), e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private void setLogRecord(LogRecord logRecord, NodeRef actionedUponNodeRef,
                              NodeRef importDest, long fileSize) {
        logRecord
                .setDocumentID((Long) myNodeService.getProperty(importDest, ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(importDest);
        logRecord.setIgID((Long) myNodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
        logRecord.setIgName((String) myNodeService.getProperty(igNodeRef, ContentModel.PROP_NAME));
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        Path path = myNodeService.getPath(importDest);
        String displayPath = PathUtils.getCircabcPath(path, true);
        displayPath = displayPath.endsWith("contains") ? displayPath
                .substring(0, displayPath.length() - "contains".length()) : displayPath;
        logRecord.setPath(displayPath);
        logRecord.setService("Library");
        logRecord.setActivity("Import finished");
        logRecord.addInfo("Import into drectory ");
        logRecord.addInfo(displayPath);
        logRecord.addInfo(" file : " + (String) myNodeService
                .getProperty(actionedUponNodeRef, ContentModel.PROP_NAME));
        logRecord.addInfo(" file size : " + String.valueOf(fileSize));

    }


    private long getFileSize(NodeRef nodeRef) {
        long size = 0L;
        ContentData content = (ContentData) myNodeService
                .getProperty(nodeRef, ContentModel.PROP_CONTENT);
        if (content != null) {
            size = content.getSize();
        }
        return size;

    }

    private void notifyUser(String userToNotify) {

        String from = mailService.getNoReplyEmailAddress();
        NodeRef person = getPersonService().getPerson(userToNotify);
        String to = (String) this.myNodeService.getProperty(person, ContentModel.PROP_EMAIL);
        String subject = MailTemplate.translate("circabc_import_subject");
        String body = MailTemplate.translate("circabc_import_body");
        boolean html = false;
        // Send the message
        try {
            mailService.send(from, to, null, subject, body, html, false);
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when sending email to user that import is finished", e);
            }
        }

    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        super.addParameterDefinitions(paramList);
        paramList.add(new ParameterDefinitionImpl(PARAM_DELETE_FILE, DataTypeDefinition.BOOLEAN,
                false, getParamDisplayLabel(PARAM_DELETE_FILE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_NOTIFY_USER, DataTypeDefinition.TEXT,
                false, getParamDisplayLabel(PARAM_NOTIFY_USER)));
        paramList.add(
                new ParameterDefinitionImpl(PARAM_DISABLE_FILE_NOTIFICATION, DataTypeDefinition.BOOLEAN,
                        false, getParamDisplayLabel(PARAM_DISABLE_FILE_NOTIFICATION)));

    }

    public MailService getMailService() {
        return mailService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public NodeService getMyNodeService() {
        return myNodeService;
    }

    public void setMyNodeService(NodeService myNodeService) {
        this.myNodeService = myNodeService;
    }

    public LogService getLogService() {
        return logService;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public ImportConfig getConfig() {
        return config;
    }

    public void setConfig(ImportConfig config) {
        this.config = config;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /***
     * since alfresco will not allow this method to update if file already exists
     * we need to do it ourself
     * @param ruleAction
     * @param actionedUponNodeRef
     */
    public void executeImplContentOverridable(Action ruleAction, NodeRef actionedUponNodeRef) {
        if (myNodeService.exists(actionedUponNodeRef) == true) {
            // The node being passed in should be an Alfresco content package
            ContentReader reader = myContentService
                    .getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                NodeRef importDest = (NodeRef) ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
                if (MimetypeMap.MIMETYPE_ACP.equals(reader.getMimetype())) {
                    // perform an import of an Alfresco ACP file (special format ZIP structure)
                    File zipFile = null;
                    try {
                        // unfortunately a ZIP file can not be read directly from an input stream so we have to create
                        // a temporary file first
                        zipFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ACP);
                        reader.getContent(zipFile);

                        ACPImportPackageHandler importHandler = new ACPImportPackageHandler(zipFile,
                                (String) ruleAction.getParameterValue(PARAM_ENCODING));

                        this.myImporterService.importView(importHandler, new Location(importDest), null, null);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (zipFile != null) {
                            boolean isDeleted = zipFile.delete();
                            if (!isDeleted && logger.isWarnEnabled()) {
                                try {
                                    logger.warn("Unable to delete file : " + zipFile.getCanonicalPath());
                                } catch (IOException e) {
                                    logger.warn("Unable to get getCanonicalPath for : " + zipFile.getPath(), e);
                                }
                            }
                        }
                    }
                } else if (MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())) {
                    // perform an import of a standard ZIP file
                    ZipFile zipFile = null;
                    File tempFile = null;
                    try {
                        tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ACP);
                        reader.getContent(tempFile);
                        // NOTE: This encoding allows us to workaround bug:
                        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
                        // We also try to use the extra encoding information if present
                        // ALF-2016
                        zipFile = new ZipFile(tempFile, "UTF-8", true);

                        // build a temp dir name based on the ID of the noderef we are importing
                        // also use the long life temp folder as large ZIP files can take a while
                        File alfTempDir = TempFileProvider.getLongLifeTempDir("import");
                        File tempDir = new File(
                                alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
                        try {
                            // TODO: improve this code to directly pipe the zip stream output into the repo objects -
                            //       to remove the need to expand to the filesystem first?
                            extractFile(zipFile, tempDir.getPath());
                            importDirectoryContentOverridable(tempDir.getPath(), importDest);
                        } finally {
                            deleteDir(tempDir);
                        }
                    } catch (IOException ioErr) {
                        throw new AlfrescoRuntimeException("Failed to import ZIP file.", ioErr);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (tempFile != null) {
                            boolean isDeleted = tempFile.delete();
                            if (!isDeleted && logger.isWarnEnabled()) {
                                try {
                                    logger.warn("Unable to delete file : " + tempFile.getCanonicalPath());
                                } catch (IOException e) {
                                    logger.warn("Unable to get getCanonicalPath : for " + tempFile.getPath(), e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /***
     *
     * @param dir
     * @param root
     */
    private void importDirectoryContentOverridable(String dir, NodeRef root) {
        File topdir = new File(dir);
        for (File file : topdir.listFiles()) {
            try {

                String fileName = file.getName();
                NodeRef targetRef = myNodeService
                        .getChildByName(root, ContentModel.ASSOC_CONTAINS, fileName);

                if (file.isFile()) {

                    if (targetRef == null) {
                        // create content node based on the file name
                        FileInfo fileInfo = myFileFolderService
                                .create(root, fileName, ContentModel.TYPE_CONTENT);
                        targetRef = fileInfo.getNodeRef();

                        // add titled aspect for the read/edit properties screens
                        Map<QName, Serializable> titledProps = new HashMap<>(1, 1.0f);
                        titledProps.put(ContentModel.PROP_TITLE, fileName);
                        myNodeService.addAspect(targetRef, ContentModel.ASPECT_TITLED, titledProps);

                    }

                    NodeRef fileRef = targetRef;

                    // push the content of the file into the node

                    ContentWriter writer = myContentService
                            .getWriter(fileRef, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(myMimetypeService.guessMimetype(fileName));
                    writer.putContent(file);
                } else {

                    if (targetRef == null) {
                        // create content node based on the file name
                        FileInfo fileInfo = myFileFolderService
                                .create(root, file.getName(), ContentModel.TYPE_FOLDER);
                        targetRef = fileInfo.getNodeRef();

                        // add the uifacets aspect for the read/edit properties screens
                        myNodeService.addAspect(targetRef, ApplicationModel.ASPECT_UIFACETS, null);
                    }

                    importDirectoryContentOverridable(file.getPath(), targetRef);
                }
            } catch (FileExistsException e) {
                // TODO: add failed file info to status message?
                throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
            }
        }
    }


    /**
     * @return the myContentService
     */
    public ContentService getMyContentService() {
        return myContentService;
    }


    /**
     * @param myContentService the myContentService to set
     */
    public void setMyContentService(ContentService myContentService) {
        this.myContentService = myContentService;
    }


    /**
     * @return the myImporterService
     */
    public ImporterService getMyImporterService() {
        return myImporterService;
    }


    /**
     * @param myImporterService the myImporterService to set
     */
    public void setMyImporterService(ImporterService myImporterService) {
        this.myImporterService = myImporterService;
    }


    /**
     * @return the myFileFolderService
     */
    public FileFolderService getMyFileFolderService() {
        return myFileFolderService;
    }


    /**
     * @param myFileFolderService the myFileFolderService to set
     */
    public void setMyFileFolderService(FileFolderService myFileFolderService) {
        this.myFileFolderService = myFileFolderService;
    }


    /**
     * @return the myMimetypeService
     */
    public MimetypeService getMyMimetypeService() {
        return myMimetypeService;
    }


    /**
     * @param myMimetypeService the myMimetypeService to set
     */
    public void setMyMimetypeService(MimetypeService myMimetypeService) {
        this.myMimetypeService = myMimetypeService;
    }

    public BulkService getBulkService() {
        return bulkService;
    }

    public void setBulkService(BulkService bulkService) {
        this.bulkService = bulkService;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }


}
