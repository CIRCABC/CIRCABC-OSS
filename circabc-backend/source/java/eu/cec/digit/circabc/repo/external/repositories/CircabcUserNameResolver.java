/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.external.repositories;

import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * User name resolver that returns the name of the current logged user by getting it from Alfresco.
 *
 * @author schwerr
 */
public class CircabcUserNameResolver implements UserNameResolver {

    private AuthenticationService authenticationService = null;

    /**
     * @see eu.cec.digit.circabc.repo.external.repositories.UserNameResolver#getUserName()
     */
    @Override
    public String getUserName() {
        return authenticationService.getCurrentUserName();
    }

    /**
     * Sets the value of the authenticationService
     *
     * @param authenticationService the authenticationService to set.
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
