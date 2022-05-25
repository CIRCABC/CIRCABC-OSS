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
package eu.cec.digit.circabc.service.log;

import eu.cec.digit.circabc.repo.log.*;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Date;
import java.util.List;

// Logger

public interface LogService {

    void log(LogRecord logRecord);

    void logRest(LogRestRecord logRestRecord);

    void logBatch(List<LogRecord> logRecords);

    List<LogActivityDAO> getActivities();

    List<LogActivityDAO> getActivitiesById(long igID);

    List<LogSearchResultDAO> search(
            long igID, String user, String service, String method, Date fromDate, Date toDate);

    int searchCount(
            long igID, String user, String service, String method, Date fromDate, Date toDate);

    List<LogSearchResultDAO> searchPage(
            long igID,
            String user,
            String service,
            String method,
            Date fromDate,
            Date toDate,
            int startRecord,
            int pageSize);

    List<LogSearchResultDAO> getHistory(long itemID, String uuid);

    long countHistory(long itemID , String uuid);

    List<LogSearchResultDAO> getHistory(long itemID, String uuid, long startRecord, long pageSize);

    void deleteInterestgroupLog(long igID);

    Date getLastLoginDateOfUser(String username);

    List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour();

    List<ActivityCountDAO> getListOfActivityCountForInterestGroup(Long igDbNode);

    Date getLastAccessOnInterestGroup(long igID);

    Date getLastUpdateOnInterestGroup(long igID);

    List<UserActionLogDAO> getRecentUserDownloads(String userId, int i);

    List<UserActionLogDAO> getRecentUserUploads(String userId, int i);

    List<Long> getUserDashboardActivityIds();

    List<UserActionLogDAO> getUserDashboardActivities(UserNewsFeedRequest request);

    List<String> getVisitedIGRestLogs(String username);

    void processRestLog();

    LogRecord prepareLogUploadRequest(NodeRef nodeRef, String originalFileName);
}
