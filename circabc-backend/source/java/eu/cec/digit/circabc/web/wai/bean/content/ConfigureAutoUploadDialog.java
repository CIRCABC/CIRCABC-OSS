/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.bean.content;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import eu.cec.digit.circabc.service.config.auto.upload.AutoUploadManagementService;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClient;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClientImpl;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import it.sauronsoftware.ftp4j.*;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class ConfigureAutoUploadDialog extends BaseWaiDialog {

    /** A logger for the class */
    final static Log logger = LogFactory.getLog(ConfigureAutoUploadDialog.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String host;
    private String port;
    private String path;
    private String username;
    private String password;
    private String emails;
    private Boolean contentNotifications;
    private Boolean jobNotifications;
    private Boolean statusBoolean;
    private Integer status;
    private List<SelectItem> availableDayChoices;
    private List<SelectItem> availableHourOptions;
    private Integer selectedDayChoice;
    private Integer selectedHourChoice;
    private Boolean autoExtract;
    private AutoUploadManagementService autoUploadManagementService;
    private Long idConfiguration;
    private Integer testResult;

    @Override
    public String getPageIconAltText() {
        return translate("configure_auto_upload_action_description");
    }

    @Override
    public String getBrowserTitle() {
        return translate("configure_auto_upload_action_title");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        Boolean inputsAreValid = validateInputs();
        String newOutCome = outcome;

        if (inputsAreValid) {
            Configuration conf = buildConfigurationFromInputs();

            if (idConfiguration != null) {
                autoUploadManagementService.updateConfiguration(conf);
                initInputs();
            } else {
                autoUploadManagementService.registerConfiguration(conf);

            }
        } else {
            newOutCome = null;
        }

        return newOutCome;
    }

    private Boolean validateInputs() {

        Boolean result = true;

        if (!(this.host.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}") || this.host
                .matches("([a-zA-Z0-9\\-]*\\.)*[a-zA-Z0-9\\-]*"))) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_host"));

        }
        if (!this.port.matches("[0-9]*")) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_port"));
        }
        if (this.username.isEmpty()) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_login"));
        }
        if (this.password.isEmpty()) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_passwords"));
        }
        if (this.path.isEmpty()) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_path"));
        }
        if (this.path.contains("../") || this.path.contains("./") || this.path.contains("\\\\")) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_path_items"));
        }

        return result;
    }

    private Configuration buildConfigurationFromInputs() {

        Configuration conf = new Configuration();
        conf.setFtpHost(host);
        conf.setFtpPort(Integer.parseInt(port));
        conf.setFtpUsername(username);
        conf.setFtpPassword(password);
        conf.setFtpPath(path);
        conf.setEmails(emails);
        conf.setFileNodeRef(this.getActionNode().getNodeRef().toString());
        conf.setParentNodeRef(
                super.getNodeService().getParentAssocs(this.getActionNode().getNodeRef()).get(0)
                        .getParentRef().toString());
        conf.setIgName(
                super.getManagementService().getCurrentInterestGroup(this.getActionNode().getNodeRef())
                        .toString());
        conf.setAutoExtract(autoExtract);
        conf.setJobNotifications(jobNotifications);
        conf.setStatus(status);
        conf.setDateRestriction(buildCronExpression(this.selectedDayChoice, this.selectedHourChoice));
        conf.setIdConfiguration(idConfiguration);

        return conf;
    }

    private String buildCronExpression(Integer selectedDayChoice2,
                                       Integer selectedHourChoice2) {

        String cronHour = "";
        String cronDay = "";

        if (selectedHourChoice2 == -1) {
            cronHour = "*";
        } else {
            cronHour = selectedHourChoice2.toString();
        }

        if (selectedDayChoice2 == -1) {
            cronDay = "*";
        } else {
            cronDay = selectedDayChoice2.toString();
        }

        return "* * " + cronHour + " ? * " + cronDay;
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        buildSelectors();
        initInputs();

    }

    private void initInputs() {
        try {
            Configuration conf = autoUploadManagementService
                    .getConfigurationByNodeRef(this.getActionNode().getNodeRef());

            if (conf != null) {
                status = conf.getStatus();
                host = conf.getFtpHost();
                port = conf.getFtpPort().toString();
                username = conf.getFtpUsername();
                password = conf.getFtpPassword();
                path = conf.getFtpPath();
                autoExtract = conf.getAutoExtract();
                jobNotifications = conf.getJobNotifications();
                emails = conf.getEmails();
                idConfiguration = conf.getIdConfiguration();
                selectedDayChoice = retrieveDayFrequencyFromCron(conf.getDateRestriction());
                selectedHourChoice = retrieveHourFrequencyFromCron(conf.getDateRestriction());
            } else {
                resetInputs();
            }

        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during querying databse for get configuration from nodeRef:" + this
                        .getActionNode().getNodeRef(), e);
            }

            resetInputs();

        }


    }

    /**
     *
     */
    public void resetInputs() {
        status = 0;
        host = "";
        port = null;
        username = "";
        password = "";
        path = "";
        autoExtract = false;
        jobNotifications = false;
        emails = "";
        idConfiguration = null;
        selectedDayChoice = -1;
        selectedHourChoice = -1;
    }

    private Integer retrieveDayFrequencyFromCron(String dateRestriction) {

        String[] strings = dateRestriction.split(" ");
        Integer result;

        if (strings[5].equals("*")) {
            result = -1;
        } else {
            result = Integer.parseInt(strings[5]);
        }

        return result;
    }

    private Integer retrieveHourFrequencyFromCron(String dateRestriction) {

        String[] strings = dateRestriction.split(" ");
        Integer result;

        if (strings[2].equals("*")) {
            result = -1;
        } else {
            result = Integer.parseInt(strings[2]);
        }

        return result;
    }

    private void buildSelectors() {

        this.availableDayChoices = new ArrayList<>();
        this.availableDayChoices
                .add(new SelectItem(-1, translate("configure_auto_upload_dialog_every_day")));
        this.availableDayChoices
                .add(new SelectItem(2, translate("configure_auto_upload_dialog_monday")));
        this.availableDayChoices
                .add(new SelectItem(3, translate("configure_auto_upload_dialog_tuesday")));
        this.availableDayChoices
                .add(new SelectItem(4, translate("configure_auto_upload_dialog_wednesday")));
        this.availableDayChoices
                .add(new SelectItem(5, translate("configure_auto_upload_dialog_thursday")));
        this.availableDayChoices
                .add(new SelectItem(6, translate("configure_auto_upload_dialog_friday")));
        this.availableDayChoices
                .add(new SelectItem(7, translate("configure_auto_upload_dialog_saturday")));
        this.availableDayChoices
                .add(new SelectItem(1, translate("configure_auto_upload_dialog_sunday")));
        /*
         * see http://quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
         */

        this.availableHourOptions = new ArrayList<>();
        this.availableHourOptions
                .add(new SelectItem(-1, translate("configure_auto_upload_dialog_every_hour")));
        for (Integer i = 0; i < 24; i++) {
            this.availableHourOptions.add(new SelectItem(i, i.toString()));
        }

    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the contentNotifications
     */
    public Boolean getContentNotifications() {
        return contentNotifications;
    }

    /**
     * @param contentNotifications the contentNotifications to set
     */
    public void setContentNotifications(Boolean contentNotifications) {
        this.contentNotifications = contentNotifications;
    }

    /**
     * @return the jobNotifications
     */
    public Boolean getJobNotifications() {
        return jobNotifications;
    }

    /**
     * @param jobNotifications the jobNotifications to set
     */
    public void setJobNotifications(Boolean jobNotifications) {
        this.jobNotifications = jobNotifications;
    }

    /**
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return the availableDayChoices
     */
    public List<SelectItem> getAvailableDayChoices() {
        return availableDayChoices;
    }

    /**
     * @param availableDayChoices the availableDayChoices to set
     */
    public void setAvailableDayChoices(List<SelectItem> availableDayChoices) {
        this.availableDayChoices = availableDayChoices;
    }

    /**
     * @return the availableHourOptions
     */
    public List<SelectItem> getAvailableHourOptions() {
        return availableHourOptions;
    }

    /**
     * @param availableHourOptions the availableHourOptions to set
     */
    public void setAvailableHourOptions(List<SelectItem> availableHourOptions) {
        this.availableHourOptions = availableHourOptions;
    }

    /**
     * @return the selectedDayChoice
     */
    public Integer getSelectedDayChoice() {
        return selectedDayChoice;
    }

    /**
     * @param selectedDayChoice the selectedDayChoice to set
     */
    public void setSelectedDayChoice(Integer selectedDayChoice) {
        this.selectedDayChoice = selectedDayChoice;
    }

    /**
     * @return the selectedHourChoice
     */
    public Integer getSelectedHourChoice() {
        return selectedHourChoice;
    }

    /**
     * @param selectedHourChoice the selectedHourChoice to set
     */
    public void setSelectedHourChoice(Integer selectedHourChoice) {
        this.selectedHourChoice = selectedHourChoice;
    }

    /**
     * @return the emails
     */
    public String getEmails() {
        return emails;
    }

    /**
     * @param emails the emails to set
     */
    public void setEmails(String emails) {
        this.emails = emails;
    }

    /**
     * @return the autoExtract
     */
    public Boolean getAutoExtract() {
        return autoExtract;
    }

    /**
     * @param autoExtract the autoExtract to set
     */
    public void setAutoExtract(Boolean autoExtract) {
        this.autoExtract = autoExtract;
    }

    /**
     * @return the autoUploadManagementService
     */
    public AutoUploadManagementService getAutoUploadManagementService() {
        return autoUploadManagementService;
    }

    /**
     * @param autoUploadManagementService the autoUploadManagementService to set
     */
    public void setAutoUploadManagementService(
            AutoUploadManagementService autoUploadManagementService) {
        this.autoUploadManagementService = autoUploadManagementService;
    }

    /**
     * @return the idConfiguration
     */
    public Long getIdConfiguration() {
        return idConfiguration;
    }

    /**
     * @param idConfiguration the idConfiguration to set
     */
    public void setIdConfiguration(Long idConfiguration) {
        this.idConfiguration = idConfiguration;
    }

    public String testConnection(ActionEvent event) {

        SimpleFtpClient ftpTest = new SimpleFtpClientImpl();

        testResult = -2;

        try {

            ftpTest.initParameters(host, Integer.parseInt(port), username, password, path);
            testResult = 1;

            if (ftpTest.getFileName() != null) {
                if (!ftpTest.getFileName().isEmpty()) {
                    if (!ftpTest.fileExists(ftpTest.getFileName())) {
                        testResult = 0;
                    }
                }
            }


        } catch (NumberFormatException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, port number is wrong", e);
                Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_port"));
            }
        } catch (IllegalStateException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, illegal state", e);

            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, i/o exception", e);

            }
        } catch (FTPIllegalReplyException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, illegal reply from FTP server", e);

            }
        } catch (FTPException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, ftp issue -> wrong path ?", e);

            }
            testResult = -1;
        } catch (FTPDataTransferException | FTPListParseException | FTPAbortedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection", e);

            }
        }

        return null;

    }

    /**
     * @return the testResult
     */
    public Integer getTestResult() {
        return testResult;
    }

    /**
     * @param testResult the testResult to set
     */
    public void setTestResult(Integer testResult) {
        this.testResult = testResult;
    }

    /**
     * @return the statusBoolean
     */
    public Boolean getStatusBoolean() {
        return (status == 1 ? true : false);
    }

    /**
     * @param statusBoolean the statusBoolean to set
     */
    public void setStatusBoolean(Boolean statusBoolean) {
        this.statusBoolean = statusBoolean;
        if (statusBoolean) {
            status = 1;
        } else {
            status = 0;
        }
    }
}
