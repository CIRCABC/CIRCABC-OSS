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
package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Baked bean for access profiles deletion
 *
 * @author Yanick Pignot
 */
public class DeleteAccessProfilesDialog extends BaseWaiDialog {

    private static final long serialVersionUID = -4578936397847959999L;

    //private static final Log logger = LogFactory.getLog(ManageAccessProfilesDialog.class);

    private static final String MSG_CONFIRMATION = "remove_access_profile_dialog_page_confirmation";
    private static final String MSG_IMPORTED_CONFIRMATION = "remove_access_profile_dialog_page_imported_confirmation";
    private static final String MSG_PAGE_TITLE = "remove_access_profile_dialog_page_title";
    private static final String MSG_ERROR = "remove_access_profile_dialog_error";

    private static final String PARAM_PROFILE_NAME = "profileName";
    private static final String PARAM_USED_TITLE = "profileDisplayTitle";
    private static final String PARAM_IMPORTED = "profileImported";

    private String profileDisplayTitle;
    private String profileName;
    private String imported;

    private Boolean clearPermission;

    @Override
    public void init(Map<String, String> arg0) {
        super.init(arg0);

        if (arg0 != null) {
            profileName = arg0.get(PARAM_PROFILE_NAME);
            profileDisplayTitle = arg0.get(PARAM_USED_TITLE);
            imported = arg0.get(PARAM_IMPORTED);
        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id is a mandatory parameter");
        } else if (profileDisplayTitle == null || profileName == null) {
            profileDisplayTitle = null;
            profileName = null;
            imported = null;
            throw new IllegalArgumentException(
                    "Missing arguments: " + PARAM_PROFILE_NAME + " and " + PARAM_USED_TITLE
                            + " are required!");
        } else if (NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode()) == false) {
            throw new IllegalArgumentException("This page is accessible only for an interest group");
        }

        if (getActionNode().hasPermission(DirectoryPermissions.DIRADMIN.toString()) == false) {
            Utils.addErrorMessage(translate(ManageAccessProfilesDialog.MSG_NO_LONGER_PERM));
        }
    }

    @Override
    public void restored() {
        this.init(null);
    }

    public String getConfirmMessage() {
        if (imported != null && imported.equals("true")) {
            return translate(MSG_IMPORTED_CONFIRMATION, profileDisplayTitle);
        } else {
            return translate(MSG_CONFIRMATION, profileDisplayTitle, getActionNode().getName());
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        try {
            final IGRootProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getIGRootProfileManagerService();
            final NodeRef nodeRef = getActionNode().getNodeRef();

            logRecord.setInfo("deleted profile " + profileName);
            profileManagerService.deleteProfile(nodeRef, profileName, clearPermission);

            // reset permission cache on the action node
            getActionNode().reset();
            // reset permission cache on all navigation node
            getNavigator().updateCircabcNavigationContext();

            return outcome;
        } catch (Exception e) {
            Utils.addErrorMessage(translate(MSG_ERROR, profileDisplayTitle, e.getMessage()));

            isFinished = false;

            return null;
        }
    }

    public String getContainerTitle() {
        return translate(MSG_PAGE_TITLE, profileDisplayTitle);
    }

    public String getBrowserTitle() {
        return translate("remove_access_profile_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("remove_access_profile_dialog_icon_tooltip");
    }

    public Boolean getClearPermission() {
        return clearPermission;
    }

    public void setClearPermission(Boolean clearPermission) {
        this.clearPermission = clearPermission;
    }
}
