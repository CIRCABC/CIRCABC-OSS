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
package eu.cec.digit.circabc.web.wai.dialog.admin;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * Searches and displays a list of users with actions to view their accounts and IGs they are
 * members of.
 *
 * @author schwerr
 */
public class ViewUserMembershipsDialog extends BaseWaiDialog {

    public static final String MSG_PAGE_TITLE = "members_home_title";
    public static final String MSG_PAGE_DESCRIPTION = "members_home_title_desc";
    public static final String MSG_NUMBER_OF_MEMBERS = "directory_number_of_members_search";
    private static final long serialVersionUID = -5640001324291280449L;
    private static final String IMAGES_ICONS_USERS_LARGE_GIF = "/images/icons/users_large.gif";
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(
            ViewUserMembershipsDialog.class);
    /**
     * Error message if use enter 0,1 or characters for search
     */
    private static final String ERROR_CIRCABC_MIN_SEARCH_CHARS = "error_circabc_min_search_chars";
    /**
     * The I18N message string for size error on member request
     */
    protected String err_member_too_long = "members_home_error_too_many_member";
    /**
     * Transient list of profiles
     */
    protected SelectItem[] profiles = null;
    protected SelectItem[] pageSizes = new SelectItem[]{new SelectItem(10),
            new SelectItem(20), new SelectItem(50), new SelectItem(100),
            new SelectItem(200)};
    /**
     * The authorityService service reference
     */
    transient private AuthorityService authorityService = null;
    /**
     * The personService service reference
     */
    transient private PersonService personService = null;
    /**
     * The domain filter. Possible values : 'allusers' ; 'cec' or 'circabc'
     */
    private String domain = null;
    /**
     * The text to search for
     */
    private String name = null;
    private Integer pageSize = null;
    private List<SearchResultRecord> searchResult = Collections.emptyList();

    private SelectItem[] filters = null;

    private boolean isCategory;

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
     */
    @Override
    public String getPageIconAltText() {
        return translate("view_user_memberships_icon_tooltip");
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
     */
    @Override
    public String getBrowserTitle() {
        return translate("view_user_memberships_browser_title");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext,
     * java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        return outcome;
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#cancel()
     */
    @Override
    public String cancel() {
        searchResult.clear();
        return super.cancel();
    }

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.DIRECTORY;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION, getActionNode().getName());
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getPageIcon() {
        return IMAGES_ICONS_USERS_LARGE_GIF;
    }

    /**
     * Init any state that may be held by this bean
     */
    public void init(final Map<String, String> parameters) {

        super.init(parameters);
        isCategory = getActionNode().hasAspect(CircabcModel.ASPECT_CATEGORY);
        // Reset vars
        this.domain = PermissionUtils.ALLUSERS;
        this.name = "";
    }

    /**
     * Perform the search with filter criteria
     *
     * @return null (Always as we stay on the same page)
     */
    public String search() {

        // Reset the previous result set
        this.searchResult = Collections.emptyList();

        // Force user to enter more than 3 chars
        if (this.name.trim().length() < 3) {
            String errorMessage = translate(ERROR_CIRCABC_MIN_SEARCH_CHARS);
            Utils.addErrorMessage(errorMessage);
            return null;
        }

        if (this.domain == null || this.name == null) {
            if (logger.isErrorEnabled()) {
                logger.error("search: Parameters are null for user |"
                        + ((NavigationBean) FacesHelper.getManagedBean(
                        FacesContext.getCurrentInstance(),
                        NavigationBean.BEAN_NAME)).getCurrentUser()
                        .getUserName() + "|");
            }
            return null;
        } else {
            this.searchResult = getUserService().
                    getUsersByDomainFirstNameLastNameEmail(this.domain,
                            this.name.trim(), true);
            if (isCategory) {
                this.searchResult = filterCategoryMembers();
            }
            return "wai:dialog:close:wai:dialog:viewUserMembershipsDialog";
        }
    }

    private List<SearchResultRecord> filterCategoryMembers() {
        List<SearchResultRecord> result = new ArrayList<>(this.searchResult.size());
        ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(getActionNode().getNodeRef());
        Set<String> users = profileManagerService.getInvitedUsers(getActionNode().getNodeRef());
        users.addAll(profileManagerService.getAllSubUsers(getActionNode().getNodeRef()));
        for (SearchResultRecord item : this.searchResult) {
            if (users.contains(item.getId())) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Get the list of profile for filter use
     *
     * @return List of profiles
     */
    public SelectItem[] getProfiles() {
        return profiles;
    }

    public SelectItem[] getFilters() {
        if (filters == null) {
            filters = PermissionUtils.getDomainFilters(false, false, false);
        }
        return filters;
    }

    public SelectItem[] getPageSizes() {
        return pageSizes;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            return getBrowseBean().getListElementNumber();
        }
        return pageSize;
    }

    public void setPageSize(Integer number) {
        if (number != null) {
            pageSize = number;
        }
    }

    /**
     * Change listener for the page size select box
     */
    public void updatePageSize(final ValueChangeEvent event) {
        setPageSize((Integer) event.getNewValue());
    }

    /**
     * Get the list of results users
     *
     * @return List of results users
     */
    public List<SearchResultRecord> getSearchResult() {
        return searchResult;
    }

    /***
     *
     * @return number of members in this IG
     */
    public String getUserCount() {
        return translate(MSG_NUMBER_OF_MEMBERS, getSearchResult().size());
    }

    /**
     * Getter for domain
     *
     * @return The domain value
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Setter for domain
     *
     * @param name The domain to set
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Change listener for the domain
     */
    public void updateDomain(final ValueChangeEvent event) {
        this.domain = (String) event.getNewValue();
    }

    /**
     * Getter for name
     *
     * @return The name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Change listener for the name
     */
    public void updateName(final ValueChangeEvent event) {
        this.name = (String) event.getNewValue();
    }

    /**
     * @return the authorityService
     */
    protected final AuthorityService getAuthorityService() {
        if (authorityService == null) {
            authorityService = Services.getAlfrescoServiceRegistry(
                    FacesContext.getCurrentInstance()).getAuthorityService();
        }
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(
            final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(
                    FacesContext.getCurrentInstance()).getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(final PersonService personService) {
        this.personService = personService;
    }
}
