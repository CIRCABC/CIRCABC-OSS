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
package eu.cec.digit.circabc.service.admin.debug;

import java.util.Date;

/**
 * Base interface to compute the report of Trigger job.
 *
 * <blockquote>
 * <p>
 * Triggers s have a name and group associated with them, which should uniquely identify them within
 * a single Scheduler. Triggers are the 'mechanism' by which Job s are scheduled. Many Trigger s can
 * point to the same Job, but a single Trigger can only point to one Job. Triggers can 'send'
 * parameters/data to Jobs by placing contents into the JobDataMap on the Trigger.
 *
 * </blockquote>
 *
 * @author Yanick Pignot
 * @see org.quartz.Trigger
 */
public interface TriggerReport {

    /**
     * @return the job report associated to the current Trigger
     */
    JobReport getJobReport();

    /**
     * The Full name of a Trigger is the result of the concatenation of its group and its name.
     *
     * @return the full name of the trigger.
     */
    String getFullname();

    /**
     * Since the most of triggers are in located in the group called <i><b>DEFAULT</b></i>, this
     * method return a user friendly unique name whitout the group if this last is named
     * <i>DEFAULT</i>
     */
    String getSimpleName();

    /**
     * @return the description of the tigger (if setted)
     */
    String getDescrition();

    /**
     * @return the formated end date
     * @see TriggerReport#getEndTime()
     */
    String getDisplayEndTime();

    /**
     * @return the formated final fire time
     * @see TriggerReport#getFinalFireTime()
     */
    String getDisplayFinalFireTime();

    /**
     * @return the formated next time
     * @see TriggerReport#getNexTime()
     */
    String getDisplayNexTime();

    /**
     * @return the formated previous fire time
     * @see TriggerReport#getPreviousFire()
     */
    String getDisplayPreviousFire();

    /**
     * @return the formated start time
     * @see TriggerReport#getStartTime()
     */
    String getDisplayStartTime();

    /**
     * The time at which the Trigger should quit repeating - even if an assigned 'repeatCount' isn't
     * yet satisfied
     *
     * @return the end fire time
     */
    Date getEndTime();

    /**
     * The last time at which the Trigger will fire, if the Trigger will repeat indefinitely, null
     * will be returned
     *
     * @return final fire time
     */
    Date getFinalFireTime();

    /**
     * The next time at which the Trigger is scheduled to fire
     *
     * @return next execution time
     */
    Date getNexTime();

    /**
     * The previous time at which the Trigger fired
     *
     * @return the previsous fire time
     */
    Date getPreviousFire();

    /**
     * The time at which the Trigger should occur
     *
     * @return the start time
     */
    Date getStartTime();

    /**
     * Whether or not the Trigger should be persisted in the JobStore for re-use after program
     * restarts
     *
     * @return if the trigger is volatile
     */
    boolean isVolatile();

    /**
     * Whether or not it is possible for this Trigger to fire again
     *
     * @return if the trigger may fire again
     */
    boolean mayFireAgain();

    /**
     * @return a String representation of the misFireInstruction
     */
    String getMisFireInstruction();

    /**
     * @return a String representation of the priority of the trigger
     */
    String getPriority();
}
