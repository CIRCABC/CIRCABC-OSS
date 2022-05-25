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

import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs edit user profile dialog (for ig root only).
 *
 * @author Yanick Pignot
 */
public class EditUserProfileDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "EditUserProfileDialog";
    private static final long serialVersionUID = 444440868117140631L;
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(EditUserProfileDialog.class);
    private String userName;
    private String userProfileGroup;
    private String fullName;
    private Node currentNode;
    private String oldUserProfile;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                // ensure that the node Id is passed as parameter.
                throw new IllegalArgumentException("The node id is a mandatory parameter");
            }

            if (parameters.get("userName") != null) {
                // the node passed as parameter is the current node.
                currentNode = getActionNode();

                userName = parameters.get("userName");

                fullName = getUserService().getUserFullName(userName);

                if (userName == null) {
                    throw new IllegalArgumentException(
                            "The user name and its full name are mandatory parameter ");
                }
            } else {
                // the node passed as parameter is the user.
                currentNode = getNavigator().getCurrentIGRoot();

                Map<String, Object> properties = getActionNode().getProperties();
                String firstName = (String) properties.get("firstName");
                String lastName = (String) properties.get("lastName");
                userName = (String) properties.get("userName");
                fullName = firstName + " " + lastName;
            }

            final ProfileManagerService profileManagerService = getProfileService();
            oldUserProfile = profileManagerService.getPersonProfile(currentNode.getNodeRef(), userName);
            Profile profile = profileManagerService.getProfile(currentNode.getNodeRef(), oldUserProfile);
            userProfileGroup = profile.getPrefixedAlfrescoGroupName();
        }

    }

    /**
     * @return
     */
    private ProfileManagerService getProfileService() {
        return getProfileManagerServiceFactory().getProfileManagerService(currentNode.getNodeRef());
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {
        try {
            final NodeRef nodeRef = getActionNode().getNodeRef();
            final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(nodeRef);
            final Profile profile = profileManagerService
                    .getProfileFromGroup(currentNode.getNodeRef(), userProfileGroup);
            final String profileName = profile.getProfileName();
            final String info = MessageFormat
                    .format("Changed profile of user {0} from {1} to {2}", fullName, oldUserProfile,
                            profileName);
            logRecord.setInfo(info);
            profileManagerService.changePersonProfile(currentNode.getNodeRef(), userName, profileName);
        } catch (Throwable err) {
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error:" + err.getMessage(), err);
            }

            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);

            outcome = null;
        }

        return outcome;
    }

    public List<SortableSelectItem> getProfiles() {
        return ProfileUtils.buildAssignableProfileItems(currentNode, logger);
    }


    public String getBrowserTitle() {
        return translate("edit_user_profile_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_user_profile_icon_tooltip");
    }

    /**
     * @return the userName
     */
    public final String getUserName() {
        return userName;
    }


    /**
     * @return the fullName
     */
    public final String getFullName() {
        return fullName;
    }

    public String getUserProfileGroup() {
        return userProfileGroup;
    }

    public void setUserProfileGroup(String userProfileGroup) {
        this.userProfileGroup = userProfileGroup;
    }

}
