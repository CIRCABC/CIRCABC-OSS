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
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.comparator.SelectItemLabelComparator;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.IECompatibilityPreference;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.*;
import java.util.Map.Entry;

public class MachineTranslationContentDialog extends BaseWaiDialog {

    public static final String DIALOG_NAME = "machineTranslationContentWai";
    public static final String BEAN_NAME = "MachineTranslationContentDialog";


    private static final String MSG_ERROR_INVALID_FILE_TYPE = "machine_translate_doc_err_invalid_file_type";
    private static final String MSG_ERROR_DOCUMENT_IS_TOO_BIG = "machine_translate_doc_err_too_big";
    private static final String MSG_ERROR_INVALID_SOURCE_LANGUAGE = "machine_translate_common_invalid_source_lang";
    private static final String MSG_ERROR_EMPTY_TARGET_LANGUAGE = "machine_translate_common_empty_target_langs";

    private static final Log logger = LogFactory.getLog(MachineTranslationContentDialog.class);


    /**
     *
     */
    private static final long serialVersionUID = 7164481741289779169L;


    private String sourceLanguage;
    private List<SelectItem> existingTranslations;
    private List<String> selectedTargetTranslations;
    private List<SelectItem> targetTranslations;
    private boolean notify;
    private boolean selectAll;

    private transient ContentFilterLanguagesService contentFilterLanguagesService;
    private transient TranslationService translationService;
    private NodeRef selectedNodeRef;
    private NodeRef copyOfDocument;


    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
        if (parameters != null) {
            existingTranslations = new ArrayList<>();
            selectedTargetTranslations = new ArrayList<>();
            targetTranslations = new ArrayList<>();
            notify = false;
            selectAll = false;

            final NodeRef nodeRef = getActionNode().getNodeRef();
            final Map<Locale, NodeRef> translations = getMultilingualContentService()
                    .getTranslations(nodeRef);
            final NodeRef pivotTranslation = getMultilingualContentService().getPivotTranslation(nodeRef);

            Set<String> existingTranslationsSet = new HashSet<>();
            for (Entry<Locale, NodeRef> item : translations.entrySet()) {

                final NodeRef nodeRefValue = item.getValue();
                final String lang = item.getKey().toString();
                final String labelByCode = getContentFilterLanguagesService()
                        .getLabelByCode(lang);
                if (getNodeService()
                        .hasAspect(nodeRefValue, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
                    // ignore empty translation
                    existingTranslationsSet.add(lang);
                    continue;
                }
                existingTranslations.add(new SelectItem(lang, labelByCode,
                        nodeRefValue.toString()));
                if (pivotTranslation.equals(nodeRefValue)) {
                    sourceLanguage = lang;
                    selectedNodeRef = nodeRefValue;
                }
                existingTranslationsSet.add(lang);
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

    public void languageChanged(ValueChangeEvent event) {
        String newLanguage = (String) event.getNewValue();
        for (SelectItem translation : existingTranslations) {
            if (translation.getValue().equals(newLanguage)) {
                selectedNodeRef = new NodeRef(translation.getDescription());
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

    @Override
    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        validate();
        if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
            isFinished = false;
            return null;
        } else {
            try {
                copyOfDocument = getTranslationService()
                        .copyDocumentToBeTranslated(selectedNodeRef);
                Long id = (Long) getNodeService().getProperty(selectedNodeRef, ContentModel.PROP_NODE_DBID);
                String name = (String) getNodeService().getProperty(copyOfDocument, ContentModel.PROP_NAME);
                int lastIndexOf = name.lastIndexOf(".");
                String newName = String.valueOf(id);
                if (lastIndexOf > -1) {
                    String extension = name.substring(lastIndexOf + 1);
                    newName = newName + "." + extension;
                }
                getNodeService().setProperty(copyOfDocument, ContentModel.PROP_NAME, newName);

                isFinished = true;
                return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
            } catch (Exception e) {
                logger.error("Error when copy document that need to be machine translated ", e);
                Utils.addErrorMessage(
                        translate(Repository.ERROR_GENERIC, e.getMessage()), e);
                isFinished = false;
                return null;
            }
        }
    }

    private void validate() {
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
        // check file extension
        if (!getTranslationService().canBeTranslated(
                getNodeService().getProperty(selectedNodeRef, ContentModel.PROP_NAME).toString())) {
            final String fileExtensions = StringUtils
                    .join(getTranslationService().getAvailableFileExtensions().iterator(), ",");
            Utils.addErrorMessage(translate(MSG_ERROR_INVALID_FILE_TYPE, fileExtensions));
        }
        // check file size
        final ContentData content = (ContentData) getNodeService()
                .getProperty(selectedNodeRef, ContentModel.PROP_CONTENT);
        final long maxFileSizeInBytes = getTranslationService().fileMaxSize();
        if (content != null && content.getSize() > maxFileSizeInBytes) {
            final String maxFileSize = FileUtils.byteCountToDisplaySize(maxFileSizeInBytes);
            Utils.addErrorMessage(translate(MSG_ERROR_DOCUMENT_IS_TOO_BIG, maxFileSize));
        }
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (isFinished) {
            Set<String> languages = new HashSet<>(selectedTargetTranslations);
            getTranslationService()
                    .translateDocument(selectedNodeRef, copyOfDocument, sourceLanguage.toUpperCase(),
                            languages, notify);
            getLogRecord().addInfo("translate from " + sourceLanguage.toUpperCase() + " to " + StringUtils
                    .join(selectedTargetTranslations.iterator(), ","));
            return outcome;
        } else {
            return null;
        }

    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }


    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }


    public List<SelectItem> getExistingTranslations() {
        return existingTranslations;
    }


    public void setExistingTranslations(List<SelectItem> existingTranslations) {
        this.existingTranslations = existingTranslations;
    }


    public List<String> getSelectedTargetTranslations() {
        return selectedTargetTranslations;
    }


    public void setSelectedTargetTranslations(
            List<String> selectedTargetTranslations) {
        this.selectedTargetTranslations = selectedTargetTranslations;
    }


    public List<SelectItem> getTargetTranslations() {
        return targetTranslations;
    }


    public void setTargetTranslations(List<SelectItem> targetTranslations) {
        this.targetTranslations = targetTranslations;
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


    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
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


    public TranslationService getTranslationService() {
        if (translationService == null) {
            translationService = Services.getCircabcServiceRegistry(
                    FacesContext.getCurrentInstance()).getNonSecuredTranslationService();
        }
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public String getMode() {
        return IECompatibilityPreference.IE_9;
    }

}
