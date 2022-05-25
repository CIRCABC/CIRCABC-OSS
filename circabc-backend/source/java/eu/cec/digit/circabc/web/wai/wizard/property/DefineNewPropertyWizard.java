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
package eu.cec.digit.circabc.web.wai.wizard.property;

import eu.cec.digit.circabc.repo.dynamic.property.DynamicPropertyImpl;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.wizard.BaseWaiWizard;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.text.MessageFormat;
import java.util.*;

public class DefineNewPropertyWizard extends BaseWaiWizard {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "DefineNewPropertyWizard";
    public static final String MSG_ERROR_MULTI_VALUE_REQUIRED = "define_property_dialog_error_multi_value_required";
    public static final String MSG_ERROR_TRANSLATION_REQUIRED = "define_property_dialog_error_no_translation";
    public static final String MSG_ERROR_LOCALE_REQUIRED = "define_property_dialog_error_locale_required";
    public static final String MSG_ERROR_VALUE_REQUIRED = "define_property_dialog_error_value_required";
    public static final String MSG_ERROR_LOCALE_DUPLICATED = "define_property_dialog_error_locale_duplicated";
    public static final String MSG_SUMMARY_TYPE = "define_property_dialog_property_type";
    public static final String MSG_SUMMARY_DESC = "define_property_dialog_property_desc";
    public static final String MSG_TYPE_PREFIX = "define_property_dialog_property_type_";
    private static final long serialVersionUID = -6962091031742104333L;
    private static final Log logger = LogFactory.getLog(DefineNewPropertyWizard.class);
    protected List<DynPropertyWrapper> propertyTranslations;
    private transient DataModel translationDataModel = null;
    private String language;
    private String text;
    private String type;
    private String validValues;
    private List<SelectItem> types = new ArrayList<>();
    private transient ContentFilterLanguagesService contentFilterLanguagesService;
    private transient DynamicPropertyService propertiesService;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        // ensure to not erase values when it is not required
        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException("Node id is a mandatory parameter");
            }

            if (!NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode())) {
                throw new IllegalArgumentException("The node id must be an interest group");
            }

            translationDataModel = null;
            propertyTranslations = new ArrayList<>(30);
            language = null;
            text = null;
            type = null;
            validValues = null;

            if (this.types.size() == 0) {
                for (final DynamicPropertyType t : DynamicPropertyType.values()) {
                    this.types.add(new SelectItem(t.name(), translate(t)));
                }

                type = DynamicPropertyType.TEXT_AREA.toString();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.wizard.BaseWizardBean#next()
     */
    @Override
    public String next() {
        //  get the current step
        final int step = Application.getWizardManager().getState().getCurrentStep();

        if (step == 2) {
            // at least one translation must be defined
            if (getTranslationDataModel().getRowCount() < 1) {
                Utils.addErrorMessage(translate(MSG_ERROR_TRANSLATION_REQUIRED));

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Impossible to add a Dynamic property to the Interest Group " + getActionNode()
                                    .getName() + " because the list of translation is empty.");
                }

                Application.getWizardManager().getState().setCurrentStep(step - 1);
            }
        } else if (step == 3) {
            // if the type is selection, check if at least one value is defined
            if (type.equals(DynamicPropertyType.SELECTION.name()) || type
                    .equals(DynamicPropertyType.MULTI_SELECTION.name())) {
                final String trimMultiValue = validValues.trim();
                if (trimMultiValue.length() < 1) {
                    Utils.addErrorMessage(translate(MSG_ERROR_MULTI_VALUE_REQUIRED));

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Impossible to add a Dynamic property to the Interest Group " + getActionNode()
                                        .getName() + " because the available values list is empty.");
                    }

                    Application.getWizardManager().getState().setCurrentStep(step - 1);
                }
            }
        }

        return super.next();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final MLText labels = new MLText();

        for (final DynPropertyWrapper wrapper : propertyTranslations) {
            labels.addValue(wrapper.getLocale(),
                    getSecurityService().getCleanHTML(wrapper.getValue(), false));
        }

        final StringBuilder formatedValues = new StringBuilder("");
        if (type.equals(DynamicPropertyType.SELECTION.name()) || type
                .equals(DynamicPropertyType.MULTI_SELECTION.name())) {
            final StringTokenizer tokens = new StringTokenizer(validValues.trim(),
                    String.valueOf(DynamicPropertyService.MULTI_VALUES_SEPARATOR), false);

            // remove blank lines.
            String line = null;
            while (tokens.hasMoreTokens()) {
                line = ((String) tokens.nextElement()).trim();

                if (line.length() > 0) {
                    formatedValues
                            .append(line)
                            .append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
                }
            }
        }

        final DynamicPropertyImpl dynamicProperty = new DynamicPropertyImpl(labels,
                DynamicPropertyType.valueOf(type), formatedValues.toString());

        String info = MessageFormat
                .format("the property {0} has been created - {1}", dynamicProperty.getName(),
                        dynamicProperty.toString());
        logRecord.setInfo(info);
        final NodeRef ig = this.getActionNode().getNodeRef();
        this.getPropertiesService().addDynamicProperty(ig, dynamicProperty);

        if (logger.isDebugEnabled()) {
            logger.debug("Property " + dynamicProperty.getName()
                    + " successfully created under the interest group " + getActionNode().getName()
                    + ". With value: " + dynamicProperty.toString());
        }

        return outcome;
    }

    public void addDescription(ActionEvent event) {
        final UIInput input = (UIInput) event.getComponent().findComponent("define-property-text");

        final UISelectOne select = (UISelectOne) event.getComponent()
                .findComponent("define-property-language");

        final String selLang = (String) select.getValue();
        final String selValue = ((String) input.getValue()).trim();

        boolean error = false;

        if (selValue.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_VALUE_REQUIRED));
            error = true;
        }

        if (selLang.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_REQUIRED));
            error = true;
        }

        final Locale selLocale = I18NUtil.parseLocale(selLang);

        for (DynPropertyWrapper wrapper : propertyTranslations) {
            if (selLocale.equals(wrapper.getLocale())) {
                Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_DUPLICATED));
                error = true;
                break;
            }
        }

        if (!error) {
            propertyTranslations.add(
                    new DynPropertyWrapper(getSecurityService().getCleanHTML(selValue, false), selLocale));
            text = null;
        }
    }

    /**
     * Returns the properties for current keywords translations JSF DataModel
     *
     * @return JSF DataModel representing the translation of the keyword
     */
    public DataModel getTranslationDataModel() {
        if (this.translationDataModel == null) {
            this.translationDataModel = new ListDataModel();
        }

        this.translationDataModel.setWrappedData(this.propertyTranslations);

        return this.translationDataModel;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a keyword translation
     */
    public void removeSelection(ActionEvent event) {
        DynPropertyWrapper wrapper = (DynPropertyWrapper) this.translationDataModel.getRowData();
        if (wrapper != null) {
            this.propertyTranslations.remove(wrapper);
        }
    }

    public String getSummary() {
        String[] labels = new String[]
                {
                        translate(MSG_SUMMARY_TYPE),
                        translate(MSG_SUMMARY_DESC)
                };

        final DynamicPropertyType enumType = DynamicPropertyType.valueOf(type);

        final StringBuilder buff = new StringBuilder("");
        boolean first = true;
        for (DynPropertyWrapper wrapper : propertyTranslations) {
            if (first) {
                first = false;
            } else {
                buff.append(", ");
            }

            buff.append('(')
                    .append(wrapper.getLanguage())
                    .append(") ")
                    .append(wrapper.getValue());
        }

        String[] values = new String[]
                {
                        translate(enumType),
                        buff.toString()
                };

        return super.buildSummary(labels, values);
    }

    private String translate(DynamicPropertyType type) {
        return translate(MSG_TYPE_PREFIX + type.name().toLowerCase());
    }

    /**
     * @return the validValues
     */
    public String getValidValues() {
        return validValues;
    }

    /**
     * @param validValues the validValues to set
     */
    public void setValidValues(String validValues) {
        this.validValues = validValues;
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
     * Method calls by the wizard to get the available langages.
     *
     * @return the list of languages where at least one property define
     */
    public SelectItem[] getLanguages() {
        // get the list of filter languages
        final List<String> languages = getContentFilterLanguagesService().getFilterLanguages();

        final List<String> filteredLanguages = new ArrayList<>(languages.size());
        filteredLanguages.addAll(languages);

        for (final DynPropertyWrapper wrapper : propertyTranslations) {
            if (wrapper.getLocale() != null) {
                filteredLanguages.remove(wrapper.getLocale().getLanguage());
            } else {
                throw new IllegalStateException(
                        "The Dynamic property " + wrapper + " has not language defined. ");
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
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }


    public List<SelectItem> getTypes() {
        return this.types;
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

    /**
     * @return the propertiesService
     */
    protected final DynamicPropertyService getPropertiesService() {
        if (propertiesService == null) {
            propertiesService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getDynamicPropertieService();
        }
        return propertiesService;
    }

    /**
     * @param propertiesService the propertiesService to set
     */
    public final void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.propertiesService = dynamicPropertyService;
    }

    public String getBrowserTitle() {
        return translate("new_dynamic_property_title");
    }

    public String getPageIconAltText() {
        return translate("new_dynamic_property_icon");
    }

    public boolean isFormProvided() {
        return false;
    }
}
