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
package eu.cec.digit.circabc.repo.log.ibatis;

import eu.cec.digit.circabc.repo.log.*;

import java.util.Date;
import java.util.List;

/**
 * @author Slobodan Filipovic
 */
public interface LogDaoService {

    Long log(LogRecordDAO logRecord);

    void logBatch(List<LogRecordDAO> logRecord);

    Integer getActivityID(String service, String activity);

    Integer insertActivity(String service, String activity);

    Long logRest(LogRestDAO logRestDAO);

    List<String> getVisitedRestLogs(Integer id, String username);

    Integer getTemplateID(String method, String template);

    Integer insertTemplate(String method, String template);

    List<LogActivityDAO> selectLogActivities();

    List<LogActivityDAO> selectLogActivitiesById(Long id);

    List<LogSearchResultDAO> search(
            Long igID, String userName, String service, String activity, Date fromDate, Date toDate);

    Integer searchCount(
            Long igID, String userName, String service, String activity, Date fromDate, Date toDate);

    List<LogSearchResultDAO> searchPage(
            Long igID,
            String userName,
            String service,
            String activity,
            Date fromDate,
            Date toDate,
            int startRecord,
            int pageSize);

    List<LogSearchResultDAO> getHistory(Long itemID, String uuid);

    long countHistory(Long itemID, String uuid);

    List<LogSearchResultDAO> getHistory(Long itemID, String uuid, long startRecord, long pageSize);

    void deleteInterestgroupLog(long igID);

    Date getLastLoginDateOfUser(String username);

    List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour();

    List<ActivityCountDAO> getListOfActivityCountForInterestGroup(Long igDbNode);

    Date getLastAccessLogOnInterestGroup(long igID);

    Date getLastUpdateLogOnInterestGroup(long igID);

    List<UserActionLogDAO> getRecentUserDownloads(String userId, int i);

    List<UserActionLogDAO> getRecentUserUploads(String userId, int i);

    List<Long> getUserDashboardActivityIds();

    List<UserActionLogDAO> getUserDashboardActivities(UserNewsFeedRequest request);

    List<LogRestDAO> getRowsToProcess();

    long getActivityID(long templateID);

    void updateRest(long id, long logId);
}
