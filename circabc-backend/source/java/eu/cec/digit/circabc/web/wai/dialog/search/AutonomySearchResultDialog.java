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
package eu.cec.digit.circabc.web.wai.dialog.search;

import eu.cec.digit.circabc.service.search.autonomy.AutonomyResultNode;
import eu.cec.digit.circabc.service.search.autonomy.AutonomyResults;
import eu.cec.digit.circabc.service.search.autonomy.AutonomySearchService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.search.SearchContextDelegate;

import javax.faces.context.FacesContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dialog that displays the result of the Autonomy search.
 *
 * @author schwerr
 */
public class AutonomySearchResultDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "AutonomySearchResultDialog";
    public static final String DIALOG_NAME = "autonomySearchResultDialogWai";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    private static final long serialVersionUID = 7975211112348148486L;
    protected AutonomySearchService autonomySearchService = null;

    protected SearchContext searchContext = null;

    private AutonomyResults autonomyResults = null;

    private LibraryBean libraryBean = null;

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
    }

    public void reset() {
        autonomyResults = null;
        searchContext = null;
    }

    public List<AutonomyResultNode> getResults() {

        if (autonomyResults == null) {
            fillBrowseNodes(getSearchContext());
        }

        return autonomyResults.getError() == null ? autonomyResults.getResults() :
                Collections.<AutonomyResultNode>emptyList();
    }

    /**
     * Query Autonomy for the specified Search Context.
     *
     * @param searchContext the search context
     */
    private void fillBrowseNodes(final SearchContext searchContext) {
        autonomyResults = autonomySearchService.search(searchContext.getText());
    }

    public String getInformationMessage() {

        getResults();

        if (autonomyResults != null && autonomyResults.getError() == null) {
            final String text = searchContext != null ? searchContext.getText() : "";
            return translate("search_results_text", text, autonomyResults.getTotalHits());
        } else if (autonomyResults != null) {
            return translate("autonomy_search_results_error") + ": " +
                    autonomyResults.getError();
        } else {
            return "Error during search. AutonomyResults is null.";
        }
    }

    protected SearchContext getSearchContext() {

        if (searchContext == null) {

            searchContext = getNavigator().getSearchContext();

            if (searchContext instanceof SearchContextDelegate) {
                searchContext =
                        ((SearchContextDelegate) searchContext).getSearchContext();
            }
        }

        return searchContext;
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean#getListPageSize();
     */
    public int getListPageSize() {
        return getLibraryBean().getListPageSize();
    }

    /**
     * @return the libraryBean
     */
    protected final LibraryBean getLibraryBean() {

        if (libraryBean == null) {
            libraryBean = (LibraryBean) Beans.getBean(LibraryBean.BEAN_NAME);
        }

        return libraryBean;
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getCancelButtonLabel()
     */
    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    @Override
    public String getPageIconAltText() {
        return translate("search_results_icon_tooltip");
    }

    @Override
    public String getBrowserTitle() {
        return translate("search_results_browser_title");
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        return CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE;
    }

    /**
     * Sets the value of the autonomySearchService
     *
     * @param autonomySearchService the autonomySearchService to set.
     */
    public void setAutonomySearchService(AutonomySearchService autonomySearchService) {
        this.autonomySearchService = autonomySearchService;
    }
}
