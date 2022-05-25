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

import org.dom4j.DocumentException;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for Server configuration reader. Generates reports on different critical points of the
 * configuration.
 *
 * @author Yanick Pignot
 */
public interface ServerConfigurationService {

    /**
     * Read the scheduler implemetation to retrieve the relevant information about configured Triggers
     * of the system.
     *
     * @return the list of trigger defined in a report
     */
    List<TriggerReport> getTriggers() throws SchedulerException;

    File getCircabcVersionProps();

    /**
     * Read the scheduler implemetation to retrieve the relevant information about <b>The Non
     * Trigered</b> jobs.
     *
     * @return the list of trigger defined in a report
     */
    List<JobReport> getNoTriggeredJobs() throws SchedulerException;

    /**
     * Read a give property or configuration files.
     *
     * @return a list of key/value with the property of the file
     */
    Map<String, String> getConfigurationFileResume(File specificFile)
            throws IOException, DocumentException;

    /**
     * Read the cache manager to retrieve the relevant information about configured Caches of the
     * system.
     *
     * @return the list of caches defined in the cache manager
     */
    List<CacheReport> getCaches() throws SchedulerException;
}
