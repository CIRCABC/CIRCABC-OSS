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
package eu.cec.digit.circabc.repo.statistics.ig;

import java.util.Date;

/** @author morleal */

public class IgStatisticsParameter {

  private Long igId;
  private Date requestDate;
  private Date creationDate;
  private Integer nbUsers;
  private Integer libraryFolderCount;
  private Integer libraryDocumentCount;
  private Long librarySize;
  private Integer informationFolderCount;
  private Integer informationDocumentCount;
  private Long informationSize;
  private Integer versionCount;
  private Long versionSize;
  private Long totalSize;
  private Integer eventCount;
  private Integer meetingCount;
  private Integer forumCount;
  private Integer topicCount;
  private Integer postCount;
  private Integer maxLevel;
  private Integer customizationAndHiddenContentCount;
  private Long customizationAndHiddenContentSize;

  public IgStatisticsParameter() {}

  public IgStatisticsParameter(
    Long igId,
    Date requestDate,
    Date creationDate,
    Integer nbUsers,
    Integer libraryFolderCount,
    Integer libraryDocumentCount,
    Long librarySize,
    Integer informationFolderCount,
    Integer informationDocumentCount,
    Long informationSize,
    Integer versionCount,
    Long versionSize,
    Long totalSize,
    Integer eventCount,
    Integer meetingCount,
    Integer forumCount,
    Integer topicCount,
    Integer postCount,
    Integer maxLevel,
    Integer customizationAndHiddenContentCount,
    Long customizationAndHiddenContentSize
  ) {
    this.igId = igId;
    this.requestDate = requestDate;
    this.creationDate = creationDate;
    this.nbUsers = nbUsers;
    this.libraryFolderCount = libraryFolderCount;
    this.libraryDocumentCount = libraryDocumentCount;
    this.librarySize = librarySize;
    this.informationFolderCount = informationFolderCount;
    this.informationDocumentCount = informationDocumentCount;
    this.informationSize = informationSize;
    this.versionCount = versionCount;
    this.versionSize = versionSize;
    this.totalSize = totalSize;
    this.eventCount = eventCount;
    this.meetingCount = meetingCount;
    this.forumCount = forumCount;
    this.topicCount = topicCount;
    this.postCount = postCount;
    this.maxLevel = maxLevel;
    this.customizationAndHiddenContentCount =
      customizationAndHiddenContentCount;
    this.customizationAndHiddenContentSize = customizationAndHiddenContentSize;
  }

  public Long getIgId() {
    return igId;
  }

  public void setIgId(Long igId) {
    this.igId = igId;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Integer getNbUsers() {
    return nbUsers;
  }

  public void setNbUsers(Integer nbUsers) {
    this.nbUsers = nbUsers;
  }

  public Integer getLibraryFolderCount() {
    return libraryFolderCount;
  }

  public void setLibraryFolderCount(Integer libraryFolderCount) {
    this.libraryFolderCount = libraryFolderCount;
  }

  public Integer getLibraryDocumentCount() {
    return libraryDocumentCount;
  }

  public void setLibraryDocumentCount(Integer libraryDocumentCount) {
    this.libraryDocumentCount = libraryDocumentCount;
  }

  public Long getLibrarySize() {
    return librarySize;
  }

  public void setLibrarySize(Long librarySize) {
    this.librarySize = librarySize;
  }

  public Integer getInformationFolderCount() {
    return informationFolderCount;
  }

  public void setInformationFolderCount(Integer informationFolderCount) {
    this.informationFolderCount = informationFolderCount;
  }

  public Integer getInformationDocumentCount() {
    return informationDocumentCount;
  }

  public void setInformationDocumentCount(Integer informationDocumentCount) {
    this.informationDocumentCount = informationDocumentCount;
  }

  public Long getInformationSize() {
    return informationSize;
  }

  public void setInformationSize(Long informationSize) {
    this.informationSize = informationSize;
  }

  public Integer getVersionCount() {
    return versionCount;
  }

  public void setVersionCount(Integer versionCount) {
    this.versionCount = versionCount;
  }

  public Long getVersionSize() {
    return versionSize;
  }

  public void setVersionSize(Long versionSize) {
    this.versionSize = versionSize;
  }

  public Long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(Long totalSize) {
    this.totalSize = totalSize;
  }

  public Integer getEventCount() {
    return eventCount;
  }

  public void setEventCount(Integer eventCount) {
    this.eventCount = eventCount;
  }

  public Integer getMeetingCount() {
    return meetingCount;
  }

  public void setMeetingCount(Integer meetingCount) {
    this.meetingCount = meetingCount;
  }

  public Integer getForumCount() {
    return forumCount;
  }

  public void setForumCount(Integer forumCount) {
    this.forumCount = forumCount;
  }

  public Integer getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(Integer topicCount) {
    this.topicCount = topicCount;
  }

  public Integer getPostCount() {
    return postCount;
  }

  public void setPostCount(Integer postCount) {
    this.postCount = postCount;
  }

  public Integer getMaxLevel() {
    return maxLevel;
  }

  public void setMaxLevel(Integer maxLevel) {
    this.maxLevel = maxLevel;
  }

  public Integer getCustomizationAndHiddenContentCount() {
    return customizationAndHiddenContentCount;
  }

  public void setCustomizationAndHiddenContentCount(
    Integer customizationAndHiddenContentCount
  ) {
    this.customizationAndHiddenContentCount =
      customizationAndHiddenContentCount;
  }

  public Long getCustomizationAndHiddenContentSize() {
    return customizationAndHiddenContentSize;
  }

  public void setCustomizationAndHiddenContentSize(
    Long customizationAndHiddenContentSize
  ) {
    this.customizationAndHiddenContentSize = customizationAndHiddenContentSize;
  }
}
