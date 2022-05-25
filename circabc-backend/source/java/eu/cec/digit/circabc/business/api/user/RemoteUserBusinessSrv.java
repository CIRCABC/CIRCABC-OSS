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


/**
 * Business service to manage remote management of users.
 * <p>
 * <b>The remote management can be disabled.<b> If not, it's usually a ldap server.
 * </p>
 *
 * @author Yanick Pignot
 * @see RemoteUserBusinessSrv#isRemoteManagementAvailable()
 */
public interface RemoteUserBusinessSrv {

    /**
     * Return if the current instanllation supports remote user managemet
     */
    boolean isRemoteManagementAvailable();

    /**
     * Reload user details from the remote user manager server.
     *
     * <b>Only the argument will be reloaded. To persist it, user UserDetailsBusinessSrv.updateUserDetails</b>
     *
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateUserDetails
     */
    void reloadDetails(final UserDetails userDetails);


    /**
     * Return if the given username is found in the remote server manager
     *
     * @param userId The user id of the user to query
     */
    boolean userExists(final String userId);

}
