/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog.audit;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.log.LogActivityDAO;
import eu.cec.digit.circabc.repo.log.LogSearchResultDAO;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExportTypeEnum;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Bean that backs the audit search.
 *
 * @author Yanick Pignot
 */
public class AuditSearchDialog extends BaseWaiDialog {

    /**
     * The name of this dialog
     */
    public static final String DIALOG_NAME = "auditSearchDialogWai";
    private static final long serialVersionUID = -7657967828274330916L;
    private static final Log logger = LogFactory.getLog(AuditSearchDialog.class);
    /**
     * The default number of audit entries to display by screen
     */
    private static final int DEFAULT_PAGINATION_SIZE = 25;
    /**
     * The default sort type (not yet modifiable via web client)
     */

    // the used message keys
    private static final String MESSAGE_ID_TITLE = "audit_search_page_title";
    private static final String MESSAGE_ID_DESCRIPTION_IG = "audit_search_page_description_for_ig";
    private static final String MESSAGE_ID_DESCRIPTION_CIRCABC = "audit_search_page_description_for_circabc";
    private static final String MESSAGE_ID_SEARCH = "search";
    private static final String MESSAGE_ID_NEW_SEARCH = "new_search";
    private static final String MESSAGE_ID_CLOSE = "close";
    private static final String MESSAGE_ID_CATEGORIES = "categories_plurial";
    private static final String MESSAGE_ID_IGROOTS = "ig_plurial";
    private static final String MESSAGE_ID_SPACES = "library_panel_container_label";
    private static final String MESSAGE_ID_ERROR_FOUND_SOURCES = "error_db_populate_lists";
    private static final String MESSAGE_ID_ERROR_DATE_MANDATORY = "audit_search_dates_mandatory";
    private static final String MESSAGE_ID_WARN_DATES_INVERSED = "audit_search_dates_limit_interval_inversed";
    private static final String MESSAGE_ID_WARN_DATE_LIMIT_EXCEEDED = "audit_search_dates_limit_exceeded_circabc";
    private static final String MESSAGE_ID_WARN_TO_DATES_IN_FUTUR = "audit_search_dates_to_date_in_futur";
    /**
     * If true, the fields of the search dialog will be erased
     */
    public boolean resetFields = true;
    // the list of available methods for each services (construct if resetFields == true)
    private Map<String, List<String>> logActivities;
    // the bundle to found I18N messages
    private ResourceBundle bundle;
    // the list of db audit information (construct on form submit)
    private transient List<LogSearchResultDAO> auditList;

    // the search criteria fields
    private String paginationAsString;
    private int pagination;
    private String user;
    private String service;
    private String method;
    private Date toDate;
    private Date fromDate;
    private List<String> warnings;
    private SelectItem[] users;

    transient private PersonService personService;

    private ExportTypeEnum exportType = ExportTypeEnum.CSV;

    private int startYear;
    private int yearCount;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        yearCount = Calendar.getInstance().get(Calendar.YEAR) - startYear + 1;

        if (resetFields) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException("The node ID is a mandatory parameter.");
            }

            long todayMillis = System.currentTimeMillis();

            auditList = null;

            bundle = Application.getBundle(FacesContext.getCurrentInstance());

            pagination = DEFAULT_PAGINATION_SIZE;
            paginationAsString = DEFAULT_PAGINATION_SIZE + "";
            user = null;
            service = null;
            // by default, the search is performed from 7 days before
            fromDate = new Date(todayMillis - (7 * 24 * 60 * 60 * 1000));
            toDate = new Date(todayMillis);

            warnings = new ArrayList<>(4);

            initAvailableMethodsForServiceMap();
            initUsers();
        }

        resetFields = true;
    }

    private void initUsers() {
        final NodeRef nodeRef = getActionNode().getNodeRef();
        NodeRef circabcContainer = getManagementService().getCurrentInterestGroup(nodeRef);
        ProfileManagerService circabcProfileManagerService = null;
        if (circabcContainer == null) {
            circabcContainer = getManagementService().getCurrentCategory(nodeRef);
            if (circabcContainer == null) {
                circabcContainer = getManagementService().getCircabcNodeRef();
                circabcProfileManagerService = getProfileManagerServiceFactory()
                        .getCircabcRootProfileManagerService();
            } else {
                circabcProfileManagerService = getProfileManagerServiceFactory()
                        .getCategoryProfileManagerService();
            }
        } else {
            circabcProfileManagerService = getProfileManagerServiceFactory()
                    .getIGRootProfileManagerService();
        }

        final Set<String> invitedUsers = circabcProfileManagerService.getInvitedUsers(circabcContainer);
        final ArrayList<SortableSelectItem> userList = new ArrayList<>(invitedUsers.size());
        for (String user : invitedUsers) {
            final String fullName = getUserService().getUserFullName(user);
            userList.add(new SortableSelectItem(user, fullName, fullName));
        }
        users = new SelectItem[invitedUsers.size()];
        userList.toArray(users);
        Arrays.sort(users);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        warnings = new ArrayList<>(4);

        checkDates();

        Long igID = getContainerID();
        // get the audit list
        auditList = getLogService().search(igID, user, service, method, fromDate, toDate);

        resetFields = false;

        // stay in the same page
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                CircabcNavigationHandler.OUTCOME_SEPARATOR +
                CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    }

    private Long getContainerID() {
        NodeRef spaceToInspect = getActionNode().getNodeRef();

        NodeRef circabcContainer = getManagementService().getCurrentInterestGroup(spaceToInspect);
        if (circabcContainer == null) {
            circabcContainer = getManagementService().getCurrentCategory(spaceToInspect);
            if (circabcContainer == null) {
                circabcContainer = getManagementService().getCircabcNodeRef();
            }
        }
        Long igID = (Long) getNodeService().getProperty(circabcContainer, ContentModel.PROP_NODE_DBID);
        return igID;
    }

    protected void checkDates() throws IllegalArgumentException {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(bundle.getString(MESSAGE_ID_ERROR_DATE_MANDATORY));
        }

        Date toDay = new Date();

        // Set the from date being at the first minute of the day
        Calendar cal = new GregorianCalendar();
        cal.setTime(fromDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        fromDate = cal.getTime();

        // Set the from date to the first hour of the day
        cal = new GregorianCalendar();
        cal.setTime(toDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        toDate = cal.getTime();

        // Set the today date to the last hour of the day
        cal = new GregorianCalendar();
        cal.setTime(toDay);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        toDay = cal.getTime();

        if (toDate.after(toDay)) {
            toDate = toDay;
            warnings.add(bundle.getString(MESSAGE_ID_WARN_TO_DATES_IN_FUTUR));
        }
        if (fromDate.after(toDate)) {
            warnings.add(bundle.getString(MESSAGE_ID_WARN_DATES_INVERSED));
        }

        if (getActionNode().hasAspect(CircabcModel.ASPECT_CIRCABC_ROOT)) {
            GregorianCalendar calForTest = new GregorianCalendar();

            if (toDay.before(toDate)) {
                calForTest.setTime(toDay);
            } else {
                calForTest.setTime(toDate);
            }

            // remove one mount to the fromDAte
            calForTest.add(Calendar.MONTH, -1);

            if (calForTest.getTime().after(fromDate)) {
                fromDate = calForTest.getTime();
                warnings.add(bundle.getString(MESSAGE_ID_WARN_DATE_LIMIT_EXCEEDED));
            }
        }
    }

    /**
     * Change listener for the method select box
     */
    public void changeServiceSelection(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();

        this.service = newValue;

        // if the new value is set as null,
        // the selected method ca be the same
        if (newValue != null) {
            this.method = null;
        }

    }

    /**
     * Change listener for the user select box
     */
    public void changeUserSelection(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();

        this.user = newValue;

        // if the new value is set as null,
        // the selected method ca be the same
        if (newValue != null) {
            this.user = null;
        }

    }

    public int getOccurence() {
        return (auditList == null) ? -1 : auditList.size();
    }

    public List<String> getWarningMessages() {
        return warnings;
    }

    public boolean isWarningFounds() {
        return !(warnings == null || warnings.size() == 0);
    }

    /**
     * Update page size based on user selection
     */
    public void updateAuditPageSize(ActionEvent event) {
        try {
            int size = Integer.parseInt(this.paginationAsString);
            if (size >= 0) {
                this.pagination = size;
            } else {
                // reset to known value if this occurs
                this.paginationAsString = Integer.toString(this.pagination);
            }
        } catch (NumberFormatException err) {
            // reset to known value if this occurs
            this.paginationAsString = Integer.toString(this.pagination);
        }
    }

    public void export() {

        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        String extension =
                exportType.equals(ExportTypeEnum.Excel) ? "xls" : exportType.toString().toLowerCase();

        if (getAuditListSize() < 0) {
            warnings = new ArrayList<>(4);
            checkDates();
            Long igID = getContainerID();
            auditList = getLogService().search(igID, user, service, method, fromDate, toDate);
        }

        ServletOutputStream outStream = null;
        try {
            outStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment;filename=circabc_log." + extension);
            switch (exportType) {
                case CSV:
                    response.setContentType("text/csv;charset=UTF-8");
                    writeCSV(getAuditList(), outStream);
                    break;
                case XML:
                    response.setContentType("text/xml;charset=UTF-8");
                    writeXML(getAuditList(), outStream);
                    break;
                case Excel:
                    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                    writeXLS(getAuditList(), outStream);
                    break;
            }

            context.responseComplete();

        } catch (Exception ex) {
            logger.error("Error exporting file of type " + exportType.toString(), ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

    }

    private void writeXML(List<LogSearchResultDAO> list,
                          ServletOutputStream outStream) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = null;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("logs");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

        for (LogSearchResultDAO item : list) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("log");
            xtw.writeAttribute("date", simpleDateFormat.format(item.getLogDate()));
            xtw.writeAttribute("username", item.getUserName());
            xtw.writeAttribute("service", item.getServiceDescription());
            xtw.writeAttribute("activity", item.getActivityDescription());
            xtw.writeAttribute("status", item.getStatus());
            xtw.writeAttribute("path", WebClientHelper.emptyStringIfNull(item.getPath()));
            xtw.writeAttribute("information", WebClientHelper.emptyStringIfNull(item.getInfo()));
            xtw.writeEndElement();
        }
        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();

    }

    private void writeCSV(List<LogSearchResultDAO> list,
                          ServletOutputStream outStream) throws IOException {

        OutputStreamWriter outStreamWriter = null;
        try {
            outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("Date");
            outStreamWriter.write(',');
            outStreamWriter.write("User Name");
            outStreamWriter.write(',');
            outStreamWriter.write("Service");
            outStreamWriter.write(',');
            outStreamWriter.write("Activity");
            outStreamWriter.write(',');
            outStreamWriter.write("Status");
            outStreamWriter.write(',');
            outStreamWriter.write("Path");
            outStreamWriter.write(',');
            outStreamWriter.write("Information");
            outStreamWriter.write('\n');

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

            for (LogSearchResultDAO item : list) {
                outStreamWriter.write(simpleDateFormat.format(item.getLogDate()));
                outStreamWriter.write(',');
                outStreamWriter.write(item.getUserName());
                outStreamWriter.write(',');
                outStreamWriter.write(item.getServiceDescription());
                outStreamWriter.write(',');
                outStreamWriter.write(item.getActivityDescription());
                outStreamWriter.write(',');
                outStreamWriter.write(item.getStatus());
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper.emptyStringIfNull(item.getPath()));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper.emptyStringIfNull(item.getInfo()));
                outStreamWriter.write('\n');
            }
        } finally {
            if (outStreamWriter != null) {
                try {
                    outStreamWriter.flush();
                } finally {
                    outStreamWriter.close();
                }
            }
        }


    }

    private void writeXLS(List<LogSearchResultDAO> logSearchResults, ServletOutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Logs");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Date");
        titleRow.createCell(1).setCellValue("User Name");
        titleRow.createCell(2).setCellValue("Service");
        titleRow.createCell(3).setCellValue("Activity");
        titleRow.createCell(4).setCellValue("Status");
        titleRow.createCell(5).setCellValue("Path");
        titleRow.createCell(6).setCellValue("Information");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");

        int idx = 1;

        for (LogSearchResultDAO logSearchResult : logSearchResults) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(simpleDateFormat.format(logSearchResult.getLogDate()));
            row.createCell(1).setCellValue(logSearchResult.getUserName());
            row.createCell(2).setCellValue(logSearchResult.getServiceDescription());
            row.createCell(3).setCellValue(logSearchResult.getActivityDescription());
            row.createCell(4).setCellValue(logSearchResult.getStatus());
            row.createCell(5).setCellValue(WebClientHelper.emptyStringIfNull(logSearchResult.getPath()));
            row.createCell(6).setCellValue(WebClientHelper.emptyStringIfNull(logSearchResult.getInfo()));
            idx++;
        }

        workbook.write(outStream);
    }

    /**
     * Util method to fill the map of methods by service according which services and methods already
     * audited in the database.
     */
    protected void initAvailableMethodsForServiceMap() {
        List<LogActivityDAO> activities = null;

        try {
            // get the list of availabe audit source (service - methods)
            activities = getLogService().getActivities();
        } catch (Exception e) {
            Utils.addErrorMessage(translate(MESSAGE_ID_ERROR_FOUND_SOURCES + e));
        }

        // construct a new hash map with a service name as key and the list of corresponding methods name
        // as a List of string
        logActivities = new HashMap<>();
        for (LogActivityDAO activity : activities) {
            String service = activity.getServiceDescription();
            String method = activity.getActivityDescription();

            // get the methods in the map for this service
            List<String> methodList = logActivities.get(service);

            if (methodList == null) {
                // The service doesn't exist yet, and the method too
                methodList = new ArrayList<>();
                methodList.add(method);

                logActivities.put(service, methodList);
            } else if (!methodList.contains(method)) {
                // The service exists but not the method
                methodList.add(method);
                logActivities.put(service, methodList);
            } else {
                // the service and the method are already set. The logic should not allow to pass here.
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerDescription()
     */
    @Override
    public String getContainerDescription() {
        // get the I18N description of the dialog in the extension/webclient.properties
        if (getActionNode().hasAspect(CircabcModel.ASPECT_CIRCABC_ROOT)) {
            return bundle.getString(MESSAGE_ID_DESCRIPTION_CIRCABC);
        } else {
            return bundle.getString(MESSAGE_ID_DESCRIPTION_IG);
        }
    }

    public String getSubspacesType() {
        if (getActionNode().hasAspect(CircabcModel.ASPECT_CIRCABC_ROOT)) {
            return bundle.getString(MESSAGE_ID_CATEGORIES);
        } else if (getActionNode().hasAspect(CircabcModel.ASPECT_CATEGORY)) {
            return bundle.getString(MESSAGE_ID_IGROOTS);
        } else {
            return bundle.getString(MESSAGE_ID_SPACES);
        }
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerTitle()
     */
    @Override
    public String getContainerTitle() {
        // get the I18N title of the dialog in the extension/webclient.properties
        return bundle.getString(MESSAGE_ID_TITLE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getCancelButtonLabel()
     */
    @Override
    public String getCancelButtonLabel() {
        // The cancel button must be renamed as 'Close'. No sens to keep 'cancel'
        return bundle.getString(MESSAGE_ID_CLOSE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonLabel()
     */
    @Override
    public String getFinishButtonLabel() {
        // if the audit list is initialized, a previous search has been launched et the label will become 'New Search'.
        // 'Search' if not.
        return (auditList == null) ? bundle.getString(MESSAGE_ID_SEARCH)
                : bundle.getString(MESSAGE_ID_NEW_SEARCH);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
     */
    @Override
    public boolean getFinishButtonDisabled() {
        // always available
        return false;
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getDefaultCancelOutcome()
     */
    @Override
    public String getDefaultCancelOutcome() {
        isFinished = true;

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    // ***	 the audit infos getters ***

    /**
     * @return the auditList
     */
    public List<LogSearchResultDAO> getAuditList() {
        return auditList;
    }


    /**
     * @return the auditList size
     */
    public int getAuditListSize() {
        return (auditList == null) ? -1 : auditList.size();
    }

    // ***	search criteria getter and setter	***

    /**
     * @return the list of available services to populate the JSF drop down
     */
    public SelectItem[] getServices() {
        // get the servies
        Set<String> servicesAsList = logActivities.keySet();

        // construct an JSF SelectItem list to populate the method drop down
        List<SelectItem> servicesAsItemList = new ArrayList<>(servicesAsList.size());
        for (String serviceName : servicesAsList) {
            servicesAsItemList.add(new SelectItem(serviceName, serviceName));
        }

        return servicesAsItemList.toArray(new SelectItem[servicesAsItemList.size()]);
    }

    /**
     * @return the list of available methods to populate the JSF drop down
     */
    public SelectItem[] getMethods() {
        List<String> methodsAsList = null;

        if (service != null && service.length() > 0) {
            // get the methods for the given service
            methodsAsList = logActivities.get(service);
        } else {
            methodsAsList = new ArrayList<>();
            // get all methods in all services
            for (Map.Entry<String, List<String>> entry : logActivities.entrySet()) {
                for (String method : entry.getValue()) {
                    // don't duplicate the name of methods
                    if (!methodsAsList.contains(method)) {
                        methodsAsList.add(method);
                    }
                }
            }
        }

        // construct an JSF SelectItem list to populate the method drop down
        List<SelectItem> methodsAsItemList = new ArrayList<>(methodsAsList.size());
        for (String method : methodsAsList) {
            methodsAsItemList.add(new SelectItem(method, method));
        }

        return methodsAsItemList.toArray(new SelectItem[methodsAsItemList.size()]);
    }

    /**
     * @return the pagination
     */
    public int getPagination() {
        return pagination;
    }

    /**
     * @param pagination the pagination to set
     */
    public void setPagination(int pagination) {
        if (pagination > 0) {
            this.pagination = pagination;
        }
    }

    /**
     * @return the paginationAsString
     */
    public String getPaginationAsString() {
        return paginationAsString;
    }

    /**
     * @param paginationAsString the paginationAsString to set
     */
    public void setPaginationAsString(String paginationAsString) {
        this.paginationAsString = paginationAsString;
    }

    /**
     * @return the fromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        // sometimes JSF returns the string with 'null'as content and not the null value.
        // Ensure that the logic tkae that in account:
        if (method == null || method.equalsIgnoreCase("null")) {
            this.method = null;
        } else {
            this.method = method;
        }
    }

    public List<SelectItem> getExportTypes() {
        return WebClientHelper.getExportedTypes();
    }

    public String getExportType() {
        return exportType.toString();
    }

    public void setExportType(String value) {
        if (value != null) {
            exportType = ExportTypeEnum.valueOf(value);
        }
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        // sometimes JSF returns the string with 'null'as content and not the null value.
        // Ensure that the logic tkae that in account:
        if (service == null || service.equalsIgnoreCase("null")) {
            this.service = null;
        } else {
            this.service = service;
        }
    }

    /**
     * @return the toDate
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * @param toDate the toDate to set
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }


    public String getBrowserTitle() {
        // TODO change me
        return getContainerTitle();
    }

    public String getPageIconAltText() {
        // TODO change me
        return getContainerTitle();
    }

    /**
     * @return the users
     */
    public SelectItem[] getUsers() {
        return users;
    }

    /**
     * @return the personService
     */
    public PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getYearCount() {
        return this.yearCount;
    }

    public void setYearCount(int yearCount) {
        this.yearCount = yearCount;
    }

}
