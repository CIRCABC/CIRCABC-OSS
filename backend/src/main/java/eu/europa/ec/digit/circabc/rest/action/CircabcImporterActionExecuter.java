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
package eu.europa.ec.digit.circabc.rest.action;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.bulk.BulkService;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.message.ValidationMessage;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import io.swagger.model.LogRecord;
import io.swagger.util.ApiToolBox;
import io.swagger.util.PathUtils;
import jakarta.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * CIRCABC-specific Alfresco action executer that imports content into the repository.
 *
 * <p>This action extends the standard Alfresco {@link ImporterActionExecuter} and is
 * registered under the bean/action name {@link #NAME} ({@code "circabc-import"}). It is
 * typically triggered by a rule or scheduled job on an uploaded package node and imports
 * its content into a destination folder. Depending on the uploaded file and the supplied
 * parameters it supports three import strategies:
 *
 * <ul>
 *   <li><b>Bulk import</b> &mdash; when the actioned-upon node is a ZIP archive that
 *       contains an {@code index.txt} descriptor, the archive is processed through the
 *       {@link BulkService}.</li>
 *   <li><b>Content-overriding import</b> &mdash; when {@link #PARAM_UPDATE_CONTENT} is set,
 *       ACP or ZIP packages are imported so that existing nodes with matching names are
 *       updated instead of duplicated (see {@link #executeImplContentOverridable}).</li>
 *   <li><b>Standard import</b> &mdash; otherwise the default Alfresco importer behaviour is
 *       used.</li>
 * </ul>
 *
 * <p>In addition to importing, the executer enforces a maximum package size, records the
 * outcome through the {@link LogService}, optionally notifies a user by e-mail, optionally
 * deletes the source node afterwards, and can suppress per-file content notifications.
 */
public class CircabcImporterActionExecuter extends ImporterActionExecuter {

  /** Log message prefix used when a temporary file cannot be deleted. */
  private static final String UNABLE_TO_DELETE_FILE =
    "Unable to delete file : ";

  /** The registered name of this action (also its Spring bean name). */
  public static final String NAME = "circabc-import";

  /** Boolean parameter: whether the source node should be deleted after a successful import. */
  public static final String PARAM_DELETE_FILE = "delete-file";

  /** Text parameter: username of the person to notify by e-mail once the import finishes. */
  public static final String PARAM_NOTIFY_USER = "notify-user";

  /**
   * Boolean parameter: when {@code true}, existing nodes are updated in place instead of
   * being duplicated (content-overriding import).
   */
  public static final String PARAM_UPDATE_CONTENT = "update-content";

  /**
   * Boolean parameter: when {@code true}, the content-notify behaviour/aspect is disabled
   * during the import so that no per-file notifications are sent.
   */
  public static final String PARAM_DISABLE_FILE_NOTIFICATION =
    "disable-file-notification";

  /** Text parameter: character encoding used when reading ACP import packages. */
  public static final String PARAM_ENCODING = "encoding";

  /** NodeRef parameter: the destination folder that receives the imported content. */
  public static final String PARAM_DESTINATION_FOLDER = "destination";

  private static final String TEMP_FILE_PREFIX = "alf";
  private static final String TEMP_FILE_SUFFIX_ACP = ".acp";
  private static final String TEMP_FILE_SUFFIX_ZIP = ".zip";
  private static final String FAILED_TO_IMPORT_ZIP =
    "Failed to import ZIP file.";
  private static final Log logger = LogFactory.getLog(
    CircabcImporterActionExecuter.class
  );

  @Autowired
  @Qualifier("nodeService")
  private NodeService myNodeService;

  @Autowired
  @Qualifier("contentService")
  private ContentService myContentService;

  @Autowired
  @Qualifier("circabcMailService")
  private MailService mailService;

  @Autowired
  private PersonService personService;

  @Autowired
  private LogService logService;

  @Autowired
  @Qualifier("ImporterService")
  @SuppressWarnings("java:S6830") // Alfresco-defined bean name
  private ImporterService myImporterService;

  @Autowired
  @Qualifier("FileFolderService")
  @SuppressWarnings("java:S6830") // Alfresco-defined bean name
  private FileFolderService myFileFolderService;

  @Autowired
  @Qualifier("mimetypeService")
  private MimetypeService myMimetypeService;

  @Autowired
  private BulkService bulkService;

  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  @Autowired
  private ApiToolBox apiToolBox;

  /** Maximum size, in megabytes, of a package that may be imported by this action. */
  private long maxSizeInMegabytes = 20;

  /**
   * Imports the content of the actioned-upon node into the configured destination folder.
   *
   * <p>This is the action entry point invoked by the Alfresco action service. It configures
   * the content-notification behaviour, resolves the destination folder, validates the
   * package size, performs the import using the appropriate strategy, optionally notifies a
   * user and optionally deletes the source node. The outcome is always recorded through the
   * {@link LogService}, whether the import succeeds or fails.
   *
   * @param ruleAction the action being executed, carrying the import parameters
   * @param actionedUponNodeRef the node holding the package (ACP/ZIP) content to import
   * @throws AlfrescoRuntimeException if any error occurs during the import
   */
  @Override
  public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
    LogRecord logRecord = new LogRecord();
    try {
      configureNotificationBehaviour(ruleAction);
      NodeRef importDest = (NodeRef) ruleAction.getParameterValue(
        PARAM_DESTINATION_FOLDER
      );
      long fileSize = getFileSize(actionedUponNodeRef);
      validateFileSize(fileSize);
      setLogRecord(logRecord, actionedUponNodeRef, importDest, fileSize);

      performImport(ruleAction, actionedUponNodeRef, importDest);
      notifyUserIfRequired(ruleAction);
      deleteFileIfRequired(ruleAction, actionedUponNodeRef);
    } catch (Exception e) {
      logRecord.setOK(false);
      if (logger.isErrorEnabled()) {
        logger.error("Error during import job ", e);
      }
      throw new AlfrescoRuntimeException(e.getMessage(), e);
    } finally {
      logService.log(logRecord);
    }
  }

  private void configureNotificationBehaviour(Action ruleAction) {
    Boolean disableFileNotification = (Boolean) ruleAction.getParameterValue(
      PARAM_DISABLE_FILE_NOTIFICATION
    );
    if (Boolean.TRUE.equals(disableFileNotification)) {
      policyBehaviourFilter.disableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    } else {
      policyBehaviourFilter.enableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    }
  }

  private void validateFileSize(long fileSize) {
    final long maxSizeInBytes = this.maxSizeInMegabytes * 1024L * 1024L;
    if (fileSize > maxSizeInBytes) {
      throw new IllegalStateException(
        "File is too big to be imported maximum size in bytes : " +
          maxSizeInBytes +
          " current size in bytes " +
          fileSize
      );
    }
  }

  private void performImport(
    Action ruleAction,
    NodeRef actionedUponNodeRef,
    NodeRef importDest
  ) throws IOException {
    if (isZipFileWithIndexInside(actionedUponNodeRef)) {
      bulkImport(actionedUponNodeRef, importDest);
    } else if (
      Boolean.TRUE.equals(ruleAction.getParameterValue(PARAM_UPDATE_CONTENT))
    ) {
      executeImplContentOverridable(ruleAction, actionedUponNodeRef);
    } else {
      super.executeImpl(ruleAction, actionedUponNodeRef);
    }
  }

  private void notifyUserIfRequired(Action ruleAction) {
    String userToNotify = (String) ruleAction.getParameterValue(
      PARAM_NOTIFY_USER
    );
    if (userToNotify != null && !userToNotify.isEmpty()) {
      notifyUser(userToNotify);
    }
  }

  private void deleteFileIfRequired(
    Action ruleAction,
    NodeRef actionedUponNodeRef
  ) {
    if (!Boolean.TRUE.equals(ruleAction.getParameterValue(PARAM_DELETE_FILE))) {
      return;
    }
    if (!myNodeService.exists(actionedUponNodeRef)) {
      if (logger.isInfoEnabled()) {
        logger.info(
          "Node " +
            actionedUponNodeRef +
            " no longer exists, skipping deletion."
        );
      }
      return;
    }
    try {
      myNodeService.deleteNode(actionedUponNodeRef);
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Failed to delete node " + actionedUponNodeRef + " after import.",
          e
        );
      }
    }
  }

  /**
   * Performs a bulk import of a ZIP archive (containing an {@code index.txt} descriptor)
   * into the destination folder.
   *
   * <p>The content of the actioned-upon node is streamed to a temporary file, parsed by the
   * {@link BulkService} into index records and validation messages, and then uploaded into
   * the destination. If the node no longer exists, or its content is not a ZIP archive, the
   * method returns without doing anything. The temporary file is always removed afterwards.
   *
   * @param actionedUponNodeRef the node holding the ZIP archive to import
   * @param importDest the destination folder that receives the imported content
   * @throws IOException if the archive cannot be read
   * @throws AlfrescoRuntimeException if the import fails
   */
  protected void bulkImport(NodeRef actionedUponNodeRef, NodeRef importDest)
    throws IOException {
    if (!myNodeService.exists(actionedUponNodeRef)) return;

    ContentReader reader = myContentService.getReader(
      actionedUponNodeRef,
      ContentModel.PROP_CONTENT
    );
    if (
      reader == null || !MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())
    ) return;

    File tempFile = null;
    try {
      tempFile = TempFileProvider.createTempFile(
        TEMP_FILE_PREFIX,
        TEMP_FILE_SUFFIX_ZIP
      );
      reader.getContent(tempFile);
      List<ValidationMessage> messages = new ArrayList<>();
      List<IndexRecord> indexRecords = bulkService.getIndexRecords(
        tempFile,
        messages
      );
      bulkService.upload(importDest, tempFile, indexRecords, messages);
    } catch (IOException ioErr) {
      throw new AlfrescoRuntimeException(FAILED_TO_IMPORT_ZIP, ioErr);
    } finally {
      deleteTempFile(tempFile);
    }
  }

  private boolean isZipFileWithIndexInside(NodeRef actionedUponNodeRef) {
    if (!myNodeService.exists(actionedUponNodeRef)) return false;

    ContentReader reader = myContentService.getReader(
      actionedUponNodeRef,
      ContentModel.PROP_CONTENT
    );
    if (
      reader == null || !MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())
    ) return false;

    ZipFile zipFile = null;
    File tempFile = null;
    try {
      tempFile = TempFileProvider.createTempFile(
        TEMP_FILE_PREFIX,
        TEMP_FILE_SUFFIX_ZIP
      );
      reader.getContent(tempFile);
      zipFile = ZipFile.builder()
        .setFile(tempFile)
        .setCharset(StandardCharsets.UTF_8)
        .setUseUnicodeExtraFields(true)
        .get();
      return zipFile.getEntry("index.txt") != null;
    } catch (IOException ioErr) {
      throw new AlfrescoRuntimeException(FAILED_TO_IMPORT_ZIP, ioErr);
    } finally {
      closeZipFile(zipFile);
      deleteTempFile(tempFile);
    }
  }

  private void setLogRecord(
    LogRecord logRecord,
    NodeRef actionedUponNodeRef,
    NodeRef importDest,
    long fileSize
  ) {
    logRecord.setDocumentID(
      (Long) myNodeService.getProperty(importDest, ContentModel.PROP_NODE_DBID)
    );
    final NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(importDest);
    logRecord.setIgID(
      (Long) myNodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
    );
    logRecord.setIgName(
      (String) myNodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)
    );
    logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
    String displayPath = getDisplayPath(importDest);
    logRecord.setPath(displayPath);
    logRecord.setService("Library");
    logRecord.setActivity("Import finished");
    logRecord.addInfo("Import into drectory ");
    logRecord.addInfo(displayPath);
    logRecord.addInfo(
      " file : " +
        myNodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME)
    );
    logRecord.addInfo(" file size : " + fileSize);
  }

  private String getDisplayPath(NodeRef importDest) {
    Path path = myNodeService.getPath(importDest);
    String displayPath = PathUtils.getCircabcPath(path, true);
    return displayPath.endsWith("contains")
      ? displayPath.substring(0, displayPath.length() - "contains".length())
      : displayPath;
  }

  private long getFileSize(NodeRef nodeRef) {
    ContentData content = (ContentData) myNodeService.getProperty(
      nodeRef,
      ContentModel.PROP_CONTENT
    );
    return content != null ? content.getSize() : 0L;
  }

  private void notifyUser(String userToNotify) {
    String from = mailService.getNoReplyEmailAddress();
    NodeRef person = personService.getPerson(userToNotify);
    String to = (String) myNodeService.getProperty(
      person,
      ContentModel.PROP_EMAIL
    );
    try {
      mailService.send(
        from,
        to,
        null,
        MailTemplate.translate("circabc_import_subject"),
        MailTemplate.translate("circabc_import_body"),
        false,
        false
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error when sending email to user that import is finished",
          e
        );
      }
    }
  }

  /**
   * Registers the CIRCABC-specific parameter definitions for this action in addition to the
   * ones declared by the base {@link ImporterActionExecuter}.
   *
   * <p>Adds definitions for {@link #PARAM_DELETE_FILE}, {@link #PARAM_NOTIFY_USER} and
   * {@link #PARAM_DISABLE_FILE_NOTIFICATION}.
   *
   * @param paramList the list of parameter definitions to be populated
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    super.addParameterDefinitions(paramList);
    paramList.add(
      new ParameterDefinitionImpl(
        PARAM_DELETE_FILE,
        DataTypeDefinition.BOOLEAN,
        false,
        getParamDisplayLabel(PARAM_DELETE_FILE)
      )
    );
    paramList.add(
      new ParameterDefinitionImpl(
        PARAM_NOTIFY_USER,
        DataTypeDefinition.TEXT,
        false,
        getParamDisplayLabel(PARAM_NOTIFY_USER)
      )
    );
    paramList.add(
      new ParameterDefinitionImpl(
        PARAM_DISABLE_FILE_NOTIFICATION,
        DataTypeDefinition.BOOLEAN,
        false,
        getParamDisplayLabel(PARAM_DISABLE_FILE_NOTIFICATION)
      )
    );
  }

  /**
   * Imports an ACP or ZIP package while overriding the content of any existing nodes whose
   * names match, rather than creating duplicates.
   *
   * <p>The content type of the actioned-upon node determines the handling: ACP packages are
   * imported through the Alfresco {@link ImporterService}, whereas ZIP packages are
   * extracted and their entries mapped onto existing nodes (updating them) or created when
   * absent. If the node no longer exists, has no readable content, or is of an unsupported
   * mimetype, the method returns without doing anything.
   *
   * @param ruleAction the action being executed, carrying the import parameters (including
   *     {@link #PARAM_DESTINATION_FOLDER} and {@link #PARAM_ENCODING})
   * @param actionedUponNodeRef the node holding the ACP/ZIP package to import
   */
  public void executeImplContentOverridable(
    Action ruleAction,
    NodeRef actionedUponNodeRef
  ) {
    if (!myNodeService.exists(actionedUponNodeRef)) return;

    ContentReader reader = myContentService.getReader(
      actionedUponNodeRef,
      ContentModel.PROP_CONTENT
    );
    if (reader == null) return;

    NodeRef importDest = (NodeRef) ruleAction.getParameterValue(
      PARAM_DESTINATION_FOLDER
    );
    String mimetype = reader.getMimetype();

    if (MimetypeMap.MIMETYPE_ACP.equals(mimetype)) {
      importAcpFile(ruleAction, reader, importDest);
    } else if (MimetypeMap.MIMETYPE_ZIP.equals(mimetype)) {
      importZipFile(actionedUponNodeRef, reader, importDest);
    }
  }

  private void importAcpFile(
    Action ruleAction,
    ContentReader reader,
    NodeRef importDest
  ) {
    File zipFile = null;
    try {
      zipFile = TempFileProvider.createTempFile(
        TEMP_FILE_PREFIX,
        TEMP_FILE_SUFFIX_ACP
      );
      reader.getContent(zipFile);
      ACPImportPackageHandler importHandler = new ACPImportPackageHandler(
        zipFile,
        (String) ruleAction.getParameterValue(PARAM_ENCODING)
      );
      myImporterService.importView(
        importHandler,
        new Location(importDest),
        null,
        null
      );
    } finally {
      deleteTempFile(zipFile);
    }
  }

  private void importZipFile(
    NodeRef actionedUponNodeRef,
    ContentReader reader,
    NodeRef importDest
  ) {
    ZipFile zipFile = null;
    File tempFile = null;
    try {
      tempFile = TempFileProvider.createTempFile(
        TEMP_FILE_PREFIX,
        TEMP_FILE_SUFFIX_ACP
      );
      reader.getContent(tempFile);
      zipFile = ZipFile.builder()
        .setFile(tempFile)
        .setCharset(StandardCharsets.UTF_8)
        .setUseUnicodeExtraFields(true)
        .get();

      File alfTempDir = TempFileProvider.getLongLifeTempDir("import");
      File tempDir = new File(
        alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId()
      );
      try {
        extractFile(zipFile, tempDir.getPath());
        importDirectoryContentOverridable(tempDir.getPath(), importDest);
      } finally {
        deleteDir(tempDir);
      }
    } catch (IOException ioErr) {
      throw new AlfrescoRuntimeException(FAILED_TO_IMPORT_ZIP, ioErr);
    } finally {
      deleteTempFile(tempFile);
    }
  }

  private void importDirectoryContentOverridable(String dir, NodeRef root) {
    File topdir = new File(dir);
    for (File file : topdir.listFiles()) {
      try {
        String fileName = file.getName();
        NodeRef targetRef = myNodeService.getChildByName(
          root,
          ContentModel.ASSOC_CONTAINS,
          fileName
        );

        if (file.isFile()) {
          importFile(file, fileName, root, targetRef);
        } else {
          importFolder(file, fileName, root, targetRef);
        }
      } catch (FileExistsException e) {
        throw new AlfrescoRuntimeException(FAILED_TO_IMPORT_ZIP, e);
      }
    }
  }

  private void importFile(
    File file,
    String fileName,
    NodeRef root,
    NodeRef targetRef
  ) {
    if (targetRef == null) {
      FileInfo fileInfo = myFileFolderService.create(
        root,
        fileName,
        ContentModel.TYPE_CONTENT
      );
      targetRef = fileInfo.getNodeRef();
      Map<QName, Serializable> titledProps = new HashMap<>(1, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, fileName);
      myNodeService.addAspect(
        targetRef,
        ContentModel.ASPECT_TITLED,
        titledProps
      );
    }
    ContentWriter writer = myContentService.getWriter(
      targetRef,
      ContentModel.PROP_CONTENT,
      true
    );
    writer.setMimetype(myMimetypeService.guessMimetype(fileName));
    writer.putContent(file);
  }

  private void importFolder(
    File file,
    String fileName,
    NodeRef root,
    NodeRef targetRef
  ) {
    if (targetRef == null) {
      FileInfo fileInfo = myFileFolderService.create(
        root,
        fileName,
        ContentModel.TYPE_FOLDER
      );
      targetRef = fileInfo.getNodeRef();
      myNodeService.addAspect(
        targetRef,
        ApplicationModel.ASPECT_UIFACETS,
        null
      );
    }
    importDirectoryContentOverridable(file.getPath(), targetRef);
  }

  private void deleteTempFile(File tempFile) {
    if (tempFile == null) return;
    try {
      java.nio.file.Files.delete(tempFile.toPath());
    } catch (IOException e) {
      if (logger.isWarnEnabled()) {
        logger.warn(UNABLE_TO_DELETE_FILE + tempFile.getPath(), e);
      }
    }
  }

  private void closeZipFile(ZipFile zipFile) {
    if (zipFile == null) return;
    try {
      zipFile.close();
    } catch (IOException e) {
      logger.warn("Unable to close zip file", e);
    }
  }
}
