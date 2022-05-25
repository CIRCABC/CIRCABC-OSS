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
package eu.cec.digit.circabc.business.api;

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
import eu.cec.digit.circabc.service.namespace.CircabcNameSpaceService;
import org.alfresco.service.namespace.QName;


/**
 * This interface represents the registry of public Business Services.
 *
 * @author yanick Pignot
 */
public interface BusinessRegistry {

    String BUSINESS_REGISTRY = "businessRegistry";

    /**
     * @see eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv identifier
     */
    QName MAIL_ME_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "MailMeContentBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.content.ContentBusinessSrv identifier
     */
    QName CONTENT_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ContentBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv identifier
     */
    QName COCI_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "CociContentBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv identifier
     */
    QName NAVIGATION_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NavigationBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv identifier
     */
    QName SPACE_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "SpaceBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv identifier
     */
    QName PROPERTIES_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "PropertiesBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.space.DossierBusinessSrv identifier
     */
    QName DOSSIER_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "DossierBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv identifier
     */
    QName LINKS_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "LinksBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.security.ProfileBusinessSrv identifier
     */
    QName PROFILE_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ProfileBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.security.PermissionsBusinessSrv identifier
     */
    QName PERMISSIONS_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "PermissionsBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv identifier
     */
    QName USER_DETAILS_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "UserDetailsBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.user.RemoteUserBusinessSrv identifier
     */
    QName REMOTE_USER_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "RemoteUserBusinessSrv");
    /**
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv identifier
     */
    QName ATTACHEMENT_BUSINESS_SERVICE = QName
            .createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "AttachementBusinessSrv");


    /**
     * @return Mail Me Content Business Srv
     */
    MailMeContentBusinessSrv getMailMeContentBusinessSrv();

    /**
     * @return Content Business Srv
     */
    ContentBusinessSrv getContentBusinessSrv();

    /**
     * @return Coci Content Business Srv
     */
    CociContentBusinessSrv getCociContentBusinessSrv();

    /**
     * @return Navigation Business Srv
     */
    NavigationBusinessSrv getNavigationBusinessSrv();

    /**
     * @return Space Business Srv
     */
    SpaceBusinessSrv getSpaceBusinessSrv();

    /**
     * @return Properties Business Srv
     */
    PropertiesBusinessSrv getPropertiesBusinessSrv();

    /**
     * @return Dossier Business Srv
     */
    DossierBusinessSrv getDossierBusinessSrv();

    /**
     * @return Links Business Srv
     */
    LinksBusinessSrv getLinksBusinessSrv();

    /**
     * @return Permissions Business Srv
     */
    PermissionsBusinessSrv getPermissionsBusinessSrv();

    /**
     * @return Profile Business Srv
     */
    ProfileBusinessSrv getProfileBusinessSrv();

    /**
     * @return User Details Business Srv
     */
    UserDetailsBusinessSrv getUserDetailsBusinessSrv();

    /**
     * @return Remote User Business Srv
     */
    RemoteUserBusinessSrv getRemoteUserBusinessSrv();

    /**
     * @return Attachement Business Srv
     */
    AttachementBusinessSrv getAttachementBusinessSrv();
}
