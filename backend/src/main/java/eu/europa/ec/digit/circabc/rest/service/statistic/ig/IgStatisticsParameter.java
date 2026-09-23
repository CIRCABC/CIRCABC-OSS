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
 *
 * @author Alain Morlet
 */
package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import java.util.Date;

/**
 * Data transfer object holding the collected statistics for a single Interest Group (IG).
 *
 * <p>An instance aggregates the metrics gathered while computing usage statistics of an IG, such as
 * the number of members, the counts and sizes of content in the Library and Information services,
 * versioning figures, event/meeting counts and Newsgroup (forum/topic/post) activity. It is a plain
 * mutable POJO: all values are populated through the setters after construction and later consumed
 * when persisting or reporting the statistics.
 */
public class IgStatisticsParameter {

  /** Identifier of the Interest Group these statistics describe. */
  private Long igId;

  /** Date on which the statistics were requested/computed. */
  private Date requestDate;

  /** Creation date of the Interest Group. */
  private Date creationDate;

  /** Number of users (members) of the Interest Group. */
  private Integer nbUsers;

  /** Number of folders in the Library service. */
  private Integer libraryFolderCount;

  /** Number of documents in the Library service. */
  private Integer libraryDocumentCount;

  /** Total size, in bytes, of the Library content. */
  private Long librarySize;

  /** Number of folders in the Information service. */
  private Integer informationFolderCount;

  /** Number of documents in the Information service. */
  private Integer informationDocumentCount;

  /** Total size, in bytes, of the Information content. */
  private Long informationSize;

  /** Number of content versions across the Interest Group. */
  private Integer versionCount;

  /** Total size, in bytes, occupied by content versions. */
  private Long versionSize;

  /** Total size, in bytes, of all content in the Interest Group. */
  private Long totalSize;

  /** Number of events in the Events service. */
  private Integer eventCount;

  /** Number of meetings in the Events service. */
  private Integer meetingCount;

  /** Number of forums in the Newsgroup service. */
  private Integer forumCount;

  /** Number of topics in the Newsgroup service. */
  private Integer topicCount;

  /** Number of posts in the Newsgroup service. */
  private Integer postCount;

  /** Maximum folder nesting depth reached within the Interest Group. */
  private Integer maxLevel;

  /** Number of customization and hidden content items. */
  private Integer customizationAndHiddenContentCount;

  /** Total size, in bytes, of customization and hidden content. */
  private Long customizationAndHiddenContentSize;

  /**
   * Creates an empty statistics parameter. This is a simple POJO/DTO whose fields are expected to be
   * populated through the corresponding setters after construction.
   */
  public IgStatisticsParameter() {
    // No initialization needed - fields are set via setters
  }

  /**
   * Returns the identifier of the Interest Group.
   *
   * @return the Interest Group id
   */
  public Long getIgId() {
    return igId;
  }

  /**
   * Sets the identifier of the Interest Group.
   *
   * @param igId the Interest Group id
   */
  public void setIgId(Long igId) {
    this.igId = igId;
  }

  /**
   * Returns the date on which the statistics were requested/computed.
   *
   * @return the request date
   */
  public Date getRequestDate() {
    return requestDate;
  }

  /**
   * Sets the date on which the statistics were requested/computed.
   *
   * @param requestDate the request date
   */
  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * Returns the creation date of the Interest Group.
   *
   * @return the creation date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Sets the creation date of the Interest Group.
   *
   * @param creationDate the creation date
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Returns the number of users (members) of the Interest Group.
   *
   * @return the number of users
   */
  public Integer getNbUsers() {
    return nbUsers;
  }

  /**
   * Sets the number of users (members) of the Interest Group.
   *
   * @param nbUsers the number of users
   */
  public void setNbUsers(Integer nbUsers) {
    this.nbUsers = nbUsers;
  }

  /**
   * Returns the number of folders in the Library service.
   *
   * @return the library folder count
   */
  public Integer getLibraryFolderCount() {
    return libraryFolderCount;
  }

  /**
   * Sets the number of folders in the Library service.
   *
   * @param libraryFolderCount the library folder count
   */
  public void setLibraryFolderCount(Integer libraryFolderCount) {
    this.libraryFolderCount = libraryFolderCount;
  }

  /**
   * Returns the number of documents in the Library service.
   *
   * @return the library document count
   */
  public Integer getLibraryDocumentCount() {
    return libraryDocumentCount;
  }

  /**
   * Sets the number of documents in the Library service.
   *
   * @param libraryDocumentCount the library document count
   */
  public void setLibraryDocumentCount(Integer libraryDocumentCount) {
    this.libraryDocumentCount = libraryDocumentCount;
  }

  /**
   * Returns the total size, in bytes, of the Library content.
   *
   * @return the library size in bytes
   */
  public Long getLibrarySize() {
    return librarySize;
  }

  /**
   * Sets the total size, in bytes, of the Library content.
   *
   * @param librarySize the library size in bytes
   */
  public void setLibrarySize(Long librarySize) {
    this.librarySize = librarySize;
  }

  /**
   * Returns the number of folders in the Information service.
   *
   * @return the information folder count
   */
  public Integer getInformationFolderCount() {
    return informationFolderCount;
  }

  /**
   * Sets the number of folders in the Information service.
   *
   * @param informationFolderCount the information folder count
   */
  public void setInformationFolderCount(Integer informationFolderCount) {
    this.informationFolderCount = informationFolderCount;
  }

  /**
   * Returns the number of documents in the Information service.
   *
   * @return the information document count
   */
  public Integer getInformationDocumentCount() {
    return informationDocumentCount;
  }

  /**
   * Sets the number of documents in the Information service.
   *
   * @param informationDocumentCount the information document count
   */
  public void setInformationDocumentCount(Integer informationDocumentCount) {
    this.informationDocumentCount = informationDocumentCount;
  }

  /**
   * Returns the total size, in bytes, of the Information content.
   *
   * @return the information size in bytes
   */
  public Long getInformationSize() {
    return informationSize;
  }

  /**
   * Sets the total size, in bytes, of the Information content.
   *
   * @param informationSize the information size in bytes
   */
  public void setInformationSize(Long informationSize) {
    this.informationSize = informationSize;
  }

  /**
   * Returns the number of content versions across the Interest Group.
   *
   * @return the version count
   */
  public Integer getVersionCount() {
    return versionCount;
  }

  /**
   * Sets the number of content versions across the Interest Group.
   *
   * @param versionCount the version count
   */
  public void setVersionCount(Integer versionCount) {
    this.versionCount = versionCount;
  }

  /**
   * Returns the total size, in bytes, occupied by content versions.
   *
   * @return the version size in bytes
   */
  public Long getVersionSize() {
    return versionSize;
  }

  /**
   * Sets the total size, in bytes, occupied by content versions.
   *
   * @param versionSize the version size in bytes
   */
  public void setVersionSize(Long versionSize) {
    this.versionSize = versionSize;
  }

  /**
   * Returns the total size, in bytes, of all content in the Interest Group.
   *
   * @return the total size in bytes
   */
  public Long getTotalSize() {
    return totalSize;
  }

  /**
   * Sets the total size, in bytes, of all content in the Interest Group.
   *
   * @param totalSize the total size in bytes
   */
  public void setTotalSize(Long totalSize) {
    this.totalSize = totalSize;
  }

  /**
   * Returns the number of events in the Events service.
   *
   * @return the event count
   */
  public Integer getEventCount() {
    return eventCount;
  }

  /**
   * Sets the number of events in the Events service.
   *
   * @param eventCount the event count
   */
  public void setEventCount(Integer eventCount) {
    this.eventCount = eventCount;
  }

  /**
   * Returns the number of meetings in the Events service.
   *
   * @return the meeting count
   */
  public Integer getMeetingCount() {
    return meetingCount;
  }

  /**
   * Sets the number of meetings in the Events service.
   *
   * @param meetingCount the meeting count
   */
  public void setMeetingCount(Integer meetingCount) {
    this.meetingCount = meetingCount;
  }

  /**
   * Returns the number of forums in the Newsgroup service.
   *
   * @return the forum count
   */
  public Integer getForumCount() {
    return forumCount;
  }

  /**
   * Sets the number of forums in the Newsgroup service.
   *
   * @param forumCount the forum count
   */
  public void setForumCount(Integer forumCount) {
    this.forumCount = forumCount;
  }

  /**
   * Returns the number of topics in the Newsgroup service.
   *
   * @return the topic count
   */
  public Integer getTopicCount() {
    return topicCount;
  }

  /**
   * Sets the number of topics in the Newsgroup service.
   *
   * @param topicCount the topic count
   */
  public void setTopicCount(Integer topicCount) {
    this.topicCount = topicCount;
  }

  /**
   * Returns the number of posts in the Newsgroup service.
   *
   * @return the post count
   */
  public Integer getPostCount() {
    return postCount;
  }

  /**
   * Sets the number of posts in the Newsgroup service.
   *
   * @param postCount the post count
   */
  public void setPostCount(Integer postCount) {
    this.postCount = postCount;
  }

  /**
   * Returns the maximum folder nesting depth reached within the Interest Group.
   *
   * @return the maximum level
   */
  public Integer getMaxLevel() {
    return maxLevel;
  }

  /**
   * Sets the maximum folder nesting depth reached within the Interest Group.
   *
   * @param maxLevel the maximum level
   */
  public void setMaxLevel(Integer maxLevel) {
    this.maxLevel = maxLevel;
  }

  /**
   * Returns the number of customization and hidden content items.
   *
   * @return the customization and hidden content count
   */
  public Integer getCustomizationAndHiddenContentCount() {
    return customizationAndHiddenContentCount;
  }

  /**
   * Sets the number of customization and hidden content items.
   *
   * @param customizationAndHiddenContentCount the customization and hidden content count
   */
  public void setCustomizationAndHiddenContentCount(
    Integer customizationAndHiddenContentCount
  ) {
    this.customizationAndHiddenContentCount =
      customizationAndHiddenContentCount;
  }

  /**
   * Returns the total size, in bytes, of customization and hidden content.
   *
   * @return the customization and hidden content size in bytes
   */
  public Long getCustomizationAndHiddenContentSize() {
    return customizationAndHiddenContentSize;
  }

  /**
   * Sets the total size, in bytes, of customization and hidden content.
   *
   * @param customizationAndHiddenContentSize the customization and hidden content size in bytes
   */
  public void setCustomizationAndHiddenContentSize(
    Long customizationAndHiddenContentSize
  ) {
    this.customizationAndHiddenContentSize = customizationAndHiddenContentSize;
  }
}
