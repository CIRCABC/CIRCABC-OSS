/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.category;

import eu.cec.digit.circabc.repo.statistics.ReportFile;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.statistics.global.GlobalStatisticsService;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 */
public class CategoryGroupStatisticsBean extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -7521049526866523827L;
    private List<ReportFile> reports;
    private GlobalStatisticsService globalStatsServ;
    private LockService circabcLockService;

    private List<FileInfo> contents;

    private String currentCategoryName;
    private NodeRef currentCategoryRef;

    /**
     *
     */
    public CategoryGroupStatisticsBean() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getPageIconAltText() {
        return translate("view_category_group_statistics_action");
    }

    @Override
    public String getBrowserTitle() {
        return translate("view_category_group_statistics_action_title");
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        currentCategoryRef = getActionNode().getNodeRef();

        currentCategoryName = this.getNodeService()
                .getProperty(currentCategoryRef, ContentModel.PROP_NAME).toString();

        loadContents(currentCategoryName);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        return outcome;
    }

    /**
     * @return the reports
     */
    public List<ReportFile> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(List<ReportFile> reports) {
        this.reports = reports;
    }

    /**
     * @return the globalStatsServ
     */
    public GlobalStatisticsService getGlobalStatsServ() {
        return globalStatsServ;
    }

    /**
     * @param globalStatsServ the globalStatsServ to set
     */
    public void setGlobalStatsServ(GlobalStatisticsService globalStatsServ) {
        this.globalStatsServ = globalStatsServ;
    }

    public void loadContents(String categoryName) {
        this.contents = Collections.emptyList();
        this.reports = Collections.emptyList();

        this.contents = globalStatsServ
                .getCategoryGroupStatsFiles(categoryName, this.currentCategoryRef);

        this.reports = new ArrayList<>();
        for (FileInfo f : this.contents) {
            this.reports.add(new ReportFile(f, f.getName()));
        }
    }

    /**
     * @return the contents
     */
    public List<FileInfo> getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(List<FileInfo> contents) {
        this.contents = contents;
    }

    /***
     *
     * @param event
     * @return
     */
    public String generateCategoryIgList(ActionEvent event) {
        if (!circabcLockService.isLocked("categStatJob-" + currentCategoryRef.getId())) {
            circabcLockService.lock("categStatJob-" + currentCategoryRef.getId());

            Runnable r = new CategoryIgListRunnable(AuthenticationUtil.getSystemUserName(),
                    currentCategoryRef);
            getAsyncThreadPoolExecutor().execute(r);
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate("action_background"));

        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    "The category statistics are already being generated, please wait.");
        }

        return "";
    }

    /**
     * @return the circabcLockService
     */
    public LockService getCircabcLockService() {
        return circabcLockService;
    }

    /**
     * @param circabcLockService the circabcLockService to set
     */
    public void setCircabcLockService(LockService circabcLockService) {
        this.circabcLockService = circabcLockService;
    }

    private class CategoryIgListRunnable implements Runnable {

        protected String userName;
        protected NodeRef categoryRef;
        public CategoryIgListRunnable(String userName, NodeRef categoryRef) {
            this.userName = userName;
            this.categoryRef = categoryRef;
        }

        public void run() {

            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
                public String doWork() {
                    try {
                        globalStatsServ.saveCategoryGroupStatistics(
                                globalStatsServ.computeCategoryGroupStatistics(categoryRef), currentCategoryName);
                    } finally {
                        circabcLockService.unlock("categStatJob-" + categoryRef.getId());
                    }

                    return null;

                }
            }, userName);

        }
    }

}
