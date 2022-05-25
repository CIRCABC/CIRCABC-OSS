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
package eu.cec.digit.circabc.web.wai.bean.navigation.event;

import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.InterestGroupBean;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

import static eu.cec.digit.circabc.model.EventModel.TYPE_EVENT;

/**
 * Bean that backs the navigation for a meeting or a event
 *
 * @author yanick pignot
 */
public class AppointmentDetailsBean extends InterestGroupBean {

    /**
     * Logger
     */
    //private static final Log logger = LogFactory.getLog(EventBean.class);

    public static final String JSP_NAME = "appointment-details.jsp";
    public static final String BEAN_NAME = "AppointmentDetailsBean";
    public static final String PROPERTY_APPOINTEMENT_OBJECT = "appointment";
    public static final String PROPERTY_IS_RECURRENT = "isReccurent";
    /**
     *
     */
    private static final long serialVersionUID = -6967963147859111196L;
    private static final String ACTIONS_MEETING = "meeting_details_actions_wai";
    private static final String ACTIONS_EVENT = "event_details_actions_wai";

    private static final String MSG_MEETING_PAGE_TITLE = "event_view_meetings_details_dialog_title";
    private static final String MSG_MEETING_PAGE_DESCRIPTION = "event_view_meetings_details_dialog_description";
    private static final String MSG_MEETING_PAGE_ICON_ALT = "event_view_meetings_details_dialog_icon_tooltip";
    private static final String MSG_MEETING_BROWSER_TITLE = "event_view_meetings_details_dialog_browser_title";

    private static final String MSG_EVENT_PAGE_TITLE = "event_view_event_details_dialog_title";
    private static final String MSG_EVENT_PAGE_DESCRIPTION = "event_view_event_details_dialog_description";
    private static final String MSG_EVENT_PAGE_ICON_ALT = "event_view_event_details_dialog_icon_tooltip";
    private static final String MSG_EVENT_BROWSER_TITLE = "event_view_event_details_dialog_browser_title";

    private static final String OCCUR_ONCE = "event_view_meetings_details_dialog_occurence_once";
    private static final String OCCUR_TIME = "event_view_meetings_details_dialog_occurence_times";
    private static final String OCCUR_EVERY_TIME = "event_view_meetings_details_dialog_occurence_every";

    private static final String PARAM_DIALOG_CALL_RETURN = "dialogAfterClose";
    private static final String PARAM_NODE_BROWSE_RETURN = "nodeIdAfterClose";

    private String closeOutcome;
    private String lastNodeId;
    private Appointment appointment;
    private Boolean isMeeting;
    private MapNode appointmentNode;

    private transient EventService eventService;
    private transient PersonService personService;
    private transient ContentFilterLanguagesService contentFilterLanguagesService;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.EVENT_APPOINTMENT;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "event/" + JSP_NAME;
    }

    public String getPageDescription() {
        return translate((isMeeting() ? MSG_MEETING_PAGE_DESCRIPTION : MSG_EVENT_PAGE_DESCRIPTION));
    }

    public String getPageTitle() {
        return translate((isMeeting() ? MSG_MEETING_PAGE_TITLE : MSG_EVENT_PAGE_TITLE),
                getAppointment().getTitle());
    }

    public String getPageIconAltText() {
        return translate((isMeeting() ? MSG_MEETING_PAGE_ICON_ALT : MSG_EVENT_PAGE_ICON_ALT));
    }

    @Override
    public String getBrowserTitle() {
        return translate((isMeeting() ? MSG_MEETING_BROWSER_TITLE : MSG_EVENT_BROWSER_TITLE));
    }

    @Override
    public String getPageIcon() {
        return "/images/icons/project-icon-forums.gif";
    }

    public String getCloseOutcome() {
        if (lastNodeId != null) {
            /*
             * This ugly workaroud is implemented for response to the lack of the navigation case:
             *
             * 	dialog A -> browse B -> dialog A.
             *
             */

            getBrowseBean().clickWai(lastNodeId);

            final FacesContext fc = FacesContext.getCurrentInstance();

            final Stack stack = CircabcNavigationHandler.getViewStack(fc);
            if (stack != null) {
                if (stack.size() > 0) {
                    // remove the browse lastNodeId call!
                    stack.pop();
                }
                if (stack.size() > 0) {
                    // remove this page call of the stack!
                    stack.pop();
                }
                if (stack.size() > 0) {
                    // remove the last dialogCall
                    stack.pop();
                }
            }
            // change the view id from Navigation container to Dialog container.
            ViewHandler viewHandler = fc.getApplication().getViewHandler();
            UIViewRoot viewRoot = viewHandler
                    .createView(fc, CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
            viewRoot.setViewId(CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
            fc.setViewRoot(viewRoot);
        }

        return closeOutcome;
    }

    @Override
    public String getWebdavUrl() {
        // not relevant for calendar
        return null;
    }

    public String getActionGroup() {
        return isMeeting() ? ACTIONS_MEETING : ACTIONS_EVENT;
    }

    /**
     * @return the appointment
     */
    public Appointment getAppointment() {
        return appointment;
    }

    /**
     * @return the appointment
     */
    public Node getAppointmentNode() {
        return appointmentNode;
    }

    public boolean isMeeting() {
        return isMeeting;
    }

    public String getDisplayEventType() {
        return AppointmentWebUtils.translate(((Event) getAppointment()).getEventType());
    }

    public String getDisplayMeetingType() {
        return ((Meeting) getAppointment()).getMeetingTypeString();
    }

    public String getDisplayMeetingAvailability() {
        return AppointmentWebUtils.translate(((Meeting) getAppointment()).getAvailability());
    }

    public String getDisplayEventPriority() {
        return AppointmentWebUtils.translate(((Event) getAppointment()).getPriority());
    }

    public String getDisplayAudienceStatus() {
        return AppointmentWebUtils.translate(getAppointment().getAudienceStatus());
    }

    public String getDisplayTimesOccurence() {
        return AppointmentWebUtils.translate(getAppointment().getOccurenceRate().getTimesOccurence());
    }

    public String getDisplayEveryTimesOccurence() {
        return AppointmentWebUtils
                .translate(getAppointment().getOccurenceRate().getEveryTimesOccurence());
    }

    public String getUserStatus() {
        return AppointmentWebUtils.translate(getEventService()
                .getMeetingStatus(getCurrentNode().getNodeRef(),
                        getNavigator().getCurrentUser().getUserName()));
    }

    public String getDisplayLanguage() {
        return getContentFilterLanguagesService().getLabelByCode(getAppointment().getLanguage());
    }

    public int getAgendaLines() {
        int lines = 3;

        if (isMeeting()) {
            final String agenda = ((Meeting) getAppointment()).getAgenda();

            if (agenda != null) {
                final StringTokenizer tokens = new StringTokenizer(agenda, "\n");
                int tokenCount = tokens.countTokens();

                if (tokenCount > lines) {
                    lines = tokenCount;
                }
            }

        }

        return lines;
    }

    public void init(Map<String, String> parameters) {
        // ensure the reset of files
        this.appointment = null;
        this.isMeeting = null;
        this.appointmentNode = null;
        this.lastNodeId = null;
        this.closeOutcome = null;

        if (parameters != null) {
            this.closeOutcome = parameters.get(PARAM_DIALOG_CALL_RETURN);
            this.lastNodeId = parameters.get(PARAM_NODE_BROWSE_RETURN);
        }

        if (closeOutcome == null) {
            this.closeOutcome = CircabcBrowseBean.PREFIXED_WAI_CLOSE_NAVIGATION_OUTCOME;
        } else {
            this.closeOutcome = CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                    + CircabcNavigationHandler.OUTCOME_SEPARATOR + this.closeOutcome;
        }

        if (TYPE_EVENT.isMatch(getCurrentNode().getType())) {
            final NodeRef eventItem = getCurrentNode().getNodeRef();
            this.appointment = getEventService().getAppointmentByNodeRef(eventItem);
            this.isMeeting = this.appointment instanceof Meeting;
            this.appointmentNode = new MapNode(eventItem);
            final Boolean isRecurrent = !MainOccurence.OnlyOnce
                    .equals(appointment.getOccurenceRate().getMainOccurence());
            this.appointmentNode.put(PROPERTY_APPOINTEMENT_OBJECT, appointment);
            this.appointmentNode.put(PROPERTY_IS_RECURRENT, isRecurrent);
        }
        // else probably in a restaure node ... nearly null pointer if not the case

    }

    public String getSpaceDisplayPath() {
        if (!isMeeting) {
            throw new IllegalAccessError("Event doesn't have library section");
        }
        final NodeRef ref = ((Meeting) getAppointment()).getLibrarySection();
        if (ref != null) {
            final String parentPath = (String) getBrowseBean().resolverSimpleDisplayPath
                    .get(new Node(ref));
            final String leafName = (String) getNodeService().getProperty(ref, ContentModel.PROP_NAME);
            return parentPath + leafName + '/';
        } else {
            return "";
        }
    }

    public List<AudienceUser> getAudienceDetail() {
        final HashMap<String, MeetingRequestStatus> statuses = getAppointment().getAudience();
        final List<AudienceUser> audienceUsers = new ArrayList<>(statuses.size());

        String authority = null;
        NodeRef person = null;
        Map<QName, Serializable> properties = null;
        String status;
        for (Map.Entry<String, MeetingRequestStatus> entry : statuses.entrySet()) {
            authority = entry.getKey();
            status = (MeetingRequestStatus.NotApplicable.equals(entry.getValue())
                    ? ""
                    : translate(AppointmentWebUtils.MSG_PREFIX_REQUEST_STATUS + entry.getValue().name()
                    .toLowerCase()));

            if (getPersonService().personExists(authority)) {
                // it is an internal user
                person = getPersonService().getPerson(authority);
                properties = getNodeService().getProperties(person);

                audienceUsers.add(new AudienceUser(
                        (String) properties.get(ContentModel.PROP_FIRSTNAME),
                        (String) properties.get(ContentModel.PROP_LASTNAME),
                        (String) properties.get(ContentModel.PROP_EMAIL),
                        status));
            } else {
                //it is an external user
                audienceUsers.add(new AudienceUser("", "", authority, status));
            }
        }

        Collections.sort(audienceUsers);

        return audienceUsers;
    }

    public String getOccurenceAsString() {
        String occurStr = null;
        final OccurenceRate occurenceRate = getAppointment().getOccurenceRate();
        switch (occurenceRate.getMainOccurence()) {
            case OnlyOnce:
                occurStr = translate(OCCUR_ONCE);
                break;

            case Times:
                occurStr = translate(OCCUR_TIME, getDisplayTimesOccurence(), occurenceRate.getTimes());
                break;

            case EveryTimes:
                occurStr = translate(OCCUR_EVERY_TIME, occurenceRate.getEvery(),
                        getDisplayEveryTimesOccurence(), occurenceRate.getTimes());
                break;
        }

        return occurStr;
    }

    /**
     * @return the list of available action for the current node that should be a Event root or child
     */
    public List<ActionWrapper> getActions() {
        return null;
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
        if (this.personService == null) {
            this.personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
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

    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (this.contentFilterLanguagesService == null) {
            this.contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentFilterLanguagesService();
        }
        return contentFilterLanguagesService;
    }

    /**
     * @param contentFilterLanguagesService the contentFilterLanguagesService to set
     */
    public final void setContentFilterLanguagesService(
            ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }
}
