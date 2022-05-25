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
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Slobodan Filipovic
 *         <p>
 *         Migration 3.1 -> 3.4.6 - 02/12/2011
 */
public class IBatisLogDaoServiceImpl implements LogDaoService {

    private static final String NULL_STRING = "null";
    private SqlSessionTemplate sqlSessionTemplate = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.cec.digit.circabc.repo.log.ibatis.LogDaoService#insertPost(eu.cec.digit.
     * circabc.service.log. LogRecord)
     */
    public Long log(LogRecordDAO logRecord) {
        Long id = (long) sqlSessionTemplate.insert("CircabcLog.insert_log_record", logRecord);
        return (id != null ? logRecord.getId() : -1);
    }

    public Integer getActivityID(String service, String activity) {
        LogActivityDAO logActivityDAO = new LogActivityDAO(service, activity);
        Integer id = (Integer) sqlSessionTemplate.selectOne("CircabcLog.select_activity_id", logActivityDAO);
        return (id != null ? id : -1);
    }

    public Integer insertActivity(String service, String activity) {
        LogActivityDAO logActivityDAO = new LogActivityDAO(service, activity);
        sqlSessionTemplate.insert("CircabcLog.insert_activity", logActivityDAO);
        return logActivityDAO.getId();
    }

    public Long logRest(LogRestDAO logRestDAO) {
        Long id = new Long(sqlSessionTemplate.insert("CircabcLog.insert_log_rest", logRestDAO));
        return (id != null ? id : -1);
    }

    @SuppressWarnings("unchecked")
    public List<String> getVisitedRestLogs(Integer id, String username) {
        return (List<String>) sqlSessionTemplate.selectList("CircabcLog.select_rest_log_by_id_user",
                new VisitedLogRestParametersDAO(username, id));
    }

    public Integer getTemplateID(String method, String template) {
        LogTemplateDAO logTemplateDAO = new LogTemplateDAO(method, template);
        Integer id = (Integer) sqlSessionTemplate.selectOne("CircabcLog.select_template_id", logTemplateDAO);
        return (id != null ? id : -1);
    }

    public Integer insertTemplate(String method, String template) {
        LogTemplateDAO logTemplateDAO = new LogTemplateDAO(method, template);
        sqlSessionTemplate.insert("CircabcLog.insert_template", logTemplateDAO);
        return logTemplateDAO.getId();
    }

    @SuppressWarnings("unchecked")
    public List<LogActivityDAO> selectLogActivities() {
        return (List<LogActivityDAO>) sqlSessionTemplate.selectList("CircabcLog.select_log_activity");
    }

    @Override
    public List<LogActivityDAO> selectLogActivitiesById(Long id) {
        Map params = new HashMap<>(1);
        params.put("id", id);
        return (List<LogActivityDAO>) sqlSessionTemplate.selectList("CircabcLog.select_log_activity_by_id", params);
    }

    @SuppressWarnings("unchecked")
    public List<LogSearchResultDAO> search(Long igID, String userName, String serviceDescription,
            String activityDescription, Date fromDate, Date toDate) {

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

        return (List<LogSearchResultDAO>) sqlSessionTemplate.selectList("CircabcLog.select_log_records", params);
    }

    @SuppressWarnings("unchecked")
    public List<LogSearchResultDAO> searchPage(Long igID, String userName, String serviceDescription,
            String activityDescription, Date fromDate, Date toDate, int startRecord, int pageSize) {
        LogSearchLimitParameterDAO params = new LogSearchLimitParameterDAO();
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
        params.setStartRecord(startRecord);
        params.setPageSize(pageSize);

        return (List<LogSearchResultDAO>) sqlSessionTemplate.selectList("CircabcLog.select_log_records_page", params);
    }

    public Integer searchCount(Long igID, String userName, String serviceDescription, String activityDescription,
            Date fromDate, Date toDate) {
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

        return (Integer) sqlSessionTemplate.selectOne("CircabcLog.select_log_records_count", params);
    }

    @SuppressWarnings("unchecked")
    public List<LogSearchResultDAO> getHistory(Long itemID, String uuid) {

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("documentId", itemID);
        params.put("uuid", uuid);
        return (List<LogSearchResultDAO>) sqlSessionTemplate.selectList("CircabcLog.select_item_history", params);
    }

    public long countHistory(Long itemID, String uuid) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("documentId", itemID);
        params.put("uuid", uuid);
        return (long) sqlSessionTemplate.selectOne("CircabcLog.count_item_history", params);
    }

    @SuppressWarnings("unchecked")
    public List<LogSearchResultDAO> getHistory(Long itemID, String uuid, long startRecord, long pageSize) {

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("documentId", itemID);
        params.put("uuid", uuid);
        params.put("startRecord", startRecord);
        params.put("pageSize", pageSize);

        return (List<LogSearchResultDAO>) sqlSessionTemplate.selectList("CircabcLog.select_item_history_pagination",
                params);
    }

    public void logBatch(List<LogRecordDAO> logRecords) {
        for (LogRecordDAO logRecordDAO : logRecords) {
            sqlSessionTemplate.insert("CircabcLog.insert_log_record", logRecordDAO);
        }
    }

    public void deleteInterestgroupLog(long igID) {

        sqlSessionTemplate.delete("CircabcLog.delete_log_by_ig", igID);
    }

    public Date getLastLoginDateOfUser(String username) {

        return (Date) sqlSessionTemplate.selectOne("CircabcLog.select_last_login_date_of_user", username);
    }

    @SuppressWarnings("unchecked")
    public List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour() {
        return (List<LogCountResultDAO>) sqlSessionTemplate
                .selectList("CircabcLog.select_count_actions_per_hour_yesterday");
    }

    @SuppressWarnings("unchecked")
    public List<ActivityCountDAO> getListOfActivityCountForInterestGroup(Long igDbNode) {

        return (List<ActivityCountDAO>) sqlSessionTemplate.selectList("CircabcLog.select_activity_of_interest_group",
                igDbNode);
    }

    /**
     * @param sqlSessionTemplate the sqlSessionTemplate to set
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public Date getLastAccessLogOnInterestGroup(long igID) {
        return (Date) sqlSessionTemplate.selectOne("CircabcLog.select_last_log_for_ig", igID);
    }

    @Override
    public Date getLastUpdateLogOnInterestGroup(long igID) {
        return (Date) sqlSessionTemplate.selectOne("CircabcLog.select_last_update_log_for_ig", igID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserActionLogDAO> getRecentUserDownloads(String userId, int i) {

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("nbResults", i);

        return (List<UserActionLogDAO>) sqlSessionTemplate.selectList("CircabcLog.select_download_logs_for_user",
                params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserActionLogDAO> getRecentUserUploads(String userId, int i) {

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("nbResults", i);

        return (List<UserActionLogDAO>) sqlSessionTemplate.selectList("CircabcLog.select_upload_logs_for_user", params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getUserDashboardActivityIds() {

        return (List<Long>) sqlSessionTemplate.selectList("CircabcLog.select_activity_id_for_news_feed");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserActionLogDAO> getUserDashboardActivities(UserNewsFeedRequest request) {
        return (List<UserActionLogDAO>) sqlSessionTemplate.selectList("CircabcLog.select_group_news_feed_uploads",
                request);
    }

    @Override
    public List<LogRestDAO> getRowsToProcess() {
        return (List<LogRestDAO>) sqlSessionTemplate.selectList("CircabcLog.select_rest_log");
    }

    @Override
    public long getActivityID(long templateID) {
        Long id = (Long) sqlSessionTemplate.selectOne("CircabcLog.select_activity_id_by_template_id", templateID);
        return (id != null ? id : -1);
    }

    @Override
    public void updateRest(long id, long logId) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("id", id);
        params.put("logId", logId);
        sqlSessionTemplate.update("CircabcLog.update_cbc_log_id", params);
    }

}
