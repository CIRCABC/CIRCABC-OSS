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
package eu.cec.digit.circabc.web.wai.dialog.customization;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.repo.customisation.nav.NavigationPreferenceImpl;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.customisation.nav.*;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.ui.common.renderer.data.CustomListRenderer;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.config.ActionsConfigElement;
import org.alfresco.web.config.ActionsConfigElement.ActionDefinition;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.config.Config;

import javax.faces.application.FacesMessage;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.jcr.PathNotFoundException;
import java.util.*;

/**
 * Dialog for navigation preferences editing.
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring.
 */
public class EditNavigationPreference extends BaseWaiDialog {

    public static final String COLUMN_NAME = "name";
    public static final String OPTION_PREVIEW = "preview";
    public static final String OPTION_DOWNLOAD = "download";
    public static final String SELECT_OPTION_DOWNLOAD = "option_download";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_TITLE_OR_NAME = "titleOrName";
    private static final String MSG_SELECT_ONE = "edit_navigation_dialog_select_one";
    private static final String LIST_SELECTED_COLUMNS = "list-selected-columns";
    private static final String LIST_AVAILABLE_COLUMNS = "list-available-columns";
    private static final String LIST_SELECTED_ACTIONS = "list-selected-actions";
    private static final String LIST_AVAILABLE_ACTIONS = "list-available-actions";
    private static final String BINDING_EDIT_PROPS = "#{WaiLeftMenuBean.editPropertiesLabel}";
    private static final String MSG_EDIT_PROPS = "edit_content_label";
    private static final String PARAM_PREF_TYPE = "prefType";
    private static final String PARAM_PREF_SERVICE = "prefService";
    private static final String VALUE_PRIMARY_COL_NAME = "PRIMARY_COL_NAME";
    private static final String VALUE_PRIMARY_COL_TITLE = "PRIMARY_COL_TITLE";
    private static final String VALUE_PRIMARY_COL_NAME_TITLE = "PRIMARY_COL_NAME_TITLE";
    private static final String VALUE_PRIMARY_COL_TITLE_NAME = "PRIMARY_COL_TITLE_NAME";
    private static final String VALUE_PRIMARY_BEST_TITLE = "PRIMARY_BEST_TITLE";
    private static final String VALUE_DEFAULT = "__default_";
    private static final String MSG_LOADED_FROM = "edit_navigation_dialog_action_loaded_from";
    private static final String MSG_UNDEFINED = "manage_navigation_dialog_undefined";
    /**
     *
     */
    private static final long serialVersionUID = -2811145987880105661L;
    private static final String navigationListShortNameRender = "shortname";
    private static final String navigationListTitleRender = "title";
    private transient NavigationPreferencesService navigationPreferencesService;
    private transient NavigationConfigService navigationConfigService;
    private NavigationPreference preference;
    private ServiceConfig globalPreference;
    private String primaryColumType;
    private List<SelectItem> availableColumns;
    private List<SelectItem> selectedColumns;
    private List<SelectItem> selectedPreviewActions;
    private List<SelectItem> availableActions;
    private List<SelectItem> selectedActions;
    private String[] highlightedAvailableColumns;
    private String[] highlightedSelectedColumns;
    private String[] highlightedAvailableActions;
    private String[] highlightedSelectedActions;
    private Integer listSize;
    private Boolean sortDescending;
    private ColumnConfig sortColumn;
    private Boolean nameAndTitleKeys;
    private Boolean bestTitleKey;
    private List<SelectItem> listSizes;
    private List<SelectItem> allAction;
    private String previewAction;
    private String renderPropertyName;
    private String selectedRenderChoice;
    private List<SelectItem> selectRenderChoices;
    private boolean contentNode = false;

    private boolean topicNode = false;
    private Boolean sortPostsDescending;

    private transient DynamicPropertyService dynamicPropertyService;

    /**
     * @return the navigationlistshortnamerender
     */
    public static String getNavigationlistshortnamerender() {
        return navigationListShortNameRender;
    }

    /**
     * @return the navigationlisttitlerender
     */
    public static String getNavigationlisttitlerender() {
        return navigationListTitleRender;
    }

    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory.");
        }

        if (parameters != null) {
            final String serviceParam = parameters.get(PARAM_PREF_SERVICE);
            final String typeParam = parameters.get(PARAM_PREF_TYPE);

            if (serviceParam == null || typeParam == null) {
                throw new IllegalArgumentException(
                        PARAM_PREF_SERVICE + " AND " + PARAM_PREF_TYPE + " are mandatories.");
            }
            initPreferences(getActionNode().getNodeRef(), serviceParam, typeParam);
        }
        this.selectRenderChoices = new ArrayList<>();
        this.selectRenderChoices.add(new SelectItem(navigationListShortNameRender,
                translate("category_customise_dialog_navigation_shortname_render")));
        this.selectRenderChoices.add(new SelectItem(navigationListTitleRender,
                translate("category_customise_dialog_navigation_title_render")));

    }

    private void initPreferences(final NodeRef ref, final String serviceParam,
                                 final String typeParam) {
        availableColumns = null;
        selectedColumns = null;
        selectedPreviewActions = null;
        previewAction = null;
        availableActions = null;
        selectedActions = null;
        listSize = null;
        sortDescending = null;
        sortPostsDescending = null;
        sortColumn = null;
        primaryColumType = null;
        nameAndTitleKeys = null;
        bestTitleKey = null;
        listSizes = null;
        allAction = null;
        highlightedAvailableColumns = null;
        highlightedSelectedColumns = null;
        highlightedAvailableActions = null;
        highlightedSelectedActions = null;
        renderPropertyName = null;
        preference = getNavigationPreferencesService()
                .getServicePreference(ref, serviceParam, typeParam);

        selectedRenderChoice =
                (preference.getRenderPropertyName() != null) ? preference.getRenderPropertyName()
                        : navigationListShortNameRender;

        globalPreference = preference.getService();

        contentNode = "content".equals(globalPreference.getType()) ||
                "forum".equals(globalPreference.getType());

        topicNode = "topic".equals(globalPreference.getType());
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        //1. Validate
        //int selColumnSize = getSelectedColumns().size();
        //int selActionSize = getSelectedActions().size();

        //2. Compute key columns
        final List<ColumnConfig> newColumns = new ArrayList<>(getSelectedColumns().size() + 2);
        final List<ColumnConfig> allColumns = globalPreference.getColumns();

        if (primaryColumType.equals(VALUE_PRIMARY_BEST_TITLE)) {
            newColumns.add(getColumnByName(COLUMN_TITLE_OR_NAME, allColumns, true));
        } else {
            final ColumnConfig name = getColumnByName(COLUMN_NAME, allColumns, true);
            final ColumnConfig title = getColumnByName(COLUMN_TITLE, allColumns, true);

            switch (primaryColumType) {
                case VALUE_PRIMARY_COL_NAME:
                    newColumns.add(name);
                    break;
                case VALUE_PRIMARY_COL_TITLE:
                    newColumns.add(title);
                    break;
                case VALUE_PRIMARY_COL_NAME_TITLE:
                    newColumns.add(name);
                    newColumns.add(title);
                    break;
                case VALUE_PRIMARY_COL_TITLE_NAME:
                    newColumns.add(title);
                    newColumns.add(name);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        //3. Compute other columns
        for (final SelectItem item : getSelectedColumns()) {
            newColumns.add(getColumnByName((String) item.getValue(), allColumns, true));
        }

        //4. Compute actions
        final List<String> newActions = new ArrayList<>(getSelectedActions().size());
        for (final SelectItem item : getSelectedActions()) {
            newActions.add((String) item.getValue());
        }

        //5. Compute other option

        //---- these config items are not configurable yet
        //preference.setDisplayActionColumn()
        //preference.setViewMode()
        //preference.setLinkTarget()

        ColumnConfig safeSortColumn = sortColumn;

        if (safeSortColumn == null) {
            safeSortColumn = newColumns.get(0);
        }
        if (sortDescending == null) {
            sortDescending = preference.isInitialSortDescending();
        }
        if (sortPostsDescending == null) {
            sortPostsDescending = preference.isInitialSortPostsDescending();
        }
        if (listSize == null) {
            listSize = preference.getListSize();
        }
        if (previewAction == null) {
            previewAction = OPTION_PREVIEW;
        }

        //6. compute breadcrumb/navigation list display title or shortname

        renderPropertyName = selectedRenderChoice;

        NavigationPreference newPreference = new NavigationPreferenceImpl(newColumns,
                newActions,
                listSize,
                safeSortColumn,
                sortDescending,
                preference.isDisplayActionColumn(),
                preference.getLinkTarget(),
                preference.getViewMode(),
                renderPropertyName,
                previewAction,
                sortPostsDescending);

        getNavigationPreferencesService().addServicePreference(getActionNode().getNodeRef(),
                globalPreference.getName(), globalPreference.getType(), newPreference);

        return outcome;
    }

    public void revert(final ActionEvent event) {
        initPreferences(getActionNode().getNodeRef(), globalPreference.getName(),
                globalPreference.getType());
    }

    public void loadDefault(final ActionEvent event) {
        final NodeRef nodeRef = getActionNode().getNodeRef();
        final ChildAssociationRef parent = getNodeService().getPrimaryParent(nodeRef);
        final NodeRef parentRef = parent.getParentRef();
        initPreferences(parentRef, globalPreference.getName(), globalPreference.getType());

        String path;
        try {
            final SimplePath simplePath = new SimplePath(getNodeService(), preference.getCustomizedOn());
            path = simplePath.toString();
        } catch (PathNotFoundException e) {
            path = "<i>" + translate(MSG_UNDEFINED) + "</i>";
        }

        Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_LOADED_FROM, path));
    }

    public String getPrimaryColumn() {
        if (primaryColumType == null) {
            final List<ColumnConfig> columns = preference.getColumns();

            if (isColumnFromName(columns, 0, COLUMN_TITLE_OR_NAME)) {
                primaryColumType = VALUE_PRIMARY_BEST_TITLE;
            } else {
                final boolean isNameFirst = isColumnFromName(columns, 0, COLUMN_NAME);
                final boolean isTitleFirst = isColumnFromName(columns, 0, COLUMN_TITLE);
                final boolean isNameSecond = isColumnFromName(columns, 1, COLUMN_NAME);
                final boolean isTitleSecond = isColumnFromName(columns, 1, COLUMN_TITLE);

                if (isNameFirst) {
                    if (isTitleSecond) {
                        primaryColumType = VALUE_PRIMARY_COL_NAME_TITLE;
                    } else {
                        primaryColumType = VALUE_PRIMARY_COL_NAME;
                    }
                } else if (isTitleFirst) {
                    if (isNameSecond) {
                        primaryColumType = VALUE_PRIMARY_COL_TITLE_NAME;
                    } else {
                        primaryColumType = VALUE_PRIMARY_COL_TITLE;
                    }
                } else {
                    primaryColumType = VALUE_PRIMARY_COL_NAME_TITLE;
                }
            }
        }
        return primaryColumType;
    }

    public void setPrimaryColumn(final String primaryColumType) {
        this.primaryColumType = primaryColumType;
    }

    public String getSortColumn() {
        if (sortColumn == null) {
            sortColumn = preference.getInitialSortColumn();
        }

        return sortColumn != null ? sortColumn.getName() : VALUE_DEFAULT;
    }

    public void setSortColumn(final String sortCol) {
        if (VALUE_DEFAULT.equals(sortCol)) {
            this.sortColumn = null;
        } else {
            for (final ColumnConfig column : globalPreference.getColumns()) {
                if (column.getName().equals(sortCol)) {
                    this.sortColumn = column;
                    break;
                }
            }
        }
    }

    public boolean isNameAndTitleKeys() {
        if (nameAndTitleKeys == null) {
            final List<ColumnConfig> keyColumns = globalPreference.getKeyColumns();
            nameAndTitleKeys = keyColumns.size() == 2;

            if (nameAndTitleKeys) {
                final String col1 = keyColumns.get(0).getName();
                final String col2 = keyColumns.get(1).getName();

                if (col1.equals(col2) || (!col1.equals(COLUMN_NAME) && !col2.equals(COLUMN_NAME)) || (
                        !col1.equals(COLUMN_TITLE) && !col2.equals(COLUMN_TITLE))) {
                    throw new IllegalStateException(
                            "This kind of double keys columns is not managed by the bean yet: " + col1 + " - "
                                    + col2);
                }
            }
        }

        return nameAndTitleKeys;
    }

    public boolean isBestTitleKey() {
        if (bestTitleKey == null) {
            final List<ColumnConfig> keyColumns = globalPreference.getKeyColumns();
            bestTitleKey = keyColumns.size() == 1;

            if (bestTitleKey && !keyColumns.get(0).getName().equals(COLUMN_TITLE_OR_NAME)) {
                throw new IllegalStateException(
                        "This kind of single key column is not managed by the bean yet: " + keyColumns.get(0)
                                .getName());
            }
        }
        return bestTitleKey;
    }

    private List<SelectItem> getAllActions() {
        if (allAction == null) {
            final List<String> actions = globalPreference.getActions();
            allAction = new ArrayList<>(actions.size());

            final Config globalConfig = Application.getConfigService(FacesContext.getCurrentInstance())
                    .getGlobalConfig();

            //  find the Actions specific config element
            final ActionsConfigElement actionConfig = (ActionsConfigElement) globalConfig
                    .getConfigElement(ActionsConfigElement.CONFIG_ELEMENT_ID);

            ActionDefinition actionDef;
            String actionMsg;
            String label;
            for (final String action : actions) {
                actionDef = actionConfig.getActionDefinition(action);
                actionMsg = actionDef.LabelMsg;

                if (BINDING_EDIT_PROPS.equals(actionMsg) || BINDING_EDIT_PROPS.equals(actionDef.Label)) {
                    label = translate(MSG_EDIT_PROPS);
                } else if (actionMsg == null || actionMsg.length() < 1) {
                    label = actionDef.Label;
                } else {
                    label = translate(actionMsg);
                }

                allAction.add(new SelectItem(action, label));
            }
        }

        return allAction;
    }

    public boolean isSortDescending() {
        if (sortDescending == null) {
            sortDescending = preference.isInitialSortDescending();
        }
        return sortDescending;
    }

    public void setSortDescending(boolean sortDescending) {
        this.sortDescending = sortDescending;
    }

    /**
     * @return the sortPostsDescending
     */
    public boolean isSortPostsDescending() {
        if (sortPostsDescending == null) {
            sortPostsDescending = preference.isInitialSortPostsDescending();
        }
        return sortPostsDescending;
    }

    /**
     * @param sortPostsDescending the sortPostsDescending to set
     */
    public void setSortPostsDescending(boolean sortPostsDescending) {
        this.sortPostsDescending = sortPostsDescending;
    }

    public String getListSize() {
        if (listSize == null) {
            listSize = preference.getListSize();
        }
        return String.valueOf(listSize);
    }

    public void setListSize(final String listSizeStr) {
        if (listSizeStr != null) {
            final int size = Integer.parseInt(listSizeStr);
            setListSize(size);
        }
    }

    public void setListSize(final int size) {
        this.listSize = size;
    }

    public List<SelectItem> getListSizes() {
        if (listSizes == null) {
            final int from = getMinListSize();
            int until = getMaxListSize();

            listSizes = new ArrayList<>(until - from);

            String strValue;
            for (int x = from; x <= until; ++x) {
                strValue = String.valueOf(x);
                listSizes.add(new SelectItem(strValue, strValue));
            }
        }
        return listSizes;
    }

    public int getMinListSize() {
        return getMinimum(globalPreference.getDisplayRowMin());
    }

    public int getMinActionSize() {
        return getMinimum(globalPreference.getDisplayActionMin());
    }

    public int getMinColumnSize() {
        return getMinimum(globalPreference.getDisplayColMin());
    }

    public int getMaxListSize() {
        return getMaximum(globalPreference.getDisplayRowMax(), NavigationPreference.MAX_LIST_SIZE);
    }

    public int getMaxActionSize() {
        return getMaximum(globalPreference.getDisplayActionMax(), globalPreference.getActions().size());
    }

    public int getMaxColumnSize() {
        return getMaximum(globalPreference.getDisplayColMax(), globalPreference.getColumns().size());
    }

    private int getMaximum(final int displayRowMax, final int max) {
        if (displayRowMax >= 0) {
            return displayRowMax;
        } else {
            return max;
        }
    }

    private int getMinimum(final int displayRowMin) {
        if (displayRowMin < 0) {
            return 0;
        } else {
            return displayRowMin;
        }
    }

    /**
     * @return the allColumns
     */
    public final List<SelectItem> getAvailableColumns() {

        if (availableColumns == null) {
            NodeRef currentInterestGroup = getManagementService()
                    .getCurrentInterestGroup(getActionNode().getNodeRef());
            List<DynamicProperty> dynamicProperties = Collections.emptyList();

            if (currentInterestGroup != null) {
                dynamicProperties = this.getDynamicPropertyService()
                        .getDynamicProperties(currentInterestGroup);
            }
            int actualPropertiesSize = dynamicProperties.size();
            final List<ColumnConfig> allColumns = globalPreference.getColumns();
            final List<ColumnConfig> definedColumns = preference.getColumns();
            final List<ColumnConfig> keyColumns = globalPreference.getKeyColumns();
            int maximumNumberOfDynamicProperties = 20;

            if (allColumns.size() - maximumNumberOfDynamicProperties > 0) {
                availableColumns = new ArrayList<>(
                        allColumns.size() - maximumNumberOfDynamicProperties + actualPropertiesSize);
            } else {
                availableColumns = new ArrayList<>(allColumns.size());
            }

            for (final ColumnConfig col : allColumns) {
                if (!definedColumns.contains(col) && !keyColumns.contains(col)) {
                    if (col.isDynamicProperty()) {
                        // process dynamic properties label
                        int index = Integer.valueOf(col.getName().replace("dynAttr", ""));
                        if (dynamicPropetyExists(index, dynamicProperties)) {

                            availableColumns.add(new SelectItem(col.getName(),
                                    getDynamicPropertylabelByIndex(dynamicProperties, index)));
                        }
                    } else {
                        availableColumns.add(new SelectItem(col.getName(), translate(col.getLabel())));
                    }
                }
            }
        }
        return availableColumns;
    }

    private String getDynamicPropertylabelByIndex(
            List<DynamicProperty> dynamicProperties, int index) {
        for (DynamicProperty dynamicProperty : dynamicProperties) {
            if (index == dynamicProperty.getIndex()) {
                return dynamicProperty.getLabel().getDefaultValue();
            }
        }
        return "";
    }

    private boolean dynamicPropetyExists(int index,
                                         List<DynamicProperty> dynamicProperties) {

        for (DynamicProperty dynamicProperty : dynamicProperties) {
            if (index == dynamicProperty.getIndex()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the allSelectedColumns
     */
    public final List<SelectItem> getSelectedColumns() {
        if (selectedColumns == null) {
            final List<ColumnConfig> definedColumns = preference.getColumns();
            final List<ColumnConfig> keyColumns = globalPreference.getKeyColumns();

            NodeRef currentInterestGroup = getManagementService()
                    .getCurrentInterestGroup(getActionNode().getNodeRef());
            List<DynamicProperty> dynamicProperties = Collections.emptyList();
            if (currentInterestGroup != null) {
                dynamicProperties = this.getDynamicPropertyService()
                        .getDynamicProperties(currentInterestGroup);
            }

            selectedColumns = new ArrayList<>();

            for (final ColumnConfig col : definedColumns) {
                if (!keyColumns.contains(col)) {
                    if (col.isDynamicProperty()) {
                        // process dynamic properties label
                        int index = Integer.valueOf(col.getName().replace("dynAttr", ""));
                        if (dynamicPropetyExists(index, dynamicProperties)) {
                            selectedColumns.add(new SelectItem(col.getName(),
                                    getDynamicPropertylabelByIndex(dynamicProperties, index)));
                        }
                    } else {
                        selectedColumns.add(new SelectItem(col.getName(), translate(col.getLabel())));
                    }
                }
            }
        }
        return selectedColumns;
    }

    public final List<SelectItem> getSelectedPreviewActions() {

        if (selectedPreviewActions == null) {
            selectedPreviewActions = new ArrayList<>();
            selectedPreviewActions.add(new SelectItem(OPTION_DOWNLOAD,
                    translate(SELECT_OPTION_DOWNLOAD)));
        }

        return selectedPreviewActions;
    }

    public boolean isContentNode() {
        return contentNode;
    }

    /**
     * @return the topicNode
     */
    public boolean isTopicNode() {
        return topicNode && Boolean.parseBoolean(CircabcConfiguration.getProperty(
                CircabcConfiguration.POSTS_SORTING_ENABLED));
    }

    /**
     * Gets the value of the previewAction
     *
     * @return the previewAction
     */
    public String getPreviewAction() {

        if (previewAction == null) {
            previewAction = preference.getPreviewAction();
        }

        return previewAction;
    }

    /**
     * Sets the value of the previewAction
     *
     * @param previewAction the previewAction to set.
     */
    public void setPreviewAction(String previewAction) {
        this.previewAction = previewAction;
    }

    /**
     * @return the allActions
     */
    public final List<SelectItem> getAvailableActions() {
        if (availableActions == null) {
            final String mandatory = globalPreference.getMandatoryAction();
            final List<SelectItem> allActions = getAllActions();
            final List<String> definedActions = preference.getActions();

            availableActions = new ArrayList<>(allActions.size());
            String value;
            for (final SelectItem action : allActions) {
                value = (String) action.getValue();
                if (!definedActions.contains(value) && !value.equals(mandatory)) {
                    //if oss version, we don't use the edit in office action
                    if (!(action.getLabel().equalsIgnoreCase(CustomListRenderer.EDIT_IN_OFFICE_ACTION)
                            && CircabcConfig.OSS)) {
                        availableActions.add(action);
                    }
                }
            }
        }
        return availableActions;
    }

    /**
     * @return the allSelectedActions
     */
    public final List<SelectItem> getSelectedActions() {
        if (selectedActions == null) {
            final String mandatory = globalPreference.getMandatoryAction();
            final List<String> definedActions = preference.getActions();
            final List<SelectItem> allActions = getAllActions();

            selectedActions = new ArrayList<>(definedActions.size() - 1);
            String value;
            for (final SelectItem action : allActions) {
                value = (String) action.getValue();
                if (definedActions.contains(value) && !value.equals(mandatory)) {
                    selectedActions.add(action);
                }
            }
        }
        return selectedActions;
    }

    public void selectColumn(final ActionEvent event) {
        move(event, LIST_AVAILABLE_COLUMNS, getAvailableColumns(), getSelectedColumns());
        highlightedAvailableColumns = null;
        highlightedSelectedColumns = null;
    }

    public void deselectColumn(final ActionEvent event) {
        move(event, LIST_SELECTED_COLUMNS, getSelectedColumns(), getAvailableColumns());
        highlightedAvailableColumns = null;
        highlightedSelectedColumns = null;
    }

    public void moveUpColumn(final ActionEvent event) {
        sortColumn(event, LIST_SELECTED_COLUMNS, getSelectedColumns(), true);
    }

    public void moveDownColumn(final ActionEvent event) {
        sortColumn(event, LIST_SELECTED_COLUMNS, getSelectedColumns(), false);
    }

    public void selectAction(final ActionEvent event) {
        move(event, LIST_AVAILABLE_ACTIONS, getAvailableActions(), getSelectedActions());
        highlightedAvailableActions = null;
        highlightedSelectedActions = null;
    }

    public void deselectAction(final ActionEvent event) {
        move(event, LIST_SELECTED_ACTIONS, getSelectedActions(), getAvailableActions());
        highlightedAvailableActions = null;
        highlightedSelectedActions = null;
    }

    public void moveUpAction(final ActionEvent event) {
        sortColumn(event, LIST_SELECTED_ACTIONS, getSelectedActions(), true);
    }

    public void moveDownAction(final ActionEvent event) {
        sortColumn(event, LIST_SELECTED_ACTIONS, getSelectedActions(), false);
    }

    private void move(final ActionEvent event, final String fromPickerId,
                      final List<SelectItem> fromList, final List<SelectItem> toList) {
        final String[] selected = getSelectedItems(event, fromPickerId);
        if (selected == null || selected.length == 0) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_SELECT_ONE));
        } else {
            final List<String> selectedAsList = Arrays.asList(selected);
            final List<SelectItem> itemsToMove = new ArrayList<>(selectedAsList.size());
            for (final SelectItem item : fromList) {
                if (selectedAsList.contains((String) item.getValue())) {
                    itemsToMove.add(item);
                }
            }

            fromList.removeAll(itemsToMove);
            toList.addAll(itemsToMove);
        }
    }

    private String[] getSelectedItems(final ActionEvent event, final String fromPickerId) {
        final UISelectMany fromPicker = (UISelectMany) event.getComponent().findComponent(fromPickerId);
        return (String[]) fromPicker.getSelectedValues();
    }

    private void sortColumn(final ActionEvent event, final String fromPickerId,
                            final List<SelectItem> listToSort, boolean up) {
        final String[] selected = getSelectedItems(event, fromPickerId);
        if (selected == null || selected.length == 0) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_SELECT_ONE));
        } else {
            final List<String> selectedAsList = Arrays.asList(selected);
            final int selectSize = selectedAsList.size();
            final int sortSize = listToSort.size();
            final List<Integer> itemsToMove = new ArrayList<>(selectSize);

            for (int x = 0; x < sortSize; ++x) {
                if (selectedAsList.contains((String) listToSort.get(x).getValue())) {
                    itemsToMove.add(x);
                }
            }

            for (int x = 0; x < selectSize; ++x) {
                final int idx = itemsToMove.get(x);
                int newIdx = idx;

                if (up && idx != 0) {
                    --newIdx;
                }
                if (!up && idx != sortSize - 1) {
                    ++newIdx;
                }

                if (idx != newIdx) {
                    Collections.swap(listToSort, idx, newIdx);
                }
            }
        }
    }

    private boolean isColumnFromName(final List<ColumnConfig> columns, final int idx,
                                     final String name) {
        if (columns == null || columns.size() < idx + 1) {
            return false;
        } else {
            return columns.get(idx).getName().equals(name);
        }
    }

    private ColumnConfig getColumnByName(final String name, List<ColumnConfig> columns,
                                         boolean failIsNull) {
        ColumnConfig col = null;

        for (ColumnConfig column : columns) {
            if (column.getName().equals(name)) {
                col = column;
                break;
            }
        }

        if (col == null && failIsNull) {
            throw new IllegalStateException(
                    "A column with name " + name + " is not found in the configuration.");
        }

        return col;
    }

    @Override
    public String getContainerTitle() {
        return translate("edit_navigation_dialog_title", globalPreference.getName(),
                globalPreference.getType(), getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("edit_navigation_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_navigation_dialog_icon_tooltip");
    }

    /**
     * @return the navigationPreferencesService
     */
    protected final NavigationPreferencesService getNavigationPreferencesService() {
        if (navigationPreferencesService == null) {
            navigationPreferencesService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNavigationPreferencesService();
        }
        return navigationPreferencesService;
    }

    /**
     * @param navigationPreferencesService the navigationPreferencesService to set
     */
    public final void setNavigationPreferencesService(
            NavigationPreferencesService navigationPreferencesService) {
        this.navigationPreferencesService = navigationPreferencesService;
    }

    /**
     * @return the navigationConfigService
     */
    protected final NavigationConfigService getNavigationConfigService() {
        if (navigationConfigService == null) {
            navigationConfigService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNavigationConfigService();
        }
        return navigationConfigService;
    }

    /**
     * @param navigationConfigService the navigationConfigService to set
     */
    public final void setNavigationConfigService(NavigationConfigService navigationConfigService) {
        this.navigationConfigService = navigationConfigService;
    }

    /**
     * @return the highlightedAvailableColumns
     */
    public final String[] getHighlightedAvailableColumns() {
        return highlightedAvailableColumns;
    }

    /**
     * @param highlightedAvailableColumns the highlightedAvailableColumns to set
     */
    public final void setHighlightedAvailableColumns(String[] highlightedAvailableColumns) {
        this.highlightedAvailableColumns = highlightedAvailableColumns.clone();
    }

    /**
     * @return the highlightedSelectedColumns
     */
    public final String[] getHighlightedSelectedColumns() {
        return highlightedSelectedColumns;
    }

    /**
     * @param highlightedSelectedColumns the highlightedSelectedColumns to set
     */
    public final void setHighlightedSelectedColumns(String[] highlightedSelectedColumns) {
        this.highlightedSelectedColumns = highlightedSelectedColumns.clone();
    }

    /**
     * @return the highlightedAvailableActions
     */
    public final String[] getHighlightedAvailableActions() {
        return highlightedAvailableActions;
    }

    /**
     * @param highlightedAvailableActions the highlightedAvailableActions to set
     */
    public final void setHighlightedAvailableActions(String[] highlightedAvailableActions) {
        this.highlightedAvailableActions = highlightedAvailableActions.clone();
    }

    /**
     * @return the highlightedSelectedActions
     */
    public final String[] getHighlightedSelectedActions() {
        return highlightedSelectedActions;
    }

    /**
     * @param highlightedSelectedActions the highlightedSelectedActions to set
     */
    public final void setHighlightedSelectedActions(String[] highlightedSelectedActions) {
        this.highlightedSelectedActions = highlightedSelectedActions.clone();
    }

    /**
     * @return the renderPropertyName
     */
    public String getRenderPropertyName() {
        return renderPropertyName;
    }

    /**
     * @param renderPropertyName the renderPropertyName to set
     */
    public void setRenderPropertyName(String renderPropertyName) {
        this.renderPropertyName = renderPropertyName;
    }

    /**
     * @return the selectedRenderChoice
     */
    public String getSelectedRenderChoice() {
        return selectedRenderChoice;
    }

    /**
     * @param selectedRenderChoice the selectedRenderChoice to set
     */
    public void setSelectedRenderChoice(String selectedRenderChoice) {
        this.selectedRenderChoice = selectedRenderChoice;
    }

    /**
     * @return the selectRenderChoices
     */
    public List<SelectItem> getSelectRenderChoices() {
        return selectRenderChoices;
    }

    /**
     * @param selectRenderChoices the selectRenderChoices to set
     */
    public void setSelectRenderChoices(List<SelectItem> selectRenderChoices) {
        this.selectRenderChoices = selectRenderChoices;
    }

    public DynamicPropertyService getDynamicPropertyService() {
        if (dynamicPropertyService == null) {
            dynamicPropertyService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getDynamicPropertieService();
        }
        return dynamicPropertyService;
    }

    public void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.dynamicPropertyService = dynamicPropertyService;
    }

}
