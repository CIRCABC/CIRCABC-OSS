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
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * Bean that backs the audit search.O
 *
 * @author Yanick Pignot
 */
public class AuditSearchPagedDialog extends BaseWaiPageableDialog<LogSearchResultDAO> {

    /**
     * The name of this dialog
     */
    public static final String DIALOG_NAME = "auditSearchPagedDialogWai";
    private static final long serialVersionUID = -7657967828274330916L;
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
    private boolean resetFields = true;
    // the list of available methods for each services (construct if resetFields == true)
    private Map<String, List<String>> logActivities;
    // the bundle to found I18N messages
    private ResourceBundle bundle;


    private String user;
    private String service;
    private String activity;
    private Date toDate;
    private Date fromDate;
    private List<String> warnings;
    private SelectItem[] users;

    transient private PersonService personService;

    private Long igID;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setIgID();

        if (resetFields) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException("The node ID is a mandatory parameter.");
            }

            long todayMillis = System.currentTimeMillis();

            bundle = Application.getBundle(FacesContext.getCurrentInstance());

            user = null;
            service = null;
            // by default, the search is performed from 7 days before
            fromDate = new Date(todayMillis - (7 * 24 * 60 * 60 * 1000));
            toDate = new Date(todayMillis);

            warnings = new ArrayList<>(4);

            initAvailableActivitiesForServiceMap();
            initUsers();
            this.totalRows = getLogService().searchCount(igID, user, service, activity, fromDate, toDate);
            this.dataList = getLogService()
                    .searchPage(igID, user, service, activity, fromDate, toDate, this.firstRow,
                            this.rowsPerPage);
        }

        resetFields = true;
    }

    private void initUsers() {
        final IGRootProfileManagerService igRootProfileManagerService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();
        NodeRef currentInterestGroup = getManagementService()
                .getCurrentInterestGroup(getActionNode().getNodeRef());
        final Set<String> invitedUsers = igRootProfileManagerService
                .getInvitedUsers(currentInterestGroup);
        final ArrayList<SortableSelectItem> userList = new ArrayList<>(invitedUsers.size());
        for (String invitedUser : invitedUsers) {
            final String fullName = getUserService().getUserFullName(invitedUser);
            userList.add(new SortableSelectItem(invitedUser, fullName, fullName));
        }
        users = new SelectItem[invitedUsers.size()];
        userList.toArray(users);
        Arrays.sort(users);

    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {

        warnings = new ArrayList<>(4);

        checkDates();
        this.firstRow = 0;
        this.totalRows = getLogService().searchCount(igID, user, service, activity, fromDate, toDate);
        this.dataList = getLogService()
                .searchPage(igID, user, service, activity, fromDate, toDate, this.firstRow,
                        this.rowsPerPage);
        resetFields = false;

        // stay in the same page
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                CircabcNavigationHandler.OUTCOME_SEPARATOR +
                CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    }

    private void setIgID() {
        NodeRef spaceToInspect = getActionNode().getNodeRef();
        NodeRef currentInterestGroup = getManagementService().getCurrentInterestGroup(spaceToInspect);
        igID = (Long) getNodeService().getProperty(currentInterestGroup, ContentModel.PROP_NODE_DBID);
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
     * Change listener for the activity select box
     */
    public void changeServiceSelection(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();

        this.service = newValue;

        // if the new value is set as null,
        // the selected activity ca be the same
        if (newValue != null) {
            this.activity = null;
        }

    }

    /**
     * Change listener for the user select box
     */
    public void changeUserSelection(ValueChangeEvent event) {
        String newValue = (String) event.getNewValue();

        this.user = newValue;

        // if the new value is set as null,
        // the selected activity ca be the same
        if (newValue != null) {
            this.user = null;
        }

    }

    public int getOccurence() {
        return (dataList == null) ? -1 : totalRows;
    }

    public List<String> getWarningMessages() {
        return warnings;
    }

    public boolean isWarningFounds() {
        return !(warnings == null || warnings.size() == 0);
    }


    /**
     * Util activity to fill the map of methods by service according which services and methods
     * already audited in the database.
     */
    protected void initAvailableActivitiesForServiceMap() {
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
        for (LogActivityDAO logActivity : activities) {
            String currentService = logActivity.getServiceDescription();
            String method = logActivity.getActivityDescription();

            // get the methods in the map for this service
            List<String> activitiesList = logActivities.get(currentService);

            if (activitiesList == null) {
                // The service doesn't exist yet, and the activity too
                activitiesList = new ArrayList<>();
                activitiesList.add(method);

                logActivities.put(currentService, activitiesList);
            } else if (!activitiesList.contains(method)) {
                // The service exists but not the activity
                activitiesList.add(method);
                logActivities.put(currentService, activitiesList);
            } else {
                // the service and the activity are already set. The logic should not allow to pass here.
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
        return (dataList == null) ? bundle.getString(MESSAGE_ID_SEARCH)
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


    /**
     * @return the list of available services to populate the JSF drop down
     */
    public SelectItem[] getServices() {
        // get the servies
        Set<String> servicesAsList = logActivities.keySet();

        List<SelectItem> servicesAsItemList = new ArrayList<>(servicesAsList.size());
        for (String serviceName : servicesAsList) {
            servicesAsItemList.add(new SelectItem(serviceName, serviceName));
        }

        return servicesAsItemList.toArray(new SelectItem[servicesAsItemList.size()]);
    }

    /**
     * @return the list of available methods to populate the JSF drop down
     */
    public SelectItem[] getActivities() {
        List<String> activitiesAsList = null;

        if (service != null && service.length() > 0) {
            // get the methods for the given service
            activitiesAsList = logActivities.get(service);
        } else {
            activitiesAsList = new ArrayList<>();
            // get all methods in all services
            for (Map.Entry<String, List<String>> entry : logActivities.entrySet()) {
                for (String method : entry.getValue()) {
                    // don't duplicate the name of methods
                    if (!activitiesAsList.contains(method)) {
                        activitiesAsList.add(method);
                    }
                }
            }
        }

        // construct an JSF SelectItem list to populate the activity drop down
        List<SelectItem> activitiesAsItemList = new ArrayList<>(activitiesAsList.size());
        for (String method : activitiesAsList) {
            activitiesAsItemList.add(new SelectItem(method, method));
        }

        return activitiesAsItemList.toArray(new SelectItem[activitiesAsItemList.size()]);
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
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(String activity) {
        // sometimes JSF returns the string with 'null'as content and not the null value.
        // Ensure that the logic take that in account:
        if (activity == null || activity.equalsIgnoreCase("null")) {
            this.activity = null;
        } else {
            this.activity = activity;
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

    @Override
    protected int getCount() {
        if (!isRenderingState()) {
            this.totalRows = getLogService().searchCount(igID, user, service, activity, fromDate, toDate);
        }
        return totalRows;
    }

    private boolean isRenderingState() {
        return FacesContext.getCurrentInstance().getRenderResponse();
    }

    @Override
    protected List<LogSearchResultDAO> getList(int firstRow, int rowsPerPage, String sortField,
                                               boolean sortAscending) {
        if (!isRenderingState()) {
            this.dataList = getLogService()
                    .searchPage(igID, user, service, activity, fromDate, toDate, this.firstRow,
                            this.rowsPerPage);
        }
        return dataList;
    }

}
