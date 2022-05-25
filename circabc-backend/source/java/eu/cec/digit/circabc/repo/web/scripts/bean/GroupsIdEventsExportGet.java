package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.event.EventItem;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import eu.cec.digit.circabc.web.WebClientHelper;
import io.swagger.api.EventsApi;
import io.swagger.model.PagedEventItems;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Webscript entry to export the events of an IG in different formats: CSV, XLS, or XML
 *
 * @author schwerr
 */
public class GroupsIdEventsExportGet extends AbstractWebScript {

    public static final String ABSTRACT = "abstract";
    public static final String AUDIENCE = "audience";
    public static final String INVITATION_MESSAGE = "invitation message";
    public static final String INVITED_USERS = "invited users";
    public static final String LANGUAGE = "language";
    public static final String LOCATION = "location";
    public static final String OCCURRENCE_RATE = "occurrence rate";
    public static final String PHONE = "phone";
    public static final String PRIORITY = "priority";
    public static final String CONTACT = "contact";
    public static final String INTEREST_GROUP = "interest group";
    public static final String INTEREST_GROUP_TITLE = "interest group title";
    public static final String TITLE = "title";
    public static final String START_TIME = "start time";
    public static final String END_TIME = "end time";
    public static final String NODE_REFERENCE = "node reference";
    static final Log logger = LogFactory.getLog(GroupsIdEventsExportGet.class);
    private EventsApi eventsApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private ApiToolBox apiToolBox;

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");
        String filter = req.getParameter("filter");
        String exactDateStr = req.getParameter("exactDate");

        try {
            NodeRef groupRef = Converter.createNodeRefFromId(id);
            NodeRef evtNodeRef =
                    this.nodeService.getChildByName(groupRef, ContentModel.ASSOC_CONTAINS, "Events");

            if (!this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
                    evtNodeRef.getId(), EventPermissions.EVEACCESS)) {
                throw new AccessDeniedException(
                        "Cannot export events because user does not have enough permissions");
            }

            if (!("Exact".equals(filter) || "Future".equals(filter) || "Previous".equals(filter))) {
                throw new IllegalArgumentException(
                        "Invalid filter value. Must be " + "'Exact', 'Future' or 'Previous'");
            }

            String format = req.getParameter("format");

            if ((format == null)
                    || !("csv".equalsIgnoreCase(format)
                    || "xls".equalsIgnoreCase(format)
                    || "xml".equalsIgnoreCase(format))) {
                throw new IllegalArgumentException("Export 'format' must be CSV, XML or XLS");
            }

            Date exactDate = null;

            if ((exactDateStr != null) && !exactDateStr.isEmpty()) {

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                exactDate = parseExactDate(exactDateStr, simpleDateFormat);
            }

            MLPropertyInterceptor.setMLAware(false);

            PagedEventItems pagedEventItems =
                    this.eventsApi.groupsIdEventsListGet(id, filter, exactDate, 1, 0, null);

            List<EventItem> eventItems = pagedEventItems.getData();

            this.export(eventItems, format.toLowerCase(), res);

        } catch (AccessDeniedException ade) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            logger.error("Could not export events.", e);
            throw new IOException("Could not export events.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    private Date parseExactDate(String exactDateStr, SimpleDateFormat simpleDateFormat) {
        Date exactDate;
        try {
            exactDate = simpleDateFormat.parse(exactDateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "The 'exactDate' has a wrong format. Must be yyyy-MM-dd", e);
        }
        return exactDate;
    }

    /**
     * @param eventsApi the eventsApi to set
     */
    public void setEventsApi(EventsApi eventsApi) {
        this.eventsApi = eventsApi;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void export(List<EventItem> eventItems, String format, WebScriptResponse response)
            throws IOException, XMLStreamException {

        try (OutputStream outStream = response.getOutputStream()) {
            response.setHeader("Content-Disposition", "attachment;filename=Events." + format);
            switch (format) {
                case "csv":
                    response.setContentType("text/csv;charset=UTF-8");
                    this.writeCSV(eventItems, outStream);
                    break;
                case "xml":
                    response.setContentType("text/xml;charset=UTF-8");
                    this.writeXML(eventItems, outStream);
                    break;
                case "xls":
                    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                    this.writeXLS(eventItems, outStream);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + format);
            }
        }
    }

    private void writeXML(List<EventItem> eventItems, OutputStream outStream)
            throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("appointments");

        for (EventItem appointment : eventItems) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("appointment");
            xtw.writeAttribute(CONTACT, appointment.getContact());
            xtw.writeAttribute(INTEREST_GROUP, appointment.getInterestGroup());
            xtw.writeAttribute(INTEREST_GROUP_TITLE, appointment.getInterestGroupTitle());
            xtw.writeAttribute(TITLE, appointment.getTitle());
            xtw.writeAttribute("date", appointment.getDate().toString());
            xtw.writeAttribute(START_TIME, appointment.getStartTime().toString());
            xtw.writeAttribute(END_TIME, appointment.getEndTime().toString());
            xtw.writeAttribute("type", appointment.getEventType().name());
            xtw.writeAttribute(NODE_REFERENCE, appointment.getEventNodeRef().toString());

            Map<QName, Serializable> additionalNodeProperties =
                    this.getNodeProperties(appointment.getEventNodeRef());
            xtw.writeAttribute(
                    ABSTRACT,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
            xtw.writeAttribute(
                    AUDIENCE,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
            xtw.writeAttribute(
                    INVITATION_MESSAGE,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
            xtw.writeAttribute(
                    INVITED_USERS,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
            xtw.writeAttribute(
                    LANGUAGE,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
            xtw.writeAttribute(
                    LOCATION,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
            xtw.writeAttribute(
                    OCCURRENCE_RATE,
                    WebClientHelper.emptyStringIfNull(
                            apiToolBox.getOccurenceAsString(
                                    (String) additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE))));
            xtw.writeAttribute(
                    PHONE,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
            xtw.writeAttribute(
                    PRIORITY,
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
            xtw.writeAttribute(
                    "url",
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

            xtw.writeEndElement();
        }

        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }

    private void writeXLS(List<EventItem> eventItems, OutputStream outStream) throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Appointments");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue(CONTACT);
        titleRow.createCell(1).setCellValue(INTEREST_GROUP);
        titleRow.createCell(2).setCellValue(INTEREST_GROUP_TITLE);
        titleRow.createCell(3).setCellValue(TITLE);
        titleRow.createCell(4).setCellValue("date");
        titleRow.createCell(5).setCellValue(START_TIME);
        titleRow.createCell(6).setCellValue(END_TIME);
        titleRow.createCell(7).setCellValue("type");
        titleRow.createCell(8).setCellValue(NODE_REFERENCE);
        titleRow.createCell(9).setCellValue(ABSTRACT);
        titleRow.createCell(10).setCellValue(AUDIENCE);
        titleRow.createCell(11).setCellValue(INVITATION_MESSAGE);
        titleRow.createCell(12).setCellValue(INVITED_USERS);
        titleRow.createCell(13).setCellValue(LANGUAGE);
        titleRow.createCell(14).setCellValue(LOCATION);
        titleRow.createCell(15).setCellValue(OCCURRENCE_RATE);
        titleRow.createCell(16).setCellValue(PHONE);
        titleRow.createCell(17).setCellValue(PRIORITY);
        titleRow.createCell(18).setCellValue("url");

        int idx = 1;

        for (EventItem appointment : eventItems) {
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

            Map<QName, Serializable> additionalNodeProperties =
                    this.getNodeProperties(appointment.getEventNodeRef());

            row.createCell(9)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
            row.createCell(10)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
            row.createCell(11)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
            row.createCell(12)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
            row.createCell(13)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
            row.createCell(14)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
            row.createCell(15)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    apiToolBox.getOccurenceAsString(
                                            (String)
                                                    additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE))));
            row.createCell(16)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
            row.createCell(17)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
            row.createCell(18)
                    .setCellValue(
                            WebClientHelper.emptyStringIfNull(
                                    additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

            idx++;
        }

        workbook.write(outStream);
    }

    private void writeCSV(List<EventItem> eventItems, OutputStream outStream) throws IOException {

        OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);

        // write byte order mark
        outStream.write(0xEF);
        outStream.write(0xBB);
        outStream.write(0xBF);

        outStreamWriter.write(CONTACT);
        outStreamWriter.write(',');
        outStreamWriter.write(INTEREST_GROUP);
        outStreamWriter.write(',');
        outStreamWriter.write(INTEREST_GROUP_TITLE);
        outStreamWriter.write(',');
        outStreamWriter.write(TITLE);
        outStreamWriter.write(',');
        outStreamWriter.write("date");
        outStreamWriter.write(',');
        outStreamWriter.write(START_TIME);
        outStreamWriter.write(',');
        outStreamWriter.write(END_TIME);
        outStreamWriter.write(',');
        outStreamWriter.write("type");
        outStreamWriter.write(',');
        outStreamWriter.write(NODE_REFERENCE);
        outStreamWriter.write(',');
        outStreamWriter.write(ABSTRACT);
        outStreamWriter.write(',');
        outStreamWriter.write(AUDIENCE);
        outStreamWriter.write(',');
        outStreamWriter.write(INVITATION_MESSAGE);
        outStreamWriter.write(',');
        outStreamWriter.write(INVITED_USERS);
        outStreamWriter.write(',');
        outStreamWriter.write(LANGUAGE);
        outStreamWriter.write(',');
        outStreamWriter.write(LOCATION);
        outStreamWriter.write(',');
        outStreamWriter.write(OCCURRENCE_RATE);
        outStreamWriter.write(',');
        outStreamWriter.write(PHONE);
        outStreamWriter.write(',');
        outStreamWriter.write(PRIORITY);
        outStreamWriter.write(',');
        outStreamWriter.write("url");

        outStreamWriter.write('\n');

        for (EventItem appointment : eventItems) {
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

            Map<QName, Serializable> additionalNodeProperties =
                    this.getNodeProperties(appointment.getEventNodeRef());
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_ABSTRACT)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_AUDIENCE)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_INVITED_USERS)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_LANGUAGE)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_LOCATION)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            apiToolBox.getOccurenceAsString(
                                    (String) additionalNodeProperties.get(EventModel.PROP_EVENT_OCCURENCE_RATE))));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_PHONE)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_PRIORITY)));
            outStreamWriter.write(',');
            outStreamWriter.write(
                    WebClientHelper.emptyStringIfNull(
                            additionalNodeProperties.get(EventModel.PROP_EVENT_URL)));

            outStreamWriter.write('\n');
        }

        outStreamWriter.flush();
        outStreamWriter.close();
    }

    private Map<QName, Serializable> getNodeProperties(NodeRef nodeRef) {

        List<ChildAssociationRef> parentAssocs = this.nodeService.getParentAssocs(nodeRef);
        NodeRef container = parentAssocs.get(0).getParentRef();
        List<ChildAssociationRef> parentContainerAssocs = this.nodeService.getParentAssocs(container);
        NodeRef eventDefinition = parentContainerAssocs.get(0).getParentRef();

        return this.nodeService.getProperties(eventDefinition);
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
