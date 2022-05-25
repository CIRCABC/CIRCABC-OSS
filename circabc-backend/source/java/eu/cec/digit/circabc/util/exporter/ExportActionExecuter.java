package eu.cec.digit.circabc.util.exporter;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;

/**
 * Action that can be executed in fore or background to export a given Category or IG
 *
 * @author schwerr
 */
public class ExportActionExecuter extends ActionExecuterAbstractBase {

    // In parameters
    public static final String NAME = "circabc-cat-ig-exporter";
    public static final String PARAM_ITEMS_TO_EXPORT = "items-to-export";
    public static final String PARAM_EXPORT_DIRECTORY = "export-directory";
    public static final String PARAM_EXPORT_AUTHORITIES = "export-authorities";
    public static final String PARAM_EXPORT_STRUCTURE = "export-structure";
    public static final String PARAM_EXPORT_HEADERS = "export-headers";
    public static final String PARAM_EXPORT_ONLY_ROOT = "export-only-root";
    public static final String PARAM_EXPORT_START = "export-start";
    // Out parameters
    public static final String PARAM_CURRENT_STATUS = "current-status";
    public static final String PARAM_START_DATE = "start-date";
    public static final String PARAM_EXE_TIME = "execution-time";
    private static final Log logger = LogFactory.getLog(ExportActionExecuter.class);
    private CircabcExporter circabcExporter = null;

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

        paramList.add(new ParameterDefinitionImpl(PARAM_ITEMS_TO_EXPORT,
                DataTypeDefinition.ANY, true,
                getParamDisplayLabel(PARAM_ITEMS_TO_EXPORT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_DIRECTORY,
                DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_EXPORT_DIRECTORY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_AUTHORITIES,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_EXPORT_AUTHORITIES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_STRUCTURE,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_EXPORT_STRUCTURE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_HEADERS,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_EXPORT_HEADERS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_ONLY_ROOT,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_EXPORT_ONLY_ROOT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_EXPORT_START,
                DataTypeDefinition.DATETIME, true,
                getParamDisplayLabel(PARAM_EXPORT_START)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        Date exportStart = (Date) action.getParameterValue(PARAM_EXPORT_START);

        // if no date set, start now
        if (exportStart != null) {

            action.setParameterValue(PARAM_CURRENT_STATUS,
                    ExportActionStatus.WAITING.toString() +
                            " - Scheduled to start on: " + exportStart);

            // precarious scheduling, redo if this becomes important. Actually
            // we should use Alfresco action scheduling, but there are many
            // parameters that we don't need
            while ((new Date()).before(exportStart)) {

                try {
                    synchronized (this) {
                        // wait for 10 seconds and re-check
                        wait(10000);
                    }
                } catch (InterruptedException e) {
                    logger.error("Export action interrupted.");
                    action.setParameterValue(PARAM_CURRENT_STATUS,
                            ExportActionStatus.INTERRUPTED.toString());
                    return;
                }
            }
        }

        action.setParameterValue(PARAM_CURRENT_STATUS, ExportActionStatus.READY.toString());

        String exportDirectory = (String) action.getParameterValue(PARAM_EXPORT_DIRECTORY);

        boolean exportAuthorities = (Boolean) action.getParameterValue(PARAM_EXPORT_AUTHORITIES);
        boolean exportStructure = (Boolean) action.getParameterValue(PARAM_EXPORT_STRUCTURE);
        boolean exportHeaders = (Boolean) action.getParameterValue(PARAM_EXPORT_HEADERS);
        boolean exportOnlyRoot = (Boolean) action.getParameterValue(PARAM_EXPORT_ONLY_ROOT);

        List<ExportItemData> itemsToExport = (List<ExportItemData>)
                action.getParameterValue(PARAM_ITEMS_TO_EXPORT);

        // export the batch of categories or IGs
        for (ExportItemData exportItemData : itemsToExport) {

            logger.info("Starting export in background. Name: " +
                    exportItemData.getName() + ", NodeRef: " +
                    exportItemData.getNodeRef().toString());

            action.setParameterValue(PARAM_CURRENT_STATUS,
                    ExportActionStatus.STARTED.toString());

            Date startDate = new Date();
            action.setParameterValue(PARAM_START_DATE, startDate);

            try {
                circabcExporter.export(exportItemData.getNodeRef(),
                        exportItemData.getName(), exportDirectory,
                        exportAuthorities, exportStructure,
                        exportHeaders, exportOnlyRoot);
            } catch (Exception e) {
                logger.error("Exception while exporting.", e);
                action.setParameterValue(PARAM_CURRENT_STATUS,
                        ExportActionStatus.FAILED.toString() + " (" +
                                e.getMessage() + ")");
                continue;
            }

            action.setParameterValue(PARAM_CURRENT_STATUS,
                    ExportActionStatus.FINISHED.toString());

            String executionTime = getExecutionTime(new Date(), startDate);
            action.setParameterValue(PARAM_EXE_TIME, action.getParameterValue(
                    PARAM_EXE_TIME) + " " + executionTime);

            logger.info("Export finished in " + executionTime + ". Name: " +
                    exportItemData.getName() + ", NodeRef: " +
                    exportItemData.getNodeRef().toString());
        }
    }

    private String getExecutionTime(Date endDate, Date startDate) {
        return DurationFormatUtils.formatDuration(endDate.getTime() -
                startDate.getTime(), "dd 'days' HH 'hours' mm 'minutes' ss 'seconds'");
    }

    /**
     * Sets the value of the circabcExporter
     *
     * @param circabcExporter the circabcExporter to set.
     */
    public void setCircabcExporter(CircabcExporter circabcExporter) {
        this.circabcExporter = circabcExporter;
    }
}
