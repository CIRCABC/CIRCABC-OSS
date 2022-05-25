package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import io.swagger.api.GroupsApi;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.UserProfile;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Webscript entry to export the members of an IG in different formats: CSV, XLS, or XML
 *
 * @author schwerr
 */
public class GroupsIdMembersExport extends AbstractWebScript {

    public static final String TITLE = "title";
    public static final String ORGANISATION = "organisation";
    static final Log logger = LogFactory.getLog(GroupsIdMembersExport.class);
    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static void writeCSV(List<UserProfile> userProfiles, OutputStream outStream)
            throws IOException {

        OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);

        outStreamWriter.write("Username");
        outStreamWriter.write(',');
        outStreamWriter.write("Title");
        outStreamWriter.write(',');
        outStreamWriter.write("First Name");
        outStreamWriter.write(',');
        outStreamWriter.write("Last Name");
        outStreamWriter.write(',');
        outStreamWriter.write("Email");
        outStreamWriter.write(',');
        outStreamWriter.write("Profile");
        outStreamWriter.write(',');
        outStreamWriter.write("Profile Title");
        outStreamWriter.write(',');
        outStreamWriter.write("Organisation");
        outStreamWriter.write(',');
        outStreamWriter.write("Postal Address");
        outStreamWriter.write('\n');

        for (UserProfile profile : userProfiles) {

            Map<String, String> props = (Map<String, String>) profile.getUser().getProperties();
            
            outStreamWriter.write("\"");
            outStreamWriter.write(profile.getUser().getUserId());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(props.get(TITLE));
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(profile.getUser().getFirstname());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(profile.getUser().getLastname());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(profile.getUser().getEmail());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(profile.getProfile().getName());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(
                    profile.getProfile().getTitle().get("en") != null
                            ? profile.getProfile().getTitle().get("en")
                            : profile.getProfile().getName());
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(props.get(ORGANISATION));
            outStreamWriter.write("\"");
            outStreamWriter.write(',');

            outStreamWriter.write("\"");
            outStreamWriter.write(props.get("postalAddress"));
            outStreamWriter.write("\"");
            outStreamWriter.write('\n');
        }

        outStreamWriter.flush();
        outStreamWriter.close();
    }

    private static void writeXML(List<UserProfile> userProfiles, OutputStream outStream)
            throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("members");

        for (UserProfile profile : userProfiles) {
            Map<String, String> props = (Map<String, String>) profile.getUser().getProperties();
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("member");
            xtw.writeAttribute("username", profile.getUser().getUserId());
            xtw.writeAttribute(TITLE, props.get(TITLE));
            xtw.writeAttribute("firstname", profile.getUser().getFirstname());
            xtw.writeAttribute("lastname", profile.getUser().getLastname());
            xtw.writeAttribute("email", profile.getUser().getEmail());
            xtw.writeAttribute("profile", profile.getProfile().getName());
            xtw.writeAttribute(
                    "profile-title",
                    profile.getProfile().getTitle().get("en") != null
                            ? profile.getProfile().getTitle().get("en")
                            : profile.getProfile().getName());
            xtw.writeAttribute(ORGANISATION, props.get(ORGANISATION));
            xtw.writeAttribute("postal-address", props.get("postalAddress"));
            xtw.writeEndElement();
        }

        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }

    private static void writeXLS(List<UserProfile> userProfiles, OutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Members");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("username");
        titleRow.createCell(1).setCellValue(TITLE);
        titleRow.createCell(2).setCellValue("first name");
        titleRow.createCell(3).setCellValue("last name");
        titleRow.createCell(4).setCellValue("email");
        titleRow.createCell(5).setCellValue("profile");
        titleRow.createCell(6).setCellValue("profile title");
        titleRow.createCell(7).setCellValue(ORGANISATION);
        titleRow.createCell(8).setCellValue("postal address");

        int idx = 1;

        for (UserProfile profile : userProfiles) {
            Map<String, String> props = (Map<String, String>) profile.getUser().getProperties();
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(profile.getUser().getUserId());
            row.createCell(1).setCellValue(props.get(TITLE));
            row.createCell(2).setCellValue(profile.getUser().getFirstname());
            row.createCell(3).setCellValue(profile.getUser().getLastname());
            row.createCell(4).setCellValue(profile.getUser().getEmail());
            row.createCell(5).setCellValue(profile.getProfile().getName());
            row.createCell(6)
                    .setCellValue(
                            profile.getProfile().getTitle().get("en") != null
                                    ? profile.getProfile().getTitle().get("en")
                                    : profile.getProfile().getName());
            row.createCell(7).setCellValue(props.get(ORGANISATION));
            row.createCell(8).setCellValue(props.get("postaAddress"));
            idx++;
        }

        workbook.write(outStream);
    }

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

        String language = req.getParameter("language");

        try {

            if (!this.currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
                    id, DirectoryPermissions.DIRMANAGEMEMBERS)) {
                throw new AccessDeniedException(
                        "Impossible to export list of users, not enough permissions");
            }

            if (language == null) {
                MLPropertyInterceptor.setMLAware(true);
            } else {
                Locale locale = new Locale(language);
                I18NUtil.setContentLocale(locale);
                I18NUtil.setLocale(locale);
                MLPropertyInterceptor.setMLAware(false);
            }

            String format = req.getParameter("format");

            if ((format == null)
                    || !("csv".equalsIgnoreCase(format)
                    || "xls".equalsIgnoreCase(format)
                    || "xml".equalsIgnoreCase(format))) {
                throw new IllegalArgumentException("Export 'format' must be CSV, XML or XLS");
            }

            PagedUserProfile result =
                    this.groupsApi.groupsIdMembersGet(id, null, language, null, null, "lastName_ASC", null);

            List<UserProfile> profiles = result.getData();

            this.export(profiles, format.toLowerCase(), res);

        } catch (Exception e) {
            logger.error("Could not export members.", e);
            throw new IOException("Could not export members.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public void export(List<UserProfile> userList, String format, WebScriptResponse response)
            throws IOException, XMLStreamException {

        try (OutputStream outStream = response.getOutputStream()) {
            response.setHeader("Content-Disposition", "attachment;filename=MemberList." + format);
            switch (format) {
                case "csv":
                    response.setContentType("text/csv;charset=UTF-8");
                    GroupsIdMembersExport.writeCSV(userList, outStream);
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

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
