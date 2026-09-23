package io.swagger.model;

/*
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
 * Data transfer object describing a bulk-import / FTP synchronization configuration.
 *
 * <p>An instance holds the settings used to import content into an Interest Group (IG) from a
 * remote FTP source. It captures the target location in the Alfresco repository (via node
 * references), the FTP connection details, and the behavioral options that drive the associated
 * import job (notifications, automatic extraction and date restrictions).
 *
 * <p>This class is a plain POJO used to carry state between the REST layer, the persistence layer
 * and the scheduled import job; it contains no business logic.
 *
 * @author beaurpi
 */
public class Configuration {

  /** Persistent identifier of this configuration. */
  private Long idConfiguration;

  /** Current status code of the configuration (e.g. enabled/disabled or job execution state). */
  private Integer status;

  /** Name of the Interest Group (IG) this configuration applies to. */
  private String igName;

  /** Node reference of the imported file within the Alfresco repository. */
  private String fileNodeRef;

  /** Node reference of the parent folder into which content is imported. */
  private String parentNodeRef;

  /** Host name or address of the remote FTP server. */
  private String ftpHost;

  /** Port used to connect to the remote FTP server. */
  private Integer ftpPort;

  /** User name used to authenticate against the FTP server. */
  private String ftpUsername;

  /** Password used to authenticate against the FTP server. */
  private String ftpPassword;

  /** Remote path on the FTP server from which content is retrieved. */
  private String ftpPath;

  /** Whether the import job should send notifications about its execution. */
  private Boolean jobNotifications;

  /** Comma-separated list of e-mail addresses to notify. */
  private String emails;

  /** Whether archive content should be automatically extracted on import. */
  private Boolean autoExtract;

  /** Restriction on the dates of content considered for import. */
  private String dateRestriction;

  /** Creates an empty configuration with all fields left unset. */
  public Configuration() {}

  /**
   * Creates a fully populated configuration.
   *
   * @param status the status code of the configuration
   * @param igName the name of the target Interest Group
   * @param fileNodeRef the node reference of the imported file
   * @param parentNodeRef the node reference of the parent folder
   * @param ftpHost the FTP server host
   * @param ftpPort the FTP server port
   * @param ftpUsername the FTP user name
   * @param ftpPassword the FTP password
   * @param ftpPath the remote FTP path
   * @param jobNotifications whether the import job should send notifications
   * @param emails the comma-separated e-mail addresses to notify
   * @param autoExtract whether archives should be automatically extracted
   * @param dateRestriction the restriction on content dates
   */
  @SuppressWarnings("java:S107") // DTO all-args constructor
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
    String dateRestriction
  ) {
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
