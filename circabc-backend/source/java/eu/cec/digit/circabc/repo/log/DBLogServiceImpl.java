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
package eu.cec.digit.circabc.repo.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.log.ibatis.LogDaoService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogRestRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.log.LogTransformService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;

/**
 * @author Slobodan Filipovic
 */
public class DBLogServiceImpl implements LogService {

    private static final String ALPHABETIC_SPACE_REGEX = "[a-z A-Z]*";
    private static final String LIBRARY = "Library";
    private static final String INFORMATION = "Information";
    private static final String NEWSGROUP = "Newsgroup";
    private static final String UPLOAD_FILE = "Upload file: ";
    private static final String UPLOAD_CONTENT = "Upload document";

    private static final Log logger = LogFactory.getLog(DBLogServiceImpl.class);

    private static final String ERROR_SEARCHING_LOG = "Error searching log :";
    private static final String ERROR_GETTING_HISTORY_FOR_ID = "Error getting history for id: ";
    private LogDaoService logDaoService;
    private ManagementService managementService;
    private NodeService nodeService;
    private AuthenticationService authenticationService;
    private LogTransformService logTransformService;

    public void setLogTransformService(LogTransformService logTransformService) {
        this.logTransformService = logTransformService;
    }

    public void log(LogRecord logRecord) {
        if ((logRecord.getActivity() == null) || (logRecord.getService() == null)) {
            return;
        }

        try {
            LogRecordDAO dbLogRecord = new LogRecordDAO();

            int activityID = logDaoService.getActivityID(logRecord.getService(), logRecord.getActivity());
            if (activityID == -1) {
                activityID = logDaoService.insertActivity(logRecord.getService(), logRecord.getActivity());
            }
            dbLogRecord.setActivityID(activityID);

            if (logRecord.getDate() != null) {
                dbLogRecord.setDate(logRecord.getDate());
            } else {
                dbLogRecord.setDate(new Date());
            }

            Long igDBID = logRecord.getIgID();
            if (igDBID != null) {
                dbLogRecord.setIgID(igDBID);
            }

            Long documentDBID = logRecord.getDocumentID();
            if (documentDBID != null) {
                dbLogRecord.setDocumentID(documentDBID);
            }

            dbLogRecord.setInfo(logRecord.getInfo());
            dbLogRecord.setPath(logRecord.getPath());
            dbLogRecord.setUser(logRecord.getUser());
            dbLogRecord.setIgName(logRecord.getIgName());

            if (logRecord.isOK()) {
                dbLogRecord.setIsOK(1);
            } else {
                dbLogRecord.setIsOK(0);
            }
            logDaoService.log(dbLogRecord);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error logging activity :", e);
            }
        }
    }

    @Override
    public void logRest(LogRestRecord logRestRecord) {
        if ((logRestRecord.getMethod() == null) || (logRestRecord.getTemplate() == null)) {
            return;
        }

        try {
            LogRestDAO dbLogRecord = new LogRestDAO();

            int templateID = logDaoService.getTemplateID(logRestRecord.getMethod(), logRestRecord.getTemplate());
            if (templateID == -1) {
                templateID = logDaoService.insertTemplate(logRestRecord.getMethod(), logRestRecord.getTemplate());
            }
            dbLogRecord.setTemplateID(templateID);

            if (logRestRecord.getDate() != null) {
                dbLogRecord.setLogDate(logRestRecord.getDate());
            } else {
                dbLogRecord.setLogDate(new Date());
            }

            if (logRestRecord.getInfo() != null && logRestRecord.getInfo().length() >= 4000) {
                logRestRecord.setInfo("{ \"error\": \"value too big to be recorded\"}");
            }

            dbLogRecord.setInfo(logRestRecord.getInfo());
            dbLogRecord.setPathOneName(logRestRecord.getPathOneName());
            dbLogRecord.setPathOneValue(logRestRecord.getPathOneValue());
            dbLogRecord.setPathTwoName(logRestRecord.getPathTwoName());
            dbLogRecord.setPathTwoValue(logRestRecord.getPathTwoValue());
            dbLogRecord.setPathThreeName(logRestRecord.getPathThreeName());
            dbLogRecord.setPathThreeValue(logRestRecord.getPathThreeValue());
            dbLogRecord.setUrl(logRestRecord.getUrl());
            dbLogRecord.setUserName(logRestRecord.getUser());
            dbLogRecord.setStatusCode(logRestRecord.getStatusCode());

            dbLogRecord.setNodeID(logRestRecord.getNodeID());
            dbLogRecord.setNodeParent(logRestRecord.getNodeParent());
            dbLogRecord.setNodePath(logRestRecord.getNodePath());

            logDaoService.logRest(dbLogRecord);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error logging rest activity :", e);
            }
        }
    }

    /**
     * @return the logDaoService
     */
    public LogDaoService getLogDaoService() {
        return logDaoService;
    }

    /**
     * @param logDaoService the logDaoService to set
     */
    public void setLogDaoService(LogDaoService logDaoService) {
        this.logDaoService = logDaoService;
    }

    public List<LogSearchResultDAO> search(long igID, String user, String service, String method, Date fromDate,
            Date toDate) {
        List<LogSearchResultDAO> logSearchResult = null;
        try {
            logSearchResult = logDaoService.search(igID, user, service, method, fromDate, toDate);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_SEARCHING_LOG, e);
            }
        }
        return logSearchResult;
    }

    public List<LogActivityDAO> getActivities() {
        List<LogActivityDAO> logActivities = null;
        try {
            logActivities = logDaoService.selectLogActivities();

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getting activities :", e);
            }
        }
        return logActivities;
    }

    @Override
    public List<LogActivityDAO> getActivitiesById(long igID) {
        List<LogActivityDAO> logActivities = null;
        try {
            logActivities = logDaoService.selectLogActivitiesById(igID);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getting activities :", e);
            }
        }
        return logActivities;
    }

    public int searchCount(long igID, String user, String service, String activity, Date fromDate, Date toDate) {
        Integer result = 0;
        try {
            result = logDaoService.searchCount(igID, user, service, activity, fromDate, toDate);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_SEARCHING_LOG, e);
            }
        }
        return result;
    }

    public List<LogSearchResultDAO> searchPage(long igID, String user, String service, String activity, Date fromDate,
            Date toDate, int startRecord, int pageSize) {
        List<LogSearchResultDAO> logSearchResult = new ArrayList<>();
        try {
            logSearchResult = logDaoService.searchPage(igID, user, service, activity, fromDate, toDate, startRecord,
                    pageSize);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_SEARCHING_LOG, e);
            }
        }
        return logSearchResult;
    }

    public List<LogSearchResultDAO> getHistory(long itemID, String uuid) {
        List<LogSearchResultDAO> logSearchResult = new ArrayList<>();
        try {
            logSearchResult = logDaoService.getHistory(itemID, uuid);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GETTING_HISTORY_FOR_ID + itemID, e);
            }
        }
        return logSearchResult;
    }

    public long countHistory(long itemID, String uuid) {
        long count = 0;
        try {
            count = logDaoService.countHistory(itemID, uuid);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GETTING_HISTORY_FOR_ID + itemID, e);
            }
        }
        return count;
    }

    public List<LogSearchResultDAO> getHistory(long itemID, String uuid, long startRecord, long pageSize) {
        List<LogSearchResultDAO> logSearchResult = new ArrayList<>();
        try {
            logSearchResult = logDaoService.getHistory(itemID, uuid, startRecord, pageSize);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GETTING_HISTORY_FOR_ID + itemID, e);
            }
        }
        return logSearchResult;
    }

    public void logBatch(List<LogRecord> logRecords) {
        List<LogRecordDAO> dbLogRecords = new ArrayList<>(logRecords.size());

        for (LogRecord logRecord : logRecords) {

            final String activity = logRecord.getActivity();
            final String service = logRecord.getService();
            if ((activity == null) || (service == null)) {
                if (logger.isErrorEnabled()) {
                    logger.error("Invalid activity " + activity + " or service " + service);
                }
                continue;
            }

            try {

                LogRecordDAO dbLogRecord = new LogRecordDAO();

                int activityID = logDaoService.getActivityID(service, activity);
                if (activityID == -1) {
                    // check if service and activity are alphabetic string with spaces
                    if (service.matches(ALPHABETIC_SPACE_REGEX) && activity.matches(ALPHABETIC_SPACE_REGEX)) {
                        activityID = logDaoService.insertActivity(service, activity);
                    } else {
                        if (logger.isErrorEnabled()) {
                            logger.error("Service : " + service + " and activity : " + activity + " do not exists");
                            logger.error("Log record : " + logRecord.toString());
                        }
                        continue;
                    }
                }
                dbLogRecord.setActivityID(activityID);

                if (logRecord.getDate() != null) {
                    dbLogRecord.setDate(logRecord.getDate());
                } else {
                    dbLogRecord.setDate(new Date());
                }

                Long igDBID = logRecord.getIgID();
                if (igDBID != null) {
                    dbLogRecord.setIgID(igDBID);
                }

                Long documentDBID = logRecord.getDocumentID();
                if (documentDBID != null) {
                    dbLogRecord.setDocumentID(documentDBID);
                }

                dbLogRecord.setInfo(logRecord.getInfo());
                dbLogRecord.setPath(logRecord.getPath());
                dbLogRecord.setUser(logRecord.getUser());
                dbLogRecord.setIgName(logRecord.getIgName());

                if (logRecord.isOK()) {
                    dbLogRecord.setIsOK(1);
                } else {
                    dbLogRecord.setIsOK(0);
                }
                dbLogRecords.add(dbLogRecord);

            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error preparing log batch :", e);
                }
            }
        }

        try {
            logDaoService.logBatch(dbLogRecords);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error performing log batch insert ", e);
            }
        }
    }

    public void deleteInterestgroupLog(long igID) {
        try {
            logDaoService.deleteInterestgroupLog(igID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error performing deletion of interest group :" + String.valueOf(igID), e);
            }
        }
    }

    public Date getLastLoginDateOfUser(String username) {
        try {
            return logDaoService.getLastLoginDateOfUser(username);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during getting last login date of" + username, e);
            }

            return null;
        }
    }

    public List<LogCountResultDAO> getNumberOfActionsYesterdayPerHour() {

        try {
            return logDaoService.getNumberOfActionsYesterdayPerHour();

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during getting number of actions in log service", e);
            }

            return Collections.emptyList();
        }
    }

    public List<ActivityCountDAO> getListOfActivityCountForInterestGroup(Long igDbNode) {

        try {

            return logDaoService.getListOfActivityCountForInterestGroup(igDbNode);

        } catch (Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during getting number of activity in log service for group id:" + igDbNode, e);
            }

            return Collections.emptyList();
        }
    }

    @Override
    public Date getLastAccessOnInterestGroup(long igID) {
        try {

            return logDaoService.getLastAccessLogOnInterestGroup(igID);

        } catch (Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during getting the last log for group id:" + igID, e);
            }

            return null;
        }
    }

    @Override
    public Date getLastUpdateOnInterestGroup(long igID) {
        try {

            return logDaoService.getLastUpdateLogOnInterestGroup(igID);

        } catch (Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during getting the last update log for group id:" + igID, e);
            }

            return null;
        }
    }

    @Override
    public List<UserActionLogDAO> getRecentUserDownloads(String userId, int i) {
        try {

            return logDaoService.getRecentUserDownloads(userId, i);

        } catch (Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during getting the last downloads log for user id:" + userId, e);
            }

            return null;
        }
    }

    @Override
    public List<UserActionLogDAO> getRecentUserUploads(String userId, int i) {
        try {

            return logDaoService.getRecentUserUploads(userId, i);

        } catch (Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during getting the last uploads log for user id:" + userId, e);
            }

            return null;
        }
    }

    /**
     * @return
     * @see eu.cec.digit.circabc.service.log.LogService#getVisitedIGRestLogs(String)
     */
    @Override
    public List<String> getVisitedIGRestLogs(String username) {
        int id = logDaoService.getTemplateID("GET", "/circabc/groups/{id}");
        if (id == -1) {
            return new ArrayList<>();
        }
        return logDaoService.getVisitedRestLogs(id, username);
    }

    @Override
    public void processRestLog() {
        List<LogRestDAO> rows = logDaoService.getRowsToProcess();
        for (LogRestDAO row : rows) {
            process(row);
        }
    }

    private void process(LogRestDAO logRestDAO) {

        try {

            LogRecordDAO dbLogRecord = logTransformService.transform(logRestDAO);

            Long logId = logDaoService.log(dbLogRecord);
            if (logId > 0) {
                logDaoService.updateRest(logRestDAO.getId(), logId);
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when processing row " + logRestDAO.toString(), e);
            }
            // mark row as processed with error
            logDaoService.updateRest(logRestDAO.getId(), -1L);
        }
    }

    @Override
    public List<Long> getUserDashboardActivityIds() {
        return logDaoService.getUserDashboardActivityIds();
    }

    @Override
    public List<UserActionLogDAO> getUserDashboardActivities(UserNewsFeedRequest request) {
        return logDaoService.getUserDashboardActivities(request);
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public LogRecord prepareLogUploadRequest(NodeRef nodeRef, String originalFileName) {

        final LogRecord logRecord = new LogRecord();

        try {

            final NodeRef currentInterestGroup = managementService.getCurrentInterestGroup(nodeRef);

            if (currentInterestGroup != null) {
                final String service;
                final Set<QName> aspects = nodeService.getAspects(nodeService.getPrimaryParent(nodeRef).getParentRef());
                if (aspects.contains(CircabcModel.ASPECT_LIBRARY)) {
                    service = LIBRARY;
                } else if (aspects.contains(CircabcModel.ASPECT_INFORMATION)) {
                    service = INFORMATION;
                } else if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP)) {
                    // for attachements
                    service = NEWSGROUP;
                } else {
                    service = null;
                }

                if (service != null) {
                    final Long igID = (Long) nodeService.getProperty(currentInterestGroup, ContentModel.PROP_NODE_DBID);
                    final Path nodePath = nodeService.getPath(nodeRef);
                    final String circabcPath = PathUtils.getCircabcPath(nodePath, true);

                    Long documentID = (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                    String user = authenticationService.getCurrentUserName();

                    logRecord.setService(service);
                    logRecord.setActivity(UPLOAD_CONTENT);
                    // add info in case the file has been rename, because
                    // another child is already existing
                    logRecord.setInfo(UPLOAD_FILE + " with original name" + originalFileName);
                    logRecord.setIgID(igID);
                    logRecord.setDocumentID(documentID);

                    if (user != null) {
                        logRecord.setUser(user);
                    } else {
                        logRecord.setUser(CircabcConstant.GUEST_AUTHORITY);
                    }
                    logRecord.setPath(circabcPath);
                }
            }

        } catch (final Exception e) {
            logger.error("Error during logging file download ", e);
        }

        return logRecord;
    }

}
