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
package eu.cec.digit.circabc.repo.customisation.logo;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Concrete implementation of Logo Preference Service
 *
 * @author Yanick Pignot
 */
public class LogoPreferencesServiceImpl implements LogoPreferencesService {

    private static final String METHOD_GET_CONFIGURED_ON = "getConfiguredOn";
    private static final String SET_PREFIX = "set";
    private static final String METHOD_GET_LOGO = "getLogo";
    private static final String REGEX_GET_OR_IS = "^is|^get";

    private static final Log logger = LogFactory.getLog(LogoPreferencesServiceImpl.class);

    private static final String CUSTOMIZATION_ROOT_FOLDER = "iglookAndFeel";
    private static final String CUSTOMIZATION_ICON_FOLDER = "icon";
    private static final String CUSTOMIZATION_ICON_CONFIG_FOLDER = "definition";
    private static final String CUSTOMIZATION_ICON_DATA_FOLDER = "images";

    private static final String CONFIG_FILE_NAME = "config.properties";

    private NodePreferencesService nodePreferencesService;
    private NodeService nodeService;
    private ContentService contentService;
    private SimpleCache<NodeRef, List<LogoDefinition>> logoCache;
    private SimpleCache<NodeRef, DefaultLogoConfiguration> configCache;

    public LogoDefinition addLogo(final NodeRef ref, final String name, final File file)
            throws CustomizationException, IOException {
        ParameterCheck.mandatory("The taget file", file);
        if (file.exists() == false) {
            throw new IOException("The file must exists.");
        }

        return addLogoImpl(ref, name, file);
    }

    public LogoDefinition addLogo(final NodeRef ref, final String name, final InputStream inputStream)
            throws CustomizationException, IOException {
        ParameterCheck.mandatory("The inputStream", inputStream);

        return addLogoImpl(ref, name, inputStream);
    }

    public List<LogoDefinition> getAllLogos(final NodeRef ref) throws CustomizationException {
        ParameterCheck.mandatory("The configurable node", ref);

        if (logoCache.contains(ref)) {
            return logoCache.get(ref);
        } else {
            final Set<NodeRef> configurationFiles = new HashSet<>();

            NodeRef recursRef = ref;

            do {
                configurationFiles.addAll(
                        nodePreferencesService.getConfigurationFiles(
                                recursRef,
                                CUSTOMIZATION_ROOT_FOLDER,
                                CUSTOMIZATION_ICON_FOLDER,
                                CUSTOMIZATION_ICON_DATA_FOLDER));

                recursRef = nodeService.getPrimaryParent(recursRef).getParentRef();
            }
            // get all icons until the root node
            while (recursRef != null
                    && nodeService.hasAspect(recursRef, CircabcModel.ASPECT_CIRCABC_ROOT) == false);

            final List<LogoDefinition> definitions = new ArrayList<>(configurationFiles.size());

            for (final NodeRef conf : configurationFiles) {
                definitions.add(toLogoDefinition(conf));
            }

            // get the root logo fist.
            Collections.reverse(definitions);

            logoCache.put(ref, definitions);

            return definitions;
        }
    }

    public DefaultLogoConfiguration getDefault(final NodeRef ref) throws CustomizationException {
        ParameterCheck.mandatory("The configurable node", ref);

        if (configCache.contains(ref)) {
            return configCache.get(ref);
        } else {
            final NodeRef configFile =
                    nodePreferencesService.getDefaultConfigurationFile(
                            ref,
                            CUSTOMIZATION_ROOT_FOLDER,
                            CUSTOMIZATION_ICON_FOLDER,
                            CUSTOMIZATION_ICON_CONFIG_FOLDER);

            final DefaultLogoConfigurationImpl configuration = toConfiguration(ref, configFile);

            configCache.put(ref, configuration);

            return configuration;
        }
    }

    public void setDefault(final NodeRef ref, final NodeRef logoRef) throws CustomizationException {
        ParameterCheck.mandatory("The configurable node", ref);
        LogoDefinition logoDefinition = null;

        if (logoRef != null) {
            for (final LogoDefinition logo : getAllLogos(ref)) {
                if (logoRef.equals(logo.getReference())) {
                    logoDefinition = logo;
                    break;
                }
            }

            if (logoDefinition == null) {
                throw new CustomizationException("Logo not found in any parent configuration");
            }
        }

        final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(ref, true);
        config.setLogo((LogoDefinitionImpl) logoDefinition);

        storeConfig(ref, config);
    }

    public void setMainPageLogoConfig(
            final NodeRef ref,
            boolean display,
            int height,
            int width,
            boolean sizeForced,
            boolean logoAtLeft)
            throws CustomizationException {
        ParameterCheck.mandatory("The configurable node", ref);

        final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(ref, true);
        config.setLogoDisplayedOnMainPage(display);
        config.setMainPageLogoHeight(height);
        config.setMainPageLogoWidth(width);
        config.setMainPageSizeForced(sizeForced);
        config.setMainPageLogoAtLeft(logoAtLeft);

        storeConfig(ref, config);
    }

    public void setOtherPagesLogoConfig(
            final NodeRef ref, boolean display, int height, int width, boolean sizeForced)
            throws CustomizationException {
        ParameterCheck.mandatory("The configurable node", ref);

        final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(ref, true);
        config.setLogoDisplayedOnAllPages(display);
        config.setOtherPagesLogoHeight(height);
        config.setOtherPagesLogoWidth(width);
        config.setOtherPagesSizeForced(sizeForced);

        storeConfig(ref, config);
    }

    public void removeLogo(final NodeRef ref, final String logoName) throws CustomizationException {
        logoCache.clear();
        configCache.clear();

        ParameterCheck.mandatory("The configurable node", ref);
        ParameterCheck.mandatoryString("The logo name", logoName);

        final NodeRef config =
                nodePreferencesService.getCustomization(
                        ref,
                        CUSTOMIZATION_ROOT_FOLDER,
                        CUSTOMIZATION_ICON_FOLDER,
                        CUSTOMIZATION_ICON_DATA_FOLDER,
                        logoName);

        if (config == null) {
            throw new CustomizationException("The logo " + logoName + " doesn't exist on node " + ref);
        } else {
            nodePreferencesService.removeCustomization(
                    ref,
                    CUSTOMIZATION_ROOT_FOLDER,
                    CUSTOMIZATION_ICON_FOLDER,
                    CUSTOMIZATION_ICON_DATA_FOLDER,
                    logoName);
        }
    }

    public void removeConfiguration(final NodeRef ref) throws CustomizationException {
        configCache.clear();

        ParameterCheck.mandatory("The configurable node", ref);
        final DefaultLogoConfiguration config = getOrCreateConfiguraton(ref, false);

        if (config == null) {
            throw new CustomizationException("The logo configuration doesn't exist on node " + ref);
        } else {
            nodePreferencesService.removeCustomization(
                    ref,
                    CUSTOMIZATION_ROOT_FOLDER,
                    CUSTOMIZATION_ICON_FOLDER,
                    CUSTOMIZATION_ICON_CONFIG_FOLDER,
                    CONFIG_FILE_NAME);
        }
    }

    // ---------
    // IOC

    /**
     * @param nodePreferencesService the nodePreferencesService to set
     */
    public final void setNodePreferencesService(NodePreferencesService nodePreferencesService) {
        this.nodePreferencesService = nodePreferencesService;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    // ---------
    // helpers

    private void storeConfig(final NodeRef ref, final DefaultLogoConfigurationImpl config) {
        try {
            // the interfate contains only getters
            final Method[] getters = DefaultLogoConfiguration.class.getDeclaredMethods();

            final Properties props = new Properties();
            for (final Method getter : getters) {
                final Object result = getter.invoke(config);
                if (result instanceof LogoDefinition) {
                    final NodeRef logoRef = ((LogoDefinition) result).getReference();
                    props.put(getter.getName(), logoRef == null ? "" : logoRef.toString());
                } else {
                    props.put(getter.getName(), result == null ? "" : result.toString());
                }
            }
            nodePreferencesService.addCustomizationFile(
                    ref,
                    CUSTOMIZATION_ROOT_FOLDER,
                    CUSTOMIZATION_ICON_FOLDER,
                    CUSTOMIZATION_ICON_CONFIG_FOLDER,
                    CONFIG_FILE_NAME,
                    props);

            configCache.put(ref, config);
        } catch (final Exception e) {
            logger.error("Impossible to set values for configuration: " + config.getConfigurationRef());
        }
    }

    private LogoDefinition addLogoImpl(final NodeRef ref, final String name, final Object content)
            throws CustomizationException, IOException {
        logoCache.clear();

        ParameterCheck.mandatory("The configurable node", ref);
        ParameterCheck.mandatory("The logo name", name);

        // test the name unicity
        if (nodePreferencesService.customizationFileExists(
                ref,
                CUSTOMIZATION_ROOT_FOLDER,
                CUSTOMIZATION_ICON_FOLDER,
                CUSTOMIZATION_ICON_DATA_FOLDER,
                name)) {
            throw new CustomizationException("The filename " + name + " is already in use.");
        }

        if (nodePreferencesService.isNodeConfigurable(ref) == false) {
            nodePreferencesService.makeConfigurable(ref);
        }

        final NodeRef customizationRef;
        if (content instanceof InputStream) {
            customizationRef =
                    nodePreferencesService.addCustomizationFile(
                            ref,
                            CUSTOMIZATION_ROOT_FOLDER,
                            CUSTOMIZATION_ICON_FOLDER,
                            CUSTOMIZATION_ICON_DATA_FOLDER,
                            name,
                            (InputStream) content);
        } else {
            customizationRef =
                    nodePreferencesService.addCustomizationFile(
                            ref,
                            CUSTOMIZATION_ROOT_FOLDER,
                            CUSTOMIZATION_ICON_FOLDER,
                            CUSTOMIZATION_ICON_DATA_FOLDER,
                            name,
                            (File) content);
        }

        return toLogoDefinition(customizationRef, ref, name);
    }

    public DefaultLogoConfigurationImpl getOrCreateConfiguraton(
            final NodeRef ref, final boolean createIfMissing) throws CustomizationException {
        try {
            final NodeRef config =
                    nodePreferencesService.getCustomization(
                            ref,
                            CUSTOMIZATION_ROOT_FOLDER,
                            CUSTOMIZATION_ICON_FOLDER,
                            CUSTOMIZATION_ICON_CONFIG_FOLDER,
                            CONFIG_FILE_NAME);

            return toConfiguration(ref, config);
        } catch (CustomizationException ex) {
            if (createIfMissing) {
                configCache.clear();

                try {
                    // Get the parent config and apply the default behaviour.
                    final DefaultLogoConfigurationImpl parentConfig =
                            (DefaultLogoConfigurationImpl) getDefault(ref);

                    if (nodePreferencesService.isNodeConfigurable(ref) == false) {
                        nodePreferencesService.makeConfigurable(ref);
                    }

                    final NodeRef config =
                            nodePreferencesService.addCustomizationFile(
                                    ref,
                                    CUSTOMIZATION_ROOT_FOLDER,
                                    CUSTOMIZATION_ICON_FOLDER,
                                    CUSTOMIZATION_ICON_CONFIG_FOLDER,
                                    CONFIG_FILE_NAME,
                                    "");

                    final DefaultLogoConfigurationImpl newConfig = parentConfig.clone();
                    newConfig.setConfigurationRef(ref, config);

                    return newConfig;
                } catch (CloneNotSupportedException e) {
                    throw new CustomizationException(e);
                }
            } else {
                return null;
            }
        }
    }

    private LogoDefinition toLogoDefinition(final NodeRef customizationRef) {
        final LogoDefinitionImpl definition = new LogoDefinitionImpl();
        definition.setReference(customizationRef);

        fillDefinition(definition);

        return definition;
    }

    /**
     * @param customizationRef
     * @param definition
     * @throws InvalidNodeRefException
     */
    private void fillDefinition(final LogoDefinitionImpl definition) throws InvalidNodeRefException {
        final NodeRef customizationRef = definition.getReference();
        final Map<QName, Serializable> props = nodeService.getProperties(customizationRef);
        definition.setDefinedOn(nodePreferencesService.getCustomizationFromNode(customizationRef));
        definition.setName((String) props.get(ContentModel.PROP_NAME));

        Serializable titleProp = props.get(ContentModel.PROP_TITLE);
        if (titleProp instanceof String) {
            definition.setTitle(titleProp.toString());
        } else if (titleProp instanceof MLText) {
            definition.setTitle(((MLText) titleProp).getDefaultValue());
        }

        Serializable descriptionProp = props.get(ContentModel.PROP_DESCRIPTION);
        if (descriptionProp instanceof String) {
            definition.setDescription(descriptionProp.toString());
        } else if (descriptionProp instanceof MLText) {
            definition.setDescription(((MLText) descriptionProp).getDefaultValue());
        }
    }

    private DefaultLogoConfigurationImpl toConfiguration(
            final NodeRef ref, final NodeRef configurationFile) throws CustomizationException {
        final Properties props = new Properties();
        InputStream contentInputStream = null;
        try {
            contentInputStream = getContentInputStream(configurationFile);
            props.load(contentInputStream);
        } catch (IOException e) {
            throw new CustomizationException("Impossible to read the default icon configuration", e);
        } catch (ContentIOException e) {
            throw new CustomizationException("Impossible to read the default icon configuration", e);
        } finally {
            try {
                if (contentInputStream != null) {
                    contentInputStream.close();
                }
            } catch (IOException e) {
                logger.error(
                        "Impossible to close stream for default icon configuration for ref"
                                + ref.toString()
                                + " configurationFile "
                                + configurationFile.toString(),
                        e);
            }
        }

        final DefaultLogoConfigurationImpl config =
                new DefaultLogoConfigurationImpl(
                        nodePreferencesService.getCustomizationFromNode(configurationFile), configurationFile);
        for (final Map.Entry<Object, Object> entry : props.entrySet()) {
            final String keyStr = (String) entry.getKey();
            final String value = (String) entry.getValue();
            try {
                if (keyStr.equals(METHOD_GET_LOGO)) {
                    // logo can be either the logo name for root config or
                    // nodeRef.toString();
                    if (NodeRef.isNodeRef(value)) {
                        final NodeRef logoRef = new NodeRef(value);
                        // test if exists
                        if (nodeService.exists(logoRef)) {
                            config.setLogo(logoRef);
                        }
                    } else {
                        for (final LogoDefinition def : getAllLogos(ref)) {
                            if (def.getName().equals(value)) {
                                config.setLogo((LogoDefinitionImpl) def);
                                break;
                            }
                        }
                    }
                } else if (keyStr.equals(METHOD_GET_CONFIGURED_ON) == false) {
                    final Method setter =
                            DefaultLogoConfigurationImpl.class.getDeclaredMethod(
                                    keyStr.replaceAll(REGEX_GET_OR_IS, SET_PREFIX), String.class);
                    setter.invoke(config, value);
                }
            } catch (final Exception e) {
                logger.warn(
                        "Impossible to set value for "
                                + keyStr
                                + " setter doens't exist in DefaultLogoConfigurationImpl?");
            }
        }

        if (config.getLogo() != null) {
            fillDefinition((LogoDefinitionImpl) config.getLogo());
        }

        return config;
    }

    private LogoDefinition toLogoDefinition(
            final NodeRef customizationRef, final NodeRef fromNode, final String name) {
        final LogoDefinitionImpl definition = new LogoDefinitionImpl();
        definition.setReference(customizationRef);
        definition.setDefinedOn(fromNode);
        definition.setName(name);

        return definition;
    }

    private InputStream getContentInputStream(final NodeRef resource) {
        // Migration 3.1 -> 3.4.6 - 12/01/2012 - Added the right property type
        // retrieval. If the customization does not exist,
        // it gets it the first time from an Alfresco content object
        // (cm:content), then it is copied to a ci:content
        QName propContent = UtilsCircabc.getPropContent(nodeService.getType(resource));
        final ContentReader contentReader = contentService.getReader(resource, propContent);
        final InputStream contentStream = contentReader.getContentInputStream();
        return contentStream;
    }

    /**
     * @return the configCache
     */
    public final SimpleCache<NodeRef, DefaultLogoConfiguration> getConfigCache() {
        return configCache;
    }

    /**
     * @param configCache the configCache to set
     */
    public final void setConfigCache(SimpleCache<NodeRef, DefaultLogoConfiguration> configCache) {
        this.configCache = configCache;
    }

    /**
     * @return the logoCache
     */
    public final SimpleCache<NodeRef, List<LogoDefinition>> getLogoCache() {
        return logoCache;
    }

    /**
     * @param logoCache the logoCache to set
     */
    public final void setLogoCache(SimpleCache<NodeRef, List<LogoDefinition>> logoCache) {
        this.logoCache = logoCache;
    }

    @Override
    public void forceClearCache() {
        logoCache.clear();
        configCache.clear();
    }
}
