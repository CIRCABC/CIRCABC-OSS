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

/**
 * Base interface to compute the report of Cron job.
 *
 * <blockquote>
 * <p>
 * Conveys the detail properties of a given Job instance. Jobs have a name and group associated with
 * them, which should uniquely identify them within a single Scheduler. Triggers are the 'mechanism'
 * by which Jobs are scheduled. Many Triggers can point to the same Job, but a single Trigger can
 * only point to one Job.
 *
 * </blockquote>
 *
 * @author Yanick Pignot
 * @see org.quartz.JobDetail
 */
public interface JobReport {

    /**
     * The Full name of a job is the result of the concatenation of its group and its name.
     *
     * @return the full name of the job.
     */
    String getFullname();

    /**
     * Since the most of jobs are in located in the group called <i><b>DEFAULT</b></i>, this method
     * return a user friendly unique name whitout the group if this last is named <i>DEFAULT</i>
     */
    String getSimpleName();

    /**
     * @return the description of the job (if setted)
     */
    String getDescrition();

    /**
     * @return the class instance where the job is defined
     */
    String getJobClass();

    /**
     * Whether or not the Job implements the interface StatefulJob
     *
     * @return if the job is statefull
     */
    boolean isStatefull();
}
