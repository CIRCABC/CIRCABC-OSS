/**
 * ***************************************************************************** Copyright 2006
 * European Community
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
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.log;

import io.swagger.model.db.ActivityCountDAO;
import io.swagger.model.db.LogActivityDAO;
import io.swagger.model.db.LogCountResultDAO;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.model.db.LogSearchLimitParameterDAO;
import io.swagger.model.db.LogSearchParameterDAO;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.model.db.LogTemplateDAO;
import io.swagger.model.db.UserActionLogDAO;
import io.swagger.model.db.UserNewsFeedRequest;
import io.swagger.model.db.VisitedLogRestParametersDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link LogDaoService}.
 *
 * <p>Provides persistence and querying of CIRCABC audit/activity log data by delegating to MyBatis
 * mapped statements defined under the {@code CircabcLog} namespace. All database access is performed
 * through the configured Spring {@link SqlSessionTemplate}. The service handles log records,
 * activity and template lookups, REST access logs, activity history and various reporting/statistics
 * queries used by dashboards and news feeds.
 *
 * @author Slobodan Filipovic
 *         <p>
 *         Migration 3.1 -> 3.4.6 - 02/12/2011
 */
public class IBatisLogDaoServiceImpl implements LogDaoService {

  /** Sentinel value used by callers to indicate that a user filter should be treated as {@code null}. */
  private static final String NULL_STRING = "null";
  /** Key used for the document identifier entry in MyBatis parameter maps. */
  private static final String DOCUMENT_ID = "documentId";
  /** Spring-managed MyBatis session template through which all mapped statements are executed. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Inserts a single log record into the audit log.
   *
   * @param logRecord the log record to persist
   * @return the generated identifier of the inserted record, or {@code -1} if the insert did not
   *     produce an identifier
   */
  public Long log(LogRecordDAO logRecord) {
    Long id = (long) sqlSessionTemplate.insert(
      "CircabcLog.insert_log_record",
      logRecord
    );
    return (id != null ? logRecord.getId() : -1);
  }

  /**
   * Resolves the identifier of an existing activity given its service and activity descriptions.
   *
   * @param service the service description
   * @param activity the activity description
   * @return the matching activity identifier, or {@code -1} if no matching activity exists
   */
  public Integer getActivityID(String service, String activity) {
    LogActivityDAO logActivityDAO = new LogActivityDAO(service, activity);
    Integer id = (Integer) sqlSessionTemplate.selectOne(
      "CircabcLog.select_activity_id",
      logActivityDAO
    );
    return (id != null ? id : -1);
  }

  /**
   * Inserts a new activity defined by its service and activity descriptions.
   *
   * @param service the service description
   * @param activity the activity description
   * @return the generated identifier of the newly inserted activity
   */
  public Integer insertActivity(String service, String activity) {
    LogActivityDAO logActivityDAO = new LogActivityDAO(service, activity);
    sqlSessionTemplate.insert("CircabcLog.insert_activity", logActivityDAO);
    return logActivityDAO.getId();
  }

  /**
   * Inserts a REST access log entry.
   *
   * @param logRestDAO the REST log entry to persist
   * @return the number of affected rows returned by the insert operation
   */
  public Long logRest(LogRestDAO logRestDAO) {
    return (long) sqlSessionTemplate.insert(
      "CircabcLog.insert_log_rest",
      logRestDAO
    );
  }

  /**
   * Retrieves the REST logs already visited by a given user for a specific record.
   *
   * @param id the identifier of the REST log record
   * @param username the login of the user
   * @return the list of visited REST log entries for the user and record
   */
  public List<String> getVisitedRestLogs(Integer id, String username) {
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_rest_log_by_id_user",
      new VisitedLogRestParametersDAO(username, id)
    );
  }

  /**
   * Resolves the identifier of an existing template given its method and template descriptions.
   *
   * @param method the method description associated with the template
   * @param template the template description
   * @return the matching template identifier, or {@code -1} if no matching template exists
   */
  public Integer getTemplateID(String method, String template) {
    LogTemplateDAO logTemplateDAO = new LogTemplateDAO(method, template);
    Integer id = (Integer) sqlSessionTemplate.selectOne(
      "CircabcLog.select_template_id",
      logTemplateDAO
    );
    return (id != null ? id : -1);
  }

  /**
   * Inserts a new template defined by its method and template descriptions.
   *
   * @param method the method description associated with the template
   * @param template the template description
   * @return the generated identifier of the newly inserted template
   */
  public Integer insertTemplate(String method, String template) {
    LogTemplateDAO logTemplateDAO = new LogTemplateDAO(method, template);
    sqlSessionTemplate.insert("CircabcLog.insert_template", logTemplateDAO);
    return logTemplateDAO.getId();
  }

  /**
   * Returns all known log activities.
   *
   * @return the complete list of log activities
   */
  public List<LogActivityDAO> selectLogActivities() {
    return sqlSessionTemplate.selectList("CircabcLog.select_log_activity");
  }

  /**
   * Returns the log activities matching the given activity identifier.
   *
   * @param id the activity identifier to filter by
   * @return the list of matching log activities
   */
  @Override
  public List<LogActivityDAO> selectLogActivitiesById(Long id) {
    Map<String, Long> params = HashMap.newHashMap(1);
    params.put("id", id);
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_log_activity_by_id",
      params
    );
  }

  /**
   * Searches log records matching the supplied criteria. A {@code userName} equal to the literal
   * {@code "null"} is treated as no user filter.
   *
   * @param igID the interest group identifier
   * @param userName the user login to filter by, or the literal {@code "null"} for no filter
   * @param serviceDescription the service description to filter by
   * @param activityDescription the activity description to filter by
   * @param fromDate the lower bound of the date range (inclusive)
   * @param toDate the upper bound of the date range (inclusive)
   * @return the list of matching log search results
   */
  public List<LogSearchResultDAO> search(
    Long igID,
    String userName,
    String serviceDescription,
    String activityDescription,
    Date fromDate,
    Date toDate
  ) {
    LogSearchParameterDAO params = new LogSearchParameterDAO();
    params.setIgID(igID);
    params.setFromDate(fromDate);
    params.setToDate(toDate);
    if (userName != null && userName.equals(NULL_STRING)) {
      params.setUserName(null);
    } else {
      params.setUserName(userName);
    }
    params.setActivityDescription(activityDescription);
    params.setServiceDescription(serviceDescription);

    return sqlSessionTemplate.selectList(
      "CircabcLog.select_log_records",
      params
    );
  }

  /**
   * Searches log records for a single page, applying the paging and filter values held in the given
   * criteria. A user equal to the literal {@code "null"} is treated as no user filter.
   *
   * @param criteria the search criteria, including filters and paging bounds
   * @return the page of matching log search results
   */
  public List<LogSearchResultDAO> searchPage(LogSearchCriteria criteria) {
    LogSearchLimitParameterDAO params = new LogSearchLimitParameterDAO();
    params.setIgID(criteria.getIgId());
    params.setFromDate(criteria.getFromDate());
    params.setToDate(criteria.getToDate());
    if (criteria.getUser() != null && criteria.getUser().equals(NULL_STRING)) {
      params.setUserName(null);
    } else {
      params.setUserName(criteria.getUser());
    }
    params.setActivityDescription(criteria.getMethod());
    params.setServiceDescription(criteria.getService());
    params.setStartRecord(criteria.getStartRecord());
    params.setPageSize(criteria.getPageSize());

    return sqlSessionTemplate.selectList(
      "CircabcLog.select_log_records_page",
      params
    );
  }

  /**
   * Counts the log records matching the supplied criteria. A {@code userName} equal to the literal
   * {@code "null"} is treated as no user filter.
   *
   * @param igID the interest group identifier
   * @param userName the user login to filter by, or the literal {@code "null"} for no filter
   * @param serviceDescription the service description to filter by
   * @param activityDescription the activity description to filter by
   * @param fromDate the lower bound of the date range (inclusive)
   * @param toDate the upper bound of the date range (inclusive)
   * @return the total number of matching log records
   */
  public Integer searchCount(
    Long igID,
    String userName,
    String serviceDescription,
    String activityDescription,
    Date fromDate,
    Date toDate
  ) {
    LogSearchParameterDAO params = new LogSearchParameterDAO();
    params.setIgID(igID);
    params.setFromDate(fromDate);
    params.setToDate(toDate);
    if (userName != null && userName.equals(NULL_STRING)) {
      params.setUserName(null);
    } else {
      params.setUserName(userName);
    }
    params.setActivityDescription(activityDescription);
    params.setServiceDescription(serviceDescription);

    return (Integer) sqlSessionTemplate.selectOne(
      "CircabcLog.select_log_records_count",
      params
    );
  }

  /**
   * Retrieves the full activity history for a given content item.
   *
   * @param itemID the database identifier of the content item
   * @param uuid the Alfresco node UUID of the content item
   * @return the list of history entries for the item
   */
  public List<LogSearchResultDAO> getHistory(Long itemID, String uuid) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(DOCUMENT_ID, itemID);
    params.put("uuid", uuid);
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_item_history",
      params
    );
  }

  /**
   * Counts the number of history entries for a given content item.
   *
   * @param itemID the database identifier of the content item
   * @param uuid the Alfresco node UUID of the content item
   * @return the total number of history entries for the item
   */
  public long countHistory(Long itemID, String uuid) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(DOCUMENT_ID, itemID);
    params.put("uuid", uuid);
    return (long) sqlSessionTemplate.selectOne(
      "CircabcLog.count_item_history",
      params
    );
  }

  /**
   * Retrieves a single page of the activity history for a given content item.
   *
   * @param itemID the database identifier of the content item
   * @param uuid the Alfresco node UUID of the content item
   * @param startRecord the index of the first record to return (zero-based offset)
   * @param pageSize the maximum number of records to return
   * @return the requested page of history entries for the item
   */
  public List<LogSearchResultDAO> getHistory(
    Long itemID,
    String uuid,
    long startRecord,
    long pageSize
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(DOCUMENT_ID, itemID);
    params.put("uuid", uuid);
    params.put("startRecord", startRecord);
    params.put("pageSize", pageSize);

    return sqlSessionTemplate.selectList(
      "CircabcLog.select_item_history_pagination",
      params
    );
  }

  /**
   * Inserts a batch of log records, persisting each entry individually.
   *
   * @param logRecords the log records to persist
   */
  public void logBatch(List<LogRecordDAO> logRecords) {
    for (LogRecordDAO logRecordDAO : logRecords) {
      sqlSessionTemplate.insert("CircabcLog.insert_log_record", logRecordDAO);
    }
  }

  /**
   * Deletes all log entries belonging to a given interest group.
   *
   * @param igID the interest group identifier whose logs should be deleted
   */
  public void deleteInterestgroupLog(long igID) {
    sqlSessionTemplate.delete("CircabcLog.delete_log_by_ig", igID);
  }

  /**
   * Returns the date of the most recent login for a given user.
   *
   * @param username the login of the user
   * @return the last login date, or {@code null} if the user never logged in
   */
  public Date getLastLoginDateOfUser(String username) {
    return (Date) sqlSessionTemplate.selectOne(
      "CircabcLog.select_last_login_date_of_user",
      username
    );
  }

  /**
   * Returns the number of logged actions for the previous day, grouped by hour.
   *
   * @return the per-hour action counts for yesterday
   */
  public List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour() {
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_count_actions_per_hour_yesterday"
    );
  }

  /**
   * Returns the count of logged activities for a given interest group, grouped by activity.
   *
   * @param igDbNode the database node identifier of the interest group
   * @return the list of activity counts for the interest group
   */
  public List<ActivityCountDAO> getListOfActivityCountForInterestGroup(
    Long igDbNode
  ) {
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_activity_of_interest_group",
      igDbNode
    );
  }

  /**
   * @param sqlSessionTemplate the sqlSessionTemplate to set
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Returns the date of the most recent access log entry for a given interest group.
   *
   * @param igID the interest group identifier
   * @return the last access log date, or {@code null} if none exists
   */
  public Date getLastAccessLogOnInterestGroup(long igID) {
    return (Date) sqlSessionTemplate.selectOne(
      "CircabcLog.select_last_log_for_ig",
      igID
    );
  }

  /**
   * Returns the date of the most recent update log entry for a given interest group.
   *
   * @param igID the interest group identifier
   * @return the last update log date, or {@code null} if none exists
   */
  @Override
  public Date getLastUpdateLogOnInterestGroup(long igID) {
    return (Date) sqlSessionTemplate.selectOne(
      "CircabcLog.select_last_update_log_for_ig",
      igID
    );
  }

  /**
   * Returns the most recent download actions performed by a given user.
   *
   * @param userId the identifier of the user
   * @param i the maximum number of results to return
   * @return the list of recent download action log entries
   */
  @Override
  public List<UserActionLogDAO> getRecentUserDownloads(String userId, int i) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("userId", userId);
    params.put("nbResults", i);

    return sqlSessionTemplate.selectList(
      "CircabcLog.select_download_logs_for_user",
      params
    );
  }

  /**
   * Returns the most recent upload actions performed by a given user.
   *
   * @param userId the identifier of the user
   * @param i the maximum number of results to return
   * @return the list of recent upload action log entries
   */
  @Override
  public List<UserActionLogDAO> getRecentUserUploads(String userId, int i) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("userId", userId);
    params.put("nbResults", i);

    return sqlSessionTemplate.selectList(
      "CircabcLog.select_upload_logs_for_user",
      params
    );
  }

  /**
   * Returns the identifiers of the activities that are eligible for inclusion in the user dashboard
   * news feed.
   *
   * @return the list of activity identifiers used by the news feed
   */
  @Override
  public List<Long> getUserDashboardActivityIds() {
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_activity_id_for_news_feed"
    );
  }

  /**
   * Returns the dashboard activities (news feed entries) matching the given request.
   *
   * @param request the news feed request describing the user and filtering parameters
   * @return the list of matching user dashboard activity log entries
   */
  @Override
  public List<UserActionLogDAO> getUserDashboardActivities(
    UserNewsFeedRequest request
  ) {
    return sqlSessionTemplate.selectList(
      "CircabcLog.select_group_news_feed_uploads",
      request
    );
  }

  /**
   * Returns the REST log rows that still need to be processed (e.g. correlated with the audit log).
   *
   * @return the list of REST log entries pending processing
   */
  @Override
  public List<LogRestDAO> getRowsToProcess() {
    return sqlSessionTemplate.selectList("CircabcLog.select_rest_log");
  }

  /**
   * Resolves the activity identifier associated with a given template identifier.
   *
   * @param templateID the template identifier
   * @return the associated activity identifier, or {@code -1} if none is associated
   */
  @Override
  public long getActivityID(long templateID) {
    Long id = (Long) sqlSessionTemplate.selectOne(
      "CircabcLog.select_activity_id_by_template_id",
      templateID
    );
    return (id != null ? id : -1);
  }

  /**
   * Links a REST log entry to its corresponding audit log record by updating the CIRCABC log id.
   *
   * @param id the identifier of the REST log entry to update
   * @param logId the identifier of the audit log record to associate
   */
  @Override
  public void updateRest(long id, long logId) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("id", id);
    params.put("logId", logId);
    sqlSessionTemplate.update("CircabcLog.update_cbc_log_id", params);
  }
}
