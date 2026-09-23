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
package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import io.swagger.exception.CustomizationException;
import io.swagger.model.Util;
import io.swagger.model.alfresco.CircabcModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Concrete implementation of {@link LogoPreferencesService}.
 *
 * <p>Manages the per-node customisation of Interest Group logos within the Alfresco repository.
 * Logos and their display configuration are stored as customisation files under a dedicated folder
 * structure ({@code iglookAndFeel/icon/...}) attached to configurable nodes, delegating the actual
 * storage and retrieval of those files to the {@link NodePreferencesService}. This service supports
 * adding and removing logos, resolving the effective set of logos visible to a node (including
 * those inherited from its parents up to the CIRCABC root), and reading/writing the default logo
 * display configuration (main page and other pages sizing, positioning and visibility).
 *
 * <p>The logo configuration is persisted as a {@code config.properties} file whose keys are derived
 * from the getter names of {@link DefaultLogoConfiguration} (reflection-based (de)serialisation).
 * Two Alfresco {@link SimpleCache} instances are used to avoid repeated repository traversals: one
 * for the resolved list of logos and one for the resolved configuration.
 *
 * @author Yanick Pignot
 */
public class LogoPreferencesServiceImpl implements LogoPreferencesService {

  /** Message used by parameter checks for the mandatory configurable node argument. */
  private static final String THE_CONFIGURABLE_NODE = "The configurable node";
  /** Name of the {@link DefaultLogoConfiguration} getter that is skipped during persistence. */
  private static final String METHOD_GET_CONFIGURED_ON = "getConfiguredOn";
  /** Prefix of a JavaBean setter, used when mapping a stored property key back to a setter. */
  private static final String SET_PREFIX = "set";
  /** Name of the getter identifying the logo property in the stored configuration. */
  private static final String METHOD_GET_LOGO = "getLogo";
  /** Regular expression matching the {@code is}/{@code get} getter prefixes to be replaced. */
  private static final String REGEX_GET_OR_IS = "^is|^get";

  private static final Log logger = LogFactory.getLog(
    LogoPreferencesServiceImpl.class
  );

  /** Root folder of the look-and-feel customisation tree attached to a node. */
  private static final String CUSTOMIZATION_ROOT_FOLDER = "iglookAndFeel";
  /** Sub-folder grouping all icon/logo related customisation. */
  private static final String CUSTOMIZATION_ICON_FOLDER = "icon";
  /** Sub-folder holding the logo configuration file. */
  private static final String CUSTOMIZATION_ICON_CONFIG_FOLDER = "definition";
  /** Sub-folder holding the logo image files. */
  private static final String CUSTOMIZATION_ICON_DATA_FOLDER = "images";

  /** File name under which the logo display configuration is persisted. */
  private static final String CONFIG_FILE_NAME = "config.properties";

  @Autowired
  private NodePreferencesService nodePreferencesService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private ContentService contentService;

  /** Cache of the resolved list of logos (including inherited ones) keyed by configurable node. */
  @Autowired
  @Qualifier("customization.logo.imagesCache")
  private SimpleCache<NodeRef, List<LogoDefinition>> logoCache;

  /** Cache of the resolved default logo configuration keyed by configurable node. */
  @Autowired
  @Qualifier("customization.logo.configCache")
  private SimpleCache<NodeRef, DefaultLogoConfiguration> configCache;

  /**
   * Adds a new logo, sourced from a file, for the given node and all of its children.
   *
   * <p>The file must exist on disk; setting the logo as the default one is a separate operation
   * (see {@link #setDefault(NodeRef, NodeRef)}).
   *
   * @param ref  the configurable node on which the logo is attached
   * @param name the mandatory and unique name of the logo
   * @param file the file holding the logo content
   * @return the newly created logo definition
   * @throws CustomizationException if the name is missing or already in use
   * @throws IOException            if the file does not exist or any I/O error occurs
   */
  public LogoDefinition addLogo(
    final NodeRef ref,
    final String name,
    final File file
  ) throws CustomizationException, IOException {
    ParameterCheck.mandatory("The taget file", file);
    if (!file.exists()) {
      throw new IOException("The file must exists.");
    }

    return addLogoImpl(ref, name, file);
  }

  /**
   * Adds a new logo, sourced from an input stream, for the given node and all of its children.
   *
   * <p>Setting the logo as the default one is a separate operation
   * (see {@link #setDefault(NodeRef, NodeRef)}).
   *
   * @param ref         the configurable node on which the logo is attached
   * @param name        the mandatory and unique name of the logo
   * @param inputStream the stream holding the logo content
   * @return the newly created logo definition
   * @throws CustomizationException if the name is missing or already in use
   * @throws IOException            if any I/O error occurs
   */
  public LogoDefinition addLogo(
    final NodeRef ref,
    final String name,
    final InputStream inputStream
  ) throws CustomizationException, IOException {
    ParameterCheck.mandatory("The inputStream", inputStream);

    return addLogoImpl(ref, name, inputStream);
  }

  /**
   * Returns all logos defined for the given node, including those inherited from its parents up to
   * the CIRCABC root node.
   *
   * <p>The result is cached per node; on a cache miss the parent hierarchy is traversed and the
   * resolved list is stored ordered from the root node down.
   *
   * @param ref the configurable node whose logos are resolved
   * @return the list of logo definitions visible to the node (never {@code null})
   * @throws CustomizationException if the reference is missing or the customisation cannot be read
   */
  public List<LogoDefinition> getAllLogos(final NodeRef ref)
    throws CustomizationException {
    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);

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
            CUSTOMIZATION_ICON_DATA_FOLDER
          )
        );

        recursRef = nodeService.getPrimaryParent(recursRef).getParentRef();
      } while ( // get all icons until the root node
        recursRef != null &&
        !nodeService.hasAspect(recursRef, CircabcModel.ASPECT_CIRCABC_ROOT)
      );

      final List<LogoDefinition> definitions = new ArrayList<>(
        configurationFiles.size()
      );

      for (final NodeRef conf : configurationFiles) {
        definitions.add(toLogoDefinition(conf));
      }

      // get the root logo fist.
      Collections.reverse(definitions);

      logoCache.put(ref, definitions);

      return definitions;
    }
  }

  /**
   * Returns the effective default logo configuration for the given node.
   *
   * <p>The configuration is resolved from the node's own customisation (or inherited from its
   * parents) and cached per node.
   *
   * @param ref the configurable node whose default configuration is resolved
   * @return the resolved default logo configuration
   * @throws CustomizationException if the reference is missing or the configuration cannot be read
   */
  public DefaultLogoConfiguration getDefault(final NodeRef ref)
    throws CustomizationException {
    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);

    if (configCache.contains(ref)) {
      return configCache.get(ref);
    } else {
      final NodeRef configFile =
        nodePreferencesService.getDefaultConfigurationFile(
          ref,
          CUSTOMIZATION_ROOT_FOLDER,
          CUSTOMIZATION_ICON_FOLDER,
          CUSTOMIZATION_ICON_CONFIG_FOLDER
        );

      final DefaultLogoConfigurationImpl configuration = toConfiguration(
        ref,
        configFile
      );

      configCache.put(ref, configuration);

      return configuration;
    }
  }

  /**
   * Sets the given logo as the default one for the node, or clears the default when {@code logoRef}
   * is {@code null}.
   *
   * <p>When a logo reference is supplied it must belong to the set returned by
   * {@link #getAllLogos(NodeRef)}; otherwise a {@link CustomizationException} is raised. The updated
   * configuration is persisted.
   *
   * @param ref     the configurable node on which the default logo is set
   * @param logoRef the reference of the logo to set as default, or {@code null} to clear it
   * @throws CustomizationException if the reference is missing or the logo is not found in any
   *                                parent configuration
   */
  public void setDefault(final NodeRef ref, final NodeRef logoRef)
    throws CustomizationException {
    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);
    LogoDefinition logoDefinition = null;

    if (logoRef != null) {
      for (final LogoDefinition logo : getAllLogos(ref)) {
        if (logoRef.equals(logo.getReference())) {
          logoDefinition = logo;
          break;
        }
      }

      if (logoDefinition == null) {
        throw new CustomizationException(
          "Logo not found in any parent configuration"
        );
      }
    }

    final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(
      ref,
      true
    );
    config.setLogo((LogoDefinitionImpl) logoDefinition);

    storeConfig(ref, config);
  }

  /**
   * Defines how the default logo must be displayed on the main page (Interest Group home) and
   * persists the updated configuration.
   *
   * @param ref        the configurable node on which the logo is configured
   * @param display    whether the logo is displayed on the main page
   * @param height     the height of the logo
   * @param width      the width of the logo
   * @param sizeForced whether the given sizes are forced ({@code true}) or treated as maximum
   * @param logoAtLeft whether the logo is displayed at the left ({@code true}) or the right
   * @throws CustomizationException if the reference is missing or the configuration cannot be stored
   */
  public void setMainPageLogoConfig(
    final NodeRef ref,
    boolean display,
    int height,
    int width,
    boolean sizeForced,
    boolean logoAtLeft
  ) throws CustomizationException {
    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);

    final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(
      ref,
      true
    );
    config.setLogoDisplayedOnMainPage(display);
    config.setMainPageLogoHeight(height);
    config.setMainPageLogoWidth(width);
    config.setMainPageSizeForced(sizeForced);
    config.setMainPageLogoAtLeft(logoAtLeft);

    storeConfig(ref, config);
  }

  /**
   * Defines whether and how the default logo must be displayed on all pages other than the Interest
   * Group home, and persists the updated configuration.
   *
   * @param ref        the configurable node on which the logo is configured
   * @param display    whether the logo is displayed on the other pages
   * @param height     the height of the logo
   * @param width      the width of the logo
   * @param sizeForced whether the given sizes are forced ({@code true}) or treated as maximum
   * @throws CustomizationException if the reference is missing or the configuration cannot be stored
   */
  public void setOtherPagesLogoConfig(
    final NodeRef ref,
    boolean display,
    int height,
    int width,
    boolean sizeForced
  ) throws CustomizationException {
    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);

    final DefaultLogoConfigurationImpl config = getOrCreateConfiguraton(
      ref,
      true
    );
    config.setLogoDisplayedOnAllPages(display);
    config.setOtherPagesLogoHeight(height);
    config.setOtherPagesLogoWidth(width);
    config.setOtherPagesSizeForced(sizeForced);

    storeConfig(ref, config);
  }

  /**
   * Removes a named logo from the given node. Both caches are cleared beforehand.
   *
   * @param ref      the configurable node from which the logo is removed
   * @param logoName the name of the logo to remove
   * @throws CustomizationException if the reference or name is missing, or the logo does not exist
   *                                on the node
   */
  public void removeLogo(final NodeRef ref, final String logoName)
    throws CustomizationException {
    logoCache.clear();
    configCache.clear();

    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);
    ParameterCheck.mandatoryString("The logo name", logoName);

    final NodeRef config = nodePreferencesService.getCustomization(
      ref,
      CUSTOMIZATION_ROOT_FOLDER,
      CUSTOMIZATION_ICON_FOLDER,
      CUSTOMIZATION_ICON_DATA_FOLDER,
      logoName
    );

    if (config == null) {
      throw new CustomizationException(
        "The logo " + logoName + " doesn't exist on node " + ref
      );
    } else {
      nodePreferencesService.removeCustomization(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_DATA_FOLDER,
        logoName
      );
    }
  }

  /**
   * Removes the logo configuration of the given node so that default (inherited) settings apply
   * again. The configuration cache is cleared beforehand.
   *
   * @param ref the configurable node whose configuration is removed
   * @throws CustomizationException if the reference is missing or the configuration does not exist
   *                                on the node
   */
  public void removeConfiguration(final NodeRef ref)
    throws CustomizationException {
    configCache.clear();

    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);
    final DefaultLogoConfiguration config = getOrCreateConfiguraton(ref, false);

    if (config == null) {
      throw new CustomizationException(
        "The logo configuration doesn't exist on node " + ref
      );
    } else {
      nodePreferencesService.removeCustomization(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_CONFIG_FOLDER,
        CONFIG_FILE_NAME
      );
    }
  }

  // ---------
  // IOC

  /**
   * @param nodePreferencesService the nodePreferencesService to set
   */
  public final void setNodePreferencesService(
    NodePreferencesService nodePreferencesService
  ) {
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

  private void storeConfig(
    final NodeRef ref,
    final DefaultLogoConfigurationImpl config
  ) {
    try {
      // the interfate contains only getters
      final Method[] getters =
        DefaultLogoConfiguration.class.getDeclaredMethods();

      final Properties props = new Properties();
      for (final Method getter : getters) {
        final Object result = getter.invoke(config);
        if (result instanceof LogoDefinition logoDefinition) {
          final NodeRef logoRef = logoDefinition.getReference();
          props.put(
            getter.getName(),
            logoRef == null ? "" : logoRef.toString()
          );
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
        props
      );

      configCache.put(ref, config);
    } catch (final Exception e) {
      logger.error(
        "Impossible to set values for configuration: " +
          config.getConfigurationRef()
      );
    }
  }

  private LogoDefinition addLogoImpl(
    final NodeRef ref,
    final String name,
    final Object content
  ) throws CustomizationException {
    logoCache.clear();

    ParameterCheck.mandatory(THE_CONFIGURABLE_NODE, ref);
    ParameterCheck.mandatory("The logo name", name);

    // test the name unicity
    if (
      nodePreferencesService.customizationFileExists(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_DATA_FOLDER,
        name
      )
    ) {
      throw new CustomizationException(
        "The filename " + name + " is already in use."
      );
    }

    if (!nodePreferencesService.isNodeConfigurable(ref)) {
      nodePreferencesService.makeConfigurable(ref);
    }

    final NodeRef customizationRef;
    if (content instanceof InputStream inputStream) {
      customizationRef = nodePreferencesService.addCustomizationFile(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_DATA_FOLDER,
        name,
        inputStream
      );
    } else {
      customizationRef = nodePreferencesService.addCustomizationFile(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_DATA_FOLDER,
        name,
        (File) content
      );
    }

    return toLogoDefinition(customizationRef, ref, name);
  }

  /**
   * Returns the logo configuration stored directly on the given node, optionally creating it when
   * missing.
   *
   * <p>When no configuration exists on the node and {@code createIfMissing} is {@code true}, a new
   * configuration file is created by copying the effective (inherited) default configuration; when
   * {@code false}, {@code null} is returned instead.
   *
   * @param ref             the configurable node whose own configuration is resolved
   * @param createIfMissing whether to create the configuration when it does not yet exist
   * @return the node's logo configuration, or {@code null} if none exists and creation was not
   *         requested
   * @throws CustomizationException if the configuration cannot be read or created
   */
  public DefaultLogoConfigurationImpl getOrCreateConfiguraton(
    final NodeRef ref,
    final boolean createIfMissing
  ) throws CustomizationException {
    try {
      final NodeRef config = nodePreferencesService.getCustomization(
        ref,
        CUSTOMIZATION_ROOT_FOLDER,
        CUSTOMIZATION_ICON_FOLDER,
        CUSTOMIZATION_ICON_CONFIG_FOLDER,
        CONFIG_FILE_NAME
      );

      return toConfiguration(ref, config);
    } catch (CustomizationException ex) {
      if (createIfMissing) {
        configCache.clear();

        // Get the parent config and apply the default behaviour.
        final DefaultLogoConfigurationImpl parentConfig =
          (DefaultLogoConfigurationImpl) getDefault(ref);

        if (!nodePreferencesService.isNodeConfigurable(ref)) {
          nodePreferencesService.makeConfigurable(ref);
        }

        final NodeRef config = nodePreferencesService.addCustomizationFile(
          ref,
          CUSTOMIZATION_ROOT_FOLDER,
          CUSTOMIZATION_ICON_FOLDER,
          CUSTOMIZATION_ICON_CONFIG_FOLDER,
          CONFIG_FILE_NAME,
          ""
        );

        final DefaultLogoConfigurationImpl newConfig = parentConfig.copy();
        newConfig.setConfigurationRef(ref, config);

        return newConfig;
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
  private void fillDefinition(final LogoDefinitionImpl definition)
    throws InvalidNodeRefException {
    final NodeRef customizationRef = definition.getReference();
    final Map<QName, Serializable> props = nodeService.getProperties(
      customizationRef
    );
    definition.setDefinedOn(
      nodePreferencesService.getCustomizationFromNode(customizationRef)
    );
    definition.setName((String) props.get(ContentModel.PROP_NAME));

    Serializable titleProp = props.get(ContentModel.PROP_TITLE);
    if (titleProp instanceof String s) {
      definition.setTitle(s);
    } else if (titleProp instanceof MLText mltext) {
      definition.setTitle(mltext.getDefaultValue());
    }

    Serializable descriptionProp = props.get(ContentModel.PROP_DESCRIPTION);
    if (descriptionProp instanceof String s) {
      definition.setDescription(s);
    } else if (descriptionProp instanceof MLText mltext) {
      definition.setDescription(mltext.getDefaultValue());
    }
  }

  private DefaultLogoConfigurationImpl toConfiguration(
    final NodeRef ref,
    final NodeRef configurationFile
  ) throws CustomizationException {
    Properties props = loadConfigurationProperties(configurationFile);
    DefaultLogoConfigurationImpl config = new DefaultLogoConfigurationImpl(
      nodePreferencesService.getCustomizationFromNode(configurationFile),
      configurationFile
    );
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      applyConfigProperty(
        config,
        ref,
        (String) entry.getKey(),
        (String) entry.getValue()
      );
    }
    if (config.getLogo() != null) {
      fillDefinition((LogoDefinitionImpl) config.getLogo());
    }
    return config;
  }

  private Properties loadConfigurationProperties(NodeRef configurationFile)
    throws CustomizationException {
    Properties props = new Properties();
    try (
      InputStream contentInputStream = getContentInputStream(configurationFile)
    ) {
      props.load(contentInputStream);
    } catch (IOException | ContentIOException e) {
      throw new CustomizationException(
        "Impossible to read the default icon configuration",
        e
      );
    }
    return props;
  }

  private void applyConfigProperty(
    DefaultLogoConfigurationImpl config,
    NodeRef ref,
    String keyStr,
    String value
  ) {
    try {
      if (keyStr.equals(METHOD_GET_LOGO)) {
        setLogoFromValue(config, ref, value);
      } else if (!keyStr.equals(METHOD_GET_CONFIGURED_ON)) {
        Method setter = DefaultLogoConfigurationImpl.class.getDeclaredMethod(
          keyStr.replaceAll(REGEX_GET_OR_IS, SET_PREFIX),
          String.class
        );
        setter.invoke(config, value);
      }
    } catch (Exception e) {
      logger.warn(
        "Impossible to set value for " +
          keyStr +
          " setter doens't exist in DefaultLogoConfigurationImpl?"
      );
    }
  }

  private void setLogoFromValue(
    DefaultLogoConfigurationImpl config,
    NodeRef ref,
    String value
  ) throws CustomizationException {
    if (NodeRef.isNodeRef(value)) {
      NodeRef logoRef = new NodeRef(value);
      if (nodeService.exists(logoRef)) {
        config.setLogo(logoRef);
      }
    } else {
      for (LogoDefinition def : getAllLogos(ref)) {
        if (def.getName().equals(value)) {
          config.setLogo((LogoDefinitionImpl) def);
          break;
        }
      }
    }
  }

  private LogoDefinition toLogoDefinition(
    final NodeRef customizationRef,
    final NodeRef fromNode,
    final String name
  ) {
    final LogoDefinitionImpl definition = new LogoDefinitionImpl();
    definition.setReference(customizationRef);
    definition.setDefinedOn(fromNode);
    definition.setName(name);

    return definition;
  }

  private InputStream getContentInputStream(final NodeRef resource) {
    QName propContent = Util.getPropContent(nodeService.getType(resource));
    final ContentReader contentReader = contentService.getReader(
      resource,
      propContent
    );
    return contentReader.getContentInputStream();
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
  public final void setConfigCache(
    SimpleCache<NodeRef, DefaultLogoConfiguration> configCache
  ) {
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
  public final void setLogoCache(
    SimpleCache<NodeRef, List<LogoDefinition>> logoCache
  ) {
    this.logoCache = logoCache;
  }

  /**
   * Clears both the logo list cache and the logo configuration cache, forcing subsequent lookups to
   * be re-resolved from the repository.
   */
  @Override
  public void forceClearCache() {
    logoCache.clear();
    configCache.clear();
  }
}
