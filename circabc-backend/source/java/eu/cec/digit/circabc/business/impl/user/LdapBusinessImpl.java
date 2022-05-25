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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.business.api.BusinessRuntimeExpection;
import eu.cec.digit.circabc.business.api.user.RemoteUserBusinessSrv;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.impl.AssertUtils;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the Business service that manage ldap common tasks.
 *
 * @author Yanick Pignot
 */
public class LdapBusinessImpl implements RemoteUserBusinessSrv {

    private static final String MSG_CANNOT_RELOAD = "business_error_cannot_reload_user_details";

    private final Log logger = LogFactory.getLog(LdapBusinessImpl.class);

    private boolean ldapAvailable = false;
    private UserService userService;

    //--------------
    //-- public methods


    public boolean isRemoteManagementAvailable() {
        return ldapAvailable && CircabcConfig.ENT;
    }

    /**
     * @param ldapAvailable the ldapAvailable to set
     */
    public final void setRemoteManagementAvailable(boolean ldapAvailable) {
        this.ldapAvailable = ldapAvailable;
    }

    public void reloadDetails(final UserDetails userDetails) {
        AssertUtils.notNull(userDetails);

        final CircabcUserDataBean ldapUserDetail = userService
                .getLDAPUserDataByUid(userDetails.getUserName());

        if (ldapUserDetail != null) {
            if (ldapUserDetail.getFirstName() != null) {
                userDetails.setFirstName(ldapUserDetail.getFirstName());
            }
            if (ldapUserDetail.getLastName() != null) {
                userDetails.setLastName(ldapUserDetail.getLastName());
            }
            if (ldapUserDetail.getEmail() != null) {
                userDetails.setEmail(ldapUserDetail.getEmail());
            }

            if (ldapUserDetail.getTitle() != null) {
                userDetails.setTitle(ldapUserDetail.getTitle());
            }
            if (ldapUserDetail.getOrgdepnumber() != null) {
                userDetails.setOrganisation(ldapUserDetail.getOrgdepnumber());
            }
            if (ldapUserDetail.getPhone() != null) {
                userDetails.setPhone(ldapUserDetail.getPhone());
            }
            if (ldapUserDetail.getPostalAddress() != null) {
                userDetails.setPostalAddress(ldapUserDetail.getPostalAddress());
            }
            if (ldapUserDetail.getFax() != null) {
                userDetails.setFax(ldapUserDetail.getFax());
            }
            if (ldapUserDetail.getDescription() != null) {
                userDetails.setDescription(ldapUserDetail.getDescription());
            }
            if (ldapUserDetail.getURL() != null) {
                userDetails.setUrl(ldapUserDetail.getURL());
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to found " + userDetails.getUserName() + " in the ldap.");
            }

            throw new BusinessRuntimeExpection(MSG_CANNOT_RELOAD);
        }
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

    public boolean userExists(final String userId) {
        if (userId == null || userId.length() < 1) {
            return false;
        } else {
            return userService.getLDAPUserDataByUid(userId) != null;
        }
    }

    /**
     * @param userService the userService to set
     */
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

}
