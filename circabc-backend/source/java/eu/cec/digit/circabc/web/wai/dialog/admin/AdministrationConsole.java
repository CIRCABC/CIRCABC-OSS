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

import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.repository.CircabcRootNode;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.categories.CategoriesDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.data.UIRichList;

import javax.faces.context.FacesContext;
import java.util.Map;


/**
 * Bean that backs the administration console
 *
 * @author Yanick Pignot
 */
public class AdministrationConsole extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "CircabcAdminConsoleDialog";
    private static final String CLOSE = "close";
    private static final String ADMIN_CONSOLE_DIALOG_ICON_TOOLTIP = "admin_console_dialog_icon_tooltip";
    private static final String ADMIN_CONSOLE_DIALOG_BROWSER_TITLE = "admin_console_dialog_browser_title";
    private static final long serialVersionUID = 4283648437777451014L;
    private Boolean viewSuperAdminRootConsole = null;
    private Boolean viewAdminRootConsole = null;
    private Boolean viewAdminHeaderConsole = null;
    private Boolean viewCategoryConsole = null;
    private Boolean viewIgConsole = null;
    private Boolean viewLibChildConsole = null;
    private Boolean viewForumChildConsole = null;
    private Boolean viewSurveyChildConsole = null;
    private Boolean viewInformationChildConsole = null;
    private Boolean viewNewsConsole = null;
    private Boolean viewSurveyConsole = null;
    private Boolean viewLibraryConsole = null;
    private Boolean viewInformationConsole = null;
    private Boolean viewEventConsole = null;
    private Boolean viewOtherServiceConsole = null;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        // clear the cache
        this.viewSuperAdminRootConsole = null;
        this.viewAdminRootConsole = null;
        this.viewAdminHeaderConsole = null;
        this.viewCategoryConsole = null;
        this.viewIgConsole = null;
        this.viewLibChildConsole = null;
        this.viewForumChildConsole = null;
        this.viewSurveyChildConsole = null;
        this.viewInformationChildConsole = null;
        this.viewNewsConsole = null;
        this.viewSurveyConsole = null;
        this.viewLibraryConsole = null;
        this.viewInformationConsole = null;
        this.viewEventConsole = null;
        this.viewOtherServiceConsole = null;

        final CategoriesDialog categoriesDialog = (CategoriesDialog) Beans.getBean("CategoriesDialog");
        if (categoriesDialog.getCategoriesRichList() == null) {
            final UIRichList richList = new UIRichList();
            richList.setViewMode("details");
            categoriesDialog.setCategoriesRichList(richList);
        }

        if (parameters == null) {
            return;
        }

        //in the restaure mode, the parameters can be null
        if (parameters != null) {
            final String id = parameters.get(NODE_ID_PARAMETER);

            // test if the application is in right state (id should be equals to the current node)
            if (getNavigator().getCurrentNodeId() == null || !getNavigator().getCurrentNodeId()
                    .equals(id)) {
                // reset permission cache on the action node
                getActionNode().reset();
                // the verfication of id parameter will be perfom here
                getNavigator().setCurrentNodeId(id);
                // reset permission cache on all navigation node
                getNavigator().updateCircabcNavigationContext();

                UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();
            }
        }


    }

    public boolean isDisplayRootMenuForSuperAdmin() {
        if (viewSuperAdminRootConsole == null) {
            viewSuperAdminRootConsole = isSuperAdmin() && isCurrentNodeCircabc();
        }

        return viewSuperAdminRootConsole;
    }

    public boolean isDisplayRootMenuForCircabcAdmin() {
        if (viewAdminRootConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeCircabc()) {
                final CircabcRootNode root = getNavigator().getCircabcHomeNode();

                viewAdminRootConsole =
                        root.hasPermission(CircabcRootPermissions.CIRCABCMANAGEMEMBERS.toString())
                                || root.hasPermission(CircabcRootPermissions.CIRCABCADMIN.toString());
            } else {
                viewAdminRootConsole = Boolean.FALSE;
            }
        }

        return viewAdminRootConsole;

    }

    public boolean isDisplayHeaderMenuForAdmin() {
        if (viewAdminHeaderConsole == null) {
            if (isCurrentNodeHeader()) {
                final CircabcRootNode root = getNavigator().getCircabcHomeNode();

                viewAdminHeaderConsole =
                        isSuperAdmin() || root.hasPermission(CircabcRootPermissions.CIRCABCADMIN.toString());
            } else {
                viewAdminHeaderConsole = Boolean.FALSE;
            }
        }

        return viewAdminHeaderConsole;

    }

    public boolean isDisplayCategoryMenu() {
        if (viewCategoryConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeCategory()) {
                final Node cat = getNavigator().getCurrentCategory();

                viewCategoryConsole =
                        cat.hasPermission(CategoryPermissions.CIRCACATEGORYMANAGEMEMBERS.toString())
                                || cat.hasPermission(CategoryPermissions.CIRCACATEGORYADMIN.toString());

            } else {
                viewCategoryConsole = Boolean.FALSE;
            }
        }

        return viewCategoryConsole;
    }

    public boolean isDisplayNewsgroupMenu() {
        if (viewNewsConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeNewsgroup()) {
                final Node news = getNavigator().getCurrentNode();

                viewNewsConsole = news.hasPermission(NewsGroupPermissions.NWSMODERATE.toString())
                        || news.hasPermission(NewsGroupPermissions.NWSADMIN.toString());

            } else {
                viewNewsConsole = Boolean.FALSE;
            }
        }

        return viewNewsConsole;
    }

    public boolean isDisplaySurveyMenu() {
        if (viewSurveyConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeSurvey()) {
                final Node survey = getNavigator().getCurrentNode();

                viewSurveyConsole = survey.hasPermission(SurveyPermissions.SURADMIN.toString());
            } else {
                viewSurveyConsole = Boolean.FALSE;
            }
        }

        return viewSurveyConsole;
    }

    public boolean isDisplayOtherServiceConsole() {
        if (viewOtherServiceConsole == null) {
            boolean isAdmin = false;
            final InterestGroupNode ig = (InterestGroupNode) getNavigator().getCurrentIGRoot();
            if (!isSuperAdmin() && ig != null) {
                if (ig.getDirectory() != null && ig
                        .hasPermission(DirectoryPermissions.DIRMANAGEMEMBERS.toString())) {
                    isAdmin = true;
                } else if (ig.getLibrary() != null && ig.getLibrary()
                        .hasPermission(LibraryPermissions.LIBADMIN.toString())) {
                    isAdmin = true;
                } else if (ig.getNewsgroup() != null && ig.getNewsgroup()
                        .hasPermission(NewsGroupPermissions.NWSMODERATE.toString())) {
                    isAdmin = true;
                } else if (ig.getEvent() != null && ig.getEvent()
                        .hasPermission(EventPermissions.EVEADMIN.toString())) {
                    isAdmin = true;
                } else if (ig.getInformation() != null && ig.getInformation()
                        .hasPermission(InformationPermissions.INFMANAGE.toString())) {
                    isAdmin = true;
                } else if (ig.getSurvey() != null && ig.getSurvey()
                        .hasPermission(SurveyPermissions.SURADMIN.toString())) {
                    isAdmin = true;
                }
            }
            viewOtherServiceConsole = isAdmin;
        }
        return viewOtherServiceConsole;
    }

    public boolean isDisplayIgMenu() {
        if (viewIgConsole == null) {
            boolean isAdmin = false;

            if (!isSuperAdmin() && isCurrentNodeInterestGroup()) {
                final InterestGroupNode ig = (InterestGroupNode) getNavigator().getCurrentIGRoot();

                if (ig != null) {
                    if (ig.getDirectory() != null && ig
                            .hasPermission(DirectoryPermissions.DIRMANAGEMEMBERS.toString())) {
                        isAdmin = true;
                    } else if (ig.getLibrary() != null && ig.getLibrary()
                            .hasPermission(LibraryPermissions.LIBADMIN.toString())) {
                        isAdmin = true;
                    } else if (ig.getNewsgroup() != null && ig.getNewsgroup()
                            .hasPermission(NewsGroupPermissions.NWSADMIN.toString())) {
                        isAdmin = true;
                    } else if (ig.getEvent() != null && ig.getEvent()
                            .hasPermission(EventPermissions.EVEADMIN.toString())) {
                        isAdmin = true;
                    } else if (ig.getInformation() != null && ig.getInformation()
                            .hasPermission(InformationPermissions.INFADMIN.toString())) {
                        isAdmin = true;
                    } else if (ig.getSurvey() != null && ig.getSurvey()
                            .hasPermission(SurveyPermissions.SURADMIN.toString())) {
                        isAdmin = true;
                    }
                }
            }

            viewIgConsole = isAdmin;
        }

        return viewIgConsole;
    }

    public boolean isDisplayLibraryChildMenu() {
        if (viewLibChildConsole == null) {
            if (isCurrentNodeLibraryChild()) {
                viewLibChildConsole = getNavigator().getCurrentNode()
                        .hasPermission(LibraryPermissions.LIBADMIN.toString());
            } else {
                viewLibChildConsole = Boolean.FALSE;
            }
        }
        return viewLibChildConsole;
    }

    public boolean isDisplayForumChildMenu() {
        if (viewForumChildConsole == null) {
            if (isCurrentNodeForumChild()) {
                viewForumChildConsole = getNavigator().getCurrentNode()
                        .hasPermission(NewsGroupPermissions.NWSADMIN.toString());
            } else {
                viewForumChildConsole = Boolean.FALSE;
            }
        }
        return viewForumChildConsole;
    }

    public boolean isDisplaySurveyChildSpaceMenu() {
        if (viewSurveyChildConsole == null) {
            if (isCurrentNodeSurveyChild()) {
                viewSurveyChildConsole = getNavigator().getCurrentNode()
                        .hasPermission(SurveyPermissions.SURADMIN.toString());
            } else {
                viewSurveyChildConsole = Boolean.FALSE;
            }
        }
        return viewSurveyChildConsole;
    }

    public boolean isDisplayInformationChildSpaceMenu() {
        if (viewInformationChildConsole == null) {
            if (isCurrentNodeInformationChild()) {
                viewInformationChildConsole = getNavigator().getCurrentNode()
                        .hasPermission(InformationPermissions.INFADMIN.toString());
            } else {
                viewInformationChildConsole = Boolean.FALSE;
            }
        }
        return viewInformationChildConsole;
    }

    public boolean isDisplayLibraryMenu() {
        if (viewLibraryConsole == null) {
            if (isCurrentNodeLibraryRoot()) {
                viewLibraryConsole = getNavigator().getCurrentNode()
                        .hasPermission(LibraryPermissions.LIBADMIN.toString());
            } else {
                viewLibraryConsole = Boolean.FALSE;
            }
        }
        return viewLibraryConsole;
    }

    public boolean isDisplayInformationMenu() {
        if (viewInformationConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeInformationRoot()) {
                final Node inf = getNavigator().getCurrentNode();

                viewInformationConsole =
                        inf.hasPermission(InformationPermissions.INFMANAGE.toString())
                                || inf.hasPermission(InformationPermissions.INFADMIN.toString());

            } else {
                viewInformationConsole = Boolean.FALSE;
            }
        }

        return viewInformationConsole;
    }

    public boolean isDisplayEventsMenu() {
        if (viewEventConsole == null) {
            if (!isSuperAdmin() && isCurrentNodeEventsRoot()) {
                final Node eve = getNavigator().getCurrentNode();

                viewEventConsole =
                        eve.hasPermission(EventPermissions.EVEADMIN.toString());
            } else {
                viewEventConsole = Boolean.FALSE;
            }
        }

        return viewEventConsole;
    }

    private boolean isSuperAdmin() {
        return getNavigator().getCurrentUser().isAdmin();
    }


    private boolean isCurrentNodeHeader() {
        return isCurrentNodeOfType(NavigableNodeType.CATEGORY_HEADER);
    }

    private boolean isCurrentNodeCircabc() {
        return isCurrentNodeOfType(NavigableNodeType.CIRCABC_ROOT);
    }

    private boolean isCurrentNodeCategory() {
        return isCurrentNodeOfType(NavigableNodeType.CATEGORY);
    }

    private boolean isCurrentNodeInterestGroup() {
        return isCurrentNodeOfType(NavigableNodeType.IG_ROOT);
    }

    private boolean isCurrentNodeLibraryChild() {
        return isCurrentNodeOfSubtypeType(NavigableNodeType.LIBRARY, NavigableNodeType.LIBRARY_CHILD);
    }

    private boolean isCurrentNodeForumChild() {
        return isCurrentNodeOfSubtypeType(NavigableNodeType.NEWSGROUP,
                NavigableNodeType.NEWSGROUP_CHILD);
    }

    private boolean isCurrentNodeSurveyChild() {
        return isCurrentNodeOfSubtypeType(NavigableNodeType.SURVEY, NavigableNodeType.SURVEY_CHILD);
    }

    private boolean isCurrentNodeInformationChild() {
        return isCurrentNodeOfSubtypeType(NavigableNodeType.INFORMATION,
                NavigableNodeType.INFORMATION_CHILD);
    }

    private boolean isCurrentNodeLibraryRoot() {
        return isCurrentNodeOfType(NavigableNodeType.LIBRARY);
    }


    private boolean isCurrentNodeNewsgroup() {
        return isCurrentNodeOfType(NavigableNodeType.NEWSGROUP);
    }

    private boolean isCurrentNodeSurvey() {
        return isCurrentNodeOfType(NavigableNodeType.SURVEY);
    }

    private boolean isCurrentNodeInformationRoot() {
        return isCurrentNodeOfType(NavigableNodeType.INFORMATION);
    }

    private boolean isCurrentNodeEventsRoot() {
        return isCurrentNodeOfType(NavigableNodeType.EVENT);
    }

    private boolean isCurrentNodeOfType(NavigableNodeType targetType) {
        final NavigableNodeType type = getNavigator().getCurrentNodeType();

        return type != null && type.equals(targetType);
    }

    private boolean isCurrentNodeOfSubtypeType(final NavigableNodeType rootType,
                                               final NavigableNodeType subType) {
        final NavigableNodeType type = getNavigator().getCurrentNodeType();

        return type != null
                && (subType.equals(type) || type.getRequireNodeType() != null && type.getRequireNodeType()
                .equals(subType))
                && !rootType.equals(type);

    }

    @Override
    public String cancel() {
        // Refresh the navigation...
        getBrowseBean().refreshBrowsing();

        return super.cancel()
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.WAI_BROWSE_OUTCOME;
    }

    @Override
    public void restored() {
        // refresh the state of the dialog
        this.init(null);
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        // nothing to do
        return outcome;
    }

    public String getBrowserTitle() {
        return translate(ADMIN_CONSOLE_DIALOG_BROWSER_TITLE);
    }

    public String getPageIconAltText() {
        return translate(ADMIN_CONSOLE_DIALOG_ICON_TOOLTIP);
    }

    @Override
    public String getCancelButtonLabel() {
        return translate(CLOSE);
    }
}
