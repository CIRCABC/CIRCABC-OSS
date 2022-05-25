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

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.ResolverHelper;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean that back the WAI search browse.
 *
 * @author yanick pignot
 */
public class SearchResultDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "SearchResultDialog";
    public static final String DIALOG_NAME = "searchResultDialogWai";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    private static final Log logger = LogFactory.getLog(SearchResultDialog.class);
    /**
     *
     */
    private static final long serialVersionUID = -7654499139347041599L;
    private static final String UNEXPECTED_ERROR = "Unexpected error:";

    private static final String TIME_TO_QUERY_AND_BUILD_MAP_NODES = "Time to query and build map nodes: ";

    private static final String SEARCH_MINIMUM = "search_minimum";

    private static final String SEARCH_FAILED_FOR = "Search failed for: ";

    private static final String END_PARENTHESE = " )";

    private static final String AND = "\" ) AND ( ";

    private static final String PATH = "( PATH:\"";

    private static final String SEARCH_RESULTS_RETURNED = "Search results returned: ";

    private static final String SEARCH_FOR = "Search for: ";

    private static final String SEARCH_QUERY = " search query: ";

    private static final String MISSING_OBJECT_RETURNED_FROM_SEARCH_INDEXES_ID = "Missing object returned from search indexes: id = ";

    private static final String TYPE = ", type = ";

    private static final String FOUND_INVALID_OBJECT_IN_DATABASE_ID = "Found invalid object in database: id = ";

    private static final String MS = "ms";

    private LibraryBean libraryBean = null;

    private List<NavigableNode> containers = null;
    private List<NavigableNode> contents = null;
    private List<NavigableNode> posts = null;

    private SearchContext searchContext;

    private boolean isNew;


    public void init(final Map<String, String> parameters) {
        super.init(parameters);
    }

    public void reset() {
        containers = null;
        contents = null;
        posts = null;
        searchContext = null;
    }

    public boolean hasSearchAvailableInCache() {
        return searchContext != null;
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean#getContainers()
     */
    public List<NavigableNode> getContainers() {
        if (containers == null) {
            fillBrowseNodes(getSearchContext());
        }
        return containers != null ? containers : Collections.<NavigableNode>emptyList();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean#getContents()
     */
    public List<NavigableNode> getContents() {
        if (contents == null) {
            fillBrowseNodes(getSearchContext());
        }
        return contents != null ? contents : Collections.<NavigableNode>emptyList();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean#getContents()
     */
    public List<NavigableNode> getPosts() {
        if (posts == null) {
            fillBrowseNodes(getSearchContext());
        }
        return posts != null ? posts : Collections.<NavigableNode>emptyList();
    }


    protected SearchContext getSearchContext() {
        if (searchContext == null) {
            searchContext = getNavigator().getSearchContext();
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
     * @param libraryBean the libraryBean to set
     */
    public final void setLibraryBean(LibraryBean libraryBean) {
        this.libraryBean = libraryBean;
    }

    public boolean isGuest() {
        return getNavigator().isGuest();
    }

    public ActionsListWrapper getActionList() {
        if (!isGuest()) {
            if (isNew) {
                return new ActionsListWrapper(getNavigator().getCurrentNode(),
                        "search_result_new_actions_wai");
            } else {
                return new ActionsListWrapper(getNavigator().getCurrentNode(),
                        "search_result_edit_actions_wai");
            }
        } else {
            return null;
        }

    }

    public String getInformationMessage() {
        final String text = (getSearchContext() != null) ? getSearchContext().getText() : "";
        final int size = getContainers().size() + getContents().size() + getPosts().size();

        return translate("search_results_text", text, size);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return AdvancedSearchDialog.WAI_DIALOG_CALL;
    }

    @Override
    protected String getDefaultCancelOutcome() {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                CircabcNavigationHandler.OUTCOME_SEPARATOR +
                CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("search_results_new_search");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public String getBrowserTitle() {
        return translate("search_results_browser_title");
    }

    public String getPageIconAltText() {
        return translate("search_results_icon_tooltip");
    }

    /**
     * Query a list of nodes for the specified Search Context<br> Based on
     * BrowseBean.searchBrowseNodes()
     *
     * @param searchContext the search context
     */
    private void fillBrowseNodes(final SearchContext searchContext) {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();

        final int minimumSearchLength = Application.getClientConfig(context).getSearchMinimum();

        // Serach under the igRoot - Not here to hasty
        // if search for entire ig root ... for instance only library
        // searchContext.setLocation(WebClientHelper.getPathFromSpaceRef(getNavigator().getCurrentIGRoot().getNodeRef(),true));

        // get the searcher object to build the query
        final String buildQuery = searchContext.buildQuery(minimumSearchLength);
        if (buildQuery == null) {
            // failed to build a valid query, the user probably did not enter the
            // minimum text required to construct a valid search
            Utils.addErrorMessage(translate(SEARCH_MINIMUM, minimumSearchLength));
            this.containers = Collections.emptyList();
            this.contents = Collections.emptyList();
            this.posts = Collections.emptyList();
            return;
        }

        //Then add the location - the whole IG
        String pathFrom = WebClientHelper
                .getPathFromSpaceRef(getNavigator().getCurrentIGRoot().getNodeRef(), true);

        // Build the query
        final String query = PATH + pathFrom + AND + buildQuery + END_PARENTHESE;

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                // perform the search against the repo
                ResultSet results = null;
                SearchParameters sp;
                try {
                    // Limit search to the first 100 matches
                    sp = new SearchParameters();
                    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                    sp.setQuery(query);
                    sp.addStore(Repository.getStoreRef());

                    int searchLimit = Application.getClientConfig(FacesContext.getCurrentInstance())
                            .getSearchMaxResults();
                    if (searchLimit > 0) {
                        sp.setLimitBy(LimitBy.FINAL_SIZE);
                        sp.setLimit(searchLimit);
                    }

                    results = getSearchService().query(sp);

                    final int searchSize = results.length();

                    if (logger.isDebugEnabled()) {
                        logger.debug(SEARCH_FOR + query);
                        logger.debug(SEARCH_RESULTS_RETURNED + searchSize);
                    }

                    // create a list of items from the results
                    containers = new ArrayList<>(results.length());
                    contents = new ArrayList<>(results.length());
                    posts = new ArrayList<>(results.length());

                    if (results.length() != 0) {
                        QName type;
                        NodeRef nodeRef;
                        FileInfo fileInfo;
                        TypeDefinition typeDef;
                        for (final ResultSetRow row : results) {
                            try {

                                nodeRef = row.getNodeRef();
                                fileInfo = getFileFolderService().getFileInfo(nodeRef);
                                if (getNodeService().exists(nodeRef)) {
                                    // find it's type so we can see if it's a node we are interested in
                                    type = getNodeService().getType(nodeRef);

                                    // make sure the type is defined in the data dictionary
                                    typeDef = getDictionaryService().getType(type);

                                    if (typeDef != null) {
                                        if (ContentModel.TYPE_CONTENT.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createContentRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                contents.add(node);
                                            }
                                        } else if (ForumModel.TYPE_POST.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createPostRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                posts.add(node);
                                            }
                                        } else if (ForumModel.TYPE_TOPIC.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createTopicRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                containers.add(node);
                                            }
                                        } else if (ForumModel.TYPE_FORUM.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createForumRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                containers.add(node);
                                            }
                                        } else if (ContentModel.TYPE_FOLDER.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createFolderRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                containers.add(node);
                                            }
                                        } else if (ApplicationModel.TYPE_FILELINK.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createFileLinkRepresentation(fileInfo, getNodeService(),
                                                            getBrowseBean());

                                            if (node != null) {
                                                contents.add(node);
                                            }
                                        } else if (ApplicationModel.TYPE_FOLDERLINK.equals(type)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createFolderLinkRepresentation(fileInfo, getNodeService(),
                                                            getBrowseBean());

                                            if (node != null) {
                                                containers.add(node);
                                            }
                                        } else if (
                                                getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER) == true &&
                                                        getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)
                                                                == false) {
                                            final NavigableNode node = ResolverHelper
                                                    .createFolderRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                containers.add(node);
                                            }
                                        } else if (getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT)) {
                                            final NavigableNode node = ResolverHelper
                                                    .createContentRepresentation(fileInfo, getNodeService(), getBrowseBean());

                                            if (node != null) {
                                                contents.add(node);
                                            }
                                        }

                                        // inform any listeners that a Node wrapper has been created
                                        /*
                                         * if (node != null) { for (NodeEventListener listener : getNodeEventListeners()) { listener.created(node, type); } }
                                         */
                                    } else {
                                        if (logger.isWarnEnabled()) {
                                            logger.warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                                        }
                                    }
                                } else {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn(
                                                MISSING_OBJECT_RETURNED_FROM_SEARCH_INDEXES_ID + nodeRef + SEARCH_QUERY
                                                        + query);
                                    }
                                }

                            } catch (final InvalidNodeRefException refErr) {
                                Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr));
                            }

                        }
                    }

                } catch (final Throwable err) {
                    if (logger.isInfoEnabled()) {
                        logger.info(SEARCH_FAILED_FOR + query);
                    }
                    Utils.addErrorMessage(translate(Repository.ERROR_SEARCH, err.getMessage()), err);
                    containers = Collections.emptyList();
                    contents = Collections.emptyList();
                    posts = Collections.emptyList();

                    if (logger.isErrorEnabled()) {
                        logger.error(UNEXPECTED_ERROR + err.getMessage(), err);
                    }
                } finally {
                    if (results != null) {
                        results.close();
                    }
                }

                return null;
            }
        };
        txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug(TIME_TO_QUERY_AND_BUILD_MAP_NODES + (endTime - startTime) + MS);
        }
    }

    public void setNew(boolean b) {
        isNew = b;
    }

}



