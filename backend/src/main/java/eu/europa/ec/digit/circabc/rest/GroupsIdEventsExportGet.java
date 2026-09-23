package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.EventsApi;
import io.swagger.model.EventItem;
import io.swagger.model.PagedEventItems;
import io.swagger.model.alfresco.EventModel;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * REST webscript endpoint that exports the events (appointments) of an Interest Group (IG) as a
 * downloadable file in CSV, XLS or XML format.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, implying an HTTP {@code GET}
 * request. The endpoint streams the export directly to the HTTP response as a file attachment
 * (rather than rendering a FreeMarker template), which is why it extends {@link AbstractWebScript}
 * instead of the declarative base class.
 *
 * <p>Expected inputs:
 *
 * <ul>
 *   <li>{@code igId} &mdash; URL template variable identifying the Interest Group whose events are
 *       exported.
 *   <li>{@code filter} &mdash; request parameter selecting which events to export; must be one of
 *       {@code Exact}, {@code Future} or {@code Previous}.
 *   <li>{@code exactDate} &mdash; optional request parameter (format {@code yyyy-MM-dd}) used when
 *       filtering by an exact date.
 *   <li>{@code format} &mdash; request parameter choosing the output format; must be {@code csv},
 *       {@code xls} or {@code xml} (case-insensitive).
 * </ul>
 *
 * <p>Access requires the {@link EventPermissions#EVEACCESS} permission on the group's {@code Events}
 * container; otherwise the response is set to HTTP 403 (Forbidden).
 *
 * @author schwerr
 */
public class GroupsIdEventsExportGet extends AbstractWebScript {

  /**
   * Column headers written as the first row/attribute set of every export, defining both the order
   * and the labels of the exported event fields. Must stay aligned with the values produced by
   * {@link #toRow(EventItem)}.
   */
  private static final String[] COLUMN_HEADERS = {
    "contact",
    "interest group",
    "interest group title",
    "title",
    "date",
    "start time",
    "end time",
    "type",
    "node reference",
    "abstract",
    "audience",
    "invitation message",
    "invited users",
    "language",
    "location",
    "occurrence rate",
    "phone",
    "priority",
    "url",
  };

  static final Log logger = LogFactory.getLog(GroupsIdEventsExportGet.class);

  @Autowired
  private EventsApi eventsApi;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Handles the export request: validates inputs and permissions, retrieves the matching events and
   * streams them to the response in the requested format.
   *
   * <p>Multilingual (ML) property interception is temporarily disabled while events are fetched so
   * that raw language-specific property values are exported, and is always restored afterwards.
   *
   * @param req the web script request; supplies the {@code igId} template variable and the
   *     {@code filter}, {@code exactDate} and {@code format} parameters
   * @param res the web script response; the exported file is written to its output stream and, on a
   *     permission failure, its status is set to HTTP 403 (Forbidden)
   * @throws IOException if the export cannot be produced or written to the response
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    String filter = req.getParameter("filter");
    String exactDateStr = req.getParameter("exactDate");

    try {
      NodeRef groupRef = Converter.createNodeRefFromId(id);
      NodeRef evtNodeRef = this.nodeService.getChildByName(
        groupRef,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      );

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfEventPermission(
          evtNodeRef.getId(),
          EventPermissions.EVEACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot export events because user does not have enough permissions"
        );
      }

      if (
        !("Exact".equals(filter) ||
          "Future".equals(filter) ||
          "Previous".equals(filter))
      ) {
        throw new IllegalArgumentException(
          "Invalid filter value. Must be 'Exact', 'Future' or 'Previous'"
        );
      }

      String format = req.getParameter("format");
      if (
        format == null ||
        !("csv".equalsIgnoreCase(format) ||
          "xls".equalsIgnoreCase(format) ||
          "xml".equalsIgnoreCase(format))
      ) {
        throw new IllegalArgumentException(
          "Export 'format' must be CSV, XML or XLS"
        );
      }

      Date exactDate = null;
      if (exactDateStr != null && !exactDateStr.isEmpty()) {
        exactDate = parseExactDate(
          exactDateStr,
          new SimpleDateFormat("yyyy-MM-dd")
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      PagedEventItems pagedEventItems = this.eventsApi.groupsIdEventsListGet(
        id,
        filter,
        exactDate,
        1,
        0,
        null
      );

      this.export(pagedEventItems.getData(), format.toLowerCase(), res);
    } catch (AccessDeniedException ade) {
      res.setStatus(Status.STATUS_FORBIDDEN);
    } catch (Exception e) {
      logger.error("Could not export events.", e);
      throw new IOException("Could not export events.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /**
   * Parses the {@code exactDate} request parameter into a {@link Date}.
   *
   * @param exactDateStr the raw date string, expected in {@code yyyy-MM-dd} format
   * @param simpleDateFormat the formatter used to parse the value
   * @return the parsed date
   * @throws IllegalArgumentException if the string does not match the expected format
   */
  private Date parseExactDate(
    String exactDateStr,
    SimpleDateFormat simpleDateFormat
  ) {
    try {
      return simpleDateFormat.parse(exactDateStr);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "The 'exactDate' has a wrong format. Must be yyyy-MM-dd",
        e
      );
    }
  }

  /**
   * Converts an {@link EventItem} and its underlying node properties into a flat {@code String}
   * array whose order matches {@link #COLUMN_HEADERS}.
   *
   * @param appointment the event to convert
   * @return an array of exported field values, aligned with {@link #COLUMN_HEADERS}
   */
  private String[] toRow(EventItem appointment) {
    Map<QName, Serializable> props = getNodeProperties(
      appointment.getEventNodeRef()
    );
    return new String[] {
      appointment.getContact(),
      appointment.getInterestGroup(),
      appointment.getInterestGroupTitle(),
      appointment.getTitle(),
      appointment.getDate().toString(),
      appointment.getStartTime().toString(),
      appointment.getEndTime().toString(),
      appointment.getEventType().name(),
      appointment.getEventNodeRef().toString(),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_ABSTRACT)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_AUDIENCE)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_INVITATION_MESSAGE)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_INVITED_USERS)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_LANGUAGE)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_LOCATION)),
      emptyStringIfNull(
        apiToolBox.getOccurenceAsString(
          (String) props.get(EventModel.PROP_EVENT_OCCURENCE_RATE)
        )
      ),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_PHONE)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_PRIORITY)),
      emptyStringIfNull(props.get(EventModel.PROP_EVENT_URL)),
    };
  }

  /**
   * Writes the given events to the response output stream in the requested format, setting the
   * appropriate content type and a {@code Content-Disposition} attachment header named
   * {@code Events.<format>}.
   *
   * @param eventItems the events to export
   * @param format the (lower-case) output format; one of {@code csv}, {@code xml} or {@code xls}
   * @param response the web script response to write to
   * @throws IOException if writing to the output stream fails
   * @throws XMLStreamException if XML serialization fails (for the {@code xml} format)
   */
  public void export(
    List<EventItem> eventItems,
    String format,
    WebScriptResponse response
  ) throws IOException, XMLStreamException {
    try (OutputStream outStream = response.getOutputStream()) {
      response.setHeader(
        "Content-Disposition",
        "attachment;filename=Events." + format
      );
      switch (format) {
        case "csv":
          response.setContentType("text/csv;charset=UTF-8");
          writeCSV(eventItems, outStream);
          break;
        case "xml":
          response.setContentType("text/xml;charset=UTF-8");
          writeXML(eventItems, outStream);
          break;
        case "xls":
          response.setContentType("application/vnd.ms-excel;charset=UTF-8");
          writeXLS(eventItems, outStream);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + format);
      }
    }
  }

  /**
   * Serializes the events as an XML document with one {@code <appointment>} element per event, each
   * exported field written as an attribute named after its {@link #COLUMN_HEADERS} entry.
   *
   * @param eventItems the events to export
   * @param outStream the stream to write the XML to
   * @throws XMLStreamException if XML serialization fails
   */
  private void writeXML(List<EventItem> eventItems, OutputStream outStream)
    throws XMLStreamException {
    XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(
      outStream,
      "UTF-8"
    );
    xtw.writeStartDocument("utf-8", "1.0");
    xtw.writeCharacters("\n");
    xtw.writeStartElement("appointments");

    for (EventItem appointment : eventItems) {
      String[] values = toRow(appointment);
      xtw.writeCharacters("\n  ");
      xtw.writeStartElement("appointment");
      for (int i = 0; i < COLUMN_HEADERS.length; i++) {
        xtw.writeAttribute(COLUMN_HEADERS[i], values[i]);
      }
      xtw.writeEndElement();
    }

    xtw.writeCharacters("\n");
    xtw.writeEndElement();
    xtw.writeEndDocument();
    xtw.flush();
    xtw.close();
  }

  /**
   * Serializes the events into an XLS (HSSF) workbook with a header row followed by one row per
   * event.
   *
   * @param eventItems the events to export
   * @param outStream the stream to write the workbook to
   * @throws IOException if writing the workbook fails
   */
  private void writeXLS(List<EventItem> eventItems, OutputStream outStream)
    throws IOException {
    try (Workbook workbook = new HSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Appointments");

      Row titleRow = sheet.createRow(0);
      for (int i = 0; i < COLUMN_HEADERS.length; i++) {
        titleRow.createCell(i).setCellValue(COLUMN_HEADERS[i]);
      }

      int idx = 1;
      for (EventItem appointment : eventItems) {
        Row row = sheet.createRow(idx++);
        String[] values = toRow(appointment);
        for (int i = 0; i < values.length; i++) {
          row.createCell(i).setCellValue(values[i]);
        }
      }

      workbook.write(outStream);
    }
  }

  /**
   * Serializes the events as a UTF-8 CSV document, prefixed with a byte order mark and starting with
   * the {@link #COLUMN_HEADERS} row.
   *
   * @param eventItems the events to export
   * @param outStream the stream to write the CSV to
   * @throws IOException if writing to the stream fails
   */
  private void writeCSV(List<EventItem> eventItems, OutputStream outStream)
    throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(
      outStream,
      StandardCharsets.UTF_8
    );

    // write byte order mark
    outStream.write(0xEF);
    outStream.write(0xBB);
    outStream.write(0xBF);

    writer.write(String.join(",", COLUMN_HEADERS));
    writer.write('\n');

    for (EventItem appointment : eventItems) {
      String[] values = toRow(appointment);
      writer.write(String.join(",", values));
      writer.write('\n');
    }

    writer.flush();
    writer.close();
  }

  /**
   * Resolves the properties of the event definition node associated with the given event node by
   * walking two levels up the parent associations (event node &rarr; container &rarr; event
   * definition).
   *
   * @param nodeRef the event node whose owning event definition properties are required
   * @return the properties of the resolved event definition node
   */
  private Map<QName, Serializable> getNodeProperties(NodeRef nodeRef) {
    List<ChildAssociationRef> parentAssocs = this.nodeService.getParentAssocs(
      nodeRef
    );
    NodeRef container = parentAssocs.get(0).getParentRef();
    List<ChildAssociationRef> parentContainerAssocs =
      this.nodeService.getParentAssocs(container);
    NodeRef eventDefinition = parentContainerAssocs.get(0).getParentRef();
    return this.nodeService.getProperties(eventDefinition);
  }

  /**
   * Returns an empty string when the given value is {@code null}, otherwise its {@code String} value.
   *
   * @param serializable the value to normalize
   * @return the value as a {@code String}, or an empty string if {@code null}
   */
  private String emptyStringIfNull(Serializable serializable) {
    return serializable == null ? "" : (String) serializable;
  }
}
