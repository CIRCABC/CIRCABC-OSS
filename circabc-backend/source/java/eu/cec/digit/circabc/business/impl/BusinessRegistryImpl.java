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
package eu.cec.digit.circabc.business.impl;


import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv;
import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.business.api.content.ContentBusinessSrv;
import eu.cec.digit.circabc.business.api.link.LinksBusinessSrv;
import eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv;
import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv;
import eu.cec.digit.circabc.business.api.security.PermissionsBusinessSrv;
import eu.cec.digit.circabc.business.api.security.ProfileBusinessSrv;
import eu.cec.digit.circabc.business.api.space.DossierBusinessSrv;
import eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv;
import eu.cec.digit.circabc.business.api.user.RemoteUserBusinessSrv;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;

/**
 * This interface represents the registry of public Business Services.
 *
 * @author yanick Pignot
 */
public class BusinessRegistryImpl implements BusinessRegistry {

    private CircabcServiceRegistry circabcServiceRegistry;

    /**
     * @return Mail Me Content Business Srv
     */
    public MailMeContentBusinessSrv getMailMeContentBusinessSrv() {
        return (MailMeContentBusinessSrv) this.circabcServiceRegistry
                .getService(MAIL_ME_BUSINESS_SERVICE);
    }

    /**
     * @return Content Business Srv
     */
    public ContentBusinessSrv getContentBusinessSrv() {
        return (ContentBusinessSrv) this.circabcServiceRegistry.getService(CONTENT_BUSINESS_SERVICE);
    }

    /**
     * @return Coci Content Business Srv
     */
    public CociContentBusinessSrv getCociContentBusinessSrv() {
        return (CociContentBusinessSrv) this.circabcServiceRegistry.getService(COCI_BUSINESS_SERVICE);
    }

    /**
     * @return Navigation Business Srv
     */
    public NavigationBusinessSrv getNavigationBusinessSrv() {
        return (NavigationBusinessSrv) this.circabcServiceRegistry
                .getService(NAVIGATION_BUSINESS_SERVICE);
    }

    /**
     * @return Space Business Srv
     */
    public SpaceBusinessSrv getSpaceBusinessSrv() {
        return (SpaceBusinessSrv) this.circabcServiceRegistry.getService(SPACE_BUSINESS_SERVICE);
    }

    /**
     * @return Properties Business Srv
     */
    public PropertiesBusinessSrv getPropertiesBusinessSrv() {
        return (PropertiesBusinessSrv) this.circabcServiceRegistry
                .getService(PROPERTIES_BUSINESS_SERVICE);
    }

    /**
     * @return Dossier Business Srv
     */
    public DossierBusinessSrv getDossierBusinessSrv() {
        return (DossierBusinessSrv) this.circabcServiceRegistry.getService(DOSSIER_BUSINESS_SERVICE);
    }

    /**
     * @return Links Business Srv
     */
    public LinksBusinessSrv getLinksBusinessSrv() {
        return (LinksBusinessSrv) this.circabcServiceRegistry.getService(LINKS_BUSINESS_SERVICE);
    }

    /**
     * @return Permission Business Srv
     */
    public PermissionsBusinessSrv getPermissionsBusinessSrv() {
        return (PermissionsBusinessSrv) this.circabcServiceRegistry
                .getService(PERMISSIONS_BUSINESS_SERVICE);
    }

    /**
     * @return Profile Business Srv
     */
    public ProfileBusinessSrv getProfileBusinessSrv() {
        return (ProfileBusinessSrv) this.circabcServiceRegistry.getService(PROFILE_BUSINESS_SERVICE);
    }

    /**
     * @return User Details Business Srv
     */
    public UserDetailsBusinessSrv getUserDetailsBusinessSrv() {
        return (UserDetailsBusinessSrv) this.circabcServiceRegistry
                .getService(USER_DETAILS_BUSINESS_SERVICE);
    }

    /**
     * @return Remote User Business Srv
     */
    public RemoteUserBusinessSrv getRemoteUserBusinessSrv() {
        return (RemoteUserBusinessSrv) this.circabcServiceRegistry
                .getService(REMOTE_USER_BUSINESS_SERVICE);
    }

    /**
     * @return Attachement Business Srv
     */
    public AttachementBusinessSrv getAttachementBusinessSrv() {
        return (AttachementBusinessSrv) this.circabcServiceRegistry
                .getService(ATTACHEMENT_BUSINESS_SERVICE);
    }

    //--------
    //IOC

    /**
     * @param circabcServiceRegistry the circabcServiceRegistry to set
     */
    public final void setCircabcServiceRegistry(CircabcServiceRegistry circabcServiceRegistry) {
        this.circabcServiceRegistry = circabcServiceRegistry;
    }
}
