/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import eu.europa.ec.digit.circabc.rest.action.CircabcImporterActionExecuter;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.DBLogServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import io.swagger.model.Configuration;
import io.swagger.model.LogRecord;
import io.swagger.util.PathUtils;
import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link AutoUploadManagementService}.
 *
 * <p>The auto-upload feature lets CIRCABC periodically fetch a file from a configured (typically
 * remote/FTP) source and push its content into a document node in the Alfresco repository. This
 * service centralises the management of that feature by combining two concerns:
 *
 * <ul>
 *   <li>Persistence of {@link Configuration} records (delegated to the injected {@link
 *       AutoUploadConfigurationService}) that describe which document is auto-uploaded, from where,
 *       and who should be notified.
 *   <li>The Alfresco repository operations that support the feature: creating/updating document
 *       content, installing or removing the inbound rule that auto-extracts uploaded ZIP archives,
 *       extracting archives on demand, sending success/error notifications, writing audit log
 *       records, and coordinating concurrent job execution through a distributed lock.
 * </ul>
 *
 * <p>This is a Spring-managed service bean; its collaborators are wired via {@code @Autowired}.
 *
 * @author beaurpi
 */
public class AutoUploadManagementServiceImpl
  implements AutoUploadManagementService
{

  /** Prefix used to build the distributed lock key for an auto-upload job (per configuration id). */
  private static final String AUTOUPLOAD = "autoupload";
  /** A logger for the class */
  static final Log logger = LogFactory.getLog(
    AutoUploadManagementServiceImpl.class
  );
  /** Title identifying the inbound rule that auto-extracts uploaded ZIP archives. */
  private static final String CIRCABC_EXTRACT_RULE_NAME =
    "CIRCABCRuleAutoExtract";
  /** Human-readable description associated with the auto-extract rule. */
  private static final String CIRCABC_EXTRACT_RULE_DESC =
    "Auto extract files inside one ZIP";

  @Autowired
  private AutoUploadConfigurationService autoUploadConfigurationService;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private ActionService actionService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private DBLogServiceImpl logService;

  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  private LockService circabcLockService;

  /**
   * Persists a new auto-upload configuration.
   *
   * @param config the configuration to register
   * @throws SQLException if the configuration cannot be stored
   * @see AutoUploadManagementService#registerConfiguration(Configuration)
   */
  @Override
  public void registerConfiguration(Configuration config) throws SQLException {
    autoUploadConfigurationService.registerConfiguration(config);
  }

  /**
   * Lists the auto-upload configurations defined for a given Interest Group.
   *
   * @param igName the identifier/name of the Interest Group whose configurations are requested
   * @return the list of matching configurations (possibly empty)
   * @throws SQLException if the configurations cannot be read
   * @see AutoUploadManagementService#listConfigurations(String)
   */
  @Override
  public List<Configuration> listConfigurations(String igName)
    throws SQLException {
    return autoUploadConfigurationService.listConfigurations(igName);
  }

  /**
   * Deletes an existing auto-upload configuration.
   *
   * @param config the configuration to delete
   * @throws SQLException if the configuration cannot be deleted
   * @see AutoUploadManagementService#deleteConfiguration(Configuration)
   */
  @Override
  public void deleteConfiguration(Configuration config) throws SQLException {
    autoUploadConfigurationService.deleteConfiguration(config);
  }

  /**
   * Updates an existing auto-upload configuration.
   *
   * @param config the configuration carrying the new values
   * @throws SQLException if the configuration cannot be updated
   * @see AutoUploadManagementService#updateConfiguration(Configuration)
   */
  @Override
  public void updateConfiguration(Configuration config) throws SQLException {
    autoUploadConfigurationService.updateConfiguration(config);
  }

  /**
   * Retrieves a single auto-upload configuration by its database identifier.
   *
   * @param idConfig the identifier of the configuration to load
   * @return the matching configuration, or {@code null} if none exists
   * @throws SQLException if the configuration cannot be read
   * @see AutoUploadManagementService#getConfigurationById(Integer)
   */
  @Override
  public Configuration getConfigurationById(Integer idConfig)
    throws SQLException {
    return autoUploadConfigurationService.getConfigurationById(idConfig);
  }

  /**
   * Builds (but does not save) the default inbound rule that automatically extracts uploaded ZIP
   * archives into the target space.
   *
   * <p>The rule matches inbound content with the {@code application/zip} MIME type and runs the
   * {@link CircabcImporterActionExecuter} asynchronously, importing the archive contents into the
   * given space, deleting the source ZIP, updating existing content and suppressing per-file
   * notifications.
   *
   * @param spaceRef the space (folder) node that will host the rule and receive the extracted files
   * @return the configured, unsaved {@link Rule}
   * @see AutoUploadManagementService#buildDefaultExtractRule(NodeRef)
   */
  @Override
  @SuppressWarnings("java:S3252")
  public Rule buildDefaultExtractRule(NodeRef spaceRef) {
    Rule rule = new Rule();
    rule.setTitle(CIRCABC_EXTRACT_RULE_NAME);
    rule.setDescription(CIRCABC_EXTRACT_RULE_DESC);
    rule.applyToChildren(false);
    rule.setExecuteAsynchronously(true);
    rule.setRuleDisabled(false);
    rule.setRuleType(RuleType.INBOUND);
    CompositeAction compositeAction = actionService.createCompositeAction();
    rule.setAction(compositeAction);

    // Conditions for the Rule
    Map<String, Serializable> actionMap = new HashMap<>();

    actionMap.put(CompareMimeTypeEvaluator.PARAM_VALUE, "application/zip");
    compositeAction.addActionCondition(
      actionService.createActionCondition(
        CompareMimeTypeEvaluator.NAME,
        actionMap
      )
    );

    // Action
    Action myAction = actionService.createAction(
      CircabcImporterActionExecuter.NAME
    );

    Map<String, Serializable> parameterValues = new HashMap<>();
    parameterValues.put(
      ImporterActionExecuter.PARAM_DESTINATION_FOLDER,
      spaceRef
    );
    parameterValues.put(CircabcImporterActionExecuter.PARAM_DELETE_FILE, true);
    parameterValues.put(
      CircabcImporterActionExecuter.PARAM_UPDATE_CONTENT,
      true
    );
    parameterValues.put(
      CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION,
      true
    );

    myAction.setParameterValues(parameterValues);

    compositeAction.addAction(myAction);
    return rule;
  }

  /**
   * Builds and saves the default auto-extract rule on the given space.
   *
   * @param spaceRef the space (folder) node to which the auto-extract rule is added
   * @see AutoUploadManagementService#addAutoExtractRuleToSpace(NodeRef)
   */
  @Override
  public void addAutoExtractRuleToSpace(NodeRef spaceRef) {
    // Add Rule
    Rule rule = buildDefaultExtractRule(spaceRef);

    // Save the rule
    ruleService.saveRule(spaceRef, rule);
  }

  /**
   * Removes the auto-extract rule from the given space, if present.
   *
   * <p>Only the first rule matching {@link #CIRCABC_EXTRACT_RULE_NAME} is removed.
   *
   * @param spaceRef the space (folder) node from which the auto-extract rule should be removed
   */
  @Override
  public void removeAutoExtractRule(NodeRef spaceRef) {
    List<Rule> listRules = ruleService.getRules(spaceRef);

    for (Rule rule : listRules) {
      if (rule.getTitle().equals(CIRCABC_EXTRACT_RULE_NAME)) {
        ruleService.removeRule(spaceRef, rule);
        break;
      }
    }
  }

  /**
   * Retrieves the auto-upload configuration associated with a given document node.
   *
   * @param nodeRef the node reference of the auto-uploaded document
   * @return the matching configuration, or {@code null} if none exists
   * @throws SQLException if the configuration cannot be read
   */
  @Override
  public Configuration getConfigurationByNodeRef(NodeRef nodeRef)
    throws SQLException {
    return autoUploadConfigurationService.getConfigurationByNodeRef(nodeRef);
  }

  /**
   * Lists every auto-upload configuration defined in the system, regardless of Interest Group.
   *
   * @return all configurations (possibly empty)
   * @throws SQLException if the configurations cannot be read
   */
  @Override
  public List<Configuration> listAllConfigurations() throws SQLException {
    return autoUploadConfigurationService.getAllConfigurations();
  }

  /**
   * Overwrites the content of an existing document node with the content of the supplied file.
   *
   * @param fileRef the node whose content is replaced
   * @param file the file providing the new content
   */
  @Override
  public void updateContent(NodeRef fileRef, File file) {
    ContentWriter writer = contentService.getWriter(
      fileRef,
      ContentModel.PROP_CONTENT,
      true
    );
    writer.putContent(file);
  }

  /**
   * Writes an audit log record describing the outcome of an auto-upload job run.
   *
   * <p>The record's activity is derived from the {@code result} (content update versus remote FTP
   * connection), and it is enriched with the Interest Group id/name, the document database id and
   * its CIRCABC path resolved from the configuration's node references.
   *
   * @param conf the configuration the job was run for; supplies the document and Interest Group
   *     node references
   * @param result the outcome of the job, controlling the activity label and success flag
   * @param jobResultInfo additional free-text detail stored on the log record
   */
  @Override
  public void logJobResult(
    Configuration conf,
    AutoUploadJobResult result,
    String jobResultInfo
  ) {
    LogRecord logRecord = new LogRecord();

    if (
      result.equals(AutoUploadJobResult.JOB_OK) ||
      result.equals(AutoUploadJobResult.JOB_ERROR)
    ) {
      logRecord.setActivity("Update Content");
    } else if (result.equals(AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM)) {
      logRecord.setActivity("Remote FTP Connection");
    }

    logRecord.setService("Auto-upload");

    logRecord.setOK((result.equals(AutoUploadJobResult.JOB_OK)));

    logRecord.setDate(new Date());

    Long igId = Long.parseLong(
      nodeService
        .getProperty(new NodeRef(conf.getIgName()), ContentModel.PROP_NODE_DBID)
        .toString()
    );
    logRecord.setIgID(igId);

    logRecord.setUser("admin");
    logRecord.setInfo(jobResultInfo);

    Long docId = Long.parseLong(
      nodeService
        .getProperty(
          new NodeRef(conf.getFileNodeRef()),
          ContentModel.PROP_NODE_DBID
        )
        .toString()
    );
    logRecord.setDocumentID(docId);

    logRecord.setIgName(
      nodeService
        .getProperty(new NodeRef(conf.getIgName()), ContentModel.PROP_NAME)
        .toString()
    );

    Path path = nodeService.getPath(new NodeRef(conf.getFileNodeRef()));
    logRecord.setPath(PathUtils.getCircabcPath(path, true));

    logService.log(logRecord);
  }

  /**
   * Sends an email notification about the outcome of an auto-upload job.
   *
   * <p>Recipients are taken from the comma-separated {@code emails} field of the configuration. The
   * mail template used depends on the {@code result} (success, error, or remote FTP problem). Any
   * exception raised while sending is caught and logged so that notification failures do not break
   * the job.
   *
   * @param conf the configuration whose document node and recipient list drive the notification
   * @param result the job outcome selecting the mail template to use
   */
  @Override
  public void sendJobNofitication(
    Configuration conf,
    AutoUploadJobResult result
  ) {
    List<String> mails = new ArrayList<>();

    if (conf.getEmails() != null) {
      Collections.addAll(mails, conf.getEmails().split(","));
    }

    try {
      if (result.equals(AutoUploadJobResult.JOB_OK)) {
        this.notificationService.notify(
          new NodeRef(conf.getFileNodeRef()),
          mails,
          MailTemplate.AUTO_UPLOAD_SUCCESS
        );
      } else if (result.equals(AutoUploadJobResult.JOB_ERROR)) {
        this.notificationService.notify(
          new NodeRef(conf.getFileNodeRef()),
          mails,
          MailTemplate.AUTO_UPLOAD_ERROR
        );
      } else if (result.equals(AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM)) {
        this.notificationService.notify(
          new NodeRef(conf.getFileNodeRef()),
          mails,
          MailTemplate.AUTO_UPLOAD_FTP_PROBLEM
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during notification phase of auto upload for document" +
            conf.getFileNodeRef(),
          e
        );
      }
    }
  }

  /**
   * Extracts the given ZIP archive node into its parent folder by executing the CIRCABC importer
   * action asynchronously.
   *
   * <p>Unlike the inbound rule built by {@link #buildDefaultExtractRule(NodeRef)}, this on-demand
   * extraction keeps the source archive (it is not deleted) and imports its content, encoded as
   * UTF-8, into the archive's current parent folder while suppressing per-file notifications.
   *
   * @param fileRef the ZIP archive node to extract
   */
  @Override
  public void extractZip(NodeRef fileRef) {
    // build the action params map based on the bean's current state
    Map<String, Serializable> params = new HashMap<>(2, 1.0f);
    params.put(
      ImporterActionExecuter.PARAM_DESTINATION_FOLDER,
      nodeService.getParentAssocs(fileRef).get(0).getParentRef()
    );
    params.put(ImporterActionExecuter.PARAM_ENCODING, "UTF-8");
    params.put(CircabcImporterActionExecuter.PARAM_DELETE_FILE, false);
    params.put(CircabcImporterActionExecuter.PARAM_NOTIFY_USER, "");
    params.put(CircabcImporterActionExecuter.PARAM_UPDATE_CONTENT, true);
    params.put(
      CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION,
      true
    );

    // build the action to execute
    Action action = actionService.createAction(
      CircabcImporterActionExecuter.NAME,
      params
    );
    action.setExecuteAsynchronously(true);

    // execute the action on the ACP file
    actionService.executeAction(action, fileRef);
  }

  /**
   * Checks whether the given node still exists in the repository.
   *
   * @param nodeRef the node reference to test
   * @return {@code true} if a node with the given reference exists, {@code false} otherwise
   */
  public boolean documentExists(NodeRef nodeRef) {
    return nodeService.exists(nodeRef);
  }

  /**
   * Creates (or reuses) a content document in the destination folder and fills it with the content
   * of a temporary file, running inside a retrying transaction.
   *
   * <p>If a child node with the given name already exists under the destination folder it is
   * returned unchanged; otherwise a new {@code cm:content} node is created, tagged with a
   * description marking it as auto-upload generated, and its content is written with a MIME type
   * guessed from the file name.
   *
   * @param fileRef unused; retained for interface compatibility/API consistency
   * @param tmpFile the temporary file providing the content to store
   * @param destinationFolder the folder under which the document is created or looked up
   * @param fileName the name of the document to create or reuse
   * @return the node reference of the created or existing document
   */
  @Override
  // NOSONAR: fileRef parameter is unused but required by interface contract for API consistency
  public NodeRef createContent(
    NodeRef fileRef, // NOSONAR - required by interface
    final File tmpFile,
    final NodeRef destinationFolder,
    final String fileName
  ) {
    RetryingTransactionHelper helper =
      transactionService.getRetryingTransactionHelper();
    return helper.doInTransaction(
      new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
        public NodeRef execute() throws Throwable {
          NodeRef node = nodeService.getChildByName(
            destinationFolder,
            ContentModel.ASSOC_CONTAINS,
            fileName
          );

          if (node == null) {
            final NodeRef createdNodeRef = fileFolderService
              .create(destinationFolder, fileName, ContentModel.TYPE_CONTENT)
              .getNodeRef();
            nodeService.setProperty(
              createdNodeRef,
              ContentModel.PROP_DESCRIPTION,
              "Document created by auto upload job"
            );

            final ContentWriter writer = contentService.getWriter(
              createdNodeRef,
              ContentModel.PROP_CONTENT,
              true
            );

            writer.setMimetype(mimetypeService.guessMimetype(fileName));

            writer.putContent(tmpFile);

            return createdNodeRef;
          }

          return node;
        }
      },
      false,
      true
    );
  }

  /**
   * Attempts to acquire the distributed lock guarding a job for the given configuration, preventing
   * concurrent runs of the same auto-upload job.
   *
   * <p>Any failure while locking (for example an already-locked item) is caught and logged, and the
   * method reports a failure to acquire.
   *
   * @param idConfiguration the identifier of the configuration whose job should be locked
   * @return {@code 1} if the lock was acquired by this call, {@code 0} if it was already locked or
   *     could not be acquired
   */
  @Override
  public Integer lockJobFile(Long idConfiguration) {
    int result = 0;

    try {
      if (!circabcLockService.isLocked(AUTOUPLOAD + idConfiguration)) {
        circabcLockService.lock(AUTOUPLOAD + idConfiguration);
        result = 1;
      }
    } catch (Exception e) {
      // result will be returned as 0
      logger.warn("Cannot lock an already locked item: " + idConfiguration, e);
    }

    return result;
  }

  /**
   * Releases the distributed lock guarding a job for the given configuration, if it is currently
   * held.
   *
   * @param idConfiguration the identifier of the configuration whose job lock should be released
   * @return {@code 1} if a held lock was released, {@code 0} if no lock was held
   */
  @Override
  public Integer unlockJobFile(Long idConfiguration) {
    Integer result = 0;

    if (circabcLockService.isLocked(AUTOUPLOAD + idConfiguration)) {
      circabcLockService.unlock(AUTOUPLOAD + idConfiguration);
      result = 1;
    }

    return result;
  }
}
