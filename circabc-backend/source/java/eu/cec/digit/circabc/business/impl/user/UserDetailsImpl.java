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
package eu.cec.digit.circabc.business.impl.user;

import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Concrete implementation of user detail intereface.
 *
 * @author Yanick Pignot
 */
public class UserDetailsImpl implements Serializable, UserDetails {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final NodeRef personRef;
    private final UserDetailsBusinessImpl userDetailSrv;

    private Map<QName, Serializable> properties;
    private Map<QName, Serializable> preferences;

    private NodeRef avatar;

    private Boolean personalDataHidden = null;

    private Map<QName, Serializable> updatedProperties;
    private Map<QName, Serializable> updatedPreferences;


    /*package*/ UserDetailsImpl(final NodeRef personRef,
                                final UserDetailsBusinessImpl userDetailSrv) {
        this.personRef = personRef;
        this.userDetailSrv = userDetailSrv;
    }

    /*package*/ UserDetailsImpl(final String userName, final UserDetailsBusinessImpl userDetailSrv) {
        this((NodeRef) null, userDetailSrv);

        properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName);
    }

    //-----------------
    //--  Mandatory values (identifiers)


    /**
     * @return the personRef
     */
    public final NodeRef getNodeRef() {
        return personRef;
    }

    /**
     * @return the personRef
     */
    public final String getUserName() {
        return (String) getProperty(ContentModel.PROP_USERNAME);
    }

    /**
     * Rtrun if the user is already created in the repository
     */
    public boolean isUserCreated() {
        return getNodeRef() != null;
    }

    //-----------------
    //--  Properties of Alfresco user model

    /**
     * @return the email
     */
    public final String getEmail() {
        return (String) getProperty(ContentModel.PROP_EMAIL);
    }

    /**
     * @param email the email to set
     */
    public final void setEmail(String email) {
        setProperty(ContentModel.PROP_EMAIL, email);
    }

    /**
     * @return the firstName
     */
    public final String getFirstName() {
        return (String) getProperty(ContentModel.PROP_FIRSTNAME);
    }

    /**
     * @param firstName the firstName to set
     */
    public final void setFirstName(String firstName) {
        setProperty(ContentModel.PROP_FIRSTNAME, firstName);
    }

    /**
     * @return the lastName
     */
    public final String getLastName() {
        return (String) getProperty(ContentModel.PROP_LASTNAME);
    }

    /**
     * @param lastName the lastName to set
     */
    public final void setLastName(String lastName) {
        setProperty(ContentModel.PROP_LASTNAME, lastName);
    }

    //-----------------
    //--  Properties of circabc user aspect

    /**
     * @return the description
     */
    public final String getDescription() {
        return (String) getProperty(UserModel.PROP_DESCRIPTION);
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        setProperty(UserModel.PROP_DESCRIPTION, description);
    }

    /**
     * @return the fax
     */
    public final String getFax() {
        return (String) getProperty(UserModel.PROP_FAX);
    }

    /**
     * @param fax the fax to set
     */
    public final void setFax(String fax) {
        setProperty(UserModel.PROP_FAX, fax);
    }

    /**
     * @return the organisation
     */
    public final String getOrganisation() {
        return (String) getProperty(UserModel.PROP_ORGDEPNUMBER);
    }

    /**
     * @param organisation the organisation to set
     */
    public final void setOrganisation(String organisation) {
        setProperty(UserModel.PROP_ORGDEPNUMBER, organisation);
    }

    /**
     * @return the phone
     */
    public final String getPhone() {
        return (String) getProperty(UserModel.PROP_PHONE);
    }

    /**
     * @param phone the phone to set
     */
    public final void setPhone(String phone) {
        setProperty(UserModel.PROP_PHONE, phone);
    }

    /**
     * @return the postalAddress
     */
    public final String getPostalAddress() {
        return (String) getProperty(UserModel.PROP_POSTAL_ADDRESS);
    }

    /**
     * @param postalAddress the postalAddress to set
     */
    public final void setPostalAddress(String postalAddress) {
        setProperty(UserModel.PROP_POSTAL_ADDRESS, postalAddress);
    }

    /**
     * @return the title
     */
    public final String getTitle() {
        return (String) getProperty(UserModel.PROP_TITLE);
    }

    /**
     * @param title the title to set
     */
    public final void setTitle(String title) {
        setProperty(UserModel.PROP_TITLE, title);
    }

    /**
     * @return the url
     */
    public final String getUrl() {
        return (String) getProperty(UserModel.PROP_URL);
    }

    /**
     * @param url the url to set
     */
    public final void setUrl(String url) {
        setProperty(UserModel.PROP_URL, url);
    }

    //-----------------
    //--  Properties located under circabc aspect that should be moved in the preferences

    /**
     * @return the globalNotification TODO set notification status as a preference!
     */
    public final Boolean getGlobalNotification() {
        final Boolean notfication = (Boolean) getProperty(UserModel.PROP_GLOBAL_NOTIFICATION);

        if (notfication == null) {
            return Boolean.TRUE;
        } else {
            return notfication;
        }
    }

    /**
     * @param globalNotification the globalNotification to set TODO set notification status as a
     *                           preference!
     */
    public final void setGlobalNotification(Boolean globalNotification) {
        setProperty(UserModel.PROP_GLOBAL_NOTIFICATION, globalNotification);
    }

    /**
     * @return the visibility TODO set visibility as a preference!
     */
    public final Boolean getVisibility() {
        final Boolean visible = (Boolean) getProperty(UserModel.PROP_VISISBILITY);

        if (visible == null) {
            return Boolean.FALSE;
        } else {
            return visible;
        }
    }

    /**
     * @param visibility the visibility to set TODO set visibility as a preference!
     */
    public final void setVisibility(Boolean visibility) {
        setProperty(UserModel.PROP_VISISBILITY, visibility);
    }

    //-----------------
    //--  Preferences

    /**
     * @return the userInterfaceLanguage
     */
    public final String getUserInterfaceLanguage() {
        final String lang = (String) getPreference(UserService.PREF_INTERFACE_LANGUAGE);

        if (lang == null || lang.length() == 0) {
            return Locale.ENGLISH.getLanguage();
        } else {
            return lang;
        }
    }

    /**
     * @param userInterfaceLanguage the userInterfaceLanguage to set
     */
    public final void setUserInterfaceLanguage(String userInterfaceLanguage) {
        setPreference(UserService.PREF_INTERFACE_LANGUAGE, userInterfaceLanguage);
    }

    /**
     * @return the contentFilterLanguage
     */
    public final Locale getContentFilterLanguage() {
        return (Locale) getPreference(UserService.PREF_CONTENT_FILTER_LANGUAGE);
    }

    /**
     * @param contentFilterLanguage the contentFilterLanguage to set
     */
    public final void setContentFilterLanguage(Locale contentFilterLanguage) {
        setPreference(UserService.PREF_CONTENT_FILTER_LANGUAGE, contentFilterLanguage);
    }

    /**
     * @return the signature
     */
    public final String getSignature() {
        return (String) getPreference(UserService.PREF_SIGNATURE);
    }

    /**
     * @param signature the signature to set
     */
    public final void setSignature(String signature) {
        setPreference(UserService.PREF_SIGNATURE, signature);
    }

    /**
     * @return the avatar
     */
    public final NodeRef getAvatar() {
        if (avatar == null) {
            avatar = userDetailSrv.getUserAvatar(getNodeRef());
        }

        return avatar;
    }

    /**
     * @param avatarRef
     */
    public void setAvatar(final NodeRef avatarRef) {
        this.avatar = avatarRef;
    }

    //-- Other methods

    /**
     * @return the updatedPreferences
     */
    public final Map<QName, Serializable> getUpdatedPreferences() {
        return updatedPreferences;
    }

    /**
     * @return the updatedProperties
     */
    public final Map<QName, Serializable> getUpdatedProperties() {
        return updatedProperties;
    }

    /**
     * @return all properties defined on the user ignoring the user updated ones
     */
    public Map<QName, Serializable> getOriginalProperties() {
        if (properties == null) {
            properties = userDetailSrv.getUserProperties(getNodeRef());
        }

        return properties;
    }

    /**
     * @return all preferences defined on the user ignoring the user updated ones
     */
    public Map<QName, Serializable> getOriginalPreferences() {
        if (preferences == null) {
            preferences = userDetailSrv.getUserPreferences(getNodeRef());
        }

        return preferences;
    }

    /**
     * @return The full name of the person. Usually FirstName LastName
     */
    public final String getFullName() {
        final String firstName = getFirstName();
        final String lastName = getLastName();

        if (firstName == null && lastName == null) {
            return getDisplayId();
        } else {
            return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName);
        }
    }

    /**
     * @return The user id used for display. The user identifier entered by user when it authenticate
     * itself if different that the UserName.
     */
    public final String getDisplayId() {
        final String displayName = (String) getProperty(UserModel.PROP_ECAS_USER_NAME);
        if (displayName == null) {
            return (String) getProperty(ContentModel.PROP_USERNAME);
        } else {
            return displayName;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetails#hidePersonalData()
     */
    public boolean isPersonalDataHidden() {
        if (personalDataHidden == null) {
            final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

            if (currentUser == null || currentUser.equals(CircabcConstant.GUEST_AUTHORITY)) {
                personalDataHidden = Boolean.TRUE;
            } else if (currentUser.equals(getUserName())) {
                personalDataHidden = Boolean.FALSE;
            } else {
                personalDataHidden = !getVisibility();
            }
        }

        return personalDataHidden;
    }

    //--  Utils

    private Serializable getProperty(final QName qname) {
        return getProperty(qname, true);
    }

    private Serializable getProperty(final QName qname, final boolean checkUpdated) {
        if (checkUpdated && updatedProperties != null && updatedProperties.containsKey(qname)) {
            // the property has been updated but can be null.
            return updatedProperties.get(qname);
        } else {
            return getOriginalProperties().get(qname);
        }
    }

    private Serializable getPreference(final QName qname) {
        return getPreference(qname, true);
    }

    private Serializable getPreference(final QName qname, final boolean checkUpdated) {
        if (getOriginalPreferences() == null) {
            return null;
        } else {
            if (checkUpdated && updatedPreferences != null && updatedPreferences.containsKey(qname)) {
                // the preference has been updated but can be null.
                return updatedPreferences.get(qname);
            } else {
                return getOriginalPreferences().get(qname);
            }
        }
    }

    private void setProperty(final QName qname, final Serializable value) {
        if (updatedProperties == null) {
            updatedProperties = new HashMap<>(12);
        }

        final Serializable originalValue = getProperty(qname, false);

        if (EqualsHelper.nullSafeEquals(originalValue, value) == false) {
            updatedProperties.put(qname, value);
        } else {
            updatedProperties.remove(qname);
        }
    }

    private void setPreference(final QName qname, final Serializable value) {
        if (updatedPreferences == null) {
            updatedPreferences = new HashMap<>(12);
        }

        final Serializable originalValue = getPreference(qname, false);

        if (EqualsHelper.nullSafeEquals(originalValue, value) == false) {
            updatedPreferences.put(qname, value);
        } else {
            updatedPreferences.remove(qname);
        }
    }
}
