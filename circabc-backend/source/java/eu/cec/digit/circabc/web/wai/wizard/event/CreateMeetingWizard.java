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
package eu.cec.digit.circabc.web.wai.wizard.event;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.repo.event.AppointmentImpl;
import eu.cec.digit.circabc.repo.event.MeetingImpl;
import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.ui.repo.converter.EnumConverter;
import eu.cec.digit.circabc.web.validator.EmailValidator;
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.bean.navigation.event.AppointmentWebUtils;
import eu.cec.digit.circabc.web.wai.dialog.notification.NotificationUtils;
import eu.cec.digit.circabc.web.wai.wizard.BaseWaiWizard;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.calendartag.util.CalendarTagUtil;
import org.joda.time.LocalTime;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

/**
 * Bean that backs the Create event wizard
 *
 * @author yanick pignot
 */
public class CreateMeetingWizard extends BaseWaiWizard {

    public static final String MSG_ERROR_TITLE_MISSING = "event_create_meetings_wizard_validation_title_missing";
    /**
     * Index of the USERS search filter index
     */
    protected static final int USERS_IDX = 0;
    /**
     * Index of the PROFILES search filter index
     */
    protected static final int PROFILES_IDX = 1;
    /**
     *
     */
    private static final long serialVersionUID = 2412399423079747117L;
    private static final Log logger = LogFactory.getLog(CreateEventWizard.class);
    private static final String USER_SPECIFIED_TWICE = "user_specified_twice";
    private static final String USER_NOT_SELECTED = "user_not_specified";
    private static final String AT_LEAST_TREE_CHAR = "invite_user_3char_error";
    private static final int MIN_CHAR_ALLOWED_FOR_QUERY = 3;
    private static final String MSG_ERROR_START_MEETING_PAST = "event_create_meetings_wizard_validation_start_in_past";
    private static final String MSG_ERROR_START_TIME_AFTER_END_TIME = "event_create_meetings_wizard_validation_start_after_end";
    private static final String MSG_ERROR_CONTACT_NAME_MISSING = "event_create_meetings_wizard_validation_contact_name_missing";
    private static final String MSG_ERROR_CONTACT_EMAIL_MISSING = "event_create_meetings_wizard_validation_contact_email_missing";
    private static final String MSG_ERROR_CONTACT_EMAIL_INVALID = "event_create_meetings_wizard_validation_contact_email_invalid";
    private static final String MSG_ERROR_CONTACT_PHONE_MISSING = "event_create_meetings_wizard_validation_contact_phone_missing";
    private static final String MSG_ERROR_TIME_MISSING = "event_create_meetings_wizard_validation_selected_occurence_miss_time";
    private static final String MSG_ERROR_EVERY_MISSING = "event_create_meetings_wizard_validation_selected_occurence_miss_every";
    private static final String MSG_ERROR_BAD_OCCURENCE_TIME = "event_create_meetings_wizard_validation_bad_occurence_time";
    private static final String MSG_ERROR_BAD_OCCURENCE_EVERY = "event_create_meetings_wizard_validation_bad_occurence_every";
    private static final String MSG_ERROR_BAD_ATTENDEES = "event_create_meetings_wizard_validation_bad_attendee_list";
    protected transient List<InternalUser> internalUsers;
    protected String externalUser;
    private transient PersonService personService;
    private transient EventService eventService;
    private Appointment appointment;
    private SelectItem[] filters;
    private transient DataModel userDataModel = null;
    private String everyAsString;
    private String timesOneAsString;
    private String timesTwoAsString;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        checkParams(parameters);
        reset();
        this.logRecord.setService("Event");
        this.logRecord.setActivity("Create meeting");
        this.isLoggingEnabled = true;
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {

        // the contact info are setted in the last step.
        validateContact(getAppointment());

        applyUserSelection();

        validateOccurenceRate();

        // if the fields are not valid, stay in the current step
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            isFinished = false;
            return null;
        } else {
            NodeRef nodeRef = getEventService()
                    .createMeeting(getActionNode().getNodeRef(), (Meeting) getAppointment());
            this.updateLogDocument(nodeRef);
        }

        return outcome;
    }


    protected void validateOccurenceRate() {
        OccurenceRate occurenceRate = getAppointment().getOccurenceRate();
        MainOccurence mainOccurence = occurenceRate.getMainOccurence();
        if (mainOccurence.equals(MainOccurence.OnlyOnce)) {
            occurenceRate.setTimesOccurence(null);
            occurenceRate.setEveryTimesOccurence(null);
        } else if (mainOccurence.equals(MainOccurence.EveryTimes)) {
            occurenceRate.setTimesOccurence(null);
        } else if (mainOccurence.equals(MainOccurence.Times)) {
            occurenceRate.setEveryTimesOccurence(null);
        }
    }

    public String getBrowserTitle() {
        return translate("event_create_meetings_wizard_browser_title");
    }

    public String getPageIconAltText() {
        return translate("event_create_meetings_wizard_icon_tooltip");
    }

    @Override
    public String next() {
        //get the current step
        int step = WaiApplication.getWizardManager().getState().getCurrentStep();
        int stepsSize = WaiApplication.getWizardManager().getStepItems().size();
        if (step == 2) {
            // validate step 1 !
            validateDetails(getAppointment());
        } else if (step == stepsSize) {
            // validate before last step (audience)
            validateAudience(getAppointment());
        }

        // if the fields are not valid, stay in the current step
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            //stay in the current step
            WaiApplication.getWizardManager().getState().setCurrentStep(step - 1);
        }

        return super.next();
    }

    /**
     * @return the available languages
     */
    public SelectItem[] getLanguages() {
        return getUserPreferencesBean().getContentFilterLanguages(false);
    }

    /**
     * @return the available types
     */
    public List<SelectItem> getMeetingTypes() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                MeetingType.values(),
                AppointmentWebUtils.MSG_PREFIX_MEETING_TYPE);
    }


    /**
     * @return true if the audience is open
     */
    public boolean isAudienceOpen() {
        return AudienceStatus.Open.equals(getAppointment().getAudienceStatus());
    }

    /**
     * @return the available timezones
     */
    public SelectItem[] getTimeZones() {

        final int tzCount = EventModel.EVENT_TIME_ZONE_CONSTRAINT_VALUES.size();
        final SelectItem[] items = new SelectItem[tzCount];

        for (int x = 0; x < tzCount; ++x) {
            items[x] = new SelectItem(EventModel.EVENT_TIME_ZONE_CONSTRAINT_VALUES.get(x));
        }

        return items;
    }

    /**
     * @return the available availabilities
     */
    public List<SelectItem> getAvailabilities() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                MeetingAvailability.values(),
                AppointmentWebUtils.MSG_PREFIX_AVAILABILITY);
    }

    /**
     * @return the audience statuses
     */
    public List<SelectItem> getAudienceStatuses() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                AudienceStatus.values(),
                AppointmentWebUtils.MSG_PREFIX_AUDIENCE_STATUS);
    }

    /**
     * @return the main occurence
     */
    public List<SelectItem> getMainOccurences() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                MainOccurence.values(),
                null);
    }

    /**
     * @return the times occurence
     */
    public List<SelectItem> getTimesOccurences() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                TimesOccurence.values(),
                AppointmentWebUtils.MSG_PREFIX_TIMES_OCCURENCE);
    }

    /**
     * @return the every times occurence
     */
    public List<SelectItem> getEveryTimesOccurences() {
        return EnumConverter.convertEnumToSelectItem(
                FacesContext.getCurrentInstance(),
                EveryTimesOccurence.values(),
                AppointmentWebUtils.MSG_PREFIX_EVERY_TIMES_OCCURENCE);
    }

    /**
     * @return the externalUser
     */
    public String getExternalUser() {
        return externalUser;
    }

    /**
     * @param externalUser the externalUser to set
     */
    public void setExternalUser(String externalUser) {
        this.externalUser = externalUser;
    }


    public SelectItem[] getFilters() {

        return new SelectItem[]
                {
                        new SelectItem("" + USERS_IDX,
                                NotificationUtils.translateAuthorityType(AuthorityType.USER))
                        , new SelectItem("" + PROFILES_IDX,
                        NotificationUtils.translateAuthorityType(AuthorityType.GROUP))
                };

    }

    /**
     * Query callback method executed by the Generic Picker component. This method is part of the
     * contract to the Generic Picker, it is up to the backing bean to execute whatever query is
     * appropriate and return the results.
     *
     * @param filterIndex Index of the filter drop-down selection
     * @param contains    Text from the contains textbox
     * @return An array of SelectItem objects containing the results to display in the picker.
     */
    public SelectItem[] pickerCallback(final int filterIndex, final String contains) {
        List<SortableSelectItem> result = null;

        if (filterIndex == PROFILES_IDX) {
            result = ProfileUtils
                    .buildMailableProfileItems(getActionNode(), contains, new ArrayList<String>(), logger);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The Profile search is performed successfully and return " + result + ". Filter Index: "
                                + filterIndex + ". Expression: " + contains + ".");
            }

        } else if (filterIndex == USERS_IDX) {
            result = PermissionUtils
                    .buildInvitedUserItems(getActionNode(), contains, false, new ArrayList<String>(), logger);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The User search is performed successfully and return " + result + ". Filter Index: "
                                + filterIndex + ". Expression: " + contains + ".");
            }
        } else {
            logger.error("The picker is called with an invalid index parameter " + filterIndex
                    + ". This last is not taken in account yet.");

            result = Collections.emptyList();
        }
        return result.toArray(new SelectItem[result.size()]);
    }


    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(final ActionEvent event) {
        final UIGenericPicker picker = (UIGenericPicker) event.getComponent().findComponent("picker");
        // test erreur

        final String[] results = picker.getSelectedResults();
        if (results == null) {
            Utils.addErrorMessage(translate(USER_NOT_SELECTED));
        } else {
            String authority = null;
            boolean foundExisting = false;
            AuthorityType authType;
            NodeRef ref;
            NodeRef interestGroup = getNavigator().getCurrentIGRoot().getNodeRef();
            final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(interestGroup);

            // all selected users
            for (String result : results) {
                authority = result;

                // only add if authority not already present in the list with same CircaRole
                foundExisting = false;
                for (InternalUser user : internalUsers) {
                    if (authority.equals(user.getAuthority())) {
                        foundExisting = true;
                        break;
                    }
                }

                // if found existing then user has to
                if (foundExisting == false) {
                    // build a display label showing the user and their profile for the space
                    authType = AuthorityType.getAuthorityType(authority);

                    if (authType == AuthorityType.GUEST || authType == AuthorityType.USER) {
                        //clen user id
                        if (authority.contains("@")) {
                            authority = authority.substring(0, authority.indexOf('@'));
                        }

                        if (!getPersonService().personExists(authority)) {
                            getUserService().createLdapUser(authority,false);
                        }

                        // found a User authority
                        ref = getPersonService().getPerson(authority);

                        internalUsers.add(new InternalUser(
                                authority,
                                (String) getNodeService().getProperty(ref, ContentModel.PROP_FIRSTNAME),
                                (String) getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME),
                                (String) getNodeService().getProperty(ref, ContentModel.PROP_EMAIL)
                        ));

                    } else {
                        internalUsers.add(new InternalUser(
                                authority,
                                profileManagerService.getProfileFromGroup(interestGroup, authority).getProfileName()
                        ));

                    }
                } else
                // foundExisting = true
                {
                    FacesContext.getCurrentInstance()
                            .addMessage(null, new FacesMessage(translate(USER_SPECIFIED_TWICE)));
                }
            } // end for each selecte user
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user
     */
    public void removeSelection(final ActionEvent event) {
        final InternalUser wrapper = (InternalUser) this.userDataModel.getRowData();
        if (wrapper != null) {
            this.internalUsers.remove(wrapper);
        }
    }


    /**
     * Returns the properties for current user-roles JSF DataModel
     *
     * @return JSF DataModel representing the current user-roles
     */
    public DataModel getUserDataModel() {
        if (this.userDataModel == null) {
            this.userDataModel = new ListDataModel();
        }
        Collections.sort(this.internalUsers);

        this.userDataModel.setWrappedData(this.internalUsers);

        return this.userDataModel;
    }

    protected void checkParams(Map<String, String> parameters) {
        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id is a manatory parameter");
        } else if (!NavigableNodeType.EVENT.isNodeFromType(getActionNode())) {
            throw new IllegalArgumentException("Aspect " + CircabcModel.ASPECT_EVENT_ROOT + " expected");
        }
    }

    protected void reset() {
        this.appointment = new MeetingImpl();
        resetFileds();
        resetAppointment(this.appointment);
        ((Meeting) this.appointment).setAvailability(MeetingAvailability.Private);
    }

    protected void resetFileds() {
        this.filters = null;
        this.userDataModel = null;
        this.internalUsers = new ArrayList<>(30);
        this.externalUser = null;
        everyAsString = null;
        timesOneAsString = null;
        timesTwoAsString = null;
    }

    protected void resetAppointment(Appointment appointment) {
        appointment.setAudienceStatus(AudienceStatus.Open);
        appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));
        appointment.setEnableNotification(Boolean.FALSE);

        if (!getNavigator().isGuest()) {
            final NodeRef person = getNavigator().getCurrentUser().getPerson();
            final Map<QName, Serializable> properties = getNodeService().getProperties(person);
            final String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
            final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
            final String phone = (String) properties.get(UserModel.PROP_PHONE);
            final String email = (String) properties.get(ContentModel.PROP_EMAIL);
            final String url = (String) properties.get(UserModel.PROP_URL);

            appointment
                    .setName((firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName));
            appointment.setPhone(phone);
            appointment.setEmail(email);
            appointment.setUrl(url);
        }

        appointment.setTimeZoneId(WebClientHelper.getCurrentGmt());

        final Calendar calendar = GregorianCalendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);
        final LocalTime nowTime = new LocalTime(hour, min);

        appointment.setStartDateAsDate(calendar.getTime());
        appointment.setStartTime(nowTime.plusMinutes(60));
        appointment.setEndTime(nowTime.plusMinutes(90));
    }

    protected void validateDetails(Appointment appointment) {
        final String title = appointment.getTitle();

        final Date startDate = appointment.getStartDateAsDate();

        final OccurenceRate occurenceRate = appointment.getOccurenceRate();
        final MainOccurence occurenceMain = occurenceRate.getMainOccurence();

        final int maxOccurence = AppointmentImpl.LIMIT_OCCURENCE_GENERATION;

        final Calendar todayCal = GregorianCalendar.getInstance();

        final Calendar startCal = GregorianCalendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, appointment.getStartTime().getHourOfDay());
        startCal.set(Calendar.MINUTE, appointment.getStartTime().getMinuteOfHour());

        final Calendar endCal = GregorianCalendar.getInstance();
        endCal.setTime(startDate);
        endCal.set(Calendar.HOUR_OF_DAY, appointment.getEndTime().getHourOfDay());
        endCal.set(Calendar.MINUTE, appointment.getEndTime().getMinuteOfHour());

        final Integer every = safeConvert(this.everyAsString, false);
        final Integer timesOne = safeConvert(this.timesOneAsString, false);
        final Integer timesTwo = safeConvert(this.timesTwoAsString, false);

        // -1 if the left today is before the start, 0 if they are on the same day, +1 if the today after start
        final int dateDiff = CalendarTagUtil.dayCompare(todayCal, startCal);

        if (title == null || title.trim().length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_TITLE_MISSING));
        }

        if (!isCleanHTML(title, false)) {
            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("title")));
        }

        if (dateDiff >= 0) {
            if (dateDiff == 1) {
                Utils.addErrorMessage(translate(MSG_ERROR_START_MEETING_PAST));
            } else if (startCal.before(todayCal)) {
                Utils.addErrorMessage(translate(MSG_ERROR_START_MEETING_PAST));
            }
        }
        if (startCal.after(endCal)) {
            Utils.addErrorMessage(translate(MSG_ERROR_START_TIME_AFTER_END_TIME));
        }
        if (occurenceMain.equals(MainOccurence.Times)) {
            if (timesOneAsString.trim().length() < 1) {
                Utils.addErrorMessage(translate(MSG_ERROR_TIME_MISSING));
            } else if (timesOne == null || timesOne > maxOccurence) {
                Utils.addErrorMessage(translate(MSG_ERROR_BAD_OCCURENCE_TIME, maxOccurence));
            } else {
                // all is ok
                occurenceRate.setTimes(timesOne);
            }
        }
        if (occurenceMain.equals(MainOccurence.EveryTimes)) {
            if (timesTwoAsString.trim().length() < 1) {
                Utils.addErrorMessage(translate(MSG_ERROR_TIME_MISSING));
            } else if (timesTwo == null || timesTwo > maxOccurence) {
                Utils.addErrorMessage(translate(MSG_ERROR_BAD_OCCURENCE_TIME, maxOccurence));
            } else {
                occurenceRate.setTimes(timesTwo);
            }

            if (everyAsString.trim().length() < 1) {
                Utils.addErrorMessage(translate(MSG_ERROR_EVERY_MISSING));
            } else if (every == null) {
                Utils.addErrorMessage(translate(MSG_ERROR_BAD_OCCURENCE_EVERY));
            } else {
                occurenceRate.setEvery(every);
            }
        }
    }

    protected void validateContact(Appointment appointment) {
        if (!isStringSetted(appointment.getName())) {
            Utils.addErrorMessage(translate(MSG_ERROR_CONTACT_NAME_MISSING));
        }
        if (!isStringSetted(appointment.getEmail())) {
            Utils.addErrorMessage(translate(MSG_ERROR_CONTACT_EMAIL_MISSING));
        } else if (!EmailValidator.isValid(appointment.getEmail())) {
            Utils.addErrorMessage(translate(MSG_ERROR_CONTACT_EMAIL_INVALID));
        }
        if (!isStringSetted(appointment.getPhone())) {
            Utils.addErrorMessage(translate(MSG_ERROR_CONTACT_PHONE_MISSING));
        }
    }

    protected void validateAudience(Appointment appointment) {
        if (isAudienceOpen() == false) {
            if (internalUsers.size() < 1 && !isStringSetted(externalUser)) {
                Utils.addErrorMessage(translate(MSG_ERROR_BAD_ATTENDEES));
            }
        }
    }

    protected void applyUserSelection() {
        if (isAudienceOpen() == false) {
            List<String> users = new ArrayList<>();

            if (externalUser != null && externalUser.length() > 0) {
                char separator = AppointmentImpl.separator.charAt(0);

                externalUser = externalUser.replace(',', separator);
                externalUser = externalUser.replace(';', separator);
                externalUser = externalUser.replace('\n', separator);

                StringTokenizer tokens = new StringTokenizer(externalUser, AppointmentImpl.separator,
                        false);
                String email;
                while (tokens.hasMoreTokens()) {
                    email = tokens.nextToken().trim();
                    if (email.length() > 0) {
                        String authority = getUserService().getUserByEmail(email);
                        if (authority == null) {
                            users.add(email);
                        } else {
                            if (!getPersonService().personExists(authority)) {
                                getUserService().createLdapUser(authority,false);
                            }
                            users.add(authority);
                        }

                    }
                }
            }

            if (internalUsers != null && internalUsers.size() > 0) {
                String authority;
                NodeRef igRootRef = getNavigator().getCurrentIGRoot().getNodeRef();
                Profile profile;
                IGRootProfileManagerService igRootProfileManagerService = getProfileManagerServiceFactory()
                        .getIGRootProfileManagerService();
                Set<String> usersInProfile;

                for (InternalUser internalUser : internalUsers) {
                    authority = internalUser.getAuthority();

                    if (AuthorityType.getAuthorityType(authority).equals(AuthorityType.GROUP)) {
                        profile = igRootProfileManagerService.getProfileFromGroup(igRootRef, authority);
                        usersInProfile = igRootProfileManagerService
                                .getPersonInProfile(igRootRef, profile.getProfileName());

                        for (final String userInProfile : usersInProfile) {
                            users.add(userInProfile);
                        }
                    } else {
                        users.add(authority);
                    }

                }
            }

            getAppointment().setInvitedUsers(users);

        }
    }


    private boolean isStringSetted(String strToTest) {
        return strToTest != null && strToTest.trim().length() > 0;
    }

    private Integer safeConvert(final String intAsString, final boolean canBeNegative) {
        if (intAsString == null || intAsString.trim().length() < 1) {
            return null;
        } else {
            try {
                final Integer value = Integer.parseInt(intAsString);

                return (!canBeNegative && value < 0) ? null : value;

            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    public String getLibraryId() {
        final NavigableNode library = (NavigableNode) getNavigator().getCurrentIGRoot()
                .get(InterestGroupNode.LIBRARY_SERVICE);

        // if library is null, the user probably has not rights to see it
        return library == null ? null : library.getId();
    }

    /**
     * @return the appointment
     */
    public Appointment getAppointment() {
        return this.appointment;
    }

    /**
     * @param appointment the appointment to set
     */
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    /**
     * @return the everyAsString
     */
    public String getEveryAsString() {
        return everyAsString;
    }

    /**
     * @param everyAsString the everyAsString to set
     */
    public void setEveryAsString(String everyAsString) {
        this.everyAsString = everyAsString;
    }

    /**
     * @return the timesOneAsString
     */
    public String getTimesOneAsString() {
        return timesOneAsString;
    }

    /**
     * @param timesOneAsString the timesOneAsString to set
     */
    public void setTimesOneAsString(String timesOneAsString) {
        this.timesOneAsString = timesOneAsString;
    }

    /**
     * @return the timesTwoAsString
     */
    public String getTimesTwoAsString() {
        return timesTwoAsString;
    }

    /**
     * @param timesTwoAsString the timesTwoAsString to set
     */
    public void setTimesTwoAsString(String timesTwoAsString) {
        this.timesTwoAsString = timesTwoAsString;
    }

    /**
     * @return the eventService
     */
    protected final EventService getEventService() {
        if (eventService == null) {
            eventService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getEventService();
        }
        return eventService;
    }

    /**
     * @param eventService the eventService to set
     */
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
