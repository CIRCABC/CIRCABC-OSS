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
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * Dialog bean to edit user profile for the WAI
 *
 * @author Guillaume
 */
public class EditUserDetailsDialog extends BaseUserDetailsBean {

    public static final String BEAN_NAME = "EditUserDetailsDialog";
    public static final String DIALOG_NAME = "userConsoleWai";
    private static final long serialVersionUID = -5363740078894071426L;
    private static final String MSG_ERR_UNEXPECTED_ERROR = "edit_user_details_erreor_unexpected";
    private static final String MSG_AVATAR_DELETED = "edit_user_details_avatar_del_success";
    private static final String MSG_DATA_LOADED = "edit_user_details_reload_success";
    private static final String MSG_SUCCESS = "edit_user_details_success";
    private static final Log logger = LogFactory.getLog(EditUserDetailsDialog.class);

    private CircabcService circabcService;

    private NodeRef userNodeRef;

    /**
     * @see org.alfresco.web.bean.dialog#finish()
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final UserDetails userDetails = getUserDetails();
        userDetails.setSignature(getSecurityService().getCleanHTML(userDetails.getSignature(), true));

        try {
            userNodeRef = getUserDetails().getNodeRef();
            getBusinessRegistry().getUserDetailsBusinessSrv()
                    .updateUserDetails(userDetails.getNodeRef(), userDetails);
            // if user interface language is changed update ui
            final Map<QName, Serializable> updatedPreferences = userDetails.getUpdatedPreferences();
            if (updatedPreferences.containsKey(UserService.PREF_INTERFACE_LANGUAGE)) {
                final UserPreferencesBean userPreferencesBean = getUserPreferencesBean();
                final String language = (String) updatedPreferences
                        .get(UserService.PREF_INTERFACE_LANGUAGE);
                userPreferencesBean.setLanguage(language);
            }

            if (updatedPreferences.containsKey(UserService.PREF_CONTENT_FILTER_LANGUAGE)) {
                final UserPreferencesBean userPreferencesBean = getUserPreferencesBean();
                final Serializable contentLang = updatedPreferences
                        .get(UserService.PREF_CONTENT_FILTER_LANGUAGE);
                if (contentLang instanceof String) {
                    userPreferencesBean.setContentFilterLanguage((String) contentLang);
                } else if (contentLang instanceof Locale) {
                    userPreferencesBean.setContentFilterLanguage(((Locale) contentLang).getLanguage());
                } else if (contentLang == null) {
                    userPreferencesBean.setContentFilterLanguage(UserPreferencesBean.MSG_CONTENTALLLANGUAGES);
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error("PREF_CONTENT_FILTER_LANGUAGE has invalid type not String neither Local");
                    }
                }
            }

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_SUCCESS));
        } catch (final BusinessStackError validationErrors) {
            for (final String msg : validationErrors.getI18NMessages()) {
                Utils.addErrorMessage(msg);
            }

            this.isFinished = false;
            return null;
        } catch (Throwable t) {
            isFinished = false;
            Utils.addErrorMessage(
                    translate(MSG_ERR_UNEXPECTED_ERROR, getUserDetails().getDisplayId(), t.getMessage()));
            return null;
        }

        return outcome;
    }

    /**
     * Refresh user data with ldap values
     */
    public void getFromLdap(ActionEvent event) {
        try {
            getBusinessRegistry().getRemoteUserBusinessSrv().reloadDetails(getUserDetails());

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_DATA_LOADED));
        } catch (final BusinessStackError validationErrors) {
            for (final String msg : validationErrors.getI18NMessages()) {
                Utils.addErrorMessage(msg);
            }
        }
    }

    /**
     * Refresh user data with ldap values
     */
    public void removeAvatar(ActionEvent event) {
        try {
            final UserDetailsBusinessSrv userDetailsBusinessSrv = getBusinessRegistry()
                    .getUserDetailsBusinessSrv();

            userDetailsBusinessSrv.removeAvatar(getUserDetails().getNodeRef());
            getUserDetails().setAvatar(userDetailsBusinessSrv.getAvatar(getUserDetails().getNodeRef()));

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_AVATAR_DELETED));
        } catch (final BusinessStackError validationErrors) {
            for (final String msg : validationErrors.getI18NMessages()) {
                Utils.addErrorMessage(msg);
            }
        }
    }

    /**
     * Refresh user data with ldap values
     */
    public void launchUpdateAvatarDialog(ActionEvent event) {
        final UpdateAvatarDialog dialog = (UpdateAvatarDialog) Beans
                .getBean(UpdateAvatarDialog.BEAN_NAME);
        dialog.start(event);
    }


    /**
     * @return if the circabc installation use a remote user manager (ldap)
     */
    public boolean isReloadbuttonAvailable() {
        return getBusinessRegistry().getRemoteUserBusinessSrv().isRemoteManagementAvailable();
    }

    /**
     * @param contentFilterLanguage the contentFilterLanguage to set
     */
    public final String getContentFilterLanguage() {
        final Locale contentFilterLanguage = getUserDetails().getContentFilterLanguage();
        if (contentFilterLanguage == null) {
            return null;
        } else {
            return contentFilterLanguage.getLanguage();
        }
    }

    /**
     * @param contentFilterLanguage the contentFilterLanguage to set
     */
    public final void setContentFilterLanguage(String contentFilterLanguage) {
        if (contentFilterLanguage == null || contentFilterLanguage
                .equalsIgnoreCase(UserPreferencesBean.MSG_CONTENTALLLANGUAGES)) {
            getUserDetails().setContentFilterLanguage(null);
        } else {
            getUserDetails().setContentFilterLanguage(I18NUtil.parseLocale(contentFilterLanguage));
        }

    }

    public String getBrowserTitle() {
        return translate("edit_user_details_header");
    }

    public String getPageIconAltText() {
        return translate("edit_user_details_icon_tooltip");
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("save");
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (getCircabcService().syncEnabled()) {
            getCircabcService().updateUser(userNodeRef);
        }
        return super.doPostCommitProcessing(context, outcome);
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}
