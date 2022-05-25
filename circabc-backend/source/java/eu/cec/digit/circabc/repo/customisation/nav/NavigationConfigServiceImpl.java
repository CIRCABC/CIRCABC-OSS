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
package eu.cec.digit.circabc.repo.customisation.nav;

import eu.cec.digit.circabc.repo.config.source.RepoRefConfigSource;
import eu.cec.digit.circabc.service.customisation.nav.ColumnConfig;
import eu.cec.digit.circabc.service.customisation.nav.NavigationConfigService;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import org.alfresco.repo.config.xml.RepoXMLConfigService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.config.ActionsConfigElement;
import org.alfresco.web.config.ActionsConfigElement.ActionGroup;
import org.springframework.extensions.config.*;
import org.springframework.extensions.config.xml.XMLConfigService;

import java.text.MessageFormat;
import java.util.*;

/**
 * Implementation of the MailPreferencesService.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. ConfigException was moved to Spring. ConfigLookupContext was moved to Spring.
 * ConfigService was moved to Spring. XMLConfigService was moved to Spring. This class seems to
 * be developed for CircaBC
 */
public class NavigationConfigServiceImpl implements NavigationConfigService {

    private static final String CONFIG_AREA = "navigation-custom";
    private static final String GLOBAL_CONFIG_CONDITION = "Global Navigation Preference";
    private static final String CUSTOM_CONFIG_CONDITION = "Custom Navigation Preference";

    private static final String ELEMENT_COLUMNS = "columns";
    private static final String ELEMENT_SERVICES = "services";
    private static final String ELEMENT_PREFERENCE = "preference";

    private static final String ELEMENT_ACTION = "action";
    private static final String ELEMENT_ACTIONS = "actions";
    private static final String ELEMENT_COLUMN = "column";
    private static final String ELEMENT_DISPLAY_ACTION = "displayAction";
    private static final String ELEMENT_DISPLAY_COL = "displayCol";
    private static final String ELEMENT_DISPLAY_ROW = "displayRow";
    private static final String ELEMENT_RESOLVER = "resolver";
    private static final String ELEMENT_CONVERTER = "converter";
    private static final String ELEMENT_LABEL = "label";
    private static final String ELEMENT_ACTION_CONFIG = "actionConfig";
    private static final String ELEMENT_ROW_VIEW = "rowView";
    private static final String ELEMENT_DISPLAY_ACTIONS = "displayActions";
    private static final String ELEMENT_LINK_DESTINATION = "linkDestination";
    private static final String ELEMENT_VIEW_MODE = "viewMode";
    private static final String ELEMENT_PREVIEWACTION = "previewAction";
    private static final String ELEMENT_INITIAL_SORT_COLUMN = "initialSortColumn";
    private static final String ELEMENT_INITIAL_SORT_DESC = "initialSortDescending";
    private static final String ELEMENT_INITIAL_SORT_POSTS_DESC = "initialSortPostsDescending";
    private static final String ELEMENT_MANDATORY_ACTION = "mandatoryAction";
    private static final String ELEMENT_ALLOW_BULK = "allowBulkOperation";
    private static final String ELEMENT_RENDER_PROPERTY_NAME = "navigationListRenderType";

    private static final String ATTR_MIN = "min";
    private static final String ATTR_MAX = "max";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ID = "id";
    // dirty solution, but we need performance
    private static final String PREFERENCE_XML =
            "<alfresco-config area=\"navigation-custom\"> \n"
                    + "	<config evaluator=\"string-compare\" condition=\"Custom Navigation Preference\"> \n"
                    + "   	<preference> \n"
                    + "	    	<rowView>{0}</rowView> \n"
                    + "	    	<displayActions>{1}</displayActions> \n"
                    + "	    	<linkDestination>{2}</linkDestination> \n"
                    + "	    	<viewMode>{3}</viewMode> \n"
                    + "			<actions> \n"
                    + "				{4} \n"
                    + "			</actions> \n"
                    + "			<initialSortColumn>{5}</initialSortColumn> \n"
                    + "			<initialSortDescending>{6}</initialSortDescending> \n"
                    + "			<columns> \n"
                    + "				{7} \n"
                    + "			</columns> \n"
                    + "	    	<navigationListRenderType>{8}</navigationListRenderType> \n"
                    + "	    	<previewAction>{9}</previewAction> \n"
                    + "			<initialSortPostsDescending>{10}</initialSortPostsDescending> \n"
                    + "		</preference> \n"
                    + "	</config> \n"
                    + "</alfresco-config>";
    private static final String COLUMN_LINE_XML = "				<column>{0}</column> \n";
    private static final String ACTION_LINE_XML = "				<action>{0}</action> \n";
    private static boolean init = false;
    private Map<String, ColumnConfig> columns = null;
    private Map<String, ServiceConfig> services = null;
    private ContentService contentService;
    private NodeService nodeService;
    private ConfigService configService;
    private RepoXMLConfigService webClientConfig;

    /**
     * @return the elementRenderPropertyName
     */
    public static String getElementRenderPropertyName() {
        return ELEMENT_RENDER_PROPERTY_NAME;
    }

    /**
     * Initialises the map using the configuration service provided
     */
    public synchronized void init() {
        if (!init) {
            try {
                columns = new HashMap<>();
                services = new HashMap<>();

                final ConfigLookupContext clContext = new ConfigLookupContext(CONFIG_AREA);
                final Config configConditions = configService.getConfig(GLOBAL_CONFIG_CONDITION, clContext);

                final Map<String, ConfigElement> configElements = configConditions.getConfigElements();

                if (configElements.containsKey(ELEMENT_COLUMNS) == false
                        || configElements.containsKey(ELEMENT_SERVICES) == false) {
                    throw new ConfigException(
                            "The elements " + ELEMENT_COLUMNS + " AND " + ELEMENT_SERVICES + " are mandatory");
                }

                ColumnConfigImpl column = null;
                for (final ConfigElement columnElement :
                        configElements.get(ELEMENT_COLUMNS).getChildren()) {
                    final String colName = columnElement.getAttribute(ATTR_NAME);

                    column = new ColumnConfigImpl(colName);
                    column.setLabel(columnElement.getChildValue(ELEMENT_LABEL));
                    column.setConverter(columnElement.getChildValue(ELEMENT_CONVERTER));
                    column.setResolver(columnElement.getChildValue(ELEMENT_RESOLVER));

                    columns.put(colName, column);
                }

                ServiceConfigImpl service = null;
                for (final ConfigElement serviceElement :
                        configElements.get(ELEMENT_SERVICES).getChildren()) {
                    final String serviceName = serviceElement.getAttribute(ATTR_NAME);
                    final String serviceType = serviceElement.getAttribute(ATTR_TYPE);

                    service = new ServiceConfigImpl(serviceName, serviceType);

                    final Pair<Integer, Integer> displayRow =
                            getMinMax(serviceElement.getChild(ELEMENT_DISPLAY_ROW));
                    final Pair<Integer, Integer> displayCol =
                            getMinMax(serviceElement.getChild(ELEMENT_DISPLAY_COL));
                    final Pair<Integer, Integer> displayAct =
                            getMinMax(serviceElement.getChild(ELEMENT_DISPLAY_ACTION));
                    final String mandatoryAction = serviceElement.getChildValue(ELEMENT_MANDATORY_ACTION);

                    service.setDisplayActionMin(displayAct.getFirst());
                    service.setDisplayActionMax(displayAct.getSecond());
                    service.setDisplayColMin(displayCol.getFirst());
                    service.setDisplayColMax(displayCol.getSecond());
                    service.setDisplayRowMin(displayRow.getFirst());
                    service.setDisplayRowMax(displayRow.getSecond());
                    service.setActionConfigName(serviceElement.getChildValue(ELEMENT_ACTION_CONFIG));
                    service.setMandatoryAction(mandatoryAction);
                    service.setBulkOperationAllowed(
                            Boolean.valueOf(serviceElement.getChildValue(ELEMENT_ALLOW_BULK)));

                    final List<String> actions = extractActions(serviceElement);
                    service.addAction(actions);

                    if (mandatoryAction != null
                            && mandatoryAction.length() > 0
                            && actions.contains(mandatoryAction) == false) {
                        throw new ConfigException(
                                "Problem in "
                                        + serviceName
                                        + ":"
                                        + serviceType
                                        + " configuration. Mandatory action ("
                                        + mandatoryAction
                                        + ") not present in the available actions ( "
                                        + actions
                                        + ").");
                    }

                    final ConfigElement columnsElement = serviceElement.getChild(ELEMENT_COLUMNS);
                    if (columnsElement != null) {
                        for (final ConfigElement columnElement : columnsElement.getChildren()) {
                            final String colName = columnElement.getAttribute(ATTR_NAME);
                            final String id = columnElement.getAttribute(ATTR_ID);

                            if (columns.containsKey(colName)) {
                                service.addColumn(columns.get(colName));

                                if (id != null && id.equalsIgnoreCase(Boolean.TRUE.toString())) {
                                    service.addKeyColumn(columns.get(colName));
                                }
                            } else {
                                throw new IllegalStateException(
                                        "The column "
                                                + colName
                                                + " doesn't exist in the avalibalbe columns: "
                                                + columns.keySet());
                            }
                        }
                    }

                    this.services.put(buildServiceKey(serviceName, serviceType), service);
                }

                // make the collections read-only
                this.columns = Collections.unmodifiableMap(this.columns);
                this.services = Collections.unmodifiableMap(this.services);
                init = true;
            } catch (final Exception e) {
                throw new ConfigException(
                        "Impossible to apply default navigation customization settings: " + e.getMessage(), e);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.nav.NavigationConfigService#getAllServiceConfig()
     */
    public Collection<ServiceConfig> getAllServiceConfig() {
        if (!init) {
            init();
        }
        return services.values();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.nav.NavigationConfigService#getServiceConfig(java.lang.String, java.lang.String)
     */
    public ServiceConfig getServiceConfig(final String serviceName, final String nodeType) {
        ParameterCheck.mandatoryString("The service name", serviceName);
        ParameterCheck.mandatoryString("The node type ", nodeType);
        if (!init) {
            init();
        }
        return services.get(buildServiceKey(serviceName, nodeType));
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.nav.NavigationConfigService#buildPreference(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NavigationPreference buildPreference(
            final String serviceName, final String serviceType, final NodeRef ref) {
        ParameterCheck.mandatoryString("The service name", serviceName);
        ParameterCheck.mandatoryString("The service type", serviceType);
        ParameterCheck.mandatory("The nodeRef ", ref);
        if (!init) {
            init();
        }

        final RepoRefConfigSource repoRefConfigSource =
                new RepoRefConfigSource(ref, contentService, nodeService);
        final XMLConfigService refConfigService = new XMLConfigService(repoRefConfigSource);

        refConfigService.initConfig();
        final Config configConditions =
                refConfigService.getConfig(CUSTOM_CONFIG_CONDITION, new ConfigLookupContext(CONFIG_AREA));

        final Map<String, ConfigElement> configElements = configConditions.getConfigElements();

        if (configElements.containsKey(ELEMENT_PREFERENCE) == false) {
            throw new ConfigException("The element " + ELEMENT_PREFERENCE + " is mandatory");
        }

        final String serviceKey = buildServiceKey(serviceName, serviceType);
        final NavigationPreferenceImpl navigationOption =
                new NavigationPreferenceImpl(services.get(serviceKey));

        final ConfigElement preference = configElements.get(ELEMENT_PREFERENCE);

        navigationOption.setActions(extractActions(preference));
        navigationOption.setColumns(extractColumns(preference));
        navigationOption.setDisplayActionColumnStr(preference.getChildValue(ELEMENT_DISPLAY_ACTIONS));
        navigationOption.setLinkTarget(preference.getChildValue(ELEMENT_LINK_DESTINATION));
        navigationOption.setListSizeStr(preference.getChildValue(ELEMENT_ROW_VIEW));
        navigationOption.setViewMode(preference.getChildValue(ELEMENT_VIEW_MODE));
        navigationOption.setPreviewAction(preference.getChildValue(ELEMENT_PREVIEWACTION));
        navigationOption.setInitialSortColumnStr(preference.getChildValue(ELEMENT_INITIAL_SORT_COLUMN));
        navigationOption.setInitialSortDescendingStr(
                preference.getChildValue(ELEMENT_INITIAL_SORT_DESC));
        navigationOption.setInitialSortPostsDescendingStr(
                preference.getChildValue(ELEMENT_INITIAL_SORT_POSTS_DESC));
        navigationOption.setRenderPropertyName(preference.getChildValue(ELEMENT_RENDER_PROPERTY_NAME));
        return navigationOption;
    }

    // ---------
    // -- Helpers

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.nav.NavigationConfigService#buildPreferenceXML(eu.cec.digit.circabc.service.customisation.nav.NavigationPreference)
     */
    public String buildPreferenceXML(final NavigationPreference navigationPreference) {
        if (!init) {
            init();
        }
        return MessageFormat.format(
                PREFERENCE_XML,
                navigationPreference.getListSize(),
                navigationPreference.isDisplayActionColumn(),
                navigationPreference.getLinkTarget(),
                navigationPreference.getViewMode(),
                xmlActions(navigationPreference.getActions()),
                safeColumnName(navigationPreference.getInitialSortColumn()),
                navigationPreference.isInitialSortDescending(),
                xmlColumns(navigationPreference.getColumns()),
                navigationPreference.getRenderPropertyName(),
                navigationPreference.getPreviewAction(),
                navigationPreference.isInitialSortPostsDescending());
    }

    private String buildServiceKey(final String name, final String type) {
        return name + "_" + type;
    }

    private Pair<Integer, Integer> getMinMax(final ConfigElement element) {
        int min = 0;
        int max = -1;

        if (element != null && element.hasAttribute(ATTR_MIN)) {
            min = Integer.parseInt(element.getAttribute(ATTR_MIN));
        }
        if (element != null && element.hasAttribute(ATTR_MAX)) {
            max = Integer.parseInt(element.getAttribute(ATTR_MAX));
        }

        return new Pair<>(min, max);
    }

    private List<String> extractActions(final ConfigElement parentElement) {
        final String actionConfigName = parentElement.getChildValue(ELEMENT_ACTION_CONFIG);
        final ConfigElement actionsElement = parentElement.getChild(ELEMENT_ACTIONS);
        final List<String> actions = new ArrayList<>();

        if (actionConfigName != null && actionConfigName.length() > 0) {
            final Config globalConfig = webClientConfig.getGlobalConfig();

            //  find the Actions specific config element
            final ActionsConfigElement actionConfig =
                    (ActionsConfigElement)
                            globalConfig.getConfigElement(ActionsConfigElement.CONFIG_ELEMENT_ID);
            if (actionConfig != null) {
                // and lookup our ActionGroup by Id
                final ActionGroup actionGroup = actionConfig.getActionGroup(actionConfigName);
                for (String actionName : actionGroup) {
                    if (actions.contains(actionName) == false) {
                        actions.add(actionName);
                    }
                }
            }
        }
        if (actionsElement != null) {
            for (final ConfigElement actionElement : actionsElement.getChildren(ELEMENT_ACTION)) {
                final String value = actionElement.getValue();
                if (value != null && value.length() > 0 && actions.contains(value) == false) {
                    actions.add(value);
                }
            }
        }

        return actions;
    }

    private List<ColumnConfig> extractColumns(final ConfigElement parentElement) {
        final ConfigElement columnElements = parentElement.getChild(ELEMENT_COLUMNS);
        final List<ColumnConfig> columnConfigs = new ArrayList<>();

        if (columnElements != null) {
            for (final ConfigElement columnElement : columnElements.getChildren(ELEMENT_COLUMN)) {
                final String value = columnElement.getValue();
                if (value != null && value.length() > 0) {
                    if (!init) {
                        init();
                    }

                    final ColumnConfig columnConfig = columns.get(value);

                    if (columns.containsKey(value) && columnConfigs.contains(columnConfig) == false) {
                        columnConfigs.add(columnConfig);
                    } else {
                        throw new ConfigException("The column " + value + " not found in the global config.");
                    }
                }
            }
        }

        return columnConfigs;
    }

    private String xmlActions(final List<String> actions) {
        final StringBuilder builder = new StringBuilder("");
        if (actions != null) {
            for (final String act : actions) {
                builder.append(MessageFormat.format(ACTION_LINE_XML, act));
            }
        }
        return builder.toString();
    }

    private String xmlColumns(final List<ColumnConfig> columns) {
        final StringBuilder builder = new StringBuilder("");
        if (columns != null) {
            for (final ColumnConfig col : columns) {
                builder.append(MessageFormat.format(COLUMN_LINE_XML, col.getName()));
            }
        }
        return builder.toString();
    }

    // ---------
    // -- IOC

    private String safeColumnName(final ColumnConfig column) {
        if (column == null) {
            return "";
        } else {
            return column.getName();
        }
    }

    /**
     * @param configService the configService to set
     */
    public final void setConfigService(final ConfigService configService) {
        this.configService = configService;
    }

    /**
     * @param webClientConfig the webClientConfig to set
     */
    public final void setWebClientConfig(final RepoXMLConfigService webClientConfig) {
        this.webClientConfig = webClientConfig;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
