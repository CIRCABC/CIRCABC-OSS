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
package eu.cec.digit.circabc.web.wai.dialog.users;

import eu.cec.digit.circabc.service.profile.permissions.CircabcRootPermissions;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog bean to edit user profile for the WAI
 *
 * @author Slobodan Filipovic, Markus Kölzer
 */
public final class ViewUserMembershipDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "ViewUserMembershipDialog";
    private static final long serialVersionUID = 395069528441074404L;
    /**
     * Logger
     */
    //private static final Log logger = LogFactory.getLog(ViewUserMembershipDialog.class);

    private String currentUserName;
    private String profileUserName;
    private boolean allowedToView;

    //***********************************************************************
    //                                                              OVERRIDES
    //***********************************************************************

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        this.profileUserName = parameters.get("profileUserName");
        this.currentUserName = this.getNavigator().getCurrentUser().getUserName();
        this.allowedToView = this.isAllowedToViewMemberships();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return outcome;
    }

    public String getContainerTitle() {
        return translate("view_user_membership_title", this.profileUserName);
    }

    public String getBrowserTitle() {
        return translate("view_user_membership_browser_title");
    }

    public String getPageIconAltText() {
        return translate("view_user_membership_icon_tooltip");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    //***********************************************************************
    //                                                      GETTER AND SETTER
    //***********************************************************************

    public boolean isAllowedToView() {
        return allowedToView;
    }

    public boolean isMyProfile() {
        return this.profileUserName.equals(this.currentUserName);
    }

    public String getUserName() {
        return this.profileUserName;
    }

    public String getUserFullName() {
        return getUserService().getUserFullName(this.profileUserName);
    }

    public List<UserIGMembershipRecord> getIgRoles() {
        if (this.isMyProfile()) {
            // you are always allowed to view all of your own IG memberships
            return getUserService().getInterestGroups(this.profileUserName);
        } else {
            // if it is not your profile we must apply some filters here because you are probably not allowed to see everything.

            List<NodeRef> filterCategories = new ArrayList<>();
            List<UserCategoryMembershipRecord> catRolesOfCurrentUser = getUserService()
                    .getCategories(this.currentUserName);
            if (catRolesOfCurrentUser.size() > 0) {
                // if you are category admin, so you are only allowed to view the IGs that are under cour category
                for (UserCategoryMembershipRecord catMembership : catRolesOfCurrentUser) {
                    filterCategories.add(new NodeRef(
                            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            catMembership.getCategoryNodeId()));
                }
            }

            return getUserService().getInterestGroups(this.profileUserName, filterCategories);
        }
    }

    public List<UserCategoryMembershipRecord> getCategoryRoles() {
        return getUserService().getCategories(this.profileUserName);
    }

    //***********************************************************************
    //                                                         PRIVATE HELPER
    //***********************************************************************

    private boolean isAllowedToViewMemberships() {
        boolean result = false;

        if (this.isMyProfile()) {
            //current user is watching own memberships
            return true;
        }

        // is circabc administrator
        if (getNavigator().getCircabcHomeNode()
                .hasPermission(CircabcRootPermissions.CIRCABCMANAGEMEMBERS.toString())) {
            return true;
        }

        // check all categories of current user
        List<UserCategoryMembershipRecord> catRolesOfCurrentUser = getUserService()
                .getCategories(this.currentUserName);
        for (UserCategoryMembershipRecord catRoleOfCurrentUser : catRolesOfCurrentUser) {
            if (catRoleOfCurrentUser.getCategoryNodeId()
                    .equals(getNavigator().getCurrentCategory().getNodeRef().getId())) {
                return true;
            }
        }

        // check all IGs of current user
        List<UserIGMembershipRecord> igRolesOfCurrentUser = getUserService()
                .getInterestGroups(this.currentUserName);
        for (UserIGMembershipRecord igRoleOfCurrentUser : igRolesOfCurrentUser) {
            String profile = igRoleOfCurrentUser.getProfile();
            if (profile.equals("IGLeader") || profile.equals("Secretary")) {
                // the current user is leader or secretary of this IG,
                // check if the profile user is a member of this IG
                if (isUserMemberOfInterestGroup(this.profileUserName,
                        igRoleOfCurrentUser.getInterestGroupNodeId())) {
                    return true;
                }
            }
        }

        return result;
    }

    private boolean isUserMemberOfCategory(String userName, String catNodeId) {
        List<UserCategoryMembershipRecord> catRoles = getUserService().getCategories(userName);
        for (UserCategoryMembershipRecord catRole : catRoles) {
            if (catRole.getCategoryNodeId().equals(catNodeId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserMemberOfInterestGroup(String userName, String igNodeId) {
        List<UserIGMembershipRecord> igRolesOfUser = getUserService().getInterestGroups(userName);
        for (UserIGMembershipRecord igRole : igRolesOfUser) {
            if (igRole.getInterestGroupNodeId().equals(igNodeId)) {
                return true;
            }
        }
        return false;
    }
}
