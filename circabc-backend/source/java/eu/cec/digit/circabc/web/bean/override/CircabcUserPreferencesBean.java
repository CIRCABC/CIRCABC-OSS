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
package eu.cec.digit.circabc.web.bean.override;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Preferences;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @author guillaume, makz
 */
public class CircabcUserPreferencesBean extends UserPreferencesBean {

    public static final String BEAN_NAME = "UserPreferencesBean";
    private static final long serialVersionUID = -7861458606474224433L;
    private static final String PREF_USER_INTERFACE_LANGUAGE = UserPreferencesBean.PREF_INTERFACELANGUAGE;
    private static final Log logger = LogFactory.getLog(CircabcUserPreferencesBean.class);
    final private Map<String, Integer> userRichListPreference = new HashMap<>(10);
    private CircabcService circabcService;

    /**
     * @return If no user is logged in it returns the JSF language otherwise it returns the language
     * that is set in the user profile.
     */
    public String getLanguage() {
        String result = "en";
        try {
            // use the JSF language if the current user is guest
            FacesContext fc = FacesContext.getCurrentInstance();
            result = fc.getViewRoot().getLocale().getLanguage();

            final String currentUser = AuthenticationUtil.getRunAsUser();
            if (currentUser != null) {
                final String guestUser = AuthenticationUtil.getGuestUserName();
                if (!currentUser.equalsIgnoreCase(guestUser)) {
                    // it is not the guest user, read the language from the user settings
                    final Preferences preferences = PreferencesService.getPreferences();
                    if (preferences != null) {
                        final Serializable userInterfaceLanguage = PreferencesService.getPreferences()
                                .getValue(PREF_USER_INTERFACE_LANGUAGE);
                        if (userInterfaceLanguage != null) {
                            if (userInterfaceLanguage instanceof Locale) {
                                Locale local = (Locale) userInterfaceLanguage;
                                result = local.getLanguage();
                            } else if (userInterfaceLanguage instanceof String) {
                                result = (String) userInterfaceLanguage;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when get language", e);
            }
        }
        return result;
    }

    /**
     * Purpose : set the language of the interface without changing the UserProfilePreference -> No
     * more done by alfresco
     *
     * @param language The language selection to set.
     */
    public void setLanguage(String language) {
        try {

            if (language != null) {
                Application.setLanguage(FacesContext.getCurrentInstance(), language);
                String currentUser = AuthenticationUtil.getRunAsUser();
                if (currentUser != null) {
                    String guestUser = AuthenticationUtil.getGuestUserName();
                    if (!currentUser.equalsIgnoreCase(guestUser)) {
                        // it's not a guest user, so store the language in the user settings
                        Preferences preferences = PreferencesService.getPreferences();
                        if (preferences != null) {
                            preferences.setValue(PREF_USER_INTERFACE_LANGUAGE, language);
                            if (getCircabcService().syncEnabled()) {
                                getCircabcService().setUILangauge(currentUser, language);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when set language", e);
            }
        }
    }

    public Map<String, Integer> getListElementPreference() {
        return userRichListPreference;
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
