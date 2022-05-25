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
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.search.AdvancedSearchDialog;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Bean that backs the "Set Keyword to a document" WAI Dialog.
 *
 * @author Yanick Pignot
 */
public class SelectKeywordsForSearchDialog extends SelectKeywordsBaseDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "SelectKeywordsForSearchDialog";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + "selectKeywordsForSearchDialogWai";
    private static final long serialVersionUID = -2381287486464195753L;
    /**
     * Advanced search dialog reference
     */
    private AdvancedSearchDialog advancedSearchDialog;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            initKeywords();
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        final List<Keyword> keywordsAsList = new ArrayList<>(settedKeywords.size());
        keywordsAsList.addAll(settedKeywords.values());

        getAdvancedSearchDialog().setKeyword(keywordsAsList);

        return outcome;
    }

    @Override
    protected void initKeywords() {
        settedKeywordsDataModel = null;

        super.initKeywords();

        // get the keywords assigned to the document and fill the map
        final List<Keyword> docKeywordsList = getAdvancedSearchDialog().getKeywords();

        if (docKeywordsList != null) {
            settedKeywords = new HashMap<>(docKeywordsList.size());
            for (final Keyword keyword : docKeywordsList) {
                settedKeywords.put(keyword.getId(), keyword);
            }
        } else {
            settedKeywords = new HashMap<>(10);
        }

    }

    public String getDialogCloseAndLaunchAction() {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR + WAI_DIALOG_CALL;
    }


    @Override
    public String getBrowserTitle() {
        return translate("select_keyword_for_search_dialog_browser_title");
    }

    @Override
    public String getPageIconAltText() {
        return translate("select_keyword_for_search_dialog_icon_tooltip");
    }

    @Override
    public Node getInterestGroup() {
        return getNavigator().getCurrentIGRoot();
    }

    /**
     * @return the advancedSearchDialog
     */
    public AdvancedSearchDialog getAdvancedSearchDialog() {
        if (advancedSearchDialog == null) {
            advancedSearchDialog = Beans.getAdvancedSearchDialog();
        }
        return advancedSearchDialog;
    }

    /**
     * @param advancedSearchDialog the advancedSearchDialog to set
     */
    public void setAdvancedSearchDialog(AdvancedSearchDialog advancedSearchDialog) {
        this.advancedSearchDialog = advancedSearchDialog;
    }


}
