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
package eu.cec.digit.circabc.web.wai.dialog.admin;

import eu.cec.digit.circabc.repo.statistics.ReportFile;
import eu.cec.digit.circabc.service.statistics.global.GlobalStatisticsService;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * @author beaurpi
 *
 */
public class GlobalStatisticsDialog extends BaseWaiDialog {

    /** Small icon default name */
    public static final String SPACE_SMALL_DEFAULT = "space_small";
    /**
     *
     */
    private static final long serialVersionUID = -2303657789984298507L;
    private static final Log logger = LogFactory.getLog(GlobalStatisticsDialog.class);
    private GlobalStatisticsService globalStatsServ;
    private List<FileInfo> contents;
    private List<ReportFile> reports;
    private String userid;
    private String result;

    public String getPageIconAltText() {
        return translate("global_statistics_dialog_alt_text");
    }

    public String getBrowserTitle() {
        return translate("global_statistics_dialog_title");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        return outcome;
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#cancel()
     */
    @Override
    public String cancel() {

        return super.cancel();
    }

    public String fireme(ActionEvent event) {

        globalStatsServ
                .saveStatsToExcel(globalStatsServ.getReportSaveFolder(), globalStatsServ.makeGlobalStats());

        loadContents();

        return null;
    }

    public String fireme2(ActionEvent event) {
        globalStatsServ.cleanAndZipPreviousReportFiles();

        loadContents();

        return null;
    }

    /***
     *
     * @param event
     * @return
     */
    public String generateDetailedIgList(ActionEvent event) {
        Runnable r = new DetailedIgListRunnable(AuthenticationUtil.getFullyAuthenticatedUser());
        getAsyncThreadPoolExecutor().execute(r);

        Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate("action_background"));

        return null;
    }

    /**
     *
     */
    public void loadContents() {
        this.contents = Collections.emptyList();
        this.reports = Collections.emptyList();

        this.contents = globalStatsServ.getListOfReportFiles();

        this.reports = new ArrayList<>();
        for (FileInfo f : this.contents) {
            this.reports.add(new ReportFile(f, f.getName()));
        }
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        if (!globalStatsServ.isReportSaveFolderExisting()) {
            globalStatsServ.prepareFolderRecipient();
        }

        loadContents();

    }

    public List<ReportFile> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(List<ReportFile> reports) {
        this.reports = reports;
    }

    public String searchLastLogin(ActionEvent event) {
        Date date;
        date = globalStatsServ.getLastLoginDateOfUser(userid);

        if (date == null) {
            result = "user never connected";
        } else {
            GregorianCalendar gcLogin = new GregorianCalendar();
            gcLogin.setTime(date);

            result = gcLogin.get(Calendar.DAY_OF_MONTH) + "-" + (gcLogin.get(Calendar.MONTH) + 1) + "-"
                    + gcLogin.get(Calendar.YEAR);
        }

        return null;
    }

    /***
     * GETTERS & SETTERS
     */


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

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the contents
     */
    public List<FileInfo> getContents() {
        return contents;
    }

    /**
     * @param containers the containers to set
     */
    public void setContents(List<FileInfo> contents) {
        this.contents = contents;
    }

    private class DetailedIgListRunnable implements Runnable {

        protected String userName;

        public DetailedIgListRunnable(String userName) {
            this.userName = userName;
        }

        public void run() {

            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
                public String doWork() {
                    globalStatsServ.saveDetailedIgStatsToExcel(globalStatsServ.getReportSaveFolder(),
                            globalStatsServ.makeDetailedIgStats());
                    return null;

                }
            }, userName);

        }
    }

}
