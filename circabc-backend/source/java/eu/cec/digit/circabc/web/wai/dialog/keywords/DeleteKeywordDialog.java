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

import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Bean that backs the "Delete Keyword" WAI Dialog.
 *
 * @author Yanick Pignot
 */
public class DeleteKeywordDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "DeleteKeywordDialog";
    private static final long serialVersionUID = 6677076443571143699L;
    /**
     * Logger (coppepa: logger must be final)
     */
    private static final Log logger = LogFactory.getLog(DeleteKeywordDialog.class);

    private static final String MSG_ERROR_NODE_LOCKED = "delete_keyword_dialog_error_node_locked";

    private transient KeywordsService keywordsService;
    private Keyword keywordToDelete = null;
    private List<Keyword> selectedKeywords;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            final String keyAsString = parameters.get(ManageKeywordsDialog.KEYWORD_PARAMETER);
            keywordToDelete = null;

            if (keyAsString == null && parameters.get("all") == null) {
                throw new IllegalArgumentException(
                        "Impossible to delete the keyword if the keyword is not set in the parameters with the key "
                                + ManageKeywordsDialog.KEYWORD_PARAMETER);
            }

            selectedKeywords = new ArrayList<>();

            if (Boolean.valueOf(parameters.get("all"))) {
                for (String nRef : parameters.get("selectedKeywords").split(",")) {
                    nRef = nRef.replace("[", "");
                    nRef = nRef.replace("]", "");
                    nRef = nRef.trim();

                    selectedKeywords.add(keywordsService.buildKeywordWithId(nRef));
                }
            }

            if (keyAsString != null) {
                keywordToDelete = getKeywordsService().buildKeywordWithId(keyAsString);
                selectedKeywords.add(keywordToDelete);
            }


        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Trying to delete the keyword " + keywordToDelete.toString() + " ...");
        }

        try {

            if (selectedKeywords != null) {
                for (Keyword k : selectedKeywords) {
                    getKeywordsService().removeKeyword(k);
                }
            }
        } catch (NodeLockedException e) {
            Utils.addErrorMessage(translate(MSG_ERROR_NODE_LOCKED));

            if (logger.isDebugEnabled()) {
                logger.debug("Impossible to delete a keyword since documents are locked");
            }

            return null;
        }

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug("Time to delete the keyword and to remove from it related documents: " + (endTime
                    - startTime) + "ms");
        }

        return outcome;
    }

    public String getKeywordTranslations() {
        String value = null;

        if (keywordToDelete.isKeywordTranslated()) {
            value = keywordToDelete.getMLValues().getValues().toString();
            value = value.substring(1, value.length() - 1);
        } else {
            value = keywordToDelete.getValue();
        }

        return value;
    }

    public String getBrowserTitle() {
        return translate("delete_keyword_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("delete_keyword_dialog_icon_tooltip");
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
     * @return the keywordToDelete
     */
    public Keyword getKeywordToDelete() {
        return keywordToDelete;
    }

    /**
     * @param keywordToDelete the keywordToDelete to set
     */
    public void setKeywordToDelete(Keyword keywordToDelete) {
        this.keywordToDelete = keywordToDelete;
    }

    /**
     * @return the selectedKeywords
     */
    public List<Keyword> getSelectedKeywords() {
        return selectedKeywords;
    }

    /**
     * @param selectedKeywords the selectedKeywords to set
     */
    public void setSelectedKeywords(List<Keyword> selectedKeywords) {
        this.selectedKeywords = selectedKeywords;
    }


}
