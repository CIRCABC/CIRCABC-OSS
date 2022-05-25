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
package eu.cec.digit.circabc.web.wai.dialog.ml;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.bean.ml.MachineTranslationPropertyDialog;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBeanData;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.generic.LightDescriptionSizeExceedException;
import eu.cec.digit.circabc.web.wai.generator.BaseDialogLauncherGenerator;
import eu.cec.digit.circabc.web.wai.generator.TranslateLongTextPropertyGenerator;
import eu.cec.digit.circabc.web.wai.generator.TranslateLongTextPropertyGeneratorNoRichText;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * Benas that back the translate propety dialog.
 *
 * @author yanick pignot
 */
public class TranslatePropertyDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "TranslatePropertyDialog";
    public static final String MSG_ERROR_LOCALE_REQUIRED = "translate_property_dialog_error_locale_required";
    public static final String MSG_ERROR_VALUE_REQUIRED = "translate_property_dialog_error_value_required";
    public static final String MSG_ERROR_LOCALE_DUPLICATED = "translate_property_dialog_error_locale_duplicated";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + "translatePropertyDialogWai";
    private static final String CI_LIGHT_DESCRIPTION = "ci:lightDescription";
    private static final long serialVersionUID = -7111360183999853548L;
    /**
     * Logger (coppepa: logger must be final)
     */
    private static final Log logger = LogFactory.getLog(TranslatePropertyDialog.class);
    private transient ContentFilterLanguagesService contentFilterLanguagesService;
    private String propertyId;
    private QName propertyQname;
    private String i18nPropety;
    private boolean displayAsArea;
    private boolean displayRichText;
    private boolean machineTranslationEnabled;
    /**
     * datamodel for table of translations for users
     */
    private transient DataModel translationDataModel = null;
    private List<MLPropertyWrapper> propertyTranslation = null;

    private String value;
    private String language;
    private String selectedLanguage;
    private String selectedValue;

    private boolean isLightDescription;


    private SelectItem[] textAreaTypes;
    private String textAreaType;
    private String selectedTextAreaType;

    private NodeRef igNodeRef;
    private NodeRef catNodeRef;

    private CircabcService circabcService;


    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        igNodeRef = null;
        catNodeRef = null;

        if (parameters != null) {
            value = null;
            language = null;
            selectedLanguage = null;
            propertyTranslation = new ArrayList<>(30);
            translationDataModel = null;
            i18nPropety = null;
            displayAsArea = false;
            displayRichText = true;
            propertyId = parameters.get(BaseDialogLauncherGenerator.PARAM_PROPERTY_KEY);
            isLightDescription = propertyId.equalsIgnoreCase(CI_LIGHT_DESCRIPTION);

            textAreaTypes = new SelectItem[2];
            textAreaTypes[0] = new SelectItem("Rich", translate("rich_textbox"));
            textAreaTypes[1] = new SelectItem("Simple", translate("simple_textbox"));

            if (isLightDescription) {
                textAreaType = "Simple";
                selectedTextAreaType = "Simple";
            } else {
                textAreaType = "Rich";
                selectedTextAreaType = "Rich";
            }

        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException("Node id is a mandatory parameter");
        }

        if (propertyId == null || propertyId.trim().length() == 0) {
            throw new IllegalArgumentException("The property id is a mandatory parameter");
        }

        int idx = propertyId.indexOf(':');

        if (idx < 0) {
            propertyQname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, propertyId);
        } else {
            final String uri = getNamespaceService().getNamespaceURI(propertyId.substring(0, idx));
            propertyQname = QName.createQName(uri, propertyId.substring(idx + 1));
        }

        final PropertyDefinition propertyDef = getDictionaryService().getProperty(propertyQname);

        if (propertyDef == null || !propertyDef.getDataType().getName()
                .equals(DataTypeDefinition.MLTEXT)) {
            throw new IllegalArgumentException("The property type definition must be a MLText !!");
        }

        final MLText values = getMLPropertyValue(getActionNode().getNodeRef(), this.propertyQname);
        if (values != null) {
            propertyTranslation.clear();
            for (Map.Entry<Locale, String> translation : values.entrySet()) {
                propertyTranslation.add(
                        new MLPropertyWrapper(getSecurityService().getCleanHTML(translation.getValue(), false),
                                translation.getKey()));
            }
        }

        i18nPropety = translate(propertyQname.getLocalName());
        displayAsArea = isTextArea(parameters);
        displayRichText = isRichTextArea(parameters);

        machineTranslationEnabled = false;
        if (!"".equals(CircabcConfiguration.getProperty(CircabcConfiguration.MT_ENABLED))) {
            machineTranslationEnabled = Boolean
                    .parseBoolean(CircabcConfiguration.getProperty(CircabcConfiguration.MT_ENABLED));
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to add translation to the property " + propertyId + " of node " + getActionNode()
                            .getName() + "...");
        }

        String sourceLanguage = null;

        final MLText mlText = new MLText();
        //	set the translations
        for (final MLPropertyWrapper wrapper : propertyTranslation) {
            if (sourceLanguage == null) {
                sourceLanguage = wrapper.getLocale().toString().toUpperCase();
            }
            mlText.addValue(wrapper.getLocale(), wrapper.getValue());
        }

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            getNodeService().setProperty(getActionNode().getNodeRef(), this.propertyQname, mlText);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        if ((getNodeService()
                .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_CATEGORY))) {
            catNodeRef = this.getActionNode().getNodeRef();
            final CategoryHeadersBeanData categoryHeadersBeanData = (CategoryHeadersBeanData) Beans
                    .getBean("CategoryHeadersBeanData");
            categoryHeadersBeanData.reset();
        }

        if ((getNodeService()
                .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_IGROOT))) {
            igNodeRef = this.getActionNode().getNodeRef();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Property " + propertyId + "  successfully updated for the node " + getActionNode()
                            .getName()
                            + "\n	node: " + getActionNode().getNodeRef()
                            + "\n	prop: " + propertyQname
                            + "\n	values: " + mlText);
        }

        //	refresh the edit content properties dialog
        Beans.getEditNodePropertiesDialog().setPropetyDefinedOutside(propertyQname);
        return outcome;
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {

        if (getCircabcService().syncEnabled()) {
            if ((propertyId.equals("title") || propertyId.equals(CI_LIGHT_DESCRIPTION))
                    && igNodeRef != null) {
                getCircabcService().updateIntestGroupProperties(igNodeRef);
            } else if (propertyId.equals("title") && catNodeRef != null) {
                getCircabcService().updateCategoryProperties(catNodeRef);
            }
        }
        return super.doPostCommitProcessing(context, outcome);
    }

    public void addSelection(ActionEvent event) {
        final UIInput inputText = (UIInput) event.getComponent()
                .findComponent("translate-property-value-input");
        final UIInput inputArea = (UIInput) event.getComponent()
                .findComponent("translate-property-value-area");
        final UISelectOne select = (UISelectOne) event.getComponent()
                .findComponent("translate-property-language");

        final String selLang = (String) select.getValue();
        final String selValue = inputText != null ? ((String) inputText.getValue()).trim()
                : ((String) inputArea.getValue()).trim();
        addSelectionImpl(selLang, selValue);
    }

    private void addSelectionImpl(final String selLang, final String selValue) {
        boolean error = false;
        if (selValue.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_VALUE_REQUIRED));
            error = true;
        }

        if (selLang.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_REQUIRED));
            error = true;
        }

        for (MLPropertyWrapper wrapper : propertyTranslation) {
            if (selLang.equalsIgnoreCase(wrapper.getLanguage())) {
                Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_DUPLICATED));
                error = true;
                break;
            }
        }

        String finalValue = getSecurityService()
                .getCleanHTML(StringEscapeUtils.unescapeHtml(selValue), false);

        if (isLightDescription) {
            finalValue = Jsoup.parse(selValue).text();

            if (finalValue.length() > LightDescriptionSizeExceedException.MAX_CHARACTER_LIMIT) {
                Utils.addErrorMessage(translate("lightDescription_limit_exceed"));
                error = true;
            }
        } else {
            finalValue = getCleanHTML(finalValue, false);
        }

        if (!error) {

            propertyTranslation.add(new MLPropertyWrapper(finalValue, new Locale(selLang)));
            this.value = null;
        }
    }

    public String getDialogCloseAndLaunchAction() {
        final FacesContext currentInstance = FacesContext.getCurrentInstance();

        final UIInput inputText = (UIInput) currentInstance.getViewRoot()
                .findComponent("FormPrincipal:translate-property-value-input");
        final UIInput inputArea = (UIInput) currentInstance.getViewRoot()
                .findComponent("FormPrincipal:translate-property-value-area");
        final UISelectOne select = (UISelectOne) currentInstance.getViewRoot()
                .findComponent("FormPrincipal:translate-property-language");

        final String selLang = (String) select.getValue();
        final String selValue = inputText != null ? ((String) inputText.getValue()).trim()
                : ((String) inputArea.getValue()).trim();

        if (selValue != null && !selValue.trim().equals("")) {
            addSelectionImpl(selLang, selValue);
            try {
                finishImpl(currentInstance, null);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Eroor when try to save property translation", e);
                }
            }
        }
        final Map<String, String> parameters = new HashMap<>(3);
        parameters.put("id", getActionNode().getId());
        parameters.put(BaseDialogLauncherGenerator.PARAM_PROPERTY_KEY, propertyId);
        parameters.put("service", "Machine translation");
        parameters.put("activity", "Property translation");
        Beans.getMachineTranslationPropertyBean().init(parameters);
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcNavigationHandler.WAI_DIALOG_PREFIX
                + MachineTranslationPropertyDialog.DIALOG_NAME;
    }

    /**
     * Method calls by the dialog to getr the available langages.
     *
     * @return the list of languages where at least one property define
     */
    public SelectItem[] getLanguages() {
        // get the list of filter languages
        final List<String> languages = getContentFilterLanguagesService().getFilterLanguages();

        final List<String> filteredLanguages = new ArrayList<>(languages.size());
        filteredLanguages.addAll(languages);

        for (final MLPropertyWrapper wrapper : propertyTranslation) {
            if (wrapper.getLocale() != null) {
                filteredLanguages.remove(wrapper.getLocale().getLanguage());
            } else {
                logger.warn("The MLProperty wrapper " + wrapper + " has not language defined.");
                throw new IllegalStateException(
                        "The MLProperty wrapper " + wrapper + " has not language defined. ");
            }
        }

        // set the item selection list
        final SelectItem[] items = new SelectItem[filteredLanguages.size()];
        int idx = 0;

        for (final String lang : filteredLanguages) {
            final String label = getContentFilterLanguagesService().getLabelByCode(lang);
            items[idx] = new SelectItem(lang, label);
            idx++;
        }

        return items;
    }

    /**
     * Returns the properties for current property translations JSF DataModel
     *
     * @return JSF DataModel representing the translation of the property
     */
    public DataModel getTranslationDataModel() {
        if (this.translationDataModel == null) {
            this.translationDataModel = new ListDataModel();
        }

        this.translationDataModel.setWrappedData(this.propertyTranslation);

        return this.translationDataModel;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a property translation
     */
    public void removeSelection(ActionEvent event) {
        MLPropertyWrapper wrapper = (MLPropertyWrapper) this.translationDataModel.getRowData();
        if (wrapper != null) {
            this.propertyTranslation.remove(wrapper);
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a property translation
     */
    public void editSelection(ActionEvent event) {
        MLPropertyWrapper wrapper = (MLPropertyWrapper) this.translationDataModel.getRowData();
        if (wrapper != null) {
            this.selectedValue = wrapper.getValue();
            this.selectedLanguage = wrapper.getLanguage();
            if (this.selectedValue == null) {
                selectedTextAreaType = "Rich";
            } else {
                if (this.selectedValue.contains("<") && this.selectedValue.contains(">")
                        && !isLightDescription) {
                    selectedTextAreaType = "Rich";
                } else {
                    selectedTextAreaType = "Simple";
                }
            }
        }
    }

    public void saveSelection(ActionEvent event) {
        final UIInput inputText = (UIInput) event.getComponent()
                .findComponent("translate-property-selected-value-input");
        final UIInput inputArea = (UIInput) event.getComponent()
                .findComponent("translate-property-selected-value-area");

        final String selValue = inputText != null ? ((String) inputText.getValue()).trim()
                : ((String) inputArea.getValue()).trim();

        boolean error = false;

        if (selValue.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_VALUE_REQUIRED));
            error = true;
        }

        int index = 0;

        for (MLPropertyWrapper wrapper : propertyTranslation) {
            if (selectedLanguage.equalsIgnoreCase(wrapper.getLanguage())) {
                break;
            }
            index++;
        }
        String finalValue = selValue;

        if (isLightDescription) {
            finalValue = Jsoup.parse(selValue).text();
            if (finalValue.length() > LightDescriptionSizeExceedException.MAX_CHARACTER_LIMIT) {
                Utils.addErrorMessage(translate("lightDescription_limit_exceed"));
                error = true;
            }
        }
        if (!error) {
            propertyTranslation
                    .set(index, new MLPropertyWrapper(finalValue, new Locale(selectedLanguage)));
            this.selectedLanguage = null;
            this.value = null;
        }
    }


    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (contentFilterLanguagesService == null) {
            contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentFilterLanguagesService();
        }
        return contentFilterLanguagesService;
    }

    /**
     * @param contentFilterLanguagesService the contentFilterLanguagesService to set
     */
    public final void setContentFilterLanguagesService(
            ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }


    public String getBrowserTitle() {
        return translate("translate_property_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("translate_property_dialog_icon_tooltip");
    }

    @Override
    public String getContainerDescription() {
        return translate("translate_property_dialog_description", i18nPropety,
                getActionNode().getName());
    }

    @Override
    public String getContainerTitle() {
        return translate("translate_property_dialog_title", i18nPropety);
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        if (value != null && !(value.length() == 0)) {
            Whitelist basicWhitelist = new Whitelist();
            basicWhitelist.addTags("p", "span", "strong", "b", "i", "u", "br", "sub", "sup", "a");
            basicWhitelist.addAttributes(":all", "style");
            this.value = Jsoup.clean(value, basicWhitelist);
        } else {
            this.value = value;
        }
    }

    /**
     * @return the displayAsArea
     */
    public final boolean isDisplayAsArea() {
        return displayAsArea;
    }

    /**
     * @param displayAsArea the displayAsArea to set
     */
    public final void setDisplayAsArea(boolean displayAsArea) {
        this.displayAsArea = displayAsArea;
    }

    private MLText getMLPropertyValue(final NodeRef nodeRef, final QName propertyQname) {
        MLText properties = null;

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            properties = (MLText) getNodeService().getProperty(nodeRef, propertyQname);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return properties;
    }

    private boolean isTextArea(final Map<String, String> parameters) {
        if (parameters == null) {
            return false;
        } else {
            final String area = parameters
                    .get(TranslateLongTextPropertyGenerator.PARAM_PROPERTY_TEXT_AREA);

            if (area == null) {
                return false;
            } else {
                return Boolean.parseBoolean(area);
            }
        }
    }

    private boolean isRichTextArea(Map<String, String> parameters) {

        if (parameters == null) {
            return true;
        } else {
            final String area = parameters
                    .get(TranslateLongTextPropertyGeneratorNoRichText.PARAM_PROPERTY_USE_RICH_TEXT);

            if (area == null) {
                return true;
            } else {
                return Boolean.parseBoolean(area);
            }
        }
    }

    /**
     * @return the displayRichText
     */
    public boolean isDisplayRichText() {
        return displayRichText;
    }

    /**
     * @param displayRichText the displayRichText to set
     */
    public void setDisplayRichText(boolean displayRichText) {
        this.displayRichText = displayRichText;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public String getMode() {
        if (isDisplayRichText()) {
            return "textareas";
        } else {
            return "none";
        }

    }

    @SuppressWarnings("unused")
    public void setMode(String mode) {

    }


    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public boolean isLightDescription() {
        return isLightDescription;
    }

    public void setLightDescription(boolean isLightDescription) {
        this.isLightDescription = isLightDescription;
    }

    public String getSelectedLanguageName() {
        String labelByCode = getContentFilterLanguagesService().getLabelByCode(selectedLanguage);
        if (labelByCode == null) {
            return selectedLanguage;
        } else {
            return labelByCode;
        }
    }

    @SuppressWarnings("unused")
    public void setSelectedLanguageName(String selectedLanguageName) {

    }


    public SelectItem[] getTextAreaTypes() {
        return textAreaTypes;
    }

    public void setTextAreaTypes(SelectItem[] textAreaTypes) {
        this.textAreaTypes = textAreaTypes;
    }

    public String getTextAreaType() {
        return textAreaType;
    }

    public void setTextAreaType(String textAreaType) {
        this.textAreaType = textAreaType;
    }

    public String getSelectedTextAreaType() {
        return selectedTextAreaType;
    }

    public void setSelectedTextAreaType(String selectedTextAreaType) {
        this.selectedTextAreaType = selectedTextAreaType;
    }

    public boolean isMachineTranslationEnabled() {
        return machineTranslationEnabled && CircabcConfig.ENT;
    }

    public void setMachineTranslationEnabled(boolean machineTranslationEnabled) {
        this.machineTranslationEnabled = machineTranslationEnabled;
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}
