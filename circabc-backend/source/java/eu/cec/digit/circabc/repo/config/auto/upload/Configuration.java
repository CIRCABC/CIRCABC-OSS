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
package eu.cec.digit.circabc.repo.config.auto.upload;

/** @author beaurpi */
public class Configuration {

    private Long idConfiguration;
    private Integer status;
    private String igName;
    private String fileNodeRef;
    private String parentNodeRef;
    private String ftpHost;
    private Integer ftpPort;
    private String ftpUsername;
    private String ftpPassword;
    private String ftpPath;
    private Boolean jobNotifications;
    private String emails;
    private Boolean autoExtract;
    private String dateRestriction;

    /** * Empty constructor */
    public Configuration() {
    }

    /**
     * * Empty constructor
     *
     * @param dateRestriction
     */
    public Configuration(
            Integer status,
            String igName,
            String fileNodeRef,
            String parentNodeRef,
            String ftpHost,
            Integer ftpPort,
            String ftpUsername,
            String ftpPassword,
            String ftpPath,
            Boolean jobNotifications,
            String emails,
            Boolean autoExtract,
            String dateRestriction) {
        this.igName = igName;
        this.status = status;
        this.fileNodeRef = fileNodeRef;
        this.parentNodeRef = parentNodeRef;
        this.ftpHost = ftpHost;
        this.ftpPort = ftpPort;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpPath = ftpPath;
        this.jobNotifications = jobNotifications;
        this.emails = emails;
        this.autoExtract = autoExtract;
        this.dateRestriction = dateRestriction;
    }

    /** @return the idConfiguration */
    public Long getIdConfiguration() {
        return idConfiguration;
    }

    /** @param idConfiguration the idConfiguration to set */
    public void setIdConfiguration(Long idConfiguration) {
        this.idConfiguration = idConfiguration;
    }

    /** @return the igName */
    public String getIgName() {
        return igName;
    }

    /** @param igName the igName to set */
    public void setIgName(String igName) {
        this.igName = igName;
    }

    /** @return the fileNodeRef */
    public String getFileNodeRef() {
        return fileNodeRef;
    }

    /** @param fileNodeRef the fileNodeRef to set */
    public void setFileNodeRef(String fileNodeRef) {
        this.fileNodeRef = fileNodeRef;
    }

    /** @return the parentNodeRef */
    public String getParentNodeRef() {
        return parentNodeRef;
    }

    /** @param parentNodeRef the parentNodeRef to set */
    public void setParentNodeRef(String parentNodeRef) {
        this.parentNodeRef = parentNodeRef;
    }

    /** @return the ftpHost */
    public String getFtpHost() {
        return ftpHost;
    }

    /** @param ftpHost the ftpHost to set */
    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    /** @return the ftpPort */
    public Integer getFtpPort() {
        return ftpPort;
    }

    /** @param ftpPort the ftpPort to set */
    public void setFtpPort(Integer ftpPort) {
        this.ftpPort = ftpPort;
    }

    /** @return the ftpUsername */
    public String getFtpUsername() {
        return ftpUsername;
    }

    /** @param ftpUsername the ftpUsername to set */
    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
    }

    /** @return the ftpPassword */
    public String getFtpPassword() {
        return ftpPassword;
    }

    /** @param ftpPassword the ftpPassword to set */
    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    /** @return the ftpPath */
    public String getFtpPath() {
        return ftpPath;
    }

    /** @param ftpPath the ftpPath to set */
    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }

    /** @return the jobNotifications */
    public Boolean getJobNotifications() {
        return jobNotifications;
    }

    /** @param jobNotifications the jobNotifications to set */
    public void setJobNotifications(Boolean jobNotifications) {
        this.jobNotifications = jobNotifications;
    }

    /** @return the emails */
    public String getEmails() {
        return emails;
    }

    /** @param emails the emails to set */
    public void setEmails(String emails) {
        this.emails = emails;
    }

    /** @return the autoExtract */
    public Boolean getAutoExtract() {
        return autoExtract;
    }

    /** @param autoExtract the autoExtract to set */
    public void setAutoExtract(Boolean autoExtract) {
        this.autoExtract = autoExtract;
    }

    /** @return the dateRestriction */
    public String getDateRestriction() {
        return dateRestriction;
    }

    /** @param dateRestriction the dateRestriction to set */
    public void setDateRestriction(String dateRestriction) {
        this.dateRestriction = dateRestriction;
    }

    /** @return the status */
    public Integer getStatus() {
        return status;
    }

    /** @param status the status to set */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
