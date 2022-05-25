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
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import eu.cec.digit.circabc.service.config.auto.upload.AutoUploadManagementService;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClient;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClientImpl;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import it.sauronsoftware.ftp4j.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class ManageAutoUploadDialog extends BaseWaiDialog {

    /** A logger for the class */
    final static Log logger = LogFactory.getLog(ManageAutoUploadDialog.class);
    /**
     *
     */
    private static final long serialVersionUID = -7468065233150680217L;
    private List<Configuration> listOfConfigurations;
    private AutoUploadManagementService autoUploadManagementService;
    private String host;
    private String port;
    private String path;
    private String username;
    private String password;
    private String emails;
    private Boolean contentNotifications;
    private Boolean jobNotifications;
    private Integer status;
    private List<SelectItem> availableDayChoices;
    private List<SelectItem> availableHourOptions;
    private Integer selectedDayChoice;
    private Integer selectedHourChoice;
    private Boolean autoExtract;
    private Long idConfiguration;
    private Integer testResult;
    private NodeRef librarySection;
    private NodeService nodeService;

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        nodeService = super.getNodeService();

        initConfigurationList();

        buildSelectors();

        //initInputs(null);

    }

    public String addConfiguration(ActionEvent event) {
        Boolean inputsAreValid = validateInputs();

        if (inputsAreValid) {
            Configuration conf = buildConfigurationFromInputs();

            if (idConfiguration != null) {
                try {
                    autoUploadManagementService.updateConfiguration(conf);

                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                            translate("manage_auto_upload_insert_configuration_success"));
                } catch (SQLException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error during inserting auto upload configuration", e);
                    }
                    Utils.addErrorMessage(translate("manage_auto_upload_insert_configuration_error"));
                }

            } else {
                try {
                    //https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2618
                    conf.setStatus(0);
                    autoUploadManagementService.registerConfiguration(conf);
                } catch (SQLException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error during updating auto upload configuration", e);
                    }
                    Utils.addErrorMessage(translate("manage_auto_upload_update_configuration_error"));
                }

            }
        }

        initConfigurationList();

        return null;
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

    private Configuration buildConfigurationFromInputs() {

        Configuration conf = new Configuration();
        conf.setFtpHost(host);
        conf.setFtpPort(Integer.parseInt(port));
        conf.setFtpUsername(username);
        conf.setFtpPassword(password);

        path = path.replace("\\", "/");

        conf.setFtpPath(path);
        conf.setEmails(emails);
        conf.setFileNodeRef(null);
        conf.setParentNodeRef(this.librarySection.toString());
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
        if (this.librarySection == null) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_target"));
        }
        if (this.path.contains("../") || this.path.contains("./") || this.path.contains("\\\\")) {
            result = false;
            Utils.addErrorMessage(translate("configure_auto_upload_dialog_invalid_path_items"));
        }

        return result;
    }

    public String initInputs(ActionEvent event) {

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
        librarySection = null;

        return null;
    }

    @Override
    public String cancel() {
        initInputs(null);
        return super.cancel();
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
     *
     */
    public void initConfigurationList() {
        try {

            this.listOfConfigurations = autoUploadManagementService
                    .listConfigurations(this.getActionNode().getNodeRef().toString());

            for (Configuration listOfConfiguration : listOfConfigurations) {
                if (listOfConfiguration.getFileNodeRef() != null) {
                    if (super.getNodeService().exists(new NodeRef(listOfConfiguration.getFileNodeRef()))) {

                        listOfConfiguration.setFileNodeRef(super.getNodeService()
                                .getProperty(new NodeRef(listOfConfiguration.getFileNodeRef()),
                                        ContentModel.PROP_NAME).toString());
                    }
                }
                if (listOfConfiguration.getParentNodeRef() != null) {
                    if (super.getNodeService().exists(new NodeRef(listOfConfiguration.getParentNodeRef()))) {
                        listOfConfiguration.setParentNodeRef(
                                getPathFromContentNode(new NodeRef(listOfConfiguration.getParentNodeRef())));
                    }
                }
            }

        } catch (SQLException e) {

            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during listing all auto upload configurations of IG: " + super.getNodeService()
                                .getProperty(this.getActionNode().getNodeRef(), ContentModel.PROP_NAME).toString(),
                        e);
            }

        }
    }


    @Override
    public String getPageIconAltText() {
        return translate("manage_auto_upload_dialog_description");
    }

    @Override
    public String getBrowserTitle() {
        return translate("manage_auto_upload_dialog_title");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        return outcome;
    }

    /**
     * @return the listOfConfigurations
     */
    public List<Configuration> getListOfConfigurations() {
        return listOfConfigurations;
    }

    /**
     * @param listOfConfigurations the listOfConfigurations to set
     */
    public void setListOfConfigurations(List<Configuration> listOfConfigurations) {
        this.listOfConfigurations = listOfConfigurations;
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

    private String getPathFromContentNode(NodeRef nodeRef) {
        String path = "";
        NodeRef tmpNodeRef = nodeRef;
        Boolean stop = false;
        Boolean firstLoop = true;

        while (!stop) {
            List<ChildAssociationRef> parent = Collections.emptyList();

            parent = nodeService.getParentAssocs(tmpNodeRef);

            if (parent.isEmpty()) {
                stop = true;
            } else {

                if (firstLoop && nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
                    tmpNodeRef = nodeRef;

                } else {
                    tmpNodeRef = parent.get(0).getParentRef();

                }

                String title = nodeService.getProperty(tmpNodeRef, ContentModel.PROP_TITLE).toString();
                /*
                 * reached library root
                 */
                if (nodeService.hasAspect(tmpNodeRef, CircabcModel.ASPECT_INFORMATION_ROOT) || nodeService
                        .hasAspect(tmpNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
                    stop = true;
                    if (firstLoop) {
                        path = title;
                    } else {
                        path = title + " > " + path;
                    }

                } else if (title.isEmpty()) {

                    if (firstLoop) {
                        path = nodeService.getProperty(tmpNodeRef, ContentModel.PROP_NAME).toString() + " > "
                                + path;
                    } else {
                        path = nodeService.getProperty(tmpNodeRef, ContentModel.PROP_NAME).toString() + " > "
                                + path;
                    }
                } else {
                    if (firstLoop) {
                        path = title;
                    } else {
                        path = title + " > " + path;
                    }
                }
            }

            firstLoop = false;
        }

        return path;
    }

    public String removeConfiguration(ActionEvent event) {

        UIActionLink uLink = (UIActionLink) event.getComponent();

        Long idConf = Long.parseLong(uLink.getParameterMap().get("idConfig"));

        Configuration conf = new Configuration();
        conf.setIdConfiguration(idConf);

        try {
            autoUploadManagementService.deleteConfiguration(conf);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during deleting auto upload configuration, id: " + idConf, e);
            }
            Utils.addErrorMessage(translate("manage_auto_upload_delete_configuration_error"));
        }

        initConfigurationList();
        return null;
    }

    public String enableConfiguration(ActionEvent event) {

        UIActionLink uLink = (UIActionLink) event.getComponent();

        Integer idConf = Integer.parseInt(uLink.getParameterMap().get("idConfig"));

        try {
            Configuration conf = autoUploadManagementService.getConfigurationById(idConf);
            conf.setStatus(1);
            autoUploadManagementService.updateConfiguration(conf);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during enabling auto upload configuration, id: " + idConf, e);
            }
            Utils.addErrorMessage(translate("manage_auto_upload_enable_configuration_error"));
        }

        initConfigurationList();
        return null;
    }

    public String disableConfiguration(ActionEvent event) {

        UIActionLink uLink = (UIActionLink) event.getComponent();

        Integer idConf = Integer.parseInt(uLink.getParameterMap().get("idConfig"));

        try {
            Configuration conf = autoUploadManagementService.getConfigurationById(idConf);
            conf.setStatus(0);
            autoUploadManagementService.updateConfiguration(conf);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during disabling auto upload configuration, id: " + idConf, e);
            }
            Utils.addErrorMessage(translate("manage_auto_upload_disable_configuration_error"));
        }

        initConfigurationList();
        return null;
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
     * @return the librarySection
     */
    public NodeRef getLibrarySection() {
        return librarySection;
    }


    /**
     * @param librarySection the librarySection to set
     */
    public void setLibrarySection(NodeRef librarySection) {
        this.librarySection = librarySection;
    }

    /**
     * @return the igId
     */
    public String getIgId() {
        final NavigableNode library = (NavigableNode) getNavigator().getCurrentIGRoot();

        // if library is null, the user probably has not rights to see it
        return library == null ? null : library.getId();
    }


    /**
     * @return the libraryId
     */
    public String getLibraryId() {
        final NavigableNode library = (NavigableNode) getNavigator().getCurrentIGRoot()
                .get(InterestGroupNode.LIBRARY_SERVICE);

        // if library is null, the user probably has not rights to see it
        return library == null ? null : library.getId();
    }


}
