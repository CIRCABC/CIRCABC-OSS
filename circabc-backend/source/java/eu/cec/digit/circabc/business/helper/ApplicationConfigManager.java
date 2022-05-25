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
package eu.cec.digit.circabc.business.helper;

import eu.cec.digit.circabc.business.api.nav.NodeType;
import eu.cec.digit.circabc.business.api.space.ContainerIcon;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.PropertySheetConfigElement;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

/**
 * Business service helper to manage server config, via the alfresco configuration service.
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. ConfigService was moved to Spring. This class seems to be developed for CircaBC
 */
public class ApplicationConfigManager {

    private static final String CONFIG_PATTERN_ICON = "{0} icons";
    private static final String CONFIG_CONTENT_WIZARDS = "Content Wizards";

    private static final String ELEM_CREATE_MIME_TYPES = "create-mime-types";
    private static final String ELEM_DEFAULT_ENCODING = "default-encoding";
    private static final String ELEM_ICON = "icons";
    private static final String ELEM_PROPERTY_SHEET = "property-sheet";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";
    private final Log logger = LogFactory.getLog(ApplicationConfigManager.class);
    private String encoding;
    private List<String> inlineEditableMimeTypes;
    private Map<QName, List<ContainerIcon>> icons = new HashMap<>(12);
    private Map<Set<QName>, Map<String, ItemConfig>> propertyConfigs = new HashMap<>(
            NodeType.values().length);
    private NamespaceService namespaceService;
    private ConfigService webConfigService;

    private boolean dynamicConfig = false;

    //--------------
    //-- public methods


    /**
     * The configuration can be dynamic or not. A dynamic config means that the configuration values
     * can be changed at the runtime.
     *
     * @return if the config dynamic or not.
     */
    public boolean isDynamicConfig() {
        return dynamicConfig;
    }

    /**
     * @param dynamicConfig the dynamicConfig to set
     */
    public final void setDynamicConfig(boolean dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    public String getEncoding() {
        // this value can be cached.
        if (this.encoding == null || isDynamicConfig()) {
            this.encoding = extractConfigValue(CONFIG_CONTENT_WIZARDS, ELEM_DEFAULT_ENCODING);
            if (this.encoding == null || this.encoding.length() == 0) {
                this.encoding = Charset.defaultCharset().name();

                if (logger.isWarnEnabled()) {
                    logger.warn("Default encoding not configurend in config '" + CONFIG_CONTENT_WIZARDS
                            + "', element '" + ELEM_DEFAULT_ENCODING + "'");
                    logger.warn("System default encoding will be used: " + this.encoding);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Application configured default encoding: " + this.encoding);
                }
            }
        }

        return this.encoding;
    }

    public List<String> getInlineEditableMimeTypes() {
        // this value can be cached.
        if (this.inlineEditableMimeTypes == null || isDynamicConfig()) {
            this.inlineEditableMimeTypes = extractConfigAttributes(CONFIG_CONTENT_WIZARDS,
                    ELEM_CREATE_MIME_TYPES, ATTR_NAME);

            if (this.inlineEditableMimeTypes == null) {
                this.inlineEditableMimeTypes = Collections.emptyList();

                if (logger.isWarnEnabled()) {
                    logger.warn("Default inline editables mimetypes not configurend in config '"
                            + CONFIG_CONTENT_WIZARDS + "', element '" + ELEM_CREATE_MIME_TYPES + "', attributes '"
                            + ATTR_NAME);
                }
            }

            if (this.inlineEditableMimeTypes.size() == 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn("No contents will be editable online!");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Application configured inline editable mimetypes: " + this.inlineEditableMimeTypes);
                }
            }


        }
        return this.inlineEditableMimeTypes;
    }

    /**
     * Get the icons of a given node type. (as Folder, Forum, ...)
     */
    public List<ContainerIcon> getIcons(final QName nodeTypeQname) {
        Assert.notNull(nodeTypeQname);

        if (!this.icons.containsKey(nodeTypeQname) || isDynamicConfig()) {
            final String typePrefixForm = nodeTypeQname.toPrefixString(getNamespaceService());
            final String configName = MessageFormat.format(CONFIG_PATTERN_ICON, typePrefixForm);

            final List<ContainerIcon> wrappers;
            final List<Pair<String, String>> iconConfigs = extractConfigAttributes(configName, ELEM_ICON,
                    ATTR_NAME, ATTR_PATH);

            if (iconConfigs == null) {
                wrappers = Collections.emptyList();

                if (logger.isWarnEnabled()) {
                    logger.warn("No icon config found for node type " + nodeTypeQname.toString()
                            + ". Please to fill '" + configName + "' configuration name.");
                }
            } else {
                wrappers = new ArrayList<>(iconConfigs.size());
                for (final Pair<String, String> pair : iconConfigs) {
                    wrappers.add(new ContainerIcon(pair.getFirst(), pair.getSecond()));
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Icons for node type " + nodeTypeQname + ": " + wrappers);
                }
            }

            icons.put(nodeTypeQname, wrappers);

        }

        return icons.get(nodeTypeQname);
    }

    /**
     * Get the icon of a given node type (as Folder, Forum, ...) with a given name
     */
    public ContainerIcon getIcon(final QName nodeTypeQname, final String name) {
        Assert.hasText(name);

        ContainerIcon result = null;

        for (final ContainerIcon icon : getIcons(nodeTypeQname)) {
            if (name.equals(icon.getIconName())) {
                result = icon;
                break;
            }
        }

        return result;
    }

    //--------------
    //-- private helpers

    /**
     * Return the node property client behaviour configuration.
     */
    public Map<String, ItemConfig> getPropertiesDefinition(final NodeRef nodeRef) {
        Assert.notNull(nodeRef);
        final Node node = new Node(nodeRef);

        // The property sheet configurat used the type and aspects of the node to build configuration,
        // it make sens to cache it with these information.
        // The retreiving of the type and aspects is not a waste of time, since the Node object caches it
        // and must retreive it later.
        final Set<QName> typesAndAspects = node.getAspects();
        typesAndAspects.add(node.getType());

        if (!this.propertyConfigs.containsKey(typesAndAspects) || isDynamicConfig()) {
            final PropertySheetConfigElement configElement = (PropertySheetConfigElement) getConfigElement(
                    node, ELEM_PROPERTY_SHEET);
            final Map<String, ItemConfig> itemConfigs;

            if (configElement != null) {
                itemConfigs = configElement.getItems();
            } else {
                itemConfigs = Collections.emptyMap();
            }

            if (itemConfigs.size() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Property definition found for node (" + nodeRef + "): " + itemConfigs.keySet());
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("No property sheet configuration found for node (" + nodeRef + ")" +
                            "\n\tWith type:   " + node.getType() +
                            "\n\tWith aspects:" + node.getAspects());
                }
            }

            this.propertyConfigs.put(typesAndAspects, itemConfigs);
        }
        return this.propertyConfigs.get(typesAndAspects);
    }

    private String extractConfigValue(final Object configId, final String configElementName) {
        final ConfigElement configElement = getConfigElement(configId, configElementName);
        if (configElement != null) {
            return configElement.getValue();
        } else {
            // config not found
            return null;
        }
    }

    private List<String> extractConfigAttributes(final Object configId,
                                                 final String configElementName, final String attributeName) {
        final ConfigElement configElement = getConfigElement(configId, configElementName);
        if (configElement != null) {
            final List<String> values = new ArrayList<>(10);
            for (ConfigElement child : configElement.getChildren()) {
                final String value = child.getAttribute(attributeName);
                if (value != null) {
                    values.add(value);
                }
            }

            return values;
        } else {
            // config not found
            return null;
        }
    }

    private List<Pair<String, String>> extractConfigAttributes(final Object configId,
                                                               final String configElementName, final String firstAttributeName,
                                                               final String secondSAttributeName) {
        final ConfigElement configElement = getConfigElement(configId, configElementName);
        if (configElement != null) {
            final List<Pair<String, String>> values = new ArrayList<>(10);
            for (ConfigElement child : configElement.getChildren()) {
                final String value1 = child.getAttribute(firstAttributeName);
                final String value2 = child.getAttribute(secondSAttributeName);
                if (value1 != null && value2 != null) {
                    values.add(new Pair<>(value1, value2));
                }
            }

            return values;
        } else {
            // config not found
            return null;
        }
    }

    //	--------------
    //-- IOC

    private ConfigElement getConfigElement(final Object configId, final String configElementName) {
        final Config config = getWebConfigService().getConfig(configId);
        if (config != null) {
            return config.getConfigElement(configElementName);
        } else {
            return null;
        }
    }

    /**
     * @return the webConfigService
     */
    protected final ConfigService getWebConfigService() {
        return webConfigService;
    }

    /**
     * @param webConfigService the webConfigService to set
     */
    public final void setWebConfigService(final ConfigService webConfigService) {
        this.webConfigService = webConfigService;
    }

    /**
     * @return the namespaceService
     */
    protected final NamespaceService getNamespaceService() {
        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }


}
