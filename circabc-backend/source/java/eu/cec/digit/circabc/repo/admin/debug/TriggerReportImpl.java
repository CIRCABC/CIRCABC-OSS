/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.admin.debug;

import eu.cec.digit.circabc.service.admin.debug.JobReport;
import eu.cec.digit.circabc.service.admin.debug.TriggerReport;
import org.quartz.*;

import java.util.Date;

/**
 * Concrete impelmentation of a Trigger Report
 *
 * @author Yanick Pignot
 */
public class TriggerReportImpl implements TriggerReport {

    private Trigger trigger;
    private JobReport jobReport;

    /**
     * @param trigger
     */
    public TriggerReportImpl(Trigger trigger, JobDetail jobDetail) {
        super();
        this.trigger = trigger;
        this.jobReport = new JobReportImpl(jobDetail);
    }

    public String getDescrition() {
        return ReportUtils.getSecuredString(trigger.getDescription());
    }

    public String getDisplayEndTime() {
        return ReportUtils.getSecuredString(this.getEndTime());
    }

    public String getDisplayFinalFireTime() {
        return ReportUtils.getSecuredString(this.getFinalFireTime());
    }

    public String getDisplayNexTime() {
        return ReportUtils.getSecuredString(this.getNexTime());
    }

    public String getDisplayPreviousFire() {
        return ReportUtils.getSecuredString(this.getPreviousFire());
    }

    public String getDisplayStartTime() {
        return ReportUtils.getSecuredString(this.getStartTime());
    }

    public Date getEndTime() {
        return trigger.getEndTime();
    }

    public Date getFinalFireTime() {
        return trigger.getFinalFireTime();
    }

    public String getFullname() {
        return trigger.getFullName();
    }

    public String getSimpleName() {
        return ReportUtils.getDisplayName(trigger.getGroup(), trigger.getName());
    }

    public JobReport getJobReport() {
        return jobReport;
    }

    public String getMisFireInstruction() {
        String result = null;
        final int code = trigger.getMisfireInstruction();
        if (code == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY) {
            result = "(Default) SMART_POLICY";
        } else if (trigger instanceof SimpleTrigger) {
            switch (code) {
                case SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW:
                    result = "(SimpleTrigger) FIRE_NOW";
                    break;

                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT:
                    result = "(SimpleTrigger) RESCHEDULE_NEXT_WITH_EXISTING_COUNT";
                    break;

                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT:
                    result = "(SimpleTrigger) RESCHEDULE_NEXT_WITH_REMAINING_COUNT";
                    break;

                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT:
                    result = "(SimpleTrigger) RESCHEDULE_";
                    break;

                case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT:
                    result = "(SimpleTrigger) RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT";
                    break;
            }
        } else if (trigger instanceof CronTrigger) {
            switch (code) {
                case CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:
                    result = "(CronTrigger) DO_NOTHING";
                    break;

                case CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:
                    result = "(CronTrigger) FIRE_ONCE_NOW";
                    break;
            }
        } else if (trigger instanceof NthIncludedDayTrigger) {
            switch (code) {
                case NthIncludedDayTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:
                    result = "(NthIncludedDayTrigger) DO_NOTHING";
                    break;

                case NthIncludedDayTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:
                    result = "(NthIncludedDayTrigger) FIRE_ONCE_NOW";
                    break;
            }
        }

        if (result == null) {
            return "N/A (code + "
                    + trigger.getMisfireInstruction()
                    + ", of implementation + "
                    + trigger.getClass().getSimpleName()
                    + " )";
        }

        return result;
    }

    public Date getNexTime() {
        return trigger.getNextFireTime();
    }

    public Date getPreviousFire() {
        return trigger.getPreviousFireTime();
    }

    public String getPriority() {
        final int priority = trigger.getPriority();

        if (priority == Trigger.DEFAULT_PRIORITY) {
            return "(default) " + priority;
        } else if (priority > Trigger.DEFAULT_PRIORITY) {
            return "(above default) " + priority;
        } else {
            return "(below default) " + priority;
        }
    }

    public Date getStartTime() {

        return trigger.getStartTime();
    }

    public boolean isVolatile() {
        return trigger.isVolatile();
    }

    public boolean mayFireAgain() {
        return trigger.mayFireAgain();
    }
}
