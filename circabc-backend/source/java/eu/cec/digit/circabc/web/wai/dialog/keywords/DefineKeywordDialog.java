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
package eu.cec.digit.circabc.web.wai.dialog.keywords;

import eu.cec.digit.circabc.repo.keywords.KeywordImpl;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Bean that backs the "Define new Keyword" WAI Dialog.
 *
 * @author Yanick Pignot
 */
public class DefineKeywordDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "DefineKeywordDialog";
    public static final String MSG_ERROR_TRANSLATION_REQUIRED = "add_new_keyword_dialog_error_no_translation";
    public static final String MSG_ERROR_LOCALE_REQUIRED = "add_new_keyword_dialog_error_locale_required";
    public static final String MSG_ERROR_VALUE_REQUIRED = "add_new_keyword_dialog_error_value_required";
    public static final String MSG_ERROR_LOCALE_DUPLICATED = "add_new_keyword_dialog_error_locale_duplicated";
    private static final String COMPONENT_ADD_KEYWORD_LANGUAGE = "add-keyword-language";
    private static final String COMPONENT_ADD_KEYWORD_VALUE = "add-keyword-value";
    private static final long serialVersionUID = 7766640743571143699L;
    /**
     * Logger (coppepa: logger must be final)
     */
    private static final Log logger = LogFactory.getLog(DefineKeywordDialog.class);

    /**
     * datamodel for table of translations for users
     */
    private transient DataModel translationDataModel = null;
    private List<KeywordWrapper> keywordTranslations = null;
    private Keyword keyWordToUpdate = null;

    private String value;
    private String language;

    private transient KeywordsService keywordsService;
    private transient ContentFilterLanguagesService contentFilterLanguagesService;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        // ensure to not erase values when it is not required
        if (parameters != null) {
            value = null;
            language = null;
            keywordTranslations = new ArrayList<>(30);
            keyWordToUpdate = null;
            translationDataModel = null;

            final String key = parameters.get(ManageKeywordsDialog.KEYWORD_PARAMETER);

            if (key != null) {
                keyWordToUpdate = getKeywordsService().buildKeywordWithId(key);
                fillFieldsForEditMode(keyWordToUpdate);
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // a keyword can only be created under an IG root
        if (!NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode())) {
            logger.error("A keyword can only be added under an IG Root");

            throw new IllegalArgumentException("A keyword can only be added under an IG Root");
        }

        // at least one translation must be defined
        if (getTranslationDataModel().getRowCount() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_TRANSLATION_REQUIRED));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Impossible to add a keyword to the Interest Group " + getActionNode().getName()
                                + " because the list of translation is empty.");
            }

            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to add translation to the Interest Group " + getActionNode().getName() + "...");
        }

        final MLText mlText = new MLText();
        //	set the keyword translations
        for (final KeywordWrapper keyword : keywordTranslations) {
            mlText.addValue(keyword.getLocale(), keyword.getValue());
        }

        // we are in the create mode
        if (keyWordToUpdate == null) {
            Keyword key = new KeywordImpl(mlText);

            key = getKeywordsService().createKeyword(
                    getActionNode().getNodeRef(),
                    key);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Keyword  successfully created for the Interest Group " + getActionNode().getName()
                                + "\n	ig: " + getActionNode().getNodeRef()
                                + "\n	keywod: " + key);
            }
        }
        // we are in the edit mode, use the keyword defined
        else {
            keyWordToUpdate.setTranlatations(mlText);

            getKeywordsService().updateKeyword(keyWordToUpdate);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Keyword  successfully updated for the Interest Group " + getActionNode().getName()
                                + "\n	ig: " + getActionNode().getNodeRef()
                                + "\n	keywod: " + keyWordToUpdate);
            }
        }

        return outcome;
    }

    public void addSelection(ActionEvent event) {
        final UIInput input = (UIInput) event.getComponent().findComponent(COMPONENT_ADD_KEYWORD_VALUE);
        final UISelectOne select = (UISelectOne) event.getComponent()
                .findComponent(COMPONENT_ADD_KEYWORD_LANGUAGE);

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

        for (KeywordWrapper wrapper : keywordTranslations) {
            if (selLang.equalsIgnoreCase(wrapper.getLanguage())) {
                Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_DUPLICATED));
                error = true;
                break;
            }
        }

        if (!error) {
            keywordTranslations.add(new KeywordWrapper(null, selValue, new Locale(selLang)));
            this.value = null;
        }
    }

    /**
     * Method calls by the dialog to getr the available langages.
     *
     * @return the list of languages where at least one keyword define
     */
    public SelectItem[] getLanguages() {
        // get the list of filter languages
        final List<String> languages = getContentFilterLanguagesService().getFilterLanguages();

        final List<String> filteredLanguages = new ArrayList<>(languages.size());
        filteredLanguages.addAll(languages);

        for (final KeywordWrapper wrapper : keywordTranslations) {
            if (wrapper.getLocale() != null) {
                filteredLanguages.remove(wrapper.getLocale().getLanguage());
            } else {
                logger.warn("The Keyword wrapper " + wrapper
                        + " has not language defined. A non multilingual keyword should only be create at the import time.");
                throw new IllegalStateException("The Keyword wrapper " + wrapper
                        + " has not language defined. A non multilingual keyword should only be create at the import time.");
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
     * Returns the properties for current keywords translations JSF DataModel
     *
     * @return JSF DataModel representing the translation of the keyword
     */
    public DataModel getTranslationDataModel() {
        if (this.translationDataModel == null) {
            this.translationDataModel = new ListDataModel();
        }

        this.translationDataModel.setWrappedData(this.keywordTranslations);

        return this.translationDataModel;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a keyword translation
     */
    public void removeSelection(ActionEvent event) {
        KeywordWrapper wrapper = (KeywordWrapper) this.translationDataModel.getRowData();
        if (wrapper != null) {
            this.keywordTranslations.remove(wrapper);
        }
    }


    public String getBrowserTitle() {
        return translate("add_new_keyword_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("add_new_keyword_dialog_icon_tooltip");
    }

    protected void fillFieldsForEditMode(Keyword keyword) {
        if (keyword.isKeywordTranslated()) {
            final MLText values = keyword.getMLValues();
            for (Map.Entry<Locale, String> translation : values.entrySet()) {
                keywordTranslations
                        .add(new KeywordWrapper(keyword.getId(), translation.getValue(), translation.getKey()));
            }
        } else {
            // the keyword is not translated, force the user to translate it
            setValue(keyword.getValue());
        }
    }


    /**
     * @return the keywordsService
     */
    protected final KeywordsService getKeywordsService() {
        if (keywordsService == null) {
            keywordsService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getKeywordsService();
        }
        return keywordsService;
    }

    /**
     * @param keywordsService the keywordsService to set
     */
    public final void setKeywordsService(KeywordsService keywordsService) {
        this.keywordsService = keywordsService;
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
        this.value = value;
    }
}
