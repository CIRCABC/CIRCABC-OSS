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
package eu.cec.digit.circabc.business.api.user;

import eu.cec.digit.circabc.business.acl.AclAwareWrapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * Encapsulate all details of a user in a RW mode. (properties / preferences / configuration)
 *
 * @author Yanick Pignot
 */
public interface UserDetails extends AclAwareWrapper {

    //-----------------
    //--  Mandatory values (identifiers)

    /**
     * @return the personRef
     */
    NodeRef getNodeRef();

    /**
     * @return the personRef
     */
    String getUserName();

    /**
     * @return the if the user is created in the repository
     */
    boolean isUserCreated();

    //-----------------
    //--  Properties of Alfresco user model

    /**
     * @return the email
     */
    String getEmail();

    /**
     * @param email the email to set
     */
    void setEmail(String email);

    /**
     * @return the firstName
     */
    String getFirstName();

    /**
     * @param firstName the firstName to set
     */
    void setFirstName(String firstName);

    /**
     * @return the lastName
     */
    String getLastName();

    /**
     * @param lastName the lastName to set
     */
    void setLastName(String lastName);

    //-----------------
    //--  Properties of circabc user aspect

    /**
     * @return the description
     */
    String getDescription();

    /**
     * @param description the description to set
     */
    void setDescription(String description);

    /**
     * @return the fax
     */
    String getFax();

    /**
     * @param fax the fax to set
     */
    void setFax(String fax);

    /**
     * @return the organisation
     */
    String getOrganisation();

    /**
     * @param organisation the organisation to set
     */
    void setOrganisation(String organisation);

    /**
     * @return the phone
     */
    String getPhone();

    /**
     * @param phone the phone to set
     */
    void setPhone(String phone);

    /**
     * @return the postalAddress
     */
    String getPostalAddress();

    /**
     * @param postalAddress the postalAddress to set
     */
    void setPostalAddress(String postalAddress);

    /**
     * @return the title
     */
    String getTitle();

    /**
     * @param title the title to set
     */
    void setTitle(String title);

    /**
     * @return the url
     */
    String getUrl();

    /**
     * @param url the url to set
     */
    void setUrl(String url);

    //-----------------
    //--  Properties located under circabc aspect that should be moved in the preferences

    /**
     * @return the globalNotification
     */
    Boolean getGlobalNotification();

    /**
     * @param globalNotification the globalNotification to set
     */
    void setGlobalNotification(Boolean globalNotification);

    /**
     * @return the visibility
     */
    Boolean getVisibility();

    /**
     * @param visibility the visibility to set
     */
    void setVisibility(Boolean visibility);

    //-----------------
    //--  Preferences

    /**
     * @return the userInterfaceLanguage
     */
    String getUserInterfaceLanguage();

    /**
     * @param userInterfaceLanguage the userInterfaceLanguage to set
     */
    void setUserInterfaceLanguage(String userInterfaceLanguage);

    /**
     * @return the contentFilterLanguage
     */
    Locale getContentFilterLanguage();

    /**
     * @param contentFilterLanguage the contentFilterLanguage to set
     */
    void setContentFilterLanguage(final Locale contentFilterLanguage);

    /**
     * @return the signature
     */
    String getSignature();

    /**
     * @param signature the signature to set
     */
    void setSignature(String signature);

    /**
     * @return the avatar
     */
    NodeRef getAvatar();

    /**
     * @param avatarRef
     */
    void setAvatar(NodeRef avatarRef);

    //-- Other methods

    /**
     * @return all preferences updated by the user
     */
    Map<QName, Serializable> getUpdatedPreferences();

    /**
     * @return all properties updated by the user
     */
    Map<QName, Serializable> getUpdatedProperties();

    /**
     * @return all properties defined on the user ignoring the user updated ones
     */
    Map<QName, Serializable> getOriginalProperties();

    /**
     * @return all preferences defined on the user ignoring the user updated ones
     */
    Map<QName, Serializable> getOriginalPreferences();

    /**
     * The full name of the person.
     *
     * @return Usually FirstName LastName
     */
    String getFullName();

    /**
     * The user identifier entered by user when it authenticate itself if different that the
     * UserName.
     *
     * @return The user id used for display.
     */
    String getDisplayId();

    /**
     * Some properties must be hidden to <b>other</b> users if the user specify its visibility to
     * false (or nothing)
     *
     * @return Current user != getUsername && getVisibility != TRUE
     */
    boolean isPersonalDataHidden();

}
