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

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.bean.override.CircabcUserPreferencesBean;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import eu.cec.digit.circabc.web.ui.repo.converter.KeywordConverter;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.search.SearchContext;
import org.alfresco.web.bean.search.SearchContextDelegate;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.repo.component.UISearchCustomProperties;
import org.apache.lucene.queryParser.QueryParser;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.cec.digit.circabc.model.DocumentModel.*;
import static org.alfresco.model.ContentModel.PROP_LOCALE;

/**
 * Bean that back the WAI advanced search.
 *
 * @author yanick pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 TODO QueryParser is taken from Lucene?
 */
public class AdvancedSearchDialog extends
        org.alfresco.web.bean.search.AdvancedSearchDialog implements WaiDialog {

    public static final String RESET = "reset";
    public static final String BEAN_NAME = "CircabcAdvancedSearchDialog";
    public static final String DIALOG_NAME = "advancedSearchDialogWai";
    public static final String WAI_DIALOG_CALL =
            CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    protected static final String NO_SELECTION = "NONE";
    private static final String MSG_SELECT_SAVED_SEARCH = "select_saved_search";
    private static final long serialVersionUID = -8191901120224724519L;
    private static final String SAVED_SEARCHES_USER = "user";
    private static final String SAVED_SEARCHES_GLOBAL = "global";


    private static final String SPACE_STRORE_STRING = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
            .toString();

    private boolean forceAnd = false;

    private CircabcUserPreferencesBean userPreferencesBean;
    private transient KeywordsService keywordsService;
    private transient DynamicPropertyService dynamicPropertyService;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        boolean reset;

        if (parameters != null) {
            reset = parameters.containsKey(RESET) ? Boolean.parseBoolean(parameters.get(RESET)) : true;
        } else {
            reset = false;
        }

        if (reset) {
            reset(null);
            this.forceAnd = false;
        }

        // Sets the initial location to know where to save the search
        // location path search
        NodeRef location;

        if (properties.getLookin().equals(SearchProperties.LOOKIN_INTEREST_GROUP)) {
            location = ((CircabcNavigationBean) navigator).getCurrentIGRoot().getNodeRef();
        } else if (properties.getLookin().equals(SearchProperties.LOOKIN_CURRENT_SERVICE)) {
            location = getCurrentServiceRoot();
        } else {
            location = ((CircabcNavigationBean) navigator).getCurrentNode().getNodeRef();
        }

        properties.setLocation(location);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // apply the alfresco search options
        super.search();

//           final SearchContext search = navigator.getSearchContext();
        final SearchContext originalSearch = navigator.getSearchContext();
        CircabcSearchContext search = new CircabcSearchContext(originalSearch);
        navigator.setSearchContext(search);

        final SearchProperties circabcProperties = (SearchProperties) super.properties;

        search.setForceAndTerms(forceAnd);

        if (circabcProperties.getStatus() != null && !circabcProperties.getStatus()
                .equals(SearchProperties.ANY_VALUE)) {
            search.addFixedValueQuery(PROP_STATUS, circabcProperties.getStatus());
        }
        if (circabcProperties.getSecurityRanking() != null && !circabcProperties.getSecurityRanking()
                .equals(SearchProperties.ANY_VALUE)) {
            search.addFixedValueQuery(PROP_SECURITY_RANKING, circabcProperties.getSecurityRanking());
        }

        if (circabcProperties.getReference() != null
                && circabcProperties.getReference().length() != 0) {
            search.addAttributeQuery(PROP_REFERENCE, circabcProperties.getReference());
        }

        if (circabcProperties.getUrl() != null && circabcProperties.getUrl().length() != 0) {
            search.addAttributeQuery(PROP_URL, circabcProperties.getUrl());
        }

        if (circabcProperties.isIssueDateChecked()) {
            final SimpleDateFormat df = CachingDateFormat.getDateFormat();
            final String strIssueDateFrom = df.format(circabcProperties.getIssueDateFrom());
            final String strIssueDateTo = df.format(circabcProperties.getIssueDateTo());
            search.addRangeQuery(PROP_ISSUE_DATE, strIssueDateFrom, strIssueDateTo, true);
        }
        if (circabcProperties.isExpirationDateChecked()) {
            final SimpleDateFormat df = CachingDateFormat.getDateFormat();
            final String strExpDateFrom = df.format(circabcProperties.getExpirationDateFrom());
            final String strExpDateTo = df.format(circabcProperties.getExpirationDateTo());
            search.addRangeQuery(PROP_EXPIRATION_DATE, strExpDateFrom, strExpDateTo, true);
        }

        // category path search
        if (circabcProperties.getKeywords() != null && circabcProperties.getKeywords().size() > 0) {
            final List<Keyword> keywords = circabcProperties.getKeywords();
            final int keySize = keywords.size();

            // use this workaround because the search context provided by alfresco can only
            // build query with ONE search criteria by qname. But here, we need one cunjunction by keyword.
            final StringBuilder uglyWorkaround = new StringBuilder("");

            NodeRef keywordRef;
            for (int x = 0; x < keySize; ++x) {
                keywordRef = keywords.get(x).getId();

                uglyWorkaround.append("+@").
                        append(QueryParser.escape(DocumentModel.PROP_KEYWORD.toString())).
                        append(":\"\\").append(QueryParser.escape(keywordRef.toString())).
                        append("\\*\"");

                if (x < keySize - 1) {
                    uglyWorkaround.append(" AND ");
                }

            }

            search.addRawQuery(uglyWorkaround.toString());

        }

        if (circabcProperties.getSelectedLanguageOption() != null && !circabcProperties
                .getSelectedLanguageOption().equals(SearchProperties.LANGUAGE_ALL)) {
            if (circabcProperties.getSelectedLanguageOption().equals(SearchProperties.LANGUAGE_CURRENT)) {
                final String userLang = getUserPreferencesBean().getContentFilterLanguage();

                if (UserPreferencesBean.MSG_CONTENTALLLANGUAGES.equalsIgnoreCase(userLang)) {
                    circabcProperties.setLanguage(null);
                } else {
                    circabcProperties.setLanguage(userLang);
                }

            }

            if (circabcProperties.getLanguage() != null) {
                search.addAttributeQuery(PROP_LOCALE, '*' + circabcProperties.getLanguage() + '*');
            }
        }

        final List<DynamicProperty> interestGroupDynamicProperties = getInterestGroupDynamicProperties();
        final Map<QName, DynamicProperty> map = new HashMap<>(interestGroupDynamicProperties.size());
        for (DynamicProperty dynamicProperty : interestGroupDynamicProperties) {
            final QName propertyQname = getDynamicPropertyService().getPropertyQname(dynamicProperty);
            map.put(propertyQname, dynamicProperty);
        }

        int i = 1;
        for (QName qName : DocumentModel.ALL_DYN_PROPS) {
            Serializable dynamicProperty = (Serializable) circabcProperties.getDynamicProperty(i);
            if (dynamicProperty != null && !dynamicProperty.toString().equals("")) {
                final DynamicProperty property = map.get(qName);
                if (property.getType().equals(DynamicPropertyType.MULTI_SELECTION)) {
                    final String validValues = property.getValidValues().trim();
                    final String value = dynamicProperty.toString();
                    if (validValues.contains(value)) {
                        dynamicProperty = "*" + value + "*";
                    }
                }
            }
            setDynamicProperty(qName, dynamicProperty, search);
            i++;
        }

        if (circabcProperties.getSelectedDeepOption() != null && !circabcProperties
                .getSelectedDeepOption().equals(SearchProperties.DEEP_OPTION_LATEST_VERSION)) {
            throw new NoSuchAlgorithmException(
                    "Perform search on the versions store is not implemented yet !!!");
        }

        NodeRef location;

        // location path search
        if (circabcProperties.getLookin().equals(SearchProperties.LOOKIN_INTEREST_GROUP)) {
            location = ((CircabcNavigationBean) navigator).getCurrentIGRoot().getNodeRef();
        } else if (circabcProperties.getLookin().equals(SearchProperties.LOOKIN_CURRENT_SERVICE)) {
            location = getCurrentServiceRoot();
        } else {
            location = ((CircabcNavigationBean) navigator).getCurrentNode().getNodeRef();
        }

        // don't use the the folder and content type
        circabcProperties.setFolderType(null);
        circabcProperties.setContentType(null);

        search.setLocation(SearchContext.getPathFromSpaceRef(location, true));

        Beans.getSearchResultDialog().reset();
        if (properties.getSavedSearch().equalsIgnoreCase(NO_SELECTION)) {
            Beans.getSearchResultDialog().setNew(true);
        } else {
            Beans.getSearchResultDialog().setNew(false);
        }

        return SearchResultDialog.WAI_DIALOG_CALL;
    }

    /**
     * Select the saved searches and filter out the ones that do not belong to this IG
     */
    public List<SelectItem> getSavedSearches() {

        String savedSearchMode = properties.getSavedSearchMode();
        boolean searchSaveGlobal = properties.isSearchSaveGlobal();
        List<SelectItem> savedSearches = new ArrayList<>();
        try {
            String locationRef = null;

            if (properties.getLocation() != null) {
                locationRef = properties.getLocation().toString();
            }

            if (locationRef != null) {
                properties.getCachedSavedSearches().put(null);
                properties.setSavedSearchMode(SAVED_SEARCHES_USER);
                List<SelectItem> userSavedSearches = getOptimizedSavedSearches(locationRef);

                properties.getCachedSavedSearches().put(null);
                properties.setSavedSearchMode(SAVED_SEARCHES_GLOBAL);
                List<SelectItem> globalSavedSearches = getOptimizedSavedSearches(locationRef);
                for (SelectItem savedSearch : userSavedSearches) {

                    if ("NONE".equals((String) savedSearch.getValue())) {
                        savedSearches.add(savedSearch);
                        continue;
                    }

                    savedSearches.add(savedSearch);

                }

                for (SelectItem savedSearch : globalSavedSearches) {

                    if ("NONE".equals((String) savedSearch.getValue())) {
                        continue;
                    }
                    savedSearches.add(savedSearch);
                }
            }
        } finally {
            properties.setSavedSearchMode(savedSearchMode);
            properties.setSearchSaveGlobal(searchSaveGlobal);
        }
        return savedSearches;
    }


    /**
     * Get saved searches for for given interest group
     *
     * @param locationRef reference of location where search is performed normaly interest group node
     *                    ref
     * @return list of saved searches as SelectItem objects
     */
    private List<SelectItem> getOptimizedSavedSearches(String locationRef) {
        List<SelectItem> savedSearches = properties.getCachedSavedSearches().get();
        if (savedSearches == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ServiceRegistry services = Repository.getServiceRegistry(fc);

            // get the searches list from the current user or global searches location
            NodeRef searchesRef = null;
            if (SAVED_SEARCHES_USER.equals(properties.getSavedSearchMode())) {
                if (userSearchFolderExists()) {
                    searchesRef = getUserSearchesRef();
                }
            } else if (SAVED_SEARCHES_GLOBAL.equals(properties.getSavedSearchMode())) {
                searchesRef = getGlobalSearchesRef();
            }

            // read the content nodes under the folder
            if (searchesRef != null) {
                DictionaryService dd = services.getDictionaryService();

                List<ChildAssociationRef> childRefs = getNodeService()
                        .getChildAssocsByPropertyValue(searchesRef, CircabcModel.PROP_LOCATION, locationRef);

                savedSearches = new ArrayList<>(childRefs.size() + 1);
                if (childRefs.size() != 0) {
                    for (ChildAssociationRef ref : childRefs) {
                        Node childNode = new Node(ref.getChildRef());
                        if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT)) {
                            savedSearches.add(new SelectItem(childNode.getId(), childNode.getName()));
                        }
                    }

                    // make sure the list is sorted by the label
                    QuickSort sorter = new QuickSort(savedSearches, "label", true,
                            IDataContainer.SORT_CASEINSENSITIVE);
                    sorter.sort();
                }
            } else {
                // handle missing/access denied folder case
                savedSearches = new ArrayList<>(1);
            }

            // add an entry (at the start) to instruct the user to select a saved search
            savedSearches.add(0, new SelectItem(NO_SELECTION,
                    Application.getMessage(FacesContext.getCurrentInstance(), MSG_SELECT_SAVED_SEARCH)));

            // store in the cache (will auto-expire)
            properties.getCachedSavedSearches().put(savedSearches);
        }

        return savedSearches;
    }

    private boolean userSearchFolderExists() {
        boolean result = false;
        if (properties.getUserSearchesRef() == null) {
            NodeRef globalRef = getGlobalSearchesRef();
            if (globalRef != null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                User user = Application.getCurrentUser(fc);
                String userName = ISO9075.encode(user.getUserName());
                String xpath =
                        NamespaceService.APP_MODEL_PREFIX + ":" + QName.createValidLocalName(userName);

                List<NodeRef> results = null;
                try {
                    results = getSearchService().selectNodes(
                            globalRef,
                            xpath,
                            null,
                            getNamespaceService(),
                            false);
                } catch (AccessDeniedException err) {
                    // ignore and return null
                }

                if (results != null) {
                    if (results.size() == 1) {
                        result = true;
                    }
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void selectSearch(ActionEvent event) {
        /*
         * Ugly workaround, we call twice the load but it is the best way to not
         * maintain a pack of copy - pasted code.
         *
         **/

        if (!NO_SELECTION.equals(properties.getSavedSearch())) {
            // read an XML serialized version of the SearchContext object
            final NodeRef searchSearchRef = new NodeRef(Repository.getStoreRef(),
                    properties.getSavedSearch());
            final ServiceRegistry services = Repository
                    .getServiceRegistry(FacesContext.getCurrentInstance());
            final ContentService cs = services.getContentService();
            try {
                if (services.getNodeService().exists(searchSearchRef)) {
                    final ContentReader reader = cs.getReader(searchSearchRef, ContentModel.PROP_CONTENT);
                    final String xml = reader.getContentString();
                    final SearchContext search = new SearchContext().fromXML(xml);
                    final SearchContext delegate = new SearchContextDelegate(search).fromXML(xml);

                    this.forceAnd = delegate.getForceAndTerms();

                }
            } catch (Throwable ignore) {
            } finally {
                super.selectSearch(event);
            }
        }
    }

    @Override
    public void reset(ActionEvent event) {
        super.reset(event);
        resetFields();

        Beans.getSearchResultDialog().reset();
    }

    private void resetFields() {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;

        circabcProperties.setSecurityRankings(null);
        circabcProperties.setStatuses(null);
        circabcProperties.setKeywords(null);
        circabcProperties.setLanguages(null);
        circabcProperties.setStatus(null);
        circabcProperties.setReference(null);
        circabcProperties.setSecurityRanking(null);
        circabcProperties.setIssueDateChecked(false);
        circabcProperties.setExpirationDateChecked(false);
        circabcProperties.setIssueDateFrom(null);
        circabcProperties.setIssueDateTo(null);
        circabcProperties.setExpirationDateFrom(null);
        circabcProperties.setExpirationDateTo(null);
        circabcProperties.setDynamicProperty1(null);
        circabcProperties.setDynamicProperty2(null);
        circabcProperties.setDynamicProperty3(null);
        circabcProperties.setDynamicProperty4(null);
        circabcProperties.setDynamicProperty5(null);
        circabcProperties.setDynamicProperty6(null);
        circabcProperties.setDynamicProperty7(null);
        circabcProperties.setDynamicProperty8(null);
        circabcProperties.setDynamicProperty9(null);
        circabcProperties.setDynamicProperty10(null);
        circabcProperties.setDynamicProperty11(null);
        circabcProperties.setDynamicProperty12(null);
        circabcProperties.setDynamicProperty13(null);
        circabcProperties.setDynamicProperty14(null);
        circabcProperties.setDynamicProperty15(null);
        circabcProperties.setDynamicProperty16(null);
        circabcProperties.setDynamicProperty17(null);
        circabcProperties.setDynamicProperty18(null);
        circabcProperties.setDynamicProperty19(null);
        circabcProperties.setDynamicProperty20(null);
        circabcProperties.setUrl(null);
        circabcProperties.setLanguage(null);
        circabcProperties.setSelectedDeepOption(SearchProperties.DEEP_OPTION_LATEST_VERSION);
        circabcProperties.setSelectedLanguageOption(SearchProperties.LANGUAGE_ALL);
        circabcProperties.setLookin(SearchProperties.LOOKIN_INTEREST_GROUP);
    }

    private NodeRef getCurrentServiceRoot() {
        final NavigableNode service = ((CircabcNavigationBean) navigator).getCurrentIGService();

        return (NodeRef) service.get(IGServicesNode.SERVICE_ROOT);
    }

    /**
     * @return Returns a list of different security rankings values to allow the user to select from
     */
    public List<SelectItem> getSecurityRankings() {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;

        if ((circabcProperties.getSecurityRankings() == null) || (Application
                .isDynamicConfig(FacesContext.getCurrentInstance()))) {
            circabcProperties.setSecurityRankings(new ArrayList<SelectItem>(5));

            for (final String securityRanking : SECURITY_RANKINGS) {
                circabcProperties.getSecurityRankings()
                        .add(new SelectItem(securityRanking, translate(securityRanking.toLowerCase())));
            }
        }

        return circabcProperties.getSecurityRankings();
    }


    /**
     * @return the keywords as a display string
     */
    public String getDisplayKeywords() {
        return KeywordConverter
                .getDisplayString(FacesContext.getCurrentInstance(), getKeywords(), Boolean.TRUE);
    }

    /**
     * @return the keywords
     */
    public List<Keyword> getKeywords() {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;
        return circabcProperties.getKeywords();
    }

    /**
     * Change listener for the method select box
     */
    public void updateSavedSearch(ValueChangeEvent event) {
        final String newValue = (String) event.getNewValue();
        if (newValue == null) {
            return;
        }

        setSavedSearch(newValue);
        selectSearch(null);

        resetFields();

        final SearchProperties circabcProperties = (SearchProperties) properties;
        final Map<String, Object> additionalProperties = properties.getCustomProperties();

        if ((Boolean) additionalProperties.get(PROP_EXPIRATION_DATE.toString()) != null) {
            circabcProperties.setExpirationDateChecked(
                    (Boolean) additionalProperties.get(PROP_EXPIRATION_DATE.toString()));
            circabcProperties.setExpirationDateFrom((Date) additionalProperties
                    .get(UISearchCustomProperties.PREFIX_DATE_FROM + PROP_EXPIRATION_DATE.toString()));
            circabcProperties.setExpirationDateTo((Date) additionalProperties
                    .get(UISearchCustomProperties.PREFIX_DATE_TO + PROP_EXPIRATION_DATE.toString()));
        }

        if ((Boolean) additionalProperties.get(PROP_ISSUE_DATE.toString()) != null) {
            circabcProperties
                    .setIssueDateChecked((Boolean) additionalProperties.get(PROP_ISSUE_DATE.toString()));
            circabcProperties.setIssueDateFrom((Date) additionalProperties
                    .get(UISearchCustomProperties.PREFIX_DATE_FROM + PROP_ISSUE_DATE.toString()));
            circabcProperties.setIssueDateTo((Date) additionalProperties
                    .get(UISearchCustomProperties.PREFIX_DATE_TO + PROP_ISSUE_DATE.toString()));
        }

        if (additionalProperties.get(PROP_LOCALE.toString()) != null) {
            final String lang = (String) additionalProperties.get(PROP_LOCALE.toString());
            final int length = lang.length();

            if (length > 2) {
                circabcProperties.setSelectedLanguageOption(SearchProperties.LANGUAGE_SPECIFY);
                circabcProperties.setLanguage(lang.substring(1, length - 1));
            }
        }

        circabcProperties.setSecurityRanking((String) additionalProperties
                .get(UISearchCustomProperties.PREFIX_LOV_ITEM + PROP_SECURITY_RANKING.toString()));
        circabcProperties.setStatus((String) additionalProperties
                .get(UISearchCustomProperties.PREFIX_LOV_ITEM + PROP_STATUS.toString()));
        circabcProperties.setReference((String) additionalProperties.get(PROP_REFERENCE.toString()));
        circabcProperties.setUrl((String) additionalProperties.get(PROP_URL.toString()));
        circabcProperties
                .setKeywords(parseKeywordQuery(additionalProperties.get(PROP_KEYWORD.toString())));

        // !!! don't save or restaure the dynamic properties !!! // Why?
        // Probably because dynamic properties are placeholders and users can update them invalidating saved searches?
        // It has been decided that this will be responsability of the user
        // Another approach would be to invalidate all saved searches

        int i = 1;
        for (QName qName : DocumentModel.ALL_DYN_PROPS) {
            Serializable dynamicProperty = (Serializable) additionalProperties.get(qName.toString());
            circabcProperties.setDynamicProperty(i, dynamicProperty);
            i++;
        }
    }

    public String getSavedSearch() {
        return super.properties.getSavedSearch();
    }

    public void setSavedSearch(String savedSearch) {
        super.properties.setSavedSearch(savedSearch);
    }

    public boolean isLookinCurrentLocationDisable() {
        final NavigableNodeType nodeType = ((CircabcNavigationBean) navigator).getCurrentNodeType();

        return nodeType == null || !nodeType.isStrictlyUnder(NavigableNodeType.IG_ROOT);
    }

    /**
     * @param keywords to set
     */
    public void setKeyword(List<Keyword> keywords) {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;
        circabcProperties.setKeywords(keywords);
    }

    /**
     * @return Returns a list of different status values to allow the user to select from
     */
    public List<SelectItem> getStatuses() {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;

        if ((circabcProperties.getStatuses() == null) || (Application
                .isDynamicConfig(FacesContext.getCurrentInstance()))) {
            circabcProperties.setStatuses(new ArrayList<SelectItem>(5));

            for (final String status : STATUS_VALUES) {
                circabcProperties.getStatuses()
                        .add(new SelectItem(status, translate(status.toLowerCase())));
            }
        }

        return circabcProperties.getStatuses();
    }

    public List<DynamicProperty> getInterestGroupDynamicProperties() {
        final NavigableNode interestGroup = ((CircabcNavigationBean) navigator).getCurrentIGRoot();

        if (interestGroup == null) {
            return null;
        } else {
            return getDynamicPropertyService().getDynamicProperties(interestGroup.getNodeRef());
        }
    }

    /**
     * @return Returns a list of different languages options to allow the user to select from
     */
    public List<SelectItem> getLanguages() {
        final SearchProperties circabcProperties = (SearchProperties) super.properties;

        if ((circabcProperties.getLanguages() == null) || (Application
                .isDynamicConfig(FacesContext.getCurrentInstance()))) {
            final SelectItem[] languagesAsArray = getUserPreferencesBean()
                    .getContentFilterLanguages(false);
            circabcProperties.setLanguages(new ArrayList<SelectItem>(languagesAsArray.length));
            Collections.addAll(circabcProperties.getLanguages(), languagesAsArray);
        }

        return circabcProperties.getLanguages();
    }

    protected String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    private List<Keyword> parseKeywordQuery(Object object) {
        if (object == null) {
            return null;
        }

        final StringTokenizer tokens = new StringTokenizer(object.toString(), "*", false);

        final List<Keyword> keywords = new ArrayList<>(tokens.countTokens());

        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.startsWith(SPACE_STRORE_STRING)) {
                keywords.add(getKeywordsService().buildKeywordWithId(token));
            }
        }

        return keywords;
    }

    private void setDynamicProperty(final QName propQname, final Serializable value,
                                    final SearchContext search) {

        if (value != null && value.toString().trim().length() > 0) {
            if (value instanceof Date) {
                final SimpleDateFormat df = CachingDateFormat.getDateFormat();

                final Calendar calendarFrom = GregorianCalendar.getInstance();
                calendarFrom.setTime((Date) value);
                final Calendar calendarTo = (Calendar) calendarFrom.clone();

                calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
                calendarFrom.set(Calendar.MINUTE, 0);
                calendarFrom.set(Calendar.SECOND, 0);

                calendarTo.set(Calendar.HOUR_OF_DAY, 23);
                calendarTo.set(Calendar.MINUTE, 59);
                calendarTo.set(Calendar.SECOND, 59);

                search.addRangeQuery(propQname, df.format(calendarFrom.getTime()),
                        df.format(calendarTo.getTime()), true);

            } else {
                search.addAttributeQuery(propQname, value.toString());
            }
        }
    }

    @Override
    protected String getDefaultCancelOutcome() {
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME +
                CircabcNavigationHandler.OUTCOME_SEPARATOR +
                CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("search");
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public String getBrowserTitle() {
        return translate("advanced_search_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("advanced_search_dialog_icon_tooltip");
    }

    public boolean isCancelButtonVisible() {
        return true;
    }

    public boolean isFormProvided() {
        return false;
    }

    /**
     * @return the userPreferencesBean
     */
    protected final CircabcUserPreferencesBean getUserPreferencesBean() {
        if (userPreferencesBean == null) {
            userPreferencesBean = Beans.getUserPreferencesBean();
        }
        return userPreferencesBean;
    }

    /**
     * @param userPreferencesBean the userPreferencesBean to set
     */
    public final void setUserPreferencesBean(CircabcUserPreferencesBean userPreferencesBean) {
        this.userPreferencesBean = userPreferencesBean;
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
     * @return the dynamicPropertyService
     */
    protected final DynamicPropertyService getDynamicPropertyService() {
        if (dynamicPropertyService == null) {
            dynamicPropertyService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getDynamicPropertieService();
        }
        return dynamicPropertyService;
    }

    /**
     * @param dynamicPropertyService the dynamicPropertyService to set
     */
    public final void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.dynamicPropertyService = dynamicPropertyService;
    }

    /**
     * @return the forceAnd
     */
    public final boolean isForceAnd() {
        return forceAnd;
    }

    /**
     * @param forceAnd the forceAnd to set
     */
    public final void setForceAnd(boolean forceAnd) {
        this.forceAnd = forceAnd;
    }
}



