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
package eu.cec.digit.circabc.web.wai.bean.ml;

import eu.cec.digit.circabc.service.translation.TranslationService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.comparator.SelectItemLabelComparator;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.IECompatibilityPreference;
import eu.cec.digit.circabc.web.wai.dialog.ml.TranslatePropertyDialog;
import eu.cec.digit.circabc.web.wai.generator.BaseDialogLauncherGenerator;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.lang.StringUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.*;

public class MachineTranslationPropertyDialog extends BaseWaiDialog {


    public static final String DIALOG_NAME = "machineTranslationPropertyWai";
    public static final String BEAN_NAME = "MachineTranslationPropertyDialog";
    /**
     *
     */
    private static final long serialVersionUID = 499810788220945374L;
    private static final String MSG_ERROR_EMPTY_TARGET_LANGUAGE = "machine_translate_common_empty_target_langs";
    private static final String MSG_ERROR_INVALID_SOURCE_LANGUAGE = "machine_translate_common_invalid_source_lang";
    private static final String MSG_ERROR_TEXT_TO_TRANSLATE_SHOULD_NOT_BE_EMPTY = "machine_translate_text_empty";
    private String propertyId;
    private QName propertyQname;
    private String i18nPropety;
    private String sourceLanguage;
    private List<SelectItem> existingTranslations;
    private List<String> selectedTargetTranslations;
    private List<SelectItem> targetTranslations;
    private String text;
    private boolean notify;
    private boolean selectAll;
    private transient ContentFilterLanguagesService contentFilterLanguagesService;
    private transient TranslationService translationService;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            propertyId = parameters
                    .get(BaseDialogLauncherGenerator.PARAM_PROPERTY_KEY);
        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException(
                    "Node id is a mandatory parameter");
        }

        if (propertyId == null || propertyId.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "The property id is a mandatory parameter");
        }

        int idx = propertyId.indexOf(':');

        if (idx < 0) {
            propertyQname = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, propertyId);
        } else {
            final String uri = getNamespaceService().getNamespaceURI(
                    propertyId.substring(0, idx));
            propertyQname = QName.createQName(uri,
                    propertyId.substring(idx + 1));
        }

        final PropertyDefinition propertyDef = getDictionaryService()
                .getProperty(propertyQname);

        if (propertyDef == null
                || !propertyDef.getDataType().getName()
                .equals(DataTypeDefinition.MLTEXT)) {
            throw new IllegalArgumentException(
                    "The property type definition must be a MLText !!");
        }

        final MLText values = getMLPropertyValue(getActionNode().getNodeRef(),
                this.propertyQname);
        existingTranslations = new ArrayList<>();
        selectedTargetTranslations = new ArrayList<>();
        notify = false;
        selectAll = false;

        i18nPropety = translate(propertyQname.getLocalName());

        if (values != null) {

            Set<String> existingTranslationsSet = new HashSet<>();

            for (Map.Entry<Locale, String> translation : values.entrySet()) {
                final String lang = translation.getKey().toString();
                final String labelByCode = getContentFilterLanguagesService()
                        .getLabelByCode(lang);
                existingTranslations.add(new SelectItem(lang, labelByCode,
                        translation.getValue()));
                existingTranslationsSet.add(lang);
            }

            if (existingTranslations.size() > 0) {
                sourceLanguage = (String) existingTranslations.get(0)
                        .getValue();
                text = existingTranslations.get(0).getDescription();
            }
            Set<String> languages = new HashSet<>(getTranslationService()
                    .getAvailableLanguages());
            targetTranslations = new ArrayList<>();
            for (String lang : languages) {
                if (existingTranslationsSet.contains(lang.toLowerCase())) {
                    continue;
                }
                final String labelByCode = getContentFilterLanguagesService()
                        .getLabelByCode(lang.toLowerCase());
                targetTranslations.add(new SelectItem(lang, labelByCode));
            }
            Collections.sort(targetTranslations,
                    new SelectItemLabelComparator());
        }
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

    @Override
    public String getBrowserTitle() {
        return translate("translate_property_dialog_browser_title");
    }

    @Override
    public String getPageIconAltText() {
        return translate("translate_property_dialog_icon_tooltip");
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        validate();
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            isFinished = false;
            return null;
        } else {
            Set<String> languages = new HashSet<>(
                    selectedTargetTranslations);
            translationService.translateProperty(getActionNode().getNodeRef(),
                    this.propertyQname, sourceLanguage.toUpperCase(),
                    languages, notify);
            getLogRecord().addInfo("translate from " + sourceLanguage.toUpperCase() + " to " + StringUtils
                    .join(selectedTargetTranslations.iterator(), ","));
        }
        return null;

    }

    private void validate() {

        if (text == null || text.trim().equals("")) {
            Utils.addErrorMessage(translate(MSG_ERROR_TEXT_TO_TRANSLATE_SHOULD_NOT_BE_EMPTY));
        }

        //check target Language
        if (selectedTargetTranslations.size() == 0) {
            Utils.addErrorMessage(translate(MSG_ERROR_EMPTY_TARGET_LANGUAGE));
        }

        //check sourceLanguage
        final Set<String> availableLanguagesSet = getTranslationService().getAvailableLanguages();
        if (!availableLanguagesSet.contains(sourceLanguage.toUpperCase())) {
            final String availableLanguages = StringUtils.join(availableLanguagesSet.iterator(), ",");
            Utils.addErrorMessage(translate(MSG_ERROR_INVALID_SOURCE_LANGUAGE, availableLanguages));
        }

    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        super.doPostCommitProcessing(context, outcome);
        if (isFinished) {
            return cloaseAndOpenTranslatePropertyDialog();
        } else {
            return null;
        }
    }

    @Override
    public String cancel() {
        super.cancel();
        return cloaseAndOpenTranslatePropertyDialog();
    }

    public void languageChanged(ValueChangeEvent event) {
        String newLanguage = (String) event.getNewValue();
        for (SelectItem translation : existingTranslations) {
            if (translation.getValue().equals(newLanguage)) {
                text = translation.getDescription();
                break;
            }
        }
    }

    public void selectAllChanged(ValueChangeEvent event) {
        if (event.getPhaseId() != PhaseId.INVOKE_APPLICATION) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
        }

        Boolean selectAll = (Boolean) event.getNewValue();
        selectedTargetTranslations.clear();
        if (selectAll) {
            for (SelectItem item : targetTranslations) {
                selectedTargetTranslations.add(item.getValue().toString());
            }
        }
    }

    private String cloaseAndOpenTranslatePropertyDialog() {
        final Map<String, String> parameters = new HashMap<>(2);
        parameters.put("id", getActionNode().getId());
        parameters.put(BaseDialogLauncherGenerator.PARAM_PROPERTY_KEY,
                propertyId);
        Beans.getTranslatePropertyDialog().init(parameters);
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + TranslatePropertyDialog.WAI_DIALOG_CALL;
    }

    private MLText getMLPropertyValue(final NodeRef nodeRef,
                                      final QName propertyQname) {
        MLText properties = null;

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            properties = (MLText) getNodeService().getProperty(nodeRef,
                    propertyQname);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return properties;
    }

    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (contentFilterLanguagesService == null) {
            contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(
                            FacesContext.getCurrentInstance())
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

    public List<SelectItem> getExistingTranslations() {
        return existingTranslations;
    }

    public void setExistingTranslations(List<SelectItem> existingTranslations) {
        this.existingTranslations = existingTranslations;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public TranslationService getTranslationService() {
        if (translationService == null) {
            translationService = Services.getCircabcServiceRegistry(
                    FacesContext.getCurrentInstance()).getTranslationService();
        }
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public List<SelectItem> getTargTranslations() {
        return targetTranslations;
    }

    public void setTargTranslations(List<SelectItem> targTranslations) {
        this.targetTranslations = targTranslations;
    }

    public List<SelectItem> getTargetTranslations() {
        return targetTranslations;
    }

    public void setTargetTranslations(List<SelectItem> targetTranslations) {
        this.targetTranslations = targetTranslations;
    }

    public List<String> getSelectedTargetTranslations() {
        return selectedTargetTranslations;
    }

    public void setSelectedTargetTranslations(
            List<String> selectedTargetTranslations) {
        this.selectedTargetTranslations = selectedTargetTranslations;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAllItems) {
        this.selectAll = selectAllItems;
    }

    @Override
    public String getMode() {
        return IECompatibilityPreference.IE_9;
    }
}
