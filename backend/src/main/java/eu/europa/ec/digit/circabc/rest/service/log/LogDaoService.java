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
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.model.db.UserActionLogDAO;
import io.swagger.model.db.UserNewsFeedRequest;
import java.util.Date;
import java.util.List;

/**
 * Data-access service (DAO) for CIRCABC audit and activity logging.
 *
 * <p>This interface defines the persistence operations used to record and query log data in the
 * relational database (via the iBatis/MyBatis based implementation). It covers two broad concerns:
 *
 * <ul>
 *   <li><b>Activity logging</b> &mdash; recording user actions (single or batch), resolving and
 *       inserting the service/activity and REST method/template lookup identifiers they reference.
 *   <li><b>Log querying</b> &mdash; searching, counting, paging and retrieving log history for an
 *       Interest Group, an item or a user, as well as aggregate statistics used by dashboards and
 *       reports.
 * </ul>
 *
 * <p>Implementations are expected to be stateless and thread-safe, and to translate the supplied
 * arguments directly into SQL statements without applying business rules.
 *
 * @author Slobodan Filipovic
 */
public interface LogDaoService {
  /**
   * Persists a single log record.
   *
   * @param logRecord the log record to store
   * @return the generated primary key of the inserted log row
   */
  Long log(LogRecordDAO logRecord);

  /**
   * Persists a batch of log records in a single operation.
   *
   * @param logRecord the list of log records to store
   */
  void logBatch(List<LogRecordDAO> logRecord);

  /**
   * Resolves the identifier of an existing activity for the given service/activity pair.
   *
   * @param service the service name the activity belongs to
   * @param activity the activity name
   * @return the activity identifier, or {@code null} if no matching activity exists
   */
  Integer getActivityID(String service, String activity);

  /**
   * Inserts a new activity for the given service/activity pair.
   *
   * @param service the service name the activity belongs to
   * @param activity the activity name
   * @return the identifier of the newly inserted activity
   */
  Integer insertActivity(String service, String activity);

  /**
   * Persists a single REST access log record.
   *
   * @param logRestDAO the REST log record to store
   * @return the generated primary key of the inserted REST log row
   */
  Long logRest(LogRestDAO logRestDAO);

  /**
   * Returns the URLs/paths already visited by a user for a given REST log entry, used to avoid
   * counting duplicate visits.
   *
   * @param id the REST log identifier
   * @param username the user whose visited entries are requested
   * @return the list of visited REST log paths for the user
   */
  List<String> getVisitedRestLogs(Integer id, String username);

  /**
   * Resolves the identifier of an existing REST template for the given method/template pair.
   *
   * @param method the HTTP method the template is associated with
   * @param template the template (URL pattern) name
   * @return the template identifier, or {@code null} if no matching template exists
   */
  Integer getTemplateID(String method, String template);

  /**
   * Inserts a new REST template for the given method/template pair.
   *
   * @param method the HTTP method the template is associated with
   * @param template the template (URL pattern) name
   * @return the identifier of the newly inserted template
   */
  Integer insertTemplate(String method, String template);

  /**
   * Returns all known log activities.
   *
   * @return the list of all log activities
   */
  List<LogActivityDAO> selectLogActivities();

  /**
   * Returns the log activity identified by the given id.
   *
   * @param id the activity identifier
   * @return the matching log activities (typically zero or one entry)
   */
  List<LogActivityDAO> selectLogActivitiesById(Long id);

  /**
   * Searches log records matching the supplied criteria.
   *
   * @param igID the Interest Group identifier to restrict the search to
   * @param userName the user name filter (may be {@code null} to match any user)
   * @param service the service filter (may be {@code null} to match any service)
   * @param activity the activity filter (may be {@code null} to match any activity)
   * @param fromDate the lower bound (inclusive) of the log date range
   * @param toDate the upper bound (inclusive) of the log date range
   * @return the list of log records matching the criteria
   */
  List<LogSearchResultDAO> search(
    Long igID,
    String userName,
    String service,
    String activity,
    Date fromDate,
    Date toDate
  );

  /**
   * Counts the log records matching the supplied criteria.
   *
   * @param igID the Interest Group identifier to restrict the count to
   * @param userName the user name filter (may be {@code null} to match any user)
   * @param service the service filter (may be {@code null} to match any service)
   * @param activity the activity filter (may be {@code null} to match any activity)
   * @param fromDate the lower bound (inclusive) of the log date range
   * @param toDate the upper bound (inclusive) of the log date range
   * @return the number of log records matching the criteria
   */
  Integer searchCount(
    Long igID,
    String userName,
    String service,
    String activity,
    Date fromDate,
    Date toDate
  );

  /**
   * Searches log records matching the supplied criteria and returns a single page of results.
   *
   * @param criteria the search criteria including filters and paging bounds (start record and page
   *     size)
   * @return the requested page of matching log records
   */
  List<LogSearchResultDAO> searchPage(LogSearchCriteria criteria);

  /**
   * Returns the full log history for a given item.
   *
   * @param itemID the identifier of the item
   * @param uuid the Alfresco node UUID of the item
   * @return the log history entries for the item
   */
  List<LogSearchResultDAO> getHistory(Long itemID, String uuid);

  /**
   * Counts the log history entries for a given item.
   *
   * @param itemID the identifier of the item
   * @param uuid the Alfresco node UUID of the item
   * @return the number of history entries for the item
   */
  long countHistory(Long itemID, String uuid);

  /**
   * Returns a single page of the log history for a given item.
   *
   * @param itemID the identifier of the item
   * @param uuid the Alfresco node UUID of the item
   * @param startRecord the zero-based index of the first record to return
   * @param pageSize the maximum number of records to return
   * @return the requested page of history entries for the item
   */
  List<LogSearchResultDAO> getHistory(
    Long itemID,
    String uuid,
    long startRecord,
    long pageSize
  );

  /**
   * Deletes all log entries belonging to the given Interest Group.
   *
   * @param igID the Interest Group identifier whose log entries are to be removed
   */
  void deleteInterestgroupLog(long igID);

  /**
   * Returns the date of the most recent login of the given user.
   *
   * @param username the user name
   * @return the last login date, or {@code null} if the user never logged in
   */
  Date getLastLoginDateOfUser(String username);

  /**
   * Returns the number of actions logged during the previous day, broken down per hour.
   *
   * @return the per-hour action counts for yesterday
   */
  List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour();

  /**
   * Returns the count of actions grouped by activity for the given Interest Group.
   *
   * @param igDbNode the database node identifier of the Interest Group
   * @return the per-activity action counts for the Interest Group
   */
  List<ActivityCountDAO> getListOfActivityCountForInterestGroup(Long igDbNode);

  /**
   * Returns the date of the last access (read) log entry recorded for the given Interest Group.
   *
   * @param igID the Interest Group identifier
   * @return the last access log date, or {@code null} if none exists
   */
  Date getLastAccessLogOnInterestGroup(long igID);

  /**
   * Returns the date of the last update (modification) log entry recorded for the given Interest
   * Group.
   *
   * @param igID the Interest Group identifier
   * @return the last update log date, or {@code null} if none exists
   */
  Date getLastUpdateLogOnInterestGroup(long igID);

  /**
   * Returns the most recent download actions performed by the given user.
   *
   * @param userId the user identifier
   * @param i the maximum number of download actions to return
   * @return the list of recent user download actions
   */
  List<UserActionLogDAO> getRecentUserDownloads(String userId, int i);

  /**
   * Returns the most recent upload actions performed by the given user.
   *
   * @param userId the user identifier
   * @param i the maximum number of upload actions to return
   * @return the list of recent user upload actions
   */
  List<UserActionLogDAO> getRecentUserUploads(String userId, int i);

  /**
   * Returns the identifiers of the activities that are relevant for the user dashboard news feed.
   *
   * @return the list of activity identifiers used by the user dashboard
   */
  List<Long> getUserDashboardActivityIds();

  /**
   * Returns the activities to display in the user dashboard news feed for the given request.
   *
   * @param request the news feed request describing the user and paging/filtering parameters
   * @return the list of user dashboard activities matching the request
   */
  List<UserActionLogDAO> getUserDashboardActivities(
    UserNewsFeedRequest request
  );

  /**
   * Returns the REST log rows that still need to be post-processed.
   *
   * @return the list of REST log rows pending processing
   */
  List<LogRestDAO> getRowsToProcess();

  /**
   * Resolves the activity identifier associated with the given REST template.
   *
   * @param templateID the REST template identifier
   * @return the associated activity identifier
   */
  long getActivityID(long templateID);

  /**
   * Links a processed REST log row to its resulting log entry.
   *
   * @param id the REST log row identifier
   * @param logId the identifier of the log entry to associate with the REST log row
   */
  void updateRest(long id, long logId);
}
