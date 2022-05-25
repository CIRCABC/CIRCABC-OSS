/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.repo.statistics.ig.Child;
import eu.cec.digit.circabc.repo.statistics.ig.ServiceTreeRepresentation;
import eu.cec.digit.circabc.repo.statistics.ig.StatData;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class IgSummaryDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = 7019246914863763412L;

    private static final Log logger = LogFactory.getLog(IgSummaryDialog.class);
    private static final Integer GLOBAL_STATISTICS = 1;
    private static final Integer TIMELINE_STATISTICS = 2;
    private static final Integer INTEREST_GROUP_STRUCTURE_STATISTICS = 3;
    private static final Integer EXPORT_CSV = 1;
    private static final Integer EXPORT_XLS = 2;
    private static final Integer EXPORT_XML = 3;
    private List<SelectItem> availableStatisticChoices;
    private List<SelectItem> availableExportOptions;
    private Integer selectedStatisticChoices;
    private Integer selectedStatisticExport;
    private IgStatisticsService statsServ;

    private NodeRef currentNode;

    private List<StatData> igData;
    private HtmlDataTable dataTable;
    private HtmlDataTable dataTable2;
    private String HtmlServiceTree;
    private List<ActivityCountDAO> igTimeLineActivity;

    private Boolean altRow = false;

    /**
     * @return the globalStatistics
     */
    public static Integer getGlobalStatistics() {
        return GLOBAL_STATISTICS;
    }

    /**
     * @return the timelineStatistics
     */
    public static Integer getTimelineStatistics() {
        return TIMELINE_STATISTICS;
    }

    /**
     * @return the interestGroupStructureStatistics
     */
    public static Integer getInterestGroupStructureStatistics() {
        return INTEREST_GROUP_STRUCTURE_STATISTICS;
    }

    /**
     * @return the exportCsv
     */
    public static Integer getExportCsv() {
        return EXPORT_CSV;
    }

    /**
     * @return the exportXml
     */
    public static Integer getExportXml() {
        return EXPORT_XML;
    }

    /**
     * @return the exportXls
     */
    public static Integer getExportXls() {
        return EXPORT_XLS;
    }

    /***
     * Initialize list of selectables values for available statistics and
     * available export options
     */
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        // available statistics

        this.availableStatisticChoices = new ArrayList<>();

        SelectItem optionGBS = new SelectItem(GLOBAL_STATISTICS,
                translate("ig_summary_statistic_to_display_global"));
        SelectItem optionTS = new SelectItem(TIMELINE_STATISTICS,
                translate("ig_summary_statistic_to_display_timeline"));
        SelectItem optionIGS = new SelectItem(
                INTEREST_GROUP_STRUCTURE_STATISTICS,
                translate("ig_summary_statistic_to_display_structure"));

        this.availableStatisticChoices.add(optionGBS);
        this.availableStatisticChoices.add(optionTS);
        this.availableStatisticChoices.add(optionIGS);

        this.selectedStatisticChoices = GLOBAL_STATISTICS;

        // available export options

        this.availableExportOptions = new ArrayList<>();

        SelectItem optionCSV = new SelectItem(EXPORT_CSV, "CSV");
        SelectItem optionXLS = new SelectItem(EXPORT_XLS, "Excel");
        SelectItem optionXML = new SelectItem(EXPORT_XML, "XML");

        this.availableExportOptions.add(optionCSV);
        this.availableExportOptions.add(optionXLS);
        this.availableExportOptions.add(optionXML);

        this.selectedStatisticExport = EXPORT_CSV;

        currentNode = getActionNode().getNodeRef();

        this.buildIGData();

    }

    /***
     * compute global data of IG
     */
    private void buildIGData() {

        this.igData = new ArrayList<>();

        this.statsServ.buildStatsData(currentNode);

        this.igData.add(new StatData(translate("ig_summary_statistic_created_date"),
                this.statsServ.getIGCreationDate(currentNode)));
        this.igData.add(new StatData(translate("ig_summary_statistic_number_of_users"),
                this.statsServ.getNumberOfUsers(currentNode)));
        this.igData.add(new StatData(translate("ig_summary_statistic_library_folder_count"),
                this.statsServ.getNumberOfLibrarySpaces()));
        this.igData.add(new StatData(translate("ig_summary_statistic_library_document_count"),
                this.statsServ.getNumberOfLibraryDocuments()));
        Long libSize = this.statsServ.getContentSizeOfLibrary();
        this.igData.add(new StatData(translate("ig_summary_statistic_library_size"),
                CircabcUploadedFile.humanReadableByteCount(libSize, false)));
        this.igData.add(new StatData(translate("ig_summary_statistic_information_folder_count"),
                this.statsServ.getNumberOfInformationSpaces()));
        this.igData.add(new StatData(translate("ig_summary_statistic_information_document_count"),
                this.statsServ.getNumberOfInformationDocuments()));
        Long infSize = this.statsServ.getContentSizeOfInformation();
        this.igData.add(new StatData(translate("ig_summary_statistic_information_size"),
                CircabcUploadedFile.humanReadableByteCount(infSize, false)));
        this.igData.add(new StatData(translate("ig_summary_statistic_version_count"),
                this.statsServ.getNumberOfVersions() + this.statsServ
                        .getNumberOfCustomizationAndHiddenContent()));
        Long verSize = this.statsServ.getSizeOfVersions();
        Long custSize = this.statsServ.getSizeOfCustomizationAndHiddenContent();
        this.igData.add(new StatData(translate("ig_summary_statistic_version_size"),
                CircabcUploadedFile.humanReadableByteCount(verSize + custSize, false)));
        this.igData.add(new StatData(translate("ig_summary_statistic_total_size"), CircabcUploadedFile
                .humanReadableByteCount((libSize + infSize + verSize + custSize), false)));
        this.igData.add(new StatData(translate("ig_summary_statistic_event_count"),
                this.statsServ.getNumbetOfEvents()));
        this.igData.add(new StatData(translate("ig_summary_statistic_meeting_count"),
                this.statsServ.getNumbetOfMeetings()));
        this.igData.add(new StatData(translate("ig_summary_statistic_forum_count"),
                this.statsServ.getNumberOfForums()));
        this.igData.add(new StatData(translate("ig_summary_statistic_topic_count"),
                this.statsServ.getNumberOfTopics()));
        this.igData.add(new StatData(translate("ig_summary_statistic_post_count"),
                this.statsServ.getNumberOfPosts()));
    }

    /***
     * Compute folder architecture of IG
     */
    private void builIGStructure() {
        ServiceTreeRepresentation informationRepresent = this.statsServ
                .getInformationStructure(currentNode);
        ServiceTreeRepresentation libraryRepresent = this.statsServ.getLibraryStructure(currentNode);
        ServiceTreeRepresentation newgroupsRepresent = this.statsServ
                .getNewsgroupsStructure(currentNode);

        HtmlServiceTree = buildUlLiTreeFolder(informationRepresent.getChild(), true);
        HtmlServiceTree += buildUlLiTreeFolder(libraryRepresent.getChild(), true);
        HtmlServiceTree += buildUlLiTreeFolder(newgroupsRepresent.getChild(), true);

    }

    /***
     *
     * @param libraryRepresent
     * @return
     */
    private String buildUlLiTreeFolder(Child child, Boolean firstChild) {
        StringBuilder html = new StringBuilder();
        if (firstChild) {
            html.append("<span class=\"recordSetHeader\">").append(child.getName()).append("</span>");
            html.append("<br/>");
        } else {
            html.append(child.getName());
        }

        if (child.getChildren().size() > 0) {
            html.append("\n");
            html.append("<ul>");
            for (Child c : child.getChildren()) {
                html.append("\n");
                if (altRow) {
                    html.append("<li class=\"recordSetRowAlt\">");
                    altRow = false;
                } else {
                    html.append("<li class=\"recordSetRow\">");
                    altRow = true;
                }

                html.append(buildUlLiTreeFolder(c, false));
                html.append("\n");
                html.append("</li>");
            }
            html.append("\n");
            html.append("</ul>");
        }

        return html.toString();

    }

    /***
     * Compute activity report of IG
     */
    private void builIGtimeline() {
        this.igTimeLineActivity = this.getStatsServ().getListOfActivityCount(currentNode);
    }

    /***
     * refresh displayed table according to the selected statistic choice
     */
    public String refresh(ActionEvent event) {

        if (getIgStatisticsChoosen()) {
            buildIGData();
        } else if (getIgTimelineChoosen()) {
            builIGtimeline();
        } else if (getIgStructureChoosen()) {
            builIGStructure();
        }

        return null;
    }

    public String getPageIconAltText() {
        return translate("ig_summary_dialog_alt_text");
    }

    public String getBrowserTitle() {
        return translate("ig_summary_dialog_title");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) {

        return outcome;
    }

    /**
     * @return the availableStatisticChoices
     */
    public List<SelectItem> getAvailableStatisticChoices() {
        return availableStatisticChoices;
    }

    /**
     * @param availableStatisticChoices
     *            the availableStatisticChoices to set
     */
    public void setAvailableStatisticChoices(
            List<SelectItem> availableStatisticChoices) {
        this.availableStatisticChoices = availableStatisticChoices;
    }

    /**
     * @return the selectedStatisticChoices
     */
    public Integer getSelectedStatisticChoices() {

        if (this.selectedStatisticChoices == null) {
            selectedStatisticChoices = GLOBAL_STATISTICS;
        }
        return selectedStatisticChoices;
    }

    /**
     * @param selectedStatisticChoices
     *            the selectedStatisticChoices to set
     */
    public void setSelectedStatisticChoices(Integer selectedStatisticChoices) {
        this.selectedStatisticChoices = selectedStatisticChoices;
    }

    /**
     * @return the availableExportOptions
     */
    public List<SelectItem> getAvailableExportOptions() {
        return availableExportOptions;
    }

    /**
     * @param availableExportOptions
     *            the availableExportOptions to set
     */
    public void setAvailableExportOptions(
            List<SelectItem> availableExportOptions) {
        this.availableExportOptions = availableExportOptions;
    }

    /**
     * @return the selectedStatisticExport
     */
    public Integer getSelectedStatisticExport() {
        if (this.selectedStatisticExport == null) {
            selectedStatisticExport = EXPORT_CSV;
        }
        return selectedStatisticExport;
    }

    /**
     * @param selectedStatisticExport
     *            the selectedStatisticExport to set
     */
    public void setSelectedStatisticExport(Integer selectedStatisticExport) {
        this.selectedStatisticExport = selectedStatisticExport;
    }

    /**
     * @return the igStatsServ
     */
    public IgStatisticsService getStatsServ() {
        return statsServ;
    }

    /**
     * @param igStatsServ
     *            the igStatsServ to set
     */
    public void setStatsServ(IgStatisticsService statsServ) {
        this.statsServ = statsServ;
    }

    /**
     * @return the igData
     */
    public List<StatData> getIgData() {
        return igData;
    }

    /**
     * @param igData
     *            the igData to set
     */
    public void setIgData(List<StatData> igData) {
        this.igData = igData;
    }

    public Boolean getIgStatisticsChoosen() {
        Boolean ok = false;

        if (this.selectedStatisticChoices != null) {
            if (this.selectedStatisticChoices.equals(GLOBAL_STATISTICS)) {
                ok = true;
            }
        } else {
            ok = true;
        }

        return ok;
    }

    public boolean getIgStructureChoosen() {
        Boolean ok = false;

        if (this.selectedStatisticChoices != null) {
            if (this.selectedStatisticChoices.equals(INTEREST_GROUP_STRUCTURE_STATISTICS)) {
                ok = true;
            }
        }
        return ok;
    }

    public boolean getIgTimelineChoosen() {
        Boolean ok = false;
        if (this.selectedStatisticChoices != null) {
            if (this.selectedStatisticChoices.equals(TIMELINE_STATISTICS)) {
                ok = true;
            }
        }
        return ok;
    }

    public String export(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        ServletOutputStream outStream = null;

        try {
            outStream = response.getOutputStream();

            if (getSelectedStatisticExport().equals(EXPORT_CSV) && getSelectedStatisticChoices()
                    .equals(GLOBAL_STATISTICS)) {
                response.setHeader("Content-Disposition", "attachment;filename=Statistics.csv");
                response.setContentType("text/csv;charset=UTF-8");
                writeCSVData(getIgData(), outStream);
            } else if (getSelectedStatisticExport().equals(EXPORT_XML) && getSelectedStatisticChoices()
                    .equals(GLOBAL_STATISTICS)) {
                response.setHeader("Content-Disposition", "attachment;filename=Statistics.xml");
                response.setContentType("text/xml;charset=UTF-8");
                writeXMLData(getIgData(), outStream);
            } else if (getSelectedStatisticExport().equals(EXPORT_XLS) && getSelectedStatisticChoices()
                    .equals(GLOBAL_STATISTICS)) {
                response.setHeader("Content-Disposition", "attachment;filename=Statistics.xls");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                writeXLS(getIgData(), outStream);
            } else if (getSelectedStatisticExport().equals(EXPORT_CSV) && getSelectedStatisticChoices()
                    .equals(TIMELINE_STATISTICS)) {
                writeCSVTimeline(getIgTimeLineActivity(), outStream);
            } else if (getSelectedStatisticExport().equals(EXPORT_XML) && getSelectedStatisticChoices()
                    .equals(TIMELINE_STATISTICS)) {

                response.setHeader("Content-Disposition", "attachment;filename=TimelineActivity.xml");
                response.setContentType("text/xml;charset=UTF-8");
                writeXMLTimeline(getIgTimeLineActivity(), outStream);

            } else if (getSelectedStatisticExport().equals(EXPORT_XLS) && getSelectedStatisticChoices()
                    .equals(TIMELINE_STATISTICS)) {
                response.setHeader("Content-Disposition", "attachment;filename=TimelineActivity.xls");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                writeXLSTimeline(getIgTimeLineActivity(), outStream);
            }

            context.responseComplete();

        } catch (Exception ex) {
            logger.error("Error during export", ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

        return null;
    }

    private void writeXLS(List<StatData> statDatas, ServletOutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Statistics");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Dimension Name");
        titleRow.createCell(1).setCellValue("Dimension Value");

        int idx = 1;

        for (StatData dim : statDatas) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(dim.getDataName());
            row.createCell(1).setCellValue(dim.getDataValue().toString());
            idx++;
        }

        workbook.write(outStream);
    }

    private void writeXLSTimeline(List<ActivityCountDAO> igTimeLineActivities,
                                  ServletOutputStream outStream) throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Timeline Activity");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Date");
        titleRow.createCell(1).setCellValue("Service");
        titleRow.createCell(2).setCellValue("Activity");
        titleRow.createCell(3).setCellValue("Action Number");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy");

        int idx = 1;

        for (ActivityCountDAO dim : igTimeLineActivities) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(simpleDateFormat.format(dim.getMonthActivity()));
            row.createCell(1).setCellValue(dim.getService());
            row.createCell(2).setCellValue(dim.getActivity());
            row.createCell(3).setCellValue(dim.getActionNumber());
            idx++;
        }

        workbook.write(outStream);
    }

    private void writeXMLTimeline(List<ActivityCountDAO> igTimeLineActivity2,
                                  ServletOutputStream outStream) throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = null;
        try {
            xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
            xtw.writeStartDocument("utf-8", "1.0");
            xtw.writeCharacters("\n");
            xtw.writeStartElement("timeline");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy");

            for (ActivityCountDAO dim : igTimeLineActivity2) {
                xtw.writeCharacters("\n  ");
                xtw.writeStartElement("month");

                xtw.writeAttribute("date", simpleDateFormat.format(dim.getMonthActivity()));
                xtw.writeCharacters("\n  ");

                xtw.writeStartElement("activity");
                xtw.writeAttribute("service", dim.getService());
                xtw.writeAttribute("action", dim.getActivity());
                xtw.writeCharacters(dim.getActionNumber().toString());
                xtw.writeEndElement();

                xtw.writeCharacters("\n  ");
                xtw.writeEndElement();
            }
            xtw.writeCharacters("\n");
            xtw.writeEndElement();
            xtw.writeEndDocument();

        } catch (XMLStreamException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during xml construction of timeline", e);
            }
        }

    }

    private void writeCSVTimeline(List<ActivityCountDAO> igTimeLineActivity2,
                                  ServletOutputStream outStream) {

        OutputStreamWriter outStreamWriter = null;

        try {
            outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("Date");
            outStreamWriter.write(',');
            outStreamWriter.write("Service");
            outStreamWriter.write(',');
            outStreamWriter.write("Activity");
            outStreamWriter.write(',');
            outStreamWriter.write("Action Number");
            outStreamWriter.write('\n');

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yyyy");

            for (ActivityCountDAO dim : igTimeLineActivity2) {

                outStreamWriter.write(simpleDateFormat.format(dim.getMonthActivity()));
                outStreamWriter.write(',');
                outStreamWriter.write(dim.getService());

                outStreamWriter.write(',');
                outStreamWriter.write(dim.getActivity());

                outStreamWriter.write(',');
                outStreamWriter.write(dim.getActionNumber());

                outStreamWriter.write('\n');
            }


        } catch (IOException e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during csv construction of timeline", e);
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

    private void writeXMLData(List<StatData> IGdata, ServletOutputStream outStream)
            throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = null;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("statistics");

        for (StatData dim : IGdata) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("dimension");
            xtw.writeAttribute("name", dim.getDataName());
            xtw.writeAttribute("value", dim.getDataValue().toString());
            xtw.writeEndElement();
        }
        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();

    }

    /**
     * @return the dtable
     */
    public HtmlDataTable getDataTable() {
        return dataTable;
    }

    /**
     * @param dtable
     *            the dtable to set
     */
    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }

    private void writeCSVData(List<StatData> IGdata, ServletOutputStream outStream)
            throws IOException {
        OutputStreamWriter outStreamWriter = null;
        try {
            outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("Dimension name");
            outStreamWriter.write(',');
            outStreamWriter.write("Dimension value");
            outStreamWriter.write('\n');

            for (StatData dim : IGdata) {
                outStreamWriter.write(dim.getDataName());
                outStreamWriter.write(',');
                outStreamWriter.write(dim.getDataValue().toString());
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

    /**
     * @return the htmlServiceTree
     */
    public String getHtmlServiceTree() {
        return HtmlServiceTree;
    }

    /**
     * @param htmlServiceTree the htmlServiceTree to set
     */
    public void setHtmlServiceTree(String htmlServiceTree) {
        HtmlServiceTree = htmlServiceTree;
    }

    /**
     * @return the igTimeLineActivity
     */
    public List<ActivityCountDAO> getIgTimeLineActivity() {
        return igTimeLineActivity;
    }

    /**
     * @param igTimeLineActivity the igTimeLineActivity to set
     */
    public void setIgTimeLineActivity(List<ActivityCountDAO> igTimeLineActivity) {
        this.igTimeLineActivity = igTimeLineActivity;
    }

    /**
     * @return the dataTable2
     */
    public HtmlDataTable getDataTable2() {
        return dataTable2;
    }

    /**
     * @param dataTable2 the dataTable2 to set
     */
    public void setDataTable2(HtmlDataTable dataTable2) {
        this.dataTable2 = dataTable2;
    }
}
