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
package eu.cec.digit.circabc.web.ui.common.renderer.data;


import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.customisation.nav.ColumnConfig;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.ui.common.component.data.UIColumn;
import eu.cec.digit.circabc.web.ui.common.component.data.UICustomList;
import eu.cec.digit.circabc.web.ui.common.component.data.UIDataPager;
import eu.cec.digit.circabc.web.ui.common.component.data.UISortLink;
import eu.cec.digit.circabc.web.ui.repo.component.UIActions;
import eu.cec.digit.circabc.web.ui.repo.component.UILockIcon;
import eu.cec.digit.circabc.web.wai.dialog.customization.EditNavigationPreference;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.ActionsConfigElement;
import org.alfresco.web.config.ActionsConfigElement.ActionDefinition;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Guillaume
 */
public class CustomListRenderer extends RichListRenderer {

    public static final String EDIT_IN_OFFICE_IMAGE = "/images/extension/icons/edit-in-office-action.png";
    public static final String EDIT_IN_OFFICE_ACTION = "edit_in_office_wai";
    public static final String EDIT_IN_OFFICE_TOOLTIP = "edit_in_office_tooltip";
    public static final String ATTR_ONCLICK = "onclick";
    private static final String ICON_EXTENSION = ".gif";
    private static final String DIRECTORY_ICONS = "/images/icons/";
    private static final String BROWSE_BEAN = "BrowseBean";
    private static final String METHOD_CLICK_WAI = "clickWai";
    private static final String TARGET_NEW = "new";
    private static final String PARAM_ID = "id";
    private static final String STYLE_LANG = "langCode";
    private static final String STYLE_LANG_WHEN_LINK = "langCodeWhenLink";
    private static final String BINDING_NOTEQUALS_NULL = "#'{'{0}.{1} != null'}'";
    private static final String COMPOSITE_BINDING = "#'{'{0}.{1}'}'";
    private static final String SIMPLE_BINDING = "#'{'{0}'}'";
    private static final String EMPTY = "";
    private static final String MSG_DOWLOAD_TOOLTIP = "generic_dowload_link_tooltip";
    private static final String MSG_BROWSE_TOOLTIP = "generic_browse_link_tooltip";
    private static final String MSG_DATE_TIME_PATTERN = WebClientHelper.MSG_DATE_TIME_PATTERN;
    private static final String MSG_ACTIONS = "actions";
    private static final String MSG_GENERIC_SORT_DESC = "generic_sort_desc";
    private static final String MSG_GENERIC_SORT_ASC = "generic_sort_asc";
    private static final String MSG_NOT_CONTENT_TOOLTIP = "generic_icon_tooltip";
    private static final String MSG_CONTENT_TOOLTIP = "generic_content_icon_tooltip";
    private static final String MSG_NO_ITEM = "no_list_items";
    private static final String ATTR_VAR = "var";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_HREF = "href";
    private static final String ATTR_CONTEXT = "context";
    private static final String ATTR_RENDERED = "rendered";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_TOOLTIP = "tooltip";
    private static final String ATTR_STYLE_CLASS = "styleClass";

    private static final String VALUE_NODE_REF = "nodeRef";
    private static final String VALUE_LANG = "lang";
    private static final String VALUE_NAME = "name";
    private static final String VALUE_SMALL_ICON = "smallIcon";
    private static final String VALUE_FILE_TYPE = "fileType16";

    private static final String FACET_SMALL_ICON = "small-icon";
    private static final String FACET_HEADER = "header";
    private static final String FACET_EMPTY = "empty";

    private static final String STYLE_PAGER_CIRCA = "pagerCirca";
    private static final String STYLE_NO_ITEM = "noItem";

    private static final String TYPE_BOTH = "both";

    private static final Log logger = LogFactory.getLog(CustomListRenderer.class);

    /**
     * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    @SuppressWarnings("unchecked")
    public void encodeChildren(FacesContext fc, UIComponent component) throws IOException {
        if (component.isRendered() && component.getChildCount() == 0) {
            // the RichList component we are working with
            final UICustomList richList = (UICustomList) component;

            final NavigationPreference configuration = richList.getConfiguration();
            final String var = configuration.getService().getType();

            richList.getAttributes().put(ATTR_VAR, var);
            final ColumnConfig initialSortColumn = configuration.getInitialSortColumn();
            if (initialSortColumn != null) {
                richList.setInitialSortColumn(initialSortColumn.getName());
            } else {
                richList.setInitialSortColumn(configuration.getColumns().get(0).getName());
            }
            richList.setInitialSortDescending(configuration.isInitialSortDescending());
            richList.setPageSize(configuration.getListSize());
            richList.setViewMode(configuration.getViewMode());

            boolean first = true;

            for (final ColumnConfig columnConfig : configuration.getColumns()) {
                final UIColumn facesColumn = new UIColumn();
                facesColumn.setPrimary(first);
                facesColumn.setActions(false);

                facesColumn.getFacets().put(FACET_HEADER, generateSortlink(fc, columnConfig));

                if (first) {
                    first = false;

                    if (configuration.getService().isBulkOperationAllowed()) {
                        logger.warn("Bulk operation not implemented yet. ");

                        facesColumn.getChildren().add(generateBulkCheckBox(var));
                    }

                    facesColumn.getFacets()
                            .put(FACET_SMALL_ICON, generateLink(fc, configuration, columnConfig, var, true));
                    facesColumn.getChildren().add(generateLink(fc, configuration, columnConfig, var, false));
                    facesColumn.getChildren().add(generateLangLabel(fc, var, configuration));

                    facesColumn.getChildren().add(generateLockIcon(fc, var));
                } else {
                    facesColumn.getChildren().add(generateText(fc, columnConfig, var));
                }

                richList.getChildren().add(facesColumn);
            }

            if (configuration.isDisplayActionColumn()) {
                final UIColumn actionsColumn = new UIColumn();
                actionsColumn.setPrimary(false);
                actionsColumn.setActions(true);
                actionsColumn.getFacets()
                        .put(FACET_HEADER, generateStaticText(Application.getMessage(fc, MSG_ACTIONS)));
                actionsColumn.getChildren().add(buildActions(fc, var, configuration));
                richList.getChildren().add(actionsColumn);
            }

            // add the empty facet
            final HtmlOutputText empty = generateStaticText(Application.getMessage(fc, MSG_NO_ITEM));
            empty.setStyleClass(STYLE_NO_ITEM);
            richList.getFacets().put(FACET_EMPTY, empty);

            // add the data pager
            final UIDataPager pager = new UIDataPager();
            pager.setId(richList.getId() + "-pager");
            pager.getAttributes().put(ATTR_STYLE_CLASS, STYLE_PAGER_CIRCA);
            richList.getChildren().add(pager);
        }
        super.encodeChildren(fc, component);
    }

    private HtmlOutputText generateText(final FacesContext fc, final ColumnConfig columnConfig,
                                        final String var) {
        final HtmlOutputText text = new HtmlOutputText();
        text.setValueBinding(ATTR_VALUE, buildBinding(fc, var, columnConfig.getResolver()));
        text.setEscape(false);

        final String converterId = columnConfig.getConverter();
        if (converterId != null && converterId.length() > 0) {
            final Converter converter = fc.getApplication().createConverter(converterId);

            //TODO set pattern configurable in nav-default-config.xml
            if (converter instanceof XMLDateConverter) {
                final XMLDateConverter dateDonverter = (XMLDateConverter) converter;
                dateDonverter.setType(TYPE_BOTH);
                dateDonverter.setPattern(Application.getMessage(fc, MSG_DATE_TIME_PATTERN));
            }

            text.setConverter(converter);
        }

        return text;
    }

    /**
     * @param fc
     * @return
     */
    private UISortLink generateSortlink(final FacesContext fc, final ColumnConfig config) {
        final UISortLink sortLink = new UISortLink();
        if (config.isDynamicProperty()) {
            sortLink.setLabel(config.getLabel());
        } else {
            sortLink.setLabel(Application.getMessage(fc, config.getLabel()));
        }
        sortLink.setValue(config.getName());
        sortLink.setTooltipAscending(Application.getMessage(fc, MSG_GENERIC_SORT_ASC));
        sortLink.setTooltipDescending(Application.getMessage(fc, MSG_GENERIC_SORT_DESC));

        return sortLink;
    }

    @SuppressWarnings("unchecked")
    private UIActionLink generateLink(final FacesContext fc, final NavigationPreference configuration,
                                      final ColumnConfig columnConfig, String var, boolean isIcon) {
        final UIActionLink link = new UIActionLink();
        FacesHelper.setupComponentId(fc, link, null);

        final String linkTarget = configuration.getLinkTarget();
        final boolean isContent = configuration.getService().getType()
                .equalsIgnoreCase(NavigationPreferencesService.CONTENT_TYPE);
        final boolean isDownload = linkTarget.equals(NavigationPreference.DOWNLOAD_LINK_ACTION);
        link.setValueBinding(ATTR_VALUE, buildBinding(fc, var, columnConfig.getResolver()));

        // Added for the document preview
        ServiceConfig serviceConfig = configuration.getService();
        String previewAction = configuration.getPreviewAction();
        String action = "alternativeBrowseUrl";
        if (CircabcConfig.ECHA) {
            action = "alternativeDownloadUrl";
        }

        // Check for information service and library
        if (previewAction == null && isDownload) { // old IGs & ISs
            link.setValueBinding(ATTR_HREF, buildBinding(fc, var, action));
            link.setTarget(TARGET_NEW);
        } else if (
                "custom-action-list-information-content".equals(serviceConfig.getActionConfigName()) &&
                        (EditNavigationPreference.OPTION_PREVIEW
                                .equals(previewAction)/* || previewAction == null*/)
                        || /* Check previewAction == null to enable preview as default */
                        "custom_action_list_library_content".equals(serviceConfig.getActionConfigName()) &&
                                (EditNavigationPreference.OPTION_PREVIEW
                                        .equals(previewAction)/* || previewAction == null*/)) {

            link.setValueBinding(ATTR_ONCLICK,
                    buildBinding(fc, var, NavigableNode.ONCLICK_PREVIEW_BEHAVIOUR));
            link.setHref("javascript:void(0);");
            link.setTarget(null);
        } else if (isDownload) {
            link.setValueBinding(ATTR_HREF, buildBinding(fc, var, action));
            link.setTarget(TARGET_NEW);
        }

        if (!isDownload) {
            if (!linkTarget.equals(NavigationPreference.BROWSE_LINK_ACTION)) {
                logger.warn(linkTarget + " not managed yet... browse link will be generated. ");
            }

            link.setActionListener(
                    buildBindingMethod(fc, BROWSE_BEAN, METHOD_CLICK_WAI, ActionEvent.class));
            final UIParameter actionParam = generateIdParam(fc, var);
            link.getChildren().add(actionParam);
        }

        link.setShowLink(!isIcon);

        if (isIcon) {
            if (isContent) {
                link.setValueBinding(ATTR_IMAGE, buildBinding(fc, var, VALUE_FILE_TYPE));
                link.setTooltip(Application.getMessage(fc, MSG_CONTENT_TOOLTIP));
            } else {
                link.setValueBinding(ATTR_IMAGE,
                        buildBinding(fc, var, VALUE_SMALL_ICON, DIRECTORY_ICONS, ICON_EXTENSION));
                link.setTooltip(Application.getMessage(fc, MSG_NOT_CONTENT_TOOLTIP));
            }

        } else {
            final String tooltipMessage;
            if (isDownload) {
                tooltipMessage = Application.getMessage(fc, MSG_DOWLOAD_TOOLTIP);
            } else {
                tooltipMessage = Application.getMessage(fc, MSG_BROWSE_TOOLTIP);
            }
            link.setValueBinding(ATTR_TOOLTIP,
                    buildBinding(fc, var, VALUE_NAME, tooltipMessage + ": ", EMPTY));
        }

        return link;
    }

    /**
     * @param fc
     * @param var
     */
    private UILockIcon generateLockIcon(final FacesContext fc, String var) {
        final UILockIcon lockIcon = new UILockIcon();
        lockIcon.setValueBinding(ATTR_VALUE, buildBinding(fc, var, VALUE_NODE_REF));
        return lockIcon;
    }

    /**
     * @param fc
     * @param var
     */
    private UIComponent generateBulkCheckBox(String var) {
        final HtmlSelectBooleanCheckbox chkBox = new HtmlSelectBooleanCheckbox();
        chkBox.setSelected(Boolean.FALSE);
        return chkBox;
    }

    /**
     * @param fc
     * @param var
     */
    private HtmlOutputLabel generateLangLabel(final FacesContext fc,
                                              String var, final NavigationPreference configuration) {
        final HtmlOutputLabel lang = new HtmlOutputLabel();
        lang.setValueBinding(ATTR_VALUE, buildBinding(fc, var, VALUE_LANG));
        lang.setValueBinding(ATTR_RENDERED, buildDifNullBinding(fc, var, VALUE_LANG));

        // Added for the document preview
        ServiceConfig serviceConfig = configuration.getService();
        String previewAction = configuration.getPreviewAction();

        if ("custom-action-list-information-content".equals(serviceConfig.getActionConfigName()) &&
                (EditNavigationPreference.OPTION_PREVIEW.equals(previewAction) || previewAction == null)
                || /* Check previewAction == null to enable preview as default */
                "custom_action_list_library_content".equals(serviceConfig.getActionConfigName()) &&
                        (EditNavigationPreference.OPTION_PREVIEW.equals(previewAction)
                                || previewAction == null)) {

            lang.setValueBinding(ATTR_ONCLICK,
                    buildBinding(fc, var, NavigableNode.ONCLICK_PREVIEW_BEHAVIOUR));
            lang.setStyleClass(STYLE_LANG_WHEN_LINK);
        } else {
            lang.setStyleClass(STYLE_LANG);
        }

        return lang;
    }

    private UIParameter generateIdParam(final FacesContext fc, String var) {
        final UIParameter actionParam = (UIParameter) fc.getApplication()
                .createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
        actionParam.setName(PARAM_ID);
        actionParam.setValueBinding(ATTR_VALUE, buildBinding(fc, var, PARAM_ID));
        return actionParam;
    }

    /**
     * @param var
     * @return
     */
    private UIActions buildActions(final FacesContext fc, final String var,
                                   final NavigationPreference configuration) {
        final UIActions actions = new UIActions();
        final String mandatoryAction = configuration.getService().getMandatoryAction();
        actions.setValue(configuration.getService().getActionConfigName());
        actions.setValueBinding(ATTR_CONTEXT, buildSimpleBinding(fc, var));
        actions.setShowLink(false);

        final List<String> availableActions = configuration.getService().getActions();
        final List<String> toRenderActions = configuration.getActions();

        if (mandatoryAction != null && mandatoryAction.length() > 0 && !toRenderActions
                .contains(mandatoryAction)) {
            toRenderActions.add(mandatoryAction);

            if (logger.isWarnEnabled()) {
                logger.warn("Mandatory action forced. Please to add it in the configuration file!:"
                        + mandatoryAction);
            }
        }

        for (final String availableAction : availableActions) {
            if (EDIT_IN_OFFICE_ACTION.equals(availableAction)) {

                ActionDefinition actionDef = getActionDefinition(fc, availableAction, actions);

                actionDef.Onclick = MessageFormat
                        .format(COMPOSITE_BINDING, var, NavigableNode.ONCLICK_EDIT_IN_OFFICE);
                actionDef.Href = "javascript:void(0);";
            }

            if (!toRenderActions.contains(availableAction)) {
                actions.addHiddenAction(availableAction);
            }
        }

        return actions;
    }

    private ActionDefinition getActionDefinition(final FacesContext context, String actionId,
                                                 UIActions actions) {

        Config config = null;

        Object actionContext = actions.getContext();

        if (actionContext instanceof Node) {
            config = Application.getConfigService(context).getConfig(actionContext);
        } else {
            config = Application.getConfigService(context).getGlobalConfig();
        }

        ActionsConfigElement actionConfig = null;

        if (config != null) {
            actionConfig = (ActionsConfigElement) config.getConfigElement(
                    ActionsConfigElement.CONFIG_ELEMENT_ID);
        }

        return actionConfig.getActionDefinition(actionId);
    }

    private HtmlOutputText generateStaticText(final String simpleValue) {
        final HtmlOutputText text = new HtmlOutputText();
        text.setValue(simpleValue);
        text.setEscape(false);
        return text;
    }

    private MethodBinding buildBindingMethod(final FacesContext fc, final String object,
                                             final String value, final Class paramClass) {
        return fc.getApplication()
                .createMethodBinding(MessageFormat.format(COMPOSITE_BINDING, object, value),
                        new Class[]{paramClass});
    }

    private ValueBinding buildSimpleBinding(final FacesContext fc, final String value) {
        return fc.getApplication().createValueBinding(MessageFormat.format(SIMPLE_BINDING, value));
    }

    private ValueBinding buildBinding(final FacesContext fc, final String object,
                                      final String value) {
        return buildBinding(fc, object, value, EMPTY, EMPTY);
    }

    private ValueBinding buildBinding(final FacesContext fc, final String object, final String value,
                                      final String prefix, final String suffix) {
        return fc.getApplication().createValueBinding(
                prefix + MessageFormat.format(COMPOSITE_BINDING, object, value) + suffix);
    }

    private ValueBinding buildDifNullBinding(final FacesContext fc, final String object,
                                             final String value) {
        return fc.getApplication()
                .createValueBinding(MessageFormat.format(BINDING_NOTEQUALS_NULL, object, value));
    }

}
