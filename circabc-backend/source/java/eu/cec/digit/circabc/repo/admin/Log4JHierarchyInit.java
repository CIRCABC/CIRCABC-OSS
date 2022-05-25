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
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package eu.cec.digit.circabc.repo.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Initialises Log4j's HierarchyDynamicMBean (refer to core-services-context.xml) and any overriding
 * log4.properties files.
 *
 * <p>Alfresco modules can provide their own log4j.properties file, which augments/overrides the
 * global log4j.properties within the Alfresco webapp. Within the module's source tree, suppose you
 * create:
 *
 * <pre>
 *      config/alfresco/module/{module.id}/log4j.properties</pre>
 * <p>
 * At deployment time, this log4j.properties file will be placed in:
 *
 * <pre>
 *      WEB-INF/classes/alfresco/module/{module.id}/log4j.properties</pre>
 * <p>
 * Where {module.id} is whatever value is set within the AMP's module.properties file. For details,
 * see: <a href='http://wiki.alfresco.com/wiki/Developing_an_Alfresco_Module'>Developing an Alfresco
 * Module</a>
 *
 * <p>For example, if {module.id} is "org.alfresco.module.avmCompare", then within your source code
 * you'll have:
 *
 * <pre>
 *      config/alfresco/module/org.alfresco.module.avmCompare/log4j.properties</pre>
 * <p>
 * This would be deployed to:
 *
 * <pre>
 *      WEB-INF/classes/alfresco/module/org.alfresco.module.avmCompare/log4j.properties</pre>
 * <p>
 * By default the 'overriding_log4j_properties' property of the 'log4JHierarchyInit' bean within
 * core-services-context.xml is:
 *
 * <pre>
 *      classpath&#042;:alfresco/module/&#042;/log4j.properties</pre>
 * <p>
 * Therefore, Log4JHierarchyInit will discover this supplimentary log4j.properties file, and merge
 * it with the main log4j.file (WEB-INF/classes/log4j.properties). For example, the
 * org.alfresco.module.avmCompare log4j.properties file might look like this:
 *
 * <pre>
 *    #-----------------------------------------------------------------------
 *    # webscript module log4j.properties
 *    #
 *    #   NOTE
 *    #   ----
 *    #
 *    #      Log4j uses the following logging levels:
 *    #      debug,info,warn,error,fatal
 *    #
 *    #      To set the logging level of {fullClassName} to {loglevel},
 *    #      add a line to this file of the following form:
 *    #
 *    #               log4j.logger.{fullClassName}={loglevel}
 *    #
 *    #      For example, to make 'com.example.MyExample' produce 'debug'
 *    #      logs, add a line like this:
 *    #
 *    #               log4j.logger.com.example.MyExample=debug
 *    #
 *    #
 *    #   WARNING
 *    #   -------
 *    #       Log properties in this log4j.properties file override/augment
 *    #       those in the webapp's main log4j.properties.
 *    #
 *    #-----------------------------------------------------------------------
 *
 *    log4j.logger.org.alfresco.module.avmCompare.AvmCompare=info
 *    </pre>
 * <p>
 * This system allows the author of any module to provide rich logging control without concern for
 * corrupting the central log4j.properties file during AMP installation/deinstallation. For details,
 * see: <a href='http://wiki.alfresco.com/wiki/Module_Management_Tool'>Module Management Tool</a>
 */
public class Log4JHierarchyInit {

    private static final String XML = ".xml";
    private HierarchyDynamicMBean log4jHierarchy;
    private List<String> extra_log4j_urls;

    public Log4JHierarchyInit() {
        extra_log4j_urls = new ArrayList<>();
    }

    /**
     * Loads a set of augmenting/overriding log4j.properties files from locations specified via an
     * array of 'spring_urls' (typically provided by a core-services-context.xml).
     *
     * <p>This function supports Spring's syntax for retrieving multiple class path resources with the
     * same name, via the "classpath&#042;:" prefix. For details, see: <a
     * href='http://www.jdocs.com/spring/1.2.8/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html'>PathMatchingResourcePatternResolver</a>.
     */
    public void setOverriding_log4j_properties(final List<String> spring_urls) {
        for (final String url : spring_urls) {
            extra_log4j_urls.add(url);
        }
    }

    public void setLog4jHierarchy(final HierarchyDynamicMBean log4jHierarchy) {
        this.log4jHierarchy = log4jHierarchy;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        // Add each logger (that has a level set) from
        // the Log4J Repository to the Hierarchy MBean

        final LoggerRepository r = LogManager.getLoggerRepository();

        // Include overriding loggers
        //
        //       Typically, extra loggers come from AMP modules, but you
        //       could add others by augmenting core-services-context.xml
        //       This mechanism allows modules to have their own local
        //       log4j.properties file within:
        //
        //          WEB-INF/classes/alfresco/module/{module.id}/log4j.properties
        //
        //       Where:  module.id is whatever value is set within the AMP's
        //               'module.properties' file.
        //
        //       See also:
        //         http://wiki.alfresco.com/wiki/Developing_an_Alfresco_Module
        //         (the module.properties section)
        //
        //       And:
        //         core-services-context.xml

        set_overriding_loggers(r);

        final Enumeration loggers = r.getCurrentLoggers();
        Logger logger = null;

        while (loggers.hasMoreElements()) {
            logger = (Logger) loggers.nextElement();
            if (logger.getLevel() != null) {
                log4jHierarchy.addLoggerMBean(logger.getName());
            }
        }
    }

    void set_overriding_loggers(final LoggerRepository hierarchy) {
        for (final String spring_url : extra_log4j_urls) {
            set_overriding_logger(spring_url, hierarchy);
        }
    }

    void set_overriding_logger(final String spring_url, final LoggerRepository hierarchy) {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final PropertyConfigurator prop_config = new PropertyConfigurator();
        final DOMConfigurator dom_config = new DOMConfigurator();

        Resource[] resources = null;

        try {
            resources = resolver.getResources(spring_url);
        } catch (final Exception e) {
            return;
        }

        InputStream istream;
        Properties properties;
        // Read each resource
        for (final Resource resource : resources) {
            istream = null;
            try {
                istream = new BufferedInputStream(resource.getInputStream());

                if (resource.getFilename().endsWith(XML)) {
                    dom_config.doConfigure(istream, hierarchy);
                } else {
                    properties = new Properties();
                    properties.load(istream);
                    prop_config.doConfigure(properties, hierarchy);
                }
            } catch (final Throwable e) {
                /* do nothing */
            } finally {
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (IOException ignore) {

                    }
                }
            }
        }
    }
}
