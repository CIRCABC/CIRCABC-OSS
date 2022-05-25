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

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

import javax.faces.model.SelectItem;
import java.util.Map;

public abstract class BaseUserDetailsBean extends BaseWaiDialog {

    /**
     * The message for the I18N view_user_details_global_notification_active
     */
    protected static final String MSG_GLOBAL_NOTIFICATION_ACTIVE = "edit_user_details_global_notification_active";
    /**
     * The message for the I18N view_user_details_global_notification_nonactive
     */
    protected static final String MSG_GLOBAL_NOTIFICATION_NONACTIVE = "edit_user_details_global_notification_nonactive";
    /**
     * The message for the I18N view_user_details_active
     */
    protected static final String MSG_VISIBILITY_ACTIVE = "edit_user_details_visibility_active";
    /**
     * The message for the I18N view_user_details_nonactive
     */
    protected static final String MSG_VISIBILITY_NONACTIVE = "edit_user_details_visibility_nonactive";

    private UserDetails userDetails;

    /**
     * We simply populate the variable of the bean
     *
     * @param parameters The parameters to initialise the bean (not used here)
     */
    @Override
    public void init(final Map<String, String> parameters) {
        if (parameters != null) {
            final String id = parameters.get(NODE_ID_PARAMETER);

            if (id == null) {
                throw new IllegalArgumentException("Id parameter is madatory");
            }

            try {
                final NodeRef personRef = new NodeRef(Repository.getStoreRef(), id);
                if (getNodeService().exists(personRef)) {
                    userDetails = getBusinessRegistry().getUserDetailsBusinessSrv().getUserDetails(personRef);
                } else {
                    parameters.remove(NODE_ID_PARAMETER);
                    userDetails = getBusinessRegistry().getUserDetailsBusinessSrv().getUserDetails(id);
                }
            } catch (final BusinessStackError validationErrors) {
                userDetails = null;
                for (final String msg : validationErrors.getI18NMessages()) {
                    Utils.addErrorMessage(msg);
                }
            }
        }

        super.init(parameters);
    }

    public boolean isDataSuccessfullyLoaded() {
        return userDetails != null;
    }

    /**
     * @return the userDetails
     */
    public final UserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * @return the userDetails
     */
    public final void setUserDetails(UserDetails details) {
        userDetails = details;
    }

    public String getProfileUrl() {
        if (isDataSuccessfullyLoaded() && userDetails.isUserCreated()) {
            return WebClientHelper
                    .getGeneratedWaiFullUrl(userDetails.getNodeRef(), ExtendedURLMode.HTTP_USERDETAILS);
        } else {
            return "N/A";
        }
    }

    /**
     * Getter for the list of items for the content filtering language selection include the label
     * 'all langaguages'
     *
     * @return List of items
     */
    public SelectItem[] getContentFilterLanguages() {
        return getUserPreferencesBean().getContentFilterLanguages();
    }

    public SelectItem[] getVisibilityOptions() {
        return new SelectItem[]{
                new SelectItem(Boolean.TRUE, translate(MSG_VISIBILITY_ACTIVE)),
                new SelectItem(Boolean.FALSE, translate(MSG_VISIBILITY_NONACTIVE))
        };
    }

    public SelectItem[] getNotificationOptions() {
        return new SelectItem[]{
                new SelectItem(Boolean.TRUE, translate(MSG_GLOBAL_NOTIFICATION_ACTIVE)),
                new SelectItem(Boolean.FALSE, translate(MSG_GLOBAL_NOTIFICATION_NONACTIVE))
        };
    }

    /**
     * @return list of items for the user 	interface language selection
     */
    public SelectItem[] getLanguages() {
        return getUserPreferencesBean().getLanguages();
    }

    /**
     * @return the avatar path
     */
    public final String getAvatarPath() {
        return WebClientHelper
                .getGeneratedWaiUrl(userDetails.getAvatar(), ExtendedURLMode.HTTP_DOWNLOAD, true);
    }

    @Override
    public ActionsListWrapper getActionList() {
        UserDetails user = getUserDetails();
        if (isDataSuccessfullyLoaded() && user != null) {
            return new ActionsListWrapper(user, "user_account_actions_wai");
        } else {
            return null;
        }
    }
}
