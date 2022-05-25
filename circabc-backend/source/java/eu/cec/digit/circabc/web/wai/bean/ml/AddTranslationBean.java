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

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import eu.cec.digit.circabc.web.wai.bean.content.AddContentBean;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dialog bean to upload a new document and to add it to an existing MLContainer.
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class AddTranslationBean extends AddContentBean {

    public static final String BEAN_NAME = "CircabcAddTranslationBean";
    public static final String DIALOG_NAME = "addTranslationWai";
    private static final String NO_LANGUAGE_SELECTED = "null";
    private static final String MSG_NO_LOCALE = "add_translation_error_language";
    private static final long serialVersionUID = 1834222719203535L;
    private NodeRef parent;
    private NodeRef startTranslation;
    private List<SelectItem> availablesContentFilterLanguages;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        // in the restaure mode, the parameters can be null
        if (parameters != null) {
            this.startTranslation = getActionNode().getNodeRef();
            final QName actionNodeType = getActionNode().getType();

            fillAvailableLanguages();

            if (ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(actionNodeType)) {
                parent = getMultilingualContentService().getPivotTranslation(startTranslation);
                parent = getNodeService().getPrimaryParent(this.parent).getParentRef();
            } else {
                parent = getNodeService().getPrimaryParent(this.startTranslation).getParentRef();
            }
        }


    }

    private void fillAvailableLanguages() {
        final List<String> uploadedFileLanguages;
        if (getUploadedFileCount() > 0) {
            // get all selected languages
            uploadedFileLanguages = new ArrayList<>(getUploadedFileCount());

            for (final CircabcUploadedFile file : getUploadedFiles()) {
                uploadedFileLanguages.add(file.getLanguage());
            }
        } else {
            uploadedFileLanguages = Collections.emptyList();
        }

        // fill the available languages
        final SelectItem[] languagesArray = getUserPreferencesBean()
                .getAvailablesContentFilterLanguages(this.startTranslation, false);
        this.availablesContentFilterLanguages = new ArrayList<>(languagesArray.length);

        for (final SelectItem item : languagesArray) {
            if (!uploadedFileLanguages.contains((String) item.getValue())) {
                availablesContentFilterLanguages.add(item);
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        try {
            return super.finishImpl(context, outcome);
        } catch (final BusinessStackError validationErrors) {
            for (final String msg : validationErrors.getI18NMessages()) {
                Utils.addErrorMessage(msg);
            }
            this.isFinished = false;
            return null;
        }
    }

    @Override
    protected NodeRef createContent(final NodeRef parent, CircabcUploadedFile bean)
            throws FileNotFoundException, IOException {
        final NodeRef created = super.createContent(parent, bean);
        getMultilingualContentService()
                .addTranslation(created, startTranslation, I18NUtil.parseLocale(bean.getLanguage()));
        return created;
    }

    @Override
    protected String getDialogToStart() {
        return DIALOG_NAME;
    }

    @Override
    protected NodeRef getParentFolder() {
        return this.parent;
    }

    @Override
    protected void clearUpload() {
        super.clearUpload();
        this.parent = null;
        this.startTranslation = null;
        this.availablesContentFilterLanguages = null;
    }

    @Override
    public void addFile(CircabcUploadedFile fileBean) {
        if (fileBean != null) {
            final String lang = fileBean.getLanguage();
            if (lang == null || lang.length() < 1 || NO_LANGUAGE_SELECTED.equalsIgnoreCase(lang)) {
                final String message = translate(MSG_NO_LOCALE);
                ErrorsRenderer
                        .addForcedMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));

                removeFile(fileBean.getFile());
            } else {
                super.addFile(fileBean);

                //reset the available languages
                fillAvailableLanguages();
            }
        }

    }

    @Override
    protected void removeFile(File file) {
        super.removeFile(file);

        // reset the available languages
        fillAvailableLanguages();
    }

    public List<SelectItem> getUnusedLanguages() {
        return availablesContentFilterLanguages;
    }

    /**
     * @return the language
     */
    public final String getLanguage() {
        return NO_LANGUAGE_SELECTED;
    }

    /**
     * @param ignore the language to set
     */
    public final void setLanguage(String ignore) {
    }

}
