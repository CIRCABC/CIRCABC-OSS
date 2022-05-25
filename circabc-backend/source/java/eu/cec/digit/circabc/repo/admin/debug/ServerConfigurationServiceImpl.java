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

import eu.cec.digit.circabc.service.admin.debug.CacheReport;
import eu.cec.digit.circabc.service.admin.debug.JobReport;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.service.admin.debug.TriggerReport;
import org.alfresco.service.cmr.repository.NodeService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.*;

/**
 * Interface for Server configuration reader.
 *
 * @author Yanick Pignot
 */
public abstract class ServerConfigurationServiceImpl implements ServerConfigurationService {

    public static final File ALFRESCO_REPO_PROPS =
            new File("classpath:alfresco", "repository.properties");
    public static final File ALFRESCO_CACHE_STATEGIES =
            new File("classpath:alfresco/domain", "cache-strategies.properties");
    public static final File ALFRESCO_TRANSACTION_PROPS =
            new File("classpath:alfresco/domain", "transaction.properties");
    public static final File ALFRESCO_EMAIN_CONFIGURATION =
            new File("classpath:alfresco/emailserver", "email-server.properties");
    //	public static final File CIRCABC_SHARED_REPO_PROPS = new
    // File("classpath:alfresco/extension/config", "circabc-shared-repository.properties");
    public static final File CIRCABC_SHARED_REPO_PROPS =
            new File("classpath:alfresco/alfresco-global.properties");
    public static final File CIRCABC_REPO_PROPS =
            new File("classpath:alfresco/extension/config", "circabc-repository.properties");
    //  Migration 3.1 -> 3.4.6 - 09/01/2012 - Replaced configuration location by
    // alfresco-global.properties
    //	public static final File CIRCABC_SETTINGS = new File("classpath:alfresco/extension/config",
    // "circabc-settings.properties");
    public static final File CIRCABC_SETTINGS = new File("classpath:", "alfresco-global.properties");
    public static final File CIRCABC_SHARED_HIBERNATE_CONFIG =
            new File("classpath:alfresco/extension/domain", "circabc-shared-hibernate-cfg.properties");
    public static final File CIRCABC_QUARTZ_PROPS =
            new File("classpath:alfresco/extension/domain", "quartz.properties");
    public static final File CIRCABC_DB_POOL =
            new File("classpath:alfresco/extension", "circabc-dbpool-context.xml");
    public static final File CIRCABC_RMI =
            new File("classpath:alfresco/extension", "circabc-rmi-core-services-context.xml");
    public static final File CIRCABC_SHARED_PROPS =
            new File("classpath:alfresco/extension", "circabc-shared.properties");
    public static final File CIRCABC_FILE_SERVER_CONF =
            new File("classpath:alfresco/extension", "file-servers-custom.xml");
    public static final File CIRCABC_BUILD_CONF =
            new File("classpath:alfresco/extension", "build-config.properties");
    public static final File ECAS_PROPS = new File("classpath:", "ecas-config.properties");
    public static final File CIRCABC_VERSION_PROPS =
            new File("classpath:alfresco/extension/messages", "circabc-version.properties");
    private NodeService nodeService;
    private Scheduler scheduler;

    public File getCircabcVersionProps() {
        return CIRCABC_VERSION_PROPS;
    }

    public List<JobReport> getNoTriggeredJobs() throws SchedulerException {
        final List<JobReport> jobReports = new ArrayList<>(5);
        final List<Trigger> triggers = getTriggersObject();

        final List<String> trigeredJobs = new ArrayList<>(triggers.size());

        for (Trigger trigger : triggers) {
            trigeredJobs.add(trigger.getJobGroup() + "." + trigger.getJobName());
        }

        final String[] jobGroups = scheduler.getJobGroupNames();
        for (final String groupName : jobGroups) {
            for (final String jobName : scheduler.getJobNames(groupName)) {
                final JobDetail jobDetail = scheduler.getJobDetail(jobName, groupName);

                if (!trigeredJobs.contains(jobDetail.getFullName())) {
                    jobReports.add(new JobReportImpl(jobDetail));
                }
            }
        }

        return jobReports;
    }

    public List<TriggerReport> getTriggers() throws SchedulerException {
        final List<Trigger> triggers = getTriggersObject();
        final List<TriggerReport> triggerReports = new ArrayList<>(triggers.size());

        for (final Trigger trigger : triggers) {
            final JobDetail jobDetail =
                    scheduler.getJobDetail(trigger.getJobName(), trigger.getJobGroup());

            triggerReports.add(new TriggerReportImpl(trigger, jobDetail));
        }

        return triggerReports;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getConfigurationFileResume(File specificFile)
            throws IOException, DocumentException {
        final Map properties = new LinkedHashMap();

        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final Resource resource = resolver.getResource(specificFile.getPath());

        if (specificFile.getName().endsWith(".xml")) {
            parseXmlRessourceAndFill(properties, resource);
        } else {
            parsePropertiesRessourceAndFill(properties, resource);
        }

        return properties;
    }

    public abstract List<CacheReport> getCaches() throws SchedulerException;

    // ----------
    // ---  Util methods

    private List<Trigger> getTriggersObject() throws SchedulerException {
        final List<Trigger> triggerReports = new ArrayList<>(20);

        final String[] triggersGroups = scheduler.getTriggerGroupNames();
        for (final String groupName : triggersGroups) {
            for (final String triggerName : scheduler.getTriggerNames(groupName)) {
                final Trigger trigger = scheduler.getTrigger(triggerName, groupName);
                triggerReports.add(trigger);
            }
        }

        return triggerReports;
    }

    @SuppressWarnings("unchecked")
    private void parsePropertiesRessourceAndFill(final Map properties, Resource resource)
            throws IOException {

        final InputStream is = resource.getInputStream();

        try {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                properties.putAll(props);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseXmlRessourceAndFill(final Map properties, Resource resource)
            throws DocumentException, IOException {
        final InputStream is = resource.getInputStream();

        try {
            if (is != null) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                final SAXReader reader = new SAXReader(false);
                reader.setIgnoreComments(true);
                reader.setIncludeExternalDTDDeclarations(false);
                reader.setIncludeInternalDTDDeclarations(false);
                reader.setValidation(false);
                final Document document = reader.read(bufferedReader);
                final Element rootElement = document.getRootElement();

                parseXmlRessourceAndFill(rootElement.getName(), properties, rootElement);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseXmlRessourceAndFill(String prefixName, final Map properties, Element element) {
        final String name = prefixName + "." + element.getName();
        final String value = element.getTextTrim();
        final Iterator attributes = element.attributeIterator();
        final Iterator childs = element.elementIterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            properties.put(name + " [attribute:" + attribute.getName() + "]", attribute.getValue());
        }

        if (value != null && value.length() > 0) {
            properties.put(name + " [value]", value);
        }

        while (childs.hasNext()) {
            Element child = (Element) childs.next();
            parseXmlRessourceAndFill(name, properties, child);
        }
    }

    // ----------
    // ---  IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the scheduler
     */
    protected final Scheduler getSchedulerFactoryBean() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public final void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
