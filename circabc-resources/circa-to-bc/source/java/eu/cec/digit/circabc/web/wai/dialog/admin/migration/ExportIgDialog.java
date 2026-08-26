package eu.cec.digit.circabc.web.wai.dialog.admin.migration;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.web.repository.CategoryHeaderNode;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryItem;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ExportIgDialog extends BaseWaiDialog {

    private static final long serialVersionUID = 1L;
    private static final Log logger = LogFactory.getLog(ExportIgDialog.class);

    private String sourceHeader;
    private String sourceCategory;
    private String sourceIg;
    private List allHeaders;
    private CategoryHeadersBean categoryHeadersBean;

    /** NodeRef of the marshalled export file (the "uploaded.xml" content node) produced by the last export. */
    private NodeRef exportedFileRef;

    public String getPageIconAltText() {
        return "Export Interest Group";
    }

    public String getBrowserTitle() {
        return "Export Interest Group";
    }

    @Override
    public void init(Map<String, String> parameters) {
        sourceHeader = null;
        sourceCategory = null;
        sourceIg = null;
        allHeaders = null;
        // NOTE: exportedFileRef is intentionally NOT reset here.
        // After a successful export, the WAI dialog framework closes and immediately
        // re-opens this same (session-scoped) dialog, which calls init() again. If we
        // cleared exportedFileRef here, the result panel in export-ig.jsp
        // (rendered="#{...exportCompleted}") would evaluate to false on the re-opened
        // page and the download link would never be shown - the user would only see the
        // status message text. The reference is instead reset at the start of each new
        // export in finishImpl(), so it reflects the latest export result.
        super.init(parameters);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        if (!getNavigator().getCurrentUser().isAdmin()) {
            Utils.addErrorMessage("Only administrators can perform this operation.");
            this.isFinished = false;
            return null;
        }
        if (sourceIg == null || !isNotEmpty(sourceIg)) {
            Utils.addErrorMessage("Please select an Interest Group to export.");
            this.isFinished = false;
            return null;
        }
        if (sourceCategory == null || !isNotEmpty(sourceCategory)) {
            Utils.addErrorMessage("Please select a Category.");
            this.isFinished = false;
            return null;
        }

        String categoryName = getName(new NodeRef(sourceCategory));
        String igName = getName(new NodeRef(sourceIg));
        String iterationName = "circabc-export-" + igName + "-"
                + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());

        ExportService exportService = (ExportService) WebApplicationContextUtils
                .getRequiredWebApplicationContext(
                        (javax.servlet.ServletContext) context.getExternalContext().getContext())
                .getBean("CircabcExportService");

        CategoryInterestGroupPair pair = new CategoryInterestGroupPair(categoryName, igName);

        // Clear any previous export result before starting a new export so the download
        // link reflects only the current attempt. (Not cleared in init() on purpose - see
        // the comment there about the dialog close/re-open lifecycle.)
        this.exportedFileRef = null;

        MigrationTracer<ImportRoot> tracer = exportService.runExport(
                pair, iterationName, "CIRCABC IG Export");

        // Keep the produced export file node (the "uploaded.xml" content node) so the
        // JSP can render a download link for it.
        this.exportedFileRef = tracer.getMarshalledFile();

        String msg = "Export completed for IG: " + igName;
        if (this.exportedFileRef != null) {
            msg += " - use the download link below to retrieve the exported file.";
        }
        Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, msg);
        logger.info(msg
                + (this.exportedFileRef != null ? " File node: " + this.exportedFileRef : ""));

        return null;
    }

    // --- dropdown data ---

    public List getAllHeaders() {
        if (allHeaders == null) {
            allHeaders = new ArrayList();
            allHeaders.add(new SelectItem("null", "<Select Header>"));
            for (CategoryHeader h : categoryHeadersBean.getCategoryHeaders()) {
                CategoryItem item = h.getCategoryHeaderItem();
                allHeaders.add(new SelectItem(item.getNodeRef().toString(), item.getName()));
            }
        }
        return allHeaders;
    }

    public List getSourceCategories() {
        if (isNotEmpty(sourceHeader)) {
            List result = new ArrayList();
            CategoryHeaderNode node = new CategoryHeaderNode(new NodeRef(sourceHeader));
            CategoryHeader header = new CategoryHeader(node);
            result.add(new SelectItem("null", "<Select Category>"));
            for (CategoryItem cat : header.getCategories()) {
                result.add(new SelectItem(cat.getNodeRef().toString(), getName(cat.getNodeRef())));
            }
            return result;
        }
        return new ArrayList();
    }

    public List getSourceIgs() {
        if (isNotEmpty(sourceCategory)) {
            List result = new ArrayList();
            for (NodeRef igRef : getManagementService().getInterestGroups(new NodeRef(sourceCategory))) {
                result.add(new SelectItem(igRef.toString(), getName(igRef)));
            }
            return result;
        }
        return new ArrayList();
    }

    private String getName(NodeRef ref) {
        Map<QName, Serializable> props = getNodeService().getProperties(ref);
        return props.containsKey(ContentModel.PROP_NAME)
                ? props.get(ContentModel.PROP_NAME).toString() : ref.toString();
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty() && !s.equalsIgnoreCase("null");
    }

    // --- getters/setters ---

    public String getSourceHeader() { return sourceHeader; }
    public void setSourceHeader(String v) { if (isNotEmpty(v)) this.sourceHeader = v; }

    public String getSourceCategory() { return sourceCategory; }
    public void setSourceCategory(String v) { if (isNotEmpty(v)) this.sourceCategory = v; }

    public String getSourceIg() { return sourceIg; }
    public void setSourceIg(String v) { if (isNotEmpty(v)) this.sourceIg = v; }

    public CategoryHeadersBean getCategoryHeadersBean() { return categoryHeadersBean; }
    public void setCategoryHeadersBean(CategoryHeadersBean v) { this.categoryHeadersBean = v; }

    // --- export result (download link) ---

    /** @return true once an export has produced a downloadable file. */
    public boolean isExportCompleted() {
        return exportedFileRef != null;
    }

    /** @return the name of the exported file node (e.g. "uploaded.xml"), or null. */
    public String getExportedFileName() {
        return exportedFileRef == null ? null : getName(exportedFileRef);
    }

    /**
     * @return the browser download URL for the exported file node, prefixed with the
     *         web application context path, or null if no export has been done yet.
     */
    public String getExportedFileDownloadUrl() {
        if (exportedFileRef == null) {
            return null;
        }
        String contextPath = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestContextPath();
        if ("/".equals(contextPath)) {
            contextPath = "";
        }
        return contextPath
                + DownloadContentServlet.generateDownloadURL(exportedFileRef, getExportedFileName());
    }
}
