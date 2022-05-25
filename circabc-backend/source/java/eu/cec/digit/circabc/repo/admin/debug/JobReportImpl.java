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
import org.quartz.JobDetail;

/**
 * Concrete impelmentation of a Job Report
 *
 * @author Yanick Pignot
 */
public class JobReportImpl implements JobReport {

    private JobDetail jobDetail;

    /**
     * @param jobDetal
     */
    public JobReportImpl(JobDetail jobDetal) {
        super();
        this.jobDetail = jobDetal;
    }

    public String getDescrition() {
        return ReportUtils.getSecuredString(jobDetail.getDescription());
    }

    public String getFullname() {
        return jobDetail.getFullName();
    }

    public String getSimpleName() {
        return ReportUtils.getDisplayName(jobDetail.getGroup(), jobDetail.getName());
    }

    public String getJobClass() {
        return jobDetail.getJobClass().getName();
    }

    public boolean isStatefull() {
        return jobDetail.isStateful();
    }
}
