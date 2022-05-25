package eu.cec.digit.circabc.util.importer;

import eu.cec.digit.circabc.util.exporter.ExportActionStatus;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Action that can be executed in fore or background to import a given Category or IG
 *
 * @author dtrovato
 */
public class ImportActionExecuter extends ActionExecuterAbstractBase {

    // In parameters
    public static final String NAME = "circabc-cat-ig-importer";
    public static final String PARAM_IMPORT_DIRECTORY = "import-directory";
    public static final String PARAM_IMPORT_NAME = "import-name";
    public static final String PARAM_IMPORT_AUTHORITIES = "import-authorities";
    public static final String PARAM_IMPORT_STRUCTURE = "import-structure";
    public static final String PARAM_IMPORT_HEADERS = "import-headers";
    public static final String PARAM_IMPORT_KEEP_UUIDS = "import-keep-uuids";
    public static final String PARAM_IMPORT_PACKAGES = "import-packages";
    public static final String PARAM_IMPORT_START = "import-start";
    // Out parameters
    public static final String PARAM_CURRENT_STATUS = "current-status";
    public static final String PARAM_START_DATE = "start-date";
    public static final String PARAM_EXE_TIME = "execution-time";
    private static final Log logger = LogFactory.getLog(ImportActionExecuter.class);
    private CircabcImporter circabcImporter = null;

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_DIRECTORY,
                DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_IMPORT_DIRECTORY)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_NAME,
                DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_IMPORT_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_AUTHORITIES,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_IMPORT_AUTHORITIES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_STRUCTURE,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_IMPORT_STRUCTURE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_HEADERS,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_IMPORT_HEADERS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_KEEP_UUIDS,
                DataTypeDefinition.BOOLEAN, true,
                getParamDisplayLabel(PARAM_IMPORT_KEEP_UUIDS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_PACKAGES,
                DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_IMPORT_PACKAGES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_IMPORT_START,
                DataTypeDefinition.DATETIME, true,
                getParamDisplayLabel(PARAM_IMPORT_START)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        String importName = (String) action.getParameterValue(PARAM_IMPORT_NAME);
        Date importScheduledStart = (Date) action.getParameterValue(PARAM_IMPORT_START);

        // if no date set, start now
        if (importScheduledStart != null) {

            action.setParameterValue(PARAM_CURRENT_STATUS,
                    ExportActionStatus.WAITING.toString() +
                            " - Import of '" + importName + "' scheduled to start on: " + importScheduledStart);

            // precarious scheduling, redo if this becomes important. Actually
            // we should use Alfresco action scheduling, but there are many
            // parameters that we don't need
            while ((new Date()).before(importScheduledStart)) {

                try {
                    synchronized (this) {
                        // wait for 10 seconds and re-check
                        wait(10000);
                    }
                } catch (InterruptedException e) {
                    logger.error("Import action interrupted.");
                    action.setParameterValue(PARAM_CURRENT_STATUS,
                            ExportActionStatus.INTERRUPTED.toString());
                    return;
                }
            }
        }

        action.setParameterValue(PARAM_CURRENT_STATUS, ExportActionStatus.READY.toString());

        String importDirectory = (String) action.getParameterValue(PARAM_IMPORT_DIRECTORY);
        boolean importAuthorities = (Boolean) action.getParameterValue(PARAM_IMPORT_AUTHORITIES);
        boolean importStructure = (Boolean) action.getParameterValue(PARAM_IMPORT_STRUCTURE);
        boolean importHeaders = (Boolean) action.getParameterValue(PARAM_IMPORT_HEADERS);
        boolean keepUUIDs = (Boolean) action.getParameterValue(PARAM_IMPORT_KEEP_UUIDS);
        List<String> packages = (List<String>) action.getParameterValue(PARAM_IMPORT_PACKAGES);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "Starting import in background '" + importName + "' in NodeRef: " + actionedUponNodeRef
                            .toString());
        }

        action.setParameterValue(PARAM_CURRENT_STATUS, ExportActionStatus.STARTED.toString());
        Date importStartDate = new Date();
        action.setParameterValue(PARAM_START_DATE, importStartDate);

        StringBuilder logs = new StringBuilder();
        StringBuilder errorLogs = new StringBuilder();
        String log;
        String errorLog;
        Date startDate = null;
        String entityName = null;
        String executionTime = null;
        for (String packagesAsText : packages) {
            entityName = packagesAsText.split(":")[0].split("-")[1];
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Starting import of entity '" + entityName + "'");
                }
                startDate = new Date();

                circabcImporter.importTo(actionedUponNodeRef, getPackages(packagesAsText),
                        importDirectory, importAuthorities, importStructure, importHeaders, keepUUIDs);

                executionTime = getExecutionTime(new Date(), startDate);
                log = "Import of '" + entityName + "' <b>finished successfully</b> in " + executionTime;
                logs.append("&nbsp;&nbsp;&nbsp;<li>").append(log).append("</li><br/>");

                if (logger.isInfoEnabled()) {
                    logger.info(log);
                }
            } catch (Exception e) {
                errorLog = "Import of '" + entityName + "' <b>failed</b> (" + e.getMessage() + ")";
                errorLogs.append("&nbsp;&nbsp;&nbsp;<li>").append(errorLog).append("</li><br/>");

                logger.error(errorLog, e);
            }
        }

        action.setParameterValue(PARAM_CURRENT_STATUS, buildStatusMessage(logs, errorLogs));
        String completeExecutionTime = getExecutionTime(new Date(), importStartDate);
        action.setParameterValue(PARAM_EXE_TIME, completeExecutionTime);

        if (logger.isInfoEnabled()) {
            logger.info(
                    "End of import '" + importName + "' (done in " + completeExecutionTime + ") in NodeRef: "
                            + actionedUponNodeRef.toString());
        }
    }

    private String buildStatusMessage(StringBuilder logs, StringBuilder errors) {
        ExportActionStatus status;
        if (logs.length() > 0) {
            if (errors.length() > 0) {
                status = ExportActionStatus.FINISHED_WITH_ERRORS;
            } else {
                status = ExportActionStatus.FINISHED;
            }
        } else {
            status = ExportActionStatus.FAILED;
        }

        StringBuilder message = new StringBuilder("<b>" + status.name() + "</b>").append("<br/>");
        if (logs.length() > 0 || errors.length() > 0) {
            message.append("<ul>");
        }

        if (logs.length() > 0) {
            message.append(logs);
        }

        if (errors.length() > 0) {
            message.append(errors);
        }

        if (logs.length() > 0 || errors.length() > 0) {
            message.append("</ul>");
        }

        return message.toString();
    }

    private String getExecutionTime(Date endDate, Date startDate) {
        return DurationFormatUtils.formatDuration(endDate.getTime() - startDate.getTime(),
                "dd 'days' HH 'hours' mm 'minutes' ss 'seconds'");
    }

    private List<String> getPackages(String packagesAsText) {
        return Arrays.asList(packagesAsText.split(":"));
    }

    /**
     * Sets the value of the circabcImporter
     *
     * @param circabcImporter the circabcImporter to set.
     */
    public void setCircabcImporter(CircabcImporter circabcImporter) {
        this.circabcImporter = circabcImporter;
    }
}
