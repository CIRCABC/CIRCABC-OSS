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
package eu.cec.digit.circabc.web.wai.dialog.event;

import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.repo.event.AppointmentUtils;
import eu.cec.digit.circabc.service.event.EventFilter;
import eu.cec.digit.circabc.service.event.EventItem;
import eu.cec.digit.circabc.service.event.EventService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExportTypeEnum;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Bean that back appoitments (Meetings and Events) search and display page.
 *
 * @author Yanick Pignot
 */
public class ViewAppointments extends BaseWaiDialog {

    public static final String BEAN_NAME = "ViewAppointments";
    /**
     * The name of this dialog
     */
    public static final String DIALOG_NAME = "viewEventsMeetingWai";
    private static final String PARAM_IG_SELECT = "igSelection";
    private static final String SELECT_CHOICE_IG_THIS = "ig";
    private static final String SELECT_CHOICE_IG_ALL = "all";
    private static final String SELECT_CHOICE_DATE_EXACT = "exact";
    private static final String SELECT_CHOICE_DATE_PREVIOUS = "previous";
    private static final String SELECT_CHOICE_DATE_FUTURE = "future";
    /**
     *
     */
    private static final long serialVersionUID = -2599920262997080980L;
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(ViewAppointments.class);
    transient private EventService eventService;
    private Date exactDate = new Date();
    private String periodSelection = SELECT_CHOICE_DATE_FUTURE;
    private String interestGroupSelection = SELECT_CHOICE_IG_ALL;
    transient private List<EventItem> appointments;
    private String userName = getNavigator().getCurrentAlfrescoUserName();
    private ExportTypeEnum exportType = ExportTypeEnum.CSV;

    public String getBrowserTitle() {
        return translate("event_view_appointments_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("event_view_appointments_dialog_icon_tooltip");
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

    public boolean isCurrentIgDisabled() {
        return getNavigator().getCurrentIGRoot() == null;
    }


    /**
     * Search appointments for current user, between two dates, for one or all interest group
     */
    public void search() {
        if (this.periodSelection == null) {
            init(null);
        }

        EventFilter filter;
        // this.exactDate
        if (this.periodSelection.equalsIgnoreCase(SELECT_CHOICE_DATE_FUTURE)) {
            filter = EventFilter.Future;
        } else if (this.periodSelection.equalsIgnoreCase(SELECT_CHOICE_DATE_PREVIOUS)) {
            filter = EventFilter.Previous;

        } else if (this.periodSelection.equalsIgnoreCase(SELECT_CHOICE_DATE_EXACT)) {
            filter = EventFilter.Exact;

        } else {
            throw new IllegalStateException("Unknown filter : " + this.periodSelection);

        }

        NodeRef eventRoot = null;
        if (this.interestGroupSelection.equals(SELECT_CHOICE_IG_ALL) || getActionNode() == null) {
            eventRoot = null;
        } else if (this.interestGroupSelection.equalsIgnoreCase(SELECT_CHOICE_IG_THIS)) {
            eventRoot = getActionNode().getNodeRef();
        } else {
            throw new IllegalStateException(
                    "Unknown interest group selector : " + this.interestGroupSelection);
        }
        this.appointments = getEventService().getAppointments(filter, eventRoot, userName,
                AppointmentUtils.convertDateToDateValue(exactDate));

    }


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        String igPreSelect = null;

        if (parameters != null) {
            igPreSelect = parameters.get(PARAM_IG_SELECT);

            // user comme the left menu action
            this.userName = null;
            this.exactDate = null;
            this.periodSelection = null;
            this.interestGroupSelection = null;
            this.appointments = null;
        } else {
            // the user click on search
        }

        // user comme the left menu action
        if (userName == null) {
            this.userName = getNavigator().getCurrentAlfrescoUserName();
        }
        if (exactDate == null) {
            this.exactDate = new Date();
        }
        if (periodSelection == null) {
            this.periodSelection = SELECT_CHOICE_DATE_FUTURE;
        }
        if (interestGroupSelection == null) {
            if (igPreSelect == null) {
                this.interestGroupSelection =
                        (isCurrentIgDisabled()) ? SELECT_CHOICE_IG_ALL : SELECT_CHOICE_IG_THIS;
            } else {
                this.interestGroupSelection = igPreSelect;
            }
        }
        if (exactDate == null) {
            this.appointments = new ArrayList<>();
        }


    }

    /**
     * @return the exactDate
     */
    public Date getExactDate() {
        return exactDate;
    }

    /**
     * @param exactDate the exactDate to set
     */
    public void setExactDate(Date exactDate) {
        this.exactDate = exactDate;
    }

    /**
     * @return the periodSelection
     */
    public String getPeriodSelection() {
        return periodSelection;
    }

    /**
     * @param periodSelection the periodSelection to set
     */
    public void setPeriodSelection(String periodSelection) {
        this.periodSelection = periodSelection;
    }

    /**
     * @return the interestGroupSelection
     */
    public String getInterestGroupSelection() {
        return interestGroupSelection;
    }

    /**
     * @param interestGroupSelection the interestGroupSelection to set
     */
    public void setInterestGroupSelection(String interestGroupSelection) {
        this.interestGroupSelection = interestGroupSelection;
    }

    /**
     * @return the appointments
     */
    public List<EventItem> getAppointments() {
        search();
        return appointments;
    }

    /**
     * @param appointments the appointments to set
     */
    public void setAppointments(List<EventItem> appointments) {
        this.appointments = appointments;
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("view_appointments_search_button_label");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {

        // stay in the same page
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                CircabcNavigationHandler.OUTCOME_SEPARATOR +
                CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;

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

    public void export() {
        search();
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        String extension =
                exportType.equals(ExportTypeEnum.Excel) ? "xls" : exportType.toString().toLowerCase();

        ServletOutputStream outStream = null;
        try {
            outStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment;filename=Events." + extension);
            switch (exportType) {
                case CSV:
                    response.setContentType("text/csv;charset=UTF-8");
                    writeCSV(outStream);
                    break;
                case XML:
                    response.setContentType("text/xml;charset=UTF-8");
                    writeXML(outStream);
                    break;
                case Excel:
                    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                    writeXLS(outStream);
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

    private void writeXML(ServletOutputStream outStream) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = null;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("appointments");

        for (EventItem appointment : appointments) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("appointment");
            xtw.writeAttribute("contact", appointment.getContact());
            xtw.writeAttribute("interest group", appointment.getInterestGroup());
            xtw.writeAttribute("interest group title", appointment.getInterestGroupTitle());
            xtw.writeAttribute("title", appointment.getTitle());
            xtw.writeAttribute("date", appointment.getDate().toString());
            xtw.writeAttribute("start time", appointment.getStartTime().toString());
            xtw.writeAttribute("end time", appointment.getEndTime().toString());
            xtw.writeAttribute("type", appointment.getEventType().name());
            xtw.writeAttribute("node reference", appointment.getEventNodeRef().toString());

            Map<QName, Serializable> additionalNodeProperties = getNodeProperties(
                    appointment.getEventNodeRef());
            xtw.writeAttribute("abstract", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
            xtw.writeAttribute("audience", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
            xtw.writeAttribute("invitation message", WebClientHelper.emptyStringIfNull(
                    additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
            xtw.writeAttribute("invited users", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
            xtw.writeAttribute("language", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
            xtw.writeAttribute("location", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
            xtw.writeAttribute("occurence rate", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE)));
            xtw.writeAttribute("phone", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
            xtw.writeAttribute("priority", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
            xtw.writeAttribute("url", WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

            xtw.writeEndElement();
        }
        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();

    }

    private void writeXLS(ServletOutputStream outStream) throws IOException {
        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Appointments");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("contact");
        titleRow.createCell(1).setCellValue("interest group");
        titleRow.createCell(2).setCellValue("interest group title");
        titleRow.createCell(3).setCellValue("title");
        titleRow.createCell(4).setCellValue("date");
        titleRow.createCell(5).setCellValue("start time");
        titleRow.createCell(6).setCellValue("end time");
        titleRow.createCell(7).setCellValue("type");
        titleRow.createCell(8).setCellValue("node reference");
        titleRow.createCell(9).setCellValue("abstract");
        titleRow.createCell(10).setCellValue("audience");
        titleRow.createCell(11).setCellValue("invitation message");
        titleRow.createCell(12).setCellValue("invited users");
        titleRow.createCell(13).setCellValue("language");
        titleRow.createCell(14).setCellValue("location");
        titleRow.createCell(15).setCellValue("occurence rate");
        titleRow.createCell(16).setCellValue("phone");
        titleRow.createCell(17).setCellValue("priority");
        titleRow.createCell(18).setCellValue("url");

        int idx = 1;

        for (EventItem appointment : appointments) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(appointment.getContact());
            row.createCell(1).setCellValue(appointment.getInterestGroup());
            row.createCell(2).setCellValue(appointment.getInterestGroupTitle());
            row.createCell(3).setCellValue(appointment.getTitle());
            row.createCell(4).setCellValue(appointment.getDate().toString());
            row.createCell(5).setCellValue(appointment.getStartTime().toString());
            row.createCell(6).setCellValue(appointment.getEndTime().toString());
            row.createCell(7).setCellValue(appointment.getEventType().toString());
            row.createCell(8).setCellValue(appointment.getEventNodeRef().toString());

            Map<QName, Serializable> additionalNodeProperties = getNodeProperties(
                    appointment.getEventNodeRef());

            row.createCell(9).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
            row.createCell(10).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
            row.createCell(11).setCellValue(WebClientHelper.emptyStringIfNull(
                    additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
            row.createCell(12).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
            row.createCell(13).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
            row.createCell(14).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
            row.createCell(15).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE)));
            row.createCell(16).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
            row.createCell(17).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
            row.createCell(18).setCellValue(WebClientHelper
                    .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

            idx++;
        }

        workbook.write(outStream);

    }

    private void writeCSV(ServletOutputStream outStream) throws IOException {
        OutputStreamWriter outStreamWriter = null;
        try {
            outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("contact");
            outStreamWriter.write(',');
            outStreamWriter.write("interest group");
            outStreamWriter.write(',');
            outStreamWriter.write("interest group title");
            outStreamWriter.write(',');
            outStreamWriter.write("title");
            outStreamWriter.write(',');
            outStreamWriter.write("date");
            outStreamWriter.write(',');
            outStreamWriter.write("start time");
            outStreamWriter.write(',');
            outStreamWriter.write("end time");
            outStreamWriter.write(',');
            outStreamWriter.write("type");
            outStreamWriter.write(',');
            outStreamWriter.write("node reference");
            outStreamWriter.write(',');
            outStreamWriter.write("abstract");
            outStreamWriter.write(',');
            outStreamWriter.write("audience");
            outStreamWriter.write(',');
            outStreamWriter.write("invitation message");
            outStreamWriter.write(',');
            outStreamWriter.write("invited users");
            outStreamWriter.write(',');
            outStreamWriter.write("language");
            outStreamWriter.write(',');
            outStreamWriter.write("location");
            outStreamWriter.write(',');
            outStreamWriter.write("occurence rate");
            outStreamWriter.write(',');
            outStreamWriter.write("phone");
            outStreamWriter.write(',');
            outStreamWriter.write("priority");
            outStreamWriter.write(',');
            outStreamWriter.write("url");

            outStreamWriter.write('\n');

            for (EventItem appointment : appointments) {
                outStreamWriter.write(appointment.getContact());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getInterestGroup());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getInterestGroupTitle());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getTitle());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getDate().toString());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getStartTime().toString());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getEndTime().toString());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getEventType().name());
                outStreamWriter.write(',');
                outStreamWriter.write(appointment.getEventNodeRef().toString());
                outStreamWriter.write(',');

                Map<QName, Serializable> additionalNodeProperties = getNodeProperties(
                        appointment.getEventNodeRef());
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper.emptyStringIfNull(
                        additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
                outStreamWriter.write(',');
                outStreamWriter.write(WebClientHelper
                        .emptyStringIfNull(additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

                outStreamWriter.write('\n');
            }
        } finally {
            if (outStreamWriter != null) {
                try {
                    outStreamWriter.flush();
                } catch (IOException ignore) {

                }
                try {
                    outStreamWriter.close();
                } catch (IOException ignore) {

                }
            }
        }
    }


    private Map<QName, Serializable> getNodeProperties(NodeRef nodeRef) {
        List<ChildAssociationRef> parentAssocs = getNodeService().getParentAssocs(nodeRef);
        NodeRef container = parentAssocs.get(0).getParentRef();
        List<ChildAssociationRef> parentContainerAssocs = getNodeService().getParentAssocs(container);
        NodeRef eventDefinition = parentContainerAssocs.get(0).getParentRef();

        return getNodeService().getProperties(eventDefinition);


    }

}


	
