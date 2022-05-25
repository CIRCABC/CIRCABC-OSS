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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.UIActionLink;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Bean that backs the navigation inside the information service
 *
 * @author yanick pignot
 */
public class InformationBean extends LibraryBean {

    /**
     * Logger
     */
    //private static final Log logger = LogFactory.getLog(InformationBean.class);

    public static final String JSP_NAME = "information-home.jsp";
    public static final String BEAN_NAME = "InformationBean";
    public static final String MSG_PAGE_TITLE = "information_home_title";
    public static final String MSG_PAGE_DESCRIPTION = "information_home_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "information_home_icon_tooltip";
    private static final String FILE_SEPARATOR = "/";
    /**
     *
     */
    private static final long serialVersionUID = -1167164595469111193L;
    private static final String BROWSE_REPO = "information_ation_browse_repository_title";
    private static final String BROWSE_REPO_TOOLTIP = "information_ation_browse_repository_tooltip";
    private static final String BROWSE_REPO_LISTENER = "InformationBean.browse";

    private static final String KEY_BROWSE = "__inf_browse";

    /**
     * The action to be displayed
     */
    private List<ActionWrapper> actions = null;

    private Boolean browse = false;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.INFORMATION;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getPageIcon() {
        return IMAGES_ICONS + getCurrentNode().getProperties().get(ICON) + GIF;
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    @Override
    public String getBrowserTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null && parameters.containsKey(KEY_BROWSE)) {
            browse = Boolean.valueOf(parameters.get(KEY_BROWSE));
        }
        // stop browsing when click on the information root.
        else if (NavigableNodeType.INFORMATION.equals(getNavigator().getCurrentNodeType())) {
            browse = Boolean.FALSE;
        }

    }

    public boolean isEditingView() {
        return browse && getCurrentNode().hasPermission(InformationPermissions.INFMANAGE.toString());
    }

    public boolean isRenderNewWindow() {
        final Map<String, Object> properties = getCurrentNode().getProperties();
        final Object adapt = properties.get(CircabcModel.PROP_INF_ADAPT.toString());

        return isIndexFileFound() && adapt != null && !(Boolean) adapt;
    }


    public String getWebdavUrl() {
        return getWebdavUrl(true);
    }

    public String getWebdavUrl(boolean relative) {
        final Node currentNode = getCurrentNode();
        final String url = (relative)
                ? WebClientHelper.getGeneratedWaiRelativeUrl(currentNode, ExtendedURLMode.WEBDAV)
                : WebClientHelper.getGeneratedWaiFullUrl(currentNode, ExtendedURLMode.WEBDAV);

        if (currentNode.hasAspect(CircabcModel.ASPECT_INFORMATION_ROOT)) {
            String indexPage = (String) currentNode.getProperties()
                    .get(CircabcModel.PROP_INF_INDEX_PAGE.toString());
            if (indexFileFound(currentNode.getNodeRef(), indexPage)) {
                // redirect to the index page
                return url + (indexPage.startsWith(FILE_SEPARATOR) == false ? FILE_SEPARATOR : "")
                        + indexPage;
            } else {
                return url;
            }
        } else {
            return url;
        }
    }

    public boolean isIndexFileFound() {
        final Node currentNode = getCurrentNode();
        final String indexPage = (String) currentNode.getProperties()
                .get(CircabcModel.PROP_INF_INDEX_PAGE.toString());

        return indexFileFound(getCurrentNode().getNodeRef(), indexPage);
    }

    private boolean indexFileFound(final NodeRef parent, final String indexPage) {
        if (indexPage == null || indexPage.trim().length() < 1) {
            return false;
        } else {
            NodeRef ref = parent;
            final StringTokenizer tokens = new StringTokenizer(indexPage, FILE_SEPARATOR, false);

            boolean found = false;

            while (tokens.hasMoreTokens()) {
                ref = getNodeService().getChildByName(ref, ContentModel.ASSOC_CONTAINS, tokens.nextToken());
                found = ref != null;
            }

            return found;
        }
    }

    public void browse(ActionEvent event) {
        final UIComponent component = event.getComponent();

        if (component instanceof UIActionLink) {
            final UIActionLink link = (UIActionLink) component;
            link.getParameterMap().put(KEY_BROWSE, "true");
        }

        getBrowseBean().clickWai(event);
    }

    public NavigationPreference getContainerNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.INFORMATION_SERVICE,
                NavigationPreferencesService.CONTAINER_TYPE);
    }

    public NavigationPreference getContentNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.INFORMATION_SERVICE,
                NavigationPreferencesService.CONTENT_TYPE);
    }

    public String getRenderPropertyNameNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.INFORMATION_SERVICE,
                NavigationPreferencesService.CONTAINER_TYPE).getRenderPropertyName();
    }


    /**
     * @return the list of available action for the current node that should be a Event root or child
     */
    @Override
    public List<ActionWrapper> getActions() {
        if (!FacesContext.getCurrentInstance().getViewRoot().getViewId()
                .equals(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE)) {
            // don't display actions when a dialog or a wizard is launched
            return null;
        }

        actions = new ArrayList<>(6);

        final NavigableNodeType type = getNavigator().getCurrentNodeType();

        if (type.equals(NavigableNodeType.INFORMATION)
                || type.equals(NavigableNodeType.INFORMATION_SPACE)) {

            if (browse) {
                actions.add(
                        new ActionWrapper(
                                CREATE_CHILDREN,
                                translate(ADD_CONTENT),
                                null,
                                ADD_CONTENT_BEAN_START,
                                translate(ADD_CONTENT_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId()
                        )
                );

                actions.add(
                        new ActionWrapper(
                                CREATE_CHILDREN,
                                translate(CREATE_SPACE_ACTION),
                                WAI_DIALOG_CREATE_SPACE_WAI,
                                DIALOG_MANAGER_SETUP_PARAMETERS,
                                translate(CREATE_SPACE_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId())
                );

                actions.add(
                        new ActionWrapper(
                                CREATE_CHILDREN,
                                translate(IMPORT),
                                null,
                                IMPORT_BEAN_START,
                                translate(IMPORT_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId()
                        )
                );

            } else {
                actions.add(
                        new ActionWrapper(
                                InformationPermissions.INFMANAGE.toString(),
                                translate(BROWSE_REPO),
                                null,
                                BROWSE_REPO_LISTENER,
                                translate(BROWSE_REPO_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId())
                );
            }
        }

        return actions;
    }
}
