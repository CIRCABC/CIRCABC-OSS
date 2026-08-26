package eu.cec.digit.circabc.web.wai.dialog.admin.migration;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportIgDialog extends BaseWaiDialog {

    private static final long serialVersionUID = 1L;
    private static final Log logger = LogFactory.getLog(ImportIgDialog.class);

    private String selectedIteration;
    private String action = "validate";
    private ImportService importService;

    public String getPageIconAltText() { return "Import CIRCABC Export"; }
    public String getBrowserTitle() { return "Import CIRCABC Export"; }

    @Override
    public void init(Map<String, String> parameters) {
        selectedIteration = null;
        action = "validate";
        super.init(parameters);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        if (!getNavigator().getCurrentUser().isAdmin()) {
            Utils.addErrorMessage("Only administrators can perform this operation.");
            this.isFinished = false;
            return null;
        }
        if (selectedIteration == null || selectedIteration.isEmpty()) {
            Utils.addErrorMessage("Please select an iteration.");
            this.isFinished = false;
            return null;
        }

        try {
            MigrationTracer<ImportRoot> tracer;
            if ("dryrun".equals(action)) {
                tracer = importService.dryRun(selectedIteration);
            } else if ("run".equals(action)) {
                tracer = importService.run(selectedIteration);
            } else {
                tracer = importService.validate(selectedIteration);
            }

            String msg = "Import " + action + " completed for iteration: " + selectedIteration;
            int errors = 0;
            if (tracer.getJournal() != null) {
                for (JournalLine line : tracer.getJournal()) {
                    if (JournalLine.Status.FAIL.equals(line.getStatus())) {
                        errors++;
                    }
                }
            }
            if (errors > 0) {
                msg += " - with " + errors + " error(s)";
                Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, msg);
            } else {
                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, msg);
            }
            logger.info(msg);
        } catch (ImportationException e) {
            logger.error("Import failed for iteration: " + selectedIteration, e);
            Utils.addErrorMessage("Import failed: " + e.getMessage(), e);
            this.isFinished = false;
            return null;
        }

        return null;
    }

    public List<SelectItem> getIterations() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem("", "<Select Iteration>"));
        try {
            for (MigrationIteration iteration : importService.getIterations(false)) {
                items.add(new SelectItem(iteration.getIdentifier(), iteration.getIdentifier()));
            }
        } catch (Exception e) {
            logger.warn("Error listing iterations", e);
        }
        return items;
    }

    public List<SelectItem> getActions() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem("validate", "Validate"));
        items.add(new SelectItem("dryrun", "Dry Run"));
        items.add(new SelectItem("run", "Run Import"));
        return items;
    }

    public String getSelectedIteration() { return selectedIteration; }
    public void setSelectedIteration(String v) { this.selectedIteration = v; }

    public String getAction() { return action; }
    public void setAction(String v) { this.action = v; }

    public ImportService getImportService() { return importService; }
    public void setImportService(ImportService v) { this.importService = v; }
}
