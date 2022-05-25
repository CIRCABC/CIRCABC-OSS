/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.ui.common.component.debug;

import eu.cec.digit.circabc.service.admin.debug.JobReport;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.service.admin.debug.TriggerReport;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.ui.common.component.debug.BaseDebugComponent;
import org.quartz.SchedulerException;

import javax.faces.context.FacesContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Component which displays the Scheduler (tiggers, cron, jobs) configuration
 *
 * @author Yanick Pignot
 */
public class UISchedulerReporter extends BaseDebugComponent {

    private transient ServerConfigurationService serverConfigurationService;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.debug.SchedulerReporter";
    }

    /**
     * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
     */
    @SuppressWarnings("unchecked")
    public Map getDebugData() {
        final Map properties = new LinkedHashMap();

        try {
            final List<TriggerReport> triggers = getServerConfigurationService().getTriggers();
            for (final TriggerReport report : triggers) {
                final String key = report.getSimpleName();

                properties.put("<b>TRIGGER: " + report.getFullname() + "</b>", "");

                properties.put(key + " - Description", report.getDescrition());
                properties.put(key + " - Start Time", report.getDisplayStartTime());
                properties.put(key + " - Previous Fire", report.getDisplayPreviousFire());
                properties.put(key + " - Next Fire", report.getDisplayNexTime());
                properties.put(key + " - End Time", report.getDisplayEndTime());
                properties.put(key + " - Final Fire Time", report.getDisplayFinalFireTime());
                properties.put(key + " - Mis Fire Instruction", report.getMisFireInstruction());
                properties.put(key + " - Priority", report.getPriority());
                properties.put(key + " - Volatile", report.isVolatile());
                properties.put(key + " - May Fire Again", report.mayFireAgain());

                JobReport jobReport = report.getJobReport();

                if (jobReport != null) {
                    final String jobKey = report.getSimpleName() + "[" + jobReport.getSimpleName() + "]";

                    properties.put("<b>  JOB: " + jobReport.getFullname() + "</b>", "");

                    properties.put(jobKey + " - Class", jobReport.getJobClass());
                    properties.put(jobKey + " - Descrition", jobReport.getDescrition());
                    properties.put(jobKey + " - Statefull", jobReport.isStatefull());
                }


            }
        } catch (SchedulerException e) {
            properties
                    .put("SchedulerException", "<b><font color=\"red\">" + e.getMessage() + "</font></b>");
        }

        try {
            final List<JobReport> simpleJobs = getServerConfigurationService().getNoTriggeredJobs();

            properties.put("**************************", "**************************");

            if (simpleJobs.size() == 0) {
                properties.put("<i>No non-triggered job found</i>", "");
            }

            for (final JobReport jobReport : simpleJobs) {
                final String jobKey = jobReport.getSimpleName();

                properties.put("<b>  Not Triggered JOB: " + jobReport.getFullname() + "</b>", "");

                properties.put(jobKey + " - Class", jobReport.getJobClass());
                properties.put(jobKey + " - Descrition", jobReport.getDescrition());
                properties.put(jobKey + " - Statefull", jobReport.isStatefull());
            }
        } catch (SchedulerException e) {
            properties
                    .put("SchedulerException", "<b><font color=\"red\">" + e.getMessage() + "</font></b>");
        }

        return properties;
    }

    /**
     * @return the serverConfigurationService
     */
    protected final ServerConfigurationService getServerConfigurationService() {
        if (serverConfigurationService == null) {
            serverConfigurationService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getServerConfigurationService();
        }
        return serverConfigurationService;
    }
}
