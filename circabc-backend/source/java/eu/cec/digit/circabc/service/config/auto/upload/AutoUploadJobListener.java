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
package eu.cec.digit.circabc.service.config.auto.upload;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import eu.cec.digit.circabc.repo.lock.DBLockServiceImpl;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClient;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClientImpl;
import it.sauronsoftware.ftp4j.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/** @author beaurpi */
public class AutoUploadJobListener implements Job {

    /** A logger for the class */
    static final Log logger = LogFactory.getLog(AutoUploadJobListener.class);
    private static final String FILE_PROCESSED = ".old";
    private DBLockServiceImpl circabcLockService;
    private AutoUploadManagementService autoUploadManagementService;
    private SimpleFtpClient ftpClient;

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        AuthenticationUtil.setRunAsUserSystem();

        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        String enabledOnHostname = (String) jobData.get("enabledOnHostname");

        if (enabledOnHostname != null && !enabledOnHostname.isEmpty()) {

            String hostName = null;

            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException e) {
            }

            if (hostName != null && !enabledOnHostname.equals(hostName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Skipping AutoUploadJobListener. Run on host: "
                                    + enabledOnHostname
                                    + ", this host: "
                                    + hostName);
                }
                return;
            }
        }

        logger.debug("Running AutoUploadJobListener");

        setCircabcLockService((DBLockServiceImpl) jobData.get("circabcLockService"));
        setAutoUploadManagementService(
                (AutoUploadManagementService) jobData.get("autoUploadManagementService"));

        List<Configuration> allConfigurations = getConfigurations();

        if (allConfigurations != null) {

            Date jobFiredAtDate = new Date();

            for (Configuration conf : allConfigurations) {
                Integer lock = 0;
                try {
                    /*
                     * 1 = lock successful
                     * 0 = already locked
                     */
                    lock = autoUploadManagementService.lockJobFile(conf.getIdConfiguration());

                    if (conf.getStatus() == 1 && lock == 1) {
                        if (isCronOkToStart(conf, jobFiredAtDate)) {
                            NodeRef fileRef = null;

                            if (conf.getFileNodeRef() != null) {
                                fileRef = new NodeRef(conf.getFileNodeRef());
                            } else {
                                fileRef = new NodeRef(conf.getParentNodeRef());
                            }

                            if (autoUploadManagementService.documentExists(fileRef)) {
                                processUpdate(conf, fileRef);
                            } else {
                                /*
                                 * remove configuration as file do not exist anymore
                                 */
                                conf.setStatus(-1);
                                try {
                                    autoUploadManagementService.updateConfiguration(conf);
                                } catch (SQLException e) {

                                    if (logger.isErrorEnabled()) {
                                        logger.error(
                                                "Impossible to update conf: "
                                                        + conf.getIdConfiguration()
                                                        + "for status failed",
                                                e);
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    if (lock == 1) {
                        Integer unlock = autoUploadManagementService.unlockJobFile(conf.getIdConfiguration());
                    }
                }
            }
        }

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * @param conf
     * @param fileRef
     */
    private void processUpdate(Configuration conf, NodeRef fileRef) {
        /*
         * -2 FTP Error
         * -1 error
         * 0 no update
         * 1 success update
         */
        AutoUploadJobResult jobResult = AutoUploadJobResult.JOB_NOTHING_TO_DO;
        String jobResultInfo = "";
        String step = "";

        ftpClient = new SimpleFtpClientImpl();
        String fileName = "";
        File tmpFile = null;

        try {

            step = "ftp initParameters";
            ftpClient.initParameters(
                    conf.getFtpHost(),
                    conf.getFtpPort(),
                    conf.getFtpUsername(),
                    conf.getFtpPassword(),
                    conf.getFtpPath());

            fileName = ftpClient.getFileName();

            step = "ftp verify remote file existing";
            if (ftpClient.fileExists(fileName)) {
                step = "ftp download remote file";
                tmpFile = ftpClient.downloadFile(fileName);

                if (conf.getFileNodeRef() != null) {
                    step = "update content from ftp file";
                    autoUploadManagementService.updateContent(fileRef, tmpFile);
                    jobResultInfo =
                            "AutoUpload -- OK file updated -- job id:"
                                    + conf.getIdConfiguration()
                                    + " -- ftp file: "
                                    + fileName
                                    + " -- from FTP:"
                                    + conf.getFtpHost();

                    if (conf.getAutoExtract()) {
                        autoUploadManagementService.extractZip(fileRef);
                        jobResultInfo =
                                "AutoUpload -- OK file updated+extracted -- job id:"
                                        + conf.getIdConfiguration()
                                        + " -- ftp file: "
                                        + fileName
                                        + " -- from FTP:"
                                        + conf.getFtpHost();
                    }
                } else // file not yet createde, so no update -> create content and update configuration
                {
                    step = "create content from ftp file";
                    NodeRef resultRef =
                            autoUploadManagementService.createContent(
                                    fileRef, tmpFile, new NodeRef(conf.getParentNodeRef()), fileName);
                    conf.setFileNodeRef(resultRef.toString());
                    autoUploadManagementService.updateConfiguration(conf);
                    jobResultInfo =
                            "AutoUpload -- OK file created -- job id:"
                                    + conf.getIdConfiguration()
                                    + " -- ftp file: "
                                    + fileName
                                    + " -- from FTP:"
                                    + conf.getFtpHost();

                    if (conf.getAutoExtract()) {
                        autoUploadManagementService.extractZip(resultRef);
                        jobResultInfo =
                                "AutoUpload -- OK file created+extracted -- job id:"
                                        + conf.getIdConfiguration()
                                        + " -- ftp file: "
                                        + fileName
                                        + " -- from FTP:"
                                        + conf.getFtpHost();
                    }
                }

                step = "ftp rename remote  processed file";
                ftpClient.renameRemoteFile(
                        fileName, fileName + FILE_PROCESSED + "." + new Date().getTime());

                jobResult = AutoUploadJobResult.JOB_OK;
            }

        } catch (IllegalStateException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP autoupload for file:" + fileName + " noderef:" + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP error at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (IOException e) {

            if (e.getClass().equals(java.net.ConnectException.class)) {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Error during execution of FTP : connection problem, is FTP up and running ?", e);
                }
                jobResult = AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM;
                jobResultInfo =
                        "AutoUpload -- NOT OK FTP ConnectException at ["
                                + step
                                + "] -- job id:"
                                + conf.getIdConfiguration()
                                + " -- ftp file: "
                                + fileName
                                + " -- from FTP:"
                                + conf.getFtpHost();
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Error during execution of FTP :File IO: autoupload for file:"
                                    + fileName
                                    + " noderef:"
                                    + fileRef,
                            e);
                }
                jobResult = AutoUploadJobResult.JOB_ERROR;
                jobResultInfo =
                        "AutoUpload -- NOT OK FTP IOException at ["
                                + step
                                + "] -- job id:"
                                + conf.getIdConfiguration()
                                + " -- ftp file: "
                                + fileName
                                + " -- from FTP:"
                                + conf.getFtpHost();
            }

        } catch (FTPIllegalReplyException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP :bad reply from ftp server: autoupload for file:"
                                + fileName
                                + " noderef:"
                                + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP FTPIllegalReplyException at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (FTPException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP autoupload for file:" + fileName + " noderef:" + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP FTPException at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (FTPDataTransferException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP :download problem: autoupload for file:"
                                + fileName
                                + " noderef:"
                                + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP FTPDataTransferException at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (FTPAbortedException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP :aborted process: autoupload for file:"
                                + fileName
                                + " noderef:"
                                + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP FTPAbortedException at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (FTPListParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during execution of FTP :problem for list files: autoupload for file:"
                                + fileName
                                + " noderef:"
                                + fileRef,
                        e);
            }
            jobResult = AutoUploadJobResult.JOB_ERROR;
            jobResultInfo =
                    "AutoUpload -- NOT OK FTP FTPListParseException at ["
                            + step
                            + "] -- job id:"
                            + conf.getIdConfiguration()
                            + " -- ftp file: "
                            + fileName
                            + " -- from FTP:"
                            + conf.getFtpHost();

        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Impossible to update conf: " + conf.getIdConfiguration() + "for status succeed", e);
            }
        } finally {
            ftpClient.logout();

            if (tmpFile != null) {
                boolean isDeleted = tmpFile.delete();
                if (!isDeleted && logger.isWarnEnabled()) {
                    try {
                        logger.warn("Unable to delete file : " + tmpFile.getCanonicalPath());
                    } catch (IOException e) {
                        logger.warn("Unable to get CanonicalPath for : " + tmpFile.getPath(), e);
                    }
                }
            }

            sendNotificationIfEnabled(conf, jobResult);

            logJobResult(conf, jobResult, jobResultInfo);
        }
    }

    /**
     * @param conf
     * @param jobResult
     * @param jobResultInfo
     */
    private void logJobResult(
            Configuration conf, AutoUploadJobResult jobResult, String jobResultInfo) {
        if (jobResult.equals(AutoUploadJobResult.JOB_OK)) {
            this.autoUploadManagementService.logJobResult(
                    conf, AutoUploadJobResult.JOB_OK, jobResultInfo);
        } else if (jobResult.equals(AutoUploadJobResult.JOB_ERROR)) {
            this.autoUploadManagementService.logJobResult(
                    conf, AutoUploadJobResult.JOB_ERROR, jobResultInfo);
        } else if (jobResult.equals(AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM)) {
            this.autoUploadManagementService.logJobResult(
                    conf, AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM, jobResultInfo);
        }
    }

    /**
     * @param conf
     * @param jobResult
     */
    private void sendNotificationIfEnabled(Configuration conf, AutoUploadJobResult jobResult) {
        if (conf.getJobNotifications()) {
            if (jobResult.equals(AutoUploadJobResult.JOB_OK)) {
                this.autoUploadManagementService.sendJobNofitication(conf, AutoUploadJobResult.JOB_OK);
            } else if (jobResult.equals(AutoUploadJobResult.JOB_ERROR)) {
                this.autoUploadManagementService.sendJobNofitication(conf, AutoUploadJobResult.JOB_ERROR);
            } else if (jobResult.equals(AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM)) {
                this.autoUploadManagementService.sendJobNofitication(
                        conf, AutoUploadJobResult.JOB_REMOTE_FTP_PROBLEM);
            }
        }
    }

    private List<Configuration> getConfigurations() {
        try {

            return autoUploadManagementService.listAllConfigurations();

        } catch (SQLException e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during fetching all configiration from autoupload DB", e);
            }

            return null;
        }
    }

    private Boolean isCronOkToStart(Configuration conf, Date jobFiredAtDate) {
        CronExpression cExp;
        Boolean result = null;

        try {
            cExp = new CronExpression(conf.getDateRestriction());
            result = cExp.isSatisfiedBy(jobFiredAtDate);
        } catch (ParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Impossible to parse cron expression: "
                                + conf.getDateRestriction()
                                + " for: "
                                + conf.getIdConfiguration(),
                        e);
            }
        }

        return result;
    }

    /** @return the circabcLockService */
    public DBLockServiceImpl getCircabcLockService() {
        return circabcLockService;
    }

    /** @param circabcLockService the circabcLockService to set */
    public void setCircabcLockService(DBLockServiceImpl circabcLockService) {
        this.circabcLockService = circabcLockService;
    }

    /** @return the autoUploadManagementService */
    public AutoUploadManagementService getAutoUploadManagementService() {
        return autoUploadManagementService;
    }

    /** @param autoUploadManagementService the autoUploadManagementService to set */
    public void setAutoUploadManagementService(
            AutoUploadManagementService autoUploadManagementService) {
        this.autoUploadManagementService = autoUploadManagementService;
    }

    /** @return the ftpClient */
    public SimpleFtpClient getFtpClient() {
        return ftpClient;
    }

    /** @param ftpClient the ftpClient to set */
    public void setFtpClient(SimpleFtpClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
