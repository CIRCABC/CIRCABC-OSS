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
package eu.europa.ec.digit.circabc.rest.job;

import eu.europa.ec.digit.circabc.rest.service.auto.upload.AutoUploadJobResult;
import eu.europa.ec.digit.circabc.rest.service.auto.upload.AutoUploadManagementService;
import eu.europa.ec.digit.circabc.rest.service.ftp.SimpleFtpClient;
import eu.europa.ec.digit.circabc.rest.service.ftp.SimpleFtpClientImpl;
import eu.europa.ec.digit.circabc.rest.service.lock.DBLockServiceImpl;
import io.swagger.model.Configuration;
import it.sauronsoftware.ftp4j.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

/**
 * Quartz scheduled job that performs automatic uploads of files fetched from remote FTP servers
 * into the CIRCABC (Alfresco) repository.
 *
 * <p>On each fire the job iterates over every auto-upload {@link Configuration} stored in the
 * dedicated auto-upload database. For every active configuration whose cron date restriction is
 * satisfied it acquires a per-configuration database lock (to guarantee single execution across a
 * clustered deployment), connects to the configured FTP server, downloads the expected file and
 * either creates a new document or updates the existing one in the repository. When the
 * configuration requests it, downloaded archives are automatically extracted. After a successful
 * transfer the remote file is renamed (suffixed with {@code .old.<timestamp>}) so it is not
 * processed again, and an optional job notification is sent. The outcome of each run is logged via
 * the {@link AutoUploadManagementService}.
 *
 * <p>The job runs under the Alfresco system user. It can be globally disabled through the
 * {@code enabled} job data flag and can be restricted to a single host through the
 * {@code enabledOnHostname} job data value. Collaborating services
 * ({@link DBLockServiceImpl} and {@link AutoUploadManagementService}) are supplied through the
 * Quartz {@link JobDataMap}.
 */
public class AutoUploadJobListener implements Job {

  /** Log message fragment appending the source FTP host. */
  private static final String FROM_FTP = " -- from FTP:";
  /** Log message fragment appending the affected node reference. */
  private static final String NODEREF = " noderef:";
  /** Log message fragment appending the configuration (job) id. */
  private static final String JOB_ID = "] -- job id:";
  /** Log message fragment appending the FTP file name. */
  private static final String FTP_FILE = " -- ftp file: ";
  /** Suffix used to rename a remote file once it has been successfully processed. */
  private static final String FILE_PROCESSED = ".old";
  /** Logger for this job. */
  static final Log logger = LogFactory.getLog(AutoUploadJobListener.class);

  /** Database-backed lock service used to coordinate execution across cluster nodes. */
  private DBLockServiceImpl circabcLockService;
  /** Service providing access to auto-upload configurations and repository operations. */
  private AutoUploadManagementService autoUploadManagementService;
  /** FTP client used to connect to and transfer files from the remote server. */
  private SimpleFtpClient ftpClient;

  /**
   * Entry point invoked by the Quartz scheduler on each trigger.
   *
   * <p>Returns immediately when the {@code enabled} job data flag is not {@code true} or when the
   * job is restricted to a different host. Otherwise it runs as the Alfresco system user,
   * initializes the collaborating services from the job data map and processes all auto-upload
   * configurations, clearing the security context when done.
   *
   * @param context the Quartz execution context providing the job detail and merged
   *     {@link JobDataMap}
   * @throws JobExecutionException if the job cannot be executed by the scheduler
   */
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException {
    JobDataMap jobData = context.getJobDetail().getJobDataMap();
    if (
      !Boolean.parseBoolean(
        (String) context.getMergedJobDataMap().get("enabled")
      )
    ) {
      return;
    }

    AuthenticationUtil.setRunAsUserSystem();
    if (!shouldRunOnThisHost(jobData)) {
      return;
    }

    logger.debug("Running AutoUploadJobListener");
    initializeServices(jobData);
    processAllConfigurations();
    AuthenticationUtil.clearCurrentSecurityContext();
  }

  private boolean shouldRunOnThisHost(JobDataMap jobData) {
    String enabledOnHostname = (String) jobData.get("enabledOnHostname");
    if (enabledOnHostname == null || enabledOnHostname.isEmpty()) {
      return true;
    }
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      return enabledOnHostname.equals(hostName);
    } catch (UnknownHostException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Unable to get the host name of the current server.", e);
      }
      return true;
    }
  }

  private void initializeServices(JobDataMap jobData) {
    setCircabcLockService(
      (DBLockServiceImpl) jobData.get("circabcLockService")
    );
    setAutoUploadManagementService(
      (AutoUploadManagementService) jobData.get("autoUploadManagementService")
    );
  }

  private void processAllConfigurations() {
    List<Configuration> allConfigurations = getConfigurations();
    if (allConfigurations.isEmpty()) return;

    Date jobFiredAtDate = new Date();
    for (Configuration conf : allConfigurations) {
      processConfiguration(conf, jobFiredAtDate);
    }
  }

  private void processConfiguration(Configuration conf, Date jobFiredAtDate) {
    Integer lock = 0;
    try {
      lock = autoUploadManagementService.lockJobFile(conf.getIdConfiguration());
      if (shouldProcessConfiguration(conf, lock, jobFiredAtDate)) {
        NodeRef fileRef = getFileRef(conf);
        if (autoUploadManagementService.documentExists(fileRef)) {
          processUpdate(conf, fileRef);
        } else {
          markConfigurationAsFailed(conf);
        }
      }
    } finally {
      if (lock == 1) {
        autoUploadManagementService.unlockJobFile(conf.getIdConfiguration());
      }
    }
  }

  private boolean shouldProcessConfiguration(
    Configuration conf,
    Integer lock,
    Date jobFiredAtDate
  ) {
    return (
      conf.getStatus() == 1 &&
      lock == 1 &&
      Boolean.TRUE.equals(isCronOkToStart(conf, jobFiredAtDate))
    );
  }

  private NodeRef getFileRef(Configuration conf) {
    return conf.getFileNodeRef() != null
      ? new NodeRef(conf.getFileNodeRef())
      : new NodeRef(conf.getParentNodeRef());
  }

  private void markConfigurationAsFailed(Configuration conf) {
    conf.setStatus(-1);
    try {
      autoUploadManagementService.updateConfiguration(conf);
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Impossible to update conf: " +
            conf.getIdConfiguration() +
            " for status failed",
          e
        );
      }
    }
  }

  private void processUpdate(Configuration conf, NodeRef fileRef) {
    AutoUploadJobResult jobResult = AutoUploadJobResult.JOB_NOTHING_TO_DO;
    String jobResultInfo = "";
    String step = "";
    String fileName = "";
    File tmpFile = null;
    ftpClient = new SimpleFtpClientImpl();

    try {
      step = "ftp initParameters";
      ftpClient.initParameters(
        conf.getFtpHost(),
        conf.getFtpPort(),
        conf.getFtpUsername(),
        conf.getFtpPassword(),
        conf.getFtpPath()
      );
      fileName = ftpClient.getFileName();

      step = "ftp verify remote file existing";
      if (Boolean.TRUE.equals(ftpClient.fileExists(fileName))) {
        step = "ftp download remote file";
        tmpFile = ftpClient.downloadFile(fileName);
        jobResultInfo = processFileUpload(conf, fileRef, fileName, tmpFile);
        step = "ftp rename remote processed file";
        ftpClient.renameRemoteFile(
          fileName,
          fileName + FILE_PROCESSED + "." + new Date().getTime()
        );
        jobResult = AutoUploadJobResult.JOB_OK;
      }
    } catch (
      IllegalStateException
      | FTPIllegalReplyException
      | FTPException
      | FTPDataTransferException
      | FTPAbortedException
      | FTPListParseException e
    ) {
      jobResult = AutoUploadJobResult.JOB_ERROR;
      jobResultInfo = buildErrorInfo(
        "FTP " + e.getClass().getSimpleName(),
        step,
        conf,
        fileName
      );
      logFtpError(fileName, fileRef, e);
    } catch (IOException e) {
      jobResult = handleIOException(e, fileName, fileRef);
      jobResultInfo = buildErrorInfo(
        e.getClass().equals(java.net.ConnectException.class)
          ? "FTP ConnectException"
          : "FTP IOException",
        step,
        conf,
        fileName
      );
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Impossible to update conf: " +
            conf.getIdConfiguration() +
            " for status succeed",
          e
        );
      }
    } finally {
      cleanup(tmpFile);
      sendNotificationIfEnabled(conf, jobResult);
      logJobResult(conf, jobResult, jobResultInfo);
    }
  }

  private String processFileUpload(
    Configuration conf,
    NodeRef fileRef,
    String fileName,
    File tmpFile
  ) throws SQLException {
    if (conf.getFileNodeRef() != null) {
      autoUploadManagementService.updateContent(fileRef, tmpFile);
      if (Boolean.TRUE.equals(conf.getAutoExtract())) {
        autoUploadManagementService.extractZip(fileRef);
        return buildSuccessInfo("file updated+extracted", conf, fileName);
      }
      return buildSuccessInfo("file updated", conf, fileName);
    } else {
      NodeRef resultRef = autoUploadManagementService.createContent(
        fileRef,
        tmpFile,
        new NodeRef(conf.getParentNodeRef()),
        fileName
      );
      conf.setFileNodeRef(resultRef.toString());
      autoUploadManagementService.updateConfiguration(conf);
      if (Boolean.TRUE.equals(conf.getAutoExtract())) {
        autoUploadManagementService.extractZip(resultRef);
        return buildSuccessInfo("file created+extracted", conf, fileName);
      }
      return buildSuccessInfo("file created", conf, fileName);
    }
  }

  private String buildSuccessInfo(
    String action,
    Configuration conf,
    String fileName
  ) {
    return (
      "AutoUpload -- OK " +
      action +
      " -- job id:" +
      conf.getIdConfiguration() +
      FTP_FILE +
      fileName +
      FROM_FTP +
      conf.getFtpHost()
    );
  }

  private String buildErrorInfo(
    String errorType,
    String step,
    Configuration conf,
    String fileName
  ) {
    return (
      "AutoUpload -- NOT OK " +
      errorType +
      " at [" +
      step +
      JOB_ID +
      conf.getIdConfiguration() +
      FTP_FILE +
      fileName +
      FROM_FTP +
      conf.getFtpHost()
    );
  }

  private AutoUploadJobResult handleIOException(
    IOException e,
    String fileName,
    NodeRef fileRef
  ) {
    if (e.getClass().equals(java.net.ConnectException.class)) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during execution of FTP: connection problem, is FTP up and running?",
          e
        );
      }
      return AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM;
    }
    logFtpError(fileName, fileRef, e);
    return AutoUploadJobResult.JOB_ERROR;
  }

  private void logFtpError(String fileName, NodeRef fileRef, Exception e) {
    if (logger.isErrorEnabled()) {
      logger.error(
        "Error during execution of FTP autoupload for file:" +
          fileName +
          NODEREF +
          fileRef,
        e
      );
    }
  }

  private void cleanup(File tmpFile) {
    ftpClient.logout();
    if (tmpFile != null) {
      try {
        java.nio.file.Files.delete(tmpFile.toPath());
      } catch (IOException e) {
        if (logger.isWarnEnabled()) {
          logger.warn("Unable to delete file: " + tmpFile.getPath(), e);
        }
      }
    }
  }

  private void logJobResult(
    Configuration conf,
    AutoUploadJobResult jobResult,
    String jobResultInfo
  ) {
    if (jobResult != AutoUploadJobResult.JOB_NOTHING_TO_DO) {
      autoUploadManagementService.logJobResult(conf, jobResult, jobResultInfo);
    }
  }

  private void sendNotificationIfEnabled(
    Configuration conf,
    AutoUploadJobResult jobResult
  ) {
    if (
      Boolean.TRUE.equals(conf.getJobNotifications()) &&
      jobResult != AutoUploadJobResult.JOB_NOTHING_TO_DO
    ) {
      autoUploadManagementService.sendJobNofitication(conf, jobResult);
    }
  }

  private List<Configuration> getConfigurations() {
    try {
      return autoUploadManagementService.listAllConfigurations();
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during fetching all configuration from autoupload DB",
          e
        );
      }
      return Collections.emptyList();
    }
  }

  private Boolean isCronOkToStart(Configuration conf, Date jobFiredAtDate) {
    try {
      return new CronExpression(conf.getDateRestriction()).isSatisfiedBy(
        jobFiredAtDate
      );
    } catch (ParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Impossible to parse cron expression: " +
            conf.getDateRestriction() +
            " for: " +
            conf.getIdConfiguration(),
          e
        );
      }
      return Boolean.FALSE;
    }
  }

  /**
   * Returns the database-backed lock service used to coordinate execution across cluster nodes.
   *
   * @return the CIRCABC lock service
   */
  public DBLockServiceImpl getCircabcLockService() {
    return circabcLockService;
  }

  /**
   * Sets the database-backed lock service used to coordinate execution across cluster nodes.
   *
   * @param circabcLockService the CIRCABC lock service to use
   */
  public void setCircabcLockService(DBLockServiceImpl circabcLockService) {
    this.circabcLockService = circabcLockService;
  }

  /**
   * Returns the service providing access to auto-upload configurations and repository operations.
   *
   * @return the auto-upload management service
   */
  public AutoUploadManagementService getAutoUploadManagementService() {
    return autoUploadManagementService;
  }

  /**
   * Sets the service providing access to auto-upload configurations and repository operations.
   *
   * @param autoUploadManagementService the auto-upload management service to use
   */
  public void setAutoUploadManagementService(
    AutoUploadManagementService autoUploadManagementService
  ) {
    this.autoUploadManagementService = autoUploadManagementService;
  }

  /**
   * Returns the FTP client used to transfer files from the remote server.
   *
   * @return the current FTP client, or {@code null} if none has been created yet
   */
  public SimpleFtpClient getFtpClient() {
    return ftpClient;
  }

  /**
   * Sets the FTP client used to transfer files from the remote server.
   *
   * @param ftpClient the FTP client to use
   */
  public void setFtpClient(SimpleFtpClient ftpClient) {
    this.ftpClient = ftpClient;
  }
}
