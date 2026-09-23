package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.UserProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * REST endpoint that exports the member list of an Interest Group as a downloadable file.
 *
 * <p>The class name follows the CIRCABC webscript naming convention and maps to a
 * {@code GET} request on {@code /groups/{igId}/members/export}. It streams the members of the
 * Interest Group identified by the {@code igId} URL template variable back to the caller as a
 * file attachment (via the {@code Content-Disposition} header) in one of three supported
 * formats: CSV, XML or XLS.</p>
 *
 * <p>Unlike most CIRCABC endpoints this webscript extends {@link AbstractWebScript} directly
 * (rather than a declarative webscript) because it writes binary/tabular content straight to the
 * response output stream instead of rendering a FreeMarker template.</p>
 *
 * <p>Key request inputs:</p>
 * <ul>
 *   <li>{@code igId} (URL template variable) &ndash; identifier of the Interest Group whose
 *       members are exported.</li>
 *   <li>{@code format} (query parameter) &ndash; required output format: {@code csv},
 *       {@code xml} or {@code xls}.</li>
 *   <li>{@code language} (query parameter) &ndash; locale used to resolve multilingual values;
 *       when omitted the raw multilingual values are returned.</li>
 *   <li>{@code profile}, {@code limit}, {@code page}, {@code order}, {@code firstName},
 *       {@code lastName}, {@code email} (query parameters) &ndash; optional filtering, ordering
 *       and paging criteria forwarded to the underlying member lookup.</li>
 * </ul>
 *
 * <p>Access is restricted to callers holding the
 * {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the target group.</p>
 */
public class GroupsIdMembersExport extends AbstractWebScript {

  /** User property key holding the member's postal address. */
  private static final String POSTAL_ADDRESS = "postalAddress";
  /** Request parameter / output column name for the member profile. */
  private static final String PROFILE = "profile";
  /** Request parameter / output column name for the member email address. */
  private static final String EMAIL = "email";
  /** User property key / output column name for the member title. */
  public static final String TITLE = "title";
  /** User property key / output column name for the member organisation. */
  public static final String ORGANISATION = "organisation";
  /** Logger for this endpoint. */
  static final Log logger = LogFactory.getLog(GroupsIdMembersExport.class);

  /** API used to retrieve the paged list of Interest Group members. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current user may manage the group's members. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the export request: validates permissions and the requested format, retrieves the
   * (optionally filtered/paged) members of the Interest Group and writes them to the response as a
   * downloadable file in the requested format.
   *
   * <p>The multilingual awareness of {@link MLPropertyInterceptor} is toggled according to the
   * requested {@code language} and always restored to its original value before returning.</p>
   *
   * @param req the web script request; supplies the {@code igId} template variable together with
   *     the {@code format}, {@code language} and member-filtering query parameters
   * @param res the web script response the exported file is streamed to
   * @throws IOException if the members cannot be exported (e.g. permission denied, invalid format
   *     or a failure while writing the response)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
    setupLocale(language);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      validatePermission(id);
      setupLocale(language);

      String format = validateFormat(req.getParameter("format"));
      PagedUserProfile result = groupsApi.groupsIdMembersGet(
        id,
        getSearchProfile(req),
        language,
        parseIntOrNull(req.getParameter("limit"), 25),
        parseIntOrNull(req.getParameter("page"), 1),
        req.getParameter("order"),
        req.getParameter("firstName"),
        req.getParameter("lastName"),
        req.getParameter(EMAIL)
      );
      export(result.getData(), format.toLowerCase(), res);
    } catch (Exception e) {
      logger.error("Could not export members.", e);
      throw new IOException("Could not export members.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /**
   * Configures the content and UI locale used to resolve multilingual values for the export.
   *
   * @param language the requested language code, or {@code null} to keep multilingual awareness
   *     (defaults the locale to English)
   */
  private void setupLocale(String language) {
    Locale locale = Locale.of(language != null ? language : "en");
    I18NUtil.setContentLocale(locale);
    I18NUtil.setLocale(locale);
    MLPropertyInterceptor.setMLAware(language == null);
  }

  /**
   * Ensures the current user is allowed to export the group's member list.
   *
   * @param id the Interest Group identifier
   * @throws AccessDeniedException if the current user lacks the
   *     {@link DirectoryPermissions#DIRMANAGEMEMBERS} permission on the group
   */
  private void validatePermission(String id) throws AccessDeniedException {
    if (
      !currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
        id,
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ) {
      throw new AccessDeniedException(
        "Impossible to export list of users, not enough permissions"
      );
    }
  }

  /**
   * Builds the profile filter passed to the member lookup from the {@code profile} request
   * parameter.
   *
   * @param req the web script request
   * @return a single-element list containing the requested profile, or an empty list when no
   *     {@code profile} parameter is provided
   */
  private List<String> getSearchProfile(WebScriptRequest req) {
    String profile = req.getParameter(PROFILE);
    if (profile == null) return Collections.emptyList();
    List<String> list = new ArrayList<>();
    list.add(profile);
    return list;
  }

  /**
   * Parses an integer request parameter, distinguishing an absent value from an empty one.
   *
   * @param value the raw parameter value
   * @param defaultValue the value returned when the parameter is present but empty
   * @return {@code null} if {@code value} is {@code null}, {@code defaultValue} if it is empty,
   *     otherwise the parsed integer
   * @throws NumberFormatException if {@code value} is non-empty and not a valid integer
   */
  private Integer parseIntOrNull(String value, int defaultValue) {
    if (value == null) return null;
    return value.isEmpty() ? defaultValue : Integer.parseInt(value);
  }

  /**
   * Validates the requested export format.
   *
   * @param format the requested format (case-insensitive)
   * @return the validated format, unchanged
   * @throws IllegalArgumentException if the format is {@code null} or not one of {@code csv},
   *     {@code xml} or {@code xls}
   */
  private String validateFormat(String format) {
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
    return format;
  }

  /**
   * Streams the given members to the response as a file attachment, setting the appropriate
   * {@code Content-Disposition} and content type for the chosen format.
   *
   * @param userList the members to export
   * @param format the export format, in lower case ({@code csv}, {@code xml} or {@code xls})
   * @param response the web script response to write to
   * @throws IOException if writing to the response fails
   * @throws XMLStreamException if XML serialization fails (XML format only)
   */
  private void export(
    List<UserProfile> userList,
    String format,
    WebScriptResponse response
  ) throws IOException, XMLStreamException {
    try (OutputStream outStream = response.getOutputStream()) {
      response.setHeader(
        "Content-Disposition",
        "attachment;filename=MemberList." + format
      );
      switch (format) {
        case "csv":
          response.setContentType("text/csv;charset=UTF-8");
          writeCSV(userList, outStream);
          break;
        case "xml":
          response.setContentType("text/xml;charset=UTF-8");
          writeXML(userList, outStream);
          break;
        case "xls":
          response.setContentType("application/vnd.ms-excel;charset=UTF-8");
          writeXLS(userList, outStream);
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + format);
      }
    }
  }

  /**
   * Writes the members as a UTF-8 CSV document with a fixed header row.
   *
   * @param userProfiles the members to write
   * @param outStream the stream to write the CSV content to
   * @throws IOException if writing fails
   */
  private static void writeCSV(
    List<UserProfile> userProfiles,
    OutputStream outStream
  ) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(
      outStream,
      StandardCharsets.UTF_8
    );
    writer.write(
      "Username,Title,First Name,Last Name,Email,Profile,Profile Title,Organisation,Postal Address\n"
    );
    for (UserProfile profile : userProfiles) {
      writeCsvRow(writer, profile);
    }
    writer.flush();
    writer.close();
  }

  /**
   * Writes a single member as a quoted, comma-separated CSV row.
   *
   * @param writer the writer to append the row to
   * @param profile the member to serialize
   * @throws IOException if writing fails
   */
  private static void writeCsvRow(
    OutputStreamWriter writer,
    UserProfile profile
  ) throws IOException {
    Map<String, String> props = profile.getUser().getProperties();
    writer.write(
      String.format(
        "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
        profile.getUser().getUserId(),
        getOrEmpty(props, TITLE),
        profile.getUser().getFirstname(),
        profile.getUser().getLastname(),
        profile.getUser().getEmail(),
        profile.getProfile().getName(),
        getProfileTitle(profile),
        getOrEmpty(props, ORGANISATION),
        getOrEmpty(props, POSTAL_ADDRESS)
      )
    );
    writer.write('\n');
  }

  /**
   * Writes the members as a UTF-8 XML document with a {@code <members>} root element.
   *
   * @param userProfiles the members to write
   * @param outStream the stream to write the XML content to
   * @throws XMLStreamException if XML serialization fails
   */
  private static void writeXML(
    List<UserProfile> userProfiles,
    OutputStream outStream
  ) throws XMLStreamException {
    XMLStreamWriter xtw = XMLOutputFactory.newInstance().createXMLStreamWriter(
      outStream,
      "UTF-8"
    );
    xtw.writeStartDocument("utf-8", "1.0");
    xtw.writeCharacters("\n");
    xtw.writeStartElement("members");
    for (UserProfile profile : userProfiles) {
      writeXmlMember(xtw, profile);
    }
    xtw.writeCharacters("\n");
    xtw.writeEndElement();
    xtw.writeEndDocument();
    xtw.flush();
    xtw.close();
  }

  /**
   * Writes a single member as a {@code <member>} element whose attributes hold the member's
   * details.
   *
   * @param xtw the XML stream writer to append to
   * @param profile the member to serialize
   * @throws XMLStreamException if XML serialization fails
   */
  private static void writeXmlMember(XMLStreamWriter xtw, UserProfile profile)
    throws XMLStreamException {
    Map<String, String> props = profile.getUser().getProperties();
    xtw.writeCharacters("\n  ");
    xtw.writeStartElement("member");
    xtw.writeAttribute("username", profile.getUser().getUserId());
    xtw.writeAttribute(TITLE, getOrEmpty(props, TITLE));
    xtw.writeAttribute("firstname", profile.getUser().getFirstname());
    xtw.writeAttribute("lastname", profile.getUser().getLastname());
    xtw.writeAttribute(EMAIL, profile.getUser().getEmail());
    xtw.writeAttribute(PROFILE, profile.getProfile().getName());
    xtw.writeAttribute("profile-title", getProfileTitle(profile));
    xtw.writeAttribute(ORGANISATION, getOrEmpty(props, ORGANISATION));
    xtw.writeAttribute("postal-address", getOrEmpty(props, POSTAL_ADDRESS));
    xtw.writeEndElement();
  }

  /**
   * Writes the members as an HSSF (legacy {@code .xls}) workbook with a single "Members" sheet.
   *
   * @param userProfiles the members to write
   * @param outStream the stream to write the workbook to
   * @throws IOException if writing fails
   */
  private static void writeXLS(
    List<UserProfile> userProfiles,
    OutputStream outStream
  ) throws IOException {
    try (Workbook workbook = new HSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Members");
      createHeaderRow(sheet);
      int idx = 1;
      for (UserProfile profile : userProfiles) {
        writeXlsRow(sheet.createRow(idx++), profile);
      }
      workbook.write(outStream);
    }
  }

  /**
   * Creates the header row of the XLS sheet.
   *
   * @param sheet the sheet to add the header row to
   */
  private static void createHeaderRow(Sheet sheet) {
    Row row = sheet.createRow(0);
    String[] headers = {
      "username",
      TITLE,
      "first name",
      "last name",
      EMAIL,
      PROFILE,
      "profile title",
      ORGANISATION,
      "postal address",
    };
    for (int i = 0; i < headers.length; i++) {
      row.createCell(i).setCellValue(headers[i]);
    }
  }

  /**
   * Populates a single XLS data row with a member's details.
   *
   * @param row the row to populate
   * @param profile the member to serialize
   */
  private static void writeXlsRow(Row row, UserProfile profile) {
    Map<String, String> props = profile.getUser().getProperties();
    row.createCell(0).setCellValue(profile.getUser().getUserId());
    row.createCell(1).setCellValue(getOrEmpty(props, TITLE));
    row.createCell(2).setCellValue(profile.getUser().getFirstname());
    row.createCell(3).setCellValue(profile.getUser().getLastname());
    row.createCell(4).setCellValue(profile.getUser().getEmail());
    row.createCell(5).setCellValue(profile.getProfile().getName());
    row.createCell(6).setCellValue(getProfileTitle(profile));
    row.createCell(7).setCellValue(getOrEmpty(props, ORGANISATION));
    row.createCell(8).setCellValue(getOrEmpty(props, "postaAddress"));
  }

  /**
   * Returns the value for the given key, or an empty string when the value is {@code null}.
   *
   * @param props the property map
   * @param key the property key
   * @return the property value, or an empty string if absent
   */
  private static String getOrEmpty(Map<String, String> props, String key) {
    return props.get(key) == null ? "" : props.get(key);
  }

  /**
   * Resolves the display title of a member's profile, falling back to the profile name.
   *
   * @param profile the member whose profile title is resolved
   * @return the English profile title if available, otherwise the profile name
   */
  private static String getProfileTitle(UserProfile profile) {
    String title = profile.getProfile().getTitle().get("en");
    return title != null ? title : profile.getProfile().getName();
  }
}
